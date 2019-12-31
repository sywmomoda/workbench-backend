package com.feiniu.yx.page.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.AliVideo;
import com.feiniu.yx.page.service.AliVideoSerivce;
import com.feiniu.yx.util.ControllerUtil;

@Controller
@RequestMapping("/video")
public class AliVideoController {
	@Autowired
	private AliVideoSerivce videoService;
	@RequestMapping("/list")
	public ModelAndView list(@ModelAttribute AliVideo video) {
		ModelAndView mv = new ModelAndView();
		List<AliVideo> list = videoService.list(video);
		mv.addObject("video/list");
		mv.addObject("list", list);
		mv.addObject("video", video);
		return mv;
	}
	
	@RequestMapping("/listData")
	public void listData(@ModelAttribute AliVideo video,HttpServletResponse response) {
		List<AliVideo> list = videoService.list(video);
		JSONObject object = new JSONObject();
		object.put("list",list);
		object.put("video", video);
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	
	@RequestMapping(value="/detailData",method=RequestMethod.POST)
	public void toDetailData(@RequestParam(defaultValue = "") String data,HttpServletResponse response) {
		JSONObject res = videoService.interfacelist(data);
		JSONObject object = new JSONObject();
		object.put("data",res);
		object.put("code", 100);
		object.put("msg", "success");
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	
	
	@RequestMapping("/toAdd")
	public ModelAndView toAdd(@RequestParam String actType,Long id) {
		ModelAndView mv = new ModelAndView();
		mv.addObject("actType", actType);
		mv.addObject("video", new AliVideo());
		if(actType.equals("update")) {
			mv.addObject("video", videoService.getVideoById(id));
		}
		return mv;
	}
	
	@RequestMapping("/insertVideo")
	public void insertVideo(@ModelAttribute AliVideo video,HttpServletResponse response) {
		videoService.insert(video);
		ControllerUtil.writeJson(response);
	}
	
	@RequestMapping("/updateVideo")
	public void updateVideo(@ModelAttribute AliVideo video,HttpServletResponse response) {
		videoService.update(video);
		ControllerUtil.writeJson(response);
	}
	
	@RequestMapping("/deleteVideo")
	public void delteVideo(@RequestParam Long id,HttpServletResponse response) {
		videoService.delete(id);
		ControllerUtil.writeJson(response);
	}
}
