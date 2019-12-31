package com.feiniu.yx.common.entity;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.feiniu.yx.util.YXBaseEntity;

public class YXLog extends YXBaseEntity{
	
	private Long id;
	private String userId;
	private String trueName;
	private String clientIp;
	@JSONField (format="yyyy-MM-dd HH:mm:ss")  
	private Date operationTime;
	private String operationMsg;
	@JSONField (format="yyyy-MM-dd HH:mm:ss")  
	private Date createTime;
	@JSONField (format="yyyy-MM-dd HH:mm:ss")  
	private Date updateTime;
	private String createId;
	private String updateId;
	private Long protoId;
	private String logType;
	
	private String logBeginTime;
	private String logEndTime;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getTrueName() {
		return trueName;
	}
	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}
	public String getClientIp() {
		return clientIp;
	}
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	public Date getOperationTime() {
		return operationTime;
	}
	public void setOperationTime(Date operationTime) {
		this.operationTime = operationTime;
	}
	public String getOperationMsg() {
		return operationMsg;
	}
	public void setOperationMsg(String operationMsg) {
		this.operationMsg = operationMsg;
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
	public String getLogBeginTime() {
		return logBeginTime;
	}
	public void setLogBeginTime(String logBeginTime) {
		this.logBeginTime = logBeginTime;
	}
	public String getLogEndTime() {
		return logEndTime;
	}
	public void setLogEndTime(String logEndTime) {
		this.logEndTime = logEndTime;
	}
	public Long getProtoId() {
		return protoId;
	}
	public void setProtoId(Long protoId) {
		this.protoId = protoId;
	}
	public String getLogType() {
		return logType;
	}
	public void setLogType(String logType) {
		this.logType = logType;
	}
	
}
