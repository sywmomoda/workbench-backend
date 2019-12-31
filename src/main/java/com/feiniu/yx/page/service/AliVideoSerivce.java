package com.feiniu.yx.page.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.AliVideo;

public interface AliVideoSerivce {
  List<AliVideo> list(AliVideo video);
  void insert(AliVideo video);
  AliVideo getVideoById(Long id);
  void update(AliVideo video);
  void delete(Long id);
  Long getMaxNum();
  JSONObject interfacelist(String data);
}
