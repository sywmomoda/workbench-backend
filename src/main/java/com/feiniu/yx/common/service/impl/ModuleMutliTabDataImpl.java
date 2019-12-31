package com.feiniu.yx.common.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.ModuleMutliTabData;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.ModuleMultiTab;
import com.feiniu.yx.page.service.ModuleMultiTabService;
import com.feiniu.yx.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author yehui
 */
@Service
public class ModuleMutliTabDataImpl implements ModuleMutliTabData {

    @Autowired
    private ModuleMultiTabService moduleMultiTabService;

    @Override
    public List<JSONObject> getModuleData(Module m , String storeCode, String previewTime)  {
        List<JSONObject> dataList = new ArrayList<JSONObject>();
        if(StringUtils.isBlank(storeCode)){
            return dataList;
        }
        JSONObject proJo  = JSONObject.parseObject(m.getModuleProperties());
        Object tabIdObject = proJo.get("tabId");
        if(null == tabIdObject){
            return dataList;
        }
        if(tabIdObject instanceof List){
            dataList = getDataOfList((JSONArray) tabIdObject,storeCode,previewTime);
        }else {
            dataList = getDataOfOne(tabIdObject.toString(),storeCode,previewTime);
        }
        return dataList;
    }


    /**
     * 查询 module 数据
     * @param ids
     * @return
     */
    private Map<String,ModuleMultiTab> getMapByList(String ids){
        List<ModuleMultiTab> tabDataList = moduleMultiTabService.selectListByIds(ids);
        Map<String,ModuleMultiTab> resMap = new HashMap<String,ModuleMultiTab>(16);
        if(null == tabDataList ||  tabDataList.size() == 0){
            return resMap;
        }
        for(int i = 0; i < tabDataList.size(); i++){
            ModuleMultiTab tab = tabDataList.get(i);
            if(null == tab ){
                continue;
            }
            Long id = tab.getId();
            resMap.put(String.valueOf(id),tab);
        }
        return resMap;
    }


    /**
     * 单个tab
     * @param tabId
     * @param storeCode
     * @return
     */
    private List<JSONObject> getDataOfOne(String tabId,String storeCode,String previewTime){
        List<JSONObject> resList= new ArrayList<JSONObject>();
        if(StringUtils.isBlank(tabId)){
            return resList;
        }
        Map<String,ModuleMultiTab> tabMap = getMapByList(tabId);
        JSONObject sin = getEachPeriod(tabId,tabMap,storeCode, previewTime);
        if(null != sin){
            resList.add(sin);
        }
        return resList;
    }

    /**
     * 多个tab
     * @param tabIdArray
     * @param storeCode
     * @return
     */
    private List<JSONObject> getDataOfList(JSONArray tabIdArray,String storeCode,String previewTime){
        List<JSONObject> resList= new ArrayList<JSONObject>();
        String ids = StringUtils.join(tabIdArray,",");
        Map<String,ModuleMultiTab> tabMap = getMapByList(ids);
        for(int i = 0; i < tabIdArray.size(); i++){
            String id = null;
            try{
                id = tabIdArray.getString(i);
            }catch (Exception e){
            }
            if(StringUtils.isBlank(id)){
                //属性表id不存在
                continue;
            }
            JSONObject sin = getEachPeriod(id,tabMap,storeCode,previewTime);
            if(null == sin){
                continue;
            }
            resList.add(sin);
        }
        return resList;
    }

    /**
     * 多个档期
     * @param ids
     * @return
     */
    private JSONObject getEachPeriod(String ids, Map<String,ModuleMultiTab> tabMap,String storeCode,String previewTime){
      JSONObject res = null;
        String[] idArray = ids.split(",");
        for(int i = 0; i < idArray.length; i++){
            ModuleMultiTab tab = tabMap.get(idArray[i]);
            if(null == tab){
                continue;
            }
            String store = tab.getStoreCode();
            if(StringUtils.isBlank(store)){
                //门店不存在
                continue;
            }
            if(!store.contains(storeCode)){
                //门店不匹配
                continue;
            }
            String beginTime = tab.getBeginTime();
            String endTime = tab.getEndTime();
            if(StringUtils.isBlank(beginTime) && StringUtils.isBlank(endTime)){
                res = convertJsonObject(tab);
                return res;
            }
            Date beginDate = DateUtil.getDate(beginTime);
            Date endDate = DateUtil.getDate(endTime);
            Date now = new Date();
            if(StringUtils.isNotBlank(previewTime)){
                now = DateUtil.getDate(previewTime,"yyyy-MM-dd HH:mm:ss");
            }

            if(beginDate.before(now) && now.before(endDate)){
                res=convertJsonObject(tab);
                return res;
            }
        }
        return null;
    }

    private JSONObject convertJsonObject(ModuleMultiTab tab){
        JSONObject  res= new JSONObject();
        res.put("content",tab.getContent());
        res.put("content1",tab.getContent1());
        res.put("linkType",tab.getLinkType());
        res.put("linkData",tab.getLinkData());
        res.put("storeCode",tab.getStoreCode());
        return res;
    }

}
