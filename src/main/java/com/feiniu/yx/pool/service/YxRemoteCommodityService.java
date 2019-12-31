package com.feiniu.yx.pool.service;

import java.util.List;
import java.util.Map;

import com.feiniu.yx.pool.entity.YxPoolCommodity;

public interface YxRemoteCommodityService {
	
	/**
	 * 货号查询
	 * @param ids
	 * @return
	 */
	public List<YxPoolCommodity> getRemoteCommodityListByIds(String[] ids);
	
	/**
	 * 
	 * @param ids
	 * @param searchType  0:货号，1:RT货号
	 * @return
	 */
	public List<YxPoolCommodity> getRemoteCommodityListByIds(String[] ids,int searchType);
	/**
	 * 
	 * @param ids
	 * @param searchType  0:货号，1:RT货号
	 * @return
	 */
	public Map<String,YxPoolCommodity>  getRemoteCommodityMapByIds(String[] ids,int searchType);


}
