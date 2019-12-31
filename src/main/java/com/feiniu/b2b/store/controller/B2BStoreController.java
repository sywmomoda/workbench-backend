package com.feiniu.b2b.store.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.entity.B2BStoreGroup;
import com.feiniu.b2b.store.service.B2BStoreGroupService;

/**
 * b2b 门店
 * 
 * @author yehui
 * 
 */
@Controller
@RequestMapping("/b2bStore")
public class B2BStoreController {

	@Autowired
	private B2BStoreService storeService;

	@Autowired
	private B2BStoreGroupService storeGroupService;
	
	@RequestMapping("/synchroRemoteStore")
	public void synchroRemoteStore(HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject object =  storeService.synchroRemoteStoreInfo();
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	
	@RequestMapping("storeSelect")
	public void storeSelect(HttpServletRequest request, HttpServletResponse response, String checkedIds,String groupIds){
		String json = storeGroupService.getTreeSelectStoreGroup(checkedIds,groupIds);
		ControllerUtil.writeJson(response, json);
	}
	
	@RequestMapping("findStoreByCode")
	public void findStoreByCode(HttpServletRequest request, HttpServletResponse response, String codes){
		if(StringUtils.isBlank(codes)){
			return;
		}
		List<B2BStore> list = storeService.getStoreByCodes(codes);
		JSONObject jObject=new JSONObject();
		jObject.put("stores", list);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}
	
	@RequestMapping("findStoreCodesByIDs")
	public void findStoreCodesByIDs(HttpServletRequest request, HttpServletResponse response, String ids){
		List<B2BStore> list = storeService.getB2BStoreByIds(ids);
		JSONObject jObject=new JSONObject();
		jObject.put("stores", list);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}
	
	@RequestMapping("searchByNameCode")
	public void searchByName(HttpServletRequest request, HttpServletResponse response, String name,String code,String groupIds){
		B2BStore s = new B2BStore();
		s.setName(name);
		s.setCode(code);
		List<B2BStore> list = storeService.searchB2BStore(s);
		List<B2BStoreGroup> gList = storeGroupService.getStoreGroupListByGroupIds(groupIds);
		List<B2BStore> addList = new ArrayList<B2BStore>();
		for(B2BStore st:list){
			for(B2BStoreGroup bg:gList){
				if((","+bg.getStoreId()+",").indexOf(","+st.getCode()+",")!=-1){
					addList.add(st);
					break;
				}
			}
		}
		JSONObject jObject=new JSONObject();
		jObject.put("stores", addList);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}
	
	@RequestMapping("searchByNameOrCode")
	public void searchByNameOrCode(HttpServletRequest request, HttpServletResponse response, String name, String groupIds){
		B2BStore s = new B2BStore();
		s.setName(name);
		List<B2BStore> list = storeService.searchB2BStoreByNameOrCode(s);
		List<B2BStoreGroup> gList = storeGroupService.getStoreGroupListByGroupIds(groupIds);
		List<B2BStore> addList = new ArrayList<B2BStore>();
		for(B2BStore st:list){
			for(B2BStoreGroup bg:gList){
				if((","+bg.getStoreId()+",").indexOf(","+st.getCode()+",")!=-1){
					addList.add(st);
					break;
				}
			}
		}
		JSONObject jObject=new JSONObject();
		jObject.put("stores", addList);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}
	@RequestMapping("/getStoreListBySubAreaId")
	public void getStoreList( HttpServletResponse response,@RequestParam String subAreaId) {
		List<B2BStore> list = storeService.getStoreBySubareaId(subAreaId);
		ControllerUtil.writeJson(response, JSONObject.toJSONString(list));
	}
}
