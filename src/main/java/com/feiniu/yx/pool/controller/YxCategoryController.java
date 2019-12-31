package com.feiniu.yx.pool.controller;

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
import com.feiniu.yx.common.service.SyncCategoryService;
import com.feiniu.yx.pool.service.YxCategoryService;
import com.feiniu.yx.util.ControllerUtil;

@Controller
@RequestMapping("/yxCategory")
public class YxCategoryController {
	
    @Autowired
    private YxCategoryService categoryService;
    
    @Autowired
    private SyncCategoryService syncCategoryService;
    
    /** 查询列表页面 */
    @RequestMapping("/category")
    public ModelAndView category() {
        ModelAndView mv = new ModelAndView("category/managerPage");
        return mv;
    }
	
    @RequestMapping("/sync")
	public void sync(HttpServletRequest request,HttpServletResponse response,@RequestParam(defaultValue="0")  String storeCode){
		JSONObject jsonObject = syncCategoryService.SyncCategory(storeCode);
		ControllerUtil.writeJson(response, jsonObject.toJSONString());
	}
	
	/** 查询全部类别树形显示 **/
    @RequestMapping("/treeJson")
    public void treeJson(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "") String siPseq,
            @RequestParam(defaultValue = "") String id,@RequestParam(defaultValue="0") String storeCode){
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
        jsonObject.put("success", true);
        ControllerUtil.writeJson(response, jsonObject.toJSONString());
    }

}
