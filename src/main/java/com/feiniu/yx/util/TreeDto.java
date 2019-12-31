package com.feiniu.yx.util;

import java.util.List;
import java.util.Map;

/**
 * @author tongwenhuan
 * 2017年2月16日 下午3:02:39
 */
public class TreeDto {

	/**树节点id**/
	private Long id;
	
	/**树节点名称**/
	private String text;
	
	/**树节点状态**/
	private String state;
	
	/**树节点复选框**/
	private Boolean checked;
	
	/**树孩子节点集合**/
	private List<TreeDto> children;
	
	/**树父节点id**/
	private String parentId;
	
	/**树节点属性集合**/
	private Map<String, String> attributes ;
	
	
	public Map<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Boolean getChecked() {
		return checked;
	}
	public void setChecked(Boolean checked) {
		this.checked = checked;
	}
	public List<TreeDto> getChildren() {
		return children;
	}
	public void setChildren(List<TreeDto> children) {
		this.children = children;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
}
