package com.feiniu.yx.page.service.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.YXCouponService;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.ModuleProperPlusService;

//限时好券
@Service
public class ITimeCouponMiniService implements CustomModule {
	
	
	@Autowired
	private YXCouponService yxCouponService;
	
	@Autowired
	private ModuleProperPlusService moduleProperPlusService;
	
	@Override
	public void findCustomData(JSONObject mjo) throws Exception {
		Long moduleId = mjo.getLong("id");
		Map<String,ModuleProperPlus> properPlusMap = moduleProperPlusService.queryModuleProperMapByModuleId(moduleId);
		if(null == properPlusMap){
			return;
		}
		String storeCode = mjo.getString("storeCode");
		ModuleProperPlus value = properPlusMap.get(storeCode);
		if(null  == value){
			return;
		}
		List<JSONObject> couponList = getcouponListByStore(value);
		if(null == couponList || couponList.size() == 0 ){
			return;
		}
		mjo.put("couponList", couponList);
	}
	
	private  List<JSONObject> getcouponListByStore(ModuleProperPlus proper){
		List<JSONObject> couponList = new ArrayList<JSONObject>();
		String  moduleProper= proper.getModuleProper();
		if(StringUtils.isBlank(moduleProper)){
			return couponList;
		}
		JSONObject proObj = JSONObject.parseObject(moduleProper);
		Object couponIdInfo = proObj.get("couponId");
		Object couponDescribeInfo = proObj.get("couponDescribe");
		Object couponNameInfo = proObj.get("couponName");
		Map<String, String> describeMap = new HashMap<String, String>();
		Map<String, String> couponNameMap = new HashMap<String, String>();
		String couponIds = null;
		if(null == couponIdInfo){
			return couponList;
		}
		if(couponIdInfo instanceof JSONArray){
			couponIdInfo = (JSONArray)couponIdInfo;
			Object[] couponIdArray = ((JSONArray) couponIdInfo).toArray();
			Object[] couponNameArray = ((JSONArray) couponNameInfo).toArray();
			couponIds = StringUtils.join(couponIdArray, ",");
			//限制门槛字段
			if(couponDescribeInfo != null) {
				if(couponDescribeInfo instanceof JSONArray){
					Object[] describeArray = ((JSONArray) couponDescribeInfo).toArray();
					for(int i = 0; i < couponIdArray.length; i++) {
						Object d = describeArray[i];
						Object id = couponIdArray[i];
						Object name = couponNameArray[i];
						if(d != null && id != null) {
							describeMap.put(id.toString(), d.toString());
							couponNameMap.put(id.toString(), name.toString());
						}
					}
				} 
			}
		}else{
			couponIds = couponIdInfo.toString();
			if(couponDescribeInfo != null) {
				describeMap.put(couponIds, couponDescribeInfo.toString());
				couponNameMap.put(couponIds, couponNameInfo.toString());
			}
		}
		if(null == couponIds){
			return couponList;
		}
		couponList = yxCouponService.setModuleCoupon(couponIds, describeMap);
		return couponList;
	}
	
	
	
	
}
