package com.feiniu.yx.pool.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.feiniu.yx.common.entity.ReturnT;
import com.feiniu.yx.common.interceptor.LogTrace;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.pool.service.PoolCommodityService;
import com.feiniu.yx.pool.service.YxPoolCommodityService;
import com.feiniu.yx.pool.service.YxPoolPeriodsService;
import com.feiniu.yx.pool.service.YxPoolPeriodsStoreService;
import com.feiniu.yx.pool.service.YxPoolService;
import com.feiniu.yx.store.entity.YXStoreGroup;
import com.feiniu.yx.store.service.YXStoreGroupService;
import com.feiniu.yx.util.ControllerUtil;
import com.feiniu.yx.util.UserUtil;

@Controller
@RequestMapping("/yxPoolCommodity")
public class YxPoolCommodityController {
	
	@Autowired
	private YxPoolService poolService;
	
	@Autowired
	private YxPoolPeriodsService poolPeriodsService;
	
    @Autowired
    private YxPoolPeriodsStoreService cprService;

	@Autowired
	private YxPoolCommodityService commodityService;
	
	@Autowired
	private YXStoreGroupService groupService;
	
	@Autowired
	private PoolCommodityService poolCommodityService;
	
    private static String pageEnv = SystemEnv.getProperty("feiniu.pageEnv");

	/**
	 * 查询池当前期商品列表页面
	 **/
	@RequestMapping("/queryPoolCommoditys")
	public String queryPoolCommoditys() {
		return "yxpool/listPoolCommodity";
	}

	/**
	 * 查池期数分页数据
	 * @param data
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value="/poolCommodityListData", method=POST)
	public void poolAndStoreData(@RequestParam(defaultValue="") String data,
			HttpServletRequest request, HttpServletResponse response) throws Exception{
		JSONObject resultObject = poolCommodityService.pageCommodityData(data);
		ControllerUtil.writeJson(response,JSONObject.toJSONStringWithDateFormat(resultObject,"yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteDateUseDateFormat, SerializerFeature.DisableCircularReferenceDetect));
	}
	
	/**
	 * 
	 * @param oldOrder
	 * @param newOrder
	 * @param editOrder
	 * @param request
	 * @param response
	 */
	@RequestMapping("commodityOrder")
	@ResponseBody
	public ReturnT<String> commodityOrder(@RequestParam(defaultValue = "0") Integer oldOrder,
			@RequestParam(defaultValue = "0") String data,
			@RequestParam(defaultValue = "0") Integer newOrder,
			@RequestParam(defaultValue = "0") Integer editOrder,
			HttpServletRequest request, HttpServletResponse response) {
		YxPoolCommodity obj = JSONObject.parseObject(data,YxPoolCommodity.class);
		String pcommodityId = obj.getCommodityId();
		return poolPeriodsService.resetOrders(obj, pcommodityId, oldOrder,newOrder);
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
    @LogTrace(msgFomort={"删除商品(商品ids:{commodityId},门店:{storeCode})","period","{periodId}"})
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
    @LogTrace(msgFomort={"批量删除商品(商品ids:{Id},门店:{storeCode})","period","{periodId}"})
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
		YxPoolPeriodsStore standard = null;
		List<YxPoolPeriodsStore> otherProvince = new ArrayList<YxPoolPeriodsStore>();
		JSONObject resultJson = new JSONObject();
		if(storeCode != null && !"".equals(storeCode) && periodId != 0){
			List<YxPoolPeriodsStore> allPeriodsStoreList = cprService.getPoolPeriodsStoresByPeriodsId(periodId, null);
			//将标准排序和待排序省份区分开
			for(int i=0;i<allPeriodsStoreList.size();i++){
				if(allPeriodsStoreList.get(i).getStoreCode().equals(storeCode)){
					standard = allPeriodsStoreList.get(i);
				}else{
					otherProvince.add(allPeriodsStoreList.get(i));
				}
			}
			if(standard.getCommoditys().contains("move")){
				standard.setCommoditys(standard.getCommoditys().replaceAll("move,", ""));
				standard.setCommoditys(standard.getCommoditys().replaceAll(",move", ""));
			}
			
			if(standard.getCommoditys()!=null&&!standard.getCommoditys().equals("")){
				String[] standardArray = standard.getCommoditys().split(",");
    			//按标准顺序进行排序
    			for(int i=0;i<otherProvince.size();i++){
    				YxPoolPeriodsStore other = otherProvince.get(i);
    				if(other.getCommoditys().contains("move")){
    					other.setCommoditys(other.getCommoditys().replaceAll("move,", ""));
    					other.setCommoditys(other.getCommoditys().replaceAll(",move", ""));
    				}
    				if(other.getCommoditys()!=null&&!other.getCommoditys().equals("")){
    					String[] otherArray = other.getCommoditys().split(",");
        				//比较长度是否想等
        				if(standardArray.length == otherArray.length){
        					//将当前数组排序
        					//Arrays.sort(otherMcArraySort);
        					//比较元素是否相同
        					Boolean flag = true;
        					for(int j=0; j<standardArray.length; j++){
        						flag = false;
        						for(int k=0; k<otherArray.length; k++){
        							if(otherArray[k].equals(standardArray[j])){
        								flag = true;
        								break;
        							}
        						}
        						if(!flag){//数组内有不同元素，跳出
        							break;
        						}
            				}
        					if(flag){
                				other.setCommoditys(standard.getCommoditys());
                				cprService.updateCommoditys(other);
        					}
        				}
    				}
    			}
    			resultJson.put("msg", "success");
    			ControllerUtil.writeJson(response, resultJson.toJSONString());
			}else{
				resultJson.put("msg", "null");
				ControllerUtil.writeJson(response, resultJson.toJSONString());
			}
		}else{
			resultJson.put("msg", "err");
			ControllerUtil.writeJson(response, resultJson.toJSONString());
		}
	}
	
	/** 进入添加池当前期商品SKUID页面 **/
    @RequestMapping("/toCommodityAdd")
	public ModelAndView toCommodityAdd(HttpServletRequest request, HttpServletResponse response, 
			@RequestParam(defaultValue = "0") Integer poolType,
			@RequestParam(defaultValue = "0") Long poolId,
			@RequestParam(defaultValue = "") String groupIds) {
		ModelAndView mv = new ModelAndView("yxpool/addCommodity");
        YxPool yxPool = poolService.queryPoolById(poolId);

        List<YXStoreGroup> groupList = groupService.getStoreGroupListByGroupIds(groupIds);
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
		String ids[] = data.replaceAll("\r\n", ",").replaceAll("\n", ",").replaceAll("\\s+","").split(",");
        String resultString = "";
        
        resultString=commodityService.addYxPoolCommodityById(ids, periodId, type, storeGroups,stores,commodityType);
		ControllerUtil.writeJson(response,resultString);
	}
	/**保存添加素材信息
	 * @throws Exception **/
	@RequestMapping("/saveCommodityPicOrText")
	@LogTrace(msgFomort={"添加池素材(picUrl:{cpc},门店:{stores},类型：{commodityType})","period","{periodId}"})
	public void saveCommodityPicOrText(HttpServletRequest request,HttpServletResponse response,
			@ModelAttribute YxPoolCommodity cpc,@RequestParam(defaultValue = "0")  Long periodId,@RequestParam(defaultValue = "0")  String storeGroups,@RequestParam(defaultValue = "0")  String stores,@RequestParam(defaultValue = "0")  String commodityType) throws Exception {

		if(cpc!=null){
			cpc.setCreateId(UserUtil.getUserId());
			cpc.setOriginate(Integer.parseInt(commodityType));
			cpc.setAddOnDate(new Date());
        	commodityService.savePicAndText(cpc, periodId, storeGroups,stores);
		}
		ControllerUtil.writeJson(response);
	}
    	
	/**初始化进入商品信息编辑页面**/
	@RequestMapping("/toCommodityEdit")
	public ModelAndView toCommodityEdit(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue = "0")  String id) {
		ModelAndView mv = new ModelAndView("yxpool/updateCommodity");
		YxPoolCommodity obj = commodityService.queryCommodityById(id);
		mv.addObject("obj", obj);
		return mv;
	}

	/**初始化进入图片信息编辑页面**/
	@RequestMapping("/toMPicCommodityEdit")
	public ModelAndView toMPicCommodityEdit(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue = "0")  String id) {
		ModelAndView mv = new ModelAndView("yxpool/updatePicCommodity");
		YxPoolCommodity obj = commodityService.queryCommodityById(id);
		mv.addObject("obj", obj);
		return mv;
	}
	
	/**初始化进入图片信息编辑页面**/
	@RequestMapping("/toMTextCommodityEdit")
	public ModelAndView toMTextCommodityEdit(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue = "0")  String id) {
		ModelAndView mv = new ModelAndView("yxpool/updateTextCommodity");
		YxPoolCommodity obj = commodityService.queryCommodityById(id);
		mv.addObject("obj", obj);
		return mv;
	}
	
	/**更新保存商品信息
	 * @throws Exception **/
    @RequestMapping("/updateCommodity")
	@LogTrace(msgFomort={"更新池商品(data:{data})","commodity","0","data:id"})
	public void updateCommodity(HttpServletRequest request,HttpServletResponse response,
			@RequestParam(defaultValue = "0") String data) throws Exception {
		YxPoolCommodity poolCommodity = (YxPoolCommodity) JSONObject.parseObject(data, YxPoolCommodity.class);
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
		YxPoolCommodity poolCommodity = (YxPoolCommodity) JSONObject.parseObject(data, YxPoolCommodity.class);
		poolCommodity.setUpdateId(UserUtil.getUserId());
		commodityService.updateCommodityPicFromPool(poolCommodity);
		ControllerUtil.writeJson(response);
	}
	
	/** 进入替换当前期商品货号页面 **/
    @RequestMapping("/replaceCommodity")
	public ModelAndView replaceCommodity(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") Long id) {
		ModelAndView mv = new ModelAndView("yxpool/replaceCommodity");
		mv.addObject("id", id);
		return mv;
	}
	
	/** 替换池当前期商品信息 **/
    @RequestMapping("/replaceAllPeriodsCommodity")
	@LogTrace(msgFomort={"替换所有区域池商品(商品id:{oldId},门店:{storeCode},新商品卖场id:{newGood})","period","{periodId}"})
	public void replaceAllPeriodsCommodity(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") String newGood,
			@RequestParam(defaultValue = "0") Long periodId,  @RequestParam(defaultValue = "0") String storeCode,
			@RequestParam(defaultValue = "0") String oldId,@RequestParam(defaultValue="0")String commodityType) throws Exception {
		String result = commodityService.checkAndReplaceCommodity(newGood, oldId, periodId, storeCode,commodityType);
		ControllerUtil.writeJson(response, result);
	}
	
//	/** 导出excel文件 */
    @RequestMapping("/exportToExcel")
	public void exportToExcel(@RequestParam(defaultValue="")String data, HttpServletRequest request, HttpServletResponse response) {
    	commodityService.exportExcel(data,response,request);
	}

	
	@RequestMapping("/getSingleCommodityProperty")
	public void getSingleCommodityProperty(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="0") String id){
		YxPoolCommodity commodity = commodityService.queryCommodityById(id);
	   ControllerUtil.writeJson(response, JSONObject.toJSONString(commodity));
	}
	
	@RequestMapping("/syncStoreDataByStore")
	public void syncStoreDataByStore(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="") String data){
		JSONObject result = commodityService.syncStoreDataByStore(data);
		ControllerUtil.writeJson(response, result.toJSONString());
	}
	
	@RequestMapping("/getSingleCommoditySaleInfo")
	public void getSingleCommoditySaleInfo(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue="0") String id, @RequestParam(defaultValue="0") String storeCode){
	   JSONObject commodity = commodityService.queryCommoditySaleInfo(id,storeCode);
	   ControllerUtil.writeJson(response, JSONObject.toJSONString(commodity));
	}

	@RequestMapping("/getSingleCommodiyInfo")
    public void getSingleCommodiyInfo(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam(defaultValue = "0") String commodityId) {
        Map<String, YxPoolCommodity> map = commodityService.getRemoteCommodity(new String[]{commodityId}, 0);
        YxPoolCommodity commodity = null;
        if (null != map) {
            commodity = map.get(commodityId);
        }
        commodity = commodity == null ? new YxPoolCommodity() : commodity;
        ControllerUtil.writeJson(response, JSONObject.toJSONString(commodity));
    }


    @RequestMapping("/updateCommoidtyOfStore")
    public void updateCommoidtyOfStore(
            HttpServletResponse response,
            @RequestParam(defaultValue="")Long id,
            @RequestParam(defaultValue="")String storeCodes,@RequestParam(defaultValue="")String groupIds){
        commodityService.updateCommoidtyOfStore(id,storeCodes,groupIds);
        ControllerUtil.writeJson(response);
    }
	
}
