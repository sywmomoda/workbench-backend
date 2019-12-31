package com.feiniu.gwms.page.controller;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.gwms.page.service.GwmsModuleService;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.service.ModuleDataService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/moduleGwms")
public class ModuleGwmsController {
	
	@Autowired
	private GwmsModuleService moduleService;
	
	@Autowired
	private ModuleDataService moduleDataService;
	
	/**
	 * 查询模块数据
	 * @author tongwenhuan
	 * 2017年2月24日
	 * @param response
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping("findModuleDate")
	public void findModuleDate(HttpServletResponse response,@RequestParam Long id,@RequestParam String storeCode){
		ControllerUtil.writeJson(response, moduleDataService.findModuledData(id,storeCode,""));
	}
	
	/**
	 * 打开模块编辑页面
	 * @author tongwenhuan
	 * 2017年2月24日
	 * @param response
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping("toEditModule")
	public void toEditModule(HttpServletResponse response,@RequestParam Long id,@RequestParam String storeCodes){
		JSONObject mjo = moduleService.findModule(id);
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	/**
	 * 编辑模块
	 * @author tongwenhuan
	 * 2017年2月24日
	 * @param request
	 * @param response
	 * @param module
	 * @throws Exception
	 */
	@RequestMapping("updateModule")
//	@LogTrace(msgFomort={"模块更新:{moduleProperties}","module","{id}"})
	public void updateModule(HttpServletRequest request, HttpServletResponse response,@ModelAttribute Module module) throws Exception{
		module.setUpdateId(UserUtil.getGWMSUserId(request));
		moduleService.updateModule(module);
		ControllerUtil.writeJson(response, moduleDataService.findModuledData(module.getId(),module.getStoreCode(),""));
	}
	
	@RequestMapping("updateModulePermission")
//	@LogTrace(msgFomort={"模块权限更新:{moduleProperties}","module","{id}"})
	public void updateModulePermission(HttpServletRequest request, HttpServletResponse response,@ModelAttribute Module module) throws Exception{
		moduleService.updateModulePermission(module);
		JSONObject mjo = new JSONObject();
		mjo.put("state", "1");
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	@RequestMapping(value="/addModuleByMT",method=POST)
//	@LogTrace(msgFomort={"添加模块(模块类型:{moduleType}，前模块ID:{preModuleId}，门店：{storeCode})","page","{pageId}"})
	public void addModuleByMT(HttpServletRequest request, HttpServletResponse response,@RequestParam Long pageId, @RequestParam Long preModuleId,
			@RequestParam Long moduleType, @RequestParam String storeCode){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("pageId", pageId);
		param.put("preModuleId", preModuleId);
		param.put("moduleType", moduleType);
		param.put("storeCode", storeCode);
		param.put("userName", UserUtil.getGWMSUserId(request));
		String moduleJson = moduleService.addModule(param);
		ControllerUtil.writeJson(response, moduleJson);
	}
	
	@RequestMapping("validateModule")
	public void validateModule(HttpServletRequest request, HttpServletResponse response,@ModelAttribute Module module) throws Exception{
		boolean flag =moduleService.validateModule(module);
		ControllerUtil.writeJsonString(response, flag?"success":"fail");

	}
}
