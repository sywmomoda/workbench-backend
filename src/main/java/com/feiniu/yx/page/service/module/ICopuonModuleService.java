package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.PageService;
import com.feiniu.yx.pool.service.YxCouponService;
import com.feiniu.yx.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ICopuonModuleService implements CustomModule {
	@Autowired
	private PageService pageService;
	@Autowired
	private YxCouponService couponService;
	@Override
	public void findCustomData(JSONObject mjo) throws Exception {
		JSONObject moduleProperties = mjo.getJSONObject("moduleProperties");
		String couponIds = moduleProperties.getString("couponId");
		if(StringUtils.isNotBlank(couponIds)){
			if(couponIds.indexOf(",")!=-1){
				JSONArray couponArray = moduleProperties.getJSONArray("couponId");
				Map<String,JSONObject> mapCoupon = couponService.mapCouponInfoByCouponIds(StringUtils.join(couponArray,","));
				String[] md5s = new String[couponArray.size()];
				String[] endInfo = new String[couponArray.size()];
				for(int i=0;i<couponArray.size();i++){
					md5s[i]= MD5Util.getMD5(couponArray.getString(i) + MD5Util.COUPON_MD5_KEY);
					endInfo[i]= mapCoupon.get(couponArray.getString(i)).getString("endInfo");
				}
				moduleProperties.put("md5s", md5s);
				moduleProperties.put("endInfos", endInfo);
			}else{
				moduleProperties.put("md5s", MD5Util.getMD5(moduleProperties.getString("couponId") + MD5Util.COUPON_MD5_KEY));
				Map<String,JSONObject> mapCoupon = couponService.mapCouponInfoByCouponIds(moduleProperties.getString("couponId"));
				moduleProperties.put("endInfos", mapCoupon.get(moduleProperties.getString("couponId")).getString("endInfo"));
			}
		}
		
		Long pageId = mjo.getLong("pageId");
		Page page = pageService.queryPageByID(pageId);
		if(null == page){
			return;
		}
		
		String pagePro = page.getPageProperties();
		JSONObject pageProObj = JSONObject.parseObject(pagePro);
		String pagebgcolor =pageProObj.getString("pagebgcolor"); 
		if(StringUtils.isBlank(pagebgcolor)){
		  return;
		}
		moduleProperties.put("pagebgColor", pagebgcolor);
	}
}
