package com.feiniu.bp.page.service;


import java.util.List;

import com.feiniu.bp.page.entity.BpPage;


public interface BpPageService {
  public String checkBpPageByPageId(BpPage page);
  public long insertBpPage(BpPage page);
  public List<BpPage> getBpPageList(BpPage page);
  public BpPage getBpPageById(Long id);
  public long updateBpPage(BpPage page);
}
