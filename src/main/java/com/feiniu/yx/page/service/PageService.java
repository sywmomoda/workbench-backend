package com.feiniu.yx.page.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.Page;

public interface PageService {

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
	public Map<String, String> getPageHtml(Map<String, Object> paramMap, String previewTime);

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
	
	/**
	 * 从ids中返回storeCode相匹配的PageId
	 * @param ids
	 * @param storeCode
	 * @return
	 */
	Long getPageIdByStoreCode(String ids, String storeCode);

	public JSONObject synsModulesOfStore(String modules, String storeCode,String copyCode);

	public JSONObject synsIndexModulesOfStore(String modules, String storeCode,String copyCode) throws Exception;
	
	String pageCopy(JSONObject param);

}
