package com.feiniu.yx.page.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.page.dao.YxRemoteCommodityDao;
import com.feiniu.yx.page.entity.YxRemoteCommodity;
import com.feiniu.yx.page.service.RemoteCommodityService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.YxPoolCommodityService;
import com.feiniu.yx.util.HttpTookit;
import com.feiniu.yx.util.ImageUtils;

@Component
public class RemoteCommodityServiceImpl implements RemoteCommodityService {
	@Autowired
	private YxRemoteCommodityDao  remoteCommodityDao;
	@Autowired
	private YxPoolCommodityService commodityService;
	
	private final String PRODUCT_SEARCH_URL = SystemEnv.getProperty("yxCommodity.productSearchUrl");
	private final String PRODUCT_SEARCH_HOST = SystemEnv.getProperty("yxCommodity.productSearchHost");
	
	private Logger logger = Logger.getLogger(RemoteCommodityService.class);
	@Override
	public List<YxRemoteCommodity> queryRemoteCommodityFromInterface(String storeCode,String category,String type, Integer sum) {
		String param = paramConcat(storeCode, category, type, sum);
		List<YxRemoteCommodity> rcList = queryRemoteInterface(param, storeCode, type);
		return rcList;
	}
	
	public Map<String,YxRemoteCommodity> queryRemoteCommodityFromInterface(String storeCode,String ids,String type) {
		StringBuffer param = new StringBuffer("?");
		param.append("store_code=").append(storeCode);
		if("tryEating".equals(type)){
			//param.append(SystemEnv.getProperty("yxCommodity.searchNewRecommendParam"));
			param.append("&fl=first_on_dt,sell_point&goods_no="+ids);
		}
		Map<String,YxRemoteCommodity> yrMap = new HashMap<String,YxRemoteCommodity>();
		List<YxRemoteCommodity> rcList = queryRemoteInterface(param.toString(), storeCode, type);
		for(YxRemoteCommodity yr:rcList){
			if(ids.indexOf(yr.getCommodityId())!=-1)
			yrMap.put(yr.getCommodityId(), yr);
		}
		return yrMap;
	}
	
	
	@Override
	public String syncRemoteCommodity(String storeCode,String category,String type, int sum) {
		String param = paramConcat(storeCode, category, type, sum);
		List<YxRemoteCommodity> rcList = queryRemoteInterface(param, storeCode, type);
		remoteCommodityDao.deleteYxRemoteCommodityByTypeAndCode(type, storeCode);
		if(rcList.size()>0){
			remoteCommodityDao.saveList(rcList);
		}
		return "更新数据"+rcList.size()+"条;";
	}

	@Override
	public List<YxRemoteCommodity> getLocalCommodityByType(String storeCode,String type) {
		List<YxRemoteCommodity> rcList = remoteCommodityDao.queryYxRemoteCommodityByTypeAndCode(type, storeCode);
		return rcList;
	}
	
	private String paramConcat(String storeCode, String category,String type, Integer sum){
		StringBuffer param = new StringBuffer("?");
		param.append("store_code=").append(storeCode);
		if("newRecommend".equals(type)){
			//param.append(SystemEnv.getProperty("yxCommodity.searchNewRecommendParam"));
			param.append("&sort=first_on_dt&fl=cp_seqs,first_on_dt,sell_point&firstondt_inc=1&resnum="+sum);
		}else if("stampPrinting".equals(type)){
			param.append("&isgroupcate=true&fl=cp_seqs,sell_point&group_field=cp_seq&resnum="+sum+"&price_type=1&sort=Hsm_soldqty&group_limit=6");
		}else if("stampPrintingList".equals(type)){
			param.append("&fl=cp_seqs,sell_point&group_field=cp_seq&resnum="+sum+"&price_type=1&sort=Hsm_soldqty");
		}else if("category".equals(type)){
			param.append("&isgroupcate=true&fl=cp_seqs,price_type,sell_point&group_field=cp_seq&resnum="+sum+"&group_limit=6&sort=Hsm_soldqty&is_pricetypes=true&price_type=2,3,4,5");
		}
		if(StringUtils.isNotBlank(category)){
			param.append("&cp_seqs=").append(category);
		}
		return param.toString();
	}
	
	
	private List<YxRemoteCommodity> queryRemoteInterface(String param, String storeCode, String type){
		List<YxRemoteCommodity> rcList = new ArrayList<YxRemoteCommodity>();
		if(StringUtils.isNotBlank(param) ) {
			String url = PRODUCT_SEARCH_URL + param;
			try{
				//String url = "http://fresh-search.beta1.fn/freshsearch/homesearchproduct?store_code=1002&group=true&group_limit=1&fl=cp_seqs&group_field=cp_seq&resnum=6&price_type=1&sort=Hsm_soldqty"+param;
				String result = HttpTookit.doGet(url);
				JSONObject object = JSONObject.parseObject(result);
			    if(null  == object){
			    	return rcList;
			    }
				JSONObject grouped = object.getJSONObject("grouped");
				
				if(grouped!=null){
					JSONObject seqs = grouped.getJSONObject("cp_seq");
					if(seqs==null)  return rcList;
					JSONArray groups = seqs.getJSONArray("groups");
					if(groups==null) return rcList;
					for (int i = 0; i < groups.size(); i++) {
						 JSONObject docObj = groups.getJSONObject(i).getJSONObject("doclist");
						 if (docObj==null) continue;
						 JSONArray doclist = docObj.getJSONArray("docs");
						 if(doclist!=null){
							 for(int j=0; j<doclist.size(); j++){
								 JSONObject seq = doclist.getJSONObject(j);
								 YxRemoteCommodity rc = convertJSONObjToCommodity(seq);
								 if(rc!=null){
									 rc.setStoreCode(storeCode);
									 rc.setType(type);
									 rcList.add(rc);
								 }
								
							 }
						 }
					 }
				}
				
				JSONObject response = object.getJSONObject("response");
				if(response!=null){
					JSONArray doclist = response.getJSONArray("docs");
					if(doclist==null) return rcList;
					for(int j=0;j<doclist.size();j++){
						 JSONObject seq = doclist.getJSONObject(j);
						 YxRemoteCommodity rc = convertJSONObjToCommodity(seq);
						 rc.setStoreCode(storeCode);
						 rc.setType(type);
						 rcList.add(rc);
					}
				}
			}catch(Exception e){
				logger.error("productSearch error: " + url.toString());
			}
		}
		return rcList;
	}
	
	private YxRemoteCommodity  convertJSONObjToCommodity(JSONObject obj){
		YxRemoteCommodity rc = new YxRemoteCommodity();
		if(obj.getString("sell_slogan")==null){
			return null;
		}
		rc.setCommodityId(obj.getString("goods_no"));//商品ID
		if(StringUtils.isNotBlank(obj.getString("pic_url"))){
			rc.setPicUrl(ImageUtils.getImageUrl(obj.getString("pic_url")));//图片地址
		}
		rc.setCategory(obj.getString("cp_seqs")!=null?obj.getString("cp_seqs"):"[]");//类目
		rc.setPromoteText(obj.getString("sell_point")!=null?obj.getString("sell_point"):"");//促销语
		rc.setTitle(obj.getString("sell_slogan")!=null?obj.getString("sell_slogan"):"");//标题
		if(obj.getInteger("price_type")!=null){
			rc.setPriceType(obj.getInteger("price_type"));
		}
		if(obj.getInteger("first_on_dt")!=null){
			Long time = obj.getLong("first_on_dt");
			Calendar cl = Calendar.getInstance();
			cl.setTimeInMillis(time*1000);
			rc.setAddOnDate(cl.getTime());
		}
		rc.setCreateId("remote");
		rc.setCreateTime(new Date());
		rc.setUpdateId("remote");
		rc.setUpdateTime(new Date());
		return rc;
	}
	
	public YxPoolCommodity  convertToPoolCommodity(YxRemoteCommodity obj){
		YxPoolCommodity rc = new YxPoolCommodity();
		rc.setCommodityId(obj.getCommodityId());//商品ID
		rc.setPicUrl(obj.getPicUrl());//图片地址
		rc.setPromoteText(obj.getPromoteText());//促销语
		rc.setTitle(obj.getTitle());//标题
		rc.setPriceType(obj.getPriceType());
		rc.setAddOnDate(obj.getAddOnDate());
		rc.setCreateId("remote");
		rc.setCreateTime(new Date());
		rc.setUpdateId("remote");
		rc.setUpdateTime(new Date());
		rc.setOriginate(1);
		return rc;
	}


	@Override
	public String syncRemoteCommodityToPool(String storeCode, Long poolId, String type, int days, int count, String category) {
		StringBuffer param = new StringBuffer("?");
		param.append("store_code=").append(storeCode);
		param.append("&sort=first_on_dt&fl=cp_seqs,first_on_dt,sell_point&resnum="+count);
		param.append("&firstondt_inc="+days);
		if(StringUtils.isNotBlank(category)){
			param.append("&cp_seqs=").append(category);
		}
		String result = "success";
		List<YxRemoteCommodity> rcList = queryRemoteInterface(param.toString(), storeCode, type);
		if(rcList.size()>0){
			List<YxPoolCommodity> pcList = new ArrayList<YxPoolCommodity>();
			for(YxRemoteCommodity yrc: rcList){
				YxPoolCommodity pc = convertToPoolCommodity(yrc);
				pcList.add(pc);
			}
			result = commodityService.addYxPoolCommodityByIdFromRemote(pcList, poolId, storeCode, 2);
		}
		return result;
	}

	@Override
	public List<YxRemoteCommodity> queryCommodityForNSelectOne(
			String storeCode, String category, Integer sum) {
		StringBuffer param = new StringBuffer("?");
		param.append("campseq=").append(category);
		param.append("&store_code=").append(storeCode);
		param.append("&sort=recommend&fl=camp_seq%2Csell_point%2Cgoods_no%2Ccp_seq%2Cdesc_av_name&pn=1&resnum="+sum);
		List<YxRemoteCommodity> rcList = new ArrayList<YxRemoteCommodity>();
		String url = PRODUCT_SEARCH_HOST + param;
		try{
			String result = HttpTookit.doGet(url);
			JSONObject object = JSONObject.parseObject(result);
		    if(null  == object){
		    	return rcList;
		    }
			JSONObject response = object.getJSONObject("response");
			if(response!=null){
				JSONArray doclist = response.getJSONArray("docs");
				if(doclist==null) return rcList;
				for(int j=0;j<doclist.size();j++){
					 JSONObject seq = doclist.getJSONObject(j);
					 YxRemoteCommodity rc = convertJSONObjToCommodity(seq);
					 rc.setStoreCode(storeCode);
					 rcList.add(rc);
				}
			}
		}catch(Exception e){
			logger.error("productSearch error: " + url.toString());
		}
		return rcList;
	}



}
