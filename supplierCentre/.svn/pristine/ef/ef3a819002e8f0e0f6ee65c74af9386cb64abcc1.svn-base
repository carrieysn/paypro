package com.meitianhui.supplierCentre.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.meitianhui.platform.utils.IDUtil;
import com.meitianhui.supplierCentre.dao.AuditFlowMapper;
import com.meitianhui.supplierCentre.entity.AuditFlow;
import com.meitianhui.supplierCentre.service.AuditFlowService;
@Service("auditFlowServiceimpl")
public class AuditFlowServiceimpl implements AuditFlowService {
	@Autowired
	private AuditFlowMapper auditFlowMapper;
	
	public void addAuditFlow(AuditFlow auditFlow) {
		auditFlow.setFlow_id(IDUtil.getUUID());
		auditFlow.setAction_date(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
		auditFlow.setCreated_date(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
	    auditFlowMapper.addAuditFlow(auditFlow);
	}
	
	public AuditFlow getAuditFlow(String supplier_id,String status) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("supplier_id", supplier_id);
		params.put("result", status);
		return auditFlowMapper.getAuditFlow(params);
	}
	
}
