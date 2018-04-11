package com.meitianhui.finance.util.refund.alipay;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.meitianhui.common.util.PropertiesConfigUtil;

public class AlipayRefundUtil {
	
	private static final Logger logger = Logger.getLogger(AlipayRefundUtil.class);

	public static boolean refund(Map<String,String> map){
		
		//String app_id="2016052701449563";
		//String private_key="MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMYEhtEHoeXW4sxBR0YEiaBKMr7h4vGY67WOwDvxqxqbHAnGihALMTgdo4qAJC9NgCz505/40oJ1MmFX6J09vCgGOxGVIu4hmjSfLPIJHfOWH2W5sI5sWIZDpJzNjRI8hFq1c0kcAMGBnW3W4QXvamlQmSuc41PvclAx4BHe8L7zAgMBAAECgYEAvgzSKAUfSaz4d/mfIoBwZrtQAQxj+GVAtTyRG/dStXJxcqBvGf7j+mvX7rIWCKBliMUua7cws60v8WWiCGicEEqJnV6/HRFMddgnXrT0Nmdi91j4lEM1qwtRPtGWWqNrjYGZhpZE8jlxR0prKZufOr6LK/Kw0c/cKwcf99J4k+ECQQD+0kHqOMKjxSj93m+lsJVG/8bkZpdR92qD9bw9W9A9X09Hj+dJvEokzkw+QxJr6V9BYjp+E+oDMpInRjwXfG5XAkEAxu8BjCkCvdJpXeMVYKGpaNLh8FR6G4b/pVMuPS1I3AlZUs1z0+GMUjqwXw7RrEPqTnyHJcIxSFoi7iXcq/0axQJBAPPdRmWgAm32nn8uY8y+jG3RFIe6wrAXxuEYyBg0iLpjPxzuU9tWDeLhZ0fiqnuJcLbDCdVku3xp5kMdzR5w36sCQQCd6myYDpYWiUkLNpBpvOr2QzEHzoeAApI9cuJpveDwiptVl7IyqADz5ZLMYr8euGrvEvtVheoJPUQtN/EMtkbhAkAc3ljPkpVh5zm1BSQZ+r9OlnlDQ6M0++4JOOKOeXRe0x5CCUJ/mmTe8ecFHlRbyZSoxk7Pw+bYMAozYJojPrZf";
		//String alipay_public_key="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";
		String refuneUrl="https://openapi.alipay.com/gateway.do";
		AlipayClient alipayClient = new DefaultAlipayClient(
				refuneUrl,
				PropertiesConfigUtil.getProperty("alipay.consumer_app_id"),
				PropertiesConfigUtil.getProperty("alipay.partner_private_key"),
				"json",
				"UTF-8",
				PropertiesConfigUtil.getProperty("alipay.face_pay_alipay_open_api_public_key"));
		AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
		map.put("out_trade_no", map.get("out_trade_no"));
		map.put("refund_amount", map.get("refund_amount"));
		map.put("refund_reason", "正常退款");
		request.setBizContent(new JSONObject(map).toString());
		AlipayTradeRefundResponse response;
		try {
			response = alipayClient.execute(request);
			logger.info("支付宝退款请求结果:"+response.getBody());
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return false;
		}
		return response.isSuccess();
	}
	
	public static void main(String[] args) throws AlipayApiException {
		String app_id="2016052701449563";
		String private_key="MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAMYEhtEHoeXW4sxBR0YEiaBKMr7h4vGY67WOwDvxqxqbHAnGihALMTgdo4qAJC9NgCz505/40oJ1MmFX6J09vCgGOxGVIu4hmjSfLPIJHfOWH2W5sI5sWIZDpJzNjRI8hFq1c0kcAMGBnW3W4QXvamlQmSuc41PvclAx4BHe8L7zAgMBAAECgYEAvgzSKAUfSaz4d/mfIoBwZrtQAQxj+GVAtTyRG/dStXJxcqBvGf7j+mvX7rIWCKBliMUua7cws60v8WWiCGicEEqJnV6/HRFMddgnXrT0Nmdi91j4lEM1qwtRPtGWWqNrjYGZhpZE8jlxR0prKZufOr6LK/Kw0c/cKwcf99J4k+ECQQD+0kHqOMKjxSj93m+lsJVG/8bkZpdR92qD9bw9W9A9X09Hj+dJvEokzkw+QxJr6V9BYjp+E+oDMpInRjwXfG5XAkEAxu8BjCkCvdJpXeMVYKGpaNLh8FR6G4b/pVMuPS1I3AlZUs1z0+GMUjqwXw7RrEPqTnyHJcIxSFoi7iXcq/0axQJBAPPdRmWgAm32nn8uY8y+jG3RFIe6wrAXxuEYyBg0iLpjPxzuU9tWDeLhZ0fiqnuJcLbDCdVku3xp5kMdzR5w36sCQQCd6myYDpYWiUkLNpBpvOr2QzEHzoeAApI9cuJpveDwiptVl7IyqADz5ZLMYr8euGrvEvtVheoJPUQtN/EMtkbhAkAc3ljPkpVh5zm1BSQZ+r9OlnlDQ6M0++4JOOKOeXRe0x5CCUJ/mmTe8ecFHlRbyZSoxk7Pw+bYMAozYJojPrZf";
		String alipay_public_key="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDI6d306Q8fIfCOaTXyiUeJHkrIvYISRcc73s3vF1ZT7XN8RNPwJxo8pWaJMmvyTn9N4HQ632qJBVHf8sxHi/fEsraprwCtzvzQETrNRwVxLO5jVmRGi60j8Ue1efIlzPXV9je9mkjzOmdssymZkh2QhUrCmZYI/FCEa3/cNMW0QIDAQAB";
		//String alipay_public_key="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
		String refuneUrl="https://openapi.alipay.com/gateway.do";
		AlipayClient alipayClient = new DefaultAlipayClient(refuneUrl,app_id,private_key,"json",
				"UTF-8",alipay_public_key);
		AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
		Map<String,String> map=new HashMap<String,String>();
		map.put("out_trade_no", "9824467327794585602896211001");
		//map.put("trade_no", "2017112611001004680073956807");
		map.put("refund_amount", "0.02");
		map.put("refund_reason", "正常退款");
		//map.put("refund_reason", "正常退款");
		//map.put("refund_reason", "正常退款");
		
		request.setBizContent(new JSONObject(map).toString());
		AlipayTradeRefundResponse response = alipayClient.execute(request);
		logger.info("支付宝退款请求结果:"+response.getBody());
		
		if(response.isSuccess()){
		System.out.println("调用成功");
		} else {
		System.out.println("调用失败");
		}
	}
}
