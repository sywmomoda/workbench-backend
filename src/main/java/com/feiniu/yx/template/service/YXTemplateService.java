package com.feiniu.yx.template.service;

import java.util.List;

import com.feiniu.yx.template.entity.YXTemplate;

public interface YXTemplateService {
	
	public void insertYXTemplate(YXTemplate template);
	public List<YXTemplate> getYXTemplateList();
	public List<YXTemplate> getYXTemplateList(YXTemplate template);
	public YXTemplate getYXTemplateById(Long Id);
	public void updateYXTemplate(YXTemplate template);
	
//	public String getArtTemplateHead(String templateCode);
//	public String getArtTemplateFoot(String templateCode);
//	public String getArtTemplate(String templateUrl);
	
}
