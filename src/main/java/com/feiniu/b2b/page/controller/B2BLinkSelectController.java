package com.feiniu.b2b.page.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.page.service.B2BPageService;
import com.feiniu.b2b.pool.service.B2BPoolCommodityService;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.entity.B2BStoreGroup;
import com.feiniu.b2b.store.service.B2BStoreGroupService;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.pool.entity.YxPicLinks;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.util.ControllerUtil;

@Controller
@RequestMapping("/b2bLinkSelect")
public class B2BLinkSelectController {
	
	@Autowired
	private B2BPageService pageService;
	
	@Autowired
	private B2BStoreService storeService;
	
	@Autowired
	private B2BStoreGroupService storeGroupService;
	
	@Autowired
	private B2BPoolCommodityService yxPoolCommodityService;
	
	@RequestMapping("/queryActivity")
    public ModelAndView queryB2BActivity(@ModelAttribute Page page, HttpServletRequest request, HttpServletResponse response){
        ModelAndView mv = new ModelAndView("cms/activityQuery");
        page.setType(3);
        List<Page> list = pageService.queryPageForLinkSelect(page);
        mv.addObject("list", list);
        mv.addObject("obj", page);
        return mv;
    }

	//查询未过期的优鲜活动页
	@RequestMapping("/queryActivityData")
	public void queryB2BActivityData(HttpServletRequest request,HttpServletResponse response, @ModelAttribute Page page) throws Exception{
		page.setType(3);
		List<Page> list = pageService.queryPageForLinkSelect(page);
        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put("list", list);
        resultMap.put("obj", page);
        String json = JSONObject.toJSONString(resultMap);
        ControllerUtil.writeJson(response, json);
	}
	
	@RequestMapping("/getStoreName")
	public void getStoreName(HttpServletRequest request, HttpServletResponse response, @RequestParam() String storeCodes) throws Exception{
		List<B2BStore> list = storeService.getStoreByCodes(storeCodes);	
		JSONObject object = new JSONObject();
		for(int i =0; i < list.size(); i++){
			B2BStore ca = list.get(i);
			object.put(ca.getCode(), ca.getName());
		}
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	/**
	 * 在链接选择中 选择去商详，通过商品ID
	 * @author lizhiyong
	 * 2017年3月13日
	 * @param request
	 * @param response
	 * @param art_no
	 * @param areaSeq
	 * @param module
	 * @param xlindex
	 * @throws Exception
	 * TODO
	 */
	@RequestMapping("/getStoreCommoditysJson")
	public void getStoreCommoditysJson(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") String art_no,
            @RequestParam(defaultValue = "0") String groupIds, @RequestParam(defaultValue = "0") String module, @RequestParam(defaultValue = "0") String xlindex) throws Exception {
		List<B2BStoreGroup> groupList = storeGroupService.getStoreGroupListByGroupIds(groupIds);
		//List<B2BStore> list = storeService.getB2BStoreByGroupIds(groupIds);
		String storeCodes = "";
		for(B2BStoreGroup g:groupList){
			storeCodes += g.getStoreId()+",";
		}
		if(storeCodes.endsWith(",")){
			storeCodes = storeCodes.substring(0,storeCodes.length()-1);
		}
		YxPicLinks ooc = new YxPicLinks();
		ooc = yxPoolCommodityService.getCommodityForSelect(art_no, storeCodes);
		
		List<YxPoolPeriodsStore> allList = (List<YxPoolPeriodsStore>) ooc.getSeqList();
		Map<String,Object> resultMap = new HashMap<String,Object>();
		for(B2BStoreGroup sg : groupList){
			List<YxPoolPeriodsStore> successStores = new ArrayList<YxPoolPeriodsStore>();
			List<YxPoolPeriodsStore> failedStores = new ArrayList<YxPoolPeriodsStore>();
			String sIds = "," + sg.getStoreId() + ",";
			for(YxPoolPeriodsStore ypps:allList){
				if(sIds.indexOf(","+ypps.getStoreCode()+",")!=-1){
					if(ypps.getCommoditys()!=""){
						successStores.add(ypps);
					}else{
						failedStores.add(ypps);
					}
				}
			}
			resultMap.put(sg.getId()+"_S", successStores);
			resultMap.put(sg.getId()+"_F", failedStores);
		}
		resultMap.put("groups", groupList);
		
		/*String storeNames = "";
		int i=0;
        for (B2BStore ys:list) {
        	i++;
            String storeName = ys.getName();
            if (i == 1) {
            	storeNames = storeName;
            } else {
            	storeNames = storeNames + "," + storeName;
            }
        }
        
        
        
        ooc.setStoreName(storeNames);
        ooc.setStoreCode(storeCodes);*/
       
        //resultMap.put("yxPicLinks", ooc);
        String json = JSONObject.toJSONString(resultMap);
        ControllerUtil.writeJson(response, json);
    }
	@RequestMapping("/getCommodityShow")
	public void getCommodityShow(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") String commodityId,
            @RequestParam(defaultValue = "0") String storeCode) throws Exception {
        JSONObject js = yxPoolCommodityService.getCommodityForShow(commodityId, storeCode);
        String json = js.toJSONString();
        ControllerUtil.writeJson(response, json);
    }
}
