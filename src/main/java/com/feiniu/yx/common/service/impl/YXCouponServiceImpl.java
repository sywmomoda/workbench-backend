package com.feiniu.yx.common.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.YXCouponService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.DateUtil;
import com.feiniu.yx.util.HttpTookit;

@Service
public class YXCouponServiceImpl implements YXCouponService {
	
	private static final Logger logger =Logger.getLogger(YXCouponServiceImpl.class);

	private static final String COUPON_API_ADDRESS =SystemEnv.getProperty("coupon.api.host")+"/coupon/outerSrv/getCouponInfo";

	private static final String COUPON_COMMDOITY_ADDRESS = SystemEnv.getProperty("coupon.api.host")+"/coupon/outerSrv/getCouponsReceivable4Detail";
	
	@Override
	public JSONObject getCouponInfoByCouponIds(String couponIds) {
		if (couponIds == null) {
			return null;
		}
		if (StringUtils.isBlank(couponIds.trim())) {
			return null;
		}
		JSONObject con= new JSONObject();
		con.put("couponIds", couponIds);
		Map<String,String> paramMap = new HashMap<>();
		paramMap.put("param", con.toJSONString());
		
		JSONObject resJson;
		try{
			String res  = HttpTookit.doPost(COUPON_API_ADDRESS, paramMap);
			if(null == res){
				 logger.error("getCouponInfoByCouponIds error! res is null!");
				 return null;
			}
			resJson = JSONObject.parseObject(res);
			
		}catch(Exception e){
			logger.error("getCouponInfoByCouponIds error!",e);
			return null;
		}	
		
		if(null == resJson){
			logger.error("getCouponInfoByCouponIds error! resJson is null!");
			return null;
		}

		String code = resJson.getString("code"); 
		if(!code.equals("200")){
			logger.error("getCouponInfoByCouponIds error! " + resJson.toJSONString());
			return null;
		}
		JSONObject data = resJson.getJSONObject("data");
		Object obj = data.get("list");//处理list为空字符串问题
		if(null == obj || !(obj instanceof List)){
			data.remove("list");
		}
		return data;
	}
	
	@Override
	public List<JSONObject> setModuleCoupon(String couponIds,Map<String, String> describeMap){
		JSONObject data = getCouponInfoByCouponIds(couponIds);
		List<JSONObject> retList = new ArrayList<JSONObject>();
		if (null == data) {
			return retList;
		}
		JSONArray couponList = data.getJSONArray("list");
		if (couponList == null || couponList.size() == 0) {
			return retList;
		}

		for (int i = 0; i < couponList.size(); i++) {
			JSONObject temp = couponList.getJSONObject(i);
			if (null == temp) {
				continue;
			}

			boolean isTure = "2".equals(temp.getString("activityStatus"))
					&& !"10".equals(temp.getString("discountType"));
			if (!isTure) {
				continue;
			}
			JSONObject coup = new JSONObject();
			String couponId = temp.getString("couponId");
			coup.put("couponId", couponId);
			coup.put("couponName", temp.getString("title"));
			
			coup.put("couponType", temp.getIntValue("discountType") == 5 ? 2: 1);//5礼品券，7优惠券，10组合券
			if(temp.getIntValue("discountType") == 7 && temp.getIntValue("couponType") == 4){//单品券直降
				coup.put("couponType", 3);
			}else if(temp.getIntValue("discountType") == 7 && temp.getIntValue("couponType") == 5){//单品券固定金额
				coup.put("couponType", 4);
			}
			coup.put("couponValue", temp.getString("vouchersDiscount"));
			coup.put("couponThreshold", temp.getString("vouchersPrice"));
			String couponDescribe = describeMap.get(couponId);
			//兼容组合券
			if (couponDescribe == null) {
				String supCouponId = temp.getString("supCouponId");
				if (supCouponId != null) {
					couponDescribe = describeMap.get(supCouponId);
				}
			}
			coup.put("couponDescribe", couponDescribe != null ? couponDescribe: "");
			
			String useDeadlineType = temp.getString("useDeadlineType");
			if("1".equals(useDeadlineType)){
				String uet = temp.getString("useEndTimestamp");
				Long useEndTimestamp = StringUtils.isBlank(uet) ? 0L : Long.valueOf(uet);
				if(useEndTimestamp.longValue() > 0){
					String useEndDate = DateUtil.getDate(new Date(useEndTimestamp),"yyyy.MM.dd");	
					coup.put("endInfo", useEndDate+"前可用");
				}
				
			}else if("2".equals(useDeadlineType)){
				String invalidDays = temp.getString("invalidDays");
				coup.put("endInfo", "领取后"+invalidDays+"天内可用");
			}
			
			
			retList.add(coup);
		}
		retList = orderListByCouponIds(couponIds,retList);
		return retList;
	}
	
	
	
	private List<JSONObject> orderListByCouponIds(String couponIds, List<JSONObject> retList){
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		for(String cId:couponIds.split(",")){
			for(JSONObject obj:retList){
				String couponId = obj.getString("couponId");
				if(couponId!=null && couponId.equals(cId)){
					returnList.add(obj);
					break;
				}
			}
		}
		return returnList;
	}
	
	/**
	 * 通过接口获得优惠券信息
	 * @param ids
	 * @param storeCode
	 * @return
	 */
	public JSONArray getJAByCouponInfoAPI(String[] ids, String storeCode) {
		if(ids == null || ids.length <= 0 || StringUtils.isBlank(storeCode)) {
			return null;
		}
		JSONObject params = new JSONObject();
		params.put("skuSeq", StringUtils.join(ids, ","));
		params.put("storeId", storeCode);
		JSONArray list = null;
		try {
			String result = HttpTookit.doPost(COUPON_COMMDOITY_ADDRESS, "param", params.toJSONString());
			if (result == null) {
				return null;
			}
			JSONObject resJson = JSONObject.parseObject(result);
			if (resJson == null) {
				return null;
			}
			String code = resJson.getString("code"); 
			if(!code.equals("200")){
				return null;
			}
			list = resJson.getJSONArray("list");
		}catch(Exception e){
	         logger.error("COUPON_COMMDOITY_ADDRESS error", e);
		}
		return list;
	}
	

}
