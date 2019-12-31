package com.feiniu.b2b.pool.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.pool.entity.B2BPoolCommodity;
import com.feiniu.b2b.pool.service.B2BPoolCommodityService;
import com.feiniu.b2b.pool.service.B2BPoolPeriodsService;
import com.feiniu.b2b.pool.service.B2BPoolPeriodsStoreService;
import com.feiniu.b2b.pool.service.B2BPoolService;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.entity.B2BStoreGroup;
import com.feiniu.b2b.store.service.B2BStoreGroupService;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.common.interceptor.LogTrace;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.DateUtil;

@Controller
@RequestMapping("/b2bPoolPeriods")
public class B2BPoolPeriodsController {
	
	private static Logger logger = Logger.getLogger(B2BPoolPeriodsController.class);
	
	@Autowired
	private B2BPoolPeriodsService cppService;
	
	@Autowired
	private B2BPoolService poolService;
	
	@Autowired
	private B2BPoolPeriodsService poolPeriodsService;
	
	@Autowired
	private B2BPoolPeriodsStoreService ppsService;
	
	@Autowired
	private B2BStoreGroupService storeGroupService;
	
	@Autowired
	private B2BStoreService storeService;
	@Autowired
	private B2BPoolCommodityService commodityService;

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
		    ControllerUtil.writeJson(response, object.toJSONString());;
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
       return 1;//
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
	
	
	/**
	 * 查询池当前期商品列表页面
	 * 
	 * @throws IOException
	 * @throws InvalidResultSetAccessException
	 **/
	@RequestMapping("/loadPoolCommoditysForModule")
	public ModelAndView loadPoolCommoditysForModule(@ModelAttribute B2BPoolCommodity obj, @RequestParam(defaultValue = "0") Long poolId,
			@RequestParam(defaultValue = "0") Long periodId, @RequestParam(defaultValue = "0") String storeCode,
			@RequestParam(defaultValue = "0") Integer oldOrder, @RequestParam(defaultValue = "0") Integer newOrder,
			@RequestParam(defaultValue = "0") String pcommodityId, @RequestParam(defaultValue = "") String storeGroupId,
			@RequestParam(defaultValue = "0") Integer editOrder,@RequestParam(defaultValue= "0") String storeName,
			HttpServletRequest request, HttpServletResponse response){
		if (obj == null ){
    		obj = new B2BPoolCommodity();
    	}
		if (obj.getPoolId() == null || obj.getPoolId() <= 0) {
			obj.setPoolId(poolId);
		}
		if (obj.getPeriods() == null || obj.getPeriods() <= 0) {
			obj.setPeriods(periodId);
		}else {
			periodId = obj.getPeriods(); 
		}
		obj.setStoreCode(storeCode);
		Map<String,B2BStore> storeMap =new HashMap<String,B2BStore>();;
		if(StringUtils.isNotBlank(storeGroupId)){
			 Map<String,B2BStoreGroup> groupMap =storeGroupService.getFmap();
			 if(null !=groupMap ){
				 B2BStoreGroup storeGroup = groupMap.get(storeGroupId);
				 storeMap = storeGroup.getStoreMap();
			 }
		}

		ModelAndView mv = new ModelAndView("b2bpool/loadPoolCommodity");
		List<B2BPoolCommodity> storeCommodityList = new ArrayList<B2BPoolCommodity>();
		YxPool pool = poolService.queryPoolAndPeriodById(poolId);
		if(null == pool || pool.getYxPoolPeriods() == null || pool.getType()!=2) {
			mv.addObject("pool", new YxPool());
			mv.addObject("poolPeriod",new YxPoolPeriods());
			obj.setTotalRows(0);
			mv.addObject("obj", obj);
			mv.addObject("msg", "当前池不存在或类型不匹配，请重新选择！");
			mv.addObject("commodityList", storeCommodityList);
			return mv;
		}
		YxPoolPeriods poolPeriod = pool.getYxPoolPeriods();
		if(periodId>0 && (long)poolPeriod.getId()!=periodId){
			int status = 0;
			for(YxPoolPeriods cpp:pool.getYppList()){
				if((long)cpp.getId()==periodId){
					poolPeriod=cpp;
				}else if((long)poolPeriod.getId() == (long)cpp.getId()){
					status = 2;
				}
				poolPeriod.setStatus(status);
			}
		}
		obj.setPeriods(poolPeriod.getId());

		List<YxPoolPeriodsStore> ppplist =ppsService.queryStoreListByPeriodsId(poolPeriod.getId());
		
		
		List<YxPoolPeriodsStore> oneList = null;
		if(!storeName.equals("0")){
			try {
				storeName = new String(storeName.getBytes("ISO8859-1"),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				storeName="";
				e.printStackTrace();
			}
			oneList = new ArrayList<YxPoolPeriodsStore>();
			for(YxPoolPeriodsStore store : ppplist){
				if(store.getStoreName().equals(storeName)){
					oneList.add(store);
					storeCode = store.getStoreCode();
				}
			}
			obj.setStoreNames(storeName);
			if(oneList.size() == 0){
				oneList = ppplist;
				mv.addObject("isSearch", "0");
			}else{
				mv.addObject("isSearch", "1");
			}
		}else{
			oneList = ppplist;
			mv.addObject("isSearch", "1");
		}
		
		
		YxPoolPeriodsStore poolPeriodsStore = null;
		if (storeCode.equals("0")) {
			poolPeriodsStore = poolPeriod.getStoreList().get(0);
			mv.addObject("storeCode", poolPeriodsStore.getStoreCode());
			obj.setStoreCode(poolPeriodsStore.getStoreCode());
		} else {
			poolPeriodsStore = ppsService.queryB2BPoolPeriodsStoreByCode(poolPeriod.getId(), storeCode);
			mv.addObject("storeCode", storeCode);
			obj.setStoreCode(storeCode);
		}
		
		if(oneList.size() == 0){ //查询异常，给出所有门店
			oneList = ppplist;
		}
		
		poolPeriod.setStoreList(oneList);
		resetStoreList(oneList);
		
		if(storeMap!=null && storeMap.size()>0){
			processProvinceList(poolPeriod,storeMap);
		}
		
		if(null == poolPeriodsStore){
			poolPeriodsStore = poolPeriod.getStoreList().get(0);
		}
		
		storeCommodityList = poolPeriodsService.getB2BPoolCommodityList(pool,poolPeriodsStore,true);

		

		// 获取分页数据
		storeCommodityList = poolPeriodsService.getPageData(storeCommodityList, obj);
		List<B2BStoreGroup> listGroup = storeGroupService.getStoreGroupListByGroupIds(null);
		mv.addObject("pool", pool);
		mv.addObject("poolPeriod", poolPeriod);
		mv.addObject("obj", obj);
		mv.addObject("showArea", storeGroupId);
		mv.addObject("commodityList", storeCommodityList);
		mv.addObject("allCount", storeCommodityList.size());
		mv.addObject("dddcInUse", "");
		mv.addObject("listGroup", listGroup);
		return mv;
	}
	
	public void processProvinceList(YxPoolPeriods poolPeriods,Map<String,B2BStore> storeMap){
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
	
	private void resetStoreList(List<YxPoolPeriodsStore> ppplist) {
		for(YxPoolPeriodsStore c : ppplist) {
			if (null != c.getCommoditys() && !"".equals(c.getCommoditys())) {
				c.setCountCommodity((c.getCommoditys().split(",").length));
				List<B2BPoolCommodity> commoditys = commodityService.getCommodityByIds(c.getCommoditys());
				int commodityNum =0,picNum=0,textNum=0;
				if(commoditys!=null && commoditys.size()>0){
					for(B2BPoolCommodity cc:commoditys){
						switch(cc.getOriginate()){
							case 1:
								commodityNum++;
								break;
							case 2:
								picNum++;
								break;
							case 3:
								textNum++;
								break;
						}
					}
				}
				c.setCommodityNum(commodityNum);
				c.setPicNum(picNum);
				c.setTextNum(textNum);
			}
			if (null != c.getCommoditys() && !"".equals(c.getCommoditys())) {
				c.setCountCommodity((c.getCommoditys().split(",").length));
			}
			//Map<String, CMSSaleArea> sam = cssaService.getAllmap();
			B2BStore store = storeService.getB2BStoreByCode(c.getStoreCode());
			//CMSSaleArea sa = sam.get(c.getProvinceSeq());
			if(store != null){
				//c.setProvinceName(sa.getName());
				c.setStoreName(store.getName());
			}		
		}
	}
}
