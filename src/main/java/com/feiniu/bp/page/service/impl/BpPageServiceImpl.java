package com.feiniu.bp.page.service.impl;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.bp.page.dao.BpPageDao;
import com.feiniu.bp.page.entity.BpPage;
import com.feiniu.bp.page.service.BpPageService;
import com.feiniu.yx.util.UserUtil;

@Service
public class BpPageServiceImpl implements BpPageService {

	@Autowired
	private BpPageDao pageDao;
	
	@Override
	public String checkBpPageByPageId(BpPage page) {
		Long id = page.getId();
		page.setId(null);
		List<BpPage> list = pageDao.getBpPageList(page);
		if(null == list){
			return "false";
		}
		if(list.size() == 0){
			return "false";
		}
		BpPage newP = list.get(0);
		if(null !=id && id.longValue() == newP.getId().longValue()){ 
			return "false";
		}
		return "true";
	}
	
	@Override
	public long insertBpPage(BpPage page) {
		String userName = UserUtil.getUserId();
		page.setCreateId(userName);
		page.setUpdateId(userName);
    	pageDao.insertBpPage(page);
    	return page.getId().longValue();
	}
	
	@Override
	public List<BpPage> getBpPageList(BpPage page) {
		return pageDao.getBpPageList(page);
	}

	@Override
	public BpPage getBpPageById(Long id) {
		return pageDao.getBpPageById(id);
	}
	
	@Override
	public long updateBpPage(BpPage page) {
		String userName = UserUtil.getUserId();
		page.setUpdateId(userName);
		pageDao.updateBpPage(page);
		return page.getId().longValue();
	}
	
}
