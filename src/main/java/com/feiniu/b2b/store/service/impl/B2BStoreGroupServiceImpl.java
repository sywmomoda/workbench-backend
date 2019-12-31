package com.feiniu.b2b.store.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.store.dao.B2BStoreGroupDao;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.entity.B2BStoreGroup;
import com.feiniu.b2b.store.service.B2BStoreGroupService;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.util.TreeDto;

@Service
public class B2BStoreGroupServiceImpl implements B2BStoreGroupService {
	
	@Autowired
	private B2BStoreGroupDao groupDao;
	
	@Autowired
	private B2BStoreService  storeSerivce;
	
    private Map<String, B2BStoreGroup> fmap = null;//所有父节点
    
    private Map<String,B2BStoreGroup> storeKeyGroupMap = null; // 所有门code对应的群组

	@Override
	public void insertStoreGroup(B2BStoreGroup group) {
		fmap = null;
		groupDao.insert(group); 
	}
	
	@Override
	public B2BStoreGroup getStoreGroup(Long Id){
		B2BStoreGroup  group =groupDao.getB2BStoreGroupById(Id);
		return group;
	}
	
	@Override
	public void updateStoreGroup(B2BStoreGroup group){
		fmap = null;
		groupDao.update(group); 
	}

	@Override
	public Map<String, B2BStoreGroup> getFmap() {
			fmap = new HashMap<String,B2BStoreGroup>();
			List<B2BStoreGroup> list = groupDao.getB2BStoreGroupList();
			for(B2BStoreGroup storeGroup : list) {
				String ids = storeGroup.getStoreId();
				List<B2BStore> listStore = storeSerivce.getStoreByCodes(ids);
				Map<String , B2BStore> storeMap = new HashMap<String , B2BStore>();
				for(B2BStore ys : listStore){
					storeMap.put(ys.getCode(), ys);
					storeGroup.setStoreMap(storeMap);	
				}
				fmap.put(storeGroup.getId().toString(), storeGroup);
				
			}
		return fmap;
	}
	

 @Override
  public Map<String,B2BStoreGroup> getStoreKeyGroupMap(){
	   getFmap();
		if(null == storeKeyGroupMap){
			storeKeyGroupMap = new HashMap<String,B2BStoreGroup>();
			for(Map.Entry<String, B2BStoreGroup> entry : fmap.entrySet()){ 
				B2BStoreGroup group = entry.getValue();
				Map<String,B2BStore> storeMap = group.getStoreMap();
			    for(Map.Entry<String,B2BStore> store : storeMap.entrySet()){
			    	String key  = store.getKey();
			    	storeKeyGroupMap.put(key, group);
			    }
				
			}
		}
		
		return storeKeyGroupMap;
	}
	
	
	@Override
	public List<B2BStore> getStoreList(B2BStoreGroup group) {
		if(null == group){
			return new ArrayList<B2BStore>();
		}
		List<B2BStore> listStore = storeSerivce.getStoreByCodes(group.getStoreId());
		return listStore;
	}
	
	@Override
	public B2BStoreGroup getStoreGroup(B2BStore store) {
		return groupDao.getB2BStoreGroup(store);
	}
	
	
	@Override
	public String getTreeJsonStoreGroup(String checkedIds){
		List<B2BStoreGroup> groupList = groupDao.getB2BStoreGroupList();
		List<TreeDto> listDto= getTreeByModule(groupList,checkedIds);
		return JSONObject.toJSONString(listDto);
	}

	private List<TreeDto> getTreeByModule(List<B2BStoreGroup> groupList,
			String checkedIds) {
		List<TreeDto> treeList = new ArrayList<TreeDto>();
		for(B2BStoreGroup m:groupList){
			String codes = m.getStoreId();
			/*if(StringUtils.isBlank(ids)){
				continue;
			}*/
			TreeDto cTree = new TreeDto();
			cTree.setId(m.getId());
			cTree.setText("("+m.getPgSeq()+")"+m.getName());
			List<TreeDto> childrenList =  new ArrayList<TreeDto>();
			List<B2BStore> mendianList = storeSerivce.getStoreByCodes(codes);
			/*if((","+checkedIds+",").contains(","+m.getId()+",")){
				cTree.setChecked(true);
			}*/
			for(B2BStore c : mendianList){
				TreeDto childNode = new TreeDto();
				childNode.setId(c.getId());
				childNode.setText("("+c.getCode()+")"+c.getName());
				if((","+checkedIds+",").contains(","+c.getId()+",")){
						childNode.setChecked(true);
			     }
				childrenList.add(childNode);
			}
			cTree.setChildren(childrenList);
			treeList.add(cTree);
		}
		return treeList;
	}

	@Override
	public List<B2BStoreGroup> getStoreGroupListByGroupIds(String ids) {
		Map<String, B2BStoreGroup> allMap= getFmap();
		List<B2BStoreGroup> groupList = new ArrayList<B2BStoreGroup>();
		if(StringUtils.isNotBlank(ids)){
			if(allMap!=null){
				String[] idArray = ids.split(",");
				for(String id:idArray){
					B2BStoreGroup  group = allMap.get(id);
					if(group!=null){
						groupList.add(group);
					}
				}
			}
		}else{
			groupList= groupDao.getB2BStoreGroupList();
		}
		return groupList;
	}

	@Override
	public String getStoreCodesByGroupIds(String ids) {
		List<B2BStoreGroup> groups = getStoreGroupListByGroupIds(ids);
		String storeCodes = "";
		for(B2BStoreGroup g:groups){
			storeCodes += g.getStoreId()+",";
		}
		if(storeCodes.endsWith(",")){
			storeCodes = storeCodes.substring(0,storeCodes.length()-1);
		}
		return storeCodes;
	}

	@Override
	public List<B2BStore> getActivityStoreList(B2BStoreGroup group) {
		if(null == group){
			return new ArrayList<B2BStore>();
		}
		List<B2BStore> listStore = storeSerivce.getActivityStoreByCodes(group.getStoreId());
		return listStore;
	}

	@Override
	public String getTreeSelectStoreGroup(String checkedIds,
			String storeGroupIds) {
		List<B2BStoreGroup> groupList = groupDao.getB2BStoreGroupList();
		List<B2BStoreGroup> removeList  = new ArrayList<B2BStoreGroup>();
        for(B2BStoreGroup bg:groupList){
        	if(!(","+storeGroupIds+",").contains(","+bg.getId()+",")){
        		removeList.add(bg);
        		continue;
        	}
        }
        groupList.removeAll(removeList);
		List<TreeDto> listDto= getTreeByModule(groupList,checkedIds);
		return JSONObject.toJSONString(listDto);
	}
	
	/**
     * 获取所有门店对应该的群组名称
     * @param storeCode
     * @return
     */
    public Map<String,String> getGroupNameOfStoreCode(){
    	Map<String,String> resultMap = new HashMap<String,String>();
    	Map<String,B2BStoreGroup> map =  getFmap();
    	for(Entry<String, B2BStoreGroup> entry : map.entrySet()){
    		B2BStoreGroup sg  = entry.getValue();
    		String name = sg.getName();
    		Map<String,B2BStore> storeMap  = sg.getStoreMap();
    		for(Entry<String,B2BStore> entry2 : storeMap.entrySet()){
    			resultMap.put(entry2.getKey(),name);
    		}
    	}
    	
    	return resultMap;
    }
    
	public void deleteB2BStoreGroupById(Long id) {
		groupDao.deleteB2BStoreGroupById(id);
	}
}
