package com.feiniu.yx.pool.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.pool.entity.YxPool;

@Repository
public class YxPoolOnlineDao{

    @Autowired
    private SqlSession sqlSession;

    public YxPool queryYxPoolById(Long id) {
        YxPool o = sqlSession.selectOne("selectYxPoolOnlineById", id);
        return o;
    }
	
	public long insertYxPool(YxPool content) {
		sqlSession.insert("insertYxPoolOnline",content);
		return content.getId();
	}
	
	public int updateYxPool(YxPool content) {
		return sqlSession.update("updateYxPoolOnline", content);
	}
}
