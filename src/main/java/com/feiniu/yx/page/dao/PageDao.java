package com.feiniu.yx.page.dao;

import com.feiniu.yx.page.entity.Page;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;
import org.apache.commons.collections.MapUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class PageDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public Page queryCMSPageByID(Long id) {
		return sqlSession.selectOne("selectSalePageById", id);
	}

	public Long insertCMSPage(Page cmsPage) {
		sqlSession.insert("insertSalePage",cmsPage);
		return cmsPage.getId();
	}

	public int updateCMSPage(Page cmsPage) {
		return sqlSession.update("updateSalePage",cmsPage);
	}
	
	public int updatePageStatus(Page cmsPage) {
		return sqlSession.update("updatePageStatus",cmsPage);
	}

	public List<Page> getPagesByIds(String pageIds) {
		String[] ids = pageIds.split(",");
		if(ids.length==0){
			return new ArrayList<Page>();
		}
		List<Page> list =  sqlSession.selectList("selectSalePageByIds",ids);
		return list;
	}

	public Integer getPageData(Page page) {
		Integer num =  (Integer)sqlSession.selectOne("selectSalePageCount",page);
		return num;
	}
	
	public Integer getPageDataByTime(Page page) {
		Integer num =  (Integer)sqlSession.selectOne("selectSalePageCountByTime",page);
		return num;
	}
	
	public Integer getCreatePageDataByTime(Page page) {
		Integer num =  (Integer)sqlSession.selectOne("selectCreateSalePageCount",page);
		return num;
	}

	
	public List<Page> queryActivityList(Page page,boolean isAll) {
		if(isAll){
			List<Page> list =  sqlSession.selectList("selectSalePages",page);
			return list;
		}else{
			PageBounds pageBounds = new PageBounds(page.getCurPage(),page.getPageRows());
			List<Page> list =  sqlSession.selectList("selectSalePages",page,pageBounds);
			@SuppressWarnings("rawtypes")
			PageList pageList = (PageList)list;
			page.setTotalRows(pageList.getPaginator().getTotalCount());
			page.setPageAmount(pageList.getPaginator().getPage());
			return list;
		}
	}
	
	public List<Page> queryActivityListForClick(Page page,boolean isAll) {
		if(isAll){
			List<Page> list =  sqlSession.selectList("selectSalePagesForClick",page);
			return list;
		}else{
			PageBounds pageBounds = new PageBounds(page.getCurPage(),page.getPageRows());
			List<Page> list =  sqlSession.selectList("selectSalePagesForClick",page,pageBounds);
			@SuppressWarnings("rawtypes")
			PageList pageList = (PageList)list;
			page.setTotalRows(pageList.getPaginator().getTotalCount());
			page.setPageAmount(pageList.getPaginator().getPage());
			return list;
		}
	}
	
	public List<Long> getOnlineAllPageId(int clientType){
		List<Long> list =  sqlSession.selectList("selectOnlineAllPageId",clientType);
		return list;
	}
	
	public List<Page> getPageCreateByTime(Map<String,Object> map){
		
		Integer curPage = MapUtils.getInteger(map, "curPage", 0);
		Integer pageRows = MapUtils.getInteger(map, "pageSize", 10);
		PageBounds pageBounds = new PageBounds(curPage,pageRows);
		List<Page> list =  sqlSession.selectList("selectPageCreateByTime",map,pageBounds);
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		map.put("total", pageList.getPaginator().getTotalCount());
		return list;
	}
	
	public Integer getPageCreateCount(Map<String,Object> map){
		return sqlSession.selectOne("selectPageCreateCount",map);
	}
	
	public List<Page> queryPageForLinkSelect(Page page){
		PageBounds pageBounds = new PageBounds(page.getCurPage(),page.getPageRows());
		List<Page> list =  sqlSession.selectList("selectPageForLinkSelect",page,pageBounds);
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		page.setTotalRows(pageList.getPaginator().getTotalCount());
		return list;
	}


	public void deletePage(Long id){
		sqlSession.delete("deletePage", id);
	}

	/**
	 * 批量更新群组信息
	 * @param page
	 */
	public void updatePageArea(Page page){
		sqlSession.update("updatePageArea", page);
	}

}
