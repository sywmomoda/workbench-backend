package com.feiniu.b2b.coupon.controller;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.coupon.service.B2BCouponService;
import com.feiniu.yx.util.ControllerUtil;

@Controller
@RequestMapping("/b2bConpon")
public class B2BCouponController {

	@Autowired
	private B2BCouponService couponService;
	
	@RequestMapping("/getCouponInfoByCouponId")
	public void getCouponInfoByCouponId(HttpServletRequest request,HttpServletResponse response,@RequestParam(defaultValue="") String couponId) throws ParseException{
		JSONObject jObject = couponService.getCouponInfo(couponId);
		ControllerUtil.writeJson(response, jObject.toJSONString());
	}
}
