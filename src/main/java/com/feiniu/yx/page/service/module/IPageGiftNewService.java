package com.feiniu.yx.page.service.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.YXConstant;
import com.feiniu.yx.common.service.YXCouponService;
import com.feiniu.yx.config.SystemEnv;
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
import com.feiniu.yx.util.MD5Util;

@Service
public class IPageGiftNewService implements CustomModule {
	
	private static final String COUPON_API_ADDRESS =SystemEnv.getProperty("coupon.api.host")+"/coupon/outerSrv/getCouponInfo";
	private static final Logger logger =Logger.getLogger(IPageGiftNewService.class);
	
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
	private YXCouponService couponService;
	
	@Override
	public void findCustomData(JSONObject mjo) {
		String storeCode = mjo.getString("storeCode");
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
				}else{
					proJo.put("couponId",null);
				}
			}
			
			mjo.put("moduleProperties", proJo);
			
			Object couponIdInfo = proJo.get("couponId");
			String couponIds = null;
			Object couponDescribeInfo = proJo.get("couponDescribe");
			Map<String, String> describeMap = new HashMap<String, String>();
			if(couponIdInfo!=null){
				if(couponIdInfo instanceof JSONArray){
					couponIdInfo = (JSONArray)couponIdInfo;
					Object[] couponIdArray = ((JSONArray) couponIdInfo).toArray();
					couponIds = StringUtils.join(couponIdArray, ",");
					//限制门槛字段
					if(couponDescribeInfo != null) {
						if(couponDescribeInfo instanceof JSONArray){
							Object[] describeArray = ((JSONArray) couponDescribeInfo).toArray();
							for(int i = 0; i < couponIdArray.length; i++) {
								Object d = describeArray[i];
								Object id = couponIdArray[i];
								if(d != null && id != null) {
									describeMap.put(id.toString(), d.toString());
								}
							}
						} 
					}
				}else{
					couponIds = couponIdInfo.toString();
					if(couponDescribeInfo != null) {
						describeMap.put(couponIds, couponDescribeInfo.toString());
					}
				}
			}
			mjo.put("couponIds", couponIds);
			mjo.put("md5", MD5Util.getMD5(couponIds + MD5Util.COUPON_MD5_KEY));
			if(StringUtils.isNotBlank(couponIds)){
				List<JSONObject> couponList = couponService.setModuleCoupon(couponIds, describeMap);
				if(couponList!=null && couponList.size()>0){
					mjo.put("couponList", couponList);
				}
			}
		}
	}
	
	/**
	 * 根据首页ID找到首页新人礼包组件“i-gift”
	 * @return
	 */
	public Module getHomeGiftModule(String storeCode){
		Module m = null;
		Long id = pageService.getPageIdByStoreCode(YXConstant.INDEX_PAGE_ID, storeCode);
		Page page = pageService.queryPageByID(id);
		if(page!=null){
			YXTemplate template = templateService.getYXTemplateById(page.getTemplateId());
			Map<String, Module> moduleMap = moduleService.getCMSModuleMapByModuleIds(page.getModules());
			Map<Long, YXModuleType> moduleTypeMap = moduleTypeService.getModuleTypeMapByIds(template.getModuleTypes());
			for (String moduleIdStr : page.getModules().split(",")) {
				Module module = moduleMap.get(moduleIdStr);
				YXModuleType moduleType = moduleTypeMap.get(module.getModuleTypeId());
				if(moduleType!=null && moduleType.getCode().equals("i-gift")){
					m = module;
					break;
				}
			}
		}
		return m;
	}
	
	/**
	 * 远程接口查询更新优惠券信息
	 * @param couponIds
	 * @return
	 *//*
	public List<JSONObject> getCouponInfoByCouponIds(String couponIds, Map<String, String> describeMap){
		JSONObject con= new JSONObject();
		con.put("couponIds", couponIds);
		Map<String,String> paramMap = new HashMap<>();
		paramMap.put("param", con.toJSONString());
		String res =null;
		try{
			res  = HttpTookit.post(COUPON_API_ADDRESS, paramMap);
		}catch(Exception e){
			logger.error("getCouponInfoByCouponIds error!",e);
			return null;
		}	
		List<JSONObject> retList= null;
		if(res!=null){
			JSONObject resJson=JSONObject.parseObject(res);
			if(resJson!=null && "200".equals(resJson.getString("code"))){
				JSONObject data = resJson.getJSONObject("data");
				if(data!=null){
					JSONArray couponList= data.getJSONArray("list");
					if(couponList!=null && couponList.size()>0){
						retList = new ArrayList<>();
						for(int i=0;i<couponList.size();i++){
							JSONObject temp=couponList.getJSONObject(i);
							//JSONObject infoObject =temp.getJSONObject("info");
							if(temp!=null){
								JSONObject coup = new JSONObject();
								//2为已开始
								if("2".equals(temp.getString("activityStatus")) && !"10".equals(temp.getString("discountType"))){
									String couponId =  temp.getString("couponId");
									coup.put("couponId", couponId);
									coup.put("couponName", temp.getString("title"));
									coup.put("couponType", temp.getIntValue("discountType") == 5 ? 2: 1);//5礼品券，7优惠券，10组合券
									if(temp.getIntValue("discountType") == 7 && temp.getIntValue("couponType") == 4){//单品券直降
										coup.put("couponType", 3);
									}else if(temp.getIntValue("discountType") == 7 && temp.getIntValue("couponType") == 5){//单品券固定金额
										coup.put("couponType", 4);
									}
									coup.put("couponValue", temp.getString("vouchersDiscount"));
									coup.put("couponThreshold", temp.getString("vouchersPrice"));
									String couponDescribe = describeMap.get(couponId);
									coup.put("couponDescribe", couponDescribe != null ? couponDescribe : "");
									//coup.put("scopeDescription", temp.getString("scopeDescription"));
									//coup.put("discountType", temp.getIntValue("discountType"));//5礼品券，7优惠券，10组合券
									retList.add(coup);
								}
							}
						}
					}
				}
			}
		}
		return retList;
	}*/

}
