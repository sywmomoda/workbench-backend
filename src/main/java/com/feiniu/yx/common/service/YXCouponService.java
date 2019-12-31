package com.feiniu.yx.common.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface YXCouponService {
	
	/**
	 * 根据券ID查询接口原始数据
	 * @param couponIds
	 * @return
	 */
	public JSONObject getCouponInfoByCouponIds(String couponIds);

	/**
	 * 对组件的券数据进行包装处理
	 * @param couponIds
	 * @param describeMap
	 * @return
	 */
	List<JSONObject> setModuleCoupon(String couponIds,
			Map<String, String> describeMap);
	
	JSONArray getJAByCouponInfoAPI(String[] ids, String storeCode);
}
