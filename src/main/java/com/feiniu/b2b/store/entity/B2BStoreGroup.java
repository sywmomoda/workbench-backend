package com.feiniu.b2b.store.entity;

import java.util.Date;
import java.util.Map;

import com.feiniu.yx.util.YXBaseEntity;

public class B2BStoreGroup  extends YXBaseEntity{
	private Long id;

	// 门店群组名称
	private String name;
	
	// 门店群组Code
	private String pgSeq;

	// 门店id
	private String storeId;

	private String createId;

	private Date createTime;

	private String updateId;

	private Date updateTime;
	
	private Map<String,B2BStore> storeMap;

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

	public Map<String, B2BStore> getStoreMap() {
		return storeMap;
	}

	public void setStoreMap(Map<String, B2BStore> storeMap) {
		this.storeMap = storeMap;
	}

	public String getPgSeq() {
		return pgSeq;
	}

	public void setPgSeq(String pgSeq) {
		this.pgSeq = pgSeq;
	}
	
}
