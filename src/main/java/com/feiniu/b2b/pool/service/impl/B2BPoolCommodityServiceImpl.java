package com.feiniu.b2b.pool.service.impl;

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

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.pool.dao.B2BPoolCommodityDao;
import com.feiniu.b2b.pool.entity.B2BPoolCommodity;
import com.feiniu.b2b.pool.service.B2BPoolCommodityService;
import com.feiniu.b2b.pool.service.B2BPoolPeriodsService;
import com.feiniu.b2b.pool.service.B2BPoolPeriodsStoreService;
import com.feiniu.b2b.pool.service.B2BPoolService;
import com.feiniu.b2b.pool.service.B2BRemoteCommodityService;
import com.feiniu.b2b.store.entity.B2BStore;
import com.feiniu.b2b.store.service.B2BStoreGroupService;
import com.feiniu.b2b.store.service.B2BStoreService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.pool.dao.YxPoolDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsDao;
import com.feiniu.yx.pool.dao.YxPoolPeriodsStoreDao;
import com.feiniu.yx.pool.entity.YxPicLinks;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.util.ExportUtil;
import com.feiniu.yx.util.UserUtil;

@Service
public class B2BPoolCommodityServiceImpl implements B2BPoolCommodityService {
	
	protected static Logger logger = Logger.getLogger(B2BPoolCommodityServiceImpl.class);

	@Autowired
	private YxPoolPeriodsDao cppDao;

	@Autowired
	private B2BPoolCommodityDao commodityDao;

	@Autowired
	private YxPoolDao b2bPoolDao;

	@Autowired
	private B2BPoolService cpsService;

	@Autowired
	private B2BPoolPeriodsStoreService cppService;

	@Autowired
	private B2BPoolPeriodsService cuppService;
	
	@Autowired
	private YxPoolPeriodsStoreDao b2bPoolPeriodsStoreDao;
	
	@Autowired
	private B2BStoreService b2bStoreService;
	
	@Autowired
	private B2BRemoteCommodityService remoteCommodityService;
	
	@Autowired
	private B2BStoreGroupService storeGroupService;
	
	private static final String ITEM_HOST = SystemEnv.getProperty("item.host") ;
    
	/***
	 * 商品ID查询商品
	 * 
	 * @param Id
	 * @return
	 */
	public B2BPoolCommodity queryCommodityById(String Id) {
		B2BPoolCommodity pc = null;
		if (Id != null) {
			pc = commodityDao.queryB2BPoolCommodityByID(Long.valueOf(Id));
			if (pc.getCustomUrl() != null && !pc.getCustomUrl().startsWith("http:")) {
				pc.setCustomUrl(ITEM_HOST+ pc.getCommodityId());
			}
			String storeCodes =pc.getStoreCodes();
			if(StringUtils.isNotBlank(storeCodes)){
				List<B2BStore> storeList = b2bStoreService.getStoreByCodes(storeCodes);
				StringBuilder sb = new StringBuilder();
				for(B2BStore csa :storeList){
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
	 * @param user
	 */
	public void updateCommodityFromPool(B2BPoolCommodity poolCommodity) {
		poolCommodity.setUpdateTime(new Date());
		commodityDao.updateB2BPoolCommodityFromPool(poolCommodity);
	}
	
	/**
	 * 池列表里的素材修改功能 只修改PC/无线图片地址，链接地址等等
	 * 
	 * @param poolCommodity
	 * @param user
	 */
	public void updateCommodityPicFromPool(B2BPoolCommodity poolCommodity) {
		poolCommodity.setUpdateTime(new Date());
		String couponProperties = poolCommodity.getCouponProperties();
		if(StringUtils.isNotBlank(couponProperties)){
			poolCommodity.setCouponProperties(couponProperties);					
		}
		commodityDao.updateB2BPoolPicCommodityFromPool(poolCommodity);
	}
	
	/**
	 * 商品对象转json
	 * @param storeCodes
	 * @param c
	 * @return
	 */
	private JSONObject commodityJO(String storeCodes, B2BPoolCommodity c) {
		JSONObject cjo = new JSONObject();
		String[] codes = storeCodes.split(",");
		String cid = c.getCommodityId();
		String sc = "," + c.getStoreCode() + ",";
		for (String code : codes) {
			if (sc.indexOf(("," + code + ",")) > -1) {
				JSONObject j = new JSONObject();
				j.put("cid", cid);
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
	 * @param auth
	 * @param poolType
	 * @param areaSeq
	 * @return
	 * @throws Exception
	 * TODO
	 */
	public String  addB2BPoolCommodityById(String[] ids, Long periodId, int poolType, String storeGroups,String stores){
		List<YxPoolPeriodsStore> periodsStoreList = cppService.listPoolPeriodsStoresByPeriodsIdAndStoreCodes(periodId, stores);
		int maxCommodityNum = getMaxPeriodsCommmodityNum(periodsStoreList);
		if(maxCommodityNum >= 200){
		  return getReturnString("-1","超过商品最大数200个","");
		}
		//远程返回的商品列表
		List<B2BPoolCommodity> remoteQueryResult = remoteCommodityService.getRemoteCommodityOfStoreInfo(ids,stores);
		if(remoteQueryResult==null){
			return getReturnString("-1","远程接口连接异常","");
		}else if(remoteQueryResult.size()==0){
			return getReturnString("-1","根据商品ID未查找到数据","");
		}
		maxCommodityNum = maxCommodityNum + remoteQueryResult.size();
		if(maxCommodityNum >= 200){
			return getReturnString("-1","超过商品最大数200个","");
		}
		Map<String, B2BPoolCommodity> remoteMap = new HashMap<String, B2BPoolCommodity>();
		for(B2BPoolCommodity c : remoteQueryResult) {
			remoteMap.put(c.getCommodityId(), c);
		}
		//将solr查到的商品信息转成json，方便后面操作
		JSONObject IDJO = new JSONObject();
		//保存添加顺序
		ArrayList<String> idlist = new ArrayList<String>();
		for(String id : ids) {
			B2BPoolCommodity bc = remoteMap.get(id);
			if (bc == null) {
				continue;
			}
			JSONObject cjo = IDJO.getJSONObject(id);
			if (cjo != null) {
				continue;
			}
			cjo = commodityJO(stores, bc);
			IDJO.put(id, cjo);
			idlist.add(id);
		}
		
		//判断哪些门店可以加商品，哪些不能加，哪些商品门店已经存在
		for(YxPoolPeriodsStore pps : periodsStoreList) {
			Map<String, B2BPoolCommodity> localCommodityMap = commodityDao.getMapCommodityByIds(pps.getCommoditys());
			for (String key : IDJO.keySet()) {
				B2BPoolCommodity old = localCommodityMap.get(key);
				JSONObject cJo = IDJO.getJSONObject(key);
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
						B2BPoolCommodity cc = remoteMap.get(key);
						cc.setPeriods(periodId);
						cc.setStoreCodes(stores);
						cc.setGroupIds(storeGroups);
						cc.setCreateId(UserUtil.getUserId());
						cc.setUpdateId(UserUtil.getUserId());
						long toAddId = commodityDao.insertB2BPoolCommodity(cc);
						cJo.put("newID", toAddId);
					}
					storeJo.put("hasC", "false");
				}
			}
		}

		YxPoolPeriods poolPeriod = cppDao.queryYxPoolPeriodsByID(periodId);
		YxPool pool = b2bPoolDao.queryB2BPoolById(poolPeriod.getPoolId());
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
			b2bPoolPeriodsStoreDao.updateCommoditys(pps);
			logger.info(pps.getStoreCode() + ":" + newIDs);
		}
		logger.info(IDJO.toJSONString());
		/*JSONObject infoMsg = getInfoMsg(ids, stores, IDJO);
		int successNum = infoMsg.getIntValue("successNum");
		String mainString =  "<p style='font-size:14px;color: red'>成功在全池导入" + successNum + "个商品,异常" + (ids.length -successNum) + "项</p>";
		String detailString = infoMsg.getString("errorMsg");
		logger.info(detailString);
		return getReturnString("0",mainString,detailString);*/
		JSONObject resultObject = new JSONObject();
		JSONObject infoMsg = getInfoMsg(ids, stores, IDJO);
		int successNum = infoMsg.getIntValue("successNum");
		infoMsg.remove("successNum");
		resultObject.put("code", "0");
		resultObject.put("infoMsg", infoMsg);
		resultObject.put("successNum", successNum);
		resultObject.put("errorNum", ids.length -successNum);
		return resultObject.toJSONString();
	}
	
	/**
	 * 获取最多商品门的个数
	 * @param listPeriodsStore
	 * @return
	 */
	private int getMaxPeriodsCommmodityNum(List<YxPoolPeriodsStore> listPeriodsStore){
		
		int maxNum = 0;
		
		for(YxPoolPeriodsStore store : listPeriodsStore){
			
			String commoditys = store.getCommoditys();
			if(StringUtils.isBlank(commoditys)){
				continue;
			}
			
			String[] goods = commoditys.split(",");
	        int length = goods.length;
	        
	        maxNum = length > maxNum ? length : maxNum;			
		}
		
		return maxNum;
		
	}
	
	
	private String getReturnString(String code,String mainString,String detailString){
		JSONObject jo= new JSONObject();	
		jo.put("code", code);
		jo.put("mainString", mainString);
		jo.put("detailString", detailString);
		return jo.toJSONString();	
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
		//StringBuilder error = new StringBuilder();
		Map<String, B2BStore> storeMap = b2bStoreService.getB2BStoreMap();
		String[] codes = storeCodes.split(",");
		for (String id : ids) {
			JSONObject c = IDJO.getJSONObject(id);
			JSONObject ma = new JSONObject();
			infoMsg.put(id, ma);
			if (c == null) {
				/*error.append(id);
				error.append("商品所有门店不可卖. <br>");*/
				ma.put(id, "商品所有门店不可卖");
				continue;
			}
			int add = 0;
			for (String code : codes) {
				B2BStore store = storeMap.get(code);
				String key = code;
				if (store != null) {
					key = store.getName()+"["+code+"]";
				}
				JSONObject s = c.getJSONObject(code);
				if (s == null) {
					ma.put(key, "商品不可卖");
					//error.append(key);
					//error.append(id);
					//error.append("商品不可卖. <br>");
					continue;
				}
				String hasC = s.getString("hasC");
				if (hasC == null) {
					//error.append(key);
					//error.append(id);
					//error.append("商品不可卖. <br>");
					ma.put(key, "商品不可卖");
					continue;
				}
				if ("true".equals(hasC)) {
					/*error.append(key);
					error.append(id);
					error.append("商品已存在. <br/>");*/
					ma.put(key, "商品已存在");
				}else {
					/*error.append(key);
					error.append(id);
					error.append("商品添加成功. <br/>");*/
					ma.put(key, "商品添加成功");
					add++;
				}
			}
			if (add > 0) {
				addNum++;
			}
		}
		infoMsg.put("successNum", addNum);
		//infoMsg.put("errorMsg", error.toString());
		return infoMsg;
	}

    
    public String savePicAndText(B2BPoolCommodity cpc, Long periodId,  String storeGroups,String stores){
    	String res ="";
    	Set<String> distinationStores = new HashSet<String>();
        List<B2BStore> storeList = b2bStoreService.getStoreByCodes(stores);
        for (B2BStore loop : storeList) {
        	distinationStores.add(loop.getCode());
        }
        YxPoolPeriods poolPeriod = cppDao.queryYxPoolPeriodsByID(periodId);
		YxPool pool = b2bPoolDao.queryB2BPoolById(poolPeriod.getPoolId());
		List<YxPoolPeriodsStore> periodsStoreList = cppService.getPoolPeriodsStoresByPeriodsId(periodId, null);
		cpc.setUpdateId(cpc.getCreateId());
		cpc.setUpdateTime(new Date());
		cpc.setPeriods(periodId);
		cpc.setPoolId(pool.getId());
		cpc.setStoreCodes(stores);
		cpc.setGroupIds(storeGroups);
		Long returnId = commodityDao.insertB2BPoolCommodity(cpc);
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
				b2bPoolPeriodsStoreDao.updateCommoditys(item);
			}
		});
		return res;
    }
    
    public String trimComma(String source){
    	if(StringUtils.isNotBlank(source)){
    		if(source.startsWith(","))
    		{
    			source = source.substring(1);
    		}
    		if(source.endsWith(",")){
    			source = source.substring(0,source.length()-1);
    		}
    	}
    	return source;
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
	private JSONObject addReplaceCommodityMessage(List<YxPoolPeriodsStore> allcppList,	B2BPoolCommodity cc,String oldCommodityStoreCode) {
		JSONObject result = new JSONObject();
		String errorMsg = "";
		if (null == allcppList) {
			errorMsg = "替换商品异常";
		}
		if (null == cc) {
			errorMsg = "替换商品不存在";
		}
		String commoditId = cc.getCommodityId();
		Map<String, JSONObject> mapMsg = new HashMap<String, JSONObject>();
		String storeCodes = cc.getStoreCode();
		if (StringUtils.isBlank(storeCodes)) {
			errorMsg = "替换商品异常";
		}
		if(StringUtils.isNotBlank(errorMsg)){
			result.put("errorMsg", errorMsg);
			return result;
		}
		Map<String, String> storeGroupName = storeGroupService.getGroupNameOfStoreCode();
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
				String tt = pps.getStoreName() + ":" + commoditId+ "商品ID替换失败";
				msgList.add(tt);
				groupObj.put("list", msgList);
				errorNum += 1;
				groupObj.put("errorNum", errorNum);
				mapMsg.put(groupName, groupObj);
				continue;
			}
			String commodityIds = pps.getCommoditys();
			
			boolean isExist = commodityIsExistsStore(commodityIds, commoditId,code);
			if (isExist) { // 商品在该门店已经存在
				String tt = pps.getStoreName() + ":" + commoditId+ "商品ID已经存在,替换失败";
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
    	List<B2BPoolCommodity> pcList = commodityDao.getB2BPoolCommodityByIds(commodityIds);
    	for(B2BPoolCommodity pc : pcList){
    		String cd = pc.getCommodityId().trim();
    		if(cd.equals(newGood)){
    			return true;
    		}
    	}
    	return false;
    }
	
    public String checkAndReplaceCommodity(String newGood, String oldId, Long periodId, String storeCode){
    	B2BPoolCommodity oldCommodity = commodityDao.queryB2BPoolCommodityByID(Long.valueOf(oldId));
    	List<YxPoolPeriodsStore> allcppList = cppService.getPoolPeriodsStoresByPeriodsId(periodId, null);
		List<B2BPoolCommodity> remoteList =null;
		String[] goods=new String[]{newGood};
		remoteList =remoteCommodityService.getRemoteCommodityOfStoreInfo(goods,storeCode);
		
		B2BPoolCommodity cc =null;
		
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
		
		messageObject= addReplaceCommodityMessage(allcppList,cc,oldCommodity.getStoreCodes());
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
        commodityDao.updateB2BPoolCommodity(cc);
        messageObject.put("successMsg", "替换成功");
        return messageObject.toJSONString();
	}
    
  
    /**
     * @Description 导出写入excel文件
     * @param outputStream 输出流
     * @param list 导出的list对象
    */  
    public void exportExcel(OutputStream outputStream, B2BPoolCommodity obj)  
    {  
        String[] titles = { "商品类型","商品卖场ID/SKUID", "商品名称","图片地址","链接地址","促销语" };
        // 创建一个workbook 对应一个excel应用文件  
        XSSFWorkbook workBook = new XSSFWorkbook();
        YxPoolPeriods poolPeriod = cuppService.queryPoolPeriodById(obj.getPeriods());
		YxPool pool= cpsService.queryPoolById(obj.getPoolId());
		pool.setYxPoolPeriods(poolPeriod);
		//按省份循环输出商品列表
		for(YxPoolPeriodsStore co2opp :poolPeriod.getStoreList()){
			B2BStore store = b2bStoreService.getB2BStoreByCode(co2opp.getStoreCode());
			if(null == store){
				continue;
			}
			String name = store.getName()+"@"+store.getCode();
			
			// 在workbook中添加一个sheet,对应Excel文件中的sheet 
            XSSFSheet sheet = workBook.createSheet(name);  
            ExportUtil exportUtil = new ExportUtil(workBook, sheet);  
            XSSFCellStyle headStyle = exportUtil.getHeadStyle();  
            XSSFCellStyle bodyStyle = exportUtil.getBodyStyle(); 
            
            sheet.setColumnWidth(2, 50 * 256);//设置第三列宽度
            sheet.setColumnWidth(3, 50 * 256);//设置第四列宽度
            sheet.setColumnWidth(4, 50 * 256);//设置第三列宽度
            // 构建表头  
            XSSFRow headRow = sheet.createRow(0);  
            XSSFCell cell = null;  
            for (int i = 0; i < titles.length; i++)  
            {  
                cell = headRow.createCell(i);  
                cell.setCellStyle(headStyle);  
                cell.setCellValue(titles[i]);  
            }  
            obj.setStoreCode(co2opp.getStoreCode());  
            // 构建表体数据
            //List<YxPoolCommodity> list = cuppService.getUniteCommodityList(obj,true);
            List<B2BPoolCommodity> list = cuppService.getB2BPoolCommodityList(pool,co2opp.getStoreCode(),true);

            B2BPoolCommodity cpc = null;  
            if (list != null && list.size() > 0)  
            {  
                for (int j = 0; j < list.size(); j++)  
                {  
                    XSSFRow bodyRow = sheet.createRow(j + 1);  
                    cpc = list.get(j);  
      
                    cell = bodyRow.createCell(0);  
                    cell.setCellStyle(bodyStyle); 
                    String type="商品";
                    switch (cpc.getOriginate()){
	                    case 1:
	                    	type ="商品";
	                    	break;
	                    case 2:
	                    	type ="素材";
	                    	break;
	                    case 3:
	                    	type ="文字链";
	                    	break;
	                    default:
	                    	type ="未知类型";
                    }
                    cell.setCellValue(type);  
                    
                    cell = bodyRow.createCell(1);  
                    cell.setCellStyle(bodyStyle);  
                    cell.setCellValue(cpc.getCommodityId());  
      
                    cell = bodyRow.createCell(2);  
                    cell.setCellStyle(bodyStyle);  
                    cell.setCellValue(cpc.getTitle());  
                    
                    if(cpc.getOriginate()==2){//图片
                    	cell = bodyRow.createCell(3);  
	                    cell.setCellStyle(bodyStyle);  
	                    cell.setCellValue(cpc.getPicUrl()); 
	                    
	                    cell = bodyRow.createCell(4);  
	                    cell.setCellStyle(bodyStyle);  
	                    cell.setCellValue(cpc.getCustomUrl()); 
                    }else{
	                    cell = bodyRow.createCell(3);  
	                    cell.setCellStyle(bodyStyle);  
	                    cell.setCellValue(cpc.getPicUrl()); 
	                    
	                    cell = bodyRow.createCell(4);  
	                    cell.setCellStyle(bodyStyle);  
	                    cell.setCellValue(cpc.getCustomUrl()); 
                    } 
                    
                    cell = bodyRow.createCell(5);  
                    cell.setCellStyle(bodyStyle);  
                    cell.setCellValue(cpc.getPromoteText());  
                }  
            }
		}
        try  
        {  
            workBook.write(outputStream);  
            outputStream.flush();  
            outputStream.close();  
        }  
        catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
        finally  
        {  
            try  
            {  
                outputStream.close();  
            }  
            catch (IOException e)  
            {  
                e.printStackTrace();  
            }  
        }  
    } 
    /*public YxPicLinks getPicLinks(String areaSeq,String seqJson){
    	YxPicLinks cMSPicLinks = new YxPicLinks();
    	String[] aeraArray = areaSeq.split(",");
		ArrayList<String> allSeq = new ArrayList<String>();
		for(int i=0;i<aeraArray.length;i++){
			String[] seqArray = saService.getProvice(aeraArray[i]);
			allSeq.addAll(Arrays.asList(seqArray));
		}
		ArrayList<YxPoolPeriodsStore> allList = new ArrayList<YxPoolPeriodsStore>();
		if(cMSPicLinks!= null){
			Map<String, String> seqJsonMap = null;
			if(CMSJOSNUtil.isJson(seqJson)){
				seqJsonMap = JSONUtil.toMap(seqJson);
			}
			
			for(int i=0;i<allSeq.size();i++){
				String key = allSeq.get(i).toString();
				if(null !=seqJsonMap && seqJsonMap.containsKey(key)){
					YxPoolPeriodsStore coo = new YxPoolPeriodsStore();
					coo.setProvinceSeq(key);
					coo.setProvinceName(saService.getNameByCode(key));
					coo.setCommoditys(seqJsonMap.get(key));
					allList.add(coo);
				}else{
					YxPoolPeriodsStore coo = new YxPoolPeriodsStore();
					coo.setProvinceSeq(key);
					coo.setProvinceName(saService.getNameByCode(key));
					coo.setCommoditys("");
					allList.add(coo);
				}
			}
			cMSPicLinks.setSeqList(allList);
		}
		return cMSPicLinks;
    }
    */
    
    /**
     * 根据商品ID获取多地多仓商品，一个商品可能应用与多个省份
     * @author lizhiyong
     * 2016年10月12日
     * @param art_no //商品ID
     * @param areaSeq //大区组
     * @return
     * @throws IOException
     * TODO
     */
    
    public YxPicLinks getCommodityForSelect(String art_no, String storeCodes) throws IOException {
    	String[] ids = new String[1];
    	ids[0] = art_no;
    	List<B2BPoolCommodity> listCommodity = remoteCommodityService.getRemoteCommodityOfStoreInfo(ids,storeCodes);
    	ids = null;
        ArrayList<YxPoolPeriodsStore> allList = new ArrayList<YxPoolPeriodsStore>();
        String[] codeArray = storeCodes.split(",");
    	Map<String, B2BStore> map = b2bStoreService.getB2BStoreMapByCode(storeCodes);
    	
    	if(listCommodity!=null && listCommodity.size()>0){
    		B2BPoolCommodity poolCommodity = listCommodity.get(0);
    		String proviceSEQ = ","+poolCommodity.getStoreCode()+",";
	    	for(int i = 0,proLen = codeArray.length; i < proLen; i++)
	    	{
	    		YxPoolPeriodsStore coo = new YxPoolPeriodsStore();
	            coo.setStoreCode(codeArray[i]);
	            coo.setStoreName(map.get(codeArray[i])!=null?map.get(codeArray[i]).getName():"");
    			if((proviceSEQ).indexOf(","+codeArray[i]+",")!=-1)
    			{	
    	            coo.setCommoditys(poolCommodity.getCommodityId());
    			}else{	
    	            coo.setCommoditys(""); 
    	        }
	            allList.add(coo);
	    	}
    	}else{
    		for(int i = 0,proLen = codeArray.length; i < proLen; i++)
	    	{
    			YxPoolPeriodsStore coo = new YxPoolPeriodsStore();
	            coo.setStoreCode(codeArray[i]);
	            coo.setStoreName(map.get(codeArray[i])!=null?map.get(codeArray[i]).getName():"");
    	        coo.setCommoditys(""); 
	            allList.add(coo);
	    	}
    	}
    	
    	YxPicLinks links = new YxPicLinks();
    	links.setGoodsNo(art_no);
    	links.setSeqList(allList);
        return links;
    }
    
	/**
	 * @Description 根据id获取商品信息
	 * @param provinceSeq
	 * @return
	*/ 
	public B2BPoolCommodity getCommodityById(String id){
		if(StringUtils.isBlank(id)) {
			return null;
		}else {
			return commodityDao.queryB2BPoolCommodityByID(Long.valueOf(id));
		}
	}
	
	/**
	 * @Description 根据id获取商品信息
	 * @param provinceSeq
	 * @return
	*/ 
	public List<B2BPoolCommodity> getCommodityByIds(String ids){
		if(StringUtils.isBlank(ids)) {
			return null;
		}else {
			return commodityDao.queryB2BPoolCommodityByIds(ids);
		}
	}
	
	public Map<String,Long> getAllCommoditys(List<YxPoolPeriodsStore> periodsStoreList){
		Map<String,Long> map = new HashMap<String,Long>();
    	for(YxPoolPeriodsStore periodProvince : periodsStoreList){
    		if (StringUtils.isNotEmpty(periodProvince.getCommoditys())) {
    			List<B2BPoolCommodity> loopLocalCommodityList = commodityDao.getB2BPoolCommodityByIds(periodProvince.getCommoditys());
                if(loopLocalCommodityList!=null&&loopLocalCommodityList.size()>0){
                	for(B2BPoolCommodity yy:loopLocalCommodityList){
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
		List<B2BPoolCommodity> list= remoteCommodityService.getRemoteCommodityOfStoreInfo(ids,storeCode);
		JSONObject res = new JSONObject();
		res.put("commodityId", commodityId);
		B2BPoolCommodity priceInfo =remoteCommodityService.getRemoteCommodityPriceInfoByStoreAndId(storeCode, commodityId);
		if(list!=null && list.size()>0 && priceInfo!=null && priceInfo.getPrice()!=null){
			B2BPoolCommodity ccCommodity = list.get(0);
			res.put("picUrl", ccCommodity.getPicUrl());
			res.put("title", ccCommodity.getTitle());
			res.put("price", priceInfo.getPrice());
		}
		return res;
	}    
}
