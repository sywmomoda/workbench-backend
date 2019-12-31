package com.feiniu.yx.page.service.module;

import java.util.*;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSONArray;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.service.ModuleService;
import com.feiniu.yx.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.ModuleProperPlusService;

@Service
public class IModuleProperPlusService implements CustomModule {

    private static final Logger logger = Logger.getLogger(IModuleProperPlusService.class);

    @Autowired
    private ModuleProperPlusService moduleProperPlusService;
    @Autowired
    private ModuleService moduleService;

    @Override
    public void findCustomData(JSONObject mjo) {
        String code = mjo.getJSONObject("moduleType").get("code").toString();
        if ("i-newCouponCenter".equals(code)) {
            findCouponCenterData(mjo);
        } else {
            Long moduleId = mjo.getLong("id");
            String storeCode = mjo.getString("storeCode");
            JSONObject moduleProperties = mjo.getJSONObject("moduleProperties");
            Map<String, ModuleProperPlus> mppMap = moduleProperPlusService.queryModuleProperMapByModuleId(moduleId);
            if (moduleProperties != null) {
                ModuleProperPlus mmp = mppMap.get(storeCode);
                if (mmp != null) {
                    JSONObject properObj = (JSONObject) JSONObject.parse(mmp.getModuleProper());
                    for (Entry<String, Object> entry : properObj.entrySet()) {
                        moduleProperties.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            mjo.put("moduleProperties", moduleProperties);
        }
    }

    private void findCouponCenterData(JSONObject mjo) {
        Long moduleId = mjo.getLong("id");
        String storeCode = mjo.getString("storeCode");
        String previewTime = mjo.getString("backendPreviewTime");
        JSONObject moduleProperties = mjo.getJSONObject("moduleProperties");
        Map<String, ModuleProperPlus> mppMap = moduleProperPlusService.queryModuleProperMapByModuleId(moduleId);
        List<Module> modules = moduleService.getCMSModuleArrayByModuleIds(moduleId.toString());
        if (modules.size() > 0) {
            Module module = modules.get(0);
            JSONObject object = JSONObject.parseObject(module.getModuleProperties());
            if (object.get("beginTimes") != null && object.get("endTimes") != null && object.get("couponCenterTabIds") != null) {
                List<String> list1 = JSONArray.parseArray(object.get("beginTimes").toString(), String.class);
                List<String> list2 = JSONArray.parseArray(object.get("endTimes").toString(), String.class);
                List<String> list3 = JSONArray.parseArray(object.get("couponCenterTabIds").toString(), String.class);
                if (list1.size() > 0) {
                    int index = -1;//档期索引
                    Map<Integer, List<Long>> listMap = stringToDate(list1, list2);
                    long time = new Date().getTime();
                    if(StringUtils.isNotBlank(previewTime)){
                        time = DateUtil.getDate(previewTime,"yyyy-MM-dd HH:mm:ss").getTime();
                    }
                    for (Entry<Integer, List<Long>> entry : listMap.entrySet()) {
                        List<Long> value = entry.getValue();
                        if (value.size() == 2) {//value存开始时间和结束时间
                            if (time > value.get(0) && time < value.get(1)) {
                                index = entry.getKey();
                                break;
                            }
                        }
                    }
                    if (moduleProperties != null && index != -1) {
                        int couponCenterTabId = Integer.parseInt(list3.get(index));
                        ModuleProperPlus mmp = mppMap.get(storeCode);
                        if (mmp != null) {
                            Map<Integer, String> map = new HashMap<>();
                            List<String> strings = JSONArray.parseArray(mmp.getModuleProper(), String.class);
                            for (String string : strings) {
                                JSONObject jsonObject = JSONObject.parseObject(string);
                                Integer centerTabId = Integer.valueOf(jsonObject.get("couponCenterTabId").toString());
                                map.put(centerTabId, string);

                            }
                            if (map.containsKey(couponCenterTabId)) {
                                JSONObject properObj = JSONObject.parseObject(map.get(couponCenterTabId));
                                for (Entry<String, Object> entry : properObj.entrySet()) {
                                    moduleProperties.put(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    }
                    mjo.put("moduleProperties", moduleProperties);
                }
            }
        }
    }


    private Map<Integer, List<Long>> stringToDate(List<String> list1, List<String> list2) {
        Map<Integer, List<Long>> map = new HashMap<>();
        List<Long> long1 = new ArrayList<>();
        List<Long> long2 = new ArrayList<>();
        for (String s : list1) {
            long time = DateUtil.getDate(s, "yyyy-MM-dd HH:mm:ss").getTime();
            long1.add(time);
        }
        for (String s : list2) {
            long time = DateUtil.getDate(s, "yyyy-MM-dd HH:mm:ss").getTime();
            long2.add(time);
        }
        for (int i = 0; i < long1.size(); i++) {
            List<Long> list = new ArrayList<>();
            list.add(long1.get(i));//开始时间
            list.add(long2.get(i));//结束时间
            map.put(i, list);
        }
        return map;
    }

}
