package com.feiniu.yx.page.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.YxCommodityRepeatCtl;
import com.feiniu.yx.page.entity.YxRemoteCommodity;

/**
 * @author tongwenhuan
 * 2017年2月24日 上午11:09:08
 */
public interface CommodityRepeatCtlService {
	
	public List<YxCommodityRepeatCtl> queryYxCommodityRepeatCtl(YxCommodityRepeatCtl yrc);
	public void addYxCommodityRepeatCtl(YxCommodityRepeatCtl yrc);
	public void delYxCommodityRepeatCtl(YxCommodityRepeatCtl yrc);
	/**
	 * 保存当日数据
	 * @param commoditys
	 * @param moduleId
	 * @param storeCode
	 * @param codeType
	 */
	public void saveTodayCommoditys(String commoditys, Long moduleId, String storeCode, String codeType);
	
	/**获取前一天数据
	 * @param moduleId
	 * @param storeCode
	 * @param codeType
	 * @return
	 */
	public String getYesterdayCommoditys(Long moduleId, String storeCode, String codeType);
	
	
	/**
	 * 获取最近显示过的商品ID
	 * @param moduleId
	 * @param storeCode
	 * @param codeType
	 * @param days
	 * @return
	 */
	public String getRecentDayCommoditys(Long moduleId, String storeCode,
			String codeType, int days);
	
}
