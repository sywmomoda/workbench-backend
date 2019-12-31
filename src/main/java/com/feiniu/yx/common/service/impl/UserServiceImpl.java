package com.feiniu.yx.common.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.YXConstant;
import com.feiniu.yx.common.service.UserService;
import com.feiniu.yx.util.HttpTookit;
import com.feiniu.yx.util.UserUtil;
import com.fn.cache.client.RedisCacheClient;

@Service
public class UserServiceImpl implements UserService {
	
	private static final Logger logger =Logger.getLogger(UserServiceImpl.class);
	
	//查用户的门店code
	private static String USER_STORE_CODE_API = YXConstant.PORTAL_HOST + "/openapi/queryMappingStoreAndArea.do";

	@Autowired
	private RedisCacheClient cacheClient;
	
	@Override
	public String getUserStore() {
		return StringUserStore();
	}
	
	private String StringUserStore() {
		String userId = UserUtil.getUserId();
		if (StringUtils.isBlank(userId)) {
			return "";
		}
		String user_cache_key = "CMS_YX_USER_CACHE_" + userId;
		String userStroe_str = cacheClient.get(user_cache_key);
		if (userStroe_str != null) {
			return userStroe_str;
		}
		
		JSONObject usr_id = new JSONObject();
		usr_id.put("usr_id",userId);
		String result = "";
		try {
			result =  HttpTookit.doPost(USER_STORE_CODE_API, "data", usr_id.toJSONString());
			cacheClient.put(user_cache_key, 300, result);
		}catch(Exception e) {
			logger.error("getUserStoreJSON error", e);
		}
		return result;
	}

	@Override
	public Map<String, Set<String>> getMapUserStores() {
		Map <String,Set<String>> result = new  HashMap<String,Set<String>>();
		String userStroe_str = StringUserStore();
		if (StringUtils.isBlank(userStroe_str)) {
			return result;
		}
		JSONObject objUser = JSON.parseObject(userStroe_str);
		JSONArray storeArray = objUser.getJSONArray("area");
		Set<String> setPgSeq = new HashSet<String>();
		if(null == storeArray){
			JSONObject data  = objUser.getJSONObject("data");
			storeArray = data.getJSONArray("area");
		}
		
		if(null == storeArray){
			return result;
		}
		JSONArray codeArray   = new JSONArray();
		for(int i = 0,len = storeArray.size(); i < len; i++){
			JSONObject single = storeArray.getJSONObject(i);
			if(null == single){
				continue;
			}
			JSONArray tp = single.getJSONArray("store_id");
			if(null == tp){
				continue;
			}
			codeArray.addAll(tp);
			String pgSeq = single.getString("area_code");
			if(null == pgSeq){
				continue;
			}
			setPgSeq.add(pgSeq);
		}
		Set<String> setCode = new HashSet<String>();
		for(int j = 0,len = codeArray.size(); j < len; j++){
			setCode.add(codeArray.getString(j));
		}
		
		result.put("codeSet", setCode);
		result.put("pgSeqSet", setPgSeq);
		return result;
	}
}
