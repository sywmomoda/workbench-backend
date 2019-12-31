package com.feiniu.yx.store.service.impl;

import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.store.dao.YXStoreDao;
import com.feiniu.yx.store.dao.YXStoreGroupDao;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.entity.YXStoreGroup;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.HttpTookit;
import com.feiniu.yx.util.TreeDto;
import scala.util.parsing.combinator.testing.Str;

@Service
public class YXStoreServiceImpl implements YXStoreService {
	
	public static Logger logger = Logger.getLogger(YXStoreServiceImpl.class);
	
	@Autowired
	private YXStoreDao storeDao;
	
	@Autowired
	private YXStoreGroupDao groupDao;
	
	@Override
	public List<YXStore> getYXStoreList() {
		List<YXStore> list = storeDao.getStoreList();
		return list;
	}

	@Override
	public Map<String, YXStore> getYXStoreMap() {
		List<YXStore> list = storeDao.getStoreList();
		Map<String, YXStore> m = new HashMap<String, YXStore>();
		for(YXStore s : list) {
			m.put(s.getCode(), s);
		}
		return m;
	}
	
	public List<YXStore> getYXStoreByIds(String ids){
		List<YXStore> list = storeDao.getStoreByIds(ids.split(","));
		return list;
	}

	@Override
	public List<YXStore> getYXStoreByGroupIds(String ids) {
		List<YXStore> stores = new ArrayList<YXStore>();
		Map<String,YXStore> m = new HashMap<String,YXStore>();
		if(StringUtils.isBlank(ids))return stores;
		Map<String, YXStore> storeMap = getYXStoreMap();
		String[] groupIds = ids.split(",");
		for(String gid : groupIds) {
			if(!StringUtils.isNumeric(gid)){
				continue;
			}
			YXStoreGroup g = groupDao.getYXStoreGroupById(Long.parseLong(gid));
			if (g == null) continue;
			String storeCodes = g.getStoreId();
			if(StringUtils.isBlank(storeCodes)) {
				continue;
			}
			String[] codes = storeCodes.split(",");
			for(String code : codes) {
				YXStore s = storeMap.get(code);
				if(s == null)continue;
				if(m.get(code) == null) {
					m.put(s.getCode(), s);
					stores.add(s);
				}
			}
		}
		return stores;
	}


	@Override
	public YXStore getYXStoreByCode(String code) {
		return storeDao.getStoreByCode(code);
	}
	
	@Override
	public List<YXStore> getStoreByCodes(String codes) {
		List<YXStore> list = new ArrayList<YXStore>();
		String[] codeArray = codes.split(",");
		if(null == codeArray){
			return list;
		}else{
			list = storeDao.getStoreByCodes(codeArray);
			return list;
		}
	}
	
	@Override
	public List<YXStore> getActivityStoreByCodes(String codeString) {
		if(codeString==null){
			return null;
		}
		String[] codes =codeString.split(",");
		List<YXStore> list = storeDao.getStoreByCodes(codes);
		return list;
	}

	/**
	 * code为null时返回所有门店，非null时返回code对应的门店
	 */
	@Override
	public Map<String, YXStore> getYXStoreMapByCode(String code) {
		List<YXStore> list = null;
		if(StringUtils.isNotBlank(code)){
			list = getStoreByCodes(code);
		}else{
			list =getYXStoreList();
		}
		if(list!=null && list.size()>0){
			Map<String,YXStore> storeMap = new HashMap<String, YXStore>();
			for(YXStore store:list){
				storeMap.put(store.getCode(), store);
			}
			return storeMap;
		}
		return null;
	}
	
	/**
	 * 参数：
	 * 优鲜 {areaCodes:['CPG1,CPG2,CPG3,CPG4,CPG5,CPG6']}
	 * 欧尚{areaCodes:['CPG1000'],'type':2}
	 * type=2 为欧尚，不传默认为优鲜	
	 */
	private final static String URL_RemoteStore=SystemEnv.getProperty("yxStoreRemote");
	
	@Override
	public JSONObject synchroRemoteStoreInfo(String areaCodes) {
		JSONObject resultObject = new JSONObject();
		JSONObject param = new JSONObject();
		param.put("areaCodes", areaCodes.split(","));
		Map<String,String> args = new HashMap<String,String>();
		args.put("data", param.toJSONString());
		String result = null;
		try {
			result = HttpTookit.doPost(URL_RemoteStore,args);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
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
			logger.error(e.getMessage(), e);
			return resultObject;
		}
		
		if(null == remoteObject){
			resultObject.put("code", 0);
			resultObject.put("msg", "接口数据请求失败");
			resultObject.put("data",result);
			return resultObject;
		}
		
		String success = remoteObject.getString("success");
		
		if(!success.equals("1")){
			resultObject.put("code", 0);
			resultObject.put("msg", "接口数据请求失败");
			resultObject.put("data",result);
			return resultObject;
		}
		
		Object dataObj = remoteObject.get("data");
		JSONArray remoteData = new JSONArray();
		if(dataObj instanceof List){
			JSONArray dataArray = remoteObject.getJSONArray("data");
			remoteData.addAll(dataArray);
		}else{
			remoteData.add(remoteObject.getJSONObject("data"));
		}
		
		if(remoteData.size() == 0){
			resultObject.put("code", 0);
			resultObject.put("msg", "接口数据返回数据异常");
			resultObject.put("data",result);
			return resultObject;
		}
		
		
		try {
			storeDao.deleteStoreAll();
			for(int i = 0; i < remoteData.size(); i++){
				JSONObject objectArea = remoteData.getJSONObject(i);
				for(Entry<String, Object> entry : objectArea.entrySet()){
					if(null == entry.getValue()){
						continue;
					}
					JSONArray storeArray = objectArea.getJSONArray(entry.getKey());
					synchroData(storeArray);
				}
				
			}
		} catch (Exception e) {
			resultObject.put("code", 0);
			resultObject.put("msg", "门店同步异常");
			resultObject.put("data",remoteData.toJSONString());
			logger.error(e.getMessage(), e);
			return resultObject;
		}
		
		resultObject.put("code", 1);
		resultObject.put("msg", "门店同步成功");
		resultObject.put("data",remoteData.toJSONString());
		return resultObject;
	}
	
	private void synchroData(JSONArray stores){		
		List<YXStore> storeList = new ArrayList<YXStore>();
		for(int i = 0, len = stores.size(); i < len ;i++){
				JSONObject oo = stores.getJSONObject(i);
				if(null == oo){
					continue;
				}
				YXStore store = new YXStore();
				store.setCode(oo.getString("storeId"));
				store.setName(oo.getString("storeName"));
				store.setStatus(oo.getIntValue("status"));
				store.setPgSeq(oo.getString("pgSeq"));
				store.setPgName(oo.getString("pgName"));
				store.setCreateId("system");
				store.setUpdateId("system");
				storeList.add(store);
		}
		storeDao.insertStoreBatch(storeList);
	}
	
	
	
	@Override
	public String getTreeJsonStore(String storeCodes){
		List<YXStore> groupList = null;
		if(StringUtils.isNotBlank(storeCodes)){
			groupList = getActivityStoreByCodes(storeCodes);
		}else{
			groupList = getYXStoreList();
		}
		 
		List<TreeDto> listDto= getTreeByModule(groupList,"");
		return JSONObject.toJSONString(listDto);
	}

	private List<TreeDto> getTreeByModule(List<YXStore> storeList,
			String checkedIds) {
		List<TreeDto> treeList = new ArrayList<TreeDto>();
		for(YXStore m:storeList){
			//String ids = m.getStoreId();
			/*if(StringUtils.isBlank(ids)){
				continue;
			}*/
			TreeDto cTree = new TreeDto();
			cTree.setId(m.getId());
			cTree.setText(m.getName()+"["+m.getCode()+"]");
			if((","+checkedIds+",").contains(","+m.getId()+",")){
				cTree.setChecked(true);
			}
			treeList.add(cTree);
		}
		return treeList;
	}

	@Override
	public List<YXStore> searchYXStoreByNameOrCode(YXStore s) {
		return storeDao.searchStoreByNameOrCode(s);
	}
	
	@Override
	public List<YXStore> searchYXStoreByNameCode(YXStore s) {
		return storeDao.searchStoreByNameCode(s);
	}

	@Override
	public YXStore getYXStoreById(Long id) {
		return storeDao.getStoreById(id);
	}
	
	public List<YXStore> getStoreByPgSeq(String pgSeq){
		return storeDao.getStoreByPgSeq(pgSeq);
	}
	
	@Override
	public String getStoreNamesByCodes(String storeCodes) {
		List<YXStore> storeList = getActivityStoreByCodes(storeCodes);
		StringBuffer sbNames = new StringBuffer();
		for(int i = 0,len = storeList.size(); i < len; i++){
			YXStore ys = storeList.get(i);
			if(null == ys){
				continue;
			}
			String name = ys.getName();
			name =i + 1 == len ? name : name + ",";
			sbNames.append(name);
		}	
		return sbNames.toString();
	}

	@Override
	public Map<String,List> validateStoreIds(String[] storeIds) {
		if(storeIds==null){
			return null;
		}

		List<String> correctStoreIds = new ArrayList();
		List<String> errorStoreIds = new ArrayList();
		//List<YXStore> storeList = storeDao.getStoreList();
		List<YXStoreGroup> groupList = groupDao.getYXStoreGroupList();
		List<String> allStoreIds = new ArrayList<>();
		List<Long> storeGroupIds = new ArrayList<>();
		List<Long> newStoreGroupIds = new ArrayList<>();
		Map<String,Long> map = new HashMap();
		for (YXStoreGroup yxStoreGroup : groupList) {
			String storeId = yxStoreGroup.getStoreId();// 1002,1001,1007,1008,1108,1009
			Long id = yxStoreGroup.getId();
			if(StringUtils.isNotBlank(storeId)){
				String[] split = storeId.split(",");
				List<String> storeIdList = Arrays.asList(split);
				allStoreIds.addAll(storeIdList);
				for (String storeId1 : storeIdList) {
					map.put(storeId1,id);
				}
			}
		}
		//test
		System.out.println("包含门店群组的门店id： ");
		for (String allStoreId : allStoreIds) {
			System.out.println(allStoreId);
		}
		for (String storeId : storeIds) {
			if(StringUtils.isBlank(storeId)){
				continue;
			}
			if(allStoreIds.contains(storeId)){
				correctStoreIds.add(storeId);
				Long storeGroupId = map.get(storeId);
				storeGroupIds.add(storeGroupId);
			}else{
				errorStoreIds.add(storeId);
			}
		}
		// 门店群组去重
		for (Long storeGroupId : storeGroupIds) {
			if(!newStoreGroupIds.contains(storeGroupId)){
				newStoreGroupIds.add(storeGroupId);
			}
		}



		Map<String,List> storeIdMap = new HashMap();
		storeIdMap.put("correctStoreIds",correctStoreIds);//正确的门店id
		storeIdMap.put("errorStoreIds",errorStoreIds);// 错误的门店id 或没有门店群组的门店id
		storeIdMap.put("storeGroupIds",newStoreGroupIds);// 门店群组id

		return storeIdMap;
	}

}
