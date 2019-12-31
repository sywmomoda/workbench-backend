package com.feiniu.yx.page.service.module;

import java.util.ArrayList;
import java.util.Date;
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
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.RemoteCommodityService;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.service.YxPoolService;
import com.feiniu.yx.util.DateUtil;

@Service
public class IStampPrintingListService implements CustomModule {
	
	@Autowired
	private RemoteCommodityService remoteCommodityService;
	
	@Autowired
	private ModuleCommodityServiceImpl moduleCommodityServiceImpl;//远程商品接口
	
	@Autowired
    private YxPoolService  poolService;
	
	private static String codeType = "stampPrintingList";
	
	@Override
	public void findCustomData(JSONObject mjo) {
		String storeCode = mjo.getString("storeCode");
		if(StringUtils.isBlank(storeCode)){
			return;
		}
		
		JSONArray commodityArray = mjo.getJSONArray("commodityList");
		List<YxPoolCommodity> commodityList = new ArrayList<YxPoolCommodity>();//池中数据列表
		if(null != commodityArray){
			for (Object object : commodityArray) {
	            JSONObject jsonObject = (JSONObject) object;
	            YxPoolCommodity t = JSONObject.toJavaObject(jsonObject, YxPoolCommodity.class);
	            commodityList.add(t);
	        }
		}
		
		JSONObject proJo = mjo.getJSONObject("moduleProperties");
		int poolShowNum = proJo.getIntValue("poolShowNum");
		Integer remoteCount = proJo.getInteger("remoteCount");
		if(null == remoteCount){
			remoteCount = 100;
		}
		Long poolId = proJo.getLong("poolId");
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
			Date endDate = yppList.get(index+1).getBeginTime();
			Date nowDate = new Date();
			if(endDate.after(nowDate)){
				long diffDay = (endDate.getTime()-nowDate.getTime())/(24*3600*1000);
				if(diffDay>=3){
					mjo.put("endDateInfo", "活动截止至"+DateUtil.getDate(yppList.get(index+1).getBeginTime(), "MM月dd日"));
				}else{
					mjo.put("endDateInfo", "活动仅剩最后"+(diffDay+1)+"天");
				}
			}else{
				mjo.put("endDateInfo", "");
			}
			mjo.put("endDate", DateUtil.getDate(yppList.get(index+1).getBeginTime(), "yyyy-MM-dd HH:mm:ss"));
		}else{//无后一期
			mjo.put("endDate", "");
			mjo.put("endDateInfo", "");
		}
		
		List<YxRemoteCommodity> rcList = remoteCommodityService.queryRemoteCommodityFromInterface(storeCode,"",codeType,remoteCount);//本地远程数据列表
		List<YxPoolCommodity> resultCommodityList =  orderRepeatProcess(commodityList,rcList,storeCode,"",poolShowNum);
		
		mjo.put("commodityList", resultCommodityList);
		
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
		Map<String,String> oldMap = new HashMap<String,String>();
		List<YxPoolCommodity> resultList = new ArrayList<YxPoolCommodity>();
		List<YxPoolCommodity> soldOutList = new ArrayList<YxPoolCommodity>();//售完列表
		int i= 0;
		for(YxPoolCommodity pc: commodityList){
			if(i >= poolShowNum) break;
			oldMap.put(pc.getCommodityId(), "1");
			if(pc.getStockSum()<1L){
				soldOutList.add(pc);
			}else{
				resultList.add(pc);
				i++;
			}
			
		}
		
		List<YxPoolCommodity> remoteList = convertRemote(rcList,storeCode);
		for(YxPoolCommodity pc: remoteList){
			if(oldMap.get(pc.getCommodityId())==null){
				if(pc.getStockSum()<1L){
					soldOutList.add(pc);
				}else{
					resultList.add(pc);
				}
			}
		}
		resultList.addAll(soldOutList);
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
		
		for(YxRemoteCommodity rc: rcList){
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
				rcNew.setDPTJ(pricePC.isDPTJ());
				rcNew.setDptjCard(pricePC.getDptjCard());
				rcNew.setCommodityCouponMap(pricePC.getCommodityCouponMap());
				remoteList.add(rcNew);
			}
		}
		return remoteList;
	}
	
}
