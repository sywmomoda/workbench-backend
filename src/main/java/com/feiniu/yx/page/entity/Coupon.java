
package com.feiniu.yx.page.entity;


import java.util.ArrayList;
import java.util.List;

public class Coupon{
	
	protected List<String> couponId = new ArrayList<String>();
	protected List<String> couponIds = new ArrayList<String>();
	protected List<String> couponName = new ArrayList<String>();
	protected List<String> couponDescribe = new ArrayList<String>();
	protected List<String> couponNames = new ArrayList<String>();
	protected List<String> couponDescribes = new ArrayList<String>();
	protected List<String> couponType = new ArrayList<String>();
	protected List<String> discountType = new ArrayList<String>();
	protected List<String> totalMaxCount = new ArrayList<String>();
	protected List<String> dayMaxCount = new ArrayList<String>();
	protected List<String> bgcolor = new ArrayList<String>();
	protected List<String> doorsill = new ArrayList<String>();
	protected List<String> description = new ArrayList<String>();
	protected List<String> date = new ArrayList<String>();
	protected List<String> value = new ArrayList<String>();
	protected List<String> title = new ArrayList<String>();
	protected List<String> beginTime = new ArrayList<String>();
	protected List<String> endTime = new ArrayList<String>();
	protected List<String> isAction = new ArrayList<String>();
	protected List<String> storeCodes = new ArrayList<String>();
	protected List<String> conponTypeName = new ArrayList<String>();
	
	public Coupon() {
		
	}
	
	public Coupon(int i){
		this.setBgcolor(null);
		this.setCouponDescribe(null);
		this.setCouponId(null);
		this.setCouponName(null);
		this.setCouponType(null);
		this.setValue(null);
		this.setDate(null);
		this.setDayMaxCount(null);
		this.setDescription(null);
		this.setDiscountType(null);
		this.setDoorsill(null);
		this.setTotalMaxCount(null);
	}
			;
	public List<String> getCouponId() {
		return couponId;
	}
	public void setCouponId(List<String> couponId) {
		this.couponId = couponId;
	}
	public List<String> getStoreCodes() {
		return storeCodes;
	}

	public void setStoreCodes(List<String> storeCodes) {
		this.storeCodes = storeCodes;
	}

	public List<String> getTitle() {
		return title;
	}
	public void setTitle(List<String> title) {
		this.title = title;
	}
	public List<String> getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(List<String> beginTime) {
		this.beginTime = beginTime;
	}
	public List<String> getEndTime() {
		return endTime;
	}
	public void setEndTime(List<String> endTime) {
		this.endTime = endTime;
	}
	public List<String> getIsAction() {
		return isAction;
	}
	public void setIsAction(List<String> isAction) {
		this.isAction = isAction;
	}
	public List<String> getCouponName() {
		return couponName;
	}
	public void setCouponName(List<String> couponName) {
		this.couponName = couponName;
	}
	public List<String> getCouponDescribe() {
		return couponDescribe;
	}
	public void setCouponDescribe(List<String> couponDescribe) {
		this.couponDescribe = couponDescribe;
	}
	public List<String> getCouponType() {
		return couponType;
	}
	public void setCouponType(List<String> couponType) {
		this.couponType = couponType;
	}
	public List<String> getDiscountType() {
		return discountType;
	}
	public void setDiscountType(List<String> discountType) {
		this.discountType = discountType;
	}
	public List<String> getTotalMaxCount() {
		return totalMaxCount;
	}
	public void setTotalMaxCount(List<String> totalMaxCount) {
		this.totalMaxCount = totalMaxCount;
	}
	public List<String> getDayMaxCount() {
		return dayMaxCount;
	}
	public List<String> getCouponNames() {
		return couponNames;
	}
	public void setCouponNames(List<String> couponNames) {
		this.couponNames = couponNames;
	}
	public List<String> getCouponDescribes() {
		return couponDescribes;
	}
	public void setCouponDescribes(List<String> couponDescribes) {
		this.couponDescribes = couponDescribes;
	}
	public List<String> getConponTypeName() {
		return conponTypeName;
	}

	public void setConponTypeName(List<String> conponTypeName) {
		this.conponTypeName = conponTypeName;
	}

	public List<String> getCouponIds() {
		return couponIds;
	}
	public void setCouponIds(List<String> couponIds) {
		this.couponIds = couponIds;
	}
	public void setDayMaxCount(List<String> dayMaxCount) {
		this.dayMaxCount = dayMaxCount;
	}
	public List<String> getBgcolor() {
		return bgcolor;
	}
	public void setBgcolor(List<String> bgcolor) {
		this.bgcolor = bgcolor;
	}
	public List<String> getDoorsill() {
		return doorsill;
	}
	public void setDoorsill(List<String> doorsill) {
		this.doorsill = doorsill;
	}
	public List<String> getDescription() {
		return description;
	}
	public void setDescription(List<String> description) {
		this.description = description;
	}
	public List<String> getDate() {
		return date;
	}
	public void setDate(List<String> date) {
		this.date = date;
	}
	public List<String> getValue() {
		return value;
	}
	public void setValue(List<String> value) {
		this.value = value;
	}
}
