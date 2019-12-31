package com.feiniu.b2b.store.dao;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.entity.B2BStoreGroup;

@Repository
public class B2BStoreGroupDao {
	@Autowired
	private SqlSession sqlSession;
	
	public void insert(B2BStoreGroup group){
		sqlSession.insert("insertB2BStoreGroup", group);
	}
	
    public List<B2BStoreGroup> getB2BStoreGroupList(){
    	return sqlSession.selectList("selectAllB2BStoreGroup");
    }	
    
    
    public B2BStoreGroup getB2BStoreGroupById(Long Id){
    	return sqlSession.selectOne("selectB2BGroupStoreById",Id);
    }
	
    
    public void update(B2BStoreGroup group){
    	sqlSession.update("updateB2BStoreGroup",group);
    }
    
    public B2BStoreGroup getB2BStoreGroup(B2BStore store){
    	return sqlSession.selectOne("selectB2BStoreByStoreId",store);
    }
    
    public void deleteB2BStoreGroupById(Long id){
    	sqlSession.delete("deleteB2BStoreGroupById", id);
    }
}
