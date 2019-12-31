package com.feiniu.yx.pool.controller;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.interceptor.LogTrace;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.pool.service.YxPoolPeriodsService;
import com.feiniu.yx.pool.service.YxPoolPeriodsStoreService;
import com.feiniu.yx.pool.service.YxPoolService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.DateUtil;

@Controller
@RequestMapping("/yxPoolPeriods")
public class YxPoolPeriodsController {

    private static Logger logger = Logger.getLogger(YxPoolPeriodsController.class);

    @Autowired
    private YxPoolPeriodsService cppService;

    @Autowired
    private YxPoolService poolService;

    @Autowired
    private YxPoolPeriodsService poolPeriodsService;

    @Autowired
    private YxPoolPeriodsStoreService ppsService;

    @RequestMapping("/addSavePeriods")
    @LogTrace(msgFomort={"添加期数:{data}","pool","0","data:poolId"})
    public void addSavePeriods(HttpServletRequest request,
                               HttpServletResponse response,
                               @RequestParam(defaultValue = "0") String data){
        YxPoolPeriods poolPeriod = (YxPoolPeriods)JSONObject.parseObject(data, YxPoolPeriods.class);
        int checked = cppService.checkPeriodDate(poolPeriod);
        if (checked > 0) {
            try {
                cppService.savePoolPeriod(poolPeriod);
                YxPoolPeriods cpp = new YxPoolPeriods();
                cpp.setPoolId(poolPeriod.getPoolId());
                cpp.setPageRows(Integer.MAX_VALUE);
                List<YxPoolPeriods> periodList = cppService.queryPeriodsList(cpp);
                ControllerUtil.writeJson(response, JSONObject.toJSONString(periodList));
            } catch (Exception e) {
                logger.error("池期数新建保存出错", e);
                ControllerUtil.writeJson(response, "池期数新建保存出错");
            }

        } else {
            ControllerUtil.writeJson(response, checked + "");
        }

    }

    //更新期数保存
    @RequestMapping("/updatePeriods")
    @LogTrace(msgFomort={"更新池期数:{data}","pool","0","data:poolId"})
    public void updatePeriods(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(defaultValue = "0") String data){

        YxPoolPeriods poolPeriod = (YxPoolPeriods)JSONObject.parseObject(data, YxPoolPeriods.class);

        int checked = cppService.checkPeriodDate(poolPeriod);
        if(checked>0){
            try{
                YxPoolPeriods cpp = new YxPoolPeriods();
                cpp.setPoolId(poolPeriod.getPoolId());
                cpp.setPageRows(Integer.MAX_VALUE);
                if(cppService.updatePoolPeriod(poolPeriod)>0){
                    List<YxPoolPeriods> periodList = cppService.queryPeriodsList(cpp);
                    ControllerUtil.writeJson(response, JSONObject.toJSONString(periodList));
                }
            }catch(Exception e){
                logger.error("池期数更新保存出错", e);
                ControllerUtil.writeJson(response, "池期数更新保存出错");
            }
        }else{
            ControllerUtil.writeJson(response, checked+"");
        }
    }


    @RequestMapping("/deletePeriods")
    @LogTrace(msgFomort={"删除池期数ID:{id}","period","{id}"})
    public void deletePeriods(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(defaultValue = "0") Long id){
        cppService.deletePeriodAll(id);
        JSONObject object = new JSONObject();
        object.put("result", "success");
        ControllerUtil.writeJson(response, object.toJSONString());
    }

    @RequestMapping("/addSaveMutiPeriods")
    @LogTrace(msgFomort="批量添加池期数")
    public void addSaveMutiPeriods(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestParam(defaultValue = "0") String data) {
        Map<String,String> jsonMap = (Map<String, String>) JSONObject.parse(data);
        Long poolId = Long.parseLong(jsonMap.get("poolId"));
        int timeCheck = checkBeginTimes(jsonMap);
        if(timeCheck==1){
            List<YxPoolPeriods> periodAddList = transPeriod(jsonMap);
            YxPoolPeriods cpp = new YxPoolPeriods();
            cpp.setPoolId(poolId);
            cpp.setPageRows(Integer.MAX_VALUE);
            List<YxPoolPeriods> periodList = cppService.queryPeriodsList(cpp);
            int checked = 1;
            for(YxPoolPeriods npp:periodAddList){//判断是否存在时间相同
                for(YxPoolPeriods opp:periodList){
                    if(npp.getBeginTime().equals(opp.getBeginTime())){
                        checked = 0;
                        break;
                    }
                }
            }
            if (checked > 0) {
                try {
                    for(YxPoolPeriods npp:periodAddList){
                        cppService.savePoolPeriod(npp);
                    }
                    List<YxPoolPeriods> periodListQquery = cppService.queryPeriodsList(cpp);
                    ControllerUtil.writeJson(response, JSONObject.toJSONString(periodListQquery));
                } catch (Exception e) {
                    logger.error("池期数新建保存出错", e);
                    ControllerUtil.writeJson(response, "池期数新建保存出错");
                }

            } else {
                ControllerUtil.writeJson(response, checked + "");
            }
        } else {
            ControllerUtil.writeJson(response, timeCheck + "");
        }
    }

    private int checkBeginTimes(Map<String, String> jsonMap) {
        String[] beginTimes = jsonMap.get("beginTime").split(",");
        String repeat = jsonMap.get("repeatTimes");
        String date = "";
        for(int i=0;i<beginTimes.length;i++){
            if(!"1".equals(repeat) && StringUtils.isNotBlank(beginTimes[i])){
                if(date .equals("")) date = beginTimes[i].substring(0, 10);
                else if(!date.equals(beginTimes[i].substring(0, 10))){
                    return -1;//非同一天日期
                }
            }
            for(int j=0;j<beginTimes.length;j++){
                if(beginTimes[i].equals(beginTimes[j]) && i!=j){
                    return 0;//有重复的
                }
            }
        }
        return 1;
    }

    private List<YxPoolPeriods> transPeriod(Map<String, String> jsonMap) {
        List<YxPoolPeriods> transList = new ArrayList<YxPoolPeriods>();
        String[] beginTimes = jsonMap.get("beginTime").split(",");
        String[] names = jsonMap.get("name").split(",");
        int times = Integer.parseInt(jsonMap.get("repeatTimes"));
        int spaceDay = Integer.parseInt(jsonMap.get("periodSpace"));
        Long poolId = Long.parseLong(jsonMap.get("poolId"));
        for(int i=0;i<beginTimes.length;i++){
            if(StringUtils.isNotBlank(beginTimes[i])){
                Date beginTime = DateUtil.getDate(beginTimes[i], "yyyy-MM-dd HH:mm:ss");
                for(int time=0;time<times;time++){
                    Calendar cl = Calendar.getInstance();
                    cl.setTime(beginTime);
                    cl.add(Calendar.DATE, time*spaceDay);
                    YxPoolPeriods p = new YxPoolPeriods();
                    p.setBeginTime(cl.getTime());
                    p.setName(names[i]);
                    p.setPoolId(poolId);
                    transList.add(p);
                }
            }
        }
        return transList;
    }

    public void processProvinceList(YxPoolPeriods poolPeriods,Map<String,YXStore> storeMap){
        if (poolPeriods.getStoreList()!= null && poolPeriods.getStoreList().size() > 0) {
            List<YxPoolPeriodsStore> list = new ArrayList<YxPoolPeriodsStore>();
            for(YxPoolPeriodsStore cpp :poolPeriods.getStoreList()){
                if(storeMap.get(cpp.getStoreCode())!=null){
                    list.add(cpp);
                }
            }
            poolPeriods.setStoreList(list);
        }
    }

    /**
     * 删除重复门店
     * @param request
     * @param response
     * @param id
     */
    @RequestMapping("/deletePeriodsStore")
    public void deletePeriodsStore(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(defaultValue = "0") Long pid,@RequestParam(defaultValue = "") String sid){
        ppsService.deletePeriodStore(pid, sid);
        ControllerUtil.writeJson(response);
    }

    /**
     * 批量删除重复门店
     * @param request
     * @param response
     * @param id
     */
    @RequestMapping("/checkPeriodStoreAndRestore")
    public void checkPeriodStoreAndRestore(HttpServletRequest request, HttpServletResponse response){
        String poolId = request.getParameter("poolId");
        String curPage = request.getParameter("curPage");
        String pageRows = request.getParameter("pageRows");
        if(StringUtils.isNotBlank(poolId)){
            long id = Long.parseLong(poolId);
            YxPool pool = poolService.queryPoolAndPeriodById(id);
            poolPeriodsService.checkPeriodStoreAndRestore(pool);
        }else{
            poolPeriodsService.checkPeriodStoreAndRestore(curPage,pageRows);
        }
        ControllerUtil.writeJson(response);
    }

    @RequestMapping("/copySavePeriods")
    @LogTrace(msgFomort={"复制期数:{data}","pool","0","data:qId"})
    public void copySavePeriods(HttpServletRequest request,
                                HttpServletResponse response,@RequestParam(defaultValue = "0") String data){
        YxPoolPeriods poolPeriod = (YxPoolPeriods)JSONObject.parseObject(data, YxPoolPeriods.class);
        List<YxPoolPeriods> yxPoolPeriods = new ArrayList<>();
        yxPoolPeriods.add(poolPeriod);
        YxPoolPeriods cpp = new YxPoolPeriods();
        cpp.setPoolId(poolPeriod.getPoolId());
        cpp.setPageRows(Integer.MAX_VALUE);
        List<YxPoolPeriods> periodList = cppService.queryPeriodsList(cpp);
        int checker=cppService.checkCopyPeriodDate(yxPoolPeriods,periodList,null);
        if(checker>0){
            poolPeriodsService.copyPeriodsById(poolPeriod);
            List<YxPoolPeriods> newPeriodList = cppService.queryPeriodsList(cpp);
            ControllerUtil.writeJson(response, JSONObject.toJSONString(newPeriodList));

        }else{
            ControllerUtil.writeJson(response, checker + "");
        }
    }

    @RequestMapping("/copySaveMutiPeriods")
    @LogTrace(msgFomort="批量复制池期数")
    public void copySaveMutiPeriods(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(defaultValue = "0") String data){
        Map<String,String> jsonMap = (Map<String, String>) JSONObject.parse(data);
        Long poolId = Long.parseLong(jsonMap.get("poolId"));
        int timeCheck = checkBeginTimes(jsonMap);
        if(timeCheck==1){
            List<YxPoolPeriods> periodAddList = copyTransPeriod(jsonMap);
            YxPoolPeriods cpp = new YxPoolPeriods();
            cpp.setPoolId(poolId);
            cpp.setPageRows(Integer.MAX_VALUE);
            List<YxPoolPeriods> periodListQquery = cppService.queryPeriodsList(cpp);
            int checker=cppService.checkCopyPeriodDate(periodAddList,periodListQquery,jsonMap.get("repeatTimes"));
            if(checker>0){
                for (YxPoolPeriods yxPoolPeriods : periodAddList) {
                    poolPeriodsService.copyPeriodsById(yxPoolPeriods);
                }
                List<YxPoolPeriods> newPeriodListQquery = cppService.queryPeriodsList(cpp);
                ControllerUtil.writeJson(response, JSONObject.toJSONString(newPeriodListQquery));
            }else {
                ControllerUtil.writeJson(response, checker + "");
            }
        }else {
            ControllerUtil.writeJson(response, timeCheck + "");
        }
    }

    private List<YxPoolPeriods> copyTransPeriod(Map<String, String> jsonMap) {
        List<YxPoolPeriods> transList = new ArrayList<YxPoolPeriods>();
        String[] beginTimes = jsonMap.get("beginTime").split(",");
        String[] names = jsonMap.get("name").split(",");
        int times = Integer.parseInt(jsonMap.get("repeatTimes"));
        int spaceDay = Integer.parseInt(jsonMap.get("periodSpace"));
        Long poolId = Long.parseLong(jsonMap.get("poolId"));
        for(int i=0;i<beginTimes.length;i++){
            if(StringUtils.isNotBlank(beginTimes[i])){
                Date beginTime = DateUtil.getDate(beginTimes[i], "yyyy-MM-dd HH:mm:ss");
                for(int time=0;time<times;time++){
                    Calendar cl = Calendar.getInstance();
                    cl.setTime(beginTime);
                    cl.add(Calendar.DATE, time*spaceDay);
                    YxPoolPeriods p = new YxPoolPeriods();
                    p.setBeginTime(cl.getTime());
                    p.setName(names[i]);
                    p.setPoolId(poolId);
                    p.setId(Long.parseLong(jsonMap.get("id")));
                    transList.add(p);
                }
            }
        }
        return transList;
    }
}
