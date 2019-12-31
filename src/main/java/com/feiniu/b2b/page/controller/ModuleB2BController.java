package com.feiniu.b2b.page.controller;

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

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.page.service.B2BModuleDataService;
import com.feiniu.b2b.page.service.B2BModuleService;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.common.interceptor.LogTrace;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.ControllerUtil;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@Controller
@RequestMapping("/moduleb2b")
public class ModuleB2BController {
	
	@Autowired
	private B2BModuleService moduleService;
	
	@Autowired
	private B2BModuleDataService moduleDataService;
	
	@Autowired
	private B2BStoreService b2bStoreService;
	
	/**
	 * 查询模块数据
	 * @author tongwenhuan
	 * 2017年2月24日
	 * @param request
	 * @param response
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping("findModuleDate")
	public void findModuleDate(HttpServletResponse response,@RequestParam Long id,@RequestParam String storeCode){
		ControllerUtil.writeJson(response, moduleDataService.findModuledData(id,storeCode));
	}
	
	/**
	 * 打开模块编辑页面
	 * @author tongwenhuan
	 * 2017年2月24日
	 * @param request
	 * @param response
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping("toEditModule")
	public void toEditModule(HttpServletResponse response,@RequestParam Long id,@RequestParam String storeCodes){
		JSONObject mjo = moduleService.findModule(id);
		List<B2BStore> storeList = b2bStoreService.getStoreByCodes(storeCodes);
		mjo.put("storeList", storeList);
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
	@LogTrace(msgFomort={"模块更新:{moduleProperties}","module","{id}"})
	public void updateModule(HttpServletRequest request, HttpServletResponse response,@ModelAttribute Module module) throws Exception{
		moduleService.updateModule(module);
		ControllerUtil.writeJson(response, moduleDataService.findModuledData(module.getId(),module.getStoreCode()));
	}
	
	@RequestMapping("updateModulePermission")
	@LogTrace(msgFomort={"模块权限更新:{moduleProperties}","module","{id}"})
	public void updateModulePermission(HttpServletRequest request, HttpServletResponse response,@ModelAttribute Module module) throws Exception{
		moduleService.updateModulePermission(module);
		JSONObject mjo = new JSONObject();
		mjo.put("state", "1");
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	@RequestMapping(value="/addModuleByMT",method=POST)
	@LogTrace(msgFomort={"添加模块(模块类型:{moduleType}，前模块ID:{preModuleId}，门店：{storeCode})","page","{pageId}"})
	public void addModuleByMT(HttpServletResponse response,@RequestParam Long pageId, @RequestParam Long preModuleId,
			@RequestParam Long moduleType, @RequestParam String storeCode){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("pageId", pageId);
		param.put("preModuleId", preModuleId);
		param.put("moduleType", moduleType);
		param.put("storeCode", storeCode);
		String moduleJson = moduleService.addModule(param);
		ControllerUtil.writeJson(response, moduleJson);
	}
	
	@RequestMapping("validateModule")
	public void validateModule(HttpServletRequest request, HttpServletResponse response,@ModelAttribute Module module) throws Exception{
		boolean flag =moduleService.validateModule(module);
		ControllerUtil.writeJsonString(response, flag?"success":"fail");

	}
}
