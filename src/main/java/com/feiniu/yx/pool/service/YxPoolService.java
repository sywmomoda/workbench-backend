package com.feiniu.yx.pool.service;


import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.pool.entity.YxPool;

import java.util.List;

public interface YxPoolService {

	long save(YxPool pool) ;
	
	List<YxPool> queryPoolList(YxPool pool) ;
	    
    YxPool queryPoolById(Long id) ;
    
    int updatePool(YxPool pool) ;
    
    int deleteById(Long id) ;
    
    YxPool queryPoolAndPeriodById(Long poolId,Long periodId) ;

    YxPool queryPoolAndPeriodByDate(Long poolId,String dataString) ;
    
    /**
     * 查询池及当前期
     * @param poolId
     * @return
     */
    YxPool queryPoolAndPeriodById(Long poolId);
    
    
    public JSONObject syncStoreByPool();
    public void removeRedisSync();

}
