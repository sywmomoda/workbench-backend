package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.core.impl.BaseCommodityServiceImpl;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.entity.YxRemoteCommodity;
import com.feiniu.yx.page.service.*;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.PoolDataService;
import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXModuleTypeService;
import com.feiniu.yx.template.service.YXTemplateService;
import com.feiniu.yx.util.YxPoolConst;
import com.fn.cache.client.RedisCacheClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
public class INewsN4OneService implements CustomModule {
	
	private static final String INDEX_PAGE_ID = SystemEnv.getProperty("index.page.id");
	@Autowired
	private RemoteCommodityService remoteCommodityService;
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
	
	@Autowired
	private BaseCommodityServiceImpl commodityService;//远程商品接口
	@Autowired
	private RedisCacheClient cacheClient;
	@Override
	public void findCustomData(JSONObject mjo) {
		String storeCode = mjo.getString("storeCode");
		if(StringUtils.isBlank(storeCode)){
			return;
		}
		String previewTime = mjo.getString("backendPreviewTime");
		JSONObject proJo  = mjo.getJSONObject("moduleProperties");
		JSONObject moduleType = mjo.getJSONObject("moduleType");
		if(moduleType==null || (!"i-newsnforone".equals(moduleType.getString("code"))
                || !"i-newsnforoneV2".equals(moduleType.getString("code")))){//非首页组件，取首页数据
			Module m = getHomeGiftModule(storeCode);
			if(m != null){
				String moduleProperties = m.getModuleProperties();
				proJo = JSONObject.parseObject(moduleProperties);
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
				proJo.put("remoteCount", 100);
				mjo.put("moduleProperties", proJo);
			}
		}
		
		String category = proJo.getString("activityData");
		if(proJo.containsKey("tabId")){
            category =  getPromoIdByMultiTab(proJo,storeCode);
        }
		mjo.put("activityId", category);
		mjo.put("showPrice", proJo.getString("showPrice"));
		mjo.put("bgColor", proJo.getString("bgColor"));
		Integer remoteCount = proJo.getInteger("remoteCount");
		if(null == remoteCount){
			remoteCount = 10;
		}
		List<YxRemoteCommodity> rcList = remoteCommodityService.queryCommodityForNSelectOne(storeCode, category, remoteCount);
		List<YxPoolCommodity> resultCommodityList =  convertRemote(rcList,storeCode);
		mjo.put("commodityList", resultCommodityList);
		if(resultCommodityList.size()==4){
			mjo.put("title", "新人专享 低价4选1");
		}else{
			mjo.put("title", "新人专享 低价选一件");
		}
		
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
     * 获取行销活动id
     * @param proJo
     * @return
     */
	private String getPromoIdByMultiTab(JSONObject proJo,String storeCode){
        String promoId = "";
	    JSONArray tabDataList = proJo.getJSONArray("tabDataList");
	    if(null == tabDataList || tabDataList.size() == 0){
	        return promoId;
        }
	    JSONObject sin = tabDataList.getJSONObject(0);
	    if(null == sin){
            return promoId;
        }
        promoId = sin.getString("linkData");
        promoId = promoId == null ? "" : promoId;
	    return promoId;
     }


	/**
	 * 根据首页ID找到首页新人礼包组件“i-newsnforone”
	 * @return
	 */
	public Module getHomeGiftModule(String storeCode){
		Module m = null;
        Module mV2 = null;  //新版本组件
		Long id = pageService.getPageIdByStoreCode(INDEX_PAGE_ID, storeCode);
		Page page = pageService.queryPageByID(id);
		if(page!=null){
			YXTemplate template = templateService.getYXTemplateById(page.getTemplateId());
			Map<String, Module> moduleMap = moduleService.getCMSModuleMapByModuleIds(page.getModules());
			Map<Long, YXModuleType> moduleTypeMap = moduleTypeService.getModuleTypeMapByIds(template.getModuleTypes());
			for (String moduleIdStr : page.getModules().split(",")) {
				Module module = moduleMap.get(moduleIdStr);
				YXModuleType moduleType = moduleTypeMap.get(module.getModuleTypeId());
				if(moduleType!=null && moduleType.getCode().equals("i-newsnforone")){
					m = module;
				}
                if(moduleType!=null && moduleType.getCode().equals("i-newsnforoneV2")){
                    mV2 = module;
                }

			}
			if(null != mV2){
                m = mV2;
            }
		}
		return m;
	}
	
	

	/**
	 * 远程数据排序并转换成池类型商品
	 * @param rcList
	 * @param storeCode
	 * @return
	 */
	private List<YxPoolCommodity> convertRemote(List<YxRemoteCommodity> rcList, String storeCode) {
		List<YxPoolCommodity> remoteList = new ArrayList<YxPoolCommodity>();
		String goodNos = "";
		for(YxRemoteCommodity rc: rcList){//初始化数据
				goodNos += rc.getCommodityId()+",";
		}
		
		Map<String, YxPoolCommodity> commodityMap = commodityService.mapCommodityPriceInfo(goodNos.split(","), storeCode);
		List<YxPoolCommodity> outList = new ArrayList<YxPoolCommodity>();
		int index = 0;
		for(YxRemoteCommodity rc: rcList){
			if(commodityMap!=null && commodityMap.get(rc.getCommodityId())!=null){
				YxPoolCommodity rcNew = remoteCommodityService.convertToPoolCommodity(rc);
				rcNew.setOriginate(1);
				YxPoolCommodity pricePC = commodityMap.get(rc.getCommodityId());
				rcNew.setUnit(pricePC.getUnit().replace("/", ""));
				rcNew.setHasService(pricePC.getHasService());
				rcNew.setPrice(pricePC.getPrice());
				rcNew.setMarketPrice(pricePC.getMarketPrice());
				rcNew.setStockSum(pricePC.getStockSum());
				if(pricePC.getStockSum()<=0){
					outList.add(rcNew);
				}else{
					index++;
					remoteList.add(rcNew);
				}
				if(index==4){
					break;
				}
			}
		}
		if(index<=4 && outList.size()>0){
			for(YxPoolCommodity pc:outList){
				index++;
				remoteList.add(pc);
				if(index==4){
					break;
				}
			}
		}
		
		return remoteList;
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
