package com.feiniu.yx.common.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.YXCacheSyncService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.HttpTookit;
import com.fn.cache.client.RedisCacheClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class YXCacheSyncServiceImpl implements YXCacheSyncService {
	
	private static final Logger logger =Logger.getLogger(YXCacheSyncServiceImpl.class);

	private static String YX_INDEX_SYCN_URL = SystemEnv.getProperty("yx.indexAPI.host")+"/indexClassify/cacheSync";
	@Autowired
	private RedisCacheClient cacheClient;


	@Override
	public JSONObject syncCache(String method, String key, String cacheData, String field) {
		JSONObject params = new JSONObject();
		params.put("method", method);
		params.put("key", key);
		params.put("cacheData", cacheData);
		params.put("field", field);
		String paramsString = params.toJSONString();
		Map<String, JSONObject> m = null;
		try {
			String result = HttpTookit.doPost(YX_INDEX_SYCN_URL,"data", paramsString);
			JSONObject jo = JSON.parseObject(result);
			if (jo == null) {
				return null;
			}
			String code = jo.getString("code");
			if ("200".equals(code)) {
				logger.info("YXCacheSync success，params:" + paramsString);
			}else{
				logger.info("YXCacheSync failed，params:" + paramsString);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
