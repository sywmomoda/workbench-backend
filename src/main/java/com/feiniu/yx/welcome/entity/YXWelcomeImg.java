package com.feiniu.yx.welcome.entity;

import java.util.Date;

public class YXWelcomeImg {
	private Long id;
	
	private Long welcomeId;
	
	private String imgUrl;
	
	private String imgSize;
	
	private String btnImgUrl;
	
	private String btnCustomUrl;
	
	private String btnImgSize;
	
	private Integer index;
	
	private String createId;
	
	private Date createTime;
	
	private String updateId;
	
	private Date updateTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getWelcomeId() {
		return welcomeId;
	}

	public void setWelcomeId(Long welcomeId) {
		this.welcomeId = welcomeId;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getImgSize() {
		return imgSize;
	}

	public void setImgSize(String imgSize) {
		this.imgSize = imgSize;
	}

	public String getBtnImgUrl() {
		return btnImgUrl;
	}

	public void setBtnImgUrl(String btnImgUrl) {
		this.btnImgUrl = btnImgUrl;
	}

	public String getBtnCustomUrl() {
		return btnCustomUrl;
	}

	public void setBtnCustomUrl(String btnCustomUrl) {
		this.btnCustomUrl = btnCustomUrl;
	}

	public String getBtnImgSize() {
		return btnImgSize;
	}

	public void setBtnImgSize(String btnImgSize) {
		this.btnImgSize = btnImgSize;
	}
	
	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
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

}
