package com.feiniu.yx.template.entity;

import java.util.Date;

import com.feiniu.yx.util.YXBaseEntity;

public class YXTemplate  extends YXBaseEntity  {

	private Long id;
	private String name; //模板名称
	private String code; //模板code
	private Integer type = 1; //模板类型，暂时没用
	private String description; //模板描述
	private String thumbnailUrl; //模板缩略图
	private String templateUrl=""; //模板路径
	private String moduleTypes; //模板关联的组件
	private String pageProperties; //初始化数据
	private String createId;
	private Date createTime;
	private String updateId;
	private Date updateTime;
	
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getTemplateUrl() {
		return templateUrl;
	}

	public void setTemplateUrl(String templateUrl) {
		this.templateUrl = templateUrl;
	}

	
	public String getPageProperties() {
		return pageProperties;
	}

	public void setPageProperties(String pageProperties) {
		this.pageProperties = pageProperties;
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

	public String getModuleTypes() {
		return moduleTypes;
	}

	public void setModuleTypes(String moduleTypes) {
		this.moduleTypes = moduleTypes;
	}

}
