package com.feiniu.yx.page.service.module;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.core.impl.ModuleCommodityServiceImpl;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.remote.CommodityActivityRemote;

@Service
public class IPackageModuleService implements CustomModule {

	@Autowired
	private ModuleCommodityServiceImpl commoditySerivce;
	
	
	@Override
	public void findCustomData(JSONObject mjo) throws Exception {
		String storeCode = mjo.getString("storeCode");
		if(StringUtils.isBlank(storeCode)){
			return;
		}
		JSONObject proJo  = mjo.getJSONObject("moduleProperties");
		String activityId = proJo.getString("activityId");
		JSONObject activityObject = CommodityActivityRemote.getActivityCommodity(storeCode, activityId);
		if(null == activityObject) {
			return;
		}
		String goods = activityObject.getString("goods");
		if(StringUtils.isBlank(goods)) {
			return;
		}
		Integer pieceNum = activityObject.getInteger("pieceNum");
		pieceNum =  pieceNum == null  ? 0 : pieceNum;
		if(pieceNum == 0) {
          return;   
		}
		String[] commodityId = goods.split(",");
		if(pieceNum > commodityId.length) {
			return;
		}
		commodityId = Arrays.copyOfRange(commodityId, 0, pieceNum);
		Map<String,YxPoolCommodity> mapCom = commoditySerivce.mapCommodityNOConponScript(commodityId, storeCode);
		if(null == mapCom || mapCom.size() == 0) {
			return;
		}
		BigDecimal sumPrice = new BigDecimal(0f);
		StringBuffer sbIds = new StringBuffer();
		List<Integer> qtyList = new ArrayList<Integer>();
        for(int i =0; i < commodityId.length; i++) {
        	YxPoolCommodity comodity = mapCom.get(commodityId[i]);
        	if(null == comodity) {
        		continue;
        	}
        	sumPrice = sumPrice.add(new BigDecimal(comodity.getPrice().toString()));
        	sbIds.append(comodity.getCommodityId()).append(",");
        	Integer qty = comodity.getMinQuantity();
        	if(qty <= 0) {
        		qty = 1;
        	}
        	qtyList.add(qty);
        }
        String ids = sbIds.toString();
        if(ids.length() > 0 && ids.endsWith(",")) {
        	ids = ids.substring(0, ids.length() -1);
        }
        String sum = commoditySerivce.getBaseCommodityServiceImpl().processPriceZero(sumPrice.toString());
        activityObject.put("sumPrice", sum);
        activityObject.put("ids", ids);
        activityObject.put("qtys", StringUtils.join(qtyList,","));
        Float promoPrice = activityObject.getFloatValue("price");
        Float subPrice = new BigDecimal(sum).subtract(new BigDecimal(promoPrice.toString())).setScale(2,BigDecimal.ROUND_DOWN).floatValue();
        activityObject.put("subPrice", commoditySerivce.getBaseCommodityServiceImpl().processPriceZero(subPrice.toString()));
		mjo.put("commodityList", mapCom.values());
		mjo.put("ruleInfo", activityObject);
	}
}
