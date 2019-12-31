package com.feiniu.bp.page.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.bp.page.entity.BpPage;

@Repository
public class BpPageDao {
	@Autowired
    private SqlSession sqlSession;
	
	public List<BpPage> getBpPageList(BpPage page){
		return sqlSession.selectList("selectBpPageList", page);
	}
	
	public void insertBpPage(BpPage page){
		sqlSession.insert("insertBpPage", page);
	}
	
	public BpPage getBpPageById(Long id){
		return sqlSession.selectOne("selectBpPageById",id);
	} 
	
	public void updateBpPage(BpPage page){
		sqlSession.update("updateBpPage", page);
	}
}
