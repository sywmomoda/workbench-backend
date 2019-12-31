package com.feiniu.yx.page.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.interceptor.LogTraceService;
import com.feiniu.yx.page.dao.ModuleDao;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.ModuleMultiTab;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.*;
import com.feiniu.yx.template.dao.YXModuleTypeDao;
import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.template.service.YXModuleTypeService;
import com.feiniu.yx.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

@Service
public class ModuleServiceImpl implements ModuleService {

    @Autowired
    private ModuleDao moduleDao;

    @Autowired
    private YXModuleTypeDao yXModuleTypeDao;

    @Autowired
    private PageService pageService;

    @Autowired
    private YXModuleTypeService moduleTypeService;

    @Autowired
    private ModuleDataService moduleDataService;

    @Autowired
    private LogTraceService logTraceService;

    @Autowired
    private ModuleProperPlusService modulePlusService;

    @Autowired
    private ModuleMultiTabService moduleMultiTabService;

    @Override
    public JSONObject findModule(Long id, String storeCode) {
        if (id == null) {
            return null;
        }
        Module m = moduleDao.queryModuleByID(id);
        if (m == null) {
            return null;
        }
        JSONObject mjo = (JSONObject) JSONObject.toJSON(m);
        YXModuleType mt = yXModuleTypeDao.getYXModuleTypeById(m.getModuleTypeId());

        JSONObject mtjo = (JSONObject) JSONObject.toJSON(mt);
        mjo.put("moduleType", mtjo);
        JSONObject proJo = JSONObject.parseObject(m.getModuleProperties());
        mjo.put("moduleProperties", proJo);
        mjo.put("storeCode", storeCode);
        if (StringUtils.isNotBlank(mt.getModuleService())) {
            moduleDataService.customDataProcess(mjo);
        }
        return mjo;
    }


    @Override
    public Map<String, Module> getCMSModuleMapByModuleIds(String modules) {
        String[] ids = modules.split(",");
        List<Module> moduleList = moduleDao.queryModulesByIds(ids);
        Map<String, Module> map = new HashMap<String, Module>();
        for (Module m : moduleList) {
            map.put(String.valueOf(m.getId()), m);
        }
        return map;
    }

    /**
     * 取所有module
     *
     * @param modules
     * @return
     * @author tongwenhuan
     * 2016年4月19日
     */
    @Override
    public List<Module> getCMSModuleArrayByModuleIds(String modules) {
        String[] ids = modules.split(",");
        return moduleDao.queryModulesByIds(ids);
    }

    /**
     * 添加模块
     *
     * @param param
     * @return
     */
    @Override
    public String addModule(Map<String, Object> param, String previewTime) {
        Long pageId = (Long) param.get("pageId");
        Page page = pageService.queryPageByID(pageId);
        Long preModuleId = (Long) param.get("preModuleId");
        Long moduleTypeId = (Long) param.get("moduleType");
        String storeCode = (String) param.get("storeCode");
        YXModuleType moduleType = moduleTypeService.getYXModuleTypeById(moduleTypeId);
        //新增一条记录
        Module module = new Module();
        module.setModuleProperties(moduleType.getModuleProperties());
        module.setName(moduleType.getName());
        module.setModuleCategory(moduleType.getModuleCategory());
        module.setModuleTypeId(moduleTypeId);
        module.setPageId(pageId);
        module.setStoreScope(page.getStoreCode());
        String userName = UserUtil.getUserId();
        module.setAdministrator(userName);
        module.setCreateId(userName);
        module.setCreateTime(new Date());
        Long moduleId = moduleDao.insertModule(module);
        module.setId(moduleId);
        //更新page中的模块ids
        String modules = page.getModules();
        if (preModuleId > 0) {
            modules = ("," + modules + ",").replace(("," + preModuleId + ","), ("," + moduleId + "," + preModuleId + ","));
            modules = modules.substring(1, modules.length() - 1);
        } else {
            modules = modules + "," + moduleId;
        }
        // 设置新增模块在页面中的位置
        page.setModules(modules);
        pageService.updateCMSPage(page);

        module.setYxModuleType(moduleType);
        return moduleDataService.findModuledData(module, storeCode, previewTime);
    }


    /**
     * 更新模块
     *
     * @param module
     * @return
     * @author lizhiyong
     * 2016年3月24日
     */
    @Override
    public void updateModule(Module module) {
        String userName = UserUtil.getUserId();
        Module modulePersist = moduleDao.queryModuleByID(module.getId());
        if (modulePersist != null) {
            if (StringUtils.isNotEmpty(module.getModuleProperties())) {
                if (module.getName().equals("活动锚点")) {
                    JSONObject proJo = JSONObject.parseObject(modulePersist.getModuleProperties());
                    proJo.putAll(JSONObject.parseObject(module.getModuleProperties()));
                    modulePersist.setModuleProperties(proJo.toJSONString());
                } else {
                    modulePersist.setModuleProperties(module.getModuleProperties());
                }
            }
            if (StringUtils.isNotBlank(module.getAdministrator())) {
                modulePersist.setAdministrator(module.getAdministrator());
            }
            if (StringUtils.isNotBlank(module.getName())) {
                modulePersist.setName(module.getName());
            }
            modulePersist.setStoreScope(module.getStoreScope());
            modulePersist.setUpdateId(userName);
            modulePersist.setUpdateTime(new Date());
            moduleDao.updateModule(modulePersist);
            moduleMultiTabUpdate(module);
            logTraceService.sendLogger(userName, userName, "", "更新模块Id:" + modulePersist.getId() + ",属性：" + modulePersist.getModuleProperties(), "page", modulePersist.getPageId());
        }
    }


    @Override
    public Long insertModule(Module m) {
        return moduleDao.insertModule(m);
    }


    @Override
    public void updateModulePermission(Module module) {
        String userName = UserUtil.getUserId();
        Module modulePersist = moduleDao.queryModuleByID(module.getId());
        if (modulePersist != null) {
            modulePersist.setAdministrator(module.getAdministrator());
            modulePersist.setUpdateId(userName);
            modulePersist.setUpdateTime(new Date());
            moduleDao.updateModule(modulePersist);
        }
        logTraceService.sendLogger(userName, userName, "", "更新模块权限Id:" + modulePersist.getId() + ",属性：" + modulePersist.getModuleProperties(), "page", modulePersist.getPageId());
    }

    /**
     * 更新多Tab moduleId
     *
     * @param module
     */
    private void moduleMultiTabUpdate(Module module) {
        JSONObject proJo = JSONObject.parseObject(module.getModuleProperties());
        String tabIdConst = "tabId";
        if (!proJo.containsKey(tabIdConst)) {
            return;
        }
        Object tabIdObject = proJo.get(tabIdConst);
        if (null == tabIdObject) {
            return;
        }
        String tabIds = "";
        if (tabIdObject instanceof List) {
            tabIds = StringUtils.join((JSONArray) tabIdObject, ",");
        } else {
            tabIds = tabIdObject.toString();
        }
        List<ModuleMultiTab> tabList = moduleMultiTabService.selectListByIds(tabIds);
        for (ModuleMultiTab tab : tabList) {
            tab.setModuleId(module.getId());
            moduleMultiTabService.update(tab);
        }
    }

    @Override
    public boolean validateModule(Module module) {
        boolean flag = true;
        Module modulePersist = moduleDao.queryModuleByID(module.getId());
        Long pageId = modulePersist.getPageId();
        Long moduleTypeId = modulePersist.getModuleTypeId();
        Page page = pageService.queryPageByID(pageId);
        Map<String, Object> queryModule = new HashMap<>();
        queryModule.put("moduleTypeId", moduleTypeId);
        queryModule.put("pageId", pageId);
        queryModule.put("exceptId", module.getId());
        queryModule.put("moduleIds", page.getModules().split(","));
        List<Module> list = moduleDao.getModuleList(queryModule);
        if (list != null) {
            String storeScope = module.getStoreScope();
            if (StringUtils.isNotBlank(storeScope)) {
                String[] scope = storeScope.split(",");
                for (Module m : list) {
                    String tempStore = m.getStoreScope();
                    if (StringUtils.isNotBlank(tempStore)) {
                        String[] temps = tempStore.split(",");
                        if (isRepeat(scope, temps)) {
                            flag = true;
                            break;
                        }
                    }
                }
            }
        }
        return flag;
    }

    public boolean isRepeat(String source[], String[] dest) {
        for (String ds : dest) {
            for (String sc : source) {
                if (sc.equals(ds)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public JSONObject findModule(String moduleId, String storeCode, String synsCode) {
        if (moduleId == null) {
            return null;
        }
        Module m = moduleDao.queryModuleByID(Long.parseLong(moduleId));
        if (m == null) {
            return null;
        }

        String storeScope = m.getStoreScope();
        if (StringUtils.isNotBlank(storeScope) && storeScope.indexOf(synsCode) == -1) {
            return null;
        }

        JSONObject mjo = (JSONObject) JSONObject.toJSON(m);

        if (storeScope.indexOf(synsCode) != -1 && storeScope.indexOf(storeCode) == -1) { //需添加要同步的storeCode
            mjo.put("add", storeCode);
        }

        if (storeScope.indexOf(synsCode) == -1 && storeScope.indexOf(storeCode) != -1) { //需删除要同步的storeCode，保证与synsCode配置一致
            mjo.put("del", storeCode);
        }

        YXModuleType mt = yXModuleTypeDao.getYXModuleTypeById(m.getModuleTypeId());

        JSONObject mtjo = (JSONObject) JSONObject.toJSON(mt);
        mjo.put("moduleType", mtjo);
        JSONObject proJo = JSONObject.parseObject(m.getModuleProperties());
        mjo.put("moduleProperties", proJo);
        mjo.put("storeCode", synsCode);
        if (StringUtils.isNotBlank(mt.getModuleService())) {
            moduleDataService.customDataProcess(mjo);
        }
        return mjo;
    }

    @Override
    public Long moduleCopy(Module module, Long pageId) {
        if (null == module) {
            return 0L;
        }
        Long oldId = module.getId();
        module.setId(null);
        module.setCreateTime(new Date());
        module.setCreateTime(new Date());
        module.setStoreScope("");
        module.setPageId(pageId);
        Long insertId = insertModule(module);
        copyModuleOtherProper(oldId, insertId);
        return insertId;

    }

    @Override
    public void updateModuleByTabId(Long moduleId, Integer couponCenterTabId) {
        Module module = moduleDao.queryModuleByID(moduleId);
        if (module != null) {
            String moduleProperties = module.getModuleProperties();
            JSONObject mpJson = JSONObject.parseObject(moduleProperties);
            if (mpJson.get("beginTimes") != null && mpJson.get("endTimes") != null) {
                List<String> beginTimes = JSONArray.parseArray(mpJson.get("beginTimes").toString(), String.class);
                List<String> endTimes = JSONArray.parseArray(mpJson.get("endTimes").toString(), String.class);
                List<String> couponCenterTabIds = JSONArray.parseArray(mpJson.get("couponCenterTabIds").toString(), String.class);
                List<String> newBeginTimes = getNewList(couponCenterTabId, beginTimes, couponCenterTabIds);
                List<String> newEndTimes = getNewList(couponCenterTabId, endTimes, couponCenterTabIds);
                List<String> newCouponCenterTabIds = new ArrayList<>();
                for (String cTabId : couponCenterTabIds) {
                    if (!couponCenterTabId.equals(Integer.valueOf(cTabId))) {
                        newCouponCenterTabIds.add(cTabId);
                    }
                }
                mpJson.put("beginTimes", newBeginTimes);
                mpJson.put("endTimes", newEndTimes);
                mpJson.put("couponCenterTabIds", newCouponCenterTabIds);
                module.setModuleProperties(mpJson.toString());
                moduleDao.updateModule(module);
                modulePlusService.delAndStorModuleProperByTabId(moduleId, couponCenterTabId);
            }
        }

    }

    private List<String> getNewList(Integer couponCenterTabId, List<String> list, List<String> couponCenterTabIds) {
        List<String> newList = new ArrayList<>();
        int index = -1;
        for (int i = 0; i < couponCenterTabIds.size(); i++) {
            if (couponCenterTabId == Integer.valueOf(couponCenterTabIds.get(i))) {
                index = i;
                break;
            }
        }
        for (int i = 0; i < list.size(); i++) {
            if (i != index) {
                newList.add(list.get(i));
            }
        }
        return newList;
    }


    private void copyModuleOtherProper(Long oldId, Long newId) {
        Map<String, ModuleProperPlus> mapPlus = modulePlusService.queryModuleProperMapByModuleId(oldId);
        if (null == mapPlus || mapPlus.size() == 0) {
            return;
        }
        try {
            for (Entry<String, ModuleProperPlus> entry : mapPlus.entrySet()) {
                ModuleProperPlus plus = entry.getValue();
                ModuleProperPlus newPlus = plus.cloneObject(plus);
                newPlus.setModuleId(null);
                newPlus.setModuleId(newId);
                newPlus.setCreateId("copysystem");
                newPlus.setUpdateId("copysystem");
                newPlus.setCreateTime(new Date());
                newPlus.setUpdateTime(new Date());
                modulePlusService.insertModuleProper(newPlus);

            }
        } catch (Exception e) {

        }
    }


}
