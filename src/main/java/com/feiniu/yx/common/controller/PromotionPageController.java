package com.feiniu.yx.common.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.entity.PromoPage;
import com.feiniu.yx.common.service.YXPromotionPageService;
import com.feiniu.yx.util.ControllerUtil;

/**
 * 查询行销活动列表
 *
 */
@Controller
@RequestMapping("promoPage")
public class PromotionPageController {
   
   @Autowired
   private  YXPromotionPageService pageService;
	
	@RequestMapping("getPromotionPages")
    public void getPromotionPages( HttpServletRequest request,HttpServletResponse response,@ModelAttribute PromoPage page){
		JSONObject object = pageService.getRemotePages(page);		
		ControllerUtil.writeJson(response, object.toJSONString());
    }
}
