package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.ModuleProperPlusService;
import com.feiniu.yx.page.service.ModuleService;
import com.feiniu.yx.page.service.PageService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.PoolDataService;
import com.feiniu.yx.remote.CommodityActivityRemote;
import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXModuleTypeService;
import com.feiniu.yx.template.service.YXTemplateService;
import com.feiniu.yx.util.YxPoolConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
@Service
public class INewsOnlyPriceService implements CustomModule {
	
	private static final String INDEX_PAGE_ID = SystemEnv.getProperty("index.page.id");
	
	@Autowired
	private PageService pageService;
	@Autowired
	private YXTemplateService templateService;
	@Autowired
	private ModuleService moduleService;
	@Autowired
	private YXModuleTypeService moduleTypeService;
	@Autowired
	private ModuleProperPlusService moduleProperPlusService;
	@Autowired
	private PoolDataService poolDataService;
	
	@Override
	public void findCustomData(JSONObject mjo) {
		String storeCode = mjo.getString("storeCode");
		String previewTime = mjo.getString("backendPreviewTime");
		JSONObject moduleType = mjo.getJSONObject("moduleType");
		if(moduleType==null || !"i-newsOnlyPrice".equals(moduleType.getString("code"))){//非首页组件，取首页数据
			Module m = getHomeGiftModule(storeCode);
			if(m != null){
				String moduleProperties = m.getModuleProperties();
				JSONObject proJo = JSONObject.parseObject(moduleProperties);
				Map<String, ModuleProperPlus> mppMap =  moduleProperPlusService.queryModuleProperMapByModuleId(m.getId());
				if(moduleProperties != null){
					ModuleProperPlus mmp = mppMap.get(storeCode);
					if(mmp!=null){
						JSONObject properObj = (JSONObject) JSONObject.parse(mmp.getModuleProper());
						for(Entry<String, Object> entry : properObj.entrySet()){
							proJo.put(entry.getKey(), entry.getValue());
						}
					}
				}
				mjo.put("moduleProperties", proJo);
			}
		}
		JSONObject proJo  = mjo.getJSONObject("moduleProperties");	
		mjo.put("bgColor", proJo.getString("bgColor"));
		List<YxPoolCommodity> commodityList = poolDataService.findListByIdAndType(proJo.getLong("poolId"), storeCode,
				YxPoolConst.COMMODITY_TYPE_TOSHOW_COMMODITY,100, previewTime);
		
		//过滤掉非新人的活动商品
		List<YxPoolCommodity> resultList = new ArrayList<YxPoolCommodity>();
		String commodityIds = "";
		int count = 0;
		for(YxPoolCommodity pc: commodityList){
			commodityIds += pc.getCommodityId()+",";
			count++;
			if(count ==20){//每20个请求一次,计数重置
				filterNewsActivity(commodityIds, storeCode, commodityList, resultList);
				commodityIds = "";
				count = 0;
			}
		}
		if(count>0){
			filterNewsActivity(commodityIds, storeCode, commodityList, resultList);
		}
		
		mjo.put("commodityList", resultList);
		
		//背景池图片
		JSONObject bgObj = new JSONObject();
		List<JSONObject> bgPicList = getCommodityList(proJo,"poolIdBg", YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC,storeCode,1,previewTime);
		if(bgPicList.size()>0){
			bgObj = bgPicList.get(0);
		}
		if(StringUtils.isNotBlank(bgObj.getString("imgUrl"))){
			mjo.put("bgImgUrl", bgObj.getString("imgUrl"));
		}
	}
	
	/**
	 * 过滤掉非新人的活动商品
	 * @param commodityIds
	 * @param storeCode
	 * @param commodityList
	 * @param resultList
	 */
	public void filterNewsActivity(String commodityIds, String storeCode, List<YxPoolCommodity> commodityList, List<YxPoolCommodity> resultList){
		if(commodityIds.endsWith(",")){
			commodityIds = commodityIds.substring(0,commodityIds.length()-1);
		}
		Map<String,JSONObject> commodityMap = CommodityActivityRemote.mapCommodityActivityV1(storeCode, commodityIds.split(","));
		if(commodityMap==null){
			return;
		}
		for(YxPoolCommodity pc: commodityList){
			JSONObject singleResult = commodityMap.get(pc.getCommodityId());
			if(singleResult!=null){
				String card = singleResult.getString("card");
				String singleType = singleResult.getString("singleType");
				//判断是否是限新人的单品特价行销活动
				if("6".equals(card) && "singleSpecialOffer".equals(singleType)){
					String price = singleResult.getString("price");
					if(StringUtils.isNotBlank(price)){
						pc.setPrice(Float.parseFloat(price));
					}
					String costPrice = singleResult.getString("costPrice");
					if(StringUtils.isNotBlank(costPrice)){
						pc.setMarketPrice(Float.parseFloat(costPrice));
					}
					if(pc.getMarketPrice()<=pc.getPrice()){
						pc.setMarketPrice(0f);
					}
					resultList.add(pc);
				}
			}
		}
	}
	
	/**
	 * 根据首页ID找到首页新人礼包组件“i-newsOnlyPrice”
	 * @return
	 */
	public Module getHomeGiftModule(String storeCode){
		Module m = null;
		Long id = pageService.getPageIdByStoreCode(INDEX_PAGE_ID, storeCode);
		Page page = pageService.queryPageByID(id);
		if(page!=null){
			YXTemplate template = templateService.getYXTemplateById(page.getTemplateId());
			Map<String, Module> moduleMap = moduleService.getCMSModuleMapByModuleIds(page.getModules());
			Map<Long, YXModuleType> moduleTypeMap = moduleTypeService.getModuleTypeMapByIds(template.getModuleTypes());
			for (String moduleIdStr : page.getModules().split(",")) {
				Module module = moduleMap.get(moduleIdStr);
				YXModuleType moduleType = moduleTypeMap.get(module.getModuleTypeId());
				if(moduleType!=null && moduleType.getCode().equals("i-newsOnlyPrice")){
					m = module;
					break;
				}
			}
		}
		return m;
	}
	
	
	/**
	 * 获取商品
	 * 
	 * @param obj
	 * @param storeCode
	 * @param totalCount
	 */
	private List<JSONObject> getCommodityList(JSONObject obj, String pojoId, String type, String storeCode,
			Integer totalCount, String previewTime) {

		List<JSONObject> list = new ArrayList<JSONObject>();
		if (null == obj) {
			return list;
		}
		if (StringUtils.isBlank(storeCode)) {
			return list;
		}
		Long poolId = obj.getLong(pojoId);
		poolId = null == poolId ? 1L : poolId;
		List<YxPoolCommodity> commodityList = poolDataService.findListByIdAndType(poolId, storeCode,
				type,totalCount.intValue(),previewTime);

		if (null == commodityList || commodityList.size() == 0) {
			return list;
		}

		for (int i = 0; i < commodityList.size(); i++) {
			YxPoolCommodity pc = commodityList.get(i);
			JSONObject temp = new JSONObject();
			temp.put("imgUrl", pc.getPicUrl());
			list.add(temp);
		}
		return list;
	}
}
