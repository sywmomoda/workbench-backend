package com.feiniu.yx.pool.entity;

import com.feiniu.yx.util.YXBaseEntity;

public class YxCategory  extends YXBaseEntity{
	
	private Long                           id;
	/**
	 * 编号
	 */
    private String                        seq;
    /**
     * 父级编号
     */
    private String                        parentSeq="0";
    
	private String name;
	//门店
	private String storeCode; 
	
	private String parentSeqList;
	
	private int level;
	
	private String siSeq;
	
	private String siPseq;
	
	private int type;
	//0:不显示，1:显示
	private int siStatus;
	
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
	public String getSeq() {
		return seq;
	}
	public void setSeq(String seq) {
		this.seq = seq;
	}
	
	
	public String getStoreCode() {
		return storeCode;
	}
	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}
	public String getParentSeq() {
		return parentSeq;
	}
	public void setParentSeq(String parentSeq) {
		this.parentSeq = parentSeq;
	}
	
	public String getParentSeqList() {
		return parentSeqList;
	}
	public void setParentSeqList(String parentSeqList) {
		this.parentSeqList = parentSeqList;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getSiSeq() {
		return siSeq;
	}
	public void setSiSeq(String siSeq) {
		this.siSeq = siSeq;
	}
	public String getSiPseq() {
		return siPseq;
	}
	public void setSiPseq(String siPseq) {
		this.siPseq = siPseq;
	}
	public int getSiStatus() {
		return siStatus;
	}
	public void setSiStatus(int siStatus) {
		this.siStatus = siStatus;
	}
	
	
	
	
}
