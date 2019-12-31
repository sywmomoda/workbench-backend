package com.feiniu.yx.page.service;

import com.alibaba.fastjson.JSONObject;

public interface CustomModule {
	/**
	 * 特殊模块处理
	 * @author lizhiyong
	 * 2017年4月28日
	 * @param mjo
	 * TODO
	 * @throws Exception 
	 */
	public void findCustomData(JSONObject mjo) throws Exception;
}
