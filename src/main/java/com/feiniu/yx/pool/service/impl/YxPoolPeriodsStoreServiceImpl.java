package com.feiniu.yx.pool.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreDao;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.pool.service.YxPoolPeriodsStoreService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.service.YXStoreService;

@Service
public class YxPoolPeriodsStoreServiceImpl implements YxPoolPeriodsStoreService {

	@Autowired
	private YxPoolPeriodsStoreDao storeDao;
	
	@Autowired
	private YXStoreService storeService;
	
	/**
	 * 通过期数id和门店codes查询门店期数表
	 */
	public List<YxPoolPeriodsStore> listPoolPeriodsStoresByPeriodsIdAndStoreCodes(Long id,String storeCodes) {
		return listPeriodsStoreByStoreCodes2PeriodsId(id, storeCodes);
	}
	
	public List<YxPoolPeriodsStore> listPeriodsStoreByStoreCodes2PeriodsId(Long periodsId,String codes) {
		if (periodsId == null || StringUtils.isBlank(codes)) {
			return new ArrayList<YxPoolPeriodsStore>();
		}
		return storeDao.listPeriodsStoreByStoreCodes2PeriodsId(periodsId, codes);
	}
	
	public List<YxPoolPeriodsStore> getPoolPeriodsStoresByPeriodsId(Long id,String storeCode){
		YxPoolPeriodsStore  store  = new YxPoolPeriodsStore();
		store.setFirstRow(1);
		store.setPageRows(Integer.MAX_VALUE);
		store.setPeriodId(id);
		store.setStoreCode(storeCode);
		List<YxPoolPeriodsStore> list = storeDao.queryStoreList(store);
		Map<String, YXStore> storeMap = storeService.getYXStoreMap();
		List<YxPoolPeriodsStore> returnlist = new ArrayList<YxPoolPeriodsStore>();
		for(YxPoolPeriodsStore c : list)
		{
			if (null != c.getCommoditys() && !"".equals(c.getCommoditys())) {
				c.setCountCommodity((c.getCommoditys().split(",").length));
			}
			YXStore store1=storeMap.get(c.getStoreCode());
			if(store1!=null){
				c.setStoreName(store1.getName());
				returnlist.add(c);
			}		
		}
		return returnlist;
	}
	
	public List<YxPoolPeriodsStore> queryStoreList(YxPoolPeriodsStore store){
		return  storeDao.queryStoreList(store);
	}	
	
	/**
	 * 查询期数下所有门店
	 * @author lizhiyong
	 * 2017年2月16日
	 * @return
	 */
	public List<YxPoolPeriodsStore> queryStoreListByPeriodsId(Long  periodsId){
		List<YxPoolPeriodsStore> list = storeDao.queryStoreListByPeriodsId(periodsId);
		List<YxPoolPeriodsStore> ret = null;
		
		if(null == list){
			return null;
		}
		ret = new ArrayList<>();
		Map<String, YXStore> storeMap =storeService.getYXStoreMap();
		if(null == storeMap){
			return list;
		}
		for(YxPoolPeriodsStore ppStore : list){
			String code = ppStore.getStoreCode();
			YXStore store = storeMap.get(code);
			if(null == store){
				continue;
			}
			ret.add(ppStore);
			ppStore.setStoreName(store.getName());
		}
		return ret;
	}	
	
	/**
	 * 通过期数id and 地区编码 查询
	 * @author lizhiyong
	 * 2017年2月16日
	 * @param periodsId
	 * @return
	 */
	public YxPoolPeriodsStore queryYxPoolPeriodsStoreByCode(Long  periodsId,String storeCode){
		return storeDao.queryYxPoolPeriodsStoreByCode(periodsId, storeCode);
	}
	
	/**
	 * @param periodStore 门店池期数
	 */
	public void updateCommoditys(YxPoolPeriodsStore periodStore) {
		storeDao.updateCommoditys(periodStore);
	}

	@Override
	public void updateYxPoolPeriodsStoreCommoditys(YxPoolPeriodsStore periodStore) {
		storeDao.updateYxPoolPeriodsStoreCommoditys(periodStore);
	}

	/**
	 * 删除重复门店信息
	 */
	@Override
	public void deletePeriodStore(Long periodId, String storeCode) {
		if (StringUtils.isBlank(storeCode)) {
			return;
		}
		List<YxPoolPeriodsStore> list = storeDao.listYxPoolPeriodsStoreByCode(periodId, storeCode);
		
		for (int i = 0; i < list.size(); i++) {
			if (i == 0) {
				continue;
			}
			YxPoolPeriodsStore s = list.get(i);
			storeDao.deleteYxPoolPeriodsStore(s.getId());
		}
	}
	
}
