package com.feiniu.yx.share.controller;

import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.entity.YXLog;
import com.feiniu.yx.common.interceptor.LogTraceService;
import com.feiniu.yx.common.service.YXLogService;
import com.feiniu.yx.pool.service.YxCouponService;
import com.feiniu.yx.share.entiry.YXCouponShare;
import com.feiniu.yx.share.service.YXCouponShareService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.UserUtil;

@Controller
@RequestMapping("YxCouponShare")
public class YXCouponShareController {
	
   @Autowired
   private YXCouponShareService couponShareService;
   
   @Autowired
   private YxCouponService couponServcie;
   
   @Autowired
   private LogTraceService logTraceService;
   
   @Autowired
   private YXLogService  logService;
   

   @RequestMapping("queryPageList")
   public ModelAndView queryPageList(HttpServletRequest request, HttpServletResponse response,
		  @ModelAttribute YXCouponShare couponShare) throws Exception{
	    
	    String name = couponShare.getName();
	    if(StringUtils.isNotBlank(name)){
	    	name = URLDecoder.decode(name, "UTF-8");
	    }
	    couponShare.setName(name);
	    List<YXCouponShare>  list = couponShareService.queryCouponShareList(couponShare);
	    ModelAndView mv = new ModelAndView("sharecoupon/activityList");
	    mv.addObject("list", list);
	    mv.addObject("obj", couponShare);
	    return mv;
	   
   }
	
   @RequestMapping("editPage")
   public ModelAndView editPage(HttpServletRequest request, HttpServletResponse response){
	   ModelAndView mv = new ModelAndView("sharecoupon/editActivity");
	   return mv;
   }
	
   @RequestMapping("/insertCouponShare")	
   public void insertCouponShare(HttpServletRequest request, HttpServletResponse response,
		   @RequestParam String data){
	   YXCouponShare share = JSONObject.parseObject(data, YXCouponShare.class);
	   Date now = new Date();
	   share.setCreateTime(now);
	   share.setUpdateTime(now);
	   String userId = UserUtil.getUserId();
	   share.setCreateId(userId);
	   share.setUpdateId(userId);
	   couponShareService.insertCouponShare(share);
	   logTraceService.sendLogger("新增","couponShare",share.getId(),request);
	   JSONObject result = new JSONObject();
	   result.put("status", 1);
	   result.put("msg","保存成功");
	   result.put("insertId", share.getId());
	   ControllerUtil.writeJson(response, result.toJSONString());
   }
   
   @RequestMapping("/getCouponShareById")
   public void queryCouponShareById(HttpServletRequest request, HttpServletResponse response,
		 @RequestParam Long id){
	   JSONObject obj = couponShareService.queryJSONObjectById(id);
	    ControllerUtil.writeJson(response,obj.toJSONString());
   }
   
   @RequestMapping("/getCouponShareAndLogById")
   public void getCouponShareAndLogById(HttpServletRequest request, HttpServletResponse response,
			 @RequestParam Long id){
	    JSONObject obj = couponShareService.queryJSONObjectById(id);
		YXLog yxLog  = new YXLog();
		yxLog.setLogType("couponShare");
		yxLog.setProtoId(id);
		List<YXLog> logList = logService.queryLogs(yxLog);
		obj.put("logList", logList);
		ControllerUtil.writeJson(response,obj.toJSONString());
   }
   
   
   @RequestMapping("/updateCouponShare")
   public void updateCouponShare(HttpServletRequest request, HttpServletResponse response,
		   @RequestParam String data){
	   YXCouponShare share = JSONObject.parseObject(data, YXCouponShare.class);
	   Date now = new Date();
	   share.setUpdateTime(now);
	   String userId = UserUtil.getUserId();
	   share.setUpdateId(userId);
	   couponShareService.updateCouponShare(share);
	   logTraceService.sendLogger("修改","couponShare",share.getId(),request);
	   JSONObject result = new JSONObject();
	   result.put("status", 1);
	   result.put("msg","保存成功");
	   ControllerUtil.writeJson(response, result.toJSONString());
   }
   
   @RequestMapping("/updateCouponShareTime")
   public void updateCoupShareTime(HttpServletRequest request, HttpServletResponse response,
		   @ModelAttribute YXCouponShare couponShare){
	   String userId = UserUtil.getUserId();
	   couponShare.setUpdateId(userId);
	   Date now = new Date();
	    couponShare.setUpdateTime(now);
	     JSONObject  result =couponShareService.updateCouponShareEndTime(couponShare);
	    int status = result.getIntValue("status");
	    if(status == 1){
	    	logTraceService.sendLogger("延长时间","couponShare",couponShare.getId(),request);
	    }
	   ControllerUtil.writeJson(response, result.toJSONString());
   }
   
   @RequestMapping("/updateCouponShareStatus")
   public void updateCouponShareStatus(HttpServletRequest request, HttpServletResponse response,
		   @ModelAttribute YXCouponShare couponShare){
	   String userId = UserUtil.getUserId();
	   couponShare.setUpdateId(userId);
	   Date now = new Date();
	   couponShare.setUpdateTime(now);
	   couponShareService.updateCouponShare(couponShare);
	   logTraceService.sendLogger("终止活动","couponShare",couponShare.getId(),request);
	   JSONObject result = new JSONObject();
	   result.put("status", 1);
	   result.put("msg","保存成功");
	   ControllerUtil.writeJson(response, result.toJSONString());
   }
   
   @RequestMapping("/getCouponListByStoreCodes")
	public void getCouponListByStoreCodes(HttpServletRequest request,
			HttpServletResponse response, @RequestParam String data) {
		JSONObject object = JSONObject.parseObject(data);
		JSONObject couponObj= couponServcie
				.getCouponListByStoreCodesInfo(object);
         couponObj.put("status", 1);
  	   ControllerUtil.writeJson(response, couponObj.toJSONString());
	}
   
   @RequestMapping("/previewCouponShare")
   public ModelAndView previewCouponShare(){
	    ModelAndView  mv = new ModelAndView("sharecoupon/preview");
	    return mv;
   }
   
}
