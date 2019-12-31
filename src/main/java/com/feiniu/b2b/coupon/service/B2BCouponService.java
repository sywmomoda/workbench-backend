package com.feiniu.b2b.coupon.service;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public interface B2BCouponService {
  public JSONArray getCouponList(String couponIds);
  public JSONObject getCouponInfo(String couponId);
  public List<JSONObject>  getSharePageInfoByCouponIds(String couponIds);
  public JSONObject getCouponListByStoreCodesInfo(JSONObject params);
}
