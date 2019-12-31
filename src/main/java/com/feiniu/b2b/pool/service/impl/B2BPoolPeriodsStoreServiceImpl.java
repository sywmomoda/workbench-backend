package com.feiniu.b2b.pool.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.b2b.pool.service.B2BPoolPeriodsStoreService;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreDao;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;

@Service
public class B2BPoolPeriodsStoreServiceImpl implements B2BPoolPeriodsStoreService {

	@Autowired
	private YxPoolPeriodsStoreDao storeDao;
	
	@Autowired
	private B2BStoreService storeService;
	
	/**
	 * 通过期数id和门店codes查询门店期数表
	 */
	public List<YxPoolPeriodsStore> listPoolPeriodsStoresByPeriodsIdAndStoreCodes(Long id,String storeCodes) {
		List<YxPoolPeriodsStore> returnlist = new ArrayList<YxPoolPeriodsStore>();
		if (StringUtils.isBlank(storeCodes)) {
			return returnlist;
		}
		List<YxPoolPeriodsStore> all =  storeDao.queryStoreListByPeriodsId(id);
		String eq_stores = "," + storeCodes + ",";
		for (YxPoolPeriodsStore pps : all) {
			String sc = "," + pps.getStoreCode() + ",";
			if (eq_stores.indexOf(sc) > -1) {
				returnlist.add(pps);
			}
		}
		return returnlist;
	}

    @Override
    public List<B2BStore> selectB2BStoreByNameOrCode(B2BStore store) {
        return storeDao.selectB2BStoreByNameOrCode(store);
    }

    public List<YxPoolPeriodsStore> getPoolPeriodsStoresByPeriodsId(Long id,String storeCode){
		YxPoolPeriodsStore  store  = new YxPoolPeriodsStore();
		store.setFirstRow(1);
		store.setPageRows(Integer.MAX_VALUE);
		store.setPeriodId(id);
		store.setStoreCode(storeCode);
		List<YxPoolPeriodsStore> list = storeDao.queryStoreList(store);
		Map<String, B2BStore> storeMap = storeService.getB2BStoreMap();
		List<YxPoolPeriodsStore> returnlist = new ArrayList<YxPoolPeriodsStore>();
		for(YxPoolPeriodsStore c : list)
		{
			if (null != c.getCommoditys() && !"".equals(c.getCommoditys())) {
				c.setCountCommodity((c.getCommoditys().split(",").length));
			}
			B2BStore store1=storeMap.get(c.getStoreCode());
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
	 * @param province
	 * @return
	 */
	public List<YxPoolPeriodsStore> queryStoreListByPeriodsId(Long  periodsId){
		List<YxPoolPeriodsStore> list = storeDao.queryStoreListByPeriodsId(periodsId);
		List<YxPoolPeriodsStore> ret = null;
		if(list!=null){
			ret = new ArrayList<>();
			Map<String, B2BStore> storeMap =storeService.getB2BStoreMap();
			for(YxPoolPeriodsStore ppStore : list){
				String code = ppStore.getStoreCode();
				if(storeMap!=null && storeMap.get(code)!=null){
					ppStore.setStoreName(storeMap.get(code).getName());
					ret.add(ppStore);
				}
			}
		}
		return ret;
	}	
	
	/**
	 * 通过期数id and 地区编码 查询
	 * @author lizhiyong
	 * 2017年2月16日
	 * @param periodsId
	 * @param provinceSeq
	 * @return
	 */
	public YxPoolPeriodsStore queryB2BPoolPeriodsStoreByCode(Long  periodsId,String storeCode){
		return storeDao.queryYxPoolPeriodsStoreByCode(periodsId, storeCode);
	}
	
	/**
	 * 
	 * @param periodProvice
	 */
	public void updateCommoditys(YxPoolPeriodsStore periodStore) {
		storeDao.updateCommoditys(periodStore);
	}


}
