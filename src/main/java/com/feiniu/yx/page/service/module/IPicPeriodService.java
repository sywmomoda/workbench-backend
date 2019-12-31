package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zhangdunyao
 *
 */
@Service
public class IPicPeriodService implements CustomModule {
	
	@Override
	public void findCustomData(JSONObject mjo) {
		String storeCode = mjo.getString("storeCode");
		if(StringUtils.isBlank(storeCode)){
			return;
		}
		JSONObject proJo  = mjo.getJSONObject("moduleProperties");
		String previewTime = mjo.getString("backendPreviewTime");
		//JSONArray commodityList = mjo.getJSONArray("commodityList");
		List<JSONObject> list = new ArrayList<JSONObject>();
		/*if(commodityList!=null && commodityList.size()>0){
			for(int i=0;i<commodityList.size();i++){
				JSONObject joo = commodityList.getJSONObject(i);
				JSONObject temp = new JSONObject();
				temp.put("imgUrl", joo.getString("picUrl"));
				temp.put("iconId",joo.getString("id"));
				temp.put("title", joo.getString("title"));
				list.add(temp);
			}
		}*/
		List<JSONArray> dataList = new ArrayList<JSONArray>();
		String[] idArray = new String[]{"bannerImg","leftImg","leftBottomImg","rightImg","rightBottomImg"};
		for(int i=0; i<idArray.length; i++){
			String id = idArray[i];
			if(proJo.get(id+"Url")!=null){
				JSONArray imgArray = getImgFromPicPeriod(id,proJo,storeCode,previewTime);
				dataList.add(imgArray);
				
				for(int j=0; j < imgArray.size(); j++){//处理老数据兼容
					JSONObject temp = imgArray.getJSONObject(j);
					if("1".equals(temp.getString("state"))){
						list.add(temp);
					}
				}
			}
		}
		
		mjo.put("commodityList", list);
		
	}
	
	/**
	 * @param objStr
	 * @param moduleProper
	 * @param storeCode
	 * @return
	 */
	public static JSONArray getImgFromPicPeriod(String objStr,
			JSONObject moduleProper,String storeCode, String previewTime) {
		Object bgObj = moduleProper.get(objStr+"BeginTime");
	 	JSONArray bgTimeArray = new JSONArray();
	 	if(bgObj instanceof JSONArray){
	 		JSONArray bg = (JSONArray)bgObj;
	 		for(int i = 0,len = bg.size(); i < len; i++){
	 			String time = bg.getString(i);
	 			bgTimeArray.add(DateUtil.getDate(time, "yyyy-MM-dd HH:mm:ss"));
	 		}
	 	}else{
	 		String time = moduleProper.getString(objStr+"BeginTime");
	 		bgTimeArray.add(DateUtil.getDate(time, "yyyy-MM-dd HH:mm:ss"));
	 	}
	 	JSONArray imgArray = new JSONArray();
		Date now = new Date();
		if(StringUtils.isNotBlank(previewTime)){
			now = DateUtil.getDate(previewTime,"yyyy-MM-dd HH:mm:ss");
		}
	 	int bgSize = bgTimeArray.size();
	 	if(bgSize == 1){
	 		JSONObject temp = new JSONObject();
	 		temp.put("beginTime", moduleProper.getString(objStr+"BeginTime"));
	 		temp.put("picUrl", moduleProper.getString(objStr+"Url"));
	 		Date one = bgTimeArray.getDate(0);
 			temp.put("state", "0");
 			if(one.before(now)){
 				temp.put("state", "1");
 			}
	 		imgArray.add(temp);
	 	}else if(bgSize > 1) {
	 		for(int j = 0,len = bgTimeArray.size(); j < len; j++){
	 			JSONObject temp = new JSONObject();
	 			Date one = bgTimeArray.getDate(j);
	 			Date next = null;
	 			if(j+1 < len){
	 				next = bgTimeArray.getDate(j+1);
	 			}
	 			temp.put("state", "0");
	 			if(one.before(now) && next == null){
	 				temp.put("state", "1");
	 			}
	 			if(one.before(now) && null !=next && now.before(next)){
	 				temp.put("state", "1");
	 			}
	 			JSONArray beginTime = moduleProper.getJSONArray(objStr+"BeginTime");
			 	JSONArray imgUrl = moduleProper.getJSONArray(objStr+"Url");
				temp.put("beginTime", beginTime.getString(j));
				temp.put("picUrl", imgUrl.getString(j));
				imgArray.add(temp);
	 		}
		 	
	 	}
	  return imgArray;
	}
}
