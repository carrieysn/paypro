package com.meitianhui.finance.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 和包分公司商户信息表
 *
 * @author dinglong
 *
 */
public class FDMerchantNo implements Serializable{

	private static final long serialVersionUID = 1L;

	/** 商户号 **/
	private String cmpay_merchant_id;
	/** 分公司 **/
	private String cmpay_merchant_name;
	/** 商户类型 **/
	private String cmpay_merchant_type;
	/** 创建时间 **/
	private Date created_time;
	/** 商户号的操作员 **/
	private String oper;
	/** 商户密钥 **/
	private String cmpay_sign_key ;
	/** 回调地址 **/
	private String cmpay_notify_url;
	/** area_id **/
	private String area_id;
	/** 区码 **/
	private String area_code;
	/** parent area_id **/
	private String parent_id;
	/** 省 **/
	private String province_name;
	/** 市 **/
	private String city_name;
	/** 区 **/
	private String district_name;
	/** 经度 **/
	private String longitude;
	/** 维度 **/
	private String latitude;
	/** 状态 **/
	private String status;
	/** 修改时间 **/
	private Date modified_time;



	public String getCmpay_merchant_id() {
		return cmpay_merchant_id;
	}
	public void setCmpay_merchant_id(String cmpay_merchant_id) {
		this.cmpay_merchant_id = cmpay_merchant_id;
	}
	public String getCmpay_merchant_name() {
		return cmpay_merchant_name;
	}
	public void setCmpay_merchant_name(String cmpay_merchant_name) {
		this.cmpay_merchant_name = cmpay_merchant_name;
	}
	public String getCmpay_merchant_type() {
		return cmpay_merchant_type;
	}
	public void setCmpay_merchant_type(String cmpay_merchant_type) {
		this.cmpay_merchant_type = cmpay_merchant_type;
	}
	public Date getCreated_time() {
		return created_time;
	}
	public void setCreated_time(Date created_time) {
		this.created_time = created_time;
	}
	public String getOper() {
		return oper;
	}
	public void setOper(String oper) {
		this.oper = oper;
	}
	public String getCmpay_sign_key() {
		return cmpay_sign_key;
	}
	public void setCmpay_sign_key(String cmpay_sign_key) {
		this.cmpay_sign_key = cmpay_sign_key;
	}
	public String getCmpay_notify_url() {
		return cmpay_notify_url;
	}
	public void setCmpay_notify_url(String cmpay_notify_url) {
		this.cmpay_notify_url = cmpay_notify_url;
	}
	public String getArea_id() {
		return area_id;
	}
	public void setArea_id(String area_id) {
		this.area_id = area_id;
	}
	public String getArea_code() {
		return area_code;
	}
	public void setArea_code(String area_code) {
		this.area_code = area_code;
	}
	public String getParent_id() {
		return parent_id;
	}
	public void setParent_id(String parent_id) {
		this.parent_id = parent_id;
	}
	public String getProvince_name() {
		return province_name;
	}
	public void setProvince_name(String province_name) {
		this.province_name = province_name;
	}
	public String getCity_name() {
		return city_name;
	}
	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}
	public String getDistrict_name() {
		return district_name;
	}
	public void setDistrict_name(String district_name) {
		this.district_name = district_name;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getModified_time() {
		return modified_time;
	}
	public void setModified_time(Date modified_time) {
		this.modified_time = modified_time;
	}


}
