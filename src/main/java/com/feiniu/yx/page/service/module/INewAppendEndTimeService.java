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
 * @ClassName INewAppendEndTime
 * @Description TODO
 * @Author xuminghui
 * @Date 2019/11/7 13:44
 **/
@Service
public class INewAppendEndTimeService implements CustomModule {
    @Override
    public void findCustomData(JSONObject mjo) throws Exception {
        String storeCode = mjo.getString("storeCode");
        if(StringUtils.isBlank(storeCode)){
            return;
        }
        String previewTime = mjo.getString("backendPreviewTime");
        List<JSONObject> list = new ArrayList<JSONObject>();
        JSONObject proJo  = mjo.getJSONObject("moduleProperties");
        String[] idArray = new String[]{"bannerImg","leftImg","leftBottomImg","rightImg","rightBottomImg"};
        for(int i=0; i<idArray.length; i++){
            String id = idArray[i];
            if(proJo.get(id+"Url")!=null){
                JSONArray imgArray = getImgFromPicPeriod(id,proJo,storeCode,previewTime);
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

    public static JSONArray getImgFromPicPeriod(String objStr,
                                                JSONObject moduleProper,String storeCode, String previewTime) {
        Object bgObjBeginTime = moduleProper.get(objStr + "BeginTime");
        Object bgObjEndTime = moduleProper.get(objStr + "EndTime");
        JSONArray bgBeginTimeArray = new JSONArray();
        JSONArray bgEndTimeArray = new JSONArray();
        Date now = new Date();
        if(StringUtils.isNotBlank(previewTime)){
            now = DateUtil.getDate(previewTime,"yyyy-MM-dd HH:mm:ss");
        }
        if (bgObjBeginTime instanceof JSONArray) {
            JSONArray bgBeginTime = (JSONArray) bgObjBeginTime;
            for (int i = 0, len = bgBeginTime.size(); i < len; i++) {
                String bTime = bgBeginTime.getString(i);
                bgBeginTimeArray.add(DateUtil.getDate(bTime, "yyyy-MM-dd HH:mm:ss"));
            }
        } else {
            String beginTime = moduleProper.getString(objStr + "BeginTime");
            bgBeginTimeArray.add(DateUtil.getDate(beginTime, "yyyy-MM-dd HH:mm:ss"));
        }

        if (bgObjEndTime instanceof JSONArray) {
            JSONArray bgEndTime = (JSONArray) bgObjEndTime;
            for (int i = 0, len = bgEndTime.size(); i < len; i++) {
                String eTime = bgEndTime.getString(i);
                if (StringUtils.isNotBlank(eTime)) {
                    bgEndTimeArray.add(DateUtil.getDate(eTime, "yyyy-MM-dd HH:mm:ss"));
                }
            }
        } else {
            String endTime = moduleProper.getString(objStr + "EndTime");
            if(StringUtils.isNotBlank(endTime)){
                bgEndTimeArray.add(DateUtil.getDate(endTime, "yyyy-MM-dd HH:mm:ss"));
            }
        }

        JSONArray imgArray = new JSONArray();
        int bgSize = bgBeginTimeArray.size();
        int egSize = bgEndTimeArray.size();
        if (bgSize == 1 && egSize == 1) {
            JSONObject temp = new JSONObject();
            temp.put("beginTime", moduleProper.getString(objStr + "BeginTime"));
            temp.put("picUrl", moduleProper.getString(objStr + "Url"));
            Date oneBeginTime = bgBeginTimeArray.getDate(0);
            Date oneEndTime = bgEndTimeArray.getDate(0);

            temp.put("state", "0");
            if (now.getTime() > oneBeginTime.getTime() && now.getTime() < oneEndTime.getTime()) {
                temp.put("state", "1");
            }
            imgArray.add(temp);
        } else if (bgSize == 1 && egSize == 0) {
            JSONObject temp = new JSONObject();
            temp.put("beginTime", moduleProper.getString(objStr + "BeginTime"));
            temp.put("picUrl", moduleProper.getString(objStr + "Url"));
            Date one = bgBeginTimeArray.getDate(0);
            temp.put("state", "0");
            if (one.before(now)) {
                temp.put("state", "1");
            }
            imgArray.add(temp);
        } else if (bgSize > 1 && egSize == bgSize) {
            for (int j = 0, len = bgBeginTimeArray.size(); j < len; j++) {
                JSONObject temp = new JSONObject();
                Date oneBeginTime = bgBeginTimeArray.getDate(j);
                Date oneEndTime = bgEndTimeArray.getDate(j);
                temp.put("state", "0");
                if (now.getTime() > oneBeginTime.getTime() && now.getTime() < oneEndTime.getTime()) {
                    temp.put("state", "1");
                }
                JSONArray beginTime = moduleProper.getJSONArray(objStr + "BeginTime");
                JSONArray imgUrl = moduleProper.getJSONArray(objStr + "Url");
                temp.put("beginTime", beginTime.getString(j));
                temp.put("picUrl", imgUrl.getString(j));
                imgArray.add(temp);
            }
        } else if (bgSize > 1 && egSize != bgSize) {
            for (int j = 0, len = bgBeginTimeArray.size(); j < len; j++) {
                JSONObject temp = new JSONObject();
                Date one = bgBeginTimeArray.getDate(j);
                Date next = null;
                if (j + 1 < len) {
                    next = bgBeginTimeArray.getDate(j + 1);
                }
                temp.put("state", "0");
                if (one.before(now) && next == null) {
                    temp.put("state", "1");
                }
                if (one.before(now) && null != next && now.before(next)) {
                    temp.put("state", "1");
                }
                JSONArray beginTime = moduleProper.getJSONArray(objStr + "BeginTime");
                JSONArray imgUrl = moduleProper.getJSONArray(objStr + "Url");
                temp.put("beginTime", beginTime.getString(j));
                temp.put("picUrl", imgUrl.getString(j));
                imgArray.add(temp);
            }
        }
        return imgArray;
    }
}
