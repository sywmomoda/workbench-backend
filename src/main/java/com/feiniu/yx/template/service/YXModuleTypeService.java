package com.feiniu.yx.template.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.feiniu.yx.template.entity.YXModuleType;

@Service
public interface YXModuleTypeService {
	
	public void insertYXModuleType(YXModuleType module);
	public List<YXModuleType> getYXModuleTypeList();
	public List<YXModuleType> getYXModuleTypeList(YXModuleType module);
	public YXModuleType getYXModuleTypeById(Long Id);
	public void updateYXModuleType(YXModuleType module);
	
	public Map<Long, YXModuleType> getModuleTypeMapByIds(String mtids);
	public List<YXModuleType> getModuleTypeListByIds(String modules);
}
