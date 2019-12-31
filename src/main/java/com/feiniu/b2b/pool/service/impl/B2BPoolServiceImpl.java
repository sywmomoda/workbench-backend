package com.feiniu.b2b.pool.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.b2b.pool.service.B2BPoolService;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.pool.dao.YxPoolDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreDao;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.util.DateUtil;

@Service
public class B2BPoolServiceImpl implements B2BPoolService {

	@Autowired
	private YxPoolDao b2bPoolDao;
	
	@Autowired
	private YxPoolPeriodsDao b2bPoolPeriodsDao;
	
	@Autowired
	private YxPoolPeriodsStoreDao b2bPoolPeriodsStoreDao;
	
	@Autowired
	private B2BStoreService storeService;
	
	@Autowired
	private B2BPoolPeriodsStoreServiceImpl b2bPoolPeriodsStoreService;
	
	
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
        long id = (Long) b2bPoolDao.insertB2BPool(pool);
        YxPoolPeriods ypp = new YxPoolPeriods();
        ypp.setPoolId(id);
        ypp.setName("第一期");
        ypp.setNumber(1);
        ypp.setBeginTime(DateUtil.getDayStart());
        //cpp.setEndTime(DateUtils.getDate(DateUtils.getDayStart(), 3650));// 默认10年
        ypp.setUpdateTime(now);
        ypp.setUpdateId(pool.getCreateId());
        ypp.setCreateId(pool.getCreateId());
        Long periodId = (Long) b2bPoolPeriodsDao.insertYxPoolPeriods(ypp);
        //String provinces[] = new String[]{};
        YxPoolPeriodsStore pro = null;
        List<B2BStore> storeList = storeService.getB2BStoreList();
        for (B2BStore s : storeList) {
            pro = new YxPoolPeriodsStore();
            pro.setPeriodId(periodId);
            pro.setStoreCode(s.getCode()); // 获取区域下的所有省份
            pro.setCreateId(pool.getCreateId());
            pro.setCreateTime(now);
            b2bPoolPeriodsStoreDao.insertYxPoolPeriodsStore(pro);
        }
        return id;
	}
	
	public List<YxPool> queryPoolList(YxPool pool) {
        List<YxPool> cpList = null;
        cpList = b2bPoolDao.queryB2BPoolList(pool);
       if(cpList!=null && cpList.size()>0){
        	for(YxPool p:cpList){
        		 p = getCurrentPeriodId(p);
        	}
        }
        return cpList;
    }
	
	/**
     * @Description: 获取当前池期数,需要查询到的期数列表已按时间从大到小排序
     * @param pool
     * @return
     * @throws
     */
    private YxPool getCurrentPeriodId(YxPool pool) {
        YxPoolPeriods cpp = new YxPoolPeriods();
        cpp.setPoolId(pool.getId());
        cpp.setPageRows(Integer.MAX_VALUE);
        pool.setCurrentPeriods(0L);
        // 获取池期数
        Long lastPeriodsId = null;
        List<YxPoolPeriods> yppList = b2bPoolPeriodsDao.queryPeriodsList(cpp);// 期数列表
        Date d = new Date();
        for(int i=0; i<yppList.size();i++){
        	YxPoolPeriods nowPP = yppList.get(i);
        	//nowPP.setStoreList( b2bPoolPeriodsStoreService.getPoolPeriodsStoresByPeriodsId(nowPP.getId(),null));
        	Calendar cl = Calendar.getInstance();
        	cl.setTime(nowPP.getBeginTime());
        	cl.set(Calendar.SECOND, 0);
        	nowPP.setBeginTime(cl.getTime());
        	if(yppList.size()==1){//只有一期时
        		lastPeriodsId = nowPP.getId();
        		nowPP.setStatus(1);
        		yppList.set(i, nowPP);
        		break;
        	}
        	
        	if(yppList.size()>0 && i<(yppList.size()-1)){//存在多期时
        		YxPoolPeriods nextPP = yppList.get(i+1);
        		if(nowPP.getBeginTime().after(d) && nextPP.getBeginTime().before(d)){//中间
        			lastPeriodsId = nextPP.getId();
        			nowPP.setStatus(2);
	        		yppList.set(i,nowPP);
	        		nextPP.setStatus(1);
	        		nextPP.setEndTime(nowPP.getBeginTime());
	        		yppList.set(i+1,nextPP);
        		}else if(nowPP.getBeginTime().after(d)){//所有期数未到,取后一期
        			lastPeriodsId = nextPP.getId();
        			nowPP.setStatus(2);
	        		yppList.set(i,nowPP);
	        		nextPP.setStatus(1);
	        		nextPP.setEndTime(nowPP.getBeginTime());
	        		yppList.set(i+1,nextPP);
        		}else if(nowPP.getBeginTime().before(d)){//所有期数时间已过，取最新一期
        			if(i==0){
        				lastPeriodsId = nowPP.getId();
	    				nowPP.setStatus(1);
	            		yppList.set(i,nowPP);
	    			}else{
	    				if(yppList.get(i-1).getBeginTime().before(d)){
	    					nowPP.setStatus(0);
		            		yppList.set(i,nowPP);
	    				}
	    			}
        			nextPP.setEndTime(nowPP.getBeginTime());
	        		nextPP.setStatus(0);
	        		yppList.set(i+1,nextPP);
        		}
        	}
        }
        pool.setCurrentPeriods(lastPeriodsId);
        pool.setYppList(yppList);
        return pool;
    }
    
    /**
     * @Description: 根据ID查询池信息
     * @param id
     *            池ID
     * @return 单个池对象
     * @throws
     */
    public YxPool queryPoolById(Long id) {
    	YxPool pool = b2bPoolDao.queryB2BPoolById(id);
        if (pool == null) {
            return pool;
        } else {
        	getCurrentPeriodId(pool);
            return pool;
        }
    }
    
    /**
     * @param request
     * @Description: 更新池信息
     * @param Pool
     *            池信息
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
        //pool.setCreateId(null);
        //pool.setCreateTime(null);
        //pool.setUpdateId("system");
        pool.setUpdateTime(d);
        int id = b2bPoolDao.updateYxPool(pool);
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
        int i = b2bPoolDao.deleteB2BPool(id);
        return i;
    }
    
    /**
     * 查询池及池的当前期
     * @author tongwenhuan
     * 2016年5月25日
     * @param poolId
     * @param periodId
     * @return
     */
    public YxPool queryPoolAndPeriodById(Long poolId,Long periodId) {
    	YxPool pool = b2bPoolDao.queryB2BPoolById(poolId);
    	YxPoolPeriods poolPeriods = b2bPoolPeriodsDao.queryYxPoolPeriodsByID(periodId);    	
    	YxPoolPeriods  pp = new YxPoolPeriods();
    	pp.setPoolId(poolId);
    	List<YxPoolPeriods> yppList = b2bPoolPeriodsDao.queryPeriodsList(pp);  
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
    
    public YxPool queryPoolAndPeriodById(Long poolId) {
    	YxPool pool = b2bPoolDao.queryB2BPoolById(poolId);
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
    	List<YxPoolPeriods> yppList = b2bPoolPeriodsDao.queryPeriodsListByPoolId(pool.getId());
    	pool.setYppList(yppList);
    	//只有1期，返回此期数
    	if(yppList.size() == 1) {    		
    		pool.setYxPoolPeriods(yppList.get(0));
    		if(null != yppList.get(0)){
    			yppList.get(0).setStatus(1);	
    		}    		
    		return;
    	}
    	//多期数判断
    	long now = (new Date()).getTime();
    	int i = 0;
    	int size = yppList.size();
    	do{
    		if(i == 0) {
    			YxPoolPeriods p1 = yppList.get(i);
    			//如果所有活动都未开始，返回第一期
        		if(now < p1.getBeginTime().getTime()) {
        			p1.setStatus(2);
        			pool.setYxPoolPeriods(p1);
        			return;
        		}
        		p1 = yppList.get(size -1);
        		//如果所有活动都结束，返回最后一期
        		if(now >= p1.getBeginTime().getTime()) {
        			p1.setStatus(1);
        			pool.setYxPoolPeriods(p1);
        			return;
        		}
    		}
    		//前后各取2个进行比较
    		YxPoolPeriods p1 = yppList.get(i);
    		YxPoolPeriods p2 = yppList.get(i+1);
    		if(now >= p1.getBeginTime().getTime() && now < p2.getBeginTime().getTime()) {
    			p1.setStatus(1);
    			pool.setYxPoolPeriods(p1);    			
    			p2 = null;
    			return;	
    		}
    		p1 = yppList.get(size - 2 - i);
    		p2 = yppList.get(size -1 - i);
    		if(now >= p1.getBeginTime().getTime() && now < p2.getBeginTime().getTime()) {
    			p1.setStatus(1);
    			pool.setYxPoolPeriods(p1);
    			p2 = null;
    			return;	
    		}
    		i++;
    	}while ((i*2) < size);
    	
    }
    
    @Override
    public List<YxPoolPeriodsStore> getPoolStoreList(Long periodsId) {
     List<YxPoolPeriodsStore> listStore = b2bPoolPeriodsStoreService.queryStoreListByPeriodsId(periodsId);
     Map<String, B2BStore> mapStore  = storeService.getB2BStoreMap();
     List<YxPoolPeriodsStore> newListStore = new ArrayList<YxPoolPeriodsStore>();
     for(YxPoolPeriodsStore pps : listStore){
    	 String key = pps.getStoreCode();
    	 B2BStore store = mapStore.get(key);
    	 if(null == store){
    		 newListStore.remove(store);
    		 continue;
    	 }
    	 String storeName = store.getName();
    	 pps.setStoreName(storeName);
    	 String commodits = pps.getCommoditys();
    	 if(StringUtils.isNotBlank(commodits)){
    		 String[] ids= commodits.split(",");
             pps.setCountCommodity(ids.length);
    	 }
    	 
    	 newListStore.add(pps);
     }
      return newListStore;
    }

}
