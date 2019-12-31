package com.feiniu.yx.pool.dao;


import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.pool.entity.YxPoolPeriods;
@Repository
public class YxPoolPeriodsOnlineDao{
	
	@Autowired
	private SqlSession sqlSession;
	
	public YxPoolPeriods queryYxPoolPeriodsByID(Long id) {
		return sqlSession.selectOne("selectYxPoolPeriodsOnlineById",id);
	}
	
	
	public int updateYxPoolPeriods(YxPoolPeriods cpp) {
		return sqlSession.update("updateYxPoolPeriodsOnline",cpp);
	}

	public long insertYxPoolPeriods(YxPoolPeriods content) {
		sqlSession.insert("insertYxPoolPeriodsOnline",content);
		return content.getId();
	}
	
	public void deleteYxPoolPeriods(long id){
		sqlSession.delete("deleteYxPoolPeriodsOnline",id);
	}
}
