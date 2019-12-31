package com.feiniu.yx.core;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface CommoditySaleInfoService {
	
	/**
	 * 查询商品销量
	 * @param storeCode
	 * @param commodityId
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	long querySaleInfo(String storeCode,String commodityId,long beginTime,long endTime);
	
	/**
	 * 查询商品评论
	 * @param storeCode
	 * @param commodityId
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	List<JSONObject> queryComments(String commodityId,String exceptWords);

}
