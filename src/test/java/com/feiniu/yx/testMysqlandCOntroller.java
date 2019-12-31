package com.feiniu.yx;

import com.feiniu.yx.welcome.entity.OsWelcome;
import com.feiniu.yx.welcome.entity.YXWelcome;
import com.feiniu.yx.welcome.entity.test.OsWelcomeTest;
import com.feiniu.yx.welcome.service.OsWelcomeService;
import com.feiniu.yx.welcome.service.YXWelcomeService;
import com.feiniu.yx.welcome.service.test.OsTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName testMysqlandCOntroller
 * @Decription TODO
 * @Author shiyouwei
 * @Date 11:00 2019/10/31
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext.xml")
public class testMysqlandCOntroller {

    @Autowired
    YXWelcomeService yxWelcomeService;


    @Autowired
    OsTest osTest;

    @Autowired
    OsWelcomeService osWelcomeService;

    /**
     * 测试是否连接到数据库
     */
    @Test
    public void test(){
        YXWelcome yxWelcome = new YXWelcome();

        /**
         * 查询所有数据
         */
        List<YXWelcome> list = yxWelcomeService.getWelcomeList(yxWelcome);

        for(YXWelcome yx:list){
            System.out.println(yx.getId()+" !!!"+yx.getName());
        }


    }




    /**
     * 测试mybatis generator
     * mybatisDAO mapper动态代理方式
     */
    @Test
    public void test1(){
        OsWelcomeTest osWelcomeTest = null;
        List<OsWelcomeTest> list = osTest.getListTest("hi");
//        JSONArray array = JSONArray.fromObject(list);
//        System.out.println(array.toString());

        Iterator it = list.iterator();
        while (it.hasNext()){
            osWelcomeTest = (OsWelcomeTest) it.next();
            System.out.println(osWelcomeTest.getName()+"||"+osWelcomeTest.getId());
        }
    }


    @Test
    public void test2(){
        OsWelcomeTest osWelcomeTest = osTest.getByNameEqualsTo("jack");
        //System.out.println(osWelcomeTest.getName()+"   "+osWelcomeTest.getId()+"time="+osWelcomeTest.getBeginTime());
        System.out.println("time = "+osWelcomeTest.getBeginTime() + " id = "+osWelcomeTest.getId());
        String strDate = osWelcomeTest.getBeginTime();
        System.out.println("getTime()= "+strDate);
        Date now = new Date();
        SimpleDateFormat si = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowtime = si.format(now);

//        Date d1 = null;
//        try {
//            d1 = si.parse(nowtime);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        System.out.println("现在时间String "+nowtime+"现在时间Date: "+now.toString());
        String s = "1999-01-26";
        java.sql.Date date = java.sql.Date.valueOf(s);
        System.out.println("转为标准Date: "+date);
        OsWelcome os = osWelcomeService.getWelcomeById((long) 40);
        System.out.println("time  =  "+os.getBeginTime() + " id = "+os.getId());


    }


    /**
     * 插入
     */
    @Test
    public void test3(){
        String s = "1999-01-26 12:13:41";

        //java.sql.Date 只有日期（yyyy-MM-dd），没有时间，所以使用java.sql.Timestamp
        java.sql.Timestamp time = java.sql.Timestamp.valueOf(s);

        OsWelcomeTest osWelcomeTest1 = new OsWelcomeTest();
        osWelcomeTest1.setBeginTime("1988-01-01 12:12:12");
        osWelcomeTest1.setId((long) 99);
        osWelcomeTest1.setName("李华");
        osWelcomeTest1.setEndTime("1989-01-01 00:00:00");
        osWelcomeTest1.setShowTime("2");
        osWelcomeTest1.setStatus((byte) 1);
        osWelcomeTest1.setCreateId("Ruby");
        osWelcomeTest1.setCreateTime(time);
        osWelcomeTest1.setUpdateId("Ruby");
        osWelcomeTest1.setUpdateTime(time);
        osTest.insert(osWelcomeTest1);
    }


}
