package com.feiniu.yx.store.controller;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.UserService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.entity.YXStoreGroup;
import com.feiniu.yx.store.service.YXStoreGroupService;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.ControllerUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/yXStore")
public class YXStoreController {
	
	@Autowired
	private YXStoreService storeService;
	
	@Autowired
	private YXStoreGroupService storeGroupService;
	
	@Autowired
	private UserService userService;
	
	@RequestMapping("/synchroRemoteStore")
	public void synchroRemoteStore(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String areaCodes){
		JSONObject object =  storeService.synchroRemoteStoreInfo(areaCodes);
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	@RequestMapping("/getStoreTree")
	public void getStoreTree(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="") String storeCodes){
		String json=  storeService.getTreeJsonStore(storeCodes);
		ControllerUtil.writeJson(response, json);
	}
	
	@RequestMapping("storeSelect")
	public void storeSelect(HttpServletRequest request, HttpServletResponse response, String checkedCodes,String groupIds, String xiaoQuIds){
		String json = storeGroupService.getTreeSelectStoreGroup(4,checkedCodes,groupIds,xiaoQuIds);
		ControllerUtil.writeJson(response, json);
	}
	
	
	@RequestMapping("findStoreByCode")
	public void findStoreByCode(HttpServletRequest request, HttpServletResponse response, String codes){
		if(StringUtils.isBlank(codes)){
			return;
		}
		List<YXStore> list = storeService.getStoreByCodes(codes);
		JSONObject jObject=new JSONObject();
		jObject.put("stores", list);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}
	
	@RequestMapping("findStoreCodesByIDs")
	public void findStoreCodesByIDs(HttpServletRequest request, HttpServletResponse response, String ids){
		List<YXStore> list = storeService.getYXStoreByIds(ids);
		JSONObject jObject=new JSONObject();
		jObject.put("stores", list);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}
	
	@RequestMapping("searchByNameOrCode")
	public void searchByNameOrCode(HttpServletRequest request, HttpServletResponse response, String name, String groupIds){
		YXStore s = new YXStore();
		s.setName(name);
		List<YXStore> list = storeService.searchYXStoreByNameOrCode(s);
		List<YXStoreGroup> gList = storeGroupService.getStoreGroupListByGroupIds(groupIds);
		List<YXStore> addList = new ArrayList<YXStore>();
		//权限过滤
		Map<String,Set<String>> userStoreGroupMap = userService.getMapUserStores();
		Set<String> userStores = userStoreGroupMap.get("codeSet");
		for(YXStore st:list){
			for(YXStoreGroup bg:gList){
				if((","+bg.getStoreId()+",").indexOf(","+st.getCode()+",")!=-1){
					if(!userStores.contains(st.getCode())){
						continue;
					}
					addList.add(st);
					break;
				}
			}
		}
		JSONObject jObject=new JSONObject();
		jObject.put("stores", addList);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}
	
	@RequestMapping("searchByNameCode")
	public void searchByNameCode(HttpServletRequest request, HttpServletResponse response, String name,String code, String groupIds){
		YXStore s = new YXStore();
		s.setName(name);
		s.setCode(code);
		List<YXStore> list = storeService.searchYXStoreByNameCode(s);
		List<YXStoreGroup> gList = storeGroupService.getStoreGroupListByGroupIds(groupIds);
		List<YXStore> addList = new ArrayList<YXStore>();
		for(YXStore st:list){
			for(YXStoreGroup bg:gList){
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

	@RequestMapping("getGroupInfoByIds")
	public void getGroupInfoByIds(HttpServletRequest request, HttpServletResponse response, String groupIds){
		List<YXStoreGroup> gList = storeGroupService.getStoreGroupListByGroupIds(groupIds);
		ControllerUtil.writeJson(response, JSONObject.toJSONString(gList));
	}
	
	@RequestMapping("getStoresByPgSeq")
	public void getStoresByPgSeq(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String pgSeq){
		List<YXStore> list = storeService.getStoreByPgSeq(pgSeq);
		ControllerUtil.writeJson(response, JSONObject.toJSONString(list));
	}

	@RequestMapping("/validateStoreIds")
	public void validateStoreIds(HttpServletResponse response,String groupIds){
		System.out.println(groupIds);
		String[] split = groupIds.split(",");
		Map<String,List> map =  storeService.validateStoreIds(split);
		String s = JSONObject.toJSONString(map);
		System.out.println(s);
		ControllerUtil.writeJson(response, JSONObject.toJSONString(map));

	}
}
