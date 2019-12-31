package com.feiniu.yx.common.entity;

import java.util.Date;

import com.feiniu.yx.util.YXBaseEntity;

/**
 * @author tongwenhuan
 * 2017年3月9日 下午5:13:26
 */
public class Res extends YXBaseEntity{
	
	private Long id; 
	private Long temp; 			//关联模板id
	private String name;		//名称
	private String url;			//地址
	private String env;			//环境
	private String createId = "admin";
	private Date createTime;
	private String updateId = "admin";
	private Date updateTime;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getTemp() {
		return temp;
	}
	public void setTemp(Long temp) {
		this.temp = temp;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getEnv() {
		return env;
	}
	public void setEnv(String env) {
		this.env = env;
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
