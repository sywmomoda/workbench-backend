package com.feiniu.yx.store.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.store.entity.YXStore;

public interface YXStoreService {

	 public List<YXStore> getYXStoreList();
	 
	 Map<String, YXStore> getYXStoreMap();
	 
	 List<YXStore> getYXStoreByGroupIds(String ids);
	 
	 /**
	  * 根据ID查询
	  *@author yehui
	  *Feb 8, 2017
	  *@param ids
	  *@return
	  */
	 public List<YXStore> getYXStoreByIds(String ids);
	 /**
	  * 根据门店code查询
	  *@author yehui
	  *Feb 15, 2017
	  *@param codes
	  *@return
	  */
	 public List<YXStore> getStoreByCodes(String codes);
	 /**
	  * 根据门店code查询单个门店信息
	  * @author lizhiyong
	  * 2017年2月17日
	  * @param code
	  * @return
	  * TODO
	  */
	 public YXStore getYXStoreByCode(String code);
	 
	 /**
	  * 根据门店codes查询单个门店信息 返回map，key=code,value=YXStore
	  * @author lizhiyong
	  * 2017年2月17日
	  * @param code
	  * @return
	  * TODO
	  */
	 public Map<String, YXStore> getYXStoreMapByCode(String code);
	 
	 /**
	  * 接口同步门店数据
	  *@author yehui
	  *Mar 10, 2017
	 * @return 
	  */
	 public JSONObject  synchroRemoteStoreInfo(String areaCodes);
	 /**
	  * 根据门店codes查询单个有效状态的门店信息
	  * @author lizhiyong
	  * 2017年5月5日
	  * @param code
	  * @return
	  * TODO
	  */
	List<YXStore> getActivityStoreByCodes(String codes);
	
	/**
	 * 
	 * @return
	 */
	public String getTreeJsonStore(String storeCodes);

	public List<YXStore> searchYXStoreByNameOrCode(YXStore s);
	
	public List<YXStore> searchYXStoreByNameCode(YXStore s);

	public YXStore getYXStoreById(Long id);
	
	public List<YXStore> getStoreByPgSeq(String pgSeq);
	
	public String getStoreNamesByCodes(String storeCodes);

	Map<String,List> validateStoreIds(String[] groupIds);
}
