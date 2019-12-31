package com.feiniu.yx.core.poolgc.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.yx.core.dao.PoolGCDao;
import com.feiniu.yx.core.poolgc.PoolGCService;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;

/**
 * 池清理，回收空池和过期池期数
 * @author tongwenhuan
 * @date 2019年3月4日
 */
@Service
public class PoolGCServiceImpl implements PoolGCService {
	
	public static Logger logger = Logger.getLogger(PoolGCServiceImpl.class);
	
	private static final int delNum = 10;
	
	@Autowired
	private PoolGCDao poolGCDao;
	
	@Override
	public void doGC() {
		List<YxPool> all = poolGCDao.getAllPool();
		
		int del_count = 1;
		for (int i = 11; i < all.size(); i++) {
			PoolGC pgc = new PoolGC(all.get(i));
			if (del_count > delNum) {
				break;
			}
			if (pgc.gc()) {
				del_count++;
			}
			pgc = null;
		}
	}
	
	/**
	 * 池回收类
	 * 回收范围：
	 * 1.超过deltime未更新的空池（没有商品）
	 * 2.超过deltime未更新的过期池期数
	 * @author tongwenhuan
	 * @date 2019年3月12日
	 */
	private class PoolGC {
		
		private YxPool pool;
		private YxPool onl_pool;
		//池期数
		private List<YxPoolPeriods> periodsList;
		private List<YxPoolPeriods> onl_periodsList;
		//池期数门店关系
		private List<YxPoolPeriodsStore> ppsList;
		private List<YxPoolPeriodsStore> onl_ppsList;
		//池商品id
		private Set<String> cidSet;
		private Set<String> onl_cidSet;
	
		private List<YxPoolPeriods> delList = null;
		private List<YxPoolPeriods> onl_delList = null; 
		
		//清除的时间阀值，超过90天未更新的池或期数
		private Long pool_del_time = new Date().getTime() - 90*24*60*60*1000L;
		private Long periods_del_time = new Date().getTime() - 15*24*60*60*1000L;
		
		public PoolGC(YxPool pool) {
			this.pool = pool;
			initData(pool.getId());
		}
		
		//池垃圾回收
		public boolean gc() {
			if (onl_pool == null || periodsList == null || onl_periodsList == null) {
				return false;
			}
			//只有1期，进行空池回收
			if (periodsList.size() == 1 && onl_periodsList.size() == 1) {
				return gcPool();
			}
			//多期，进行过期池期数回收
			if (periodsList.size() > 1 && onl_periodsList.size() > 1) {
				return gcPeriods();
			}
			
			return false;
		}
		
		private void initData(Long id) {
			onl_pool = poolGCDao.getOnlPoolById(id);
			periodsList = poolGCDao.getPeriodsListByPoolId(id);
			onl_periodsList = poolGCDao.getOnlPeriodsListByPoolId(id);
		}
		
		/**
		 * 空池回收
		 * @return
		 */
		private boolean gcPool() {
			//池的基础信息90天未更新
			if (!checkPoolTime()) {
				return false;
			}
			//池中没有数据
			if(!checkPoolPeriodsStore(ppsList) || !checkPoolPeriodsStore(onl_ppsList)) {
				return false;
			}
			//池期数15天未更新
			if (!checkPeriodsTime(periodsList.get(0))) {
				return false;
			}
			if (!checkPeriodsTime(onl_periodsList.get(0))) {
				return false;
			}
			
			//删除池期数与门店关系
			ppsList = poolGCDao.getPoolPeriodsStoreListByperiodsId(periodsList.get(0).getId());
			onl_ppsList = poolGCDao.getOnlPoolPeriodsStoreListByperiodsId(onl_periodsList.get(0).getId());
			if (ppsList != null) {
				for (YxPoolPeriodsStore ps : ppsList) {
					poolGCDao.delPoolPeriodsStore(ps.getId());
				}
			}
			
			if (onl_ppsList != null) {
				for (YxPoolPeriodsStore ps : onl_ppsList) {
					poolGCDao.delOnlPoolPeriodsStore(ps.getId());
				}
			}
			//删除池期数
			poolGCDao.delPoolPeriods(periodsList.get(0).getId());
			poolGCDao.delOnlPoolPeriods(onl_periodsList.get(0).getId());
			//删除池
			poolGCDao.delPool(pool.getId());
			poolGCDao.delOnlPool(onl_pool.getId());
			logger.error("POOLGC:del pool," + pool.getId());
			return true;
		}
		
		/**
		 * 池期数回收
		 * @return
		 */
		private boolean gcPeriods() {
			initDelPoolPeriods();
			if (delList.size() == 0 || onl_delList.size() == 0) {
				return false;
			}
			initDelPeriodsStoreAndCids();
			//删商品和附近属性
			for (String id : cidSet) {
				poolGCDao.delPoolCommodity(id);
				poolGCDao.delYxPoolProperPlus(id);
			}
			for (String id : onl_cidSet) {
				poolGCDao.delOnlPoolCommodity(id);
				poolGCDao.delOnlYxPoolProperPlus(id);
			}
			//删除池门店关系
			for (YxPoolPeriodsStore ps : ppsList) {
				poolGCDao.delPoolPeriodsStore(ps.getId());
			}
			for (YxPoolPeriodsStore ps : onl_ppsList) {
				poolGCDao.delOnlPoolPeriodsStore(ps.getId());
			}
			//删池期数
			for (YxPoolPeriods pp : delList) {
				poolGCDao.delPoolPeriods(pp.getId());
			}
			for (YxPoolPeriods pp : onl_delList) {
				poolGCDao.delOnlPoolPeriods(pp.getId());
			}
			logger.error("POOLGC:del periods," + pool.getId());
			return true;
		}
		
		private void initDelPoolPeriods() {
			//重排序，从晚到早进行排序
			sort();
			delList = getDelPeriodsList(periodsList);
			onl_delList = getDelPeriodsList(onl_periodsList);
		}
		
		//收集过期期数 
		private List<YxPoolPeriods> getDelPeriodsList(List<YxPoolPeriods> list) {
			List<YxPoolPeriods> delList = new ArrayList<YxPoolPeriods>();
			Date t = new Date();
			boolean delPeriods = false;
			for (int i  = 0; i < list.size(); i++) {
				YxPoolPeriods pp = list.get(i);
				if (delPeriods) {
					//删除15未做更改的期数
					if (checkPeriodsTime(pp)) {
						delList.add(pp);
					}
				} else {
					//如果期数早于当前时间，标记为过期可以删除
					if (pp.getBeginTime().before(t)) {
						delPeriods = true;
					}
				}
			}
			return delList;
		}
		
		private void initDelPeriodsStoreAndCids() {
			cidSet = new HashSet<>();
			onl_cidSet = new HashSet<>();
			ppsList = new ArrayList<YxPoolPeriodsStore>();
			onl_ppsList = new ArrayList<YxPoolPeriodsStore>();
			for (YxPoolPeriods pp : delList) {
				List<YxPoolPeriodsStore> pps_temp = poolGCDao.getPoolPeriodsStoreListByperiodsId(pp.getId());
				for (YxPoolPeriodsStore pps : pps_temp) {
					ppsList.add(pps);
					String cids = pps.getCommoditys();
					if (StringUtils.isBlank(cids)) {
						continue;
					}
					String[] cs = cids.split(",");
					for (String id : cs) {
						if (StringUtils.isBlank(id)) {
							continue;
						}
						cidSet.add(id);
					}
				}
			}
			for (YxPoolPeriods pp : onl_delList) {
				List<YxPoolPeriodsStore> pps_temp = poolGCDao.getOnlPoolPeriodsStoreListByperiodsId(pp.getId());
				for (YxPoolPeriodsStore pps : pps_temp) {
					onl_ppsList.add(pps);
					String cids = pps.getCommoditys();
					if (StringUtils.isBlank(cids)) {
						continue;
					}
					String[] cs = cids.split(",");
					for (String id : cs) {
						if (StringUtils.isBlank(id)) {
							continue;
						}
						onl_cidSet.add(id);
					}
				}
			}
		}
		
		/**
		 * 更新时间早于当前时间90天
		 * 判断池是否满足回收时间
		 * @return
		 */
		private boolean checkPoolTime() {
			return (pool.getCreateTime().getTime() < pool_del_time) 
					&& (pool.getUpdateTime().getTime() < pool_del_time)
					&& (onl_pool.getCreateTime().getTime() < pool_del_time) 
					&& (onl_pool.getUpdateTime().getTime() < pool_del_time);
		}
		
		
		/**
		 * 池期数更新时间早于当前时间15天
		 * @return
		 */
		private boolean checkPeriodsTime(YxPoolPeriods pp) {
			return (pp.getCreateTime().getTime() < periods_del_time) && (pp.getUpdateTime().getTime() < periods_del_time);
		}
		
		/**
		 * 检查池是否为空池
		 * @return
		 */
		private boolean checkPoolPeriodsStore(List<YxPoolPeriodsStore> ppsList) {
			boolean comms = true;
			for (YxPoolPeriodsStore ps : ppsList) {
				if (StringUtils.isNotBlank(ps.getCommoditys())) {
					comms = false;
					break;
				}
			}
			return comms;
		}
		
		/**
		 * 按照开始时间从晚到早排序
		 */
		private void sort() {
			//将池期数按照时间从早到晚排序
			Collections.sort(periodsList, new Comparator<YxPoolPeriods>() {
			
				@Override
				public int compare(YxPoolPeriods o1, YxPoolPeriods o2) {
					if (o1.getBeginTime().after(o2.getBeginTime())) {
						return -1;
					} else {
						return 1;
					}
				}
			});
			
			Collections.sort(onl_periodsList, new Comparator<YxPoolPeriods>() {
				
				@Override
				public int compare(YxPoolPeriods o1, YxPoolPeriods o2) {
					if (o1.getBeginTime().after(o2.getBeginTime())) {
						return -1;
					} else {
						return 1;
					}
				}
			});
		}
	}
	
//	public static void main(String[] args) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		
//		List<YxPoolPeriods> list =  new ArrayList<>();
//		Date t = new Date();
//		Date b1 = new Date(t.getTime() - 2*24*60*60*1000L);
//		Date b2 = new Date(t.getTime() - 24*60*60*1000L);
//		Date b3 = new Date(t.getTime() + 2*24*60*60*1000L);
//		
//		YxPoolPeriods p1 = new YxPoolPeriods();
//		YxPoolPeriods p2 = new YxPoolPeriods();
//		YxPoolPeriods p3 = new YxPoolPeriods();
//		
//		p1.setId(1l);
//		p2.setId(2l);
//		p3.setId(3l);
//		
//		p1.setBeginTime(b1);
//		p2.setBeginTime(b2);
//		p3.setBeginTime(b3);
//		
//		list.add(p1);
//		list.add(p2);
//		list.add(p3);
//		
//		for (YxPoolPeriods pp : list) {
//			System.out.println(pp.getId() + " " + sdf.format(pp.getBeginTime()));
//		}
//		
//		Collections.sort(list, new Comparator<YxPoolPeriods>() {
//			
//			@Override
//			public int compare(YxPoolPeriods o1, YxPoolPeriods o2) {
//				if (o1.getBeginTime().after(o2.getBeginTime())) {
//					return -1;
//				} else {
//					return 1;
//				}
//			}
//		});
//		
//		System.out.println("");
//		
//		for (YxPoolPeriods pp : list) {
//			System.out.println(pp.getId() + " " + sdf.format(pp.getBeginTime()));
//		}
//		
//		boolean delPeriods = false;
//		for (int i  = 0; i < list.size(); i++) {
//			YxPoolPeriods pp = list.get(i);
//			if (delPeriods) {
//				System.out.println("del " + pp.getId());
//			} else {
//				//如果期数大于当前时间，退出循环
//				if (pp.getBeginTime().before(t)) {
//					delPeriods = true;
//				}
//			}
//		}
//	}

}
