package com.feiniu.yx.pool.controller;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.pool.service.YxCouponService;
import com.feiniu.yx.util.ControllerUtil;

@RequestMapping("/coupon")
@Controller
public class YxCouponController {
	
	@Autowired
	public YxCouponService couponService;
	
	@RequestMapping("/getCouponInfoByCouponId")
	public void getCouponInfoByCouponId(HttpServletRequest request,HttpServletResponse response,@RequestParam String couponId) throws ParseException{
		/*if(couponId.indexOf("1")!=-1){
			JSONObject jObject  = new JSONObject();
			jObject.put("couponName", "测试券"+couponId);
			ControllerUtil.writeJson(response, jObject.toJSONString());
		}else{
			ControllerUtil.writeJsonString(response, "nodata");
		}*/
		JSONObject jObject = couponService.getCouponInfoByCouponId(couponId);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}
	
	@RequestMapping("/getCouponStoreCodesById")
	public void getCouponStoreCodesById(HttpServletRequest request,HttpServletResponse response,@RequestParam String couponIds){
		JSONObject jObject = couponService.getCouponStoreCodesById(couponIds);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}
}
