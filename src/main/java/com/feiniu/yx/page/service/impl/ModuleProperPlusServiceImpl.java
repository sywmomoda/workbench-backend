package com.feiniu.yx.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.UserService;
import com.feiniu.yx.page.dao.ModuleProperPlusDao;
import com.feiniu.yx.page.dao.ModuleProperPlusOnlineDao;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.page.service.ModuleProperPlusService;
import com.feiniu.yx.pool.service.YxCouponService;
import com.feiniu.yx.store.dao.YXStoreGroupDao;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.entity.YXStoreGroup;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.TreeDto;
import com.feiniu.yx.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ModuleProperPlusServiceImpl implements ModuleProperPlusService {

    @Autowired
    private ModuleProperPlusDao moduleProperPlusDao;

    @Autowired
    private ModuleProperPlusOnlineDao moduleProperPlusOnlineDao;

    @Autowired
    private YXStoreGroupDao groupDao;

    @Autowired
    private YXStoreService storeSerivce;

    @Autowired
    private UserService userService;

    @Autowired
    private YxCouponService couponService;

    @Override
    public Map<String, ModuleProperPlus> queryModuleProperMapByModuleId(
            Long moduleId) {
        List<ModuleProperPlus> mppList = moduleProperPlusDao.queryModulesByModuleId(moduleId);
        Map<String, ModuleProperPlus> mmpMap = new HashMap<String, ModuleProperPlus>();
        for (ModuleProperPlus mpp : mppList) {
            mmpMap.put(mpp.getStoreCode(), mpp);
        }
        return mmpMap;
    }

    @Override
    public ModuleProperPlus findModuleProperById(Long id) {
        ModuleProperPlus mpp = moduleProperPlusDao.queryModuleProperByID(id);
        return mpp;
    }

    @Override
    public ModuleProperPlus findModuleProperByIdAndStoreCode(ModuleProperPlus m) {
        ModuleProperPlus mpp = moduleProperPlusDao.queryModuleProperByIdAndStoreCode(m);
        return mpp;
    }


    @Override
    public String addOrUpdateModuleProper(Long moduleId, List<YXStore> stores, String properData, String saveType) {
        if (saveType.equals("coupon")) {
            String failed = addOrUpdateCuntomModuleProper(moduleId, stores, properData);
            return failed;
        }
        if (saveType.equals("couponVIP")) {
            String failed = addOrUpdateCouponVIPModuleProper(moduleId, stores, properData);
            return failed;
        }
        if (saveType.equals("couponCenter")) {
            String failed = addOrUpdateCouponCenterModuleProper(moduleId, stores, properData);
            return failed;
        }
        Map<String, ModuleProperPlus> mmpMap = queryModuleProperMapByModuleId(moduleId);
        String userName = UserUtil.getUserId();
        for (YXStore ys : stores) {
            ModuleProperPlus mmp = new ModuleProperPlus();
            String storeCode = ys.getCode();
            if (mmpMap.get(storeCode) != null) {
                mmp = mmpMap.get(storeCode);
                mmp.setModuleProper(properData);
                mmp.setUpdateId(userName);
                moduleProperPlusDao.updateModuleProper(mmp);
            } else {
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


    private String addOrUpdateCuntomModuleProper(Long moduleId, List<YXStore> stores, String properData) {
        Map<String, ModuleProperPlus> mmpMap = queryModuleProperMapByModuleId(moduleId);
        JSONObject objData = JSONObject.parseObject(properData);
        String key = objData.getString("key");
        String userName = UserUtil.getUserId();
        for (YXStore ys : stores) {
            ModuleProperPlus mmp = new ModuleProperPlus();
            String storeCode = ys.getCode();
            if (mmpMap.get(storeCode) != null) {
                mmp = mmpMap.get(storeCode);
                String proper = mmp.getModuleProper();
                JSONObject proObj = null;
                if (StringUtils.isBlank(proper)) {
                    proObj = new JSONObject();
                } else {
                    proObj = JSONObject.parseObject(proper);
                }
                proObj.put(key, JSON.parseObject(properData));
                mmp.setModuleProper(proObj.toJSONString());
                mmp.setUpdateId(userName);
                moduleProperPlusDao.updateModuleProper(mmp);
            } else {
                JSONObject proObj = new JSONObject();
                ;
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

    private String addOrUpdateCouponVIPModuleProper(Long moduleId, List<YXStore> stores, String properData) {
        Map<String, ModuleProperPlus> mmpMap = queryModuleProperMapByModuleId(moduleId);
        String userName = UserUtil.getUserId();
        String failedStoreName = "";
        for (YXStore ys : stores) {
            ModuleProperPlus mmp = new ModuleProperPlus();
            String storeCode = ys.getCode();
            JSONObject objData = JSONObject.parseObject(properData);
            String data = getCouponVIPProper(objData, storeCode);
            boolean isExist = mmpMap.get(storeCode) != null;
            if (StringUtils.isBlank(data)) {
                if (isExist) {
                    mmp = mmpMap.get(storeCode);
                    moduleProperPlusDao.deleteModuleProperById(mmp.getId());
                }
                failedStoreName += ys.getName() + ",";
                continue;
            }
            if (isExist) {
                mmp = mmpMap.get(storeCode);
                mmp.setModuleProper(data);
                mmp.setUpdateId(userName);
                moduleProperPlusDao.updateModuleProper(mmp);
            } else {
                mmp.setModuleId(moduleId);
                mmp.setModuleProper(data);
                mmp.setStoreCode(storeCode);
                mmp.setCreateTime(new Date());
                mmp.setCreateId(userName);
                moduleProperPlusDao.insertModuleProper(mmp);
            }
        }
        return failedStoreName;
    }

    private String addOrUpdateCouponCenterModuleProper(Long moduleId, List<YXStore> stores, String properData) {
        Map<String, ModuleProperPlus> mmpMap = queryModuleProperMapByModuleId(moduleId);
        String userName = UserUtil.getUserId();
        for (YXStore ys : stores) {
            ModuleProperPlus mmp = new ModuleProperPlus();
            String storeCode = ys.getCode();
            if (mmpMap.get(storeCode) != null) {
                mmp = mmpMap.get(storeCode);
                String moduleProper = mmp.getModuleProper();

                List<String> strings = JSONObject.parseArray(moduleProper, String.class);
                Map<Integer, String> map = new HashMap<>();
                Map<Integer, String> result = new HashMap<>();
                List<String> list = new ArrayList<>();
                JSONObject properDataJson = JSONObject.parseObject(properData);
                Integer tabId = Integer.valueOf(properDataJson.get("couponCenterTabId").toString());
                for (String string : strings) {
                    JSONObject object = JSONObject.parseObject(string);
                    map.put(Integer.valueOf(object.get("couponCenterTabId").toString()), string);
                }
                if (map.containsKey(tabId)) {
                    map.put(tabId, properData);
                } else {
                    map.put(tabId, properData);
                }
                List<Map.Entry<Integer, String>> entries = new ArrayList<>(map.entrySet());
                Collections.sort(entries, new Comparator<Map.Entry<Integer, String>>() {
                    public int compare(Map.Entry<Integer, String> o1, Map.Entry<Integer, String> o2) {
                        return o1.getKey().compareTo(o2.getKey());
                    }
                });
                for (Map.Entry<Integer, String> entry : entries) {
                    result.put(entry.getKey(), entry.getValue());
                }
                for (Integer id : result.keySet()) {
                    list.add(map.get(id));
                }
                mmp.setModuleProper(list.toString());
                mmp.setUpdateId(userName);
                moduleProperPlusDao.updateModuleProper(mmp);
            } else {
                mmp.setModuleId(moduleId);
                List<String> list = new ArrayList<>();
                list.add(properData);
                mmp.setModuleProper(list.toString());
                mmp.setStoreCode(storeCode);
                mmp.setCreateTime(new Date());
                mmp.setCreateId(userName);
                moduleProperPlusDao.insertModuleProper(mmp);
            }
        }
        return "";
    }

    private String getCouponVIPProper(JSONObject objData, String storeCode) {
        Object commodityIdObj = objData.get("commodityId");
        JSONArray arrayId = new JSONArray();
        JSONArray arrayCouponId = new JSONArray();
        JSONArray arrayName = new JSONArray();
        if (commodityIdObj instanceof List) {
            arrayId = (JSONArray) commodityIdObj;
            arrayCouponId = objData.getJSONArray("couponId");
            arrayName = objData.getJSONArray("commodityName");
        } else {
            arrayId.add(commodityIdObj.toString());
            arrayCouponId.add(objData.getString("couponId"));
            arrayName.add(objData.getString("commodityName"));
        }
        JSONArray newId = new JSONArray();
        newId.addAll(arrayId);
        JSONArray newCouponId = new JSONArray();
        newCouponId.addAll(arrayCouponId);
        JSONArray newName = new JSONArray();
        newName.addAll(arrayName);
        for (int i = 0, len = newId.size(); i < len; i++) {
            String commodityId = newId.getString(i);
            String couponId = newCouponId.getString(i);
            String name = newName.getString(i);
            String couponIds = couponService.getCouponIdsByCommodityId(new String[]{commodityId}, storeCode);
            if (StringUtils.isBlank(couponIds) || !couponIds.contains(couponId)) {
                arrayCouponId.remove(couponId);
                arrayId.remove(commodityId);
                arrayName.remove(name);
            }
        }
        if (arrayId.size() == 1) {
            objData.put("couponId", arrayCouponId.getString(0));
            objData.put("commodityId", arrayId.getString(0));
            objData.put("commodityName", arrayName.getString(0));
        }

        newId = null;
        newCouponId = null;
        newName = null;
        if (arrayId.size() == 0) {
            return null;
        }
        return objData.toJSONString();

    }


    public void insertModuleProper(ModuleProperPlus module) {
        String userName = UserUtil.getUserId();
        module.setCreateTime(new Date());
        module.setCreateId(userName);
        moduleProperPlusDao.insertModuleProper(module);
    }


    @Override
    public void deleteModuleProper(Long moduleId, List<YXStore> stores) {
        Map<String, ModuleProperPlus> mmpMap = queryModuleProperMapByModuleId(moduleId);
        for (YXStore ys : stores) {
            ModuleProperPlus mmp = new ModuleProperPlus();
            String storeCode = ys.getCode();
            if (mmpMap.get(storeCode) != null) {
                mmp = mmpMap.get(storeCode);
                moduleProperPlusDao.deleteModuleProperById(mmp.getId());
            }
        }
    }

    @Override
    public void deleteModuleProperByKey(Long moduleId, List<YXStore> stores, String key) {
        Map<String, ModuleProperPlus> mmpMap = queryModuleProperMapByModuleId(moduleId);
        for (YXStore ys : stores) {
            ModuleProperPlus mmp = new ModuleProperPlus();
            String storeCode = ys.getCode();
            if (mmpMap.get(storeCode) != null) {
                mmp = mmpMap.get(storeCode);
                String moduleProper = mmp.getModuleProper();
                if (StringUtils.isBlank(moduleProper)) {
                    continue;
                }
                JSONObject pro = JSONObject.parseObject(moduleProper);
                pro.remove(key);
                if (pro.size() == 0) {
                    moduleProperPlusDao.deleteModuleProperById(mmp.getId());
                } else {
                    mmp.setModuleProper(pro.toJSONString());
                    moduleProperPlusDao.updateModuleProper(mmp);
                }

            }
        }
    }

    @Override
    public void deleteModuleProperById(Long id) {
        moduleProperPlusDao.deleteModuleProperById(id);
    }


    /* (non-Javadoc)
     * @see com.feiniu.yx.page.service.ModuleProperPlusService#getTreeSelectStoreGroupByType(java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public String getTreeSelectStoreGroupByType(String checkedCodes, String groupIds, String xiaoQuIds, Long moduleId) {
        List<YXStoreGroup> groupList = groupDao.getYXStoreGroupList();
        List<YXStoreGroup> removeList = new ArrayList<YXStoreGroup>();
        for (YXStoreGroup bg : groupList) {
            if (!("," + groupIds + ",").contains("," + bg.getId() + ",")
                    && !("," + xiaoQuIds + ",").contains("," + bg.getId() + ",")
                    && !("," + groupIds + ",").contains("," + bg.getPid() + ",")) {
                removeList.add(bg);
                continue;
            }
        }
        groupList.removeAll(removeList);
        List<TreeDto> listDto = getTreeByModuleProper(groupList, checkedCodes, moduleId);
        return JSONObject.toJSONString(listDto);
    }


    /**
     * @param groupList    覆盖区域
     * @param checkedCodes 已选择code,暂时无效
     * @param moduleId     组件ID
     * @return
     */
    private List<TreeDto> getTreeByModuleProper(List<YXStoreGroup> groupList,
                                                String checkedCodes, Long moduleId) {
        List<TreeDto> treeList = new ArrayList<TreeDto>();
        Map<String, ModuleProperPlus> properMap = queryModuleProperMapByModuleId(moduleId);
        getTreeList(groupList, treeList, properMap);
        return treeList;
    }

    /**
     * @param groupList         覆盖区域
     * @param checkedCodes      已选择code,暂时无效
     * @param moduleId          组件ID
     * @param couponCenterTabId 档期id
     * @return
     */
    private List<TreeDto> getTreeByModuleProperAndTabId(List<YXStoreGroup> groupList,
                                                        String checkedCodes, Long moduleId, Integer couponCenterTabId) {
        List<TreeDto> treeList = new ArrayList<TreeDto>();
        Map<String, ModuleProperPlus> properMap = queryModuleProperMapByModuleIdAndTabId(moduleId, couponCenterTabId);
        getTreeList(groupList, treeList, properMap);
        return treeList;
    }

    private void getTreeList(List<YXStoreGroup> groupList, List<TreeDto> treeList, Map<String, ModuleProperPlus> properMap) {
        Map<String, Set<String>> userStoreGroupMap = userService.getMapUserStores();
        Set<String> userStores = userStoreGroupMap.get("codeSet");
        Set<String> userGroups = userStoreGroupMap.get("pgSeqSet");
        Map<String, List<TreeDto>> childMap = new HashMap<String, List<TreeDto>>();
        for (YXStoreGroup m : groupList) {

            if (!userGroups.contains(m.getPgSeq())) {
                continue;
            }
			/*if(StringUtils.isBlank(ids)){
				continue;
			}*/
            TreeDto cTree = new TreeDto();
            cTree.setId(m.getId());
            cTree.setText(m.getName() + "[" + m.getPgSeq() + "]");
            List<TreeDto> childrenList = childMap.get(m.getPid() + "");
            if (childrenList == null) {
                childrenList = new ArrayList<TreeDto>();
            }

            if (m.getLevel() == 3) {
                List<TreeDto> childList = new ArrayList<TreeDto>();
                String codes = m.getStoreId();
                List<YXStore> mendianList = storeSerivce.getStoreByCodes(codes);
                for (YXStore c : mendianList) {
                    if (!userStores.contains(c.getCode())) {
                        continue;
                    }
                    TreeDto childNode = new TreeDto();
                    childNode.setId(c.getId());
                    String plusText = "";
                    if (properMap.get(c.getCode()) != null) {
                        ModuleProperPlus mpp = properMap.get(c.getCode());
					/*if(mpp.getModuleProper().length()>53){
						plusText = "-"+ mpp.getModuleProper().substring(0,50)+"...";
					}else{*/
                        plusText = "-" + mpp.getModuleProper();
                        /*}*/
                    }
                    childNode.setText(c.getName() + "[" + c.getCode() + "]" + plusText);
				/*if((","+checkedCodes+",").contains(","+c.getCode()+",")){
						childNode.setChecked(true);
			     }*/
                    childList.add(childNode);
                }

                if (childMap.get(m.getId() + "") == null) {
                    childMap.put(m.getId() + "", childList);
                }
            }

			/*if((","+checkedCodes+",").contains(","+m.getStoreId()+",")){
				cTree.setChecked(true);
			}*/
            if (childMap.get(m.getId() + "") == null) {
                List<TreeDto> list = new ArrayList<>();
                childMap.put(m.getId() + "", list);
            }

            if (m.getLevel() <= 1) {
                cTree.setChildren(childMap.get(m.getId() + ""));
                treeList.add(cTree);
            } else {
                cTree.setChildren(childMap.get(m.getId() + ""));
                childrenList.add(cTree);
                childMap.put(m.getPid() + "", childrenList);
            }

			/*if(childrenList.size()==0) continue;
			cTree.setChildren(childrenList);
			treeList.add(cTree);*/
        }

        for (int i = treeList.size() - 1; i >= 0; i--) {//去掉无门店的节点数据
            TreeDto cTree = treeList.get(i);
            if (cTree.getChildren() == null) {
                treeList.remove(cTree);
                continue;
            }
            for (int j = cTree.getChildren().size() - 1; j >= 0; j--) {
                TreeDto cTreeChild1 = cTree.getChildren().get(j);
                if (cTreeChild1.getChildren() == null) {
                    cTree.getChildren().remove(cTreeChild1);
                    continue;
                }
                for (int k = cTreeChild1.getChildren().size() - 1; k >= 0; k--) {
                    TreeDto cTreeChild2 = cTreeChild1.getChildren().get(k);
                    if (cTreeChild2.getChildren() == null) {
                        cTreeChild1.getChildren().remove(cTreeChild2);
                        continue;
                    }
                    if (cTreeChild2.getChildren().size() == 0) {
                        cTreeChild1.getChildren().remove(cTreeChild2);
                    }
                }
                if (cTreeChild1.getChildren().size() == 0) {
                    cTree.getChildren().remove(cTreeChild1);
                }
            }
            if (cTree.getChildren().size() == 0) {
                treeList.remove(cTree);
            }
        }
    }

    @Override
    public void syncModuleProperPlus(Long moduleId) {
        List<ModuleProperPlus> mppList = moduleProperPlusDao.queryModulesByModuleId(moduleId);
        List<ModuleProperPlus> onlineMppList = moduleProperPlusOnlineDao.queryModulesByModuleId(moduleId);
        Map<Long, ModuleProperPlus> onlineMap = new HashMap<Long, ModuleProperPlus>();
        for (ModuleProperPlus mpp : onlineMppList) {
            onlineMap.put(mpp.getId(), mpp);
        }
        for (ModuleProperPlus mpp : mppList) {
            if (onlineMap.get(mpp.getId()) != null) {//更新原有数据
                ModuleProperPlus onlineMpp = onlineMap.get(mpp.getId());
                if ((!onlineMpp.getUpdateId().equals(mpp.getUpdateId())) || (!onlineMpp.getModuleProper().equals(mpp.getModuleProper()))) {
                    moduleProperPlusOnlineDao.updateModuleProper(mpp);
                }
                onlineMap.remove(mpp.getId());//删除数据标记去除
            } else {//新增数据
                moduleProperPlusOnlineDao.insertModuleProper(mpp);
            }
        }
        for (ModuleProperPlus mpp : onlineMppList) {//已删除数据
            if (onlineMap.get(mpp.getId()) != null) {
                moduleProperPlusOnlineDao.deleteModuleProperById(mpp.getId());
            }
        }
    }

    @Override
    public void updateModuleProper(ModuleProperPlus one) {
        moduleProperPlusDao.updateModuleProper(one);

    }

    @Override
    public Map<String, ModuleProperPlus> queryModuleProperMapByModuleIdAndTabId(Long moduleId, Integer couponCenterTabId) {
        List<ModuleProperPlus> mppList = moduleProperPlusDao.queryModulesByModuleId(moduleId);
        Map<String, ModuleProperPlus> mmpMap = new HashMap<String, ModuleProperPlus>();
        if (mppList.size() > 0) {
            for (ModuleProperPlus moduleProperPlus : mppList) {
                String moduleProper = moduleProperPlus.getModuleProper();
                if (moduleProper.contains("couponCenterTabId")) {//组件附加属性包含"couponCenterTabId"字符串
                    List<String> list = JSONObject.parseArray(moduleProper, String.class);
                    for (String str : list) {
                        JSONObject properDataJson = JSONObject.parseObject(str);
                        if (properDataJson.get("couponCenterTabId") != null) {
                            if (couponCenterTabId.equals(Integer.valueOf(properDataJson.get("couponCenterTabId").toString()))) {
                                moduleProperPlus.setModuleProper(str);
                                mmpMap.put(moduleProperPlus.getStoreCode(), moduleProperPlus);
                            }
                        }
                    }
                }
            }
        }
        return mmpMap;
    }

    @Override
    public String getTreeSelectStoreGroupByTypeAndTabId(String checkedCodes, String groupIds, String xiaoQuIds, Long moduleId, Integer couponCenterTabId) {
        List<YXStoreGroup> groupList = groupDao.getYXStoreGroupList();
        List<YXStoreGroup> removeList = new ArrayList<YXStoreGroup>();
        for (YXStoreGroup bg : groupList) {
            if (!("," + groupIds + ",").contains("," + bg.getId() + ",")
                    && !("," + xiaoQuIds + ",").contains("," + bg.getId() + ",")
                    && !("," + groupIds + ",").contains("," + bg.getPid() + ",")) {
                removeList.add(bg);
                continue;
            }
        }
        groupList.removeAll(removeList);
        List<TreeDto> listDto = getTreeByModuleProperAndTabId(groupList, checkedCodes, moduleId, couponCenterTabId);
        return JSONObject.toJSONString(listDto);
    }

    @Override
    public void deleteModuleProperByTabId(Long moduleId, List<YXStore> stores, Integer couponCenterTabId) {
        Map<String, ModuleProperPlus> mmpMap = queryModuleProperMapByModuleId(moduleId);
        Map<String, ModuleProperPlus> map = new HashMap<>();
        for (YXStore ys : stores) {
            String storeCode = ys.getCode();
            if (mmpMap.containsKey(storeCode)) {
                map.put(storeCode, mmpMap.get(storeCode));
            }
        }
        List<ModuleProperPlus> list = new ArrayList<>();
        for (String storeCode : map.keySet()) {
            list.add(map.get(storeCode));
        }
        for (ModuleProperPlus moduleProperPlus : list) {
            List<String> listStr = new ArrayList<>();
            List<String> strings = JSONObject.parseArray(moduleProperPlus.getModuleProper(), String.class);
            for (String string : strings) {
                JSONObject properDataJson = JSONObject.parseObject(string);
                Integer centerTabId = Integer.valueOf(properDataJson.get("couponCenterTabId").toString());
                if (!centerTabId.equals(couponCenterTabId)) {
                    listStr.add(string);
                }
            }
            if (listStr.size() > 0) {
                moduleProperPlus.setModuleProper(listStr.toString());
                moduleProperPlusDao.updateModuleProper(moduleProperPlus);
            } else {
                moduleProperPlusDao.deleteModuleProperById(moduleProperPlus.getId());
            }
        }
    }

    @Override
    public void delAndStorModuleProperByTabId(Long moduleId, Integer couponCenterTabId) {
        delModuleProperByTabId(moduleId, couponCenterTabId);
    }

    @Override
    public void delCouponCenterByTabId(Long moduleId, Integer[] couponCenterTabIds) {
        List<ModuleProperPlus> moduleProperPluses = moduleProperPlusDao.queryModulesByModuleId(moduleId);
        Set<Integer> hashSet = new HashSet<>();
        List<Integer> resultList = new ArrayList<>(Arrays.asList(couponCenterTabIds));
        List<Integer> list = new ArrayList<>();
        for (ModuleProperPlus moduleProperPlus : moduleProperPluses) {
            String moduleProper = moduleProperPlus.getModuleProper();
            List<String> modulePropers = JSONObject.parseArray(moduleProper, String.class);
            for (String proper : modulePropers) {
                JSONObject properDataJson = JSONObject.parseObject(proper);
                Integer centerTabId = Integer.valueOf(properDataJson.get("couponCenterTabId").toString());
                hashSet.add(centerTabId);
            }
        }
        for (Integer integer : hashSet) {
            if (!resultList.contains(integer)) {
                list.add(integer);
            }
        }
        for (Integer couponCenterTabId : list) {
            delModuleProperByTabId(moduleId, couponCenterTabId);
        }
    }

    @Override
    public void delCouponCenterByModuleId(Long moduleId) {
        moduleProperPlusDao.deleteModuleProperByModuleId(moduleId);
    }

    private void delModuleProperByTabId(Long moduleId, Integer couponCenterTabId) {
        List<ModuleProperPlus> moduleProperPluses = moduleProperPlusDao.queryModulesByModuleId(moduleId);
        for (ModuleProperPlus moduleProperPlus : moduleProperPluses) {
            String moduleProper = moduleProperPlus.getModuleProper();
            List<String> list = new ArrayList<>();
            List<String> strings = JSONObject.parseArray(moduleProper, String.class);
            for (String string : strings) {
                JSONObject properDataJson = JSONObject.parseObject(string);
                if (properDataJson.get("couponCenterTabId") != null && (!Integer.valueOf(properDataJson.get("couponCenterTabId").toString()).equals(couponCenterTabId))) {
                    list.add(string);
                }
            }
            if (list.size() > 0) {
                moduleProperPlus.setModuleProper(list.toString());
                moduleProperPlusDao.updateModuleProper(moduleProperPlus);
            } else {
                moduleProperPlusDao.deleteModuleProperById(moduleProperPlus.getId());
            }
        }
    }


}
