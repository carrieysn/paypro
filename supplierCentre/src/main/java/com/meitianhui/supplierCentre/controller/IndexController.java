package com.meitianhui.supplierCentre.controller;


import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class IndexController {
	//private Log logger = LogFactory.getLog(IndexController.class);
	/**
	 * 跳转-首页
	 * @param request
	 * @return
	 */
	@RequestMapping("index")
	public String index(HttpServletRequest request){
		return "index";
	}
	/**
	 * 跳转-招商页面
	 * @param request
	 * @return
	 */
	@RequestMapping("zsindex")
	public String zsindex(HttpServletRequest request){
		return "main/attract";
	}
	/**
	 * 跳转-资讯页面
	 * @param request
	 * @return
	 */
	@RequestMapping("zxindex")
	public String zxindex(HttpServletRequest request){
		return "main/news";
	}
	/**
	 * 跳转-资讯详情页面
	 * @param request
	 * @return
	 */
	@RequestMapping("newshow")
	public String zxdetail(HttpServletRequest request){
		String num = request.getParameter("zid");
		String page = "";
		if("r".equals(num)){
			page = "main/z_detail01";
		}if("s".equals(num)){
			page = "main/z_detail02";
		}if("t".equals(num)){
			page = "main/z_detail03";
		}if("f".equals(num)){
			page = "main/newsdetail";
		}
		return page;
	}
}
