package com.feiniu.b2b.page.service;

import com.feiniu.yx.page.entity.Module;

/**
 * @author tongwenhuan
 * 2017年2月24日 上午11:09:08
 */

public interface B2BModuleDataService {

	String findModuledData(Long id, String storeCode);
	
	String findModuledData(Module module , String storeCode);
	
}
