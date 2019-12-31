package com.feiniu.yx.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.core.UserInfoService;
import com.feiniu.yx.util.HttpTookit;

//限时好券
@Service
public class UserInfoServiceImpl implements UserInfoService {
	public static Logger logger = Logger.getLogger(UserInfoServiceImpl.class);
	private final String TRY_EATING_RECEIVED_API = SystemEnv.getProperty("userInfo.tryeatingRecivedUser");
	private final String SEARCH_MEMBER_INFO_API = SystemEnv.getProperty("userInfo.searchMembers");
	
	private final String[] nameGenerate = new String[]{"小*","不*是","t*r","y*d","x*i","挥*手","青*","陈*亮","小*子","d*r","y*n","李*","张*","磊*","孙*","一*","人*美","是*n","g*d","h*p","b*n"};
	
	@Override
	public List<JSONObject> queryReceivedUser(String commodityId, String storeCode, int limit) {
		List<JSONObject> objList = new ArrayList<JSONObject>();
		JSONObject data = getCommonData();
		JSONObject body = new JSONObject();
		body.put("goodsNo", commodityId);
		body.put("storeCode", storeCode);
		data.put("body", body);
		Map<String, String> dataMap = new HashMap<String,String>();
		dataMap.put("data", data.toJSONString());
		dataMap.put("h5", "yx_touch");
		String result = HttpTookit.doPost(TRY_EATING_RECEIVED_API , dataMap);
		if(result!=null){
			JSONObject robject = JSONObject.parseObject(result);
			if(0 == robject.getInteger("errorCode")){
				JSONObject rbody = robject.getJSONObject("body");
				JSONArray userList = new JSONArray();
				if(rbody.containsKey("guidList")){
					userList = rbody.getJSONArray("guidList");
				}
				if(userList.size()>0){
					objList = queryAccountInfo("guid",userList);
				}
			} 
		}
		
		long seed = Long.parseLong(commodityId.replaceAll("[a-zA-Z]",""));
		objList	 = nameRebuild(objList,seed,limit);
			
		return objList;
	}
	
	
	/**
	 * 用户名处理，不满数量时补全
	 * @param objList
	 * @param seed 
	 * @param limit 
	 * @return
	 */
	private List<JSONObject> nameRebuild(List<JSONObject> objList, long seed, int limit) {
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		int length = nameGenerate.length;
		Random random = new Random(seed);
		for(int i=0; i<limit; i++){
			JSONObject newUser = new JSONObject();
			String userName = "";
			if(objList.size()>i){
				JSONObject realUser = objList.get(i);
				if(StringUtils.isNotBlank(realUser.getString("NICKNAME"))){//先取用户昵称
					userName = realUser.getString("NICKNAME");
				}else if(StringUtils.isNotBlank(realUser.getString("REAL_NAME"))){//再取用户实名
					userName = realUser.getString("REAL_NAME");
				}else if(StringUtils.isNotBlank(realUser.getString("MEM_USERNAME"))){//最后取系统名
					userName = realUser.getString("MEM_USERNAME");
				}
				if(userName.startsWith("FN_")){
					userName = "FN_"+replaceAction(userName.substring(3));
				}else{
					userName = replaceAction(userName);
				}
				
			}else{
				float f = random.nextFloat();
				if(f<0.5){
					userName = "FN_"+random.nextInt(10)+"*"+random.nextInt(10);
				}else{
					userName = nameGenerate[random.nextInt(length)];
				}
				
			}
			
			newUser.put("userName", userName);
			returnList.add(newUser);
		}
		return returnList;
	}
	
	
	/**
	 * 用户名*号替换
	 * @param userName
	 * @return
	 */
	private String replaceAction(String userName) {
		String userNameAfterReplaced = "";
		int nameLength = userName.length();
		if (nameLength == 1) {
			userNameAfterReplaced = "*";
		} else if (nameLength == 2){
			userNameAfterReplaced = userName.substring(0,1)+"*";
		} else if (nameLength >=3){
			userNameAfterReplaced = userName.substring(0,1)+"*"+ userName.substring(nameLength-1);
		}
		return userNameAfterReplaced;
	}
	

	/**
	 * 根据用户guid查询用户名称
	 * @param type
	 * @param value
	 * @return
	 */
	protected List<JSONObject> queryAccountInfo(String type, JSONArray value) {
		List<JSONObject> objList = new ArrayList<JSONObject>();
		String queryType = "";
		
		if ("email".equals(type)) {
			queryType = "1";
		} else if ("cellphone".equals(type)) {
			queryType = "2";
		} else if ("username".equals(type)) {
			queryType = "3";
		} else if ("guid".equals(type)) {
			queryType = "5";
		} 
		Map<String, String> param = new LinkedHashMap<>();
		param.put("names", JSONArray.toJSONString(value));
		param.put("type", queryType);
		//memMemberApiSearchMemberUrl="http://mem-info.beta1.fn/member_api/searchMembers";
		try {
			String resp = HttpTookit.doPost(SEARCH_MEMBER_INFO_API, param);
			JSONObject respJson = JSON.parseObject(resp);
			if(respJson!=null){
				Integer code = respJson.getInteger("code");
				JSONArray respData = respJson.getJSONArray("data");
				if (code != null && code == 100) {
					if(respData!=null && respData.size()>0){
						for(int i=0;i<respData.size();i++){
							JSONObject userInfo = new JSONObject();
							JSONObject resultData = respData.getJSONObject(i);
							userInfo.put("NICKNAME", resultData.get("NICKNAME"));
							userInfo.put("REAL_NAME", resultData.get("REAL_NAME"));
							userInfo.put("MEM_USERNAME", resultData.get("MEM_USERNAME"));
							objList.add(userInfo);
						}
					}
				}
			}
		} catch (Exception ex) {
			//return this.requestException(request, resp, ex);
		}
		return objList;
	}
	
	private JSONObject getCommonData(){
		JSONObject data = new JSONObject();
		data.put("addrId", "");
		data.put("apiVersion", "a1.00");
		data.put("appVersion", "1.0.3");
		data.put("channel", "CHANNEL_VALUE");
		data.put("deviceId", "");
		data.put("httpsEnable", 1);
		data.put("isSimulator", false);
		data.put("networkType", "");
		data.put("osType", "3");
		data.put("reRule", "");
		data.put("clientId", "");
		data.put("time", "");
		data.put("token", "");
		data.put("viewSize", "720x1280");
		return data;
	}
	
	
	
	
	
}
