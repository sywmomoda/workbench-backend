package com.feiniu.yx.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.ModuleMutliTabData;
import com.feiniu.yx.core.utils.ThreadLocalUtil;
import com.feiniu.yx.page.dao.ModuleDao;
import com.feiniu.yx.page.dao.PageDao;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.ModuleDataService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.PoolDataService;
import com.feiniu.yx.template.dao.YXModuleTypeDao;
import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.util.YxPoolConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tongwenhuan
 * 2017年2月24日 上午11:09:56
 */
@Component
public class ModuleDataServiceImpl implements ModuleDataService {

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	private PageDao pageDao;

	@Autowired
	private YXModuleTypeDao yXModuleTypeDao;

	@Autowired
	private PoolDataService poolDataService;

	@Autowired
    private ModuleMutliTabData moduleMutliTabData;

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
	@Override
	public JSONObject findModuleJSON(Module m , String storeCode, String previewTime) {
		if(m == null) {
            return null;
        }
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
		mjo.put("pageId", pageId);
		//绑定单个池
		Long poolId = proJo.getLong("poolId");
		if(poolId == null && proJo.containsKey("one")){
			JSONObject one = proJo.getJSONObject("one");
			poolId = one.getLong("poolId");
		}
		String commodityType = proJo.getString("commodityType");
		//默认商品图片类型
		if(StringUtils.isBlank(commodityType)) {
            commodityType = YxPoolConst.COMMODITY_TYPE_TOSHOW_COMMODITYPIC;
        }
		Integer totalCount = proJo.getInteger("totalCount");
		List<YxPoolCommodity> commodityList = null;
		if(poolId != null){
			if(totalCount == null) {
                totalCount = 1;
            }
            //图片要求放在组件的第一张
			boolean firstPic = proJo.containsKey("firstPic");
			commodityType =  firstPic ? YxPoolConst.COMMODITY_TYPE_TOSHOW_COMMODITY : commodityType;
			commodityList = poolDataService.findListByIdAndType(poolId, storeCode, commodityType,totalCount.intValue(),previewTime);
			commodityList = commodityList == null ? new ArrayList<YxPoolCommodity>() : commodityList;
			if(firstPic){
				List<YxPoolCommodity> picList = poolDataService.findListByIdAndType(poolId, storeCode, YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC,1,previewTime);
				if(null != picList && picList.size() > 0){
					commodityList.addAll(0, picList);
				}
			}

		}

		mjo.put("commodityList", commodityList);
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
						List<YxPoolCommodity> cList = poolDataService.findListByIdAndType(poolId, storeCode, commodityType,totalCount.intValue(),previewTime);
						mutiCommodityList.add(cList);
					}
				}
			}
			mjo.put("mutiCommodityList", mutiCommodityList);
		}
		mjo.put("backendPreviewTime",previewTime);//此处后台预览时间处理
		String dataType = proJo.getString("dataType");
		String dataTypeConst = "multiTab";
		if(null != dataType && dataType.equals(dataTypeConst)){
			List<JSONObject> tabDataList = moduleMutliTabData.getModuleData(m,storeCode,previewTime);
			mjo.put("tabDataList",tabDataList);
		}
		//需要特殊处理的模块
		customDataProcess(mjo);
		return mjo;
	}



	@Override
    public String findModuledData(Module m , String storeCode, String previewTime) {
		return findModuleJSON(m,storeCode, previewTime).toJSONString();
	}

	@Override
	public String findModuledData(Long id, String storeCode, String previewTime) {
		if(id == null) {
            return null;
        }
		Module m = moduleDao.queryModuleByID(id);
		if(m == null) {
            return null;
        }
		YXModuleType mt = yXModuleTypeDao.getYXModuleTypeById(m.getModuleTypeId());
		m.setYxModuleType(mt);
		return findModuledData(m, storeCode, previewTime);
	}

	/**
	 * 初始化模块类型数据
	 * @author tongwenhuan
	 * 2017年2月24日
	 * @param m
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

	@Override
    public void customDataProcess(JSONObject jo){
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
