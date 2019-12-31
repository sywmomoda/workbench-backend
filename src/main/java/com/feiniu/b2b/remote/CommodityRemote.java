package com.feiniu.b2b.remote;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.util.HttpTookit;
import com.feiniu.yx.util.YxPoolConst;

/**
 * B2B商品远程接口调用
 * @author tongwenhuan
 *
 */
public class CommodityRemote {
	
	private static Logger logger = Logger.getLogger(CommodityRemote.class);
	//商品信息接口
	private static final String B2BCOMMODITY_STORE_INFO = YxPoolConst.B2B_COMMODITY_HOST_DOMAIN+"/commodity/getRtInfoByItemNoForCMS";
	//价格信息接口
	private static final String B2B_COMMODITY_PRICE_INFO = YxPoolConst.B2B_COMMODITY_HOST_DOMAIN+"/commodity/getGoodsInfoByItemNoForCMS";
	/**
	 * 通过门店和商品id查询商品信息
	 * @param ids
	 * @param codes
	 * @return
	 */
	public static JSONObject getInfoByIds2StoreCodes(String ids,String codes){
		if (StringUtils.isBlank(ids)) {
			return null;
		}
		JSONObject jObject = new JSONObject();
		jObject.put("item_no", ids);
		if (StringUtils.isNotBlank(codes)) {
			jObject.put("rt_no", codes);
		}
		JSONObject remoteData = null;
		try {
			String result = HttpTookit.doPost(B2BCOMMODITY_STORE_INFO,"data",jObject.toJSONString());
			if (null == result) {
				return null;
			}
			JSONObject resultObject = JSONObject.parseObject(result);
			String code = resultObject.getString("code");
			
			if(!code.equals("0")){
				return null;
			}
			String data = resultObject.getString("data");
			
			if(data== null){
				return null;
			}
			remoteData = JSONObject.parseObject(data);
			if(remoteData.size() == 0){
	            return null;
			}
		}catch(Exception e){
	         logger.error("B2BCOMMODITY_STORE_INFO error", e);
		}
		return remoteData;
	}
	
	public static JSONObject getInfoByIds(String ids){
		return getInfoByIds2StoreCodes(ids, null);
	}
	
	public static JSONObject getPriceByStoreAndIds(String ids, String storeCode) {
		JSONObject goodsInfo = null;
		JSONObject params = new JSONObject();
		params.put("item_no", ids);
		params.put("rt_no", storeCode);
		try {
			String result = HttpTookit.doPost(B2B_COMMODITY_PRICE_INFO,"data", params.toJSONString());
			JSONObject jo = JSON.parseObject(result);
	        String code = jo.getString("code");
	        if(!"0".equals(code)){
	            return null;
	        }
	        JSONObject dataObject = jo.getJSONObject("data");
	        if (dataObject == null) {
	        	return null;
	        }
	        goodsInfo = dataObject.getJSONObject("goods_info");
		} catch(Exception e) {
			 logger.error("B2BCOMMODITY_STORE_INFO error", e);
		}
		return goodsInfo;
	}
}
