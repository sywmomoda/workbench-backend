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
public class YxPoolCommodityDao{

    @Autowired
    private SqlSession sqlSession;


    public YxPoolCommodity queryYxPoolCommodityByID(Long id) {
        return sqlSession.selectOne("selectYxPoolCommodityById", id);
    }

    public long insertYxPoolCommodity(YxPoolCommodity cpc) {
        sqlSession.insert("insertYxPoolCommodity", cpc);
        return cpc.getId();
    }

    public void updateYxPoolCommodityFromPool(YxPoolCommodity cpc) {
        sqlSession.update("updateYxPoolCommodityFromPool", cpc);
    }
    
    public void updateYxPoolPicCommodityFromPool(YxPoolCommodity cpc) {
        sqlSession.update("updateYxPoolPicCommodityFromPool", cpc);
    }

    public void updateYxPoolCommodity(YxPoolCommodity cpc) {
        sqlSession.update("updateYxPoolCommodity", cpc);
    }

    public YxPoolCommodity getCommodity(String commodityId, Long periodId) {
        YxPoolCommodity c = null;
        if (StringUtils.isBlank(commodityId) || periodId == null) {
            return c;
        }
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("commodityId", commodityId);
        m.put("periodId", periodId);
        List<YxPoolCommodity> temp = sqlSession.selectList("selectYxPoolCommodityByCommodityIdAndPeriodId", m);
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
    public List<YxPoolCommodity> getYxPoolCommodityByIds(String ids) {
        List<YxPoolCommodity> list = new ArrayList<YxPoolCommodity>();
        if (StringUtils.isEmpty(ids)) {
            return list;
        }
        String[] cids = ids.split(",");
        if(cids.length==0){
        	 return list;
        }
        List<YxPoolCommodity> temp = sqlSession.selectList("selectYxPoolCommodityByIds", cids);
        Map<String, YxPoolCommodity> map = new HashMap<String, YxPoolCommodity>();
        for (YxPoolCommodity c : temp) {
        	if (c.getPicUrl() != null && !c.getPicUrl().startsWith("http:")) {
//                c.setPicUrl(CmsUtil.getImgHost() + c.getPicUrl());
            }
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
    
    
    /**
     * 查询商品map,key为值商品ID
     * @param ids
     * @return
     */
    public Map<String, YxPoolCommodity> getMapCommodityByIds(String ids) {
        Map<String, YxPoolCommodity> map = new HashMap<String, YxPoolCommodity>();

    	if (StringUtils.isEmpty(ids)) {
            return map;
        }
        String[] cids = ids.split(",");
        if(cids.length==0){
        	 return map;
        }
        List<YxPoolCommodity> temp = sqlSession.selectList("selectYxPoolCommodityByIds", cids);
        for (YxPoolCommodity c : temp) {
            map.put(c.getCommodityId(), c);
        }
        return map;
    }
    

    /**
     * 批量删除商品
     * 
     * @param id
     * @author tongwenhuan
     */
    public void deleteYxPoolCommodityById(String id) {
        sqlSession.delete("deleteYxPoolCommodityById", id);
    }

    
    public List<YxPoolCommodity> queryYxPoolCommodityByIds(String ids) {
    	List<YxPoolCommodity> list = new ArrayList<YxPoolCommodity>();
        if (StringUtils.isEmpty(ids)) {
            return list;
        }
        String[] cids = ids.split(",");
        List<YxPoolCommodity> temp = sqlSession.selectList("selectYxPoolCommodityByIds", cids);
        Map<String, YxPoolCommodity> map = new HashMap<String, YxPoolCommodity>();
        for (YxPoolCommodity c : temp) {
            map.put(c.getId().toString(), c);
        }
        // 重新排序
        for (String id : cids) {
            YxPoolCommodity c2 = map.get(id);
            if (c2 != null) {
                list.add(c2);
            }
        }
    	return list;
    }

    public YxPoolCommodity findByCommodityId(String id) {
        return sqlSession.selectOne("findByCommodityId",id);
    }

    public void updateStoreAndGroupIds(YxPoolCommodity cpc){
       sqlSession.update("updateStoreAndGroupIdsOfYxPoolCommodity",cpc);
    }
}
