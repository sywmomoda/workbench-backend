package com.feiniu.yx.page.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.feiniu.yx.page.dao.YxCommodityRepeatCtlDao;
import com.feiniu.yx.page.entity.YxCommodityRepeatCtl;
import com.feiniu.yx.page.service.CommodityRepeatCtlService;
import com.feiniu.yx.util.DateUtil;

@Component
public class CommodityRepeatCtlServiceImpl implements CommodityRepeatCtlService {
	@Autowired
	private YxCommodityRepeatCtlDao  yxCommodityRepeatCtlDao;
	
	@Override
	public List<YxCommodityRepeatCtl> queryYxCommodityRepeatCtl(
			YxCommodityRepeatCtl yrc) {
		return yxCommodityRepeatCtlDao.queryYxCommodityRepeatCtlByTypeAndCode(yrc);
	}

	@Override
	public void addYxCommodityRepeatCtl(YxCommodityRepeatCtl yrc) {
		yxCommodityRepeatCtlDao.insertYxCommodityRepeatCtl(yrc);
		
	}

	@Override
	public void delYxCommodityRepeatCtl(YxCommodityRepeatCtl yrc) {
		yxCommodityRepeatCtlDao.deleteYxCommodityRepeatCtlByTypeAndCode(yrc);
		
	}

	/* (non-Javadoc)
	 * @see com.feiniu.yx.page.service.CommodityRepeatCtlService#saveTodayCommoditys(java.lang.String, java.lang.Long, java.lang.String, java.lang.String)
	 */
	@Override
	public void saveTodayCommoditys(String commoditys, Long moduleId,
			String storeCode, String codeType) {
		//后台不保存数据处理，直接取online数据，保持同步
		/*YxCommodityRepeatCtl queryYrc = new YxCommodityRepeatCtl();
		queryYrc.setModuleId(moduleId);
		queryYrc.setType(codeType);
		queryYrc.setDateStamp(DateUtil.getDefindedDate(new Date()));
		queryYrc.setOnlineStatus(onlineStatus);
		queryYrc.setStoreCode(storeCode);
		queryYrc.setCreateId("system");
		queryYrc.setUpdateId("system");
		List<YxCommodityRepeatCtl> crcList = queryYxCommodityRepeatCtl(queryYrc);
		
		if(crcList.size()>0){
			for(YxCommodityRepeatCtl crc: crcList){
				String oldCommoditys = crc.getCommodityIds();
				for(String uc:commoditys.split(",")){
					if(crc.getCommodityIds().indexOf(uc)==-1){
						crc.setCommodityIds(crc.getCommodityIds()+ "," + uc);
					}
				}
				if(!oldCommoditys.equals(crc.getCommodityIds())){
					yxCommodityRepeatCtlDao.updateCommdityRepeatCtl(crc);
				}
			}
		}else{
			queryYrc.setCommodityIds(commoditys);
			queryYrc.setCreateTime(new Date());
			yxCommodityRepeatCtlDao.insertYxCommodityRepeatCtl(queryYrc);
		}*/
		
	}

	/* (non-Javadoc)
	 * @see com.feiniu.yx.page.service.CommodityRepeatCtlService#getYesterdayCommoditys(java.lang.Long, java.lang.String, java.lang.String)
	 */
	@Override
	public String getYesterdayCommoditys(Long moduleId, String storeCode, String codeType) {

		YxCommodityRepeatCtl queryYrc = new YxCommodityRepeatCtl();
		queryYrc.setModuleId(moduleId);
		queryYrc.setType(codeType);
		queryYrc.setStoreCode(storeCode);
		queryYrc.setDateStamp(DateUtil.getYesterday());
		queryYrc.setOnlineStatus(1);
		String reusltCommoditys = "";
		List<YxCommodityRepeatCtl> crcList = queryYxCommodityRepeatCtl(queryYrc);
		for(YxCommodityRepeatCtl crc: crcList){
			reusltCommoditys += crc.getCommodityIds()+",";
		}
		return reusltCommoditys;
	}

	@Override
	public String getRecentDayCommoditys(Long moduleId, String storeCode,
			String codeType, int days) {
		YxCommodityRepeatCtl queryYrc = new YxCommodityRepeatCtl();
		queryYrc.setModuleId(moduleId);
		queryYrc.setType(codeType);
		queryYrc.setStoreCode(storeCode);
		queryYrc.setOnlineStatus(1);
		String reusltCommoditys = "";
		for(int i=days; i>0; i--){//保证最初ID按顺序处理
			Calendar calendar = Calendar.getInstance();
			queryYrc.setDateStamp(DateUtil.getDate(calendar, "yyyyMMdd", -i));
			List<YxCommodityRepeatCtl> crcList = queryYxCommodityRepeatCtl(queryYrc);
			for(YxCommodityRepeatCtl crc: crcList){
				reusltCommoditys += crc.getCommodityIds()+",";
			}
		}
		
		return reusltCommoditys;
	}


}
