package com.feiniu.yx.pool.service;

import com.feiniu.yx.pool.entity.YxPoolCommodity;

import java.util.List;

/**
 * @author tongwenhuan
 * 2017年3月13日 下午2:37:57
 */
public interface PoolDataService {
	
	List<YxPoolCommodity> findListByIdAndType(Long id, String storeCode, String type, int count, String previewTime);

}
