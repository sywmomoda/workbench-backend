package com.feiniu.yx.store.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.UserService;
import com.feiniu.yx.store.dao.YXStoreGroupDao;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.entity.YXStoreGroup;
import com.feiniu.yx.store.service.YXStoreGroupService;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.TreeDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

@Service
public class YXStoreGroupServiceImpl implements YXStoreGroupService {
	
	@Autowired
	private YXStoreGroupDao groupDao;
	
	@Autowired
	private YXStoreService  storeSerivce;
	
	@Autowired
	private UserService userService;

	@Override
	public void insertStoreGroup(YXStoreGroup group) {
		groupDao.insert(group); 
	}
	
	@Override
	public YXStoreGroup getStoreGroup(Long Id){
		YXStoreGroup  group =groupDao.getYXStoreGroupById(Id);
		return group;
	}
	
	@Override
	public void updateStoreGroup(YXStoreGroup group){
		groupDao.update(group); 
	}

	@Override
	public void delStoreGroup(YXStoreGroup group){
		groupDao.del(group);
	}

	@Override
	public Map<String, YXStoreGroup> getFmap() {
		Map<String, YXStoreGroup> map = new HashMap<String,YXStoreGroup>();
		List<YXStoreGroup> list = groupDao.getYXStoreGroupList();
		for(YXStoreGroup storeGroup : list) {
			String ids = storeGroup.getStoreId();
			List<YXStore> listStore = storeSerivce.getStoreByCodes(ids);
			Map<String , YXStore> storeMap = new HashMap<String , YXStore>();
			for(YXStore ys : listStore){
				storeMap.put(ys.getCode(), ys);
			}
			storeGroup.setStoreMap(storeMap);	
			map.put(storeGroup.getId().toString(), storeGroup);
		}
		return map;
	}
	
	@Override
	public List<YXStore> getStoreList(YXStoreGroup group) {
		if(null == group){
			return new ArrayList<YXStore>();
		}
		List<YXStore> listStore = storeSerivce.getStoreByCodes(group.getStoreId());
		return listStore;
	}
	
	
	@Override
	public String getTreeJsonStoreGroup(){
		List<YXStoreGroup> groupList = groupDao.getYXStoreGroupList();
		List<TreeDto> listDto= getTreeByModuleByLevel(4,groupList,"");
		return JSONObject.toJSONString(listDto);
	}
	
	
	@Override
	public String getTreeJsonStoreGroupOfPermission(){
		Map<String,Set<String>> permissonMap = userService.getMapUserStores();
		List<YXStoreGroup> groupList = listYXStoreGroup();
		Set<String> pgSeqSet = permissonMap.get("pgSeqSet");
		Set<String> codeSet = permissonMap.get("codeSet");
		List<TreeDto> treeList = new ArrayList<TreeDto>();
		 if(null == pgSeqSet){
			 return JSONObject.toJSONString(treeList); 
		 }
		 Set<String> existSet = new HashSet<String>();
		 for(YXStoreGroup ysg : groupList){
			 String code = ysg.getPgSeq();
			 if(!pgSeqSet.contains(code)){
				 continue;
			 }
			 String storeId = ysg.getStoreId();
			 if(StringUtils.isBlank(storeId)){
				 continue;
			 }
			 String[] storeArr = storeId.split(",");
			 StringBuffer sbIds = new StringBuffer();
			 for(String storeCode : storeArr){
				 if(codeSet.contains(storeCode)){
					 sbIds.append(storeCode).append(",");
					 existSet.add(storeCode);
				 }
			 }
			 String storeCodes = sbIds.toString();
			 if(storeCodes.endsWith(",")){
				 storeCodes = storeCodes.substring(0,storeCodes.length()-1);
			 }
			 TreeDto cTree = getTreeByPermission(ysg,storeCodes);
			 treeList.add(cTree);
		 }
		 if(existSet.size() == 0){
			 return JSONObject.toJSONString(treeList); 
		 }
		 if(existSet.size() == codeSet.size()){ 
			 return JSONObject.toJSONString(treeList); 
		 }
		//未设置门店
		 StringBuffer sbNoCode = new StringBuffer(); //
		 for(String code : codeSet){
			 if(!existSet.contains(code)){
				 sbNoCode.append(code).append(",");
			 }
		 }
		 String noCodes = sbNoCode.toString();
		 if(noCodes.endsWith(",")){
			 noCodes = noCodes.substring(0,noCodes.length()-1);
		 }
		 YXStoreGroup noStoreGroup = new YXStoreGroup(); 
		 noStoreGroup.setPgSeq("-1");
		 noStoreGroup.setName("未设置群组");
		 TreeDto cTreeNo = getTreeByPermission(noStoreGroup,noCodes);
		 treeList.add(cTreeNo);
		 return JSONObject.toJSONString(treeList); 
		 
	}
	
	private TreeDto getTreeByPermission(YXStoreGroup group,
			String storeCodes){
			TreeDto cTree = new TreeDto();
			cTree.setId(group.getId());
			cTree.setText(group.getName()+"["+group.getPgSeq()+"]");
			List<TreeDto> childrenList =  new ArrayList<TreeDto>();
			List<YXStore> mendianList = storeSerivce.getStoreByCodes(storeCodes);
			for(YXStore c : mendianList){
				TreeDto childNode = new TreeDto();
				childNode.setId(c.getId());
				childNode.setText(c.getName()+"["+c.getCode()+"]");
				childrenList.add(childNode);
			}
			cTree.setChildren(childrenList);
			return cTree;
	}
	
	

	/*private List<TreeDto> getTreeByModule(List<YXStoreGroup> groupList,
			String checkedCodes) {
		List<TreeDto> treeList = new ArrayList<TreeDto>();
		Map<String, List<TreeDto>> childMap = new HashMap<String, List<TreeDto>>();
		for(YXStoreGroup m:groupList){
			String codes = m.getStoreId();
			*//*if(StringUtils.isBlank(ids)){
				continue;
			}*//*
			TreeDto cTree = new TreeDto();
			List<TreeDto> chList = childMap.get(m.getPid()+"");
			if(chList==null){
				chList = new ArrayList<TreeDto>();
			}
			cTree.setParentId(m.getPid()+"");
			cTree.setId(m.getId());
			cTree.setText(m.getName()+"["+m.getPgSeq()+"]");
			List<TreeDto> childrenList =  new ArrayList<TreeDto>();
			if(m.getLevel()==3){
				List<YXStore> mendianList = storeSerivce.getStoreByCodes(codes);
				for(YXStore c : mendianList){
					TreeDto childNode = new TreeDto();
					childNode.setId(c.getId());
					childNode.setText(c.getName()+"["+c.getCode()+"]");
					//checkedCodes为空表示所有门店
					if((","+checkedCodes+",").contains(","+c.getCode()+",") || StringUtils.isBlank(checkedCodes)){
						childNode.setChecked(true);
					}
					childrenList.add(childNode);
				}
				if(childMap.get(m.getId()+"")==null){
					childMap.put(m.getId()+"",childrenList);
				}
			}

			if(childMap.get(m.getId()+"")==null){
				List<TreeDto> list  = new ArrayList<>();
				childMap.put(m.getId()+"",list);
			}

			if(m.getLevel()<=1){
				cTree.setChildren(childMap.get(m.getId()+""));
				treeList.add(cTree);
			}else{
				cTree.setChildren(childMap.get(m.getId()+""));
				chList.add(cTree);
				childMap.put(m.getPid()+"",chList);
			}
		}
		return treeList;
	}*/

	@Override
	public List<YXStoreGroup> getStoreGroupListByGroupIds(String ids) {
		Map<String, YXStoreGroup> allMap= getFmap();
		List<YXStoreGroup> groupList = new ArrayList<YXStoreGroup>();
		if(StringUtils.isNotBlank(ids)){
			if(allMap!=null){
				String[] idArray = ids.split(",");
				for(String id:idArray){
					YXStoreGroup  group = allMap.get(id);
					if(group!=null){
						groupList.add(group);
					}
				}
			}
		}else{
			groupList= groupDao.getYXStoreGroupList();
		}
		return groupList;
	}

	@Override
	public String getStoreCodesByGroupIds(String ids) {
		List<YXStoreGroup> groups = getStoreGroupListByGroupIds(ids);
		String storeCodes = "";
		for(YXStoreGroup g:groups){
			storeCodes += g.getStoreId()+",";
		}
		if(storeCodes.endsWith(",")){
			storeCodes = storeCodes.substring(0,storeCodes.length()-1);
		}
		return storeCodes;
	}
	
	@Override
	public String getGroupAllIds(){
		List<YXStoreGroup> list= listYXStoreGroup();
		StringBuffer sbIds = new StringBuffer();
		for(YXStoreGroup sg : list){
			if(null == sg){
				continue;
			}
			sbIds.append(sg.getId().toString()).append(",");
		}
		String ids = sbIds.toString();
		if(ids.endsWith(",")){
			ids = ids.substring(0,ids.length() -1);	
		}
		return ids;
	}

	@Override
	public List<YXStore> getActivityStoreList(YXStoreGroup group) {
		if(null == group){
			return new ArrayList<YXStore>();
		}
		List<YXStore> listStore = storeSerivce.getActivityStoreByCodes(group.getStoreId());
		return listStore;
	}

	@Override
	public String getTreeSelectStoreGroup(int level, String checkedCodes, String groupIds, String xiaoQuIds) {
		List<YXStoreGroup> groupList = groupDao.getYXStoreGroupList();
		Map<String,YXStoreGroup> groupMap = new HashMap<String,YXStoreGroup>();
		for(YXStoreGroup bg:groupList){
			groupMap.put(bg.getId()+"",bg);
		}

		//池数据编辑时，xiaoQuIds无数据
		if(StringUtils.isBlank(xiaoQuIds)){
			xiaoQuIds = "";
			String newGroupIds = "";
			for(YXStoreGroup bg:groupList){
				if(bg.getLevel()==3){
					YXStoreGroup bgParent = groupMap.get(bg.getPid()+"");
					// 兼容池中旧数据groupIds数据是大区的情况，xiaoQuIds取大区下所有小区
					if((","+groupIds+",").contains(","+bgParent.getPid()+",")){
						xiaoQuIds += bg.getId()+",";
					}
					// 池中新数据groupIds数据是小区
					if((","+groupIds+",").contains(","+bg.getId()+",")){
						xiaoQuIds += bg.getId()+",";
						newGroupIds += bgParent.getPid()+",";
					}
				}
			}
			if(StringUtils.isNotBlank(newGroupIds)){
				groupIds = newGroupIds;
			}
		}

		List<YXStoreGroup> removeList  = new ArrayList<YXStoreGroup>();
		for(YXStoreGroup bg:groupList){
			if(!(","+groupIds+",").contains(","+bg.getId()+",") && !(","+groupIds+",").contains(","+bg.getPid()+",")
					&& !(","+xiaoQuIds+",").contains(","+bg.getId()+",") && !(","+xiaoQuIds+",").contains(","+bg.getPid()+",")
					 ){
					removeList.add(bg);
			}
		}
        groupList.removeAll(removeList);
		List<TreeDto> listDto= getTreeByModuleByLevel(level, groupList,checkedCodes);
		//清除无数据的二级分区
		for(TreeDto td: listDto){
			if(td.getChildren()!=null && td.getChildren().size()>0){
				for(int i=td.getChildren().size()-1; i>0; i--){
					TreeDto tdC = td.getChildren().get(i);
					if(tdC.getChildren()==null || tdC.getChildren().size()==0){
						td.getChildren().remove(tdC);
					}
				}
			}
		}
		return JSONObject.toJSONString(listDto);
	}

	@Override
	public String showSelectGroupByLevel(int level, String checkedNodes) {
		List<YXStoreGroup> groupList = groupDao.getYXStoreGroupList();
		List<TreeDto> listDto= getTreeByModuleByLevel(level, groupList, checkedNodes);
		return JSONObject.toJSONString(listDto);
	}

	private List<TreeDto> getTreeByModuleByLevel(int level, List<YXStoreGroup> groupList,
										  String checkedNodes) {
		List<TreeDto> treeList = new ArrayList<TreeDto>();
		Map<String, List<TreeDto>> childMap = new HashMap<String, List<TreeDto>>();
		for(YXStoreGroup m:groupList){
			String codes = m.getStoreId();
			/*if(StringUtils.isBlank(ids)){
				continue;
			}*/
			if(m.getLevel()>level){
				continue;
			}

			TreeDto cTree = new TreeDto();
			List<TreeDto> chList = childMap.get(m.getPid()+"");
			if(chList==null){
				chList = new ArrayList<TreeDto>();
			}
			cTree.setParentId(m.getPid()+"");
			cTree.setId(m.getId());
			cTree.setText(m.getName()+"["+m.getPgSeq()+"]");
			if((","+checkedNodes+",").contains(","+ cTree.getId()+",")){
				cTree.setChecked(true);
			}
			List<TreeDto> childrenList =  new ArrayList<TreeDto>();
			if(m.getLevel()==3 && m.getLevel()<level){
				List<YXStore> mendianList = storeSerivce.getStoreByCodes(codes);
				for(YXStore c : mendianList){
					TreeDto childNode = new TreeDto();
					childNode.setId(c.getId());
					childNode.setText(c.getName()+"["+c.getCode()+"]");
					//checkedCodes为空表示所有门店
					if((","+checkedNodes+",").contains(","+c.getCode()+",") || StringUtils.isBlank(checkedNodes)){
						childNode.setChecked(true);
					}
					childrenList.add(childNode);
				}
				if(childMap.get(m.getId()+"")==null){
					childMap.put(m.getId()+"",childrenList);
				}
			}

			if(childMap.get(m.getId()+"")==null){
				List<TreeDto> list  = new ArrayList<>();
				childMap.put(m.getId()+"",list);
			}

			if(m.getLevel()<=1){
				cTree.setChildren(childMap.get(m.getId()+""));
				treeList.add(cTree);
			}else{
				cTree.setChildren(childMap.get(m.getId()+""));
				chList.add(cTree);
				childMap.put(m.getPid()+"",chList);
			}
		}
		return treeList;
	}

	@Override
	public List<YXStoreGroup> listYXStoreGroup(String ids) {
		List<YXStoreGroup> list = new ArrayList<YXStoreGroup>();
		if (StringUtils.isBlank(ids)) {
			return list;
		}
		String[] id_array = ids.split(",");
		list = groupDao.listYXStoreGroupByIds(id_array);
		return list;
	}
	
	@Override
	public List<YXStoreGroup> listYXStoreGroup() {
		return groupDao.getYXStoreGroupList();
	}

	@Override
	public List<YXStoreGroup> getAreaGroup() {
		//YXStoreGroup query = new YXStoreGroup();
		//query.setLevel(1);
		List<YXStoreGroup> groupList = new ArrayList<YXStoreGroup>();
		List<YXStoreGroup> allgroupList = groupDao.getYXStoreGroupList();
		Map<String, List<YXStoreGroup>> childMap = new HashMap<String, List<YXStoreGroup>>();
		for(YXStoreGroup m:allgroupList){
			List<YXStoreGroup> xiaoquList = m.getGroupList();
			if(xiaoquList==null){
				xiaoquList = new ArrayList<YXStoreGroup>();
			}


			List<YXStoreGroup> chList = childMap.get(m.getPid()+"");
			if(chList==null){
				chList = new ArrayList<YXStoreGroup>();
			}
			Map<String, YXStore> storeMap = new HashMap<String,YXStore>();
			if(m.getLevel()==3){
				List<YXStore> mendianList = storeSerivce.getStoreByCodes(m.getStoreId());
				for(YXStore c : mendianList){
					storeMap.put(m.getId()+"",c);
				}
				m.setStoreMap(storeMap);
			}

			if(childMap.get(m.getId()+"")==null){
				List<YXStoreGroup> list  = new ArrayList<>();
				childMap.put(m.getId()+"",list);
			}

			if(m.getLevel()<=1){
				m.setGroupList(childMap.get(m.getId()+""));
				groupList.add(m);
			}else{
				m.setGroupList(childMap.get(m.getId()+""));
				chList.add(m);
				childMap.put(m.getPid()+"",chList);
			}
		}

		return groupList;
	}
	
	@Override
	public List<YXStoreGroup> getYXStoreGroupByPgSeq(String pgSeq){
		List<YXStoreGroup> groupList= new ArrayList<YXStoreGroup>();
		List<YXStoreGroup> list = listYXStoreGroup();
		for(YXStoreGroup sg : list){
			if(null == sg){
				continue;
			}
			String ps = sg.getPgSeq();
			if(StringUtils.isBlank(ps)){
				continue;
			}
			if(ps.equals(pgSeq)){
				groupList.add(sg);
			}
		}
		
		return groupList;
	}
	
    /**
     * 获取所有门店对应该的群组名称
     * @param
     * @return
     */
    public Map<String,String> getGroupNameOfStoreCode(){
    	Map<String,String> resultMap = new HashMap<String,String>();
    	Map<String,YXStoreGroup> map =  getFmap();
    	for(Entry<String, YXStoreGroup> entry : map.entrySet()){
    		YXStoreGroup sg  = entry.getValue();
    		String name = sg.getName();
    		Map<String,YXStore> storeMap  = sg.getStoreMap();
    		for(Entry<String,YXStore> entry2 : storeMap.entrySet()){
    			resultMap.put(entry2.getKey(),name);
    		}
    	}
    	
    	return resultMap;
    }
}
