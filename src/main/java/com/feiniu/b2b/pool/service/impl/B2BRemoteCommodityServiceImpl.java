package com.feiniu.b2b.pool.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.pool.entity.B2BPoolCommodity;
import com.feiniu.b2b.pool.service.B2BRemoteCommodityService;
import com.feiniu.b2b.remote.CommodityRemote;
import com.feiniu.yx.util.DateUtil;
import com.feiniu.yx.util.ImageUtils;
import com.feiniu.yx.util.YxPoolConst;

/**
 * 通过SOA接口查询优鲜商品
 * @author tongwenhuan
 *
 */
@Service
public class B2BRemoteCommodityServiceImpl implements B2BRemoteCommodityService {

//	private static Logger logger = Logger.getLogger(B2BRemoteCommodityServiceImpl.class);
	
	@Override
	public List<B2BPoolCommodity> getRemoteCommodityOfStoreInfo(String[] ids,String storeCodes) {
		List<B2BPoolCommodity> poolCommoditys = new ArrayList<B2BPoolCommodity>();
		if(ids==null ||ids.length<=0)return null;
		if(StringUtils.isBlank(storeCodes)){
			getRemoteCommodityOfStoreInfo(ids);
		}
		HashSet<String> idSet = getStringByArray(ids,15);
		HashSet<String> codeSet = getStringByArray(storeCodes.split(","), 30);
		for(String single : idSet){
			poolCommoditys.addAll(batchRemoteCommodityOfStoreInfo(single,codeSet));
		}
		return poolCommoditys;
	}

	/**
	 * 
	 * @param ids
	 * @param codeSet
	 * @return
	 */
	private List<B2BPoolCommodity> batchRemoteCommodityOfStoreInfo(String ids,HashSet<String> codeSet) {
		List<B2BPoolCommodity> poolCommoditys = new ArrayList<B2BPoolCommodity>();
		JSONObject allObj = new JSONObject();
		for (String codes : codeSet) {
			JSONObject data = CommodityRemote.getInfoByIds2StoreCodes(ids, codes);
			if (null == data) {
				continue;
			}
			if (data.size() == 0) {
				continue;
			}
			if (allObj.size() == 0) {
				allObj = data;
				continue;
			}
			allObj = getPlusStoreCode(allObj, data);
		}
		for (Entry<String, Object> entry : allObj.entrySet()) {
			B2BPoolCommodity commodity = new B2BPoolCommodity();
			Object value = entry.getValue();

			if (null == value) {
				continue;
			}
			String single = value.toString();
			JSONObject comObject = JSONObject.parseObject(single);
			commodity = this.convertJsonToCommodity(comObject);
			poolCommoditys.add(commodity);
		}
		return poolCommoditys;
	}
	
	/**
	 * 把门店串到一起
	 * @param allObj
	 * @param data
	 * @return
	 */
	private JSONObject getPlusStoreCode(JSONObject allObj,JSONObject data){
		for(Entry<String, Object> entry : data.entrySet()){
			String key = entry.getKey();
			if(null == key){
				continue;
			}
			Object value = entry.getValue();
			if(null == value){
				continue;
			}
		    JSONObject valObj = JSONObject.parseObject(value.toString());
		    JSONArray codeArray = valObj.getJSONArray("rt_no");
		    if(null == codeArray){
		    	continue;
		    }
		    if(codeArray.size() == 0){
		    	continue;
		    }
		    if(allObj.containsKey(key)){
		    	JSONObject existObj = allObj.getJSONObject(key);
		    	if(null == existObj){
		    		continue;
		    	}
		    	JSONArray existArray = existObj.getJSONArray("rt_no");
		    	if(null == existArray){
		    	  continue;	
		    	}
		    	if(existArray.size() == 0){
		    	  continue;	
		    	}
		    	codeArray.addAll(existArray);
		    	existObj.put("rt_no",codeArray);
		    	continue;
		    }
		    allObj.put(key, value);
		}		
		return allObj;
	}
	
	private HashSet<String> getStringByArray(String[] array, int number) {
		HashSet<String> result = new HashSet<String>();
		if (null == array) {
			return result;
		}
		if (array.length == 0) {
			return result;
		}
		if (number == 0) {
			return result;
		}
		int length = array.length;
		int multiple = length / number;
		int remainder = length % number;

		for (int j = 1; j <= multiple; j++) {
			String[] ii = Arrays.copyOfRange(array, number * (j - 1), (j)* number);
			result.add(StringUtils.join(ii, ","));
		}
		if (remainder > 0) {
			String[] ii = Arrays.copyOfRange(array, number * multiple, number* multiple + remainder);
			result.add(StringUtils.join(ii, ","));
		}
		return result;
	}
	
	/**
	 * 通过商品id查询所能够卖的门店,每次只能查询10个
	 * @param ids
	 * @return
	 */
	public List<B2BPoolCommodity> getRemoteCommodityOfStoreInfo(String[] ids){
        int singleNum = 3;  //每次查询的个数
		if(ids==null ||ids.length<=0)return null;
		List<B2BPoolCommodity> poolCommoditys = new ArrayList<B2BPoolCommodity>();
		int length = ids.length;
        int multiple = length / singleNum ;
        int remainder = length % singleNum;
        for(int j = 1; j <= multiple; j++){
       	 String[]  ii = Arrays.copyOfRange(ids, singleNum*(j-1), (j)*singleNum);
       	 List<B2BPoolCommodity> batchCommoditys = batchRemoteCommodityOfStoreInfo(ii);
       	    if(null == batchCommoditys){
       		   continue;
       	    }
       	    poolCommoditys.addAll(batchCommoditys);
        }
        
        if(remainder > 0){
       	  String[]  ii = Arrays.copyOfRange(ids, singleNum*multiple, singleNum*multiple+remainder);
       	  List<B2BPoolCommodity> batchCommoditys = batchRemoteCommodityOfStoreInfo(ii);
   	      if(null != batchCommoditys){
   	    	poolCommoditys.addAll(batchCommoditys);
   	      }
        }
        
		return poolCommoditys;
	}
	
	private List<B2BPoolCommodity> batchRemoteCommodityOfStoreInfo(String[] ids){
		JSONObject remoteData = CommodityRemote.getInfoByIds(StringUtils.join(ids,","));
		if (remoteData == null) {
			return null;
		}
		List<B2BPoolCommodity> poolCommoditys = new ArrayList<B2BPoolCommodity>();
		for(Entry<String, Object> entry : remoteData.entrySet()){
			B2BPoolCommodity commodity = new B2BPoolCommodity();
			Object value = entry.getValue();
			
			if(null == value){
				continue;
			}
			String single = value.toString();
			JSONObject comObject = JSONObject.parseObject(single);
			
			commodity = this.convertJsonToCommodity(comObject);
			poolCommoditys.add(commodity);
		}
		return poolCommoditys;
	}
	
	
	@Override
	/**
     * 根据门店和商品ID获取商品价格库存信息
     * @author lizhiyong
     * 2017年3月8日
     * @param object
     * @return
     */
	public Map<String, B2BPoolCommodity> getRemoteCommodityListByStoreAndIds(String storeCode, String[] ids) {
		if(ids==null || ids.length<=0 ||StringUtils.isBlank(storeCode)){
			return null;
		}
		//分批次查询
		HashSet<String> idSet = getStringByArray(ids, 20);
		Map<String,B2BPoolCommodity> poolCommoditys = new HashMap<String,B2BPoolCommodity>();
		for(String single : idSet){
			Map<String,B2BPoolCommodity> m = getCommodityListByStoreAnd20Ids(storeCode,single);
			if (m != null) {
				poolCommoditys.putAll(m);
			}
		}
        return poolCommoditys;		
	}
	
	//查询20个商品
	private Map<String, B2BPoolCommodity> getCommodityListByStoreAnd20Ids(String storeCode, String ids) {
		if (StringUtils.isBlank(ids)) {
			return null;
		}
		Map<String,B2BPoolCommodity> poolCommoditys = new HashMap<String,B2BPoolCommodity>();
        
        JSONObject goodsInfo = CommodityRemote.getPriceByStoreAndIds(ids, storeCode);
        if(goodsInfo == null){
        	return null;
        }
        for(Entry<String, Object> entry : goodsInfo.entrySet()){
        	Object value = entry.getValue();
        	if(null == value){
        		continue;
        	}
        	String single = value.toString();
        	B2BPoolCommodity commodity = convertPriceJsonToCommodity(JSONObject.parseObject(single), false);
        	if(null  == commodity){
        		continue;
        	}
        	poolCommoditys.put(commodity.getCommodityId(),commodity);
        }
		return poolCommoditys;
	}
	
	/**
     * 将接口json串转换成YxPoolCommodity对象
     * @author lizhiyong
     * 2017年3月8日
     * @param object
     * @return
     */
    public B2BPoolCommodity convertJsonToCommodity(JSONObject object) {
    	B2BPoolCommodity pc = new B2BPoolCommodity();
    	if (object.get("item_no") != null){
            pc.setCommodityId(object.getString("item_no"));
        }
    	
        if (object.get("pic") != null) {
        	String picUrl = object.getString("pic");
			picUrl = picUrl.indexOf(",") > -1 ? picUrl.split(",")[0] : picUrl;
            pc.setPicUrl(ImageUtils.getImageUrl(picUrl));
        }
        
        if (object.get("goods_name") != null)
            pc.setTitle(object.getString("goods_name"));
        if (object.get("rt_no") != null){
        	JSONArray storeArray= object.getJSONArray("rt_no");
        	String array = StringUtils.join(storeArray,",");
            pc.setStoreCode(array);
        }
        pc.setExist(true);
        pc.setOriginate(YxPoolConst.YX_COMMODITY_TYPE_COMMODITY);
        pc.setUrlType("1");
        return pc;
    }
    
    
    /***
     * 将接口json串转换成B2BPoolCommodity对象
     * @param object
     * @param isShowEx
     * @return
     */
    public B2BPoolCommodity convertPriceJsonToCommodity(JSONObject object,boolean isShowEx){
    	
    	Integer status = object.getInteger("status");// 状态 1:开启0:关闭
    	if(isShowEx || status == 1){
    		B2BPoolCommodity pc = new B2BPoolCommodity();
	    	if (object.get("item_no") != null){
	            pc.setCommodityId(object.getString("item_no"));
	        }
	    	
	    	if(object.get("goods_name") != null){
	    		pc.setTitle(object.getString("goods_name"));
	    	}
	    	
	    	if (object.get("stock") != null){  //可卖量，实时库存
	            pc.setStockSum(object.getLong("stock"));
	    	}
	    	
	    	
	    	if(object.get("pic") !=null){
	    		String picUrl = object.getString("pic");
				picUrl = picUrl.indexOf(",") > -1 ? picUrl.split(",")[0] : picUrl;
	            pc.setPicUrl(ImageUtils.getImageUrl(picUrl));
	    	}
	    	//最后售价， 原价，促销价，专享价三者中最低的
	    	Float price = object.getFloat("real_price");
	    	pc.setPrice(price);
	    	
	    	if (object.get("rt_no") != null){
	            pc.setStoreCode(object.getString("rt_no"));
	        }
	    	
	    	if(object.get("bargain_is") != null){ //是否打折
	    		pc.setBargaiIs(object.getInteger("bargain_is"));	
	    	}
	    	
	    	if(object.get("sale_type") != null){ //销售类型
	    		pc.setSaleType(object.getInteger("sale_type"));	
	    	}
	    	
	    	
	    	if(object.get("moq") != null){//最小倍数
	    		pc.setMinQuantity(object.getInteger("moq"));	
	    	}
	    	
	    	if(object.get("spec") != null){ // 商品规格
	    		pc.setUnit(object.getString("spec")); 
	    	}
	    	
	    	if(object.get("prom_grade") != null){ // 促销等级
	    		Integer promGrade = object.getInteger("prom_grade");
	    		if(promGrade > 0){
	    			Long prmoSTime = object.getLong("prom_stime"); //促销开始时间
	    			Long prmoETime = object.getLong("prom_etime"); //促销结束时间
	    			 //是否进行中的促销
	    			int isAction = DateUtil.isNowTime(new Date(prmoSTime), new Date(prmoETime));
	    			promGrade = isAction == 2 ? promGrade : 0;
	    			
	    		}
//	    		if(promGrade > 0){  //促销商品取促销价格
//	    			Float promPrice =  object.getFloat("prom_price");
//	    			//促销价比原价便宜，显示促销价
//	    			if (promPrice < price) {
//	    				pc.setPrice(promPrice);
//	    			}
//	    		}
	    		pc.setPromGrade(promGrade); 
	    		
	    	}
	    	
	    	pc.setMinQuantity(1);
			
	    	if(object.get("moq") != null){ //最小起订量
	    		if(pc.getSaleType()!=null && pc.getSaleType() == 2){//成倍
	    			pc.setMinQuantity(object.getInteger("moq"));
	    		}
	    		if(object.getInteger("moq") > pc.getStockSum()){
	    			pc.setStockSum(0L);
	    		}
	    	}
	    	
	    	if(object.get("spec") != null){ // 商品规格
	    		pc.setUnit(object.getString("spec")); 
	    	}
	    	
	    	//箱规
	    	String box_spec = object.getString("box_spec");
	    	if (StringUtils.isBlank(box_spec)) {
	    		//包装单位
	    		String salePack = object.getString("sale_pack");
	    		//销售单位
	    		int om = object.getIntValue("om");
	    		box_spec = "";
	    		if (om > 1) {
	    			box_spec = om + salePack + "/箱";
	    		}
	    	}
			pc.setBoxSpec(box_spec);
			
			pc.setLimitNum(object.getInteger("limit_num"));//限购量

	    	
	        pc.setExist(true);
	        pc.setOriginate(YxPoolConst.YX_COMMODITY_TYPE_COMMODITY);
	        pc.setUrlType("1");
	        return pc;
    	}else{
    		return null;
    	}
    }
    
	@Override
	public B2BPoolCommodity getRemoteCommodityPriceInfoByStoreAndId(
			String storeCode, String id) {
		String [] ids = new String[1];
		ids[0] = id;
		 Map<String,B2BPoolCommodity>  map =  getRemoteCommodityListByStoreAndIds(storeCode, ids);
		 if(null  ==  map ){
			 return null;
		 }
		return map.get(id);
	}
	
}
