package com.feiniu.yx.common.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.YXCouponXiaoQuService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.HttpTookit;

@Service
public class YXCouponXiaoQuServiceImpl implements YXCouponXiaoQuService {
	private static final Logger logger =Logger.getLogger(YXCouponXiaoQuServiceImpl.class);
	
	private static String XIAOQU_URL=SystemEnv.getProperty("yxXiaoQu.url");
	
	@Override
	public String getJSONXiaoQu(String pgSeq) {
		JSONArray array = toTreeList(pgSeq);
		return array.toJSONString();
	}
	
	private JSONObject getRemoteXiaoQu(){
		JSONObject param = new JSONObject();
		param.put("type", 1);
		Map<String,String> data = new HashMap<String,String>();
		data.put("data", param.toJSONString());
		JSONObject resJson = null;
		try{
			String res  = HttpTookit.doPost(XIAOQU_URL,data);
			if(null == res){
				 logger.error("getRemoteXiaoQu error! res is null!");
				 return null;
			}
			resJson = JSONObject.parseObject(res);
			
		}catch(Exception e){
			logger.error("getRemoteXiaoQu error!",e);
			return null;
		}
		if(null == resJson){
			return null;
		}
		String success = resJson.getString("success");
		if(!success.equals("1")){
			logger.error("getRemoteXiaoQu error! "+resJson.toJSONString());
			return null;
		}		
		JSONObject xqObj = resJson.getJSONObject("data");
		return xqObj;
	}
	
	
	private JSONArray toTreeList(String pgSeq){
		JSONObject dataJSON = getRemoteXiaoQu();
		JSONArray resultArray = new JSONArray();
		if(null == dataJSON){
			return resultArray;
		}
		if(StringUtils.isBlank(pgSeq)){
			return resultArray;
		}		
		if(pgSeq.equals("ALL")){
			for(Entry<String, Object> entry : dataJSON.entrySet()){
				Object value  = entry.getValue();
				if(null == value){
					continue;
				}
				JSONObject single = JSONObject.parseObject(value.toString());
				JSONArray subList = single.getJSONArray("subList");
				if(null == subList){
					continue;
				}
				if(subList.size() == 0){
					continue;
				}
				resultArray.addAll(subList);
			}
			return resultArray;
		}
		Object areaValue =  dataJSON.get(pgSeq);
		if(null == areaValue){
			return resultArray;
		}
		JSONObject single = JSONObject.parseObject(areaValue.toString());
		if(null == single){
			return resultArray;
		}
		JSONArray subList = single.getJSONArray("subList");
		if(null == subList){
			return resultArray;
		}
		resultArray = subList;
		return resultArray;
	}
	
	/**
	 * 
	 * @param miniCode 小区code
	 * @return
	 */
    public String getXiaoQuNamesByCodes(String miniCode){

    	
    	JSONArray arr = toTreeList("ALL");
    	if(null == arr){
    		return "";
    	}
    	if(arr.size() == 0){
    		return "";
    	}
    	StringBuffer sbNames = new StringBuffer();
       for(int i = 0, len = arr.size(); i < len; i++){
    	   JSONObject single = arr.getJSONObject(i);
    	   String name = single.getString("subarea");
    	   String code = single.getString("subId");
    	   if(StringUtils.isBlank(code)){
    		   continue;
    	   }
    	   if(ArrayUtils.contains(miniCode.split(","),code)){
    		   sbNames.append(name).append(",");
    	   }
       }
       String names = sbNames.toString();
       if(names.endsWith(",")){
    	   names = names.substring(0, names.length()-1);
       }
       return names;
    }
    
    private Map<String,Set<String>> getSingleXiaoQu(){
    	Map<String,Set<String>> resultMap = new HashMap<String,Set<String>>();
    	JSONArray  xqArray = toTreeList("ALL");
    	for(int i = 0,len = xqArray.size(); i < len; i++){
    		JSONObject oo = xqArray.getJSONObject(i);
    		if(null == oo){
    			continue;
    		}
    		String subId = oo.getString("subId");
    		if(StringUtils.isBlank(subId)){
    			continue;
    		}
    		JSONArray subList = oo.getJSONArray("subList");
    		if(null ==subList){
    			continue;
    		}
    		Set<String>  storeSet = new HashSet<String>();
    		for(int j = 0,jLen = subList.size(); j < jLen; j++){
    			JSONObject store = subList.getJSONObject(j);
    			if(null == store){
    				continue;
    			}
    			String storeCode = store.getString("storeId");
    			if(StringUtils.isBlank(storeCode)){
    				continue;
    			}
    			storeSet.add(storeCode);
    		}
    		resultMap.put(oo.getString("subId"), storeSet);
    	}
   
    	return resultMap;
    }
    
    
    /*
     * 获取小区的所有门店
     */
    public String  getStoreCodesByXiaoQuCodes(String codes){
    	String[] codeArray = codes.split(",");
    	Map<String,Set<String>> xqMap = getSingleXiaoQu();
    	Set<String> allSet = new HashSet<String>();
    	for(int  i = 0,len = codeArray.length; i < len; i++){
    		String cd = codeArray[i];
    		if(StringUtils.isBlank(cd)){
    			continue;
    		}
    		Set<String> set = xqMap.get(cd);
    		if(null == set){
    			continue;
    		}
    		allSet.addAll(set);
    	}
    	StringBuffer sbCodes = new StringBuffer();
    	for(String cc : allSet){
    		if(StringUtils.isBlank(cc)){
    			continue;
    		}
    		sbCodes.append(cc).append(",");
    	}
    	String storeCodes = sbCodes.toString();
    	if(storeCodes.endsWith(",")){
    		storeCodes = storeCodes.substring(0, storeCodes.length() -1);
    	}
    	return storeCodes;
    }
    

}
