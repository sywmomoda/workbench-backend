package com.feiniu.yx.page.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.page.entity.Module;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class ModuleDao {
	
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
	
	public void deleteModuleByID(Long id) {
		sqlSession.delete("deleteModuleById", id);
	}
	
	public void deleteModuleOnlineById(Long id) {
		sqlSession.delete("deleteModuleOnlineById", id);
	}
	
	public Long insertModule(Module module){
		if(module.getModuleCategory()==null){
			module.setModuleCategory(1);
		}
		module.setUpdateTime(new Date());
		sqlSession.insert("insertModule",module);
		return module.getId();
	}
	
	public int updateModule(Module module){
		if(module.getModuleCategory()==null){
			module.setModuleCategory(1);
		}
		module.setUpdateTime(new Date());
		return sqlSession.update("updateModule",module);
	}
	
	public List<Module> getModuleList(Map module){
		return sqlSession.selectList("selectModulesForValidate",module);
	}
	
	public List<Module> getModuleList(Module module){
		
		PageBounds pageBounds = new PageBounds(module.getCurPage(),module.getPageRows());
		List<Module> list =sqlSession.selectList("selectModuleAll", module,pageBounds);  
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		module.setTotalRows(pageList.getPaginator().getTotalCount());
		module.setPageAmount(pageList.getPaginator().getPage());
		return list;	
	}

}
