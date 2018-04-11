package com.meitianhui.finance.util;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.hisun.iposm.HiiposmUtil;
import com.meitianhui.common.util.MD5Util;

public class CMPayHttpUtil {
	private static Logger log = Logger.getLogger(CMPayHttpUtil.class);

	public String httpPost(String url, Map<String, String> param) {
		String result = null;
		// 创建信任证书
		CloseableHttpClient httpClient = null;
		HttpPost httpPost = null;
		CloseableHttpResponse response = null;
		try {
			httpClient = createSSLClientDefault();
			httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			List<NameValuePair> paramList = new ArrayList<NameValuePair>();
			for (String key : param.keySet()) {
				paramList.add(new BasicNameValuePair(key, param.get(key)));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(paramList, Consts.UTF_8));
			httpPost.setConfig(RequestConfig.custom().setConnectTimeout(30000).setSocketTimeout(30000).build());
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();
			// 如果响应码是200
			if (HttpStatus.SC_OK == statusCode) {
				result = EntityUtils.toString(entity);
			}
		} catch (Exception e) {
			log.error("和包支付请求异常", e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				if (httpPost != null) {
					httpPost.releaseConnection();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				log.error("和包支付关闭http异常", e);
			}
		}
		return result;
	}

	// 创建链接
	public CloseableHttpClient createSSLClientDefault() throws Exception {
		SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new AllTrustStrategy()).build();
		SSLConnectionSocketFactory sslSf = new SSLConnectionSocketFactory(sslContext);
		return HttpClients.custom().setSSLSocketFactory(sslSf).build();
	}

	// 加载证书
	private class AllTrustStrategy implements TrustStrategy {
		public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
			return true;
		}
	}

	/**
	 * 组装mac加密明文串
	 * 
	 * @param oprId
	 * @param pikFlag
	 * @param userToken
	 * @param reserved1
	 * @param productName
	 * @param periodUnit
	 * @param period
	 * @param orderId
	 * @param orderDate
	 * @param currency
	 * @param amount
	 * @param version
	 * @param type
	 * @param signType
	 * @param requestId
	 * @param merchantId
	 * @param notifyUrl
	 * @param characterSet
	 * 
	 * @param merchantId
	 * @param orderNo
	 * @param orderReqNo
	 * @param orderDate
	 * @param barcode
	 * @param orderAmt
	 * @param key
	 * @param oprId
	 * @param pikFlag
	 * @param userToken
	 * @param reserved1
	 * @param productName
	 * @param periodUnit
	 * @param period
	 * @param orderId
	 * @param orderDate2
	 * @param currency
	 * @param amount
	 * @return
	 */
	public static String createOrderMacStr(String notifyUrl, String merchantId, String requestId, String signType,
			String type, String version, String amount, String currency, String orderDate, String orderId,
			String period, String periodUnit, String productName, String reserved1, String userToken, String pikFlag,
			String oprId, String signKey) {
		StringBuilder sb = new StringBuilder();
		sb.append(notifyUrl);
		sb.append(merchantId).append(requestId);
		sb.append(signType).append(type);
		sb.append(version).append(orderDate);
		sb.append(currency).append(orderDate);
		sb.append(orderId).append(period);
		sb.append(periodUnit).append(productName);
		sb.append(reserved1).append(userToken);
		sb.append(pikFlag).append(oprId);
		HiiposmUtil util = new HiiposmUtil();
		return util.MD5Sign(sb.toString(), signKey);
	}

	/**
	 * 组装mac加密明文串
	 * 
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
	 * @param signKey
	 * @param barcode
	 * @param orderAmt
	 * @param key
	 * @return
	 */
	public static String createOrderQueryMacStr(String merchantId, String requestId, String signType, String type,
			String version, String orderDate, String orderId, String signKey) {
		StringBuilder sb = new StringBuilder();
		sb.append(merchantId).append(requestId);
		sb.append(signType).append(type);
		sb.append(version).append(orderDate);
		sb.append(orderId);
		HiiposmUtil util = new HiiposmUtil();
		return util.MD5Sign(sb.toString(), signKey);
	}

	public static String createReverseMacStr(String merchantId, String requestId, String signType, String type,
			String version, String orequestId, String oorderDate, String signKey) {
		StringBuilder sb = new StringBuilder();
		sb.append(merchantId).append(requestId);
		sb.append(signType).append(type);
		sb.append(version).append(orequestId);
		sb.append(oorderDate);
		HiiposmUtil util = new HiiposmUtil();
		return util.MD5Sign(sb.toString(), signKey);
	}

	/**
	 * 将url参数转换成map
	 * 
	 * @param param
	 *            aa=11&bb=22&cc=33
	 * @return
	 */
	public static Map<String, Object> getUrlParams(String param) {
		Map<String, Object> map = new HashMap<String, Object>(0);
		if (StringUtils.isBlank(param)) {
			return map;
		}
		String[] params = param.split("&");
		for (int i = 0; i < params.length; i++) {
			String[] p = params[i].split("=");
			if (p.length == 2) {
				map.put(p[0], p[1]);
			}
		}
		return map;
	}
	
}
