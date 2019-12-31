package com.feiniu.yx.pool.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.pool.entity.YxCategory;

@Repository
public class YxCategoryDao{

    @Autowired
    private SqlSession sqlSession;


    public List<YxCategory> queryYxCategorys(YxCategory c) {
    	List<YxCategory> o = sqlSession.selectList("selectYxCategorys", c);
        return o;
    }
    
    public int queryYxCategoryCount(String parentSeq) {
    	YxCategory yxCategory = new YxCategory();
    	yxCategory.setParentSeq(parentSeq);
    	Integer i = sqlSession.selectOne("selectYxCategoryCount", yxCategory);
        return i;
    }
    
    public void clearAll(String storeCode) {
    	YxCategory yxCategory = new YxCategory();
    	yxCategory.setStoreCode(storeCode);
    	sqlSession.delete("deleteAllCategory",yxCategory);
    }
    
    public void batchInsert(List<YxCategory> l) {
    	sqlSession.insert("batchAddYxCategory",l);
    }
}
