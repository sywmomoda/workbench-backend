package com.feiniu.yx.page.service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.Module;

/**
 * @author tongwenhuan
 * 2017年2月24日 上午11:09:08
 */
public interface ModuleDataService {
	
	JSONObject findModuleJSON(Module module, String storeCode, String previewTime);

	String findModuledData(Long id, String storeCode, String previewTime);
	
	String findModuledData(Module module , String storeCode, String previewTime);
	
	void customDataProcess(JSONObject jo);
	
}
