package com.feiniu.yx.core;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface UserInfoService {
	
	
	List<JSONObject> queryReceivedUser(String commodityId,String storeCode, int limit);

}
