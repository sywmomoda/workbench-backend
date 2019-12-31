package com.feiniu.b2b.coupon.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.coupon.service.B2BCouponService;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.DateUtil;
import com.feiniu.yx.util.HttpTookit;

@Service
public class B2BCouponServiceImpl implements B2BCouponService {
	private static final Logger logger =Logger.getLogger(B2BCouponServiceImpl.class);
	private static final String COUPON_API_ADDRESS =SystemEnv.getProperty("b2bcoupon.api.host")+"/b2bCoupon/outerSrv/getCouponInfo";
	
	private static final String COUPON_COMMDOITY_ADDRESS = SystemEnv.getProperty("coupon.api.host")+"/coupon/outerSrv/getCouponsReceivable4Detail";
	
	
	@Autowired
	private B2BStoreService storeServcie;
	
	@Override
	public JSONObject getCouponInfo(String couponId) {
		if(StringUtils.isNotBlank(couponId)) {
			couponId = couponId.trim();
		}
		JSONArray list = getCouponList(couponId);
		JSONObject res = new JSONObject();
		if(null == list || list.size() == 0){
			res.put("msg", "根据优惠券ID:" + couponId + "未查找到相应的优惠券信息");
			return res;
		}
		for(int i = 0; i < list.size(); i++){
			JSONObject single = list.getJSONObject(i);
			if(null == single){
				continue;
			}
			String resId = single.getString("couponId");
			if(!resId.trim().equals(couponId)){
			 continue;	
			}
			String check = checkCouponStatus(single);
			if(!"OK".equals(check)){
				res.put("msg", check);
				return res;
			}
			res.put("couponId", couponId);
			res.put("couponName", single.getString("title"));
			res.put("vouchersDiscount", single.getString("vouchersDiscount"));
			res.put("vouchersPrice", single.getString("vouchersPrice"));
			res.put("scopeDescription", single.getString("scopeDescription"));
			res.put("discountType", single.getString("discountType"));
			res.put("limitUser", single.getString("limitUser"));
			// couponType:优惠券适用范围(0:全场通用 1:范围限定
			// 2:商品限定3:品牌限定,4:单品限定即单品券) discountType=7
			// 且couponType=4为单品券；discountType=7
			// 且couponType!=4则为普通优惠券
			res.put("couponType", single.getString("couponType"));
			res.put("totalMaxCount", single.getString("totalMaxCount"));
			res.put("dayMaxCount", single.getString("dayMaxCount"));
			res.put("skuId", single.getString("skuId"));
			res.put("useEndTimestamp",single.getString("useEndTimestamp"));
			res.put("useStartTimestamp",single.getString("useStartTimestamp"));
			if("5".equals(single.getString("discountType"))){
				res.put("skuId", single.getString("skuIdGift"));
			}
			res.put("skuName", single.getString("skuName"));
			String storeIds = single.getString("storeIds");
			int storeType = single.getIntValue("storeType");
			storeIds = getStoreCode(storeType,storeIds);
			res.put("storeCodes", storeIds);
		}
		return res;
	}
	
	private String getStoreCode(int storeType ,String storeIds){
		if(storeType == 2){
			return storeIds;
		}
		if(storeType == 3){  //小区
			String[] ids = storeIds.split(",");
			List<String> list = new ArrayList<String>();
			for(int i = 0; i < ids.length; i++) {
				String id = ids[i];
				if(StringUtils.isBlank(id)) {
					continue;
				}
				if(!id.contains("_")) {
					continue;
				}
				list.add(id.split("_")[1]);
			}
			storeIds = storeServcie.getStoreCodesByXiaoQuCodes(StringUtils.join(list,","));	
		}else if(storeType == 1){ //区域
		   String ids = getCouponStoreCodeByPgSeq(storeIds);
		   if(StringUtils.isNotBlank(ids)){
			   storeIds = ids;  
		   }
		}
		return storeIds;
	}
	
	@Override
	public JSONArray getCouponList(String couponIds) {
		if (couponIds == null) {
			return null;
		}
		if (StringUtils.isBlank(couponIds.trim())) {
			return null;
		}
		JSONObject con= new JSONObject();
		con.put("couponIds", couponIds);
		con.put("src", 1);//接口来源（1:大润发 2:欧尚）默认按1处理
		Map<String,String> paramMap = new HashMap<>();
		paramMap.put("param", con.toJSONString());
		JSONObject resJson;
		try{
			String res  = HttpTookit.doPost(COUPON_API_ADDRESS, paramMap);
			if(null == res){
				 logger.error("getCouponInfoByCouponIds error! res is null!");
				 return null;
			}
			resJson = JSONObject.parseObject(res);
			
		}catch(Exception e){
			logger.error("getCouponInfoByCouponIds error!",e);
			return null;
		}	
		
		if(null == resJson){
			logger.error("getCouponInfoByCouponIds error! resJson is null!");
			return null;
		}

		String code = resJson.getString("code"); 
		if(!code.equals("200")){
			logger.error("getCouponInfoByCouponIds error! " + resJson.toJSONString());
			return null;
		}
		JSONObject data = resJson.getJSONObject("data");
		String  listStr = data.getString("list");
		if(StringUtils.isBlank(listStr)) {
			return null;
		}
		JSONArray list =  JSONArray.parseArray(listStr);
		return list;
	}
	
	/**
     *  publishStatus '发布状态(0:未发布、1:已发布、2:手动下架、3:自动过期),
     *  activityStatus	String	活动状态(1：未开始2：已开始 3：已停领4：已停止)
     *  isExport {"0": "行销发券", "1": "用户领券"},    		
	 * @author lizhiyong
	 * 2017年6月2日
	 * @param jj
	 * @return
	 */
	public String checkCouponStatus(JSONObject jj){
		/*int isExport = jj.getIntValue("isExport");

		if(isExport==0){
			return "不能布置券类型为行销发券的优惠券";
		}*/
		int publishStatus = jj.getIntValue("publishStatus");
		
		if(publishStatus!=1){
			return "券的发布状态不是已发布状态,请检查";
		}
		int activityStatus = jj.getIntValue("activityStatus");
		if(activityStatus!=2){
			return "券的活动状态不是已开始状态,请检查";
		}
		
		int totalMaxCount = jj.getIntValue("totalMaxCount");
		int totalTakenCount = jj.getIntValue("totalTakenCount");
		
		if(totalTakenCount>=totalMaxCount){
			return "该券已被领完,请重新设置";
		}
		
		String supCouponId =jj.getString("supCouponId");
		if(StringUtils.isNotBlank(supCouponId)){
			return "不能布置组合券的子券";
		}
		String isCrmExchange = jj.getString("isCrmExchange");
		//String isCrmLimitUser = jj.getString("isCrmLimitUser");
		if(!"0".equals(isCrmExchange)){
			return "不能布置直冲券";
		}
		/*if(!"0".equals(isCrmLimitUser)){
			return "不能布置用户限定券";
		}*/
		
		int discountType = jj.getIntValue("discountType");
		String useDeadlineType = jj.getString("useDeadlineType");//使用期限。1、固定时间段 2、领取后几天  为2时接券开始和截止时间、券使用开始和结束时间均为空
		if(discountType!=10 && "1".equals(useDeadlineType)){
			long receiveEndTime = 0;
			if(jj.getString("receiveEndTimestamp")!=null && !"".equals(jj.getString("receiveEndTimestamp"))){
				receiveEndTime = jj.getLongValue("receiveEndTimestamp");
			}
			long nowTimestamp =new Date().getTime();
	
			if(receiveEndTime<nowTimestamp){
				return "当前时间大于该券的领取结束时间";
			}
		
			long useEndTime = 0;
			if(jj.getString("useEndTimestamp")!=null && !"".equals(jj.getString("useEndTimestamp"))){
				useEndTime = jj.getLongValue("useEndTimestamp");
			}
			
			if(useEndTime<nowTimestamp){
				return "当前时间大于该券的使用结束时间";
			}
			
		}
		return "OK";
	}
	
	private String getCouponStoreCodeByPgSeq(String seqs){
		List<B2BStore> list = new ArrayList<B2BStore>();
		String[] seqArray = seqs.split(",");
		for(String sq : seqArray){
			List<B2BStore> single = storeServcie.getStoreByPgSeq(sq);
			if(null != single && single.size() > 0){
				list.addAll(single);
			}
		}
		StringBuffer sbCode = new StringBuffer();
		for(int i = 0; i < list.size(); i++){
			B2BStore ys = list.get(i);
			if(null == ys){
				continue;
			}
			String code = ys.getCode();
			if(StringUtils.isBlank(code)){
				continue;
			}
			sbCode.append(code).append(",");
		}
		String resultCode = sbCode.toString();
		if(resultCode.endsWith(",")){
			resultCode = resultCode.substring(0,resultCode.length() -1);
		}
		return resultCode;
	}
	
	
	/**
	 * 通过接口获得优惠券信息
	 * @param ids
	 * @param storeCode
	 * @return
	 */
	public JSONArray getJAByCouponInfoAPI(String[] ids, String storeCode) {
		if(ids == null || ids.length <= 0 || StringUtils.isBlank(storeCode)) {
			return null;
		}
		JSONObject params = new JSONObject();
		params.put("skuSeq", StringUtils.join(ids, ","));
		params.put("storeId", storeCode);
		JSONArray list = null;
		try {
			String result = HttpTookit.doPost(COUPON_COMMDOITY_ADDRESS, "param", params.toJSONString());
			if (result == null) {
				return null;
			}
			JSONObject resJson = JSONObject.parseObject(result);
			if (resJson == null) {
				return null;
			}
			String code = resJson.getString("code"); 
			if(!code.equals("200")){
				return null;
			}
			list = resJson.getJSONArray("list");
		}catch(Exception e){
	         logger.error("COUPON_COMMDOITY_ADDRESS error", e);
		}
		return list;
	}
	
	@Override
	public List<JSONObject>  getSharePageInfoByCouponIds(String couponIds){
		List<JSONObject> resList = new ArrayList<JSONObject>();
		JSONArray arr = getCouponList(couponIds);
		if(null == arr){
			return resList;
		}
		for(int i = 0; i < arr.size(); i++){
			JSONObject single = arr.getJSONObject(i);
			String couponId = single.getString("couponId");
			String couponName = single.getString("couponName");
			String couponType = single.getString("couponType");
			String discountType = single.getString("discountType");
			String vouchersPrice = single.getString("vouchersPrice");
			String vouchersDiscount = single.getString("vouchersDiscount");
			String storeIds = single.getString("storeIds");
			Integer storeType = single.getInteger("storeType");
			String storeNames = "全国通用";
			if(storeType.intValue() == 1){ //区域
				storeNames = storeIds;
			}else if(storeType.intValue() == 2){ //门店
				storeNames = storeServcie.getStoreNamesByCodes(storeIds);
			}else if(storeType.intValue() == 3){
				storeNames = storeServcie.getStoreCodesByXiaoQuCodes(storeIds);
				;	
			}
			String isExport  = single.getString("isExport"); //优惠券类型
			String scope = single.getString("scopeDescription");
			String ust = single.getString("useStartTimestamp");
			Long useStartTimestamp = StringUtils.isBlank(ust) ? 0L : Long.valueOf(ust);
			String uet = single.getString("useEndTimestamp");
			Long useEndTimestamp = StringUtils.isBlank(uet) ? 0L : Long.valueOf(uet);
			String rst = single.getString("receiveStartTimestamp");
			Long receiveStartTimestamp = StringUtils.isBlank(rst) ? 0L : Long.valueOf(rst);
			String ret = single.getString("receiveEndTimestamp");
			Long receiveEndTimestamp = StringUtils.isBlank(ret) ? 0L : Long.valueOf(ret);
			String useStartTime = "";
			String useEndTime = "";
			String receiveStartTime = "";
			String receiveEndTime = "";
			if(useStartTimestamp.longValue()  > 0){
				useStartTime = DateUtil.getDate(new Date(useStartTimestamp));
			}
			if(useEndTimestamp.longValue() > 0){
				useEndTime = DateUtil.getDate(new Date(useEndTimestamp));	
			}
			if(receiveEndTimestamp.longValue()  > 0){
				receiveStartTime = DateUtil.getDate(new Date(receiveStartTimestamp));
			}
			if(receiveEndTimestamp.longValue() > 0){
				receiveEndTime = DateUtil.getDate(new Date(receiveEndTimestamp));
			}
			String maxCount = single.getString("totalMaxCount"); 
			String takenCount = single.getString("totalTakenCount"); 
			Long totalMaxCount = StringUtils.isBlank(maxCount) ? 0L : Long.valueOf(maxCount);
			Long totalTakenCount = StringUtils.isBlank(takenCount) ? 0L : Long.valueOf(takenCount);
			Long surplus = totalMaxCount - totalTakenCount;
			String useDeadlineType = single.getString("useDeadlineType");
			String invalidDays = single.getString("invalidDays");
			JSONObject newSingle = new JSONObject();
            newSingle.put("isExport", isExport);
			newSingle.put("couponId", couponId);
			newSingle.put("couponName", couponName);
			newSingle.put("couponType", couponType);
			newSingle.put("discountType", discountType);
			newSingle.put("vouchersPrice", vouchersPrice);
			newSingle.put("vouchersDiscount", vouchersDiscount);
			newSingle.put("storeType", storeType);
			newSingle.put("storeIds", storeIds);
			newSingle.put("storeNames", storeNames);
			newSingle.put("scope", scope);
			newSingle.put("useStartTime", useStartTime);
			newSingle.put("useEndTime", useEndTime);
			newSingle.put("receiveStartTime", receiveStartTime);
			newSingle.put("receiveEndTime", receiveEndTime);
			newSingle.put("surplus", surplus);
			newSingle.put("totalMaxCount", totalMaxCount);
			newSingle.put("useDeadlineType", useDeadlineType);
			newSingle.put("invalidDays", invalidDays);
			resList.add(newSingle);
		}
		return orderListByCouponIds(couponIds, resList);
	}
	
	private List<JSONObject> orderListByCouponIds(String couponIds, List<JSONObject> retList){
		List<JSONObject> returnList = new ArrayList<JSONObject>();
		for(String cId:couponIds.split(",")){
			for(JSONObject obj:retList){
				String couponId = obj.getString("couponId");
				if(couponId!=null && couponId.equals(cId)){
					returnList.add(obj);
					break;
				}
			}
		}
		return returnList;
	}
	
	
	private final static String COUPON_API_BY_CODE = SystemEnv.getProperty("b2bcoupon.api.host")+"/b2bCoupon/outerSrv/getCoupons4Stores";

	
	private JSONObject getCouponListByStoreCodes(JSONObject params){
		String storeCode= params.getString("storeCode");
		String miniCode = params.getString("miniCode");
		String pgSeq = params.getString("pgSeq");
		String pageNo = params.getString("pageNo");
		String pageSize = params.getString("pageSize");
		String couponIdOrName = params.getString("couponIdOrName");
		String exceptCouponIds = params.getString("exceptCouponIds");
		int type = params.getIntValue("type");
		JSONObject res=new JSONObject();
		Map<String, String> paramMap = new HashMap<String, String>();
		JSONObject jo1=new JSONObject();
		String storeType = "0";
		jo1.put("isTakeout", 0);
		if(type == 2){  //是否是外卖会员活动
			jo1.put("isTakeout", 1);
		}
		String storeIds = ""; //默认查询全国
		if((!pgSeq.equals("ALL")) && StringUtils.isBlank(storeCode)){  
			//只选区域
			storeType = "1";
			storeIds = pgSeq;
		}else if((!pgSeq.equals("ALL")) && StringUtils.isNotBlank(storeCode)){
			//选择门店
			storeType = "2";
			storeIds = storeCode;
		}
		if(StringUtils.isNotBlank(miniCode) && StringUtils.isNotBlank(storeCode)){
			//选择门店
			storeType = "2";
			storeIds = storeCode;
		}
		if(StringUtils.isNotBlank(miniCode) && StringUtils.isBlank(storeCode)){
			//选择小区
			storeType = "3";
			storeIds = miniCode;
		}
		jo1.put("storeIds", storeIds);
		jo1.put("storeType", storeType);
		if(StringUtils.isBlank(pageNo)){
			pageNo = "1";
		}
		if(StringUtils.isBlank(pageSize)){
			pageSize = "10";
		}
		
		jo1.put("pageNo", pageNo);
		jo1.put("pageSize", pageSize);
		if(StringUtils.isNotBlank(couponIdOrName)){
			jo1.put("searchInfo", couponIdOrName.trim());
		}
		if(StringUtils.isNotBlank(exceptCouponIds)){
			jo1.put("exceptCouponIds", exceptCouponIds);
		}
		jo1.put("sortField", 1);
		jo1.put("isCrm", 0);
		paramMap.put("param", jo1.toJSONString());
		String result = null;
	    
	    try {
			result = HttpTookit.doPost(COUPON_API_BY_CODE, paramMap);
		}catch(Exception e){
	         logger.error("storeIds COUPON INFO ERROR", e);
	         return res;
		}
		if (null == result) {
            logger.error("storeIds COUPON INFO ERROR:result is null,"+storeIds);
            return res;
        }
		JSONObject jo = JSONObject.parseObject(result);
		JSONObject data = jo.getJSONObject("data");
	    if(null  == data){
	    	  return res;
	     }
	    res.putAll(data);
		return res;
	}
	
	@Override
	public JSONObject getCouponListByStoreCodesInfo(JSONObject params){
		JSONObject data = getCouponListByStoreCodes(params);
		JSONObject resObj = new JSONObject();
		String count = data.getString("count");
		JSONArray arr = data.getJSONArray("list");
		resObj.putAll(params);
		resObj.put("count", count);
		if(null == arr){
			resObj.put("couponList", new ArrayList<JSONObject>());
			return resObj;
		}
		List<JSONObject> resList = new ArrayList<JSONObject>();
		for(int i = 0; i < arr.size(); i++){
			JSONObject single = arr.getJSONObject(i);
			String couponId = single.getString("couponId");
			String couponName = single.getString("couponName");
			String discountType = single.getString("discountType");
			String storeIds = single.getString("storeIds");
			Integer storeType = single.getInteger("storeType");
			String vouchersPrice = single.getString("vouchersPrice");
			String vouchersDiscount = single.getString("vouchersDiscount");
			String storeNames = "全国通用";
			if(storeType.intValue() == 1){//区域
				storeNames = storeIds;
			}else if(storeType.intValue() == 2){ //门店
				storeNames = storeServcie.getStoreNamesByCodes(storeIds);
			}else if(storeType.intValue() == 3){ //小区
				storeNames = storeServcie.getStoreNamesByXiaoQuCodes(storeIds);
			}
			String isExport  = single.getString("isExport"); //优惠券类型
			String couponType = single.getString("couponType");
			String scope = single.getString("scopeDescription");
			String ust = single.getString("useStartTimestamp");
			Long useStartTimestamp = StringUtils.isBlank(ust) ? 0L : Long.valueOf(ust);
			String uet = single.getString("useEndTimestamp");
			Long useEndTimestamp = StringUtils.isBlank(uet) ? 0L : Long.valueOf(uet);
			String rst = single.getString("receiveStartTimestamp");
			Long receiveStartTimestamp = StringUtils.isBlank(rst) ? 0L : Long.valueOf(rst);
			String ret = single.getString("receiveEndTimestamp");
			Long receiveEndTimestamp = StringUtils.isBlank(ret) ? 0L : Long.valueOf(ret);
			String useStartTime = "";
			String useEndTime = "";
			String receiveStartTime = "";
			String receiveEndTime = "";
			if(useStartTimestamp.longValue()  > 0){
				useStartTime = DateUtil.getDate(new Date(useStartTimestamp));
			}
			if(useEndTimestamp.longValue() > 0){
				useEndTime = DateUtil.getDate(new Date(useEndTimestamp));	
			}
			if(receiveEndTimestamp.longValue()  > 0){
				receiveStartTime = DateUtil.getDate(new Date(receiveStartTimestamp));
			}
			if(receiveEndTimestamp.longValue() > 0){
				receiveEndTime = DateUtil.getDate(new Date(receiveEndTimestamp));
			}
			String tmc = single.getString("totalMaxCount");
			Long  totalMaxCount = StringUtils.isBlank(tmc) ? 0L : Long.valueOf(tmc);
			String tkc = single.getString("totalTakenCount");
			Long totalTakenCount = StringUtils.isBlank(tkc) ? 0L : Long.valueOf(tkc);
			Long surplus = totalMaxCount - totalTakenCount;
			String useDeadlineType = single.getString("useDeadlineType");
			String invalidDays = single.getString("invalidDays");
			String limitUser = single.getString("limitUser");
			JSONObject newSingle = new JSONObject();
			newSingle.put("couponId", couponId);
			newSingle.put("couponName", couponName);
			newSingle.put("discountType", discountType);
			newSingle.put("vouchersPrice", vouchersPrice);
			newSingle.put("vouchersDiscount", vouchersDiscount);
			newSingle.put("storeType", storeType);
			newSingle.put("storeIds", storeIds);
			newSingle.put("storeNames", storeNames);
			newSingle.put("couponType", couponType);
			newSingle.put("isExport", isExport);
			newSingle.put("scope", scope);
			newSingle.put("useStartTime", useStartTime);
			newSingle.put("useEndTime", useEndTime);
			newSingle.put("receiveStartTime", receiveStartTime);
			newSingle.put("receiveEndTime", receiveEndTime);
			newSingle.put("totalMaxCount", totalMaxCount);
			newSingle.put("surplus", surplus);
			newSingle.put("useDeadlineType", useDeadlineType);
			newSingle.put("invalidDays", invalidDays);
			newSingle.put("limitUser", limitUser);
			resList.add(newSingle);
		}
		resObj.put("couponList", resList);
		return resObj;
	}
	
	
}
