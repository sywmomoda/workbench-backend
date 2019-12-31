package com.feiniu.yx.page.service.impl;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.interceptor.LogTraceService;
import com.feiniu.yx.common.service.YXVideoSerice;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.page.dao.AliVideoDao;
import com.feiniu.yx.page.entity.AliVideo;
import com.feiniu.yx.page.service.AliVideoSerivce;
import com.feiniu.yx.util.UserUtil;
/**
 * 
 * @author yehui
 *2019年7月9日
 */
@Service
public class AliVideoSerivceImpl implements AliVideoSerivce {

	private static Logger logger = Logger.getLogger(AliVideoSerivceImpl.class);
	
	private static final String DOWN_ViDEO_PREFIX =SystemEnv.getProperty("fn.video.download.host");
	
	@Autowired
	private AliVideoDao videoDao;
	
	@Autowired
	private YXVideoSerice videoSerivce;
	
	@Autowired
	private LogTraceService logSerice;
	
	@Override
	public List<AliVideo> list(AliVideo video) {
		updateVideoStatus();
		List<AliVideo> list = videoDao.list(video);
		for(AliVideo v : list) {
			v.setFrontUrl(DOWN_ViDEO_PREFIX+v.getCustomUrl());
		}
		return list;
	}
	
	private void updateVideoStatus () {
		AliVideo video = new AliVideo();
		video.setStatus(1) ; //未完成
		List<AliVideo> list = videoDao.list(video);
		for(AliVideo v : list) {
			if(null == v) {
				continue;
			}
			String contentId = v.getContentId();
			JSONObject res = videoSerivce.videoProgress(contentId);
			if(null == res) {
				continue;
			}
			String code = res.getString("code");
			if("succ".equals(code)) {
				v.setStatus(2);//已经完成
			}
			videoDao.update(v);
		}
	}
	
	
	@Override
	public void insert(AliVideo video) {
		String userId = UserUtil.getUserId();
		video.setStatus(1);
		video.setUpdateId(userId);
		video.setCreateId(userId);
		videoDao.insert(video);
		logSerice.sendLogger("【添加】："+JSONObject.toJSONString(video), "alivideo", video.getId());
	}
	
	@Override
	public AliVideo getVideoById(Long id) {
		return videoDao.getVideoById(id);
	}
	
	@Override
	public void update(AliVideo video) {
		AliVideo old = videoDao.getVideoById(video.getId());
		video.setStatus(2);
		if(!old.getContentId().equals(video.getContentId())){
			video.setStatus(1);
		}
		video.setUpdateId(UserUtil.getUserId());
		videoDao.update(video);
		logSerice.sendLogger("【更新】:"+JSONObject.toJSONString(video), "alivideo", video.getId());
	}
	
	@Override
	public void delete(Long id) {
		AliVideo old = videoDao.getVideoById(id);
		videoDao.delete(id);
		logSerice.sendLogger("【删除】:"+JSONObject.toJSONString(old), "alivideo", old.getId());
	}
	
	@Override
	public Long getMaxNum() {
		return videoDao.getMaxNum();
	}
	
	@Override
	public JSONObject interfacelist(String data) {
		AliVideo video = null;
		try {
			video = JSONObject.parseObject(data, AliVideo.class);
		} catch (Exception e) {
			logger.error("[json数据异常]"+data);
		}
		if(null  == video) {
			video = new AliVideo();
		}
		final int status = 2;
		video.setStatus(status);
		List<AliVideo> list = this.list(video);
		JSONObject res = new JSONObject();
		if(null == list || list.size() == 0) {
			return res;
		}
		List<JSONObject> resList = new ArrayList<JSONObject>();
		for(AliVideo v : list) {
			JSONObject sin = new JSONObject(16);
			sin.put("frontUrl", v.getFrontUrl());
			sin.put("id",v.getId());
			sin.put("name", v.getName());
			sin.put("note", v.getNote());
			sin.put("status", v.getStatus());
			sin.put("imgUrl",v.getImgUrl());
			sin.put("gifUrl",v.getGifUrl());
			resList.add(sin);
		}
		res.put("curPage", video.getCurPage());
		res.put("pageRows", video.getPageRows());
		res.put("total", video.getTotalRows());
		res.put("list", resList);
		return res;
	}
}
