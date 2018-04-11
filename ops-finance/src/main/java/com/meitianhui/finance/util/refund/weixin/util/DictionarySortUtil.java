package com.meitianhui.finance.util.refund.weixin.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meitianhui.common.util.StringUtil;
import com.meitianhui.finance.util.refund.weixin.Md5Util;



public class DictionarySortUtil {

	private static final Logger logger = Logger.getLogger(DictionarySortUtil.class);
	private static SpellComparator spellComparator = null;

	public static String createSign(Map<String, String> map,String pay_key) {
		if (map == null || map.size() == 0)
			return null;
		StringBuffer sb = new StringBuffer();
		List<String> keys = executeDictionnarySort(map);
		for (String key : keys) {
			if (!StringUtil.isEmpty(map.get(key))) {
				sb.append(key + "=" + map.get(key) + "&");
			}
		}
		sb.append("key="+pay_key);
		logger.info("签名值:"+sb.toString());
		String sign = Md5Util.MD5Encode(sb.toString(), "UTF-8").toUpperCase();
		return sign;
	}
	
	
	
	public static String createSign(Map<String, String> map) {
		if (map == null || map.size() == 0)
			return null;
		StringBuffer sb = new StringBuffer();
		List<String> keys = executeDictionnarySort(map);
		for (String key : keys) {
			if (!StringUtil.isEmpty(map.get(key))) {
				sb.append(key + "=" + map.get(key) + "&");
			}
		}
		logger.info("截取前1:"+sb.toString());
		String str=sb.toString().substring(0,sb.toString().lastIndexOf("&"));
		logger.info("截取后1:"+str);
		
		String sign = Md5Util.MD5Encode(str, "UTF-8").toUpperCase();
		return sign;
	}

	static {
		spellComparator = new DictionarySortUtil().new SpellComparator();
	}

	public static List<String> executeDictionnarySort(Map<String, String> map) {
		if (map == null || map.isEmpty())
			return null;
		List<String> keys = new ArrayList<String>();
		keys.addAll(map.keySet());
		Collections.sort(keys, spellComparator);
		return keys;
	}

	public static String executeDictionnarySort(List<String> list) {
		if (list == null || list.isEmpty())
			return null;
		StringBuilder sb = new StringBuilder();
		Collections.sort(list, spellComparator);
		for (String s : list) {
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * 字典排序比较器
	 */

	class SpellComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			try {
				// 取得比较对象的汉字编码，并将其转换成字符串
				String s1 = new String(o1.toString().getBytes("GB2312"), "ISO-8859-1");
				String s2 = new String(o2.toString().getBytes("GB2312"), "ISO-8859-1");
				// 运用String类的 compareTo（）方法对两对象进行比较
				return s1.compareTo(s2);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}
	}

}
