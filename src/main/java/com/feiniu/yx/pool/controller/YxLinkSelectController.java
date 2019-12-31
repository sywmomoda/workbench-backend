package com.feiniu.yx.pool.controller;

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
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.PageService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.YxPoolCommodityService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.ControllerUtil;

@Controller
@RequestMapping("/yxLinkSelect")
public class YxLinkSelectController {
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private YXStoreService storeService;
	
	@Autowired
	private YxPoolCommodityService yxPoolCommodityService;
	
	@RequestMapping("/queryYxActivity")
    public ModelAndView queryYxActivity(@ModelAttribute Page page, HttpServletRequest request, HttpServletResponse response){
        ModelAndView mv = new ModelAndView("cms/activityQuery");
        page.setType(3);
        List<Page> list = pageService.queryPageForLinkSelect(page);
        mv.addObject("list", list);
        mv.addObject("obj", page);
        return mv;
    }

	//查询未过期的优鲜活动页
	@RequestMapping("/queryYxActivityData")
	public void queryYxActivityData(HttpServletRequest request,HttpServletResponse response, @ModelAttribute Page page) throws Exception{
		page.setType(2);
		List<Page> list = pageService.queryPageForLinkSelect(page);
        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put("list", list);
        resultMap.put("obj", page);
        String json = JSONObject.toJSONString(resultMap);
        ControllerUtil.writeJson(response, json);
	}
	
	@RequestMapping("/getStoreName")
	public void getStoreName(HttpServletRequest request, HttpServletResponse response, @RequestParam() String storeCodes) throws Exception{
		List<YXStore> list = storeService.getStoreByCodes(storeCodes);	
		JSONObject object = new JSONObject();
		for(int i =0; i < list.size(); i++){
			YXStore ca = list.get(i);
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
            @RequestParam(defaultValue = "0") String storeCodes,@RequestParam(defaultValue = "0") String searchType) throws Exception {
		JSONObject object = yxPoolCommodityService.getCommodityForSelect(art_no, storeCodes,searchType);
		ControllerUtil.writeJson(response, object.toJSONString());
		
    }
	@RequestMapping("/getCommodityShow")
	public void getCommodityShow(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") String commodityId,
            @RequestParam(defaultValue = "0") String storeCode) throws Exception {
        JSONObject js = yxPoolCommodityService.getCommodityForShow(commodityId, storeCode);
        String json = js.toJSONString();
        ControllerUtil.writeJson(response, json);
    }
	
	@RequestMapping("/validCommoditysForShow")
	public void validCommoditysShow(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") String commodityIds,
            @RequestParam(defaultValue = "0") String storeCodes) throws Exception {
		Map<String,Map<String, YxPoolCommodity>> js = yxPoolCommodityService.validCommodityForShow(commodityIds, storeCodes);
        String json = JSONObject.toJSONString(js);
        ControllerUtil.writeJson(response, json);
    }
	
	@RequestMapping("/getCommodityForModule")
	public void getCommodityForModule(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") String commodityId,
			@RequestParam(defaultValue = "0") String selectStoreCode) throws Exception {
        JSONObject js = yxPoolCommodityService.getCommodityForModule(commodityId,selectStoreCode);
        String json = js.toJSONString();
        ControllerUtil.writeJson(response, json);
    }
	
	@RequestMapping("/getCommodityByCouponIdForModule")
	public void getCommodityByCouponIdForModule(HttpServletRequest request, HttpServletResponse response, @RequestParam(defaultValue = "0") String commodityId,
			@RequestParam(defaultValue = "0") String selectStoreCode,@RequestParam String couponId){
		JSONObject js = yxPoolCommodityService.getCommodityByCouponIdForModule(commodityId,selectStoreCode,couponId);
        String json = js.toJSONString();
        ControllerUtil.writeJson(response, json);
	}
	
}
