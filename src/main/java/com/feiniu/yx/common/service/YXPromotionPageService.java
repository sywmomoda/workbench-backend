package com.feiniu.yx.common.service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.entity.PromoPage;

public interface YXPromotionPageService {
  public JSONObject  getRemotePages(PromoPage page);
  public JSONObject getPromoSinglePageById(String id);
}
