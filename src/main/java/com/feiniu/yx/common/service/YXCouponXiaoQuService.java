package com.feiniu.yx.common.service;

public interface YXCouponXiaoQuService {
  public String getJSONXiaoQu(String pgSeq);
  public String getXiaoQuNamesByCodes(String miniCode);
  public String  getStoreCodesByXiaoQuCodes(String codes);
}
