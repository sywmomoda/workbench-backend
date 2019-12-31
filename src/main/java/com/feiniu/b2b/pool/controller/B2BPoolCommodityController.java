package com.feiniu.b2b.pool.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.UserUtil;

@Controller
@RequestMapping("/b2bPoolCommodity")
public class B2BPoolCommodityController {
	
	@Autowired
	private B2BPoolService poolService;
	
	@Autowired
	private B2BPoolPeriodsService poolPeriodsService;
	
    @Autowired
    private B2BPoolPeriodsStoreService cprService;

    
	@Autowired
	private B2BPoolCommodityService commodityService;
	
	@Autowired
	private B2BStoreService storeService;
	
	@Autowired
	private B2BStoreGroupService groupService;

	
    private static String pageEnv = SystemEnv.getProperty("feiniu.pageEnv");

	/**
	 * 查询池当前期商品列表页面
	 * 
	 * @throws IOException
	 * @throws InvalidResultSetAccessException
	 **/
	@RequestMapping("/queryPoolCommoditys")
	public ModelAndView queryPoolCommoditys(HttpServletRequest request, HttpServletResponse response){
		ModelAndView mv = new ModelAndView("b2bpool/listPoolCommodity");
		return mv;
	}
	
	
	/**
	 * 查池与期数json
	 * @param data
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/poolCommodityListData", method=POST)
	public void poolAndStoreData(@RequestParam(defaultValue="") String data,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		JSONObject resultObject = new JSONObject();
		if (StringUtils.isBlank(data)) {
			ControllerUtil.writeJson(response,resultObject.toJSONString());
			return;
		}
		JSONObject dataJo = JSON.parseObject(data);
		Long poolId = dataJo.getLong("poolId");
		Long periodId = dataJo.getLong("periodId");
		YxPool yxPool  = null;
		if(periodId > 0){
			yxPool = poolService.queryPoolAndPeriodById(poolId, periodId);
		}else{
			yxPool = poolService.queryPoolAndPeriodById(poolId);
		}
		List<B2BStoreGroup> listGroup = groupService.getStoreGroupListByGroupIds(null);
		//全部门店
		List<YxPoolPeriodsStore> storelistAll = cprService.queryStoreListByPeriodsId(yxPool.getYxPoolPeriods().getId());
		String storeCode = dataJo.getString("storeCode");
		String groupId = dataJo.getString("groupId");
		String storeId = dataJo.getString("storeId");
		String storeName = dataJo.getString("storeName");
		//根据查询条件来过滤门店
		List<YxPoolPeriodsStore> storelistOne = setStoreList(storelistAll,groupId,storeId,storeName);
		if(storelistOne.size() == 0){ //查询异常，给出所有门店
			storelistOne = storelistAll;
			dataJo.put("groupId", "0");
		}
		//选中的门店
		YxPoolPeriodsStore pps = null;
		if (StringUtils.isBlank(storeCode) && storelistOne != null && storelistOne.size() > 0) {
			//默认选择第一个门店
			pps = storelistOne.get(0);
			storeCode = pps.getStoreCode();
			dataJo.put("storeCode", storeCode);
		} else {
            B2BStore ps = new B2BStore();
            if(StringUtils.isNotBlank(storeId)){
                ps.setName(storeId);
            }
            List<B2BStore> b2bList = null;
            if(StringUtils.isBlank(storeId) && StringUtils.isNotBlank(storeName)){
                ps.setName(storeName);
            }
            if(StringUtils.isBlank(storeId) && StringUtils.isBlank(storeName)){
                ps.setName(storeCode);
            }
            b2bList= storeService.searchB2BStoreByNameOrCode(ps);
            if(b2bList != null  && b2bList.size() > 0){
                B2BStore store = b2bList.get(0);
                pps = cprService.queryB2BPoolPeriodsStoreByCode(yxPool.getYxPoolPeriods().getId(), store.getCode());
                pps.setStoreName(store.getName());
            }
		}
		if(pps == null){
			if(storelistOne.size() == 0){
				pps = new YxPoolPeriodsStore();
			}else{
				pps = storelistOne.get(0);	
			}	
		}
		//设置商品数，图片，文字数量
		resetYxPoolPeriodsStore(pps);
		
		//获取门店的商品详情
		List<B2BPoolCommodity> provinceCommodityList = poolPeriodsService.getB2BPoolCommodityList(yxPool, pps, true);
		B2BPoolCommodity obj = JSONObject.parseObject(data, B2BPoolCommodity.class);
		//获取分页数据
		provinceCommodityList = poolPeriodsService.getPageData(provinceCommodityList, obj);
		dataJo.put("totalRows", obj.getTotalRows());
		dataJo.put("pageRows", obj.getPageRows());
		dataJo.put("curPage", obj.getCurPage());
		resultObject.put("param", dataJo);
		resultObject.put("pool", yxPool);
		resultObject.put("listGroup", listGroup); //群组列表
		resultObject.put("storelist", storelistOne); //门店列表
		resultObject.put("commodityList", provinceCommodityList);
		resultObject.put("poolPeriodsStore", pps);
		//SerializerFeature.DisableCircularReferenceDetect,fastjson把对象转化成json避免$ref 
		ControllerUtil.writeJson(response,JSONObject.toJSONStringWithDateFormat(resultObject,"yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteDateUseDateFormat, SerializerFeature.DisableCircularReferenceDetect));
	}
	/**
	 * 调整商品顺序
	 * @param request
	 * @param response
	 * @param data
	 */
	@RequestMapping(value = "/resetOrder", method = POST)
	public void resetOrder(HttpServletRequest request,
			HttpServletResponse response, @RequestParam String data) {
		JSONObject param = JSONObject.parseObject(data);
		Long periodId = param.getLong("periodId");
		String storeCode = param.getString("storeCode");
		String commondityId = param.getString("commondityId");
		int order = param.getIntValue("order");
		poolPeriodsService.resetOrder(periodId, storeCode, commondityId, order);
		ControllerUtil.writeJson(response);
	}
	
	/**
	 * 获取门店
	 * @param listPeriodsStore
	 * @param groupId
	 * @return
	 */
	
	private List<YxPoolPeriodsStore> setStoreList(List<YxPoolPeriodsStore> listPeriodsStore,String groupId,String storeId,String storeName){
		Map<String,YxPoolPeriodsStore> psMap = new HashMap<String,YxPoolPeriodsStore>();  //key为storeCode
		Map<String,YxPoolPeriodsStore> psMap2 = new HashMap<String,YxPoolPeriodsStore>();  //key为storeName
		for(YxPoolPeriodsStore pps : listPeriodsStore){
			psMap.put(pps.getStoreCode(), pps);
			psMap2.put(pps.getStoreName(), pps);
		}
		List<YxPoolPeriodsStore> psSelectList = new ArrayList<YxPoolPeriodsStore>();
		if(StringUtils.isNotBlank(storeId)  && StringUtils.isNotBlank(storeName)){
			YxPoolPeriodsStore yxObj = psMap.get(storeId);
			if(null !=yxObj){
				if(yxObj.getStoreName().equals(storeName)){
					psSelectList.add(yxObj);
				}
			}
			return psSelectList;
		}
		if(StringUtils.isNotBlank(storeId)){  //门店code 查询
			YxPoolPeriodsStore yxObj = psMap.get(storeId);
			if(null !=yxObj){
				psSelectList.add(yxObj);
			}
			if(psSelectList.size() > 0){
				return psSelectList;
			}
		}
		if(StringUtils.isNotBlank(storeName)){//门店名称 查询	
			YxPoolPeriodsStore yxObj = psMap2.get(storeName);
			if(null !=yxObj){
				psSelectList.add(yxObj);
			}
			if(psSelectList.size() > 0){
				return psSelectList;
			}
			
		}		
		if(StringUtils.isBlank(groupId) || groupId.equals("0")){
			return psSelectList;
		}
		Map<String,B2BStoreGroup> mapGroup = groupService.getFmap();
		Map<String,B2BStore> storeMap = new HashMap<String,B2BStore>();
	    if(groupId.equals("-1")){ //未分组的门店
	    	for(Map.Entry<String,B2BStoreGroup> sgp : mapGroup.entrySet()){
	    		B2BStoreGroup storeGroup = sgp.getValue();
	    		storeMap.putAll(storeGroup.getStoreMap());
	    	}
	    	for(Map.Entry<String,YxPoolPeriodsStore> entry : psMap.entrySet()){
				String key = entry.getKey();
				B2BStore store = storeMap.get(key); 
				if(null == store){  //分组门店中不存在，表明门店未分组
					psSelectList.add(entry.getValue());
				}
			}
	    }else{ //分组门店
	    	B2BStoreGroup sg = mapGroup.get(groupId);	
	    	storeMap = sg.getStoreMap();
	    	for(Map.Entry<String,B2BStore> entry : storeMap.entrySet()){
				B2BStore bs = entry.getValue();
				YxPoolPeriodsStore ypp = psMap.get(bs.getCode());
				if(null == ypp){
					continue;
				}
				psSelectList.add(ypp);
			}
	    }
		return psSelectList;
	}
	
	
	/**
	 * 设置门店名称
	 * @param ppplist
	 */
	/*private void setStoreNames(List<YxPoolPeriodsStore> ppplist) {
		Map<String, B2BStore> sam = storeService.getB2BStoreMapByCode(null);
		for(YxPoolPeriodsStore c : ppplist) {
			B2BStore sa = sam.get(c.getStoreCode());
			if(sa != null){
				c.setStoreName(sa.getName());
				Map<String,B2BStoreGroup> mapGroup = groupService.getStoreKeyGroupMap();
				B2BStoreGroup gp = mapGroup.get(sa.getCode());
				Long groupId = gp == null ? null : gp.getId();
				String id = groupId == null ? "none" : groupId.toString();
				c.setGroupId(id);
			}		
		}
	}*/
	
	/**
	 * 获取对应地区
	 * @param ppplist
	 * @param storeCode
	 */
	/*private YxPoolPeriodsStore getYxPoolPeriodsStoreByCode(List<YxPoolPeriodsStore> ppplist, String storeCode) {
		YxPoolPeriodsStore yxPoolPeriodsStore = null;
		if("0".equals(storeCode)) { //默认显示第一个
			yxPoolPeriodsStore = ppplist.get(0);
		}else {
			for(YxPoolPeriodsStore c : ppplist) {
				if(storeCode.equals(c.getStoreCode())) {
					yxPoolPeriodsStore = c;
				}
			}
		}
		if(yxPoolPeriodsStore == null){
			yxPoolPeriodsStore =ppplist.get(0);
		}
		resetYxPoolPeriodsStore(yxPoolPeriodsStore);
		return yxPoolPeriodsStore;
	}*/
	
	/**
	 * 设置数量
	 * 商品总数，素材总数，文字链总数
	 * @param c
	 */
	private void resetYxPoolPeriodsStore(YxPoolPeriodsStore c) {
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

    /** 同步区域数据 **/
    @RequestMapping("/synchStoreData")
    @LogTrace(msgFomort={"同步当前区域数据，池ID:{poolId}","period","{poolPeriodId}"})
    public void synchStoreData(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(defaultValue = "0") String poolId) throws Exception {
        poolPeriodsService.synchStoreData(poolId);
        ControllerUtil.writeJson(response);
    }
    
    /** 清空池当前期所有商品 **/
    @LogTrace(msgFomort={"删除当前期数所有商品","period","{periodId}"})
    @RequestMapping("/deletePeriodAllCommodity")
    public void deletePeriodAllCommodity(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") Long periodId)
            throws Exception {
        poolPeriodsService.deletePeriodAllCommodity(periodId);
        ControllerUtil.writeJson(response);
    }
    
    /** 删除池当前期中选中的商品 **/
    @RequestMapping("/deleteCommodity")
    @LogTrace(msgFomort={"删除商品(商品ids:{commodityId},省份:{storeCode})","period","{periodId}"})
    public void deleteCommodity(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") String commodityId,
            @RequestParam(defaultValue = "0") Integer orders, @RequestParam(defaultValue = "0") Long periodId, String storeCode) throws Exception {
        poolPeriodsService.deletePeriodsAllProinceCommodity(commodityId, periodId, orders, storeCode);
        ControllerUtil.writeJson(response);
    }
    
    /***
     * 
     * @param request
     * @param response
     * @param periodId
     * @param Id
     *            商品ID 用逗号隔开的字符串
     * @throws Exception
     */
    @RequestMapping("/deleteButchCommodity")
    @LogTrace(msgFomort={"批量删除商品(商品ids:{Id},省份:{storeCode})","period","{periodId}"})
    public void deleteButchCommodity(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") Long periodId, String Id,
            String storeCode) throws Exception {
        String[] ids = Id.split(",");
        poolPeriodsService.deleteButchCommodity(ids, periodId, storeCode);
        ControllerUtil.writeJson(response);
    }
    
    @RequestMapping("/oneCommodityOrder")
	public void oneCommodityOrder(HttpServletRequest request, 
			HttpServletResponse response, 
			@RequestParam(defaultValue = "0") Long periodId,
			@RequestParam(defaultValue = "") String storeCode) throws Exception {
    	JSONObject resultJson = new JSONObject();
		YxPoolPeriodsStore standard = null;
		List<YxPoolPeriodsStore> otherProvince = new ArrayList<YxPoolPeriodsStore>();
		if (storeCode != null && !"".equals(storeCode) && periodId != 0) {
			List<YxPoolPeriodsStore> allPeriodsStoreList = cprService.getPoolPeriodsStoresByPeriodsId(periodId, null);
			// 将标准排序和待排序省份区分开
			for (int i = 0; i < allPeriodsStoreList.size(); i++) {
				if (allPeriodsStoreList.get(i).getStoreCode().equals(storeCode)) {
					standard = allPeriodsStoreList.get(i);
				} else {
					otherProvince.add(allPeriodsStoreList.get(i));
				}
			}
			String standardIds = standard.getCommoditys();
			if (StringUtils.isBlank(standardIds)) {
				resultJson.put("msg", "success");
				ControllerUtil.writeJson(response, resultJson.toJSONString());
				return;
			}
			if (standardIds.contains("move")) {
				standardIds = standardIds.replaceAll("move,", "");
				standardIds = standardIds.replaceAll(",move", "");
			}
			String[] standardArray = standardIds.split(",");
			// 按标准顺序进行排序
			for (int i = 0; i < otherProvince.size(); i++) {
				YxPoolPeriodsStore other = otherProvince.get(i);
				String otherIds = other.getCommoditys();
				if (StringUtils.isBlank(otherIds)) {
					continue;
				}
				if (otherIds.contains("move")) {
					otherIds = otherIds.replaceAll("move,", "");
					otherIds = otherIds.replaceAll(",move", "");
				}
				String[] otherArray = otherIds.split(",");
				if (standardArray.length != otherArray.length) {
					continue;
				}
				// 将当前数组排序
				// Arrays.sort(otherMcArraySort);
				// 比较元素是否相同
				Boolean flag = true;
				for (int j = 0; j < standardArray.length; j++) {
					flag = false;
					for (int k = 0; k < otherArray.length; k++) {
						if (otherArray[k].equals(standardArray[j])) {
							flag = true;
							break;
						}
					}
					if (!flag) {// 数组内有不同元素，跳出
						break;
					}
				}
				if (flag) {
					other.setCommoditys(standard.getCommoditys());
					cprService.updateCommoditys(other);
				}
			}
			resultJson.put("msg", "success");
			ControllerUtil.writeJson(response, resultJson.toJSONString());
		}else {
			resultJson.put("msg", "success");
			ControllerUtil.writeJson(response, resultJson.toJSONString());
	    }
  }
    
   
	
	/** 进入添加池当前期商品SKUID页面 **/
    @RequestMapping("/toCommodityAdd")
	public ModelAndView toCommodityAdd(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(defaultValue = "0") Integer poolType,
			@RequestParam(defaultValue = "0") Long poolId,
			@RequestParam(defaultValue = "") String groupIds) {
		ModelAndView mv = new ModelAndView("b2bpool/addCommodity");
        YxPool yxPool = poolService.queryPoolById(poolId);

        List<B2BStoreGroup> groupList = groupService.getStoreGroupListByGroupIds(groupIds);
        mv.addObject("yxPool", yxPool);
        mv.addObject("groupList", groupList);
        if(groupList!=null){
        	mv.addObject("groupListLen", groupList.size());
        }else{
            mv.addObject("groupListLen", 0);
        }
        mv.addObject("cmsEnv", pageEnv);
		return mv;
	}
 /** 保存池当前期商品信息 **/
    @RequestMapping("/savePeriodsCommodity")
	@LogTrace(msgFomort={"添加池商品(ids:{data},门店:{stores})","period","{periodId}"})
	public void savePeriodsCommodity(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") String data,
			@RequestParam(defaultValue = "0") Long periodId, @RequestParam(defaultValue = "0") int type, @RequestParam(defaultValue = "1") String commodityType,
			 @RequestParam(defaultValue = "0") String storeGroups, @RequestParam(defaultValue = "0") String stores)
			throws Exception {
		String ids[] = data.replaceAll("\r\n", ",").replaceAll("\n", ",").split(",");
        String resultString = "";
        
        resultString=commodityService.addB2BPoolCommodityById(ids, periodId, type, storeGroups,stores);
		ControllerUtil.writeJson(response,resultString);
	}
	/**保存添加素材信息
	 * @throws Exception **/
	@RequestMapping("/saveCommodityPicOrText")
	@LogTrace(msgFomort={"添加池素材(picUrl:{cpc},门店:{stores},类型：{commodityType})","period","{periodId}"})
	public void saveCommodityPicOrText(HttpServletRequest request,HttpServletResponse response,
			@ModelAttribute B2BPoolCommodity cpc,@RequestParam(defaultValue = "0")  Long periodId,@RequestParam(defaultValue = "0")  String storeGroups,@RequestParam(defaultValue = "0")  String stores,@RequestParam(defaultValue = "0")  String commodityType) throws Exception {

		if(cpc!=null){
			cpc.setCreateId(UserUtil.getUserId());
			cpc.setOriginate(Integer.parseInt(commodityType));
        	commodityService.savePicAndText(cpc, periodId, storeGroups,stores);
		}
		ControllerUtil.writeJson(response);
	}
    	
	/**初始化进入商品信息编辑页面**/
	@RequestMapping("/toCommodityEdit")
	public ModelAndView toCommodityEdit(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue = "0")  String id) {
		ModelAndView mv = new ModelAndView("b2bpool/updateCommodity");
		B2BPoolCommodity obj = commodityService.queryCommodityById(id);
		mv.addObject("obj", obj);
		return mv;
	}

	/**初始化进入图片信息编辑页面**/
	@RequestMapping("/toMPicCommodityEdit")
	public ModelAndView toMPicCommodityEdit(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue = "0")  String id) {
		ModelAndView mv = new ModelAndView("b2bpool/updatePicCommodity");
		B2BPoolCommodity obj = commodityService.queryCommodityById(id);
		mv.addObject("obj", obj);
		return mv;
	}
	
	/**初始化进入图片信息编辑页面**/
	@RequestMapping("/toMTextCommodityEdit")
	public ModelAndView toMTextCommodityEdit(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue = "0")  String id) {
		ModelAndView mv = new ModelAndView("b2bpool/updateTextCommodity");
		B2BPoolCommodity obj = commodityService.queryCommodityById(id);
		mv.addObject("obj", obj);
		return mv;
	}
	
	/**更新保存商品信息
	 * @throws Exception **/
    @RequestMapping("/updateCommodity")
	@LogTrace(msgFomort={"更新池商品(data:{data})","commodity","0","data:id"})
	public void updateCommodity(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue = "0") String data) throws Exception {
    	B2BPoolCommodity poolCommodity = (B2BPoolCommodity) JSONObject.parseObject(data, B2BPoolCommodity.class);
		poolCommodity.setUpdateId(UserUtil.getUserId());
		commodityService.updateCommodityFromPool(poolCommodity);
		ControllerUtil.writeJson(response);
	}
	
	/**更新保存商品信息
	 * @throws Exception **/
    @RequestMapping("/updatePicCommodity")
	@LogTrace(msgFomort={"更新池素材(data:{data})","commodity","0","data:id"})
	public void updatePicCommodity(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue = "0") String data) throws Exception {
    	B2BPoolCommodity poolCommodity = (B2BPoolCommodity) JSONObject.parseObject(data, B2BPoolCommodity.class);
		poolCommodity.setUpdateId(UserUtil.getUserId());
		commodityService.updateCommodityPicFromPool(poolCommodity);
		ControllerUtil.writeJson(response);
	}
	
	/** 进入替换当前期商品货号页面 **/
    @RequestMapping("/replaceCommodity")
	public ModelAndView replaceCommodity(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") Long id) {
		ModelAndView mv = new ModelAndView("b2bpool/replaceCommodity");
		mv.addObject("id", id);
		return mv;
	}
	
	/** 替换池当前期商品信息 **/
    @RequestMapping("/replaceAllPeriodsCommodity")
	@LogTrace(msgFomort={"替换所有区域池商品(商品id:{oldId},门店:{storeCode},新商品卖场id:{newGood})","period","{periodId}"})
	public void replaceAllPeriodsCommodity(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") String newGood,
			@RequestParam(defaultValue = "0") Long periodId,  @RequestParam(defaultValue = "0") String storeCode,
			@RequestParam(defaultValue = "0") String oldId) throws Exception {
		String result = commodityService.checkAndReplaceCommodity(newGood, oldId, periodId, storeCode);
		ControllerUtil.writeJson(response, result);
	}
    
    /**
     * 获取单个商品
     * @param request
     * @param response
     * @param id
     */
    @RequestMapping("/getSingleCommodityProperty")
	public void getSingleCommodityProperty(HttpServletRequest request,
			HttpServletResponse response, @RequestParam String id) {
		B2BPoolCommodity commodity = commodityService.getCommodityById(id);
		String storeNames = storeService.getStoreNamesByCodes(commodity
				.getStoreCodes());
		commodity.setStoreNames(storeNames);
		ControllerUtil.writeJson(response, JSONObject.toJSONString(commodity));
	}
    
	
	/** 导出excel文件 */
    /*@RequestMapping("/exportToExcel")
	public void exportToExcel(@ModelAttribute B2BPoolCommodity obj, HttpServletRequest request, HttpServletResponse response) {
		if (obj.getTitle() != null) {
			obj.setTitle(StringUtils.encodeStr(obj.getTitle()));
		}
		obj.setFirstRow(1);
		obj.setPageRows(Integer.MAX_VALUE);
		try {
			String fileName = obj.getPoolId() + "_" + obj.getPeriods() + ".xlsx";
			fileName = ExportUtil.encodeFilename(fileName, request);
			OutputStream outputStream = response.getOutputStream();
			response.setHeader("Content-disposition", "attachment; filename=" + fileName);// 组装附件名称和格式
			response.setContentType("application/vnd.ms-excel");
			commodityService.exportExcel(outputStream, obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	
//    @RequestMapping("/getMcIdCommodity")
//	public void getMcIdCommodity(HttpServletRequest request,HttpServletResponse response,
//    		@RequestParam(defaultValue = "0") String areaSeq,
//    		@RequestParam(defaultValue = "0") String mcId) throws Exception{
//		YxPoolCommodity ooc = null;//commodityService.getMcIdCommodity(mcId,areaSeq);
//		if(ooc!=null){
//			//ooc.setPicUrl(CmsUtil.getImgHost()+ooc.getPicUrl());
//			String jsonvalue = JSONObject.toJSONString(ooc);
//			ControllerUtil.writeJson(response, jsonvalue);
//		}else{
//			ControllerUtil.writeJson(response);
//		}
//	}
	
	/*private void processStoreGroups(List<B2BPoolCommodity> list){
		if (list != null &&list.size() > 0) {
			Map<String, B2BStoreGroup> groupMap= groupService.getFmap();
			StringBuilder sb =null ;
			for(B2BPoolCommodity c :list){
				sb = new StringBuilder();
				String groupIds=c.getGroupIds();
				if(StringUtils.isNotBlank(groupIds)){
					String[] ids=groupIds.split(",");
					for(String id:ids){
						if(groupMap.get(id)!=null){
							sb.append(groupMap.get(id).getName()).append("  ");
						}
					}
				}
				c.setGroupNames(sb.toString());
			}
		}
	}*/
}
