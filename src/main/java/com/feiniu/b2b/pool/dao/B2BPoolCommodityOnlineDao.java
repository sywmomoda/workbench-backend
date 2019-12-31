package com.feiniu.b2b.pool.dao;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.feiniu.b2b.pool.entity.B2BPoolCommodity;

@Repository
public class B2BPoolCommodityOnlineDao{

    @Autowired
    private SqlSession sqlSession;

	public void batchDeleteB2BPoolCommodity(String oldCommodityIds) {
		if (StringUtils.isEmpty(oldCommodityIds)) {
            return ;
        }
        String[] cids = oldCommodityIds.split(",");
		sqlSession.delete("deleteCommodityOnlineByIds",cids);
	}
	
	public void batchSaveUniteCommodity(List<B2BPoolCommodity> commodityList) {
		if(commodityList.size()>0){
			sqlSession.insert("insertBatchYxPoolCommodityOnline", commodityList);	
		}
	}

}
