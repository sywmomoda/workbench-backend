package com.feiniu.yx.util;

import java.io.File;

import com.feiniu.yx.config.SystemEnv;

public class TplUtil {
	
	//模板根目录
	public static String getTemplatePath(String temp_url) {
		return (SystemEnv.getProperty("webRootPath")+ temp_url).replace("/", File.separator);
	}
	//头部模板
	public static String getTemplateHeadPath(String temp_url) {
		return getTemplatePath(temp_url) + File.separator + "template" + File.separator + "template_head.html";
	}
	//底部模板
	public static String getTemplateFootPath(String temp_url) {
		return getTemplatePath(temp_url) + File.separator + "template" + File.separator + "template_foot.html";
	}
	//模块模板
	public static String getModulePath(String temp_url, String moduleCode) {
		return getTemplatePath(temp_url) + File.separator + "modules" + File.separator + moduleCode + File.separator + "show/module.html";
	}

	// 模块模板
	public static String getModuleHeadPath(String temp_url, String fileName) {
		return getTemplatePath(temp_url) + File.separator + "template"+ File.separator + fileName+".html";
	}
}
