package com.feiniu.yx.welcome.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.welcome.entity.YXWelcomeImg;


@Repository
public class YXWelcomeImgDao {
	@Autowired
	  private SqlSession sqlSession;
	
	   public void insertBatch(List<YXWelcomeImg> list){
		   sqlSession.insert("insertBatchYXWelcomeImg",list);
	   }
	   
	   public List<YXWelcomeImg> getYXWelcomeImgsOfByYxWelcomeId(Long welcomeId){
		   return sqlSession.selectList("selectYXWelcomeImgByWelcomeId",welcomeId);
	   }
	   
	   public void deleteYXWelcomeImgsOfByYXwelcomeId(Long welcomeId){
		  sqlSession.selectList("deleteYXWelcomeImgByWelcomeId",welcomeId);
	   }
}
