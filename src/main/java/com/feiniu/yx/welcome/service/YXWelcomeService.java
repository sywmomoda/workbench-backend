package com.feiniu.yx.welcome.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.welcome.entity.YXWelcome;

public interface YXWelcomeService {
	
	public List<YXWelcome> getWelcomeList(YXWelcome welcome);
	
	public JSONObject checkDate(YXWelcome welcome);
	
	public void updateYXWelcomeStatus(Long id);
	
	public void deleteYXWelcome(Long id);
	
	public YXWelcome getWelcomeById(Long id);
	
	public void insertYXWelcome(String data);
	
	public JSONObject getWelcomeOfImgs(Long id);
	
	public void updateYXWelcome(String data);
}
