package com.feiniu.b2b.page.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.page.service.B2BModuleDataService;
import com.feiniu.b2b.page.service.B2BModuleService;
import com.feiniu.yx.common.interceptor.LogTraceService;
import com.feiniu.yx.page.dao.ModuleDao;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.template.dao.YXModuleTypeDao;
import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.template.service.YXModuleTypeService;
import com.feiniu.yx.util.UserUtil;

@Service
public class B2BModuleServiceImpl implements B2BModuleService{
	
	@Autowired
	private ModuleDao moduleDao;
	
	@Autowired
	private YXModuleTypeDao yXModuleTypeDao;
	
	@Autowired
	private B2BPageServiceImpl pageService;
	
	@Autowired
	private YXModuleTypeService moduleTypeService;

	@Autowired
	private B2BModuleDataService moduleDataService;
	
	@Autowired
	private LogTraceService logTraceService;
	
	@Override
	public JSONObject findModule(Long id) {
		if(id == null)return null;
		Module m = moduleDao.queryModuleByID(id);
		if(m == null)return null;
		JSONObject mjo = (JSONObject) JSONObject.toJSON(m);
		YXModuleType mt = yXModuleTypeDao.getYXModuleTypeById(m.getModuleTypeId());
		JSONObject mtjo = (JSONObject) JSONObject.toJSON(mt);
		mjo.put("moduleType", mtjo);
		JSONObject proJo = JSONObject.parseObject(m.getModuleProperties());
		mjo.put("moduleProperties", proJo);
		return mjo;
	}
	
	
	public Map<String, Module> getCMSModuleMapByModuleIds(String modules) {
		String[] ids = modules.split(",");
		List<Module> moduleList = moduleDao.queryModulesByIds(ids);
		Map<String, Module> map = new HashMap<String, Module>();
		for(Module m: moduleList){
			map.put(String.valueOf(m.getId()), m);
		}
		return map;
	}
	
	/**
	 * 取所有module
	 * @author tongwenhuan
	 * 2016年4月19日
	 * @param modules
	 * @return
	 */
	public List<Module> getCMSModuleArrayByModuleIds(String modules) {
		String[] ids = modules.split(",");
		return moduleDao.queryModulesByIds(ids);
	}
	
	/**
	 * 添加模块
	 * @param auth
	 * @param param
	 * @return
	 */
	public String addModule(Map<String, Object> param) {
        Long pageId = (Long) param.get("pageId");
        Page page = pageService.queryPageByID(pageId);
        Long preModuleId = (Long)param.get("preModuleId");
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
        if(preModuleId > 0){
        	modules = (","+modules+",").replace((","+preModuleId+","), ("," + moduleId + "," + preModuleId + ","));
            modules = modules.substring(1,modules.length()-1);
        }else{
        	modules = modules  +"," + moduleId;
        }
        // 设置新增模块在页面中的位置
        page.setModules(modules);
        pageService.updateCMSPage(page);
        
        module.setYxModuleType(moduleType);
        return moduleDataService.findModuledData(module,storeCode);
	}

	
	
	/**
	 * 更新模块
	 * @author lizhiyong
	 * 2016年3月24日
	 * @param auth
	 * @param module
	 * @return
	 */
	public void updateModule(Module module) {
        String userName = UserUtil.getUserId();
        Module modulePersist = moduleDao.queryModuleByID(module.getId());
        if (modulePersist != null) {
	        if (StringUtils.isNotEmpty(module.getModuleProperties())) {
	        	if(module.getName().equals("活动锚点")){
		        	JSONObject proJo = JSONObject.parseObject(modulePersist.getModuleProperties());
		        	proJo.putAll(JSONObject.parseObject(module.getModuleProperties()));
		            modulePersist.setModuleProperties(proJo.toJSONString());
	        	}else{
		            modulePersist.setModuleProperties(module.getModuleProperties());
	        	}
	        }
	        modulePersist.setAdministrator(module.getAdministrator());
	        modulePersist.setName(module.getName());
	        modulePersist.setStoreScope(module.getStoreScope());
	        modulePersist.setUpdateId(userName);
	        modulePersist.setUpdateTime(new Date());
	        moduleDao.updateModule(modulePersist);
        }
        logTraceService.sendLogger(userName, userName, "", "更新模块Id:"+modulePersist.getId()+",属性："+modulePersist.getModuleProperties(), "page", modulePersist.getPageId());
	}
	
	
	public Long insertModule(Module m){
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
        logTraceService.sendLogger(userName, userName, "", "更新模块权限Id:"+modulePersist.getId()+",属性："+modulePersist.getModuleProperties(), "page", modulePersist.getPageId());
	}


	@Override
	public boolean validateModule(Module module) {
		boolean flag = true;
		Module modulePersist = moduleDao.queryModuleByID(module.getId());
        Long pageId = modulePersist.getPageId();
        Long moduleTypeId=modulePersist.getModuleTypeId();
        Page page = pageService.queryPageByID(pageId);
        Map<String,Object> queryModule = new HashMap<>();
        queryModule.put("moduleTypeId", moduleTypeId);
        queryModule.put("pageId", pageId);
        queryModule.put("exceptId", module.getId());
        queryModule.put("moduleIds", page.getModules().split(","));
        List<Module> list=moduleDao.getModuleList(queryModule);
        if(list!=null){
        	String storeScope=module.getStoreScope();
        	if(StringUtils.isNotBlank(storeScope)){
        		String[] scope=storeScope.split(",");
	        	for(Module m:list){
	        		String tempStore=m.getStoreScope();
	        		if(StringUtils.isNotBlank(tempStore)){
	        			String[] temps=tempStore.split(",");
	        			if(isRepeat(scope,temps)){
	        				flag=true;
	        				break;
	        			}
	        		}
	        	}
        	}
        }
		return flag;
	}
	
	public boolean isRepeat(String source[],String[] dest){
		for(String ds:dest){
			for(String sc:source){
				if(sc.equals(ds)){
					return true;
				}
			}
		}
		return false;
	}
	

}
