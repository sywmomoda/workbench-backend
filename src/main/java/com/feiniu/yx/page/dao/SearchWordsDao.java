package com.feiniu.yx.page.dao;

import com.feiniu.yx.page.entity.SearchWords;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SearchWordsDao {
	@Autowired
	private SqlSession sqlSession;
	
	public  List<SearchWords> list(SearchWords searchWords){
		PageBounds pageBounds = new PageBounds(searchWords.getCurPage(),searchWords.getPageRows());
		List<SearchWords> list = sqlSession.selectList("searchWordsList", searchWords, pageBounds);
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		searchWords.setTotalRows(pageList.getPaginator().getTotalCount());
		searchWords.setPageAmount(pageList.getPaginator().getPage());
		return list;
	}

	public  List<SearchWords> queryCheckRepeat(SearchWords searchWords){
		List<SearchWords> list = sqlSession.selectList("queryCheckRepeat", searchWords);
		return list;
	}
	
	public void insert(SearchWords searchWords) {
		sqlSession.insert("insertSearchWords", searchWords);
	}
	
	public SearchWords findByID(Long id) {
		return sqlSession.selectOne("findSearchWordsByID", id);
	}
	
	public void update(SearchWords searchWords) {
		sqlSession.update("updateSearchWords", searchWords);
	}
	
	public void delete(Long id) {
		sqlSession.delete("deleteSearchWords", id);
	}
	
}
