package com.feiniu.yx.pool.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.pool.entity.YxPoolPeriods;


@Repository
public class YxPoolPeriodsDao{
	
	@Autowired
	private SqlSession sqlSession;
	
	public YxPoolPeriods queryYxPoolPeriodsByID(Long id) {
		return sqlSession.selectOne("selectYxPoolPeriodsById",id);
	}
	
	
	public List<YxPoolPeriods> queryYxPoolPeriodsList(YxPoolPeriods cpp) {
		return sqlSession.selectList("selectYxPoolPeriodsOrderByNumber",cpp);
	}
	
	//查询最后一期
	public YxPoolPeriods queryCMSPoolPeriodsLast(YxPoolPeriods cpp) {
		List<YxPoolPeriods> cppList=sqlSession.selectList("selectYxPoolPeriodsOrderByNumber",cpp);
		YxPoolPeriods cupp =null;
		if(cppList!=null&& cppList.size()>0){
			cupp = cppList.get(0);
		}
		return cupp;
	}
	
	
	public int updateCMSPoolPeriods(YxPoolPeriods cpp) {
		return sqlSession.update("updateYxPoolPeriods",cpp);
	}

	/**
	 * @author lizhiyong
	 * 2017年2月15日
	 * @Description: 按条件查询池期数列表
	 * @param cpp 查询条件对象
	 * @return 池期数列表
	 * @throws
	*/ 
	public List<YxPoolPeriods> queryPeriodsList(YxPoolPeriods cpp) {
		return sqlSession.selectList("selectYxPoolPeriodsOrderByBeginTime",cpp);
	}	

	public long insertYxPoolPeriods(YxPoolPeriods content) {
		sqlSession.insert("insertYxPoolPeriods",content);
		return content.getId();
	}
	
	/**
	 * 查询pool对应的所有期数
	 * @author lizhiyong
	 * 2017年2月15日
	 * @param poolId
	 * @return
	 */
	public List<YxPoolPeriods> queryPeriodsListByPoolId(Long poolId) {
		return sqlSession.selectList("queryPeriodsListByPoolId",poolId);
	}
	
	/**
	 * 删除池期数
	 * @author lizhiyong
	 * 2017年2月15日
	 * @param id
	 * TODO
	 */
	public void deleteYxPoolPeriods(Long id){
		 sqlSession.delete("deleteYxPoolPeriods", id);
	}

	public YxPoolPeriods findPeriodsByOldId(Long oldId) {
		return sqlSession.selectOne("findByOldId",oldId);
	}


	public long addYxPoolPeriods(YxPoolPeriods yx) {
		 sqlSession.insert("addYxPoolPeriods",yx);
		 return yx.getId();
	}
}
