package com.feiniu.yx.store.controller;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.entity.YXStoreGroup;
import com.feiniu.yx.store.service.YXStoreGroupService;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/yXStoreGroup")
public class YXStoreGroupController {

	@Autowired
	private YXStoreService storeService;
	
	@Autowired
	private YXStoreGroupService storeGroupService;
	
	@RequestMapping("queryStoreGroup")	
	public ModelAndView queryStoreGroup(HttpServletRequest request, HttpServletResponse response){
		ModelAndView mv = new ModelAndView("store/queryStoreGroup");
		return mv;
	}
	
	@RequestMapping("findAllStoreGroup")
	public void findAllStoreGroup(HttpServletRequest request, HttpServletResponse response){
		String json = storeGroupService.getTreeJsonStoreGroup();
		ControllerUtil.writeJson(response, json);
	}

	@RequestMapping("showSelectGroup")
	public void showSelectGroup(HttpServletRequest request, HttpServletResponse response,
								@RequestParam(defaultValue="4") int showLevel,@RequestParam(defaultValue="") String checkedNodes){
		String json = storeGroupService.showSelectGroupByLevel(showLevel,checkedNodes);
		ControllerUtil.writeJson(response, json);
	}


	@RequestMapping("findStoreGroupOfPermission")
	public void findStoreGroupOfPermission(HttpServletRequest request, HttpServletResponse response){
		String json = storeGroupService.getTreeJsonStoreGroupOfPermission();
		ControllerUtil.writeJson(response, json);
	}
	
	@RequestMapping("addStoreGroup")
	public  ModelAndView addStoreGroup(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="") long id){
		ModelAndView mv = new ModelAndView("store/addStoreGroup");
		YXStoreGroup group = storeGroupService.getStoreGroup(id);
		group.setLevel(group.getLevel()+1);
		if(group.getLevel() ==3 ){
			List<YXStore> list = storeService.getStoreByPgSeq(group.getPgSeq());
			//List<YXStore> list = storeService.getYXStoreList();
			mv.addObject("list", list);
			mv.addObject("pgSeq", group.getPgSeq());
		}
		//List<YXStore> list = storeService.getStoreByPgSeq(pgSeq);
		//List<YXStore> list = storeService.getYXStoreList();
		//mv.addObject("list", list);
		//mv.addObject("pgSeq", pgSeq);
		mv.addObject("group", group);
		return mv;
	}
	
	@RequestMapping("toUpdateStoreGroup")
	public ModelAndView toUpdateStoreGroup(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="0") Long id){
		ModelAndView mv = new ModelAndView("store/updateStoreGroup");
		YXStoreGroup group = storeGroupService.getStoreGroup(id);
		if(group.getLevel()==3){
			List<YXStore> list = storeService.getStoreByPgSeq(group.getPgSeq());
			String stores = group.getStoreId();
			if(StringUtils.isNotBlank(stores)) {
				for(YXStore s : list) {
					if((","+stores+",").contains(","+s.getCode()+",")){
						s.setChecked(1);
					}
				}
			}
			mv.addObject("list", list);
		}
		mv.addObject("group", group);
		mv.addObject("pgSeq", group.getPgSeq());
		return mv;
	}
	
	/*public void getStoresByGroup(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="0") Long id,@RequestParam String pgSeq){
		YXStoreGroup group = storeGroupService.getStoreGroup(id);
		List<YXStore> list = storeService.getStoreByPgSeq(pgSeq);
		String storeIds = group.getStoreId();
		if(StringUtils.isNotBlank(storeIds)) {
			for(YXStore s : list) {
				if((","+storeIds+",").contains(","+s.getCode()+",")){
					s.setChecked(1);
				}
			}
		}
	}*/
	
	
	
	@RequestMapping("updateStoreGroup")
	public void updateStoreGroup(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="0") String data){
		YXStoreGroup group = JSONObject.parseObject(data, YXStoreGroup.class);
		String username = UserUtil.getUserId();
		group.setUpdateId(username);
        group.setUpdateTime(new Date());
        storeGroupService.updateStoreGroup(group);
        JSONObject result = new JSONObject();
		result.put("code", 100);
		result.put("msg", "success");
        ControllerUtil.writeJson(response, result.toJSONString());
	}

	@RequestMapping("delStoreGroup")
	public void delStoreGroup(HttpServletRequest request, HttpServletResponse response,
							  @RequestParam(defaultValue="0") Long id){
		YXStoreGroup group = storeGroupService.getStoreGroup(id);
		String username = UserUtil.getUserId();
		group.setUpdateId(username);
		group.setUpdateTime(new Date());
		storeGroupService.delStoreGroup(group);
		JSONObject result = new JSONObject();
		result.put("code", 100);
		result.put("msg", "success");
		ControllerUtil.writeJson(response, result.toJSONString());
	}
	
	@RequestMapping("saveStoreGroup")
	public void saveStoreGroup(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "0") String data){
		YXStoreGroup group = JSONObject.parseObject(data, YXStoreGroup.class);
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
		YXStoreGroup group=storeGroupService.getStoreGroup(groupId);
		List<YXStore> list=storeGroupService.getActivityStoreList(group);
		JSONObject jObject=new JSONObject();
		jObject.put("stores", list);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}

	@RequestMapping("storeSelect")
	public void storeSelectModuleProper(HttpServletRequest request, HttpServletResponse response, Integer showLevel,
										String checkedCodes, String groupIds, String xiaoQuIds){
		if(showLevel==null){
			showLevel = 4;
		}
		String json = storeGroupService.getTreeSelectStoreGroup(showLevel,checkedCodes,groupIds,xiaoQuIds);
		ControllerUtil.writeJson(response, json);
	}
}
