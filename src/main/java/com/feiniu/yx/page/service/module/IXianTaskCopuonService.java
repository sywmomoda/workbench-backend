package com.feiniu.yx.page.service.module;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.YXCouponService;
import com.feiniu.yx.core.impl.ModuleCommodityServiceImpl;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.CustomModule;
import com.feiniu.yx.page.service.PageService;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class IXianTaskCopuonService implements CustomModule {
	@Autowired
	private PageService pageService;
	@Autowired
	private YXCouponService yxCouponService;
	@Autowired
	private ModuleCommodityServiceImpl moduleCommodityService;
	@Override
	public void findCustomData(JSONObject mjo) throws Exception {
		JSONObject moduleProperties = mjo.getJSONObject("moduleProperties");
		String couponIds = moduleProperties.getString("couponId");
		String storeCode = mjo.getString("storeCode");
		if(StringUtils.isNotBlank(couponIds)){
			if(couponIds.indexOf(",")!=-1){
				JSONArray couponArray = moduleProperties.getJSONArray("couponId");
				String[] md5s = new String[couponArray.size()];
				String[] picUrl = new String[couponArray.size()];
				String[] status = new String[couponArray.size()];
				for(int i=0;i<couponArray.size();i++){
					String couponId = couponArray.getString(i);
					JSONObject checkResult = checkCouponInfo(couponId,storeCode);
					if("OK".equals(checkResult.getString("msg"))){
						picUrl[i] = checkResult.getString("commodityPic");
						status[i] = "1";
					}else{
						picUrl[i] = "";
						status[i] = "0";
					}
					md5s[i]= MD5Util.getMD5(couponArray.getString(i) + MD5Util.COUPON_MD5_KEY);
				}
				moduleProperties.put("md5s", md5s);
				moduleProperties.put("picUrl", picUrl);
				moduleProperties.put("status", status);
			}else{
				JSONObject checkResult = checkCouponInfo(moduleProperties.getString("couponId"),storeCode);
				if("OK".equals(checkResult.getString("msg"))){
					moduleProperties.put("picUrl",checkResult.getString("commodityPic"));
					moduleProperties.put("status","1");
				}else{
					moduleProperties.put("picUrl","");
					moduleProperties.put("status","0");
				}
				moduleProperties.put("md5s", MD5Util.getMD5(moduleProperties.getString("couponId") + MD5Util.COUPON_MD5_KEY));
			}
		}
		
		Long pageId = mjo.getLong("pageId");
		Page page = pageService.queryPageByID(pageId);
		if(null == page){
			return;
		}
		
		String pagePro = page.getPageProperties();
		JSONObject pageProObj = JSONObject.parseObject(pagePro);
		String pagebgcolor =pageProObj.getString("pagebgcolor"); 
		if(StringUtils.isBlank(pagebgcolor)){
		  return;
		}
		moduleProperties.put("pagebgColor", pagebgcolor);
	}


	public JSONObject checkCouponInfo(String id,String storeCode){
		JSONObject res = new JSONObject();
		JSONObject dataObject = yxCouponService.getCouponInfoByCouponIds(id);
		JSONArray arr = dataObject.getJSONArray("list");
		if (arr != null) {
			for (int i = 0; i < arr.size(); i++) {
				JSONObject jj = arr.getJSONObject(i);
				String couponId = jj.getString("couponId");
				// publishStatus '发布状态(0:未发布、1:已发布、2:手动下架、3:自动过期),
				// activityStatus String 活动状态(1：未开始2：已开始 3：已停领4：已停止)
				// isExport {"0": "行销发券", "1": "用户领券"},
				if (id.equals(couponId)) {
					String check = checkCouponStatus(jj);
					if ("OK".equals(check)) {
						res.put("couponId", couponId);
						res.put("couponName", jj.getString("title"));
						res.put("vouchersDiscount", jj.getString("vouchersDiscount"));
						res.put("vouchersPrice", jj.getString("vouchersPrice"));
						res.put("scopeDescription", jj.getString("scopeDescription"));
						res.put("discountType", jj.getString("discountType"));
						res.put("limitUser", jj.getString("limitUser"));
						// couponType:优惠券适用范围(0:全场通用 1:范围限定
						// 2:商品限定3:品牌限定,4:单品限定即单品券) discountType=7
						// 且couponType=4为单品券；discountType=7
						// 且couponType!=4则为普通优惠券
						res.put("couponType", jj.getString("couponType"));
						res.put("totalMaxCount", jj.getString("totalMaxCount"));
						res.put("dayMaxCount", jj.getString("dayMaxCount"));
						res.put("skuId", jj.getString("skuId"));
						if("5".equals(jj.getString("discountType"))){
							res.put("skuId", jj.getString("skuIdGift"));
							String comId = jj.getString("skuIdGift");
							//此处验证商品信息
							Map<String, YxPoolCommodity> comMap = moduleCommodityService.mapCommodityNOConponScript(comId.split(","), storeCode);
							if(comMap.get(comId)!=null  && comMap.get(comId).isExist() && comMap.get(comId).getStockSum()>0){
								res.put("msg", check);
								res.put("commodityPic", comMap.get(comId).getPicUrl());
							}else{
								res.put("commodityPic", "");
								res.put("msg", "商品不存在或已售完");
							}
						}else{
							res.put("msg", check);
						}
						res.put("skuName", jj.getString("skuName"));
						String storeIds = jj.getString("storeIds");
						int storeType = jj.getIntValue("storeType");

						//区域信息添加时已处理，此不做检查
						/*if(storeType == 3){  //小区
							storeIds = xqService.getStoreCodesByXiaoQuCodes(storeIds);
						}else if(storeType == 1){ //区域
							String ids = getCouponStoreCodeByPgSeq(storeIds);
							if(StringUtils.isNotBlank(ids)){
								storeIds = ids;
							}
						}*/
					} else {
						res.put("msg", check);
					}
					break;
				}
			}
		}
		if (res.get("msg") == null && res.get("couponName") == null) {
			res.put("msg", "根据优惠券ID:" + id + "未查找到相应的优惠券信息");
		}
		return res;

	}


	/**
	 *  publishStatus '发布状态(0:未发布、1:已发布、2:手动下架、3:自动过期),
	 *  activityStatus	String	活动状态(1：未开始2：已开始 3：已停领4：已停止)
	 *  isExport {"0": "行销发券", "1": "用户领券"},
	 * @author lizhiyong
	 * 2017年6月2日
	 * @param jj
	 * @return
	 */
	public String checkCouponStatus(JSONObject jj){
		int isExport = jj.getIntValue("isExport");

		if(isExport==0){
			return "不能布置券类型为行销发券的优惠券";
		}
		int publishStatus = jj.getIntValue("publishStatus");

		if(publishStatus!=1){
			return "券的发布状态不是已发布状态,请检查";
		}
		int activityStatus = jj.getIntValue("activityStatus");
		if(activityStatus!=2){
			return "券的活动状态不是已开始状态,请检查";
		}

		int totalMaxCount = jj.getIntValue("totalMaxCount");
		int totalTakenCount = jj.getIntValue("totalTakenCount");

		if(totalTakenCount>=totalMaxCount){
			return "该券已被领完,请重新设置";
		}

		String supCouponId =jj.getString("supCouponId");
		if(StringUtils.isNotBlank(supCouponId)){
			return "不能布置组合券的子券";
		}
		String isCrmExchange = jj.getString("isCrmExchange");
		String isCrmLimitUser = jj.getString("isCrmLimitUser");
		if(!"0".equals(isCrmExchange)){
			return "不能布置直冲券";
		}
		if(!"0".equals(isCrmLimitUser)){
			return "不能布置用户限定券";
		}

		int discountType = jj.getIntValue("discountType");
		String useDeadlineType = jj.getString("useDeadlineType");//使用期限。1、固定时间段 2、领取后几天  为2时接券开始和截止时间、券使用开始和结束时间均为空
		if(discountType!=10 && "1".equals(useDeadlineType)){
			long receiveEndTime = 0;
			if(jj.getString("receiveEndTimestamp")!=null && !"".equals(jj.getString("receiveEndTimestamp"))){
				receiveEndTime = jj.getLongValue("receiveEndTimestamp");
			}
			long nowTimestamp =new Date().getTime();

			if(receiveEndTime<nowTimestamp){
				return "当前时间大于该券的领取结束时间";
			}

			long useEndTime = 0;
			if(jj.getString("useEndTimestamp")!=null && !"".equals(jj.getString("useEndTimestamp"))){
				useEndTime = jj.getLongValue("useEndTimestamp");
			}

			if(useEndTime<nowTimestamp){
				return "当前时间大于该券的使用结束时间";
			}

		}
		return "OK";
	}

}
