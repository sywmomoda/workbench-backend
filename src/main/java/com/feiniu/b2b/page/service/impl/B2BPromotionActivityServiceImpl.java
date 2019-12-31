package com.feiniu.b2b.page.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.page.service.B2BPromotionActivityService;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.DateUtil;
import com.feiniu.yx.util.HttpTookit;

@Service
public class B2BPromotionActivityServiceImpl implements B2BPromotionActivityService {

	//行销活动列表接口
	private final static String URL_RemotePage=SystemEnv.getProperty("b2bPromotionActivity");
	
	@Autowired
	private B2BStoreService b2bStoreService;
	
	@Override
	public JSONObject getRemotePages(String pageNo,String pageSize,String id,String name) {
		JSONObject resultObject = new JSONObject();
		String result = null;
		try {
			Map<String,String> params = new  HashMap<String,String>();
			JSONObject paramsJSON = new JSONObject();
			paramsJSON.put("pageNo", pageNo);
			paramsJSON.put("pageSize", pageSize);
			paramsJSON.put("bizType", "1");//优鲜：1， 欧尚：2
			paramsJSON.put("orderBy", "desc");
			if(StringUtils.isNotBlank(id)){
				paramsJSON.put("id", id);
			}
			
			if(StringUtils.isNotBlank(name)){
				paramsJSON.put("name", name);
			}
			
			params.put("param", paramsJSON.toJSONString());
			result = HttpTookit.doPost(URL_RemotePage,params);
		}catch(Exception e){
			return errorMsg();
		}
		
		if(result == null){
			return errorMsg();
		}
		
		JSONObject remoteObject = JSONObject.parseObject(result);
		String code = remoteObject.getString("code");
		if(!"200".equals(code)){
			return errorMsg();
		}
	   
		JSONObject body = remoteObject.getJSONObject("body");
		if (body == null) {
			return errorMsg();
		}
		
		Long curPage = Long.parseLong(pageNo);
		Long pageRows = Long.parseLong(pageSize);
		
		Long total = body.getLong("totalRow");
		Long pageAmount = total/pageRows + (total%pageRows==0?0:1);
		resultObject.put("code", 1);
		resultObject.put("msg", "成功");
		resultObject.put("curPage", curPage);
		resultObject.put("pageRows", pageRows);
		resultObject.put("pageAmount", pageAmount);
		resultObject.put("total", total);
		
		JSONArray rows = new JSONArray();
		JSONArray pages = new JSONArray();
		JSONArray activityInfoList = body.getJSONArray("activityList");
		if (activityInfoList != null) {
			rows.addAll(activityInfoList);
		}
		for(int i = 0; i < rows.size(); i++){
			JSONObject rowObj =  rows.getJSONObject(i);
			 JSONObject p = new JSONObject();
			 p.put("id", rowObj.getLong("id"));
			 p.put("name", rowObj.get("name"));		 
			 Date startDate = DateUtil.getDate(rowObj.getString("startTime"),"yyyy-MM-dd HH:mm:ss");
			 Date endDate = DateUtil.getDate(rowObj.getString("endTime"),"yyyy-MM-dd HH:mm:ss");
			 
			 p.put("status", getPromotionPageStatus(startDate,endDate));
			 String storeCode = rowObj.getString("store");
			 if(StringUtils.isNotBlank(storeCode)){
				 String storeName = b2bStoreService.getStoreNamesByCodes(storeCode);
				 p.put("storeCode", storeCode);
				 p.put("storeName", storeName);
			 }

			 pages.add(p);
		}
		
		resultObject.put("list", pages);
		return resultObject;
	}
	
	@Override
	public JSONObject getPromoSinglePageById(String id){
		if(StringUtils.isBlank(id)){
			return null;
		}
		JSONObject  obj = getRemotePages("1", "10", id, "");
		if(null == obj){
			return null;
		}
		JSONArray list= obj.getJSONArray("list");
		if(null == list){
		  return null;
		}
		if(list.size() == 0){
			return null;
		}
		return list.getJSONObject(0);
	}
	
	private JSONObject errorMsg() {
		JSONObject resultObject = new JSONObject();
		resultObject.put("code", 0);
		resultObject.put("msg", "接口数据返回数据异常");
		resultObject.put("data",null);
		return resultObject;
	}
	
	private int getPromotionPageStatus(Date startDate,Date endDate){
		Date now = new Date();
		int status = 0;
		if(null == startDate){
			return status;
		}
		if(null == endDate){
			return status;
		}
		if(startDate.getTime() > now.getTime()){
			status = 1;  //未开始 
		}else if(startDate.getTime() < now.getTime() && endDate.getTime() >= now.getTime()){
			status = 2; // 进行开始 
		}
		return status;
	}

}
