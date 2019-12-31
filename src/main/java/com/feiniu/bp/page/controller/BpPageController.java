package com.feiniu.bp.page.controller;

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
import com.feiniu.bp.page.entity.BpPage;
import com.feiniu.bp.page.service.BpPageService;
import com.feiniu.yx.util.ControllerUtil;

@Controller
@RequestMapping("bpPage")
public class BpPageController {

	@Autowired
	private BpPageService pageService;
	
	@RequestMapping("/pageView")
	public ModelAndView pageView(HttpServletRequest request, HttpServletResponse response) {
		List<BpPage> pageList = pageService.getBpPageList(new BpPage());
		ModelAndView mv = new ModelAndView("pointPage/page");
		mv.addObject("pageList", pageList);
		return mv;
	}
	
	@RequestMapping("/getPbPageList")
	public void getPbPageList(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute BpPage page){
		List<BpPage> pageList = pageService.getBpPageList(page);
		JSONObject result = new JSONObject();
		result.put("code", 100);
		result.put("msg", "success");
		result.put("pageList", pageList);
		ControllerUtil.writeJson(response, result.toJSONString());
	}
	
	@RequestMapping("/checkedPageId")
	public void checkedPageId(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="0") String data){
		BpPage page = JSONObject.parseObject(data,BpPage.class);
		page.setId(JSONObject.parseObject(data).getLong("dataIndex"));
		String result = pageService.checkBpPageByPageId(page);
		ControllerUtil.writeJson(response, result);
	}
	
	@RequestMapping("insertPage")
	public void insertPage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="0") String data){
		BpPage page = JSONObject.parseObject(data,BpPage.class);
		long id = pageService.insertBpPage(page);
		JSONObject result = new JSONObject();
		result.put("code", 100);
		result.put("msg", "success");
		result.put("id", id);
		ControllerUtil.writeJson(response,result.toJSONString());
	}
	
	@RequestMapping("/getBpPageById")
	public void getBpPageById(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="0") Long id){
		BpPage page = pageService.getBpPageById(id);
		ControllerUtil.writeJson(response, JSONObject.toJSONString(page));
	}
	
	@RequestMapping("/updatePage")
	public void updatePage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="0") String data){
		BpPage page = JSONObject.parseObject(data,BpPage.class);
		Long id = pageService.updateBpPage(page);
		JSONObject result = new JSONObject();
		result.put("code", 100);
		result.put("msg", "success");
		result.put("id", id);
		ControllerUtil.writeJson(response,result.toJSONString());
	}
	
}
