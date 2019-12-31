package com.feiniu.gwms.page.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.gwms.common.GwmsConstant;
import com.feiniu.gwms.page.service.GwmsPageService;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.PagePublishService;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXTemplateService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.UserUtil;

@Controller
@RequestMapping("/pageGwms")
public class PageGwmsController {
	
	@Autowired
	private GwmsPageService gwmsService;
	
	@Autowired
	private PagePublishService pagePublishService;
	
	@Autowired
	private YXTemplateService templateService;

	@RequestMapping("/listAll")
    public ModelAndView listAll(HttpServletRequest request, HttpServletResponse response, Page page) {
        ModelAndView mv = new ModelAndView("page_gwms/activityList");
        String local = request.getParameter("local");
        String userName = UserUtil.getGWMSUserId(request);
        if("1".equals(local)){
        	page.setCreateId(userName);
        }
        page.setType(4);//官微秒杀
        List<Page> list = gwmsService.queryActivityList(page, 0);
        mv.addObject("list", list);
        mv.addObject("page", page);
        mv.addObject("local", local);
        mv.addObject("userName", userName);
        mv.addObject("yxurl", GwmsConstant.GWMSURL);
        return mv;
    }
	
	@RequestMapping("/toCreatePage")
    public ModelAndView toCreatePage(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mv = new ModelAndView("page_gwms/addActivity");
		mv.addObject("templateId", GwmsConstant.TEMPID);
        return mv;
    }
	
	@RequestMapping("/toCreatePageByTemplateId")
    public ModelAndView toCreatePageByTemplateId(HttpServletRequest request, HttpServletResponse response,@RequestParam Long id) {
		ModelAndView mv = new ModelAndView("page_gwms/addActivity");
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
		gwmsService.createPage(page);
		return "redirect:listAll.form "; 
    }
	
	@RequestMapping(value="/updatePage")
	//@LogTrace(msgFomort={"更新活动属性(标题:{name},描述:{description},权限:{administrator},开始时间:{activityBeginTime},结束时间:{activityEndTime})","page","{id}"})
    public void updatePage(HttpServletRequest request, HttpServletResponse response, @ModelAttribute Page page){
		gwmsService.updateCMSPageInfo(page);
		JSONObject mjo = new JSONObject();
		mjo.put("state", "1");
		ControllerUtil.writeJson(response, mjo.toJSONString());
    }
	
	@RequestMapping("/toEditPage")
    public ModelAndView toEditPage(HttpServletRequest request, HttpServletResponse response, @RequestParam Long id , @RequestParam(defaultValue="") String storeCode){
		ModelAndView mv = new ModelAndView("page_gwms/editActivity");
		String userName = UserUtil.getGWMSUserId(request);
        Page page = gwmsService.queryPageByID(id);
        YXTemplate template = templateService.getYXTemplateById(page.getTemplateId());
        page.setYxTemplate(template);
        page.setPagePropertieMap(JSONObject.parseObject(page.getPageProperties()));
        mv.addObject("pageId",id);
        mv.addObject("cmspage",page);
        mv.addObject("userName", userName);
        mv.addObject("storeCode","1");
        return mv;
    }
	
	@RequestMapping("/toEditPageInfo")
    public ModelAndView toEditPageInfo(HttpServletRequest request, HttpServletResponse response, @RequestParam Long id , @RequestParam(defaultValue="") String storeCode){
		ModelAndView mv = new ModelAndView("page_gwms/editActivityInfo");
		Page page = gwmsService.queryPageByID(id);
        mv.addObject("cmsPage", page);
        return mv;
    }
	
	@RequestMapping("/savePageModules")
	//@LogTrace(msgFomort={"保存活动页(组件:{modules}，属性：{pageProperties})","page","{id}"})
	public void savePageModules(HttpServletRequest request, HttpServletResponse response, @ModelAttribute Page page){
		String result = gwmsService.savePageModules(page);
		ControllerUtil.writeJson(response, result);
	}
	
	@RequestMapping("/preview")
	public ModelAndView preview(HttpServletRequest request, HttpServletResponse response, @RequestParam Long pageId, @RequestParam String st){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("pageId", pageId);
		param.put("store", "1");
		Map<String, String> pageHtmlMap = gwmsService.getPageHtml(param);
		ModelAndView mv = new ModelAndView("page_gwms/preview");
		mv.addAllObjects(pageHtmlMap);
		return mv;
	}
	
	@RequestMapping("/publish")
	//@LogTrace(msgFomort={"发布活动页","page","{pageId}"})
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
		gwmsService.pausePage(pageId,2);
		ControllerUtil.writeJson(response);
	}
}
