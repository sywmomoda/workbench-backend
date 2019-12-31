package com.feiniu.yx.page.controller;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.dao.PageDao;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.ModuleService;
import com.feiniu.yx.page.service.PageService;
import com.feiniu.yx.pool.service.SyncPoolService;
import com.feiniu.yx.util.ControllerUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Controller
@RequestMapping("/yxTools")
public class YXToolsController {
	@Autowired
	private PageService cp;
	@Autowired
	private PageDao pageDao;
	@Autowired
	private ModuleService ms;
	
	@Autowired
	private SyncPoolService syncService;

	@RequestMapping("/queryPage")
	public ModelAndView queryPage(HttpServletRequest request,
			HttpServletResponse response, @ModelAttribute Page obj) {

		List<Page> list = cp.queryPageForLinkSelect(obj);
		ModelAndView mv = new ModelAndView("cmsactivity/queryActivityPage");
		mv.addObject("listPage", list);
		mv.addObject("obj", obj);
		return mv;
	}
	
	
	@RequestMapping("/toUpdatePage")
	public ModelAndView toUpdatePage(HttpServletRequest request,
			HttpServletResponse response, @RequestParam Long id) {		
		ModelAndView mv = new ModelAndView("cmsactivity/eidtActivityPage");
		Page page = cp.queryPageByID(id);
		mv.addObject("page", page);
		return mv;
	}
	
	

	@RequestMapping("/updatePage")
	public void toUpdatePage(HttpServletRequest request,
			HttpServletResponse response, @ModelAttribute Page page) {		
		Page pagePris = cp.queryPageByID(page.getId());
		String pw = request.getParameter("pw");
		JSONObject mjo = new JSONObject();
		if(page.getId()==null && "cmsAreaOp".equals(pw)){
			pageDao.updatePageArea(page);
			mjo.put("result", "110");
		}else{
			pageDao.updateCMSPage(pagePris);
			mjo.put("result", "1");
		}
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	
	@RequestMapping("/queryModules")
	public ModelAndView queryModules(HttpServletRequest request,
			HttpServletResponse response,@ModelAttribute Module obj) {
		ModelAndView mv = new ModelAndView("cmsactivity/queryActivityModules");
		String pageId = request.getParameter("pageId");
		String moduleId = request.getParameter("moduleId");
		if(StringUtils.isNoneBlank(pageId)){
			Page page = cp.queryPageByID(Long.parseLong(pageId));
			List<Module> list = ms.getCMSModuleArrayByModuleIds(page.getModules());
			mv.addObject("listModules", list);
		}
		mv.addObject("moduleId", moduleId);
		mv.addObject("pageId", pageId);
		mv.addObject("obj", obj);
		return mv;
	}
		
	@RequestMapping("/queryModuleById")
	public ModelAndView queryModuleById(HttpServletRequest request,
			HttpServletResponse response, @RequestParam Long id) {
		ModelAndView mv = new ModelAndView("cmsactivity/editActivityModule");
		Module module = ms.getCMSModuleArrayByModuleIds(id+"").get(0);
		mv.addObject("cmsModule", module);
		return mv;
	}
	
	@RequestMapping("/editOfSaveModule")
	public void editOfSaveModule(@ModelAttribute Module module,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		   ms.updateModule(module);
		   JSONObject mjo = new JSONObject();
			mjo.put("result", "1");
			ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	
	
	
	@RequestMapping("/toAddModule")
	public ModelAndView toAddModule(HttpServletRequest request, HttpServletResponse response)
	{
		ModelAndView mv = new ModelAndView("cmsactivity/addActivityModule");
		return mv;
	}
	
	@RequestMapping("/manualSyncPool")
	public void manualSyncPool(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Long poolId){
		syncService.manualSyncPool(poolId);
		JSONObject result = new JSONObject();
		result.put("msg","success");
		ControllerUtil.writeJson(response, result.toJSONString());
	}
	
	
}
