package com.feiniu.yx.pool.entity;

import java.util.List;

public class YxPicLinks {

	// Fn或者RT货号
	private String goodsNo;
	
	private String seqJson;
		
	// 省份list
	private List<?> seqList;
	//区域String
	private String storeCode;
	//区域名
	private String storeName;
	
	public String getSeqJson() {
		return seqJson;
	}

	public void setSeqJson(String seqJson) {
		this.seqJson = seqJson;
	}

	public List<?> getSeqList() {
		return seqList;
	}

	public void setSeqList(List<?> seqList) {
		this.seqList = seqList;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getGoodsNo() {
		return goodsNo;
	}

	public void setGoodsNo(String goodsNo) {
		this.goodsNo = goodsNo;
	}
}
