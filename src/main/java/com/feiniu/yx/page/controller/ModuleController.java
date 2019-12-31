package com.feiniu.yx.page.controller;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.interceptor.LogTrace;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.service.ModuleDataService;
import com.feiniu.yx.page.service.ModuleService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/module")
public class ModuleController {
	
	@Autowired
	private ModuleService moduleService;
	
	@Autowired
	private ModuleDataService moduleDataService;
	
	@Autowired
	private YXStoreService yXStoreService;
	
	/**
	 * 查询模块数据
	 * @author tongwenhuan
	 * 2017年2月24日
	 * @param response
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping("findModuleDate")
	public void findModuleDate(HttpServletResponse response,@RequestParam Long id,@RequestParam String storeCode,@RequestParam String previewTime){
		ControllerUtil.writeJson(response, moduleDataService.findModuledData(id,storeCode,previewTime));
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
	public void toEditModule(HttpServletResponse response,@RequestParam Long id,@RequestParam String storeCodes,@RequestParam String storeCode){
		JSONObject mjo = moduleService.findModule(id,storeCode);
		List<YXStore> storeList = yXStoreService.getStoreByCodes(storeCodes);
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
	public void updateModule(HttpServletRequest request, HttpServletResponse response,@ModelAttribute Module module,@RequestParam String previewTime) throws Exception{
		moduleService.updateModule(module);
		ControllerUtil.writeJson(response, moduleDataService.findModuledData(module.getId(),module.getStoreCode(),previewTime));
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
			@RequestParam Long moduleType, @RequestParam String storeCode,@RequestParam String previewTime){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("pageId", pageId);
		param.put("preModuleId", preModuleId);
		param.put("moduleType", moduleType);
		param.put("storeCode", storeCode);
		String moduleJson = moduleService.addModule(param,previewTime);
		ControllerUtil.writeJson(response, moduleJson);
	}
	
	@RequestMapping("validateModule")
	public void validateModule(HttpServletRequest request, HttpServletResponse response,@ModelAttribute Module module) throws Exception{
		boolean flag =moduleService.validateModule(module);
		ControllerUtil.writeJsonString(response, flag?"success":"fail");

	}

	/**
	 * 首页优惠券删除档期
	 * @param request
	 * @param response
	 * @param moduleId
	 * @param couponCenterTabId
	 */
	@RequestMapping("delModuleTab")
	public void delModuleTab(HttpServletRequest request, HttpServletResponse response,@RequestParam Long moduleId,@RequestParam Integer couponCenterTabId){
		moduleService.updateModuleByTabId(moduleId,couponCenterTabId);
		JSONObject mjo = new JSONObject();
		mjo.put("state", "1");
		ControllerUtil.writeJson(response, mjo.toJSONString());

	}
}
