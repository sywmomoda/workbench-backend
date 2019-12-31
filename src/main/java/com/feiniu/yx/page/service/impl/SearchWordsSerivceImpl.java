package com.feiniu.yx.page.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.fcc.core.common.utils.Md5Util;
import com.feiniu.yx.common.entity.ReturnT;
import com.feiniu.yx.common.interceptor.LogTraceService;
import com.feiniu.yx.common.service.YXCacheSyncService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.page.dao.SearchWordsDao;
import com.feiniu.yx.page.entity.SearchWords;
import com.feiniu.yx.page.service.SearchWordsSerivce;
import com.feiniu.yx.pool.entity.YxPool;
import com.feiniu.yx.pool.entity.YxPoolCommodity;
import com.feiniu.yx.pool.entity.YxPoolPeriods;
import com.feiniu.yx.pool.entity.YxPoolPeriodsStore;
import com.feiniu.yx.pool.service.SyncPoolService;
import com.feiniu.yx.pool.service.YxPoolCommodityService;
import com.feiniu.yx.pool.service.YxPoolPeriodsStoreService;
import com.feiniu.yx.pool.service.YxPoolService;
import com.feiniu.yx.store.entity.YXStore;
import com.feiniu.yx.store.service.YXStoreService;
import com.feiniu.yx.util.DateUtil;
import com.feiniu.yx.util.MD5Util;
import com.feiniu.yx.util.UserUtil;
import com.feiniu.yx.util.YxPoolConst;
import com.fn.cache.client.RedisCacheClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchWordsSerivceImpl implements SearchWordsSerivce {

	private static final String DOWN_searchWords_PREFIX =SystemEnv.getProperty("fn.searchWords.download.host");
	
	@Autowired
	private SearchWordsDao searchWordsDao;
	
	@Autowired
	private LogTraceService logSerice;

	@Autowired
	private SyncPoolService syncPoolService;

	@Autowired
	private YxPoolPeriodsStoreService cprService;

	@Autowired
	private YxPoolCommodityService commodityService;

	@Autowired
	private YxPoolService poolService;

	@Autowired
	private YXStoreService storeService;

	@Autowired
	private YXCacheSyncService yXCacheSyncService;

	@Autowired
	private RedisCacheClient cacheClient;
	private final static String SEARCH_KEYWORDS_KEY = "YX_SEARCH_KEYWORDS_";
	private static final String YX_STORE_CACHE = "YX_STORE_CACHE_FOR_SEQ";

	@Override
	public List<SearchWords> list(SearchWords searchWords) {
		List<SearchWords> list = searchWordsDao.list(searchWords);
		for(SearchWords s:list){
			System.out.println(Md5Util.getMd5(s.getKeywords()));
		}
		return list;
	}
	

	
	@Override
	public ReturnT<String> insert(SearchWords searchWords) {
		boolean checkRepeatPass = checkRepeatWords(searchWords);
		if(!checkRepeatPass){
			return new ReturnT(-1,"关键词存在重复");
		}
		String userId = UserUtil.getUserId();
		searchWords.setStatus(1);
		searchWords.setUpdateId(userId);
		searchWords.setCreateId(userId);
		searchWordsDao.insert(searchWords);
		logSerice.sendLogger("【添加】："+JSONObject.toJSONString(searchWords), "alisearchWords", searchWords.getId());
		return new ReturnT<String>(200,"操作成功");
	}

	private boolean checkRepeatWords(SearchWords searchWords){
		SearchWords query = new SearchWords();
		query.setKeywords(searchWords.getKeywords());
		List<SearchWords> list = searchWordsDao.queryCheckRepeat(query);
		for(SearchWords sw: list){
			if(sw.getId()!=searchWords.getId()){
				return false;
			}
		}
		return true;

	}
	
	@Override
	public SearchWords getSearchWordsById(Long id) {
		return searchWordsDao.findByID(id);
	}
	
	@Override
	public ReturnT<String> update(SearchWords searchWords) {
		SearchWords old = searchWordsDao.findByID(searchWords.getId());
		boolean checkRepeatPass = checkRepeatWords(searchWords);
		if(!checkRepeatPass){
			return new ReturnT(-1,"关键词存在重复");
		}

		if(old!=null){
			searchWords.setUpdateId(UserUtil.getUserId());
			searchWordsDao.update(searchWords);

			if(old.getStatus()==3) {//已发布的数据删除旧缓存数据，更新缓存数据
				syncPoolService.syncPool(searchWords.getPoolId());
				String key = SEARCH_KEYWORDS_KEY + MD5Util.getMD5(old.getKeywords());
				//cacheClient.del(key);
				yXCacheSyncService.syncCache("del",key,"","");
				String result = cacheDate(searchWords);
			}

			logSerice.sendLogger("【更新】:"+JSONObject.toJSONString(searchWords), "searchWords", searchWords.getId());
		}
		return new ReturnT<String>(200,"操作成功");
	}
	
	@Override
	public ReturnT<String> delete(Long id) {
		SearchWords old = searchWordsDao.findByID(id);
		if(old.getStatus()==3){//已发布的数据删除去掉缓存数据
			String key = SEARCH_KEYWORDS_KEY + MD5Util.getMD5(old.getKeywords());
			//cacheClient.del(key);
			yXCacheSyncService.syncCache("del",key,"","");
		}
		searchWordsDao.delete(id);
		logSerice.sendLogger("【删除】:"+JSONObject.toJSONString(old), "searchWords", old.getId());
		return new ReturnT<String>(200,"操作成功");
	}

	@Override
	public ReturnT<String> publishSearchWords(Long id) {
		SearchWords searchWords = searchWordsDao.findByID(id);
		syncPoolService.syncPool(searchWords.getPoolId());
		searchWords.setStatus(3);

		//已发布的数据更新缓存数据
		String result = cacheDate(searchWords);

		searchWords.setUpdateId(UserUtil.getUserId());
		searchWordsDao.update(searchWords);
		logSerice.sendLogger("【发布】:"+JSONObject.toJSONString(searchWords), "searchWords", searchWords.getId());
		return new ReturnT<String>(200,"操作成功");
	}


	public String cacheDate(SearchWords searchWords){
		String key = SEARCH_KEYWORDS_KEY + MD5Util.getMD5(searchWords.getKeywords());
		YxPool pool = poolService.queryPoolAndPeriodById(searchWords.getPoolId());
		List<YxPoolPeriods> ppList = pool.getYppList();
		Map<String, JSONArray> storeMap = new HashMap<String, JSONArray>();
		for(YxPoolPeriods pp: ppList){
			Date beginTime= pp.getBeginTime();
			List<YxPoolPeriodsStore> stroeList =  cprService.queryStoreListByPeriodsId(pp.getId());
			for(YxPoolPeriodsStore store : stroeList){
				String storeCode = store.getStoreCode();
				List<YxPoolCommodity> commoditys = commodityService.getCommodityByIds(store.getCommoditys());
				if(commoditys==null){
					continue;
				}
				JSONArray arrayList = new JSONArray();
				int index = 0;
				for(YxPoolCommodity commodity: commoditys){
					if(index >= 10){
						break;
					}
					if(commodity.getOriginate() == YxPoolConst.YX_COMMODITY_TYPE_PIC && "firstlevellist".equals(commodity.getUrlType())){
						JSONObject obj = new JSONObject();
						String urlData = commodity.getUrlProperties();
						if(StringUtils.isNotBlank(urlData) && urlData.startsWith("{")){
							JSONObject cateJson = JSONObject.parseObject(urlData);
							if(null == cateJson){
								continue;
							}
							String single = null;
							if(cateJson.containsKey(storeCode)){
								single = cateJson.getString(storeCode);
							}else{
								String areaCode = this.getStoreSeqByCode(storeCode);
								if(cateJson.containsKey(areaCode)){  //根据区域返回类目
									single = cateJson.getString(areaCode);
								}
							}
							JSONObject singleObj = null;
							if(null == single){
								singleObj = cateJson;
							}else{
								singleObj = JSONObject.parseObject(single);
							}
							//过滤7自定义夹子  9：行销夹子
							if(singleObj.containsKey("categoryType") && StringUtils.isNotBlank(singleObj.getString("categoryType"))){
								if(singleObj.getIntValue("categoryType")!=7 && singleObj.getIntValue("categoryType")!=9){
									obj.put("imgUrl",commodity.getPicUrl());
									obj.put("title",commodity.getTitle());
									obj.put("seq",singleObj.getString("categorySeq"));
									obj.put("siSeq",singleObj.getString("categorySiSeq"));
									arrayList.add(obj);
									index++;
								}
							}
						}
					}
				}

				if(index>=6){
					JSONObject storeObj = new JSONObject();
					JSONArray periodArray = storeMap.get(storeCode);
					if(periodArray==null){
						periodArray = new JSONArray();
					}
					storeObj.put("beginTime", DateUtil.getDate(beginTime,"yyyy-MM-dd HH:mm:ss"));
					storeObj.put("dataArray",JSONObject.toJSONString(arrayList));
					periodArray.add(storeObj);
					storeMap.put(storeCode,periodArray);
				}
			}
		}
		for(Map.Entry<String, JSONArray> entry : storeMap.entrySet()){
			String storeCode = entry.getKey();
			JSONArray arrayList = entry.getValue();
			//cacheClient.hset(key, storeCode, JSONObject.toJSONString(arrayList));
			yXCacheSyncService.syncCache("hset",key,JSONObject.toJSONString(arrayList),storeCode);
		}

		return "OK";
	}


	public String getStoreSeqByCode(String code) {
		String seq = null;
		String cache = cacheClient.get(YX_STORE_CACHE);
		if (cache == null || "{}".equals(cache)) {
			List<YXStore> list = storeService.getYXStoreList();
			JSONObject jo = new JSONObject();
			for (YXStore s : list) {
				if (s.getCode().equals(code)) {
					seq = s.getPgSeq();
				}
				jo.put(s.getCode(), s.getPgSeq());
			}
			cacheClient.put(YX_STORE_CACHE, 300, jo.toJSONString());
		} else {
			JSONObject jo = JSON.parseObject(cache);
			seq = jo.getString(code);
		}
		return seq;
	}

}
