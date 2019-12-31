package com.feiniu.yx.page.controller;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.entity.ReturnT;
import com.feiniu.yx.page.entity.SearchWords;
import com.feiniu.yx.page.service.SearchWordsSerivce;
import com.feiniu.yx.util.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/searchWords")
public class SearchWordsController {
	@Autowired
	private SearchWordsSerivce searchWordsService;
	@RequestMapping("/list")
	public ModelAndView list(@ModelAttribute SearchWords searchWords) {
		ModelAndView mv = new ModelAndView();
		List<SearchWords> list = searchWordsService.list(searchWords);
		mv.addObject("searchWords/list");
		mv.addObject("list", list);
		mv.addObject("searchWords", searchWords);
		return mv;
	}
	
	@RequestMapping("/listData")
	public void listData(@ModelAttribute SearchWords searchWords,HttpServletResponse response) {
		List<SearchWords> list = searchWordsService.list(searchWords);
		JSONObject object = new JSONObject();
		object.put("list",list);
		object.put("searchWords", searchWords);
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	
	@RequestMapping("/toAdd")
	public ModelAndView toAdd(@RequestParam String actType,Long id) {
		ModelAndView mv = new ModelAndView();
		mv.addObject("actType", actType);
		mv.addObject("searchWords", new SearchWords());
		if(actType.equals("update")) {
			mv.addObject("searchWords", searchWordsService.getSearchWordsById(id));
		}
		return mv;
	}
	
	@RequestMapping("/insertSearchWords")
	public void insertsearchWords(@ModelAttribute SearchWords searchWords, HttpServletResponse response) {
		ReturnT<String> rt = searchWordsService.insert(searchWords);
		ControllerUtil.writeJson(response, JSONObject.toJSONString(rt));
	}
	
	@RequestMapping("/updateSearchWords")
	public void updatesearchWords(@ModelAttribute SearchWords searchWords,HttpServletResponse response) {
		ReturnT<String> rt = searchWordsService.update(searchWords);
		ControllerUtil.writeJson(response, JSONObject.toJSONString(rt));
	}
	
	@RequestMapping("/deleteSearchWords")
	public void deltesearchWords(@RequestParam Long id,HttpServletResponse response) {
		ReturnT<String> rt = searchWordsService.delete(id);
		ControllerUtil.writeJson(response, JSONObject.toJSONString(rt));
	}

	@RequestMapping("/pubSearchWords")
	public void pubSearchWords(@RequestParam Long id,HttpServletResponse response) {
		ReturnT<String> rt = searchWordsService.publishSearchWords(id);
		ControllerUtil.writeJson(response, JSONObject.toJSONString(rt));
	}
}
