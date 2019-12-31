package com.feiniu.yx.core.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.core.CommoditySaleInfoService;
import com.feiniu.yx.util.HttpTookit;

//限时好券
@Service
public class CommoditySaleInfoServiceImpl implements CommoditySaleInfoService {
	public static Logger logger = Logger.getLogger(CommoditySaleInfoServiceImpl.class);
	private final String SALE_INFO_API = SystemEnv.getProperty("storeApp.saleInfoUrl");
	private final String SALE_COMMENTS_API = SystemEnv.getProperty("saleInfo.saleCommentsInfoUrl");
	private final String HEADPICARRAY = SystemEnv.getProperty("saleInfo.saleCommentsHeadPics");
	@Override
	public long querySaleInfo(String storeCode, String commodityId, long beginTime,
			long endTime) {
		//http://searchplatform.beta1.fn/fresh_sale_data?goods_no=P0717060000015646,P0117060000014509&facet=term:aggs_field:goods_no,sum:sum_sale_qty:sale_qty&facet_ext=size:1,start:0&dt=1517414400000_1517500799999
		//StringBuilder api = new StringBuilder("http://searchplatform.beta1.fn/fresh_sale_data");
		//api.append(SALE_INFO_API);
		StringBuilder api = new StringBuilder(SALE_INFO_API);
		api.append("?goods_no=").append(commodityId);
		api.append("&store_code=").append(storeCode);
		api.append("&facet=term:aggs_field:goods_no,sum:sum_sale_qty:sale_qty&facet_ext=size:1,start:0");
		api.append("&dt=").append(beginTime).append("_").append(endTime);
		long amount = 0;
		try {
			String result = HttpTookit.doGet(api.toString());
			JSONObject job = JSONObject.parseObject(result);
			if (!job.containsKey("response")) {
				logger.error("SALE_INFO_API error:" + result);
				return 0;
			}
			JSONObject response = job.getJSONObject("response");
			if (!response.containsKey("results")) {
				logger.error("SALE_INFO_API error:" + result);
				return 0;
			}
			JSONObject results = response.getJSONObject("results");
			JSONObject facet = results.getJSONObject("facet");
			JSONArray list = facet.getJSONArray("data");
			if (null == list || list.size() == 0) {
				return 0;
			}
			JSONObject obj = (JSONObject) list.get(0);
			if(obj.containsKey("sum_sale_qty")){
				amount = obj.getJSONObject("sum_sale_qty").getLongValue("value");
			}
			return amount;
			
		}catch(Exception e){
	         logger.error("SALE_INFO_API error", e);
	         return 0;
		}
	}

	@Override
	public List<JSONObject> queryComments(String commodityId, String exceptWords) {
		
		List<JSONObject> objList = new ArrayList<JSONObject>();
		String exceptWordsOr = exceptWords.replace(",", " OR ");
		StringBuilder api = new StringBuilder(SALE_COMMENTS_API);
		api.append("?goods_no=").append(commodityId);
		api.append("&count=10&fl=comment_text");
		String searchDSL = "((-comment_text:\"\") AND comment_text:*)";
		if(exceptWordsOr.length()>0){
			searchDSL = "((-comment_text:\"\") AND comment_text:*) AND (-(comment_text:("+exceptWordsOr+")))";
		}
		try {
			api.append("&search_dsl=").append(URLEncoder.encode(searchDSL,"UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			logger.error("SALE_COMMENTS_API URLEncoder error:" + searchDSL);
		}
		//http://searchplatform.idc1.fn/fresh_comment?goods_no=P2818060000120110,P1218070000123275&count=10&fl=comment_text&search_dsl=(-(comment_text:(一二三 OR 好评)))
		try {
			String result = HttpTookit.doGet(api.toString());
			JSONObject job = JSONObject.parseObject(result);
			if (!job.containsKey("response")) {
				logger.error("SALE_COMMENTS_API error:" + result);
				return objList;
			}
			JSONObject response = job.getJSONObject("response");
			if (!response.containsKey("results")) {
				logger.error("SALE_COMMENTS_API error:" + result);
				return objList;
			}
			JSONObject results = response.getJSONObject("results");
			JSONArray list = results.getJSONArray("list");
			if (null == list || list.size() == 0) {
				return objList;
			}else{
				for(int i=0;i<list.size();i++){
					JSONObject obj = list.getJSONObject(i);//{"comment_text":"XXX"}
					obj.put("headPic", getRandomHeadPic());
					objList.add(obj);
				}
			}
			return objList;
			
		}catch(Exception e){
	         logger.error("SALE_COMMENTS_API error", e);
	         return objList;
		}
	}
	
	private String getRandomHeadPic(){
		String[] picArray = HEADPICARRAY.split(",");
		int rad = (int)(Math.random()*100);
		int index = rad%picArray.length;
		return picArray[index];
	}
	
	
	
	
	
	
	
}
