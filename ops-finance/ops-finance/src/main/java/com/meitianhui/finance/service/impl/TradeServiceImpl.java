package com.meitianhui.finance.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.meitianhui.common.constant.ResultData;
import com.meitianhui.common.exception.BusinessException;
import com.meitianhui.common.exception.SystemException;
import com.meitianhui.common.util.BeanConvertUtil;
import com.meitianhui.common.util.DateUtil;
import com.meitianhui.common.util.FastJsonUtil;
import com.meitianhui.common.util.HttpClientUtil;
import com.meitianhui.common.util.IDUtil;
import com.meitianhui.common.util.MoneyUtil;
import com.meitianhui.common.util.PropertiesConfigUtil;
import com.meitianhui.common.util.RedisUtil;
import com.meitianhui.common.util.StringUtil;
import com.meitianhui.common.util.ValidateUtil;
import com.meitianhui.finance.constant.Constant;
import com.meitianhui.finance.constant.RspCode;
import com.meitianhui.finance.constant.TradeIDUtil;
import com.meitianhui.finance.controller.FinanceController;
import com.meitianhui.finance.dao.FinanceDao;
import com.meitianhui.finance.dao.PrepayCardDao;
import com.meitianhui.finance.dao.TransactionDao;
import com.meitianhui.finance.dao.VoucherDao;
import com.meitianhui.finance.entity.FDCashDailyAccountMember;
import com.meitianhui.finance.entity.FDGoldDailyAccountMember;
import com.meitianhui.finance.entity.FDMemberAsset;
import com.meitianhui.finance.entity.FDMemberCashLog;
import com.meitianhui.finance.entity.FDMemberGoldLog;
import com.meitianhui.finance.entity.FDMemberPointLog;
import com.meitianhui.finance.entity.FDMemberVoucherLog;
import com.meitianhui.finance.entity.FDPointDailyAccountMember;
import com.meitianhui.finance.entity.FDTransactions;
import com.meitianhui.finance.entity.FDTransactionsResult;
import com.meitianhui.finance.entity.FDVoucherDailyAccountMember;
import com.meitianhui.finance.entity.FdVoucherToGoldLog;
import com.meitianhui.finance.service.AlipayPayService;
import com.meitianhui.finance.service.BestPayService;
import com.meitianhui.finance.service.FinanceSyncService;
import com.meitianhui.finance.service.NotifyService;
import com.meitianhui.finance.service.TradeService;
import com.meitianhui.finance.service.WechatPayService;

/**
 * 交易服务层
 * 
 * @author Tiny
 *
 */
@SuppressWarnings("unchecked")
@Service
public class TradeServiceImpl implements TradeService {

	private static final Logger logger = Logger.getLogger(TradeServiceImpl.class);

	@Autowired
	public FinanceDao financeDao;
	@Autowired
	public RedisUtil redisUtil;
	@Autowired
	public TransactionDao transactionDao;
	@Autowired
	public NotifyService notifyService;
	@Autowired
	private AlipayPayService alipayPayService;
	@Autowired
	private WechatPayService wechatPayService;
	@Autowired
	private BestPayService bastPayService;
	@Autowired
	private FinanceSyncService financeSyncService;
	@Autowired
	private VoucherDao voucherDao;
	@Autowired
	public PrepayCardDao prepayCardDao;
	/*
	 * @Autowired public MiniAppUtil miniAppUtil;
	 */

	@Override
	public void tradeCodeRegister(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			String security_code = TradeIDUtil.getSecurityCode();
			// 数据缓存到redis中
			paramsMap.put("security_code", security_code);
			redisUtil.setObj(security_code, paramsMap, 120);
			// 返回数据
			Map<String, Object> resultDataMap = new HashMap<String, Object>();
			resultDataMap.put("security_code", security_code);
			result.setResultData(resultDataMap);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void tradeCodeVerify(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			ValidateUtil.validateParams(paramsMap, new String[] { "security_code" });
			String security_code = (String) paramsMap.get("security_code");
			Object obj = redisUtil.getObj(security_code);
			if (obj == null) {
				throw new BusinessException(RspCode.TRADE_CODE_ERROR, "交易码失效或不存在");
			} else {
				redisUtil.del(security_code);
				Map<String, Object> reqMap = (Map<String, Object>) obj;
				result.setResultData(reqMap);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void barCodeCreate(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			ValidateUtil.validateParams(paramsMap, new String[] { "member_id", "auth_code_type" });
			// security_type R(收款码 received) P(付款码Payment)
			String auth_code = TradeIDUtil.getSecurityCode();
			// 数据缓存到redis中
			paramsMap.put("auth_code", auth_code);
			redisUtil.setObj(auth_code, paramsMap, 120);
			// 返回数据
			Map<String, Object> resultDataMap = new HashMap<String, Object>();
			resultDataMap.put("auth_code", auth_code);
			result.setResultData(resultDataMap);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void barCodePay(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			ValidateUtil.validateParams(paramsMap, new String[] { "data_source", "payment_way_key", "auth_code",
					"amount", "member_id", "detail", "out_trade_body" });
			String payment_way_key = (String) paramsMap.get("payment_way_key");
			// 扫码方
			String scan_member = paramsMap.get("member_id") + "";
			paramsMap.remove("member_id");
			if (payment_way_key.equals(Constant.PAYMENT_WAY_10)) {
				// 支付宝扫码支付
				paramsMap.put("out_member_id", Constant.MEMBER_ID_PLATFORM);
				paramsMap.put("in_member_id", scan_member);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_11)) {
				// 微信扫码支付
				paramsMap.put("out_member_id", Constant.MEMBER_ID_PLATFORM);
				paramsMap.put("in_member_id", scan_member);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_05)) {
				// 零钱扫码支付,验证交易码并获取交易码中的会员信息
				String auth_code = (String) paramsMap.get("auth_code");
				Object obj = redisUtil.getObj(auth_code);
				if (obj == null) {
					throw new BusinessException(RspCode.TRADE_CODE_ERROR, "交易码失效或不存在");
				}
				redisUtil.del(auth_code);
				Map<String, Object> reqMap = (Map<String, Object>) obj;
				// 创建人
				String create_member = reqMap.get("member_id") + "";

				// security_type R(收款码 received) P(付款码payment)
				// 判断是收款码还是付款码
				String auth_code_type = reqMap.get("auth_code_type") + "";
				String out_member_id = null;
				String in_member_id = null;
				if (auth_code_type.equals(Constant.SECURITY_TYPE_RECEIVED)) {
					// 创建收款码的人为收款人,扫码的人为付款人
					out_member_id = scan_member;
					in_member_id = create_member;
				} else if (auth_code_type.equals(Constant.SECURITY_TYPE_PAYMENT)) {
					// 创建收款码的人为付款人,扫码的人为收款人
					out_member_id = create_member;
					in_member_id = scan_member;
				} else {
					throw new BusinessException(RspCode.TRADE_CODE_ERROR, "无效交易码");
				}
				paramsMap.put("out_member_id", out_member_id);
				paramsMap.put("in_member_id", in_member_id);
			}
			// 调用余额支付接口
			balancePay(paramsMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 余额充值接口
	 * 
	 * @param map
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void balanceRecharge(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			// out_trade_no, 必传参数
			ValidateUtil.validateParams(paramsMap,
					new String[] { "data_source", "payment_way_key", "amount", "member_id", "detail" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("buyer_id", Constant.MEMBER_ID_MTH);
			paramsMap.put("seller_id", paramsMap.get("member_id"));
			// 余额充值
			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_BALANCERECHARGE);
			paramsMap.put("order_type_key", Constant.ORDER_TYPE_RECHARGE);

			String remark = Constant.DATA_SOURCE_MAP.get(paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get(paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get(paramsMap.get("payment_way_key")) + "|" + paramsMap.get("amount");
			paramsMap.put("remark", remark);
			// 入库
			transactionsCreate(paramsMap, result);
			if (MoneyUtil.moneyComp("0", (String) paramsMap.get("amount"))) {
				throw new BusinessException(RspCode.TRADE_AMOUNT_ERROR, RspCode.MSG.get(RspCode.TRADE_AMOUNT_ERROR));
			}
			// 检测充值方是否存在
			Map<String, Object> queryMap = new HashMap<String, Object>();
			queryMap.put("member_id", paramsMap.get("seller_id"));
			FDMemberAsset buyer = financeDao.selectFDMemberAsset(queryMap);
			if (buyer == null) {
				throw new BusinessException(RspCode.MEMBER_NO_EXIST, RspCode.MSG.get(RspCode.MEMBER_NO_EXIST));
			}
			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			String transaction_no = (String) resultMap.get("transaction_no");
			String payment_way_key = paramsMap.get("payment_way_key") + "";
			String data_source = paramsMap.get("data_source") + "";
			if (payment_way_key.equals(Constant.PAYMENT_WAY_01)) {
				Map<String, String> alipayMap = new HashMap<String, String>();
				String total_fee = paramsMap.get("amount") + "";
				String subject = paramsMap.get("detail") + "";
				Map<String, Object> reqMap = new HashMap<String, Object>();
				reqMap.put("out_trade_no", transaction_no);
				reqMap.put("amount", total_fee);
				reqMap.put("subject", subject);
				if (data_source.equals(Constant.DATA_SOURCE_SJLY_01)) {
					alipayMap = alipayPayService.consumerAlipayInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_02)) {
					alipayMap = alipayPayService.storeAlipayInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_12)) {
					alipayMap = alipayPayService.shumeAlipayInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_14)) {
					alipayMap = alipayPayService.cashierAlipayInfoGet(reqMap);
				} else {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "支付宝支付暂未开通");
				}
				alipayMap.put("transaction_no", transaction_no);
				result.setResultData(alipayMap);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_02)) {
				Map<String, Object> wechatInfoMap = new HashMap<String, Object>();
				Map<String, Object> reqMap = new HashMap<String, Object>();
				reqMap.put("out_trade_no", transaction_no);
				reqMap.put("amount", paramsMap.get("amount") + "");
				reqMap.put("body", paramsMap.get("detail") + "");
				reqMap.put("spbill_create_ip", "127.0.0.1");
				if (data_source.equals(Constant.DATA_SOURCE_SJLY_01)) {
					wechatInfoMap = wechatPayService.consumerWechatInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_02)) {
					wechatInfoMap = wechatPayService.storeWechatInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_12)) {
					wechatInfoMap = wechatPayService.shumeWechatInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_14)) {
					wechatInfoMap = wechatPayService.cashierWechatInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_17)) {
					reqMap.put("openid", paramsMap.get("openid"));
					wechatInfoMap = wechatPayService.miniAppWechatInfoGet(reqMap);
				}else {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "微信支付暂未开通");
				}
				wechatInfoMap.put("transaction_no", transaction_no);
				result.setResultData(wechatInfoMap);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 余额支付 账户余额、礼券余额
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void balancePay(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		String transaction_no = "";
		try {
			// out_trade_no, 必传参数
			ValidateUtil.validateParams(paramsMap, new String[] { "data_source", "payment_way_key", "amount",
					"out_member_id", "in_member_id", "detail" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("buyer_id", paramsMap.get("out_member_id"));
			paramsMap.put("seller_id", paramsMap.get("in_member_id"));
			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			// 零钱支付
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_BALANCEPAY);
			String remark = Constant.DATA_SOURCE_MAP.get(paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get(paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get(paramsMap.get("payment_way_key")) + "|" + paramsMap.get("amount");
			paramsMap.put("remark", remark);

			// 将交易请求到交易表中
			transactionsCreate(paramsMap, result);
			// 校验交易对象
			buyerSellerValidate(paramsMap.get("buyer_id") + "", paramsMap.get("seller_id") + "");
			if (MoneyUtil.moneyComp("0", (String) paramsMap.get("amount"))) {
				throw new BusinessException(RspCode.TRADE_AMOUNT_ERROR, RspCode.MSG.get(RspCode.TRADE_AMOUNT_ERROR));
			}

			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			transaction_no = (String) resultMap.get("transaction_no");
			// 数据来源
			String data_source = (String) paramsMap.get("data_source");
			// 支付方式
			String payment_way_key = (String) paramsMap.get("payment_way_key");
			boolean flag = true;
			if (payment_way_key.equals(Constant.PAYMENT_WAY_05)) {
				// 零钱
				financeValidate(paramsMap.get("buyer_id") + "", Constant.PAYMENT_WAY_05, paramsMap.get("amount") + "");
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_06)) {
				// 礼券
				financeValidate(paramsMap.get("buyer_id") + "", Constant.PAYMENT_WAY_06, paramsMap.get("amount") + "");
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_08)) {
				// 金币
				financeValidate(paramsMap.get("buyer_id") + "", Constant.PAYMENT_WAY_08, paramsMap.get("amount") + "");
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_09)) {
				// 积分 丁忍2017-09-19新增
				financeValidate(paramsMap.get("buyer_id") + "", Constant.PAYMENT_WAY_09, paramsMap.get("amount") + "");
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_10)) {
				ValidateUtil.validateParams(paramsMap, new String[] { "auth_code" });
				if (!data_source.equals(Constant.DATA_SOURCE_SJLY_02)
						&& !data_source.equals(Constant.DATA_SOURCE_SJLY_04)
						&& !data_source.equals(Constant.DATA_SOURCE_SJLY_14)
						&& !data_source.equals(Constant.DATA_SOURCE_SJLY_15)) {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "支付宝扫码支付功能暂未开通");
				}
				flag = false;
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("out_trade_no", transaction_no);
				tempMap.put("total_amount", paramsMap.get("amount") + "");
				tempMap.put("auth_code", paramsMap.get("auth_code") + "");
				tempMap.put("subject", paramsMap.get("detail") + "");
				// TODO 后期去掉
				String scan_flag = StringUtil.formatStr(paramsMap.get("scan_flag"));
				alipayPayService.barCodePay(tempMap, scan_flag);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_11)) {
				flag = false;
				ValidateUtil.validateParams(paramsMap, new String[] { "auth_code" });
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("out_trade_no", transaction_no);
				tempMap.put("total_fee", paramsMap.get("amount") + "");
				tempMap.put("auth_code", paramsMap.get("auth_code") + "");
				tempMap.put("body", paramsMap.get("detail") + "");
				String type = "";
				String app_key = "";
				String app_id = "";
				String mch_id = "";
				String cert_local_path = "";
				String cert_password = "";
				if (data_source.equals(Constant.DATA_SOURCE_SJLY_02)) {
					type = "店东助手";
					app_key = PropertiesConfigUtil.getProperty("wechat.store_app_key");
					app_id = PropertiesConfigUtil.getProperty("wechat.store_app_id");
					mch_id = PropertiesConfigUtil.getProperty("wechat.store_mch_id");
					cert_local_path = PropertiesConfigUtil.getProperty("wechat.store_cert_local_path");
					cert_password = PropertiesConfigUtil.getProperty("wechat.store_cert_password");
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_04)) {
					type = "鸿商收银";
					app_key = PropertiesConfigUtil.getProperty("wechat.store_app_key");
					app_id = PropertiesConfigUtil.getProperty("wechat.store_app_id");
					mch_id = PropertiesConfigUtil.getProperty("wechat.store_mch_id");
					cert_local_path = PropertiesConfigUtil.getProperty("wechat.store_cert_local_path");
					cert_password = PropertiesConfigUtil.getProperty("wechat.store_cert_password");
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_14)) {
					type = "惠点收银";
					app_key = PropertiesConfigUtil.getProperty("wechat.cashier_app_key");
					app_id = PropertiesConfigUtil.getProperty("wechat.cashier_app_id");
					mch_id = PropertiesConfigUtil.getProperty("wechat.cashier_mch_id");
					cert_local_path = PropertiesConfigUtil.getProperty("wechat.cashier_cert_local_path");
					cert_password = PropertiesConfigUtil.getProperty("wechat.cashier_cert_password");
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_15)) {
					type = "惠驿哥";
					app_key = PropertiesConfigUtil.getProperty("wechat.hyg_app_key");
					app_id = PropertiesConfigUtil.getProperty("wechat.hyg_app_id");
					mch_id = PropertiesConfigUtil.getProperty("wechat.hyg_mch_id");
					cert_local_path = PropertiesConfigUtil.getProperty("wechat.hyg_cert_local_path");
					cert_password = PropertiesConfigUtil.getProperty("wechat.hyg_cert_password");
				} else {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "微信扫码支付功能暂未开通");
				}
				// TODO 后期去掉
				String scan_flag = StringUtil.formatStr(paramsMap.get("scan_flag"));
				wechatPayService.barCodePay(type, app_key, app_id, mch_id, cert_local_path, cert_password, tempMap,
						scan_flag);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_14)) {
				flag = false;
				ValidateUtil.validateParams(paramsMap, new String[] { "auth_code" });
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("orderNo", transaction_no);
				tempMap.put("orderReqNo", transaction_no);
				tempMap.put("amount",
						MoneyUtil.moneyMulOfNotPoint(paramsMap.get("amount").toString(), String.valueOf(100)));
				tempMap.put("barcode", paramsMap.get("auth_code") + "");
				tempMap.put("attach", paramsMap.get("detail") + "");
				tempMap.put("member_id", paramsMap.get("seller_id") + "");
				// TODO 后期去掉
				String scan_flag = StringUtil.formatStr(paramsMap.get("scan_flag"));
				bastPayService.barCodePay(tempMap, scan_flag);
			} else {
				throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "支付方式不存在");
			}
			if (flag) {
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("transaction_no", transaction_no);
				tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
				result.setResultData(new ResultData());
				transactionConfirmed(tempMap, result);
			}
			// 记录亲情卡的交易记录
			transPrepayCardAdd(paramsMap, transaction_no);

			// 增加见面礼逻辑
			meetGiftAdd(paramsMap.get("buyer_id") + "", paramsMap.get("seller_id") + "");
		} catch (BusinessException e) {
			fDTransactionLogCreate(transaction_no, "余额支付交易失败," + e.getMsg());
			throw e;
		} catch (SystemException e) {
			fDTransactionErrorLogCreate(transaction_no, "余额支付交易异常," + e.getMsg());
			throw e;
		} catch (Exception e) {
			fDTransactionErrorLogCreate(transaction_no, "余额支付交易异常," + e.getMessage());
			throw e;
		}
	}

	/**
	 * 订单支付
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void orderPay(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		String transaction_no = "";
		try {
			// out_trade_no, 必传参数
			ValidateUtil.validateParams(paramsMap, new String[] { "data_source", "payment_way_key", "amount",
					"out_member_id", "in_member_id", "detail" });

			String data_source = (String) paramsMap.get("data_source");
			if (null == Constant.DATA_SOURCE_MAP.get(data_source)) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("buyer_id", paramsMap.get("out_member_id"));
			paramsMap.put("seller_id", paramsMap.get("in_member_id"));
			// 支付方式
			String payment_way_key = (String) paramsMap.get("payment_way_key");

			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_ORDERPAY);

			String remark = Constant.DATA_SOURCE_MAP.get(paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get(paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get(paramsMap.get("payment_way_key")) + "|" + paramsMap.get("amount");
			paramsMap.put("remark", remark);
			transactionsCreate(paramsMap, result);
			if (MoneyUtil.moneyComp("0", (String) paramsMap.get("amount"))) {
				throw new BusinessException(RspCode.TRADE_AMOUNT_ERROR, RspCode.MSG.get(RspCode.TRADE_AMOUNT_ERROR));
			}
			// 验证订单金额
			orderAmountValidate(paramsMap);
			// 校验交易对象
			buyerSellerValidate(paramsMap.get("buyer_id") + "", paramsMap.get("seller_id") + "");

			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			transaction_no = (String) resultMap.get("transaction_no");

			boolean flag = true;
			if (payment_way_key.equals(Constant.PAYMENT_WAY_01)) {
				flag = false;
				String total_fee = paramsMap.get("amount") + "";
				String subject = paramsMap.get("detail") + "";
				Map<String, String> alipayMap = new HashMap<String, String>();
				Map<String, Object> reqMap = new HashMap<String, Object>();
				reqMap.put("out_trade_no", transaction_no);
				reqMap.put("amount", total_fee);
				reqMap.put("subject", subject);
				// 支付宝
				if (data_source.equals(Constant.DATA_SOURCE_SJLY_01)) {
					alipayMap = alipayPayService.consumerAlipayInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_02)) {
					alipayMap = alipayPayService.storeAlipayInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_12)) {
					alipayMap = alipayPayService.shumeAlipayInfoGet(reqMap);
				} else {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "支付宝支付暂未开通");
				}
				alipayMap.put("transaction_no", transaction_no);
				result.setResultData(alipayMap);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_02)) {
				flag = false;
				Map<String, Object> wechatInfoMap = new HashMap<String, Object>();
				Map<String, Object> reqMap = new HashMap<String, Object>();
				reqMap.put("out_trade_no", transaction_no);
				reqMap.put("amount", paramsMap.get("amount") + "");
				reqMap.put("body", paramsMap.get("detail") + "");
				reqMap.put("spbill_create_ip", "127.0.0.1");
				// 微信
				if (data_source.equals(Constant.DATA_SOURCE_SJLY_01)) {
					wechatInfoMap = wechatPayService.consumerWechatInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_02)) {
					wechatInfoMap = wechatPayService.storeWechatInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_12)) {
					wechatInfoMap = wechatPayService.shumeWechatInfoGet(reqMap);
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_17)) {
					reqMap.put("openid", paramsMap.get("openid"));
					wechatInfoMap = wechatPayService.miniAppWechatInfoGet(reqMap);
				} else {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "微信支付暂未开通");
				}
				wechatInfoMap.put("transaction_no", transaction_no);
				result.setResultData(wechatInfoMap);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_04)) {
				return;
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_05)) {
				// 零钱
				financeValidate(paramsMap.get("buyer_id") + "", Constant.PAYMENT_WAY_05, paramsMap.get("amount") + "");
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_07)) {
				// 亲情卡支付
				financeValidate(paramsMap.get("buyer_id") + "", Constant.PAYMENT_WAY_05, paramsMap.get("amount") + "");
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_08)) {
				// 金币
				financeValidate(paramsMap.get("buyer_id") + "", Constant.PAYMENT_WAY_08, paramsMap.get("amount") + "");
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_09)) {
				// 积分 丁忍2017-09-14新增
				financeValidate(paramsMap.get("buyer_id") + "", Constant.PAYMENT_WAY_09, paramsMap.get("amount") + "");
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_15)) {
				flag = false;
				// 惠易定wap支付
				if (data_source.equals(Constant.DATA_SOURCE_SJLY_06)
						|| data_source.equals(Constant.DATA_SOURCE_SJLY_13)) {
					Map<String, Object> biz_content = new HashMap<String, Object>();
					biz_content.put("return_url", paramsMap.get("return_url"));
					biz_content.put("subject", paramsMap.get("detail"));
					biz_content.put("body", paramsMap.get("detail"));
					biz_content.put("out_trade_no", transaction_no);
					biz_content.put("timeout_express", paramsMap.get("timeout"));
					biz_content.put("total_fee", paramsMap.get("amount") + "");
					biz_content.put("extra_common_param", paramsMap.get("extra_params"));
					alipayPayService.hydAliPayWapPay(biz_content, result);
				} else {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "支付宝支付暂未开通");
				}
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_16)) {
				flag = false;
				// 惠易定网页支付
				if (data_source.equals(Constant.DATA_SOURCE_SJLY_06)
						|| data_source.equals(Constant.DATA_SOURCE_SJLY_13)) {
					Map<String, Object> biz_content = new HashMap<String, Object>();
					biz_content.put("return_url", paramsMap.get("return_url"));
					biz_content.put("subject", paramsMap.get("detail"));
					biz_content.put("body", paramsMap.get("detail"));
					biz_content.put("out_trade_no", transaction_no);
					biz_content.put("timeout_express", paramsMap.get("timeout"));
					biz_content.put("total_fee", paramsMap.get("amount") + "");
					biz_content.put("extra_common_param", paramsMap.get("extra_params"));
					alipayPayService.hydAliPayPcPay(biz_content, result);
				} else {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "支付宝支付暂未开通");
				}
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_17)) {
				flag = false;
				// 微信H5网页支付
				if (data_source.equals(Constant.DATA_SOURCE_SJLY_13)) {
					Map<String, Object> wechatInfoMap = new HashMap<String, Object>();
					Map<String, Object> reqMap = new HashMap<String, Object>();
					reqMap.put("out_trade_no", transaction_no);
					reqMap.put("amount", paramsMap.get("amount") + "");
					reqMap.put("body", paramsMap.get("detail") + "");
					reqMap.put("spbill_create_ip", "127.0.0.1");
					reqMap.put("time_expire", paramsMap.get("time_expire") + "");
					wechatInfoMap = wechatPayService.huidianWechatH5Pay(reqMap);
					result.setResultData(wechatInfoMap);
				} else {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "微信H5网页支付暂未开通");
				}
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_18)) {
				flag = false;
				// 惠易定PC网页扫码支付
				if (data_source.equals(Constant.DATA_SOURCE_SJLY_06)
						|| data_source.equals(Constant.DATA_SOURCE_SJLY_13)) {
					Map<String, Object> wechatInfoMap = new HashMap<String, Object>();
					Map<String, Object> reqMap = new HashMap<String, Object>();
					reqMap.put("out_trade_no", transaction_no);
					reqMap.put("amount", paramsMap.get("amount") + "");
					reqMap.put("body", paramsMap.get("detail") + "");
					reqMap.put("spbill_create_ip", "127.0.0.1");
					reqMap.put("time_expire", paramsMap.get("time_expire") + "");
					wechatInfoMap = wechatPayService.huidianWechatPcBarCodePay(reqMap);
					result.setResultData(wechatInfoMap);
				} else {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "微信PC支付暂未开通");
				}
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_20)) {
				flag = false;
				ValidateUtil.validateParams(paramsMap, new String[] { "timeout" });
				Map<String, Object> reqMap = new HashMap<String, Object>();
				reqMap.put("out_trade_no", transaction_no);
				reqMap.put("total_amount", paramsMap.get("amount") + "");
				reqMap.put("subject", paramsMap.get("detail") + "");
				reqMap.put("timeout_express", paramsMap.get("timeout") + "");
				String qr_code = alipayPayService.scanCodePay(reqMap);
				Map<String, Object> alipayInfoMap = new HashMap<String, Object>();
				alipayInfoMap.put("qr_code", qr_code);
				result.setResultData(alipayInfoMap);
			} else {
				throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "支付方式不存在");
			}
			if (flag) {
				// 支付结果确定
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("transaction_no", transaction_no);
				tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
				result.setResultData(new ResultData());
				transactionConfirmed(tempMap, result);
			}
			// 记录亲情卡的交易记录
			String card_no = StringUtil.formatStr(paramsMap.get("card_no"));
			if (!StringUtils.isEmpty(card_no)) {
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("member_id", paramsMap.get("seller_id"));
				FDMemberAsset seller = financeDao.selectFDMemberAsset(tempMap);
				Date date = new Date();
				tempMap.clear();
				tempMap.put("transaction_id", IDUtil.getUUID());
				tempMap.put("transaction_no", transaction_no);
				tempMap.put("member_id", paramsMap.get("buyer_id"));
				tempMap.put("transaction_member_type", seller.getMember_type_key());
				tempMap.put("transaction_member_id", seller.getMember_id());
				tempMap.put("card_no", card_no);
				tempMap.put("business_type_key", Constant.BUSINESS_TYPE_ORDERPAY);
				tempMap.put("payment_way_key", paramsMap.get("payment_way_key"));
				tempMap.put("transaction_date", date);
				tempMap.put("detail", paramsMap.get("detail"));
				tempMap.put("amount", "-" + paramsMap.get("amount"));
				tempMap.put("modified_date", date);
				tempMap.put("created_date", date);
				tempMap.put("remark", paramsMap.get("remark"));
				prepayCardDao.insertTransPrepayCard(tempMap);
			}
		} catch (BusinessException e) {
			fDTransactionLogCreate(transaction_no, "订单支付交易失败," + e.getMsg());
			throw e;
		} catch (SystemException e) {
			fDTransactionErrorLogCreate(transaction_no, "订单支付交易异常," + e.getMsg());
			throw e;
		} catch (Exception e) {
			fDTransactionErrorLogCreate(transaction_no, "订单支付交易异常," + e.getMessage());
			throw e;
		}
	}

	/**
	 * 订单赠送 买家是消费者,卖家是商户
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void orderReward(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		String transaction_no = "";
		try {
			ValidateUtil.validateParams(paramsMap, new String[] { "data_source", "payment_way_key", "amount",
					"out_member_id", "in_member_id", "detail", "out_trade_no" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("buyer_id", paramsMap.get("out_member_id"));
			paramsMap.put("seller_id", paramsMap.get("in_member_id"));
			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			// 订单赠送
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_ORDERREWARD);

			String remark = Constant.DATA_SOURCE_MAP.get(paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get(paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get(paramsMap.get("payment_way_key")) + "|" + paramsMap.get("amount");
			paramsMap.put("remark", remark);
			// 将交易请求到交易表中
			transactionsCreate(paramsMap, result);
			// 校验交易对象
			buyerSellerValidate(paramsMap.get("buyer_id") + "", paramsMap.get("seller_id") + "");
			// 校验买方账户余额是否能进行交易
			financeValidate(paramsMap.get("buyer_id") + "", paramsMap.get("payment_way_key") + "",
					paramsMap.get("amount") + "");

			if (MoneyUtil.moneyComp("0", (String) paramsMap.get("amount"))) {
				throw new BusinessException(RspCode.TRADE_AMOUNT_ERROR, RspCode.MSG.get(RspCode.TRADE_AMOUNT_ERROR));
			}
			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			transaction_no = resultMap.get("transaction_no") + "";

			Map<String, Object> tempMap = new HashMap<String, Object>();

			if (((String) paramsMap.get("payment_way_key")).equals(Constant.PAYMENT_WAY_06)) {
				// 调用用户礼券充值接口
				Map<String, Object> out_trade_body = FastJsonUtil.jsonToMap(paramsMap.get("out_trade_body") + "");
				String mobile = StringUtil.formatStr(out_trade_body.get("mobile"));
				if (!StringUtils.isEmpty(mobile)) {
					String member_service_url = PropertiesConfigUtil.getProperty("member_service_url");
					// 检测消费者是否已经进行过礼券兑换金币操作
					Map<String, String> reqParams = new HashMap<String, String>();
					Map<String, Object> bizParams = new HashMap<String, Object>();
					reqParams.put("service", "member.memberInfoFindByMobile");
					bizParams.put("mobile", mobile);
					bizParams.put("member_type_key", Constant.MEMBER_TYPE_CONSUMER);
					reqParams.put("params", FastJsonUtil.toJson(bizParams));
					String resultStr = HttpClientUtil.postShort(member_service_url, reqParams);
					resultMap = FastJsonUtil.jsonToMap(resultStr);
					if (((String) resultMap.get("rsp_code")).equals(RspCode.RESPONSE_SUCC)) {
						bizParams.clear();
						Map<String, Object> dateMap = (Map<String, Object>) resultMap.get("data");
						String consumer_id = dateMap.get("consumer_id") + "";
						bizParams.put("consumer_id", consumer_id);
						List<FdVoucherToGoldLog> list = voucherDao.selectFdVoucherToGoldLog(bizParams);
						if (list.size() > 0) {
							throw new BusinessException(RspCode.TRADE_FAIL, "此用户礼劵帐户已升级成金币帐户，不能再分配礼劵。");
						}
					}

					resultMap.clear();
					reqParams.clear();
					bizParams.clear();
					String store_name = "";
					reqParams.put("service", "member.storeFind");
					bizParams.put("member_id", paramsMap.get("buyer_id"));
					reqParams.put("params", FastJsonUtil.toJson(bizParams));
					resultStr = HttpClientUtil.postShort(member_service_url, reqParams);
					resultMap = FastJsonUtil.jsonToMap(resultStr);
					if (((String) resultMap.get("rsp_code")).equals(RspCode.RESPONSE_SUCC)) {
						Map<String, Object> dateMap = (Map<String, Object>) resultMap.get("data");
						store_name = StringUtil.formatStr(dateMap.get("stores_name"));
					}
					// 查询店东手机号
					reqParams.clear();
					bizParams.clear();
					resultMap.clear();
					reqParams.put("service", "member.userInfoFind");
					bizParams.put("member_id", paramsMap.get("buyer_id"));
					bizParams.put("member_type_key", Constant.MEMBER_TYPE_STORES);
					reqParams.put("params", FastJsonUtil.toJson(bizParams));
					resultStr = HttpClientUtil.postShort(member_service_url, reqParams);
					resultMap = FastJsonUtil.jsonToMap(resultStr);
					if (!((String) resultMap.get("rsp_code")).equals(RspCode.RESPONSE_SUCC)) {
						throw new BusinessException((String) resultMap.get("error_code"),
								(String) resultMap.get("error_msg"));
					}
					Map<String, Object> storeInfoMap = (Map<String, Object>) resultMap.get("data");
					String store_mobile = StringUtil.formatStr(storeInfoMap.get("mobile"));

					tempMap.clear();
					tempMap.put("fromUser", (String) paramsMap.get("buyer_id"));
					tempMap.put("toUser", mobile);
					tempMap.put("store_mobile", store_mobile);
					tempMap.put("data_source", paramsMap.get("data_source"));
					tempMap.put("tradeId", paramsMap.get("out_trade_no"));
					tempMap.put("amount", (String) paramsMap.get("amount"));
					tempMap.put("identity", "DX" + transaction_no);
					tempMap.put("message", "店铺(" + store_name + ")为您充值了" + paramsMap.get("amount") + "礼券");
					tempMap.put("scenariosId", "1");
					financeSyncService.giftAccountsAPI(tempMap);
				}
			}
			tempMap.clear();
			tempMap.put("transaction_no", transaction_no);
			tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
			result.setResultData(new ResultData());
			transactionConfirmed(tempMap, result);
		} catch (BusinessException e) {
			fDTransactionLogCreate(transaction_no, "订单赠送交易失败," + e.getMsg());
			throw e;
		} catch (SystemException e) {
			fDTransactionErrorLogCreate(transaction_no, "订单赠送交易异常," + e.getMsg());
			throw e;
		} catch (Exception e) {
			fDTransactionErrorLogCreate(transaction_no, "订单赠送交易异常," + e.getMessage());
			throw e;
		}
	}

	/**
	 * 余额提现
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void balanceWithdraw(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		String transaction_no = "";
		try {
			// 周六周日不容许提现
			String new_date = DateUtil.getFormatDate(DateUtil.fmt_yyyyMMdd);
			int week = DateUtil.dayForWeek(DateUtil.getFormatDate(DateUtil.fmt_yyyyMMdd));
			if (week > 5) {
				throw new BusinessException(RspCode.TRADE_FAIL, "抱歉,非工作日不能申请提现");
			}
			// 假期不容许提现
			if (null != Constant.HOLIDAY_MAP.get(new_date)) {
				throw new BusinessException(RspCode.TRADE_FAIL, "抱歉,非工作日不能申请提现");
			}
			// out_trade_no, 必传参数
			ValidateUtil.validateParams(paramsMap, new String[] { "data_source", "member_id", "amount", "detail" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("buyer_id", paramsMap.get("member_id"));
			paramsMap.put("seller_id", Constant.MEMBER_ID_MTH);
			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			// 余额提现
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_BALANCEWITHDRAW);
			paramsMap.put("order_type_key", Constant.ORDER_TYPE_BALANCEWITHDRAW);
			paramsMap.put("payment_way_key", Constant.PAYMENT_WAY_05);

			String remark = Constant.DATA_SOURCE_MAP.get(paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get(paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get(paramsMap.get("payment_way_key")) + "|" + paramsMap.get("amount");
			paramsMap.put("remark", remark);
			transactionsCreate(paramsMap, result);
			// 提现金额不能小于100
			if (!MoneyUtil.moneyComp((String) paramsMap.get("amount"), "100")) {
				throw new BusinessException(RspCode.TRADE_AMOUNT_ERROR, "提现金额不能小于100");
			}
			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			transaction_no = resultMap.get("transaction_no") + "";
			// 校验账户余额是否能进行交易
			financeValidate(paramsMap.get("buyer_id") + "", Constant.PAYMENT_WAY_05, paramsMap.get("amount") + "");

			Map<String, Object> tempMap = new HashMap<String, Object>();
			// 余额体现交易确定，直接调用交易确认接口
			tempMap.put("transaction_no", transaction_no);
			tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
			result.setResultData(new ResultData());
			transactionConfirmed(tempMap, result);
		} catch (BusinessException e) {
			fDTransactionLogCreate(transaction_no, "余额提现交易异常," + e.getMsg());
			throw e;
		} catch (SystemException e) {
			fDTransactionErrorLogCreate(transaction_no, "余额提现交易异常," + e.getMsg());
			throw e;
		} catch (Exception e) {
			fDTransactionErrorLogCreate(transaction_no, "余额提现交易异常," + e.getMessage());
			throw e;
		}
	}

	/**
	 * 余额提现退款
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void balanceWithdrawRefund(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		String transaction_no = "";
		try {
			ValidateUtil.validateParams(paramsMap,
					new String[] { "data_source", "old_transaction_no", "detail", "out_trade_body" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_BALANCEWITHDRAWREFUND);

			// 查询原交易信息
			String old_transaction_no = (String) paramsMap.get("old_transaction_no");
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("transaction_no", old_transaction_no);
			List<FDTransactionsResult> fDTransactionsResultList = financeDao.selectFDTransactionsResult(tempMap);
			if (fDTransactionsResultList.size() == 0) {
				throw new BusinessException(RspCode.TRADE_NO_EXIST, RspCode.MSG.get(RspCode.TRADE_NO_EXIST));
			}
			FDTransactionsResult fDTransactionsResult = fDTransactionsResultList.get(0);

			paramsMap.put("out_trade_no", old_transaction_no);
			// 交易冲正。买卖双方交换
			paramsMap.put("buyer_id", fDTransactionsResult.getSeller_id());
			paramsMap.put("seller_id", fDTransactionsResult.getBuyer_id());
			paramsMap.put("amount", fDTransactionsResult.getAmount().toString());
			paramsMap.put("payment_way_key", fDTransactionsResult.getPayment_way_key());

			String remark = Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get((String) paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get((String) paramsMap.get("payment_way_key")) + "|"
					+ (String) paramsMap.get("amount");
			paramsMap.put("remark", remark);

			// 检测提现是否已经做过退款
			tempMap.clear();
			tempMap.put("out_trade_no", old_transaction_no);
			tempMap.put("business_type_key", Constant.BUSINESS_TYPE_BALANCEWITHDRAWREFUND);
			FDTransactions old_transactions = financeDao.selectFDTransactions(tempMap);
			if (old_transactions != null) {
				throw new BusinessException(RspCode.PROCESSING, "请勿重新操作");
			}

			transactionsCreate(paramsMap, result);

			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			transaction_no = (String) resultMap.get("transaction_no");

			tempMap.clear();
			tempMap.put("transaction_no", transaction_no);
			tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
			result.setResultData(new ResultData());
			transactionConfirmed(tempMap, result);
		} catch (BusinessException e) {
			fDTransactionLogCreate(transaction_no, "交易冲正交易失败," + e.getMsg());
			throw e;
		} catch (SystemException e) {
			fDTransactionErrorLogCreate(transaction_no, "交易冲正交易异常," + e.getMsg());
			throw e;
		} catch (Exception e) {
			fDTransactionErrorLogCreate(transaction_no, "交易冲正交易异常," + e.getMessage());
			throw e;
		}
	}

	/**
	 * 订单结算
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void orderSettlement(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		String transaction_no = "";
		try {
			// out_trade_no, 必传参数
			ValidateUtil.validateParams(paramsMap,
					new String[] { "data_source", "out_member_id", "in_member_id", "amount", "detail" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("buyer_id", paramsMap.get("out_member_id"));
			paramsMap.put("seller_id", paramsMap.get("in_member_id"));
			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_ORDERSETTLEMENT);
			String remark = Constant.DATA_SOURCE_MAP.get(paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get(paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get(paramsMap.get("payment_way_key")) + "|" + paramsMap.get("amount");
			paramsMap.put("remark", remark);
			// 将交易请求到交易表中
			transactionsCreate(paramsMap, result);
			if (MoneyUtil.moneyComp("0", (String) paramsMap.get("amount"))) {
				throw new BusinessException(RspCode.TRADE_AMOUNT_ERROR, RspCode.MSG.get(RspCode.TRADE_AMOUNT_ERROR));
			}
			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			transaction_no = resultMap.get("transaction_no") + "";

			// 校验账户余额是否能进行交易
			financeValidate(paramsMap.get("buyer_id") + "", paramsMap.get("payment_way_key") + "",
					paramsMap.get("amount") + "");

			// 礼券充值，直接调用交易确认接口
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("transaction_no", transaction_no);
			tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
			result.setResultData(new ResultData());
			transactionConfirmed(tempMap, result);
		} catch (BusinessException e) {
			fDTransactionLogCreate(transaction_no, "订单结算交易失败," + e.getMsg());
			throw e;
		} catch (SystemException e) {
			fDTransactionErrorLogCreate(transaction_no, "订单结算交易异常," + e.getMsg());
			throw e;
		} catch (Exception e) {
			fDTransactionErrorLogCreate(transaction_no, "订单结算交易异常," + e.getMessage());
			throw e;
		}
	}

	/**
	 * 订单退款
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void orderRefund(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		String transaction_no = "";
		try {
			// out_trade_no, 必传参数
			ValidateUtil.validateParams(paramsMap,
					new String[] { "data_source", "out_member_id", "in_member_id", "amount", "detail" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("buyer_id", paramsMap.get("out_member_id"));
			paramsMap.put("seller_id", paramsMap.get("in_member_id"));
			// 支付方式转换
			String payment_way_key = paramsMap.get("payment_way_key") + "";
			if (payment_way_key.equals(Constant.PAYMENT_WAY_01) || payment_way_key.equals(Constant.PAYMENT_WAY_02)
					|| payment_way_key.equals(Constant.PAYMENT_WAY_10)
					|| payment_way_key.equals(Constant.PAYMENT_WAY_11)
					|| payment_way_key.equals(Constant.PAYMENT_WAY_12)
					|| payment_way_key.equals(Constant.PAYMENT_WAY_14)) {
				payment_way_key = Constant.PAYMENT_WAY_05;
			}
			paramsMap.put("payment_way_key", payment_way_key);

			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_ORDERREFUND);
			String remark = Constant.DATA_SOURCE_MAP.get(paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get(paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get(paramsMap.get("payment_way_key")) + "|" + paramsMap.get("amount");
			paramsMap.put("remark", remark);
			transactionsCreate(paramsMap, result);
			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			transaction_no = resultMap.get("transaction_no") + "";
			Map<String, Object> tempMap = new HashMap<String, Object>();
			// 检查买家是否存在
			tempMap.clear();
			tempMap.put("member_id", paramsMap.get("buyer_id"));
			FDMemberAsset buyer = financeDao.selectFDMemberAsset(tempMap);
			if (buyer == null) {
				throw new BusinessException(RspCode.MEMBER_NO_EXIST, RspCode.MSG.get(RspCode.MEMBER_NO_EXIST));
			}
			tempMap.clear();
			tempMap.put("transaction_no", transaction_no);
			tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
			result.setResultData(new ResultData());
			transactionConfirmed(tempMap, result);
		} catch (BusinessException e) {
			fDTransactionLogCreate(transaction_no, "订单退款交易失败," + e.getMsg());
			throw e;
		} catch (SystemException e) {
			fDTransactionErrorLogCreate(transaction_no, "订单退款交易异常," + e.getMsg());
			throw e;
		} catch (Exception e) {
			fDTransactionErrorLogCreate(transaction_no, "订单退款交易异常," + e.getMessage());
			throw e;
		}
	}

	/**
	 * 交易冲正
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void transactionReverse(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		String transaction_no = "";
		try {
			ValidateUtil.validateParams(paramsMap,
					new String[] { "data_source", "old_transaction_no", "detail", "out_trade_body" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_TRANSACTIONREVERSE);

			// 查询原交易信息
			String old_transaction_no = (String) paramsMap.get("old_transaction_no");
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("transaction_no", old_transaction_no);
			List<FDTransactionsResult> fDTransactionsResultList = financeDao.selectFDTransactionsResult(tempMap);
			if (fDTransactionsResultList.size() == 0) {
				throw new BusinessException(RspCode.TRADE_NO_EXIST, RspCode.MSG.get(RspCode.TRADE_NO_EXIST));
			}
			FDTransactionsResult fDTransactionsResult = fDTransactionsResultList.get(0);

			paramsMap.put("out_trade_no", old_transaction_no);
			// 交易冲正。买卖双方交换
			paramsMap.put("buyer_id", fDTransactionsResult.getSeller_id());
			paramsMap.put("seller_id", fDTransactionsResult.getBuyer_id());
			paramsMap.put("amount", fDTransactionsResult.getAmount().toString());
			// 支付方式转换
			String payment_way_key = fDTransactionsResult.getPayment_way_key();
			if (payment_way_key.equals(Constant.PAYMENT_WAY_01) || payment_way_key.equals(Constant.PAYMENT_WAY_02)
					|| payment_way_key.equals(Constant.PAYMENT_WAY_10)
					|| payment_way_key.equals(Constant.PAYMENT_WAY_11)
					|| payment_way_key.equals(Constant.PAYMENT_WAY_12)
					|| payment_way_key.equals(Constant.PAYMENT_WAY_14)) {
				payment_way_key = Constant.PAYMENT_WAY_05;
			}

			paramsMap.put("payment_way_key", payment_way_key);
			String remark = Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get((String) paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get((String) paramsMap.get("payment_way_key")) + "|"
					+ (String) paramsMap.get("amount");
			paramsMap.put("remark", remark);

			// 检查交易是否已经冲正
			tempMap.clear();
			tempMap.put("out_trade_no", old_transaction_no);
			tempMap.put("business_type_key", Constant.BUSINESS_TYPE_TRANSACTIONREVERSE);
			FDTransactions old_transactions = financeDao.selectFDTransactions(tempMap);
			if (old_transactions != null) {
				throw new BusinessException(RspCode.PROCESSING, "请勿重新操作");
			}

			transactionsCreate(paramsMap, result);

			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			transaction_no = (String) resultMap.get("transaction_no");

			// 礼券退款
			if (fDTransactionsResult.getPayment_way_key().equals(Constant.PAYMENT_WAY_06)) {
				// 如果买方是消费者,则调用消费者礼券变更接口
				tempMap.clear();
				tempMap.put("member_id", paramsMap.get("buyer_id"));
				FDMemberAsset fDMemberAsset = financeDao.selectFDMemberAsset(tempMap);
				if (fDMemberAsset.getMember_type_key().equals(Constant.MEMBER_TYPE_CONSUMER)) {
					tempMap.clear();
					tempMap.put("amount", (String) paramsMap.get("amount"));
					tempMap.put("identity", "DX" + fDTransactionsResult.getTransaction_no());
					tempMap.put("message", "退还" + paramsMap.get("amount") + "礼券");
					tempMap.put("scenariosId", "2");
					financeSyncService.giftAccountsAPI(tempMap);
				}
			}
			tempMap.clear();
			tempMap.put("transaction_no", transaction_no);
			tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
			result.setResultData(new ResultData());
			transactionConfirmed(tempMap, result);
		} catch (BusinessException e) {
			fDTransactionLogCreate(transaction_no, "交易冲正交易失败," + e.getMsg());
			throw e;
		} catch (SystemException e) {
			fDTransactionErrorLogCreate(transaction_no, "交易冲正交易异常," + e.getMsg());
			throw e;
		} catch (Exception e) {
			fDTransactionErrorLogCreate(transaction_no, "交易冲正交易异常," + e.getMessage());
			throw e;
		}
	}

	/**
	 * 会员资产清零
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void assetClear(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			ValidateUtil.validateParams(paramsMap,
					new String[] { "data_source", "member_id", "payment_way_key", "out_trade_no", "detail" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("buyer_id", paramsMap.get("member_id"));
			paramsMap.put("seller_id", Constant.MEMBER_ID_MTH);
			paramsMap.put("amount", "0");
			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_ASSETCLEAR);
			String remark = Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get((String) paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get((String) paramsMap.get("payment_way_key")) + "|"
					+ (String) paramsMap.get("amount");
			paramsMap.put("remark", remark);
			// 将交易请求到交易表中
			transactionsCreate(paramsMap, result);

			// 礼券充值，直接调用交易确认接口
			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("transaction_no", (String) resultMap.get("transaction_no"));
			tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
			result.setResultData(new ResultData());
			transactionConfirmed(tempMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 余额冻结
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void balanceFreeze(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			ValidateUtil.validateParams(paramsMap,
					new String[] { "data_source", "member_id", "member_type_key", "amount", "detail" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			BigDecimal amount = new BigDecimal(paramsMap.get("amount") + "");
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", paramsMap.get("member_id"));
			tempMap.put("member_type_key", paramsMap.get("member_type_key"));
			FDMemberAsset member = financeDao.selectFDMemberAsset(tempMap);
			// 冻结前金额
			BigDecimal pre_balance = member.getCash_froze();
			// 冻结后余额
			BigDecimal balance = MoneyUtil.moneyAdd(pre_balance, amount);

			FDMemberCashLog operateLog = new FDMemberCashLog();
			operateLog.setLog_id(IDUtil.getUUID());
			operateLog.setMember_id(member.getMember_id());
			operateLog.setMember_type_key(member.getMember_type_key());
			operateLog.setCategory(Constant.CATEGORY_FROZEN);
			operateLog.setTracked_date(new Date());
			operateLog.setPre_balance(pre_balance);
			operateLog.setAmount(amount);
			operateLog.setBalance(balance);
			Map<String, Object> remarkMap = new HashMap<String, Object>();
			remarkMap.put("detail", paramsMap.get("detail"));
			remarkMap.put("transaction_member_id", Constant.MEMBER_ID_MTH);
			remarkMap.put("transaction_member_type_key", Constant.MEMBER_TYPE_COMPANY);
			remarkMap.put("transaction_member_name", "每天惠");
			remarkMap.put("transaction_member_contact", "每天惠");
			remarkMap.put("operator", paramsMap.get("operator"));
			remarkMap.put("operator_type", paramsMap.get("operator_type"));
			operateLog.setRemark(FastJsonUtil.toJson(remarkMap));
			// 记录余额变更日志
			transactionDao.insertFDMemberCashLog(operateLog);
			tempMap.clear();
			tempMap.put("member_id", member.getMember_id());
			tempMap.put("member_type_key", member.getMember_type_key());
			tempMap.put("trade_cash_froze", amount);
			financeDao.updateFDMemberAsset(tempMap);

		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 余额解冻
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void balanceUnFreeze(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			ValidateUtil.validateParams(paramsMap,
					new String[] { "data_source", "member_id", "member_type_key", "amount", "detail" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			BigDecimal amount = new BigDecimal(paramsMap.get("amount") + "");
			// 交易金额取反
			amount = amount.negate();
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", paramsMap.get("member_id"));
			FDMemberAsset member = financeDao.selectFDMemberAsset(tempMap);
			// 解冻前金额
			BigDecimal pre_balance = member.getCash_froze();
			// 解冻后金额
			BigDecimal balance = MoneyUtil.moneyAdd(pre_balance, amount);
			if (balance.compareTo(BigDecimal.ZERO) < 0) {
				throw new BusinessException(RspCode.TRADE_AMOUNT_ERROR, "解冻余额超过当前冻结金额");
			}
			FDMemberCashLog operateLog = new FDMemberCashLog();
			operateLog.setLog_id(IDUtil.getUUID());
			operateLog.setMember_id(member.getMember_id());
			operateLog.setMember_type_key(member.getMember_type_key());
			operateLog.setCategory(Constant.CATEGORY_FROZEN);
			operateLog.setTracked_date(new Date());
			operateLog.setPre_balance(pre_balance);
			operateLog.setAmount(amount);
			operateLog.setBalance(balance);
			Map<String, Object> remarkMap = new HashMap<String, Object>();
			remarkMap.put("detail", paramsMap.get("detail"));
			remarkMap.put("transaction_member_id", Constant.MEMBER_ID_MTH);
			remarkMap.put("transaction_member_type_key", Constant.MEMBER_TYPE_COMPANY);
			remarkMap.put("transaction_member_name", "每天惠");
			remarkMap.put("transaction_member_contact", "每天惠");
			remarkMap.put("operator", paramsMap.get("operator"));
			remarkMap.put("operator_type", paramsMap.get("operator_type"));
			operateLog.setRemark(FastJsonUtil.toJson(remarkMap));
			// 记录余额变更日志
			transactionDao.insertFDMemberCashLog(operateLog);
			tempMap.clear();
			tempMap.put("member_id", member.getMember_id());
			tempMap.put("trade_cash_froze", amount);
			financeDao.updateFDMemberAsset(tempMap);

		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void transactionStatusFind(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			// 交易号和外部订单号必须有一个
			ValidateUtil.validateParamsNum(paramsMap, new String[] { "transaction_no", "out_trade_no" }, 1);
			List<FDTransactionsResult> fDTransactionsResultList = financeDao.selectFDTransactionsResult(paramsMap);
			Map<String, Object> resultMap = new HashMap<String, Object>();
			if (fDTransactionsResultList.size() == 0) {
				throw new BusinessException(RspCode.TRADE_NO_EXIST, RspCode.MSG.get(RspCode.TRADE_NO_EXIST));
			} else {
				FDTransactionsResult fDTransactionsResult = fDTransactionsResultList.get(0);
				resultMap.put("transaction_no", fDTransactionsResult.getTransaction_no());
				resultMap.put("amount", fDTransactionsResult.getAmount().toString());
				resultMap.put("buyer_id", fDTransactionsResult.getBuyer_id());
				resultMap.put("seller_id", fDTransactionsResult.getSeller_id());
				resultMap.put("transaction_status", fDTransactionsResult.getTransaction_status());
				resultMap.put("transaction_date",
						DateUtil.date2Str(fDTransactionsResult.getTransaction_date(), DateUtil.fmt_yyyyMMddHHmmss));
			}
			result.setResultData(resultMap);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 财务校验,验证账户的余额支付能进行交易
	 * 
	 * @param member_id
	 * @param payment_way_key
	 * @param trade_value
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void financeValidate(String member_id, String payment_way_key, String trade_value)
			throws BusinessException, SystemException, Exception {
		try {
			// 礼券类型
			String balance_type = "";
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", member_id);
			FDMemberAsset fDMemberAsset = financeDao.selectFDMemberAsset(tempMap);
			if (fDMemberAsset == null) {
				throw new BusinessException(RspCode.MEMBER_NO_EXIST, RspCode.MSG.get(RspCode.MEMBER_NO_EXIST));
			}
			// 如果是公司账户，直接跳过校验
			if (fDMemberAsset.getMember_type_key().equals(Constant.MEMBER_TYPE_COMPANY)) {
				return;
			}
			String pro_trade_value = "0.00";
			String balance = "0.00";
			if (payment_way_key.equals(Constant.PAYMENT_WAY_05)) {
				balance_type = "可用零钱余额";
				// 总余额
				String cash_balance = fDMemberAsset.getCash_balance().toString();
				// 冻结金额
				String cash_froze = fDMemberAsset.getCash_froze().toString();
				// 可用余额
				pro_trade_value = MoneyUtil.moneySub(cash_balance, cash_froze);
				balance = MoneyUtil.moneySub(pro_trade_value, trade_value);
				if (!MoneyUtil.moneyComp(balance, "0.00")) {
					throw new BusinessException(RspCode.BALANCE_NO_ENOUGH, balance_type + "不足");
				}
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_06)) {
				balance_type = "可用礼券数";
				pro_trade_value = fDMemberAsset.getVoucher_balance().toString();
				balance = MoneyUtil.moneySub(pro_trade_value, trade_value);
				if (!MoneyUtil.moneyComp(balance, "0.00")) {
					throw new BusinessException(RspCode.BALANCE_NO_ENOUGH, balance_type + "不足");
				}
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_08)) {
				balance_type = "可用金币余额";
				pro_trade_value = fDMemberAsset.getGold().toString();
				if (!MoneyUtil.moneyComp(pro_trade_value, "0.00")) {
					throw new BusinessException(RspCode.BALANCE_NO_ENOUGH, balance_type + "不足");
				}
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_09)) {
				balance_type = "可用积分";
				pro_trade_value = fDMemberAsset.getBonus().toString();
				if (!MoneyUtil.moneyComp(pro_trade_value, "0.00")) {
					throw new BusinessException(RspCode.BALANCE_NO_ENOUGH, balance_type + "不足");
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 完善交易数据
	 * 
	 * @param fDTransactionsResult
	 */
	private void completeTransactionData(final FDTransactionsResult fDTransactionsResult) {
		try {
			Map<String, Object> tempMap = new HashMap<String, Object>();
			/**
			 * 如果买家不存在,将买家替换成每天惠,同理,如果卖家不存在,将卖家替换成每天惠
			 */
			tempMap.put("member_id", fDTransactionsResult.getBuyer_id());
			FDMemberAsset buyer = financeDao.selectFDMemberAsset(tempMap);
			if (null == buyer) {
				throw new BusinessException(RspCode.BUYER_NO_EXIST, "买家" + fDTransactionsResult.getBuyer_id() + "不存在");
			} else {
				fDTransactionsResult.setBuyer_member_type(buyer.getMember_type_key());
			}
			Map<String, String> userInfo = userInfoFindByMember(buyer.getMember_id(), buyer.getMember_type_key());
			fDTransactionsResult.setBuyer_name(userInfo.get("name"));
			fDTransactionsResult.setBuyer_contact(userInfo.get("contact"));
			/**
			 * 卖家信息补充
			 */
			tempMap.clear();
			tempMap.put("member_id", fDTransactionsResult.getSeller_id());
			FDMemberAsset seller = financeDao.selectFDMemberAsset(tempMap);
			if (null == seller) {
				throw new BusinessException(RspCode.SELLER_NO_EXIST,
						"卖家" + fDTransactionsResult.getSeller_id() + "不存在");
			} else {
				fDTransactionsResult.setSeller_member_type(seller.getMember_type_key());
			}
			userInfo = userInfoFindByMember(seller.getMember_id(), seller.getMember_type_key());
			fDTransactionsResult.setSeller_name(userInfo.get("name"));
			fDTransactionsResult.setSeller_contact(userInfo.get("contact"));
			userInfo.clear();
			tempMap.clear();
			tempMap = null;
			userInfo = null;
		} catch (BusinessException e) {
			logger.info(e);
		} catch (SystemException e) {
			logger.error("完成交易数据异常", e);
		} catch (Exception e) {
			logger.error("完成交易数据异常", e);
		}
	}

	private Map<String, String> userInfoFindByMember(String member_id, String member_type_key) {
		Map<String, String> user = new HashMap<String, String>();
		user.put("name", "");
		user.put("contact", "");
		String cacheKey = "[userInfoFindByMember]_" + member_id + member_type_key;
		try {
			Object obj = redisUtil.getObj(cacheKey);
			if (null != obj) {
				user = (Map<String, String>) obj;
				return user;
			}
			// 如果是公司账号，就不需要查询会员信息
			if (member_type_key.equals(Constant.MEMBER_TYPE_COMPANY)) {
				if (member_id.equals(Constant.MEMBER_ID_BUSINESS)) {
					user.put("name", "每天惠商贸公司");
					user.put("contact", "每天惠商贸公司");
				} else {
					user.put("name", "每天惠");
					user.put("contact", "每天惠");
				}
			} else {
				String member_service_url = PropertiesConfigUtil.getProperty("member_service_url");
				Map<String, String> reqParams = new HashMap<String, String>();
				Map<String, String> paramMap = new HashMap<String, String>();
				String resultStr = null;
				reqParams.put("service", "member.memberInfoFindByMemberId");
				paramMap.put("member_id", member_id);
				paramMap.put("member_type_key", member_type_key);
				reqParams.put("params", FastJsonUtil.toJson(paramMap));
				resultStr = HttpClientUtil.postShort(member_service_url, reqParams);
				Map<String, Object> resultMap = FastJsonUtil.jsonToMap(resultStr);
				if (((String) resultMap.get("rsp_code")).equals(RspCode.RESPONSE_SUCC)) {
					Map<String, Object> dateMap = (Map<String, Object>) resultMap.get("data");
					if (member_type_key.equals(Constant.MEMBER_TYPE_CONSUMER)) {
						user.put("name", StringUtil.formatStr(dateMap.get("nick_name")));
						user.put("contact", StringUtil.formatStr(dateMap.get("mobile")));
					} else if (member_type_key.equals(Constant.MEMBER_TYPE_STORES)) {
						user.put("name", StringUtil.formatStr(dateMap.get("stores_name")));
						String contact = StringUtil.formatStr(dateMap.get("contact_person")) + "|"
								+ StringUtil.formatStr(dateMap.get("contact_tel"));
						if (!contact.equals("|")) {
							user.put("contact", contact);
						}
					} else if (member_type_key.equals(Constant.MEMBER_TYPE_SUPPLIER)) {
						user.put("name", StringUtil.formatStr(dateMap.get("supplier_name")));
						String contact = StringUtil.formatStr(dateMap.get("contact_person")) + "|"
								+ StringUtil.formatStr(dateMap.get("contact_tel"));
						if (!contact.equals("|")) {
							user.put("contact", contact);
						}
					}
				}
			}
			// 缓存数据，有效时间为一个小时
			redisUtil.setObj(cacheKey, user, 3600);
		} catch (SystemException e) {
			logger.error("交易信息完善异常", e);
		} catch (Exception e) {
			logger.error("交易信息完善异常", e);
		}
		return user;
	}

	private void transactionsCreate(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			// 此处可以用map,可以用javaBean
			Date date = new Date();
			FDTransactions fDTransactions = new FDTransactions();
			BeanConvertUtil.mapToBean(fDTransactions, paramsMap);
			fDTransactions.setTransaction_id(IDUtil.getUUID());
			fDTransactions.setTransaction_no(TradeIDUtil.getTradeNo());
			fDTransactions.setTransaction_date(date);
			fDTransactions.setCreated_date(date);
			financeDao.insertFDTransactions(fDTransactions);
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("transaction_no", fDTransactions.getTransaction_no());
			result.setResultData(resultMap);
			transactionFlowCreate(fDTransactions.getTransaction_no(), "1-生成交易", fDTransactions.getRemark());
			// 记录交易日志
			fDTransactionLogCreate(fDTransactions.getTransaction_no(), fDTransactions.getRemark());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 校验买卖双方是否是同一个人
	 * 
	 * @param buyer_id
	 * @param serller_id
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void buyerSellerValidate(String buyer_id, String seller_id)
			throws BusinessException, SystemException, Exception {
		try {
			if (buyer_id.equals(seller_id)) {
				if (!buyer_id.equals(Constant.MEMBER_ID_MTH) && !buyer_id.equals(Constant.MEMBER_ID_HYD)
						&& !buyer_id.equals(Constant.MEMBER_ID_ANONYMITY)
						&& buyer_id.equals(Constant.MEMBER_ID_BUSINESS)
						&& !buyer_id.equals(Constant.MEMBER_ID_PLATFORM)) {
					throw new BusinessException(RspCode.TRADE_FAIL, "请勿非法交易");
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 订单金额验证
	 * 
	 * @param buyer_id
	 * @param serller_id
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void orderAmountValidate(Map<String, Object> fDTransactions)
			throws BusinessException, SystemException, Exception {
		try {
			String order_type_key = StringUtil.formatStr(fDTransactions.get("order_type_key"));
			if (order_type_key.equals(Constant.ORDER_TYPE_LOCAL_SALE)) {
				Map<String, Object> orderMap = FastJsonUtil.jsonToMap(fDTransactions.get("out_trade_body") + "");
				BigDecimal amount = new BigDecimal(fDTransactions.get("amount") + "");
				BigDecimal total_fee = new BigDecimal(orderMap.get("total_fee") + "");
				if (amount.compareTo(total_fee) != 0) {
					throw new BusinessException(RspCode.TRADE_FAIL, "支付金额异常,请重新购买");
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void transactionConfirmed(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		FDTransactionsResult fDTransactionsResult = null;
		try {
			ValidateUtil.validateParams(paramsMap, new String[] { "transaction_no", "transaction_status" });
			FDTransactions tDTransactions = financeDao.selectFDTransactions(paramsMap);
			if (tDTransactions == null) {
				throw new BusinessException(RspCode.TRADE_NO_EXIST, RspCode.MSG.get(RspCode.TRADE_NO_EXIST));
			}
			fDTransactionsResult = new FDTransactionsResult();
			BeanConvertUtil.copyProperties(fDTransactionsResult, tDTransactions);
			BeanConvertUtil.mapToBean(fDTransactionsResult, paramsMap);
			fDTransactionsResult.setCreated_date(new Date());
			// 完善交易信息
			completeTransactionData(fDTransactionsResult);
			// 写入交易结果表
			financeDao.insertFDTransactionsResult(fDTransactionsResult);

			transactionFlowCreate(fDTransactionsResult.getTransaction_no(), "2-交易结果", fDTransactionsResult.getRemark());
			Map<String, Object> beanToMap = BeanConvertUtil.beanToMap(fDTransactionsResult);
			// 根据交易结果装确定是否进行进行财务分账
			if (fDTransactionsResult.getTransaction_status().equals(Constant.TRANSACTION_STATUS_CONFIRMED)) {
				// 分账
				subAccounts(fDTransactionsResult);
			}
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("transaction_no", fDTransactionsResult.getTransaction_no());
			result.setResultData(tempMap);
			tDTransactions = null;
			fDTransactionsResult = null;
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 分账
	 * 
	 * @param fDTransactionsResult
	 */
	@Override
	public void subAccounts(FDTransactionsResult fDTransactionsResult) {
		boolean error = false;
		String remark = "交易成功";
		try {
			String business_type_key = fDTransactionsResult.getBusiness_type_key();
			// 根据业务类型 调用对于的业务处理逻辑
			if (business_type_key.equals(Constant.BUSINESS_TYPE_BALANCERECHARGE)) {
				// 余额充值
				balanceRechargeSubAccounts(fDTransactionsResult);
			} else if (business_type_key.equals(Constant.BUSINESS_TYPE_BALANCEPAY)) {
				// 余额支付
				balancePaySubAccounts(fDTransactionsResult);
			} else if (business_type_key.equals(Constant.BUSINESS_TYPE_ORDERREWARD)) {
				// 订单赠送
				orderRewardSubAccounts(fDTransactionsResult);
			} else if (business_type_key.equals(Constant.BUSINESS_TYPE_ORDERPAY)) {
				// 订单支付
				orderPaySubAccounts(fDTransactionsResult);
			} else if (business_type_key.equals(Constant.BUSINESS_TYPE_ORDERREFUND)) {
				// 订单退款
				orderRefundSubAccounts(fDTransactionsResult);
			} else if (business_type_key.equals(Constant.BUSINESS_TYPE_ORDERSETTLEMENT)) {
				// 订单结算
				orderSettlement(fDTransactionsResult);
			} else if (business_type_key.equals(Constant.BUSINESS_TYPE_BALANCEWITHDRAW)) {
				// 余额提现
				balanceWithdrawSubAccounts(fDTransactionsResult);
			} else if (business_type_key.equals(Constant.BUSINESS_TYPE_BALANCEWITHDRAWREFUND)) {
				// 余额提现退款
				balanceWithdrawRefundSubAccounts(fDTransactionsResult);
			} else if (business_type_key.equals(Constant.BUSINESS_TYPE_TRANSACTIONREVERSE)) {
				// 交易冲正
				transactionReverseSubAccounts(fDTransactionsResult);
			} else if (business_type_key.equals(Constant.BUSINESS_TYPE_ASSETCLEAR)) {
				// 资产清零分账
				assetClearSubAccounts(fDTransactionsResult);
			} else if (business_type_key.equals(Constant.BUSINESS_TYPE_POSPAY)) {
				// pos结算
				posSettlement(fDTransactionsResult);
			}
		} catch (BusinessException e) {
			error = true;
			remark = e.getMsg();
			logger.error("分账失败," + e.getMsg());
			fDTransactionLogCreate(fDTransactionsResult.getTransaction_no(), "分账失败," + e.getMsg());
		} catch (SystemException e) {
			error = true;
			remark = e.getMsg();
			logger.error("分账异常", e);
			fDTransactionErrorLogCreate(fDTransactionsResult.getTransaction_no(), "分账异常," + e.getMsg());
		} catch (Exception e) {
			error = true;
			remark = e.getLocalizedMessage();
			logger.error("分账异常", e);
			fDTransactionErrorLogCreate(fDTransactionsResult.getTransaction_no(), "分账异常," + e.getMessage());
		} finally {
			fDTransactionResultUpdate(fDTransactionsResult, error, remark);
		}
	}

	/**
	 * 余额充值
	 * 
	 * @Title: balanceRechargeSubAccounts
	 * @param fDTransactionsResult
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 * @author tiny
	 */
	private void balanceRechargeSubAccounts(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方出账,卖方入账
			cashChange(fDTransactionsResult, fDTransactionsResult.getBuyer_id(), fDTransactionsResult.getSeller_id());
			// 消费者充值送金币
			if (fDTransactionsResult.getSeller_member_type().equals(Constant.MEMBER_TYPE_CONSUMER)) {
				rewardConsumerGold(fDTransactionsResult.getTransaction_no(), fDTransactionsResult.getSeller_id(),
						fDTransactionsResult.getAmount().intValue(), "充值赠送");
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 余额支付
	 * 
	 * @Title: balancePaySubAccounts
	 * @param fDTransactionsResult
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 * @author tiny
	 */
	private void balancePaySubAccounts(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方出账,卖方入账
			if (fDTransactionsResult.getPayment_way_key().equals(Constant.PAYMENT_WAY_05)) {
				// 惠支付
				huiPay(fDTransactionsResult);
			} else if (fDTransactionsResult.getPayment_way_key().equals(Constant.PAYMENT_WAY_06)) {
				// 礼券
				voucherPay(fDTransactionsResult);
			} else if (fDTransactionsResult.getPayment_way_key().equals(Constant.PAYMENT_WAY_08)) {
				// 金币
				goldPay(fDTransactionsResult);
			} else if (fDTransactionsResult.getPayment_way_key().equals(Constant.PAYMENT_WAY_10)) {
				// 支付宝扫码支付
				alipayCodePay(fDTransactionsResult);
			} else if (fDTransactionsResult.getPayment_way_key().equals(Constant.PAYMENT_WAY_11)) {
				// 微信扫码支付
				wechatCodePay(fDTransactionsResult);
			} else if (fDTransactionsResult.getPayment_way_key().equals(Constant.PAYMENT_WAY_14)) {
				// 翼支付扫码支付
				bestCodePay(fDTransactionsResult);
			} else if (fDTransactionsResult.getPayment_way_key().equals(Constant.PAYMENT_WAY_09)) {
				// 积分
				pointPay(fDTransactionsResult);
			}
			// 零钱交易赠送消费者金币 买方是消费者,卖方是店东的时候,才能赠送
			if (fDTransactionsResult.getBuyer_member_type().equals(Constant.MEMBER_TYPE_CONSUMER)
					&& fDTransactionsResult.getSeller_member_type().equals(Constant.MEMBER_TYPE_STORES)
					&& (fDTransactionsResult.getPayment_way_key().equals(Constant.PAYMENT_WAY_05))) {
				rewardConsumerGold(fDTransactionsResult.getTransaction_no(), fDTransactionsResult.getBuyer_id(),
						fDTransactionsResult.getAmount().intValue(), "系统赠送");
			}

			String out_trade_body = StringUtil.formatStr(fDTransactionsResult.getOut_trade_body());
			// 检测是否是扫码支付，如果是扫码，则调用消息推送
			if (!StringUtils.isEmpty(out_trade_body)) {
				Map<String, Object> outTradeBodyMap = FastJsonUtil.jsonToMap(out_trade_body);
				String security_code = StringUtil.formatStr(outTradeBodyMap.get("security_code"));
				String security_code_type = StringUtil.formatStr(outTradeBodyMap.get("security_code_type"));
				if (!StringUtils.isEmpty(security_code) && !StringUtils.isEmpty(security_code_type)) {
					// 调用消息推送
					Map<String, String> extrasMap = new HashMap<String, String>();
					extrasMap.put("type", Constant.PUSH_MESSAGE_TYPE_01);
					extrasMap.put("amount", fDTransactionsResult.getAmount() + "");
					extrasMap.put("security_code", security_code);
					String msg = "交易成功\n金额:" + fDTransactionsResult.getAmount() + "元";
					String member_id = StringUtil.formatStr(outTradeBodyMap.get("buyer_id"));
					// 如果是收款码(P付,R收),给买家进行消息推送
					if (security_code_type.equals("R")) {
						member_id = StringUtil.formatStr(outTradeBodyMap.get("seller_id"));
					}
					notifyService.pushAppMessage(member_id, msg, extrasMap);
				}
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 订单赠送
	 * 
	 * @Title: orderRewardSubAccounts
	 * @param fDTransactionsResult
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 * @author tiny
	 */
	private void orderRewardSubAccounts(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方出账,卖家入账
			String payment_way_key = fDTransactionsResult.getPayment_way_key();
			if (payment_way_key.equals(Constant.PAYMENT_WAY_05)) {
				cashChange(fDTransactionsResult, fDTransactionsResult.getBuyer_id(),
						fDTransactionsResult.getSeller_id());
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_06)) {
				voucherChange(fDTransactionsResult, fDTransactionsResult.getBuyer_id(),
						fDTransactionsResult.getSeller_id());
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_08)) {
				goldChange(fDTransactionsResult, fDTransactionsResult.getBuyer_id(),
						fDTransactionsResult.getSeller_id());
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 余额提现
	 * 
	 * @Title: balanceWithdrawSubAccounts
	 * @param fDTransactionsResult
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 * @author tiny
	 */
	private void balanceWithdrawSubAccounts(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方出账,公司虚拟账户入账
			cashChange(fDTransactionsResult, fDTransactionsResult.getBuyer_id(), fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 余额提现退款
	 * 
	 * @Title: balanceWithdrawRefundSubAccounts
	 * @param fDTransactionsResult
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 * @author tiny
	 */
	private void balanceWithdrawRefundSubAccounts(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			cashChange(fDTransactionsResult, fDTransactionsResult.getBuyer_id(), fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 订单退款
	 * 
	 * @param fDTransactionsResult
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void orderRefundSubAccounts(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			String payment_way_key = fDTransactionsResult.getPayment_way_key();
			// 买方入账,卖方出账
			if (payment_way_key.equals(Constant.PAYMENT_WAY_05)) {
				// 零钱
				huiPay(fDTransactionsResult);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_06)) {
				// 礼券冲正
				voucherPay(fDTransactionsResult);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_08)) {
				// 金币支付
				goldPay(fDTransactionsResult);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 交易冲正
	 * 
	 * @param fDTransactionsResult
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void transactionReverseSubAccounts(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方入账,买方出账
			String payment_way_key = fDTransactionsResult.getPayment_way_key();
			if (payment_way_key.equals(Constant.PAYMENT_WAY_05)) {
				// 零钱
				huiPay(fDTransactionsResult);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_06)) {
				// 礼券冲正
				voucherPay(fDTransactionsResult);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_08)) {
				// 金币冲正
				goldPay(fDTransactionsResult);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_09)) {
				// 积分冲正
				pointPay(fDTransactionsResult);
			}

		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 订单结算
	 * 
	 * @param fDTransactionsResult
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void orderSettlement(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方出,买方入
			String payment_way_key = fDTransactionsResult.getPayment_way_key();
			if (payment_way_key.equals(Constant.PAYMENT_WAY_05)) {
				// 零钱
				huiPay(fDTransactionsResult);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 资产清零分账
	 * 
	 * @param fDTransactionsResult
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void assetClearSubAccounts(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方入账,买方出账
			String payment_way_key = fDTransactionsResult.getPayment_way_key();
			if (payment_way_key.equals(Constant.PAYMENT_WAY_05)) {

			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_06)) {
				// 礼券清零
				voucherClear(fDTransactionsResult, fDTransactionsResult.getBuyer_id(),
						fDTransactionsResult.getSeller_id());
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 订单支付
	 * 
	 * @param fDTransactionsResult
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void orderPaySubAccounts(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			String payment_way_key = fDTransactionsResult.getPayment_way_key();
			if (payment_way_key.equals(Constant.PAYMENT_WAY_01)) {
				// 支付宝
				alipayPay(fDTransactionsResult);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_02)) {
				// 微信支付
				wechatPay(fDTransactionsResult);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_05)) {
				// 零钱
				huiPay(fDTransactionsResult);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_06)) {
				// 礼券支付
				voucherPay(fDTransactionsResult);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_07)) {
				// 亲情卡支付
				perPay(fDTransactionsResult);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_08)) {
				// 金币支付
				goldPay(fDTransactionsResult);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_09)) {
				// 积分支付 丁忍2017-9-14新增
				pointPay(fDTransactionsResult);
			}
			// 交易完成后通知
			if (!StringUtils.isEmpty(fDTransactionsResult.getOrder_type_key())) {
				notifyService.tradeNotify(fDTransactionsResult);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 支付宝扫码支付
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void alipayCodePay(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 每天惠平台出账,卖方入账
			cashChange(fDTransactionsResult, Constant.MEMBER_ID_PLATFORM, fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 支付宝支付
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void alipayPay(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 每天惠平台出账,卖方入账
			cashChange(fDTransactionsResult, Constant.MEMBER_ID_PLATFORM, fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 微信扫码支付
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void wechatCodePay(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 每天惠平台出账,卖方入账
			cashChange(fDTransactionsResult, Constant.MEMBER_ID_PLATFORM, fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 微信支付
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void wechatPay(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 每天惠平台出账,卖方入账
			cashChange(fDTransactionsResult, Constant.MEMBER_ID_PLATFORM, fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 翼支付扫码支付
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void bestCodePay(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 每天惠平台出账,卖方入账
			cashChange(fDTransactionsResult, Constant.MEMBER_ID_PLATFORM, fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 零钱支付
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void huiPay(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方出账,卖方入账
			cashChange(fDTransactionsResult, fDTransactionsResult.getBuyer_id(), fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 亲情卡支付
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void perPay(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方出账,卖方入账
			cashChange(fDTransactionsResult, fDTransactionsResult.getBuyer_id(), fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 礼券支付
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void voucherPay(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方出账,卖方入账
			voucherChange(fDTransactionsResult, fDTransactionsResult.getBuyer_id(),
					fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 金币支付
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void goldPay(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方出账,卖方入账
			goldChange(fDTransactionsResult, fDTransactionsResult.getBuyer_id(), fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 账户余额交易
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void cashChange(FDTransactionsResult fDTransactionsResult, String out_member_id, String in_member_id)
			throws BusinessException, SystemException, Exception {
		try {
			// 交易礼券金额
			BigDecimal amount = fDTransactionsResult.getAmount();
			/** 出账方资产变更 **/
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", out_member_id);
			FDMemberAsset out_member = financeDao.selectFDMemberAsset(tempMap);

			// 记录日志
			memberCashChangeDaily(fDTransactionsResult, out_member, Constant.CATEGORY_EXPENDITURE, amount.negate());
			// 资产变更
			memberCashUpdate(fDTransactionsResult, out_member, Constant.CATEGORY_EXPENDITURE, amount.negate(),
					"3-金额出账");

			/** 入账方资产变更 **/
			tempMap.clear();
			tempMap.put("member_id", in_member_id);
			FDMemberAsset in_member = financeDao.selectFDMemberAsset(tempMap);
			// 记录日志
			memberCashChangeDaily(fDTransactionsResult, in_member, Constant.CATEGORY_INCOME, amount);
			// 资产变更
			memberCashUpdate(fDTransactionsResult, in_member, Constant.CATEGORY_INCOME, amount, "4-金额入账");
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 礼券交易
	 * 
	 * @param fDTransactionsResult
	 * @param out_member_id
	 * @param in_member_id
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void voucherChange(FDTransactionsResult fDTransactionsResult, String out_member_id, String in_member_id)
			throws BusinessException, SystemException, Exception {
		try {
			// 交易礼券金额
			BigDecimal amount = fDTransactionsResult.getAmount();
			/** 出账方资产变更 **/
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", out_member_id);
			FDMemberAsset out_member = financeDao.selectFDMemberAsset(tempMap);

			// 记录日志
			memberVoucherChangeDaily(fDTransactionsResult, out_member, Constant.CATEGORY_EXPENDITURE, amount.negate());
			// 资产变更
			memberVoucherUpdate(fDTransactionsResult, out_member, Constant.CATEGORY_EXPENDITURE, amount.negate(),
					"3-金额出账");

			/** 入账方资产变更 **/
			tempMap.clear();
			tempMap.put("member_id", in_member_id);
			FDMemberAsset in_member = financeDao.selectFDMemberAsset(tempMap);
			// 记录日志
			memberVoucherChangeDaily(fDTransactionsResult, in_member, Constant.CATEGORY_INCOME, amount);
			// 资产变更
			memberVoucherUpdate(fDTransactionsResult, in_member, Constant.CATEGORY_INCOME, amount, "4-金额入账");
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 礼券交易
	 * 
	 * @param fDTransactionsResult
	 * @param out_member_id
	 * @param in_member_id
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void voucherClear(FDTransactionsResult fDTransactionsResult, String out_member_id, String in_member_id)
			throws BusinessException, SystemException, Exception {
		try {
			/** 出账方资产变更 **/
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", out_member_id);
			FDMemberAsset out_member = financeDao.selectFDMemberAsset(tempMap);
			// 交易礼券金额
			BigDecimal amount = out_member.getVoucher_balance();

			// 记录日志
			memberVoucherChangeDaily(fDTransactionsResult, out_member, Constant.CATEGORY_EXPENDITURE, amount.negate());
			// 资产变更
			memberVoucherUpdate(fDTransactionsResult, out_member, Constant.CATEGORY_EXPENDITURE, amount.negate(),
					"3-金额出账");

			/** 入账方资产变更 **/
			tempMap.clear();
			tempMap.put("member_id", in_member_id);
			FDMemberAsset in_member = financeDao.selectFDMemberAsset(tempMap);
			// 记录日志
			memberVoucherChangeDaily(fDTransactionsResult, in_member, Constant.CATEGORY_INCOME, amount);
			// 资产变更
			memberVoucherUpdate(fDTransactionsResult, in_member, Constant.CATEGORY_INCOME, amount, "4-金额入账");
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 金币交易
	 * 
	 * @param fDTransactionsResult
	 * @param out_member
	 * @param in_member
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void goldChange(FDTransactionsResult fDTransactionsResult, String out_member_id, String in_member_id)
			throws BusinessException, SystemException, Exception {
		try {
			// 交易金币金额
			BigDecimal amount = fDTransactionsResult.getAmount();
			/** 出账方资产变更 **/
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", out_member_id);
			FDMemberAsset out_member = financeDao.selectFDMemberAsset(tempMap);
			// 记录日志
			memberGoldChangeDaily(fDTransactionsResult, out_member, Constant.CATEGORY_EXPENDITURE, amount.negate());
			// 资产变更
			memberGoldUpdate(fDTransactionsResult, out_member, Constant.CATEGORY_EXPENDITURE, amount.negate(),
					"3-金额出账");

			/** 入账方资产变更 **/
			tempMap.clear();
			tempMap.put("member_id", in_member_id);
			FDMemberAsset in_member = financeDao.selectFDMemberAsset(tempMap);
			// 记录日志
			memberGoldChangeDaily(fDTransactionsResult, in_member, Constant.CATEGORY_INCOME, amount);
			// 资产变更
			memberGoldUpdate(fDTransactionsResult, in_member, Constant.CATEGORY_INCOME, amount, "4-金额入账");
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 会员账户余额变更
	 * 
	 * @param fDTransactionsResult
	 * @param member_id
	 *            会员id
	 * @param category
	 *            资产分类
	 * @param trade_amount
	 *            交易金额 区分正负号
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void memberCashUpdate(FDTransactionsResult fDTransactionsResult, FDMemberAsset member, String category,
			BigDecimal trade_amount, String step) throws BusinessException, SystemException, Exception {
		try {
			// 计算金额
			BigDecimal pre_balance = member.getCash_balance();
			BigDecimal balance = MoneyUtil.moneyAdd(pre_balance, trade_amount);
			FDMemberCashLog operateLog = new FDMemberCashLog();
			operateLog.setLog_id(IDUtil.getUUID());
			operateLog.setMember_id(member.getMember_id());
			operateLog.setMember_type_key(member.getMember_type_key());
			operateLog.setCategory(category);
			operateLog.setTracked_date(new Date());
			operateLog.setPre_balance(pre_balance);
			operateLog.setAmount(trade_amount);
			operateLog.setBalance(balance);
			Map<String, Object> remarkMap = new HashMap<String, Object>();
			remarkMap.put("transaction_no", fDTransactionsResult.getTransaction_no());
			remarkMap.put("business_type_key", fDTransactionsResult.getBusiness_type_key());
			remarkMap.put("payment_way_key", fDTransactionsResult.getPayment_way_key());
			remarkMap.put("detail", fDTransactionsResult.getDetail());
			// 判断余额变更的会员是买方，还是卖方
			if (member.getMember_id().equals(fDTransactionsResult.getBuyer_id())) {
				remarkMap.put("transaction_member_id", fDTransactionsResult.getSeller_id());
				remarkMap.put("transaction_member_type_key", fDTransactionsResult.getSeller_member_type());
				remarkMap.put("transaction_member_name", fDTransactionsResult.getSeller_name());
				remarkMap.put("transaction_member_contact", fDTransactionsResult.getSeller_contact());
			} else {
				remarkMap.put("transaction_member_id", fDTransactionsResult.getBuyer_id());
				remarkMap.put("transaction_member_type_key", fDTransactionsResult.getBuyer_member_type());
				remarkMap.put("transaction_member_name", fDTransactionsResult.getBuyer_name());
				remarkMap.put("transaction_member_contact", fDTransactionsResult.getBuyer_contact());
			}
			operateLog.setRemark(FastJsonUtil.toJson(remarkMap));
			// 记录余额变更日志
			transactionDao.insertFDMemberCashLog(operateLog);
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", member.getMember_id());
			tempMap.put("trade_cash_balance", trade_amount);
			Integer update_flag = financeDao.updateFDMemberAsset(tempMap);
			if (update_flag == 0) {
				throw new BusinessException(RspCode.TRADE_FAIL, "现金余额更新失败");
			}
			remarkMap.clear();
			remarkMap.put("payment_way_key", Constant.PAYMENT_WAY_MAP.get(fDTransactionsResult.getPayment_way_key()));
			remarkMap.put("business_type_key",
					Constant.BUSINESS_TYPE_MAP.get(fDTransactionsResult.getBusiness_type_key()));
			remarkMap.put("detail", fDTransactionsResult.getDetail());
			remarkMap.put("member_id", member.getMember_id());
			remarkMap.put("pre_balance", pre_balance + "");
			remarkMap.put("amount", trade_amount + "");
			remarkMap.put("balance", balance + "");
			transactionFlowCreate(fDTransactionsResult.getTransaction_no(), step, FastJsonUtil.toJson(remarkMap));

			// 发短信,非公司账号发送短信
			if (!member.getMember_type_key().equals(Constant.MEMBER_TYPE_COMPANY)) {
				String in_or_out = "支出";
				if (trade_amount.compareTo(BigDecimal.ZERO) > 0) {
					in_or_out = "入账";
				}
				String sms_info = "您的余额账户" + in_or_out + "了" + trade_amount.abs() + "元,时间:"
						+ DateUtil.date2Str(fDTransactionsResult.getTransaction_date(), DateUtil.fmt_yyyyMMddHHmmss);
				sendMsgNotify(member, sms_info);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 会员礼券资产更新
	 * 
	 * @param fDTransactionsResult
	 * @param member_id
	 *            会员id
	 * @param category
	 *            资产分类
	 * @param trade_amount
	 *            交易金额 区分正负号
	 * @param step
	 *            步数
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void memberVoucherUpdate(FDTransactionsResult fDTransactionsResult, FDMemberAsset member, String category,
			BigDecimal trade_amount, String step) throws BusinessException, SystemException, Exception {
		try {
			// 计算金额
			BigDecimal pre_balance = member.getVoucher_balance();
			BigDecimal balance = MoneyUtil.moneyAdd(pre_balance, trade_amount);
			FDMemberVoucherLog operateLog = new FDMemberVoucherLog();
			operateLog.setLog_id(IDUtil.getUUID());
			operateLog.setMember_id(member.getMember_id());
			operateLog.setMember_type_key(member.getMember_type_key());
			operateLog.setCategory(category);
			operateLog.setTracked_date(new Date());
			operateLog.setPre_balance(pre_balance);
			operateLog.setAmount(trade_amount);
			operateLog.setBalance(balance);
			Map<String, Object> remarkMap = new HashMap<String, Object>();
			remarkMap.put("transaction_no", fDTransactionsResult.getTransaction_no());
			remarkMap.put("business_type_key", fDTransactionsResult.getBusiness_type_key());
			remarkMap.put("payment_way_key", fDTransactionsResult.getPayment_way_key());
			remarkMap.put("detail", fDTransactionsResult.getDetail());
			// 如果是订单赠送
			if (fDTransactionsResult.getBusiness_type_key().equals(Constant.BUSINESS_TYPE_ORDERREWARD)) {
				if (member.getMember_id().equals(fDTransactionsResult.getBuyer_id())) {
					Map<String, Object> out_trade_body = FastJsonUtil
							.jsonToMap((String) fDTransactionsResult.getOut_trade_body());
					String mobile = StringUtil.formatStr(out_trade_body.get("mobile"));
					if (!"".equals(mobile)) {
						remarkMap.put("detail", mobile);
					} else {
						if (!fDTransactionsResult.getBuyer_contact().equals("")) {
							remarkMap.put("detail", fDTransactionsResult.getBuyer_contact());
						}
					}
				} else {
					if (!fDTransactionsResult.getBuyer_contact().equals("")) {
						remarkMap.put("detail", fDTransactionsResult.getBuyer_contact());
					}
				}
			}

			// 判断余额变更的会员是买方，还是卖方
			if (member.getMember_id().equals(fDTransactionsResult.getBuyer_id())) {
				remarkMap.put("transaction_member_id", fDTransactionsResult.getSeller_id());
				remarkMap.put("transaction_member_type_key", fDTransactionsResult.getSeller_member_type());
				remarkMap.put("transaction_member_name", fDTransactionsResult.getSeller_name());
				remarkMap.put("transaction_member_contact", fDTransactionsResult.getSeller_contact());
			} else {
				remarkMap.put("transaction_member_id", fDTransactionsResult.getBuyer_id());
				remarkMap.put("transaction_member_type_key", fDTransactionsResult.getBuyer_member_type());
				remarkMap.put("transaction_member_name", fDTransactionsResult.getBuyer_name());
				remarkMap.put("transaction_member_contact", fDTransactionsResult.getBuyer_contact());
			}
			operateLog.setRemark(FastJsonUtil.toJson(remarkMap));
			// 记录余额变更日志
			transactionDao.insertFDMemberVoucherLog(operateLog);
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", member.getMember_id());
			tempMap.put("trade_voucher_balance", trade_amount);
			Integer update_flag = financeDao.updateFDMemberAsset(tempMap);
			if (update_flag == 0) {
				throw new BusinessException(RspCode.TRADE_FAIL, "礼券余额更新失败");
			}
			remarkMap.clear();
			remarkMap.put("payment_way_key", Constant.PAYMENT_WAY_MAP.get(fDTransactionsResult.getPayment_way_key()));
			remarkMap.put("business_type_key",
					Constant.BUSINESS_TYPE_MAP.get(fDTransactionsResult.getBusiness_type_key()));
			remarkMap.put("detail", fDTransactionsResult.getDetail());
			remarkMap.put("member_id", member.getMember_id());
			remarkMap.put("pre_balance", pre_balance + "");
			remarkMap.put("amount", trade_amount + "");
			remarkMap.put("balance", balance + "");
			transactionFlowCreate(fDTransactionsResult.getTransaction_no(), step, FastJsonUtil.toJson(remarkMap));

			// 发通知消息,非公司账号发送消息
			if (!member.getMember_type_key().equals(Constant.MEMBER_TYPE_COMPANY)) {
				String in_or_out = "支出";
				if (trade_amount.compareTo(BigDecimal.ZERO) > 0) {
					in_or_out = "入账";
				}
				String sms_info = "您的礼券账户" + in_or_out + "了" + trade_amount.abs() + "个礼券,时间:"
						+ DateUtil.date2Str(fDTransactionsResult.getTransaction_date(), DateUtil.fmt_yyyyMMddHHmmss);
				notifyService.financeChangeAppNotify(member.getMember_id(), sms_info);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 会员金币资产更新
	 * 
	 * @param fDTransactionsResult
	 * @param member_id
	 *            会员id
	 * @param category
	 *            资产分类
	 * @param trade_amount
	 *            交易金额 区分正负号
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void memberGoldUpdate(FDTransactionsResult fDTransactionsResult, FDMemberAsset member, String category,
			BigDecimal trade_amount, String step) throws BusinessException, SystemException, Exception {
		try {
			// 计算金额
			BigDecimal pre_balance = new BigDecimal(member.getGold());
			BigDecimal balance = MoneyUtil.moneyAdd(pre_balance, trade_amount);
			FDMemberGoldLog operateLog = new FDMemberGoldLog();
			operateLog.setLog_id(IDUtil.getUUID());
			operateLog.setMember_id(member.getMember_id());
			operateLog.setMember_type_key(member.getMember_type_key());
			operateLog.setCategory(category);
			operateLog.setTracked_date(new Date());
			operateLog.setPre_balance(pre_balance);
			operateLog.setAmount(trade_amount);
			operateLog.setBalance(balance);
			Map<String, Object> remarkMap = new HashMap<String, Object>();
			remarkMap.put("transaction_no", fDTransactionsResult.getTransaction_no());
			remarkMap.put("business_type_key", fDTransactionsResult.getBusiness_type_key());
			remarkMap.put("payment_way_key", fDTransactionsResult.getPayment_way_key());
			remarkMap.put("detail", fDTransactionsResult.getDetail());
			// 判断余额变更的会员是买方，还是卖方
			if (member.getMember_id().equals(fDTransactionsResult.getBuyer_id())) {
				remarkMap.put("transaction_member_id", fDTransactionsResult.getSeller_id());
				remarkMap.put("transaction_member_type_key", fDTransactionsResult.getSeller_member_type());
				remarkMap.put("transaction_member_name", StringUtil.formatStr(fDTransactionsResult.getSeller_name()));
				remarkMap.put("transaction_member_contact", fDTransactionsResult.getSeller_contact());
			} else {
				remarkMap.put("transaction_member_id", fDTransactionsResult.getBuyer_id());
				remarkMap.put("transaction_member_type_key", fDTransactionsResult.getBuyer_member_type());
				remarkMap.put("transaction_member_name", StringUtil.formatStr(fDTransactionsResult.getBuyer_name()));
				remarkMap.put("transaction_member_contact", fDTransactionsResult.getBuyer_contact());
			}
			operateLog.setRemark(FastJsonUtil.toJson(remarkMap));
			// 记录余额变更日志
			transactionDao.insertFDMemberGoldLog(operateLog);
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", member.getMember_id());
			tempMap.put("trade_gold", trade_amount);
			Integer update_flag = financeDao.updateFDMemberAsset(tempMap);
			if (update_flag == 0) {
				throw new BusinessException(RspCode.TRADE_FAIL, "金币余额更新失败");
			}
			remarkMap.clear();
			remarkMap.put("payment_way_key", Constant.PAYMENT_WAY_MAP.get(fDTransactionsResult.getPayment_way_key()));
			remarkMap.put("business_type_key",
					Constant.BUSINESS_TYPE_MAP.get(fDTransactionsResult.getBusiness_type_key()));
			remarkMap.put("detail", fDTransactionsResult.getDetail());
			remarkMap.put("member_id", member.getMember_id());
			remarkMap.put("pre_balance", pre_balance + "");
			remarkMap.put("amount", trade_amount + "");
			remarkMap.put("balance", balance + "");
			transactionFlowCreate(fDTransactionsResult.getTransaction_no(), step, FastJsonUtil.toJson(remarkMap));

			// 发通知消息,非公司账号发送消息
			if (!member.getMember_type_key().equals(Constant.MEMBER_TYPE_COMPANY)) {
				String in_or_out = "支出";
				if (trade_amount.compareTo(BigDecimal.ZERO) > 0) {
					in_or_out = "入账";
				}
				String sms_info = "您的金币账户" + in_or_out + "了" + trade_amount.abs() + "个金币,时间:"
						+ DateUtil.date2Str(fDTransactionsResult.getTransaction_date(), DateUtil.fmt_yyyyMMddHHmmss);
				notifyService.financeChangeAppNotify(member.getMember_id(), sms_info);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 
	 * @param fDTransactionsResult
	 * @param member_id
	 *            会员id
	 * @param finance_type
	 *            交易类型
	 * @param trade_amount
	 *            交易金额 区分正负号
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void memberCashChangeDaily(FDTransactionsResult fDTransactionsResult, FDMemberAsset member,
			String finance_type, BigDecimal trade_amount) throws BusinessException, SystemException, Exception {
		try {
			// 计算金额
			BigDecimal pre_balance = member.getCash_balance();
			BigDecimal balance = MoneyUtil.moneyAdd(pre_balance, trade_amount);
			// 创建日记账对象
			FDCashDailyAccountMember daily = new FDCashDailyAccountMember();
			BeanConvertUtil.copyProperties(daily, fDTransactionsResult);
			daily.setDaily_account_id(IDUtil.getUUID());
			daily.setMember_id(member.getMember_id());
			if (member.getMember_id().equals(fDTransactionsResult.getBuyer_id())) {
				daily.setTransaction_member_id(fDTransactionsResult.getSeller_id());
				daily.setTransaction_member_type(fDTransactionsResult.getSeller_member_type());
				daily.setTransaction_member_name(fDTransactionsResult.getSeller_name());
				daily.setTransaction_member_contact(fDTransactionsResult.getSeller_contact());
			} else {
				daily.setTransaction_member_id(fDTransactionsResult.getBuyer_id());
				daily.setTransaction_member_type(fDTransactionsResult.getBuyer_member_type());
				daily.setTransaction_member_name(fDTransactionsResult.getBuyer_name());
				daily.setTransaction_member_contact(fDTransactionsResult.getBuyer_contact());
			}
			daily.setBooking_mark(finance_type);
			daily.setPre_balance(pre_balance);
			daily.setAmount(trade_amount);
			daily.setBalance(balance);
			daily.setAccount_date(new Date());
			String member_type = member.getMember_type_key();
			if (member_type.equals(Constant.MEMBER_TYPE_CONSUMER)) {
				transactionDao.insertFDCashDailyAccountConsumer(daily);
			} else if (member_type.equals(Constant.MEMBER_TYPE_STORES)) {
				transactionDao.insertFDCashDailyAccountStore(daily);
			} else if (member_type.equals(Constant.MEMBER_TYPE_SUPPLIER)) {
				transactionDao.insertFDCashDailyAccountSupplier(daily);
			} else if (member_type.equals(Constant.MEMBER_TYPE_COMPANY)) {
				transactionDao.insertFDCashDailyAccount(daily);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 会员账户余额变更日记账
	 * 
	 * @param fDTransactionsResult
	 * @param member_id
	 *            会员id
	 * @param finance_type
	 *            交易类型
	 * @param trade_amount
	 *            交易金额 区分正负号
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void memberVoucherChangeDaily(FDTransactionsResult fDTransactionsResult, FDMemberAsset member,
			String finance_type, BigDecimal trade_amount) throws BusinessException, SystemException, Exception {
		try {

			// 计算金额
			BigDecimal pre_balance = member.getVoucher_balance();
			BigDecimal balance = MoneyUtil.moneyAdd(pre_balance, trade_amount);
			// 创建日记账对象
			FDVoucherDailyAccountMember daily = new FDVoucherDailyAccountMember();
			BeanConvertUtil.copyProperties(daily, fDTransactionsResult);
			daily.setDaily_account_id(IDUtil.getUUID());
			daily.setMember_id(member.getMember_id());
			if (member.getMember_id().equals(fDTransactionsResult.getBuyer_id())) {
				daily.setTransaction_member_id(fDTransactionsResult.getSeller_id());
				daily.setTransaction_member_type(fDTransactionsResult.getSeller_member_type());
				daily.setTransaction_member_name(fDTransactionsResult.getSeller_name());
				daily.setTransaction_member_contact(fDTransactionsResult.getSeller_contact());
			} else {
				daily.setTransaction_member_id(fDTransactionsResult.getBuyer_id());
				daily.setTransaction_member_type(fDTransactionsResult.getBuyer_member_type());
				daily.setTransaction_member_name(fDTransactionsResult.getBuyer_name());
				daily.setTransaction_member_contact(fDTransactionsResult.getBuyer_contact());
			}
			daily.setBooking_mark(finance_type);
			daily.setPre_balance(pre_balance);
			daily.setAmount(trade_amount);
			daily.setBalance(balance);
			daily.setAccount_date(new Date());
			// 如果是订单赠送
			if (fDTransactionsResult.getBusiness_type_key().equals(Constant.BUSINESS_TYPE_ORDERREWARD)) {
				if (member.getMember_id().equals(fDTransactionsResult.getBuyer_id())) {
					Map<String, Object> out_trade_body = FastJsonUtil
							.jsonToMap((String) fDTransactionsResult.getOut_trade_body());
					String mobile = StringUtil.formatStr(out_trade_body.get("mobile"));
					if (!"".equals(mobile)) {
						daily.setDetail(mobile);
					} else {
						if (!fDTransactionsResult.getBuyer_contact().equals("")) {
							daily.setDetail(fDTransactionsResult.getBuyer_contact());
						}
					}
				} else {
					if (!fDTransactionsResult.getBuyer_contact().equals("")) {
						daily.setDetail(fDTransactionsResult.getBuyer_contact());
					}
				}
			}
			String member_type = member.getMember_type_key();
			if (member_type.equals(Constant.MEMBER_TYPE_CONSUMER)) {
				transactionDao.insertFDVoucherDailyAccountConsumer(daily);
			} else if (member_type.equals(Constant.MEMBER_TYPE_STORES)) {
				transactionDao.insertFDVoucherDailyAccountStore(daily);
			} else if (member_type.equals(Constant.MEMBER_TYPE_SUPPLIER)) {
				transactionDao.insertFDVoucherDailyAccountSupplier(daily);
			} else if (member_type.equals(Constant.MEMBER_TYPE_COMPANY)) {
				transactionDao.insertFDVoucherDailyAccount(daily);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 会员账户余额变更日记账
	 * 
	 * @param fDTransactionsResult
	 * @param member_id
	 *            会员id
	 * @param finance_type
	 *            交易类型
	 * @param trade_amount
	 *            交易金额 区分正负号
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void memberGoldChangeDaily(FDTransactionsResult fDTransactionsResult, FDMemberAsset member,
			String finance_type, BigDecimal trade_amount) throws BusinessException, SystemException, Exception {
		try {
			// 计算金额
			BigDecimal pre_balance = new BigDecimal(member.getGold());
			BigDecimal balance = MoneyUtil.moneyAdd(pre_balance, trade_amount);
			// 创建日记账对象
			FDGoldDailyAccountMember daily = new FDGoldDailyAccountMember();
			BeanConvertUtil.copyProperties(daily, fDTransactionsResult);
			daily.setDaily_account_id(IDUtil.getUUID());
			daily.setMember_id(member.getMember_id());
			if (member.getMember_id().equals(fDTransactionsResult.getBuyer_id())) {
				daily.setTransaction_member_id(fDTransactionsResult.getSeller_id());
				daily.setTransaction_member_type(fDTransactionsResult.getSeller_member_type());
				daily.setTransaction_member_name(fDTransactionsResult.getSeller_name());
				daily.setTransaction_member_contact(fDTransactionsResult.getSeller_contact());
			} else {
				daily.setTransaction_member_id(fDTransactionsResult.getBuyer_id());
				daily.setTransaction_member_type(fDTransactionsResult.getBuyer_member_type());
				daily.setTransaction_member_name(fDTransactionsResult.getBuyer_name());
				daily.setTransaction_member_contact(fDTransactionsResult.getBuyer_contact());
			}
			daily.setBooking_mark(finance_type);
			daily.setPre_balance(pre_balance);
			daily.setAmount(trade_amount);
			daily.setBalance(balance);
			daily.setAccount_date(new Date());
			String member_type = member.getMember_type_key();
			if (member_type.equals(Constant.MEMBER_TYPE_CONSUMER)) {
				transactionDao.insertFDGoldDailyAccountConsumer(daily);
			} else if (member_type.equals(Constant.MEMBER_TYPE_STORES)) {
				transactionDao.insertFDGoldDailyAccountStore(daily);
			} else if (member_type.equals(Constant.MEMBER_TYPE_SUPPLIER)) {

			} else if (member_type.equals(Constant.MEMBER_TYPE_COMPANY)) {
				transactionDao.insertFDGoldDailyAccount(daily);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	private void fDTransactionResultUpdate(FDTransactionsResult fDTransactionsResult, boolean error, String remark) {
		// 如果交易异常了则需要更新交易结果中交易的状态
		try {
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("transaction_no", fDTransactionsResult.getTransaction_no());
			String transaction_status = Constant.TRANSACTION_STATUS_COMPLETED;
			if (error) {
				transaction_status = Constant.TRANSACTION_STATUS_ERROR;
			}
			tempMap.put("transaction_status", transaction_status);
			transactionDao.updateFDTransactionsResult(tempMap);
			tempMap.put("flow_key", fDTransactionsResult.getBusiness_type_key());
			tempMap.put("remark", remark);

			Map<String, Object> remarkMap = new HashMap<String, Object>();
			remarkMap.put("payment_way_key", Constant.PAYMENT_WAY_MAP.get(fDTransactionsResult.getPayment_way_key()));
			remarkMap.put("business_type_key",
					Constant.BUSINESS_TYPE_MAP.get(fDTransactionsResult.getBusiness_type_key()));
			remarkMap.put("detail", fDTransactionsResult.getDetail());
			transactionFlowCreate(fDTransactionsResult.getTransaction_no(), "5-交易成功", FastJsonUtil.toJson(remarkMap));
		} catch (BusinessException e) {
			logger.error(e.getMsg());
		} catch (SystemException e) {
			logger.error("更新交易结果表异常," + e.getMessage());
		} catch (Exception e) {
			logger.error("更新交易结果表异常," + e.getMessage());
		}
	}

	@Override
	public void transactionFlowCreate(String transaction_no, String flow_key, String remark) {
		// 如果交易异常了则需要更新交易结果中交易的状态
		try {
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("flow_id", IDUtil.getUUID());
			tempMap.put("transaction_no", transaction_no);
			tempMap.put("flow_key", flow_key);
			tempMap.put("remark", remark);
			tempMap.put("tracked_date", new Date());
			transactionDao.insertFDTransactionFlow(tempMap);
		} catch (BusinessException e) {
			logger.error(e.getMsg());
		} catch (SystemException e) {
			logger.error("记录交易流程异常," + e.getMessage());
		} catch (Exception e) {
			logger.error("记录交易流程异常," + e.getMessage());
		}
	}

	@Override
	public void fDTransactionLogCreate(String transaction_no, String event) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("log_id", IDUtil.getUUID());
			map.put("transaction_no", transaction_no);
			map.put("tracked_date", new Date());
			map.put("event", event);
			transactionDao.insertFDTransactionLog(map);
		} catch (BusinessException e) {
			logger.error(e.getMsg());
		} catch (SystemException e) {
			logger.error("记录异常交易异常," + e.getMessage());
		} catch (Exception e) {
			logger.error("记录异常交易异常," + e.getMessage());
		}
	}

	@Override
	public void fDTransactionErrorLogCreate(String transaction_no, String event) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("log_id", IDUtil.getUUID());
			map.put("transaction_no", transaction_no);
			map.put("tracked_date", new Date());
			map.put("event", event);
			transactionDao.insertFDTransactionErrorLog(map);
		} catch (BusinessException e) {
			logger.error(e.getMsg());
		} catch (SystemException e) {
			logger.error("记录异常交易异常," + e.getMessage());
		} catch (Exception e) {
			logger.error("记录异常交易异常," + e.getMessage());
		}
	}

	@Override
	public void transactionStatusConfirmed(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		ValidateUtil.validateParams(paramsMap, new String[] { "data_source", "payment_way_key", "transaction_no" });
		String data_source = paramsMap.get("data_source") + "";
		String payment_way_key = paramsMap.get("payment_way_key") + "";
		String out_trade_no = paramsMap.get("transaction_no") + "";
		boolean flag = false;
		Map<String, Object> query_response = new HashMap<String, Object>();
		if (payment_way_key.equals(Constant.PAYMENT_WAY_01) || payment_way_key.equals(Constant.PAYMENT_WAY_10)) {
			String app_id = "";
			String private_key = "";
			if (payment_way_key.equals(Constant.PAYMENT_WAY_01)) {
				// 支付宝支付
				private_key = PropertiesConfigUtil.getProperty("alipay.partner_private_key");
				if (data_source.equals(Constant.DATA_SOURCE_SJLY_01)) {
					app_id = PropertiesConfigUtil.getProperty("alipay.consumer_app_id");
				} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_02)) {
					app_id = PropertiesConfigUtil.getProperty("alipay.store_app_id");
				} else {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "此数据来源的支付方式不存");
				}
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_10)) {
				// 支付宝面对面扫码支付
				app_id = PropertiesConfigUtil.getProperty("alipay.face_pay_app_id");
				private_key = PropertiesConfigUtil.getProperty("alipay.face_pay_open_api_private_key");
			}
			// 调用交易查询接口
			String status = alipayPayService.orderQuery(app_id, private_key, out_trade_no, query_response);
			if (status.equals(AlipayPayServiceImpl.ALIPAY_TRADE_SUCCESS)) {
				flag = true;
			}
		} else if (payment_way_key.equals(Constant.PAYMENT_WAY_02) || payment_way_key.equals(Constant.PAYMENT_WAY_11)) {
			// 微信支付和微信扫码支付
			String app_key = "";
			String app_id = "";
			String mch_id = "";
			String cert_local_path = "";
			String cert_password = "";
			if (data_source.equals(Constant.DATA_SOURCE_SJLY_01)) {
				app_key = PropertiesConfigUtil.getProperty("wechat.consumer_app_key");
				app_id = PropertiesConfigUtil.getProperty("wechat.consumer_app_id");
				mch_id = PropertiesConfigUtil.getProperty("wechat.consumer_mch_id");
				cert_local_path = PropertiesConfigUtil.getProperty("wechat.consumer_cert_local_path");
				cert_password = PropertiesConfigUtil.getProperty("wechat.consumer_cert_password");
			} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_02)
					|| data_source.equals(Constant.DATA_SOURCE_SJLY_04)) {
				app_key = PropertiesConfigUtil.getProperty("wechat.store_app_key");
				app_id = PropertiesConfigUtil.getProperty("wechat.store_app_id");
				mch_id = PropertiesConfigUtil.getProperty("wechat.store_mch_id");
				cert_local_path = PropertiesConfigUtil.getProperty("wechat.store_cert_local_path");
				cert_password = PropertiesConfigUtil.getProperty("wechat.store_cert_password");
			} else if (data_source.equals(Constant.DATA_SOURCE_SJLY_14)) {
				app_key = PropertiesConfigUtil.getProperty("wechat.cashier_app_key");
				app_id = PropertiesConfigUtil.getProperty("wechat.cashier_app_id");
				mch_id = PropertiesConfigUtil.getProperty("wechat.cashier_mch_id");
				cert_local_path = PropertiesConfigUtil.getProperty("wechat.cashier_cert_local_path");
				cert_password = PropertiesConfigUtil.getProperty("wechat.cashier_cert_password");
			} else {
				throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "此数据来源的支付方式不存");
			}
			// 调用交易查询接口
			String status = wechatPayService.orderQuery(app_key, app_id, mch_id, cert_local_path, cert_password,
					out_trade_no, query_response);
			if (status.equals(WechatPayServiceImpl.WECHAT_TRADE_SUCCESS)) {
				flag = true;
			}
		} else {
			throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "支付方式不存在");
		}
		// 如果交易已经完成,则调用交易确认接口
		if (flag) {
			// TODO 对交易进行撤销

		}
	}

	/**
	 * 记录亲情卡的交易记录
	 * 
	 * @Title: transPrepayCardAdd
	 * @param paramsMap
	 * @param transaction_no
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 * @author tiny
	 */
	private void transPrepayCardAdd(Map<String, Object> paramsMap, String transaction_no)
			throws BusinessException, SystemException, Exception {
		String card_no = StringUtil.formatStr(paramsMap.get("card_no"));
		if (!StringUtils.isEmpty(card_no)) {
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", paramsMap.get("seller_id"));
			FDMemberAsset seller = financeDao.selectFDMemberAsset(tempMap);
			Date date = new Date();
			tempMap.clear();
			tempMap.put("transaction_id", IDUtil.getUUID());
			tempMap.put("transaction_no", transaction_no);
			tempMap.put("member_id", paramsMap.get("buyer_id"));
			tempMap.put("transaction_member_type", seller.getMember_type_key());
			tempMap.put("transaction_member_id", seller.getMember_id());
			tempMap.put("card_no", card_no);
			tempMap.put("business_type_key", Constant.BUSINESS_TYPE_ORDERPAY);
			tempMap.put("payment_way_key", paramsMap.get("payment_way_key"));
			tempMap.put("transaction_date", date);
			tempMap.put("detail", paramsMap.get("detail"));
			tempMap.put("amount", "-" + paramsMap.get("amount"));
			tempMap.put("modified_date", date);
			tempMap.put("created_date", date);
			tempMap.put("remark", paramsMap.get("remark"));
			prepayCardDao.insertTransPrepayCard(tempMap);
		}
	}

	/**
	 * 新增见面礼
	 * 
	 * @Title: meetGiftAdd
	 * @param buyer_id
	 * @param seller_id
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 * @author tiny
	 */
	private void meetGiftAdd(String buyer_id, String seller_id) throws BusinessException, SystemException, Exception {

	}

	/**
	 * 消费者充值送金币
	 * 
	 * @param transaction_no
	 * @param paramsMap
	 */
	private void rewardConsumerGold(final String out_trade_no, final String member_id, final Integer amount,
			final String detail) {
		FinanceController.threadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// 如果是消费者充值,调用余额支付接口赠送金币
					if (amount > 0) {
						Map<String, Object> bizParams = new HashMap<String, Object>();
						bizParams.put("data_source", "SJLY_03");
						bizParams.put("payment_way_key", "ZFFS_08");
						bizParams.put("detail", detail);
						bizParams.put("amount", amount + "");
						bizParams.put("out_member_id", Constant.MEMBER_ID_MTH);
						bizParams.put("in_member_id", member_id);
						bizParams.put("out_trade_no", out_trade_no);
						bizParams.put("out_trade_body", FastJsonUtil.toJson(bizParams));
						logger.info("赠送消费者金币->" + bizParams.toString());
						balancePay(bizParams, new ResultData());
					}
				} catch (BusinessException e) {
					logger.info(e.getMsg());
				} catch (SystemException e) {
					logger.info(e.getMsg());
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
			}
		});
	}

	/**
	 * 信息通知
	 * 
	 * @param member
	 * @param sms_info
	 */
	private void sendMsgNotify(FDMemberAsset member, String sms_info) {
		// 资产信息变更通知
		notifyService.financeChangeSMSNotify(member.getMember_id(), member.getMember_type_key(), sms_info);
		notifyService.financeChangeAppNotify(member.getMember_id(), sms_info);
	}

	/**
	 * 扫码支付
	 * 
	 */
	@Override
	public void barCodePayForSalesassistant(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			ValidateUtil.validateParams(paramsMap, new String[] { "data_source", "payment_way_key", "auth_code",
					"amount", "member_id", "detail", "out_trade_body" });
			String payment_way_key = (String) paramsMap.get("payment_way_key");// 支付方式
			// 扫码方
			String scan_member = paramsMap.get("member_id") + "";
			paramsMap.remove("member_id");
			if (payment_way_key.equals(Constant.PAYMENT_WAY_10)) {
				// 支付宝扫码支付
				paramsMap.put("out_member_id", Constant.MEMBER_ID_PLATFORM);
				paramsMap.put("in_member_id", scan_member);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_11)) {
				// 微信扫码支付
				paramsMap.put("out_member_id", Constant.MEMBER_ID_PLATFORM);
				paramsMap.put("in_member_id", scan_member);
			}
			// 调用支付接口（支付宝和微信）
			balancePayForSalesassistant(paramsMap, result);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 余额支付 账户余额、礼券余额
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	@Override
	public void balancePayForSalesassistant(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		String transaction_no = "";
		try {
			// out_trade_no, 必传参数
			ValidateUtil.validateParams(paramsMap, new String[] { "data_source", "payment_way_key", "amount",
					"out_member_id", "in_member_id", "detail" });
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("buyer_id", paramsMap.get("out_member_id"));
			paramsMap.put("seller_id", paramsMap.get("in_member_id"));
			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			// 零钱支付
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_BALANCEPAY);
			String remark = Constant.DATA_SOURCE_MAP.get(paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get(paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get(paramsMap.get("payment_way_key")) + "|" + paramsMap.get("amount");
			paramsMap.put("remark", remark);

			// 将交易请求到交易表中
			transactionsCreate(paramsMap, result);
			// 校验交易对象是否为同一个人
			buyerSellerValidate(paramsMap.get("buyer_id") + "", paramsMap.get("seller_id") + "");
			if (MoneyUtil.moneyComp("0", (String) paramsMap.get("amount"))) {
				throw new BusinessException(RspCode.TRADE_AMOUNT_ERROR, RspCode.MSG.get(RspCode.TRADE_AMOUNT_ERROR));
			}

			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			transaction_no = (String) resultMap.get("transaction_no");
			// 数据来源
			String data_source = (String) paramsMap.get("data_source");
			// 支付方式
			String payment_way_key = (String) paramsMap.get("payment_way_key");
			boolean flag = true;
			if (payment_way_key.equals(Constant.PAYMENT_WAY_10)) {
				ValidateUtil.validateParams(paramsMap, new String[] { "auth_code" });
				if (!data_source.equals(Constant.DATA_SOURCE_SJLY_16)) {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "支付宝扫码支付功能暂未开通");
				}
				flag = false;
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("out_trade_no", transaction_no);
				tempMap.put("total_amount", paramsMap.get("amount") + "");
				tempMap.put("auth_code", paramsMap.get("auth_code") + "");
				tempMap.put("subject", paramsMap.get("detail") + "");
				// TODO 后期去掉
				String scan_flag = "new";
				alipayPayService.barCodePay(tempMap, scan_flag);
			} else if (payment_way_key.equals(Constant.PAYMENT_WAY_11)) {
				flag = false;
				ValidateUtil.validateParams(paramsMap, new String[] { "auth_code" });
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("out_trade_no", transaction_no);
				tempMap.put("total_fee", paramsMap.get("amount") + "");
				tempMap.put("auth_code", paramsMap.get("auth_code") + "");
				tempMap.put("body", paramsMap.get("detail") + "");
				String type = "";
				String app_key = "";
				String app_id = "";
				String mch_id = "";
				String cert_local_path = "";
				String cert_password = "";
				if (data_source.equals(Constant.DATA_SOURCE_SJLY_16)) {
					type = "搞掂";
					app_key = PropertiesConfigUtil.getProperty("wechat.salesassistant_app_key");
					app_id = PropertiesConfigUtil.getProperty("wechat.salesassistant_app_id");
					mch_id = PropertiesConfigUtil.getProperty("wechat.salesassistant_mch_id");
					cert_local_path = PropertiesConfigUtil.getProperty("wechat.salesassistant_cert_local_path");
					cert_password = PropertiesConfigUtil.getProperty("wechat.salesassistant_cert_password");
				} else {
					throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "微信扫码支付功能暂未开通");
				}
				// TODO 后期去掉
				String scan_flag = "new";
				wechatPayService.barCodePay(type, app_key, app_id, mch_id, cert_local_path, cert_password, tempMap,
						scan_flag);
			} else {
				throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, "支付方式不存在");
			}
			if (flag) {
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.put("transaction_no", transaction_no);
				tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
				result.setResultData(new ResultData());
				transactionConfirmed(tempMap, result);
			}
		} catch (BusinessException e) {
			fDTransactionLogCreate(transaction_no, "余额支付交易失败," + e.getMsg());
			throw e;
		} catch (SystemException e) {
			fDTransactionErrorLogCreate(transaction_no, "余额支付交易异常," + e.getMsg());
			throw e;
		} catch (Exception e) {
			fDTransactionErrorLogCreate(transaction_no, "余额支付交易异常," + e.getMessage());
			throw e;
		}
	}

	/**
	 * 积分支付
	 * 
	 * @param paramMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void pointPay(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方出账,卖方入账
			pointChange(fDTransactionsResult, fDTransactionsResult.getBuyer_id(), fDTransactionsResult.getSeller_id());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 积分交易
	 * 
	 * @param fDTransactionsResult
	 * @param out_member_id
	 * @param in_member_id
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void pointChange(FDTransactionsResult fDTransactionsResult, String out_member_id, String in_member_id)
			throws BusinessException, SystemException, Exception {
		try {
			// 交易礼券金额
			BigDecimal amount = fDTransactionsResult.getAmount();
			/** 出账方资产变更 **/
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", out_member_id);
			FDMemberAsset out_member = financeDao.selectFDMemberAsset(tempMap);

			// 记录日志
			memberPointChangeDaily(fDTransactionsResult, out_member, Constant.CATEGORY_EXPENDITURE, amount.negate());
			// 资产变更
			memberPointUpdate(fDTransactionsResult, out_member, Constant.CATEGORY_EXPENDITURE, amount.negate(),
					"3-金额出账");

			/** 入账方资产变更 **/
			tempMap.clear();
			tempMap.put("member_id", in_member_id);
			FDMemberAsset in_member = financeDao.selectFDMemberAsset(tempMap);
			// 记录日志
			memberPointChangeDaily(fDTransactionsResult, in_member, Constant.CATEGORY_INCOME, amount);
			// 资产变更
			memberPointUpdate(fDTransactionsResult, in_member, Constant.CATEGORY_INCOME, amount, "4-金额入账");
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 会员账户积分变更
	 * 
	 * @param fDTransactionsResult
	 * @param member_id
	 *            会员id
	 * @param category
	 *            资产分类
	 * @param trade_amount
	 *            交易金额 区分正负号
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void memberPointUpdate(FDTransactionsResult fDTransactionsResult, FDMemberAsset member, String category,
			BigDecimal trade_amount, String step) throws BusinessException, SystemException, Exception {
		try {
			// 计算金额
			BigDecimal pre_balance = new BigDecimal(member.getBonus());
			BigDecimal balance = MoneyUtil.moneyAdd(pre_balance, trade_amount);
			FDMemberPointLog operateLog = new FDMemberPointLog();
			operateLog.setLog_id(IDUtil.getUUID());
			operateLog.setMember_id(member.getMember_id());
			operateLog.setMember_type_key(member.getMember_type_key());
			operateLog.setCategory(category);
			operateLog.setTracked_date(new Date());
			operateLog.setPre_balance(pre_balance);
			operateLog.setAmount(trade_amount);
			operateLog.setBalance(balance);
			Map<String, Object> remarkMap = new HashMap<String, Object>();
			remarkMap.put("transaction_no", fDTransactionsResult.getTransaction_no());
			remarkMap.put("business_type_key", fDTransactionsResult.getBusiness_type_key());
			remarkMap.put("payment_way_key", fDTransactionsResult.getPayment_way_key());
			remarkMap.put("detail", fDTransactionsResult.getDetail());
			// 判断余额变更的会员是买方，还是卖方
			if (member.getMember_id().equals(fDTransactionsResult.getBuyer_id())) {
				remarkMap.put("transaction_member_id", fDTransactionsResult.getSeller_id());
				remarkMap.put("transaction_member_type_key", fDTransactionsResult.getSeller_member_type());
				remarkMap.put("transaction_member_name", fDTransactionsResult.getSeller_name());
				remarkMap.put("transaction_member_contact", fDTransactionsResult.getSeller_contact());
			} else {
				remarkMap.put("transaction_member_id", fDTransactionsResult.getBuyer_id());
				remarkMap.put("transaction_member_type_key", fDTransactionsResult.getBuyer_member_type());
				remarkMap.put("transaction_member_name", fDTransactionsResult.getBuyer_name());
				remarkMap.put("transaction_member_contact", fDTransactionsResult.getBuyer_contact());
			}
			operateLog.setRemark(FastJsonUtil.toJson(remarkMap));
			// 记录余额变更日志
			transactionDao.insertFDMemberPointLog(operateLog);
			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("member_id", member.getMember_id());
			tempMap.put("trade_bonus", trade_amount);
			Integer update_flag = financeDao.updateFDMemberAsset(tempMap);
			if (update_flag == 0) {
				throw new BusinessException(RspCode.TRADE_FAIL, "现金余额更新失败");
			}
			remarkMap.clear();
			remarkMap.put("payment_way_key", Constant.PAYMENT_WAY_MAP.get(fDTransactionsResult.getPayment_way_key()));
			remarkMap.put("business_type_key",
					Constant.BUSINESS_TYPE_MAP.get(fDTransactionsResult.getBusiness_type_key()));
			remarkMap.put("detail", fDTransactionsResult.getDetail());
			remarkMap.put("member_id", member.getMember_id());
			remarkMap.put("pre_balance", pre_balance + "");
			remarkMap.put("amount", trade_amount + "");
			remarkMap.put("balance", balance + "");
			transactionFlowCreate(fDTransactionsResult.getTransaction_no(), step, FastJsonUtil.toJson(remarkMap));

			// 发短信,非公司账号发送短信
			if (!member.getMember_type_key().equals(Constant.MEMBER_TYPE_COMPANY)) {
				String in_or_out = "支出";
				if (trade_amount.compareTo(BigDecimal.ZERO) > 0) {
					in_or_out = "入账";
				}
				String sms_info = "您的余额账户" + in_or_out + "了" + trade_amount.abs().intValue() + "积分,时间:"
						+ DateUtil.date2Str(fDTransactionsResult.getTransaction_date(), DateUtil.fmt_yyyyMMddHHmmss);
				sendMsgNotify(member, sms_info);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 会员账户积分变更日记账
	 * 
	 * @param fDTransactionsResult
	 * @param member_id
	 *            会员id
	 * @param finance_type
	 *            交易类型
	 * @param trade_amount
	 *            交易金额 区分正负号
	 * @throws BusinessException
	 * @throws SystemException
	 */
	private void memberPointChangeDaily(FDTransactionsResult fDTransactionsResult, FDMemberAsset member,
			String finance_type, BigDecimal trade_amount) throws BusinessException, SystemException, Exception {
		try {

			// 计算金额
			BigDecimal pre_balance = new BigDecimal(member.getBonus());
			BigDecimal balance = MoneyUtil.moneyAdd(pre_balance, trade_amount);
			// 创建日记账对象
			FDPointDailyAccountMember daily = new FDPointDailyAccountMember();
			BeanConvertUtil.copyProperties(daily, fDTransactionsResult);
			daily.setDaily_account_id(IDUtil.getUUID());
			daily.setMember_id(member.getMember_id());
			if (member.getMember_id().equals(fDTransactionsResult.getBuyer_id())) {
				daily.setTransaction_member_id(fDTransactionsResult.getSeller_id());
				daily.setTransaction_member_type(fDTransactionsResult.getSeller_member_type());
				daily.setTransaction_member_name(fDTransactionsResult.getSeller_name());
				daily.setTransaction_member_contact(fDTransactionsResult.getSeller_contact());
			} else {
				daily.setTransaction_member_id(fDTransactionsResult.getBuyer_id());
				daily.setTransaction_member_type(fDTransactionsResult.getBuyer_member_type());
				daily.setTransaction_member_name(fDTransactionsResult.getBuyer_name());
				daily.setTransaction_member_contact(fDTransactionsResult.getBuyer_contact());
			}
			daily.setBooking_mark(finance_type);
			daily.setPre_balance(pre_balance);
			daily.setAmount(trade_amount);
			daily.setBalance(balance);
			daily.setAccount_date(new Date());

			String member_type = member.getMember_type_key();
			if (member_type.equals(Constant.MEMBER_TYPE_CONSUMER)) {
				transactionDao.insertFDPointDailyAccountConsumer(daily);
			} else if (member_type.equals(Constant.MEMBER_TYPE_STORES)) {
				transactionDao.insertFDPointDailyAccountStore(daily);
			} else if (member_type.equals(Constant.MEMBER_TYPE_COMPANY)) {
				transactionDao.insertFDPointDailyAccount(daily);
			}
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * pos支付___丁龙
	 */
	@Override
	public void posPayForSalesassistant(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {

		String transaction_no = "";
		try {
			// out_trade_no, 必传参数
			ValidateUtil.validateParams(paramsMap, new String[] { "data_source", "payment_way_key", "amount",
					"member_id", "detail", "out_trade_no", "out_trade_body", "transaction_no", "transaction_date" });
			String payment_way_key = (String) paramsMap.get("payment_way_key");
			String scan_member = paramsMap.get("member_id") + "";
			if (payment_way_key.equals(Constant.PAYMENT_WAY_22) || payment_way_key.equals(Constant.PAYMENT_WAY_23)
					|| payment_way_key.equals(Constant.PAYMENT_WAY_24)) {
				// pos支付
				paramsMap.put("out_member_id", Constant.MEMBER_ID_PLATFORM);
				paramsMap.put("in_member_id", scan_member);
			} else {
				throw new BusinessException(RspCode.PAYMENT_WAY_ERROR, RspCode.MSG.get(RspCode.PAYMENT_WAY_ERROR));
			}
			if (null == Constant.DATA_SOURCE_MAP.get((String) paramsMap.get("data_source"))) {
				throw new BusinessException(RspCode.DATA_SOURCE_ERROR, RspCode.MSG.get(RspCode.DATA_SOURCE_ERROR));
			}
			paramsMap.put("buyer_id", paramsMap.get("out_member_id"));
			paramsMap.put("seller_id", paramsMap.get("in_member_id"));
			paramsMap.put("currency_code", Constant.CURRENCY_CNY);
			// pos机支付
			paramsMap.put("business_type_key", Constant.BUSINESS_TYPE_POSPAY);
			paramsMap.put("order_type_key", Constant.ORDER_TYPE_HUIYIDING2);
			String remark = Constant.DATA_SOURCE_MAP.get(paramsMap.get("data_source")) + "|"
					+ Constant.BUSINESS_TYPE_MAP.get(paramsMap.get("business_type_key")) + "|"
					+ Constant.PAYMENT_WAY_MAP.get(paramsMap.get("payment_way_key")) + "|" + paramsMap.get("amount");
			paramsMap.put("remark", remark);

			// 将交易信息存入交易表中
			transactionsPOSPayCreate(paramsMap, result);
			// 校验交易对象是否为同一个人
			buyerSellerValidate(paramsMap.get("buyer_id") + "", paramsMap.get("seller_id") + "");
			// 判断传入的交易金额是否是正数
			if (MoneyUtil.moneyComp("0", (String) paramsMap.get("amount"))) {
				throw new BusinessException(RspCode.TRADE_AMOUNT_ERROR, RspCode.MSG.get(RspCode.TRADE_AMOUNT_ERROR));
			}

			Map<String, Object> resultMap = (Map<String, Object>) result.getResultData();
			transaction_no = (String) resultMap.get("transaction_no");

			Map<String, Object> tempMap = new HashMap<String, Object>();
			tempMap.put("transaction_no", transaction_no);
			tempMap.put("transaction_status", Constant.TRANSACTION_STATUS_CONFIRMED);
			result.setResultData(new ResultData());
			transactionConfirmed(tempMap, result);

		} catch (BusinessException e) {
			fDTransactionLogCreate(transaction_no, "POS支付交易失败," + e.getMsg());
			throw e;
		} catch (SystemException e) {
			fDTransactionErrorLogCreate(transaction_no, "PSO支付交易异常," + e.getMsg());
			throw e;
		} catch (Exception e) {
			fDTransactionErrorLogCreate(transaction_no, "POS支付交易异常," + e.getMessage());
			throw e;
		}

	}

	/**
	 * 记录交易日志(pos支付专用)___丁龙
	 * 
	 * @param paramsMap
	 * @param result
	 * @throws BusinessException
	 * @throws SystemException
	 * @throws Exception
	 */
	private void transactionsPOSPayCreate(Map<String, Object> paramsMap, ResultData result)
			throws BusinessException, SystemException, Exception {
		try {
			String transaction_no = (String) paramsMap.get("transaction_no");
			// 此处可以用map,可以用javaBean
			Date date = new Date();
			FDTransactions fDTransactions = new FDTransactions();
			BeanConvertUtil.mapToBean(fDTransactions, paramsMap);
			fDTransactions.setTransaction_id(IDUtil.getUUID());
			// 如果接口没有传入流水号,就创建一个流水号.(pos支付业务是接口中传入流水号)
			if (StringUtils.isBlank(transaction_no)) {
				fDTransactions.setTransaction_no(TradeIDUtil.getTradeNo());
			}
			// pos支付时,前端会传一个交易时间(我们只需要保存即可)
			if (StringUtils.isNotBlank((String) paramsMap.get("transaction_date"))) {
				Date date2 = DateUtil.str2Date((String) paramsMap.get("transaction_date"), DateUtil.fmt_yyyyMMddHHmmss);
				fDTransactions.setTransaction_date(date2);
			} else {
				fDTransactions.setTransaction_date(date);
			}
			fDTransactions.setCreated_date(date);

			// 判断交易流水号是否重复
			Map<String, Object> tmpMap = new HashMap<>();
			tmpMap.put("transaction_no", transaction_no);
			List<FDTransactions> ListFDTransactions = financeDao.selectListFDTransactions(tmpMap);
			if (ListFDTransactions.size() >= 1) {
				for (FDTransactions fdTransactions2 : ListFDTransactions) {
					if (paramsMap.get("out_trade_no").equals(fdTransactions2.getOut_trade_no())) {
						throw new BusinessException("交易流水号重复", "订单已支付");
					}
				}
				throw new BusinessException("交易流水号重复", "交易流水号重复");
			}

			// 将交易(详细)信息存入数据库中
			financeDao.insertFDTransactions(fDTransactions);
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("transaction_no", fDTransactions.getTransaction_no());
			result.setResultData(resultMap);
			// 将交易(简单)信息存入数据库中
			transactionFlowCreate(fDTransactions.getTransaction_no(), "1-生成交易", fDTransactions.getRemark());
			// 将交易日志存入日志表中
			fDTransactionLogCreate(fDTransactions.getTransaction_no(), fDTransactions.getRemark());
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * pos结算(pos支付专用)___丁龙
	 * 
	 * @param fDTransactionsResult
	 */
	private void posSettlement(FDTransactionsResult fDTransactionsResult)
			throws BusinessException, SystemException, Exception {
		try {
			// 买方出,买方入
			huiPay(fDTransactionsResult);
		} catch (BusinessException e) {
			throw e;
		} catch (SystemException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
}
