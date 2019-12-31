package com.feiniu.yx.common.entity;


import java.util.List;

import com.feiniu.yx.util.YXBaseEntity;

public class FileList extends YXBaseEntity{
	private String fileID;
	private String fileName;
	private String fileSize;
	private String fileType;
	private String filePath;
	private String lastEditDate;
	private List<FileList> fileLists;
	private String parentPath;
	
	public String getLastEditDate() {
		return lastEditDate;
	}
	public void setLastEditDate(String lastEditDate) {
		this.lastEditDate = lastEditDate;
	}
	public String getFileID() {
		return fileID;
	}
	public void setFileID(String fileID) {
		this.fileID = fileID;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileSize() {
		return fileSize;
	}
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public List<FileList> getFileLists() {
		return fileLists;
	}
	public void setFileLists(List<FileList> fileLists) {
		this.fileLists = fileLists;
	}
	public String getParentPath() {
		return parentPath;
	}
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}
	
}
