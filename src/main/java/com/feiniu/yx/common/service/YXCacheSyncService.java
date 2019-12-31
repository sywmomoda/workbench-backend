package com.feiniu.yx.common.service;

import com.alibaba.fastjson.JSONObject;

public interface YXCacheSyncService {

	/**
	 * 缓存同步，目前只实现了 hset  hdel  del
	 * @param method 方法
	 * @param key  key
	 * @param cacheData  缓存数据
	 * @param field  field
	 * @return
	 */
	JSONObject syncCache(String method, String key, String cacheData, String field);
}
