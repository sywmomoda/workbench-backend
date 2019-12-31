package com.feiniu.yx.common.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.feiniu.yx.common.service.YXCouponXiaoQuService;
import com.feiniu.yx.util.ControllerUtil;

@RequestMapping("yxXiaoQu")
@Controller
public class YXCouponXiaoQuController {
	@Autowired
	private YXCouponXiaoQuService xiaoQuService;
	
	@RequestMapping("/getJSONXiaoQu")
	public void getJSONXiaoQu(HttpServletRequest request,HttpServletResponse response,
			@RequestParam String pgSeq) {
		String json = xiaoQuService.getJSONXiaoQu(pgSeq);
		ControllerUtil.writeJson(response, json);
	}
}
