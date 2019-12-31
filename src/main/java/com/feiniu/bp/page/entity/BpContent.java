package com.feiniu.bp.page.entity;


import java.util.Date;
import java.util.Map;

import com.feiniu.yx.util.YXBaseEntity;

public class BpContent extends YXBaseEntity {
	private Long id;
	
	private Integer channelType;
	
	private String pageId;
	
	private String pageName;
	
	private Integer clientType;
	
	private String pageCol;
	
	private Integer status = 1;  //默认启用
	
	private Integer trackType;
	
	private String otherProperties;
	
	private Map<String,String> otherProMap;
	
	private String createId;
	
	private Date createTime;
	
	private String updateId;
	
	private Date updateTime;
	
	public Integer getChannelType() {
		return channelType;
	}

	public void setChannelType(Integer channelType) {
		this.channelType = channelType;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public String getPageCol() {
		return pageCol;
	}

	public void setPageCol(String pageCol) {
		this.pageCol = pageCol;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPageId() {
		return pageId;
	}

	public void setPageId(String pageId) {
		this.pageId = pageId;
	}

	public Integer getTrackType() {
		return trackType;
	}

	public void setTrackType(Integer trackType) {
		this.trackType = trackType;
	}

	public String getOtherProperties() {
		return otherProperties;
	}

	public void setOtherProperties(String otherProperties) {
		this.otherProperties = otherProperties;
	}

	
	public Map<String, String> getOtherProMap() {
		return otherProMap;
	}

	public void setOtherProMap(Map<String, String> otherProMap) {
		this.otherProMap = otherProMap;
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

	public Integer getClientType() {
		return clientType;
	}

	public void setClientType(Integer clientType) {
		this.clientType = clientType;
	}

}
