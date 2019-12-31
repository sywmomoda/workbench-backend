package com.feiniu.yx.page.dao;

import com.feiniu.yx.page.entity.ModuleMultiTab;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ModuleMultiTabDao {
    @Autowired
    private SqlSession sqlSession;

    public Long insert(ModuleMultiTab multiTab){
        sqlSession.insert("insertModuleMultiTab",multiTab);
        return multiTab.getId();
    }

    public void update(ModuleMultiTab multiTab){
        sqlSession.update("updateModuleMultiTab",multiTab);
    }

    public ModuleMultiTab selectById(Long id){
        return sqlSession.selectOne("selectModuleMultiTabById",id);
    }

    public List<ModuleMultiTab> selectListByIds(String ids){
        return sqlSession.selectList("selectModuleMultiTabByIds",ids);
    }

    public void delete(Long id){
        sqlSession.delete("deleteModuleMultiTabById",id);
    }
}
