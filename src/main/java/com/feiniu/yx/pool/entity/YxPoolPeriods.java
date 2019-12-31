package com.feiniu.yx.pool.entity;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.feiniu.yx.util.YXBaseEntity;

public class YxPoolPeriods extends YXBaseEntity {
	
	private Long id;
	
	private Long poolId =0L;
	
	private Integer number = 0;
	
	private String name = "" ;

	@JSONField (format="yyyy-MM-dd HH:mm:ss")  
	private Date beginTime;
	@JSONField (format="yyyy-MM-dd HH:mm:ss")  
	private Date endTime;
	
	private Date updateTime;
	
	private String updateId = "";
	
	private String createId="";
	
	private Date createTime;
	
	private int status;
	
	private int countGoods;
	
	private String goods = "";

	private List<YxPoolPeriodsStore> storeList;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Long getPoolId() {
		return poolId;
	}

	public void setPoolId(Long poolId) {
		this.poolId = poolId;
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
	
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	public String getUpdateId() {
		return updateId;
	}

	public void setUpdateId(String updateId) {
		this.updateId = updateId;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getCountGoods() {
		return countGoods;
	}

	public void setCountGoods(int countGoods) {
		this.countGoods = countGoods;
	}

	public List<YxPoolPeriodsStore> getStoreList() {
		return storeList;
	}

	public void setStoreList(List<YxPoolPeriodsStore> storeList) {
		this.storeList = storeList;
	}

	public String getGoods() {
		return goods;
	}

	public void setGoods(String goods) {
		this.goods = goods;
	}
}
