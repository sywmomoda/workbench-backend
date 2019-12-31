package com.feiniu.yx.pool.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.pool.entity.YxPoolProperPlus;
@Repository
public class YxPoolProperPlusOnlineDao {
	@Autowired
	private SqlSession sqlSession;
	
	public void batchInsert(List<YxPoolProperPlus> list){
		sqlSession.insert("batchInsertOnlineYxPoolProper", list);
	}
	
	public long insert(YxPoolProperPlus properPlus){
		sqlSession.insert("insertOnlineYxPoolProper", properPlus);
		return properPlus.getId();
	}
	
	public void delete(YxPoolProperPlus proper){
		sqlSession.delete("deleteOnlineYxPoolProper", proper);
	}
	
	public List<YxPoolProperPlus> queryProperList(Long commodityId){
		return sqlSession.selectList("selectOnlineYxPoolProperList", commodityId);
	}
	
}
