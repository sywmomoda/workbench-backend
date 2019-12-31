package com.feiniu.b2b.share.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.feiniu.b2b.coupon.service.B2BCouponService;
import com.feiniu.b2b.share.entiry.B2BCouponShare;
import com.feiniu.b2b.share.service.B2BCouponShareService;
import com.feiniu.yx.common.entity.YXLog;
import com.feiniu.yx.common.interceptor.LogTraceService;
import com.feiniu.yx.common.service.YXLogService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.LayuiDataUtil;
import com.feiniu.yx.util.UserUtil;

@Controller
@RequestMapping("/B2BCouponShare")
public class B2BCouponShareController {
	
   @Autowired
   private B2BCouponShareService couponShareService;
   
   @Autowired
   private B2BCouponService couponServcie;
   
   @Autowired
   private LogTraceService logTraceService;
   
   @Autowired
   private YXLogService  logService;
   

   @RequestMapping("/queryPageList")
   public ModelAndView queryPageList( HttpServletResponse response,@ModelAttribute B2BCouponShare couponShare ) throws Exception{
	    ModelAndView mv = new ModelAndView("b2bsharecoupon/activityList");
	    mv.addObject("object", couponShare);
	    return mv;
	   
   }
   
   @RequestMapping("/listData")
   public void listData(@RequestParam String data,HttpServletResponse response,@RequestParam Integer page, @RequestParam Integer limit) {
	   B2BCouponShare couponShare = JSONObject.parseObject(data, B2BCouponShare.class);
	   couponShare = couponShare == null ? new B2BCouponShare() : couponShare;
	   couponShare.setCurPage(page);
	   couponShare.setPageRows(limit);
	   List<B2BCouponShare>  list = couponShareService.queryCouponShareList(couponShare);
	   ControllerUtil.writeJson(response,JSONObject.toJSONStringWithDateFormat(LayuiDataUtil.convertData(list, couponShare), 
			   "yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteDateUseDateFormat));
   }
	
   @RequestMapping("/editPage")
   public ModelAndView editPage(HttpServletRequest request, HttpServletResponse response){
	   ModelAndView mv = new ModelAndView("b2bsharecoupon/editActivity");
	   return mv;
   }
	
   @RequestMapping("/insertCouponShare")	
   public void insertCouponShare(HttpServletRequest request, HttpServletResponse response,
		   @RequestParam String data){
	   B2BCouponShare share = JSONObject.parseObject(data, B2BCouponShare.class);
	   Date now = new Date();
	   share.setCreateTime(now);
	   share.setUpdateTime(now);
	   String userId = UserUtil.getUserId();
	   share.setCreateId(userId);
	   share.setUpdateId(userId);
	   couponShareService.insertCouponShare(share);
	   logTraceService.sendLogger("新增","B2BCouponShare",share.getId(),request);
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
		yxLog.setLogType("B2BCouponShare");
		yxLog.setProtoId(id);
		List<YXLog> logList = logService.queryLogs(yxLog);
		obj.put("logList", logList);
		ControllerUtil.writeJson(response,obj.toJSONString());
   }
   
   
   @RequestMapping("/updateCouponShare")
   public void updateCouponShare(HttpServletRequest request, HttpServletResponse response,
		   @RequestParam String data){
	   B2BCouponShare share = JSONObject.parseObject(data, B2BCouponShare.class);
	   Date now = new Date();
	   share.setUpdateTime(now);
	   String userId = UserUtil.getUserId();
	   share.setUpdateId(userId);
	   couponShareService.updateCouponShare(share);
	   logTraceService.sendLogger("修改","B2BCouponShare",share.getId(),request);
	   JSONObject result = new JSONObject();
	   result.put("status", 1);
	   result.put("msg","保存成功");
	   ControllerUtil.writeJson(response, result.toJSONString());
   }
   
   @RequestMapping("/updateCouponShareTime")
   public void updateCoupShareTime(HttpServletRequest request, HttpServletResponse response,
		   @ModelAttribute B2BCouponShare couponShare){
	   String userId = UserUtil.getUserId();
	   couponShare.setUpdateId(userId);
	   Date now = new Date();
	    couponShare.setUpdateTime(now);
	     JSONObject  result =couponShareService.updateCouponShareEndTime(couponShare);
	    int status = result.getIntValue("status");
	    if(status == 1){
	    	logTraceService.sendLogger("延长时间","B2BCouponShare",couponShare.getId(),request);
	    }
	   ControllerUtil.writeJson(response, result.toJSONString());
   }
   
   @RequestMapping("/updateCouponShareStatus")
   public void updateCouponShareStatus(HttpServletRequest request, HttpServletResponse response,
		   @ModelAttribute B2BCouponShare couponShare){
	   String userId = UserUtil.getUserId();
	   couponShare.setUpdateId(userId);
	   Date now = new Date();
	   couponShare.setUpdateTime(now);
	   couponShareService.updateCouponShare(couponShare);
	   logTraceService.sendLogger("终止活动","B2BCouponShare",couponShare.getId(),request);
	   JSONObject result = new JSONObject();
	   result.put("status", 1);
	   result.put("msg","保存成功");
	   ControllerUtil.writeJson(response, result.toJSONString());
   }
   
   @RequestMapping("/getCouponListByStoreCodes")
	public void getCouponListByStoreCodes(HttpServletRequest request,
			HttpServletResponse response, @RequestParam String data) {
		JSONObject object = JSONObject.parseObject(data);
		JSONObject couponObj= couponServcie.getCouponListByStoreCodesInfo(object);
         couponObj.put("status", 1);
  	   ControllerUtil.writeJson(response, couponObj.toJSONString());
	}
   
   @RequestMapping("/previewCouponShare")
   public ModelAndView previewCouponShare(){
	    ModelAndView  mv = new ModelAndView("b2bsharecoupon/preview");
	    return mv;
   }
   
}
