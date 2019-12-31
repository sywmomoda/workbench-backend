package com.feiniu.yx.store.entity;

import java.util.Date;

public class YXStore  {

	private Long id;

	// 名称
	private String name;

	// 代码
	private String code;
	
	//状态
	private int status;

	private String createId;

	private Date createTime;

	private String updateId;

	private Date updateTime;
	//是否选中,门店群组编辑用
	private int checked = 0;
	
	private String pgSeq;
	
	private String pgName;

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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCreateId() {
		return createId;
	}

	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public int getChecked() {
		return checked;
	}

	public void setChecked(int checked) {
		this.checked = checked;
	}

	public String getPgSeq() {
		return pgSeq;
	}

	public void setPgSeq(String pgSeq) {
		this.pgSeq = pgSeq;
	}

	public String getPgName() {
		return pgName;
	}

	public void setPgName(String pgName) {
		this.pgName = pgName;
	}
   
}
