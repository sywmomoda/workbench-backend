package com.feiniu.yx.pool.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.YXCouponService;
import com.feiniu.yx.common.service.YXCouponXiaoQuService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.pool.service.YxCouponService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.DateUtil;
import com.feiniu.yx.util.HttpTookit;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

@Service
public class YxCouponServiceImpl implements YxCouponService {

	private static Logger logger = Logger.getLogger(YxCouponServiceImpl.class);
	
		
	@Autowired
	private YXStoreService storeService;
	
	@Autowired
	private YXCouponXiaoQuService xqService;
	
	@Autowired
	private YXCouponService couponService;
	
	@Override
	public  JSONObject getCouponStoreCodesById(String couponIds){
		JSONObject res= new JSONObject();
        JSONObject dataObject = couponService.getCouponInfoByCouponIds(couponIds);
        if(null == dataObject){
        	return res;
        }
        JSONArray arr = dataObject.getJSONArray("list");
        if (arr == null) {
        	return res;
        }
        for(int i=0;i<arr.size();i++){
    		JSONObject jj =arr.getJSONObject(i);
    		int storeType = jj.getIntValue("storeType");
    		String storeIds = jj.getString("storeIds");
    		if(storeType == 3){
    			storeIds = xqService.getStoreCodesByXiaoQuCodes(storeIds);
    		}else if(storeType == 1){ //区域
    		   String ids = getCouponStoreCodeByPgSeq(storeIds);
    		   if(StringUtils.isNotBlank(ids)){
    			   storeIds = ids;  
    		   }
    		}
        	res.put(jj.getString("couponId"),storeIds);
        }
        return res;
	}
	
	private String getCouponStoreCodeByPgSeq(String seqs){
		List<YXStore> list = new ArrayList<YXStore>();
		String[] seqArray = seqs.split(",");
		for(String sq : seqArray){
			List<YXStore> single = storeService.getStoreByPgSeq(sq);
			if(null != single && single.size() > 0){
				list.addAll(single);
			}
		}
		StringBuffer sbCode = new StringBuffer();
		for(int i = 0; i < list.size(); i++){
			YXStore ys = list.get(i);
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

	@Override
	public JSONObject getCouponInfoByCouponId(String id) throws ParseException {
		JSONObject res = new JSONObject();
		JSONObject dataObject = couponService.getCouponInfoByCouponIds(id);
		JSONArray arr = dataObject.getJSONArray("list");
		if (arr != null) {
			for (int i = 0; i < arr.size(); i++) {
				JSONObject jj = arr.getJSONObject(i);
				String couponId = jj.getString("couponId");
				// publishStatus '发布状态(0:未发布、1:已发布、2:手动下架、3:自动过期),
				// activityStatus String 活动状态(1：未开始2：已开始 3：已停领4：已停止)
				// isExport {"0": "行销发券", "1": "用户领券"},
				if (id.equals(couponId)) {
					String check = checkCouponStatus(jj);
					if ("OK".equals(check)) {
						res.put("couponId", couponId);
						res.put("couponName", jj.getString("title"));
						res.put("vouchersDiscount", jj.getString("vouchersDiscount"));
						res.put("vouchersPrice", jj.getString("vouchersPrice"));
						res.put("scopeDescription", jj.getString("scopeDescription"));
						res.put("discountType", jj.getString("discountType"));
						res.put("limitUser", jj.getString("limitUser"));
						// couponType:优惠券适用范围(0:全场通用 1:范围限定
						// 2:商品限定3:品牌限定,4:单品限定即单品券) discountType=7
						// 且couponType=4为单品券；discountType=7
						// 且couponType!=4则为普通优惠券
						res.put("couponType", jj.getString("couponType"));
						res.put("totalMaxCount", jj.getString("totalMaxCount"));
						res.put("dayMaxCount", jj.getString("dayMaxCount"));
						res.put("skuId", jj.getString("skuId"));
						if("5".equals(jj.getString("discountType"))){
							res.put("skuId", jj.getString("skuIdGift"));
						}
						res.put("skuName", jj.getString("skuName"));
						String storeIds = jj.getString("storeIds");
						int storeType = jj.getIntValue("storeType");
						if(storeType == 3){  //小区
							storeIds = xqService.getStoreCodesByXiaoQuCodes(storeIds);	
						}else if(storeType == 1){ //区域
			    		   String ids = getCouponStoreCodeByPgSeq(storeIds);
			    		   if(StringUtils.isNotBlank(ids)){
			    			   storeIds = ids;  
			    		   }
			    		}
						String useDeadlineType = jj.getString("useDeadlineType");
						if("1".equals(useDeadlineType)){
							String uet = jj.getString("useEndTimestamp");
							Long useEndTimestamp = StringUtils.isBlank(uet) ? 0L : Long.valueOf(uet);
							if(useEndTimestamp.longValue() > 0){
								String useEndDate = DateUtil.getDate(new Date(useEndTimestamp),"MM/dd");
								res.put("useEndTime", useEndDate);
							}
							String ust = jj.getString("useStartTimestamp");
							Long useStartTimestamp = StringUtils.isBlank(ust) ? 0L : Long.valueOf(ust);
							if(useStartTimestamp.longValue() > 0){
								String useBegin = DateUtil.getDate(new Date(useStartTimestamp),"MM/dd");
								res.put("useStartTime", useBegin);
							}
							if(res.getString("useStartTime")!=null && res.getString("useEndTime")!=null ){
								res.put("endInfo", "使用期限："+ res.getString("useStartTime") + "-" + res.getString("useEndTime"));
								res.put("endInfoBrief", res.getString("useStartTime") + "-" + res.getString("useEndTime"));
							}

						}else if("2".equals(useDeadlineType)){
							String invalidDays = jj.getString("invalidDays");
							res.put("endInfo", "领取后"+invalidDays+"日内可用");
							res.put("endInfoBrief", invalidDays+"日内可用");
						}

						res.put("storeCodes", storeIds);
					} else {
						res.put("msg", check);
					}
					break;
				}
			}
		}
		if (res.get("msg") == null && res.get("couponName") == null) {
			res.put("msg", "根据优惠券ID:" + id + "未查找到相应的优惠券信息");
		}
		return res;
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
		int isExport = jj.getIntValue("isExport");

		if(isExport==0){
			return "不能布置券类型为行销发券的优惠券";
		}
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
		String isCrmLimitUser = jj.getString("isCrmLimitUser");
		if(!"0".equals(isCrmExchange)){
			return "不能布置直冲券";
		}
		if(!"0".equals(isCrmLimitUser)){
			return "不能布置用户限定券";
		}
		
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
	
	@Override
	public List<JSONObject>  getSharePageInfoByCouponIds(String couponIds){
		List<JSONObject> resList = new ArrayList<JSONObject>();
		JSONObject data = couponService.getCouponInfoByCouponIds(couponIds);
		JSONArray arr = data.getJSONArray("list");
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
				storeNames = storeService.getStoreNamesByCodes(storeIds);
			}else if(storeType.intValue() == 3){
				storeNames = xqService.getXiaoQuNamesByCodes(storeIds);
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
	
	
	private final static String COUPON_API_BY_CODE = SystemEnv.getProperty("coupon.api.host")+"/coupon/outerSrv/getCoupons4Stores";

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
				storeNames = storeService.getStoreNamesByCodes(storeIds);
			}else if(storeType.intValue() == 3){ //小区
				storeNames = xqService.getXiaoQuNamesByCodes(storeIds);
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
			resList.add(newSingle);
		}
		resObj.put("couponList", resList);
		return resObj;
	}
	
	
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
	
	@Override
	public String  getCouponIdsByCommodityId(String[] ids,String storeCode){
		JSONArray list = couponService.getJAByCouponInfoAPI(ids, storeCode);
		if(null == list){
			return null;
		}

		StringBuffer sbCouponIds = new StringBuffer();
		for(int i = 0; i < list.size(); i++){
			JSONObject single = list.getJSONObject(i);
			if(null == single){
				continue;
			}
			boolean isTure = "2".equals(single.getString("activityStatus"))
					&& !"10".equals(single.getString("discountType"));
			if (!isTure) {
				continue;
			}
			String couponId = single.getString("couponId");
			if(StringUtils.isBlank(couponId)){
				continue;
			}
			sbCouponIds.append(couponId).append(",");
		}
		
		String couponIds = sbCouponIds.toString();
		if(couponIds.endsWith(",")){
			couponIds = couponIds.substring(0, couponIds.length() -1);
		}
		return couponIds;
	}

	
	@Override
	public Map<String, JSONObject> mapCouponInfoByCouponIds(String couponIds) {
		Map<String, JSONObject> mapResult = new HashMap<String, JSONObject>();
		JSONObject dataObject = couponService.getCouponInfoByCouponIds(couponIds);
		if(null == dataObject){
			return mapResult;
		}
		JSONArray arr = dataObject.getJSONArray("list");
		if (null == arr) {
			return mapResult;
		}
		for (int i = 0; i < arr.size(); i++) {
			JSONObject res = new JSONObject();
			JSONObject jj = arr.getJSONObject(i);
			String couponId = jj.getString("couponId");
			// publishStatus '发布状态(0:未发布、1:已发布、2:手动下架、3:自动过期),
			// activityStatus String 活动状态(1：未开始2：已开始 3：已停领4：已停止)
			// isExport {"0": "行销发券", "1": "用户领券"},
			res.put("couponId", couponId);
			res.put("couponName", jj.getString("title"));
			res.put("vouchersDiscount", jj.getString("vouchersDiscount"));
			res.put("vouchersPrice", jj.getString("vouchersPrice"));
			res.put("scopeDescription", jj.getString("scopeDescription"));
			res.put("discountType", jj.getString("discountType"));
			// couponType:优惠券适用范围(0:全场通用 1:范围限定
			// 2:商品限定3:品牌限定,4:单品限定即单品券) discountType=7
			// 且couponType=4为单品券；discountType=7
			// 且couponType!=4则为普通优惠券
			res.put("couponType", jj.getString("couponType"));
			res.put("totalMaxCount", jj.getString("totalMaxCount"));
			res.put("dayMaxCount", jj.getString("dayMaxCount"));
			String storeIds = jj.getString("storeIds");
			res.put("storeCodes", storeIds);
			String useDeadlineType = jj.getString("useDeadlineType");
			if("1".equals(useDeadlineType)){
				String uet = jj.getString("useEndTimestamp");
				String ust = jj.getString("useStartTimestamp");
				Long useEndTimestamp = StringUtils.isBlank(uet) ? 0L : Long.valueOf(uet);
				Long useStartTimestamp = StringUtils.isBlank(ust) ? 0L : Long.valueOf(ust);
				if(useEndTimestamp.longValue() > 0){
					String useEndDate = DateUtil.getDate(new Date(useEndTimestamp),"yyyy/MM/dd");
					String useStartDate = DateUtil.getDate(new Date(useStartTimestamp),"yyyy/MM/dd");
					res.put("endInfo", useStartDate+"-"+useEndDate);
				}

			}else if("2".equals(useDeadlineType)){
				String invalidDays = jj.getString("invalidDays");
				res.put("endInfo", "领取后"+invalidDays+"天内可用");
			}

			mapResult.put(couponId, res);
		}
		return mapResult;
	}
	
}
