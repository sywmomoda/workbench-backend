package com.feiniu.b2b.pool.service;

import java.util.List;
import java.util.Map;


public interface B2BCategoryService {

	public void sync(String storeCode);
	
	public boolean getIsLock();
	
	public void lock();
	
	public void releaseLock();

	public List<Map<String, Object>> getLocalCategory(String id,String storeCode);

	public List<Map<String, Object>> getLocalCategoryTree(String areaCode,
			String checkedCodes, Integer level);
	
	

}
