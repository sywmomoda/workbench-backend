package com.feiniu.yx.page.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.YXConstant;
import com.feiniu.yx.common.interceptor.LogTrace;
import com.feiniu.yx.common.service.UserService;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.PagePublishService;
import com.feiniu.yx.page.service.PageService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.entity.YXStoreGroup;
import com.feiniu.yx.store.service.YXStoreGroupService;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXTemplateService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.PageColorUtil;
import com.feiniu.yx.util.UserUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/page")
public class PageController {
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private PagePublishService pagePublishService;
	
	@Autowired
	private YXTemplateService templateService;
	
	@Autowired
	private YXStoreGroupService storeGroupService;
	
	@Autowired
	private YXStoreService storeService;
	
	@Autowired
	private UserService userService;
	
	@RequestMapping("/listAll")
    public ModelAndView listAll(HttpServletRequest request, HttpServletResponse response, Page page) {
        ModelAndView mv = new ModelAndView("page/activityList");
        String local = request.getParameter("local");
        String userName = UserUtil.getUserId();
        if("1".equals(local)){
        	page.setCreateId(userName);
        }
        page.setType(2);
        List<Page> list = pageService.queryActivityList(page, 0);
        List<YXStoreGroup> listGroup = storeGroupService.getAreaGroup();
        mv.addObject("list", list);
        mv.addObject("page", page);
        mv.addObject("local", local);
        mv.addObject("userName", userName);
        mv.addObject("yxuri", YXConstant.YXURI);
        mv.addObject("listGroup", listGroup);
        return mv;
    }
	
	@RequestMapping("/toCreatePage")
    public ModelAndView toCreatePage(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mv = new ModelAndView("page/addActivity");
		List<YXStoreGroup> groups = storeGroupService.listYXStoreGroup();
		mv.addObject("groups", groups);
		mv.addObject("templateId", YXConstant.TEMPID);
        return mv;
    }
	
	@RequestMapping("/toCreatePageByTemplateId")
    public ModelAndView toCreatePageByTemplateId(HttpServletRequest request, HttpServletResponse response,@RequestParam Long id) {
		ModelAndView mv = new ModelAndView("page/addActivity");
		List<YXStoreGroup> groups = storeGroupService.listYXStoreGroup();
		mv.addObject("groups", groups);
		mv.addObject("templateId", id);
        return mv;
    }
	
	/**
	 * 创建活动
	 * @author tongwenhuan
	 * 2017年3月14日
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/createPage",method=POST)
    public String createPage(Page page) {
		pageService.createPage(page);
		return "redirect:listAll.form"; 
    }
	
	@RequestMapping(value="/updatePage")
	@LogTrace(msgFomort={"更新活动属性(标题:{name},描述:{description},权限:{administrator},开始时间:{activityBeginTime},结束时间:{activityEndTime})","page","{id}"})
    public void updatePage(HttpServletRequest request, HttpServletResponse response, @ModelAttribute Page page){
		pageService.updateCMSPageInfo(page);
		JSONObject mjo = new JSONObject();
		mjo.put("state", "1");
		ControllerUtil.writeJson(response, mjo.toJSONString());
    }
	
	@RequestMapping("/toEditPage")
    public ModelAndView toEditPage(HttpServletRequest request, HttpServletResponse response, 
    		@RequestParam Long id , @RequestParam(defaultValue="") String storeCode, @RequestParam(defaultValue="") String groupId){
		ModelAndView mv = new ModelAndView("page/editActivity");
		String userName = UserUtil.getUserId();
		if(id == null)return null;
		Page page = pageService.queryPageByID(id);
        YXTemplate template = templateService.getYXTemplateById(page.getTemplateId());
        page.setYxTemplate(template);
        try{
        	page.setPagePropertieMap(JSONObject.parseObject(page.getPageProperties()));
        }catch(Exception e){}
		Map<String, YXStoreGroup> areaMap = new HashMap<String,YXStoreGroup>();
		Map<String, YXStoreGroup> groupMap = storeGroupService.getFmap();
        String groupIds = page.getStoreGroupIds();
		//处理当小区数据为空时，获取大区下所有小区数据
		if(StringUtils.isBlank(page.getXiaoQuIds())){
			String xiaoQuIds = "";
			List<YXStoreGroup> groupList = storeGroupService.listYXStoreGroup();
			for(YXStoreGroup bg:groupList){
				if(bg.getLevel()==3){
					YXStoreGroup bgParent = groupMap.get(bg.getPid()+"");
					// 兼容池中旧数据groupIds数据是大区的情况，xiaoQuIds取大区下所有小区
					if((","+page.getStoreGroupIds()+",").contains(","+bgParent.getPid()+",")){
						xiaoQuIds += bg.getId()+",";
					}
				}
			}
			if(xiaoQuIds.endsWith(",")){
				xiaoQuIds = xiaoQuIds.substring(0,xiaoQuIds.length()-1);
			}
			page.setXiaoQuIds(xiaoQuIds);
			pageService.updateCMSPage(page);
		}
        String xiaoQuIds = page.getXiaoQuIds();
		List<YXStoreGroup> listXiaoQu = new ArrayList<YXStoreGroup>();

        if (null != groupMap) {
			String[] xids = xiaoQuIds.split(",");
			for (String xid : xids) {
				YXStoreGroup sg = groupMap.get(xid);
				if (sg != null) {
					YXStoreGroup parentSg = groupMap.get(sg.getPid()+"");
					if(parentSg!=null){
						if(areaMap.get(parentSg.getPid()+"")!=null){
							YXStoreGroup areaGroup = areaMap.get(parentSg.getPid()+"");
							areaGroup.setPreStoreCodes(areaGroup.getPreStoreCodes()+","+sg.getStoreId());
						}else{
							areaMap.put(parentSg.getPid()+"",groupMap.get(parentSg.getPid()+""));
							areaMap.get(parentSg.getPid()+"").setPreStoreCodes(sg.getStoreId());
						}
						sg.setPid(parentSg.getPid());
					}
					listXiaoQu.add(sg);
				}
			}
        }
        
        StringBuilder storeCodeSb= new StringBuilder();
        int j = 0;
        for (YXStoreGroup bg : listXiaoQu) {
        	if (j > 0) {
        		storeCodeSb.append(",");
        	}
    		storeCodeSb.append(bg.getStoreId());
    		j++;
        }
		List<YXStoreGroup> listGroup =  new ArrayList<YXStoreGroup>(areaMap.values());
        JSONArray groups = (JSONArray) JSON.toJSON(listGroup);
		JSONArray xiaoQus = (JSONArray) JSON.toJSON(listXiaoQu);
        
        YXStore yxStore = storeService.getYXStoreByCode(storeCode);
        String storeName = "";
        if(yxStore != null){
        	storeName = yxStore.getName() + "[" + storeCode+"]";
        }
        //用户有权限的门店
        String userStore = userService.getUserStore();
        mv.addObject("groupId", groupId);
        mv.addObject("userStore", userStore);
        mv.addObject("pageId",id);
        mv.addObject("cmspage",page);
        mv.addObject("groups", groups.toJSONString());
		mv.addObject("xiaoQus", xiaoQus.toJSONString());
        mv.addObject("userName", userName);
        mv.addObject("storeCode",storeCode);
        mv.addObject("storeName",storeName);
        mv.addObject("activityStoreCode",storeCodeSb.toString());
        mv.addObject("colors",PageColorUtil.getPageColor());
        return mv;
    }
	
	@RequestMapping("/toEditPageInfo")
    public ModelAndView toEditPageInfo(HttpServletRequest request, HttpServletResponse response,
    		@RequestParam Long id , @RequestParam(defaultValue="") String storeCode,@RequestParam(defaultValue = "") String actType){
		ModelAndView mv = new ModelAndView("page/editActivityInfo");
		Page page = pageService.queryPageByID(id);

        if(actType.equals("copy")) {
        	//List<YXStoreGroup> groups = storeGroupService.getStoreGroupListByGroupIds(page.getStoreGroupIds());
        	//mv.addObject("group", groups);
            if(StringUtils.isBlank(page.getXiaoQuIds())){
                Map<String, YXStoreGroup> groupMap = storeGroupService.getFmap();
                String xiaoQuIds = "";
                List<YXStoreGroup> groupList = storeGroupService.listYXStoreGroup();
                for(YXStoreGroup bg:groupList){
                    if(bg.getLevel()==3){
                        YXStoreGroup bgParent = groupMap.get(bg.getPid()+"");
                        // 兼容池中旧数据groupIds数据是大区的情况，xiaoQuIds取大区下所有小区
                        if((","+page.getStoreGroupIds()+",").contains(","+bgParent.getPid()+",")){
                            xiaoQuIds += bg.getId()+",";
                        }
                    }
                }
                if(xiaoQuIds.endsWith(",")){
                    xiaoQuIds = xiaoQuIds.substring(0,xiaoQuIds.length()-1);
                }
                page.setXiaoQuIds(xiaoQuIds);
				pageService.updateCMSPage(page);
            }
        }
        mv.addObject("cmsPage", page);
    	mv.addObject("actType", actType);
        return mv;
    }

	@RequestMapping("/checkModuleSave")
	public void checkModuleSave(HttpServletRequest request, HttpServletResponse response, @RequestParam Long pageId, @RequestParam String oldModule){
		Page page = pageService.queryPageByID(pageId);
		JSONObject mjo = new JSONObject();
		if(oldModule.equals(page.getModules())){
			mjo.put("state", "1");
			mjo.put("msg", "验证通过");
		}else{
			mjo.put("state", "-1");
			mjo.put("msg", "模块顺序或数量有变动，请先更新后再编辑!");
		}
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	@RequestMapping("/savePageModules")
	@LogTrace(msgFomort={"保存活动页(组件:{modules}，属性：{pageProperties})","page","{id}"})
	public void savePageModules(HttpServletRequest request, HttpServletResponse response, @ModelAttribute Page page){
		String result = pageService.savePageModules(page);
		ControllerUtil.writeJson(response, result);
	}
	
	@RequestMapping("/preview")
	public ModelAndView preview(HttpServletRequest request, HttpServletResponse response, @RequestParam Long pageId, @RequestParam String st, @RequestParam String previewTime){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("pageId", pageId);
		param.put("store", st);
		Map<String, String> pageHtmlMap = pageService.getPageHtml(param,previewTime);
		ModelAndView mv = new ModelAndView("page/preview");
		mv.addAllObjects(pageHtmlMap);
		return mv;
	}
	
	@RequestMapping("/publish")
	@LogTrace(msgFomort={"发布活动页","page","{pageId}"})
	public void publish(HttpServletRequest request, HttpServletResponse response, @RequestParam Long pageId){
		pagePublishService.publish(pageId);
		ControllerUtil.writeJson(response);
	}
	
	/**
	 * 暂停活动
	 * @author tongwenhuan
	 * 2017年3月29日
	 * @param request
	 * @param response
	 * @param pageId
	 */
	@RequestMapping("/pause")
	public void pause(HttpServletRequest request, HttpServletResponse response, @RequestParam Long pageId){
		//设为待发布
		pageService.pausePage(pageId,2);
		ControllerUtil.writeJson(response);
	}
	
	/**
	 * 复制店铺模块
	 * @param request
	 * @param response
	 * @param storeCode
	 * @throws Exception
	 */
	@RequestMapping(value="/synsStoreModule")
	@LogTrace(msgFomort={"同步活动门店配置(需同步门店:{storeCode},模板门店:{synsCode})","page","{pageId}"})
    public void copyStore(HttpServletRequest request, HttpServletResponse response,@RequestParam Long pageId,@RequestParam String storeCode,@RequestParam String synsCode) throws Exception{
		Page page = pageService.queryPageByID(pageId);
		JSONObject mjo = new JSONObject();
		String modules = page.getModules();
		if(StringUtils.isBlank(modules)){
			mjo.put("state", "0");
			mjo.put("msg", "该门店下无数据内容可同步!");
		} else {
				mjo = pageService.synsIndexModulesOfStore(modules,storeCode,synsCode);
		}
		ControllerUtil.writeJson(response, mjo.toJSONString());
    }
	
	
	@RequestMapping(value="/pageCopy")
	public void pageCopy(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String data) {
		JSONObject param = JSONObject.parseObject(data);
		String res = pageService.pageCopy(param);
		ControllerUtil.writeJson(response, res);
	}

}
