package com.feiniu.yx.share.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.feiniu.yx.common.entity.YXLog;
import com.feiniu.yx.common.interceptor.LogTraceService;
import com.feiniu.yx.common.service.YXLogService;
import com.feiniu.yx.pool.service.YxCouponService;
import com.feiniu.yx.share.entiry.YXCouponShare;
import com.feiniu.yx.share.service.YXCouponShareService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.LayuiDataUtil;
import com.feiniu.yx.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/yx2CouponShare")
public class YX2CouponShareController {
	
   @Autowired
   private YXCouponShareService couponShareService;

    @Autowired
    private YxCouponService couponServcie;
   
   @Autowired
   private LogTraceService logTraceService;
   
   @Autowired
   private YXLogService  logService;
   

   @RequestMapping("/queryPageList")
   public ModelAndView queryPageList(HttpServletResponse response, @ModelAttribute YXCouponShare couponShare ) throws Exception{
	    ModelAndView mv = new ModelAndView("yxsharecoupon/activityList");
	    mv.addObject("object", couponShare);
	    return mv;
	   
   }
   
   @RequestMapping("/listData")
   public void listData(@RequestParam String data,HttpServletResponse response,@RequestParam Integer page, @RequestParam Integer limit) {
	   YXCouponShare couponShare = JSONObject.parseObject(data, YXCouponShare.class);
	   couponShare = couponShare == null ? new YXCouponShare() : couponShare;
       couponShare.setType(3);
	   couponShare.setCurPage(page);
	   couponShare.setPageRows(limit);
	   List<YXCouponShare>  list = couponShareService.queryCouponShareList(couponShare);
	   ControllerUtil.writeJson(response,JSONObject.toJSONStringWithDateFormat(LayuiDataUtil.convertData(list, couponShare), 
			   "yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteDateUseDateFormat));
   }
	
   @RequestMapping("/editPage")
   public ModelAndView editPage(HttpServletRequest request, HttpServletResponse response){
	   ModelAndView mv = new ModelAndView("yxsharecoupon/editActivity");
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
	   logTraceService.sendLogger("新增","YX2CouponShare",share.getId(),request);
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
		yxLog.setLogType("YX2CouponShare");
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
	   logTraceService.sendLogger("修改","YX2CouponShare",share.getId(),request);
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
	    	logTraceService.sendLogger("延长时间","YX2CouponShare",couponShare.getId(),request);
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
	   logTraceService.sendLogger("终止活动","YX2CouponShare",couponShare.getId(),request);
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
	    ModelAndView  mv = new ModelAndView("yxsharecoupon/preview");
	    return mv;
   }
   
}
