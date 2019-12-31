package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.PoolDataService;
import com.feiniu.yx.util.YxPoolConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class IFeedsFlowService implements CustomModule {
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
		long poolAdId = proJo.getLongValue("poolIdAd");
		if(poolAdId>0) {
			List<JSONObject> picAdList = getCommodityList(poolAdId, YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC, storeCode, 10, previewTime);
			mjo.put("picAdList", picAdList);
		}
	}
	
	
	/**
	 * 获取商品
	 * 
	 * @param storeCode
	 * @param totalCount
	 */
	private List<JSONObject> getCommodityList(Long poolId, String type, String storeCode,
			Integer totalCount,String previewTime) {

		List<JSONObject> list = new ArrayList<JSONObject>();
		List<YxPoolCommodity> commodityList = poolDataService.findListByIdAndType(poolId, storeCode,
				type,totalCount.intValue(),previewTime);

		if (null == commodityList || commodityList.size() == 0) {
			return list;
		}

		for (int i = 0; i < commodityList.size(); i++) {
			YxPoolCommodity pc = commodityList.get(i);
			JSONObject temp = new JSONObject();
			if((int)pc.getOriginate()==2){
				temp.put("picUrl", pc.getPicUrl());
				temp.put("linkType", 2);
				temp.put("linkUrl", pc.getCommodityId());
				temp.put("title", pc.getTitle());
			}
			
			list.add(temp);
		}
		return list;
	}
	
}
