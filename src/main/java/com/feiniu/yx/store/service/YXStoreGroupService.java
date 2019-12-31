package com.feiniu.yx.store.service;


import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.entity.YXStoreGroup;

import java.util.List;
import java.util.Map;

public interface YXStoreGroupService {
	
	public void insertStoreGroup(YXStoreGroup group);
	
	public YXStoreGroup getStoreGroup(Long Id);
	
	public void updateStoreGroup(YXStoreGroup group);

	public void delStoreGroup(YXStoreGroup group);
	/**
     * 获取所有父节点map
     * key:code
	 * @return the fmap
	 */
	public Map<String, YXStoreGroup> getFmap();
	
	/**
	 * 根据门店群组，查询门店
	 *@author yehui
	 *Feb 15, 2017
	 *@param group
	 *@return
	 */
	public List<YXStore> getStoreList(YXStoreGroup group);

	/**
	 * 显示所有结构
	 * @return
	 */
	public String getTreeJsonStoreGroup();
	/**
	 * 根据群组IDS查询所有群组，IDS为空时。返回所有群组
	 * @author lizhiyong
	 * 2017年2月17日
	 * @param ids
	 * @return
	 * TODO
	 */
	public List<YXStoreGroup> getStoreGroupListByGroupIds(String ids);
	
	public String getStoreCodesByGroupIds(String ids);
	
	List<YXStoreGroup> listYXStoreGroup(String ids);
	
	List<YXStoreGroup> listYXStoreGroup();

	List<YXStoreGroup> getAreaGroup();

	/**
	 * 根据门店群组获取该群组下所有有效的门店
	 * @author lizhiyong
	 * 2017年5月5日
	 * @param group
	 * @return
	 * TODO
	 */
	public List<YXStore> getActivityStoreList(YXStoreGroup group);
	/**
	 * 
	 * @return
	 */
	public Map<String,String> getGroupNameOfStoreCode();

	
	public String getTreeSelectStoreGroup(int level, String checkedCodes, String groupIds, String xiaoQuIds);

	public String showSelectGroupByLevel(int level, String checkedNodes);
	
	/**
	 * 权限门店权限
	 * @return
	 */
	public String getTreeJsonStoreGroupOfPermission();
	

	public List<YXStoreGroup> getYXStoreGroupByPgSeq(String pgSeq);
	
	public String getGroupAllIds();
}
