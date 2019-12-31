package com.feiniu.yx.welcome.controller;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.store.service.YXStoreGroupService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.DateUtil;
import com.feiniu.yx.welcome.entity.OsWelcome;
import com.feiniu.yx.welcome.service.OsWelcomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.List;

/**
 * @ClassName OsWelcomeController
 * @Decription TODO
 * @Author shiyouwei
 * @Date 15:21 2019/10/30
 */

@Controller
@RequestMapping("/OsWelcome")
public class OsWelcomeController {

    @Autowired
    private OsWelcomeService welcomeService;
    @Autowired
    private YXStoreGroupService sgService;
    @RequestMapping("queryScheduleList")
    public ModelAndView queryScheduleList(HttpServletRequest request,
                                          HttpServletResponse response,@ModelAttribute OsWelcome welcome){
        ModelAndView mv = new ModelAndView("oswelcome/queryList");
        List<OsWelcome> list = welcomeService.getWelcomeList(welcome);
        mv.addObject("obj", welcome);
        mv.addObject("list", list);
        return mv;
    }

    @RequestMapping("toAddWelcome")
    public ModelAndView toAddWelcome(HttpServletRequest request,
                                     HttpServletResponse response){
        ModelAndView mv = new ModelAndView("oswelcome/add");
        return mv;

    }

    @RequestMapping("checkDate")
    public void checkDate(HttpServletRequest request,
                          HttpServletResponse response,@ModelAttribute OsWelcome welcome){
        JSONObject object =  welcomeService.checkDate(welcome);
        ControllerUtil.writeJson(response, object.toJSONString());

    }

    @RequestMapping("toAddImg")
    public ModelAndView toAddImg(HttpServletRequest request,
                                 HttpServletResponse response,@ModelAttribute OsWelcome welcome){
        ModelAndView mv = new ModelAndView("oswelcome/addImg");
        mv.addObject("obj", welcome);
        String beginTime =  DateUtil.getDate(welcome.getBeginTime(),"yyyy-MM-dd HH:mm:ss");
        String endTime = DateUtil.getDate(welcome.getEndTime(),"yyyy-MM-dd HH:mm:ss");
        String groupIds = sgService.getGroupAllIds();
        String storeCode = sgService.getStoreCodesByGroupIds(groupIds);
        mv.addObject("beginTime",beginTime);
        mv.addObject("endTime",endTime);
        mv.addObject("storeCode",storeCode);
        mv.addObject("groupIds",groupIds);
        return mv;

    }

    @RequestMapping("addOrSave")
    public void addOrSave(HttpServletRequest request,
                          HttpServletResponse response,@RequestParam String data){
        welcomeService.insertOsWelcome(data);
        JSONObject object = new JSONObject();
        object.put("success", "ok");
        ControllerUtil.writeJson(response, object.toJSONString());
    }

    @RequestMapping("updateOsWelcomeStatus")
    public void updateOsWelcomeStatus(HttpServletRequest request,
                                      HttpServletResponse response,@RequestParam Long id){
        welcomeService.updateOsWelcomeStatus(id);
        JSONObject object = new JSONObject();
        object.put("success", "ok");
        ControllerUtil.writeJson(response, object.toJSONString());
    }



    @RequestMapping("deleteOsWelcome")
    private void deleteOsWelcome(HttpServletRequest request,
                                 HttpServletResponse response,@RequestParam Long id){
        welcomeService.deleteOsWelcome(id);
        JSONObject object = new JSONObject();
        object.put("success", "ok");
        ControllerUtil.writeJson(response, object.toJSONString());
    }

    @RequestMapping("toEditWelcome")
    public ModelAndView toEditWelcome(HttpServletRequest request,
                                      HttpServletResponse response,@RequestParam Long id){
        ModelAndView mv  = new ModelAndView("oswelcome/edit");
        OsWelcome obj = welcomeService.getWelcomeById(id);
        String beginTime =  DateUtil.getDate(obj.getBeginTime(),"yyyy-MM-dd HH:mm:ss");
        String endTime = DateUtil.getDate(obj.getEndTime(),"yyyy-MM-dd HH:mm:ss");
        mv.addObject("beginTime",beginTime);
        mv.addObject("endTime",endTime);
        mv.addObject("obj", obj);
        return mv;

    }


    @RequestMapping("toEditImg")
    public ModelAndView toEditImg(HttpServletRequest request,
                                  HttpServletResponse response,@ModelAttribute OsWelcome welcome){
        ModelAndView mv = new ModelAndView("oswelcome/editImg");
        OsWelcome obj = welcomeService.getWelcomeById(welcome.getId());
        obj.setShowTime(welcome.getShowTime());
        String  nameP=welcome.getName();
        try {
            //obj.setName(new String(nameP.getBytes("gb2312"),"UTF-8"));
            obj.setName(URLDecoder.decode(nameP,"UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            obj.setName("");
        }
        String beginTime =  DateUtil.getDate(welcome.getBeginTime(),"yyyy-MM-dd HH:mm:ss");
        String endTime = DateUtil.getDate(welcome.getEndTime(),"yyyy-MM-dd HH:mm:ss");
        String groupIds = sgService.getGroupAllIds();
        String storeCode = sgService.getStoreCodesByGroupIds(groupIds);
        JSONObject object = welcomeService.getWelcomeOfImgs(welcome.getId());
        mv.addObject("beginTime",beginTime);
        mv.addObject("endTime",endTime);
        mv.addObject("picUrl", object.get("picUrl"));
        mv.addObject("btnPicUrl", object.get("btnPicUrl"));
        mv.addObject("url", object.get("url"));
        mv.addObject("type", object.get("type"));
        mv.addObject("length", object.get("length"));
        mv.addObject("totalCount", object.getInteger("totalCount"));
        mv.addObject("storeCode",storeCode);
        mv.addObject("groupIds",groupIds);
        mv.addObject("obj", obj);
        return mv;

    }


    @RequestMapping("updateOrSave")
    public void updateOrSave(HttpServletRequest request,
                             HttpServletResponse response, @RequestParam String data){
        welcomeService.updateOsWelcome(data);
        JSONObject object = new JSONObject();
        object.put("success", "ok");
        ControllerUtil.writeJson(response, object.toJSONString());
    }

    @RequestMapping("toViewImg")
    public ModelAndView toViewImg(HttpServletRequest request,
                                  HttpServletResponse response, @RequestParam Long id){
        ModelAndView mv = new ModelAndView("oswelcome/viewImg");
        OsWelcome obj = welcomeService.getWelcomeById(id);
        obj.setShowTime(obj.getShowTime());
        String beginTime =  DateUtil.getDate(obj.getBeginTime(),"yyyy-MM-dd HH:mm:ss");
        String endTime = DateUtil.getDate(obj.getEndTime(),"yyyy-MM-dd HH:mm:ss");
        JSONObject object = welcomeService.getWelcomeOfImgs(obj.getId());
        mv.addObject("beginTime",beginTime);
        mv.addObject("endTime",endTime);
        mv.addObject("picUrl", object.get("picUrl"));
        mv.addObject("btnPicUrl", object.get("btnPicUrl"));
        mv.addObject("url", object.get("url"));
        mv.addObject("type", object.get("type"));
        mv.addObject("length", object.get("length"));
        mv.addObject("totalCount", object.getInteger("totalCount"));
        mv.addObject("obj", obj);
        return mv;

    }
}
