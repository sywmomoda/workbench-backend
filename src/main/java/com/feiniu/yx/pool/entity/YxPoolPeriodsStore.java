package com.feiniu.yx.pool.entity;

import java.util.Date;

import com.feiniu.yx.util.YXBaseEntity;

public class YxPoolPeriodsStore extends YXBaseEntity {

	private Long id;
	
	private Long periodId = 0L;
		
	private String storeCode ="";
	
	private String storeName;
	
	private String groupId;
	
	private String commoditys ="";
	
	private int countCommodity;
	
	private Date createTime;

	private Date updateTime;

	private String createId = "";

	private String updateId = "";
		
	private int unExistCount;
	
	private int commodityNum;
	private int picNum;
	private int textNum;

	public int getUnExistCount() {
		return unExistCount;
	}

	public void setUnExistCount(int unExistCount) {
		this.unExistCount = unExistCount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPeriodId() {
		return periodId;
	}

	public void setPeriodId(Long periodId) {
		this.periodId = periodId;
	}

	public String getStoreCode() {
		return storeCode;
	}


	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}

	public String getCommoditys() {
		return commoditys;
	}

	public void setCommoditys(String commoditys) {
		this.commoditys = commoditys;
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

	public int getCountCommodity() {
		return countCommodity;
	}

	public void setCountCommodity(int countCommodity) {
		this.countCommodity = countCommodity;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public int getCommodityNum() {
		return commodityNum;
	}

	public void setCommodityNum(int commodityNum) {
		this.commodityNum = commodityNum;
	}

	public int getPicNum() {
		return picNum;
	}

	public void setPicNum(int picNum) {
		this.picNum = picNum;
	}

	public int getTextNum() {
		return textNum;
	}

	public void setTextNum(int textNum) {
		this.textNum = textNum;
	}
	
}
