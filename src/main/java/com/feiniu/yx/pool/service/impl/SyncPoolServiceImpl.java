package com.feiniu.yx.pool.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.feiniu.yx.pool.dao.YxPoolCommodityDao;
import com.feiniu.yx.pool.dao.YxPoolCommodityOnlineDao;
import com.feiniu.yx.pool.dao.YxPoolDao;
import com.feiniu.yx.pool.dao.YxPoolOnlineDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsOnlineDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreOnlineDao;
import com.feiniu.yx.pool.dao.YxPoolProperPlusDao;
import com.feiniu.yx.pool.dao.YxPoolProperPlusOnlineDao;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.pool.entity.YxPoolProperPlus;
import com.feiniu.yx.pool.service.SyncPoolService;
import com.fn.cache.client.RedisCacheClient;

/**
 * 同步池数据到online表
 * 优化：先判断表的更新时间，
 * 线下数据更新时间大于online，进行更新
 * @author tongwenhuan
 *
 */
@Component
public class SyncPoolServiceImpl implements SyncPoolService {

	private static Logger logger = Logger.getLogger(SyncPoolServiceImpl.class);
	
	@Autowired
	private YxPoolDao poolDao;
	@Autowired
	private YxPoolOnlineDao poolOnlineDao;
	
	@Autowired
	private YxPoolPeriodsDao periodsDao;
	@Autowired
	private YxPoolPeriodsOnlineDao periodsOnlineDao;
	
	@Autowired
	private YxPoolPeriodsStoreDao periodsStoreDao;
	@Autowired
	private YxPoolPeriodsStoreOnlineDao periodsStoreOnlineDao;
	
	@Autowired
	private YxPoolCommodityDao commodityDao;
	@Autowired
	private YxPoolCommodityOnlineDao commodityOnlineDao;
	@Autowired
	private YxPoolProperPlusDao properPlusDao;
	@Autowired
	private YxPoolProperPlusOnlineDao properPlusOnlineDao;
	
	@Autowired
	private RedisCacheClient cacheClient;
	
	private static String SYNC_POOL_KEY = "SYNC_POOL_ID_";
	
	@Override
	public void syncPool(Long poolId) {
		if (poolId == null) {
			logger.error("syncPool error, poolId is null");
			return;
		}
		//每个pool，30秒同步一次
		String key = SYNC_POOL_KEY + poolId.toString();
		String d = cacheClient.get(key);
		if (d != null) {
			return;
		}
		cacheClient.setex(key, 60, "ok");
		//同步到online表
		sync(poolId);
	}
	
	private void sync(Long poolId) {
		YxPool pool = poolDao.queryYxPoolById(poolId);
		if (pool == null) {
			logger.equals("syncPool error, pool is null, poolId is " + poolId);
			return;
		}
		YxPool pool_online = poolOnlineDao.queryYxPoolById(poolId);
		//第一次同步池数据，直接添加
		if (pool_online == null) {
			poolOnlineDao.insertYxPool(pool);
		} else {
			Date upd_time = pool.getUpdateTime();
			Date upd_time_online = pool_online.getUpdateTime();
			if (upd_time == null || upd_time_online == null) {
				logger.equals("syncPool error, upd_time or upd_time_online is null, poolId is " + poolId);
				return;
			}
			//判断更新时间，线下数据更新时间大于online，进行更新
			if (upd_time.getTime() > upd_time_online.getTime()) {
				poolOnlineDao.updateYxPool(pool);
			}
		}
		//同步池期数
		syncPoolPeriods(poolId);
	}


	/**
	 * 同步池期数
	 * @param poolId 池id
	 */
	private void syncPoolPeriods(Long poolId) {
		List<YxPoolPeriods> yppList = periodsDao.queryPeriodsListByPoolId(poolId);//取出的数据为按beginTime升序
		if (yppList == null) {
			logger.equals("syncPool error, pool periods null, poolId is " + poolId);
			return;
		}
		//倒序处理,按时间从新到旧排列
		Collections.sort(yppList, new Comparator<YxPoolPeriods>() {
            public int compare(YxPoolPeriods o1, YxPoolPeriods o2) {
                //降序
                return o2.getBeginTime().compareTo(o1.getBeginTime());
            }
        });
		
		boolean hasCurrent = false;//用以判断已过期的不同步
		for (YxPoolPeriods period: yppList) {
			YxPoolPeriods onlinePeriod = periodsOnlineDao.queryYxPoolPeriodsByID(period.getId());
			//第一次同步数据，直接添加
			if (onlinePeriod == null) {
				periodsOnlineDao.insertYxPoolPeriods(period);
            }else{
            	//先判断数据是否有更新
            	Date upd_time = period.getUpdateTime();
    			Date upd_time_online = onlinePeriod.getUpdateTime();
    			//判断更新时间，线下数据更新时间大于online，进行更新
    			if (upd_time.getTime() > upd_time_online.getTime()) {
    				periodsOnlineDao.updateYxPoolPeriods(period);
    			}
			}
			if(hasCurrent){//存在当前期，旧期跳过，不同步期数门店数据
				continue;
			}
			//同步期数门店数据
			syncPeriodStroe(period.getId());
			
			if(period.getBeginTime().before(new Date())){
				hasCurrent = true;
			}
		}
	}
	
	
	public void manualSyncPool(Long poolId){
		YxPool pool = poolDao.queryYxPoolById(poolId);
		if (pool == null) {
			logger.equals("syncPool error, pool is null, poolId is " + poolId);
			return;
		}
		YxPool pool_online = poolOnlineDao.queryYxPoolById(poolId);
		//第一次同步池数据，直接添加
		if (pool_online == null) {
			poolOnlineDao.insertYxPool(pool);
		} else {
			Date upd_time = pool.getUpdateTime();
			Date upd_time_online = pool_online.getUpdateTime();
			if (upd_time == null || upd_time_online == null) {
				logger.equals("syncPool error, upd_time or upd_time_online is null, poolId is " + poolId);
				return;
			}
			poolOnlineDao.updateYxPool(pool);
		}
		//同步池期数
		manualSyncPoolPeriods(poolId);
	}
	
	/**
	 * 同步池期数
	 * @param poolId 池id
	 */
	private void manualSyncPoolPeriods(Long poolId) {
		List<YxPoolPeriods> yppList = periodsDao.queryPeriodsListByPoolId(poolId);//取出的数据为按beginTime升序
		if (yppList == null) {
			logger.equals("syncPool error, pool periods null, poolId is " + poolId);
			return;
		}
		//倒序处理,按时间从新到旧排列
		Collections.sort(yppList, new Comparator<YxPoolPeriods>() {
            public int compare(YxPoolPeriods o1, YxPoolPeriods o2) {
                //降序
                return o2.getBeginTime().compareTo(o1.getBeginTime());
            }
        });
		
		boolean hasCurrent = false;//用以判断已过期的不同步
		for (YxPoolPeriods period: yppList) {
			YxPoolPeriods onlinePeriod = periodsOnlineDao.queryYxPoolPeriodsByID(period.getId());
			//第一次同步数据，直接添加
			if (onlinePeriod == null) {
				periodsOnlineDao.insertYxPoolPeriods(period);
            }else{
            	//先判断数据是否有更新
            	//Date upd_time = period.getUpdateTime();
    			//Date upd_time_online = onlinePeriod.getUpdateTime();
    			periodsOnlineDao.updateYxPoolPeriods(period);
			}
			if(hasCurrent){//存在当前期，旧期跳过，不同步期数门店数据
				continue;
			}
			//同步期数门店数据
			syncPeriodStroe(period.getId());
			
			if(period.getBeginTime().before(new Date())){
				hasCurrent = true;
			}
		}
	}
	
	/**
	 * 同步期数门店表数据
	 * @param periodId
	 */
	private void syncPeriodStroe(Long periodId) {
		/*复制期数关联各省份数据*/
		List<YxPoolPeriodsStore> storeList =  periodsStoreDao.queryStoreListByPeriodsId(periodId);
		YxPoolPeriodsStore queryStore = new YxPoolPeriodsStore();
		queryStore.setPeriodId(periodId);
		List<YxPoolPeriodsStore> storeOnlineList = periodsStoreOnlineDao.queryStoreList(queryStore);
		Map<Long,YxPoolPeriodsStore> onlineStoreMap = new HashMap<Long,YxPoolPeriodsStore>();
		Map<Long,YxPoolPeriodsStore> storeMap = new HashMap<Long,YxPoolPeriodsStore>();
		for(YxPoolPeriodsStore s:storeList){
			storeMap.put(s.getId(), s);
		}
		
		for(YxPoolPeriodsStore s:storeOnlineList){
			onlineStoreMap.put(s.getId(), s);
			if(storeMap.get(s.getId())==null){
				periodsStoreOnlineDao.deleteYxPoolPeriodsStore(s.getId());
			}
		}
		storeList = removeRepStore(storeList);
		String newCommodityIds = "",oldCommodityIds = "";
		List<YxPoolPeriodsStore> storeInsertList = new ArrayList<>();
		for(YxPoolPeriodsStore pv: storeList){
			if(!"".equals(pv.getCommoditys())) {
				newCommodityIds += pv.getCommoditys()+",";
			}
			YxPoolPeriodsStore pvOnline = onlineStoreMap.get(pv.getId());
			if(pvOnline!=null){
				if(!"".equals(pvOnline.getCommoditys())){
					oldCommodityIds += pvOnline.getCommoditys()+",";
				}
				if(!pv.getCommoditys().equals(pvOnline.getCommoditys())){
					periodsStoreOnlineDao.updateStore(pv);
				}
			}else{
				storeInsertList.add(pv);
			}
		}
		
		newCommodityIds = toDeleteRepeat(newCommodityIds);
		if(newCommodityIds.endsWith(",")){
			newCommodityIds = newCommodityIds.substring(0, newCommodityIds.length()-1);
		}
		
		oldCommodityIds = toDeleteRepeat(oldCommodityIds);
		if(oldCommodityIds.endsWith(",")){
			oldCommodityIds = oldCommodityIds.substring(0, oldCommodityIds.length()-1);
		}
		
		//periodsStoreOnlineDao.deleteStoreByPeriodId(periodId);//修改更新添加删除分开处理
		if(storeInsertList.size()>0){
			periodsStoreOnlineDao.batchSaveStore(storeInsertList);
		}
		
		//比较id列表数据
		String news[] = newCommodityIds.split(",");
		String olds[] = oldCommodityIds.split(",");
		String updateIds[] = getJiaoJi(news, olds);//需要更新的数据id
		String insertIds[] = getChaJi(news, updateIds);//需要新增的数据id
		String delIds[] = getChaJi(olds, updateIds);//需要删除的数据id
		String toUpdateIds = StringUtils.join(updateIds, ",");
		String toDelIds = StringUtils.join(delIds, ",");
		/*复制更新所有商品数据*/
		List<YxPoolCommodity> insertList = new ArrayList<>();
		List<YxPoolCommodity> commodityList = commodityDao.getYxPoolCommodityByIds(newCommodityIds);
		List<YxPoolCommodity> onlineCommodityList = commodityOnlineDao.getYxPoolCommodityByIds(toUpdateIds);
		Map<Long,YxPoolCommodity> onlineCommodityMap = new HashMap<Long,YxPoolCommodity>();
		for(YxPoolCommodity onlineYpc:onlineCommodityList){
			onlineCommodityMap.put(onlineYpc.getId(), onlineYpc);
		}
		for(YxPoolCommodity ypc:commodityList){
			for(String insertId: insertIds){
				if(String.valueOf(ypc.getId()).equals(insertId)){
					ypc.setCreateTime(new Date());
					insertList.add(ypc);
					break;
				}
			}
			
			if(onlineCommodityMap.get(ypc.getId())!=null){
				if(!onlineCommodityMap.get(ypc.getId()).getUpdateTime().equals(ypc.getUpdateTime())){//判断时间有更新
					commodityOnlineDao.UpdateUniteCommodity(ypc);
				}
			}else{//处理未同步成功的数据
				if(!insertList.contains(ypc)){
					insertList.add(ypc);
				}
			}
		}
		if(toDelIds.length()>0){
			commodityOnlineDao.batchDeleteYxPoolCommodity(toDelIds);//删除原有商品
		}
		if(insertList.size()>0){
			commodityOnlineDao.batchSaveUniteCommodity(insertList);//同步新商品
		}
		//commodityOnlineDao.batchUpdateUniteCommodity(updateList);//同步新商品
		syncPoolProper(newCommodityIds);  //同步池的属性
	}
	
	/**
	 * 去掉重复的门店
	 * @param list: 门店集合
	 * @return 没有重复的门店集合
	 */
	private List<YxPoolPeriodsStore> removeRepStore(List<YxPoolPeriodsStore> list) {
		List<YxPoolPeriodsStore> temp = new ArrayList<YxPoolPeriodsStore>();
		Map<String, String> m = new HashMap<String, String>();
		if (list == null) return null;
		for (YxPoolPeriodsStore ps : list) {
			if (m.get(ps.getStoreCode()) != null) {
				continue;
			}
			m.put(ps.getStoreCode(), ps.getStoreCode());
			temp.add(ps);
		}
		return temp;
	}
	
	
	private void syncPoolProper(String newCommodityIds){
		String[] ids = newCommodityIds.split(",");
		List<YxPoolProperPlus> list = properPlusDao.queryProperList(ids);
		if(null == list || list.size()==0){
			return;
		}
		for(String id : ids){
			if(StringUtils.isBlank(id)){
				continue;
			}
			Long commodityId = Long.parseLong(id);
			List<YxPoolProperPlus> listArray = new ArrayList<YxPoolProperPlus>();
			for(YxPoolProperPlus pp: list){
				if((long)pp.getCommodityId()==(long)commodityId){
					listArray.add(pp);
				}
			}
			if(0 == listArray.size()){
				continue;
			}
			YxPoolProperPlus ypp = new YxPoolProperPlus();
			ypp.setCommodityId(commodityId);
			properPlusOnlineDao.delete(ypp);
			properPlusOnlineDao.batchInsert(listArray);
		}
	}
	
	
	/**
	 * @Description 去除重复的ID
	 * @param newIdList: 逗号分隔的ID列表
	 * @return
	*/ 
	private String toDeleteRepeat(String newIdList) {
		String ids[] = newIdList.split(",");
		Map<String,String> idMap = new HashMap<String,String>();
		String newIds = "";
		for(String id: ids){
			if(idMap.get(id)==null && !"".equals(id)){
				newIds += id+",";
				idMap.put(id, id);
			}
		}
		return newIds;
	}
	
	/**
     * 求交集
     * 
     * @param m
     * @param n
     * @return
     */
	private static String[] getJiaoJi(String[] m, String[] n) {
		List<String> rs = new ArrayList<String>();

		// 将较长的数组转换为set
		Set<String> set = new HashSet<String>(
				Arrays.asList(m.length > n.length ? m : n));

		// 遍历较短的数组，实现最少循环
		for (String i : m.length > n.length ? n : m) {
			if (set.contains(i)) {
				rs.add(i);
			}
		}

		String[] arr = {};
		return rs.toArray(arr);
	}

    /**
     * 求差集
     * 
     * @param m
     * @param n
     * @return
     */
	private static String[] getChaJi(String[] m, String[] n) {
		// 将较长的数组转换为set
		Set<String> set = new HashSet<String>(Arrays.asList(m.length > n.length ? m : n));

		// 遍历较短的数组，实现最少循环
		for (String i : m.length > n.length ? n : m) {
			// 如果集合里有相同的就删掉，如果没有就将值添加到集合
			if (set.contains(i)) {
				set.remove(i);
			} else {
				set.add(i);
			}
		}

		String[] arr = {};
		return set.toArray(arr);
	}
}
