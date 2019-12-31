package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.PoolDataService;
import com.feiniu.yx.util.DateUtil;
import com.feiniu.yx.util.YxPoolConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName IFanLiPeriodService
 * @Description TODO
 * @Author xuminghui
 * @Date 2019/10/30 11:23
 **/
@Service
public class IFanLiPeriodServiceToken implements CustomModule {
    @Autowired
    private PoolDataService poolDataService;

    @Override
    public void findCustomData(JSONObject mjo) {
        String storeCode = mjo.getString("storeCode");
        if(StringUtils.isBlank(storeCode)){
            return;
        }
        String previewTime = mjo.getString("backendPreviewTime");

        JSONObject proJo  = mjo.getJSONObject("moduleProperties");
        List<JSONObject> list = new ArrayList<JSONObject>();
        JSONArray imgArray = getImgFromPicPeriod(proJo,storeCode,previewTime);
        for(int j=0; j < imgArray.size(); j++){//处理老数据兼容
            JSONObject temp = imgArray.getJSONObject(j);
            if("1".equals(temp.getString("state"))){
                JSONArray picUrl = temp.getJSONArray("picUrl");
                for (int i = 0; i < picUrl.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("state",temp.getString("state"));
                    jsonObject.put("picUrl",picUrl.get(i));
                    jsonObject.put("beginTime",temp.getDate("beginTime"));
                    list.add(jsonObject);
                }
            }
        }
        mjo.put("commodityList", list);

    }

    /**
     * @param moduleProper
     * @param storeCode
     * @return
     */
    public JSONArray getImgFromPicPeriod(JSONObject moduleProper,String storeCode, String previewTime) {
        Object bgObj = moduleProper.get("bannerImgBeginTime");
        JSONArray bgTimeArray = new JSONArray();
        if(bgObj instanceof JSONArray){
            JSONArray bg = (JSONArray)bgObj;
            for(int i = 0,len = bg.size(); i < len; i++){
                String time = bg.getString(i);
                bgTimeArray.add(DateUtil.getDate(time, "yyyy-MM-dd HH:mm:ss"));
            }
        }else{
            if(bgObj!=null){
                String time = moduleProper.getString("bannerImgBeginTime");
                bgTimeArray.add(DateUtil.getDate(time, "yyyy-MM-dd HH:mm:ss"));
            }
        }
        JSONArray imgArray = new JSONArray();

        Date now = new Date();
        if(StringUtils.isNotBlank(previewTime)){
            now = DateUtil.getDate(previewTime,"yyyy-MM-dd HH:mm:ss");
        }

        int bgSize = bgTimeArray.size();
        if(bgSize == 1){
            JSONObject temp = new JSONObject();
            temp.put("beginTime", moduleProper.getString("bannerImgBeginTime"));
            String bigImgPoolId = moduleProper.getString("bigImgPoolId");
            String leftSmallPoolId = moduleProper.getString("leftSmallPoolId");
            String rightSmallPoolId = moduleProper.getString("rightSmallPoolId");
            List<YxPoolCommodity> listByBigImgPoolId=new ArrayList<>();
            List<YxPoolCommodity> listByLeftSmallPoolId=new ArrayList<>();
            List<YxPoolCommodity> listByRightSmallPoolId=new ArrayList<>();
            if(bigImgPoolId!=null){
                Long bigImgId = Long.valueOf(bigImgPoolId);
                listByBigImgPoolId = poolDataService.findListByIdAndType(bigImgId, storeCode, YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC, 1,previewTime);
            }
            if(leftSmallPoolId!=null){
                long leftSmallId = Long.valueOf(leftSmallPoolId);
                listByLeftSmallPoolId = poolDataService.findListByIdAndType(leftSmallId, storeCode, YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC, 1,previewTime);
            }
            if(rightSmallPoolId!=null){
                long rightSmallId = Long.valueOf(rightSmallPoolId);
                listByRightSmallPoolId = poolDataService.findListByIdAndType(rightSmallId, storeCode, YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC, 1,previewTime);
            }
            JSONArray jsonArray = new JSONArray();
            if(listByLeftSmallPoolId.size()>0&&listByRightSmallPoolId.size()>0){//同时配有左右小图
                jsonArray.add(listByLeftSmallPoolId.get(0).getPicUrl());
                jsonArray.add(listByRightSmallPoolId.get(0).getPicUrl());
                temp.put("picUrl",jsonArray);
            }else if(listByBigImgPoolId.size()>0){
                jsonArray.add(listByBigImgPoolId.get(0).getPicUrl());
                temp.put("picUrl",jsonArray);
            }else {
                temp.put("picUrl",jsonArray);
            }
            Date one = bgTimeArray.getDate(0);
            temp.put("state", "0");
            if(one.before(now)){
                temp.put("state", "1");
            }
            imgArray.add(temp);
        }else if(bgSize > 1) {
            JSONArray beginTime = moduleProper.getJSONArray("bannerImgBeginTime");
            JSONArray bigImgPoolId = moduleProper.getJSONArray("bigImgPoolId");
            JSONArray leftSmallPoolId = moduleProper.getJSONArray("leftSmallPoolId");
            JSONArray rightSmallPoolId = moduleProper.getJSONArray("rightSmallPoolId");
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
                List<YxPoolCommodity> listByBigImgPoolId=new ArrayList<>();
                List<YxPoolCommodity> listByLeftSmallPoolId=new ArrayList<>();
                List<YxPoolCommodity> listByRightSmallPoolId=new ArrayList<>();
                if(!bigImgPoolId.get(j).toString().equals("")&&bigImgPoolId.get(j)!=null){
                    long bigImgId = bigImgPoolId.getLongValue(j);
                    listByBigImgPoolId = poolDataService.findListByIdAndType(bigImgId, storeCode, YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC, 1,previewTime);
                }
                if(!leftSmallPoolId.get(j).toString().equals("")&&leftSmallPoolId.get(j)!=null){
                    long leftSmallId = leftSmallPoolId.getLongValue(j);
                    listByLeftSmallPoolId = poolDataService.findListByIdAndType(leftSmallId, storeCode, YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC, 1,previewTime);
                }
                if(!rightSmallPoolId.get(j).toString().equals("")&&rightSmallPoolId.get(j)!=null){
                    long rightSmallId = rightSmallPoolId.getLongValue(j);
                    listByRightSmallPoolId = poolDataService.findListByIdAndType(rightSmallId, storeCode, YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC, 1,previewTime);
                }
                JSONArray jsonArray = new JSONArray();
                if(listByLeftSmallPoolId.size()>0&&listByRightSmallPoolId.size()>0){//同时配有左右小图
                    jsonArray.add(listByLeftSmallPoolId.get(0).getPicUrl());
                    jsonArray.add(listByRightSmallPoolId.get(0).getPicUrl());
                    temp.put("picUrl",jsonArray);
                }else if(listByBigImgPoolId.size()>0){
                    jsonArray.add(listByBigImgPoolId.get(0).getPicUrl());
                    temp.put("picUrl",jsonArray);
                }else {
                    temp.put("picUrl",jsonArray);
                }
                temp.put("beginTime", beginTime.getString(j));
                imgArray.add(temp);
            }

        }
        return imgArray;
    }
}
