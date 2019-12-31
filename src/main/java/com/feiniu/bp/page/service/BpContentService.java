package com.feiniu.bp.page.service;

import java.util.List;

import com.feiniu.bp.page.entity.BpContent;

public interface BpContentService {
  public Long insertBpContent(BpContent content);
  
  public List<BpContent> getBpContentList(BpContent content);
  
  public BpContent getBpContentById(Long id);
  
  public Long updateBpContent(BpContent content);
  
  public Long updateBpContentStatus(BpContent content);
  
  public String checkPagColRepetition(BpContent content);
}
