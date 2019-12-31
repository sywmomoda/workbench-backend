package com.feiniu.yx.pool.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.yx.pool.dao.YxPoolProperPlusDao;
import com.feiniu.yx.pool.entity.YxPoolProperPlus;
import com.feiniu.yx.pool.service.YxPoolProperPlusService;
import com.feiniu.yx.util.UserUtil;

@Service
public class YxPoolProperPlusServiceImpl implements YxPoolProperPlusService {

	@Autowired
	private YxPoolProperPlusDao properDao;
	
	@Override
	public Long[] batchInsert(List<YxPoolProperPlus> list) {
		if(null == list){
			return new Long[0];
		}
		int len = list.size();
		Long [] ids = new Long[len];
		for(int i = 0; i < len; i++){
			YxPoolProperPlus pp = list.get(i);
			properDao.delete(pp);
		}
		String userName = UserUtil.getUserId();
		for(int j =0; j < len; j++){
			YxPoolProperPlus pp = list.get(j);
			pp.setCreateId(userName);
			pp.setUpdateId(userName);
			ids[j] = properDao.insert(pp);
		}
		return ids;
		
	}
	
	@Override
	public List<YxPoolProperPlus> queryProperList(Long commodityId) {
		List<YxPoolProperPlus> list = properDao.queryProperList(commodityId);
		list = null == list ?  new ArrayList<YxPoolProperPlus>() : list;
		return list;
	}
	
	public YxPoolProperPlus queryProperPlusSingle(YxPoolProperPlus properPlus){
		return properDao.queryProperPlusSingle(properPlus);
	}
	@Override
	public void updateProperCommodityId(String ids,Long commodityId) {
		if(null == ids){
			return;
		}
		String[] idArr = ids.split(",");
		for(int i = 0; i < idArr.length; i++){
			String id = idArr[i];
			if(StringUtils.isBlank(id)){
				continue;
			}
			YxPoolProperPlus pp = properDao.queryProperById(Long.parseLong(id));
			pp.setCommodityId(commodityId);
			properDao.updateYxPoolProperPlusCommodityId(pp);
		}
		
	}

}
