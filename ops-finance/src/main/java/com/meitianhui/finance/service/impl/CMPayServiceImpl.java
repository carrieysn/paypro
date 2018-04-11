package com.meitianhui.finance.service.impl;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hisun.iposm.HiiposmUtil;
import com.meitianhui.common.constant.ResultData;
import com.meitianhui.common.exception.BusinessException;
import com.meitianhui.common.exception.SystemException;
import com.meitianhui.common.util.DateUtil;
import com.meitianhui.common.util.FastJsonUtil;
import com.meitianhui.common.util.HttpClientUtil;
import com.meitianhui.common.util.IDUtil;
import com.meitianhui.common.util.PropertiesConfigUtil;
import com.meitianhui.common.util.StringUtil;
import com.meitianhui.finance.constant.Constant;
import com.meitianhui.finance.constant.RspCode;
import com.meitianhui.finance.constant.TradeIDUtil;
import com.meitianhui.finance.controller.FinanceController;
import com.meitianhui.finance.service.CMPayService;
import com.meitianhui.finance.service.TradeService;
import com.meitianhui.finance.util.CMPayHttpUtil;

@SuppressWarnings("unchecked")
@Service
public class CMPayServiceImpl implements CMPayService {

	private final Logger logger = Logger.getLogger(CMPayServiceImpl.class);

	@Autowired
	private TradeService tradeService;

	// private String url = "http://211.138.236.210/cps/cmpayService";
	private String url = "http://ipos.10086.cn/cps/cmpayService";

	/** 交易等待 **/
	public static String BESTPAY_TRADE_WAIT = "TRADE_WAIT";
	/** 交易成功 **/
	public static String BESTPAY_TRADE_SUCCESS = "TRADE_SUCCESS";
	/** 交易关闭 **/
	public static String BESTPAY_TRADE_CLOSED = "TRADE_CLOSED";

	@Override
	public void barCodePay(Map<String, Object> paramsMap, String scan_flag)
			throws BusinessException, SystemException, Exception {
		String characterSet = "00";
		String notifyUrl = PropertiesConfigUtil.getProperty("cmpay.cmpay_notify_url");
		String merchantId = "";
		if (paramsMap.get("merchantId") == null) {
			merchantId = PropertiesConfigUtil.getProperty("cmpay.cmpay_merchant_id");
		} else {
			merchantId = (String) paramsMap.get("merchantId");
		}
		String requestId = (String) paramsMap.get("requestId");
		String signType = "MD5";
		String type = "CloudQuickPay";
		String version = "2.0.1";
		String amount = (String) paramsMap.get("amount");
		// String amount = "80000";
		String currency = "00";
		String orderDate = DateUtil.getFormatDate("yyyyMMdd");
		// String orderDate = "20480701";
		String orderId = (String) paramsMap.get("orderId");
		String period = "5";
		String periodUnit = "00";
		String productName = (String) paramsMap.get("productName");
		// String productName = "ggggggg";
		String reserved1 = (String) paramsMap.get("reserved1");
		String userToken = (String) paramsMap.get("userToken");
		String pikFlag = "0";
		String oprId = storesIdFind((String) paramsMap.get("member_id"));
		String signKey = PropertiesConfigUtil.getProperty("cmpay.cmpay_sign_key");
		try {
			String signData = characterSet + notifyUrl + merchantId + requestId + signType + type + version + amount
					+ currency + orderDate + orderId + period + periodUnit + productName + reserved1 + userToken
					+ pikFlag + oprId;
			logger.info("和包支付扫码支付签名字串,request ->" + signData);
			HiiposmUtil util = new HiiposmUtil();
			String hmac = util.MD5Sign(signData, signKey);
			// -- 请求报文
			String buf = "hmac=" + hmac + "&characterSet=" + characterSet + "&notifyUrl=" + notifyUrl + "&merchantId="
					+ merchantId + "&requestId=" + requestId + "&signType=" + signType + "&type=" + type + "&version="
					+ version + "&amount=" + amount + "&currency=" + currency + "&orderDate=" + orderDate + "&orderId="
					+ orderId + "&period=" + period + "&periodUnit=" + periodUnit + "&productName=" + productName
					+ "&reserved1=" + reserved1 + "&userToken=" + userToken + "&pikFlag=" + pikFlag + "&oprId=" + oprId;
			logger.info("和包支付扫码支付,请求字串->" + buf);
			// 发送请求
			String resultStr = util.sendAndRecv(url, buf, characterSet);
			logger.info("和包支付扫码支付,返回字串->" + resultStr);
			Map<String, Object> urlParams = CMPayHttpUtil.getUrlParams(resultStr);
			if (!"000000".equals(urlParams.get("returnCode"))) {
				String message = URLDecoder.decode((String)urlParams.get("message"));
				throw new BusinessException("和包支付失败", message);
			}
			Map<String, Object> query_response = new HashMap<>();
			String transStatus = BESTPAY_TRADE_CLOSED;
			// 无论成功与否,都进行交易查询
			if (scan_flag.equals("new2")) {
				// 6次轮询，每次轮询间隔5秒
				transStatus = queryTransStatus(merchantId, TradeIDUtil.getTradeNo(), signType, orderDate, orderId,
						signKey, 6, 10000, query_response);
			} else {
				// TODO 后期废除
				// (10次轮询，每次轮询间隔5秒)
				transStatus = queryTransStatus(merchantId, TradeIDUtil.getTradeNo(), signType, orderDate, orderId,
						signKey, 10, 5000, query_response);
			}

			if (transStatus.equals(BESTPAY_TRADE_SUCCESS)) {
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("transaction_no", orderId);
				tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
				tempMap.put("transaction_body", FastJsonUtil.toJson(query_response));
				tempMap.put("order_type_key", merchantId);
				tradeService.transactionConfirmed(tempMap, new ResultData());
			} else {
				if (scan_flag.equals("new2")) {
					// 后台轮询交易
					asyncLoopQuery(merchantId, requestId, signType, type, version, orderDate, orderId, amount, signKey);
				} else {
					// TODO 2.9.5后的版本废除
					// 交易冲正
					reverse(merchantId, TradeIDUtil.getTradeNo(), signType, requestId, orderDate);
					throw new BusinessException("和包支付异常", "和包支付条码支付失败");
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 后台轮询
	 * 
	 * @Title: asyncLoopQuery
	 * @param out_trade_no
	 * @param merchantId
	 * @param orderNo
	 * @param orderReqNo
	 * @param orderDate
	 * @author tiny
	 */
	private void asyncLoopQuery(final String merchantId, final String requestId, final String signType,
			final String type, final String version, final String orderDate, final String orderId, final String amount,
			final String signKey) {
		FinanceController.threadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Map<String, Object> query_response = new HashMap<String, Object>();
					// 6次轮询，每次轮询间隔5秒
					String transaction_status = queryTransStatus(merchantId, requestId, signType, orderDate, orderId,
							signKey, 6, 10000, query_response);
					// 交易成功,进行交易确认,否则进行交易取消
					if (transaction_status.equals(BESTPAY_TRADE_SUCCESS)) {
						transaction_status = Constant.TRANSACTION_STATUS_CONFIRMED;
					} else {
						transaction_status = Constant.TRANSACTION_STATUS_ERROR;
						// 交易冲正
						// reverse(merchantId, TradeIDUtil.getTradeNo(),
						// signType, requestId, orderDate);
					}
					// 交易确定
					Map<String, Object> tempMap = new HashMap<String, Object>();
					tempMap.put("transaction_no", requestId);
					tempMap.put("transaction_status", transaction_status);
					tempMap.put("transaction_body", FastJsonUtil.toJson(query_response));
					tempMap.put("order_type_key", merchantId);
					tradeService.transactionConfirmed(tempMap, new ResultData());
					if (!"confirmed".equals(transaction_status)) {
						throw new BusinessException("和包支付异常", "和包支付条码支付失败");
					}
				} catch (Exception e) {
					logger.error("和包支付条码支付异常", e);
				}
			}
		});
	}

	/**
	 * 交易结果查询
	 * 
	 * @param query_response
	 * @param k
	 * @param j
	 * @param orderId
	 * @param orderDate
	 * @param version
	 * @param type
	 * @param signType
	 * @param requestId
	 * @param merchantId
	 * 
	 * @param merchantId
	 * @param orderNo
	 * @param orderReqNo
	 * @param orderDate
	 * @param orderId2
	 * @param mac
	 */
	public String queryTransStatus(String merchantId, String requestId, String signType, String orderDate,
			String orderId, String signKey, int qry_num, long sleep_time, Map<String, Object> query_response)
					throws BusinessException, SystemException, Exception {
		String transStatus = BESTPAY_TRADE_WAIT;
		try {
			// 轮询6次
			for (int i = 1; i <= qry_num; i++) {
				logger.info("和包支付-第" + i + "次交易查询");
				transStatus = queryOrder(merchantId, requestId, signType, orderDate, orderId, query_response, signKey);
				if (transStatus.equals("支付完成")) {
					transStatus = BESTPAY_TRADE_SUCCESS;
					break;
				} else if (transStatus.equals("订单关闭")) {
					transStatus = BESTPAY_TRADE_CLOSED;
					break;
				}
				Thread.sleep(sleep_time);
			}
		} catch (Exception e) {
			logger.error("和包支付条码支付查询异常", e);
		}
		return transStatus;
	}

	/**
	 * 订单查询接口
	 * 
	 * @Title: queryOrder
	 * @author tiny
	 * @param query_response
	 * @param orderId
	 * @param orderDate
	 * @param version
	 * @param type
	 * @param signType
	 * @param requestId
	 * @param merchantId
	 * @param signKey
	 */
	public String queryOrder(String merchantId, String requestId, String signType, String orderDate, String orderId,
			Map<String, Object> query_response, String signKey) {
		String status = "等待付款";
		try {
			String characterSet = "00";
			String type = "OrderQueryByYPOS";
			String version = "2.0.0";
			String signData = characterSet + merchantId + requestId + signType + type + version + orderDate + orderId;
			logger.info("和包支付-签名字串,request->" + signData);
			HiiposmUtil util = new HiiposmUtil();
			String hmac = util.MD5Sign(signData, signKey);
			String buf = "characterSet=" + characterSet + "&merchantId=" + merchantId + "&requestId=" + requestId
					+ "&signType=" + signType + "&type=" + type + "&version=" + version + "&orderDate=" + orderDate
					+ "&orderId=" + orderId;
			buf = "hmac=" + hmac + "&" + buf;
			logger.info("和包支付-交易查询,request->" + buf);
			String responseStr = util.sendAndRecv(url, buf, characterSet);
			logger.info("和包支付-交易查询,response->" + responseStr);
			if (responseStr != null) {
				Map<String, Object> responseMap = CMPayHttpUtil.getUrlParams(responseStr);
				if ("000000".equals(responseMap.get("returnCode"))) {
					// 交易状态
					status = (String) responseMap.get("ordSts");
					// status = "订单关闭";
					query_response.putAll(responseMap);
				}
			}
		} catch (Exception e) {
			logger.error("和包支付条码支付查询异常", e);
		}
		return status;
	}

	/**
	 * 冲正
	 * 
	 * @param orderDate
	 * @param requestId
	 * @param version
	 * @param type
	 * @param signType
	 * @param string
	 * @param merchantId
	 * 
	 * @param amount
	 * @param string
	 * @param orderId
	 * @param requestId
	 * @param merchantId
	 * 
	 * @param merchantId
	 * @param orderNo
	 * @param orderReqNo
	 * @param orderDate
	 * @param mac
	 */
	public void reverse(final String merchantId, final String requestId, final String signType, final String orequestId,
			final String oorderDate) throws BusinessException, SystemException, Exception {
		FinanceController.threadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				String characterSet = "00";
				String signKey = PropertiesConfigUtil.getProperty("cmpay.cmpay_sign_key");
				String type = "CloudTxnCancel";
				String version = "2.0.0";
				int i = 3;
				while (i > 0) {
					try {
						Thread.sleep(1000);
						i--;
						// -- 签名
						String signData = characterSet + merchantId + requestId + signType + type + version + orequestId
								+ oorderDate;
						HiiposmUtil util = new HiiposmUtil();
						String hmac = util.MD5Sign(signData, signKey);
						String buf = "hmac=" + hmac + "&characterSet=" + characterSet + "&merchantId=" + merchantId
								+ "&requestId=" + requestId + "&signType=" + signType + "&type=" + type + "&version="
								+ version + "&orequestId=" + orequestId + "&oorderDate=" + oorderDate;
						logger.info("和包支付-冲正签名字串,request->" + buf);
						String responseStr = util.sendAndRecv(url, buf, characterSet);
						logger.info("和包支付-冲正,response->" + responseStr);
						if (responseStr != null) {
							Map<String, Object> responseMap = CMPayHttpUtil.getUrlParams(responseStr);
							if ("000000".equals(responseMap.get("returnCode"))) {
								i = 0;
							}
						}
					} catch (Exception e) {
						logger.info("和包支付-交易冲正异常", e);
					}
				}
			}
		});
	}

	private String storesIdFind(String member_id) {
		String storeId = IDUtil.generateCode(10);
		try {
			Map<String, Object> storesMap = new HashMap<String, Object>();
			storesMap.put("contact_tel", member_id);
			String member_service_url = PropertiesConfigUtil.getProperty("member_service_url");
			Map<String, String> reqParams = new HashMap<String, String>();
			Map<String, String> paramMap = new HashMap<String, String>();
			String resultStr = null;
			reqParams.clear();
			paramMap.clear();
			reqParams.put("service", "member.storeForOrderFind");
			paramMap.put("stores_id", member_id);
			reqParams.put("params", FastJsonUtil.toJson(paramMap));
			resultStr = HttpClientUtil.post(member_service_url, reqParams);
			Map<String, Object> resultMap = FastJsonUtil.jsonToMap(resultStr);
			if (((String) resultMap.get("rsp_code")).equals(RspCode.RESPONSE_SUCC)) {
				Map<String, Object> dateMap = (Map<String, Object>) resultMap.get("data");
				String contact_tel = StringUtil.formatStr(dateMap.get("contact_tel"));
				if (!contact_tel.equals("")) {
					storeId = contact_tel.substring(1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return storeId;
	}

}
