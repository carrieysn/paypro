package com.meitianhui.supplierCentre.entity;

/***
 * 商品类目枚举类
 * 
 * @author 丁硕
 * @date 2016年4月25日
 */
public enum GoodsDisplayAreaEnum {

	/**基础，买手**/
	ARTS("工艺", "gy.png", "slide-gongyi"),
	FOOD("美食", "ms.png", "slide-meishi"),
	USUAL("日用", "rz.png", "slide-riyong"),
	LIVING("家居", "jj.png", "slide-jiaju"),
	DRINK("饮料","js.png", "slide-yingliao"),
	LIQUOR("酒水", "js2.png", "slide-jiushui"),
	OFFICE("文体","bg.png", "slide-bangong"),
	BABY("母婴", "my.png", "slide-muying"),
	TOYS("玩具", "wj.png", "slide-wanju"),
	DIGIT("数码", "shuma.png", "slide-shuma"),
	BAGS("箱包", "xb.png", "slide-xiangbao"),
	DRESS("服饰", "fs.png", "slide-fushi"),
	BULIDING("建材","jc.png", "slide-jiancai"),
	HARDWARE("五金","wj-01.png", "slide-wujing"),
	CAREPPRODUCT("洗护","xihu.png", "slide-xihu"),
	COSMETIC("美妆","meizhuang.png", "slide-meizhuang"),
	CIGARETTE("香烟","xiangyan.png", "slide-xiangyan"),
	APPLIANCE("电器","dianqi.png", "slide-dianqi"),
	OTHER("其它", "qt.png", "slide-qita"),
	
	/**颂到家**/
	HEALTH("健康", "bjp.png", "slide-baojian"),
	TONIC("滋补", "zibu.png", "slide-zibu"),
	/*LIVELIHOOD("计生", "jisheng.png", "slide-jisheng"),*/
	SPICE("情趣", "jisheng.png", "slide-jisheng"),
	
	/**水来了**/
	BARRELLED("桶装水","tzs.png", "slide-tongzhuangshui"),
	BOTTLED("瓶装水","pzs.png", "slide-pingzhuangshui"),
	WATEREQUIPMENT("水设备","ssb.png", "slide-shuishebei"),
	
	/**惠如意**/
	FOODSTUFF("粮油", "ls.png", "slide-liangshi"),
	FRESH("生鲜", "sx.png", "slide-shengxian"),
	DRYCARGO("干货", "gh.png", "slide-ganhuo"),
	GERMCHIT("种苗", "zm.png", "slide-zhongmiao"),
	FLOWER("花卉", "huahui.png", "slide-huahui"),
	DRUGS("中药材", "zyc.png", "slide-zhongyaocai"),
	FARM("农机", "nj.png", "slide-nongji"),
	PESTICIDE("农药", "ny.png", "slide-nongyao"),
	FERTILIZER("化肥", "hf.png", "slide-huafei");
	
//	AGRICULTURE("农资","nz.png", "slide-nz"),
	
	private GoodsDisplayAreaEnum(String display_area, String icon_path, String app_css){
		this.display_area = display_area;
		this.icon_path = icon_path;
		this.app_css = app_css;
	}
	/**类目名称**/
	private String display_area;	
	/**web端icon path**/
	private String icon_path;	
	/**app css样式**/
	private String app_css;	
	public String getDisplay_area() {
		return display_area;
	}
	public void setDisplay_area(String display_area) {
		this.display_area = display_area;
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
