package com.meitianhui.supplierCentre.service;

import java.util.List;
import java.util.Map;

import com.meitianhui.partner.entity.SessionUser;
import com.meitianhui.platform.entity.Page;

/***
 * 供应商逻辑处理接口
 * 
 * @author 丁硕
 * @date 2016年11月2日
 */
public interface SupplierService {

	/***
	 * 查询单个供应商信息
	 * @param supper_id
	 * @return
	 * @author 丁硕
	 * @date   2016年11月2日
	 */
	public Map<String, Object> queryOneSupplier(String supplier_id);
	/**
	 * 通过手机号查询供应商
	 * @param contact_tel
	 * @return
	 */
	public Map<String,Object>  selectSupplierByTel(String contact_tel);
	
	/***
	 * 查询供应商合同信息
	 * @param supplier_id
	 * @return
	 * @author 丁硕
	 * @date   2016年11月4日
	 */
	public List<Map<String, Object>> querySupplierContractList(String supplier_id);
	
	/***
	 * 查询供应商商业类型
	 * @param supplier_id
	 * @return
	 * @author 丁硕
	 * @date   2017年3月31日
	 */
	public Map<String, Object> querySupplierBusinessInfo(String supplier_id);
	
	/***
	 * 查询供应商日志信息
	 * @param supplier_id
	 * @return
	 * @author 丁硕
	 * @date   2016年11月2日
	 */
	public Page querySupplierLogListPage(int pageNum, int pageSize, String supplier_id);
	
	/***
	 * 保存供应商操作日志
	 * @param user
	 * @param event
	 * @author 丁硕
	 * @date   2016年11月4日
	 */
	public void saveSupplierLog(SessionUser user, String event);
}
