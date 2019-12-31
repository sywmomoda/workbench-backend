package com.feiniu.b2b.pool.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.pool.service.B2BPoolService;
import com.feiniu.yx.common.interceptor.LogTraceService;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.UserUtil;

@Controller
@RequestMapping("/b2bPool")
public class B2BPoolController {
	
	@Autowired
	private B2BPoolService poolService;
	
	@Autowired
    private LogTraceService traceService;

	 /** 初始化进入添加页面 **/
    @RequestMapping("/toAdd")
    public ModelAndView gotoAdd(HttpServletRequest request, @RequestParam(defaultValue = "") String goToPage, @RequestParam(defaultValue = "0") Long id) {
    	ModelAndView mv = new ModelAndView("b2bpool/addPool");
        return mv;
    }
    
    @RequestMapping("/save")
    public void save(HttpServletRequest request, HttpServletResponse response, YxPool  pool){
        pool.setCreateId(UserUtil.getUserId());
        long id = poolService.save(pool);
        JSONObject r = new JSONObject();
        r.put("id", id);
        traceService.sendLogger(JSONObject.toJSONString(pool),"pool", id,request);
        ControllerUtil.writeJson(response, r.toJSONString());
    }
    
    /**
     * @param yxPool
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/query")
    public ModelAndView query(@ModelAttribute YxPool yxPool, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("b2bpool/queryPool");
        yxPool.setType(2);
        List<YxPool> poolList = poolService.queryPoolList(yxPool);
        mv.addObject("poolList", poolList);
        mv.addObject("obj", yxPool);
        return mv;
    }
    
    /** 初始化进入修改页面 **/
    @RequestMapping("/toUpdate")
    public ModelAndView toUpdate(HttpServletRequest request, @RequestParam(defaultValue = "") String from,@RequestParam(defaultValue = "") String groupIds, @RequestParam(defaultValue = "0") Long id) {
        ModelAndView mv = new ModelAndView("b2bpool/updatePool");
        mv.addObject("obj", poolService.queryPoolById(id));
        mv.addObject("from", from);
        mv.addObject("showGroup", groupIds);
        return mv;
    }
    
    /** 更新保存 **/
    @RequestMapping("/update")
    public void update(HttpServletRequest request, HttpServletResponse response,YxPool pool){
        JSONObject result = new JSONObject();
        pool.setUpdateId(UserUtil.getUserId());
    	poolService.updatePool(pool);
    	traceService.sendLogger(JSONObject.toJSONString(pool),"pool", pool.getId(),request);
        result.put("success", true);
        ControllerUtil.writeJson(response, result.toJSONString());
    }

    /**切换池选择列表*/
    @RequestMapping("poolSelectTab")
    public ModelAndView poolSelectTab(@ModelAttribute YxPool pool, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("b2bpool/poolSelectTab");
        List<YxPool> poolList = poolService.queryPoolList(pool);
        String toId = request.getParameter("toId");
        mv.addObject("poolList", poolList);
        mv.addObject("obj", pool);
        mv.addObject("toId", toId);
        return mv;
    }

	/** 池切换时选择或新建页面 */
	@RequestMapping("poolSelectType")
	public ModelAndView poolSelectType(@ModelAttribute YxPool pool,
			HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mv = new ModelAndView("b2bpool/poolSelectType");
		List<YxPool> poolList = poolService.queryPoolList(pool);
		String toId = request.getParameter("toId");
		mv.addObject("poolList", poolList);
		mv.addObject("obj", pool);
		mv.addObject("toId", toId);
		return mv;
	}
	
	 /** 初始化进入添加页面 **/
    @RequestMapping("/toAddTab")
    public ModelAndView gotoAddTab(HttpServletRequest request, @RequestParam(defaultValue = "") String type) {
        ModelAndView mv = new ModelAndView("b2bpool/addPoolTab");
        mv.addObject("type", type);
        return mv;
    }
    
    @RequestMapping("/getPoolPermission")
    public void getPoolPermission(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") long id){
    	YxPool cmsPool = poolService.queryPoolById(id);
        ControllerUtil.writeJson(response, JSONObject.toJSONString(cmsPool));
    }
    
    
    @RequestMapping("/getPoolStoreList")
    public void getPoolStoreList(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") long periodsId){
    	List<YxPoolPeriodsStore> list = poolService.getPoolStoreList(periodsId);
    	ControllerUtil.writeJson(response, JSONObject.toJSONString(list));
    }

}
