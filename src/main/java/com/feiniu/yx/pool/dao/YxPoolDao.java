package com.feiniu.yx.pool.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.pool.entity.YxPool;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class YxPoolDao{

    @Autowired
    private SqlSession sqlSession;

    public YxPool queryB2BPoolById(Long id) {
    	return queryYxPoolById(id);
    }

    public YxPool queryYxPoolById(Long id) {
        YxPool o = sqlSession.selectOne("selectYxPoolById", id);
        return o;
    }

    /**
     * @Description: 按条件查询池信息
     * @param cmsPool
     *            查询条件对象
     * @return 池列表
     * @throws
     */
    public List<YxPool> queryYxPoolList(YxPool pool) {
    	if(pool == null)return null;
        // 设置需要查询的条件，如果为空则不匹配
        PageBounds pageBounds = new PageBounds(pool.getCurPage(),pool.getPageRows());
		List<YxPool> list = sqlSession.selectList("selectYxPools", pool, pageBounds);
		//获得结果集条总数
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		pool.setTotalRows(pageList.getPaginator().getTotalCount());
		pool.setPageAmount(pageList.getPaginator().getTotalPages());
		return list;
    }
    
    public List<YxPool> queryAll() {
    	return sqlSession.selectList("selectAllPool");
    }
    
    public List<YxPool> queryB2BPoolList(YxPool pool) {
    	return queryYxPoolList(pool);
    }
    
	public int deleteYxPool(Long id) {
		return sqlSession.delete("deleteYxPool", id);
	}
	
	public int deleteB2BPool(Long id) {
		return deleteYxPool(id);
	} 
	
	public Long insertB2BPool(YxPool content) {
		return insertYxPool(content);
	}
	
	public Long insertYxPool(YxPool content) {
		sqlSession.insert("insertYxPool",content);
		return content.getId();
	}
	
	public int updateYxPool(YxPool content) {
		return sqlSession.update("updateYxPool", content);
	}
}
