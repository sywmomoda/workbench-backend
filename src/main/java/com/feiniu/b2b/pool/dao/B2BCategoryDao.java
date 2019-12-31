package com.feiniu.b2b.pool.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.b2b.pool.entity.B2BCategory;


@Repository
public class B2BCategoryDao{

    @Autowired
    private SqlSession sqlSession;


    public List<B2BCategory> queryB2BCategorys(B2BCategory c) {
    	List<B2BCategory> o = sqlSession.selectList("selectB2BCategorys", c);
        return o;
    }
    
    public int queryB2BCategoryCount(String parentSeq) {
    	B2BCategory b2bCategory = new B2BCategory();
    	b2bCategory.setParentSeq(parentSeq);
    	Integer i = sqlSession.selectOne("selectB2BCategoryCount", b2bCategory);
        return i;
    }
    
    public void clearAll(String storeCode) {
    	B2BCategory b2bCategory = new B2BCategory();
    	b2bCategory.setStoreCode(storeCode);
    	sqlSession.delete("deleteAllB2BCategory",b2bCategory);
    }
    
    public void batchInsert(List<B2BCategory> l) {
    	sqlSession.insert("batchAddB2BCategory",l);
    }
}
