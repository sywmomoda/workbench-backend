package com.feiniu.yx.pool.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.pool.entity.YxPoolCommodity;

public interface PoolCommodityService {
	
	/**
	 * 查池分页数据
	 * @param data
	 * @return
	 */
	JSONObject pageCommodityData(String data);
	
	/**
	 * 查模块绑定的池数据
	 * @return
	 */
	List<YxPoolCommodity> listCommodityDataForModule(Long poolId, String storeCode, String previewTime);

}
