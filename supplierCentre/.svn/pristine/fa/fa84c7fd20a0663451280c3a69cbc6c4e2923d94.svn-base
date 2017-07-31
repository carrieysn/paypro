package com.meitianhui.supplierCentre.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.meitianhui.supplierCentre.entity.SessionSupplier;
import com.meitianhui.supplierCentre.util.ActionHelper;
import com.meitianhui.supplierCentre.util.SupplierActionHelper;

/***
 * 用户登录拦截器
 * 
 * @author 丁硕
 * @date 2016年9月21日
 */
public class LoginInterceptor implements HandlerInterceptor{

	private Log logger = LogFactory.getLog(LoginInterceptor.class);
	
	/**
	 * 进入Controller层之前
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //对用户进行验证，判断请求参数中是否有token信息，如果有信息，则从缓存中取出相关信息去比较数据的有效性
		SessionSupplier user = SupplierActionHelper.getSessionUser(request);
		if(user == null){  //没有进行登录
			String current_url = ActionHelper.getFullRequestUrl(request);
			response.sendRedirect(request.getContextPath() + "/login");
			logger.info("用户未登录，拦截请求地址：" + current_url);
			return false;
 		} else{ //放行
			return true;
		}
	}
	
	/***
	 * 页面渲染完成后
	 */
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) throws Exception {
	}

	/**
	 * controller层业务逻辑处理完成后
	 */
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView view) throws Exception {
	}

}
