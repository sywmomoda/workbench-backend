package com.feiniu.yx.common.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.common.entity.YXLog;
import com.github.miemiedev.mybatis.paginator.domain.Order;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;

@Repository
public class YXLogDao {

	 @Autowired
	 private SqlSession sqlSession;
	 
	 public void addYXLog(YXLog yXLog) {
		 sqlSession.insert("insertYXLog", yXLog);
	 }
	
	 public List<YXLog> queryLogs(YXLog yXLog) {
		 PageBounds pageBounds = new PageBounds(yXLog.getCurPage(),yXLog.getPageRows(),Order.formString("id.desc"));
		 List<YXLog> logs = sqlSession.selectList("selectYXLogs", yXLog,pageBounds);
		 PageList<YXLog> pageList = (PageList<YXLog>)logs;
		 yXLog.setTotalRows(pageList.getPaginator().getTotalCount());
		 return logs;
	 }
	 
	 public void deleteLogs(YXLog yXLog){
		 sqlSession.delete("deleteLogs", yXLog);
	 }
	 
}
