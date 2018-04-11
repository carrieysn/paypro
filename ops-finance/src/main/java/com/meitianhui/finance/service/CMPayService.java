package com.meitianhui.finance.service;

import java.util.Map;

import com.meitianhui.common.exception.BusinessException;
import com.meitianhui.common.exception.SystemException;

public interface CMPayService {
	
	/**
	 * 和包扫码支付 2017/12/25 丁龙
	 * @param paramsMap
	 * @throws BusinessException
	 * @throws SystemException
	 */
	public void barCodePay(Map<String, Object> paramsMap,String scan_flag) throws BusinessException, SystemException, Exception;
	
}
