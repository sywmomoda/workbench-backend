package com.feiniu.b2b.pool.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.b2b.pool.dao.B2BPoolCommodityOnlineDao;
import com.feiniu.b2b.pool.service.B2BPoolPeriodsOnlineService;
import com.feiniu.yx.pool.dao.YxPoolPeriodsOnlineDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreOnlineDao;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;

@Service
public class B2BPoolPeriodsOnlineServiceImpl implements B2BPoolPeriodsOnlineService {
 
	@Autowired
	private YxPoolPeriodsStoreOnlineDao b2bPoolPeriodsStoreDao;
	
	@Autowired
	private YxPoolPeriodsOnlineDao cppDao;
	
	@Autowired
	private B2BPoolCommodityOnlineDao commodityDao;
	
	public void deletePeriodsAll(long periodId){
		
		YxPoolPeriods cpp = cppDao.queryYxPoolPeriodsByID(periodId);
		if(null == cpp){
			return;
		}
		
		YxPoolPeriodsStore yxPoolPeriodsStore = new YxPoolPeriodsStore();
		yxPoolPeriodsStore.setPeriodId(periodId);
		yxPoolPeriodsStore.setPageRows(1000);
        // 获得期数下所有省份与商品的关系
        List<YxPoolPeriodsStore> yxPoolPeriodsStoreList = b2bPoolPeriodsStoreDao.queryStoreList(yxPoolPeriodsStore);
        for (YxPoolPeriodsStore pro : yxPoolPeriodsStoreList) {
            String commoditys = pro.getCommoditys();
            if (StringUtils.isNotBlank(commoditys)) {
                commodityDao.batchDeleteB2BPoolCommodity(commoditys);
            }
            b2bPoolPeriodsStoreDao.deleteYxPoolPeriodsStore(pro.getId());
        }        
        cppDao.deleteYxPoolPeriods(periodId);
	}
}
