package com.feiniu.yx.page.service.module;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.util.DateUtil;

@Service
public class INewRecommendListService implements CustomModule {
	
//	private static final Logger logger =Logger.getLogger(INewRecommendListService.class);
//	
//	@Autowired
//	private RemoteCommodityService remoteCommodityService;
//	@Autowired
//	private CommodityRepeatCtlService commodityRepeatCtlService;
//	@Autowired
//	private YxRemoteCommodityService yxRemoteCommodityService;//远程商品接口
//	
//	private static String codeType = "newRecommend";
	
	@Override
	public void findCustomData(JSONObject mjo) {
//		Long moduleId = mjo.getLong("id");
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
	            if(t.getAddOnDate()!=null){
					t.setRemark(DateUtil.getDate(t.getAddOnDate(), "MM月dd日"));//上新日期格式转换
				}
	            commodityList.add(t);
	        }
		}
		
		/*List<YxPoolCommodity> resultCommodityList = new ArrayList<YxPoolCommodity>();
		String recentCommoditys = "";
		if(commodityList.size()>3){
			int days = commodityList.size()/3;
			recentCommoditys  = commodityRepeatCtlService.getRecentDayCommoditys(moduleId,storeCode,codeType,days);
		}
		resultCommodityList =  orderRepeatProcess(commodityList,recentCommoditys);*/
		mjo.put("commodityList", commodityList);
		
	}

//	/**
//	 * 去重每天轮换处理
//	 * @param commodityList
//	 * @param rcList
//	 * @param storeCode
//	 * @param yesterdayCommoditys
//	 * @return
//	 */
//	private List<YxPoolCommodity> orderRepeatProcess(List<YxPoolCommodity> commodityList, String recentCommoditys) {
//		Map<String,Integer> oldMap = new HashMap<String,Integer>();
//		Integer index = 0;
//		for(String uc:recentCommoditys.split(",")){
//			if(StringUtils.isNotBlank(uc)){
//				oldMap.put(uc, index++);
//			}
//		}
//		
//		List<YxPoolCommodity> resultList = new ArrayList<YxPoolCommodity>();
//		List<YxPoolCommodity> secondList = new ArrayList<YxPoolCommodity>();
//		for(YxPoolCommodity pc: commodityList){
//			if(null==oldMap.get(pc.getCommodityId())){
//				resultList.add(pc);
//			}else{
//				secondList.add(pc);
//			}
//		}
//		resultList.addAll(secondList);
//		return resultList;
//	}

	
}
