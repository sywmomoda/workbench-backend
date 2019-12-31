package com.feiniu.yx.pool.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.feiniu.yx.common.entity.ReturnT;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.entity.YxPoolPeriods;

public interface YxPoolPeriodsService{

	/**
	 * 删除指定期数
	 * @param periodId
	 */
    void deletePeriodAll(Long periodId);
   
    /**
     * 清空期数下数据
     * @param periodId
     */
    void deletePeriodAllCommodity(Long periodId);

    void deletePeriodsAllProinceCommodity(String commodityId, Long periodId, Integer orders, String province);
   
    /**
     * @Description 查询池各期列表
     * @param cpp
     * @return
     */
    List<YxPoolPeriods> queryPeriodsList(YxPoolPeriods cpp);

    int updatePoolPeriod(YxPoolPeriods cmsPoolPeriod) ;

    Long savePoolPeriod(YxPoolPeriods cmsPoolPeriod);

    ReturnT<String> resetOrders(YxPoolCommodity obj, String commodityId, Integer oldOrder, Integer newOrder) ;

    int checkPeriodDate(YxPoolPeriods cmsPoolPeriod) ;

    int checkCopyPeriodDate(List<YxPoolPeriods> cmsPoolPeriods, List<YxPoolPeriods> periodListQquery,String repeatTimes);

    void deleteButchCommodity(String[] ids, Long periodId, String province) ;
    
    /**
     * 同步池对应的门店
     * 当门店有增加时，将门店和池绑定
     * @param periodId
     * @param poolId
     */
    void synchStoreData(String poolId) ;
    
//   List<YxPoolCommodity> getYxPoolCommodityList(Long poolId,String code,boolean isShow) ;
    
//	List<YxPoolCommodity> getYxPoolCommodityList(YxPool pool,YxPoolPeriodsStore ppp,boolean isShow) ;

	void checkPeriodStoreAndRestore(YxPool pool);

	void checkPeriodStoreAndRestore(String curPage, String pageRows);

	void copyPeriodsById(YxPoolPeriods yxPoolPeriods);
}
