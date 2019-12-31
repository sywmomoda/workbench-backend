package com.feiniu.b2b.share.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.b2b.share.entiry.B2BCouponShare;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class B2BCouponShareDao {

	@Autowired
	private SqlSession sqlSession;
	
	public void insert(B2BCouponShare couponShare){
		sqlSession.insert("insertB2BCouponShare", couponShare);
	}
	
	public B2BCouponShare queryCouponShareById(Long id){
		return sqlSession.selectOne("selectB2BCouponShareById", id);
	}
	
	public void update(B2BCouponShare couponShare){
		sqlSession.update("updateB2BCouponShare",couponShare);
	}
	
	public List<B2BCouponShare> queryCouponShareList(B2BCouponShare couponShare){
		PageBounds pageBounds = new PageBounds(couponShare.getCurPage(),couponShare.getPageRows());
		List<B2BCouponShare>  list= sqlSession.selectList("queryB2BCouponShareList",couponShare,pageBounds);
		//获得结果集条总数
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		couponShare.setTotalRows(pageList.getPaginator().getTotalCount());
		return list;
	}
}
