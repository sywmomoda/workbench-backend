package com.feiniu.yx.core;

import java.util.List;

import com.feiniu.yx.pool.entity.YxPoolCommodity;

public interface SuperCommodityService {
	
	/**
	 * 重新设置商品信息
	 * @param list
	 * @param storeCode
	 */
	void resetParams(List<YxPoolCommodity> list, String storeCode);

}
