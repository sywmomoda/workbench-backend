package com.feiniu.b2b.pool.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.feiniu.b2b.pool.entity.B2BPoolCommodity;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;

@Service
public interface B2BPoolPeriodsService{

    String queryPoolPeriods(YxPoolPeriods cpp) ;

    YxPoolPeriods queryPoolPeriodById(Long currentPeriods) ;

    void  deletePeriodAll(Long periodId);
   
    void deletePeriodAllCommodity(Long periodId);

    void deletePeriodsAllProinceCommodity(String commodityId, Long periodId, Integer orders, String province);
   
    List<YxPoolPeriods> queryPeriodsList(YxPoolPeriods cpp);

    int updatePoolPeriod(YxPoolPeriods cmsPoolPeriod) ;

    Long savePoolPeriod(YxPoolPeriods cmsPoolPeriod);

//    void resetOrders(B2BPoolCommodity obj, String commodityId, Integer oldOrder, Integer newOrder) ;
//    
//    void exchangeOrders(B2BPoolCommodity obj, String commodityId, Integer oldOrder, Integer newOrder) ;

    int checkPeriodDate(YxPoolPeriods cmsPoolPeriod) ;

    void deleteButchCommodity(String[] ids, Long periodId, String province) ;
   
    List<B2BPoolCommodity> getPageData(List<B2BPoolCommodity> list, B2BPoolCommodity poolCommodity);
    
    /**
     * 同步池对应的门店
     * 当门店有增加时，将门店和池绑定
     * @param periodId
     * @param poolId
     */
    void synchStoreData(String poolId) ;
    
    List<B2BPoolCommodity> getB2BPoolCommodityList(Long poolId,String code,boolean isShow) ;
    
    List<B2BPoolCommodity> getB2BPoolCommodityList(YxPool pool,String code,boolean isShow) ;
    
	List<B2BPoolCommodity> getB2BPoolCommodityList(YxPool pool,YxPoolPeriodsStore ppp,boolean isShow) ;

    void styleFault(List<B2BPoolCommodity> alllist,String seq);
    
    /**
     * 池商品重排序
     * @param periodId
     * @param commondityId
     * @param order
     */
    void resetOrder(Long periodId, String storeCode, String commondityId, int order);
    
}
