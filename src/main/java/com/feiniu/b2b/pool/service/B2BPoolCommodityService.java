package com.feiniu.b2b.pool.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.pool.entity.B2BPoolCommodity;
import com.feiniu.yx.pool.entity.YxPicLinks;

public interface B2BPoolCommodityService {

	public B2BPoolCommodity queryCommodityById(String Id) ;

	public void updateCommodityFromPool(B2BPoolCommodity poolCommodity) ;

	public void updateCommodityPicFromPool(B2BPoolCommodity poolCommodity);

	public String  addB2BPoolCommodityById(String[] ids, Long periodId, int poolType, String storeGroups,String stores);

    public String savePicAndText(B2BPoolCommodity cpc, Long periodId,  String storeGroups,String stores);
    
    public String checkAndReplaceCommodity(String newGood, String oldId, Long periodId, String storeCode);

    public void exportExcel(OutputStream outputStream, B2BPoolCommodity obj)  ;
    
    //public YxPicLinks getPicLinks(String areaSeq,String seqJson);
    
    public YxPicLinks getCommodityForSelect(String art_no, String storeCodes) throws IOException;
    
	public B2BPoolCommodity getCommodityById(String id);

	public List<B2BPoolCommodity> getCommodityByIds(String ids);

	public JSONObject getCommodityForShow(String commodityId, String storeCode);
}
