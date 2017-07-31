package com.meitianhui.supplierCentre.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 内部买手合作公司枚举
 * 
 * @author 丁硕
 * @date 2016年7月25日
 */
public enum AppEnum {

	BASE("base", "买手", "工艺,美食,日用,家居,饮料,酒水,文体,母婴,玩具,数码,箱包,服饰,建材,五金,洗护,美妆,香烟,电器,其它"),	//所有基础的
	SDJ("sdj", "颂到家", "健康,滋补,情趣"),
	SLL("sll", "水来了", "桶装水,瓶装水,水设备"),
	HRY("hry", "惠如意", "粮油,生鲜,干货,种苗,花卉,中药材,农机,农药,化肥");
	
	private static Map<String, List<GoodsDisplayAreaEnum>> displayAreaMap = new HashMap<String, List<GoodsDisplayAreaEnum>>();

	AppEnum(String app_key, String app_name, String app_display_area) {
		this.app_key = app_key;
		this.app_name = app_name;
		this.app_display_area = app_display_area;
	}
	
	/***
	 * 根据app_key获取对应的app信息
	 * @param app_key
	 * @return
	 * @author 丁硕
	 * @date   2016年7月25日
	 */
	public static AppEnum getApp(String app_key){
		if(HRY.getApp_key().equals(app_key)){
			return HRY;
		}
		return null;
	}

	private String app_key; // app值，参数值
	private String app_name; // app名称
	private String app_display_area; // app对应的商品类目

	public String getApp_key() {
		return app_key;
	}

	public void setApp_key(String app_key) {
		this.app_key = app_key;
	}

	public String getApp_name() {
		return app_name;
	}

	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}

	public String getApp_display_area() {
		return app_display_area;
	}

	public void setApp_display_area(String app_display_area) {
		this.app_display_area = app_display_area;
	}
	
	/***
	 * 获取对应APP的商品类目列表
	 * @return
	 * @author 丁硕
	 * @date   2016年7月30日
	 */
	public List<GoodsDisplayAreaEnum> getApp_display_area_list(){
		if(!displayAreaMap.containsKey(this.getApp_key())){
			synchronized (AppEnum.class){
				List<GoodsDisplayAreaEnum> enumList = new ArrayList<GoodsDisplayAreaEnum>();
				 String[] displayAreas = this.getApp_display_area().split(",");
				 for(String displayArea : displayAreas){
					 for(GoodsDisplayAreaEnum areaEnum : GoodsDisplayAreaEnum.values()){
						 if(displayArea.equals(areaEnum.getDisplay_area())){
							 enumList.add(areaEnum);
						 }
					 }
				 }
				 displayAreaMap.put(this.getApp_key(), enumList);
			}
		}
		return displayAreaMap.get(this.getApp_key());
	}

}
