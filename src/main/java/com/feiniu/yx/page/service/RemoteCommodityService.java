package com.feiniu.yx.page.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.YxRemoteCommodity;
import com.feiniu.yx.pool.entity.YxPoolCommodity;

/**
 * @author tongwenhuan
 * 2017年2月24日 上午11:09:08
 */
public interface RemoteCommodityService {
	
	List<YxRemoteCommodity> queryRemoteCommodityFromInterface(String storeCode,String category,String type,Integer sum);
	List<YxRemoteCommodity> getLocalCommodityByType(String storeCode,String type);
	public String syncRemoteCommodity(String storeCode,String category,String type, int sum);
	public String syncRemoteCommodityToPool(String storeCode,Long poolId,String type, int getDays, int sum, String category);
	public YxPoolCommodity convertToPoolCommodity(YxRemoteCommodity rc);
	
	public Map<String,YxRemoteCommodity> queryRemoteCommodityFromInterface(String storeCode,String ids,String type);
	
	List<YxRemoteCommodity> queryCommodityForNSelectOne(String storeCode,String category,Integer sum);
}
