package com.feiniu.b2b.page.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.page.service.B2BPromotionActivityService;
import com.feiniu.yx.util.ControllerUtil;

/**
 * 查询行销活动列表
 *
 */
@Controller
@RequestMapping("b2bPromoActivity")
public class B2BPromotionActivityController {
   
   @Autowired
   private  B2BPromotionActivityService pageService;
	
	@RequestMapping("b2bPromoActivity")
    public void getPromotionPages( HttpServletRequest request,HttpServletResponse response,
    		@RequestParam(defaultValue="")String id, @RequestParam(defaultValue="")String name,
    		@RequestParam(defaultValue="1")String curPage){
		JSONObject object = pageService.getRemotePages(curPage, "10",id,name);		
		ControllerUtil.writeJson(response, object.toJSONString());
    }
}
