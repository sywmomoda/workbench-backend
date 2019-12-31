package com.feiniu.yx.core.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.core.SuperCommodityService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.remote.CommodityActivityRemote;
import com.feiniu.yx.util.HttpTookit;

@Service
public class ModuleCommodityServiceImpl implements SuperCommodityService {
	
	private Logger logger = Logger.getLogger(ModuleCommodityServiceImpl.class);
	
	//独家自制
	private final static String COMMODITY_OWN_HOMEMADE = SystemEnv.getProperty("yxCommodity.own.homeMade");
	//独家自制id
	private final static String COMMODITY_OWN_AVSEQ = SystemEnv.getProperty("yxCommodity.own.avSeq");
	//独家自制查询id
	private final static String COMMODITY_OWN_ATSEQ = SystemEnv.getProperty("yxCommodity.own.atSeq");
	
	//自有品牌
	private final static String COMMODITY_OWN_BRAND = SystemEnv.getProperty("yxCommodity.own.brand");
	
	//优惠券接口
	private static final String COUPON_COMMDOITY_ADDRESS = SystemEnv.getProperty("coupon.api.host")+"/coupon/outerSrv/getSingleCouponsReceivable";
	
	//线上专供
	private static final String GOODS_CENTER_HOST= SystemEnv.getProperty("goods.center.host");

	@Autowired
	private BaseCommodityServiceImpl baseCommodityServiceImpl;
	
	public BaseCommodityServiceImpl getBaseCommodityServiceImpl() {
		return baseCommodityServiceImpl;
	}
	
	@Override
	public void resetParams(List<YxPoolCommodity> list, String storeCode) {
		baseCommodityServiceImpl.resetParams(list, storeCode);
		resetScript(list, storeCode);
	}
	
	/**
	 * 通过接口获得商品基础信息&角标
	 * @param ids
	 * @param storeCode
	 * @return
	 */
	public Map<String, YxPoolCommodity> mapCommodity(String[] ids, String storeCode) {
		List<YxPoolCommodity> list = baseCommodityServiceImpl.listCommodityPriceInfo(ids, storeCode);
		if (list == null) {
			return null;
		}
		resetScript(list, storeCode);
		Map<String, YxPoolCommodity> map = new HashMap<String, YxPoolCommodity>();
		for (YxPoolCommodity c : list) {
			map.put(c.getCommodityId(), c);
		}
		return map;
	}
	
	
	public Map<String, YxPoolCommodity> mapCommodityNOConponScript(String[] ids, String storeCode) {
		List<YxPoolCommodity> list = baseCommodityServiceImpl.listCommodityAllInfo(ids, storeCode);
		if (list == null) {
			return null;
		}
		//行销活动
		Map<String, JSONObject> act_m = CommodityActivityRemote.mapCommodityActivity(ids, storeCode,0);
		//新人专享行销
		Map<String,JSONObject> mapSingleSpecialOffer = CommodityActivityRemote.mapCommodityActivity(ids, storeCode,1);
		//独家自制
		Map<String, JSONObject> ownhomemade_m = getMapByOwnHomeMadeAPI(ids);
		//品牌数组
		String[] brandSeqs = getArrayBrandSeq(list);
		JSONObject brand_jo = getJOByBrandAPI(brandSeqs);
		boolean ohm_is_nonull = (ownhomemade_m != null);
		for (YxPoolCommodity c : list) {
			resetActParam(c, act_m);
			resetSingleSpecialOfferScript(mapSingleSpecialOffer,c);
			boolean isMarketPrice = false; //是否展示市场价
			if(c.getIsSSM()){
				isMarketPrice = true;
			}
			if(c.isDPTJ()){
				isMarketPrice = true;
			}
			if(!isMarketPrice){
				c.setMarketPrice(0f);
			}
			if(c.getPrice().equals(c.getMarketPrice())){
				c.setMarketPrice(0f);
			}
			
			if (ohm_is_nonull) {
				resetOwnHomeMadeParam(c, ownhomemade_m.get(c.getCommodityId()));
			}
			resetBrandParam(c, brand_jo);
		}
		Map<String, YxPoolCommodity> map = new HashMap<String, YxPoolCommodity>();
		for (YxPoolCommodity c : list) {
			map.put(c.getCommodityId(), c);
		}
		return map;
	}
	
	
	
	/**
	 * 设置角标信息
	 * @param list
	 * @param storeCode
	 */
	private void resetScript(List<YxPoolCommodity> list, String storeCode) {
		int length = list.size();
		int total = 100;
		int multiple = length / total ;
		int remainder = length % total;
		for(int j = 1; j <= multiple; j++){
			bacthResetScript(list.subList(total*(j-1), (j)*total),storeCode);
		}
		if(remainder > 0){
			bacthResetScript(list.subList(total*multiple, total*multiple+remainder),storeCode);
		}
	}
	/**
	 * 批量设置角标信息
	 * @param list
	 * @param storeCode
	 */
	private void bacthResetScript(List<YxPoolCommodity> list, String storeCode){
		String[] ids = baseCommodityServiceImpl.arrayIds(list);
		//行销活动
		Map<String, JSONObject> act_m = CommodityActivityRemote.mapCommodityActivity(ids,storeCode,0);
		//新人专享行销
		Map<String,JSONObject> mapSingleSpecialOffer = CommodityActivityRemote.mapCommodityActivity(ids, storeCode,1);
		//独家自制
		Map<String, JSONObject> ownhomemade_m = getMapByOwnHomeMadeAPI(ids);
		//线上专供
		Map<String,String>  onlineOwn = getOnlineScript(ids, storeCode) ;
		//品牌数组
		String[] brandSeqs = getArrayBrandSeq(list);
		JSONObject brand_jo = getJOByBrandAPI(brandSeqs);
		//优惠券
		Map<String, String> coupon_m = getCouponInfoByCommodityId(ids, storeCode);
		boolean ohm_is_nonull = (ownhomemade_m != null);
		for (YxPoolCommodity c : list) {
			resetActParam(c, act_m);
			resetSingleSpecialOfferScript(mapSingleSpecialOffer,c);
			boolean isMarketPrice = false; //是否展示市场价
			if(c.getIsSSM()){
				isMarketPrice = true;
			}
			if(c.isDPTJ()){
				isMarketPrice = true;
			}
			if(!isMarketPrice){
				c.setMarketPrice(0f);
			}
			if(c.getPrice().equals(c.getMarketPrice())){
				c.setMarketPrice(0f);
			}
			resetOnlineOwnParam(c,onlineOwn);
			if (ohm_is_nonull) {
				resetOwnHomeMadeParam(c, ownhomemade_m.get(c.getCommodityId()));
			}
			resetBrandParam(c, brand_jo);
			resetCouponParam(c ,coupon_m);
			
		}
	}
	
	/**
	 * 活动品牌id数组
	 * @param list
	 * @return
	 */
	private String[] getArrayBrandSeq(List<YxPoolCommodity> list) {
		List<String> bss = new ArrayList<String>();
		for (YxPoolCommodity c : list) {
			String brandSeq = c.getBrandSeq();
			if (StringUtils.isNotBlank(brandSeq)) {
				bss.add(brandSeq);
			}
		}
		String[] strings = new String[bss.size()];
		return (String[]) bss.toArray(strings);
	}
	
	/**
	 * 通过接口取独家自制信息
	 * @param ids
	 * @return
	 */
	private Map<String, JSONObject> getMapByOwnHomeMadeAPI(String[] ids) {
		if(ids == null || ids.length <= 0) {
			return null;
		}
		JSONObject params = new JSONObject();
		params.put("goodsNos", ids);
		params.put("atSeq", COMMODITY_OWN_ATSEQ);
		String result = null;
		try {
			result = HttpTookit.doPost(COMMODITY_OWN_HOMEMADE, "data", params.toJSONString());
		}catch(Exception e){
	         logger.error("getMapByOwnHomeMadeAPI error", e);
		}
		JSONObject jo = JSON.parseObject(result);
		if (jo == null) {
			return null;
		}
		String success = jo.getString("success");
        if(!"1".equals(success)){
            return null;
        }
        JSONArray arr = null;
        try {
        	arr = jo.getJSONArray("data");
        } catch(Exception e) {
        	
        }
        if (arr == null || arr.size() == 0) {
        	return null;
        }
        
        Map<String, JSONObject> m = new HashMap<String, JSONObject>();
        for (int i = 0; i < arr.size(); i++) {
        	JSONObject o = arr.getJSONObject(i);
        	String goodsNo = o.getString("goodsNo");
        	m.put(goodsNo, o);
        }
        return m;
	}
	
	/**
	 * 通过接口取品牌信息
	 * @param brandSeqs 品牌id
	 * @return
	 */
	private JSONObject getJOByBrandAPI(String[] brandSeqs) {
		if(brandSeqs == null || brandSeqs.length <= 0) {
			return null;
		}
		JSONObject params = new JSONObject();
		params.put("avSeqs", brandSeqs);
		String result = null;
		try {
			result = HttpTookit.doPost(COMMODITY_OWN_BRAND, "data", params.toJSONString());
		}catch(Exception e){
	         logger.error("getJOByBrandAPI error", e);
		}
		JSONObject jo = JSON.parseObject(result);
		if (jo == null) {
			return null;
		}
		String success = jo.getString("success");
        if(!"1".equals(success)){
            return null;
        }
        JSONObject data = jo.getJSONObject("data");
        return data;
	}
	
	/**
	 * 通过接口获得优惠券信息
	 * @param ids
	 * @param storeCode
	 * @return
	 */
	private JSONArray getJAByCouponInfoAPI(String[] ids, String storeCode) {
		if(ids == null || ids.length <= 0 || StringUtils.isBlank(storeCode)) {
			return null;
		}
		JSONObject params = new JSONObject();
		params.put("skuSeqs", StringUtils.join(ids, ","));
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
	
	/**
	 * 获得优惠券信息
	 * @param ids
	 * @param storeCode
	 * @return
	 */
	private Map<String,String> getCouponInfoByCommodityId(String[] ids, String storeCode){
		JSONArray list = getJAByCouponInfoAPI(ids, storeCode);
		Map<String,String> couMap = new HashMap<String,String>();
		if(null == list){
			return couMap;
		}
		for(int i = 0, len = list.size(); i < len; i++){
			JSONObject single = list.getJSONObject(i);
			int discountType = single.getIntValue("discountType");
			int couponType = single.getIntValue("couponType");			
			//0:不限制 3:限新用户 4:限老用户
			Integer limitUser = single.getInteger("limitUser");
			//是否单品券
			if(discountType != 7){
				continue;
			}
			if(couponType != 4 && couponType != 5){
				continue;
			}
			JSONArray skuSeqs = single.getJSONArray("skuSeqs");
			
			for(String id : ids){
				if(!skuSeqs.contains(id)){
					continue;
				}
				Float vouchersDiscount = single.getFloat("vouchersDiscount");
				if(null == vouchersDiscount){
					continue;
				}
				String key = id+"_"+ couponType;
				if(couMap.containsKey(key)){
					String value = couMap.get(key);
					String vou = value.split("_")[0];
					boolean flag = couponType == 5 ? vouchersDiscount < Float.valueOf(vou) : 
					vouchersDiscount > Float.valueOf(vou);	
					if(flag){
						couMap.put(key, vouchersDiscount+"_"+limitUser);
					}
					continue;
				}
				couMap.put(key, vouchersDiscount+"_"+limitUser);
			}
		}
		return couMap;
	}
	
	
	
	/**
	 * 线上专享
	 * @param c
	 * @param jo
	 */
	private void resetOnlineOwnParam(YxPoolCommodity c, Map<String,String> map) {
		if (map == null) {
			return;
		}
		String falg = map.get(c.getCommodityId());
		if(null == falg || !falg.equals("true")) {
			return;
		}
		c.setOnlineOwnTitle("线上专供");
	}
	
	/**
	 * 设置独家自制
	 * @param c
	 * @param jo
	 */
	private void resetOwnHomeMadeParam(YxPoolCommodity c, JSONObject jo) {
		if (jo == null) {
			return;
		}
		String avSeq = jo.getString("avSeq");
		if (StringUtils.isBlank(avSeq)) {
			return;
		}
		if (!avSeq.equals(COMMODITY_OWN_AVSEQ)) {
			return;
		}
		String[] propertyTitle = c.getPropertyTitle();
		String[] new_propertyTitle;
		if(propertyTitle.length > 0){
        	String tt = propertyTitle[0];
        	new_propertyTitle = new String[]{"","独家自制"};
        	new_propertyTitle[0] = tt;
        } else {
        	new_propertyTitle = new String[]{"独家自制"};
        }
		c.setPropertyTitle(new_propertyTitle);
	}
	
	/**
	 * 设置行销活动角标
	 * @param c 商品
	 * @param m 角标信息
	 */
	private void resetActParam(YxPoolCommodity c, Map<String, JSONObject> m) {
		//角标级别：印花促销>领券优惠>买N赠1>行销活动>促销2-9或单品特价>新品
		
		Integer isNew = c.getIsNew();
		String scriptTitle = isNew == 1 ? "新品" : "";
		//先判断是否有促销价
		if(c.getIsSSM()){
			if(c.getPriceType() != null && c.getPriceType().intValue() == 1){ 
	        	c.setPropertyTitle(new String[]{"超值印花"});
	        }
			String promoScript = resetPromoScript(c);
			if(StringUtils.isNotBlank(promoScript)){
				scriptTitle = promoScript;
			}
		}
		if (m == null) {
			c.setScriptTitle(scriptTitle);
			return;
		}
		JSONObject jo = m.get(c.getCommodityId());

		//再判断是否行销活动商品
		if (jo == null) {
			c.setScriptTitle(scriptTitle);
			return;
		}
		String singleProductScript = resetSingleProductScript(jo,c);
		c.setDptjCard(jo.getInteger("card"));
		if(StringUtils.isNotBlank(singleProductScript)){
			scriptTitle = singleProductScript;
		}
		/*Integer activityType = jo.getInteger("activityType");
		if(activityType.intValue() == -1){
			c.setScriptTitle(scriptTitle);
			return;
		}		
		c.setActivityType(activityType);*/
		String ruleDesc = jo.getString("activityContent");
		if(StringUtils.isBlank(ruleDesc)){
			c.setScriptTitle(scriptTitle);
			return;
		}
		String[] ruleArr = ruleDesc.split(",");
		scriptTitle = ruleArr[ruleArr.length-1];
		c.setScriptTitle(scriptTitle);
	}
	
	/**
	 * 设置促销2-9
	 * @param c
	 * @param jo
	 */
	private String resetPromoScript(YxPoolCommodity c) {
		Float marketPrice = c.getMarketPrice();
		Float promoPrice = c.getPrice();
		String title = "";
		Integer saleLevel = c.getPriceType();
		if (c.getIsSSM() && saleLevel >= 2 && saleLevel <= 9) {
			BigDecimal marketPriceB = new BigDecimal(marketPrice.toString());
			BigDecimal promoPriceB = new BigDecimal(promoPrice.toString());
			Float differ = marketPriceB.subtract(promoPriceB).floatValue();
			boolean flagSL = promoPriceB.divide(marketPriceB, 2,
					BigDecimal.ROUND_DOWN).floatValue() >= 0.95f ? false: true;
			if (differ.floatValue() > 0f && flagSL) {
				title = "省"+ baseCommodityServiceImpl.processPriceZero(differ.toString()) + "元";
			}
		}
		return title;
	}
	
	
	/**
	 * 新人专享
	 * @param mapSingleSpecialOffer
	 * @param c
	 */
	private void resetSingleSpecialOfferScript(Map<String,JSONObject> mapSingleSpecialOffer,YxPoolCommodity c){
		if(null == mapSingleSpecialOffer) {
		  return;
		}
		String commodityId = c.getCommodityId();
		JSONObject singleSpecialOffer =  mapSingleSpecialOffer.get(commodityId);
		if(null != singleSpecialOffer){
			Integer card = singleSpecialOffer.getInteger("card");
			c.setDptjCard(card);
			String content = singleSpecialOffer.getString("activityContent");
			if(StringUtils.isBlank(content)) {
				content = resetSingleProductScript(singleSpecialOffer,c);
			}
			c.setDptjScript(content);

		}
		
	}
	
	/**
	 * 设单品
	 * @param jo
	 * @param c
	 * @return
	 */
	private String resetSingleProductScript(JSONObject jo,YxPoolCommodity c){
		Float marketPrice = c.getMarketPrice();
		BigDecimal marketPriceB = new BigDecimal(marketPrice.toString());
		String title ="";
		if(null == jo){
			return title;
		}
		Integer discountType = jo.getInteger("preferentialType");
		if(null == discountType){
			return title;
		}
		Float littlePrice = jo.getFloat("price");
		BigDecimal promoPriceB = new BigDecimal(littlePrice.toString());
		Integer  singleSpecialOfferCard = jo.getInteger("singleSpecialOffer_card");
		boolean isSpecialOfferNew = (null != singleSpecialOfferCard && singleSpecialOfferCard.intValue() != 6);
		if(isSpecialOfferNew){
			if(promoPriceB.subtract(new BigDecimal(c.getPrice())).floatValue() < 0f){
				c.setPrice(littlePrice);
			}
			Float costPrice = jo.getFloat("costPrice");
			c.setMarketPrice(costPrice);
		}
		//1、一口价 2、折扣 3、减钱
		if(discountType.intValue() == 1){
			if(marketPrice.equals(0f)){
				return title;
			}
			Float differ  = marketPriceB.subtract(promoPriceB).floatValue();
			boolean flagSL  = promoPriceB.divide(marketPriceB, 2, BigDecimal.ROUND_DOWN).floatValue() >= 0.95f ? false : true;
			if(differ.floatValue() > 0f && flagSL){
				title ="省" + baseCommodityServiceImpl.processPriceZero(differ.toString())+"元";
			}
			if(isSpecialOfferNew) {
				c.setDPTJ(true); //单品特价
			} 

		}else if(discountType.intValue() == 2){
			if(marketPrice.equals(0f)){
				return title;
			}
			Integer discount = jo.getInteger("preferentialValue");
			BigDecimal discountPriceB = marketPriceB.multiply(new BigDecimal(discount*0.01)).setScale(2, BigDecimal.ROUND_DOWN);
			if(discountPriceB.floatValue() > 0f){
				if(discount < 95){
					Integer newDiscount = discount  % 10 == 0 ? discount / 10 : discount;
					title =newDiscount+"折";
				}
				if(isSpecialOfferNew) {
					c.setDPTJ(true); //单品特价
				} 
			}
		}else if(discountType.intValue() == 3){
			Float reduceMoney = jo.getFloat("preferentialValue");
			if(marketPrice.equals(0f)){
				return title;
			}
			BigDecimal reduceMoneyB = new BigDecimal(reduceMoney.toString());
			BigDecimal singPriceB=  marketPriceB.subtract(reduceMoneyB);
			if(singPriceB.floatValue() > 0f){
				boolean flagSL  = singPriceB.divide(marketPriceB, 2, BigDecimal.ROUND_DOWN).floatValue() >= 0.95f ? false : true;
				if(reduceMoney.floatValue() > 0f && flagSL){
					title ="省"+baseCommodityServiceImpl.processPriceZero(reduceMoney.toString())+"元";
				}
				if(isSpecialOfferNew) {
					c.setDPTJ(true); //单品特价
				} 
			}
		}
		return title;
	}
	
	
	
	/**
	 * 设单品
	 * @param jo
	 * @param c
	 * @return
	 */
	/*private String resetSingleProductScript(JSONObject jo,YxPoolCommodity c){
		Float littlePrice = c.getPrice();
		Float marketPrice = c.getMarketPrice();
		BigDecimal marketPriceB = new BigDecimal(marketPrice.toString());
		String title ="";
		if(null == jo){
			return title;
		}
		Integer discountType = jo.getInteger("preferentialType");
		if(null == discountType){
			return title;
		}
		//1、一口价 2、折扣 3、减钱
		if(discountType.intValue() == 1){
			if(marketPrice.equals(0f)){
				return title;
			}
			Float discountPrice = jo.getFloat("preferentialValue");
			if(discountPrice.floatValue() < littlePrice.floatValue()){
				littlePrice = discountPrice;
				c.setPrice(Float.valueOf(baseCommodityServiceImpl.processPriceZero(littlePrice.toString())));
				BigDecimal promoPriceB = new BigDecimal(littlePrice.toString());
				Float differ  = marketPriceB.subtract(promoPriceB).floatValue();
				boolean flagSL  = promoPriceB.divide(marketPriceB, 2, BigDecimal.ROUND_HALF_UP).floatValue() >= 0.95f ? false : true;
				if(differ.floatValue() > 0f && flagSL){
					title ="省" + baseCommodityServiceImpl.processPriceZero(differ.toString())+"元";
				}
				c.setDPTJ(true); //单品特价
			}

		}else if(discountType.intValue() == 2){
			if(marketPrice.equals(0f)){
				return title;
			}
			Integer discount = jo.getInteger("preferentialValue");
			
			BigDecimal discountPriceB = marketPriceB.multiply(new BigDecimal(discount*0.01)).setScale(2, BigDecimal.ROUND_DOWN);
			if(discountPriceB.floatValue() > 0f && discountPriceB.floatValue() < c.getPrice().floatValue()){
				c.setPrice(Float.valueOf(baseCommodityServiceImpl.processPriceZero(discountPriceB.toString())));
				if(discount < 95){
					Integer newDiscount = discount  % 10 == 0 ? discount / 10 : discount;
					title =newDiscount+"折";
				}
				c.setDPTJ(true); //单品特价
			}
		}else if(discountType.intValue() == 3){
			Float reduceMoney = jo.getFloat("preferentialValue");
			if(marketPrice.equals(0f)){
				return title;
			}
			BigDecimal reduceMoneyB = new BigDecimal(reduceMoney.toString());
			BigDecimal singPriceB=  marketPriceB.subtract(reduceMoneyB);
			if(singPriceB.floatValue() > 0f && singPriceB.floatValue() < c.getPrice().floatValue()){
				boolean flagSL  = singPriceB.divide(marketPriceB, 2, BigDecimal.ROUND_DOWN).floatValue() >= 0.95f ? false : true;
				if(reduceMoney.floatValue() > 0f && flagSL){
					title ="省"+baseCommodityServiceImpl.processPriceZero(reduceMoney.toString())+"元";
				}
				c.setPrice(Float.valueOf(baseCommodityServiceImpl.processPriceZero(singPriceB.toString())));
				c.setDPTJ(true); //单品特价
			}
		}
		return title;
	}*/
	
	
	/**
	 * 设置品质自采
	 * @param c
	 * @param jo
	 */
	private void resetBrandParam(YxPoolCommodity c, JSONObject jo) {
		if (jo == null) {
			return;
		}
		String[] propertyTitle = c.getPropertyTitle();
		for (String t : propertyTitle) {
			if (t.equals("独家自制")) {
				return;
			}
		}
		String isb = jo.getString(c.getBrandSeq());
		if (!"1".equals(isb)) {
			return;
		}
		String[] new_propertyTitle;
		if(propertyTitle.length > 0){
        	String tt = propertyTitle[0];
        	new_propertyTitle = new String[]{"","品质自采"};
        	new_propertyTitle[0] = tt;
        } else {
        	new_propertyTitle = new String[]{"品质自采"};
        }
		c.setPropertyTitle(new_propertyTitle);
	}
	
	/**
	 * 设置优惠券信息
	 * @param c 商品
	 * @param couponMap 优惠券信息
	 */
	private void resetCouponParam(YxPoolCommodity c, Map<String, String> couponMap) {
		 String couponPro4 = couponMap.get(c.getCommodityId()+"_"+"4"); //4:单品限定（直降）
		 String couponPro5 = couponMap.get(c.getCommodityId()+"_"+"5"); //5:指单品券（固定金额）
		 Float type4Num = 0f;
		 Float type5Num = 0f;
		 if(StringUtils.isNotBlank(couponPro4)){
			 String value = couponPro4.split("_")[0];
	         value = baseCommodityServiceImpl.processPriceZero(value);
			 type4Num =  Float.valueOf(value);
		 }
		 if(StringUtils.isNotBlank(couponPro5)){
			 String value = couponPro5.split("_")[0];
      		 type5Num = new BigDecimal(c.getPrice().toString()).subtract(new BigDecimal(value)).floatValue();
		 }
		 Float num = 0f;
		 String couponType = "4";
		 String limitUser ="0" ;
		 if(type4Num > 0){
			 num = type4Num; 
			 limitUser = couponPro4.split("_")[1];
		 }
		 if(type5Num > 0 && type5Num > type4Num){
			 num = type5Num; 
			 couponType = "5";
			 limitUser = couponPro5.split("_")[1];
		 }
		 if(num == 0){
			 return;
		 }
		 Map<String,String> pam = new HashMap<String,String>();
		 pam.put("value", String.valueOf(num));
		 pam.put("limitUser", limitUser); //4限老用户，3限新用户，5外卖会员
		 pam.put("title", "领券省"+baseCommodityServiceImpl.processPriceZero(String.valueOf(num))+"元");
		 pam.put("couponType", couponType);
		 c.setCommodityCouponMap(pam);
	}
	
	/**
	 * 线上专享接口
	 * @param ids
	 * @param storeCode
	 * @return
	 */
	private Map<String,String> getOnlineScript(String[] ids,String storeCode){
		if(ids == null || ids.length <= 0) {
			return null;
		}
		JSONObject params = new JSONObject();
		params.put("skuCodes", ids);
		params.put("storeId", storeCode);
		params.put("type", 1);
		JSONArray list = null;
		try {
			String result = HttpTookit.doPost(GOODS_CENTER_HOST+"/rest/storeItem/getAllMarkBySkuCodes", "data", params.toJSONString());
			if (result == null) {
				return null;
			}
			JSONObject resJson = JSONObject.parseObject(result);
			if (resJson == null) {
				return null;
			}
			String success = resJson.getString("success"); 
			if(!success.equals("1")){
				return null;
			}
			list = resJson.getJSONArray("data");
		}catch(Exception e){
	         logger.error("COUPON_COMMDOITY_ADDRESS error", e);
		}
		return setMapOnlineScript(list);
	} 
	
	/**
	 * 设线上专享角标
	 * @param list
	 * @return
	 */
	private Map<String,String> setMapOnlineScript(JSONArray list) {
		if(null == list || list.size() == 0) {
			return null;
		}
		Map<String,String> resMap = new HashMap<String,String>();
		for(int i = 0; i < list.size(); i++) {
			JSONObject sin = list.getJSONObject(i);
			String goodsNo = sin.getString("goodsNo");
			String specialFlag = sin.getString("specialFlag");
			if(!specialFlag.equals("1")) {
				continue;
			}
			Date specialStartDate = sin.getDate("specialStartDate");
			Date specialEndDate = sin.getDate("specialEndDate");
			Date now = new Date();
			if(null == specialStartDate && null == specialEndDate) {
				resMap.put(goodsNo, "true");
				continue;
			}
			if(null == specialStartDate && null != specialEndDate
					&& specialEndDate.after(now)) {
				resMap.put(goodsNo, "true");
				continue;
			}
			if(null != specialStartDate && null == specialEndDate
					&& specialStartDate.after(now)) {
				resMap.put(goodsNo, "true");
				continue;
			}
			if(null != specialStartDate && null != specialEndDate
					&& now.before(specialEndDate)&& now.after(specialStartDate)) {
				resMap.put(goodsNo, "true");
				continue;
			}
		}
		return resMap;
	}
	
	
}
