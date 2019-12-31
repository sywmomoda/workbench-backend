package com.feiniu.yx.store.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class YXStoreGroup {
	private Long id;

	// 门店群组名称
	private String name;
	//父级ID
	private Long pid;
	//级别
	private int level;

	// 门店id
	private String storeId;
	
	//大区code
	private String pgSeq;

	private String createId;

	private Date createTime;

	private String updateId;

	private Date updateTime;
	
	private Map<String,YXStore> storeMap;

	private List<YXStoreGroup> groupList;
	
	//有权限的门店code
	private String preStoreCodes = ""; 

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


	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getPgSeq() {
		return pgSeq;
	}

	public void setPgSeq(String pgSeq) {
		this.pgSeq = pgSeq;
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

	public Map<String, YXStore> getStoreMap() {
		return storeMap;
	}

	public void setStoreMap(Map<String, YXStore> storeMap) {
		this.storeMap = storeMap;
	}

	public String getPreStoreCodes() {
		return preStoreCodes;
	}

	public void setPreStoreCodes(String preStoreCodes) {
		this.preStoreCodes = preStoreCodes;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public List<YXStoreGroup> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<YXStoreGroup> groupList) {
		this.groupList = groupList;
	}
}
