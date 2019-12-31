package com.feiniu.yx.welcome.dao.test;

import com.feiniu.yx.welcome.entity.test.OsWelcomeTest;
import com.feiniu.yx.welcome.entity.test.OsWelcomeTestExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

public interface OsWelcomeDAOTest {
    long countByExample(OsWelcomeTestExample example);

    int deleteByExample(OsWelcomeTestExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OsWelcomeTest record);

    int insertSelective(OsWelcomeTest record);

    List<OsWelcomeTest> selectByExample(OsWelcomeTestExample example);

    OsWelcomeTest selectByPrimaryKey(Long id);

    OsWelcomeTest selectByName(OsWelcomeTestExample example);

    int updateByExampleSelective(@Param("record") OsWelcomeTest record, @Param("example") OsWelcomeTestExample example);

    int updateByExample(@Param("record") OsWelcomeTest record, @Param("example") OsWelcomeTestExample example);

    int updateByPrimaryKeySelective(OsWelcomeTest record);

    int updateByPrimaryKey(OsWelcomeTest record);
}