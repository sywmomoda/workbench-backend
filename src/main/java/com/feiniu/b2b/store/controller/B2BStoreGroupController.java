package com.feiniu.b2b.store.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.entity.B2BStoreGroup;
import com.feiniu.b2b.store.service.B2BStoreGroupService;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.b2b.store.service.B2BSubAreaService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.UserUtil;

@Controller
@RequestMapping("/b2bStoreGroup")
public class B2BStoreGroupController {

	@Autowired
	private B2BStoreService storeService;
	
	@Autowired
	private B2BStoreGroupService storeGroupService;
	
	@Autowired
	private B2BSubAreaService subAreaService;
	
	@RequestMapping("queryStoreGroup")	
	public ModelAndView queryStoreGroup(HttpServletRequest request, HttpServletResponse response){
		ModelAndView mv = new ModelAndView("b2bstore/queryStoreGroup");
		return mv;
	}
	
	@RequestMapping("findAllStoreGroup")
	public void findAllStoreGroup(HttpServletRequest request, HttpServletResponse response){
		String json = storeGroupService.getTreeJsonStoreGroup("");
		ControllerUtil.writeJson(response, json);
	}
	
	@RequestMapping("addStoreGroup")
	public  ModelAndView addStoreGroup(HttpServletRequest request, HttpServletResponse response){
		ModelAndView mv = new ModelAndView("b2bstore/addStoreGroup");
		List<B2BStore> list = storeService.getB2BStoreList();
		mv.addObject("list", list);
		return mv;
	}
	
	@RequestMapping("toUpdateStoreGroup")
	public ModelAndView toUpdateStoreGroup(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="0") Long id){
		ModelAndView mv = new ModelAndView("b2bstore/updateStoreGroup");
		B2BStoreGroup group = storeGroupService.getStoreGroup(id);
		List<B2BStore> list = storeService.getB2BStoreList();
		String stores = group.getStoreId();
		if(StringUtils.isNotBlank(stores)) {
			for(B2BStore s : list) {
				if((","+stores+",").contains(","+s.getCode()+",")){
					s.setChecked(1);
				}
			}
		}
		mv.addObject("group", group);
		mv.addObject("list", list);
		return mv;
	}
	
	@RequestMapping("updateStoreGroup")
	public void updateStoreGroup(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="0") String data){
		B2BStoreGroup group = JSONObject.parseObject(data, B2BStoreGroup.class);
		String username = UserUtil.getUserId();
		group.setUpdateId(username);
        group.setUpdateTime(new Date());
        storeGroupService.updateStoreGroup(group);
        JSONObject result = new JSONObject();
		result.put("code", 100);
		result.put("msg", "success");
        ControllerUtil.writeJson(response, result.toJSONString());
	}
	
	@RequestMapping("saveStoreGroup")
	public void saveStoreGroup(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "0") String data){
		B2BStoreGroup group = JSONObject.parseObject(data, B2BStoreGroup.class);
		String username = UserUtil.getUserId();
		group.setCreateId(username);
        group.setUpdateId(username);
        group.setUpdateTime(new Date());
		storeGroupService.insertStoreGroup(group);
		JSONObject result = new JSONObject();
		result.put("code", 100);
		result.put("msg", "success");
		ControllerUtil.writeJson(response, result.toJSONString());
	}
	
	@RequestMapping("/getStoreByGroupId")
	public void getStoreByGroupId(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "0") Long groupId){
		B2BStoreGroup group=storeGroupService.getStoreGroup(groupId);
		List<B2BStore> list=storeGroupService.getActivityStoreList(group);
		JSONObject jObject=new JSONObject();
		jObject.put("stores", list);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}
	
	@RequestMapping("deleteStoreGroupById")
	public void deleteStoreGroupById(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "0") Long id){
		storeGroupService.deleteB2BStoreGroupById(id);
		JSONObject result = new JSONObject();
		result.put("code", 100);
		result.put("msg", "success");
		ControllerUtil.writeJson(response, result.toJSONString());
	}
	
	@RequestMapping("/getSubAreaList")
	public void getSubAreaList(HttpServletResponse response,@RequestParam String pgSeq) {
		List<JSONObject> list = subAreaService.getSubAreaList(pgSeq);
		ControllerUtil.writeJson(response,JSONObject.toJSONString(list));
	}
}
