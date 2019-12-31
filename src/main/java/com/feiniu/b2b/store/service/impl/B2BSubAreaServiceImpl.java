package com.feiniu.b2b.store.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.store.service.B2BSubAreaService;
import com.feiniu.yx.util.HttpTookit;
import com.feiniu.yx.util.YxPoolConst;

@Service
public class B2BSubAreaServiceImpl implements B2BSubAreaService {

	private final static String URL_RemoteSubArea=YxPoolConst.B2B_COMMODITY_HOST_DOMAIN+"/area/getSubAreaBySupType";
	
	Logger logger = Logger.getLogger(B2BSubAreaServiceImpl.class);
	
	@Override
	public List<JSONObject> getSubAreaList(){
		List<JSONObject>  list = getRemoteSubAreaList();
		list = null == list ? new ArrayList<JSONObject>() : list;
		return list;
	}
	
	@Override
	public List<JSONObject> getSubAreaList(String areaId) {
		if(StringUtils.isBlank(areaId)) {
			return getSubAreaList();
		}
		if(areaId.equals("ALL")) {
			return getSubAreaList();
		}
		Map<String,List<JSONObject>> map = setMap(getRemoteSubAreaList());
		if(null == map) {
			return  new ArrayList<JSONObject>();
		}
		String[] ids = areaId.split(",");
		List<JSONObject>  listALL = null ;
		for(String id : ids) {
			listALL = null == listALL ? new ArrayList<JSONObject>() : listALL;
			List<JSONObject>  list = map.get(id); 
			if(null == list) {
				continue;
			}
			listALL.addAll(list);
		}		
		return listALL;
	}
	
	
	private List<JSONObject> getRemoteSubAreaList() {
		JSONObject dataParam = new JSONObject();
		dataParam.put("sup_type", 1);
		Map<String,String> param = new HashMap<String,String>();
		param.put("data", dataParam.toJSONString());
		JSONObject resJson;
		try{
			String res  = HttpTookit.doPost(URL_RemoteSubArea, param);
			if(null == res){
				 logger.error("getSubAreaList error! res is null!");
				 return null;
			}
			resJson = JSONObject.parseObject(res);
			
		}catch(Exception e){
			logger.error("getSubAreaList error!",e);
			return  null;
		}
		
		int code = resJson.getIntValue("code");
		if(code != 0) {
			logger.error("getSubAreaList 请求异常! res=["+resJson.toJSONString()+"]");
			return null;
		}
		JSONObject data = resJson.getJSONObject("data");
		if(null == data) {
			logger.error("getSubAreaList 请求异常! res=["+resJson.toJSONString()+"]");
			return null;
		}
		String area = data.getString("area");
		if(StringUtils.isBlank(area)) {
			logger.error("getSubAreaList 请求异常! res=["+resJson.toJSONString()+"]");
			return null;
		}
		List<JSONObject> dataList = JSONObject.parseArray(area,JSONObject.class);	
		if(null == dataList || dataList.size() == 0) {
			return null;
		}
		return dataList;
	}
	
	private Map<String,List<JSONObject>> setMap(List<JSONObject> dataList){
		Map<String,List<JSONObject>> res = new HashMap<String,List<JSONObject>>(16);
		for(JSONObject object : dataList) {
			if(null == object) {
				continue;
			}
			String areaId = object.getString("area_id");
			if(null == areaId) {
				continue;
			}
			List<JSONObject> list  = res.get(areaId);
			list =  null == list ? new ArrayList<JSONObject>() : list;
			list.add(object);
			res.put(areaId, list);
		}
		return res;
	}
	
}
