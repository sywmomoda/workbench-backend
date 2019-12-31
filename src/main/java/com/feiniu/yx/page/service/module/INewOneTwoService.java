package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.PoolDataService;
import com.feiniu.yx.util.YxPoolConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @ClassName newOneTwo
 * @Description TODO
 * @Author xuminghui
 * @Date 2019/11/6 9:07
 **/
@Service
public class INewOneTwoService implements CustomModule {
    @Autowired
    private PoolDataService poolDataService;

    @Override
    public void findCustomData(JSONObject mjo) throws Exception {
        String storeCode = mjo.getString("storeCode");
        if(StringUtils.isBlank(storeCode)){
            return;
        }
        String previewTime = mjo.getString("backendPreviewTime");
        JSONObject proJo  = mjo.getJSONObject("moduleProperties");
        //优选档期处理
        String leftIndex[] = new String[]{"leftOne","leftTwo","leftThree","leftFour"};
        String rightIndex[] = new String[]{"rightOne","rightTwo","rightThree","rightFour"};
        String rightBottomIndex[] = new String[]{"rightBottomOne","rightBottomTwo","rightBottomThree","rightBottomFour"};
        List<YxPoolCommodity> commodityList = new ArrayList<YxPoolCommodity>();
        for(int i=0; i<leftIndex.length; i++){
            JSONObject obj = proJo.getJSONObject(leftIndex[i]);
            if(checkIsInNow(obj)){
                YxPoolCommodity poolId = getCommodityList(obj, "poolId", YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC, storeCode, 1,previewTime);
                commodityList.add(poolId);
                break;
            }
        }
        for(int i=0; i<rightIndex.length; i++){
            JSONObject obj = proJo.getJSONObject(rightIndex[i]);
            if(checkIsInNow(obj)){
                commodityList.add(getCommodityList(obj, "poolId", YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC, storeCode, 1,previewTime));
                break;
            }
        }
        for(int i=0; i<rightBottomIndex.length; i++){
            JSONObject obj = proJo.getJSONObject(rightBottomIndex[i]);
            if(obj!=null){
                if(checkIsInNow(obj)){
                    commodityList.add(getCommodityList(obj, "poolId", YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC, storeCode, 1,previewTime));
                    break;
                }
            }
        }
        mjo.put("commodityList", commodityList);
    }

    private boolean checkIsInNow(JSONObject objNew) {
        String bt = objNew.getString("startTime");
        String et = objNew.getString("endTime");
        String isUse = objNew.getString("isUse");
        if("1".equals(isUse)){
            Calendar cl = Calendar.getInstance();
            int hour = cl.get(cl.HOUR_OF_DAY);
            int minute = cl.get(cl.MINUTE);
            int now = hour*100 + minute;
            int beginTime = Integer.parseInt(bt.replace(":", ""));
            int endTime = Integer.parseInt(et.replace(":", ""));
            if(endTime <= beginTime){
                endTime = endTime + 2400;
            }
            if((now>=beginTime && now<endTime) || ((now+2400)>=beginTime && (now+2400)<endTime)){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取图片
     * @param obj
     * @param storeCode
     * @param totalCount
     */
    private YxPoolCommodity getCommodityList(JSONObject obj, String pojoId, String type, String storeCode,
                                              Integer totalCount,String previewTime) {
        Long poolId = obj.getLong(pojoId);
        poolId = null == poolId ? 1L : poolId;
        YxPoolCommodity yxPoolCommodity = poolDataService.findListByIdAndType(poolId, storeCode,
                type, totalCount.intValue(),previewTime).get(0);
        return yxPoolCommodity;
    }
}
