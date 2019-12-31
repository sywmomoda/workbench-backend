package com.feiniu.b2b.store.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.store.dao.B2BStoreDao;
import com.feiniu.b2b.store.dao.B2BStoreGroupDao;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.entity.B2BStoreGroup;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.util.HttpTookit;
import com.feiniu.yx.util.YxPoolConst;
import com.fn.cache.client.RedisCacheClient;

@Service
public class B2BStoreServiceImpl implements B2BStoreService {
	private static Logger logger = Logger.getLogger(B2BStoreService.class);
	
	@Autowired
	private B2BStoreDao storeDao;
	
	@Autowired
	private B2BStoreGroupDao groupDao;
	
	@Autowired
	private RedisCacheClient cacheClient;
	
	private static final String B2B_STORE_CACHE = "B2B_STORE_CACHE_FOR_SEQ";
	
	@Override
	public List<B2BStore> getB2BStoreList() {
		List<B2BStore> list = storeDao.getStoreList();
		return list;
	}

	@Override
	public Map<String, B2BStore> getB2BStoreMap() {
		List<B2BStore> list = storeDao.getStoreList();
		Map<String, B2BStore> b2bStoreMap = new HashMap<String, B2BStore>();
		for(B2BStore s : list) {
			b2bStoreMap.put(s.getCode(), s);
		}
		return b2bStoreMap;
	}
	
	public List<B2BStore> getB2BStoreByIds(String ids){
		List<B2BStore> list = storeDao.getStoreByIds(ids.split(","));
		return list;
	}

	@Override
	public List<B2BStore> getB2BStoreByGroupIds(String ids) {
		List<B2BStore> stores = new ArrayList<B2BStore>();
		Map<String,B2BStore> m = new HashMap<String,B2BStore>();
		if(StringUtils.isBlank(ids))return stores;
		Map<String, B2BStore> storeMap = getB2BStoreMap();
		String[] groupIds = ids.split(",");
		for(String gid : groupIds) {
			if(!StringUtils.isNumeric(gid)){
				continue;
			}
			B2BStoreGroup g = groupDao.getB2BStoreGroupById(Long.parseLong(gid));
			if(g!=null){
				String storeCodes = g.getStoreId();
				if(StringUtils.isBlank(storeCodes)) {
					continue;
				}
				String[] codes = storeCodes.split(",");
				for(String code : codes) {
					B2BStore s = storeMap.get(code);
					if(s == null)continue;
					if(m.get(code) == null) {
						m.put(s.getCode(), s);
						stores.add(s);
					} 
				}
			}
			
		}
		return stores;
	}


	@Override
	public B2BStore getB2BStoreByCode(String code) {
		return storeDao.getStoreByCode(code);
	}
	
	@Override
	public List<B2BStore> getStoreByCodes(String codes) {
		List<B2BStore> list = new ArrayList<B2BStore>();
		String[] codeArray = codes.split(",");
		if(null == codeArray){
			return list;
		}
		
		for(int i =0; i < codeArray.length;i++){
		  B2BStore ys =storeDao.getStoreByCode(codeArray[i]);
		  if(ys != null){
			  list.add(ys);
		  }
		}
		return list;
	}
	
	@Override
	public List<B2BStore> getActivityStoreByCodes(String codes) {
		if(codes==null){
			return null;
		}
		String[] ids =codes.split(",");
		List<B2BStore> list = storeDao.getStoreByCodes(ids);
		return list;
	}
	
	public String getStoreNamesByCodes(String codes){
		List<B2BStore> list  = getActivityStoreByCodes(codes);
		if(null == list){
			return "";
		}
		List<String> codeList = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++){
			B2BStore store = list.get(i);
			if(null == store){
				continue;
			}
			String name = store.getName();
			if(StringUtils.isBlank(name)){
				continue;
			}
			codeList.add(name);
		}
		String[] codesArray =  new String[codeList.size()];
		codeList.toArray(codesArray);
		return StringUtils.join(codesArray, ",");
	}

	/**
	 * code为null时返回所有门店，非null时返回code对应的门店
	 */
	@Override
	public Map<String, B2BStore> getB2BStoreMapByCode(String code) {
		Map<String, B2BStore> b2bStoreMap = getB2BStoreMap();
		if(null  == code){
			return b2bStoreMap;
		}
		
		String[] codes = code.split(",");
		getB2BStoreMap();
		Map<String,B2BStore> newStore = new HashMap<String,B2BStore>();
		for(String cd : codes){
			if(b2bStoreMap.containsKey(cd)){
				newStore.put(cd, b2bStoreMap.get(cd));
			}
		}
		return newStore;
	}
	
	private final static String URL_RemoteStore=YxPoolConst.B2B_COMMODITY_HOST_DOMAIN+"/rt/getAllRtInfo";
	
	@Override
	public JSONObject synchroRemoteStoreInfo() {
		JSONObject resultObject = new JSONObject();
		String result = null;
		try {
			result = HttpTookit.doPost(URL_RemoteStore,new HashMap<String,String>());
		}catch(Exception e){
			resultObject.put("code", 0);
			resultObject.put("msg", "接口数据返回数据异常");
			resultObject.put("data",result);
			return resultObject;
		}
		
		JSONObject remoteObject = null;
		try {
			remoteObject = JSONObject.parseObject(result);
		} catch (Exception e) {
			resultObject.put("code", 0);
			resultObject.put("msg", "接口数据返回数据异常");
			resultObject.put("data",result);
			return resultObject;
		}
		
		if(null == remoteObject){
			resultObject.put("code", 0);
			resultObject.put("msg", "接口数据返回数据异常");
			resultObject.put("data",result);
			return resultObject;
		}
		
		
		String code = remoteObject.getString("code");
		

		if(!code.equals("0")){
			resultObject.put("code", 0);
			resultObject.put("msg", "接口数据请求失败");
			resultObject.put("data",result);
			return resultObject;
		}
		
		String data = remoteObject.getString("data");
		
		if(StringUtils.isBlank(data)){
			resultObject.put("code", 0);
			resultObject.put("msg", "接口数据异常");
			resultObject.put("data",result);
			return remoteObject;
		}
		
		
		
		JSONArray remoteData = remoteObject.getJSONArray("data");
		
		if(remoteData.size() == 0){
			resultObject.put("code", 0);
			resultObject.put("msg", "接口数据返回数据异常");
			resultObject.put("data",result);
			return resultObject;
		}
		
		try {
			synchroData(remoteData);
		} catch (Exception e) {
			logger.error(e.toString());
			resultObject.put("code", 0);
			resultObject.put("msg", "门店同步异常");
			resultObject.put("data",remoteData.toJSONString());
			return remoteObject;
		}
		
		resultObject.put("code", 1);
		resultObject.put("msg", "门店同步成功");
		resultObject.put("data",remoteData.toJSONString());
		return resultObject;
	}
	
	private final static String URL_RemoteStoreIfo=YxPoolConst.B2B_COMMODITY_HOST_DOMAIN+"/rt/getRtInfo";
	
	
	public JSONObject getStoreInfo(String storeCode) {
		Map<String,String> param = new HashMap<String,String>();
		JSONObject data = new JSONObject();
		data.put("sup_type", 1);
		if(StringUtils.isBlank(storeCode)){
			data.put("rt_no",storeCode);
		}
		param.put("data", data.toString());
		JSONObject remoteObject = null;
		String result = null;
		try {
			result = HttpTookit.doPost(URL_RemoteStoreIfo,param);
			remoteObject = JSONObject.parseObject(result);
		}catch(Exception e){
			logger.debug(URL_RemoteStoreIfo+":接口异常");
		}
		
		if(null == remoteObject){
			return null;
		}
		String code = remoteObject.getString("code");		
		if(!code.equals("0")){
			logger.debug(URL_RemoteStoreIfo+":接口异常["+result+"]");
			return null;
		}
		
		JSONObject dataObject = remoteObject.getJSONObject("data");
		if(dataObject == null){
			return null;
		}
		JSONArray  rt = dataObject.getJSONArray("rt");
		if(null == rt || rt.size() == 0){
			return null;
		}
		
		JSONObject resultObject = new JSONObject();
		for(int i = 0; i < rt.size(); i++){
			JSONObject single = rt.getJSONObject(i);
			if(null == single){
				continue;
			}
			resultObject.put(single.getString("rt_no"), single);
		}
		return resultObject;
	}
	
	
	private void synchroData(JSONArray dataArray){	
		JSONObject rtInfo = getStoreInfo(null);
		List<B2BStore> storeList = new ArrayList<B2BStore>();
		for(int i = 0; i < dataArray.size(); i++){
			JSONObject oo = dataArray.getJSONObject(i);
			B2BStore store = new B2BStore();
			String code = oo.getString("rt_no");
			store.setCode(code);
			JSONObject single = rtInfo.getJSONObject(code);
			String subAreaId = single != null ? single.getString("sub_area_id") : "";
			store.setSubAreaId(subAreaId);
			store.setName(oo.getString("rt_name"));
			store.setStatus(oo.getIntValue("status"));
			store.setAreaId(oo.getString("area_id"));
			store.setCreateId("system");
			store.setUpdateId("system");
			storeList.add(store);
		}
		storeDao.deleteStoreAll();
		storeDao.insertStoreBatch(storeList);
	}

	@Override
	public List<B2BStore> searchB2BStore(B2BStore s) {
		return storeDao.searchB2BStore(s);
	}

	@Override
	public String getStoreSeqByCode(String code) {
		String seq = null;
		String cache = cacheClient.get(B2B_STORE_CACHE);
		if (cache == null) {
			List<B2BStore> list = storeDao.getStoreList();
			JSONObject jo = new JSONObject();
			for (B2BStore s : list) {
				if (s.getCode().equals(code)) {
					seq = s.getAreaId();
				}
				jo.put(s.getCode(), s.getAreaId());
			}
			cacheClient.put(B2B_STORE_CACHE, 300, jo.toJSONString());
		} else {
			JSONObject jo = JSON.parseObject(cache);
			seq = jo.getString(code);
		}
		return seq;
	}
	
	@Override
	public List<B2BStore> getStoreByPgSeq(String pgSeq){
		return storeDao.getStoreByPgSeq(pgSeq);
	}
	
	public B2BStore getB2BStoreById(Long id){
		return storeDao.getB2BStoreById(id);
	}
	
	
	@Override
	public List<B2BStore> getStoreBySubareaId(String subareaId) {
		return storeDao.getStoreBySuareaId(subareaId);
	}
	
	@Override
	public String getStoreCodesByXiaoQuCodes(String subareaId) {
		List<B2BStore> storeList = getStoreBySubareaId(subareaId);
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < storeList.size(); i++){
			B2BStore store = storeList.get(i);
			if(null == store){
				continue;
			}
			sb.append(store.getCode()).append(",");
			
		}
		String codes = sb.toString();
		if(codes.endsWith(",")){
			codes = codes.substring(0, codes.length() -1);
		}
		return codes;
	}
	
	@Override
	public String getStoreNamesByXiaoQuCodes(String subareaId) {
		List<B2BStore> storeList = getStoreBySubareaId(subareaId);
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < storeList.size(); i++){
			B2BStore store = storeList.get(i);
			if(null == store){
				continue;
			}
			sb.append(store.getName()).append(",");
			
		}
		String names = sb.toString();
		if(names.endsWith(",")){
			names = names.substring(0, names.length() -1);
		}
		return names;
	}
	
	
	@Override
	public List<B2BStore> searchB2BStoreByNameOrCode(B2BStore s) {
		return storeDao.searchStoreByNameOrCode(s);
	}
	
}
