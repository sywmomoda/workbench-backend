package com.feiniu.b2b.pool.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.feiniu.b2b.pool.entity.B2BPoolCommodity;

@Repository
public class B2BPoolCommodityDao{

    @Autowired
    private SqlSession sqlSession;

    public B2BPoolCommodity queryB2BPoolCommodityByID(Long id) {
        return sqlSession.selectOne("selectB2BPoolCommodityById", id);
    }

    public long insertB2BPoolCommodity(B2BPoolCommodity cpc) {
        sqlSession.insert("insertB2BPoolCommodity", cpc);
        return cpc.getId();
    }

    public void updateB2BPoolCommodityFromPool(B2BPoolCommodity cpc) {
        sqlSession.update("updateB2BPoolCommodityFromPool", cpc);
    }
    
    public void updateB2BPoolPicCommodityFromPool(B2BPoolCommodity cpc) {
        sqlSession.update("updateB2BPoolPicCommodityFromPool", cpc);
    }

    public void updateB2BPoolCommodity(B2BPoolCommodity cpc) {
        sqlSession.update("updateB2BPoolCommodity", cpc);
    }

    public B2BPoolCommodity getCommodity(String commodityId, Long periodId) {
    	B2BPoolCommodity c = null;
        if (StringUtils.isBlank(commodityId) || periodId == null) {
            return c;
        }
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("commodityId", commodityId);
        m.put("periodId", periodId);
        List<B2BPoolCommodity> temp = sqlSession.selectList("selectB2BPoolCommodityByCommodityIdAndPeriodId", m);
        if (temp.size() > 0) {
            c = temp.get(0);
        }
        return c;
    }

    /**
     * @Description 根据ID查询所有商品列表
     * @param poolCommodity
     * @return
     */
    public List<B2BPoolCommodity> getB2BPoolCommodityByIds(String ids) {
        List<B2BPoolCommodity> list = new ArrayList<B2BPoolCommodity>();
        if (StringUtils.isEmpty(ids)) {
            return list;
        }
        String[] cids = ids.split(",");
        if(cids.length==0){
        	 return list;
        }
        List<B2BPoolCommodity> temp = sqlSession.selectList("selectB2BPoolCommodityByIds", cids);
        Map<String, B2BPoolCommodity> map = new HashMap<String, B2BPoolCommodity>();
        for (B2BPoolCommodity c : temp) {
        	if (c.getPicUrl() != null && !c.getPicUrl().startsWith("http:")) {
//                c.setPicUrl(CmsUtil.getImgHost() + c.getPicUrl());
            }
            map.put(c.getId().toString(), c);
        }
        // 重新排序
        for (String id : cids) {
        	B2BPoolCommodity c2 = map.get(id);
            if (c2 != null) {
                list.add(c2);
            }
        }
        return list;
    }
    
    public Map<String, B2BPoolCommodity>  getMapCommodityByIds(String ids) {
    	 Map<String, B2BPoolCommodity> m = new HashMap<String, B2BPoolCommodity>();
        if (StringUtils.isEmpty(ids)) {
            return m;
        }
        String[] cids = ids.split(",");
        if(cids.length==0){
        	 return m;
        }
        List<B2BPoolCommodity> temp = sqlSession.selectList("selectB2BPoolCommodityByIds", cids);
        for (B2BPoolCommodity c : temp) {
            m.put(c.getCommodityId(), c);
        }
        return m;
    }

    /**
     * 批量删除商品
     * 
     * @param id
     * @author tongwenhuan
     */
    public void deleteB2BPoolCommodityById(String id) {
        sqlSession.delete("deleteB2BPoolCommodityById", id);
    }

    
    public List<B2BPoolCommodity> queryB2BPoolCommodityByIds(String ids) {
    	List<B2BPoolCommodity> list = new ArrayList<B2BPoolCommodity>();
        if (StringUtils.isEmpty(ids)) {
            return list;
        }
        String[] cids = ids.split(",");
        List<B2BPoolCommodity> temp = sqlSession.selectList("selectB2BPoolCommodityByIds", cids);
        Map<String, B2BPoolCommodity> map = new HashMap<String, B2BPoolCommodity>();
        for (B2BPoolCommodity c : temp) {
            map.put(c.getId().toString(), c);
        }
        // 重新排序
        for (String id : cids) {
        	B2BPoolCommodity c2 = map.get(id);
            if (c2 != null) {
                list.add(c2);
            }
        }
    	return list;
    }

}
