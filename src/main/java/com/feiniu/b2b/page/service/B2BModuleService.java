package com.feiniu.b2b.page.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.Module;

/**
 * @author zhangdunyao
 *
 */
@Service
public interface B2BModuleService {
	
	/**
	 * 模块编辑数据
	 * @author tongwenhuan
	 * 2017年2月24日
	 * @param id
	 * @return
	 */
	JSONObject findModule(Long id);
	
	
	public Map<String, Module> getCMSModuleMapByModuleIds(String modules);
	
	/**
	 * 取所有module
	 * @author tongwenhuan
	 * 2016年4月19日
	 * @param modules
	 * @return
	 */
	public List<Module> getCMSModuleArrayByModuleIds(String modules);
	
	/**
	 * 添加模块
	 * @param auth
	 * @param param
	 * @return
	 */
	public String addModule(Map<String, Object> param);
    

	/**
	 * 更新组件 moduleProperties
	 * @author tongwenhuan
	 * 2016年4月15日
	 * @param module
	 */
	public void updateModule(Module module);
	
	public Long insertModule(Module m);


	/**
	 * 更新组件权限
	 * @param module
	 */
	void updateModulePermission(Module module);
	
	public boolean validateModule(Module module);

}
