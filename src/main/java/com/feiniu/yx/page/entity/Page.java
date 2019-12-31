package com.feiniu.yx.page.entity;

import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.util.YXBaseEntity;

import java.util.Date;
import java.util.Map;


public class Page extends YXBaseEntity implements Cloneable{

	private Long id;//
	private Long templateId;//模板ID
	private String storeCode; //活动群组区域
	private String storeNames;
	private String storeGroupIds; //活动群组区域
	private String storeGroupNames;
	private String xiaoQuIds; //活动群组区域
	private String xiaoQuNames;
	private String name;//名称
	private String pageProperties;//属性
	private Integer type; //类型：1:优鲜首页，2:优鲜活动页，3:b2b活动页
	private String modules;
	private String url = "";
	private String description;
	private Integer status;
	private String mainPicUrl = "";
	private Date activityBeginTime;//开始时间
	private Date activityEndTime;//结束时间
	public Date createTime;
	private Date updateTime;
	private String createId;
	private String updateId;
	private String administrator = "";
	private Long combineId = 0L;//合并ID
	private String searchWords;
	private String searchAdPic;
	
	private YXTemplate yxTemplate;
	private Map<String, Object> pagePropertieMap;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	public String getPageProperties() {
		return pageProperties;
	}

	public void setPageProperties(String pageProperties) {
		this.pageProperties = pageProperties;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getModules() {
		return modules;
	}

	public void setModules(String modules) {
		this.modules = modules;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public String getMainPicUrl() {
		return mainPicUrl;
	}

	public void setMainPicUrl(String mainPicUrl) {
		this.mainPicUrl = mainPicUrl;
	}

	public Date getActivityBeginTime() {
		return activityBeginTime;
	}

	public void setActivityBeginTime(Date activityBt) {
		this.activityBeginTime = activityBt;
	}

	public Date getActivityEndTime() {
		return activityEndTime;
	}

	public void setActivityEndTime(Date activityEt) {
		this.activityEndTime = activityEt;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getCreateId() {
		return createId;
	}

	public void setCreateId(String createId) {
		this.createId = createId;
	}

	public String getUpdateId() {
		return updateId;
	}

	public void setUpdateId(String updateId) {
		this.updateId = updateId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAdministrator() {
		return administrator;
	}

	public void setAdministrator(String administrator) {
		this.administrator = administrator;
	}

	public Long getCombineId() {
		return combineId;
	}

	public void setCombineId(Long combineId) {
		this.combineId = combineId;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}

	public YXTemplate getYxTemplate() {
		return yxTemplate;
	}

	public void setYxTemplate(YXTemplate yxTemplate) {
		this.yxTemplate = yxTemplate;
	}

	public String getStoreGroupIds() {
		return storeGroupIds;
	}

	public void setStoreGroupIds(String storeGroupIds) {
		this.storeGroupIds = storeGroupIds;
	}

	public String getStoreNames() {
		return storeNames;
	}

	public void setStoreNames(String storeNames) {
		this.storeNames = storeNames;
	}

	public String getStoreGroupNames() {
		return storeGroupNames;
	}

	public void setStoreGroupNames(String storeGroupNames) {
		this.storeGroupNames = storeGroupNames;
	}

	public Map<String, Object> getPagePropertieMap() {
		return pagePropertieMap;
	}

	public void setPagePropertieMap(Map<String, Object> pagePropertieMap) {
		this.pagePropertieMap = pagePropertieMap;
	}

	public String getSearchWords() {
		return searchWords;
	}

	public void setSearchWords(String searchWords) {
		this.searchWords = searchWords;
	}

	public String getSearchAdPic() {
		return searchAdPic;
	}

	public void setSearchAdPic(String searchAdPic) {
		this.searchAdPic = searchAdPic;
	}

	public String getXiaoQuIds() {
		return xiaoQuIds;
	}

	public void setXiaoQuIds(String xiaoQuIds) {
		this.xiaoQuIds = xiaoQuIds;
	}

	public String getXiaoQuNames() {
		return xiaoQuNames;
	}

	public void setXiaoQuNames(String xiaoQuNames) {
		this.xiaoQuNames = xiaoQuNames;
	}

	@Override
	public Page clone() {
		Page page = null;
		
		try {
			page =  (Page) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return page;
	}

}
