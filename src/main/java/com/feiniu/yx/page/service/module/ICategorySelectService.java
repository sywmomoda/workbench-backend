package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.core.impl.BaseCommodityServiceImpl;
import com.feiniu.yx.page.entity.YxRemoteCommodity;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.RemoteCommodityService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ICategorySelectService implements CustomModule {
	
	@Autowired
	private RemoteCommodityService remoteCommodityService;
	
	@Autowired
	private BaseCommodityServiceImpl commodityService;//远程商品接口
	
	private static String codeType = "category";
	
	@Override
	public void findCustomData(JSONObject mjo) {
		String storeCode = mjo.getString("storeCode");
		if(StringUtils.isBlank(storeCode)){
			return;
		}
		String previewTime = mjo.getString("backendPreviewTime");
		JSONObject proJo  = mjo.getJSONObject("moduleProperties");
		String category = proJo.getString("searchCategory");
		Integer remoteCount = proJo.getInteger("remoteCount");
		if(null == remoteCount){
			remoteCount = 12;
		}
		handleSchedule(proJo,previewTime);
		JSONArray commodityArray = mjo.getJSONArray("commodityList");
		List<YxPoolCommodity> commodityList = new ArrayList<YxPoolCommodity>();
		List<YxPoolCommodity> soldOutList = new ArrayList<YxPoolCommodity>();
		String poolCommoditys = "", soldOutCommoditys = "";
		if(null != commodityArray){
			for (Object object : commodityArray) {
	            JSONObject jsonObject = (JSONObject) object;
	            YxPoolCommodity t = JSONObject.toJavaObject(jsonObject, YxPoolCommodity.class);
	            if(t.getOriginate()==1 && t.getStockSum()>0L){
	            	poolCommoditys += t.getCommodityId()+",";
	                commodityList.add(t);
	            }else if(t.getOriginate()==1 && t.getStockSum()<1L){
	            	soldOutCommoditys += t.getCommodityId()+",";
	            	soldOutList.add(t);
	            }
	        }
		}
		
		if(poolCommoditys.length()>0){
			poolCommoditys = poolCommoditys.substring(0,poolCommoditys.length()-1);
		}
		if(soldOutCommoditys.length()>0){
			soldOutCommoditys = "," + soldOutCommoditys.substring(0,soldOutCommoditys.length()-1);
		}
		mjo.put("moreCommoditys", poolCommoditys + soldOutCommoditys);
		if(commodityList.size()<6){
			List<YxRemoteCommodity> rcList = remoteCommodityService.queryRemoteCommodityFromInterface(storeCode, category, codeType, remoteCount);
			List<YxPoolCommodity> resultCommodityList =  orderRepeatProcess(commodityList,rcList,storeCode,soldOutList);
			mjo.put("commodityList", resultCommodityList);
		}
	}

	private void handleSchedule(JSONObject proJo,String previewTime){
		Object beginTimeObj = proJo.get("beginTime");
		Object endTimeObj = proJo.get("endTime");
		Object imgUrlNewObj = proJo.get("imgUrlNew");
		Object imgUrlTaoObj = proJo.get("imgUrlTao");
		JSONArray bgBeginTimeArray = new JSONArray();
		JSONArray bgEndTimeArray = new JSONArray();
		JSONArray urlNewArray = new JSONArray();
		JSONArray urlTaoArray = new JSONArray();
		if(beginTimeObj instanceof JSONArray){
			JSONArray beginTime = (JSONArray)beginTimeObj;
			for(int i = 0,len = beginTime.size(); i < len; i++){
				String time = beginTime.getString(i);
				bgBeginTimeArray.add(DateUtil.getDate(time, "yyyy-MM-dd HH:mm:ss"));
			}
		}else {
			String beginTime = proJo.getString("beginTime");
			bgBeginTimeArray.add(DateUtil.getDate(beginTime, "yyyy-MM-dd HH:mm:ss"));
		}
		if(endTimeObj instanceof JSONArray){
			JSONArray endTime = (JSONArray)endTimeObj;
			for(int i = 0,len = endTime.size(); i < len; i++){
				String time = endTime.getString(i);
				bgEndTimeArray.add(DateUtil.getDate(time, "yyyy-MM-dd HH:mm:ss"));
			}
		}else {
			String endTime = proJo.getString("endTime");
			bgEndTimeArray.add(DateUtil.getDate(endTime, "yyyy-MM-dd HH:mm:ss"));
		}
		if(imgUrlNewObj instanceof JSONArray){
			JSONArray urlNew = (JSONArray)imgUrlNewObj;
			for(int i = 0,len = urlNew.size(); i < len; i++){
				String urlNewString = urlNew.getString(i);
				urlNewArray.add(urlNewString);
			}
		}else {
			String urlNewString = proJo.getString("imgUrlNew");
			urlNewArray.add(urlNewString);
		}
		if(imgUrlTaoObj instanceof JSONArray){
			JSONArray urlTao = (JSONArray)imgUrlTaoObj;
			for(int i = 0,len = urlTao.size(); i < len; i++){
				String urlTaoString = urlTao.getString(i);
				urlTaoArray.add(urlTaoString);
			}
		}else {
			String urlTaoString = proJo.getString("imgUrlTao");
			urlTaoArray.add(urlTaoString);
		}
		for(int j = 0,len = bgBeginTimeArray.size(); j < len; j++){
			Date oneBeginTime = bgBeginTimeArray.getDate(j);
			Date oneEndTime = bgEndTimeArray.getDate(j);
			Date now = new Date();
			if(StringUtils.isNotBlank(previewTime)){
				now = DateUtil.getDate(previewTime,"yyyy-MM-dd HH:mm:ss");
			}
			if(now.getTime()>oneBeginTime.getTime()&&now.getTime()<oneEndTime.getTime()){
				proJo.put("topImgUrlNew",urlNewArray.getString(j));
				proJo.put("topImgUrlTao",urlTaoArray.getString(j));
			}
		}
	}
	
	/**
	 * 去重排序处理
	 * @param commodityList
	 * @param rcList
	 * @param storeCode
	 * @param soldOutList 
	 * @param yesterdayCommoditys 
	 * @return
	 */
	private List<YxPoolCommodity> orderRepeatProcess(
			List<YxPoolCommodity> commodityList, List<YxRemoteCommodity> rcList,String storeCode, List<YxPoolCommodity> soldOutList) {
		Map<String,String> oldMap = new HashMap<String,String>();
		
		List<YxPoolCommodity> resultList = new ArrayList<YxPoolCommodity>();
		int i= 0;
		for(YxPoolCommodity pc: commodityList){//此列表已过滤无货商品
			oldMap.put(pc.getCommodityId(), "1");
			resultList.add(pc);
			i++;
		}
		
		List<YxPoolCommodity> remoteList = convertRemote(rcList,storeCode);
		for(YxPoolCommodity pc: remoteList){
			if(i>=6) break;
			if(pc.getStockSum()<1L){
				soldOutList.add(pc);
			}else{
				if(oldMap.get(pc.getCommodityId())==null){
					resultList.add(pc);
					i++;
				}
			}
		}
		
		if(i<6){//不够6个商品，从售完列表补充
			for(YxPoolCommodity pc: soldOutList){
				if(i>=6) break;
				resultList.add(pc);
				i++;
			}
		}
		
		return resultList;
	}

	/**
	 * 远程数据排序并转换成池类型商品
	 * @param rcList
	 * @param storeCode
	 * @return
	 */
	private List<YxPoolCommodity> convertRemote(List<YxRemoteCommodity> rcList, String storeCode) {
		List<YxPoolCommodity> remoteList = new ArrayList<YxPoolCommodity>();
		String goodNos = "";
		Map<String,List<String>> categoryMap = new HashMap<String,List<String>>();
		for(YxRemoteCommodity rc: rcList){//初始化数据
			try{
				Object obj = JSONObject.parse(rc.getCategory());
				@SuppressWarnings("unchecked")
				List<String> category =  (List<String>) obj;
				categoryMap.put(rc.getCommodityId(), category);
				goodNos += rc.getCommodityId()+",";
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		Map<String, YxPoolCommodity> commodityMap = commodityService.mapCommodityPriceInfo(goodNos.split(","), storeCode);
		
		Map<String,String> hasCategoryMap = new HashMap<String,String>();
		hasCategoryMap.put("hasCate1", "");
		hasCategoryMap.put("hasCate2", "");
		hasCategoryMap.put("hasCate3", "");
		List<YxRemoteCommodity> priceTOrderList = orderByCategoryList(rcList,categoryMap,2,hasCategoryMap);//2级排序
		priceTOrderList.addAll(orderByCategoryList(rcList,categoryMap,3,hasCategoryMap));//3级排序
		priceTOrderList.addAll(rcList);
		
		for(YxRemoteCommodity rc: priceTOrderList){
			if(commodityMap!=null && commodityMap.get(rc.getCommodityId())!=null){
				YxPoolCommodity rcNew = remoteCommodityService.convertToPoolCommodity(rc);
				rcNew.setOriginate(1);
				YxPoolCommodity pricePC = commodityMap.get(rc.getCommodityId());
				rcNew.setUnit(pricePC.getUnit().replace("/", ""));
				rcNew.setHasService(pricePC.getHasService());
				rcNew.setPrice(pricePC.getPrice());
				rcNew.setMarketPrice(pricePC.getMarketPrice());
				rcNew.setStockSum(pricePC.getStockSum());
				remoteList.add(rcNew);
			}
		}
		return remoteList;
	}

	/**
	 * 接级别排序处理
	 * @param rcList
	 * @param categoryMap 
	 * @param level 
	 * @return
	 */
	private List<YxRemoteCommodity> orderByCategoryList(List<YxRemoteCommodity> rcList,Map<String,List<String>> categoryMap, int level,Map<String,String> hasCateMap) {
		//String levelCategory = "";
		List<YxRemoteCommodity> resultList = new ArrayList<YxRemoteCommodity>();
		for(YxRemoteCommodity yrc: rcList){
			List<String> category = categoryMap.get(yrc.getCommodityId());
			if(category!=null && category.size()>=level &&  hasCateMap.get("hasCate"+level).indexOf(category.get(level-1))==-1){
				for(int i=level; i<=category.size(); i++){//添加同级和下级
					hasCateMap.put("hasCate"+i,hasCateMap.get("hasCate"+i)+category.get(i-1)+",");
				}
				resultList.add(yrc);
			}
		}
		rcList.removeAll(resultList);
		return resultList;
	}
	
}
