package com.feiniu.b2b.pool.service;

import java.util.List;
import java.util.Map;

import com.feiniu.b2b.pool.entity.B2BPoolCommodity;

public interface B2BRemoteCommodityService {
	
	public List<B2BPoolCommodity> getRemoteCommodityOfStoreInfo(String[] ids);
	public List<B2BPoolCommodity> getRemoteCommodityOfStoreInfo(String[] ids,String storeCodes);
	
		
	public Map<String,B2BPoolCommodity> getRemoteCommodityListByStoreAndIds(String storeCode,String[] ids);

    /**
     * 
     * 获取单个商品的信息
     *
     * @param storeCode
     * @param id
     * @return
     */
	public B2BPoolCommodity getRemoteCommodityPriceInfoByStoreAndId(String storeCode,String id);

}
