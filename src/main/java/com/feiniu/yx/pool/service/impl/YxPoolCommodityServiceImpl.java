package com.feiniu.yx.pool.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.YXPromotionPageService;
import com.feiniu.yx.core.CommoditySaleInfoService;
import com.feiniu.yx.core.impl.BaseCommodityServiceImpl;
import com.feiniu.yx.pool.dao.YxPoolCommodityDao;
import com.feiniu.yx.pool.dao.YxPoolDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreDao;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.pool.entity.YxPoolProperPlus;
import com.feiniu.yx.pool.service.YxCouponService;
import com.feiniu.yx.pool.service.YxPoolCommodityService;
import com.feiniu.yx.pool.service.YxPoolPeriodsStoreService;
import com.feiniu.yx.pool.service.YxPoolProperPlusService;
import com.feiniu.yx.pool.service.YxPoolService;
import com.feiniu.yx.pool.service.YxRemoteCommodityService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.entity.YXStoreGroup;
import com.feiniu.yx.store.service.YXStoreGroupService;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.ExportUtil;
import com.feiniu.yx.util.UserUtil;
import com.feiniu.yx.util.YxPoolConst;

@Service
public class YxPoolCommodityServiceImpl implements YxPoolCommodityService {

	protected static Logger logger = Logger.getLogger(YxPoolCommodityServiceImpl.class);

	@Autowired
	private YxPoolPeriodsDao cppDao;

	@Autowired
	private YxPoolCommodityDao commodityDao;

	@Autowired
	private YxPoolDao yxPoolDao;

	@Autowired
	private YxPoolService cpsService;

	@Autowired
	private YxPoolPeriodsStoreService cppService;

	@Autowired
	private YxPoolPeriodsStoreDao yxPoolPeriodsStoreDao;

	@Autowired
	private YXStoreService yxStoreService;

	@Autowired
	private YXStoreGroupService yxStoreGroupService;

	@Autowired
	private YxRemoteCommodityService remoteCommodityService;

	@Autowired
	private BaseCommodityServiceImpl baseCommodityService;

	@Autowired
	private YxPoolProperPlusService properPlusService;

	@Autowired
	private YxCouponService couponService;

	@Autowired
	private YXPromotionPageService promoPageService;

	@Autowired
	private CommoditySaleInfoService commoditySaleInfoService;
	/***
	 * 商品ID查询商品
	 *
	 * @param Id
	 * @return
	 */
	public YxPoolCommodity queryCommodityById(String Id) {
		YxPoolCommodity pc = null;
		if (Id != null) {
			pc = commodityDao.queryYxPoolCommodityByID(Long.valueOf(Id));
			//处理groupId旧版兼容大区ID
			String groupIds= pc.getGroupIds();
			List<YXStoreGroup> pageGroupList = yxStoreGroupService.listYXStoreGroup(groupIds);
			for(YXStoreGroup group: pageGroupList){
				if(group.getLevel()!=3){
					List<YXStoreGroup> groupList = yxStoreGroupService.listYXStoreGroup();
					Map<String,YXStoreGroup> groupMap = new HashMap<String,YXStoreGroup>();
					for(YXStoreGroup bg:groupList){
						groupMap.put(bg.getId()+"",bg);
					}

					//池数据编辑时，xiaoQuIds无数据
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

			String storeCodes =pc.getStoreCodes();
			if(StringUtils.isNotBlank(storeCodes)){
				List<YXStore> storeList = yxStoreService.getStoreByCodes(storeCodes);
				StringBuilder sb = new StringBuilder();
				for(YXStore csa :storeList){
					sb.append(csa.getName()).append(",");
				}
				String areaName=sb.toString();
				if(areaName.endsWith(",")){
					areaName = areaName.substring(0,areaName.length()-1);
				}
				pc.setStoreNames(areaName);
			}
			String couponProperties = pc.getCouponProperties();
			if(StringUtils.isNotBlank(couponProperties)){
				@SuppressWarnings("unchecked")
				Map<String,String> couponMap = JSONObject.parseObject(couponProperties,Map.class);
				pc.setCouponPropertiesMap(couponMap);
			}
		}
		return pc;
	}

	/**
	 * 池列表里的修改功能 只修改促销语，角标等等
	 *
	 * @param poolCommodity
	 * @param
	 */
	@Override
    public void updateCommodityFromPool(YxPoolCommodity poolCommodity) {
		poolCommodity.setUpdateTime(new Date());
		commodityDao.updateYxPoolCommodityFromPool(poolCommodity);
	}

	/**
	 * 池列表里的素材修改功能 只修改PC/无线图片地址，链接地址等等
	 *
	 * @param poolCommodity
	 * @param
	 */
	@Override
    public void updateCommodityPicFromPool(YxPoolCommodity poolCommodity) {
		poolCommodity.setUpdateTime(new Date());
		String couponProperties = poolCommodity.getCouponProperties();
		if(StringUtils.isNotBlank(couponProperties)){
			poolCommodity.setCouponProperties(couponProperties);
		}
		commodityDao.updateYxPoolPicCommodityFromPool(poolCommodity);
	}

	private JSONObject commodityJO(String storeCodes, YxPoolCommodity c) {
		JSONObject cjo = new JSONObject();
		String[] codes = storeCodes.split(",");
		String cid = c.getCommodityId();
		cjo.put("cid", cid);
		String sc = "," + c.getStoreCode() + ",";
		for (String code : codes) {
			if (sc.indexOf(("," + code + ",")) > -1) {
				JSONObject j = new JSONObject();
				cjo.put(code, j);
			}
		}
		return cjo;
	}

	/**
	 * @Description:根据商品ID添加商品，
	 * @author lizhiyong
	 * 2017年3月7日
	 * @param ids
	 * @param periodId
	 * @param
	 * @param poolType
	 * @param
	 * @return
	 * @throws Exception
	 */
	@Override
    public String addYxPoolCommodityById(String[] ids, Long periodId, int poolType, String storeGroups, String stores, String commodityType){
		return addCommodityByIds(ids, periodId, storeGroups, stores, commodityType, 0, new HashMap<String, YxPoolCommodity>());
	}

	private String  addCommodityByIds(String[] ids, Long periodId, String storeGroups,String stores,String commodityType, int classId, Map<String, YxPoolCommodity> initMap){
		int searchType = commodityType.equals("0") ? 1 :0;  //查询商品类型 0:以商品编号查询 ; 1:rt货号查询 ,默认0
		//用solr接口查询商品的信息，找到可以添加的门店
		Map<String,YxPoolCommodity> remoteQueryResult = remoteCommodityService.getRemoteCommodityMapByIds(ids,searchType);
		if (remoteQueryResult.size() == 0) {
			return getReturnString("-1","根据商品ID未查找到数据","");
		}
		//将solr查到的商品信息转成json，方便后面操作
		JSONObject IDJO = new JSONObject();
		//保存添加顺序
		ArrayList<String> idlist = new ArrayList<String>();
		for(String id : ids) {
			YxPoolCommodity c = remoteQueryResult.get(id);
			if (c == null) {
				continue;
			}
			JSONObject cjo = IDJO.getJSONObject(id);
			if (cjo != null) {
				continue;
			}
			cjo = commodityJO(stores, c);
			IDJO.put(id, cjo);
			idlist.add(id);
		}

		//查询期数门店表中门店记录
		List<YxPoolPeriodsStore> periodsStoreList = cppService.listPoolPeriodsStoresByPeriodsIdAndStoreCodes(periodId, stores);
		//判断哪些门店可以加商品，哪些不能加，哪些商品门店已经存在
		for(YxPoolPeriodsStore pps : periodsStoreList) {
			Map<String, YxPoolCommodity> localCommodityMap = commodityDao.getMapCommodityByIds(pps.getCommoditys());
			for (String key : IDJO.keySet()) {
				JSONObject cJo = IDJO.getJSONObject(key);
				String commodityId = cJo.getString("cid");
				YxPoolCommodity old = localCommodityMap.get(commodityId);
				JSONObject storeJo = cJo.getJSONObject(pps.getStoreCode());
				//商品加不了这个门店
				if (storeJo == null) {
					continue;
				}
				//门店已经存在这个商品
				if (old != null) {
					cJo.put("oldID", old.getId());
					storeJo.put("hasC", "true");
				} else {
					//门店可以加这个商品，添加到数据库，获得新的商品id，保存到商品json
					Object newID = cJo.get("newID");
					if (newID == null) {
						YxPoolCommodity cc = remoteQueryResult.get(key);
						cc.setPeriods(periodId);
						cc.setStoreCodes(stores);
						cc.setClassId(classId);
						cc.setGroupIds(storeGroups);
						cc.setCreateId(UserUtil.getUserId());
						cc.setUpdateId(UserUtil.getUserId());
						if(initMap.get(cc.getCommodityId())!=null){//保存远程接口中的上新日期
							cc.setAddOnDate(initMap.get(cc.getCommodityId()).getAddOnDate());
						}
						if(cc.getAddOnDate()==null){
							cc.setAddOnDate(new Date());
						}
						long toAddId = commodityDao.insertYxPoolCommodity(cc);
						cJo.put("newID", toAddId);
					}
					storeJo.put("hasC", "false");
				}
			}
		}

		YxPoolPeriods poolPeriod = cppDao.queryYxPoolPeriodsByID(periodId);
		//查询池信息，获得添加的规则
		YxPool pool = yxPoolDao.queryYxPoolById(poolPeriod.getPoolId());
		//通过json找到门店添加的新商品id，并更新到数据库
		for(YxPoolPeriodsStore pps : periodsStoreList) {
			int count = 0;
			StringBuilder addIDs = new StringBuilder();
			for (String key : idlist) {
				JSONObject cJo = IDJO.getJSONObject(key);
				JSONObject storeJo = cJo.getJSONObject(pps.getStoreCode());
				if (storeJo == null) {
					continue;
				}
				String hasC = storeJo.getString("hasC");
				if (hasC == null) {
					continue;
				}
				Long newID = cJo.getLong("newID");
				//商品不在池里才加
				if (newID != null && hasC.equals("false")) {
					if (count >  0) {
						addIDs.append(",");
					}
					addIDs.append(newID);
					count++;
				}
			}
			String localIDs = pps.getCommoditys();
			String addIDs_str = addIDs.toString();
			if (StringUtils.isBlank(localIDs) && StringUtils.isBlank(addIDs_str)) {
				continue;
			}
			String newIDs = "";
			if (StringUtils.isBlank(localIDs)) {
				newIDs = addIDs_str;
			}else {
				if (pool.getOrderRule() == 1) {
					newIDs = addIDs_str +","+ localIDs;
				} else {
					newIDs = localIDs +","+ addIDs_str;
				}
			}
			pps.setCommoditys(newIDs);
			pps.setUpdateId(UserUtil.getUserId());
			yxPoolPeriodsStoreDao.updateCommoditys(pps);
			logger.info(pps.getStoreCode() + ":" + newIDs);
		}
		logger.info(IDJO.toJSONString());
		JSONObject resultObject = new JSONObject();
		JSONObject infoMsg = getInfoMsg(ids, stores, IDJO);
		int successNum = infoMsg.getIntValue("successNum");
		infoMsg.remove("successNum");
		resultObject.put("code", "0");
		resultObject.put("infoMsg", infoMsg);
		resultObject.put("successNum", successNum);
		resultObject.put("errorNum", ids.length -successNum);
		return resultObject.toString();
	}

	/**
	 * 添加提示信息
	 * @param storeCodes
	 * @param IDJO
	 * @return
	 */
	private JSONObject getInfoMsg(String[] ids, String storeCodes, JSONObject IDJO) {
		JSONObject infoMsg = new JSONObject();
		infoMsg.put("successNum", 0);
		if (StringUtils.isBlank(storeCodes)) {
			return infoMsg;
		}
		int addNum = 0;
		Map<String, YXStore> storeMap = yxStoreService.getYXStoreMap();
		String[] codes = storeCodes.split(",");
		for (String id : ids) {
			JSONObject c = IDJO.getJSONObject(id);
			JSONObject ma = new JSONObject();
			infoMsg.put(id, ma);
			if (c == null) {
				ma.put(id, "商品所有门店不可卖");
				continue;
			}
			int add = 0;
			for (String code : codes) {
				YXStore store = storeMap.get(code);
				String key = code;
				if (store != null) {
					key = store.getName()+"["+code+"]";
				}
				JSONObject s = c.getJSONObject(code);
				if (s == null) {
					ma.put(key, "商品不可卖");
					continue;
				}
				String hasC = s.getString("hasC");
				if (hasC == null) {
					ma.put(key, "商品不可卖");
					continue;
				}
				if ("true".equals(hasC)) {
					ma.put(key, "商品已存在");
				}else {
					ma.put(key, "商品添加成功");
					add++;
				}
			}
			if (add > 0) {
				addNum++;
			}
		}
		infoMsg.put("successNum", addNum);
		return infoMsg;
	}

	@Override
    public String  addYxPoolCommodityByIdFromRemote(List<YxPoolCommodity> commodityList, Long poolId, String storeCode, int classId){
		YxPool pool = cpsService.queryPoolById(poolId);
		Long periodId = pool.getCurrentPeriods();
		List<YXStoreGroup> groupList = yxStoreGroupService.listYXStoreGroup();
		String storeGroups = "";
		for(YXStoreGroup sg : groupList){
			if(sg.getStoreId().indexOf(storeCode)!=-1){
				storeGroups = sg.getId()+"";
				break;
			}
		}
		String idString = "";
		Map<String,YxPoolCommodity> initMap =new HashMap<String,YxPoolCommodity>();
		for(YxPoolCommodity yc:commodityList){
			idString += yc.getCommodityId()+",";
			initMap.put(yc.getCommodityId(), yc);
		}
		String[] ids = idString.split(",");

		addCommodityByIds(ids, periodId, storeGroups, storeCode, "1", classId,initMap);
		return "添加成功";
	}

	private String getReturnString(String code,String mainString,String detailString){
		JSONObject jo= new JSONObject();
		jo.put("code", code);
		jo.put("mainString", mainString);
		jo.put("detailString", detailString);
		return jo.toJSONString();
	}

	public String savePicAndText(YxPoolCommodity cpc, Long periodId,  String storeGroups,String stores){
		String res ="";
		Set<String> distinationStores = new HashSet<String>();
		List<YXStore> storeList = yxStoreService.getStoreByCodes(stores);
		for (YXStore loop : storeList) {
			distinationStores.add(loop.getCode());
		}
		YxPoolPeriods poolPeriod = cppDao.queryYxPoolPeriodsByID(periodId);
		YxPool pool = yxPoolDao.queryYxPoolById(poolPeriod.getPoolId());
		List<YxPoolPeriodsStore> periodsStoreList = cppService.getPoolPeriodsStoresByPeriodsId(periodId, null);
		cpc.setUpdateId(cpc.getCreateId());
		cpc.setUpdateTime(new Date());
		cpc.setPeriods(periodId);
		cpc.setPoolId(pool.getId());
		cpc.setStoreCodes(stores);
		cpc.setGroupIds(storeGroups);
		String properties = cpc.getCouponProperties();
		String urlType = cpc.getUrlType();
		if(urlType.equals("coupon")){ //不写入数据库
			cpc.setCouponProperties("");
		}
		Long returnId = commodityDao.insertYxPoolCommodity(cpc);
		if(urlType.equals("coupon")){//更新设置领券的
			properPlusService.updateProperCommodityId(properties, returnId);
		}
		for (YxPoolPeriodsStore store : periodsStoreList) {
			if(!distinationStores.contains(store.getStoreCode())){
				continue;
			}
			String commoditys = store.getCommoditys();
			if (pool.getOrderRule() == 0) {// 根据排序规则保存
				commoditys = commoditys +","+returnId.toString();
			} else if (pool.getOrderRule() == 1) {
				commoditys = returnId.toString()+","+commoditys;
			} else {
				commoditys = commoditys +","+returnId.toString();
			}
			commoditys = trimComma(commoditys);
			store.setCommoditys(commoditys);
		}
		CollectionUtils.forAllDo(periodsStoreList, new Closure() {
			@Override
			public void execute(Object input) {
				YxPoolPeriodsStore item = (YxPoolPeriodsStore) input;
				item.setUpdateId(UserUtil.getUserId());
				item.setUpdateTime(new Date());
				yxPoolPeriodsStoreDao.updateCommoditys(item);
			}
		});
		return res;
	}

	public String trimComma(String source){
		if(StringUtils.isNotBlank(source)){
			if(source.startsWith(",")){
				source = source.substring(1);
			}
			if(source.endsWith(",")){
				source = source.substring(0,source.length()-1);
			}
		}
		return source;
	}

	public String checkAndReplaceCommodity(String newNO, String oldId, Long periodId, String storeCode,String commodityType){
		int searchType = 0;  //查询商品类型;0:以商品编号查询 1：rt货号查询 ,默认0
		if(commodityType.equals("0")){
			searchType = 1;
		}
		YxPoolCommodity oldCommodity = commodityDao.queryYxPoolCommodityByID(Long.valueOf(oldId));
		List<YxPoolPeriodsStore> allcppList = cppService.getPoolPeriodsStoresByPeriodsId(periodId, null);
		String[] goods=new String[]{newNO};
		List<YxPoolCommodity> remoteList=remoteCommodityService.getRemoteCommodityListByIds(goods,searchType);
		YxPoolCommodity cc =null;
		JSONObject messageObject = new JSONObject();
		if (remoteList == null || remoteList.size() == 0) {
			messageObject.put("errorMsg", "替换失败，该商品ID或SKUID不存在。");
			return messageObject.toJSONString();
		}
		cc = remoteList.get(0);
		if(null == cc){
			messageObject.put("errorMsg", "替换失败，该商品ID或SKUID不存在。");
			return messageObject.toJSONString();
		}
		messageObject= addReplaceCommodityMessage(allcppList,cc,cc.getCommodityId(),cc.getRtNo(),searchType,oldCommodity.getStoreCodes());
		boolean isExist = messageObject.getBooleanValue("isExist"); //商品是否存在
		if(isExist){
			return messageObject.toJSONString();
		}
		int status = messageObject.getIntValue("status");  //商品替换信息返回状态
		if(status !=1){
			return messageObject.toJSONString();
		}

		cc.setId(Long.valueOf(oldId));
		cc.setSellPoint(oldCommodity.getSellPoint());
		cc.setUpdateTime(new Date());
		cc.setPeriods(periodId);
		cc.setGroupIds(oldCommodity.getGroupIds());
		cc.setStoreCodes(oldCommodity.getStoreCodes());
		cc.setUpdateId(UserUtil.getUserId());
		commodityDao.updateYxPoolCommodity(cc);
		messageObject.put("successMsg", "替换成功");

		return messageObject.toJSONString();
	}

	/**
	 * 判断商品是否可以替换，以及替换后返回的提示信息
	 * @param allcppList
	 * @param cc
	 * @param newNo
	 * @param rtNo
	 * @param searchType
	 * @param oldCommodityStoreCode  //旧商品的storeCode
	 * @return
	 */
	private JSONObject addReplaceCommodityMessage(List<YxPoolPeriodsStore> allcppList,	YxPoolCommodity cc, String newNo,String rtNo,int searchType ,String oldCommodityStoreCode) {
		String newGood = newNo;
		if(searchType == 1){
			newGood = rtNo;
		}

		JSONObject result = new JSONObject();
		String errorMsg = "";
		if (null == allcppList) {
			errorMsg = "替换商品异常";
		}
		if (null == cc) {
			errorMsg = "替换商品不存在";
		}
		Map<String, JSONObject> mapMsg = new HashMap<String, JSONObject>();
		String storeCodes = cc.getStoreCode();
		if (StringUtils.isBlank(storeCodes)) {
			errorMsg = "替换商品异常";
		}
		if(StringUtils.isNotBlank(errorMsg)){
			result.put("errorMsg", errorMsg);
			return result;
		}
		Map<String, String> storeGroupName = yxStoreGroupService.getGroupNameOfStoreCode();;
		for (YxPoolPeriodsStore pps : allcppList) {
			String code = pps.getStoreCode();
			String groupName = storeGroupName.get(code);
			if(StringUtils.isBlank(groupName)){
				groupName="其他";
			}
			JSONObject groupObj= mapMsg.get(groupName);
			if(null == groupObj){
				groupObj = new JSONObject();
			}
			@SuppressWarnings("unchecked")
			List<String> msgList =groupObj.getObject("list",List.class);
			int sucessNum = groupObj.getIntValue("sucessNum");
			int errorNum = groupObj.getIntValue("errorNum");
			if (null == msgList) {
				msgList = new ArrayList<String>();
			}
			if ((!storeCodes.contains(code)) || (!oldCommodityStoreCode.contains(code))) { // 商品不在该门店范围
				String tt = pps.getStoreName() + ":" + newGood+ "(商品ID/RT货号)替换失败";
				msgList.add(tt);
				groupObj.put("list", msgList);
				errorNum += 1;
				groupObj.put("errorNum", errorNum);
				mapMsg.put(groupName, groupObj);
				continue;
			}
			String commodityIds = pps.getCommoditys();

			boolean isExist = commodityIsExistsStore(commodityIds, newNo,code);
			if (isExist) { // 商品在该门店已经存在
				String tt = pps.getStoreName() + ":" + newGood+ "(商品ID/RT货号)已经存在,替换失败";
				msgList.clear();
				mapMsg.clear();
				msgList.add(tt);
				groupObj.put("list", msgList);
				mapMsg.put(groupName, groupObj);
				result.put("infoMsg", mapMsg);
				result.put("isExist",true);
				return result;
			}

			sucessNum += 1;
			groupObj.put("sucessNum", sucessNum);
			mapMsg.put(groupName, groupObj);
		}
		result.put("infoMsg", mapMsg);
		result.put("status", 1);
		result.put("isExist",false);
		return result;
	}


	/**
	 * 判断商品在当前门中是否存在
	 * @return
	 */
	private boolean commodityIsExistsStore(String commodityIds,String newGood,String storeCode){
		List<YxPoolCommodity> pcList = commodityDao.getYxPoolCommodityByIds(commodityIds);
		for(YxPoolCommodity pc : pcList){
			String cd = pc.getCommodityId().trim();
			if(cd.equals(newGood)){
				return true;
			}
		}
		return false;
	}


	/**
	 * 根据商品ID获取多地多仓商品，一个商品可能应用与多个省份
	 * @author lizhiyong
	 * 2016年10月12日
	 * @param art_no //商品ID
	 * @param  //大区组
	 * @return
	 * @throws IOException
	 */
	@Override
    public JSONObject getCommodityForSelect(String art_no, String storeCodes, String searchType) throws IOException {
		String[] ids =new String[]{art_no};
		List<YxPoolCommodity> listCommodity = remoteCommodityService.getRemoteCommodityListByIds(ids,Integer.parseInt(searchType));
		ids = null;
		List<YXStore> storeList = yxStoreService.getActivityStoreByCodes(storeCodes);
		if(null == storeList){
			return null;
		}
		if(storeList.size() == 0){
			return null;
		}
		Map<String,String> groupNameMap = yxStoreGroupService.getGroupNameOfStoreCode();;
		JSONObject resultObject = new JSONObject();
		String inCode = "" ;//商品的所在门店
		YxPoolCommodity commodity = null;
		if(null != listCommodity && listCommodity.size() > 0){
			commodity = listCommodity.get(0);
		}
		if(null != commodity){
			inCode = commodity.getStoreCode();
		}

		for(int i = 0,len = storeList.size(); i < len; i++){
			YXStore store = storeList.get(i);
			String code = store.getCode();
			String groupName = groupNameMap.get(code);
			if(StringUtils.isBlank(groupName)){
				continue;
			}
			JSONObject detail = resultObject.getJSONObject(groupName);
			if(null == detail){
				detail = new JSONObject();
			}
			int successNum = detail.getIntValue("successNum");
			int errorNum = detail.getIntValue("errorNum");
			JSONArray storeArray = detail.getJSONArray("list");
			if(null == storeArray){
				storeArray = new JSONArray();
			}
			JSONObject singleStore = new JSONObject();
			if(inCode.contains(code)){
				successNum += 1;
				singleStore.put("isExist", true);
			}else{
				errorNum += 1;
			}
			singleStore.put("storeCode", code);
			singleStore.put("storeName", store.getName());
			storeArray.add(singleStore);
			detail.put("successNum", successNum);
			detail.put("errorNum", errorNum);
			detail.put("list", storeArray);
			if(listCommodity.size()>0&&"1".equals(searchType)){
				detail.put("commodityId",commodity.getCommodityId());
			}
			resultObject.put(groupName, detail);
		}


		return resultObject;
	}

	/**
	 * @Description 根据id获取商品信息
	 * @param id
	 * @return
	 */
	@Override
    public YxPoolCommodity getCommodityById(String id){
		if(StringUtils.isBlank(id)) {
			return null;
		}else {
			return commodityDao.queryYxPoolCommodityByID(Long.valueOf(id));
		}
	}

	/**
	 * @Description 根据id获取商品信息
	 * @param ids
	 * @return
	 */
	@Override
    public List<YxPoolCommodity> getCommodityByIds(String ids){
		if(StringUtils.isBlank(ids)) {
			return null;
		}else {
			return commodityDao.queryYxPoolCommodityByIds(ids);
		}
	}

	public Map<String,Long> getAllCommoditys(List<YxPoolPeriodsStore> periodsStoreList){
		Map<String,Long> map = new HashMap<String,Long>();
		for(YxPoolPeriodsStore periodProvince : periodsStoreList){
			if (StringUtils.isNotEmpty(periodProvince.getCommoditys())) {
				List<YxPoolCommodity> loopLocalCommodityList = commodityDao.getYxPoolCommodityByIds(periodProvince.getCommoditys());
				if(loopLocalCommodityList!=null&&loopLocalCommodityList.size()>0){
					for(YxPoolCommodity yy:loopLocalCommodityList){
						if(yy.getOriginate()==1){
							map.put(yy.getCommodityId(), yy.getId());
						}
					}
				}
			}
		}
		return map;
	}

	public   String[] concat(String[] array1, String[] array2) {
		String[] result = Arrays.copyOf(array1, array1.length + array2.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}

	@Override
	public JSONObject getCommodityForShow(String commodityId, String storeCode) {
		String[] ids = new String[]{commodityId};
		JSONObject res = new JSONObject();
		res.put("commodityId", commodityId);
		List<YxPoolCommodity> list = baseCommodityService.listCommodityAllInfo(ids, storeCode);
		if(list != null && list.size() > 0){
			YxPoolCommodity ccCommodity = list.get(0);
			res.put("picUrl", ccCommodity.getPicUrl());
			res.put("title", ccCommodity.getTitle());
			res.put("price", ccCommodity.getPrice());
			res.put("unit", ccCommodity.getUnit());
		}
		return res;
	}

	@Override
	public JSONObject getCommodityForModule(String commodityId,String selectStoreCode){
		String[] ids = new String[]{commodityId};
		JSONObject res = new JSONObject();
		res.put("commodityId", commodityId);
		List<YxPoolCommodity> list =remoteCommodityService.getRemoteCommodityListByIds(ids);
		if(null == list || list.size() == 0){
			res.put("isExist", "0");
			return res;
		}
		YxPoolCommodity com = list.get(0);
		if(null == com){
			res.put("isExist", "0");
			return res;
		}
		res.put("name", com.getTitle());
		String storeCode = com.getStoreCode();
		res.put("storeCode", storeCode);
		res.put("isExist", "1");
		String[] codeArray = selectStoreCode.split(",");
		StringBuffer sbNotSale = new StringBuffer();
		StringBuffer sbSaleOut = new StringBuffer();
		String isSale = "1";  //可卖
		String saleOut = "1";  //库存
		//List<String> couponList = new ArrayList<String>();
		//StringBuffer sbNoCoupon = new StringBuffer();
		for(String code : codeArray){
			//String couponIds = couponService.getCouponIdsByCommodityId(ids, code);
			/*if(StringUtils.isBlank(couponIds)){
				sbNoCoupon.append(code).append(",");
			}*/
			//couponList.add(couponIds);
			List<YxPoolCommodity> newlyList = baseCommodityService.listCommodityPriceInfo(ids, code);
			if(null == newlyList || newlyList.size() == 0){
				sbNotSale.append(code).append(",");
				isSale = "0";
				continue;
			}
			YxPoolCommodity newlyCom = newlyList.get(0);
			if(null == newlyCom || !newlyCom.isExist()){
				sbNotSale.append(code).append(",");
				isSale = "0";
				continue;
			}
			if(newlyCom.getStockSum() <= 0){
				sbSaleOut.append(code).append(",");
				saleOut = "0";
			}
		}
		res.put("isSale", isSale);
		res.put("saleOut", saleOut);
		res.put("notSaleCode", sbNotSale.toString());
		res.put("saleOutCode", sbSaleOut.toString());
		//res.put("couponIds", getCouponByModule(couponList));
		/*String storeName = yxStoreService.getStoreNamesByCodes(sbNoCoupon.toString());
		if(StringUtils.isNotBlank(storeName)){
			res.put("noCouponName", storeName);
		}*/
		return res;
	}

	@Override
    public JSONObject getCommodityByCouponIdForModule(String commodityId, String selectStoreCode, String couponId){
		String[] ids = new String[]{commodityId};
		JSONObject res = new JSONObject();
		String[] codeArray = selectStoreCode.split(",");
		List<String> noCode = new ArrayList<String>();
		for(String code : codeArray){
			String couponIds = couponService.getCouponIdsByCommodityId(ids, code);
			if(StringUtils.isBlank(couponIds)){
				noCode.add(code);
				continue;
			}
			if(!couponIds.contains(couponId)){
				noCode.add(code);
				continue;
			}
		}
		if(noCode.size() == codeArray.length){
			//所有门店都没有查询到券，商品不支持该券
			res.put("isExist", "false");
		}
		if(noCode.size() > 0){
			res.put("isUse", "false");
		}
		String storeName = yxStoreService.getStoreNamesByCodes(StringUtils.join(noCode, ","));
		res.put("storeName", storeName);
		return res;
	}

	/*private String getCouponByModule(List<String> couponList){
		String jointly = "";
		if(couponList.contains(null)){
			return jointly;
		}
		if(couponList.contains("")){
			return jointly;
		}
		int len = couponList.size();
		if(len == 1){
			jointly = couponList.get(0);
			return jointly;
		}
		String[] first = couponList.get(0).split(",");
		for(int i = 1; i < len; i++){
			String cp = couponList.get(i);
			List<String> tm = sameInArray(first,cp.split(","));
			first = tm.toArray(new String[]{});
		}
		jointly = StringUtils.join(first,",");
		return jointly;
	}
	private static <T> List<T> sameInArray(T[] t1, T[] t2) {
		List<T> list1 = Arrays.asList(t1);
		List<T> list2 = new ArrayList<T>();
		for (T t : t2) {
			if (list1.contains(t)) {
				list2.add(t);
			}
		}
		return list2;
	}*/



	@Override
	public void deleteRemotePoolCommodityDate(Long poolId, int classId,String storeCode) {
		YxPool pool =  cpsService.queryPoolAndPeriodById(poolId);
		YxPoolPeriodsStore yxPoolPeriodsStore = new YxPoolPeriodsStore();
		yxPoolPeriodsStore.setPeriodId(pool.getYxPoolPeriods().getId());
		yxPoolPeriodsStore.setPageRows(10000);
		if(StringUtils.isNotBlank(storeCode) ){
			yxPoolPeriodsStore.setStoreCode(storeCode);//门店不为空，且门店不匹配时，跳过
		}
		// 获得期数下所有省份与商品的关系
		List<YxPoolPeriodsStore> yxPoolPeriodsStoreList = yxPoolPeriodsStoreDao.queryStoreList(yxPoolPeriodsStore);
		for (YxPoolPeriodsStore pro : yxPoolPeriodsStoreList) {
			if(pro==null){
				continue;
			}
			if(StringUtils.isNotBlank(storeCode) && !pro.getStoreCode().equals(storeCode)){
				continue;//门店不为空，且门店不匹配时，跳过
			}
			String commoditys = pro.getCommoditys();
			List<YxPoolCommodity> cList = this.getCommodityByIds(commoditys);
			if(cList!=null && cList.size()>0){
				for(YxPoolCommodity pc: cList){
					if(pc!=null && pc.getClassId().intValue()==classId){
						commodityDao.deleteYxPoolCommodityById(pc.getId()+""); // 删除商品

						if(commoditys.startsWith(pc.getId()+",")){
							commoditys = commoditys.replace(pc.getId()+",", "");
						}else if(commoditys.endsWith(","+pc.getId())){
							commoditys = commoditys.replace(","+pc.getId(), "");
						}else if(commoditys.equals(pc.getId()+"")){
							commoditys = "";
						}else if(commoditys.contains(","+pc.getId()+",")){
							commoditys = commoditys.replace(","+pc.getId()+",", ",");
						}
					}
				}
				pro.setCommoditys(commoditys);
				yxPoolPeriodsStoreDao.updateCommoditys(pro); // 更新商品省份的关联
			}

		}

	}

	/**
	 * 同步门店数据
	 * @param data
	 * @return
	 */
	@Override
    public JSONObject syncStoreDataByStore(String data){
		JSONObject resultObj = new JSONObject();
		JSONObject dataObj = JSONObject.parseObject(data);
		if(null == dataObj){
			resultObj.put("status", "0");
			resultObj.put("message", "同步参数为空");
			return resultObj;
		}
		String syncStoreCode = dataObj.getString("syncStoreCode");
		Long periodId = dataObj.getLong("periodId");
		String storeCode = dataObj.getString("storeCode");
		String groupId = dataObj.getString("groupId");
		YxPoolPeriodsStore ppsSync = new YxPoolPeriodsStore();
		ppsSync.setPeriodId(periodId);
		ppsSync.setStoreCode(syncStoreCode);
		List<YxPoolPeriodsStore> syncList = cppService.queryStoreList(ppsSync);
		if(null == syncList || syncList.size()== 0){
			resultObj.put("status", "0");
			resultObj.put("message", "同步门店不存在");
			return resultObj;
		}
		ppsSync = syncList.get(0);
		if(null == ppsSync){
			resultObj.put("status", "0");
			resultObj.put("message", "同步门店不存在");
			return resultObj;
		}

		String syncCommoditys = ppsSync.getCommoditys();
		if(StringUtils.isBlank(syncCommoditys)){
			resultObj.put("status", "0");
			resultObj.put("message", "该门店下无数据内容可同步");
			return resultObj;
		}

		YxPoolPeriodsStore pps = new YxPoolPeriodsStore(); //被同步的门店
		pps.setPeriodId(periodId);
		pps.setStoreCode(storeCode);
		List<YxPoolPeriodsStore> ppsList = cppService.queryStoreList(pps);
		if(null == ppsList || ppsList.size()== 0){
			resultObj.put("status", "0");
			resultObj.put("message", "门店不存在");
			return resultObj;
		}
		pps = ppsList.get(0);
		if(null == pps){
			resultObj.put("status", "0");
			resultObj.put("message", "门店不存在");
			return resultObj;
		}
		List<YxPoolCommodity> syncComList = commodityDao.queryYxPoolCommodityByIds(syncCommoditys);
		if(null == syncComList || syncComList.size() == 0){
			resultObj.put("status", "0");
			resultObj.put("message", "该门店下无数据内容可同步");
			return resultObj;
		}
		String ids = syncInsertCommoditys(syncComList,storeCode,groupId,syncStoreCode);
		if(StringUtils.isBlank(ids)){
			resultObj.put("status", "0");
			resultObj.put("message", "该门店下无数据内容可同步");
			return resultObj;
		}
		pps.setCommoditys(ids);
		yxPoolPeriodsStoreDao.updateCommoditys(pps);
		resultObj.put("status", "1");
		resultObj.put("message", "门店数据同步成功");
		return resultObj;
	}


	/**
	 * 写入同步商品
	 * @param syncComList
	 * @param storeCode
	 * @param groupId
	 * @param syncStoreCode
	 * @return
	 */
	private String syncInsertCommoditys(List<YxPoolCommodity> syncComList,String storeCode,String groupId,String syncStoreCode){

		List<String> idList = new ArrayList<String>();
		for(YxPoolCommodity pc : syncComList){
			if(pc.getOriginate() != YxPoolConst.YX_COMMODITY_TYPE_COMMODITY){
				continue;
			}
			String commodityId = pc.getCommodityId();
			if(StringUtils.isBlank(commodityId)){
				continue;
			}
			idList.add(commodityId);
		}
		Map<String,YxPoolCommodity> mapComm = baseCommodityService.mapCommodityPriceInfo(idList.toArray(new String[idList.size()]), storeCode);
		StringBuffer sbIds = new StringBuffer();
		for(YxPoolCommodity pc : syncComList){
			if(null == pc){
				continue;
			}
			if(pc.getOriginate() == YxPoolConst.YX_COMMODITY_TYPE_COMMODITY){
				if(null == mapComm){
					continue;
				}
				String comId = pc.getCommodityId();
				YxPoolCommodity tempCom = mapComm.get(comId);
				if(null == tempCom){
					continue;
				}
				if(!tempCom.isExist()){
					//商品不存在
					continue;
				}
			}
			YxPoolCommodity newCom = new YxPoolCommodity();
			newCom.setCommodityId(pc.getCommodityId());
			newCom.setTitle(pc.getTitle());
			newCom.setPicUrl(pc.getPicUrl());
			newCom.setPicTurnUrl(pc.getPicTurnUrl());
			newCom.setSellPoint(pc.getSellPoint());
			newCom.setPeriods(pc.getPeriods());
			newCom.setCustomUrl(pc.getCustomUrl());
			newCom.setPromoteText(pc.getPromoteText());
			newCom.setStoreCodes(storeCode);
			if(!groupId.equals("all")){ //门店没有设置分区
				newCom.setGroupIds(groupId);
			}else{
				newCom.setGroupIds("");
			}
			newCom.setOriginate(pc.getOriginate());
			newCom.setCreateId(UserUtil.getUserId());
			newCom.setDescription(pc.getDescription());
			newCom.setRemark(pc.getRemark());
			newCom.setAddOnDate(pc.getAddOnDate());
			commodityDao.insertYxPoolCommodity(newCom);
			Long id = newCom.getId();
			sbIds.append(id.toString()).append(",");
			if(pc.getOriginate() == YxPoolConst.YX_COMMODITY_TYPE_COMMODITY){
				//商品不进行链接有效验证
				continue;
			}
			String urlType = pc.getUrlType();
			String urlProperties = pc.getUrlProperties();
			JSONObject params = new JSONObject();
			params.put("urlType", urlType);
			params.put("urlProperties", urlProperties);
			params.put("id", id);
			params.put("storeCode", storeCode);
			params.put("syncStoreCode", syncStoreCode);
			params.put("syncId", pc.getId());
			if(StringUtils.isBlank(newCom.getGroupIds())){
				params.put("noGroup", true);
			}
			JSONObject valid = validateCommdityUrl(params);
			urlType = valid.getString("urlType");
			urlProperties = valid.getString("urlProperties");
			newCom = commodityDao.queryYxPoolCommodityByID(id);
			newCom.setUrlProperties(urlProperties);
			newCom.setUrlType(urlType);
			commodityDao.updateYxPoolPicCommodityFromPool(newCom);

		}
		String ids = sbIds.toString();
		if(StringUtils.isNotBlank(ids)){
			ids = ids.substring(0, ids.length()-1);
		}
		return ids;
	}

	/**
	 * 验证链接是否正常在这个门店显示
	 * @param params
	 * @return
	 */
	private JSONObject validateCommdityUrl(JSONObject params){
		String urlType = params.getString("urlType");
		String urlProperties = params.getString("urlProperties");
		String storeCode = params.getString("storeCode");
		String syncStoreCode = params.getString("syncStoreCode");
		Long syncId = params.getLong("syncId");
		Long id = params.getLong("id");
		JSONObject resultObj = new JSONObject();
		resultObj.put("urlType", "none");
		resultObj.put("urlProperties", "1");
		if(urlType.equals("detail")){
			if(StringUtils.isBlank(urlProperties)){
				return resultObj;
			}
			String[] ids = new String[]{urlProperties.trim()};
			Map<String,YxPoolCommodity> mapCom = baseCommodityService.mapCommodityPriceInfo(ids, syncStoreCode);
			if(null == mapCom || mapCom.size() == 0){
				return resultObj;
			}
			YxPoolCommodity pc = mapCom.get(urlProperties.trim());
			if(null == pc){
				return resultObj;
			}
			if(!pc.isExist()){
				return resultObj;
			}
		}else if(urlType.equals("coupon")){
			YxPoolProperPlus proPlus = new YxPoolProperPlus();
			proPlus.setCommodityId(syncId);
			proPlus.setStoreCode(syncStoreCode);
			proPlus = properPlusService.queryProperPlusSingle(proPlus);
			if(null == proPlus){
				return resultObj;
			}
			String commodityProper  = proPlus.getCommodityProper();
			JSONObject comProper = JSONObject.parseObject(commodityProper);
			if(null == comProper){
				return resultObj;
			}
			String couponId = comProper.getString("couponId");
			JSONObject couponObj = couponService.getCouponStoreCodesById(couponId);
			if(!couponObj.containsKey(couponId)){
				return resultObj;
			}
			String storeIds = couponObj.getString(couponId);
			if(!storeIds.contains(storeCode)){
				return resultObj;
			}
			YxPoolProperPlus newPlus = new YxPoolProperPlus();
			newPlus.setStoreCode(storeCode);
			newPlus.setCommodityId(id);
			newPlus.setCommodityProper(proPlus.getCommodityProper());
			newPlus.setCreateId(UserUtil.getUserId());
			List<YxPoolProperPlus> insertList = new ArrayList<YxPoolProperPlus>();
			insertList.add(newPlus);
			properPlusService.batchInsert(insertList);
		}else if(urlType.equals("cms")){
			boolean noGroup = params.getBooleanValue("noGroup");
			if(noGroup){
				return resultObj;
			}
		}else if(urlType.equals("campseq")){
			if(StringUtils.isBlank(urlProperties)){
				return resultObj;
			}
			JSONObject single = promoPageService.getPromoSinglePageById(urlProperties);
			if(null == single){
				return resultObj;
			}
			String code = single.getString("storeCode");
			if(StringUtils.isBlank(code)){
				return resultObj;
			}
			if(!code.contains(storeCode)){
				return resultObj;
			}
		}
		resultObj.put("urlType", urlType);
		resultObj.put("urlProperties",urlProperties);
		return resultObj;
	}



	/**
	 * 导出excel
	 * @param data
	 * @param response
	 * @return
	 */
	@Override
    public void exportExcel(String data, HttpServletResponse response, HttpServletRequest request){
		JSONObject dataObj = JSONObject.parseObject(data);
		if(null == dataObj){
			return;
		}
		Long poolId = dataObj.getLong("poolId");
		Long periodId = dataObj.getLong("periodId");
		String storeCode = dataObj.getString("storeCode");
		String fileName = "["+poolId+"("+periodId+")]"+ storeCode;
		fileName = ExportUtil.encodeFilename(fileName, request);
		response.setHeader("Content-disposition", "attachment; filename=" + fileName+".xlsx");// 组装附件名称和格式
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Type", "application/octet-stream");
		YxPoolPeriodsStore periodStore=yxPoolPeriodsStoreDao.queryYxPoolPeriodsStoreByCode(periodId, storeCode);
		if(null == periodStore){
			return;
		}
		String ids = periodStore.getCommoditys();
		if(StringUtils.isBlank(ids)){
			return;
		}
		List<YxPoolCommodity> listComm = commodityDao.queryYxPoolCommodityByIds(ids);
		if(null == listComm){
			return;
		}
		if(listComm.size() == 0){
			return;
		}

		List<String> comIds = new ArrayList<String>();
		for(YxPoolCommodity pc : listComm){
			if(null == pc){
				continue;
			}
			if(pc.getOriginate() == YxPoolConst.YX_COMMODITY_TYPE_COMMODITY){
				comIds.add(pc.getCommodityId());
			}

		}
		Map<String,YxPoolCommodity> mapCom =new HashMap<String,YxPoolCommodity>();
		if(comIds.size() > 0){
			mapCom = baseCommodityService.mapCommodityPriceInfo(comIds.toArray(new String[comIds.size()]), storeCode);
		}
		XSSFWorkbook workBook = new XSSFWorkbook();
		OutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			XSSFSheet sheet = workBook.createSheet(storeCode);
			// 创建Excel的sheet的一行
			ExportUtil exportUtil = new ExportUtil(workBook, sheet);
			XSSFCellStyle headStyle = exportUtil.getHeadStyle();
			XSSFCellStyle bodyStyle = exportUtil.getBodyStyle();
			XSSFRow row = sheet.createRow(0);
			String[] titles = { "类型","商品ID", "名称", "是否可卖", "群组区域","链接地址"};
			for(int i =0,len = titles.length; i < len; i++){
				XSSFCell cell = row.createCell(i);
				cell.setCellValue(titles[i]);
				cell.setCellStyle(headStyle);
			}
			String[] types = {"商品","素材","文字链"};
			List<YXStoreGroup> groupList = yxStoreGroupService.listYXStoreGroup();
			for(int j = 0,len = listComm.size();j < len; j++){
				YxPoolCommodity commodity = listComm.get(j);
				String groupIds = commodity.getGroupIds();
				String groupNames = getGroupNames(groupList, groupIds);
				XSSFRow rowData = sheet.createRow(j+1);
				String tp = "";
				int originate = commodity.getOriginate();
				if(0 < originate && originate < 4){
					tp = types[originate - 1];
				}
				XSSFCell cellContent = rowData.createCell(0);
				cellContent.setCellValue(tp);
				cellContent.setCellStyle(bodyStyle);
				if(!tp.equals("商品")){
					cellContent = rowData.createCell(1);
					cellContent.setCellValue("");
					cellContent.setCellStyle(bodyStyle);
					cellContent = rowData.createCell(2);
					cellContent.setCellValue(commodity.getTitle());
					cellContent.setCellStyle(bodyStyle);
					cellContent = rowData.createCell(3);
					cellContent.setCellValue("");
					cellContent.setCellStyle(bodyStyle);
					cellContent = rowData.createCell(4);
					cellContent.setCellValue(groupNames);
					cellContent.setCellStyle(bodyStyle);
					JSONObject uri = new JSONObject();
					uri.put("type", commodity.getUrlType());
					uri.put("value", commodity.getUrlProperties());
					cellContent = rowData.createCell(5);
					cellContent.setCellValue(uri.toJSONString());
					cellContent.setCellStyle(bodyStyle);
					continue;
				}
				String commodityId = commodity.getCommodityId();
				cellContent = rowData.createCell(1);
				cellContent.setCellValue(commodityId);
				cellContent.setCellStyle(bodyStyle);
				cellContent= rowData.createCell(2);
				cellContent.setCellValue(commodity.getTitle());
				cellContent.setCellStyle(bodyStyle);
				String isSale = "不可卖";
				if (mapCom.containsKey(commodityId)) {
					commodity = mapCom.get(commodityId);
					if (commodity.getStockSum() > 0) {
						isSale = "可卖";
					}
				}
				cellContent = rowData.createCell(3);
				cellContent.setCellValue(isSale);
				cellContent.setCellStyle(bodyStyle);
				cellContent = rowData.createCell(4);
				cellContent.setCellValue(groupNames);
				cellContent.setCellStyle(bodyStyle);
				cellContent = rowData.createCell(5);
				cellContent.setCellValue("");
				cellContent.setCellStyle(bodyStyle);
			}
			workBook.write(outputStream);
		} catch (Exception e) {
			logger.debug(e.toString());
		}finally{
			try {
				workBook.close();
				if(null !=outputStream){
					outputStream.flush();
					outputStream.close();
				}
			} catch (Exception e) {
				logger.debug(e.toString());
			}
		}
	}

	private String  getGroupNames(List<YXStoreGroup> list,String groupIds){
		if(null == list){
			return "";
		}
		Map<String,String> map  = new HashMap<String, String>();
		for(YXStoreGroup sg: list){
			if(null == sg){
				continue;
			}
			map.put(sg.getId().toString(), sg.getName());
		}
		String[] ids = groupIds.split(",");

		String[] names = new String[ids.length];
		for(int i = 0,len = ids.length; i< len; i++){
			String id = ids[i];
			String name = map.get(id);
			if(StringUtils.isBlank(name)){
				continue;
			}
			names[i] = name;
		}
		return StringUtils.join(names, ",");

	}

	@Override
	public Map<String,Map<String, YxPoolCommodity>> validCommodityForShow(String commodityIds,
																		  String storeCodes) {
		String[] ids = commodityIds.split(";");
		String[] storeCodeGroup = storeCodes.split(",");
		Map<String,Map<String, YxPoolCommodity>> storeMap = new HashMap<String,Map<String, YxPoolCommodity>>();
		for(String storeCode:storeCodeGroup){
			Map<String, YxPoolCommodity> commodityMap = new HashMap<String, YxPoolCommodity>();
			for(String id:ids){
				Map<String, YxPoolCommodity> pcMap = baseCommodityService.mapCommodityPriceInfo(id.split(","), storeCode);
				if(pcMap==null){
					pcMap = new HashMap<String, YxPoolCommodity>();
				}
				commodityMap.putAll(pcMap);
			}
			storeMap.put(storeCode,commodityMap);
		}

		return storeMap;
	}

	@Override
	public JSONObject queryCommoditySaleInfo(String id, String storeCode) {
		JSONObject obj = new JSONObject();
		Map<String, YxPoolCommodity> pcMap = baseCommodityService.mapCommodityPriceInfo(id.split(","), storeCode);
		YxPoolCommodity pc = pcMap.get(id);
		if(pc!=null){
			obj.put("commodityId", id);
			obj.put("saleQty", pc.getStockSum());
			long endTime = new Date().getTime();
			long beginTime = endTime-7*24*3600*1000;
			long saleSum = commoditySaleInfoService.querySaleInfo(storeCode, id, beginTime, endTime);
			obj.put("saleSum", saleSum);
		}
		return obj;
	}

	@Override
	public Map<String,YxPoolCommodity> getRemoteCommodity(String[] ids,int searchType){
        Map<String,YxPoolCommodity> remoteQueryResult = remoteCommodityService.getRemoteCommodityMapByIds(ids,searchType);
        return remoteQueryResult;
    }


    /**
     * 商品修改门店配置
     * @param id
     * @param storeCodes
     * @param groupIds
     */
    @Override
    public void updateCommoidtyOfStore(Long id,String storeCodes,String groupIds){
        YxPoolCommodity commodity = commodityDao.queryYxPoolCommodityByID(id);
        String comStore = commodity.getStoreCodes();
        if(StringUtils.isBlank(comStore)){
            return;
        }
        if(StringUtils.isBlank(storeCodes)){
            return;
        }
        String[] comStoreArray = comStore.split(",");
        String[] storeArray =storeCodes.split(",");
        List<String> addCode = new ArrayList<String>();
        List<String> delCode = new ArrayList<String>();
        Arrays.sort(comStoreArray);
        Arrays.sort(storeArray);
        for(int i = 0; i < storeArray.length; i++){
            String code = storeArray[i];
            if(Arrays.binarySearch(comStoreArray,code) <= -1){
                addCode.add(code);
            }
        }
        addPeriodStoreCommoditysByStoreCode(addCode,commodity);
        for(int i = 0; i < comStoreArray.length; i++){
            String code = comStoreArray[i];
            if(Arrays.binarySearch(storeArray,code) <= -1){
                delCode.add(code);
            }
        }
        delPeriodStoreCommoditysByStoreCode(delCode,commodity);
        commodity.setStoreCodes(storeCodes);
        commodity.setGroupIds(groupIds);
        commodityDao.updateStoreAndGroupIds(commodity);
    }


    /**
     * 根据门店添加商品id
     * @param addCode
     */
    private void addPeriodStoreCommoditysByStoreCode(List<String> addCode,YxPoolCommodity commodity){
       List<YxPoolPeriodsStore> listPS =  cppService.listPeriodsStoreByStoreCodes2PeriodsId(commodity.getPeriods(),StringUtils.join(addCode,","));
       if(null ==listPS || listPS.size() == 0){
           return;
       }
       for(YxPoolPeriodsStore pps : listPS){
           String commodityIds = pps.getCommoditys();
           Long id = commodity.getId();
           if(StringUtils.isBlank(commodityIds)){
               commodityIds = String.valueOf(id);
           }else{
               if(commodityIds.contains(String.valueOf(id))){
                   continue;
               }
               commodityIds +=(","+commodity.getId());
           }
           pps.setCommoditys(commodityIds);
           cppService.updateCommoditys(pps);
       }
    }
    /**
     * 根据门店删除商品id
     * @param addCode
     */
    private void delPeriodStoreCommoditysByStoreCode(List<String> addCode,YxPoolCommodity commodity){
        List<YxPoolPeriodsStore> listPS =  cppService.listPeriodsStoreByStoreCodes2PeriodsId(commodity.getPeriods(),StringUtils.join(addCode,","));
        if(null ==listPS || listPS.size() == 0){
            return;
        }
        for(YxPoolPeriodsStore pps : listPS){
            String commodityIds = pps.getCommoditys();
            if(StringUtils.isBlank(commodityIds)){
                continue;
            }
            String[] idArray = commodityIds.split(",");
            int index = Arrays.binarySearch(idArray,String.valueOf(commodity.getId()));
            if(index > -1){
                idArray = ArrayUtils.remove(commodityIds.split(","), index);
            }
            pps.setCommoditys(StringUtils.join(idArray,","));
            cppService.updateCommoditys(pps);
        }
    }
}
