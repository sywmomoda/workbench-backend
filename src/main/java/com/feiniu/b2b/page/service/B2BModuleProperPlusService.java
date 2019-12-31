package com.feiniu.b2b.page.service;


import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.yx.page.entity.ModuleProperPlus;

/**
 * @author zhangdunyao
 *
 */
@Service
public interface B2BModuleProperPlusService {

	String getTreeSelectStoreGroupByType(String checkedCodes, String groupIds,
			Long moduleId);
	public Map<String, ModuleProperPlus> queryModuleProperMapByModuleId(
			Long moduleId);
	public String addOrUpdateModuleProper(Long moduleId, List<B2BStore> stores, String properData,String saveType);
	
	public void syncModuleProperPlus(Long moduleId); 
	
	public void deleteModuleProper(Long moduleId, List<B2BStore> stores);
}
