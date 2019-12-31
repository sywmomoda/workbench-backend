package com.feiniu.yx.page.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.page.entity.YxCommodityRepeatCtl;

@Repository
public class YxCommodityRepeatCtlDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public YxCommodityRepeatCtl queryYxCommodityRepeatCtlByID(Long id) {
		return sqlSession.selectOne("selectYxCommodityRepeatCtlById", id);
	}

	public List<YxCommodityRepeatCtl> queryYxCommodityRepeatCtlByTypeAndCode(YxCommodityRepeatCtl yrc) {
		List<YxCommodityRepeatCtl> list =  sqlSession.selectList("selectYxCommodityRepeatCtlByCodeDate",yrc);
		return list;
	}
	
	public void deleteYxCommodityRepeatCtlByID(Long id) {
		sqlSession.delete("deleteYxCommodityRepeatCtlById", id);
	}
	
	
	public Long insertYxCommodityRepeatCtl(YxCommodityRepeatCtl yrc){
		yrc.setUpdateTime(new Date());
		sqlSession.insert("insertYxCommodityRepeatCtl",yrc);
		return yrc.getId();
	}

	public void saveList(List<YxCommodityRepeatCtl> rcList) {
		sqlSession.insert("insertYxCommodityRepeatCtlByBatch", rcList);
		
	}
	
	public void deleteYxCommodityRepeatCtlByTypeAndCode(YxCommodityRepeatCtl yrc) {
		sqlSession.delete("deleteYxCommodityRepeatCtlByTypeAndCode", yrc);
		
	}

	public void updateCommdityRepeatCtl(YxCommodityRepeatCtl crc) {
		sqlSession.update("updateYxCommodityRepeatCtl", crc);
	}
	
	

}
