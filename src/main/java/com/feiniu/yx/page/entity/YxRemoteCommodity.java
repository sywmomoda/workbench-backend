package com.feiniu.yx.page.entity;

import java.util.Date;
import java.util.Map;

import com.feiniu.yx.util.YXBaseEntity;

public class YxRemoteCommodity extends YXBaseEntity {

	private Long id;
	
	private String type;
	
	private String commodityId = "";
	// 商品名称
	private String title = "";
	// 商品图片地址
	private String picUrl = "";
	// 促销语
	private String promoteText = "";
	
	private Long stockSum= 0L;

	private Integer priceType= 0;

	private Date updateTime;

	private String updateId = "";
	
	private Date createTime;

	private String createId = "";
	
	private String category;
	
	private String storeCode ="";//添加商品的时候存放商品的销售门店
	
	private Date addOnDate;//上新时间
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getCommodityId() {
		return commodityId;
	}

	public void setCommodityId(String commodityId) {
		this.commodityId = commodityId;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public String getPromoteText() {
		return promoteText;
	}
	
	public void setPromoteText(String promoteText) {
		this.promoteText = promoteText;
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
			
	public String getCreateId() {
		return createId;
	}

	public void setCreateId(String createId) {
		this.createId = createId;
	}

	public void setUpdateId(String updateId) {
		this.updateId = updateId;
	}

	public Long getStockSum() {
		return stockSum;
	}

	public void setStockSum(Long stockSum) {
		this.stockSum = stockSum;
	}


	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getPriceType() {
		return priceType;
	}

	public void setPriceType(Integer priceType) {
		this.priceType = priceType;
	}

	public Date getAddOnDate() {
		return addOnDate;
	}

	public void setAddOnDate(Date addOnDate) {
		this.addOnDate = addOnDate;
	}
	
	

}
