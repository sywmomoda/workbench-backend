package com.feiniu.yx.page.controller;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.interceptor.LogTrace;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.page.service.ModuleProperPlusService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/moduleProperPlus")
public class ModuleProperPlusController {


    @Autowired
    private ModuleProperPlusService moduleProperPlusService;

    @Autowired
    private YXStoreService storeService;

    /**
     * 编辑数据获取
     *
     * @param response
     * @param id
     * @throws Exception
     */
    @RequestMapping("getModuleProper")
    public void getModuleProper(HttpServletResponse response, @RequestParam Long id, @RequestParam Long moduleId) {
        YXStore ys = storeService.getYXStoreById(id);
        ModuleProperPlus mpp = new ModuleProperPlus();
        if (ys != null) {
            Map<String, ModuleProperPlus> mmpMap = moduleProperPlusService.queryModuleProperMapByModuleId(moduleId);
            mpp = mmpMap.get(ys.getCode());
        }
        if (mpp != null) {
            ControllerUtil.writeJson(response, JSONObject.toJSONString(mpp));
        } else {
            mpp = new ModuleProperPlus();
            ControllerUtil.writeJson(response, JSONObject.toJSONString(mpp));
        }
    }

    @RequestMapping("/queryCouponOfModuelProperById")
    public void queryCouponOfModuelProperById(HttpServletResponse response, HttpServletRequest request,
                                              @RequestParam Long moduleId) {
        Map<String, ModuleProperPlus> mmpMap = moduleProperPlusService.queryModuleProperMapByModuleId(moduleId);
        ControllerUtil.writeJson(response, JSONObject.toJSONString(mmpMap));
    }


    /**
     * 编辑模块
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("addOrUpdateModuleProper")
    @LogTrace(msgFomort = {"模块附加属性添加或更新:{properData}，门店：{storeIds}", "module", "{moduleId}"})
    public void addOrUpdateModuleProper(HttpServletRequest request, HttpServletResponse response,
                                        @RequestParam Long moduleId, @RequestParam String storeIds, @RequestParam String properData,
                                        @RequestParam(defaultValue = "0") String saveType) throws Exception {
        List<YXStore> stores = storeService.getYXStoreByIds(storeIds);
        String failedStoreName = moduleProperPlusService.addOrUpdateModuleProper(moduleId, stores, properData, saveType);
        JSONObject mjo = new JSONObject();
        mjo.put("state", "1");
        mjo.put("info", failedStoreName);
        ControllerUtil.writeJson(response, mjo.toJSONString());
    }


    @RequestMapping("deleteModuleProper")
    @LogTrace(msgFomort = {"模块附加属性删除:{storeIds}", "module", "{moduleId}"})
    public void deleteModuleProper(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam Long moduleId, @RequestParam String storeIds) throws Exception {
        List<YXStore> stores = storeService.getYXStoreByIds(storeIds);
        moduleProperPlusService.deleteModuleProper(moduleId, stores);
        JSONObject mjo = new JSONObject();
        mjo.put("state", "1");
        ControllerUtil.writeJson(response, mjo.toJSONString());
    }

    @RequestMapping("deleteModuleProperByKey")
    @LogTrace(msgFomort = {"模块附加属性删除:{storeIds}", "module", "{moduleId}"})
    public void deleteModuleProperByKey(HttpServletRequest request, HttpServletResponse response,
                                        @RequestParam Long moduleId, @RequestParam String storeIds, @RequestParam String key) throws Exception {
        List<YXStore> stores = storeService.getYXStoreByIds(storeIds);
        moduleProperPlusService.deleteModuleProperByKey(moduleId, stores, key);
        JSONObject mjo = new JSONObject();
        mjo.put("state", "1");
        ControllerUtil.writeJson(response, mjo.toJSONString());
    }

    @RequestMapping("storeSelectModuleProper")
    public void storeSelectModuleProper(HttpServletRequest request, HttpServletResponse response,
                                        String checkedCodes, String groupIds, String xiaoQuIds, Long moduleId) {
        String json = moduleProperPlusService.getTreeSelectStoreGroupByType(checkedCodes, groupIds, xiaoQuIds, moduleId);
        ControllerUtil.writeJson(response, json);
    }

    /**
     * 根据模块id和档期查询领卷中心门店券
     *
     * @param response
     * @param id
     * @param moduleId
     * @param couponCenterTabId
     */
    @RequestMapping("getCouponCenterModuleProper")
    public void getCouponCenterModuleProper(HttpServletResponse response, @RequestParam Long id, @RequestParam Long moduleId, @RequestParam(defaultValue = "0") Integer couponCenterTabId) {
        YXStore ys = storeService.getYXStoreById(id);
        ModuleProperPlus mpp = new ModuleProperPlus();
        if (ys != null) {
            Map<String, ModuleProperPlus> mmpMap = moduleProperPlusService.queryModuleProperMapByModuleIdAndTabId(moduleId, couponCenterTabId);
            mpp = mmpMap.get(ys.getCode());
        }
        if (mpp != null) {
            ControllerUtil.writeJson(response, JSONObject.toJSONString(mpp));
        } else {
            mpp = new ModuleProperPlus();
            ControllerUtil.writeJson(response, JSONObject.toJSONString(mpp));
        }
    }

    /**
     * @param request
     * @param response
     * @param moduleId
     * @param storeIds
     * @param couponCenterTabId
     */
    @RequestMapping("deleteModuleProperByTabId")
    public void deleteModuleProperByTabId(HttpServletRequest request, HttpServletResponse response,
                                          @RequestParam Long moduleId, @RequestParam String storeIds, @RequestParam Integer couponCenterTabId){
        List<YXStore> stores = storeService.getYXStoreByIds(storeIds);
        moduleProperPlusService.deleteModuleProperByTabId(moduleId, stores, couponCenterTabId);
        JSONObject mjo = new JSONObject();
        mjo.put("state", "1");
        ControllerUtil.writeJson(response, mjo.toJSONString());
    }

    /**
     * 查询领劵中心门店对应的档期优惠券
     *
     * @param request
     * @param response
     * @param checkedCodes
     * @param groupIds
     * @param xiaoQuIds
     * @param moduleId
     * @param couponCenterTabId
     */
    @RequestMapping("storeSelectModuleProperByTabId")
    public void storeSelectModuleProperByTabId(HttpServletRequest request, HttpServletResponse response,
                                               String checkedCodes, String groupIds, String xiaoQuIds, Long moduleId, Integer couponCenterTabId) {
        String json = moduleProperPlusService.getTreeSelectStoreGroupByTypeAndTabId(checkedCodes, groupIds, xiaoQuIds, moduleId, couponCenterTabId);
        ControllerUtil.writeJson(response, json);
    }

    /**
     * 删除未保存的档期优惠券
     *
     * @param request
     * @param response
     * @param moduleId
     * @param couponCenterTabIds
     */
    @RequestMapping("delCouponCenterByTabId")
    public void delCouponCenterByTabId(HttpServletRequest request, HttpServletResponse response, @RequestParam Long moduleId, @RequestParam Integer[] couponCenterTabIds) {
        moduleProperPlusService.delCouponCenterByTabId(moduleId, couponCenterTabIds);
    }
    /**
     * 删除未保存的档期优惠券
     *
     * @param request
     * @param response
     * @param moduleId
     */
    @RequestMapping("delCouponCenterByModuleId")
    public void delCouponCenterByModuleId(HttpServletRequest request, HttpServletResponse response, @RequestParam Long moduleId) {
        moduleProperPlusService.delCouponCenterByModuleId(moduleId);
    }

}
