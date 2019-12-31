package com.feiniu.yx.pool.service;

import java.util.List;

import com.feiniu.yx.pool.entity.YxPoolProperPlus;

public interface YxPoolProperPlusService {
	public Long[] batchInsert(List<YxPoolProperPlus> list);
	public List<YxPoolProperPlus> queryProperList(Long commodityId);
	public void updateProperCommodityId(String ids,Long commodityId);
	public YxPoolProperPlus queryProperPlusSingle(YxPoolProperPlus properPlus);
}
