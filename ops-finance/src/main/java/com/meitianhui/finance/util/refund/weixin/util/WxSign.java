package com.meitianhui.finance.util.refund.weixin.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meitianhui.common.util.PropertiesConfigUtil;
import com.meitianhui.finance.util.refund.weixin.common.PayConfigUtil;




public class WxSign {
	private static final Logger logger = Logger.getLogger(WxSign.class);

	public static String getSign(Map<String, String> map) {
		ArrayList<String> list = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue() != "" && !"sign".equals(entry.getKey())) {
				sb.setLength(0);
				list.add(sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&").toString());
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		sb.setLength(0);
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		
		sb.append("key=").append(PropertiesConfigUtil.getProperty("wechat.consumer_app_key"));
		String result = sb.toString();
		logger.info("WxSign Before:" + result);
		result = MD5Util.MD5Encode(result, "UTF-8").toUpperCase();
		logger.info("WxSign After:" + result);
		return result;
	}
}
