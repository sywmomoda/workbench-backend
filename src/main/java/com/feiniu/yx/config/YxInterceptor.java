package com.feiniu.yx.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class YxInterceptor extends HandlerInterceptorAdapter{
	
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		initBasePath(request);
		return true;
	}
	
	public void initBasePath(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String path = request.getContextPath();
    	String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path;
		session.setAttribute("basePath",basePath);
	}

}
