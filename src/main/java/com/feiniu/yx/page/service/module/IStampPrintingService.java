package com.feiniu.yx.page.service.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.core.impl.ModuleCommodityServiceImpl;
import com.feiniu.yx.page.entity.YxRemoteCommodity;
import com.feiniu.yx.page.service.CommodityRepeatCtlService;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.RemoteCommodityService;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.service.YxPoolService;
import com.feiniu.yx.util.DateUtil;

@Service
public class IStampPrintingService implements CustomModule {
	
	@Autowired
	private RemoteCommodityService remoteCommodityService;
	
	@Autowired
	private CommodityRepeatCtlService commodityRepeatCtlService;
	
	@Autowired
	private ModuleCommodityServiceImpl moduleCommodityServiceImpl;//远程商品接口
	
	@Autowired
    private YxPoolService  poolService;
	
	private static String codeType = "stampPrinting";
	
	@Override
	public void findCustomData(JSONObject mjo) {
		Long moduleId = mjo.getLong("id");
		//处理池期数
		JSONObject proJo  = mjo.getJSONObject("moduleProperties");
		Long poolId = proJo.getLong("poolId");
		int poolShowNum = proJo.getIntValue("poolShowNum");
		YxPool pool = poolService.queryPoolAndPeriodById(poolId);
		List<YxPoolPeriods> yppList = pool.getYppList();
		int index = 0;
		for(YxPoolPeriods pp: yppList){
			if(pool.getYxPoolPeriods()!=null && (long)pool.getYxPoolPeriods().getId()==(long)pp.getId()){
				break;
			}
			index++;
		}
		if(yppList.size() > (index+1)){
			mjo.put("endDate", DateUtil.getDate(yppList.get(index+1).getBeginTime(), "yyyy-MM-dd HH:mm:ss"));
		}else{//无后一期
			mjo.put("endDate", "");
		}
		
		
		String storeCode = mjo.getString("storeCode");
		if(StringUtils.isBlank(storeCode)){
			return;
		}
		JSONArray commodityArray = mjo.getJSONArray("commodityList");
		List<YxPoolCommodity> commodityList = new ArrayList<YxPoolCommodity>();
		if(null != commodityArray){
			for (Object object : commodityArray) {
	            JSONObject jsonObject = (JSONObject) object;
	            YxPoolCommodity t = JSONObject.toJavaObject(jsonObject, YxPoolCommodity.class);
	            commodityList.add(t);
	        }
		}
		
		String yesterdayCommoditys = commodityRepeatCtlService.getYesterdayCommoditys(moduleId,storeCode,codeType);
		List<YxRemoteCommodity> rcList = remoteCommodityService.queryRemoteCommodityFromInterface(storeCode, "", codeType, 12);
		List<YxPoolCommodity> resultCommodityList =  orderRepeatProcess(commodityList,rcList,storeCode,yesterdayCommoditys,poolShowNum);
		
		mjo.put("commodityList", resultCommodityList);
		
		if(resultCommodityList.size()>0){
			String commoditys = "";
			int count = 0;
			for(YxPoolCommodity pc:resultCommodityList){
				if(count>=6){
					break;
				}
				commoditys += pc.getCommodityId()+",";
				count++;
			}
			commodityRepeatCtlService.saveTodayCommoditys(commoditys,moduleId,storeCode,codeType);
		}
	}
	
	/**
	 * 去重排序处理
	 * @param commodityList
	 * @param rcList
	 * @param storeCode
	 * @param yesterdayCommoditys 
	 * @param poolShowNum 
	 * @return
	 */
	private List<YxPoolCommodity> orderRepeatProcess(
			List<YxPoolCommodity> commodityList, List<YxRemoteCommodity> rcList,String storeCode, String yesterdayCommoditys, int poolShowNum) {
		Map<String,String> oldMap = new HashMap<String,String>();//记录池中存在的，和
		for(String uc:yesterdayCommoditys.split(",")){
			if(StringUtils.isNotBlank(uc)){
				oldMap.put(uc, "1");
			}
		}
		
		List<YxPoolCommodity> resultList = new ArrayList<YxPoolCommodity>();
		List<YxPoolCommodity> soldOutList = new ArrayList<YxPoolCommodity>();//售完列表
		int i= 0;
		for(YxPoolCommodity pc: commodityList){//1.优化池中有货，人工不考虑前一天是否显示
			if(i>=6 || i>=poolShowNum) break;
			//if(pc.getPriceType()!=null && pc.getPriceType()==1 && pc.getIsSSM()){
				if(pc.getStockSum()<1L){
					soldOutList.add(pc);
				}else{
					oldMap.put(pc.getCommodityId(), "1");
					resultList.add(pc);
					i++;
				}
			//}
		}
		
		if(i<6){//不足6个从接口补充
			List<YxPoolCommodity> remoteList = convertRemote(rcList,storeCode);
			String hasCommoditys = "";
			for(YxPoolCommodity pc: remoteList){
				if(i>=6) break;
				if(pc.getStockSum()>0L){//2.接口有货商品，过滤前一天显示过和池中存在的
					if(oldMap.get(pc.getCommodityId())==null){
						hasCommoditys += pc.getCommodityId()+",";
						resultList.add(pc);
						i++;
					}
				}
			}
			
			if(i<6){//3.不足6个，从远程列表中补充（即前一天显示过的）
				for(YxPoolCommodity pc: remoteList){
					if(i>=6) break;
					if(pc.getStockSum()<1L){
						soldOutList.add(pc);
					}else{
						if(hasCommoditys.indexOf(pc.getCommodityId()+",")<0){//过滤接口数据已添加的
							resultList.add(pc);
							i++;
						}
					}
				}
			}
			
			if(i<6){//4.还不足6个，售完列表中补充
				for(YxPoolCommodity pc: soldOutList){
					if(i>=6) break;
					resultList.add(pc);
					i++;
				}
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
	private List<YxPoolCommodity> convertRemote(List<YxRemoteCommodity> rcList,
			String storeCode) {
		
		List<YxPoolCommodity> remoteList = new ArrayList<YxPoolCommodity>();
		String goodNos = "";
		Map<String,List<String>> categoryMap = new HashMap<String,List<String>>();
		for(YxRemoteCommodity rc: rcList){//初始化数据
			Object obj = JSONObject.parse(rc.getCategory());
			@SuppressWarnings("unchecked")
			List<String> category =  (List<String>) obj;
			categoryMap.put(rc.getCommodityId(), category);
			goodNos += rc.getCommodityId()+",";
		}
		
		Map<String, YxPoolCommodity> commodityMap = moduleCommodityServiceImpl.mapCommodity(goodNos.split(","), storeCode);
		
		Map<String,String> hasCategoryMap = new HashMap<String,String>();
		hasCategoryMap.put("hasCate1", "");
		hasCategoryMap.put("hasCate2", "");
		hasCategoryMap.put("hasCate3", "");
		List<YxRemoteCommodity> resultLevelList = orderByCategoryList(rcList,categoryMap,1,hasCategoryMap);//1级排序
		resultLevelList.addAll(orderByCategoryList(rcList,categoryMap,2,hasCategoryMap));//2级排序
		resultLevelList.addAll(orderByCategoryList(rcList,categoryMap,3,hasCategoryMap));//3级排序
		resultLevelList.addAll(rcList);
		
		for(YxRemoteCommodity rc: resultLevelList){
			if(commodityMap!=null && commodityMap.get(rc.getCommodityId())!=null){
				YxPoolCommodity rcNew = remoteCommodityService.convertToPoolCommodity(rc);
				YxPoolCommodity pricePC = commodityMap.get(rc.getCommodityId());
				rcNew.setPrice(pricePC.getPrice());
				rcNew.setUnit(pricePC.getUnit().replace("/", ""));
				rcNew.setHasService(pricePC.getHasService());
				rcNew.setStockSum(pricePC.getStockSum());
				rcNew.setSpecWeight(pricePC.getSpecWeight());
				rcNew.setMarketPrice(pricePC.getMarketPrice());
				//设置角标信息
				rcNew.setScriptTitle(pricePC.getScriptTitle());
				rcNew.setPropertyTitle(pricePC.getPropertyTitle());
				rcNew.setCommodityCouponMap(pricePC.getCommodityCouponMap());
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
