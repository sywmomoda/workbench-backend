package com.feiniu.b2b.page.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.common.B2BConstant;
import com.feiniu.b2b.page.service.B2BModuleDataService;
import com.feiniu.b2b.pool.entity.B2BPoolCommodity;
import com.feiniu.b2b.pool.service.B2BPoolDataService;
import com.feiniu.yx.core.utils.ThreadLocalUtil;
import com.feiniu.yx.page.dao.ModuleDao;
import com.feiniu.yx.page.dao.PageDao;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.template.dao.YXModuleTypeDao;
import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.util.HttpTookit;
import com.feiniu.yx.util.ImageUtils;
import com.feiniu.yx.util.YxPoolConst;
/**
 * @author tongwenhuan
 * 2017年2月24日 上午11:09:56
 */
@Component
public class B2BModuleDataServiceImpl implements B2BModuleDataService {
	
	private static Logger logger = Logger.getLogger(B2BModuleDataServiceImpl.class);

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	private YXModuleTypeDao yXModuleTypeDao;
	
	@Autowired
	private B2BPoolDataService poolDataService;
	
	@Autowired
	private PageDao pageDao; 
	

	@Override
	public String findModuledData(Module m , String storeCode) {
		return findModuleJSON(m,storeCode).toJSONString();
	}
	
	@Override
	public String findModuledData(Long id, String storeCode) {
		if(id == null)return null;
		Module m = moduleDao.queryModuleByID(id);
		if(m == null)return null;
		YXModuleType mt = yXModuleTypeDao.getYXModuleTypeById(m.getModuleTypeId());
		m.setYxModuleType(mt);
		String moduleData = null;
		try {
			moduleData = findModuledData(m, storeCode);
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		return moduleData;
	}
	
	/**
	 * 处理模板数据
	 * 返回数据格式：
	 * {
			id:1,
			moduleType:{
				id:1,
				code:test
			},
			moduleProperties:{
				name:xxxxx,
				picUrl:xxxxx
			},
			commodityList:[]
		}
	 */
	private JSONObject findModuleJSON(Module m , String storeCode) {
		if(m == null)return null;
		JSONObject mjo = new JSONObject();
		mjo.put("id", m.getId());
		mjo.put("name", m.getName());
		mjo.put("storeScope", m.getStoreScope());
		mjo.put("administrator", m.getAdministrator());
		mjo.put("moduleType", initModuleTypeData(m));
		mjo.put("storeCode", storeCode);
		String moduleProperties = m.getModuleProperties();
		JSONObject proJo = JSONObject.parseObject(moduleProperties);
		Long pageId = m.getPageId();
		Page cmsPage = pageDao.queryCMSPageByID(pageId);
		ThreadLocalUtil.setPage(cmsPage);
		if(null != cmsPage){
			String pagePro = cmsPage.getPageProperties();
			proJo.putAll(JSONObject.parseObject(pagePro));
		}
		mjo.put("moduleProperties", proJo);
		mjo.put("backendB2B", "yes");
		Integer totalCount = proJo.getInteger("totalCount");
		
		//是否是自动抓取
		String source = proJo.getString("source");
		if("keyword".equals(source)){
             HashMap<String, String> params = new HashMap<String, String>();
             
                JSONObject param = new JSONObject();
                JSONObject body = new JSONObject();

                param.put("apiVersion", "");
                param.put("appVersion", "");
                param.put("areaCode", "");
                
                body.put("rt_no", storeCode);
                body.put("sup_no", "B0023");
                body.put("sup_type", "1");
                body.put("page", "0");
                body.put("page_size", totalCount);
                body.put("orderFlag", "3");
                body.put("desc", "1");
                body.put("searchData", proJo.getString("keyword"));
                body.put("facet", "0");
                param.put("body", body);

             params.put("data", param.toString());
      
			 String data = HttpTookit.doPost(B2BConstant.B2B_COMMODITY_URI, params);				 
			 JSONObject result = JSON.parseObject(data);
			 
			 List<B2BPoolCommodity> commodityList = new ArrayList<B2BPoolCommodity>();
			 
			 JSONArray list= result.getJSONObject("body").getJSONArray("recommendList").getJSONObject(0).getJSONArray("MerchandiseList");
			 
			 if(list != null){
				 for(int i = 0;i< list.size();i++){
					 JSONObject one =  (JSONObject) list.get(i);
					 B2BPoolCommodity commodity = new B2BPoolCommodity();
					commodity.setUnit(one.getString("spec"));
					commodity.setCommodityId(one.getString("item_no"));
					commodity.setTitle(one.getString("gname"));
					//commodity.setClassId(one.getInteger("cid")); 废弃
					commodity.setClassId(null);
                    commodity.setPrice(one.getFloat("price"));
                    commodity.setStockSum(one.getLong("stock")); //stock -1无限 0没库存 1有库存
                    commodity.setPromGrade(one.getInteger("prom_grade"));
                    
                    if(StringUtils.isNotBlank(one.getString("imgurl"))){
                    	commodity.setPicUrl(ImageUtils.getImageUrl(one.getString("imgurl")));//图片地址
            		}
                    commodityList.add(commodity);
				 }
					mjo.put("commodityList", commodityList);

			 }else{
					mjo.put("commodityList", new ArrayList<Object>());
			 }
		}else{
			//绑定单个池
			Long poolId = proJo.getLong("poolId");
			String commodityType = proJo.getString("commodityType");
			//默认商品图片类型
			if(StringUtils.isBlank(commodityType)) commodityType = YxPoolConst.COMMODITY_TYPE_TOSHOW_COMMODITYPIC;
			if(poolId != null){
				List<?> commodityList = null;
				if(totalCount == null)totalCount = 1;
				commodityList = poolDataService.findListByIdAndType(poolId, storeCode, commodityType, totalCount.intValue());
				mjo.put("commodityList", commodityList == null ? new ArrayList<Object>():commodityList);
			}
			//绑定多个池
			Object poolIds = proJo.get("poolIds");
			if(poolIds != null){
				List<Object>  mutiCommodityList = new ArrayList<Object>(); 
				if (poolIds instanceof List) {
					List<?> tPoolIdList = (List<?>) proJo.get("poolIds");
					for (int i = 0; i < tPoolIdList.size(); i++) {
						String s_pid = String.valueOf(tPoolIdList.get(i));
						if(StringUtils.isBlank(s_pid)){
							mutiCommodityList.add(new ArrayList<Object>());
						}else {
							poolId = Long.parseLong(s_pid);
							List<?> cList = poolDataService.findListByIdAndType(poolId, storeCode, commodityType,totalCount.intValue());
							mutiCommodityList.add(cList);
						}
					}
				}
				mjo.put("mutiCommodityList", mutiCommodityList);
			}
		}

		//需要特殊处理的模块
		customDataProcess(mjo);
		return mjo;
	}

	/**
	 * 初始化模块类型数据
	 * @author tongwenhuan
	 * 2017年2月24日
	 * @param long id
	 */
	private JSONObject initModuleTypeData(Module m) {
		JSONObject mtjo = new JSONObject();
		YXModuleType mt = m.getYxModuleType(); 
		if(mt == null){
			mt = yXModuleTypeDao.getYXModuleTypeById(m.getModuleTypeId());
		}
		mtjo = (JSONObject) JSON.toJSON(mt);
		return mtjo;
	}
	
	/**
	 * 特殊处理
	 * @param jo
	 */
	private void customDataProcess(JSONObject jo){
		//商品默认图片
		jo.put("b2b_baseImg", B2BConstant.B2B_BASEIMG);
		
		JSONObject mtjo = jo.getJSONObject("moduleType");
		//需要特殊处理
		if(mtjo!=null && StringUtils.isNotBlank(mtjo.getString("moduleService"))){
			String moduleService =mtjo.getString("moduleService");
			CustomModule customModule = null;
			if(moduleService.indexOf(",")!=-1){
				String[] services=moduleService.split(",");
				for(String ss:services){
					WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
					if(wac.containsBean(ss)){
						customModule =  (CustomModule)wac.getBean(ss);
					}
					try {
						if(null != customModule){
							customModule.findCustomData(jo);	
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else{
				WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
				if(wac.containsBean(moduleService)){
					customModule =  (CustomModule)wac.getBean(moduleService);
				}
					
				try {
					if(null != customModule){
						customModule.findCustomData(jo);	
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
