package com.feiniu.yx.welcome.dao;

import com.feiniu.yx.welcome.entity.OsWelcome;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @ClassName OsWelcomeDao
 * @Decription TODO
 * @Author shiyouwei
 * @Date 15:25 2019/10/30
 */

@Repository
public class OsWelcomeDao {
    @Autowired
    private SqlSession sqlSession;

    public List<OsWelcome> getWelcomeList(OsWelcome welcome){
        PageBounds pageBounds = new PageBounds(welcome.getCurPage(),welcome.getPageRows());
        List<OsWelcome> list =  sqlSession.selectList("selectAllOsWelcome", welcome,pageBounds);
        //获得结果集条总数
        @SuppressWarnings("rawtypes")
        PageList pageList = (PageList)list;
        welcome.setTotalRows(pageList.getPaginator().getTotalCount());
        return list;
    }

    public List<OsWelcome> getWelcomeListAfterNow(){
        OsWelcome welcome = new OsWelcome();
        welcome.setEndTime(new Date());
        List<OsWelcome> list =  sqlSession.selectList("selectOsWelcomeAfterNow", welcome);
        return list;
    }


    public Long insert(OsWelcome welcome){
        sqlSession.insert("insertOsWelcome",welcome);
        return welcome.getId();
    }

    public void updateOsWelcomeStatus(Long id){
        sqlSession.insert("updaetOsWelcomeStatus",id);
    }

    public void delete(Long id){
        sqlSession.insert("deleteOsWelcome",id);
    }

    public OsWelcome getWelcomeById(Long id){
        return sqlSession.selectOne("selectOsWelcomById", id);
    }

    public Long update(OsWelcome welcome){
        long id =  sqlSession.insert("updateOsWelcome",welcome);
        return id;
    }
}
