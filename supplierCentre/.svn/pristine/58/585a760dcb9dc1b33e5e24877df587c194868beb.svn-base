<%@page import="com.meitianhui.platform.constant.ConfigConstant"%>
<%@page import="com.meitianhui.supplierCentre.util.SupplierActionHelper"%>
<%@page import="com.meitianhui.platform.utils.ConfigurationHelper"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<%
	session.setAttribute("ctx", request.getContextPath());
	if(StringUtils.isNotBlank(request.getParameter("token"))){
		session.setAttribute("token", request.getParameter("token"));
	}
	if(request.getAttribute("token") != null){
		session.setAttribute("token", (String)request.getAttribute("token"));
	}
	request.setAttribute("portal_server", ConfigurationHelper.getInstance().getValue(ConfigConstant.PORTAL_SERVER));
	session.setAttribute("sessionUser", SupplierActionHelper.getSessionUser(request));
	session.setAttribute("static_resource_server", "//mps-static.meitianhui.com/product");	//静态资源访问地址
%>
<head>
	<meta http-equiv="content-type" content="text/html;charset=UTF-8" />
	<meta name="renderer" content="webkit|ie-comp|ie-stand" /> 
	<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
	<meta name="renderer" content="webkit"/> 
	<title>每天惠商家中心</title>
	<!-- 项目基础css文件 -->
	<link rel="stylesheet" type="text/css" href="${ctx}/common/static/css/base.css?v=0.01" />
	<link rel="stylesheet" type="text/css" href="${ctx}/common/static/css/index.css?v=0.02" />
    <link type="text/css" href="${ctx}/common/static/css/amazeui.swiper.min.css" rel="stylesheet" />
	<link rel="stylesheet" type="text/css" href="${static_resource_server}/common/third-part/beatPicker/beatPicker.min.css" />
	<!-- jQuery -->
	<script type="text/javascript" src="${ctx}/common/static/js/jquery-1.11.3.min.js"></script>
    <script type="text/javascript" src="${ctx}/common/static/js/amazeui.swiper.min.js"></script>
    <!-- 弹窗控件 -->
	<script type="text/javascript" src="${ctx}/common/third-part/layer/layer.js"></script>
	<!-- 时间控件 -->
	<script type="text/javascript" src="${static_resource_server}/common/third-part/beatPicker/beatPicker.min.js"></script>
	<!-- 分页控件 -->
	<script type="text/javascript" src="${static_resource_server}/common/third-part/jqPaginator.min.js"></script>
	<!-- art-template模板引擎 -->
	<script type="text/javascript" src="${static_resource_server}/common/third-part/art-template.js"></script>
	<!-- 二级菜单优化 -->
	<script type="text/javascript" src="${static_resource_server}/common/third-part/jquery.menu-aim.js"></script>
	<!-- 表单验证控件 -->
	<script type="text/javascript" src="${static_resource_server}/common/third-part/Validform_v5.3.2.js"></script>
	<!-- plupload -->
	<script type="text/javascript" src="${static_resource_server}/common/third-part/plupload/plupload.full.min.js"></script>
	<!-- 全局公用JS -->
	<script type="text/javascript" src="${ctx}/common/static/js/application.js?v=0.014"></script>
	<!-- 自定义分页 -->
	<script type="text/javascript" src="${ctx}/common/static/js/paginator.js?v=0.01"></script>
	<script type="text/javascript">
		var ctx = "${ctx}", token = "${token}", portal_server = "${portal_server}";
		template.helper('fmoney', function (money, n){
			return M.fmoney(money, n);
		});
	</script>
	<%@ include file="/common/layout/include.jsp" %>
</head>


