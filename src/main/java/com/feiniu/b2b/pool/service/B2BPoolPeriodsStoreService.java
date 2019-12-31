package com.feiniu.b2b.pool.service;

import java.util.List;

import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;

public interface B2BPoolPeriodsStoreService {

	public List<YxPoolPeriodsStore> getPoolPeriodsStoresByPeriodsId(Long id,String storeCode);
	
	public List<YxPoolPeriodsStore> queryStoreList(YxPoolPeriodsStore store);
	
	public List<YxPoolPeriodsStore> queryStoreListByPeriodsId(Long  periodsId);
	
	public YxPoolPeriodsStore queryB2BPoolPeriodsStoreByCode(Long  periodsId,String storeCode);
	
	public void updateCommoditys(YxPoolPeriodsStore periodStore) ;
	
	/**
	 * 通过期数id和门店codes查询门店期数表
	 */
	List<YxPoolPeriodsStore> listPoolPeriodsStoresByPeriodsIdAndStoreCodes(Long id,String storeCodes);

    public List<B2BStore> selectB2BStoreByNameOrCode(B2BStore store);
}
