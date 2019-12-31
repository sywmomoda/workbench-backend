package com.feiniu.yx.template.controller;

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
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXTemplateService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.UserUtil;

@Controller
@RequestMapping("/yXTemplate")
public class YXTemplateController  {
	
	@Autowired
	private YXTemplateService templateService;
	
   @RequestMapping("queryTemplate")
   public ModelAndView queryTemplate(@ModelAttribute YXTemplate obj,HttpServletRequest request, HttpServletResponse response){
	    ModelAndView mv = new ModelAndView("template/queryTemplate");
	    List<YXTemplate> listTemplate = templateService.getYXTemplateList(obj);
	    mv.addObject("listTemplate", listTemplate);
	    mv.addObject("obj", obj);
	    return mv;
   }
   
   @RequestMapping("toAddTemplate")
   public ModelAndView toAddTemplate(HttpServletRequest request, HttpServletResponse response){
	   ModelAndView mv = new ModelAndView("template/addTemplate");
	   return mv;
   }
   
   @RequestMapping("addOrSaveTemplate")
   public void addOrSaveTemplate(HttpServletRequest request, HttpServletResponse response,
		   YXTemplate template){
	   String username = UserUtil.getUserId();
	   template.setCreateId(username);
	   template.setUpdateId(username);
	   templateService.insertYXTemplate(template);
	   ControllerUtil.writeJson(response);
   }
   
   @RequestMapping("toEditTemplate")
   public ModelAndView  toEditTemplate(HttpServletRequest request, HttpServletResponse response,
		   @RequestParam(defaultValue="0") Long id){
	   ModelAndView mv = new ModelAndView("template/editTemplate");
	   YXTemplate template = templateService.getYXTemplateById(id);
	   mv.addObject("tem", template);
	   return mv;
   }
   
   @RequestMapping("editOrSaveTemplate")
   public void editOrSaveTemplate(HttpServletRequest request, HttpServletResponse response,
		   YXTemplate template){
	   String username = UserUtil.getUserId();
	   template.setCreateId(username);
	   template.setUpdateId(username);
	   templateService.updateYXTemplate(template);
	   JSONObject result = new JSONObject();
	   result.put("code", 100);
	   result.put("msg", "success");
	   ControllerUtil.writeJson(response, result.toJSONString());
   }
   
}
