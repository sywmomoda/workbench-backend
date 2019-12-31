package com.feiniu.yx.pool.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.pool.dao.YxPoolDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreDao;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.pool.service.YxPoolService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.DateUtil;
import com.fn.cache.client.RedisCacheClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YxPoolServiceImpl implements YxPoolService {
	
	private static Logger logger = Logger.getLogger(YxPoolServiceImpl.class);
	
	@Autowired
	private YxPoolDao yxPoolDao;
	
	@Autowired
	private YxPoolPeriodsDao yxPoolPeriodsDao;
	
	@Autowired
	private YxPoolPeriodsStoreDao yxPoolPeriodsStoreDao;
	
	@Autowired
	private YXStoreService storeService;
	
	@Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	private final String SYNC_KEY = "CMS_YX_SYCN_ALL_STORE"; // 缓存数据
	
	@Autowired
	private RedisCacheClient cacheClient;
	
	public long save(YxPool pool) {
        Date now = new Date();
        String administrator = pool.getAdministrator();
        if (StringUtils.isNotBlank(administrator)) {
            if (!administrator.startsWith(",")) {
                administrator = "," + administrator;
            }
            if (!administrator.endsWith(",")) {
                administrator += ",";
            }  
        }
        
        pool.setAdministrator(administrator);
        
        // end
        pool.setUpdateId(pool.getCreateId());
        pool.setUpdateTime(now);
        pool.setCreateTime(now);
        long id = (Long) yxPoolDao.insertYxPool(pool);
        YxPoolPeriods ypp = new YxPoolPeriods();
        ypp.setPoolId(id);
        ypp.setName("第一期");
        ypp.setNumber(1);
        ypp.setBeginTime(DateUtil.getDayStart());
        //cpp.setEndTime(DateUtils.getDate(DateUtils.getDayStart(), 3650));// 默认10年
        ypp.setUpdateTime(now);
        ypp.setUpdateId(pool.getCreateId());
        ypp.setCreateId(pool.getCreateId());
        Long periodId = (Long) yxPoolPeriodsDao.insertYxPoolPeriods(ypp);
        //String provinces[] = new String[]{};
        YxPoolPeriodsStore pro = null;
        List<YXStore> storeList = storeService.getYXStoreList();
        for (YXStore s : storeList) {
            pro = new YxPoolPeriodsStore();
            pro.setPeriodId(periodId);
            pro.setStoreCode(s.getCode()); // 获取区域下的所有省份
            pro.setCreateId(pool.getCreateId());
            pro.setCreateTime(now);
            yxPoolPeriodsStoreDao.insertYxPoolPeriodsStore(pro);
        }
        return id;
	}
	
	@Override
	public List<YxPool> queryPoolList(YxPool pool) {
        List<YxPool> cpList = yxPoolDao.queryYxPoolList(pool);
        if(cpList!=null && cpList.size()>0){
        	for(YxPool p : cpList){
        		 setCurrentPeriod(p);
        	}
        }
        return cpList;
    }
    
    /**
     * @Description: 根据ID查询池信息
     * @param id
     *            池ID
     * @return 单个池对象
     * @throws
     */
    public YxPool queryPoolById(Long id) {
    	YxPool pool = yxPoolDao.queryYxPoolById(id);
        if (pool == null) {
            return pool;
        } else {
        	setCurrentPeriod(pool);
            return pool;
        }
    }
    
    /**
     * @Description: 更新池信息
     * @param pool
     * @return
     * @throws
     */
    public int updatePool(YxPool pool) {
        YxPool oldPool = queryPoolById(pool.getId());

        String administrator = pool.getAdministrator();
        if (StringUtils.isNotBlank(administrator)) {
            if (!administrator.startsWith(",")) {
                administrator = "," + administrator;
            }
            if (!administrator.endsWith(",")) {
                administrator += ",";
            }
            pool.setAdministrator(administrator);
        }else{
            pool.setAdministrator(oldPool.getAdministrator());
        }

        Date d = new Date();
        pool.setUpdateTime(d);
        int id = yxPoolDao.updateYxPool(pool);
        return id;
    }
    
    /**
     * @Description: 根据ID删除池
     * @param id
     *            池ID
     * @return
     * @throws
     */
    public int deleteById(Long id) {
        int i = yxPoolDao.deleteYxPool(id);
        return i;
    }
    
    /**
     * 查询池及池的期数
     * @author tongwenhuan
     * 2016年5月25日
     * @param poolId
     * @param periodId
     * @return
     */
    public YxPool queryPoolAndPeriodById(Long poolId,Long periodId) {
    	YxPool pool = yxPoolDao.queryYxPoolById(poolId);
    	if (periodId == null || periodId==0 ) {
    		setCurrentPeriod(pool);
    		return pool;
    	} 
    	YxPoolPeriods poolPeriods = yxPoolPeriodsDao.queryYxPoolPeriodsByID(periodId);
		if (pool.getId() - poolPeriods.getPoolId() != 0) {
			setCurrentPeriod(pool);
    		return pool;
		}
    	YxPoolPeriods  pp = new YxPoolPeriods();
    	pp.setPoolId(poolId);
    	List<YxPoolPeriods> yppList = yxPoolPeriodsDao.queryPeriodsList(pp);
    	int len = (null ==yppList ? 0 : yppList.size());
    	int status = 1;
    	if(len > 1){
    		int index = 0;
    		for(int i = 0; i < len; i++) {
    			boolean isEquals = yppList.get(i).getId().longValue() == poolPeriods.getId().longValue();
    			if (isEquals){
    				index = i;
    			} 
    		} 
    		 Date now = new Date();
    		if(index == 0){ //最后一期 
      		    Date nextBeginTime = yppList.get(index).getBeginTime();   
      		    if(nextBeginTime.after(now)){  
      		    	status = 2;
      		    }else{      		    	
      		    	status = 1;
      		    }
    		}else{    		 
    		  Date nextBeginTime = yppList.get(index-1).getBeginTime();  //取出当前期的前面后面一期		
    		  if(now.after(poolPeriods.getBeginTime()) && now.before(nextBeginTime)){
    			status = 1;
    		  }else if(now.before(poolPeriods.getBeginTime())){
    			status = 2;
    		  }else if(now.after(nextBeginTime)){
    			status = 0;
    		}
    	  }
    	}
    	poolPeriods.setStatus(status);
    	pool.setYxPoolPeriods(poolPeriods);    	
    	return pool;
    }


	/**
	 * 查询池及池的期数
	 * @author tongwenhuan
	 * 2016年5月25日
	 * @param poolId
	 * @param dateString
	 * @return
	 */
	@Override
	public YxPool queryPoolAndPeriodByDate(Long poolId,String dateString) {
		if(StringUtils.isBlank(dateString)){
			return queryPoolAndPeriodById(poolId);
		}
		Date now = DateUtil.getDate(dateString,"yyyy-MM-dd HH:mm:ss");
		YxPool pool = yxPoolDao.queryYxPoolById(poolId);
		List<YxPoolPeriods> list = yxPoolPeriodsDao.queryPeriodsListByPoolId(poolId);
		int len = (null ==list ? 0 : list.size());
		if(len<=0){
			return pool;
		}
		int status = 1;
		if(len > 1){
			//初始状态 过期
			status = 0;
			//当前期数
			YxPoolPeriods op = null;
			//当前时间
			for (int i = list.size() - 1; i > -1; i--) {
				YxPoolPeriods p = list.get(i);
				if(now.after(p.getBeginTime())) {
					if (op == null) {
						op = p;
						status = 1;
					} else {
						status = 0;
					}
				} else {
					status = 2;
				}
				p.setStatus(status);
			}

			if (op == null && list.size()>0) {
				if (status == 0) {
					op = list.get(list.size() - 1);
				} else if (status == 2) {
					op = list.get(0);
				}
			}
			pool.setYxPoolPeriods(op);
		}else{
			pool.setYxPoolPeriods(list.get(0));
		}
		return pool;
	}
    
    public YxPool queryPoolAndPeriodById(Long poolId) {
    	YxPool pool = yxPoolDao.queryYxPoolById(poolId);
    	if(pool!=null){
    		setCurrentPeriod(pool);
    	}
    	return pool;
    }
    
    /**
     * 设置池正在使用的期数
     * 如果只有1期，则是正在进行的期数
     * 如果是多期，
     * 1、期数时间与当前时间进行比较，取最近一期
     * 2、期数时间都未开始，取第一期
     * 3、期数时间都过期，取最后一期
     * @author tongwenhuan
     * 2016年5月23日
     * @param pool
     */
    private void setCurrentPeriod(YxPool pool) {
    	// 期数列表
    	List<YxPoolPeriods> yppList = yxPoolPeriodsDao.queryPeriodsListByPoolId(pool.getId());
    	pool.setYppList(yppList);
    	if(yppList.size()>0){
    		pool.setYxPoolPeriods(getOnPeriodsAndSetStatus(yppList));
    	}
    }

    /**
     * 返回当前期数，并设置所有期数状态
     * 期数状态：0 过期，1 当前期  2 未开始期数
     * @author tongwenhuan
     * 2018年1月24日
     * @param list
     * @return 档期期数
     */
    private YxPoolPeriods getOnPeriodsAndSetStatus(List<YxPoolPeriods> list) {
    	//初始状态 过期
    	int status = 0;
    	//当前期数
    	YxPoolPeriods op = null;
    	//当前时间
    	Date now = new Date();
		for (int i = list.size() - 1; i > -1; i--) {
    		YxPoolPeriods p = list.get(i);
    		if(now.after(p.getBeginTime())) {
    			if (op == null) {
    				op = p;
    				status = 1;
    			} else {
    				status = 0;
    			}
    		} else {
    			status = 2;
    		}
    		p.setStatus(status);
    	}
    	
    	if (op == null && list.size()>0) {
    		if (status == 0) {
    			op = list.get(list.size() - 1);
    		} else if (status == 2) {
    			op = list.get(0);
    		}
    	}
    	
    	
    	return op;
    }

	/**
	 * 同步所有池的门店
	 */
    @Override
	public JSONObject syncStoreByPool() {
		JSONObject resultObject = new JSONObject();
		resultObject.put("status", 1);
		resultObject.put("message", "同步进行中...");
		long time = cacheClient.ttl(SYNC_KEY);
		if (time > 0) {
			return resultObject;
		}
		long n = cacheClient.incr(SYNC_KEY, 600);
		if(n == 1) {
			threadPoolTaskExecutor.execute(new SyncPoolThread());
			resultObject.put("status", 0);
			resultObject.put("message", "正在给所有池同步更新所有门店数据，请稍后...");
		} 
		return resultObject;
	}
	
	
	public void removeRedisSync(){
		cacheClient.remove(SYNC_KEY);
	}
	
	class SyncPoolThread implements Runnable {
		
		@Override
		public void run() {
			long st = System.currentTimeMillis();
			List<YxPool> listPool = yxPoolDao.queryAll();
			List<YXStore> allStore = storeService.getYXStoreList();
			for(int num = 0; num <= listPool.size(); num++){
				try {
					YxPool pool = listPool.get(num);
					syncPool(pool,allStore);	
				} catch (Exception e) {				
					logger.error("SyncPoolThread error", e);
				}
			}
			logger.error("SyncPoolThread end:" + (System.currentTimeMillis() - st));
		}
	}
 
    private void syncPool(YxPool p, List<YXStore> allStore){
    	List<YxPoolPeriods> yppList = yxPoolPeriodsDao.queryPeriodsListByPoolId(p.getId());
    	getOnPeriodsAndSetStatus(yppList);
    	for (YxPoolPeriods pps : yppList) {
    		if (pps.getStatus() == 0) { // 过期的档期不同步
    			continue;
    		}
    		synchSinglePeriodStoreDate(pps.getId(), allStore);
    	}
    }
	
    /**
     * 同步期数门店
     * @param periodId
     * @param allStore
     */
    private void synchSinglePeriodStoreDate(Long periodId,List<YXStore> allStore){
        List<YxPoolPeriodsStore> list = yxPoolPeriodsStoreDao.queryStoreListByPeriodsId(periodId);
    	Map<String, String> poolStroreMap = new HashMap<String, String>();
    	for (YxPoolPeriodsStore ps : list) {
    		poolStroreMap.put(ps.getStoreCode(), ps.getStoreCode());
    	}
    	for (YXStore s : allStore) {
    		String code = poolStroreMap.get(s.getCode());
    		if (code != null) {
    			continue;
    		}
    		YxPoolPeriodsStore cMSUnitePeriodsProvince = new YxPoolPeriodsStore();
    		cMSUnitePeriodsProvince.setPeriodId(periodId);
    		cMSUnitePeriodsProvince.setCreateId("sys2");
    		cMSUnitePeriodsProvince.setStoreCode(s.getCode()); // 获取区域下的所有省份
    		cMSUnitePeriodsProvince.setCreateTime(new Date());
    		yxPoolPeriodsStoreDao.insertYxPoolPeriodsStore(cMSUnitePeriodsProvince);
    	}
    }
    
}
