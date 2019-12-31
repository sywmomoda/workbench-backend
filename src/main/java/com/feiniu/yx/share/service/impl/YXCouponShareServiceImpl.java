package com.feiniu.yx.share.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.pool.service.YxCouponService;
import com.feiniu.yx.share.dao.YXCouponShareDao;
import com.feiniu.yx.share.entiry.YXCouponShare;
import com.feiniu.yx.share.service.YXCouponShareService;
import com.feiniu.yx.util.HttpTookit;

@Service
public class YXCouponShareServiceImpl implements YXCouponShareService {
	
	public static Logger logger = Logger.getLogger(YXCouponShareServiceImpl.class);
	
	@Autowired
	private YXCouponShareDao shareDao;
	
	@Autowired
	private YxCouponService couponService;
	
	private static String  COUPON_FRONT_HOST = SystemEnv.getProperty("couponShare.front.host");
	private static String  COUPON_FRONT_HOST2 = SystemEnv.getProperty("couponShare.front2.host");
    private static String  COUPON_FRONT_HOST3 = SystemEnv.getProperty("couponShare.front3.host");  //优惠券分享新的
	private static String YXTASK_DOMAIN = SystemEnv.getProperty("yxtask.domain");
	
	@Override
	public void insertCouponShare(YXCouponShare couponShare){
		shareDao.insert(couponShare);
	}
	
	@Override
	public YXCouponShare queryCouponShareById(Long id) {
		YXCouponShare couponShare = shareDao.queryCouponShareById(id);
		setYXCouponShare(couponShare);

		return couponShare;
	}
	
	public JSONObject queryJSONObjectById(Long id){
		YXCouponShare couponShare = queryCouponShareById(id);
		JSONObject result = JSONObject.parseObject(JSONObject.toJSONStringWithDateFormat(couponShare, "yyyy-MM-dd HH:mm:ss"));
		String couponPropreties = couponShare.getCouponProperties();
		JSONObject couponObj = JSONObject.parseObject(couponPropreties);
		String couponIds = couponObj.getString("couponIds");
		List<JSONObject> couponList = couponService.getSharePageInfoByCouponIds(couponIds);
		result.put("couponList", couponList);
		String couponNames = couponObj.getString("couponNames");
		validaCouponName(couponIds,couponNames,couponList);
		return result;
	}
	
	/**
	 * 替换名称
	 * @param couponIds
	 * @param couponNames
	 * @param couponList
	 */
	private void validaCouponName(String couponIds,String couponNames,List<JSONObject> couponList){
		if(StringUtils.isBlank(couponIds)){
			return;
		}
		if(StringUtils.isBlank(couponNames)){
			return;
		}
		if(null == couponList){
			return;
		}
		if(couponList.isEmpty()){
			return;
		}
		String[] ids = couponIds.split(",");
		String[] names = couponNames.split(",");
		Map<String,String> map = new HashMap<String,String>();
		for(int i =0; i < ids.length; i++){
			if(names.length > i){
				map.put(ids[i], names[i]);
			}
		}
		for(JSONObject obj : couponList){
			String couponName = obj.getString("couponName");
			String couponId = obj.getString("couponId");
			String name = map.get(couponId);
			if(StringUtils.isNotBlank(name)){
				couponName  = name;
			}
			obj.put("couponName", couponName);
		}
		
	}
	

	@Override
	public void updateCouponShare(YXCouponShare couponShare){
		shareDao.update(couponShare);
		updatePageRedisData(couponShare.getId().toString());
	}
   
   
	@Override
	public JSONObject updateCouponShareEndTime(YXCouponShare couponShare) {
		JSONObject result = new JSONObject();

		Long id = couponShare.getId();
		if(null == id){
			result.put("status", 0);
			result.put("msg","id为空");
			return result;
		}
		Date endDate = couponShare.getEndTime();
		YXCouponShare couponShareOld = queryCouponShareById(id);
		String couponPropreties = couponShareOld.getCouponProperties();
		JSONObject couponObj = JSONObject.parseObject(couponPropreties);
		String couponIds = couponObj.getString("couponIds");
		List<JSONObject> couponList = couponService.getSharePageInfoByCouponIds(couponIds);
		for(int i = 0; i < couponList.size(); i++){
			JSONObject obj = couponList.get(i);
			Date recEndDate = obj.getDate("receiveEndTime");
			if(null == recEndDate){
				continue;
			}
			if(endDate.after(recEndDate)){
				String couponId = obj.getString("couponId");
				result.put("status", 2);
				result.put("couponId",couponId);
				return result;
			}
			
		}
		shareDao.update(couponShare);
		updatePageRedisData(couponShare.getId().toString());
		result.put("status", 1);
		result.put("msg","保存成功");
		return result;
	}
	
	private void updatePageRedisData(String pageId){
		JSONObject param = new JSONObject();
		param.put("pageId", pageId);
		StringBuffer uri = new StringBuffer();
		uri.append(YXTASK_DOMAIN);
		uri.append("/rest/yxCouponShare/updatePageData");
		String res = HttpTookit.doPost(uri.toString(), "data",param.toJSONString());
		logger.debug(res);
	}
	
	@Override
	public List<YXCouponShare> queryCouponShareList(YXCouponShare couponShare) {
		List<YXCouponShare> list = shareDao.queryCouponShareList(couponShare);
		Integer total = couponShare.getTotalRows();
		if(total > 0 && list.size() == 0){
			couponShare.setCurPage(1);
			list = shareDao.queryCouponShareList(couponShare);
		}
		for(YXCouponShare ycs : list){
			if(null  == ycs){
				continue;
			}
			setYXCouponShare(ycs);
		}
		
		return list;
	}
	
	
	
	private void setYXCouponShare(YXCouponShare couponShare){
		
		if(couponShare.getStatus() != 3){  //非终止状态
			int status = getShareStatus(couponShare.getBeginTime(),couponShare.getEndTime());
			couponShare.setStatus(status);
		}	
		/*String names = storeService.getStoreNamesByCodes(couponShare.getStoreCode());
		couponShare.setStoreName(names);*/
		Long id = couponShare.getId();
		couponShare.setCode(getShareCode(id));
		String frontUrl = COUPON_FRONT_HOST.replace("{id}",couponShare.getCode()) ;
		if(couponShare.getType() == 2){
			frontUrl = COUPON_FRONT_HOST2.replace("{id}", couponShare.getCode());
		}else if(couponShare.getType() == 3){ //优惠券分享新版
            frontUrl = COUPON_FRONT_HOST3.replace("{id}", couponShare.getCode());
        }
		couponShare.setFrontUrl(frontUrl);
	}
	
	private String getShareCode(Long id){
		if(null == id){
			return "";
		}
		
		int len = getLen(id);
		if(len > 9){
			return id.toString();
		}
		int surplusLen = 9 -len;
		StringBuffer sbCode = new StringBuffer();
		sbCode.append("FXY");
		for(int i = 0; i < surplusLen; i++){
			sbCode.append("0");	
		}
		sbCode.append(id);
		return sbCode.toString();
	}
	
	private static int getLen(long x){
	     if(x<10) return 1;
	     return getLen(x/10)+1;
	}
	
	private int getShareStatus(Date beginTime,Date endTime){
		Date now = new Date();
		int status = -1; //无效
		if(now.before(beginTime)){
			status = 2; //未开始
		}else if(now.after(endTime)){
			status = 0; //已经过期
		}else if(now.after(beginTime) && now.before(endTime)){
			status = 1 ;//进行中
		}
		return status;
	}
	
}
