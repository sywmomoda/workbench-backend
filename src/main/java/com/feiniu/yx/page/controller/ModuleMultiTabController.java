package com.feiniu.yx.page.controller;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.ModuleMultiTab;
import com.feiniu.yx.page.service.ModuleMultiTabService;
import com.feiniu.yx.util.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author yehui
 */
@Controller
@RequestMapping("/moduleMultiTab")
public class ModuleMultiTabController {
    @Autowired
    private ModuleMultiTabService moduleMultiTabService;

    @RequestMapping("/insetContent")
    public void insertContent(HttpServletResponse response, @RequestParam(defaultValue = "") String data){
        ModuleMultiTab multiTab = JSONObject.parseObject(data,ModuleMultiTab.class);
        Long id = moduleMultiTabService.insert(multiTab);
        JSONObject res = new JSONObject();
        res.put("id",id);
        res.put("success","success");
        ControllerUtil.writeJson(response,res.toJSONString());
    }

    @RequestMapping("/updateContent")
    public void updateContent(HttpServletResponse response, @RequestParam(defaultValue = "") String data){
        ModuleMultiTab multiTab = JSONObject.parseObject(data,ModuleMultiTab.class);
        moduleMultiTabService.update(multiTab);
        JSONObject res = new JSONObject();
        res.put("id",multiTab.getId());
        res.put("success","success");
        ControllerUtil.writeJson(response,res.toJSONString());
    }
    @RequestMapping("/updateContentOfStoreCode")
    public void updateContentOfStoreCode(HttpServletResponse response, @RequestParam(defaultValue = "") String  ids,
                                         @RequestParam(defaultValue = "")  String storeCode){
        moduleMultiTabService.updateStoreCode(ids,storeCode);
        JSONObject res = new JSONObject();
        res.put("id",ids);
        res.put("success","success");
        ControllerUtil.writeJson(response,res.toJSONString());
    }

    @RequestMapping("/selectListByIds")
    public void selectListByIds(HttpServletResponse response,@RequestParam(defaultValue = "") String ids){
        List<ModuleMultiTab> list = moduleMultiTabService.selectListByIds(ids);
        ControllerUtil.writeJson(response,JSONObject.toJSONString(list));
    }

    @RequestMapping("/deleteContent")
    public  void deleteContent(HttpServletResponse response,@RequestParam(defaultValue = "") Long id){
        moduleMultiTabService.delete(id);
        JSONObject res = new JSONObject();
        res.put("id",id);
        res.put("success","success");
        ControllerUtil.writeJson(response,res.toJSONString());
    }

    @RequestMapping("/deleteContentByIds")
    public void deleteContentByIds(HttpServletResponse response,@RequestParam(defaultValue = "") String ids){
        moduleMultiTabService.deleteByIds(ids);
        JSONObject res = new JSONObject();
        res.put("id",ids);
        res.put("success","success");
        ControllerUtil.writeJson(response,res.toJSONString());
    }
}
