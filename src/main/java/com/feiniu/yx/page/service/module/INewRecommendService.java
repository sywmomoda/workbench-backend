package com.feiniu.yx.page.service.module;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.YxRemoteCommodity;
import com.feiniu.yx.page.service.CommodityRepeatCtlService;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.RemoteCommodityService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.YxRemoteCommodityService;
import com.feiniu.yx.util.DateUtil;
@Service
public class INewRecommendService implements CustomModule {
	
	private static final Logger logger =Logger.getLogger(INewRecommendService.class);
	
	@Autowired
	private RemoteCommodityService remoteCommodityService;
	@Autowired
	private CommodityRepeatCtlService commodityRepeatCtlService;
	@Autowired
	private YxRemoteCommodityService yxRemoteCommodityService;//远程商品接口
	
	private static String codeType = "newRecommend";
	
	@Override
	public void findCustomData(JSONObject mjo) {
		Long moduleId = mjo.getLong("id");
		String storeCode = mjo.getString("storeCode");
		if(StringUtils.isBlank(storeCode)){
			return;
		}
		JSONArray commodityArray = mjo.getJSONArray("commodityList");
		List<YxPoolCommodity> commodityList = new ArrayList<YxPoolCommodity>();
		List<YxPoolCommodity> soldOutList = new ArrayList<YxPoolCommodity>();
		if(null != commodityArray){
			for (Object object : commodityArray) {
	            JSONObject jsonObject = (JSONObject) object;
	            YxPoolCommodity t = JSONObject.toJavaObject(jsonObject, YxPoolCommodity.class);
	            if(t.getAddOnDate()!=null){
					t.setRemark(DateUtil.getDate(t.getAddOnDate(), "MM月dd日"));//上新日期格式转换
				}
	            if(t.getStockSum()>0L){
	            	commodityList.add(t);
	            }else{
	            	soldOutList.add(t);
	            }
	            
	        }
		}
		
		List<YxPoolCommodity> resultCommodityList = new ArrayList<YxPoolCommodity>();
		if(commodityList.size()>3){
			int days = commodityList.size()/3;
			String recentCommoditys = commodityRepeatCtlService.getRecentDayCommoditys(moduleId,storeCode,codeType,days);
			resultCommodityList =  orderRepeatProcess(commodityList,recentCommoditys);
		}else{//不够3个，从售完列表补足
			int i = commodityList.size();
			for(YxPoolCommodity pc: soldOutList){
				if(i>=3) break;
				commodityList.add(pc);
				i++;
			}
			resultCommodityList = commodityList;
		}
		mjo.put("commodityList", resultCommodityList);
		if(resultCommodityList.size()>0){
			String commoditys = "";
			int count = 0;
			for(YxPoolCommodity pc:resultCommodityList){
				if(count>=3){
					break;
				}
				commoditys += pc.getCommodityId()+",";
				count++;
			}
			commodityRepeatCtlService.saveTodayCommoditys(commoditys,moduleId,storeCode,codeType);
		}
		
	}
	

	/**
	 * 去重每天轮换处理
	 * @param commodityList
	 * @param rcList
	 * @param storeCode
	 * @param yesterdayCommoditys
	 * @return
	 */
	private List<YxPoolCommodity> orderRepeatProcess(
			List<YxPoolCommodity> commodityList, String recentCommoditys) {
		Map<String,Integer> oldMap = new HashMap<String,Integer>();
		Integer index = 0;
		for(String uc:recentCommoditys.split(",")){
			if(StringUtils.isNotBlank(uc)){
				oldMap.put(uc, index++);
			}
		}
		
		List<YxPoolCommodity> resultList = new ArrayList<YxPoolCommodity>();
		int i= 0;
		String addCommoditys = "";
		for(YxPoolCommodity pc: commodityList){
			if(i>=3) break;
			if(null==oldMap.get(pc.getCommodityId())){
				resultList.add(pc);
				addCommoditys += pc.getCommodityId()+",";
				i++;
			}
		}
		boolean beginIndex = false;
		if(resultList.size()<3 && resultList.size()>0){//按index继续
			for(YxPoolCommodity pc: commodityList){
				if(resultList.get(resultList.size()-1).getCommodityId().equals(pc.getCommodityId())){
					beginIndex = true;//取得beginIndex
					continue;
				}
				
				if(beginIndex){//按beginIndex往后添加
					if(resultList.size()>=3) break;
					resultList.add(pc);
					addCommoditys += pc.getCommodityId()+",";
				}
				
			}
		}
		
		if(resultList.size()<3){//不足再按排前补全
			for(YxPoolCommodity pc: commodityList){
				if(resultList.size()>=3) break;
				
				if(addCommoditys.indexOf(pc.getCommodityId())==-1){
					resultList.add(pc);
				}
			}
		}
		
		return resultList;
	}


	
}
