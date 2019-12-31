package com.feiniu.b2b.pool.service;


import java.util.List;

import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;

public interface B2BPoolService {

	public long save(YxPool pool) ;
	
	public List<YxPool> queryPoolList(YxPool pool) ;
	    
    public YxPool queryPoolById(Long id) ;
    
    public int updatePool(YxPool pool) ;
    
    public int deleteById(Long id) ;
    
    public YxPool queryPoolAndPeriodById(Long poolId,Long periodId) ;
    
    public YxPool queryPoolAndPeriodById(Long poolId);
    
    public List<YxPoolPeriodsStore> getPoolStoreList(Long periodsId);

}
