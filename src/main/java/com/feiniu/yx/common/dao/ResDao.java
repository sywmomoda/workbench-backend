package com.feiniu.yx.common.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.feiniu.yx.common.entity.Res;
import com.feiniu.yx.config.SystemEnv;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

/**
 * @author tongwenhuan
 * 2017年3月9日 下午5:16:19
 */
@Component
public class ResDao {

	@Autowired
	private SqlSession sqlSession;
	
	public List<Res> findList(Res res) {
		PageBounds pageBounds = new PageBounds(res.getCurPage(),res.getPageRows());
		List<Res> list = sqlSession.selectList("selectResListByRes", res,pageBounds);
		//获得结果集条总数
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		res.setTotalRows(pageList.getPaginator().getTotalCount());
		return list;
	}
	
	public List<Res> findAll(){
		Res res = new Res();
		res.setEnv(SystemEnv.getProperty("fn.env"));
		List<Res> list = sqlSession.selectList("selectResListByRes", res);
		return list;
	}
	
	public Res findOne(Long id) {
		return sqlSession.selectOne("selectResById", id);
	}
	
	public void addOne(Res res) {
		sqlSession.insert("insertRes", res);
	}
	
	public void updOneTemp(Res res) {
		sqlSession.update("updateResTemp", res);
	}
	
	public void updOneTemp(Long id) {
		sqlSession.update("updateResTemp2", id);
	}
	
	public void delOne(Long id) {
		sqlSession.delete("deleteRes", id);
	}
	
	public void updNameAndPath(Res res) {
		sqlSession.update("updNameAndPath", res);
	}
}
