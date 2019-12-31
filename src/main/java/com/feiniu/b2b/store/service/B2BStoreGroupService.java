package com.feiniu.b2b.store.service;


import java.util.List;
import java.util.Map;

import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.entity.B2BStoreGroup;


public interface B2BStoreGroupService {
	public void insertStoreGroup(B2BStoreGroup group);
	public B2BStoreGroup getStoreGroup(Long Id);
	public void updateStoreGroup(B2BStoreGroup group);
	/**
     * 获取所有父节点map
     * key:code
	 * @return the fmap
	 */
	public Map<String, B2BStoreGroup> getFmap();
	
	/**
	 *获取所有门店的群组
	 * @return
	 */
	
	public Map<String,B2BStoreGroup> getStoreKeyGroupMap();
	
	/**
	 * 根据门店群组，查询门店
	 *@author yehui
	 *Feb 15, 2017
	 *@param group
	 *@return
	 */
	public List<B2BStore> getStoreList(B2BStoreGroup group);
	

	
	/**
	 * 通过门店查询群组
	 *@author yehui
	 *Feb 15, 2017
	 *@param store
	 *@return
	 */
    public B2BStoreGroup getStoreGroup(B2BStore store);
	
	public String getTreeJsonStoreGroup(String checkedIds);
	/**
	 * 根据群组IDS查询所有群组，IDS为空时。返回所有群组
	 * @author lizhiyong
	 * 2017年2月17日
	 * @param ids
	 * @return
	 * TODO
	 */
	public List<B2BStoreGroup> getStoreGroupListByGroupIds(String ids);
	
	public String getStoreCodesByGroupIds(String ids);
	

	/**
	 * 根据门店群组获取该群组下所有有效的门店
	 * @author lizhiyong
	 * 2017年5月5日
	 * @param group
	 * @return
	 * TODO
	 */
	public List<B2BStore> getActivityStoreList(B2BStoreGroup group);
	
	/**
	 * 根据门店群组获取该群组下所有有效的门店
	 * @author lizhiyong
	 * 2017年5月5日
	 * @param group
	 * @return
	 * TODO
	 */
	public String getTreeSelectStoreGroup(String checkedIds,String storeGroupIds);
	
	
	public Map<String,String> getGroupNameOfStoreCode();
	
	public void deleteB2BStoreGroupById(Long id);
}
