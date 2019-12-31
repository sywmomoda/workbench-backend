package com.feiniu.yx.template.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.template.entity.YXModuleType;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;


@Repository
public class YXModuleTypeDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public List<YXModuleType> getYXModelTypeList(YXModuleType module){
		PageBounds pageBounds = new PageBounds(module.getCurPage(),module.getPageRows());
		List<YXModuleType> list = sqlSession.selectList("selectAllYXModuleType", module,pageBounds);
		//获得结果集条总数
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		module.setTotalRows(pageList.getPaginator().getTotalCount());
		return list;
	}
	
	public void insert(YXModuleType module){
		sqlSession.insert("insertYXModuleType",module);
	}
	
	public void upate(YXModuleType module){
		sqlSession.update("updateYXModuleType",module);
	}
	
	public YXModuleType getYXModuleTypeById(Long Id){
		return sqlSession.selectOne("selectYXModuleTypeById", Id);
	}

	public List<YXModuleType> queryModuleTypesByIds(String moduleTypeIds) {
		if (!"".equals(moduleTypeIds)) {
			if(moduleTypeIds.endsWith(",")){
				moduleTypeIds = moduleTypeIds.substring(0,moduleTypeIds.length()-1);
			}
			Map<String, Object> param=new HashMap<String, Object>();             
            param.put("moduleTypeIds", moduleTypeIds);
            return sqlSession.selectList("selectYXModuleTypeListByIds", param);
       } else {
           return new ArrayList<YXModuleType>();
       }
	}
	
	
}
