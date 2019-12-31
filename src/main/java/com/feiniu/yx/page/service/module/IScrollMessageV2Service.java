package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.ModuleMultiTab;
import com.feiniu.yx.page.service.CustomModule;
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
public class IScrollMessageV2Service implements CustomModule {

    @Autowired
    private ModuleMultiTabService moduleMultiTabService;
    @Override
    public void findCustomData(JSONObject mjo) throws Exception {
        String storeCode = mjo.getString("storeCode");
        if(StringUtils.isBlank(storeCode)){
            return;
        }
        String previewTime = mjo.getString("backendPreviewTime");
        JSONObject proJo  = mjo.getJSONObject("moduleProperties");
        Object tabNameObject = proJo.get("tabName");
        if(null == tabNameObject){
            return;
        }
        if(tabNameObject instanceof List){
            mjo.put("dataList",getDataOfList(proJo,storeCode,previewTime));
            return;
        }
        mjo.put("dataList",getDataOfOne(proJo,storeCode,previewTime));
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


    private List<JSONObject> getDataOfOne(JSONObject proJo,String storeCode, String previewTime){
        List<JSONObject> resList= new ArrayList<JSONObject>();
        String tabName = proJo.getString("tabName");
        String tabId = proJo.getString("tabId");
        String tabStore = proJo.getString("tabStore");
        if(!tabStore.contains(storeCode)){
            return resList;
        }
        Map<String,ModuleMultiTab> tabMap = getMapByList(tabId);
        JSONObject sin = getEachPeriod(tabId,tabMap,previewTime);
        if(null != sin){
            resList.add(sin);
        }
        return resList;
    }

    /**
     * 数组
     * @param proJo
     * @param storeCode
     * @return
     */
    private List<JSONObject> getDataOfList(JSONObject proJo,String storeCode, String previewTime){
        List<JSONObject> resList= new ArrayList<JSONObject>();
        JSONArray tabNameArray = proJo.getJSONArray("tabName");
        JSONArray tabIdArray = proJo.getJSONArray("tabId");
        JSONArray tabStoreArray = proJo.getJSONArray("tabStore");
        String ids = StringUtils.join(tabIdArray,",");
        Map<String,ModuleMultiTab> tabMap = getMapByList(ids);
        for(int i = 0; i < tabNameArray.size(); i++){
            String store = null;
            String id = null;
            try{
                store = tabStoreArray.getString(i);
                id = tabIdArray.getString(i);
            }catch (Exception e){
            }
            if(StringUtils.isBlank(store)){
                //门点不存在
                continue;
            }
            if(!store.contains(storeCode)){
                //门店不匹配
                 continue;
            }
            if(StringUtils.isBlank(id)){
                //属性表id不存在
                continue;
            }
            JSONObject sin = getEachPeriod(id,tabMap,previewTime);
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
    private JSONObject getEachPeriod(String ids, Map<String,ModuleMultiTab> tabMap, String previewTime){
      JSONObject res = null;
        String[] idArray = ids.split(",");
        Date now = new Date();
        if(StringUtils.isNotBlank(previewTime)){
            now = DateUtil.getDate(previewTime,"yyyy-MM-dd HH:mm:ss");
        }
        for(int i = 0; i < idArray.length; i++){
            ModuleMultiTab tab = tabMap.get(idArray[i]);
            if(null == tab){
                continue;
            }
            String proper="";// = tab.getModuleProper();
            JSONObject properObject = JSONObject.parseObject(proper);
            String beginTime = properObject.getString("beginTime");
            String endTime = properObject.getString("endTime");
            Date beginDate = DateUtil.getDate(beginTime);
            Date endDate = DateUtil.getDate(endTime);
            if(beginDate.before(now) && now.before(endDate)){
                res= new JSONObject();
                res.put("title",properObject.getString("contentTxt"));
                return res;
            }
        }
        return null;
    }

}
