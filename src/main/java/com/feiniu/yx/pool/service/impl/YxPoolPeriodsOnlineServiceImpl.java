package com.feiniu.yx.pool.service.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.yx.pool.dao.YxPoolCommodityOnlineDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsOnlineDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreOnlineDao;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.pool.service.YxPoolPeriodsOnlineService;

@Service
public class YxPoolPeriodsOnlineServiceImpl implements YxPoolPeriodsOnlineService {
 
	@Autowired
	private YxPoolPeriodsStoreOnlineDao yxPoolPeriodsStoreDao;
	
	@Autowired
	private YxPoolPeriodsOnlineDao cppDao;
	
	@Autowired
	private YxPoolCommodityOnlineDao commodityDao;
	
	public void deletePeriodsAll(long periodId){
		
		YxPoolPeriods cpp = cppDao.queryYxPoolPeriodsByID(periodId);
		if(null == cpp){
			return;
		}
		
		YxPoolPeriodsStore yxPoolPeriodsStore = new YxPoolPeriodsStore();
		yxPoolPeriodsStore.setPeriodId(periodId);
		yxPoolPeriodsStore.setPageRows(1000);
        // 获得期数下所有省份与商品的关系
        List<YxPoolPeriodsStore> yxPoolPeriodsStoreList = yxPoolPeriodsStoreDao.queryStoreList(yxPoolPeriodsStore);
        for (YxPoolPeriodsStore pro : yxPoolPeriodsStoreList) {
            String commoditys = pro.getCommoditys();
            if (StringUtils.isNotBlank(commoditys)) {
                commodityDao.batchDeleteYxPoolCommodity(commoditys);
            }
            yxPoolPeriodsStoreDao.deleteYxPoolPeriodsStore(pro.getId());
        }        
        cppDao.deleteYxPoolPeriods(periodId);
	}
}
