package com.feiniu.yx.pool.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.pool.entity.YxPoolCommodity;

public interface YxPoolCommodityService {

	YxPoolCommodity queryCommodityById(String Id) ;

	void updateCommodityFromPool(YxPoolCommodity poolCommodity) ;

	void updateCommodityPicFromPool(YxPoolCommodity poolCommodity);

	String  addYxPoolCommodityById(String[] ids, Long periodId, int poolType, String storeGroups,String stores,String commodityType);

    String savePicAndText(YxPoolCommodity cpc, Long periodId,  String storeGroups,String stores);
    
    String checkAndReplaceCommodity(String newGood, String oldId, Long periodId, String storeCode,String commodityType);

//    void exportExcel(OutputStream outputStream, YxPoolCommodity obj);
    
    JSONObject getCommodityForSelect(String art_no, String storeCodes,String searchType) throws IOException;
    
	YxPoolCommodity getCommodityById(String id);

	List<YxPoolCommodity> getCommodityByIds(String ids);

	JSONObject getCommodityForShow(String commodityId, String storeCode);
	JSONObject getCommodityForModule(String commodityId,String selectStoreCode);
	JSONObject getCommodityByCouponIdForModule(String commodityId,String selectStoreCode,String couponId);

//	/**
//	 * 更新商品主图
//	 * @param list
//	 * @return
//	 */
//	List<YxPoolCommodity> updateCommodityImg(List<YxPoolCommodity> list);
//	
//	List<YxPoolCommodity> updateCommodityImg(List<YxPoolCommodity> list,String storeCode);
	
	public String  addYxPoolCommodityByIdFromRemote(List<YxPoolCommodity> remoteQueryResult, Long poolId, String storeCode, int classId);

	void deleteRemotePoolCommodityDate(Long poolId, int classId, String storeCode);
	public JSONObject syncStoreDataByStore(String data);
	public void exportExcel(String data,HttpServletResponse response,HttpServletRequest request);

	Map<String,Map<String, YxPoolCommodity>> validCommodityForShow(String commodityIds, String storeCodes);

	JSONObject queryCommoditySaleInfo(String id, String storeCode);

    Map<String,YxPoolCommodity> getRemoteCommodity(String[] ids,int searchType);
    void updateCommoidtyOfStore(Long id,String storeCodes,String groupIds);
}
