package com.feiniu.yx.page.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.interceptor.LogTraceService;
import com.feiniu.yx.page.entity.YxRemoteCommodity;
import com.feiniu.yx.page.service.RemoteCommodityService;
import com.feiniu.yx.pool.service.YxPoolCommodityService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.ControllerUtil;

@Controller
@RequestMapping("/yxRemoteCommodity")
public class YxRemoteCommodityController {
	
	@Autowired
	private LogTraceService logTraceService;
	
	@Autowired
	private RemoteCommodityService remoteCommodityService;
	
	@Autowired
	private YXStoreService yXStoreService;
	
	@Autowired
	private YxPoolCommodityService commodityService;
	
	/**
	 * 查询模块数据
	 * @author tongwenhuan
	 * 2017年2月24日
	 * @param request
	 * @param response
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping("syncRemoteCommodity")
	public void syncRemoteCommodity(HttpServletResponse response,HttpServletRequest request,@RequestParam String type, @RequestParam String storeCode){
		JSONObject mjo = new JSONObject();
		int remoteCount = 36;
		String count = request.getParameter("remoteCount");
		if(StringUtils.isNotBlank(count)){
			remoteCount = Integer.parseInt(count);
		}
		String result = remoteCommodityService.syncRemoteCommodity(storeCode, "", type,remoteCount);
		mjo.put("爆款同步门店"+storeCode+"result", result);
		mjo.put("type", type);
		logTraceService.sendLogger("system", "system", "", mjo.toJSONString(), "page", 1L);
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	@RequestMapping("syncNewAddCommodity")
	public void syncNewAddCommodity(HttpServletResponse response,HttpServletRequest request,@RequestParam long poolId, @RequestParam String storeCode){
		JSONObject mjo = new JSONObject();
		String type = "newRecommend";
		int getDays = 1;
		
		String days = request.getParameter("days");
		if(StringUtils.isNotBlank(days)){
			getDays = Integer.parseInt(days);
		}
		
		int remoteCount = 30;
		String count = request.getParameter("remoteCount");
		if(StringUtils.isNotBlank(count)){
			remoteCount = Integer.parseInt(count);
		}
		String cpSeqs = request.getParameter("cpSeqs");
		commodityService.deleteRemotePoolCommodityDate(poolId, 2, storeCode);
		String result = remoteCommodityService.syncRemoteCommodityToPool(storeCode, poolId, type, getDays,remoteCount,cpSeqs);
		mjo.put("新品同步门店"+storeCode+"result", result);
		mjo.put("poolId", poolId);
		logTraceService.sendLogger("system", "system", "", mjo.toJSONString(), "pool", poolId);
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	@RequestMapping("syncRemoteCommodityAll")
	public void syncRemoteCommodityAll(HttpServletResponse response,HttpServletRequest request,@RequestParam String type){
		JSONObject mjo = new JSONObject();
		int remoteCount = 36;
		String count = request.getParameter("remoteCount");
		if(StringUtils.isNotBlank(count)){
			remoteCount = Integer.parseInt(count);
		}
		final int sum = remoteCount;
		final String requestType = type;
		final List<YXStore> stList = yXStoreService.getYXStoreList();
		new Thread(){
            @Override
            public void run() {
            	for(YXStore ys: stList){
        			String storeCode = ys.getCode();
        			try{
        				String result = remoteCommodityService.syncRemoteCommodity(storeCode, "", requestType,sum);
        				logTraceService.sendLogger("system", "system", "", "爆款同步门店"+storeCode+":"+result, "pool", 1L);
        				Thread.sleep(1000);
        			}catch(Exception e){
        				logTraceService.sendLogger("system", "system", "", storeCode+"爆款同步失败："+e.getLocalizedMessage(), "pool", 1L);
        			}
        		}
            }
        }.start();
		mjo.put("result", "success");
		mjo.put("remoteCount", remoteCount);
		mjo.put("type", type);
		logTraceService.sendLogger("system", "system", "", mjo.toJSONString(), "page", 1L);
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	@RequestMapping("syncNewAddCommodityAll")
	public void syncNewAddCommodityAll(HttpServletResponse response,HttpServletRequest request,@RequestParam long poolId){
		JSONObject mjo = new JSONObject();
		int getDays = 1;
		String days = request.getParameter("days");
		if(StringUtils.isNotBlank(days)){
			getDays = Integer.parseInt(days);
		}
		int remoteCount = 30;
		String count = request.getParameter("remoteCount");
		if(StringUtils.isNotBlank(count)){
			remoteCount = Integer.parseInt(count);
		}
		
		String cpSeqs = request.getParameter("cpSeqs");
		String ids = request.getParameter("groupIds");
		if(StringUtils.isBlank(ids)){
			ids = "1,2,3,4,5,6";
		}
		final int fOnDay = getDays;
		final int sum = remoteCount;
		final long pId = poolId;
		final String category = cpSeqs;
		final List<YXStore> stList = yXStoreService.getYXStoreByGroupIds(ids);
		new Thread(){
            @Override
            public void run() {
            	for(YXStore ys: stList){
        			String storeCode = ys.getCode();
        			try{
        				commodityService.deleteRemotePoolCommodityDate(pId, 2, storeCode);
        				String result = remoteCommodityService.syncRemoteCommodityToPool(storeCode, pId, "newRecommend", fOnDay, sum, category);
        				logTraceService.sendLogger("system", "system", "", "新品同步门店"+storeCode+":"+result, "pool", pId);
        				Thread.sleep(2000);
        			}catch(Exception e){
        				logTraceService.sendLogger("system", "system", "", storeCode+"新品同步失败："+e.getLocalizedMessage(), "pool", pId);
        			}
        		}
            }
        }.start();
		mjo.put("poolId", poolId);
		mjo.put("getDays", getDays);
		mjo.put("cpSeqs", cpSeqs);
		mjo.put("remoteCount", remoteCount);
		mjo.put("groupIds", ids);
		mjo.put("result", "success");
		logTraceService.sendLogger("system", "system", "", mjo.toJSONString(), "pool", poolId);
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	@RequestMapping("getLocalRemoteCommodity")
	public void getLocalRemoteCommodity(HttpServletResponse response,@RequestParam String type,@RequestParam String storeCode){
		JSONObject mjo = new JSONObject();
		List<YxRemoteCommodity> rcList = remoteCommodityService.getLocalCommodityByType(storeCode, type);
		mjo.put("rcList", rcList);
		ControllerUtil.writeJson(response, mjo.toJSONString());
	}
	
	
}
