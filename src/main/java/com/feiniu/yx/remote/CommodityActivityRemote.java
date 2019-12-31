package com.feiniu.yx.remote;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.HttpTookit;

/**
 * 行销活动远程接口
 * @author:tongwenhuan
 * @time:2018年12月6日 下午3:37:48
 */
public class CommodityActivityRemote {
	
	private static Logger logger = Logger.getLogger(CommodityActivityRemote.class);
	
	//查商品行销活动类型接口
	private static final String ACTIVITY_TYPE_API = SystemEnv.getProperty("yxCommodity.activityType");
	//行销活动列表接口
	private final static String URL_RemotePage = SystemEnv.getProperty("yxPromotionPage");
	private final static String ACTIVITY_Commodity_URL = SystemEnv.getProperty("yxcommodity.activityPage");
	
	//activityChannel  活动终端0门店1大润发优鲜2淘鲜达3欧尚
	
	
	/**
	 * 批量查商品新用户行销活动信息
	 * @param ids 商品id
	 * @param storeCode 门店codes
	 * @return
	 */
	
	public static Map<String, JSONObject> mapCommodityActivity(String[] ids,String storeCode,int newSign) {
		if (ids == null || ids.length <= 0 || StringUtils.isBlank(storeCode)) {
			return null;
		}
		JSONObject params = new JSONObject();
		params.put("productIdList", ids);
		params.put("storeId", storeCode);
		params.put("activityChannel", 1);
		JSONObject userInfo = new JSONObject();
		userInfo.put("uId", "");
		userInfo.put("newSign", newSign);//老用户
		params.put("userInfo", userInfo);
		Map<String, JSONObject> m = null;
		try {
			String result = HttpTookit.doPost(ACTIVITY_TYPE_API+"/v1",params.toJSONString());
			JSONObject jo = JSON.parseObject(result);
			if (jo == null) {
				return null;
			}
			String code = jo.getString("code");
			if (!"200".equals(code)) {
				return null;
			}
			JSONArray body = jo.getJSONArray("body");
			if (body == null) {
				return m;
			}
			m = new HashMap<String, JSONObject>();
			for (int i = 0; i < body.size(); i++) {
				JSONObject activity = body.getJSONObject(i);
				if (null == activity) {
					continue;
				}
				String commodityId = activity.getString("productId");
				JSONObject singleResult = new JSONObject();
				String activityInfoStr = activity.getString("activityInfo");
				String activityContent = null;
				Integer card = null;  //card = 6 代表是新的标
				if (StringUtils.isNotBlank(activityInfoStr)) {
					JSONObject activityInfo = JSONObject.parseObject(activityInfoStr);
					activityContent = activityInfo.getString("activityContent");
					singleResult.put("activityContent", activityContent);
					card = activityInfo.getInteger("card");
					singleResult.put("card", card);	
					singleResult.put("activity_card", card);
				}
				String singleString = activity.getString("singleActivity");
				if (StringUtils.isBlank(singleString)) {
					m.put(commodityId, singleResult);
					continue;
				}
				
				JSONObject singleActivity = JSONObject.parseObject(singleString);
				if (null == singleActivity) {
					m.put(commodityId, singleResult);
					continue;
				}
				String buyNSendOneStr = singleActivity.getString("buyNSendOne");
				if (StringUtils.isNotBlank(buyNSendOneStr)) {
					JSONObject buyNSendOne = JSONObject.parseObject(buyNSendOneStr);
					activityContent = buyNSendOne.getString("activityContent");
					singleResult.put("activityContent", activityContent);
				}
				String singleSpecialOfferStr = singleActivity.getString("singleSpecialOffer");
				if (StringUtils.isNotBlank(singleSpecialOfferStr)) {
					JSONObject singleSpecialOffer = JSONObject.parseObject(singleSpecialOfferStr);
					singleResult.put("preferentialType",singleSpecialOffer.getString("preferentialType"));
					singleResult.put("preferentialValue",singleSpecialOffer.getString("preferentialValue"));
					Float costPrice = singleSpecialOffer.getFloat("costPrice");
					Float price = singleSpecialOffer.getFloat("price");
					singleResult.put("costPrice", costPrice);
					singleResult.put("price", price);
					Integer singleSpecialOfferCard = singleSpecialOffer.getInteger("card");
					if(null == card || card.intValue() != 6){
						card = singleSpecialOfferCard;
						singleResult.put("card", card);	
					}
					singleResult.put("singleSpecialOffer_card", singleSpecialOfferCard);
				}
				
				m.put(commodityId, singleResult);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return m;
	}

	/**
	 * { 
	 * "activityId": "769",//活动id //非必填 
	 * "activityChannel": 1,//业务类型 
	 * "title": "测试",//活动名称 //非必填 
	 * "activityTypes":[12,23,11],//活动类型过滤 
	 * "pageNo": 1,//页号,
	 * "pageSize": 10//分页数量 
	 * } 条件查询行销活动列表
	 * 
	 * @return
	 */
	public static JSONObject listActivity(JSONObject param) {
		/*
		 * JSONObject paramsJSON = new JSONObject(); paramsJSON.put("pageNo", pageNo);
		 * paramsJSON.put("pageSize", pageSize); paramsJSON.put("activityChannel", 1);
		 * if(StringUtils.isNotBlank(id)){ paramsJSON.put("activityId", id); }
		 * if(StringUtils.isNotBlank(name)){ paramsJSON.put("title", name); }
		 */
		JSONObject jo = null;
		try {
			String result = HttpTookit.doPost(URL_RemotePage, param.toJSONString());
			if(result == null){
				return null;
			}
			JSONObject remoteObject = JSONObject.parseObject(result);
			String code = remoteObject.getString("code");
			if(!"200".equals(code)){
				return null;
			}
			jo = remoteObject.getJSONObject("body");
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		return jo;
	}
	
	/**
	 * 条件查询行销活动列表
	 * @return
	 */
	public static Map<String, JSONObject> mapCommodityActivityV1(String storeCode,String[] commodityIds) {
		if (commodityIds == null || commodityIds.length <= 0 || StringUtils.isBlank(storeCode)) {
			return null;
		}
		JSONObject params = new JSONObject();
		params.put("productIdList", commodityIds);
		params.put("storeId", storeCode);
		params.put("activityChannel", 1);
		JSONObject userInfo =  new JSONObject();
		userInfo.put("uId", "");
		userInfo.put("newSign", 1);
		params.put("userInfo", userInfo);
		Map<String, JSONObject> m = null;
		try {
			String result = HttpTookit.doPost(ACTIVITY_TYPE_API+"/v1",params.toJSONString());
			JSONObject jo = JSON.parseObject(result);
			if (jo == null) {
				return null;
			}
			String code = jo.getString("code");
			if (!"200".equals(code)) {
				return null;
			}
			JSONArray body = jo.getJSONArray("body");
			if (body == null) {
				return m;
			}
			m = new HashMap<String, JSONObject>();
			for (int i = 0; i < body.size(); i++) {
				JSONObject activity = body.getJSONObject(i);
				if (null == activity) {
					continue;
				}
				String commodityId = activity.getString("productId");
				JSONObject singleResult = new JSONObject();
				String activityInfoStr = activity.getString("activityInfo");
				String activityContent = null;
				if (StringUtils.isNotBlank(activityInfoStr)) {
					JSONObject activityInfo = JSONObject.parseObject(activityInfoStr);
					activityContent = activityInfo.getString("activityContent");
					singleResult.put("activityContent", activityContent);
				}
				String singleString = activity.getString("singleActivity");
				if (StringUtils.isBlank(singleString)) {
					singleResult.put("activityContent", activityContent);
					continue;
				}
				JSONObject singleActivity = JSONObject.parseObject(singleString);
				if (null == singleActivity) {
					singleResult.put("activityContent", activityContent);
					continue;
				}
				String buyNSendOneStr = singleActivity.getString("buyNSendOne");//买N赠1活动
				if (StringUtils.isNotBlank(buyNSendOneStr)) {
					JSONObject buyNSendOne = JSONObject.parseObject(buyNSendOneStr);
					activityContent = buyNSendOne.getString("activityContent");
					singleResult.put("activityContent", activityContent);
					singleResult.put("card",buyNSendOne.getString("card"));
					singleResult.put("singleType","buyNSendOne");
				}
				String singleSpecialOfferStr = singleActivity.getString("singleSpecialOffer");//单品特价活动
				if (StringUtils.isNotBlank(singleSpecialOfferStr)) {
					JSONObject singleSpecialOffer = JSONObject.parseObject(singleSpecialOfferStr);
					singleResult.put("preferentialType",singleSpecialOffer.getString("preferentialType"));
					singleResult.put("preferentialValue",singleSpecialOffer.getString("preferentialValue"));
					singleResult.put("card",singleSpecialOffer.getString("card"));
					singleResult.put("price",singleSpecialOffer.getString("price"));
					singleResult.put("costPrice",singleSpecialOffer.getString("costPrice"));
					singleResult.put("singleType","singleSpecialOffer");
				}
				
				m.put(commodityId, singleResult);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return m;
	}
	
	/**
	 * 根据活动id查商品
	 * @param storeCode
	 * @param activityId
	 * @return
	 */
	public static JSONObject getActivityCommodity(String storeCode,String activityId){
		JSONObject param = new JSONObject();
		param.put("storeId", storeCode);
		param.put("activityId", activityId);
		param.put("poolId", 1);
		param.put("activityChannel", 1);
		JSONObject jo = null;
		try {
			String result = HttpTookit.doPost(ACTIVITY_Commodity_URL,param.toJSONString());
			jo = JSON.parseObject(result);
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		if (jo == null) {
			return null;
		}
		String code = jo.getString("code");
		if (!"200".equals(code)) {
			return null;
		}
		JSONObject body = jo.getJSONObject("body");
		if (body == null) {
			return null;
		}
		String activityInfo = body.getString("activityInfo");
		if(StringUtils.isBlank(activityInfo)) {
			return null;
		}
		JSONObject infoObject  = JSONObject.parseObject(activityInfo);
		if(null == infoObject || infoObject.size() == 0) {
			return null;
		}
		Date startTime = infoObject.getDate("startTime");
		Date endTime = infoObject.getDate("endTime");
		String activityRule = infoObject.getString("activityRuleList");
		if(StringUtils.isBlank(activityRule)) {
			return null;
		}
		JSONArray ruleArray = JSONObject.parseArray(activityRule);
		if(null == ruleArray || ruleArray.size() == 0) {
			return null;
		}
		JSONObject sin = null;
		for(int i = 0; i < ruleArray.size(); i++) {
			sin = ruleArray.getJSONObject(i);
			if(null != sin) {
				break;
			}
		}
		String goods = body.getString("goodsList");
		if(StringUtils.isBlank(goods)) {
			return null;
		}
		JSONArray array = JSONArray.parseArray(goods);
		if(null == array || array.size() == 0) {
			return null;
		}
		if(null == sin) {
		  return null;	
		}
		Date now = new Date();
		if(!(now.after(startTime) && now.before(endTime))) {
			return null;
		}
		sin.put("goods", StringUtils.join(array,","));
		return sin;
	}

}
