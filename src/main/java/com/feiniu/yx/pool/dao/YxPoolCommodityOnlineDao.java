package com.feiniu.yx.pool.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.pool.entity.YxPoolCommodity;

@Repository
public class YxPoolCommodityOnlineDao{

    @Autowired
    private SqlSession sqlSession;

	public void batchDeleteYxPoolCommodity(String oldCommodityIds) {
		if (StringUtils.isEmpty(oldCommodityIds)) {
            return ;
        }
        String[] cids = oldCommodityIds.split(",");
		sqlSession.delete("deleteCommodityOnlineByIds",cids);
	}
	
	public void batchSaveUniteCommodity(List<YxPoolCommodity> commodityList) {
		if(commodityList.size()>0){
			sqlSession.insert("insertBatchYxPoolCommodityOnline", commodityList);	
		}
	}

	public List<YxPoolCommodity> getYxPoolCommodityByIds(String ids) {
		List<YxPoolCommodity> list = new ArrayList<YxPoolCommodity>();
        if (StringUtils.isEmpty(ids)) {
            return list;
        }
        String[] cids = ids.split(",");
        if(cids.length==0){
        	 return list;
        }
        List<YxPoolCommodity> temp = sqlSession.selectList("selectYxPoolCommodityOnlineByIds", cids);
        Map<String, YxPoolCommodity> map = new HashMap<String, YxPoolCommodity>();
        for (YxPoolCommodity c : temp) {
            map.put(c.getId().toString(), c);
        }
        // 重新排序
        for (String id : cids) {
            YxPoolCommodity c2 = map.get(id);
            if (c2 != null) {
            	if(c2.getAddOnDate() == null){
            		c2.setAddOnDate(new Date());
            	}
                list.add(c2);
            }
        }
        return list;
	}

	public void UpdateUniteCommodity(YxPoolCommodity ypc) {
		 sqlSession.update("updateYxPoolCommodityOnline", ypc);
	}

}
