package com.feiniu.b2b.page.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.common.B2BConstant;
import com.feiniu.b2b.page.service.B2BModuleDataService;
import com.feiniu.b2b.page.service.B2BPageService;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.entity.B2BStoreGroup;
import com.feiniu.b2b.store.service.B2BStoreGroupService;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.common.YXConstant;
import com.feiniu.yx.page.dao.PageDao;
import com.feiniu.yx.page.dao.PageOnlineDao;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.ModuleService;
import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXModuleTypeService;
import com.feiniu.yx.template.service.YXTemplateService;
import com.feiniu.yx.util.ArtTemplateUtil;
import com.feiniu.yx.util.TplUtil;
import com.feiniu.yx.util.UserUtil;

@Service
public class B2BPageServiceImpl implements B2BPageService{
	
	private static Logger logger = Logger.getLogger(B2BPageService.class);
	
	@Autowired
	private PageDao pageDao;

	@Autowired
	private PageOnlineDao pageOnlineDao;
	
	@Autowired
	private ModuleService moduleService;
	@Autowired
	private YXModuleTypeService moduleTypeService;
	@Autowired
	private B2BModuleDataService moduleDataService;
	@Autowired
	private YXTemplateService templateService;
	@Autowired
	private B2BStoreService storeService;
	@Autowired
	private B2BStoreGroupService storeGroupService;

	/** 根据ID查询页面
	 * @param pageId
	 * @return
	 */
	public Page queryPageByID(Long pageId) {
		Page page = pageDao.queryCMSPageByID(pageId);
		page.setStoreCode(storeGroupService.getStoreCodesByGroupIds(page.getStoreGroupIds()));
        return page;
	}

	/** 获取页面html
	 * @param auth
	 * @param paramMap
	 * @return
	 */
	public Map<String, String> getPageHtml(Map<String, Object> paramMap) {
		Map<String, String> pageHtmlMap = new HashMap<String, String>();
        Long pageId = (Long) paramMap.get("pageId");
        String storeCode = (String) paramMap.get("store");
        Page page = pageDao.queryCMSPageByID(pageId);
        YXTemplate template = templateService.getYXTemplateById(page.getTemplateId());
        String templateUrl = template.getTemplateUrl();
        //页头模板
        String pageHeadTpl = TplUtil.getTemplateHeadPath(templateUrl);
        //页尾模板
        String pageFootTpl = TplUtil.getTemplateFootPath(templateUrl);
        try{
        	page.setPagePropertieMap(JSONObject.parseObject(page.getPageProperties()));
        }catch(Exception e){
        }
        JSONObject pjo = (JSONObject) JSON.toJSON(page);
        pjo.put("staticHost", YXConstant.STATICHOST);
        pjo.put("staticHost_elf", B2BConstant.STATICHOST);
        String hfdata = pjo.toJSONString();
        pageHtmlMap.put("pageHead", ArtTemplateUtil.exeTemplateByPath(pageHeadTpl, hfdata));
        pageHtmlMap.put("pageContent", getModulesContent(page,templateUrl,storeCode));
        pageHtmlMap.put("pagefoot", ArtTemplateUtil.exeTemplateByPath(pageFootTpl, hfdata));
        return pageHtmlMap;
	}
	
	/**
	 * 获取模块html
	 * @author tongwenhuan
	 * 2017年3月10日
	 * @param page
	 * @return
	 */
	private String getModulesContent(Page page, String templateUrl ,String storeCode){
		//模块集合
		Map<String, Module> moduleMap = moduleService.getCMSModuleMapByModuleIds(page.getModules());
		StringBuilder moduleTypeIds = new StringBuilder();
		for (Entry<String, Module> module : moduleMap.entrySet()) {
			if (moduleTypeIds.length() > 0) {
				moduleTypeIds.append(",");
			}
			moduleTypeIds.append(module.getValue().getModuleTypeId().toString());
		}
		//模块类型集合
		Map<Long, YXModuleType> moduleTypeMap = moduleTypeService.getModuleTypeMapByIds(moduleTypeIds.toString());
		StringBuilder modulehtmls = new StringBuilder("");
		for (String moduleIdStr : page.getModules().split(",")) {
			Module module = moduleMap.get(moduleIdStr);
			if(module.getStoreScope().indexOf(storeCode)!=-1){
				YXModuleType moduleType = moduleTypeMap.get(module.getModuleTypeId());
				String moduleTpl = TplUtil.getModulePath(templateUrl, moduleType.getCode());
				String data = moduleDataService.findModuledData(module, storeCode);
				modulehtmls.append(ArtTemplateUtil.exeTemplateByPath(moduleTpl, data));
			}
		}
		return modulehtmls.toString();
	}


	/** 保存活动，更新组件列表
	 * @param auth
	 * @param params
	 * @return
	 */
	public String savePageModules(Page page) {
        Page pagePersis = this.queryPageByID(page.getId());
        pagePersis.setUpdateId("");
        pagePersis.setUpdateTime(new Date());
        // 更新模块在页面中的位置
        pagePersis.setModules(page.getModules());
        String userName = UserUtil.getUserId();
        pagePersis.setUpdateId(userName);
        pagePersis.setPageProperties(page.getPageProperties());
        pageDao.updateCMSPage(pagePersis);
        
        return JSONObject.toJSONString(pagePersis);
	}
	
	 /**
     * 创建活动页service
     * 
     * @param auth
     * @param params
     * @return
     */
    public void createPage(Page cmsPage) {
    	String userName = UserUtil.getUserId();
        cmsPage.setCreateId(userName);
        cmsPage.setCreateTime(new Date());
        cmsPage.setUpdateId(userName);
        cmsPage.setUpdateTime(new Date());
        cmsPage.setPageProperties("{\"background\":\"#1b53a6\"}");
        cmsPage.setModules("");
        cmsPage.setMainPicUrl("");
        cmsPage.setStatus(2);
        cmsPage.setStoreCode(storeGroupService.getStoreCodesByGroupIds(cmsPage.getStoreGroupIds()));
        
        String admin = cmsPage.getAdministrator();
        if (admin == null) {
        	admin = "";
        }
        if (admin.indexOf(userName) < 0) {
        	admin += "," + userName;
        }
        cmsPage.setAdministrator(admin);
        Long pageId = pageDao.insertCMSPage(cmsPage);
        Long templateId = cmsPage.getTemplateId();
        YXTemplate cmsTemplate = null;
        StringBuilder moduleIds = new StringBuilder();
        cmsTemplate = templateService.getYXTemplateById(templateId);
        String moduleTypes = cmsTemplate.getModuleTypes();
        Map<Long, YXModuleType> moduleTypeMap = moduleTypeService.getModuleTypeMapByIds(moduleTypes);
        Long [] moduleTypeArr = new Long[moduleTypeMap.size()];
        moduleTypeMap.keySet().toArray(moduleTypeArr);
    	for (Long moduleTypeStr : moduleTypeArr) {
    		YXModuleType type = moduleTypeMap.get(moduleTypeStr);
    		// 统一模板默认只创建头部通栏，其他组件再手工添加。
    		if(type.getCode().equals("m-pic-banner-noboder")||type.getCode().equals("i-picSlider")||type.getCode().equals("m-float-icon")){
                Module module = createModule(moduleTypeMap.get(moduleTypeStr), pageId);
                module.setStoreScope(cmsPage.getStoreCode());
                module.setCreateId(userName);
                module.setAdministrator(userName);
                Long moduleId = moduleService.insertModule(module);
                if (moduleIds.length() > 0) {
                    moduleIds.append(",");
                }
                moduleIds.append(moduleId);
                break;
            }
    	}

        cmsPage.setId(pageId);
        cmsPage.setModules(moduleIds.toString());
        cmsPage.setPageProperties(cmsTemplate.getPageProperties());
        pageDao.updateCMSPage(cmsPage);

    }
    
    /**
     * @Title: 根据模块类型和页面ID创建一个模块对象
     * @param moduleType
     * @param pageId
     * @return CMSModule
     */
    private Module createModule(YXModuleType moduleType, Long pageId) {
        Module module = new Module();
        module.setModuleProperties(moduleType.getModuleProperties());
        module.setModuleTypeId(moduleType.getId());
        module.setName(moduleType.getName());
        module.setCreateTime(new Date());
        module.setUpdateTime(new Date());
        module.setModuleCategory(moduleType.getModuleCategory());
        module.setPageId(pageId);
        return module;
	}
    
    public int updateCMSPageInfo(Page cmsPage) {
    	Page page = pageDao.queryCMSPageByID(cmsPage.getId());
    	page.setName(cmsPage.getName());
    	page.setActivityBeginTime(cmsPage.getActivityBeginTime());
    	page.setActivityEndTime(cmsPage.getActivityEndTime());
    	page.setAdministrator(cmsPage.getAdministrator());
    	page.setDescription(cmsPage.getDescription());
    	page.setUrl(cmsPage.getUrl());
    	page.setMainPicUrl(cmsPage.getMainPicUrl());
    	String userName = UserUtil.getUserId();
    	page.setUpdateId(userName);
    	page.setUpdateTime(new Date());
		return pageDao.updateCMSPage(page);
	}
	    
    /**
     * 获取页面的区域和类目
     *@author yehui
     *Sep 7, 2016
     *@param list
     */
    private void getPageStoreName(List<Page> list){
    	Map<String, B2BStoreGroup> allGroup = storeGroupService.getFmap();
    	Map<String, B2BStore> storeMap = storeService.getB2BStoreMap();
        if(list!=null && list.size()>0){
	        for (Page p : list) {
	        	
	            List<B2BStoreGroup> groupList = new ArrayList<B2BStoreGroup>();
	            List<B2BStore> stores = new ArrayList<B2BStore>();
	        	String storeCodeS = "";
	        	String groupIds[] = p.getStoreGroupIds().split(",");
	        	StringBuilder storeGroupNames = new StringBuilder();
	        	int j = 0;
	        	for(String id : groupIds){
	        		if(allGroup.get(id)==null) continue;
	        		storeCodeS += allGroup.get(id).getStoreId()+",";
	        		groupList.add(allGroup.get(id));
	        		
	        		if(j > 0) {
	            		storeGroupNames.append(",");
	            	}
	            	storeGroupNames.append(allGroup.get(id).getName());
	            	j++;
	        	}
	        	
	        	String[] codes = storeCodeS.split(",");
				for(String code : codes) {
					B2BStore s = storeMap.get(code);
					if(s == null)continue;
					stores.add(s);
				}
	            
	            p.setStoreGroupNames(storeGroupNames.toString());
	            
	            StringBuilder storeNames = new StringBuilder();
	            StringBuilder storeCodes = new StringBuilder();
	            int i = 0;
	            for (B2BStore store : stores) {
	            	if(i > 0) {
	            		storeNames.append(",");
	            		storeCodes.append(",");
	            	}
	            	storeNames.append(store.getName());
	            	storeCodes.append(store.getCode());
	            	i++;
	            }
	            p.setStoreNames(storeNames.toString());
	            p.setStoreCode(storeCodes.toString());
	        }
        }
    }
	    
	@Override
	public List<Page> queryActivityList(Page page, int i) {
		if(i==0){
			List<Page> list = pageDao.queryActivityList(page,false);
			getPageStoreName(list);
		    return list;
		}else{
			List<Page> list = pageDao.queryActivityList(page,true);
			getPageStoreName(list);
		    return list;
		}
	}
		
	public List<Page> queryPageForLinkSelect(Page page){
		 List<Page> list = pageDao.queryActivityList(page,false);
		 getPageStoreName(list);
	     return list;
	}

	@Override
	public void updateCMSPage(Page page) {
		pageDao.updateCMSPage(page);
		
	}
	
	/**
	 * 暂停或者取消暂停
	 */
	@Override
	public void pausePage(Long id, int status) {
		Page page = new Page();
		page.setId(id);
		page.setStatus(status);
		pageDao.updatePageStatus(page);
		pageOnlineDao.updatePageStatus(page);
	}
	
	@Override
    public void deletePage(Long id) {
		pageDao.deletePage(id);		
	}

	public Page copyActivityOfSave(String data) {
        Map<String, String> map = (Map<String, String>) JSONObject.parse(data);
        Long oldId = Long.parseLong(map.get("oldId"));
        Page page = JSONObject.parseObject(data, Page.class);
        Page existPage = pageDao.queryCMSPageByID(oldId);
        Page newPage = page;
        Long templateId = existPage.getTemplateId();
        newPage.setTemplateId(templateId);
        newPage.setType(3);
        newPage.setStatus(2);
        newPage.setPageProperties(existPage.getPageProperties());
        String userName = UserUtil.getUserId();
        newPage.setCreateId(userName);
        newPage.setUpdateId(userName);
        newPage.setCreateTime(new Date());
        newPage.setUpdateTime(new Date());
        
        String admin = newPage.getAdministrator();
        if(StringUtils.isNotBlank(admin)&&admin.indexOf(userName) == -1){       	
            	admin = userName + ","+ admin;
        }
        
        if(StringUtils.isBlank(admin)){
    		    admin = userName;
        }
        newPage.setAdministrator(admin);
        
        newPage.setModules("");
        newPage.setId(null);
        Long newPageId = pageDao.insertCMSPage(newPage);
        Map<String, Module> moduleMap = moduleService.getCMSModuleMapByModuleIds(existPage.getModules());
        
        String[] existModuleIds = existPage.getModules().split(",");
        StringBuffer sbModuleIds = new StringBuffer();
        for (int j = 0; j < existModuleIds.length; j++) {
            Module module = moduleMap.get(existModuleIds[j]);
            if(module != null){
            	module.setId(null);
                module.setPageId(newPageId);
                module.setStoreScope(storeGroupService.getStoreCodesByGroupIds(page.getStoreGroupIds()));
                resetPropers(module);
                Long newModuleId = moduleService.insertModule(module);
                sbModuleIds.append(newModuleId).append(",");
            }
        }
        
        String newModuleIds = sbModuleIds.toString();
        if(newModuleIds.endsWith(",")){
        	newModuleIds = newModuleIds.substring(0,newModuleIds.length()-1);
    	}
        newPage = pageDao.queryCMSPageByID(newPageId);
        newPage.setModules(newModuleIds);
        newPage.setUpdateId(userName);
        pageDao.updateCMSPage(newPage);
        return newPage;
	}
	
	private Module resetPropers(Module m){
		
		String moduleProperties = m.getModuleProperties();
		JSONObject proJo = JSONObject.parseObject(moduleProperties);
		try{
			//池处理
			Long poolId = proJo.getLong("poolId");
			if(poolId!=null){
				proJo.put("poolId", "1");
			}
			Object poolIds = proJo.get("poolIds");
			if(poolIds!=null){
				if (poolIds instanceof List) {
					List<String> tPoolIdList = (List<String>) proJo.get("poolIds");
					for (int i = 0; i < tPoolIdList.size(); i++) {
						tPoolIdList.set(i, "");
					}
					proJo.put("poolIds", tPoolIdList);
				}
			}
			//链接处理
			String[] pics  = new String[]{"pic1","pic2","pic3"};
			for(String pic:pics){
				JSONObject picData = (JSONObject) proJo.get(pic);
				if(picData!=null){
					picData.put("data", "1");
					picData.put("type", "gohome");
					proJo.put(pic, picData);
				}
			}
			String[] urlTypes  = new String[]{"urlType1","urlType2","urlType3","urlType4","urlType5","urlType6","urlType7","urlType8"};
			for(String type:urlTypes){
				String uType = proJo.getString(type);
				if(uType!=null){
					proJo.put(type, "gohome");
				}
			}
			String[] urlValues  = new String[]{"urlValue1","urlValue2","urlValue3","urlValue4","urlValue5","urlValue6","urlValue7","urlValue8"};
			for(String value:urlValues){
				String uValue = proJo.getString(value);
				if(uValue!=null){
					proJo.put(value, "1");
				}
			}
		}catch(Exception e){
			logger.error("复制活动属性重置错误,moduleId:"+m.getId()+";");
			e.printStackTrace();
		}
		
		m.setModuleProperties(proJo.toJSONString());
		return m;
	}
}
