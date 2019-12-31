package com.feiniu.yx.template.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.yx.template.dao.YXTemplateDao;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXTemplateService;

@Service
public class YXTemplateServiceImpl implements YXTemplateService {
	
	@Autowired
	private YXTemplateDao templateDao;
	//默认缩略图
	private final static String URL = "http://img18.fn-mart.com/pic/c1d2133ccf5bdc91dfdb/B2Hzzz5TvnfdBdZdXn/59eaeajGC9LaKx/CsmRslirmT2AWC-eAAAUjbRwsFM623.jpg";
	
	@Override
	public void insertYXTemplate(YXTemplate template) {
		if(template != null && StringUtils.isBlank(template.getTemplateUrl()))
			template.setTemplateUrl(URL);
		templateDao.insert(template);
	}
	
	@Override
	public List<YXTemplate> getYXTemplateList() {
	 List<YXTemplate> templateList = templateDao.getYXTemplateList();
	 return templateList;
	}
	
	@Override
	public List<YXTemplate> getYXTemplateList(YXTemplate template) {
		return templateDao.getYXTemplateList(template);
	}
	
	@Override
	public YXTemplate getYXTemplateById(Long Id) {
		return templateDao.getYXTemplateById(Id);
	}
	
	@Override
	public void updateYXTemplate(YXTemplate template) {
		templateDao.update(template);
	}
}
