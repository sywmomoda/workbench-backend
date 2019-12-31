package com.feiniu.yx.pool.service.impl;

import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.service.PoolCommodityService;
import com.feiniu.yx.pool.service.PoolDataService;
import com.feiniu.yx.util.YxPoolConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PoolDataServiceImpl implements PoolDataService {

	@Autowired
	private PoolCommodityService poolCommodityService;
	
	/**
	 * 
	 * @author lizhiyong
	 * 2017年3月13日
	 * @param id 池ID
	 * @param storeCode 门店code
	 * @param type 需要显示的商品类型，详见YxPoolConst
	 * @param count 需要显示的商品数目，为0时为不限量
	 * @return
	 * TODO
	 */
	@Override
	public List<YxPoolCommodity> findListByIdAndType(Long poolId, String storeCode,String type, int count, String previewTime) {
		List<YxPoolCommodity> list = poolCommodityService.listCommodityDataForModule(poolId, storeCode,previewTime);
		if(type==null || "".equals(type) || "null".equals(type)){
			type =YxPoolConst.COMMODITY_TYPE_TOSHOW_ALLINONE;
		}
		List<YxPoolCommodity> resultList = new ArrayList<YxPoolCommodity>();
		if(list!=null&&list.size()>0){
			for(YxPoolCommodity poolCommodity :list){
				if(!poolCommodity.isExist()){
					continue;
				}
				//COUNT为0时不限制数量
				if(count==0 || resultList.size()<count){
					int originate = poolCommodity.getOriginate();
					//商品（全部、商品、商品+图片、商品+文字链）
					if(originate==YxPoolConst.YX_COMMODITY_TYPE_COMMODITY){//商品
						if(type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_ALLINONE)
								|| type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_COMMODITY)
										|| type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_COMMODITYPIC)
										|| type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_COMMODITYTEXT)){
							resultList.add(poolCommodity);
						}
					}
					//图片（全部、图片、商品+图片、图片+文字链）
					else if(originate==YxPoolConst.YX_COMMODITY_TYPE_PIC){//图片
						if(type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_ALLINONE)
								|| type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_PIC)
										|| type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_COMMODITYPIC)
										|| type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_PICTEXT)){
							resultList.add(poolCommodity);
						}
					}
					//文字链（全部、文字链、文字链+图片、商品+文字链）
					else if(originate==YxPoolConst.YX_COMMODITY_TYPE_TEXT){//文字链
						if(type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_ALLINONE)
								|| type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_TEXT)
										|| type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_COMMODITYTEXT)
										|| type.equals(YxPoolConst.COMMODITY_TYPE_TOSHOW_PICTEXT)){
							resultList.add(poolCommodity);
						}
					}
				}else{
					break;
				}
			}
		}
		return resultList;
	}

}
