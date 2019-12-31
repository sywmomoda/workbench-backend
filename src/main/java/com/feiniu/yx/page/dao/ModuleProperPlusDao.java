package com.feiniu.yx.page.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.page.entity.ModuleProperPlus;

@Repository
public class ModuleProperPlusDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public ModuleProperPlus queryModuleProperByID(Long id) {
		return sqlSession.selectOne("selectModuleProperPlusById", id);
	}

	public ModuleProperPlus queryModuleProperByIdAndStoreCode(ModuleProperPlus modulep) {
		return sqlSession.selectOne("selectModuleProperPlusByModuleIdAndStoreCode", modulep);
	}
	
	public List<ModuleProperPlus> queryModulesByModuleId(Long moduleId) {
		List<ModuleProperPlus> list =  sqlSession.selectList("selectModuleProperPlusByModuleId",moduleId);
		return list;
	}
	
	public void deleteModuleProperByModuleId(Long moduleId) {
		sqlSession.delete("deleteModuleProperPlusByModuleId", moduleId);
	}
	
	public void deleteModuleProperById(Long id) {
		sqlSession.delete("deleteModuleProperPlusById", id);
	}
	
	public Long insertModuleProper(ModuleProperPlus modulep){
		modulep.setUpdateTime(new Date());
		sqlSession.insert("insertModuleProperPlus",modulep);
		return modulep.getId();
	}
	
	public int updateModuleProper(ModuleProperPlus modulep){
		return sqlSession.update("updateModuleProperPlus",modulep);
	}
}
