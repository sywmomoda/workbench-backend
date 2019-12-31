package com.feiniu.yx.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.YXConstant;
import com.feiniu.yx.page.dao.ModuleDao;
import com.feiniu.yx.page.dao.PageDao;
import com.feiniu.yx.page.dao.PageOnlineDao;
import com.feiniu.yx.page.entity.Coupon;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.ModuleDataService;
import com.feiniu.yx.page.service.ModuleProperPlusService;
import com.feiniu.yx.page.service.ModuleService;
import com.feiniu.yx.page.service.PageService;
import com.feiniu.yx.pool.service.YxCouponService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.entity.YXStoreGroup;
import com.feiniu.yx.store.service.YXStoreGroupService;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.template.dao.YXModuleTypeDao;
import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXModuleTypeService;
import com.feiniu.yx.template.service.YXTemplateService;
import com.feiniu.yx.util.ArtTemplateUtil;
import com.feiniu.yx.util.TplUtil;
import com.feiniu.yx.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
public class PageServiceImpl implements PageService{
	
//	private static Logger logger = Logger.getLogger(PageServiceImpl.class);
	
	@Autowired
	private PageDao pageDao;

	@Autowired
	private PageOnlineDao pageOnlineDao;
	
	@Autowired
	private ModuleDao moduleDao;
	
	@Autowired
	private YXModuleTypeDao yXModuleTypeDao;
	
	@Autowired
	private ModuleService moduleService;
	
	@Autowired
	private ModuleProperPlusService moduleProperService;
	
	@Autowired
	private YXModuleTypeService moduleTypeService;
	@Autowired
	private ModuleDataService moduleDataService;
	@Autowired
	private YXTemplateService templateService;
	@Autowired
	private YXStoreService storeService;
	@Autowired
	private YXStoreGroupService storeGroupService;
	
	@Autowired
	public YxCouponService couponService;
	

	/** 根据ID查询页面
	 * @param pageId
	 * @return
	 */
	public Page queryPageByID(Long pageId) {
		Page page = pageDao.queryCMSPageByID(pageId);
		if (page == null) return page;
		page.setStoreCode(storeGroupService.getStoreCodesByGroupIds(page.getXiaoQuIds()));
        return page;
	}

	/** 获取页面html
	 * @param paramMap
	 * @return
	 */
	public Map<String, String> getPageHtml(Map<String, Object> paramMap, String previewTime) {
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
        String hfdata = pjo.toJSONString();
        pageHtmlMap.put("pageHead", ArtTemplateUtil.exeTemplateByPath(pageHeadTpl, hfdata));
        pageHtmlMap.put("pageContent", getModulesContent(page,templateUrl,storeCode,previewTime));
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
	private String getModulesContent(Page page, String templateUrl ,String storeCode, String previewTime){
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
		Map<String,String> headMap  = getModuleHeadTpl(templateUrl);
		StringBuilder modulehtmls = new StringBuilder("");
		for (String moduleIdStr : page.getModules().split(",")) {
			Module module = moduleMap.get(moduleIdStr);
			if(module.getStoreScope().indexOf(storeCode)!=-1 || StringUtils.isBlank(module.getStoreScope())){//无门店信息表示所有门店
				YXModuleType moduleType = moduleTypeMap.get(module.getModuleTypeId());
				String moduleTpl = TplUtil.getModulePath(templateUrl, moduleType.getCode());
				String data = moduleDataService.findModuledData(module, storeCode, previewTime);
				modulehtmls.append(ArtTemplateUtil.exeTemplateByPath(moduleTpl, data,headMap));
			}
		}
		return modulehtmls.toString();
	}

	private Map<String,String> getModuleHeadTpl(String templateUrl){
         Map<String,String> map = new HashMap<String,String>();
		String leftUrl = TplUtil.getModuleHeadPath(templateUrl,"color_head_left");
		if(StringUtils.isNotBlank(leftUrl)){
			map.put("{{replaceHeadLeft}}", leftUrl);
		}
		String rightUrl = TplUtil.getModuleHeadPath(templateUrl,"color_head_right");
		if(StringUtils.isNotBlank(rightUrl)){
			map.put("{{replaceHeadRight}}", rightUrl);
		}
		return map;
		
	}
	/** 保存活动，更新组件列表
	 * @param page
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
     * @param cmsPage
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
        cmsPage.setType(2);
        cmsPage.setStatus(2);
        cmsPage.setStoreCode(storeGroupService.getStoreCodesByGroupIds(cmsPage.getXiaoQuIds()));
        String admin = cmsPage.getAdministrator();
        if (admin == null) {
        	admin = "";
        }
        if (admin.indexOf(userName) < 0) {
        	admin += "," + userName;
        }
        // 活动相关人，在后面加‘，’，查询时用户名+‘，’
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
    		// 统一模板默认创建头部通栏,购物车，其他组件再手工添加。
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
            }
    	}
    	
    	if(StringUtils.isBlank(moduleIds.toString())  && moduleTypeArr.length>0){//无匹配组件，默认第一个
            Module module = createModule(moduleTypeMap.get(moduleTypeArr[0]), pageId);
            module.setStoreScope(cmsPage.getStoreCode());
            module.setCreateId(userName);
            module.setAdministrator(userName);
            Long moduleId = moduleService.insertModule(module);
            if (moduleIds.length() > 0) {
                moduleIds.append(",");
            }
            moduleIds.append(moduleId);
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
		page.setSearchWords(cmsPage.getSearchWords());
		page.setSearchAdPic(cmsPage.getSearchAdPic());
    	if(StringUtils.isNotBlank(cmsPage.getStoreGroupIds())){
    		page.setStoreGroupIds(cmsPage.getStoreGroupIds());
    	}
    	if(StringUtils.isNotBlank(cmsPage.getModules())){
    		page.setModules(cmsPage.getModules());
    	}
		if(StringUtils.isNotBlank(cmsPage.getXiaoQuIds())){
			page.setXiaoQuIds(cmsPage.getXiaoQuIds());
		}
    	if(StringUtils.isNotBlank(cmsPage.getPageProperties())){
    		page.setPageProperties(cmsPage.getPageProperties());
    	}
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
        if(list!=null && list.size()>0){
	        for (Page p : list) {
	            List<YXStore> stores = storeService.getYXStoreByGroupIds(p.getXiaoQuIds());
	            List<YXStoreGroup> groupList = storeGroupService.getStoreGroupListByGroupIds(p.getStoreGroupIds());

	            //处理兼容老数据小区数据为空时的情况
				if(StringUtils.isBlank(p.getXiaoQuIds())){
					List<YXStoreGroup> groupAllList = storeGroupService.listYXStoreGroup();
					Map<String,YXStoreGroup> groupMap = new HashMap<String,YXStoreGroup>();
					for(YXStoreGroup bg:groupAllList){
						groupMap.put(bg.getId()+"",bg);
					}
					String xiaoQuIds = "0,";
					for(YXStoreGroup bg:groupAllList){
						if(bg.getLevel()==3){
							YXStoreGroup bgParent = groupMap.get(bg.getPid()+"");
							// 旧数据groupIds数据是大区
							if((","+p.getStoreGroupIds()+",").contains(","+bgParent.getPid()+",")){
								xiaoQuIds += bg.getId()+",";
							}
						}
					}
					p.setXiaoQuIds(xiaoQuIds);
				}
				List<YXStoreGroup> xiaoQuList = storeGroupService.getStoreGroupListByGroupIds(p.getXiaoQuIds());
	            StringBuilder storeGroupNames = new StringBuilder();
	            int j = 0;
	            for (YXStoreGroup group : groupList) {
	            	if(j > 0) {
	            		storeGroupNames.append(",");
	            	}
	            	storeGroupNames.append(group.getName());
	            	j++;
	            }
				StringBuilder xiaoQuNames = new StringBuilder();
				int m = 0;
				for (YXStoreGroup group : xiaoQuList) {
					if(m > 0) {
						xiaoQuNames.append(",");
					}
					xiaoQuNames.append(group.getName());
					m++;
				}
	            
	            p.setStoreGroupNames(storeGroupNames.toString());
				p.setXiaoQuNames(xiaoQuNames.toString());
	            StringBuilder storeNames = new StringBuilder();
	            StringBuilder storeCodes = new StringBuilder();
	            int i = 0;
	            for (YXStore store : stores) {
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
		 for(Page p:list){
		 	p.setYxTemplate(templateService.getYXTemplateById(p.getTemplateId()));
		 }
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

	@Override
	public Long getPageIdByStoreCode(String ids, String storeCode) {
		Long pageId = 0L;
		List<Page> pageList = pageDao.getPagesByIds(ids);
		List<YXStoreGroup> groupList = storeGroupService.listYXStoreGroup();
		for(Page p:pageList){
			if(pageId > 0) break;
			for(YXStoreGroup group: groupList){
				if((","+p.getXiaoQuIds()+",").indexOf(","+group.getId()+",")!=-1){
					String stores = group.getStoreId();
					if((","+stores+",").indexOf(","+storeCode+",")!=-1){
						pageId = p.getId();
						break;
					}
				}
			}
		}
		return pageId;
	}

	/*
	 * 同步门店模块
	 */
	public JSONObject synsModulesOfStore(String modules, String storeCode,String synsCode) {
		String[] moduleList = modules.split(",");
		JSONObject mjo = new JSONObject();
		
		for(int i = 0; i< moduleList.length; i++){
			JSONObject obj = moduleService.findModule(moduleList[i],storeCode, synsCode);
			if(obj == null){
				continue;
			}
			
			String storeScope = obj.getString("storeScope");
			String addStoreCode = obj.getString("add");
			String delStoreCode = obj.getString("del");
			
			//如需要同步的门店，不在选择的门店中，则加上
			if(StringUtils.isNotBlank(addStoreCode)){  
				storeScope = storeScope + "," + addStoreCode;
			}	
			
			//如需要同步的门店，在选择的门店外其他的模块中包含，则清除
			if(StringUtils.isNotBlank(delStoreCode)){
			     String[] scopes = storeScope.split(",");
			     StringBuilder sb = new StringBuilder();
                 for(int j= 0;j<scopes.length;j++){
                	 if(scopes[j].equals(delStoreCode)){
                		 continue;
                	 }
                	 sb.append(scopes[j]+",");
                 }
                 storeScope = sb.substring(0, sb.length()-1);
			}
			
			Module module = new Module();
			module.setStoreScope(storeScope);
			module.setId(Long.parseLong(moduleList[i]));
			moduleService.updateModule(module);
		}
		mjo.put("state", "1");
		mjo.put("msg", "success");
		return mjo;
	}

	
	public JSONObject synsIndexModulesOfStore(String modules,String storeCode, String synsCode) throws Exception {
		String[] moduleList = modules.split(",");
		JSONObject mjo = new JSONObject();
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i< moduleList.length; i++){
			JSONObject object = moduleService.findModule(moduleList[i],storeCode, synsCode);
			if(object == null){
				continue;
			}
			
			String storeScope = object.getString("storeScope");
			String addStoreCode = object.getString("add");
			String delStoreCode = object.getString("del");
			
			//如需要同步的门店，不在选择的门店中，则加上
			if(StringUtils.isNotBlank(addStoreCode)){  
				storeScope = storeScope + "," + addStoreCode;
			}	
			
			//如需要同步的门店，在选择的门店外其他的模块中包含，则清除
			if(StringUtils.isNotBlank(delStoreCode)){
			     String[] scopes = storeScope.split(",");
			     StringBuilder sbuilder = new StringBuilder();
                 for(int j= 0;j<scopes.length;j++){
                	 if(scopes[j].equals(delStoreCode)){
                		 continue;
                	 }
                	 sbuilder.append(scopes[j]+",");
                 }
                 storeScope = sbuilder.substring(0, sbuilder.length()-1);
			}
			
			//有信息改变才更新
			if(StringUtils.isNotBlank(addStoreCode)||StringUtils.isNotBlank(delStoreCode)){
				Module oModule = new Module();
				oModule.setStoreScope(storeScope);
				oModule.setId(Long.parseLong(moduleList[i]));
				moduleService.updateModule(oModule);		
			}
			
		
			//处理配置项参数
			ModuleProperPlus m = new ModuleProperPlus();
			Long moduleId = Long.parseLong(moduleList[i]);
			m.setModuleId(moduleId);
			
			m.setStoreCode(synsCode);	
			ModuleProperPlus synsModule = moduleProperService.findModuleProperByIdAndStoreCode(m);
			
			m.setStoreCode(storeCode);
			ModuleProperPlus  module = moduleProperService.findModuleProperByIdAndStoreCode(m);				
			
			if(synsModule != null){
				ModuleProperPlus one = new ModuleProperPlus();
				Module mo = moduleDao.queryModuleByID(moduleId);
				YXModuleType mt = yXModuleTypeDao.getYXModuleTypeById(mo.getModuleTypeId());
				String type = mt.getCode();
				String moduleName = mo.getName();
				String proper = synsModule.getModuleProper();
				JSONObject obj = (JSONObject) JSONObject.parse(proper);	
								
				if(type.equals("i-gift")||type.equals("i-giftMini")||type.equals("i-timeCouponMini")||type.equals("i-popUp")||type.equals("i-seckill")||type.equals("m-coupon")||type.equals("m-pic-two")||type.equals("i-selected")||type.equals("m-pic-banner-noboder")||type.equals("i-commodity")){  //i-stampPrinting,i-baokuan,i-newRecommend,i-commodityTime
					String coupon = obj.getString("couponId");
					if(type.equals("i-popUp")){
						coupon = obj.getJSONObject("picType").getString("couponId");
					}
					if(type.equals("i-seckill")||type.equals("i-commodity")||type.equals("i-storeManagerRecommend")){
						coupon = obj.getJSONObject("moreType").getString("couponId");
					}
					if(type.equals("m-pic-two")||type.equals("m-pic-banner-noboder")){
						coupon = obj.getJSONObject("pic1").getString("couponId");
					}
					if(type.equals("i-selected")){
						coupon = obj.getJSONObject("type").getString("couponId");
					}
							
					if(coupon.indexOf("[") != -1){ //多张券
						proper = processMutiCoupons(obj,storeCode,synsCode,sb,moduleName);																				
					}else{ //单张券
				           JSONObject checkMsg = couponService.getCouponInfoByCouponId(coupon);
							String sCode = getSCode(checkMsg);
							if(StringUtils.isBlank(sCode)){
								if(sb.indexOf(moduleName) == -1){
								    sb.append(moduleName).append(",");
								}
								proper = "";
							}else if(sCode.indexOf(storeCode) == -1){
								if(sb.indexOf(moduleName) == -1){
								    sb.append(moduleName).append(",");
								}
								proper = "";
							}			
					}								
				}else if(type.equals("i-couponPopup")){
					String coupons = obj.getString("couponIds");
					if(coupons.indexOf("[") != -1){ //多个档期
						JSONArray $couponId = obj.getJSONArray("couponIds");
						JSONArray $title = obj.getJSONArray("title");
						JSONArray $isAction = obj.getJSONArray("isAction");
						JSONArray $beginTime = obj.getJSONArray("beginTime");
						JSONArray $endTime = obj.getJSONArray("endTime");
						JSONArray $couponNames = obj.getJSONArray("couponNames");
						JSONArray $couponDescribes = obj.getJSONArray("couponDescribes");

						Coupon newCoupon = new Coupon(1);
						int n = 0;
					
						for(int j = 0; j<$couponId.size();j++){	
							String couponCode = $couponId.get(j).toString();
							String title = $title.get(j).toString();
							String isAction = $isAction.get(j).toString();
							String beginTime = $beginTime.get(j).toString();
							String endTime = $endTime.get(j).toString();
							String couponNames = $couponNames.get(j).toString();
							String couponDescribes = $couponDescribes.get(j).toString();
							
							String[] couponArray = couponCode.split(",");						
							String[] couponNamesArray = couponNames.split(",");
							String[] couponDescribesArray = couponDescribes.split(",");
							
							if(couponArray.length == 1){  //单一档期内只有一个优惠券
								   JSONObject checkMsg = couponService.getCouponInfoByCouponId(couponCode);
								   String sCode = getSCode(checkMsg);
									if(StringUtils.isBlank(sCode)){
										if(sb.indexOf(moduleName) == -1){
										    sb.append(moduleName).append(",");
										}
									}else if(sCode.indexOf(storeCode) == -1){
										if(sb.indexOf(moduleName) == -1){
										    sb.append(moduleName).append(",");
										}
									}else{								   
									   newCoupon = buildNewCouponProper(obj, j,newCoupon);
									   n++;
								   }
							}else{ // 单一档期内多个优惠券
								StringBuilder couponIdsSB = new StringBuilder();
								StringBuilder couponNamesSB = new StringBuilder();
								StringBuilder couponDescribesSB = new StringBuilder();						
								
								for(int k =0;k < couponArray.length;k++){
									JSONObject checkMsg = couponService.getCouponInfoByCouponId(couponArray[k]);
							        String sCode = checkMsg.getString("storeCodes");
							        if(sCode == null){
							        	if(sb.indexOf(moduleName) == -1){
							        	   sb.append(moduleName).append(",");
							        	}
							        }else if((sCode.indexOf(storeCode) == -1 && sCode.indexOf(synsCode) != -1)||checkMsg.get("msg")!=null){
							        	if(sb.indexOf(moduleName) == -1){
							        	   sb.append(moduleName).append(",");
							        	}
									}else{								
										   couponIdsSB.append(couponArray[k]).append(",");
										   couponNamesSB.append(couponNamesArray[k]).append(",");
										   couponDescribesSB.append(couponDescribesArray[k]).append(",");					   
									 }							
								}
								
								if(couponIdsSB.length() > 1){
									newCoupon.getCouponIds().add(couponIdsSB.substring(0, couponIdsSB.length()-1));
									if(couponNamesSB.length() > 1){
										newCoupon.getCouponNames().add(couponNamesSB.substring(0, couponNamesSB.length()-1));
									}
									
									if(couponDescribesSB.length() > 1){
										newCoupon.getCouponDescribes().add(couponDescribesSB.substring(0, couponDescribesSB.length()-1));
									}										
									
									if(StringUtils.isNotBlank(beginTime)){
										newCoupon.getBeginTime().add(beginTime);
									}
									if(StringUtils.isNotBlank(endTime)){
										newCoupon.getEndTime().add(endTime);
									}								
									if(StringUtils.isNotBlank(title)){
										newCoupon.getTitle().add(title);
									}			
									if(StringUtils.isNotBlank(isAction)){
										newCoupon.getIsAction().add(isAction);
									}	
									n++;	
								}						
							}
						}
						
						if(n >1){
							proper = JSONObject.toJSONString(newCoupon);			
						}else if(n == 1){
							proper = JSONObject.toJSONString(newCoupon).replaceAll("[\\[\\]]", "");
						}else{
							proper = "";
						}
					}else{ //一个档期
						proper = processEachSchedule(obj,storeCode,synsCode,sb,moduleName);
					}
				}

				one.setModuleProper(proper);			
				one.setModuleId(synsModule.getModuleId());
				one.setStoreCode(storeCode);	
				if(module == null && StringUtils.isNotBlank(proper)){								
					moduleProperService.insertModuleProper(one);
				}else if(module != null && StringUtils.isNotBlank(proper)){
					one.setId(module.getId());
					one.setUpdateTime(new Date());
					one.setUpdateId(module.getCreateId());
					moduleProperService.updateModuleProper(one);
				}else if(module != null && StringUtils.isBlank(proper)){
					moduleProperService.deleteModuleProperById(module.getId());
				}
			}

			if(module != null && synsModule == null){
				moduleProperService.deleteModuleProperById(module.getId());
			}			
		}
		
		if(StringUtils.isNotBlank(sb.toString())){
			mjo.put("state", "2");
			mjo.put("msg", sb.substring(0, sb.length()-1));		
			return mjo;
		}
		
		mjo.put("state", "1");
		mjo.put("msg", "success");
		return mjo;
	}

	@Override
	public String pageCopy(JSONObject param) {
		Long pageId = param.getLong("id");
		Page page = pageDao.queryCMSPageByID(pageId);
		JSONObject resMsg = new JSONObject();
		if(null == page) {
			resMsg.put("msg", "复制异常");
			return resMsg.toJSONString();
		}
		Page newPage = page.clone();
		if(null == newPage) {
			resMsg.put("msg", "复制异常");
			return resMsg.toJSONString();
		}
		Date beginTime = param.getDate("activityBeginTime");
		Date endTime = param.getDate("activityEndTime");
		String admin = param.getString("administrator");
		String usrname = UserUtil.getUserId();
		String name = param.getString("name");
		Object storeGroupIds =param.get("storeGroupIds");
		String goupIds = storeGroupIds.toString();
		if(storeGroupIds instanceof List) {
			goupIds = StringUtils.join(param.getJSONArray("storeGroupIds"),",");
		}
        Object xiaoQuIds =param.get("xiaoQuIds");
        String xqIds = xiaoQuIds.toString();
        if(xiaoQuIds instanceof List) {
            xqIds = StringUtils.join(param.getJSONArray("xiaoQuIds"),",");
        }
		newPage.setId(null);
		newPage.setName(name);
		newPage.setStoreGroupIds(goupIds);
        newPage.setXiaoQuIds(xqIds);
		newPage.setActivityBeginTime(beginTime);
		newPage.setActivityEndTime(endTime);
        newPage.setAdministrator(admin);
        newPage.setCreateId(usrname);
        newPage.setUpdateId(usrname);
        newPage.setCreateTime(new Date());
        newPage.setUpdateTime(new Date());
        newPage.setStatus(2);//待发布
        String moduleIds = page.getModules();
		if (StringUtils.isBlank(moduleIds)) {
			resMsg.put("msg", "请添加被复制活动模块");
			return resMsg.toJSONString();
		}
        Long newId = pageDao.insertCMSPage(newPage);
		List<Module> listModule = moduleService.getCMSModuleArrayByModuleIds(moduleIds);
		String[] idArray = moduleIds.split(",");
        String[]  newIdArray = new String[idArray.length];
		for(Module m  : listModule) {
        	if(null == m) {
        		continue;
        	}
        	Long mId = m.getId();
        	int index = getIndex(idArray,mId.toString());
        	Long moduleId =  moduleService.moduleCopy(m, newId);
        	index = index  == -1 ? newIdArray.length -1 : index;
        	newIdArray[index] = moduleId.toString(); 
        }
		newPage = pageDao.queryCMSPageByID(newId);
		newPage.setModules(StringUtils.join(newIdArray,",").replaceAll("null", ""));
		pageDao.updateCMSPage(newPage);
        resMsg.put("msg", "success");
        resMsg.put("pageId", newId);
		return resMsg.toJSONString();
	}
	
	
	private static int getIndex(String[] arr, String value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(value)) {
                return i;
            }
        }
        return -1;//如果未找到返回-1
	}


	private String processEachSchedule(JSONObject obj,String storeCode,String synsCode,StringBuilder sb,String moduleName) throws Exception{
		String coupons = obj.getString("couponIds");
		String couponNames = obj.getString("couponNames");
		String couponDescribes = obj.getString("couponDescribes");
		String proper = "";
		if(StringUtils.isNotBlank(coupons)){
			String[] couponArray = coupons.split(",");
			String[] couponNamesArray = couponNames.split(",");
			String[] couponDescribesArray = couponDescribes.split(",");
			StringBuilder newId = new StringBuilder();
			StringBuilder newName = new StringBuilder();
			StringBuilder newDescribe = new StringBuilder();
			for(int j = 0; j<couponArray.length; j++){
				JSONObject checkMsg = couponService.getCouponInfoByCouponId(couponArray[j]);
				String sCode = getSCode(checkMsg);
				if(sCode.indexOf(storeCode) == -1){								
		        	sb.append(moduleName).append(",");
				}else{
					newId.append(couponArray[j]).append(",");
					newName.append(couponNamesArray[j]).append(",");
					newDescribe.append(couponDescribesArray[j]).append(",");
				}
			}
			
			if(newId.length() >0){
				String newCouponId = newId.substring(0,newId.length()-1);
				obj.put("couponIds", newCouponId);
			}
			
			if(newName.length() > 0){
				String newCouponName = newName.substring(0,newName.length()-1);
				obj.put("couponNames", newCouponName);
			}
			
			if(newDescribe.length() > 0){
				String newCouponDescribe = newDescribe.substring(0,newDescribe.length()-1);
				obj.put("couponDescribes", newCouponDescribe);
			}

			if(newId.length()==0&&newName.length()==0&&newDescribe.length()==0){
				proper = "";
			}else{
				proper = obj.toJSONString();
			}		
		}
		return proper;
	}
	
	private String processMutiCoupons(JSONObject obj,String storeCode,String synsCode,StringBuilder sb,String moduleName) throws Exception{
		JSONArray $couponId = obj.getJSONArray("couponId");									
		Coupon newCoupon = new Coupon();
		int n = 0;
		String proper = "";
		
		for(int j = 0; j<$couponId.size();j++){	
			String couponCode = $couponId.get(j).toString();
			JSONObject checkMsg = couponService.getCouponInfoByCouponId(couponCode);
			String sCode = getSCode(checkMsg);

			if(StringUtils.isBlank(sCode)){
				if(sb.indexOf(moduleName) == -1){
				    sb.append(moduleName).append(",");
				}
			}else if(sCode.indexOf(storeCode) == -1){
				if(sb.indexOf(moduleName) == -1){
				    sb.append(moduleName).append(",");
				}
			}else{

				newCoupon = buildNewCouponProper(obj, j,newCoupon);
				n++;
			}												
		}	
		
		if(n >1){
			proper = JSONObject.toJSONString(newCoupon);			
		}else if(n ==1){
			proper = JSONObject.toJSONString(newCoupon).replaceAll("[\\[\\]]", "");
		}else{
			proper = "";
		}									
		return proper;
	}

	private Coupon buildNewCouponProper(JSONObject obj, int j,Coupon newCoupon){
		
		newCoupon.getCouponId().add(obj.getJSONArray("couponId").get(j).toString());
		
		if(obj.containsKey("couponType")){
			if(StringUtils.isNotBlank(obj.getString("couponType"))){
				JSONArray $couponType = obj.getJSONArray("couponType");
				String couponType = $couponType.get(j).toString();
				newCoupon.getCouponType().add(couponType);								
			}else{
				newCoupon.getCouponType().add("");
			}		
		}else{
			newCoupon.setCouponType(null);
		}
		
		if(obj.containsKey("conponTypeName")){
			if(StringUtils.isNotBlank(obj.getString("conponTypeName"))){
				JSONArray $conponTypeName = obj.getJSONArray("conponTypeName");
				String conponTypeName = $conponTypeName.get(j).toString();
				newCoupon.getConponTypeName().add(conponTypeName);								
			}else{
				newCoupon.getConponTypeName().add("");
			}		
		}else{
			newCoupon.setConponTypeName(null);
		}
		
		if(obj.containsKey("couponName")){
			if(StringUtils.isNotBlank(obj.getString("couponName"))){
				JSONArray $couponName = obj.getJSONArray("couponName");
				String couponName = $couponName.get(j).toString();
				newCoupon.getCouponName().add(couponName);							
			}else{
				newCoupon.getCouponName().add("");					
			}
		}else{
			newCoupon.setCouponName(null);
		}
		
	    if(obj.containsKey("couponDescribe")){
	    	if(StringUtils.isNotBlank(obj.getString("couponDescribe"))){
				JSONArray $couponDescribe = obj.getJSONArray("couponDescribe");
				String couponDescribe = $couponDescribe.get(j).toString();
				newCoupon.getCouponDescribe().add(couponDescribe);							
			}else{
				newCoupon.getCouponDescribe().add("");
			}
		}else{
				newCoupon.setCouponDescribe(null);		
		}	
		
        if(obj.containsKey("discountType")){
        	if(StringUtils.isNotBlank(obj.getString("discountType"))){
    			JSONArray $discountType = obj.getJSONArray("discountType");
    			String discountType = $discountType.get(j).toString();
    			newCoupon.getDiscountType().add(discountType);							
    		}else{
    			newCoupon.getDiscountType().add("");	
    		}
    	}else{
    			newCoupon.setDiscountType(null);   	
		}	

        if(obj.containsKey("totalMaxCount")){
        	if(StringUtils.isNotBlank(obj.getString("totalMaxCount"))){
    			JSONArray $totalMaxCount = obj.getJSONArray("totalMaxCount");
    			String totalMaxCount = $totalMaxCount.get(j).toString();
    			newCoupon.getTotalMaxCount().add(totalMaxCount);							
    		}else{
    			newCoupon.getTotalMaxCount().add("");
    		}
    	}else{
    			newCoupon.setTotalMaxCount(null);   	
		}
			
        if(obj.containsKey("dayMaxCount")){
        	if(StringUtils.isNotBlank(obj.getString("dayMaxCount"))){
    			JSONArray $dayMaxCount = obj.getJSONArray("dayMaxCount");
    			String dayMaxCount = $dayMaxCount.get(j).toString();
    			newCoupon.getDayMaxCount().add(dayMaxCount);							
    		}else{
    			newCoupon.getDayMaxCount().add("");	
    		}
    	}else{  			
    		newCoupon.setDayMaxCount(null);
		}
				
        if(obj.containsKey("bgcolor")){
        	if(StringUtils.isNotBlank(obj.getString("bgcolor"))){
    			JSONArray $bgcolor = obj.getJSONArray("bgcolor");
    			String bgcolor = $bgcolor.get(j).toString();
    			newCoupon.getBgcolor().add(bgcolor);							
    		}else{
    			newCoupon.getBgcolor().add("");	
    		}
    	}else{
    		newCoupon.setBgcolor(null);
	    }
				
        if(obj.containsKey("doorsill")){
        	if(StringUtils.isNotBlank(obj.getString("doorsill"))){
    			JSONArray $doorsill = obj.getJSONArray("doorsill");
    			String doorsill = $doorsill.get(j).toString();
    			newCoupon.getDoorsill().add(doorsill);							
    		}else{
    			newCoupon.getDoorsill().add("");
    		}
    	}else{
    		newCoupon.setDoorsill(null);
		}
				
        if(obj.containsKey("description")){
        	if(StringUtils.isNotBlank(obj.getString("description"))){
    			JSONArray $description = obj.getJSONArray("description");
    			String description = $description.get(j).toString();
    			newCoupon.getDescription().add(description);							
    		}else{
    			newCoupon.getDescription().add("");	
    		}
    	}else{
    		newCoupon.setDescription(null);
		}
			
        if(obj.containsKey("date")){
        	if(StringUtils.isNotBlank(obj.getString("date"))){
    			JSONArray $date = obj.getJSONArray("date");
    			String date = $date.get(j).toString();
    			newCoupon.getDate().add(date);							
    		}else{
    			newCoupon.getDate().add("");
    		}
    	}else{
    		newCoupon.setDate(null);
		}
		
		
       if(obj.containsKey("value")){
    	   if(StringUtils.isNotBlank(obj.getString("value"))){
	   			JSONArray $value = obj.getJSONArray("value");
	   			String value = $value.get(j).toString();
	   			newCoupon.getValue().add(value);							
   			}else{
   				newCoupon.getValue().add("");
   			}
   		}else{
   			newCoupon.setValue(null);
		}
		
		
        if(obj.containsKey("title")){
        	if(StringUtils.isNotBlank(obj.getString("title"))){
    			JSONArray $title = obj.getJSONArray("title");
    			String title = $title.get(j).toString();
    			newCoupon.getTitle().add(title);							
    		}else{
    			newCoupon.getTitle().add("");	
    		}
    	}else{
    		newCoupon.setTitle(null);
		}
		
		
        if(obj.containsKey("isAction")){
        	if(StringUtils.isNotBlank(obj.getString("isAction"))){
    			    JSONArray $isAction = obj.getJSONArray("isAction");
    				String isAction = $isAction.get(j).toString();
    				newCoupon.getIsAction().add(isAction);							
    		}else{
    			newCoupon.getIsAction().add("");
    		}
    	}else{
    		newCoupon.setIsAction(null);
		}
		
		
        if(obj.containsKey("beginTime")){
        	if(StringUtils.isNotBlank(obj.getString("beginTime"))){
    			JSONArray $beginTime = obj.getJSONArray("beginTime");
    			String beginTime = $beginTime.get(j).toString();
    			newCoupon.getBeginTime().add(beginTime);							
    		}else{
    			newCoupon.getBeginTime().add("");
    		}
    	}else{
    		newCoupon.setBeginTime(null);
		}
		
		
        if(obj.containsKey("endTime")){
        	if(StringUtils.isNotBlank(obj.getString("endTime"))){
    			JSONArray $endTime = obj.getJSONArray("endTime");
    			String endTime = $endTime.get(j).toString();
    			newCoupon.getEndTime().add(endTime);							
    		}else{
    			newCoupon.getEndTime().add("");
    		}
    	}else{
    		newCoupon.setEndTime(null);
		}
		
		
        if(obj.containsKey("storeCodes")){
        	if(StringUtils.isNotBlank(obj.getString("storeCodes"))){
    			JSONArray $storeCodes = obj.getJSONArray("storeCodes");
    			String storeCodes = $storeCodes.get(j).toString();
    			newCoupon.getStoreCodes().add(storeCodes);							
    		}else{
    			newCoupon.getStoreCodes().add("");	
    		}
    	}else{
    		newCoupon.setStoreCodes(null);
		}
        
        if(obj.containsKey("couponDescribes")){
        	if(StringUtils.isNotBlank(obj.getString("couponDescribes"))){
    			JSONArray $couponDescribes = obj.getJSONArray("couponDescribes");
    			String couponDescribes = $couponDescribes.get(j).toString();
    			newCoupon.getCouponDescribes().add(couponDescribes);							
    		}else{
    			newCoupon.getCouponDescribes().add("");	
    		}
    	}else{
    		newCoupon.setCouponDescribes(null);
		}
        
        if(obj.containsKey("couponIds")){
        	if(StringUtils.isNotBlank(obj.getString("couponIds"))){
    			JSONArray $couponIds = obj.getJSONArray("couponIds");
    			String couponIds = $couponIds.get(j).toString();
    			newCoupon.getCouponIds().add(couponIds);							
    		}else{
    			newCoupon.getCouponIds().add("");	
    		}
    	}else{
    		newCoupon.setCouponIds(null);
		}
        
        if(obj.containsKey("couponNames")){
        	if(StringUtils.isNotBlank(obj.getString("couponNames"))){
    			JSONArray $couponNames = obj.getJSONArray("couponNames");
    			String couponNames = $couponNames.get(j).toString();
    			newCoupon.getCouponNames().add(couponNames);							
    		}else{
    			newCoupon.getCouponNames().add("");	
    		}
    	}else{
    		newCoupon.setCouponNames(null);
		}
		
			
		return newCoupon;
			
	}
	
	private String getStoreIds(String sCode) {
		List<YXStore> stores = storeService.getStoreByPgSeq(sCode);
		StringBuilder sb = new StringBuilder();
		for(YXStore one : stores){
			sb.append(one.getCode()).append(",");
		}
		return sb.substring(0,sb.length()-1);
	}
	
	private String getSCode(JSONObject checkMsg){
		String sCode = "";
		if(checkMsg.get("msg") ==null){
			sCode = checkMsg.getString("storeCodes");
			if(StringUtils.isBlank(sCode)){
				sCode = "all";
			}
			if(sCode.indexOf("CPG") != -1){
				sCode = getStoreIds(sCode);
			}
		}
		return sCode;
	}

}
