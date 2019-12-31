package com.feiniu.yx.page.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class ModuleProperPlusOnlineDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public ModuleProperPlus queryModuleProperByID(Long id) {
		return sqlSession.selectOne("selectModuleProperPlusOnlineById", id);
	}

	public List<ModuleProperPlus> queryModulesByModuleId(Long moduleId) {
		List<ModuleProperPlus> list =  sqlSession.selectList("selectModuleProperPlusOnlineByModuleId",moduleId);
		return list;
	}
	
	public void deleteModuleProperByModuleId(Long moduleId) {
		sqlSession.delete("deleteModuleProperPlusOnlineByModuleId", moduleId);
	}
	
	public void deleteModuleProperById(Long id) {
		sqlSession.delete("deleteModuleProperPlusOnlineById", id);
	}
	
	public Long insertModuleProper(ModuleProperPlus modulep){
		sqlSession.insert("insertModuleProperPlusOnline",modulep);
		return modulep.getId();
	}
	
	public int updateModuleProper(ModuleProperPlus modulep){
		modulep.setUpdateTime(new Date());
		return sqlSession.update("updateModuleProperPlusOnline",modulep);
	}
	
	public void insertOrUpdateCMSModuleOnline(ModuleProperPlus onlineModulePP) {
		ModuleProperPlus onlineMPP = sqlSession.selectOne("selectModuleProperPlusOnlineById", onlineModulePP.getId());
		if(onlineMPP==null){
			sqlSession.insert("insertModuleProperPlusOnline",onlineModulePP);
		}else{
			sqlSession.update("updateModuleProperPlusOnline", onlineModulePP);
		}
	}
}
