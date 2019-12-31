package com.feiniu.yx.common.entity;

public class PromoPage {
	private String activityId;
	private String title;
	private int activityChannel = 1;
	private Integer[] activityTypes;
	private Long pageNo = 1L;
	private Long pageSize = 10L;

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getActivityChannel() {
		return activityChannel;
	}

	public void setActivityChannel(int activityChannel) {
		this.activityChannel = activityChannel;
	}

	public Integer[] getActivityTypes() {
		return activityTypes;
	}

	public void setActivityTypes(Integer[] activityTypes) {
		this.activityTypes = activityTypes;
	}

	public Long getPageNo() {
		return pageNo;
	}

	public void setPageNo(Long pageNo) {
		this.pageNo = pageNo;
	}

	public Long getPageSize() {
		return pageSize;
	}

	public void setPageSize(Long pageSize) {
		this.pageSize = pageSize;
	}

}
