package com.meitianhui.finance.controller;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hisun.iposm.HiiposmUtil;
import com.meitianhui.common.exception.BusinessException;
import com.meitianhui.common.util.PropertiesConfigUtil;
import com.meitianhui.finance.service.TradeService;

/**
 * 微信异步结果通知
 * 
 * @author Tiny
 *
 */
@Controller
public class CMPayNotifyControler {

	private static Logger logger = Logger.getLogger(CMPayNotifyControler.class);

	@Autowired
	private TradeService tradeService;

	@RequestMapping(value = "/cmPayNotify")
	public void consumerWechatNotity(HttpServletRequest request, HttpServletResponse response) {
		//System.out.println("111111111111111111111111111111111111111111111111111111111");
		String type = "和包支付";
		String signKey = PropertiesConfigUtil.getProperty("cmpay.cmpay_sign_key");
		PrintWriter out = null;
		try {
			out = response.getWriter();
			String merchantId = request.getParameter("merchantId");
			String payNo = request.getParameter("payNo");
			String returnCode = request.getParameter("returnCode");
			String message = request.getParameter("message");
			String signType = request.getParameter("signType");
			String amount = request.getParameter("amount");
			String orderId = request.getParameter("orderId");
			String cashAmt = request.getParameter("cashAmt");
			String coupAmt = request.getParameter("coupAmt");
			String vchAmt = request.getParameter("vchAmt");
			String status = request.getParameter("status");
			String reserved1 = request.getParameter("reserved1");
			String orderDate = request.getParameter("orderDate");
			String hmac = request.getParameter("hmac");
			// 必输字段 非空验证
			if (merchantId == null) {
				merchantId = "";
			}
			if (payNo == null) {
				payNo = "";
			}
			if (returnCode == null) {
				returnCode = "";
			}
			if (message == null) {
				message = "";
			}
			if (signType == null) {
				signType = "MD5";
			}
			if (amount == null) {
				amount = "";
			}
			if (reserved1 == null) {
				reserved1 = "";
			}
			if (status == null) {
				status = "";
			}
			if (orderDate == null) {
				orderDate = "";
			}
			// 验签报文
			String signData = merchantId + payNo + returnCode + message + signType + amount + orderId + cashAmt
					+ coupAmt + vchAmt + status + reserved1 + orderDate;
			HiiposmUtil util = new HiiposmUtil();
			// 验签消息摘要
			String hmac1 = util.MD5Sign(signData, signKey);
			// 验签
			boolean sign_flag = util.MD5Verify(signData, hmac, signKey);

			if (sign_flag) {// 验签成功
				if (status != "SUCCESS") {
					out.println("支付结果异常");
					throw new BusinessException("和包支付异常", "支付结果异常");
				}
				logger.info(type + "和包支付异步结果通知:验签成功");
				out.println("SUCCESS");
			} else {
				out.println("验签失败！");
				throw new BusinessException("和包支付异常", "数据签名非法，有可能被第三方篡改");
			}
		} catch (Exception e) {
			out.println("交易异常:" + e.getMessage());
		} finally {
			if (null != out) {
				out.close();
			}
		}
	}
}
