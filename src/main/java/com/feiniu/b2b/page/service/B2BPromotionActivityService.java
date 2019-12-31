package com.feiniu.b2b.page.service;

import com.alibaba.fastjson.JSONObject;

public interface B2BPromotionActivityService {
  public JSONObject  getRemotePages(String pageNo,String pageSize,String id,String name);
  public JSONObject getPromoSinglePageById(String id);
}
