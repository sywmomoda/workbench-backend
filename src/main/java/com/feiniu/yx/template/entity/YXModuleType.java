package com.feiniu.yx.template.entity;

import java.util.Date;

import com.feiniu.yx.util.YXBaseEntity;

/**
 * CMSModuleType 实体类
 * Sun Oct 19 11:41:02 CST 2014
 * @zb
 */ 

public class YXModuleType extends YXBaseEntity {

	private Long id;
	
	private String code;
	
	private String name;
	
	private String moduleProperties;
	
	private String description;
	
	private String thumbnailUrl;
	
	private Integer editRight;
	
	private Integer deleteRight;
	
	private Integer moveRight;
	
	private Integer copyRight;
	
	private Integer moduleCategory;
	
	private Integer display;
	
	private Integer shareRight;
	
	private String createId;
	
	private Date createTime;
	
	private Date updateTime;
	
	private String updateId;
	
	private String moduleService;
	

	public Long getId(){
		return id;
	}

	public void setId(Long id){
		this.id=id;
	}

	public String getCode(){
		return code;
	}

	public void setCode(String code){
		this.code=code;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name=name;
	}

	public String getModuleProperties(){
		return moduleProperties;
	}

	public void setModuleProperties(String moduleProperties){
		this.moduleProperties=moduleProperties;
	}

	public String getDescription(){
		return description;
	}

	public void setDescription(String description){
		this.description=description;
	}

	public String getThumbnailUrl(){
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl){
		this.thumbnailUrl=thumbnailUrl;
	}

	public Integer getEditRight(){
		return editRight;
	}

	public void setEditRight(Integer editRight){
		this.editRight=editRight;
	}

	public Integer getDeleteRight(){
		return deleteRight;
	}

	public void setDeleteRight(Integer deleteRight){
		this.deleteRight=deleteRight;
	}

	public Integer getMoveRight(){
		return moveRight;
	}

	public void setMoveRight(Integer moveRight){
		this.moveRight=moveRight;
	}

	public Integer getCopyRight() {
		return copyRight;
	}

	public void setCopyRight(Integer copyRight) {
		this.copyRight = copyRight;
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

	public Date getUpdateTime(){
		return updateTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime=updateTime;
	}

	public String getUpdateId(){
		return updateId;
	}

	public void setUpdateId(String updateId){
		this.updateId=updateId;
	}
	
	public Integer getModuleCategory() {
		return moduleCategory;
	}

	public void setModuleCategory(Integer moduleCategory) {
		this.moduleCategory = moduleCategory;
	}

	public Integer getDisplay() {
		return display;
	}

	public void setDisplay(Integer display) {
		this.display = display;
	}

	public Integer getShareRight() {
		return shareRight;
	}

	public void setShareRight(Integer shareRight) {
		this.shareRight = shareRight;
	}

	public String getModuleService() {
		return moduleService;
	}

	public void setModuleService(String moduleService) {
		this.moduleService = moduleService;
	}
}

