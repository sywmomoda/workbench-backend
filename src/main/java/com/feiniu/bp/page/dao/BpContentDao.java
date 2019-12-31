package com.feiniu.bp.page.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.bp.page.entity.BpContent;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class BpContentDao {
	@Autowired
	private SqlSession sqlSession;
	
    public void insertBpContent(BpContent content){
    	sqlSession.insert("insertBpContent", content);
    }
    
	public List<BpContent> getBpContentList(BpContent content) {
		PageBounds pageBounds = new PageBounds(content.getCurPage(),content.getPageRows());
		List<BpContent> list = sqlSession.selectList("selectBpContentList",content,pageBounds);
		//获得结果集条总数
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList) list;
		content.setTotalRows(pageList.getPaginator().getTotalCount());
		return list;
	}
    
    public BpContent getBpContentById(Long id){
    	return sqlSession.selectOne("selectBpContentById", id);
    }
    
    public void updataBpContent(BpContent content){
    	sqlSession.update("updateBpContent", content);
    }
    
    public void updateBpContentStatus(BpContent content){
    	sqlSession.update("updateBpContentStatus", content);
    }
}
