package com.feiniu.yx.welcome.service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.welcome.entity.OsWelcome;

import java.util.List;

/**
 * @ClassName OsWelcomeService
 * @Decription TODO
 * @Author shiyouwei
 * @Date 15:36 2019/10/30
 */
public interface OsWelcomeService {

    List<OsWelcome> getWelcomeList(OsWelcome welcome);

    JSONObject checkDate(OsWelcome welcome);

    void updateOsWelcomeStatus(Long id);

    void deleteOsWelcome(Long id);

    OsWelcome getWelcomeById(Long id);

    void insertOsWelcome(String data);

    JSONObject getWelcomeOfImgs(Long id);

    void updateOsWelcome(String data);
}
