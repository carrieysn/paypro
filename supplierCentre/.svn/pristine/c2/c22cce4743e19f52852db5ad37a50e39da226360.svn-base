package com.meitianhui.supplierCentre.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.meitianhui.supplierCentre.entity.PreSupplier;

public interface PreSupplierMapper {
	
	/***
	 * 新增推荐商品信息
	 * @param preSupplier
	 */
	public void savePreSupplier(PreSupplier preSupplier);
	
	
	/***
	 * 更新推荐商品信息
	 * @param preSupplier
	 */
	public void updatePreSupplier(PreSupplier preSupplier);
	/**
	 * 查询推荐的商品列表
	 * @param audit_status
	 * @return
	 */
	public List<PreSupplier> selectPreSupplierList(Map<String, Object> params);

    /**
     * 验证商户是否推荐过商品
     * @param mobile
     * @return
     */
	public List<PreSupplier> selectPreListByMobile(String mobile);
	
	/**
	 * 查询当前用户下所有商品列表
	 * @param tel
	 * @return
	 */
	public List<PreSupplier> selectAllPreList(String tel);
	
	/**
	 * 查询对应id的商品信息
	 * @param supplier_id
	 * @return
	 */
	public PreSupplier selectPreSupplierBySupplierId(String supplier_id);
	
	/**
	 * 查询商家推荐商品列表
	 * @param contact_tel
	 * @return
	 */
	public List<PreSupplier> selectPagePreList(String contact_tel);
	/**
	 * 查询单个商品详细信息
	 * @param supplier_id
	 * @return
	 */
	public PreSupplier selectOnePreSupplier(String supplier_id);
	/**
	 * 查询相同商品链接的商品信息
	 * @param pic_detail_info
	 * @return
	 */
	public List<PreSupplier> selectSamePreSupplier(Map<String,Object> params);
	
	/**
	 * 查询供应商商业类型
	 * @param supplier_id
	 * @return
	 */
	public Map<String, Object> querySupplierBussinesInfo(@Param("supplier_id") String supplier_id);

}
