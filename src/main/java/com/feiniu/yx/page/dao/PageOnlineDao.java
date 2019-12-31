package com.feiniu.yx.page.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.page.entity.Page;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class PageOnlineDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public Page findOne(Long id) {
		return sqlSession.selectOne("selectOnlinePageById", id);
	}

	public void insertOne(Page page) {
		sqlSession.insert("insertOnlinePage",page);
	}

	public int updateOne(Page page) {
		return sqlSession.update("updateOnlinePage",page);
	}
	
	public int updatePageStatus(Page page) {
		return sqlSession.update("updateOnlinePageStatus",page);
	}
	
	public List<Page> getOnLinePageList(Page page){
		PageBounds pageBounds = new PageBounds(page.getCurPage(),page.getPageRows());
		List<Page> list =sqlSession.selectList("selectOnlinePageAll", page,pageBounds);
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		page.setTotalRows(pageList.getPaginator().getTotalCount());
		page.setPageAmount(pageList.getPaginator().getPage());
        return list;
	}
	
	public void deleteOnlinePage(Long id){
		sqlSession.delete("deleteOnlinePage", id);
	}

}
