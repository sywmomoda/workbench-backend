package com.feiniu.b2b.store.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

public interface B2BSubAreaService {
	public List<JSONObject> getSubAreaList(String areaId);
	public List<JSONObject> getSubAreaList();
}
