package com.feiniu.yx.page.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.YXConstant;
import com.feiniu.yx.common.service.YXCacheSyncService;
import com.feiniu.yx.page.dao.ModuleDao;
import com.feiniu.yx.page.dao.ModuleOnlineDao;
import com.feiniu.yx.page.dao.PageDao;
import com.feiniu.yx.page.dao.PageOnlineDao;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.ModuleMultiTabService;
import com.feiniu.yx.page.service.ModuleProperPlusService;
import com.feiniu.yx.page.service.PagePublishService;
import com.feiniu.yx.pool.service.SyncPoolService;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXTemplateService;
import com.feiniu.yx.util.HttpTookit;
import com.feiniu.yx.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tongwenhuan
 * 2017年2月28日 上午9:20:38
 */
@Service(value="pagePublishService")
public class PagePublishServiceImpl implements PagePublishService {

	@Autowired
	private PageDao pageDao;
	@Autowired
	private PageOnlineDao pageOnlineDao;

	@Autowired
	private ModuleDao moduleDao;
	@Autowired
	private ModuleOnlineDao moduleOnlineDao;

	@Autowired
	private YXTemplateService templateService;

	@Autowired
	private SyncPoolService syncPoolService;

	@Autowired
	private ModuleProperPlusService moduleProperPlusService;

	@Autowired
	private YXCacheSyncService yXCacheSyncService;

	@Autowired
    private ModuleMultiTabService moduleMultiTabService;

	//@Autowired
	//private RedisCacheClient cacheClient;
	private final static String SEARCH_ACTIVITY_KEYWORDS_KEY = "YX_SEARCH_ACTIVITY_KEYWORDS_";

	@Override
	public String publish(Long id) {
		Page p = pageDao.queryCMSPageByID(id);
		//onlinepage
		Page op = pageOnlineDao.findOne(id);
		//第一次发布，更改状态
		if(p.getStatus().intValue() == 2) {
			p.setStatus(1); //已发布
			pageDao.updatePageStatus(p);
		}
		//第一次发布
		if(op == null){
			pageOnlineDao.insertOne(p);
		}else {
			pageOnlineDao.updateOne(p);
		}

		String state = syncModule(p.getModules());
		Long templateId =p.getTemplateId();
		YXTemplate tt = templateService.getYXTemplateById(templateId);
		//优鲜首页不发布页面，只同步数据到online表
		if(tt!=null && !tt.getCode().equals("index")){
			HttpTookit.doGet(YXConstant.PUBLISH + p.getId());
		}

		state = keyWordsSearchDataInit(op,p);

		return state;
	}

	/**
	 * 数据缓存处理
	 * @param op onlinePage
	 * @param  p page
	 * @return
	 */
	private String keyWordsSearchDataInit(Page op, Page p) {
		if(op!=null && p.getSearchWords().equals(op.getSearchWords()) && p.getSearchAdPic().equals(op.getSearchAdPic()) && p.getActivityEndTime().getTime()==op.getActivityEndTime().getTime()){
			return "OK";
		}
		String[] wordsOld = op!=null ? op.getSearchWords().split(",") : new String[]{};
		String[] wordsNew = p.getSearchWords().split(",");

		for(String groupId: p.getXiaoQuIds().split(",")){
			for(String words:wordsOld){//删除旧缓存
				String key = SEARCH_ACTIVITY_KEYWORDS_KEY + MD5Util.getMD5(words) + "_" + groupId;
				//cacheClient.hdel(key, "ACT_" + p.getId());
				yXCacheSyncService.syncCache("hdel",key,"","ACT_" + p.getId());
			}
			for(String words:wordsNew){//更新新缓存
				String key = SEARCH_ACTIVITY_KEYWORDS_KEY + MD5Util.getMD5(words) + "_" + groupId;

				JSONObject obj = new JSONObject();
				obj.put("imgUrl",p.getSearchAdPic());
				obj.put("id",p.getId());
				obj.put("endDate",p.getActivityEndTime().getTime());

				//cacheClient.hset(key, "ACT_"+ p.getId(), JSONObject.toJSONString(obj));
				yXCacheSyncService.syncCache("hset", key, JSONObject.toJSONString(obj),"ACT_" + p.getId());
			}
		}
		return "OK";
	}


	private String syncModule(String moduleIds) {
		List<Module> moduleList = moduleDao.queryModulesByIds(moduleIds.split(","));
		for (Module module : moduleList) {
			if (module != null) {
				moduleOnlineDao.insertOrUpdateCMSModuleOnline(module);
				JSONObject proJo = JSONObject.parseObject(module.getModuleProperties());
				if(proJo.containsKey("poolId")){//同步池数据
					String poolId = proJo.get("poolId").toString();
					if(StringUtils.isNotBlank(poolId)){
						Long id = Long.parseLong(poolId);
						syncPoolService.syncPool(id);
					}
				}
				if(proJo.containsKey("rightSmallPoolId") && proJo.containsKey("bigImgPoolId") && proJo.containsKey("leftSmallPoolId")){
					ArrayList<JSONArray> list = new ArrayList<JSONArray>();
					JSONArray jsonArray = new JSONArray();
					if(proJo.containsKey("bigImgPoolId")){
						Object bgObj = proJo.get("bigImgPoolId");
						if(bgObj instanceof JSONArray){
							JSONArray bigImgPoolId = proJo.getJSONArray("bigImgPoolId");
							list.add(bigImgPoolId);
						}else {
							jsonArray.add(proJo.getString("bigImgPoolId"));
						}
					}
					if(proJo.containsKey("leftSmallPoolId")){
						Object bgObj = proJo.get("leftSmallPoolId");
						if(bgObj instanceof JSONArray){
							JSONArray leftSmallPoolId = proJo.getJSONArray("leftSmallPoolId");
							list.add(leftSmallPoolId);
						}else {
							jsonArray.add(proJo.getString("leftSmallPoolId"));
						}
					}
					if(proJo.containsKey("rightSmallPoolId")){
						Object bgObj = proJo.get("rightSmallPoolId");
						if(bgObj instanceof JSONArray){
							JSONArray rightSmallPoolId = proJo.getJSONArray("rightSmallPoolId");
							list.add(rightSmallPoolId);
						}else {
							jsonArray.add(proJo.getString("rightSmallPoolId"));
						}
					}
					list.add(jsonArray);
					syncPoolData(list);
				}
				if(proJo.containsKey("poolIds")){
					Object poolIds = proJo.get("poolIds");
					if(poolIds instanceof List){
						List<?> tPoolId = (List<?>) poolIds;
						if(tPoolId!=null&& tPoolId.size()>0){
							for(int i=0;i<tPoolId.size();i++){
								String poolId  = tPoolId.get(i).toString();
								if(!"remote".equals(poolId) && StringUtils.isNotBlank(poolId)){
									Long id = Long.parseLong(poolId);
									syncPoolService.syncPool(id);
								}
							}
						}
					}
				}

				syncCustomModule(proJo);

				moduleProperPlusService.syncModuleProperPlus(module.getId());
                syncModuleMultiTabPro(proJo);
			}
		}
		return "OK";
	}
	
	private void syncPoolData(ArrayList<JSONArray> list){
		for (JSONArray jsonArray : list) {
			for (int i = 0; i < jsonArray.size(); i++) {
				String poolId = jsonArray.get(i).toString();
				if(StringUtils.isNotBlank(poolId)){
					syncPoolService.syncPool(Long.valueOf(poolId));
				}
			}
		}
	}

    /**
     * 同步模板多tab属性
     * @param proJo
     */
	private void syncModuleMultiTabPro(JSONObject proJo){
	    String keyConst ="tabId";
        if(!proJo.containsKey(keyConst)){
	        return;
        }
        Object tabIdObject = proJo.get("tabId");
        if(null == tabIdObject){
            return;
        }
        String ids ="";
        if(tabIdObject instanceof  List){
            ids = StringUtils.join((JSONArray)tabIdObject,",");
        }else{
            ids = tabIdObject.toString();
        }
        if(StringUtils.isBlank(ids)){
            return;
        }
        moduleMultiTabService.syncModuleMultiTab(ids);
    }


	/**
	 * 特殊组件同步
	 * @param proJo
	 */
	private void syncCustomModule(JSONObject proJo) {
		if(proJo.containsKey("one")){//同步池数据
			JSONObject one = proJo.getJSONObject("one");
			syncCustomPool(one);
		}
		if(proJo.containsKey("two")){//同步池数据
			JSONObject one = proJo.getJSONObject("two");
			syncCustomPool(one);
		}
		if(proJo.containsKey("three")){//同步池数据
			JSONObject one = proJo.getJSONObject("three");
			syncCustomPool(one);
		}
		if(proJo.containsKey("four")){//同步池数据
			JSONObject one = proJo.getJSONObject("four");
			syncCustomPool(one);
		}

		if(proJo.containsKey("poolIdsAd")){
			Object poolIds = proJo.get("poolIdsAd");
			if(poolIds instanceof List){
				List<?> tPoolId = (List<?>) poolIds;
				if(tPoolId!=null&& tPoolId.size()>0){
					for(int i=0;i<tPoolId.size();i++){
						String poolId  = tPoolId.get(i).toString();
						if(!"remote".equals(poolId) && StringUtils.isNotBlank(poolId)){
							Long id = Long.parseLong(poolId);
							syncPoolService.syncPool(id);
						}
					}
				}
			}
		}

		if(proJo.containsKey("poolIdsTop")){
			Object poolIds = proJo.get("poolIdsTop");
			if(poolIds instanceof List){
				List<?> tPoolId = (List<?>) poolIds;
				if(tPoolId!=null&& tPoolId.size()>0){
					for(int i=0;i<tPoolId.size();i++){
						String poolId  = tPoolId.get(i).toString();
						if(!"remote".equals(poolId) && StringUtils.isNotBlank(poolId)){
							Long id = Long.parseLong(poolId);
							syncPoolService.syncPool(id);
						}
					}
				}
			}
		}

		if(proJo.containsKey("poolIdsData")){
			Object poolIds = proJo.get("poolIdsData");
			if(poolIds instanceof List){
				List<?> tPoolId = (List<?>) poolIds;
				if(tPoolId!=null&& tPoolId.size()>0){
					for(int i=0;i<tPoolId.size();i++){
						String poolId  = tPoolId.get(i).toString();
						if(!"remote".equals(poolId) && StringUtils.isNotBlank(poolId)){
							Long id = Long.parseLong(poolId);
							syncPoolService.syncPool(id);
						}
					}
				}
			}
		}

		String poolIdNames = "yxbannerPoolId,xjhbannerPoolId,yhjPoolId,poolIdBg,poolIdAd";
		String[] poolIds = poolIdNames.split(",");
		for(String poolIdName:poolIds){
			if(proJo.containsKey(poolIdName)){//同步池数据
				String poolId = proJo.get(poolIdName).toString();
				if(StringUtils.isNotBlank(poolId)){
					Long id = Long.parseLong(poolId);
					syncPoolService.syncPool(id);
				}
			}
		}

	}

	private void syncCustomPool(JSONObject object){
		String poolId = object.get("poolId").toString();
		if(StringUtils.isNotBlank(poolId)){
			Long id = Long.parseLong(poolId);
			syncPoolService.syncPool(id);
		}
	}

}
