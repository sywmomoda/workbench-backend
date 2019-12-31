package com.feiniu.b2b.store.service;

import java.util.List;
import java.util.Map;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.store.entity.B2BStore;

public interface B2BStoreService {

	 public List<B2BStore> getB2BStoreList();
	 
	 Map<String, B2BStore> getB2BStoreMap();
	 
	 List<B2BStore> getB2BStoreByGroupIds(String ids);
	 
	 /**
	  * 根据ID查询
	  *@author yehui
	  *Feb 8, 2017
	  *@param ids
	  *@return
	  */
	 public List<B2BStore> getB2BStoreByIds(String ids);
	 /**
	  * 根据门店code查询
	  *@author yehui
	  *Feb 15, 2017
	  *@param codes
	  *@return
	  */
	 public List<B2BStore> getStoreByCodes(String codes);
	 /**
	  * 
	  * @param codes
	  * @return
	  */
	 public String getStoreNamesByCodes(String codes);
	 
	 /**
	  * 根据门店code查询单个门店信息
	  * @author lizhiyong
	  * 2017年2月17日
	  * @param code
	  * @return
	  * TODO
	  */
	 public B2BStore getB2BStoreByCode(String code);
	 
	 /**
	  * 根据门店codes查询单个门店信息 返回map，key=code,value=B2BStore
	  * @author lizhiyong
	  * 2017年2月17日
	  * @param code
	  * @return
	  * TODO
	  */
	 public Map<String, B2BStore> getB2BStoreMapByCode(String code);
	 
	 /**
	  * 接口同步门店数据
	  *@author yehui
	  *Mar 10, 2017
	 * @return 
	  */
	 public JSONObject  synchroRemoteStoreInfo();
	 /**
	  * 根据门店codes查询单个有效状态的门店信息
	  * @author lizhiyong
	  * 2017年5月5日
	  * @param code
	  * @return
	  * TODO
	  */
	List<B2BStore> getActivityStoreByCodes(String codes);


	public List<B2BStore> searchB2BStore(B2BStore s);

	/**
	 * 根据门店code获取大区ID  areaID
	 * @param code
	 * @return
	 */
	public String getStoreSeqByCode(String code);
	
	public List<B2BStore> getStoreByPgSeq(String pgSeq);
	
	public B2BStore getB2BStoreById(Long id);
	
	public List<B2BStore> getStoreBySubareaId(String subareaId);
	
	public String getStoreCodesByXiaoQuCodes(String subareaId);
	
	public String getStoreNamesByXiaoQuCodes(String subareaId);
	
	public List<B2BStore> searchB2BStoreByNameOrCode(B2BStore s);
}
