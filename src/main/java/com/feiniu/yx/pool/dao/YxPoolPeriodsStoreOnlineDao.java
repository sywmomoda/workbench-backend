package com.feiniu.yx.pool.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;

@Repository
public class YxPoolPeriodsStoreOnlineDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public YxPoolPeriodsStore queryYxPoolPeriodsStoreById(Long id) {
		return sqlSession.selectOne("selectYxPoolPeriodsStoreOnlineById", id);
	}
   public void batchSaveStore(List<YxPoolPeriodsStore> provinceList){
		 sqlSession.insert("insertBatchYxPoolPeriodsStoreOnline", provinceList);
		}
   
	public void deleteStoreByPeriodId(Long id) {
		sqlSession.delete("deleteYxPoolPeriodsStoreOnlineByPeriodId", id);
	}
	
	
	public List<YxPoolPeriodsStore> queryStoreList(YxPoolPeriodsStore pp) {
		return sqlSession.selectList("selectYxPoolPeriodsStoreOnlineList", pp);		
	}
	
	public void deleteYxPoolPeriodsStore(Long id){
		sqlSession.delete("deleteYxPoolPeriodsStoreOnline",id);
	}
	
	public void updateStore(YxPoolPeriodsStore pv) {
		sqlSession.update("updateYxPoolPeriodsStoreOnline", pv);	
	}
}
