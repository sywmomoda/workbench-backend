package com.feiniu.yx.page.service.module;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.ModuleDataService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.PoolDataService;
import com.feiniu.yx.util.YxPoolConst;
@Service
public class IXianTeMaiService implements CustomModule {
	@Autowired
	private ModuleDataService moduleDataService;
	
	@Autowired
	private PoolDataService poolDataService;
	
	@Override
	public void findCustomData(JSONObject mjo) {
		String storeCode = mjo.getString("storeCode");
		if(StringUtils.isBlank(storeCode)){
			return;
		}
		String previewTime = mjo.getString("backendPreviewTime");
		JSONObject proJo  = mjo.getJSONObject("moduleProperties");
		
		//优选档期处理
		String index[] = new String[]{"one","two","three","four"};
		List<JSONObject> commodityList = null;
		for(int i=0; i<index.length; i++){
			JSONObject obj = proJo.getJSONObject(index[i]); 
			if(checkIsInNow(obj)){
				commodityList = getCommodityList(obj,"poolId",YxPoolConst.COMMODITY_TYPE_TOSHOW_COMMODITY,storeCode,6,previewTime);
				break;
			}
		}
		mjo.put("commodityList", commodityList);
	}
	
	
	
	private boolean checkIsInNow(JSONObject objNew) {
		String bt = objNew.getString("startTime");
		String et = objNew.getString("endTime");
		String isUse = objNew.getString("isUse");
		if("1".equals(isUse)){
			Calendar cl = Calendar.getInstance();
			int hour = cl.get(cl.HOUR_OF_DAY);
			int minute = cl.get(cl.MINUTE);
			int now = hour*100 + minute;
			int beginTime = Integer.parseInt(bt.replace(":", ""));
			int endTime = Integer.parseInt(et.replace(":", ""));
			if(endTime <= beginTime){
				endTime = endTime + 2400;
			}
			if((now>=beginTime && now<endTime) || ((now+2400)>=beginTime && (now+2400)<endTime)){
				return true;
			}
		}
		return false;
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
			if((int)pc.getOriginate()==1){
				temp.put("imgUrl", pc.getPicUrl());
				temp.put("linkType", 2);
				temp.put("linkUrl", pc.getCommodityId());
				temp.put("title", pc.getTitle());
				temp.put("price", pc.getPrice());
				temp.put("unit", pc.getUnit());
				temp.put("marketPrice", pc.getMarketPrice());
				temp.put("specWeight", pc.getSpecWeight());
				temp.put("hasService", pc.getHasService());
				temp.put("scriptTitle", pc.getScriptTitle());
				temp.put("propertyTitle", pc.getPropertyTitle());
				temp.put("commodityCouponMap", pc.getCommodityCouponMap());
			}
			
			list.add(temp);
		}
		return list;
	}
	
}
