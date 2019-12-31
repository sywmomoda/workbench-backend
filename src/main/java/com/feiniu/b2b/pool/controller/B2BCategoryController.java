package com.feiniu.b2b.pool.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.pool.service.B2BCategoryService;
import com.feiniu.yx.util.ControllerUtil;

@Controller
@RequestMapping("/b2bCategory")
public class B2BCategoryController {
	
    @Autowired
    private B2BCategoryService categoryService;
    
    /** 查询列表页面 */
    @RequestMapping("/category")
    public ModelAndView category() {
        ModelAndView mv = new ModelAndView("category_b2b/managerPage");
        return mv;
    }
	
    @RequestMapping("/sync")
	public void sync(HttpServletRequest request,HttpServletResponse response,@RequestParam(defaultValue="0")  String storeCode){
		JSONObject jsonObject=new JSONObject();
		final String code = storeCode;
		if(categoryService.getIsLock()){
			jsonObject.put("success", false);
            jsonObject.put("msg", "已有任务在执行，请稍后再试！");
            ControllerUtil.writeJson(response, jsonObject.toJSONString());
            return;
		}else{
			categoryService.lock();
			 new Thread(){
	                @Override
	                public void run() {
	                	categoryService.sync(code);
	                }
	            }.start();
	            jsonObject.put("success", true);
	            jsonObject.put("msg", "同步中，请稍候！");
	            ControllerUtil.writeJson(response, jsonObject.toJSONString());
		}
	}
	
	/** 查询全部类别树形显示 **/
    @RequestMapping("/treeJson")
    public void treeJson(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "") String areaCode,
            @RequestParam(defaultValue = "0") String id,@RequestParam(defaultValue="0") String storeCode){
        List<Map<String, Object>> result = categoryService.getLocalCategory(id,storeCode);
        ControllerUtil.writeJson(response, JSONArray.toJSONString(result));
    }
    
    /** 查询全部类别树形显示 **/
    @RequestMapping("/treeJsonCategorySelect")
    public void treeJsonCategorySelect(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "") String areaCode,
            @RequestParam(defaultValue = "0") Integer level,@RequestParam(defaultValue="0") String checkedCodes){
        List<Map<String, Object>> result = categoryService.getLocalCategoryTree(areaCode,checkedCodes,level);
        ControllerUtil.writeJson(response, JSONArray.toJSONString(result));
    }
    
    @RequestMapping("releaseLock")
    public void releaseLock(HttpServletResponse response) throws Exception {
        JSONObject jsonObject = new JSONObject();
        categoryService.releaseLock();
        jsonObject.put("success", true);
        ControllerUtil.writeJson(response, jsonObject.toJSONString());
    }

}
