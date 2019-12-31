package com.feiniu.yx.pool.service;

import java.util.List;

import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;

public interface YxPoolPeriodsStoreService {

	List<YxPoolPeriodsStore> getPoolPeriodsStoresByPeriodsId(Long id,String storeCode);
	
	List<YxPoolPeriodsStore> queryStoreList(YxPoolPeriodsStore store);
	
	List<YxPoolPeriodsStore> queryStoreListByPeriodsId(Long  periodsId);
	
	YxPoolPeriodsStore queryYxPoolPeriodsStoreByCode(Long  periodsId,String storeCode);
	
	void updateCommoditys(YxPoolPeriodsStore periodStore) ;

	/**
	 * 更新池期数商品
	 * @param periodStore 池期数
	 */
	void updateYxPoolPeriodsStoreCommoditys(YxPoolPeriodsStore periodStore);
	
	/**
	 * 通过期数id和门店codes查询门店期数表
	 */
	List<YxPoolPeriodsStore> listPoolPeriodsStoresByPeriodsIdAndStoreCodes(Long id,String storeCodes);
	
	void  deletePeriodStore(Long periodId, String storeCode);
	
	List<YxPoolPeriodsStore> listPeriodsStoreByStoreCodes2PeriodsId(Long periodsId,String codes);
}
