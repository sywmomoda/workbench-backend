package com.feiniu.yx.page.service.module;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.YXCouponService;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.util.DateUtil;

@Service
public class ICouponPopupModule implements CustomModule {
	
//	@Autowired
//	private ModuleDataService moduleDataService;
	
	@Autowired
	private YXCouponService couponService;
	
	
	@Override
	public void findCustomData(JSONObject mjo) throws Exception {
		JSONObject properties = mjo.getJSONObject("moduleProperties");
		Object cId = properties.get("couponIds");
		JSONArray idArray = new JSONArray();
		JSONArray describesArray = new JSONArray();
		JSONArray btArray = new JSONArray();
		JSONArray etArray = new JSONArray();
		JSONArray actArray = new JSONArray();
		JSONArray nameArray = new JSONArray();
		if(cId instanceof List){
			idArray = properties.getJSONArray("couponIds");
			describesArray = properties.getJSONArray("couponDescribes");
			btArray = properties.getJSONArray("beginTime");
			etArray = properties.getJSONArray("endTime");
			actArray = properties.getJSONArray("isAction");
			nameArray = properties.getJSONArray("title");
		}else{
			String idStr = properties.getString("couponIds");
			idArray.add(idStr);
			String desStr = properties.getString("couponDescribes");	
			describesArray.add(desStr);
			String btStr = properties.getString("beginTime");
			btArray.add(btStr);
			String etStr = properties.getString("endTime");
			etArray.add(etStr);
			String actStr = properties.getString("isAction");
			actArray.add(actStr);
			String nameStr = properties.getString("title");
			nameArray.add(nameStr);
		}
		
		int index = getActionTimeIndex(btArray,etArray,actArray);
		if(index == -1){
			return;
		}
		
		String couponIds = idArray.getString(index);
		if(StringUtils.isBlank(couponIds)){
			return;
		}
		Map<String,String> describeMap = setDescribesToMap(couponIds,describesArray.getString(index));
		List<JSONObject> couponList = couponService.setModuleCoupon(couponIds,describeMap);
		mjo.put("couponList", couponList);
		
		
	}
	
	/**
	 * 获取当进行中的期数
	 * @param btArray
	 * @param etArray
	 * @param actArray
	 * @return  返回进行中的索引
	 */
	private int getActionTimeIndex(JSONArray btArray, JSONArray etArray,JSONArray actArray){
		
		for(int i = 0, len = btArray.size(); i < len; i++){
			Integer act = actArray.getInteger(i);
			String bt = btArray.getString(i);
			String et = etArray.getString(i);
			if (StringUtils.isBlank(bt) || StringUtils.isBlank(et)) {
				continue;
			}
			Date dBt = DateUtil.getDate(bt, "yyyy-MM-dd HH:mm:ss");
			Date dEt = DateUtil.getDate(et, "yyyy-MM-dd HH:mm:ss");
			Date now = new Date();
			if(act == 0){
				if(i+1 < len){
					dEt = btArray.getDate(i+1);
				}else{
					dEt = DateUtil.getDate(now, 1);
				}
			}
			
			
			if(!(dBt.before(now) && dEt.after(now))){
				continue;
			}else{
				return i;
			}
			
		}
		
		return -1;
	}
	
	
	/**
	 * 把ids对应该的门槛装map
	 * @param ids
	 * @param des
	 * @return
	 */
	private Map<String,String> setDescribesToMap(String ids,String des){
		String[] idArray = ids.split(",");
		String[] desArray = des.split(",");
		Map<String,String> resultMap = new HashMap<String,String>();
		if(idArray.length == 0 ||  desArray.length == 0){
			return resultMap;
		}
		for(int i = 0; i < idArray.length; i++){
			String desStr = "";
			if (desArray.length > i) {
				desStr = desArray[i];
			}
			resultMap.put(idArray[i], desStr);
		}
		return resultMap;
	}
	
	

/*	private List<JSONObject> setModuleCoupon(String couponIds,Map<String, String> describeMap) {
		JSONObject data = couponService.getCouponInfoByCouponIds(couponIds);
		List<JSONObject> retList = new ArrayList<JSONObject>();
		if (null == data) {
			return retList;
		}
		JSONArray couponList = data.getJSONArray("list");
		if (couponList == null || couponList.size() == 0) {
			return retList;
		}

		for (int i = 0; i < couponList.size(); i++) {
			JSONObject temp = couponList.getJSONObject(i);
			if (null == temp) {
				continue;
			}

			boolean isTure = "2".equals(temp.getString("activityStatus"))
					&& !"10".equals(temp.getString("discountType"));
			if (!isTure) {
				continue;
			}
			JSONObject coup = new JSONObject();
			String couponId = temp.getString("couponId");
			coup.put("couponId", couponId);
			coup.put("couponName", temp.getString("title"));
			coup.put("couponType", temp.getIntValue("discountType") == 5 ? 2: 1);//5礼品券，7优惠券，10组合券
			if(temp.getIntValue("discountType") == 7 && temp.getIntValue("couponType") == 4){//单品券直降
				coup.put("couponType", 3);
			}else if(temp.getIntValue("discountType") == 7 && temp.getIntValue("couponType") == 5){//单品券固定金额
				coup.put("couponType", 4);
			}
			coup.put("couponValue", temp.getString("vouchersDiscount"));
			coup.put("couponThreshold", temp.getString("vouchersPrice"));
			String couponDescribe = describeMap.get(couponId);
			//兼容组合券
			if (couponDescribe == null) {
				String supCouponId = temp.getString("supCouponId");
				if (supCouponId != null) {
					couponDescribe = describeMap.get(supCouponId);
				}
			}
			coup.put("couponDescribe", couponDescribe != null ? couponDescribe: "");
			retList.add(coup);
		}
		return retList;
	}
*/	
}
