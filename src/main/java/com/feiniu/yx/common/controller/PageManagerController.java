package com.feiniu.yx.common.controller;

import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.dao.ModuleDao;
import com.feiniu.yx.page.dao.PageOnlineDao;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.PageService;
import com.feiniu.yx.util.ControllerUtil;

@Controller
@RequestMapping("/pageManager")
public class PageManagerController {
	@Autowired
	private PageService pageService;
	
	@Autowired
	private ModuleDao moduleDao;
	
	@Autowired
	private PageOnlineDao pageOnlineDao;
	
	@RequestMapping("/queryPages")
	public ModelAndView queryPages(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute Page page){
		 ModelAndView mv = new ModelAndView("system/page");
		 List<Page> list = pageService.queryActivityList(page, 0);
		 mv.addObject("list", list);
		 String name = page.getName();
		 try {
			   page.setName(URLDecoder.decode(name,"UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
				page.setName("");
			}
		 mv.addObject("obj", page);
		 return mv;
	}
	
	@RequestMapping("/toUpdatePage")
	public ModelAndView toUpdatePage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Long id){
		ModelAndView mv = new ModelAndView("system/eidtPage");
		Page page = pageService.queryPageByID(id);
		mv.addObject("page", page);
		return mv;
	}
	
	@RequestMapping("toUpdateOfSave")
	public void toUpdateOfSave(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute Page page){
		pageService.updateCMSPage(page);
		JSONObject object = new JSONObject();
		object.put("result", "success");
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	
	
	@RequestMapping("deletePage")
	public void deletePage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Long id){
		pageService.deletePage(id);
		JSONObject object = new JSONObject();
		object.put("result", "success");
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	
	
	@RequestMapping("/queryModules")
	public ModelAndView queryModules(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute Module module){	
		ModelAndView mv = new ModelAndView("system/modules");
		List<Module> list = moduleDao.getModuleList(module);
		Long pageId = module.getPageId();
		if(pageId !=null && pageId == 0){
			module.setPageId(null);
		}
		mv.addObject("list", list);
		mv.addObject("obj", module);
		return mv;
	}
	
	@RequestMapping("toUpdateModule")
	public ModelAndView toUpdateModule(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Long id){
		ModelAndView mv = new ModelAndView("system/editModule");
		Module module = moduleDao.queryModuleByID(id);
		mv.addObject("module", module);
		return mv;
	}
	
	
	@RequestMapping("toSaveModule")
	public void toSaveModule(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute Module module){
		module.setUpdateTime(new Date());
   		moduleDao.updateModule(module);
   		JSONObject object = new JSONObject();
		object.put("result", "success");
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	
	@RequestMapping("/queryOnlinePages")
	public ModelAndView queryOnlinePages(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute Page page){	
		 ModelAndView mv = new ModelAndView("system/pageOnline");
		 List<Page> list = pageOnlineDao.getOnLinePageList(page);
		 mv.addObject("list", list);
		 mv.addObject("obj", page);
		 return mv;
	}
	
	
	@RequestMapping("/toUpdateOnlinePage")
	public ModelAndView toUpdateOnlinePage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Long id){
		ModelAndView mv = new ModelAndView("system/eidtOnlinePage");
		Page page = pageOnlineDao.findOne(id);
		mv.addObject("page", page);
		return mv;
	}
	
	@RequestMapping("toUpdateOfOnlineSave")
	public void toUpdateOfOnlineSave(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute Page page){
		pageOnlineDao.updateOne(page);
		JSONObject object = new JSONObject();
		object.put("result", "success");
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	
	@RequestMapping("deleteOnlinePage")
	public void deleteOnlinePage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Long id){
		pageOnlineDao.deleteOnlinePage(id);
		JSONObject object = new JSONObject();
		object.put("result", "success");
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	
	
}
