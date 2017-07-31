package com.meitianhui.supplierCentre.util;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.meitianhui.platform.constant.OperateServerContant;
import com.meitianhui.platform.exception.PlatformApiException;
import com.meitianhui.platform.utils.PlatformApiUtil;

/***
 * 运营内部系统接口请求工具类
 * 
 * @author 丁硕
 * @date 2016年8月15日
 */
public class OperateApiUtil {

	/***
	 * 加载订单统计信息
	 * @return
	 * @throws PlatformApiException
	 * @author 丁硕
	 * @date   2016年8月15日
	 */
	public static JSONObject loadOrderDashBoard() throws PlatformApiException{
		Map<String, Object> params = new HashMap<String, Object>();
		String data = PlatformApiUtil.requestServiceApi(OperateServerContant.DATAWAREHOUSE_API_SERVER + "/api/order", "order.queryOrderDashboard", params);
		JSONObject json = JSONObject.parseObject(data);
		return json;
	}
	
	/**
	 * 加载供应商订单统计
	 * @param supplier_id
	 * @return
	 * @throws PlatformApiException
	 * @author 丁硕
	 * @date   2017年4月11日
	 */
	public static JSONObject loadsSupplierOrderStatistic(String supplier_id) throws PlatformApiException{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("supplier_id", supplier_id);
		String data = PlatformApiUtil.requestServiceApi(OperateServerContant.DATAWAREHOUSE_API_SERVER + "/api/procedure", "procedure.queryStatisticsSupplierOrderSummary", params);
		JSONArray array = JSONArray.parseArray(data);
		if(array.size() > 0){
			return array.getJSONObject(0);
		}
		return new JSONObject();
	}
	
	/***
	 * 按月统计供应商订单
	 * @param supplier_id
	 * @param order_type
	 * @return
	 * @throws PlatformApiException
	 * @author 丁硕
	 * @date   2017年4月11日
	 */
	public static JSONArray loadSupplierOrderTypeMonthly(String supplier_id, String order_type) throws PlatformApiException{
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("supplier_id", supplier_id);
		params.put("order_type", order_type);
		String data = PlatformApiUtil.requestServiceApi(OperateServerContant.DATAWAREHOUSE_API_SERVER + "/api/procedure", "procedure.queryStatisticsSupplierOrderTypeMonthly", params);
		JSONArray array = JSONArray.parseArray(data);
		return array;
	}
	
}
