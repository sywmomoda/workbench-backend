package com.feiniu.yx.page.service;

import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.store.entity.YXStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author zhangdunyao
 *
 */
@Service
public interface ModuleProperPlusService {

	Map<String, ModuleProperPlus> queryModuleProperMapByModuleId(Long moduleId);

	ModuleProperPlus findModuleProperById(Long id);

	String addOrUpdateModuleProper(Long moduleId, List<YXStore> stores,
								   String properData,String saveType);

	void deleteModuleProper(Long moduleId, List<YXStore> stores);
	void deleteModuleProperByKey(Long moduleId, List<YXStore> stores,String key);

	String getTreeSelectStoreGroupByType(String checkedCodes, String groupIds, String xiaoQuIds,
										 Long moduleId);

	void syncModuleProperPlus(Long moduleId);

	ModuleProperPlus findModuleProperByIdAndStoreCode(ModuleProperPlus m);

	void deleteModuleProperById(Long id);

	void insertModuleProper(ModuleProperPlus one);

	void updateModuleProper(ModuleProperPlus one);

	Map<String,ModuleProperPlus> queryModuleProperMapByModuleIdAndTabId(Long moduleId, Integer couponCenterTabId);

	String getTreeSelectStoreGroupByTypeAndTabId(String checkedCodes, String groupIds, String xiaoQuIds, Long moduleId, Integer couponCenterTabId);

	void deleteModuleProperByTabId(Long moduleId, List<YXStore> stores, Integer couponCenterTabId);

    void delAndStorModuleProperByTabId(Long moduleId, Integer couponCenterTabId);

	void delCouponCenterByTabId(Long moduleId, Integer[] couponCenterTabIds);

	void delCouponCenterByModuleId(Long moduleId);
}
