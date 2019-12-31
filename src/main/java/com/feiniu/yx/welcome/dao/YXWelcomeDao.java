package com.feiniu.yx.welcome.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.welcome.entity.YXWelcome;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class YXWelcomeDao {
  @Autowired
  private SqlSession sqlSession;
  
  public List<YXWelcome> getWelcomeList(YXWelcome welcome){
	 PageBounds pageBounds = new PageBounds(welcome.getCurPage(),welcome.getPageRows());
	 List<YXWelcome> list =  sqlSession.selectList("selectAllYXWelcome", welcome,pageBounds);
	 //获得结果集条总数
	 @SuppressWarnings("rawtypes")
	 PageList pageList = (PageList)list;
	 welcome.setTotalRows(pageList.getPaginator().getTotalCount());
	 return list;
  }
  
  /**
   * 取结束时间大于当前时间的活动
   * @author tongwenhuan
   * 2017年3月31日
   * @return
   */
  public List<YXWelcome> getWelcomeListAfterNow(){
	  YXWelcome welcome = new YXWelcome();
	  welcome.setEndTime(new Date());
	  List<YXWelcome> list =  sqlSession.selectList("selectYXWelcomeAfterNow", welcome);
	  return list;
  }
  
  
  public Long insert(YXWelcome welcome){
	  sqlSession.insert("insertYXWelcome",welcome);
	  return welcome.getId();
  }
  
  public void updateYXWelComeStatus(Long id){
	  sqlSession.insert("updaetYXWelcomeStatus",id); 
  }
  
  public void delete(Long id){
	  sqlSession.insert("deleteYXWelcome",id); 
  }
  
  public YXWelcome getWelcomeById(Long id){
	 return sqlSession.selectOne("selectYXWelcomById", id);
  }
  
  public Long update(YXWelcome welcome){
	  long id =  sqlSession.insert("updateYXWelcome",welcome);
	  return id;
  }
  
}
