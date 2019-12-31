package com.feiniu.yx.page.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.YxRemoteCommodity;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class YxRemoteCommodityDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public YxRemoteCommodity queryYxRemoteCommodityByID(Long id) {
		return sqlSession.selectOne("selectYxRemoteCommodityById", id);
	}

	public List<YxRemoteCommodity> queryYxRemoteCommodityByTypeAndCode(String type,String code) {
		YxRemoteCommodity yrc = new YxRemoteCommodity();
		yrc.setType(type);
		yrc.setStoreCode(code);
		List<YxRemoteCommodity> list =  sqlSession.selectList("selectYxRemoteCommodityTypeAndCode",yrc);
		return list;
	}
	
	public List<YxRemoteCommodity> queryYxRemoteCommodityByIds(String[] ids) {
		if(ids.length==0){
			return new ArrayList<YxRemoteCommodity>();
		}
		List<YxRemoteCommodity> list =  sqlSession.selectList("selectYxRemoteCommodityByIds",ids);
		return list;
	}
	
	public void deleteYxRemoteCommodityByID(Long id) {
		sqlSession.delete("deleteYxRemoteCommodityById", id);
	}
	
	
	public Long insertYxRemoteCommodity(YxRemoteCommodity module){
		module.setUpdateTime(new Date());
		sqlSession.insert("insertYxRemoteCommodity",module);
		return module.getId();
	}

	public void saveList(List<YxRemoteCommodity> rcList) {
		sqlSession.insert("insertYxRemoteCommodityByBatch", rcList);
		
	}
	
	public void deleteYxRemoteCommodityByTypeAndCode(String type,String code) {
		YxRemoteCommodity yrc = new YxRemoteCommodity();
		yrc.setType(type);
		yrc.setStoreCode(code);
		sqlSession.delete("deleteYxRemoteCommodityByTypeAndCode", yrc);
		
	}
	
	

}
