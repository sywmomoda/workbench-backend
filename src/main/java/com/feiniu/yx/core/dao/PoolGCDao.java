package com.feiniu.yx.core.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;

@Repository(value ="poolGCDao" )
public class PoolGCDao {
	
    @Autowired
    private SqlSession sqlSession;
    
    public List<YxPool> getAllPool() {
    	return sqlSession.selectList("selectAllPool");
    }
    
    public YxPool getOnlPoolById(Long id) {
    	return sqlSession.selectOne("selectYxPoolOnlineById", id);
    }
    
    public List<YxPoolPeriods> getPeriodsListByPoolId(Long id) {
    	return sqlSession.selectList("queryPeriodsListByPoolId", id);
    }
    
    public List<YxPoolPeriods> getOnlPeriodsListByPoolId(Long id) {
    	return sqlSession.selectList("queryOnlPeriodsListByPoolId", id);
    }
    
    public List<YxPoolPeriodsStore> getPoolPeriodsStoreListByperiodsId(Long periodsId) {
        return sqlSession.selectList("selectYxPoolPeriodsStoreByPeriodsId", periodsId);
    }
    
    public List<YxPoolPeriodsStore> getOnlPoolPeriodsStoreListByperiodsId(Long periodsId) {
        return sqlSession.selectList("selectOnlYxPoolPeriodsStoreByPeriodsId", periodsId);
    }
    
    public void delPool(Long id) {
    	sqlSession.delete("deleteYxPool",id);
    }
    
    public void delOnlPool(Long id) {
    	sqlSession.delete("deleteOnlYxPoolById",id);
    }

    public void delPoolPeriods(Long id) {
        sqlSession.delete("delPoolPeriodsById",id);
    }

    public void delOnlPoolPeriods(Long id) {
        sqlSession.delete("delPoolPeriodsByIdOnline",id);
    }

    public void delPoolPeriodsStore(Long id) {
        sqlSession.delete("delPoolPeriodsStore",id);
    }

    public void delOnlPoolPeriodsStore(Long id) {
        sqlSession.delete("delPoolPeriodsStoreOnline",id);
    }

    public void delPoolCommodity(String id) {
        sqlSession.delete("deleteYxPoolCommodityById", id);
    }

    public void delOnlPoolCommodity(String commodityId) {
        sqlSession.delete("deleteCommodityOnlineById",commodityId);
    }

    public void delYxPoolProperPlus(String commodityId) {
        sqlSession.delete("delYxPoolProperPlusByCommodityId",commodityId);
    }

    public void delOnlYxPoolProperPlus(String commodityId) {
        sqlSession.delete("delYxPoolProperPlusOnlineByCommodityId",commodityId);
    }
}
