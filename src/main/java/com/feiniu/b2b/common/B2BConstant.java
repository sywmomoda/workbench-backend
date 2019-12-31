package com.feiniu.b2b.common;

import com.feiniu.yx.config.SystemEnv;

/**
 * @author tongwenhuan
 * 2017年3月10日 上午11:53:19
 */
public class B2BConstant {

	//静态文件路径
	public static final String STATICPATH = SystemEnv.getProperty("staticPath");
	//静态域名
	public static final String STATICHOST = SystemEnv.getProperty("staticHost_elf");
	//新增活动默认模板
	public static final String TEMPID = SystemEnv.getProperty("b2bTemplateId");
	//发布活动地址
	public static final String PUBLISH = SystemEnv.getProperty("b2bPublishPage");
	//外网地址
	public static final String B2BURL = SystemEnv.getProperty("b2bcmspath");
	//默认商品主图
	public final static String B2B_BASEIMG = SystemEnv.getProperty("cms.b2b.baseImg");
	//商品明细地址
	public static final String B2B_COMMODITY_URI = SystemEnv.getProperty("b2bCommodityUrl");
}
