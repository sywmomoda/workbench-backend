
package com.feiniu.yx.page.entity;

import java.util.Date;
import java.util.Map;

import com.feiniu.yx.util.YXBaseEntity;

public class ModuleProperPlus extends YXBaseEntity{

	protected Long id;
	protected Long moduleId;
	protected String storeCode;
	protected String moduleProper = "";
	protected String createId = "";
	protected Date createTime;
	protected String updateId = "";
	protected Date updateTime;
	protected Map<String, Object> modulePropertieMap;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCreateId() {
		return createId;
	}

	public void setCreateId(String createId) {
		this.createId = createId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUpdateId() {
		return updateId;
	}

	public void setUpdateId(String updateId) {
		this.updateId = updateId;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}


	public Map<String, Object> getModulePropertieMap() {
		return modulePropertieMap;
	}

	public void setModulePropertieMap(Map<String, Object> modulePropertieMap) {
		this.modulePropertieMap = modulePropertieMap;
	}

	public Long getModuleId() {
		return moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}

	public String getModuleProper() {
		return moduleProper;
	}

	public void setModuleProper(String moduleProper) {
		this.moduleProper = moduleProper;
	}
	
	public ModuleProperPlus cloneObject(ModuleProperPlus object) {
		ModuleProperPlus plus = new ModuleProperPlus();
		plus.setId(object.getId());
		plus.setModuleId(object.getModuleId());
		plus.setModuleProper(object.getModuleProper());
		plus.setStoreCode(object.getStoreCode());
		return plus;
	}
	
}
