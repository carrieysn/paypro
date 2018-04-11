package com.meitianhui.finance.util.refund.weixin.common;

import java.util.UUID;

public class PayConfigUtil {
	
	//↓↓↓↓↓↓↓↓↓↓请在这里配置您的基本信息↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
	
	//微信分配的公众账号ID（企业号corpid即为此appId）
	//public static String WX_APPID="wx2ba2ecd5b368be10";
	
	//微信支付分配的商户号
	//public static String  WX_MCH_ID="1325257001";
	
	//秘钥
	//public static String API_NOTWEB_KEY="n3Kc45F8Urza2IVQKLAw2yTIafr8GRdo";
	
	// 秘钥
	//public static String MCH_API_KEY = "n3Kc45F8Urza2IVQKLAw2yTIafr8GRdo";

	public static final String WX_CERTFILEPATH = "D:/consumer_apiclient_cert.p12";
	
	//异步通知接口                                                 
	//public static String NOTIFY_URL= System.getProperty("wx.pay.notify.path");
	public static String NOTIFY_URL= "http://www.4ygx.com/app/pay/wxnotify";

	/** 微信客户端调用微信支付的指定包名 */
	public static final String WEIXIN_PREPAY_PACKAGE = "Sign=WXPay";

	/** 微信交易类型 APP支付 */
	public static final String WEIXIN_PREPAY_TRADE_TYPE_APP = "APP";

	/** 微信交易类型 网页支付 */
	public static final String WEIXIN_PREPAY_TRADE_TYPE_JSAPI = "JSAPI";

	//统一下单接口
	public static String UFDODER_URL="https://api.mch.weixin.qq.com/pay/unifiedorder";

	/**微信支付通知成功状态**/
	public static final String WECHAT_RESPONSE_SUCCESS = "SUCCESS";
	/**微信支付通知失败状态**/
	public static final String WECHAT_RESPONSE_FAIL = "FAIL";

	public static final String WECHAT_RESPONSE_OK = "OK";
	
	public static String get32UUID() {
		return UUID.randomUUID().toString().trim().replaceAll("-", "");
	}
	
	//↑↑↑↑↑↑↑↑↑↑请在这里配置您的基本信息↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
	
}
