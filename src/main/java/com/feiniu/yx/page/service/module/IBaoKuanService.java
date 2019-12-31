package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.core.CommoditySaleInfoService;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
@Service
public class IBaoKuanService implements CustomModule {
	
//	private static final Logger logger =Logger.getLogger(IBaoKuanService.class);
	
	@Autowired
	private CommoditySaleInfoService commoditySaleInfoService;//远程商品接口
	
	
	@Override
	public void findCustomData(JSONObject mjo) {
		String storeCode = mjo.getString("storeCode");
		if(StringUtils.isBlank(storeCode)){
			return;
		}
		JSONObject proJo  = mjo.getJSONObject("moduleProperties");

		//根据星期几处理显示头部图片
		Calendar d = Calendar.getInstance();
		int day = d.get(Calendar.DAY_OF_WEEK);//sunday 1  monday2
		if(day==1){
			day = 8;
		}
		String headPicUrl = proJo.getString("headPicUrl"+(day-1));
		String bgColor = proJo.getString("bgColor"+(day-1));
		if(StringUtils.isBlank(headPicUrl)){//兼容原数据处理
			headPicUrl = proJo.getString("headPicUrl");
		}
		if(StringUtils.isBlank(bgColor)){//兼容原数据处理
			bgColor = proJo.getString("bgColor");
		}
		proJo.put("headPicUrl",headPicUrl);
		proJo.put("bgColor",bgColor);
		mjo.put("moduleProperties", proJo);

		String beginT = proJo.getString("beginTime");
		String endT = proJo.getString("endTime");
		Date b = DateUtil.getDate(beginT, "yyyy-MM-dd HH:mm:ss");
		Date e = DateUtil.getDate(endT, "yyyy-MM-dd HH:mm:ss");
		JSONArray commodityArray = mjo.getJSONArray("commodityList");
		JSONArray commodityArrayNew = new JSONArray();
		JSONArray commodityArrayNewOut = new JSONArray();
		Date now = new Date();
		if(null != commodityArray){
			for (int i = 0; i < commodityArray.size();i++) {
				JSONObject commodityObject = (JSONObject) commodityArray.get(i);
	            JSONObject jsonObject = new JSONObject();
	            YxPoolCommodity pc = JSONObject.toJavaObject(commodityObject, YxPoolCommodity.class);
	            if(pc.getAddOnDate()!=null){
					jsonObject.put("remark", DateUtil.getDate(pc.getAddOnDate(), "MM月dd日"));
				}
	            jsonObject.put("picUrl", pc.getPicUrl());
	            jsonObject.put("commodityId", pc.getCommodityId());
	            jsonObject.put("title", pc.getTitle());
	            jsonObject.put("promoteText", pc.getPromoteText());
	            jsonObject.put("price", pc.getPrice());
	            jsonObject.put("unit", pc.getUnit());
	            jsonObject.put("marketPrice", pc.getMarketPrice());
	            jsonObject.put("specWeight", pc.getSpecWeight());
	            jsonObject.put("hasService", pc.getHasService());
	            jsonObject.put("stockSum", pc.getStockSum());
	            long saleSum = 0;
				if(b.before(now) && e.after(now)){
					long beginTime = b.getTime();
					long endTime = now.getTime();
					saleSum = commoditySaleInfoService.querySaleInfo(storeCode, pc.getCommodityId(), beginTime, endTime);
				} 
				jsonObject.put("saleSum", 0);
				jsonObject.put("soldRatio", 0);
				
				long xqSum = pc.getStockSum();
				long hasSale = 0L;
				double soldRatio = 0;
				
				hasSale = Math.round(6.0*Math.sqrt(saleSum));//Y=6*√X（X开根号）
	            if(hasSale < 10){//当Y<10时，展示Z（5-10内的一个随机整数）
	            	long seed = Long.parseLong(pc.getCommodityId().replaceAll("[a-zA-Z]",""));
	            	Random random = new Random(seed+100);
	            	hasSale = random.nextInt(5)+5;
	            }
	            if(hasSale < saleSum){//当Y<X时（按以上算法在当实际销量大于36时，Y将小于X），展示X
	            	hasSale = saleSum;
	            }
	            
				if(xqSum > 0){
					/*String describString = pc.getDescription();
		            if(describString.startsWith("{")){
		            	jsonObject.putAll(JSONObject.parseObject(describString));
		            	if(jsonObject.containsKey("xqSum") && StringUtils.isNotBlank(jsonObject.getString("xqSum"))){
			            	xqSum = jsonObject.getIntValue("xqSum");//
			            }
		            }*/
		            
		            soldRatio = 10*(Math.sqrt(100.0*saleSum/(xqSum+saleSum)));//Y=10*√X（X开根号）
		            
		            if(soldRatio < 10){//当Y<10%时，展示Z（5%-10%内的一个随机百分数）
		            	long seed = Long.parseLong(pc.getCommodityId().replaceAll("[a-zA-Z]",""));
		            	Random random = new Random(seed);
		            	soldRatio = random.nextInt(5)+5;
		            }
		            
		            if(soldRatio>100){
		            	soldRatio = 100;
		            }
		            
				}else{//库存为0，直接100%
					soldRatio = 100;
				}
				
				jsonObject.put("saleSum", hasSale);
	            jsonObject.put("soldRatio", Math.round(soldRatio));
	            
	            if(jsonObject.getIntValue("soldRatio")==100){
	            	commodityArrayNewOut.add(jsonObject);
	            }else{
	            	commodityArrayNew.add(jsonObject);
	            }
	            
	        }
		}
		commodityArrayNew.addAll(commodityArrayNewOut);
		mjo.put("commodityList", commodityArrayNew);
		
	}
	
}
