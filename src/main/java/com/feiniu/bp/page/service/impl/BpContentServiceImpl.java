package com.feiniu.bp.page.service.impl;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.bp.page.dao.BpContentDao;
import com.feiniu.bp.page.entity.BpContent;
import com.feiniu.bp.page.service.BpContentService;
import com.feiniu.yx.common.interceptor.LogTraceService;
import com.feiniu.yx.util.UserUtil;

@Service
public class BpContentServiceImpl implements BpContentService {
	@Autowired
	private BpContentDao contentDao;
	
	@Autowired
	private LogTraceService logTraceService;

	@Override
	public Long insertBpContent(BpContent content) {
		String userName = UserUtil.getUserId();
		content.setCreateId(userName);
		content.setUpdateId(userName);
		contentDao.insertBpContent(content);
		return content.getId();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<BpContent> getBpContentList(BpContent content) {
		List<BpContent> conList = contentDao.getBpContentList(content);
		for (BpContent con : conList) {
			String otherPro = con.getOtherProperties();
			if (StringUtils.isBlank(otherPro)) {
				continue;
			}
			con.setOtherProMap(JSONObject.parseObject(otherPro, HashMap.class));
		}
		return conList;
	}

	@Override
	public BpContent getBpContentById(Long id) {
		return contentDao.getBpContentById(id);
	}

	@Override
	public Long  updateBpContent(BpContent content) {
		String userName = UserUtil.getUserId();
		content.setUpdateId(userName);
		Long id = content.getId();
		BpContent exist = contentDao.getBpContentById(id);
		contentDao.updataBpContent(content);
		String operationMsg =  checkUpdateFields(exist,content);
		if(StringUtils.isNotBlank(operationMsg)){
			writeBpContentLogs("更新"+operationMsg+"字段", id);
		}
		return id;
	}
	
	private String checkUpdateFields(BpContent exist , BpContent nev){
		if(exist.getId() == null){
			return null;
		}
		StringBuffer sbMsg = new StringBuffer();
		if(!exist.getPageName().equals(nev.getPageName())){
			sbMsg.append("\"").append("页面").append("\"").append("、");	
		}
		if(!exist.getPageCol().equals(nev.getPageCol())){
			sbMsg.append("\"").append("page_col").append("\"").append("、");	
		}	
		if(exist.getTrackType().intValue() != nev.getTrackType()){
			sbMsg.append("\"").append("track_type").append("\"").append("、");	
		}
		String exPro = exist.getOtherProperties();
		String nevPro = nev.getOtherProperties();
		JSONObject exObj = JSONObject.parseObject(exPro);
		JSONObject nevObj = JSONObject.parseObject(nevPro);
		if(!exObj.getString("actComment").equals(nevObj.getString("actComment"))){
			sbMsg.append("\"").append("行动说明").append("\"").append("、");	
		}
		if(!exObj.getString("colPosition").equals(nevObj.getString("colPosition"))){
			sbMsg.append("\"").append("col_position").append("\"").append("、");	
		}
		if(!exObj.getString("colPosContent").equals(nevObj.getString("colPosContent"))){
			sbMsg.append("\"").append("col_pos_content").append("\"").append("、");	
		}
		if(!exObj.getString("remarks").equals(nevObj.getString("remarks"))){
			sbMsg.append("\"").append("remarks").append("\"").append("、");	
		}
		if(!exObj.getString("abtest").equals(nevObj.getString("abtest"))){
			sbMsg.append("\"").append("abtest").append("\"").append("、");	
		}
		if(!exObj.getString("description").equals(nevObj.getString("description"))){
			sbMsg.append("\"").append("备注说明").append("\"").append("、");	
		}
		String msg = sbMsg.toString();
		if(msg.length() > 0){
			msg = msg.substring(0, msg.length() -1);
		}
		return msg;
	}
	
	
	@Override
	public Long updateBpContentStatus(BpContent content) {
		contentDao.updateBpContentStatus(content);
		String  operationMsg ="";
		if(content.getStatus().intValue() == 1){
			operationMsg = "【状态：废弃 ——>启用】";
		}else if(content.getStatus().intValue() == 0){
			operationMsg = "【状态：启用 ——>废弃】";
		}
		Long id = content.getId();
		writeBpContentLogs(operationMsg,id);
		return id;
	}
	
	/**
	 *写日志
	 * @param log
	 */
	private void writeBpContentLogs(String  operationMsg,Long Id){
		if(StringUtils.isBlank(operationMsg)){
			return;
		}
		logTraceService.sendLogger(operationMsg, "bpContent", Id);
	}
	
	@Override
	public String checkPagColRepetition(BpContent content) {
		Long id = content.getId();
		content.setId(null);
		List<BpContent> list = contentDao.getBpContentList(content);
		if(null == list){
			return "false";
		}
		if(list.size() == 0){
			return "false";
		}
		BpContent newCon = list.get(0);
		//去掉自己
		if(null !=id && id.longValue() == newCon.getId().longValue()){ 
			return "false";
		}
		return "true";
	}

}
