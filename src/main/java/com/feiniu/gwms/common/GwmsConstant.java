package com.feiniu.gwms.common;

import com.feiniu.yx.config.SystemEnv;

/**
 * @author tongwenhuan
 * 2017年3月10日 上午11:53:19
 */
public class GwmsConstant {

	//静态域名
	public static final String STATICHOST_GW = SystemEnv.getProperty("gw.static.host");
	//新增活动默认模板
	public static final String TEMPID = SystemEnv.getProperty("gwmsTemplateId");
	//发布活动地址
	public static final String PUBLISH = SystemEnv.getProperty("publishPage");
	//外网地址
	public static final String GWMSURL = SystemEnv.getProperty("yxcmspath");
	//关注门店id
	public static final String GW_GZMD_ID = SystemEnv.getProperty("gw.gzmd.id");
	
	
}
