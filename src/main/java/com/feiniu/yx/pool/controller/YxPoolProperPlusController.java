package com.feiniu.yx.pool.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.pool.entity.YxPoolProperPlus;
import com.feiniu.yx.pool.service.YxPoolProperPlusService;
import com.feiniu.yx.util.ControllerUtil;

@Controller
@RequestMapping("/YxPoolProperPlus")
public class YxPoolProperPlusController {		
	@Autowired
	private YxPoolProperPlusService properPlusService;
	
	@RequestMapping("/insertProperByStorecodes")
	public void  insertProperByStorecodes(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue = "0") String data){
		List<YxPoolProperPlus> list = JSONObject.parseArray(data,YxPoolProperPlus.class);
		Long[] ids = properPlusService.batchInsert(list);
		JSONObject object = new JSONObject();
		object.put("ids", ids);
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	
	@RequestMapping("/queryProperByCommodityId")
	public void queryProperByCommodityId(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue = "0") Long commodityId){
		List<YxPoolProperPlus> list = properPlusService.queryProperList(commodityId);
		JSONObject object = new JSONObject();
		object.put("list", list);
		ControllerUtil.writeJson(response, object.toJSONString());
	}
}
