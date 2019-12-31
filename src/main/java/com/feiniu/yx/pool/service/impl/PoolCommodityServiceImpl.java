package com.feiniu.yx.pool.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.UserService;
import com.feiniu.yx.core.SuperCommodityService;
import com.feiniu.yx.core.impl.ModuleCommodityServiceImpl;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.entity.YxPoolCommodityComparator;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.pool.service.PoolCommodityService;
import com.feiniu.yx.pool.service.YxPoolCommodityService;
import com.feiniu.yx.pool.service.YxPoolPeriodsStoreService;
import com.feiniu.yx.pool.service.YxPoolService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.entity.YXStoreGroup;
import com.feiniu.yx.store.service.YXStoreGroupService;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.YxPoolConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PoolCommodityServiceImpl implements PoolCommodityService {

	@Autowired
	private UserService userService;
	
	@Autowired
	private YXStoreGroupService storeGroupService;
	
    @Autowired
    private YxPoolPeriodsStoreService cprService;

	@Autowired
	private YxPoolCommodityService commodityService;
	
	@Autowired
	private YxPoolService poolService;
	
	@Autowired
	private YXStoreService storeService;
	
	@Autowired
	private ModuleCommodityServiceImpl moduleCommodityServiceImpl;
	
	@Override
	public JSONObject pageCommodityData(String data) {
		JSONObject resultObject = new JSONObject();
		//验证 data不为空 
		if (StringUtils.isBlank(data)) {
			resultObject.put("status", 0);
			resultObject.put("msg", "参数异常");
			return resultObject;
		}
		JSONObject dataJo = JSON.parseObject(data);
		Long poolId = dataJo.getLong("poolId");
		Long periodId = dataJo.getLong("periodId");
		YxPool pool =  getYxPool(poolId, periodId);
		periodId = pool.getYxPoolPeriods().getId();
		
		//所有大区
		List<YXStoreGroup> areaGroup = storeGroupService.getAreaGroup();//大区
		String areaId = dataJo.getString("areaId");

		//用户有权限访问的群组
		List<YXStoreGroup> listGroup = listPreGroup(areaGroup,areaId);//小区
		if (listGroup.size() == 0) {
			resultObject.put("status", 101);
			resultObject.put("msg", "没有门店权限");
			return resultObject;
		}

		if(StringUtils.isBlank(areaId)){
			dataJo.put("areaId", listGroup.get(0).getPid());
		}

		String groupId = dataJo.getString("groupId");
		YXStoreGroup sg = selectedGroup(listGroup, groupId);
		dataJo.put("groupId", sg.getId().toString());
		if(StringUtils.isBlank(groupId)){
			dataJo.put("groupId", "");
		}
		List<YxPoolPeriodsStore> storeList =listPeriodsStores(periodId, sg.getPreStoreCodes());
		//群组下门店数据
		if (storeList.size() == 0) {
			//门店为空继续查找下一个群组
			int gpIndex = listGroup.indexOf(sg);
			for(int i = gpIndex + 1; i < listGroup.size();i++){
				sg = listGroup.get(i);
				if(null == sg){
					continue;
				}
				storeList = listPeriodsStores(periodId, sg.getPreStoreCodes());
				if(storeList.size() > 0){
					break;
				}
			}
		}
		//遍历所有群组为空
		if(storeList.size() == 0){
			resultObject.put("status", 102);
			resultObject.put("msg", "没有门店");
			return resultObject;
		}
		String storeId = dataJo.getString("storeId"); //查询id
		String storeName = dataJo.getString("storeName"); //查询名称
		//查询选中门店
		YxPoolPeriodsStore store = queryStore(storeId, storeName, storeList);
		if (store != null) {
			storeList = new ArrayList<YxPoolPeriodsStore>();
			storeList.add(store);
		}else {
			String storeCode = dataJo.getString("storeCode"); //选中code
			//默认选中门店
			store = selectedStore(storeCode, storeList);
		}
		dataJo.put("storeCode", store.getStoreCode());
		
		//查询分页数据
		List<YxPoolCommodity> provinceCommodityList = ListCommoditysForPage(pool, store, dataJo);
		
		resultObject.put("param", dataJo);
		resultObject.put("pool", pool);
		resultObject.put("areaGroup", areaGroup); //大区列表
		resultObject.put("listGroup", listGroup); //小区列表
		resultObject.put("storelist", storeList); //门店列表
		resultObject.put("commodityList", provinceCommodityList);
		resultObject.put("poolPeriodsStore", store);
		return resultObject;
	}
	
	/**
	 * 查询pool对应期数的数据
	 * @param pool
	 * @param store
	 * @param dataJo
	 * @return
	 */
	private List<YxPoolCommodity> ListCommoditysForPage(YxPool pool, YxPoolPeriodsStore store, JSONObject dataJo) {
		List<YxPoolCommodity> commoditys = commodityService.getCommodityByIds(store.getCommoditys());
		if  (commoditys == null || commoditys.size() == 0) {
			return commoditys;
		}
		//设置数据总数，商品数，图片数，文件数
		resetYxPoolPeriodsStore(store, commoditys);
		//查询类型 -1全部,1 商品, 2 素材, 3文字链接 
		Integer type = dataJo.getInteger("originate");
		//通过类型来获得池的数据
		commoditys = getPoolDataByType(commoditys, type, store.getStoreCode(), pool, moduleCommodityServiceImpl.getBaseCommodityServiceImpl(),1);
		dataJo.put("totalRows", commoditys.size());
		//更新库存排序的id
		//updateOrderIdsByStore(commoditys,pool,store);
		//获得分页数据
		commoditys = getPageData(commoditys, dataJo);

		List<YXStoreGroup> groupList = storeGroupService.listYXStoreGroup();
		Map<String,YXStoreGroup> groupMap = new HashMap<String,YXStoreGroup>();
		for(YXStoreGroup bg:groupList){
			groupMap.put(bg.getId()+"",bg);
		}
		for(YxPoolCommodity pc: commoditys){//处理groupIds数据兼容老大区
			//处理groupId旧版兼容大区ID
			String groupIds= pc.getGroupIds();
			List<YXStoreGroup> pageGroupList = storeGroupService.listYXStoreGroup(groupIds);
			for(YXStoreGroup group: pageGroupList){
				if(group.getLevel()<2){
					String xiaoQuIds = "";
					for(YXStoreGroup bg:groupList){
						if(bg.getLevel()==3){
							YXStoreGroup bgParent = groupMap.get(bg.getPid()+"");
							// 兼容池中旧数据groupIds数据是大区的情况，xiaoQuIds取大区下所有小区
							if((","+groupIds+",").contains(","+bgParent.getPid()+",")){
								xiaoQuIds += bg.getId()+",";
							}
						}
					}
					if(xiaoQuIds.endsWith(",")){
						xiaoQuIds = xiaoQuIds.substring(0,xiaoQuIds.length()-1);
					}
					pc.setGroupIds(xiaoQuIds);
					break;
				}
			}
		}
		return commoditys;
	}
	
	//更新库存排序的id
	private void updateOrderIdsByStore(List<YxPoolCommodity> commoditys,YxPool pool,YxPoolPeriodsStore store){
		if(pool.getStockFt().intValue() != 1){
			return;
		}
		List<String> ids = new ArrayList<String>();
		if(null == commoditys){
			return;
		}
		if(commoditys.size() == 0){
			return;
		}
		for(int i = 0,len = commoditys.size(); i < len; i++){
			YxPoolCommodity pc = commoditys.get(i);
			ids.add(pc.getId().toString());
		}
		store.setCommoditys(StringUtils.join(ids, ","));
		cprService.updateYxPoolPeriodsStoreCommoditys(store);
	}

	/**
	 * 通过类型来获得池的数据
	 * @param list
	 * @param type -1全部,1 商品, 2 素材, 3文字链接
	 * @return
	 */
	private List<YxPoolCommodity> getPoolDataByType(List<YxPoolCommodity> list, Integer type, String storeCode, YxPool pool, SuperCommodityService commodityService, int querySource) {
		if (type == null) {
			type = -1;
		}
		List<YxPoolCommodity> query = new ArrayList<YxPoolCommodity>();
		for (YxPoolCommodity c : list) {
			int originate = c.getOriginate();
			if ((type + 1) == 0) {
				if (originate == 1) {
					query.add(c);
				}
			} else {
				if ((originate - type) == 0) {
					query.add(c);
				}
			}
		}
		if(type.intValue() == 2){
		    return query;
        }
		if (type.intValue() == 3) {
            query = orderPicList(query,storeCode);
			return query;
		}
		//设置商品的价格库存等信息
		commodityService.resetParams(query, storeCode);
		//根据规则重排序
		if(querySource!=1) {
			orderList(query, pool);
			if (type.intValue() == 1) {
				return query;
			}
			//重新设置商品的顺序
			for (int i = 0, j = 0; i < list.size() && query.size() > 0; i++) {
				YxPoolCommodity c = list.get(i);
				if (c.getOriginate().intValue() == YxPoolConst.YX_COMMODITY_TYPE_COMMODITY) {
					list.set(i, query.get(j));
					j++;
				}
			}
		}
		if(type == -1){
            list = orderPicList(list,storeCode);
        }
		return list;
	}


    /***
     * 图片设置了商品的信息
     * @param list
     * @param storeCode
     * @return
     */
	private Map<String,YxPoolCommodity> getPicOfCommodityId(List<YxPoolCommodity> list,String storeCode){
        List<String> picIdList = new ArrayList<String>();
        for(YxPoolCommodity commodity : list){
            if(commodity.getOriginate() != 2){  //不是图片类型
                continue;
            }
            String commodityId = commodity.getCommodityId();
            if(StringUtils.isBlank(commodityId)){ //有设置商品的图片
                continue;
            }
            picIdList.add(commodityId);
        }
        Map<String ,YxPoolCommodity> comMap = moduleCommodityServiceImpl.getBaseCommodityServiceImpl()
                .mapCommodityPriceInfo(picIdList.toArray(new String[]{}),storeCode);
        if(null == comMap){
            new HashMap<String,YxPoolCommodity>();
        }
        return comMap;
    }


    /**
     * 图片设置商品排序
     * @param list
     * @param storeCode
     */
	private List<YxPoolCommodity> orderPicList(List<YxPoolCommodity> list,String storeCode){
        Map<String ,YxPoolCommodity> comMap = getPicOfCommodityId(list,storeCode);
        List<YxPoolCommodity>  newList = new ArrayList<YxPoolCommodity>(); //不用排序的YxPoolCommodity
        List<YxPoolCommodity>  lastList = new ArrayList<YxPoolCommodity>();  //商品库存为0 的图片
        for( int i = 0; i < list.size(); i++){
            YxPoolCommodity commodity = list.get(i);
            if(commodity.getOriginate() != 2){//不是图片类型
                newList.add(commodity);
                 continue;
            }
            String commodityId = commodity.getCommodityId();
            if(StringUtils.isBlank(commodityId)){
                newList.add(commodity);
                continue;
            }
            String sellPoint = commodity.getSellPoint();
            if(sellPoint.equals("2")){  //没有设置商品库存排序
                newList.add(commodity);
                continue;
            }
            YxPoolCommodity remoteCom = comMap.get(commodityId);
            if(null == remoteCom){
                newList.add(commodity);
                continue;
            }
            if(remoteCom.getStockSum() <= 0){
                lastList.add(commodity);
            }else{
                newList.add(commodity);
            }
        }
        if(lastList.size() > 0){
            newList.addAll(lastList);
        }
        return newList;
    }


	/**
	 * 根据规则排序
	 * @param list
	 * @param pool
	 */
	private void orderList(List<YxPoolCommodity> list, YxPool pool) {
		//按照价格排序
    	if(pool.getOrderType().intValue() == 2) {
    		YxPoolCommodityComparator comparator = new YxPoolCommodityComparator();
			comparator.setOrderOrientation(pool.getOrderRule());
    		Collections.sort(list, comparator);
    	}

    	//库存容错，库存为0的放到最后
    	if(pool.getStockFt().intValue() == 1) {
    		YxPoolCommodityComparator comparator = new YxPoolCommodityComparator();
			comparator.setOrderType(3);
    		Collections.sort(list, comparator);
    	}

	}
	
	/**
	 * 检验查询条件&获得分页
	 * @param dataJo
	 */
	private List<YxPoolCommodity> getPageData(List<YxPoolCommodity> list, JSONObject dataJo) {
		//检验查询条件
		String q_commodityId = dataJo.getString("commodityId");
		String q_title = dataJo.getString("title");
		boolean no_q_title = StringUtils.isBlank(q_title);
		boolean no_q_id =  StringUtils.isBlank(q_commodityId);
		//查询结果
		List<YxPoolCommodity> qList = new ArrayList<YxPoolCommodity>();
		for (YxPoolCommodity c : list) {
			//查标题
			if(no_q_id && !no_q_title) {
				String title = c.getTitle();
				//没有查到
				if (!title.contains(q_title)) {
					continue;
				}
			}
			//查商品id
			if(!no_q_id && no_q_title) {
				String id = c.getCommodityId();
				//没有查到
				if (!id.equals(q_commodityId)) {
					continue;
				}
			}
			//查标题和id
			if(!no_q_id && !no_q_title) {
				String id = c.getCommodityId();
				String title = c.getTitle();
				//没有查到
				if (!id.equals(q_commodityId) || !title.contains(q_title)) {
					continue;
				}
			}
			qList.add(c);
		}
		
		//分页
		Integer pageRows = dataJo.getInteger("pageRows");
		Integer curPage = dataJo.getInteger("curPage");
		int rows = 10;
		int cur = 1;
		if (pageRows != null) {
			rows = pageRows;
		}
		if (curPage != null) {
			cur = curPage;
		}
		dataJo.put("curPage", cur);
		dataJo.put("pageRows", rows);
		List<YxPoolCommodity> pageList = new ArrayList<YxPoolCommodity>();
		for (int i = (cur-1)*rows, j = 0; i < qList.size() && j < rows; i++,j++) {
			YxPoolCommodity c2 = qList.get(i);
			c2.setOrder(i+1);
			pageList.add(c2);
		}
		return pageList;
	}
	
	private YxPool getYxPool(Long poolId, Long periodId) {
		YxPool yxPool  = null;
		if(periodId > 0){
			yxPool = poolService.queryPoolAndPeriodById(poolId, periodId);
		}else{
			yxPool = poolService.queryPoolAndPeriodById(poolId);
		}
		return yxPool;
	}

	private YxPool getYxPoolByDateString(Long poolId, String previewTime) {
		YxPool yxPool  = poolService.queryPoolAndPeriodByDate(poolId,previewTime);
		return yxPool;
	}
	
	/**
	 * 获得有权访问的群组
	 * @return
	 */
	private List<YXStoreGroup> listPreGroup(List<YXStoreGroup> allGroup, String areaId) {
		//用户有权限访问的区域
		Map<String,Set<String>> permissonMap = userService.getMapUserStores();
		List<YXStoreGroup> resultList =  new ArrayList<YXStoreGroup>();
		//获取用户权限区域
		Set<String> pgSeqSet = permissonMap.get("pgSeqSet");
		if(null == pgSeqSet){
			 return resultList;
		}
		//获取用户权限门店
		Set<String> codeSet = permissonMap.get("codeSet");
		if(null == codeSet){
			return resultList;
		}
		
		//数据库记录的群组
		//List<YXStoreGroup> allGroup = storeGroupService.listYXStoreGroup();
		for(YXStoreGroup g : allGroup){
			if(StringUtils.isNotBlank(areaId)){
				if(!areaId.equals(g.getId()+"")){
					continue;
				}
			}
			String code = g.getPgSeq();
			String storeIds = "";
			if(g.getGroupList().size()>0){
				for(YXStoreGroup gLevel2: g.getGroupList()){
					if(gLevel2.getGroupList().size()>0){
						for(YXStoreGroup gLevel3: gLevel2.getGroupList()){
							//storeIds += gLevel3.getStoreId();
							String preStores = getPreStoreCodes(codeSet, gLevel3.getStoreId());
							if(pgSeqSet.contains(code) && StringUtils.isNotBlank(preStores)){
								gLevel3.setPid(g.getId());
								gLevel3.setPreStoreCodes(preStores);
								resultList.add(gLevel3);
							}
						}
					}
				}
			}
			if(resultList.size()>0){
				break;
			}

		}
		return resultList;
	}

	/**
	 * 查询选中群组,默认选中第一个
	 * @param list
	 * @param groupId
	 * @return
	 */
	private YXStoreGroup selectedGroup(List<YXStoreGroup> list, String groupId) {
		YXStoreGroup result = list.get(0);

		if (StringUtils.isBlank(groupId)) {
			String storeCodes = "";
			for (YXStoreGroup g : list) {
				storeCodes += g.getPreStoreCodes()+",";
			}
			storeCodes = storeCodes.substring(0,storeCodes.length()-1);
			result.setPreStoreCodes(storeCodes);
			return result;
		}
		for (YXStoreGroup g : list) {
			if (groupId.equals(g.getId().toString())) {
				result = g;
				break;
			}
		}
		return result;
	}
	
	/**
	 * 查询群组下有权限的门店
	 * @param codeSet
	 * @param codes
	 * @return
	 */
	private String getPreStoreCodes(Set<String> codeSet, String codes) {
		List<String> preCodes = new ArrayList<String>();
		if (StringUtils.isBlank(codes)) {
			return "";
		}
		String[] ca = codes.split(",");
		for (String c : ca) {
			if (codeSet.contains(c)) {
				preCodes.add(c);
			}
		}
		
		if (preCodes.size() == 0) {
			return "";
		}
		return StringUtils.join(preCodes, ",");
	}
	
	/**
	 * 获得群组下门店的数据
	 * @param periodId
	 * @param codes
	 * @return
	 */
	private List<YxPoolPeriodsStore> listPeriodsStores(Long periodId, String codes) {
		List<YxPoolPeriodsStore> result = cprService.listPeriodsStoreByStoreCodes2PeriodsId(periodId, codes);
		if (result.size() > 0) {
			List<YXStore> storeList =  storeService.getStoreByCodes(codes);
			Map<String,String> nameMap = new HashMap<String, String>();
			for (YXStore s : storeList) {
				nameMap.put(s.getCode(), s.getName());
			}
			for (YxPoolPeriodsStore ps : result) {
				ps.setStoreName(nameMap.get(ps.getStoreCode()));
			}
		}
		return result;
	}
	
	/**
	 * 根据id或者名称查询
	 * @param storeId
	 * @param storeName
	 * @param storeList
	 * @return
	 */
	private YxPoolPeriodsStore queryStore(String storeId, String storeName, List<YxPoolPeriodsStore> storeList) {
		YxPoolPeriodsStore result = null;
		if (StringUtils.isNotBlank(storeId)) {
			for (YxPoolPeriodsStore s : storeList) {
				if (s.getStoreCode().equals(storeId)) {
					result = s;
					break;
				}
			}
		}
		if (result != null) {
			return result;
		}
		
		if (StringUtils.isNotBlank(storeName)) {
			for (YxPoolPeriodsStore s : storeList) {
				if (s.getStoreName().contains(storeName)) {
					result = s;
					break;
				}
			}
		}
		
		if (result != null) {
			return result;
		}
		
		return result;
	}
	
	/**
	 * 默认选中门店数据
	 * @param storeCode 选中门店code
	 * @param storeList
	 * @return
	 */
	private YxPoolPeriodsStore selectedStore(String storeCode, List<YxPoolPeriodsStore> storeList) {
		YxPoolPeriodsStore result = null;
		
		if (StringUtils.isNotBlank(storeCode)) {
			for (YxPoolPeriodsStore s : storeList) {
				if (s.getStoreCode().equals(storeCode)) {
					result = s;
					break;
				}
			}
		}
		if (result == null) {
			result = storeList.get(0);
		}
		return result;
	}
	
	/**
	 * 设置数量
	 * 商品总数，素材总数，文字链总数
	 * @param c
	 */
	private void resetYxPoolPeriodsStore(YxPoolPeriodsStore c, List<YxPoolCommodity> commoditys ) {
		int commodityNum =0,picNum=0,textNum=0;
		if(commoditys != null && commoditys.size() > 0) {
			c.setCountCommodity(commoditys.size());
			for(YxPoolCommodity cc:commoditys){
				switch(cc.getOriginate()){
					case 1:
						commodityNum++;
						break;
					case 2:
						picNum++;
						break;
					case 3:
						textNum++;
						break;
				}
			}
		}
		c.setCommodityNum(commodityNum);
		c.setPicNum(picNum);
		c.setTextNum(textNum);
	}

	@Override
	public List<YxPoolCommodity> listCommodityDataForModule(Long poolId, String storeCode, String previewTime) {
		//查询池，活动当前池期数
		YxPool pool = new YxPool();
		if(StringUtils.isNotBlank(previewTime)){
			pool = getYxPoolByDateString(poolId, previewTime);
		}else{
			pool =  getYxPool(poolId, 0l);
		}
		if (pool == null) {
			return null;
		}
		Long periodsId = pool.getYxPoolPeriods().getId();
		YxPoolPeriodsStore store = cprService.queryYxPoolPeriodsStoreByCode(periodsId, storeCode);
		if (store == null) {
			return null;
		}
		List<YxPoolCommodity> commoditys = commodityService.getCommodityByIds(store.getCommoditys());
		if  (commoditys == null || commoditys.size() == 0) {
			return commoditys;
		}
		return getPoolDataByType(commoditys, -1, store.getStoreCode(), pool, moduleCommodityServiceImpl,0);
	}

}
