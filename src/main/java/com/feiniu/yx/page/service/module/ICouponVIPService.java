package com.feiniu.yx.page.service.module;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.core.impl.ModuleCommodityServiceImpl;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.ModuleProperPlusService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.YxCouponService;
import com.feiniu.yx.util.MD5Util;

@Service
public class ICouponVIPService implements CustomModule {
	
	@Autowired
	private ModuleProperPlusService moduleProperPlusService;
	
	@Autowired
	private ModuleCommodityServiceImpl moduleCommodityService;
	
	@Autowired
	private YxCouponService couponService;
	
	@Override
	public void findCustomData(JSONObject mjo) throws Exception {
		Long moduleId = mjo.getLong("id");
		String storeCode = mjo.getString("storeCode");
		JSONObject moduleProperties = mjo.getJSONObject("moduleProperties");
		if(null  == moduleProperties){
			return;
		}
		Map<String, ModuleProperPlus> mppMap = moduleProperPlusService.queryModuleProperMapByModuleId(moduleId);
		ModuleProperPlus mmp = mppMap.get(storeCode);
		if(null == mmp){
			return;
		}
		JSONObject properObj = JSONObject.parseObject(mmp.getModuleProper());
		if(null == properObj){
			return;
		}
		JSONArray array = setListCommodity(properObj,storeCode);
		if(null == array || array.size() == 0){
			return;
		}
		moduleProperties.put("commodityList", array);
		mjo.put("moduleProperties", moduleProperties);
	}
	
	private JSONArray setListCommodity(JSONObject properObj,String storeCode){
		Object comObject = properObj.get("commodityId");
		if(null == comObject){
			return null;
		}
		JSONArray comIdArray= new JSONArray();
		JSONArray comNameArray= new JSONArray();
		JSONArray couponIdArray = new JSONArray();
		if(comObject instanceof List){
			comIdArray.addAll(properObj.getJSONArray("commodityId"));
			comNameArray.addAll(properObj.getJSONArray("commodityName"));
			couponIdArray.addAll(properObj.getJSONArray("couponId"));
		}else{
			comIdArray.add(properObj.getString("commodityId"));
			comNameArray.add(properObj.getString("commodityName"));
			couponIdArray.add(properObj.getString("couponId"));
		}
		String[] ids = comIdArray.toArray(new String[comIdArray.size()]);
		Map<String,YxPoolCommodity> comMap = moduleCommodityService.mapCommodityNOConponScript(ids, storeCode);
		if(null == comMap){
			return null;
		}
		Map<String,JSONObject> mapCoupon = couponService.mapCouponInfoByCouponIds(StringUtils.join(couponIdArray,","));
		JSONArray result = new JSONArray();
		for(int i =0,len = comIdArray.size(); i < len; i++){
			String id = comIdArray.getString(i);
			YxPoolCommodity pc = comMap.get(id);
			if(null == pc){
				continue;
			}
			JSONObject sin = new JSONObject();
			sin.put("commodityId", id);
			sin.put("price", pc.getPrice());
			sin.put("isExist", pc.isExist());
			sin.put("stockSum", pc.getStockSum());
			sin.put("picUrl", pc.getPicUrl());
			sin.put("unit", pc.getUnit());
			sin.put("limitQty", pc.getLimitQty());
			sin.put("saleQty", pc.getStockSum());
			sin.put("minQuantity", pc.getMinQuantity());
			String title = comNameArray.getString(i);
			if(StringUtils.isBlank(title)){
				title = pc.getTitle();
			}
			sin.put("title", title);
			sin.put("promoteText", pc.getPromoteText());
			sin.put("scriptTitle", pc.getScriptTitle());
			sin.put("propertyTitle", pc.getPropertyTitle());
			String couponId = couponIdArray.getString(i);
			sin.put("couponId", couponId);
			sin.put("md5", MD5Util.getMD5(couponId + MD5Util.COUPON_MD5_KEY));
			JSONObject coupon = mapCoupon.get(couponId);
			if(null == coupon){
				result.add(sin);
				continue;
			}
			//5礼品券，7优惠券，10组合券
			String discountType = coupon.getString("discountType");
			//只处理优惠券类型
			if (!"7".equals(discountType)) {
				result.add(sin);
				continue;
			}
			String couponType = coupon.getString("couponType");
			String vouchersDiscount = coupon.getString("vouchersDiscount");
			vouchersDiscount = moduleCommodityService.getBaseCommodityServiceImpl().processPriceZero(vouchersDiscount);
			sin.put("vouchersDiscount", vouchersDiscount); 
			//“固定金额单品券”——“券后价”=“券固定金额”。
			//其他“券后价”=“优鲜价”—“券优惠金额”；
			if ("5".equals(couponType)) {
				sin.put("currentPrice", vouchersDiscount);  //券后价格
			} else {
				Float num = new BigDecimal(pc.getPrice().toString()).subtract(new BigDecimal(vouchersDiscount)).floatValue();
         		String currentPrice= String.valueOf(num);
         		vouchersDiscount = moduleCommodityService.getBaseCommodityServiceImpl().processPriceZero(vouchersDiscount);
         		sin.put("vouchersDiscount", vouchersDiscount);
         		currentPrice = moduleCommodityService.getBaseCommodityServiceImpl().processPriceZero(currentPrice);
         		sin.put("currentPrice", currentPrice);//券后价格
			}
			sin.put("couponType", couponType);
			sin.put("discountType", coupon.getString("discountType"));
			sin.put("vouchersPrice", coupon.getString("vouchersPrice"));
			result.add(sin);
		}
		return result;
	}
}
