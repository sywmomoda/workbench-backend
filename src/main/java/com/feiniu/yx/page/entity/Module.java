
package com.feiniu.yx.page.entity;

import java.util.Date;
import java.util.Map;

import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.util.YXBaseEntity;

public class Module extends YXBaseEntity{

	protected Long id;
	protected String name = "";
	protected Long moduleTypeId = 0L;
	protected String moduleProperties = "";
	protected Long pageId = 0L;
	protected String createId = "";
	protected Date createTime;
	protected String updateId = "";
	protected Date updateTime;
	protected String storeCode;
	protected String storeScope="";//覆盖门店,数据为空时表示覆盖所有门店
	protected String hasSync;//是否已同步
	protected String administrator="";

	protected YXModuleType yxModuleType;
	protected Map<String, Object> modulePropertieMap;
	// 模块在页面的顺序
	protected Integer moduleCategory;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getModuleTypeId() {
		return moduleTypeId;
	}

	public void setModuleTypeId(Long moduleTypeId) {
		this.moduleTypeId = moduleTypeId;
	}

	public String getModuleProperties() {
		return moduleProperties;
	}

	public void setModuleProperties(String moduleProperties) {
		this.moduleProperties = moduleProperties;
	}

	public Long getPageId() {
		return pageId;
	}

	public void setPageId(Long pageId) {
		this.pageId = pageId;
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


	public YXModuleType getYxModuleType() {
		return yxModuleType;
	}

	public void setYxModuleType(YXModuleType yxModuleType) {
		this.yxModuleType = yxModuleType;
	}

	public Map<String, Object> getModulePropertieMap() {
		return modulePropertieMap;
	}

	public void setModulePropertieMap(Map<String, Object> modulePropertieMap) {
		this.modulePropertieMap = modulePropertieMap;
	}

	public String getAdministrator() {
		return administrator;
	}

	public void setAdministrator(String administrator) {
		this.administrator = administrator;
	}

	public Integer getModuleCategory() {
		return moduleCategory;
	}

	public void setModuleCategory(Integer moduleCategory) {
		this.moduleCategory = moduleCategory;
	}

	public String getHasSync() {
		return hasSync;
	}

	public void setHasSync(String hasSync) {
		this.hasSync = hasSync;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}

	public String getStoreScope() {
		return storeScope;
	}

	public void setStoreScope(String storeScope) {
		this.storeScope = storeScope;
	}

}
