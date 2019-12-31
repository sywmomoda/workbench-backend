package com.feiniu.yx.page.service.module;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.YXCouponService;
import com.feiniu.yx.core.UserInfoService;
import com.feiniu.yx.core.impl.BaseCommodityServiceImpl;
import com.feiniu.yx.core.impl.CommoditySaleInfoServiceImpl;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.page.entity.YxRemoteCommodity;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.ModuleProperPlusService;
import com.feiniu.yx.page.service.RemoteCommodityService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.util.DateUtil;

//限时好券
@Service
public class ITryEatingService implements CustomModule {
	
	public static Logger logger = Logger.getLogger(ITryEatingService.class);
	@Autowired
	private YXCouponService yxCouponService;
	
	@Autowired
	private ModuleProperPlusService moduleProperPlusService;
	
	@Autowired
	private BaseCommodityServiceImpl baseCommodityService;
	@Autowired
	private UserInfoService userInfoService;//远程商品接口
	@Autowired
	private RemoteCommodityService remoteCommodityService;
	
	//private static String exceptWords = SystemEnv.getProperty("tryEating.exceptWords");;
	
	@Override
	public void findCustomData(JSONObject mjo) throws Exception {
		Long moduleId = mjo.getLong("id");
		Map<String,ModuleProperPlus> properPlusMap = moduleProperPlusService.queryModuleProperMapByModuleId(moduleId);
		if(null == properPlusMap){
			return;
		}
		String storeCode = mjo.getString("storeCode");
		ModuleProperPlus value = properPlusMap.get(storeCode);
		if(null  == value){
			return;
		}
		List<JSONObject> commodityList = getcommodityListByStore(value,storeCode);
		if(null == commodityList || commodityList.size() == 0 ){
			return;
		}
		mjo.put("eatingData", commodityList);
	}
	
	private  List<JSONObject> getcommodityListByStore(ModuleProperPlus proper,String storeCode){
		List<JSONObject> commodityList = new ArrayList<JSONObject>();
		String  moduleProper= proper.getModuleProper();
		if(StringUtils.isBlank(moduleProper)){
			return commodityList;
		}
		JSONObject proObj = JSONObject.parseObject(moduleProper);
		Object commodityIds = proObj.get("commodityIds");
		Object couponIds = proObj.get("couponIds");
		Object commodityLimits = proObj.get("commodityLimits");
		Object commodityNeedPraises = proObj.get("commodityNeedPraises");
		Object beginTimes = proObj.get("beginTime");
		Object endTimes = proObj.get("endTime");
		if(null == commodityIds){
			return commodityList;
		}
		Date now = new Date();
		if(commodityIds instanceof JSONArray){
			commodityIds = (JSONArray)commodityIds;
			Object[] commodityIdArray = ((JSONArray) commodityIds).toArray();
			Object[] couponIdArray = ((JSONArray) couponIds).toArray();
			Object[] commodityLimitArray = ((JSONArray) commodityLimits).toArray();
			Object[] commodityNeedPraiseArray = ((JSONArray) commodityNeedPraises).toArray();
			Object[] beginTimeArray = ((JSONArray) beginTimes).toArray();
			Object[] endTimeArray = ((JSONArray) endTimes).toArray();
			for(int i = 0; i < commodityIdArray.length; i++) {
				String couponId = couponIdArray[i].toString();
				String commodityLimit = commodityLimitArray[i].toString();
				String commodityNeedPraise = commodityNeedPraiseArray[i].toString();
				String beginTime = beginTimeArray[i].toString();
				String endTime = endTimeArray[i].toString();
				String commoditys = commodityIdArray[i].toString();
				JSONArray objArray = eatingCommodityListCreate(couponId, commodityLimit, commodityNeedPraise, beginTime, endTime, commoditys, storeCode);
				JSONObject obj = new JSONObject();
				Date b = DateUtil.getDate(beginTime, "yyyy-MM-dd HH:mm:ss");
				Date e = DateUtil.getDate(endTime, "yyyy-MM-dd HH:mm:ss");
				if(b.before(now) && e.after(now)){
					obj.put("state", 1);
				}
				obj.put("beginTime", beginTime);
				obj.put("endTime", endTime);
				obj.put("commodityList", objArray);
				if(objArray.size()>0){
					commodityList.add(obj);
				}
			}
		}else{
			String couponId = couponIds.toString();
			String commodityLimit = commodityLimits.toString();
			String commodityNeedPraise = commodityNeedPraises.toString();
			String beginTime = beginTimes.toString();
			String endTime = endTimes.toString();
			String commoditys = commodityIds.toString();
			commodityIds = commodityIds.toString();
			JSONArray objArray = eatingCommodityListCreate(couponId, commodityLimit, commodityNeedPraise, beginTime, endTime, commoditys, storeCode);
			JSONObject obj = new JSONObject();
			Date b = DateUtil.getDate(beginTime, "yyyy-MM-dd HH:mm:ss");
			Date e = DateUtil.getDate(endTime, "yyyy-MM-dd HH:mm:ss");
			if(b.before(now) && e.after(now)){
				obj.put("state", 1);
			}
			obj.put("beginTime", beginTime);
			obj.put("endTime", endTime);
			obj.put("commodityList", objArray);
			if(objArray.size()>0){
				commodityList.add(obj);
			}
		}
		return commodityList;
	}
	
	private JSONArray eatingCommodityListCreate(String couponId,String commodityLimit,String commodityNeedPraise,String beginTime,String endTime,String commoditys,String storeCode){
		JSONArray objArray = new JSONArray();
		String[] couponIds = couponId.split(",");
		String[] commodityLimits = commodityLimit.split(",");
		String[] commodityNeedPraises = commodityNeedPraise.split(",");
		String[] commodityIds = commoditys.split(",");
		Map<String,YxRemoteCommodity> yrMap = remoteCommodityService.queryRemoteCommodityFromInterface(storeCode, commoditys, "tryEating");
		Map<String, YxPoolCommodity> listMap = baseCommodityService.mapCommodityAllInfo(commodityIds, storeCode);
		for(int i = 0; i < couponIds.length; i++) {
			JSONObject proObj = new JSONObject();
			proObj.put("beginTime", beginTime);
			proObj.put("endTime", endTime);
			proObj.put("commodityNeedPraise", commodityNeedPraises[i]);
			proObj.put("commodityLimit", commodityLimits[i]);
			if(listMap.containsKey(commodityIds[i])){
				YxPoolCommodity pc = listMap.get(commodityIds[i]);
				int limit = 10;
				try{
					if(Integer.parseInt(commodityLimits[i])<limit){
						limit = Integer.parseInt(commodityLimits[i]);
					}
				}catch(Exception e){
					logger.info("Integer.parseInt error"+commodityLimits);
				}
				List<JSONObject> userList = userInfoService.queryReceivedUser(pc.getCommodityId(),storeCode,limit);
				proObj.put("userList", userList);//接口不需要此信息
				proObj.put("commodityId", pc.getCommodityId());
				proObj.put("subTitle", pc.getPromoteText());
				proObj.put("picUrl", pc.getPicUrl());
				proObj.put("linkType", 2);
				proObj.put("linkUrl", pc.getCommodityId());
				proObj.put("title", pc.getTitle());
				proObj.put("price", pc.getPrice());
				proObj.put("unit", pc.getUnit());
				proObj.put("marketPrice", pc.getMarketPrice());
				proObj.put("specWeight", pc.getSpecWeight());
				proObj.put("hasService", pc.getHasService());
				proObj.put("scriptTitle", pc.getScriptTitle());
				proObj.put("propertyTitle", pc.getPropertyTitle());
				proObj.put("commodityCouponMap", pc.getCommodityCouponMap());
				proObj.put("stockSum", pc.getStockSum());
				proObj.put("isNew", pc.getIsNew());
				if(yrMap.get(commodityIds[i])!=null){
					Date fsDate = yrMap.get(commodityIds[i]).getAddOnDate();
					proObj.put("remark", DateUtil.getDate(fsDate, "MM月dd日"));
				}
				
				objArray.add(proObj);
			}
		}
		return objArray;
	}
	
	
}
