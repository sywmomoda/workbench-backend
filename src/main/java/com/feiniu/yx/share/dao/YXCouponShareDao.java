package com.feiniu.yx.share.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.share.entiry.YXCouponShare;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class YXCouponShareDao {

	@Autowired
	private SqlSession sqlSession;
	
	public void insert(YXCouponShare couponShare){
		sqlSession.insert("insertCouponShare", couponShare);
	}
	
	public YXCouponShare queryCouponShareById(Long id){
		return sqlSession.selectOne("selectCouponShareById", id);
	}
	
	public void update(YXCouponShare couponShare){
		sqlSession.update("updateCouponShare",couponShare);
	}
	
	public List<YXCouponShare> queryCouponShareList(YXCouponShare couponShare){
		PageBounds pageBounds = new PageBounds(couponShare.getCurPage(),couponShare.getPageRows());
		List<YXCouponShare>  list= sqlSession.selectList("queryCouponShareList",couponShare,pageBounds);
		//获得结果集条总数
		@SuppressWarnings("rawtypes")
		PageList pageList = (PageList)list;
		couponShare.setTotalRows(pageList.getPaginator().getTotalCount());
		return list;
	}
}
