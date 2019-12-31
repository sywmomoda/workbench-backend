package com.feiniu.yx.page.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.page.entity.Module;


@Repository
public class ModuleOnlineDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public Module queryModuleByID(Long id) {
		return sqlSession.selectOne("selectModuleById", id);
	}


	public List<Module> queryModulesByIds(String[] ids) {
		if(ids.length==0){
			return new ArrayList<Module>();
		}
		List<Module> list =  sqlSession.selectList("selectModulesByIds",ids);
		return list;
	}
	
	
	public void deleteModuleOnlineById(Long id) {
		sqlSession.delete("deleteModuleOnlineById", id);
	}
	
	


	public void insertOrUpdateCMSModuleOnline(Module onlineModule) {
		Module onlineM = sqlSession.selectOne("selectModuleOnlineById", onlineModule.getId());
		onlineModule.setCreateTime(new Date());
		onlineModule.setUpdateTime(new Date());
		if(onlineModule.getModuleCategory()==null){
			onlineModule.setModuleCategory(1);
		}
		if(onlineM==null){
			sqlSession.insert("insertModuleOnline",onlineModule);
		}else{
			sqlSession.update("updateModuleOnline", onlineModule);
		}
	}
}
