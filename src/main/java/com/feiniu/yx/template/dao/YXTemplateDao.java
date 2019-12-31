package com.feiniu.yx.template.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.feiniu.yx.template.entity.YXTemplate;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class YXTemplateDao {
	@Autowired
	private SqlSession sqlSession;
	
	public void insert(YXTemplate template){
		sqlSession.insert("insertYXTemplate",template);
	}
	
	public List<YXTemplate> getYXTemplateList(){
		return sqlSession.selectList("selectAllYXTemplate");
	}
	
	public List<YXTemplate> getYXTemplateList(YXTemplate template){
		PageBounds pageBounds = new PageBounds(template.getCurPage(),template.getPageRows());
		List<YXTemplate> list = sqlSession.selectList("selectAllYXTemplate", template,pageBounds);
		//获得结果集条总数
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		template.setTotalRows(pageList.getPaginator().getTotalCount());
		return list;
	}
	
	public YXTemplate getYXTemplateById(Long Id){
		return  sqlSession.selectOne("selectYXTemplateById",Id);
	}
	
	public void update(YXTemplate template){
		sqlSession.update("updateYXTemplate",template);
	}
}
