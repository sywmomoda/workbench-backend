package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.PoolDataService;
import com.feiniu.yx.util.YxPoolConst;
import com.fn.cache.client.RedisCacheClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IIconService implements CustomModule {
	
	@Autowired
	private PoolDataService poolDataService;
	@Autowired
	private RedisCacheClient cacheClient;
	private final static String ICON_BG_KEY = "YX_ICON_BG_KEY"; 
	
	@Override
	public void findCustomData(JSONObject mjo) {
		String storeCode = mjo.getString("storeCode");
		if(StringUtils.isBlank(storeCode)){
			return;
		}
		String previewTime = mjo.getString("backendPreviewTime");
		JSONObject proJo  = mjo.getJSONObject("moduleProperties");
		
		JSONObject moduleType = mjo.getJSONObject("moduleType");
		if("i-icon".equals(moduleType.getString("code"))){//icon组件背景处理存入缓存
			String bgColor = proJo.getString("bgColor");
			//背景图处理
			JSONObject bgObj = new JSONObject();
			List<JSONObject> bgPicList = getCommodityList(proJo,"poolIdBg", YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC,storeCode,1, previewTime);
			if(bgPicList.size()>0){
				bgObj = bgPicList.get(0);
			}
			if(StringUtils.isNotBlank(bgObj.getString("imgUrl"))){
				mjo.put("bgImgUrl", bgObj.getString("imgUrl"));
				cacheClient.put(ICON_BG_KEY+"_"+storeCode, bgObj.getString("imgUrl"));
			}else if(StringUtils.isNotBlank(bgColor)){
				mjo.put("bgColor", bgColor);
				cacheClient.put(ICON_BG_KEY+"_"+storeCode, bgColor);
			}else{
				cacheClient.del(ICON_BG_KEY+"_"+storeCode);
			}
		}else{//利益点组件及滚动消息组件背景从缓存读取
			String bgString = cacheClient.get(ICON_BG_KEY+"_"+storeCode);
			if(StringUtils.isNotBlank(bgString)){
				if(bgString.startsWith("#")){//颜色
					mjo.put("bgColor", bgString);
				}else{
					mjo.put("bgImgUrl", bgString);
				}
			}
		}
	}
	
	
	
	
	/**
	 * 获取商品
	 * 
	 * @param obj
	 * @param storeCode
	 * @param totalCount
	 */
	private List<JSONObject> getCommodityList(JSONObject obj, String pojoId, String type, String storeCode,
			Integer totalCount, String previewTime) {

		List<JSONObject> list = new ArrayList<JSONObject>();
		if (null == obj) {
			return list;
		}
		if (StringUtils.isBlank(storeCode)) {
			return list;
		}
		Long poolId = obj.getLong(pojoId);
		poolId = null == poolId ? 1L : poolId;
		List<YxPoolCommodity> commodityList = poolDataService.findListByIdAndType(poolId, storeCode,
				type,totalCount.intValue(),previewTime);

		if (null == commodityList || commodityList.size() == 0) {
			return list;
		}

		for (int i = 0; i < commodityList.size(); i++) {
			YxPoolCommodity pc = commodityList.get(i);
			JSONObject temp = new JSONObject();
			temp.put("imgUrl", pc.getPicUrl());
			list.add(temp);
		}
		return list;
	}
	
}
