package com.feiniu.yx.welcome.service.test;

import com.feiniu.yx.welcome.dao.test.OsWelcomeDAOTest;
import com.feiniu.yx.welcome.entity.test.OsWelcomeTest;
import com.feiniu.yx.welcome.entity.test.OsWelcomeTestExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName OsTest
 * @Decription TODO
 * @Author shiyouwei
 * @Date 16:58 2019/12/11
 */


@Service
public class OsTest {
    @Autowired
    private OsWelcomeDAOTest osWelcomeDAOTest;

    /**
     * 模糊查询
     * @param name
     * @return
     */
    public List<OsWelcomeTest> getListTest(String name){
        OsWelcomeTestExample example = new OsWelcomeTestExample();
//        OsWelcomeTestExample.Criteria criteria = example.createCriteria();

        //设置查询条件
        example.createCriteria().andNameLike(name);


        //设置排序条件 默认升序
//        String order = "id DESC";
//        example.setOrderByClause(order);

        return osWelcomeDAOTest.selectByExample(example);
    }


    /**
     * 条件查询 name
     * @param name1
     * @return
     */
    public OsWelcomeTest getByNameEqualsTo(String name1){
        OsWelcomeTestExample ex = new OsWelcomeTestExample();
        OsWelcomeTestExample.Criteria criteria = ex.createCriteria();
        criteria.andNameEqualTo(name1);
        return osWelcomeDAOTest.selectByName(ex);
    }


    /**
     * 插入
     * @param osWelcomeTest
     */
    public void insert(OsWelcomeTest osWelcomeTest){
        osWelcomeDAOTest.insert(osWelcomeTest);
    }

}
