package com.feiniu.b2b.share.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.coupon.service.B2BCouponService;
import com.feiniu.b2b.share.dao.B2BCouponShareDao;
import com.feiniu.b2b.share.entiry.B2BCouponShare;
import com.feiniu.b2b.share.service.B2BCouponShareService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.HttpTookit;
import com.fn.cache.client.RedisCacheClient;

@Service
public class B2BCouponShareServiceImpl implements B2BCouponShareService {
	
	public static Logger logger = Logger.getLogger(B2BCouponShareServiceImpl.class);
	//缓存时间
	private static final int CACHE_EXPIRE = 120;
	private static final int COUPON_DATA_CACHE_EXPIRE = 1296000;
	
	
	@Autowired
	private B2BCouponShareDao shareDao;
	
	@Autowired
	private B2BCouponService couponService;
	
	private static String  COUPON_FRONT_HOST = SystemEnv.getProperty("b2bCouponShare.front.host");
	private static String  COUPON_FRONT_HOST2 = SystemEnv.getProperty("couponShare.front2.host");
	private static String YX_INDEX_HOST = SystemEnv.getProperty("yx.indexAPI.host");
	
	@Autowired
	private RedisCacheClient cacheClient;
	
	@Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	@Override
	public void insertCouponShare(B2BCouponShare couponShare){
		shareDao.insert(couponShare);
		updatePageRedisData(couponShare.getId());
	}
	
	@Override
	public B2BCouponShare queryCouponShareById(Long id) {
		B2BCouponShare couponShare = shareDao.queryCouponShareById(id);
		setYXCouponShare(couponShare);
		return couponShare;
	}
	
	public JSONObject queryJSONObjectById(Long id){
		B2BCouponShare couponShare = queryCouponShareById(id);
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
	public void updateCouponShare(B2BCouponShare couponShare){
		shareDao.update(couponShare);
		updatePageRedisData(couponShare.getId());
	}
   
   
	@Override
	public JSONObject updateCouponShareEndTime(B2BCouponShare couponShare) {
		JSONObject result = new JSONObject();

		Long id = couponShare.getId();
		if(null == id){
			result.put("status", 0);
			result.put("msg","id为空");
			return result;
		}
		Date endDate = couponShare.getEndTime();
		B2BCouponShare couponShareOld = queryCouponShareById(id);
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
		updatePageRedisData(couponShare.getId());
		result.put("status", 1);
		result.put("msg","保存成功");
		return result;
	}
	
	private void updatePageRedisData(Long pageId){
		if(null == pageId) {
			return;
		}
		String pageCode = getShareCode(pageId);
		StringBuffer uri = new StringBuffer();
		uri.append(YX_INDEX_HOST);
		uri.append("/b2bCouponShare/getNoCacheData");
		final String res = HttpTookit.doPost(uri.toString(), "pageCode",pageCode);
		final String pastKey = "CMS_YX_SHARE_COUPON_TIME_"+pageId; //缓存过期时间
		final String key = "B2B_COUPON_SHARE_KEY_"+pageCode;  //缓存数据
		Long cacheTime = cacheClient.ttl(pastKey); //缓存更新时间
		//缓存更新时间过期
		if(cacheTime == null || cacheTime <= 0) {
			long ct = cacheClient.incr(pastKey);
			if (ct == 1) {
				cacheClient.expire(pastKey, CACHE_EXPIRE);
				threadPoolTaskExecutor.execute(new Runnable(){
					@Override
					public void run() {
						try {
							cacheClient.put(key, COUPON_DATA_CACHE_EXPIRE, res);
						} catch (Exception e) {
						}
					}
					
				});
			}
		}
	}
	
	@Override
	public List<B2BCouponShare> queryCouponShareList(B2BCouponShare couponShare) {
		List<B2BCouponShare> list = shareDao.queryCouponShareList(couponShare);
		Integer total = couponShare.getTotalRows();
		if(total > 0 && list.size() == 0){
			couponShare.setCurPage(1);
			list = shareDao.queryCouponShareList(couponShare);
		}
		for(B2BCouponShare ycs : list){
			if(null  == ycs){
				continue;
			}
			setYXCouponShare(ycs);
		}
		
		return list;
	}
	
	
	
	private void setYXCouponShare(B2BCouponShare couponShare){
		
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
