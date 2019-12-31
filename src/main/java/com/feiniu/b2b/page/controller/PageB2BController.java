package com.feiniu.b2b.page.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.common.B2BConstant;
import com.feiniu.b2b.page.service.B2BPageService;
import com.feiniu.b2b.store.dao.B2BStoreGroupDao;
import com.feiniu.b2b.store.entity.B2BStoreGroup;
import com.feiniu.yx.common.interceptor.LogTrace;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.PagePublishService;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXTemplateService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.PageColorUtil;
import com.feiniu.yx.util.UserUtil;

@Controller
@RequestMapping("/pageb2b")
public class PageB2BController {
	
	@Autowired
	private B2BPageService pageService;
	
	@Autowired
	private PagePublishService b2bPagePublishService;
	
	@Autowired
	private YXTemplateService templateService;
	
	@Autowired
	private B2BStoreGroupDao storeGroupDao;
	
	@RequestMapping("/listAll")
    public ModelAndView listAll(HttpServletRequest request, HttpServletResponse response, Page page) {
        ModelAndView mv = new ModelAndView("page_b2b/activityList");
        String local = request.getParameter("local");
        String userName = UserUtil.getUserId();
        if("1".equals(local)){
        	page.setCreateId(userName);
        }
        page.setType(3);
        List<Page> list = pageService.queryActivityList(page, 0);
        List<B2BStoreGroup> listGroup = storeGroupDao.getB2BStoreGroupList();
        mv.addObject("list", list);
        mv.addObject("page", page);
        mv.addObject("local", local);
        mv.addObject("userName", userName);
        mv.addObject("yxurl", B2BConstant.B2BURL);
        mv.addObject("listGroup", listGroup);
        return mv;
    }
	
	@RequestMapping("/toCreatePage")
    public ModelAndView toCreatePage(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mv = new ModelAndView("page_b2b/addActivity");
		List<B2BStoreGroup> listGroup = storeGroupDao.getB2BStoreGroupList();
		mv.addObject("groups", listGroup);
		mv.addObject("templateId", B2BConstant.TEMPID);
        return mv;
    }
	
	@RequestMapping("/toCreatePageByTemplateId")
    public ModelAndView toCreatePageByTemplateId(HttpServletRequest request, HttpServletResponse response,@RequestParam Long id) {
		ModelAndView mv = new ModelAndView("page_b2b/addActivity");
		List<B2BStoreGroup> listGroup = storeGroupDao.getB2BStoreGroupList();
		mv.addObject("groups", listGroup);
		mv.addObject("templateId", id);
        return mv;
    }
	
	/**
	 * 创建活动
	 * @author tongwenhuan
	 * 2017年3月14日
	 * @param request
	 * @param response
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/createPage",method=POST)
    public String createPage(Page page) {
		pageService.createPage(page);
		return "redirect:listAll.form "; 
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
    public ModelAndView toEditPage(HttpServletRequest request, HttpServletResponse response, @RequestParam Long id , @RequestParam(defaultValue="") String storeCode){
		ModelAndView mv = new ModelAndView("page_b2b/editActivity");
		String userName = UserUtil.getUserId();
        Page page = pageService.queryPageByID(id);
        //判读权限
//        String admin = ","+page.getAdministrator()+",";
//        if(admin.indexOf(","+userName+",") == -1){
//        	mv = new ModelAndView("redirect:listAll.form");
//        	return mv;
//        }
        YXTemplate template = templateService.getYXTemplateById(page.getTemplateId());
        page.setYxTemplate(template);
        try{
        	page.setPagePropertieMap(JSONObject.parseObject(page.getPageProperties()));
        }catch(Exception e){
        	
        }
        Long selectGroupId = 0L;
        List<B2BStoreGroup> listGroup = storeGroupDao.getB2BStoreGroupList();
        StringBuilder storeCodeSb= new StringBuilder();
        
        List<B2BStoreGroup> removeList  = new ArrayList<B2BStoreGroup>();
        for(B2BStoreGroup bg:listGroup){
        	if(!(","+page.getStoreGroupIds()+",").contains(","+bg.getId()+",")){
        		removeList.add(bg);
        		continue;
        	}
        }
        listGroup.removeAll(removeList);
        int j = 0;
        if(listGroup.size()>0){
        	selectGroupId = listGroup.get(0).getId();
        	if(StringUtils.isBlank(storeCode)){
        		storeCode = listGroup.get(0).getStoreId().split(",")[0];
            }
        }
        
        for(B2BStoreGroup bg:listGroup){
        	if((","+bg.getStoreId()+",").contains(","+storeCode+",")){
        		selectGroupId = bg.getId();
        	}
        	if(j>0){
        		storeCodeSb.append(",");
        	}
    		j++;
    		storeCodeSb.append(bg.getStoreId());
        }
        mv.addObject("pageId",id);
        mv.addObject("cmspage",page);
        mv.addObject("storeGroups",listGroup);
        mv.addObject("selectGroupId",selectGroupId);
        mv.addObject("userName", userName);
        mv.addObject("storeCode",storeCode);
        mv.addObject("activityStoreCode",storeCodeSb.toString());
        mv.addObject("colors",PageColorUtil.getPageColor());
        return mv;
    }
	
	@RequestMapping("/toEditPageInfo")
    public ModelAndView toEditPageInfo(HttpServletRequest request, HttpServletResponse response, @RequestParam Long id , @RequestParam(defaultValue="") String storeCode){
		ModelAndView mv = new ModelAndView("page_b2b/editActivityInfo");
		Page page = pageService.queryPageByID(id);
        mv.addObject("cmsPage", page);
        return mv;
    }
	
	@RequestMapping("/savePageModules")
	@LogTrace(msgFomort={"保存活动页(组件:{modules}，属性：{pageProperties})","page","{id}"})
	public void savePageModules(HttpServletRequest request, HttpServletResponse response, @ModelAttribute Page page){
		String result = pageService.savePageModules(page);
		ControllerUtil.writeJson(response, result);
	}
	
	@RequestMapping("/preview")
	public ModelAndView preview(HttpServletRequest request, HttpServletResponse response, @RequestParam Long pageId, @RequestParam String st){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("pageId", pageId);
		param.put("store", st);
		Map<String, String> pageHtmlMap = pageService.getPageHtml(param);
		ModelAndView mv = new ModelAndView("page_b2b/preview");
		mv.addAllObjects(pageHtmlMap);
		return mv;
	}
	
	@RequestMapping("/storeSelect")
	public ModelAndView storeSelect(HttpServletRequest request, HttpServletResponse response){
		Map<String, Object> param = new HashMap<String, Object>();
		Map<String, String> pageHtmlMap = pageService.getPageHtml(param);
		ModelAndView mv = new ModelAndView("page_b2b/storeSelect");
		mv.addAllObjects(pageHtmlMap);
		return mv;
	}
	
	@RequestMapping("/publish")
	@LogTrace(msgFomort={"发布活动页","page","{pageId}"})
	public void publish(HttpServletRequest request, HttpServletResponse response, @RequestParam Long pageId){
		b2bPagePublishService.publish(pageId);
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
	
	@RequestMapping("/activityCopy")
    public ModelAndView activityCopy(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") Long pageId,
            @RequestParam(defaultValue = "0") String areaSEQ, @RequestParam(defaultValue = "0") String clientType) {
        ModelAndView mv = new ModelAndView("page_b2b/activityCopy");
        Page page = pageService.queryPageByID(pageId);
        List<B2BStoreGroup> storeGroup = storeGroupDao.getB2BStoreGroupList();
        mv.addObject("page", page);
        mv.addObject("storeGroup",storeGroup);
        return mv;
    }

    @RequestMapping("/activityCopyOfSave")
    public void activityCopyOfSave(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") String data) throws Exception {
        Page cmsPage = pageService.copyActivityOfSave(data);
        ControllerUtil.writeJson(response, "{\"result\":\"success\",\"pageId\":\"" + cmsPage.getId() + "\"}");
    }
}
