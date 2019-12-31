package com.feiniu.b2b.page.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.coupon.service.B2BCouponService;
import com.feiniu.b2b.page.service.B2BModuleProperPlusService;
import com.feiniu.b2b.store.dao.B2BStoreGroupDao;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.entity.B2BStoreGroup;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.page.dao.ModuleProperPlusDao;
import com.feiniu.yx.page.dao.ModuleProperPlusOnlineDao;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.util.TreeDto;
import com.feiniu.yx.util.UserUtil;

@Service
public class B2BModuleProperPlusServiceImpl implements B2BModuleProperPlusService{
	
	@Autowired
	private ModuleProperPlusDao moduleProperPlusDao;
	
	@Autowired
	private ModuleProperPlusOnlineDao moduleProperPlusOnlineDao;
	
	@Autowired
	private B2BStoreGroupDao groupDao;
	
	@Autowired
	private B2BStoreService  storeSerivce;
	
	
	@Autowired
	private B2BCouponService couponService;

	@Override
	public String addOrUpdateModuleProper(Long moduleId, List<B2BStore> stores,
			String properData, String saveType) {
		if(saveType.equals("coupon")){
			String  failed= addOrUpdateCuntomModuleProper(moduleId,stores,properData);
			return failed;
		}
		Map<String, ModuleProperPlus> mmpMap = queryModuleProperMapByModuleId(moduleId);
		String userName = UserUtil.getUserId();
		for(B2BStore ys: stores){
			ModuleProperPlus mmp = new ModuleProperPlus();
			String storeCode = ys.getCode();
			if(mmpMap.get(storeCode)!=null){
				mmp = mmpMap.get(storeCode);
				mmp.setModuleProper(properData);
				mmp.setUpdateId(userName);
				moduleProperPlusDao.updateModuleProper(mmp);
			}else{
				mmp.setModuleId(moduleId);
				mmp.setModuleProper(properData);
				mmp.setStoreCode(storeCode);
				mmp.setCreateTime(new Date());
				mmp.setCreateId(userName);
				moduleProperPlusDao.insertModuleProper(mmp);
			}
		}
		return "";
	}
	
	private String addOrUpdateCuntomModuleProper(Long moduleId, List<B2BStore> stores, String properData){
		Map<String, ModuleProperPlus> mmpMap = queryModuleProperMapByModuleId(moduleId);
		JSONObject objData = JSONObject.parseObject(properData);
		String key = objData.getString("key");
		String userName = UserUtil.getUserId();
		for(B2BStore ys: stores){
			ModuleProperPlus mmp = new ModuleProperPlus();
			String storeCode = ys.getCode();
			if(mmpMap.get(storeCode)!=null){
				mmp = mmpMap.get(storeCode);
				String proper = mmp.getModuleProper();
				JSONObject proObj = null;
				if(StringUtils.isBlank(proper)){
					proObj = new JSONObject();
				}else{
					proObj = JSONObject.parseObject(proper);
				}
				proObj.put(key, JSON.parseObject(properData));
				mmp.setModuleProper(proObj.toJSONString());
				mmp.setUpdateId(userName);
				moduleProperPlusDao.updateModuleProper(mmp);
			}else{
				JSONObject proObj = new JSONObject();;
				proObj.put(key, JSON.parseObject(properData));
				mmp.setModuleId(moduleId);
				//mmp.setModuleProper(properData);
				mmp.setModuleProper(proObj.toJSONString());
				mmp.setStoreCode(storeCode);
				mmp.setCreateTime(new Date());
				mmp.setCreateId(userName);
				moduleProperPlusDao.insertModuleProper(mmp);
			}
		}
		return "";
	}
	
	
	@Override
	public Map<String, ModuleProperPlus> queryModuleProperMapByModuleId(
			Long moduleId) {
		List<ModuleProperPlus> mppList = moduleProperPlusDao.queryModulesByModuleId(moduleId);
		Map<String, ModuleProperPlus> mmpMap = new HashMap<String, ModuleProperPlus>();
		for(ModuleProperPlus mpp: mppList){
			mmpMap.put(mpp.getStoreCode(), mpp);
		}
		return mmpMap;
	}
	
	
	/* (non-Javadoc)
	 * @see com.feiniu.yx.page.service.ModuleProperPlusService#getTreeSelectStoreGroupByType(java.lang.String, java.lang.String, java.lang.Long)
	 */
	@Override
	public String getTreeSelectStoreGroupByType(String checkedCodes, String groupIds, Long moduleId) {
		List<B2BStoreGroup> groupList = groupDao.getB2BStoreGroupList();
		List<B2BStoreGroup> removeList  = new ArrayList<B2BStoreGroup>();
        for(B2BStoreGroup bg:groupList){
        	if(!(","+groupIds+",").contains(","+bg.getId()+",")){
        		removeList.add(bg);
        		continue;
        	}
        }
        groupList.removeAll(removeList);
		List<TreeDto> listDto= getTreeByModuleProper(groupList,checkedCodes,moduleId);
		return JSONObject.toJSONString(listDto);
	}
	
	
	/**
	 * @param groupList 覆盖区域
	 * @param checkedCodes 已选择code,暂时无效
	 * @param moduleId 组件ID
	 * @return
	 */
	private List<TreeDto> getTreeByModuleProper(List<B2BStoreGroup> groupList,
			String checkedCodes, Long moduleId) {
		List<TreeDto> treeList = new ArrayList<TreeDto>();
		Map<String,ModuleProperPlus> properMap = queryModuleProperMapByModuleId(moduleId);
		for(B2BStoreGroup m:groupList){
			String codes = m.getStoreId();
			TreeDto cTree = new TreeDto();
			cTree.setId(m.getId());
			cTree.setText(m.getName()+"["+m.getPgSeq()+"]");
			List<TreeDto> childrenList =  new ArrayList<TreeDto>();
			List<B2BStore> mendianList = storeSerivce.getStoreByCodes(codes);
			
			for(B2BStore c : mendianList){
				TreeDto childNode = new TreeDto();
				childNode.setId(c.getId());
				String plusText = "";
				if(properMap.get(c.getCode())!=null){
					ModuleProperPlus mpp = properMap.get(c.getCode());
					/*if(mpp.getModuleProper().length()>53){
						plusText = "-"+ mpp.getModuleProper().substring(0,50)+"...";
					}else{*/
						plusText = "-"+ mpp.getModuleProper();
					/*}*/
				}
				childNode.setText(c.getName()+"["+c.getCode()+"]"+plusText);
				/*if((","+checkedCodes+",").contains(","+c.getCode()+",")){
						childNode.setChecked(true);
			     }*/
				childrenList.add(childNode);
			}
			if(childrenList.size()==0) continue;
			cTree.setChildren(childrenList);
			treeList.add(cTree);
		}
		return treeList;
	}
	
	
	@Override
	public void syncModuleProperPlus(Long moduleId) {
		List<ModuleProperPlus> mppList = moduleProperPlusDao.queryModulesByModuleId(moduleId);
		List<ModuleProperPlus> onlineMppList = moduleProperPlusOnlineDao.queryModulesByModuleId(moduleId);
		Map<Long,ModuleProperPlus> onlineMap = new HashMap<Long,ModuleProperPlus>();
		for(ModuleProperPlus mpp: onlineMppList){
			onlineMap.put(mpp.getId(), mpp);
		}
		for(ModuleProperPlus mpp: mppList){
			if(onlineMap.get(mpp.getId())!=null){//更新原有数据
				ModuleProperPlus onlineMpp = onlineMap.get(mpp.getId());
				if( (!onlineMpp.getUpdateId().equals(mpp.getUpdateId())) || (!onlineMpp.getModuleProper().equals(mpp.getModuleProper())) ){
					moduleProperPlusOnlineDao.updateModuleProper(mpp);
				}
				onlineMap.remove(mpp.getId());//删除数据标记去除
			}else{//新增数据
				moduleProperPlusOnlineDao.insertModuleProper(mpp);
			}
		}
		for(ModuleProperPlus mpp: onlineMppList){//已删除数据
			if(onlineMap.get(mpp.getId()) != null){
				moduleProperPlusOnlineDao.deleteModuleProperById(mpp.getId());
			}
		}
	}
	
	
	@Override
	public void deleteModuleProper(Long moduleId, List<B2BStore> stores) {
		Map<String, ModuleProperPlus> mmpMap = queryModuleProperMapByModuleId(moduleId);
		for(B2BStore ys: stores){
			ModuleProperPlus mmp = new ModuleProperPlus();
			String storeCode = ys.getCode();
			if(mmpMap.get(storeCode)!=null){
				mmp = mmpMap.get(storeCode);
				moduleProperPlusDao.deleteModuleProperById(mmp.getId());
			}
		}
	}

}
