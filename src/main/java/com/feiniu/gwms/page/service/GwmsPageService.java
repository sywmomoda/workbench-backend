package com.feiniu.gwms.page.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.feiniu.yx.page.entity.Page;
@Service
public interface GwmsPageService {

	/** 根据ID查询页面
	 * @param pageId
	 * @return
	 */
	public Page queryPageByID(Long pageId);

	/** 获取页面html
	 * @param auth
	 * @param paramMap
	 * @return
	 */
	public Map<String, String> getPageHtml(Map<String, Object> paramMap);

	/** 保存活动，更新组件列表
	 * @param auth
	 * @param page
	 * @return
	 */
	public String savePageModules(Page page);
	
	 /**
     * 创建活动页service
     * 
     * @param auth
     * @param params
     * @return
     */
    void createPage(Page page);
    
    
    public int updateCMSPageInfo(Page cmsPage);

	public List<Page> queryActivityList(Page page, int i);

	public List<Page> queryPageForLinkSelect(Page page);

	public void updateCMSPage(Page page);
	
	public void pausePage(Long id, int status);
	
	public void deletePage(Long id);

}
