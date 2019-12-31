package com.feiniu.yx.pool.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.interceptor.LogTraceService;
import com.feiniu.yx.pool.dao.YxPoolDao;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.service.SyncPoolService;
import com.feiniu.yx.pool.service.YxPoolService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.UserUtil;

@Controller
@RequestMapping("/yxPool")
public class YxPoolController {
	
	private static Logger logger = Logger.getLogger(YxPoolController.class);
	
	@Autowired
	private YxPoolService poolService;

	@Autowired
    private LogTraceService traceService;
	
	@Autowired
	SyncPoolService syncPoolService;
	
	 /** 初始化进入添加页面 **/
    @RequestMapping("/toAdd")
    public ModelAndView gotoAdd(HttpServletRequest request, @RequestParam(defaultValue = "") String goToPage, @RequestParam(defaultValue = "0") Long id) {
    	ModelAndView mv = new ModelAndView("yxpool/addPool");
        return mv;
    }
    
    @RequestMapping("/save")
    public void save(HttpServletRequest request, HttpServletResponse response, YxPool  pool){
        pool.setCreateId(UserUtil.getUserId());
        long id = poolService.save(pool);
        JSONObject r = new JSONObject();
        r.put("id", id);
    	traceService.sendLogger(JSONObject.toJSONString(pool),"pool",id,request);
        ControllerUtil.writeJson(response, r.toJSONString());
    }
    
    @RequestMapping("/query")
    public ModelAndView query(@ModelAttribute YxPool yxPool, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("yxpool/queryPool");
        List<YxPool> poolList = poolService.queryPoolList(yxPool);
        mv.addObject("poolList", poolList);
        mv.addObject("obj", yxPool);
        return mv;
    }
    
    /** 初始化进入修改页面 **/
    @RequestMapping("/toUpdate")
    public ModelAndView toUpdate(HttpServletRequest request, @RequestParam(defaultValue = "") String from,@RequestParam(defaultValue = "") String groupIds, @RequestParam(defaultValue = "0") Long id) {
        ModelAndView mv = new ModelAndView("yxpool/updatePool");
        YxPool p = poolService.queryPoolAndPeriodById(id);
        Collections.reverse(p.getYppList());
        mv.addObject("obj", p);
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
    	traceService.sendLogger(JSONObject.toJSONString(pool),"pool",pool.getId(),request);
        result.put("success", true);
        ControllerUtil.writeJson(response, result.toJSONString());
    }

    /**切换池选择列表*/
    @RequestMapping("poolSelectTab")
    public ModelAndView poolSelectTab(@ModelAttribute YxPool pool, HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mv = new ModelAndView("yxpool/poolSelectTab");
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
		ModelAndView mv = new ModelAndView("yxpool/poolSelectType");
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
        ModelAndView mv = new ModelAndView("yxpool/addPoolTab");
        mv.addObject("type", type);
        return mv;
    }
    
    @RequestMapping("/getPoolPermission")
    public void getPoolPermission(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") long id){
    	YxPool cmsPool = poolService.queryPoolById(id);
        ControllerUtil.writeJson(response, JSONObject.toJSONString(cmsPool));
    }
    
    @RequestMapping("/getPoolPreiodsList")
    public void getPoolPreiodsList(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") Long poolId){
    	YxPool cmsPool = poolService.queryPoolAndPeriodById(poolId);
    	ControllerUtil.writeJson(response, JSONObject.toJSONString(cmsPool));
    }
    
    @RequestMapping("/syncAllPoolStores")
    public void syncAllPoolStores(HttpServletRequest request, HttpServletResponse response){
    	JSONObject result = poolService.syncStoreByPool();
       ControllerUtil.writeJson(response, result.toJSONString());
    }
    
    @RequestMapping("/removeSyncAllStoreRedis")
    public void removeSyncAllStoreRedis(HttpServletRequest request, HttpServletResponse response){
    	poolService.removeRedisSync();
    	JSONObject result = new JSONObject();
    	result.put("msg","success");
        ControllerUtil.writeJson(response, result.toJSONString());
    }

    /**
     * 判断用户是否有权限
     * @param request
     * @param response
     * @param id
     */
    @RequestMapping(value="/checkPremission", method=GET)
    public void checkPermission(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") long id) {
    	JSONObject res = new JSONObject();
    	//是否有权限, 0 无， 1有
    	res.put("hasPre", 0);
    	boolean auth = false;
    	YxPool cmsPool = yxPoolDao.queryYxPoolById(id);
    	if(id == 0 || cmsPool == null) {
    		auth = false;
    		logger.info("pool is null, id is " + id);
    	} else {
    		auth = authCheck(cmsPool);
    	}
    	if (auth) {
    		res.put("hasPre", 1);
    	}
    	ControllerUtil.writeJson(response, res.toJSONString());
    }
    
    @RequestMapping(value="/syncPoolDataById")
    public void syncPoolDataById( @RequestParam(defaultValue = "0") long poolId, HttpServletResponse response) {
    	syncPoolService.syncPool(poolId);
    	ControllerUtil.writeJson(response);
    }

    private boolean authCheck(YxPool cmsPool) {
    	String admin = cmsPool.getAdministrator();
    	String creater = cmsPool.getCreateId();
    	String userName = UserUtil.getUserId();
    	if(admin == null || creater == null) {
    		return false;
    	}
    	admin = "," + admin + "," + creater + ",";
    	//判断权限
    	if(admin.indexOf(userName) > -1 ) {
    		return true;
    	}
    	return false;
    } 
    
    @Autowired
	private YxPoolDao yxPoolDao;
}
