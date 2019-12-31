package com.feiniu.b2b.pool.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.pool.dao.B2BPoolCommodityDao;
import com.feiniu.b2b.pool.entity.B2BPoolCommodity;
import com.feiniu.b2b.pool.entity.B2BPoolCommodityComparator;
import com.feiniu.b2b.pool.service.B2BPoolPeriodsService;
import com.feiniu.b2b.pool.service.B2BPoolService;
import com.feiniu.b2b.store.dao.B2BStoreDao;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.pool.dao.YxPoolPeriodsDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreDao;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.util.UserUtil;
import com.feiniu.yx.util.YxPoolConst;

@Service
public class B2BPoolPeriodsServiceImpl implements B2BPoolPeriodsService{

    private static Logger                logger = Logger.getLogger(B2BPoolPeriodsServiceImpl.class);

    @Autowired
    private YxPoolPeriodsDao cppDao;

    @Autowired
    private B2BPoolCommodityDao       commodityDao;
    
    @Autowired
    private YxPoolPeriodsStoreDao         b2bPoolPeriodsStoreDao;
    
    @Autowired
    private B2BPoolPeriodsStoreServiceImpl  b2bPoolPeriodsStoreService;

    @Autowired
    private B2BStoreService  storeService;
    
    @Autowired
    private B2BPoolService  poolService;
    
    @Autowired
    private B2BPoolPeriodsOnlineServiceImpl  yppOnlineService;
    
    @Autowired
    private B2BRemoteCommodityServiceImpl b2bRemoteCommodityServiceImpl;

    /**
     * 查询其实
     * 
     * @param cpp
     * @return json字符串
     * @throws FeiniuException
     */
    public String queryPoolPeriods(YxPoolPeriods cpp) {
        logger.info("Start call queryCMSPoolPeriods!");
        return JSONObject.toJSONString(cppDao.queryYxPoolPeriodsList(cpp));
    }

    /**
     * @Description: 根据ID查询池期数信息
     * @param currentPeriods
     *            池数ID
     * @return 池期数对象信息
     */
    public YxPoolPeriods queryPoolPeriodById(Long currentPeriods) {
        YxPoolPeriods c = cppDao.queryYxPoolPeriodsByID(currentPeriods);
        List<YxPoolPeriodsStore> cpList = b2bPoolPeriodsStoreService.getPoolPeriodsStoresByPeriodsId(currentPeriods, null);
        c.setStoreList(cpList);
        return c;
    }
    
    public void   deletePeriodAll(Long periodId){
    	YxPoolPeriodsStore yxPoolPeriodsStore = new YxPoolPeriodsStore();
        yxPoolPeriodsStore.setPeriodId(periodId);
        yxPoolPeriodsStore.setPageRows(1000);
        // 获得期数下所有省份与商品的关系
        List<YxPoolPeriodsStore> yxPoolPeriodsStoreList = b2bPoolPeriodsStoreService.queryStoreList(yxPoolPeriodsStore);
        for (YxPoolPeriodsStore pro : yxPoolPeriodsStoreList) {
            String commoditys = pro.getCommoditys();
            if (StringUtils.isNotBlank(commoditys)) {
                String[] ids = commoditys.split(",");
                for (String id : ids) {
                    commodityDao.deleteB2BPoolCommodityById(id); // 删除商品
                }
            }
            b2bPoolPeriodsStoreDao.deleteYxPoolPeriodsStore(pro.getId());
        }        
        cppDao.deleteYxPoolPeriods(periodId);
        
        //删除online 的期数下的所有商品
        yppOnlineService.deletePeriodsAll(periodId);
    }  
    
    /**
     * 删除当前期的所有商品
     * 
     * @param periodId
     * @param auth
     * @author lizhiyong
     */
    public void deletePeriodAllCommodity(Long periodId) {
        YxPoolPeriodsStore yxPoolPeriodsStore = new YxPoolPeriodsStore();
        yxPoolPeriodsStore.setPeriodId(periodId);
        yxPoolPeriodsStore.setPageRows(1000);
        // 获得期数下所有省份与商品的关系
        List<YxPoolPeriodsStore> yxPoolPeriodsStoreList = b2bPoolPeriodsStoreDao.queryStoreList(yxPoolPeriodsStore);
        for (YxPoolPeriodsStore pro : yxPoolPeriodsStoreList) {
            String commoditys = pro.getCommoditys();
            if (StringUtils.isNotBlank(commoditys)) {
                String[] ids = commoditys.split(",");
                for (String id : ids) {
                    commodityDao.deleteB2BPoolCommodityById(id); // 删除商品
                }
                pro.setCommoditys("");
                b2bPoolPeriodsStoreDao.updateCommoditys(pro); // 更新商品省份的关联
            }
        }
    }

    /**
     * @param orders  
     * @Description 删除池期数与商品的关联,2015-5-12修改同步删除所有省份关联 by zdy
     * @param commodityId
     *            商品ID
     * @param periodId
     *            池期数ID
     */
    public void deletePeriodsAllProinceCommodity(String commodityId, Long periodId, Integer orders, String province) {
        List<YxPoolPeriodsStore> periodsProvinceAllList = b2bPoolPeriodsStoreService.getPoolPeriodsStoresByPeriodsId(periodId, null);
        String[] boolArray = new String[periodsProvinceAllList.size()];
        for (int i = 0; i < periodsProvinceAllList.size(); i++) {
        	 YxPoolPeriodsStore temp = periodsProvinceAllList.get(i);
            if (temp.getCommoditys().contains(commodityId)) {
                boolArray[i] = "true";
            } else {
                boolArray[i] = "false";
            }
        	YxPoolPeriodsStore peroidProvince = periodsProvinceAllList.get(i);
            String nowCommoditys = peroidProvince.getCommoditys();
            String newIds = ("," + nowCommoditys + ",").replace("," + commodityId + ",", ",");// 替换ID删除关联
    		if(newIds!=null && newIds.startsWith(",")){
    			newIds = newIds.substring(1, newIds.length());
    		}
    		if(newIds!=null && newIds.endsWith(",")){
    			newIds = newIds.substring(0, newIds.length()-1);
    		}
            peroidProvince.setCommoditys(newIds);
            peroidProvince.setUpdateId("system");
            peroidProvince.setUpdateTime(new Date());
            b2bPoolPeriodsStoreDao.updateCommoditys(peroidProvince);
        }
        
        Arrays.sort(boolArray);
        if (Arrays.binarySearch(boolArray, "true") < 0) {
            commodityDao.deleteB2BPoolCommodityById(commodityId);
        }
    }
    /**
     * @Description 查询池各期列表
     * @param cpp
     * @return
     */
    public List<YxPoolPeriods> queryPeriodsList(YxPoolPeriods cpp) {
        List<YxPoolPeriods> cppList = cppDao.queryPeriodsList(cpp);
        // 更新状态
        /*for (YxPoolPeriods c : cppList) {
            Date d = new Date();
            if (c.getBeginTime().before(d) && c.getEndTime().after(d)) {
                c.setStatus(1);
            } else if (c.getEndTime().before(d)) {
                c.setStatus(0);
            } else {
                c.setStatus(2);
            }
        }*/
        Date d = new Date();;
        for(int i=0; i<cppList.size();i++){
        	YxPoolPeriods nowPP = cppList.get(i);
        	Calendar cl = Calendar.getInstance();
        	cl.setTime(nowPP.getBeginTime());
        	cl.set(Calendar.SECOND, 0);
        	nowPP.setBeginTime(cl.getTime());
        	if(cppList.size()==1){//只有一期时
        		nowPP.setStatus(1);
        		cppList.set(i, nowPP);
        		break;
        	}
        	
        	if(cppList.size()>0 && i<(cppList.size()-1)){//存在多期时
        		YxPoolPeriods nextPP = cppList.get(i+1);
        		if(nowPP.getBeginTime().after(d) && nextPP.getBeginTime().before(d)){//中间
        			nowPP.setStatus(2);
	        		cppList.set(i,nowPP);
	        		nextPP.setStatus(1);
	        		cppList.set(i+1,nextPP);
        		}else if(nowPP.getBeginTime().after(d)){//所有期数未到,取后一期
        			nowPP.setStatus(2);
	        		cppList.set(i,nowPP);
	        		nextPP.setStatus(1);
	        		cppList.set(i+1,nextPP);
        		}else if(nowPP.getBeginTime().before(d)){//所有期数时间已过，取最新一期
        			if(i==0){
	    				nowPP.setStatus(1);
	            		cppList.set(i,nowPP);
	    			}else{
	    				if(cppList.get(i-1).getBeginTime().before(d)){
	    					nowPP.setStatus(0);
		            		cppList.set(i,nowPP);
	    				}
	    			}
	        		nextPP.setStatus(0);
	        		cppList.set(i+1,nextPP);
        		}
        	}
        }
        
        return cppList;
    }

    /**
     * @Description 更新池某一期信息
     * @param cmsPoolPeriod
     * @param auth
     * @return
     */
    public int updatePoolPeriod(YxPoolPeriods cmsPoolPeriod) {
        cmsPoolPeriod.setUpdateId("system");
        cmsPoolPeriod.setUpdateTime(new Date());
        int i = cppDao.updateCMSPoolPeriods(cmsPoolPeriod);
        return i;
    }

    /**
     * @Description 保存池某一期信息
     * @param cmsPoolPeriod
     * @param request
     * @return
     */
    public Long savePoolPeriod(YxPoolPeriods cmsPoolPeriod) {
        Date now = new Date();
        cmsPoolPeriod.setNumber(1);
        Long id = cppDao.insertYxPoolPeriods(cmsPoolPeriod);
        
        YxPoolPeriodsStore pro = null;
        List<B2BStore> storeList = storeService.getB2BStoreList();
        for (B2BStore s : storeList) {
            pro = new YxPoolPeriodsStore();
            pro.setPeriodId(id);
            pro.setStoreCode(s.getCode()); // 获取区域下的所有省份
            pro.setCreateId(cmsPoolPeriod.getCreateId());
            pro.setCreateTime(now);
            b2bPoolPeriodsStoreDao.insertYxPoolPeriodsStore(pro);
        }
        return id;
    }
    
    public void resetOrder(Long periodId, String storeCode,String commondityId, int order) {
    	YxPoolPeriodsStore cpp = b2bPoolPeriodsStoreDao.queryYxPoolPeriodsStoreByCode(periodId, storeCode);
    	String idStr = cpp.getCommoditys();
    	if(idStr == null) return;
    	String[] id_array = idStr.split(",");
    	int index = -1; //商品的位置
    	for(int i = 0; i < id_array.length; i++) {
    		String id = id_array[i];
    		if (id.equals(commondityId)) {
    			index = i;
    			break;
    		}
    	}
    	//商品不存在，退出
    	if(index < 0) return;
    	int new_index = index + order;
    	if(new_index > id_array.length)return;
    	String temp = id_array[new_index];
    	id_array[new_index] = commondityId;
    	id_array[index] = temp;
    	String updateId = UserUtil.getUserId();
    	cpp.setCommoditys(StringUtils.join(id_array, ","));
        cpp.setUpdateTime(new Date());
        cpp.setUpdateId(updateId);
        b2bPoolPeriodsStoreDao.updateCommoditys(cpp);
    }

    /**
     * @Description 验证商品池各期数的时间是否有重叠
     * @param cmsPoolPeriod
     * @return 0：结束时间小于开始时间 1：正常 -1：有重叠
     */
    public int checkPeriodDate(YxPoolPeriods cmsPoolPeriod) {
        YxPoolPeriods cpp = new YxPoolPeriods();
        cpp.setPoolId(cmsPoolPeriod.getPoolId());
        /*if (cmsPoolPeriod.getBeginTime().after(cmsPoolPeriod.getEndTime()) || cmsPoolPeriod.getBeginTime().getTime() == cmsPoolPeriod.getEndTime().getTime()) {
            return 0;// 结束时间小于开始时间，返回0;
        }*/
        cpp.setPageRows(Integer.MAX_VALUE);
        List<YxPoolPeriods> cppList = cppDao.queryPeriodsList(cpp);
        for (YxPoolPeriods c : cppList) {
            if (cmsPoolPeriod.getId() == null || (long) cmsPoolPeriod.getId() != (long) c.getId()) {// 排除本身与新增时ID为null
                // 判断时间段是否有重合
                if (c.getBeginTime().equals(cmsPoolPeriod.getBeginTime()))// 都在前面
                {
                	return -1;
                }
            }
        }
        return 1;
    }

    /***
     * 批量删除商品
     * 2015-5-12修改同步删除所有省份关联 by zdy
     * @param ids
     */
    public void deleteButchCommodity(String[] ids, Long periodId, String province) {
        List<YxPoolPeriodsStore> periodsProvinceAllList = b2bPoolPeriodsStoreService.getPoolPeriodsStoresByPeriodsId(periodId, null);
       
        for (int i = 0; i < periodsProvinceAllList.size(); i++) {
        	YxPoolPeriodsStore peroidProvince = periodsProvinceAllList.get(i);
            String nowCommoditys = "," + peroidProvince.getCommoditys() + ",";
            String newIds = "";
            for (String commodityId : ids) {
                newIds = (nowCommoditys).replace("," + commodityId + ",", ",");// 替换ID删除关联
                nowCommoditys = newIds;
            }
            peroidProvince.setCommoditys(newIds);
            peroidProvince.setUpdateId("system");
            peroidProvince.setUpdateTime(new Date());
            b2bPoolPeriodsStoreDao.updateCommoditys(peroidProvince);
        }

        String[] boolArray = new String[periodsProvinceAllList.size()];

        for (int j = 0; j < ids.length; j++) {
            for (int i = 0; i < periodsProvinceAllList.size(); i++) {
                YxPoolPeriodsStore temp = periodsProvinceAllList.get(i);
                if (temp.getCommoditys().contains(ids[j])) {
                    boolArray[i] = "true";
                } else {
                    boolArray[i] = "false";
                }
            }
            Arrays.sort(boolArray);
            if (Arrays.binarySearch(boolArray, "true") < 0) {
                if (ids[j] != null) {
                    B2BPoolCommodity c = commodityDao.queryB2BPoolCommodityByID(Long.valueOf(ids[j]));
                    if (c != null) {
                        commodityDao.deleteB2BPoolCommodityById(ids[j]);
                    }
                }
            }
        }

    }
    
    
    /**
     * @Description 对列表数据进行分页处理并显示序号
     * @param list
     * @param poolCommodity
     * @return
     */
    public List<B2BPoolCommodity> getPageData(List<B2BPoolCommodity> list, B2BPoolCommodity poolCommodity) {
    	boolean isOriginate = poolCommodity.getOriginate()!=null 
    			&& poolCommodity.getOriginate().intValue()>=0;
    	List<B2BPoolCommodity> originateList = new ArrayList<B2BPoolCommodity>();
    	if(isOriginate){  //类型查询
    		for(int j = 0; j < list.size(); j++){
    			B2BPoolCommodity obj = list.get(j);
    			B2BPoolCommodity cc = (B2BPoolCommodity) obj;
    			if(cc.getOriginate().intValue()==poolCommodity.getOriginate().intValue()){
        			cc.setOrder(j + 1); 
        			originateList.add(cc);            		
        		}
        	}
    	}else{
    		originateList = list;
    	}
    	
        List<B2BPoolCommodity> queryList = new ArrayList<B2BPoolCommodity>();
        for (int i = 0; i < originateList.size(); i++) {// 条件查询
        	B2BPoolCommodity obj = originateList.get(i);
        	B2BPoolCommodity cc = (B2BPoolCommodity) obj;
        	if (StringUtils.isNotBlank(poolCommodity.getTitle())) {// 名称查询
                if (cc.getTitle().indexOf(poolCommodity.getTitle()) != -1) {
                    cc.setOrder(i + 1);
                    queryList.add(cc);
                }
            }else if (StringUtils.isNotBlank(poolCommodity.getCommodityId())){// ID查询
                if (cc.getCommodityId().equals(poolCommodity.getCommodityId())) {
                    cc.setOrder(i + 1);
                    queryList.add(cc);
                }
            } else {
                cc.setOrder(i + 1);
                queryList.add(cc);
            }
        }
        
        
        poolCommodity.setTotalRows(queryList.size());
        List<B2BPoolCommodity> newList = new ArrayList<B2BPoolCommodity>();
        for (int i = 0; i < queryList.size(); i++) {
            if (poolCommodity.getTotalRows() < (poolCommodity.getPageRows() * (poolCommodity.getCurPage() - 1))) {
                poolCommodity.setCurPage(1);
            }
            if ((i + 1) > (poolCommodity.getPageRows() * (poolCommodity.getCurPage() - 1))
                    && (i + 1) <= (poolCommodity.getPageRows() * poolCommodity.getCurPage())) {
            	B2BPoolCommodity obj = queryList.get(i);
                B2BPoolCommodity cc = (B2BPoolCommodity) obj;
                // cc.setOrder(i+1);
                newList.add(cc);
            }
        }

        return newList;
    }
    
    @Autowired
    private B2BStoreDao storeDao;

	/**
	 * 同步门店
	 */
	public void synchStoreData(String poolId) {
		if (StringUtils.isBlank(poolId)) {
			return;
		}
		YxPool pool = poolService.queryPoolById(Long.parseLong(poolId));
		if (null == pool) {
			return;
		}
		List<YxPoolPeriods> list = pool.getYppList();
		for (YxPoolPeriods pps : list) {
			if (pps.getStatus() == 0) {
				continue;
			}
			Long id = pps.getId();
			synchSinglePeriodStoreDate(id);
		}
	}
    
    
    
    
    private void synchSinglePeriodStoreDate(Long periodId){
    	List<YxPoolPeriodsStore> list = b2bPoolPeriodsStoreDao.queryStoreListByPeriodsId(periodId);
    	Map<String, String> poolStroreMap = new HashMap<String, String>();
    	for (YxPoolPeriodsStore ps : list) {
    		poolStroreMap.put(ps.getStoreCode(), ps.getStoreCode());
    	}
    	
    	List<B2BStore> allStore = storeDao.getStoreList();
    	for (B2BStore s : allStore) {
    		String code = poolStroreMap.get(s.getCode());
    		if (code != null) {
    			continue;
    		}
    		YxPoolPeriodsStore cMSUnitePeriodsProvince = new YxPoolPeriodsStore();
    		cMSUnitePeriodsProvince.setPeriodId(periodId);
    		cMSUnitePeriodsProvince.setCreateId("system");
    		cMSUnitePeriodsProvince.setStoreCode(s.getCode()); // 获取区域下的所有省份
    		cMSUnitePeriodsProvince.setCreateTime(new Date());
    		b2bPoolPeriodsStoreDao.insertYxPoolPeriodsStore(cMSUnitePeriodsProvince);
    	}
    }
    
    
    
    /**
     * 查询池中所有数据,并进行初始化
     * @author lizhiyong
     * 2017年3月13日
     * @param pool
     * @param ppp
     * @param isShow
     * @return
     */
    public List<B2BPoolCommodity> getB2BPoolCommodityList(Long poolId,String code,boolean isShow) {
    	YxPool pool = poolService.queryPoolAndPeriodById(poolId);
    	YxPoolPeriodsStore ppp = b2bPoolPeriodsStoreDao.queryYxPoolPeriodsStoreByCode(pool.getYxPoolPeriods().getId(), code);
    	if(ppp==null)return null;
    	return getB2BPoolCommodityList(pool,ppp,isShow);
    }
    
    /**
     * 查询池中所有数据,并进行初始化
     * @author lizhiyong
     * 2016年5月25日
     * @param pool
     * @param ppp
     * @param isShow
     * @return
     */
    public List<B2BPoolCommodity> getB2BPoolCommodityList(YxPool pool,String code,boolean isShow) {
    	YxPoolPeriodsStore ppp = b2bPoolPeriodsStoreDao.queryYxPoolPeriodsStoreByCode(pool.getYxPoolPeriods().getId(), code);
    	return getB2BPoolCommodityList(pool,ppp,isShow);
    }
    
    @SuppressWarnings("unchecked")
	public List<B2BPoolCommodity> getB2BPoolCommodityList(YxPool pool,YxPoolPeriodsStore ppp,boolean isShow) {
    	List<B2BPoolCommodity> list = commodityDao.queryB2BPoolCommodityByIds(ppp.getCommoditys());
    	String seq = ppp.getStoreCode();
    	if(list == null || list.size() == 0){
    		list = new ArrayList<B2BPoolCommodity>();
    		if(StringUtils.isNotBlank(ppp.getCommoditys())){
        		updateB2BPoolPeriodsStoreCommodities(ppp,list);
        	}
    		return list;
    	}
    	//是否显示下架商品
    	List<B2BPoolCommodity> nolist = null;
    	if(isShow) nolist = new ArrayList<B2BPoolCommodity>();
    	StringBuilder commodityIds = new StringBuilder(); 									//商城商品id
    	List<B2BPoolCommodity> commoditylist = new ArrayList<B2BPoolCommodity>();	//商城商品列表
    	List<String> clist = new ArrayList<String>();		//所有商品列表，用于还原顺序
    	Map<String,B2BPoolCommodity> resultMap = new HashMap<String,B2BPoolCommodity>();		//所有接口返回并且可用商品
    	List<B2BPoolCommodity> alllist = new ArrayList<B2BPoolCommodity>();
    	for(int i = 0; i < list.size(); i++) {
    		B2BPoolCommodity cc = list.get(i);
			int originate = cc.getOriginate().intValue();
    		if(originate == YxPoolConst.YX_COMMODITY_TYPE_PIC || originate == YxPoolConst.YX_COMMODITY_TYPE_TEXT ){
    			//后台不再处理链接跳转地址，至前台判断
    			appPicCheck(cc,seq);
    		}else{
    			commodityIds.append(cc.getCommodityId()).append(",");
    			commoditylist.add(cc);
    	    	clist.add(cc.getId().toString());
    		}
    	}
    	
    	//自营可用商品
    	if(StringUtils.isNotBlank(commodityIds.toString())){
    		resetSelfCommodityFromSOA(seq,commodityIds,commoditylist,resultMap, nolist);
    	}
    	//获取所有可用商品
    	setAlllist(clist,resultMap,alllist);
    	
    	//款色码容错
    	/*if(pool.getIsReplace().intValue() == 1) {
    		styleFault(alllist,seq);
    	}*/
    	
    	//按照价格排序
    	if(pool.getOrderType().intValue() == 2) {
    		B2BPoolCommodityComparator comparator = new B2BPoolCommodityComparator();
			comparator.setOrderOrientation(pool.getOrderRule());
    		Collections.sort(alllist, comparator);
    		comparator = null;
    	}

    	//库存容错，库存为0的放到最后
    	if(pool.getStockFt().intValue() == 1) {
    		B2BPoolCommodityComparator comparator = new B2BPoolCommodityComparator();
			comparator.setOrderType(3);
    		Collections.sort(alllist, comparator);
    		comparator = null;
    	}
 
    	
    	//处理返回的结果集
    	List<B2BPoolCommodity> result = initResultList(list, nolist, alllist);
    	if(isShow){
    		updateB2BPoolPeriodsStoreCommodities(ppp,result);
    	}
    	
    	alllist = null;
    	nolist = null;
    	return result;
    }
    
    /**
     * 收集所有可用商品，并且保存原有顺序
     * @author lizhiyong
     * 2016年6月2日
     * @param clist
     * @param resultMap
     * @param alllist
     */
    private void setAlllist(List<String> clist,Map<String,B2BPoolCommodity> resultMap,List<B2BPoolCommodity> alllist) {
    	for(String id : clist) {
    		B2BPoolCommodity c = resultMap.get(id);
    		if(c != null) {
    			alllist.add(c);
    		}
    	}
    }
    
    /**
     * 无线图片处理
     */
    private void appPicCheck(B2BPoolCommodity cc, String seq) {
    	//图片属性
    	String urlProperties = cc.getUrlProperties();
    	
    	//图片类型
    	String urlType = cc.getUrlType();
    	if(cc.getUrlType() == null) {
    		cc.setCustomUrl(urlProperties);	
    	}else {
    		try{
    			if("firstlevellist".equals(urlType)){
        			JSONObject urljo = JSONObject.parseObject(urlProperties);
        			String cate = null;
        			String pgSeq  = storeService.getStoreSeqByCode(seq);
        			if(urljo.containsKey(pgSeq)){//新数据按区域
        				cate = urljo.getString(pgSeq);
        			}
        			cate  = cate == null ? urlProperties : cate;
        			cc.setCustomUrl(cate);
        		}else {
        			cc.setCustomUrl(urlProperties);	
        		}
    		}catch (Exception e){
    			logger.error("无线链接转换出错！");
    		}
    		
    	}
    }

    private static final String ITEM_HOST = SystemEnv.getProperty("feiniu.itemHost");
//    private static final String TUAN_ITEM_HOST = SystemEnv.getProperty("feiniu.tuan.itemHost");
//    private static final String CATEGORY_URL = SystemEnv.getProperty("feiniu.category_url");
    
    
  
    /**
     * 自营商品处理
     * 1、过滤掉积分商品，下架商品（接口未返回的）
     * 2、返回所有可用商品
     * @author lizhiyong
     * 2016年5月24日
     * @param seq
     */
    private void resetSelfCommodityFromSOA(String seq,StringBuilder selfids, List<B2BPoolCommodity> selflist,Map<String,B2BPoolCommodity> resultMap, List<B2BPoolCommodity> nolist) {
		String idString =selfids.toString();
		String[] ids=idString.split(",");
    	Map<String,B2BPoolCommodity> pcMap = b2bRemoteCommodityServiceImpl.getRemoteCommodityListByStoreAndIds(seq,ids);
    	 if(pcMap == null) {
    		 pcMap = new HashMap<String,B2BPoolCommodity>();
 		}
    	 for(B2BPoolCommodity cc : selflist) {
    		 B2BPoolCommodity cpc = pcMap.get(cc.getCommodityId());
    		 if (cpc != null) {
    			 //报名商品
				 if((long)cc.getClassId() == 1 ){
					 cpc.setClassId(1);
				 }
				// cpc.setTitle(cc.getTitle()); // 商品名称
				// cpc.setPicUrl(cc.getPicUrl());
				 cpc.setId(cc.getId());
				 cpc.setOriginate(cc.getOriginate());
				 cpc.setSellPoint(cc.getSellPoint());
				 cpc.setUpdateId(cc.getUpdateId());
				 cpc.setUpdateTime(cc.getUpdateTime());
				 cpc.setPicTurnUrl(cc.getPicTurnUrl());
				 cpc.setUrlProperties(cc.getUrlProperties());
				 cpc.setDescription(cc.getDescription());
				 cpc.setGroupIds(cc.getGroupIds());
				 //自定义促销语
				 if(StringUtils.isNotBlank(cc.getPromoteText())){
					 cpc.setPromoteText(cc.getPromoteText());
				 }
				 if (cc.getUrlType() != null && cc.getUrlType().equals("1")) {
		        	 cpc.setCustomUrl(ITEM_HOST+cpc.getCommodityId());
		         }
    			 resultMap.put(cpc.getId().toString(), cpc);
    		 }else {
				if(nolist != null) {
					cc.setExist(false);
					nolist.add(cc);
				}
			}
    	 }
    }
    
    /**
     * 款式容错,通过soa接口查询可以销售的款式商品
     */
    public void styleFault(List<B2BPoolCommodity> alllist,String seq) {
    	Map<String,B2BPoolCommodity> m = new HashMap<String,B2BPoolCommodity>();
    	for(B2BPoolCommodity c : alllist) {
    		m.put(c.getCommodityId(), c);
    	}
    	
    	for(int i = 0; i < alllist.size(); i++) {
    		B2BPoolCommodity upc = alllist.get(i);
    		//判断库存，库存为0才容错
    		if(upc.getStockSum() > 0)continue;
    		
    		//款式商品集合
    		List<B2BPoolCommodity> stylelist = null;

	    	stylelist = new ArrayList<B2BPoolCommodity>();//cMSCommodityInfoService.getGroupItemBySEQFromPM(upc.getCommodityId(),seq);
	    		
    		
    		if(stylelist == null || stylelist.size() == 0) continue;
    		
    		for(B2BPoolCommodity sc : stylelist){
    			//判断是否与已有商品冲突
    			if(m.get(sc.getCommodityId()) != null) {
    				continue;
    			}
    			//容错，找一个可以卖的
    			if(sc.getStockSum()>0 && sc.isExist()) {
    				sc.setId(upc.getId());
    				if((long)upc.getClassId()==1){
    					sc.setTitle(upc.getTitle()); // 商品名称
    					sc.setPicUrl(upc.getPicUrl());
    					sc.setClassId(1);
	   				}
    				sc.setOriginate(upc.getOriginate());
    				sc.setUpdateTime(new Date());
    				sc.setUpdateId("system");
    				commodityDao.updateB2BPoolCommodity(sc);//更新替换
    				alllist.set(i, sc);
    				break;
    			}
    		}
    	}
    }
    
    /**
     * 处理返回结果的列表
     * @author lizhiyong
     * 2016年5月25日
     * @param list
     */
    private List<B2BPoolCommodity> initResultList(List<B2BPoolCommodity> list,List<B2BPoolCommodity> nolist,List<B2BPoolCommodity> alllist) {
    	List<B2BPoolCommodity> result = new ArrayList<B2BPoolCommodity>();
    	int i = 0; 
    	for(B2BPoolCommodity c: list) {
    		int originate = c.getOriginate().intValue();
    		if(originate == YxPoolConst.YX_COMMODITY_TYPE_COMMODITY ) {
    			if(i < alllist.size()) {
    				c = alllist.get(i);
    				result.add(c);
    			}
    			i++;
    		}else {
    			result.add(c);
    		}
    		
    	}
    	if(nolist != null) {
    		result.addAll(nolist);
    	}
    	return result;
    }
    
    
    /**
     * 更新商品顺序，解决排序问题
     * @author lizhiyong
     * 2016年6月20日
     */
    private void updateB2BPoolPeriodsStoreCommodities(YxPoolPeriodsStore ppp,List<B2BPoolCommodity> result) {
    	StringBuilder cs = new StringBuilder();
    	int i = 0;
    	for(B2BPoolCommodity c : result) {
    		if(i > 0) {
    			cs.append(",");
    		}
    		cs.append(c.getId().toString());
    		i++;
    	}
    	ppp.setCommoditys(cs.toString());
    	ppp.setUpdateTime(new Date());
    	ppp.setUpdateId("sys");
        b2bPoolPeriodsStoreDao.updateCommoditys(ppp);
    }
}
