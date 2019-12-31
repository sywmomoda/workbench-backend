package com.feiniu.b2b.page.controller;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.page.service.B2BModuleProperPlusService;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.common.interceptor.LogTrace;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.util.ControllerUtil;

@Controller
@RequestMapping("/b2bModuleProperPlus")
public class B2BModuleProperPlusController {
	
	
	@Autowired
	private B2BModuleProperPlusService moduleProperPlusService;
	
	@Autowired
	private B2BStoreService storeService;

	@RequestMapping("storeSelectModuleProper")
	public void storeSelectModuleProper(HttpServletRequest request, HttpServletResponse response, 
			String checkedCodes,String groupIds,Long moduleId){
		String json = moduleProperPlusService.getTreeSelectStoreGroupByType(checkedCodes,groupIds,moduleId);
		ControllerUtil.writeJson(response, json);
	}
	
	@RequestMapping("addOrUpdateModuleProper")
	@LogTrace(msgFomort={"模块附加属性添加或更新:{properData}，门店：{storeIds}","module","{moduleId}"})
	public void addOrUpdateModuleProper(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam Long moduleId, @RequestParam String storeIds, @RequestParam String properData,
			@RequestParam(defaultValue="0")String saveType) throws Exception{
		List<B2BStore> stores = storeService.getB2BStoreByIds(storeIds);
		String failedStoreName = moduleProperPlusService.addOrUpdateModuleProper(moduleId,stores,properData,saveType);
		JSONObject mjo = new JSONObject();
		mjo.put("state", "1");
		mjo.put("info", failedStoreName);
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	/**
	 * 编辑数据获取
	 * @param request
	 * @param response
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping("getModuleProper")
	public void getModuleProper(HttpServletResponse response,@RequestParam Long id,@RequestParam Long moduleId){
		B2BStore ys = storeService.getB2BStoreById(id);
		ModuleProperPlus mpp = new ModuleProperPlus(); 
		if(ys!=null){
			Map<String, ModuleProperPlus> mmpMap = moduleProperPlusService.queryModuleProperMapByModuleId(moduleId);
			mpp = mmpMap.get(ys.getCode());
		}
		if(mpp!=null){
			ControllerUtil.writeJson(response, JSONObject.toJSONString(mpp));
		}else{
			mpp = new ModuleProperPlus(); 
			ControllerUtil.writeJson(response, JSONObject.toJSONString(mpp));
		}
	}
	
	@RequestMapping("deleteModuleProper")
	@LogTrace(msgFomort={"模块附加属性删除:{storeIds}","module","{moduleId}"})
	public void deleteModuleProper(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Long moduleId, @RequestParam String storeIds) throws Exception{
		List<B2BStore> stores = storeService.getB2BStoreByIds(storeIds);
		moduleProperPlusService.deleteModuleProper(moduleId,stores);
		JSONObject mjo = new JSONObject();
		mjo.put("state", "1");
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
}
