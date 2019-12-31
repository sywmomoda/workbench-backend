package com.feiniu.yx.common.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.SyncCategoryService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.pool.dao.YxCategoryDao;
import com.feiniu.yx.pool.entity.YxCategory;
import com.feiniu.yx.util.HttpTookit;
import com.fn.cache.client.RedisCacheClient;

import scala.actors.threadpool.Arrays;

/**
 * 类目同步服务
 * @author:tongwenhuan
 * @time:2018年11月7日 上午9:30:32
 */
@Service
public class SyncCategoryServiceImpl implements SyncCategoryService {
	
	public static final Logger LOGGER = LoggerFactory.getLogger(SyncCategoryServiceImpl.class);
	
	private final static String SYNC_CATEGORY_URL = SystemEnv.getProperty("yxStoreCategory");
	private static final String SYNC_CATEGORY_LOCK_KEY = "SYNC_CATEGORY_LOCK_KEY";
	
	@Autowired
    private ThreadPoolTaskExecutor sysnCategoryThreadPoolTaskExecutor;
    
    @Autowired
    private YxCategoryDao yxCategoryDao;
	
	@Autowired 
	private RedisCacheClient  cacheClient;

	/**
	 * 同步类目
	 * @param groupIds 群组id
	 * CPG1,CPG2,CPG3,CPG4,CPG5,CPG6
	 */
	@Override
	public JSONObject SyncCategory(String groupIds) {
		//先判断是否有正在进行中的同步
		long time  = cacheClient.ttl(SYNC_CATEGORY_LOCK_KEY);
		if (time > 0) {
			return getReturnObj(false, "已有任务在执行，请等待！");
		}
		long i = cacheClient.incr(SYNC_CATEGORY_LOCK_KEY, 360);
		if (i == 1) {
			sync(groupIds);
		} else {
			return getReturnObj(false, "已有任务在执行，请稍后再试！");
		}
		return getReturnObj(true, "同步中，请稍候！");
	}
	
	private void sync(final String groupIds) {
		try {
			if (StringUtils.isBlank(groupIds)) {
				return;
			}
			sysnCategoryThreadPoolTaskExecutor.execute(new Runnable(){

				@Override
				public void run() {
					try {
						syncData(groupIds);
					} catch (Exception e) {
						LOGGER.error(e.getMessage(),e);
					}
				}
				
			});
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void syncData(String groupIds) throws IOException, InterruptedException {
		//门店code数组，用set去重
		Set<String> groupIdSet = new HashSet<String>();
		groupIdSet.addAll(Arrays.asList(groupIds.split(",")));
		if (groupIdSet.size() == 0) {
			return;
		}
    	final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(3000);
        StartWorkOfInsertCategory(queue);
        final ArrayBlockingQueue<Boolean> signQueue = new ArrayBlockingQueue<>(groupIdSet.size());
        for (String key : groupIdSet) {
        	LOGGER.info(key + " delete data start");
            clearTable(key);
            LOGGER.info(key + " delete data end!");
            sysnCategoryThreadPoolTaskExecutor.execute(new fetchRemoteCategoryWorker(key, queue, signQueue));
        }
        // 等待子线程结束并插入endFlag
        int storeCount = 0;
        while (signQueue.take()) {
        	storeCount += 1;
            if (storeCount == groupIdSet.size()) {
                queue.put("end");
                break;
            }
        }
    }
	
	private void StartWorkOfInsertCategory(final ArrayBlockingQueue<Object> queue) {
        Runnable batchInsertCategoryThread = new Runnable(){
            @Override
            public void run() {
                Object element = null;
                int count = 0;
                ArrayList<YxCategory> buff = new ArrayList<>(100);
                try {
                    while (true) {
                        element = queue.take();
                        // 到了队尾
                        if (element instanceof String) {
                            if (buff.size() > 0) {
                            	count += buff.size();
                                yxCategoryDao.batchInsert(buff);	
                                buff.clear();
                            }
                            break;
                        } else if (element instanceof YxCategory) {
                            buff.add((YxCategory) element);
                            if (buff.size() >= 100) {
                                count += buff.size();
                                yxCategoryDao.batchInsert(buff);
                                buff.clear();
                                LOGGER.info("over " + count + "条，queue " + queue.size() + "条");
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                LOGGER.info(count + " copy data over!");
            }
        };
        sysnCategoryThreadPoolTaskExecutor.execute(batchInsertCategoryThread);
    }
	
	class fetchRemoteCategoryWorker implements Runnable{
        private String                      groupId;
        private ArrayBlockingQueue<Object>  queue;
        private ArrayBlockingQueue<Boolean> signQueue;

        public fetchRemoteCategoryWorker (String groupId ,  ArrayBlockingQueue<Object> queue , ArrayBlockingQueue<Boolean> signQueue ) {
            super();
            this.groupId = groupId;
            this.queue = queue;
            this.signQueue = signQueue;
        }

        @Override
        public void run() {
            try {
                LOGGER.info(groupId + " data thread start");
                fetchRemoteCategoryBy("0", groupId, queue);
                LOGGER.info(groupId + " data thread end");
            } catch (IOException e) {
                LOGGER.error(groupId + " data thread error", e);
            } finally {
                try {
                    signQueue.put(true);
                } catch (InterruptedException e) {
                	 LOGGER.error(e.getMessage(),e);
                }
            }
        }

    }
	
	/**
     * 从远程获取某一地区的商品分类
     * 
     * @param parentCateogryCode
     * @param areaSeq
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private List<YxCategory> fetchRemoteCategoryBy(String parentCateogryCode, String groupId, ArrayBlockingQueue<Object> queue) throws IOException {
    	  List<YxCategory> resultList = new ArrayList<YxCategory>();
          Map<String, String> args = new HashMap<String, String>();
          JSONObject parameters = new JSONObject();
          parameters.put("pgSeq", groupId);
          parameters.put("envType", 3);
          args.put("data", parameters.toJSONString());
          String responseText = HttpTookit.doPost(SYNC_CATEGORY_URL, args);
          if (StringUtils.isEmpty(responseText)) {
              return resultList;
          }
          JSONObject responseJSON = JSONObject.parseObject(responseText);
          JSONArray responseBody = responseJSON.getJSONArray("data");
          String status = responseJSON.getString("success");
          if (!StringUtils.equals("1", status)) {
              return resultList;
          }
          resultList = setYxCategory(responseBody,queue,parentCateogryCode,groupId);
          return resultList;
    }
    
    /**
     * 设置类目
     * @param responseBody
     * @param queue
     * @return
     */
    private List<YxCategory> setYxCategory(JSONArray responseBody, ArrayBlockingQueue<Object> queue,String parentSeq,String groupId){
    	List<YxCategory> cateList = new ArrayList<YxCategory>();
    	JSONObject loop = null;
        for (int i = 0; i < responseBody.size(); i++) {
        	YxCategory category = new YxCategory();
        	String singleData = responseBody.getString(i);
        	loop = JSONObject.parseObject(singleData);
        	category.setSeq(loop.getString("gcSeq"));
            category.setName(loop.getString("showName"));
            category.setParentSeq(parentSeq);
            String parentList = null;
            if(loop.get("gpList") !=null){
            	parentList = loop.getString("gpList");
            }else{
            	parentList = loop.getString("parentList");
            }
            category.setStoreCode(groupId);
            category.setParentSeqList(parentList);
            category.setLevel(loop.getIntValue("siLevel"));
            category.setSiStatus(loop.getIntValue("siStatus"));
            category.setType(loop.getIntValue("siType"));
            category.setSiSeq(loop.getString("siSeq"));
            category.setSiPseq(loop.getString("siPseq"));
            String childrenJson = loop.getString("children");
            try {
                if (queue != null) {
                    queue.put(category);
                }
            } catch (InterruptedException e) {
            	
            }
            cateList.add(category);
            
            if(StringUtils.isBlank(childrenJson)){
            	continue;
            }
            
            JSONArray childrenList = JSONArray.parseArray(childrenJson);
            
            if(null != childrenList){
            	setYxCategory(childrenList,queue,category.getSeq(),groupId);
            }
        }
        return cateList;
    }
	
	private void clearTable(String groupId) {
    	if (StringUtils.isNotBlank(groupId) && groupId.startsWith("CPG")) {
    		yxCategoryDao.clearAll(groupId);
    	}
    }
	
	private JSONObject getReturnObj(boolean bl, String msg) {
		JSONObject r = new JSONObject();
		r.put("success", bl);
		r.put("msg", msg);
		return r;
	}

}
