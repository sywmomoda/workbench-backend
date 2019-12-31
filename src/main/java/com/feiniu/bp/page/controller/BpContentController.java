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
import com.feiniu.bp.page.entity.BpContent;
import com.feiniu.bp.page.service.BpContentService;
import com.feiniu.yx.util.ControllerUtil;

@RequestMapping("bpContent")
@Controller
public class BpContentController {
	@Autowired
	private BpContentService contentService;
	@RequestMapping("/queryContent")
	public ModelAndView queryContent(HttpServletRequest request,HttpServletResponse response,
			@ModelAttribute BpContent content) {
		List<BpContent> list = contentService.getBpContentList(content);
		ModelAndView mv = new ModelAndView("pointPage/pageContent");
		mv.addObject("contentList", list);
		mv.addObject("content", content);
		return mv;
	}
	
	@RequestMapping("/insertBpContent")
	public void insertBpContent(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue="0") String data){
		BpContent content = JSONObject.parseObject(data,BpContent.class);
		contentService.insertBpContent(content);
		ControllerUtil.writeJson(response);
	}
	
	@RequestMapping("/getBpContentList")
	public void getBpContentList(HttpServletRequest request,HttpServletResponse response,
			@ModelAttribute BpContent content){
		List<BpContent> list = contentService.getBpContentList(content);
		JSONObject result = new JSONObject();
		result.put("code", 100);
		result.put("msg", "seccess");
		result.put("contentList", list);
		ControllerUtil.writeJson(response, result.toJSONString());
	}
	
	@RequestMapping("getBpContentById")
	public void getBpContentById(HttpServletRequest request,HttpServletResponse response,
			@RequestParam Long id){
		BpContent content = contentService.getBpContentById(id);
		JSONObject result = new JSONObject();
		result.put("code", 100);
		result.put("msg", "seccess");
		result.put("content", content);
		ControllerUtil.writeJson(response, result.toJSONString());
	}
	
	@RequestMapping("/updateBpContent")
	public void updateBpContent(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue="0") String data){
		BpContent content = JSONObject.parseObject(data,BpContent.class);
		contentService.updateBpContent(content);
		ControllerUtil.writeJson(response);
	}
	
	@RequestMapping("/updateBpContentStatus")
	public void updateBpContentStatus(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue="0") String data){
		BpContent content = JSONObject.parseObject(data,BpContent.class);
		contentService.updateBpContentStatus(content);
		ControllerUtil.writeJson(response);
	}
	
	@RequestMapping("/checkPageColRepetition")
	public void checkPageColRepetition(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue="0") String data){
		BpContent content = JSONObject.parseObject(data,BpContent.class);
		content.setId(JSONObject.parseObject(data).getLong("dataIndex"));
		String result = contentService.checkPagColRepetition(content);
		ControllerUtil.writeJson(response, result);
	}
	
}
