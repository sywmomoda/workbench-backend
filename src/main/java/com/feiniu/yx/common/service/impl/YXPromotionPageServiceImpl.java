package com.feiniu.yx.common.service.impl;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.entity.PromoPage;
import com.feiniu.yx.common.service.YXPromotionPageService;
import com.feiniu.yx.remote.CommodityActivityRemote;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.DateUtil;

@Service
public class YXPromotionPageServiceImpl implements YXPromotionPageService {

	@Autowired
	private YXStoreService yxStoreService;
	
	@Override
	public JSONObject getRemotePages(PromoPage page) {
		JSONObject resultObject = new JSONObject();
		JSONObject param = JSONObject.parseObject(JSONObject.toJSONString(page));
		//param.put("pageNo", value);
		//param.put("pageSize", value);
		//param.put("id", value);
		//param.put("title", value);
		JSONObject body = CommodityActivityRemote.listActivity(param);
		if (body == null) {
			return errorMsg();
		}
		
		Long curPage = page.getPageNo();
		Long pageRows = page.getPageSize();
		Long pageAmount = body.getLong("pageAmount");
		Long total = body.getLong("totalNum");
		
		resultObject.put("code", 1);
		resultObject.put("msg", "成功");
		resultObject.put("curPage", curPage);
		resultObject.put("pageRows", pageRows);
		resultObject.put("pageAmount", pageAmount);
		resultObject.put("total", total);
		
		JSONArray pages = new JSONArray();
		JSONArray activityInfoList = body.getJSONArray("activityInfoList");
		if (activityInfoList != null) {
			for(int i = 0; i < activityInfoList.size(); i++){
				JSONObject rowObj =  activityInfoList.getJSONObject(i);
				JSONObject p = new JSONObject();
				p.put("id", rowObj.getLong("activityId"));
				p.put("name", rowObj.get("activityName"));		
				p.put("card", rowObj.get("card"));		 
				Date startDate = DateUtil.getDate(rowObj.getString("startTime"),"yyyy-MM-dd HH:mm:ss");
				Date endDate = DateUtil.getDate(rowObj.getString("endTime"),"yyyy-MM-dd HH:mm:ss");
				
				p.put("status", getPromotionPageStatus(startDate,endDate));
				String storeCode = rowObj.getString("store");
				if(StringUtils.isNotBlank(storeCode)){
					String storeName = yxStoreService.getStoreNamesByCodes(storeCode);
					p.put("storeCode", storeCode);
					p.put("storeName", storeName);
				}
				
				pages.add(p);
			}
		}
		resultObject.put("list", pages);
		return resultObject;
	}
	
	@Override
	public JSONObject getPromoSinglePageById(String id){
		if(StringUtils.isBlank(id)){
			return null;
		}
		PromoPage page = new PromoPage();
		page.setActivityId(id);
		JSONObject  obj = getRemotePages(page);
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
