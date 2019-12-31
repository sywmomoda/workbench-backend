package com.feiniu.yx.page.dao;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.feiniu.yx.page.entity.AliVideo;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class AliVideoDao {
	@Autowired
	private SqlSession sqlSession;
	
	public  List<AliVideo> list(AliVideo video){
		PageBounds pageBounds = new PageBounds(video.getCurPage(),video.getPageRows());
		List<AliVideo> list = sqlSession.selectList("list", video,pageBounds);
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		video.setTotalRows(pageList.getPaginator().getTotalCount());
		video.setPageAmount(pageList.getPaginator().getPage());
		return list;
	}
	
	public void insert(AliVideo video) {
		sqlSession.insert("insert", video);
	}
	
	public AliVideo getVideoById(Long id) {
		return sqlSession.selectOne("videoById", id);
	}
	
	public void update(AliVideo video) {
		sqlSession.update("update", video);
	}
	
	public void delete(Long id) {
		sqlSession.delete("delete", id);
	}
	
	public Long getMaxNum() {
		Long manNum= sqlSession.selectOne("maxNum");
		return manNum;
	}
}
