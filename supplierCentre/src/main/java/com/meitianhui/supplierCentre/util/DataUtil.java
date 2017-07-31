package com.meitianhui.supplierCentre.util;

import java.util.Date;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.meitianhui.supplierCentre.entity.GoodsCategoryEnum;

public class DataUtil {
	
	
	/**
	 * 生成对应的商品编码
	 * @param key
	 * @return
	 */
	public static String getGoodsCode(String key){ 
		String goods_code = "";
		//GoodsCategoryEnum.SAMPLE.getKey()
		GoodsCategoryEnum  categoryEnum = GoodsCategoryEnum.getCategory(key);
		goods_code = categoryEnum.getCode() + StringUtils.substring((new Date()).getTime() + "", 0, 12);
		return goods_code;
	}
	/**
	 * 生成对应的商品活动码
	 * @param key
	 * @return
	 */
	public static String getGoodsActiveCode(){ 
		    String activeCode = "";
		    int max=9;int min=2;
	        Random random = new Random();
	        int s = random.nextInt(max)%(max-min+1) + min;
	        activeCode = s+""+(int)((Math.random()*9+1)*1000);
	        return activeCode;
	}
	
	public static final String FROMCENTRE = "centre";

}
