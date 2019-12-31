package com.feiniu.yx.template.controller;

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
import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXModuleTypeService;
import com.feiniu.yx.template.service.YXTemplateService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.UserUtil;

@Controller
@RequestMapping("/yXModuleType")
public class YXModuleTypeController {
   
	@Autowired
	private YXModuleTypeService moduleTypeService;
	@Autowired
	private YXTemplateService templateService;
	
	@RequestMapping("queryModuleType")
	public ModelAndView queryModuleType(@ModelAttribute YXModuleType obj,HttpServletRequest request, HttpServletResponse response){
		ModelAndView mv = new ModelAndView("template/queryModuleType");
		List<YXModuleType> modelTypeList =  moduleTypeService.getYXModuleTypeList(obj);
		mv.addObject("obj", obj);
		mv.addObject("listComponent", modelTypeList);
		return mv;
	}
	
	@RequestMapping("toAddModuleType")
	public ModelAndView toAddModuleType(HttpServletRequest request, HttpServletResponse response){
		ModelAndView  mv  = new ModelAndView("template/addModuleType");
		return mv;
	}
	
	@RequestMapping("addOrSaveModelType")
	public void addOrSaveModelType(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String data){
		 YXModuleType module = JSONObject.parseObject(data, YXModuleType.class);
		  String username = UserUtil.getUserId();
		  module.setCreateId(username);
		  module.setUpdateId(username);
		  moduleTypeService.insertYXModuleType(module);
		  JSONObject result = new JSONObject();
		  result.put("code", 100);
		  result.put("msg", "success");
		  ControllerUtil.writeJson(response, result.toJSONString());
	}
	
	@RequestMapping("toEditModuleType")
	public ModelAndView toEditModuleType(HttpServletRequest request, HttpServletResponse response,
	@RequestParam Long id){
		ModelAndView  mv  = new ModelAndView("template/editModuleType");
		YXModuleType moduleType = moduleTypeService.getYXModuleTypeById(id);
		mv.addObject("com", moduleType);
		return mv;
	}
	
	
	@RequestMapping("editOrSaveModuleType")
	public void editOrSaveModuleType(HttpServletRequest request, HttpServletResponse response,
	@RequestParam String data){
		YXModuleType module = JSONObject.parseObject(data, YXModuleType.class);
		  String username = UserUtil.getUserId();
		  module.setUpdateId(username);
		  module.setUpdateTime(new Date());
		  moduleTypeService.updateYXModuleType(module);
		  JSONObject result = new JSONObject();
		  result.put("code", 100);
		  result.put("msg", "success");
		  ControllerUtil.writeJson(response, result.toJSONString());
	}
	
	@RequestMapping("getModuleTypeList")
	public void getModuleTypeList(HttpServletRequest request, HttpServletResponse response,@RequestParam Long yxTemplateId){
		YXTemplate template = templateService.getYXTemplateById(yxTemplateId);
		List<YXModuleType> moduleTypeListJson = moduleTypeService.getModuleTypeListByIds(template.getModuleTypes());
		ControllerUtil.writeJson(response, JSONObject.toJSONString(moduleTypeListJson));
	}
	
	
}
