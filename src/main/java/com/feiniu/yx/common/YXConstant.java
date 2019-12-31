package com.feiniu.yx.common;

import com.feiniu.yx.config.SystemEnv;

/**
 * @author tongwenhuan
 * 2017年3月10日 上午11:53:19
 */
public class YXConstant {

	//静态文件路径
	public static final String STATICPATH = SystemEnv.getProperty("staticPath");
	//静态域名
	public static final String STATICHOST = SystemEnv.getProperty("staticHost");
	//新增活动默认模板
	public static final String TEMPID = SystemEnv.getProperty("templateId");
	//发布活动地址
	public static final String PUBLISH = SystemEnv.getProperty("publishPage");
	//外网地址
	public static final String YXURI = SystemEnv.getProperty("yxcmspath");
	//优鲜首页id
	public static final String INDEX_PAGE_ID = SystemEnv.getProperty("index.page.id");
	//portal权限接口地址
	public static final String PORTAL_HOST = SystemEnv.getProperty("portal.host");
	//优鲜模板id
	public static final String INDEX_TEMPLATE_ID = SystemEnv.getProperty("yx.index.templeta.id");
	// 当前登录用户ID
	public static final String PORTAL_CURRENT_USERNAME = "__PORTAL_CURRENT_USERNAME";
	// Portal后台SSO Token Cookie名
	public static final String PORTAL_SSO_COOKIE_TOKEN_NAME = "s98r5h2s6v1m37o";
}
