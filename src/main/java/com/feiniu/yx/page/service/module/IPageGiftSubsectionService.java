package com.feiniu.yx.page.service.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.YXConstant;
import com.feiniu.yx.common.service.YXCouponService;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.ModuleProperPlus;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.ModuleProperPlusService;
import com.feiniu.yx.page.service.ModuleService;
import com.feiniu.yx.page.service.PageService;
import com.feiniu.yx.template.entity.YXModuleType;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXModuleTypeService;
import com.feiniu.yx.template.service.YXTemplateService;

/**
 * 
 * @author yehui
 * 2019年7月8日
 */
@Service
public class IPageGiftSubsectionService implements CustomModule {
	
	@Autowired
	private PageService pageService;
	
	@Autowired
	private ModuleService moduleService;
	
	@Autowired
	private YXModuleTypeService moduleTypeService;
	
	@Autowired
	private ModuleProperPlusService moduleProperPlusService;
	
	@Autowired
	private YXCouponService couponService;
	
	@Autowired
	private YXTemplateService templateService;
	
	@Override
	public void findCustomData(JSONObject mjo) {
		String storeCode = mjo.getString("storeCode");
		Module m = getHomeGiftModule(storeCode);
		if(m == null){
		  return;
		}
		String moduleProperties = m.getModuleProperties();
		JSONObject proJo = JSONObject.parseObject(moduleProperties);
		mjo.put("moduleProperties", proJo);
		Map<String, ModuleProperPlus> mppMap =  moduleProperPlusService.queryModuleProperMapByModuleId(m.getId());
		if(mppMap.size() == 0) {
			return;
		}
		ModuleProperPlus  properPlus =  mppMap.get(storeCode);
		if(null == properPlus) {
			return;
		}
		String modulePro = properPlus.getModuleProper();
		if(StringUtils.isBlank(modulePro)) {
			return;
		}
		JSONObject moduleObj = JSONObject.parseObject(modulePro);
		for(String key : moduleObj.keySet()) {
			JSONObject sin = moduleObj.getJSONObject(key);
			setProper(key,sin,proJo);
		}
	}
	
	/**
	 * 
	 * @param sin  券属性
	 * @param proJo  module 数据
	 * @return
	 */
	private void setProper(String key,JSONObject sin,JSONObject proJo) {
		Object couponIdInfo = sin.get("couponId");
		Object couponDescribeInfo = sin.get("couponDescribe");
		if(null == couponIdInfo) {
			return;
		}
		JSONArray couponIdList = new JSONArray();
		JSONArray couponDescribeList = new JSONArray();
		if(couponIdInfo instanceof List){
			couponIdList = (JSONArray) couponIdInfo;
			couponDescribeList = (JSONArray) couponDescribeInfo;
		}else {
			couponIdList.add(couponIdInfo);
			couponDescribeList.add(couponDescribeInfo);
		}
		if(null == couponIdList || couponIdList.size() == 0) {
			return;
		}
		Map<String, String> describeMap = new HashMap<String, String>();
		for(int i = 0; i < couponIdList.size(); i++) {
			String id = couponIdList.getString(i);
			describeMap.put(id, couponDescribeList.getString(i));
		}
		List<JSONObject> couponList = couponService.setModuleCoupon(StringUtils.join(couponIdList.listIterator(), ","), describeMap);
		if(couponList!=null && couponList.size()>0){
			JSONObject keySin = new JSONObject();
			keySin.put("list", couponList);
			Float total = 0f;
			for(JSONObject coupon : couponList) {
				if(null == coupon) {
					continue;
				}
				total += coupon.getFloatValue("couponValue");
			}
			keySin.put("total", total);
			proJo.put(key, keySin);
		}
	}
	
	/**
	 * 根据首页ID找到首页新人礼包组件“i-giftSubsection”
	 * @return
	 */
	public Module getHomeGiftModule(String storeCode){
		Module m = null;
		Long id = pageService.getPageIdByStoreCode(YXConstant.INDEX_PAGE_ID,storeCode);
		Page page = pageService.queryPageByID(id);
		if(page!=null){
			YXTemplate template = templateService.getYXTemplateById(page.getTemplateId());
			Map<String, Module> moduleMap = moduleService.getCMSModuleMapByModuleIds(page.getModules());
			Map<Long, YXModuleType> moduleTypeMap = moduleTypeService.getModuleTypeMapByIds(template.getModuleTypes());
			for (String moduleIdStr : page.getModules().split(",")) {
				Module module = moduleMap.get(moduleIdStr);
				YXModuleType moduleType = moduleTypeMap.get(module.getModuleTypeId());
				if(moduleType!=null && moduleType.getCode().equals("i-giftSubsection")){
					m = module;
					break;
				}
			}
		}
		return m;
	}

}
