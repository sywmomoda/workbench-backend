package com.feiniu.yx.common.service;

import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;

public interface YXVideoSerice {
	JSONObject upload(MultipartFile file);
	JSONObject videoProgress(String contentId);
}
