package com.feiniu.yx.share.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.share.entiry.YXCouponShare;

public interface YXCouponShareService {
	public void insertCouponShare(YXCouponShare couponShare);
	public YXCouponShare queryCouponShareById(Long id);
	public JSONObject queryJSONObjectById(Long id);
	public void updateCouponShare(YXCouponShare couponShare);
	public List<YXCouponShare> queryCouponShareList(YXCouponShare couponShare);
	public JSONObject updateCouponShareEndTime(YXCouponShare couponShare);
}
