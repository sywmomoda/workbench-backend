package com.feiniu.yx.common.service;

import java.util.Map;
import java.util.Set;

public interface UserService {

	/**
	 * 获得登录用户的有权限门店
	 * @return
	 * {
		"usr_id": "pm73",
		"store_id": [
			"1009",
			"133332",
			"12"
			]
		}
	 */
	String getUserStore();
	
	
	Map<String,Set<String>> getMapUserStores();
}
