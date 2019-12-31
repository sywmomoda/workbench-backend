package com.feiniu.yx.share.entiry;

import java.util.Date;
import com.feiniu.yx.util.YXBaseEntity;

public class YXCouponShare extends YXBaseEntity {

	private Long id;
	
	//编码
	private String code;
	
	private String name;
	
	private String title;
	
	private Integer status;
	
	private String frontUrl;
	
	private String storeName;
	
	private Date searchBeginTime1;  //查询开始时间
	
	private Date searchBeginTime2;  //查询开始时间
	
	private Date beginTime;
	
	private Date endTime;
	
	private String storeCode;
	
	private String pageProperties;
	
	private String couponProperties;
	
	private String ruleProperties;
	
	private String createId;
	
	private Date createTime;
	
	private String updateId;
	
	private Date updateTime;
	
	private Integer type; //1：普通活动,2:外卖会员活动
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getFrontUrl() {
		return frontUrl;
	}

	public void setFrontUrl(String frontUrl) {
		this.frontUrl = frontUrl;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public Date getSearchBeginTime1() {
		return searchBeginTime1;
	}

	public void setSearchBeginTime1(Date searchBeginTime1) {
		this.searchBeginTime1 = searchBeginTime1;
	}

	public Date getSearchBeginTime2() {
		return searchBeginTime2;
	}

	public void setSearchBeginTime2(Date searchBeginTime2) {
		this.searchBeginTime2 = searchBeginTime2;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}
	
	public String getPageProperties() {
		return pageProperties;
	}

	public void setPageProperties(String pageProperties) {
		this.pageProperties = pageProperties;
	}

	public String getCouponProperties() {
		return couponProperties;
	}

	public void setCouponProperties(String couponProperties) {
		this.couponProperties = couponProperties;
	}

	public String getRuleProperties() {
		return ruleProperties;
	}

	public void setRuleProperties(String ruleProperties) {
		this.ruleProperties = ruleProperties;
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

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}
