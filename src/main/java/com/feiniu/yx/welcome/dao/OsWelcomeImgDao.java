package com.feiniu.yx.welcome.dao;

import com.feiniu.yx.welcome.entity.OsWelcomeImg;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName OsWelcomeImgDao
 * @Decription TODO
 * @Author shiyouwei
 * @Date 15:26 2019/10/30
 */

@Repository
public class OsWelcomeImgDao {

    @Autowired
    private SqlSession sqlSession;

    public void insertBatch(List<OsWelcomeImg> list){
        sqlSession.insert("insertBatchOsWelcomeImg",list);
    }

    public List<OsWelcomeImg> getOsWelcomeImgsOfByOsWelcomeId(Long welcomeId){
        return sqlSession.selectList("selectOsWelcomeImgByWelcomeId",welcomeId);
    }

    public void deleteOsWelcomeImgsOfByOsWelcomeId(Long welcomeId){
        sqlSession.selectList("deleteOsWelcomeImgByWelcomeId",welcomeId);
    }
}
