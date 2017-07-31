package com.meitianhui.supplierCentre.util;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.meitianhui.platform.constant.AppportalServerContant;
import com.meitianhui.platform.exception.PlatformApiException;
import com.meitianhui.platform.utils.PlatformApiUtil;

public class BaseApiUtil {

	/***
	 * 供应商登录
	 * @param params
	 * @return
	 * @throws PlatformApiException
	 * @author 丁硕
	 * @date   2016年9月21日
	 */
	public static JSONObject supplierLogin(Map<String, Object> params) throws PlatformApiException{
		params.put("data_source", "SJLY_03");
		String data = PlatformApiUtil.requestServiceApi(AppportalServerContant.BASE_USER_SERVER_URL, "infrastructure.supplierLogin", params);
		JSONObject json = JSONObject.parseObject(data);
		return json;
	}
	
	/**
	 * 验证供应商账号是否存在
	 * @param mobile
	 * @throws PlatformApiException
	 * @author 丁硕
	 * @date   2016年9月22日
	 */
	public static boolean validAccountExists(String mobile) throws PlatformApiException{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("mobile", mobile);
		params.put("member_type_key", "supplier");
		String data = PlatformApiUtil.requestServiceApi(AppportalServerContant.BASE_USER_SERVER_URL, "infrastructure.memberTypeValidateByMobile", params);
		JSONObject json = JSONObject.parseObject(data);
		return "exist".equals(json.getString("member_type"));
	}
	
}
