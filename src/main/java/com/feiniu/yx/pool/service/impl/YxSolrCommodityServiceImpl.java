package com.feiniu.yx.pool.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.YxRemoteCommodityService;
import com.feiniu.yx.util.HttpTookit;
import com.feiniu.yx.util.ImageUtils;
import com.feiniu.yx.util.YxPoolConst;

/**
 *	通过solr搜索接口查商品详细 
 */
@Service
public class YxSolrCommodityServiceImpl implements YxRemoteCommodityService {
	
	private Logger logger = Logger.getLogger(YxSolrCommodityServiceImpl.class);
	
	//后台solr接口，通过RT货号查询用
	private final static String SOLR_COMMOIDTY_URL = SystemEnv.getProperty("yxCommodity.solr");
	//前台solr，商品id查询用
	private final static String SOLR_URL_GOODSNO = SystemEnv.getProperty("yxCommodity.front.solr");

	@Override
	public List<YxPoolCommodity> getRemoteCommodityListByIds(String[] ids) {
		return this.getRemoteCommodityListByIds(ids,0);
	}
 
	/**
	 * 查商品数据，
	 * searchType： 1：RT货号查询，默认商品id查询
	 */
	@Override
	public List<YxPoolCommodity> getRemoteCommodityListByIds(String[] ids,int searchType) {
		if(ids==null ||ids.length<=0)return null;
		ids = getArrayNotNull(ids);
		List<YxPoolCommodity> poolCommoditys = new ArrayList<YxPoolCommodity>();
		int length = ids.length;
		int multiple = length / 10 ;
		int remainder = length % 10;
		for(int j = 1; j <= multiple; j++){
			String[]  ii = Arrays.copyOfRange(ids, 10*(j-1), (j)*10);
			List<YxPoolCommodity> batchCommoditys = getListCommodity(batchRemoteCommodityOfStoreInfo(ii,searchType));
    	    if(null == batchCommoditys){
    		   continue;
    	    }
    	    poolCommoditys.addAll(batchCommoditys);
		}
     
		if(remainder > 0){
			String[]  ii = Arrays.copyOfRange(ids, 10*multiple, 10*multiple+remainder);
			List<YxPoolCommodity> batchCommoditys = getListCommodity(batchRemoteCommodityOfStoreInfo(ii,searchType));
			if(null != batchCommoditys){
				poolCommoditys.addAll(batchCommoditys);
			}
		}
		return poolCommoditys;
	}
 
	/**
	 * 查商品数据，
	 * searchType： 1：RT货号查询，默认商品id查询
	 */
	@Override
	public Map<String,YxPoolCommodity>  getRemoteCommodityMapByIds(String[] ids,int searchType) {
		if(ids==null ||ids.length<=0)return null;
		ids = getArrayNotNull(ids);
		Map<String,YxPoolCommodity> poolMap= new HashMap<String,YxPoolCommodity>();
		int length = ids.length;
		int multiple = length / 10 ;
		int remainder = length % 10;
		for(int j = 1; j <= multiple; j++){
			String[]  ii = Arrays.copyOfRange(ids, 10*(j-1), (j)*10);
			Map<String,YxPoolCommodity> batchMap = batchRemoteCommodityOfStoreInfo(ii,searchType);
    	    if(null == batchMap){
    		   continue;
    	    }
			poolMap.putAll(batchMap);
		}
     
		if(remainder > 0){
			String[]  ii = Arrays.copyOfRange(ids, 10*multiple, 10*multiple+remainder);
			Map<String,YxPoolCommodity> batchMap = batchRemoteCommodityOfStoreInfo(ii,searchType);
			if(null != batchMap){
				poolMap.putAll(batchMap);
			}
		}
		return poolMap;
	}
	
	private Map<String,YxPoolCommodity> batchRemoteCommodityOfStoreInfo(String[] ids,int searchType){
		if (searchType == 1) {
			return batchRemoteCommodityOfStoreInfoBySERIALNO(ids, searchType);
		}
		Map<String,YxPoolCommodity> m = new HashMap<String,YxPoolCommodity>();
		if(ids.length == 0){
			return m;
		}
		StringBuilder apiurl = new StringBuilder();
		apiurl.append(SOLR_URL_GOODSNO);
		
		if(ids.length == 1) {
			apiurl.append("(GOODS_NO:"+ids[0]+")");
		}else {
			apiurl.append("(GOODS_NO:");
			for (int i = 0; i < ids.length; i++) {
				if (i > 0) {
					apiurl.append("%20OR%20");
				}
				apiurl.append(ids[i]);
			}
			apiurl.append(")");
		}
		
		
		try {
			String result = HttpTookit.doGet(apiurl.toString());
			if (result == null) {
				return m;
			}
			JSONObject rs = JSONObject.parseObject(result);
			JSONObject response = rs.getJSONObject("response");
			if (response == null) {
				return m;
			}
			JSONArray docs = response.getJSONArray("docs");
			if (docs == null) {
				return m;
			}
			m = getMapCommodity(docs);
			
		} catch(Exception e) {
			logger.error("solr error: " + apiurl.toString());
			return m;
		}
		return m;
	}
	
	/**
	    * 返回商品的map 
	    * @param docs
	    * @return
	    */
   private Map<String,YxPoolCommodity> getMapCommodity(JSONArray docs){
	   Map<String, YxPoolCommodity> maps = new HashMap<String,YxPoolCommodity>();
	   for(int i = 0, len = docs.size(); i < len; i++){
		   YxPoolCommodity singleCommdity = null;
		   JSONObject singleDoc = docs.getJSONObject(i);
		   String keyNo = singleDoc.getString("GOODS_NO");
		   String storeCode = singleDoc.getString("STORE_CODE");
		   if (maps.containsKey(keyNo)) {//商品存在
			   singleCommdity = maps.get(keyNo);
			   //累加商品的区域
			   storeCode = singleCommdity.getStoreCode() + "," +storeCode;
			   singleCommdity.setStoreCode(storeCode);
			   continue;
		   }
		   singleCommdity = new YxPoolCommodity(); 
		   String title = singleDoc.getString("TITLE");
		   String picUrl = singleDoc.getString("PIC_URL");
		   String fsDate = singleDoc.getString("FIRST_ON_DT");
		   if(StringUtils.isNotBlank(fsDate)){
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					singleCommdity.setAddOnDate(sdf.parse(fsDate));
				} catch (ParseException e) {
				}
		   }
		   singleCommdity.setCommodityId(keyNo);
		   singleCommdity.setStoreCode(storeCode);
		   singleCommdity.setTitle(title);
		   if(StringUtils.isNotBlank(picUrl)){
			   singleCommdity.setPicUrl(ImageUtils.getImageUrl(picUrl));
		   }
		   singleCommdity.setOriginate(YxPoolConst.YX_COMMODITY_TYPE_COMMODITY);
		   maps.put(keyNo, singleCommdity);
		}
		return maps;
   }
	
	 /**
	  * 分批处理
	  * @param ids
	  * @param searchType
	  * @return
	  */
	private Map<String,YxPoolCommodity> batchRemoteCommodityOfStoreInfoBySERIALNO(String[] ids,int searchType){
		StringBuilder api = new StringBuilder();
		api.append(SOLR_COMMOIDTY_URL);
		
		if(ids.length == 0){
			return new HashMap<String,YxPoolCommodity>();
		}
		String paramName = searchType == 1 ? "SERIAL_NO" : "GOODS_NO";
		api.append(paramName).append(":(");
		String no = StringUtils.join(ids,"%20OR%20");
		api.append(no).append(")");
		api.append("%20AND%20MERCHANT_TYPE:1");
		api.append("&wt=json");
		JSONArray docs = null;
		try {
			String result = HttpTookit.doGet(api.toString());
			JSONObject job = JSONObject.parseObject(result);
			if (!job.containsKey("response")) {
				logger.error("SOLR_COMMOIDTY_URL error:" + result);
				return null;
			}
			JSONObject response = job.getJSONObject("response");
			if (!response.containsKey("docs")) {
				logger.error("SOLR_COMMOIDTY_URL error:" + result);
				return null;
			}
			docs = response.getJSONArray("docs");
			if (null == docs) {
				logger.error("SOLR_COMMOIDTY_URL error:" + result);
				return null;
			}
			if (docs.size() == 0){
				return new HashMap<String,YxPoolCommodity>();
			}
		}catch(Exception e){
	         logger.error("SOLR_COMMOIDTY_URL error", e);
	         return null;
		}
		return getMapCommodity(docs,searchType);
	}
  
	/**
	 * 数组去重
	 * @param str
	 * @return
	 */
	private String[] getArrayNotNull(String[] str) {
		List<String> list = new ArrayList<String>();
		String[] result = new String[list.size()];
		if (null == str) {
			return list.toArray(result);
		}
	  
		if (str.length == 0) {
			return list.toArray(result);
		}
	  
		for (String s : str) {
			if (StringUtils.isBlank(s)) {
			   continue;
			}
			list.add(s.trim());
		}
		return list.toArray(result);
	}
 
   /**
    * 返回商品的map 
    * @param docs
    * @return
    */
   private Map<String,YxPoolCommodity> getMapCommodity(JSONArray docs ,int searchType){
	   Map<String, YxPoolCommodity> maps = new HashMap<String,YxPoolCommodity>();
	   for(int i = 0, len = docs.size(); i < len; i++){
		   YxPoolCommodity singleCommdity = null;
		   JSONObject singleDoc = docs.getJSONObject(i);
		   String keyNo = singleDoc.getString("GOODS_NO");
		   
		   if(searchType == 1){  //RT货号时
			   keyNo = singleDoc.getString("SERIAL_NO");
		   }
		   String storeCode = singleDoc.getString("STORE_CODE");
		   Integer showStatus = singleDoc.getInteger("SHOW_STATUS");//商品是否显示  1显示
		   //商品不显示，跳过
	    	if (showStatus != 1) {
	    		 continue;
	    	}
		   //区域不存在，跳过这条记录
		   if (StringUtils.isBlank(storeCode)) { 
			   continue;
		   }
		   if (maps.containsKey(keyNo)) {//商品存在
			   singleCommdity = maps.get(keyNo);
			   //累加商品的区域
			   storeCode = singleCommdity.getStoreCode() + "," +storeCode;
			   singleCommdity.setStoreCode(storeCode);
			   continue;
		   }
		   singleCommdity = new YxPoolCommodity(); 
		   String rtNo = singleDoc.getString("SERIAL_NO");
		   String goods_no = singleDoc.getString("GOODS_NO");
		   String title = singleDoc.getString("TITLE");
		   String picUrl = singleDoc.getString("PIC_URL");
		   String fsDate = singleDoc.getString("FIRST_ON_DT");
		   if(StringUtils.isNotBlank(fsDate)){
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
					singleCommdity.setAddOnDate(sdf.parse(fsDate));
				} catch (ParseException e) {
				}
		   }
		   singleCommdity.setCommodityId(goods_no);
		   singleCommdity.setStoreCode(storeCode);
		   singleCommdity.setRtNo(rtNo);
		   singleCommdity.setRemark(rtNo);  //写入数据库
		   singleCommdity.setTitle(title);
		   if(StringUtils.isNotBlank(picUrl)){
			   singleCommdity.setPicUrl(ImageUtils.getImageUrl(picUrl));
		   }
		   singleCommdity.setOriginate(YxPoolConst.YX_COMMODITY_TYPE_COMMODITY);
		   
		   maps.put(keyNo, singleCommdity);
		}
		return maps;
   }
   
   /**
    * 将搜索查的数据拼装成集合
    * @param docs
    * @return
    */
   private List<YxPoolCommodity> getListCommodity(Map<String,YxPoolCommodity> maps){
	   List<YxPoolCommodity> list = new ArrayList<YxPoolCommodity>();
	   if(null == maps){
		   return list;
	   }
	   if (maps.size() == 0) {
		   return list;
	   }
	   //转换成list
	   for(Entry<String, YxPoolCommodity> entry : maps.entrySet()){
		   YxPoolCommodity pp = entry.getValue();
		   list.add(pp);
	   }
	   return list;
   }
  
}
