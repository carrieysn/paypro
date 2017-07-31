package com.meitianhui.supplierCentre.service;

import java.util.List;
import java.util.Map;

import com.meitianhui.platform.entity.Page;
import com.meitianhui.platform.exception.BusinessException;
import com.meitianhui.supplierCentre.entity.PreSupplier;

public interface PreSupplierService {
	
	/**
	 * 根据条件查询推荐商品明细列表
	 * @param audit_status
	 * @return
	 */
	public List<PreSupplier> queryPreSupplierList(Map<String,Object> map);
	/**
	 * 验证商户是否推荐过商品
	 * @param mobile
	 * @return
	 */
	public List<PreSupplier> selectPreListByMobile(String mobile);
	
	/**
	 * 新增或添加商品
	 * @param preSupplier
	 * @return
	 * @throws BusinessException
	 */
	public PreSupplier saveOrUpdateGoods(PreSupplier preSupplier) throws BusinessException;
	/**
	 * 驳回状态修改
	 * @param preSupplier
	 * @return
	 * @throws BusinessException
	 */
	public PreSupplier editGoods(PreSupplier preSupplier) throws BusinessException;
	/**
	 * 查询单个商品基础信息
	 * @param supplier_id
	 * @return
	 */
	public PreSupplier selectBasalGoods(String supplier_id);
	/**
	 * 商品寄样
	 * @param preSupplier
	 * @throws BusinessException
	 */
	public void operateGoods(PreSupplier preSupplier) throws BusinessException;
	/**
	 * 查询所有商品列表
	 * @param mobile
	 * @return
	 */
	public List<PreSupplier> selectAllPreList(String mobile);
	
	/**
	 * 分页查询商品列表
	 * @param contact_tel
	 * @return
	 */
	public Page selectPagePreList(int pageNum, int pageSize, String contact_tel);
	
	/**
	 * 查询单个推荐商品详细信息
	 * @param supplier_id
	 * @return
	 */
	public PreSupplier queryDetailPreSupplier(String supplier_id);
	/**
	 * 查询相同链接已报名产品
	 * @param pic_info_detail
	 * @return
	 */
	public List<PreSupplier> selectSamePreSupplierList(Map<String,Object> params);
	
	/**
	 * 查询供应商商业类型
	 * @param supplier_id
	 * @return
	 */
	public Map<String, Object> querySupplierBusinessInfo(String supplier_id);
}
