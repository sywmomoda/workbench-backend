package com.feiniu.yx.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@Service
public class CategoryUtil {
	
	
	/**
	 * 类目处理
	 * @param moduleProperties
	 */
	public static void categoryCustom(JSONObject object ,String storeCode){
		if(null  == object){
			return;
		}
		
		String type1 = object.getString("type");
		
		
		if(StringUtils.isBlank(type1) ||!type1.equals("firstlevellist")){//类型
			return;
		}
		
		String data1 = object.getString("data");
		
		if(StringUtils.isBlank(data1)){
			return;
		}
		
		JSONObject dataObj = JSONObject.parseObject(data1);
		if(dataObj.containsKey(storeCode)){
			String cate = dataObj.getString(storeCode);
			object.put("data", cate);
		}
	}
	
	private static void setcategoryArray(JSONObject object ,String storeCode,String type, String data){
		JSONArray typeArray = object.getJSONArray(type);
		JSONArray dataArray = object.getJSONArray(data);
		if(null  == dataArray){
			return;
		}
		
		JSONArray newArray = new JSONArray();
		for(int i = 0;  i < typeArray.size();i++){
			String tp = typeArray.getString(i);
			if(!tp.equals("firstlevellist")){
				continue;
			}
			JSONObject dt = dataArray.getJSONObject(i);
			
			if(null == dt){
				continue;
			}
			
			String result =  dt.getString(storeCode);
			newArray.add(result);
		}
		
		object.put(data, newArray);
	}
	
	
	public static void categoryCustom(JSONObject object ,String storeCode,String type, String data){
		
		
		if(null == object){
			return;
		}
		

		
		Object type1 = object.get(type);
		
		if(null == type1){
			return;
		}
		
		if(type1 instanceof List){
			setcategoryArray(object,storeCode,type,data);
			return; 
		}
		

		String typeStr =object.getString(type);
		
		if(StringUtils.isBlank(typeStr) || !typeStr.equals("firstlevellist")){  //类目
			return;
		}

		
		String data1 = object.getString(data);
		
		if(StringUtils.isBlank(data1)){
			return;
		}
		
		JSONObject dataObj = JSONObject.parseObject(data1);		
		
		if(null != dataObj&&dataObj.containsKey(storeCode)){
			String cate = dataObj.getString(storeCode);
			object.put(data, cate);
		}
		
		
	}
}
