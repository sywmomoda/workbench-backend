package com.feiniu.yx.common.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.feiniu.yx.common.dao.ResDao;
import com.feiniu.yx.common.entity.Res;
import com.feiniu.yx.common.entity.ReturnT;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.ControllerUtil;

/**
 * 静态文件管理
 * @author tongwenhuan
 * 2017年3月9日 下午4:55:39
 */
@Controller
@RequestMapping("/res")
public class ResController {
	
	@Autowired
	private ResDao resDao;
	
	private static final String ENV = SystemEnv.getProperty("fn.env");

	@RequestMapping(value="/findList",method=GET)
	public ModelAndView list(HttpServletResponse response,@ModelAttribute Res res){
		ModelAndView mv = new ModelAndView("system/resList");
		mv.addObject("resList", resDao.findList(res));
		mv.addObject("res", res);
		return mv;
	}
	
	@RequestMapping(value="/toAdd",method=GET)
	public ModelAndView toAdd(){
		ModelAndView mv = new ModelAndView("system/resAdd");
		return mv;
	}
	
	@RequestMapping(value="/toDel",method=POST)
	public void toEdit(HttpServletResponse response,Long id){
		resDao.delOne(id);
		ControllerUtil.writeJson(response);
	}
	
	@RequestMapping(value="/save",method=POST)
	public void save(HttpServletResponse response, Res res){
		res.setEnv(ENV);
		resDao.addOne(res);
		ControllerUtil.writeJson(response);
	}
	
	@RequestMapping(value="/toUpd",method=GET)
	public ModelAndView toAdd(Long id){
		ModelAndView mv = new ModelAndView("system/resUpd");
		Res res = resDao.findOne(id);
		mv.addObject("res", res);
		return mv;
	}
	
	@RequestMapping(value="/update",method=POST)
	public void update(HttpServletResponse response, Res res){
		resDao.updNameAndPath(res);
		ControllerUtil.writeJson(response);
	}
	
	@RequestMapping(value="/updateVer",method=GET)
	@ResponseBody
	public ReturnT<String> update(HttpServletResponse response, Long id){
		resDao.updOneTemp(id);
		return ReturnT.SUCCESS;
	}
	
	@RequestMapping(value="/authority",method=GET)
	public ModelAndView authority(HttpServletResponse response,@ModelAttribute Res res){
		ModelAndView mv = new ModelAndView("system/authority");
		return mv;
	}
	
}
