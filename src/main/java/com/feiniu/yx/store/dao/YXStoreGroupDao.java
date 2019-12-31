package com.feiniu.yx.store.dao;

import com.feiniu.yx.store.entity.YXStoreGroup;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class YXStoreGroupDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public void insert(YXStoreGroup group){
		sqlSession.insert("insertStoreGroup", group);
	}
	
    public List<YXStoreGroup> getYXStoreGroupList(){
    	return sqlSession.selectList("selectAllStoreGroup");
    }

	public List<YXStoreGroup> queryStoreGroupByParam(YXStoreGroup group){
		return sqlSession.selectList("queryStoreGroupByParam",group);
	}
    
    
    public YXStoreGroup getYXStoreGroupById(Long Id){
    	return sqlSession.selectOne("selectStoreById",Id);
    }
    
    public void update(YXStoreGroup group){
    	sqlSession.update("updateStoreGroup",group);
    }

	public void del(YXStoreGroup group){
		sqlSession.delete("deleteStoreGroup",group);
	}
    
    public List<YXStoreGroup> listYXStoreGroupByIds(String[] ids) {
    	return sqlSession.selectList("listYXStoreGroupByIds",ids);
    }
}
