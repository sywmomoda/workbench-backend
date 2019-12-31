package com.feiniu.yx.pool.dao;

import java.util.ArrayList;
import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.feiniu.yx.pool.entity.YxPoolProperPlus;
@Repository
public class YxPoolProperPlusDao {
	@Autowired
	private SqlSession sqlSession;
	
	public void batchInsert(List<YxPoolProperPlus> list){
		sqlSession.insert("batchInsertYxPoolProper", list);
	}
	
	public long insert(YxPoolProperPlus properPlus){
		sqlSession.insert("insertYxPoolProper", properPlus);
		return properPlus.getId();
	}
	
	public void delete(YxPoolProperPlus proper){
		sqlSession.delete("deleteYxPoolProper", proper);
	}
	
	public List<YxPoolProperPlus> queryProperList(Long commodityId){
		return sqlSession.selectList("selectYxPoolProperList", commodityId);
	}
	
	public List<YxPoolProperPlus> queryProperList(String[] ids){
		if(ids.length==0){
			return new ArrayList<YxPoolProperPlus>();
		}
		return sqlSession.selectList("selectYxPoolProperListByComoditys", ids);
	}
	
	public YxPoolProperPlus queryProperById(Long id){
		return sqlSession.selectOne("selectYxPoolProperById", id);
	}
	
	public YxPoolProperPlus queryProperPlusSingle(YxPoolProperPlus properPlus){
		return sqlSession.selectOne("selectYxPoolProperSingle", properPlus);
	}
	
	public void updateYxPoolProperPlusCommodityId(YxPoolProperPlus properPlus){
		sqlSession.update("updateYxPoolProperCommodityId", properPlus);
	} 
	
}
