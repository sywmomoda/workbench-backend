package com.feiniu.yx.page.dao;

import com.feiniu.yx.page.entity.ModuleMultiTab;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ModuleMultiTabOnlineDao {
    @Autowired
    private SqlSession sqlSession;

    public void insert(List<ModuleMultiTab> list){
        sqlSession.insert("insertModuleMultiTabOnline",list);
    }


    public void delete(String[] ids){
        sqlSession.delete("deleteModuleMultiTabOnlineById",ids);
    }
}
