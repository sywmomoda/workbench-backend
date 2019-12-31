package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.core.impl.BaseCommodityServiceImpl;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.RemoteCommodityService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.YxRemoteCommodityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName MBaiKuanService
 * @Description TODO
 * @Author xuminghui
 * @Date 2019/12/4 9:37
 **/
@Service
public class IModuleBaoKuanService implements CustomModule {
    @Autowired
    private BaseCommodityServiceImpl baseCommodityService;
    @Autowired
    private YxRemoteCommodityService remoteCommodityService;

    @Override
    public void findCustomData(JSONObject mjo) throws Exception {
        String storeCode = mjo.getString("storeCode");
        if (StringUtils.isBlank(storeCode)) {
            return;
        }
        JSONArray commodityArray = mjo.getJSONArray("commodityList");
        JSONArray jsonArray = new JSONArray();
        if (null != commodityArray) {
            JSONObject commodityJson = (JSONObject) mjo.getJSONArray("commodityList").get(0);
            String urlType = commodityJson.getString("urlType");
            if (urlType.equals("detail")) {
                String commodityId = commodityJson.getString("urlProperties").trim();
                String[] ids = new String[]{commodityId};
                if(!commodityId.substring(0,1).equals("P")){
                    List<YxPoolCommodity> listCommodity = remoteCommodityService.getRemoteCommodityListByIds(ids,1);
                    commodityId = listCommodity.get(0).getCommodityId();
                    ids = new String[]{commodityId};
                }
                List<YxPoolCommodity> list = baseCommodityService.listCommodityAllInfo(ids, storeCode);
                if (list.size() > 0) {
                    YxPoolCommodity yxPoolCommodity = list.get(0);
                    yxPoolCommodity.setPicUrl(commodityJson.getString("picUrl"));
                    jsonArray.add(yxPoolCommodity);
                }
            }
        }
        mjo.put("commodityList", jsonArray);
    }
}
