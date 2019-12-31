package com.feiniu.yx.pool.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface YxCouponService {
	
	public JSONObject getCouponInfoByCouponId(String couponId) throws ParseException;
	public  JSONObject getCouponStoreCodesById(String couponIds);
	public List<JSONObject>  getSharePageInfoByCouponIds(String couponIds);
	public JSONObject getCouponListByStoreCodesInfo(JSONObject params);
//	/**
//	 * 商品券
//	 * @param params
//	 * @return
//	 */
//	public Map<String,String> getCouponInfoByCommodityId(JSONObject params);
	
	public String  getCouponIdsByCommodityId(String[] ids,String storeCode);
	public Map<String, JSONObject> mapCouponInfoByCouponIds(String couponIds);
}
