package com.feiniu.yx.template.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.yx.template.dao.YXModuleTypeDao;
import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.template.service.YXModuleTypeService;

@Service
public class YXModuleTypeServiceImpl implements YXModuleTypeService {
	
	@Autowired 
	private YXModuleTypeDao moduleTypeDao;
	
	@Override
	public YXModuleType getYXModuleTypeById(Long Id) {
		return moduleTypeDao.getYXModuleTypeById(Id);
	}
	
	@Override
	public List<YXModuleType> getYXModuleTypeList() {
		return null;
	}
	
	@Override
	public List<YXModuleType> getYXModuleTypeList(YXModuleType module) {
		return moduleTypeDao.getYXModelTypeList(module);
	}
	
	@Override
	public void insertYXModuleType(YXModuleType module) {
		moduleTypeDao.insert(module);
	}
	
	@Override
	public void updateYXModuleType(YXModuleType module) {
		moduleTypeDao.upate(module);
	}
	
	public List<YXModuleType> getModuleTypeListByIds(String modules) {
		List<YXModuleType> moduleList = moduleTypeDao.queryModuleTypesByIds(modules);
		return moduleList;
	}
	
	public Map<Long, YXModuleType> getModuleTypeMapByIds(String modules) {
		List<YXModuleType> moduleList = moduleTypeDao.queryModuleTypesByIds(modules);
		Map<Long, YXModuleType> map = new HashMap<Long, YXModuleType>();
		for(YXModuleType m: moduleList){
			map.put(m.getId(), m);
		}
		return map;
	}

}
