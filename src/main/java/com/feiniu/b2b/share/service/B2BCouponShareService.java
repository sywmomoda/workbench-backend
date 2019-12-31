package com.feiniu.b2b.share.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.share.entiry.B2BCouponShare;

public interface B2BCouponShareService {
	public void insertCouponShare(B2BCouponShare couponShare);
	public B2BCouponShare queryCouponShareById(Long id);
	public JSONObject queryJSONObjectById(Long id);
	public void updateCouponShare(B2BCouponShare couponShare);
	public List<B2BCouponShare> queryCouponShareList(B2BCouponShare couponShare);
	public JSONObject updateCouponShareEndTime(B2BCouponShare couponShare);
}
