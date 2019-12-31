package com.feiniu.yx.pool.service;

import java.util.List;
import java.util.Map;

public interface YxCategoryService {

	public List<Map<String, Object>> getLocalCategory(String id,String storeCode);

	public List<Map<String, Object>> getLocalCategoryTree(String areaCode,
			String checkedCodes, Integer level);

}
