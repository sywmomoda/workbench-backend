package com.feiniu.yx.pool.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.yx.common.entity.ReturnT;
import com.feiniu.yx.pool.dao.YxPoolCommodityDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreDao;
import com.feiniu.yx.pool.dao.YxPoolProperPlusDao;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.pool.entity.YxPoolProperPlus;
import com.feiniu.yx.pool.service.YxPoolPeriodsService;
import com.feiniu.yx.pool.service.YxPoolService;
import com.feiniu.yx.store.dao.YXStoreDao;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.service.YXStoreService;

@Service
public class YxPoolPeriodsServiceImpl implements YxPoolPeriodsService{

    private static Logger                logger = Logger.getLogger(YxPoolPeriodsServiceImpl.class);

    @Autowired
    private YxPoolPeriodsDao         cppDao;

    @Autowired
    private YxPoolCommodityDao       commodityDao;

    @Autowired
    private YxPoolPeriodsStoreDao         yxPoolPeriodsStoreDao;

    @Autowired
    private YxPoolPeriodsStoreServiceImpl  yxPoolPeriodsStoreService;

    @Autowired
    private YXStoreService  storeService;

    @Autowired
    private YxPoolService  poolService;
    
    @Autowired
	private YxPoolProperPlusDao properPlusDao;

    @Autowired
    private YxPoolPeriodsOnlineServiceImpl  yppOnlineService;

    public void deletePeriodAll(Long periodId){
        YxPoolPeriodsStore yxPoolPeriodsStore = new YxPoolPeriodsStore();
        yxPoolPeriodsStore.setPeriodId(periodId);
        yxPoolPeriodsStore.setPageRows(1000);
        // 获得期数下所有省份与商品的关系
        List<YxPoolPeriodsStore> yxPoolPeriodsStoreList = yxPoolPeriodsStoreService.queryStoreList(yxPoolPeriodsStore);
        for (YxPoolPeriodsStore pro : yxPoolPeriodsStoreList) {
            String commoditys = pro.getCommoditys();
            if (StringUtils.isNotBlank(commoditys)) {
                String[] ids = commoditys.split(",");
                for (String id : ids) {
                    commodityDao.deleteYxPoolCommodityById(id); // 删除商品
                }
            }
            yxPoolPeriodsStoreDao.deleteYxPoolPeriodsStore(pro.getId());
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
        List<YxPoolPeriodsStore> yxPoolPeriodsStoreList = yxPoolPeriodsStoreDao.queryStoreList(yxPoolPeriodsStore);
        for (YxPoolPeriodsStore pro : yxPoolPeriodsStoreList) {
            String commoditys = pro.getCommoditys();
            if (StringUtils.isNotBlank(commoditys)) {
                String[] ids = commoditys.split(",");
                for (String id : ids) {
                    commodityDao.deleteYxPoolCommodityById(id); // 删除商品
                }
                pro.setCommoditys("");
                yxPoolPeriodsStoreDao.updateCommoditys(pro); // 更新商品省份的关联
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
        List<YxPoolPeriodsStore> periodsProvinceAllList = yxPoolPeriodsStoreService.getPoolPeriodsStoresByPeriodsId(periodId, null);
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
            yxPoolPeriodsStoreDao.updateCommoditys(peroidProvince);
        }

        Arrays.sort(boolArray);
        if (Arrays.binarySearch(boolArray, "true") < 0) {
            commodityDao.deleteYxPoolCommodityById(commodityId);
        }
    }
    /**
     * @Description 查询池各期列表
     * @param cpp
     * @return
     */
    public List<YxPoolPeriods> queryPeriodsList(YxPoolPeriods cpp) {
        List<YxPoolPeriods> cppList = cppDao.queryPeriodsList(cpp);
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
        List<YXStore> storeList = storeService.getYXStoreList();
        for (YXStore s : storeList) {
            pro = new YxPoolPeriodsStore();
            pro.setPeriodId(id);
            pro.setStoreCode(s.getCode()); // 获取区域下的所有省份
            pro.setCreateId(cmsPoolPeriod.getCreateId());
            pro.setCreateTime(now);
            yxPoolPeriodsStoreDao.insertYxPoolPeriodsStore(pro);
        }
        return id;
    }

    /**
     * @Description: 重新设置池中商品的排序
     * @param periodId
     *            池期数相关参数
     * @param commodityId
     *            商品ID
     * @param oldOrder
     *            原排序位置
     * @param newOrder
     *            新排序位置
     */
    public ReturnT<String> resetOrders(YxPoolCommodity obj, String commodityId, Integer oldOrder, Integer newOrder) {
        YxPoolPeriodsStore cpp = yxPoolPeriodsStoreDao.queryYxPoolPeriodsStoreByCode(obj.getPeriods(), obj.getStoreCode());
        if (cpp == null) {
            return new ReturnT<String>(0, "期数不存在");
        }
        //商品id
        String cids = cpp.getCommoditys();
        if (StringUtils.isBlank(cids)) {
            return new ReturnT<String>(0, "商品为空");
        }

        String ids[] = cpp.getCommoditys().split(",");

        if (oldOrder == null || newOrder == null) {
            return new ReturnT<String>(0, "位置数字不正确");
        }

        //判断位置是否超过数组长度
        if (oldOrder.intValue() > ids.length || newOrder.intValue() > ids.length || newOrder.intValue() == 0 || oldOrder.intValue() == 0) {
            return new ReturnT<String>(0, "位置数字不正确");
        }
        //位置没移动，直接返回
        if ((oldOrder - newOrder) == 0) {
            return new ReturnT<String>(0, "位置一样");
        }
        String cid = ids[oldOrder - 1];
        //原位置商品发生变动，直接返回
        if (!cid.equals(commodityId)) {
            return new ReturnT<String>(0, "原位置商品发生变动");
        }
        cid = ids[newOrder - 1];
        //位置已经移动，返回
        if (cid.equals(commodityId)) {
            return new ReturnT<String>(0, "位置已经移动");
        }

        StringBuilder commoditys = new StringBuilder();
        for (int i = 0; i < ids.length; i++) {
            String s = ids[i];
            if (i != 0) {
                commoditys.append(",");
            }
            if ((oldOrder - 1) == i) {
                commoditys.append(cid);
                continue;
            }
            if ((newOrder - 1) == i) {
                commoditys.append(commodityId);
                continue;
            }
            commoditys.append(s);
        }
        cpp.setCommoditys(commoditys.toString());
        cpp.setUpdateTime(new Date());
        cpp.setUpdateId("system");
        yxPoolPeriodsStoreDao.updateYxPoolPeriodsStoreCommoditys(cpp);
        return new ReturnT<String>(1, "成功");
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


    public int checkCopyPeriodDate(List<YxPoolPeriods> yxPoolPeriodss,List<YxPoolPeriods> periodListQquery,String repeatTimes) {
        if(yxPoolPeriodss.size()==1){
            YxPoolPeriods yxPoolPeriods = yxPoolPeriodss.get(0);
            YxPoolPeriods yx = cppDao.findPeriodsByOldId(yxPoolPeriods.getId());
            if(yxPoolPeriods.getBeginTime().getTime()<=yx.getBeginTime().getTime()){
                return -3;//开始时间必须大于复制期数的开始时间
            }
            for (YxPoolPeriods poolPeriods : periodListQquery) {
                if(yxPoolPeriods.getName().equals(poolPeriods.getName())){
                    return -5;//输入的name和数据库name重复
                }
                if(yxPoolPeriods.getBeginTime().getTime()==poolPeriods.getBeginTime().getTime()){
                    return -6;//添加的时间和其他期数的时间段重读
                }
            }
            return 1;
        }else {
            if(Long.parseLong(repeatTimes)==1){
                for (int i = 0; i < yxPoolPeriodss.size(); i++) {
                    for (int j = i+1; j < yxPoolPeriodss.size(); j++) {
                        if(yxPoolPeriodss.get(i).getName().equals(yxPoolPeriodss.get(j).getName())){
                            return -4;//输入的期数重复
                        }
                    }
                }
            }

            for (YxPoolPeriods poolPeriodss : yxPoolPeriodss) {
                for (YxPoolPeriods yxPoolPeriods : periodListQquery) {
                    if(poolPeriodss.getName().equals(yxPoolPeriods.getName())){
                        return -5;//输入的name和数据库name重复
                    }
                }
            }
            YxPoolPeriods yx = cppDao.findPeriodsByOldId(yxPoolPeriodss.get(0).getId());
            for (int i = 0; i < yxPoolPeriodss.size(); i++) {
                if(yxPoolPeriodss.get(i).getBeginTime().getTime()<=yx.getBeginTime().getTime()){
                    return -3;//开始时间必须大于复制期数的开始时间
                }
            }
            for (YxPoolPeriods poolPeriodss : yxPoolPeriodss) {
                for (YxPoolPeriods yxPoolPeriods : periodListQquery) {
                    if(poolPeriodss.getBeginTime().getTime()==yxPoolPeriods.getBeginTime().getTime()){
                        return -6;//输入期数的间隔时间与数据库时间段相同
                    }
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
        List<YxPoolPeriodsStore> periodsProvinceAllList = yxPoolPeriodsStoreService.getPoolPeriodsStoresByPeriodsId(periodId, null);

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
            yxPoolPeriodsStoreDao.updateCommoditys(peroidProvince);
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
                    YxPoolCommodity c = commodityDao.queryYxPoolCommodityByID(Long.valueOf(ids[j]));
                    if (c != null) {
                        commodityDao.deleteYxPoolCommodityById(ids[j]);
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
    public List<YxPoolCommodity> getPageData(List<YxPoolCommodity> list, YxPoolCommodity poolCommodity) {

        boolean isOriginate = poolCommodity.getOriginate()!=null
                && poolCommodity.getOriginate().intValue()>=0;
        List<YxPoolCommodity> originateList = new ArrayList<YxPoolCommodity>();
        if(isOriginate){  //类型查询
            for(int j = 0; j < list.size(); j++){
                YxPoolCommodity obj = list.get(j);
                YxPoolCommodity cc = (YxPoolCommodity) obj;
                if(cc.getOriginate().intValue()==poolCommodity.getOriginate().intValue()){
                    cc.setOrder(j + 1);
                    originateList.add(cc);
                }
            }
        }else{
            originateList = list;
        }

        List<YxPoolCommodity> queryList = new ArrayList<YxPoolCommodity>();
        for (int i = 0; i < originateList.size(); i++) {// 条件查询
            YxPoolCommodity obj = originateList.get(i);
            YxPoolCommodity cc = (YxPoolCommodity) obj;
            if (StringUtils.isNotBlank(poolCommodity.getTitle())) {// 名称查询
                if (cc.getTitle().indexOf(poolCommodity.getTitle()) != -1) {
                    cc.setOrder(i + 1);
                    queryList.add(cc);
                }
            } else if (StringUtils.isNotBlank(poolCommodity.getCommodityId()))// ID查询
            {
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
        List<YxPoolCommodity> newList = new ArrayList<YxPoolCommodity>();
        for (int i = 0; i < queryList.size(); i++) {
            if (poolCommodity.getTotalRows() < (poolCommodity.getPageRows() * (poolCommodity.getCurPage() - 1))) {
                poolCommodity.setCurPage(1);
            }
            if ((i + 1) > (poolCommodity.getPageRows() * (poolCommodity.getCurPage() - 1))
                    && (i + 1) <= (poolCommodity.getPageRows() * poolCommodity.getCurPage())) {
                YxPoolCommodity obj = queryList.get(i);
                YxPoolCommodity cc = (YxPoolCommodity) obj;
                // cc.setOrder(i+1);
                newList.add(cc);
            }
        }

        return newList;
    }

    @Autowired
    private YXStoreDao yxStoreDao;

    /**
     * 同步门店
     */
    public void synchStoreData(String poolId) {
        if(StringUtils.isBlank(poolId)){
            return;
        }
        YxPool pool = poolService.queryPoolById(Long.parseLong(poolId));
        if(null == pool){
            return;
        }
        List<YxPoolPeriods> list = pool.getYppList();
        for(YxPoolPeriods pps : list){
            if(pps.getStatus() == 0){ //过期的档期不同步
                continue;
            }
            Long id = pps.getId();
            synchSinglePeriodStoreDate(id);
        }

    }

    private void synchSinglePeriodStoreDate(Long periodId){
        List<YxPoolPeriodsStore> list = yxPoolPeriodsStoreDao.queryStoreListByPeriodsId(periodId);
        Map<String, String> poolStroreMap = new HashMap<String, String>();
        for (YxPoolPeriodsStore ps : list) {
            poolStroreMap.put(ps.getStoreCode(), ps.getStoreCode());
        }
        List<YXStore> allStore = yxStoreDao.getStoreList();
        for (YXStore s : allStore) {
            String code = poolStroreMap.get(s.getCode());
            if (code != null) {
                continue;
            }
            YxPoolPeriodsStore cMSUnitePeriodsProvince = new YxPoolPeriodsStore();
            cMSUnitePeriodsProvince.setPeriodId(periodId);
            cMSUnitePeriodsProvince.setCreateId("sys1");
            cMSUnitePeriodsProvince.setStoreCode(s.getCode()); // 获取区域下的所有省份
            cMSUnitePeriodsProvince.setCreateTime(new Date());
            yxPoolPeriodsStoreDao.insertYxPoolPeriodsStore(cMSUnitePeriodsProvince);
        }
    }

    @Override
    public void checkPeriodStoreAndRestore(YxPool pool) {
        if(null == pool){
            return;
        }
        List<YXStore> allStore = yxStoreDao.getStoreList();
        logger.info("Start checkPeriodStoreAndRestore poolId="+pool.getId());
        List<YxPoolPeriods> list = pool.getYppList();
        for(YxPoolPeriods pps : list){
            List<YxPoolPeriodsStore> psList = yxPoolPeriodsStoreDao.queryStoreListByPeriodsId(pps.getId());
            if(psList.size()>allStore.size()){//关系列表大于门店数，有问题
                deleteRepeatStoreRelation(pps.getId(),allStore);
            }
        }

    }

    private void deleteRepeatStoreRelation(Long periodsId, List<YXStore> allStore) {
        for(YXStore s: allStore){
            List<YxPoolPeriodsStore> pstoreList = yxPoolPeriodsStoreDao.listYxPoolPeriodsStoreByCode(periodsId, s.getCode());
            if(pstoreList!=null && pstoreList.size()>1){
                Iterator<YxPoolPeriodsStore> it = pstoreList.iterator();
                int si = pstoreList.size();
                while(it.hasNext()) {
                    YxPoolPeriodsStore ss = it.next();
                    long id = ss.getId();
                    if(StringUtils.isBlank(ss.getCommoditys())){//判断该门店数据为空才可删除
                        yxPoolPeriodsStoreDao.deleteYxPoolPeriodsStore(id);
                        it.remove();
                        si--;
                        if (si == 1) {
                            break;
                        };
                    }
                }
                if(pstoreList.size()>1){
                    for(int i=1; i<pstoreList.size(); i++){//保留一条记录（index=0），其余记录删除
                        long id = pstoreList.get(i).getId();
                        yxPoolPeriodsStoreDao.deleteYxPoolPeriodsStore(id);
                    }
                }
            }
        }

    }

    @Override
    public void checkPeriodStoreAndRestore(String curPage, String pageRows) {
        YxPool yxPool = new YxPool();
        yxPool.setPageRows(100);
        if(StringUtils.isNotBlank(curPage)){
            yxPool.setCurPage(Integer.parseInt(curPage));
        }
        if(StringUtils.isNotBlank(pageRows)){
            yxPool.setPageRows(Integer.parseInt(pageRows));
        }
        List<YxPool> poolList = poolService.queryPoolList(yxPool);
        for(YxPool pool:poolList){
            checkPeriodStoreAndRestore(pool);
        }
    }

    @Override
    public void copyPeriodsById(YxPoolPeriods yxPoolPeriods) {
        //查找old数据
        YxPoolPeriods yx = cppDao.findPeriodsByOldId(yxPoolPeriods.getId());
        //插入一条新的期数
        yx.setBeginTime(yxPoolPeriods.getBeginTime());//设置开始时间
        yx.setName(yxPoolPeriods.getName());//设置期数name
        yx.setPoolId(yxPoolPeriods.getPoolId());//设置池id
        long newPeriodsId = cppDao.addYxPoolPeriods(yx);
        //查询关联的店铺
        List<YxPoolPeriodsStore> yxPoolPeriodsStoreList = yxPoolPeriodsStoreDao.findPeriodsStoreByOld(yxPoolPeriods.getId());
        if(yxPoolPeriodsStoreList!=null && yxPoolPeriodsStoreList.size() > 0){
            //有关联的店铺信息
        	String oldCommodityIds = "";
            for(YxPoolPeriodsStore yxPoolPeriodsStore:yxPoolPeriodsStoreList){
                //yxPoolPeriodsStore.setPeriodId(newPeriodsId);
                if(!"".equals(yxPoolPeriodsStore.getCommoditys())) {
                	oldCommodityIds += yxPoolPeriodsStore.getCommoditys()+",";
    			}
                //yxPoolPeriodsStoreDao.addyxPoolPeriodsStore(yxPoolPeriodsStore);
            }
            oldCommodityIds = toDeleteRepeat(oldCommodityIds);
            List<YxPoolCommodity> commodityList = commodityDao.getYxPoolCommodityByIds(oldCommodityIds);
            Map<String, String> newIdMap = new HashMap<String, String>();
            //保存商品数据
            for(YxPoolCommodity pc: commodityList) {
            	long oldId = pc.getId();
            	pc.setId(null);
            	long newId = commodityDao.insertYxPoolCommodity(pc);
            	newIdMap.put(oldId + "", newId + "");
            	//保存商品关联属性
            	List<YxPoolProperPlus> list = properPlusDao.queryProperList(oldId);
            	if(list.size() > 0) {
            		for(YxPoolProperPlus pp : list) {
                		pp.setCommodityId(newId);
                		pp.setId(null);
                	}
                	properPlusDao.batchInsert(list);
            	}
            }
            
            for(YxPoolPeriodsStore yxPoolPeriodsStore:yxPoolPeriodsStoreList){
                //店铺和新期数建立关系
                yxPoolPeriodsStore.setPeriodId(newPeriodsId);
                
                //更新商品ID
                String oldCIds[] = yxPoolPeriodsStore.getCommoditys().split(",");
                String newCIds = "";
                for(int i = 0; i<oldCIds.length; i++) {
                	String oldCId = oldCIds[i];
                	if(StringUtils.isNotBlank(newIdMap.get(oldCId))) {
                		newCIds += newIdMap.get(oldCId) + ",";
                	}
                }
                if(newCIds.endsWith(",")){
                	newCIds = newCIds.substring(0, newCIds.length()-1);
        		}
                yxPoolPeriodsStore.setCommoditys(newCIds);
                
                yxPoolPeriodsStoreDao.insertYxPoolPeriodsStore(yxPoolPeriodsStore);
            }
        }
    }
    
    /**
	 * @Description 去除重复的ID
	 * @param newIdList: 逗号分隔的ID列表
	 * @return
	*/ 
	private String toDeleteRepeat(String newIdList) {
		String ids[] = newIdList.split(",");
		Map<String,String> idMap = new HashMap<String,String>();
		String newIds = "";
		for(String id: ids){
			if(idMap.get(id)==null && !"".equals(id)){
				newIds += id+",";
				idMap.put(id, id);
			}
		}
		return newIds;
	}
}


