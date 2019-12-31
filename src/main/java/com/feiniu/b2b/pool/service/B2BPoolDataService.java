package com.feiniu.b2b.pool.service;

import java.util.List;

import com.feiniu.b2b.pool.entity.B2BPoolCommodity;


public  interface B2BPoolDataService   {
   List<B2BPoolCommodity> findListByIdAndType(Long poolId, String storeCode,String type, int count);
}
