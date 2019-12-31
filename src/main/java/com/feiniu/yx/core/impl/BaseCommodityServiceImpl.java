package com.feiniu.yx.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.core.SuperCommodityService;
import com.feiniu.yx.core.utils.GoodsUtils;
import com.feiniu.yx.core.utils.ThreadLocalUtil;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.util.ImageUtils;

/**
 * 重设商品信息服务
 * 重设基本信息，价格，库存，标题，主图等
 * @author tongwenhuan
 * @time 2018-6-25
 */
@Service
public class BaseCommodityServiceImpl implements SuperCommodityService {
	private final static String INDEX_ID = SystemEnv.getProperty("index.page.id");
	
	/**
	 * 给池的商品集合重置价格库存标题图片信息
	 */
	@Override
	public void resetParams(List<YxPoolCommodity> list, String storeCode) {
		String[] ids = arrayIds(list);
		Map<String, JSONObject> info_m = GoodsUtils.getMapByInfoAPI(ids);
		Map<String, JSONObject> price_m = GoodsUtils.getMapByPriceAPI(ids, storeCode);
		boolean info_is_nonull = info_m.size() > 0;
		boolean price_is_nonull = price_m.size() > 0;
		for (YxPoolCommodity yc : list) {
			if (info_is_nonull) {
				resetInfoParam(yc, info_m.get(yc.getCommodityId()));
			}
			if (price_is_nonull) {
				resetPriceParam(yc, price_m.get(yc.getCommodityId()));
			}
		}
	}
	
	protected String[] arrayIds(List<YxPoolCommodity> list) {
		String[] ids = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			YxPoolCommodity c = list.get(i);
			ids[i] = c.getCommodityId();
		}
		return ids;
	}
	
	/**
	 * 通过商品id查询商品价格库存信息
	 * @param ids 商品id
	 * @param storeCode 门店code
	 */
	public Map<String, YxPoolCommodity> mapCommodityPriceInfo(String[] ids, String storeCode) {
		List<YxPoolCommodity> list = listCommodityPriceInfo(ids, storeCode);
		if (list == null) {
			return null;
		}
		Map<String, YxPoolCommodity> map = new HashMap<String, YxPoolCommodity>();
		for (YxPoolCommodity c : list) {
			map.put(c.getCommodityId(), c);
		}
		return map;
	}
	
	/**
	 * 通过商品id查询商品价格库存信息
	 * @param ids 商品id
	 * @param storeCode 门店code
	 */
	public List<YxPoolCommodity> listCommodityPriceInfo(String[] ids, String storeCode) {
		if (ids == null) {
			return null;
		}
		Map<String, JSONObject> price_m = GoodsUtils.getMapByPriceAPI(ids, storeCode);
		if (price_m.size() == 0) {
			return null;
		}
		List<YxPoolCommodity> list = new ArrayList<YxPoolCommodity>();
		for (String id : ids) {
			YxPoolCommodity yc = null;
			JSONObject jo = price_m.get(id);
			if (jo != null) {
				yc = new YxPoolCommodity();
				yc.setCommodityId(id);
				resetPriceParam(yc, jo);
				//当前门店可售
				if (yc.isExist()) {
					list.add(yc);
				}
			}
		}
		return list;
	}
	
	/**
	 * 通过商品id查询商品价格库存信息
	 * @param ids 商品id
	 * @param storeCode 门店code
	 */
	public List<YxPoolCommodity> listCommodityAllInfo(String[] ids, String storeCode) {
		if (ids == null) {
			return null;
		}
		List<YxPoolCommodity> list = listCommodityPriceInfo(ids, storeCode);
		Map<String, JSONObject> info_m = GoodsUtils.getMapByInfoAPI(ids);
		if (info_m.size() == 0) {
			return list;
		}
		for (YxPoolCommodity yc : list) {
			resetInfoParam(yc, info_m.get(yc.getCommodityId()));
		}
		return list;
	}
	

	/**
	 * 通过商品id查询商品价格库存信息
	 * @param ids 商品id
	 * @param storeCode 门店code
	 */
	public Map<String, YxPoolCommodity> mapCommodityAllInfo(String[] ids, String storeCode) {
		if (ids == null) {
			return null;
		}
		List<YxPoolCommodity>  list = listCommodityAllInfo(ids,storeCode);
		if(null == list){
			return null;
		}
		Map<String, YxPoolCommodity> map = new HashMap<String, YxPoolCommodity>();
		for (YxPoolCommodity yc : list) {
			map.put(yc.getCommodityId(), yc);
		}
		return map;
	}
	
	/**
	 * 设置商品信息
	 * @param c 商品
	 * @param jo 商品信息
	 */
	private void resetPriceParam(YxPoolCommodity c, JSONObject jo) {
		if (jo == null) {
			c.setExist(false);
			return;
		}
		Integer isSale = jo.getInteger("isSale");//是否该区域可卖 1 可卖
    	Integer status = jo.getInteger("status");//商品状态 9 下架
    	Integer isAbn = jo.getInteger("isAbn");//商品是否异常 0 正常
    	Integer showStatus = jo.getInteger("showStatus");//商品是否显示  1显示
    	if (isSale !=1 || status == 9 || isAbn != 0 || showStatus != 1) {
    		c.setExist(false);
    		return;
    	}
    	Integer isNew = jo.getInteger("isNew"); //商品是否为新品
    	if(null != isNew){
    		c.setIsNew(isNew);
    	}
	    Long saleQty = jo.getLong("saleQty"); //库存 0 不可卖
    	if (saleQty != null){
            c.setStockSum(saleQty);
    	}
    	Float price = jo.getFloat("price"); //价格
    	if (price != null) {
    		c.setPrice(price);
    	}
    	Integer priceType = jo.getInteger("priceType"); //0正常价，1促销价
    	if (priceType != null){
            c.setIsSSM(priceType==1);
        }
    	Float costPrice = jo.getFloat("costPrice"); //市场价
    	if (null != costPrice && null != price) {
        	/*if (costPrice >= price) {
        		String costPrice_str = String.valueOf(costPrice);
        		String costPrice_str_pro = processPriceZero(costPrice_str);
        		c.setMarketPrice(Float.parseFloat(costPrice_str_pro));
        	}*/
    		String costPrice_str = String.valueOf(costPrice);
    		String costPrice_str_pro = processPriceZero(costPrice_str);
    		c.setMarketPrice(Float.parseFloat(costPrice_str_pro));
        }
    	Integer goodsType = jo.getInteger("goodsType");//商品类别
    	String suUnit =  jo.getString("suUnit");
    	c.setUnit("500g");
    	if (goodsType != null && goodsType == 0){//0为标品 1为称重品，称重品单位由售卖量+售卖单位组成
    		c.setUnit(suUnit);
    		//标品规格
    		String spec = jo.getString("spec");
    		if (spec != null) {
    			c.setSpecWeight(spec);
    		}
    	} else if (goodsType != null && goodsType == 1){//1为称重品，称重品单位由售卖量+售卖单位组成
    		Integer saleWay = jo.getInteger("saleWay"); //saleway==0 为纯称重品，纯称重品需设置最小起订量和售卖单位
    		if (saleWay !=null && saleWay == 0){
    			Integer suNum = jo.getInteger("suNum");
    			Integer minQuantity = jo.getInteger("minQuantity");
    			if (suNum != null && minQuantity != null){
    	    		c.setMinQuantity(suNum*minQuantity);
    			}
    		}
    		Integer specWeight = jo.getInteger("specWeight");//称重品每件的重量
    		if (specWeight != null && specWeight.intValue() > 0) {
    			c.setSpecWeight(specWeight + "g/" + suUnit);
    			if(specWeight > c.getStockSum()){
    				c.setStockSum(0L);
    			}
    		}
    	}
    	if (jo.get("service") != null){
        	JSONArray servicesArray = jo.getJSONArray("service");
            c.setHasService(servicesArray!=null&&servicesArray.size()>0?"1":"0");
        }
    	if (jo.get("saleLevel") != null){//促销级别
        	Integer saleLevel = jo.getInteger("saleLevel");
            c.setPriceType(saleLevel);//因前台solr  saleLevel返回为price_type字段，统一处理
        }
	}
	
	//处理小数点后两位
	public String processPriceZero(String price){
		if(StringUtils.isBlank(price) || !price.contains("."))return price;
		if(price.startsWith(".")){
			price = "0"+price;
		}
		int index = price.indexOf(".");
		if(index!= -1 && price.length() - index >3){
			price =price.substring(0,index+3);
		}
		Float floatValue=Float.parseFloat(price);
		String parsedString= floatValue.toString();
		if(parsedString.endsWith(".0")||parsedString.endsWith(".00")){//去0
			parsedString =parsedString.substring(0,index);
		}
		return parsedString;
	}
 
    /**
     * 重新设置商品标题，主图等
     * @param c 商品实体
     * @param jo 商品信息
     */
    private void resetInfoParam(YxPoolCommodity c, JSONObject jo) {
    	if (jo == null) {
    		return;
    	}
		String picUrl = jo.getString("picUrl");
		String sellPt = jo.getString("sellPt");
		String title = jo.getString("title");
		String brandSeq = jo.getString("brandSeq");
		String picTurnUrl  = c.getPicTurnUrl();
		Page page = ThreadLocalUtil.getPage();
		boolean isIndex = false;

		if(null != page && StringUtils.isNotBlank(INDEX_ID)){
			Long pageId = page.getId();
			String id = pageId == null ? "" : pageId.toString();
			if(INDEX_ID.contains(id)){
                isIndex = true;  //是首页
			}
		}
		if(StringUtils.isNotBlank(picTurnUrl)&& !isIndex){
			picUrl = picTurnUrl;
		}
		if(StringUtils.isNotBlank(picUrl)){
			c.setPicUrl(ImageUtils.getImageUrl(picUrl));
		}
		if(StringUtils.isBlank(c.getPromoteText())){ 
			//设置促销语,为空时取接口，不为空取本地
			c.setPromoteText(sellPt);
		}
		if(StringUtils.isNotBlank(title)){
			c.setTitle(title);
		}
		if(StringUtils.isNotBlank(brandSeq)){
			c.setBrandSeq(brandSeq);
		}

		String attribute =  jo.getString("attribute");
		String cpSeq = jo.getString("cpSeq");
		if(StringUtils.isNotBlank(attribute) && StringUtils.isNotBlank(cpSeq)){
		    JSONObject param = new JSONObject();
            param.put("cpSeq",cpSeq);
            param.put("seqList",JSONObject.parseArray(attribute));
            c.setClod(GoodsUtils.getIsColdAttribute(param));
        }

    }



}
