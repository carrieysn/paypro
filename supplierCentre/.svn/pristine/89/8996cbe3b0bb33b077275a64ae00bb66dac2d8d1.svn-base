package com.meitianhui.supplierCentre.entity;

/***
 * 商品类型枚举类
 * 
 * @author 丁硕
 * @date 2016年4月25日
 */
public enum GoodsCategoryEnum {

	PRESELL("预售", "PS", "预售(PS)", "ys.png", "slide-yushou"),
	WHOLESALE("批发", "BS", "批发(BS)-我要批", "wh.png", "slide-weihuo"),
	EXCHANGE("兑换", "XS", "兑换(XS)-名品汇", "dh.png", "slide-duihuan"),
	SAMPLE("试样", "FS", "返利(FS)-领了么", "sy.png", "slide-shiyang"),
	LUCKDRAW("奖品", "GS", "奖赠品(GS)", "jp.png", "slide-choujiang"),
	GROUPBUYING("团购", "TS", "团购(TS)-伙拼团", "tg.png", "slide-tuangou");
	
	private String key; // 类型键
	private String code; // 类型代码
	private String name; // 类型名称
	private String icon_path; // 类型图片地址
	private String app_css; // 类型app样式

	GoodsCategoryEnum(String key, String code, String name, String icon_path, String app_css) {
		this.key = key;
		this.code = code;
		this.name = name;
		this.icon_path = icon_path;
		this.app_css = app_css;
	}
	
	/***
	 * 根据商品类型获取对应商品枚举
	 * @param key
	 * @return
	 * @author 丁硕
	 * @date   2016年11月21日
	 */
	public static GoodsCategoryEnum getCategory(String key){
		for(GoodsCategoryEnum categoryEnum : GoodsCategoryEnum.values()){
			if(categoryEnum.getKey().equals(key)){
				return categoryEnum;
			}
		}
		return null;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon_path() {
		return icon_path;
	}

	public void setIcon_path(String icon_path) {
		this.icon_path = icon_path;
	}

	public String getApp_css() {
		return app_css;
	}

	public void setApp_css(String app_css) {
		this.app_css = app_css;
	}

}
