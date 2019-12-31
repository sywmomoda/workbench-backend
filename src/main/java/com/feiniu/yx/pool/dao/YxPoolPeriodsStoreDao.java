package com.feiniu.yx.pool.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.feiniu.b2b.store.entity.B2BStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;

@Repository
public class YxPoolPeriodsStoreDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public YxPoolPeriodsStore queryYxPoolPeriodsStoreByCode(Long periodsId,String code) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("periodsId", periodsId);
		map.put("storeCode", code);
		return sqlSession.selectOne("selectYxPoolPeriodsStoreByStoreCode", map);
	}
	
	public List<YxPoolPeriodsStore> listYxPoolPeriodsStoreByCode(Long periodsId,String code) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("periodsId", periodsId);
		map.put("storeCode", code);
		return sqlSession.selectList("selectYxPoolPeriodsStoreByStoreCode", map);
	}
	
	public List<YxPoolPeriodsStore> listPeriodsStoreByStoreCodes2PeriodsId(Long periodsId,String codes) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("periodsId", periodsId);
		map.put("storeCodes", codes.split(","));
		return sqlSession.selectList("selectPoolPeriodsStoreByStoreCodesAndPeriodsId", map);
	}
	
	/**
	 * 查询期数下所有门店
	 * @author lizhiyong
	 * 2017年2月15日
	 * @param periodsId
	 * @return
	 */
	public List<YxPoolPeriodsStore> queryStoreListByPeriodsId(Long periodsId) {
		return sqlSession.selectList("selectYxPoolPeriodsStoreByPeriodsId", periodsId);		
	}
	
	public List<YxPoolPeriodsStore> queryStoreList(YxPoolPeriodsStore pp) {
		return sqlSession.selectList("selectYxPoolPeriodsStoreList", pp);		
	}
	/**
	 * @Description 更新关联商品列表及排序
	 * @param p
	 *            池期数信息
	 */
	public void updateCommoditys(YxPoolPeriodsStore pv) {
		String commoditys = pv.getCommoditys();
		if(StringUtils.isNotBlank(commoditys)){
			String[] ids = commoditys.split(",");
			commoditys = trimJoin(ids,",");
			pv.setCommoditys(commoditys);
		}
		sqlSession.update("updateYxPoolPeriodsStoreCommoditys", pv);	
	}
	
	public void updateYxPoolPeriodsStoreCommoditys(YxPoolPeriodsStore pv) {
		sqlSession.update("updateYxPoolPeriodsStoreCommoditys", pv);
	}
	
	public long insertYxPoolPeriodsStore(YxPoolPeriodsStore content) {
		content.setCreateTime(new Date());
		sqlSession.insert("insertYxPoolPeriodsStore",content);
		return content.getId();
	}
	
	/**
	 * @Description 更新关联商品列表及排序
	 * @param p
	 *            池期数信息
	 */
	public void updateYxPoolPeriodsStore(YxPoolPeriodsStore pv) {
		sqlSession.update("updateYxPoolPeriodsStore", pv);	
	}
	

	public void deleteYxPoolPeriodsStore(Long id) {
		sqlSession.delete("deleteYxPoolPeriodsStore",id);
	}

	private String trimJoin(String[] array, String separator) {
		if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = ",";
        }
        int startIndex=0;
        int endIndex =array.length;
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return "";
        }
        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length())
                        + separator.length());
        StringBuffer buf = new StringBuffer(bufSize);
        for (int i = startIndex; i < endIndex; i++) {
        	if(StringUtils.isNotBlank(array[i])){
	            if (i > startIndex) {
	                buf.append(separator);
	            }
	            if (array[i] != null) {
	                buf.append(array[i]);
	            }
        	}
        }
        String com = buf.toString();
        if(com!=null && com.startsWith(",")){
        	com = com.substring(1);
        }
        return com;
    }

	public List<YxPoolPeriodsStore> findPeriodsStoreByOld(Long id) {
		return sqlSession.selectList("findPeriodsStoreByOld",id);
	}

	public void addyxPoolPeriodsStore(YxPoolPeriodsStore yxPoolPeriodsStore) {
		sqlSession.insert("addyxPoolPeriodsStore",yxPoolPeriodsStore);
	}

	public List<B2BStore> selectB2BStoreByNameOrCode(B2BStore store){
        return sqlSession.selectList("selectB2BStoreByNameCode",store);
    }
}
