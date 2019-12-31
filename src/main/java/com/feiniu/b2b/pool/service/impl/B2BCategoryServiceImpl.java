package com.feiniu.b2b.pool.service.impl;

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
import com.feiniu.b2b.pool.dao.B2BCategoryDao;
import com.feiniu.b2b.pool.entity.B2BCategory;
import com.feiniu.b2b.pool.service.B2BCategoryService;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.HttpTookit;
import com.fn.cache.client.RedisCacheClient;

import scala.actors.threadpool.Arrays;

@Service
public class B2BCategoryServiceImpl implements B2BCategoryService{
	
	
    public static final Logger     LOGGER    = LoggerFactory.getLogger(B2BCategoryServiceImpl.class);
    
    private final static String URL = SystemEnv.getProperty("yxStoreCategory");
    
    public static final String COMMODITY_CATEGORY_KEY = "COMMODITY_CATEGORY_KEY";
	
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    
    @Autowired
    private B2BCategoryDao b2bCategoryDao;
    
    @Autowired 
    private RedisCacheClient  cacheClient;
    
    public  void sync(String areaSeq){
    	try {
			syncData(areaSeq);
		} catch (Exception e) {
			LOGGER.debug(e.getMessage());
		} 
    }
    
    @SuppressWarnings("unchecked")
	private synchronized void syncData(String areaSeq) throws IOException, InterruptedException {
    	final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(1000);
        StartWorkOfInsertCategory(queue);
        Set<String> storeCodeSet = new HashSet<String>();
        /*final Map<String, YXStore> stroeMapping = storeService.getYXStoreMap();
        if("0".equals(storeCode)){
        	storeCodeSet = stroeMapping.keySet();
        }else{
        	storeCodeSet.add(storeCode);
        }*/
        storeCodeSet.addAll(Arrays.asList(areaSeq.split(",")));
       

        final ArrayBlockingQueue<Boolean> signQueue = new ArrayBlockingQueue<>(storeCodeSet.size());
        for (String key : storeCodeSet) {
        	//YXStore store = stroeMapping.get(key);
        	//String storeName = store.getName();
        	LOGGER.info(key + " delete data start");
            clearTable(key);
            LOGGER.info(key + " delete data end!");
            LOGGER.info(key + " copy data start");
            threadPoolTaskExecutor.execute(new Thread(new fetchRemoteCategoryWorker(key, "", queue, signQueue)));
            //Thread.currentThread().sleep(1000);
        }
        // 等待子线程结束并插入endFlag
        int storeCount = 0;
        while (signQueue.take()) {
        	storeCount += 1;
            if (storeCount == storeCodeSet.size()) {
                queue.put("end");
                break;
            }
        }
    }

    private void clearTable(String areaSeq) {
    	String code = areaSeq.equals("0") ? "" : areaSeq;
    	b2bCategoryDao.clearAll(code);
    }

    private void StartWorkOfInsertCategory(final ArrayBlockingQueue<Object> queue) {
        Runnable batchInsertCategoryThread = new Runnable(){
            @Override
            public void run() {
                Object element = null;
                int count = 0;
                ArrayList<B2BCategory> buff = new ArrayList<>(100);
                try {
                    while (true) {
                        element = queue.take();

                        if (element instanceof String) {// 到了队尾了
                            if (StringUtils.equals("end", element.toString()) && buff.size() > 0) {
                            	b2bCategoryDao.batchInsert(buff);	
                                buff.clear();
                                break;
                            }
                        }
                        if (element instanceof B2BCategory) {
                            buff.add((B2BCategory) element);
                            if (buff.size() >= 100) {
                                count += buff.size();
                                b2bCategoryDao.batchInsert(buff);
                                buff.clear();
                                LOGGER.info("over " + count + "条，queue " + queue.size() + "条");
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    releaseLock();
                }
                LOGGER.info(count + " copy data over!");
            }
        };
        threadPoolTaskExecutor.execute(batchInsertCategoryThread);

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
    private List<B2BCategory> fetchRemoteCategoryBy(String parentCateogryCode, String areaSeq, ArrayBlockingQueue<Object> queue) throws IOException {
    	  List<B2BCategory> resultList = new ArrayList<B2BCategory>();
          Map<String, String> args = new HashMap<String, String>();
          JSONObject parameters = new JSONObject();
           parameters.put("pgSeq", areaSeq);
           parameters.put("envType", 4);
          args.put("data", parameters.toJSONString());

          String responseText = null;
          responseText = HttpTookit.doPost(URL, args);

          if (StringUtils.isEmpty(responseText)) {
              return resultList;
          }
          JSONObject responseJSON = JSONObject.parseObject(responseText);
          JSONArray responseBody = responseJSON.getJSONArray("data");
          String status = responseJSON.getString("success");
         
          if (!StringUtils.equals("1", status)) {
              return resultList;
          }
          resultList =  setB2BCategory(responseBody,queue,"0",areaSeq);
          return resultList;

    }

    class fetchRemoteCategoryWorker implements Runnable{
        private String                      storeName;
        private String                      storeCode;
        private ArrayBlockingQueue<Object>  queue;
        private ArrayBlockingQueue<Boolean> signQueue;

        public fetchRemoteCategoryWorker (String storeCode , String storeName , ArrayBlockingQueue<Object> queue , ArrayBlockingQueue<Boolean> signQueue ) {
            super();
            this.storeCode = storeCode;
            this.storeName = storeName;
            this.queue = queue;
            this.signQueue = signQueue;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                LOGGER.info(storeName + " data thread start");
                fetchRemoteCategoryBy("0", storeCode, queue);
                String cacheResult = cacheClient.get(COMMODITY_CATEGORY_KEY);
                Map<String, Object> result = (Map<String, Object>) JSONObject.parse(cacheResult);
                if (result != null) {
                    result.remove(storeCode);
                    cacheClient.put(COMMODITY_CATEGORY_KEY, 60*60,JSONObject.toJSONString(result));
                }

                LOGGER.info(storeName + " data thread end");
            } catch (IOException e) {
                LOGGER.error(storeName + " data thread error", e);
            } finally {
                try {
                    signQueue.put(true);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static String SYNCHRONIZE_LOCK_KEY = "SYNCHRONIZE_LOCK_KEY_B2BCATE";

    public boolean getIsLock() {
        return  cacheClient.get(SYNCHRONIZE_LOCK_KEY) != null ;
    }

    public void lock() {
        LOGGER.info("get lock!");
       cacheClient.put(SYNCHRONIZE_LOCK_KEY,"true");;
    }

    public void releaseLock() {
        LOGGER.info("release lock!");
        cacheClient.remove(SYNCHRONIZE_LOCK_KEY);;
    }
    
    /**
     * 设置类目
     * @param responseBody
     * @param queue
     * @return
     */
    private List<B2BCategory> setB2BCategory(JSONArray responseBody, ArrayBlockingQueue<Object> queue,String parentSeq,String storeCode){
    	 
    	List<B2BCategory> cateList = new ArrayList<B2BCategory>();
    	JSONObject loop = null;
        for (int i = 0; i < responseBody.size(); i++) {
        	B2BCategory category = new B2BCategory();
        	String singleData = responseBody.getString(i);
        	loop = JSONObject.parseObject(singleData);
        	category.setSeq(loop.getString("gcSeq"));
            category.setName(loop.getString("showName"));
           // String siPseq =loop.getString("siPseq");
           // siPseq= StringUtils.isBlank(siPseq) ? "0" : siPseq;
            category.setParentSeq(parentSeq);
            String parentList = null;
            if(loop.get("gpList") !=null){
            	parentList = loop.getString("gpList");
            }else{
            	parentList = loop.getString("parentList");
            }
            category.setStoreCode(storeCode);
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
            	setB2BCategory(childrenList,queue,category.getSeq(),storeCode);
            }
        }
    	   
        return cateList;
    }

    public List<Map<String, Object>> getLocalCategory(String parentCateogryCode,String storeCode) {
    	B2BCategory b2bCategory = new B2BCategory();
    	b2bCategory.setParentSeq(parentCateogryCode);
    	b2bCategory.setStoreCode(storeCode);
        List<B2BCategory> children = b2bCategoryDao.queryB2BCategorys(b2bCategory);
        List<Map<String, Object>> result = new ArrayList<>(children.size());

        Map<String, Object> item = null;
        int childrenCount = 0;
        for (B2BCategory loop : children) {
            item = new HashMap<String, Object>();
            
            
            childrenCount = b2bCategoryDao.queryB2BCategoryCount(loop.getSeq());
            item.put("state", childrenCount > 0 ? "closed" : "open");
            
            JSONObject object = new JSONObject();
            object.put("siseq", loop.getSiSeq());
            object.put("cateseq", loop.getParentSeqList());
            object.put("level", loop.getLevel());
            object.put("siStatus", loop.getSiStatus());
            object.put("type", loop.getType());
            item.put("text", loop.getName());
            item.put("name", object.toJSONString());
            item.put("id", loop.getSeq());
            result.add(item);
        }
        return result;

    }

	@Override
	public List<Map<String, Object>> getLocalCategoryTree(String areaCode,
			String checkedCodes, Integer level) {
		B2BCategory B2BCategory = new B2BCategory();
		B2BCategory.setParentSeq("");
    	//B2BCategory.setLevel(level);
    	B2BCategory.setStoreCode(areaCode);
        List<B2BCategory> categoryList = b2bCategoryDao.queryB2BCategorys(B2BCategory);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        Map<String, Object> item = null;
        Map<String, Map<String, Object>> parentMap = new HashMap<String, Map<String, Object>>();
        for (B2BCategory loop : categoryList) {
        	if(loop.getLevel()==1){
        		item = new HashMap<String, Object>();
                item.put("state", "open");
                if(checkedCodes.indexOf(loop.getSeq())!=-1){
        			item.put("checked", "checked");
        		}
                JSONObject object = new JSONObject();
                object.put("siseq", loop.getSiSeq());
                object.put("cateseq", loop.getParentSeqList());
                object.put("level", loop.getLevel());
                object.put("siStatus", loop.getSiStatus());
                object.put("type", loop.getType());
                item.put("children", new ArrayList<Map<String, Object>>());
                item.put("text", loop.getName());
                item.put("name", object.toJSONString());
                item.put("id", loop.getSeq());
                parentMap.put(loop.getSeq(), item);
                result.add(item);
        	}
        }
        for (B2BCategory loop : categoryList) {
            String parentSeq = loop.getParentSeq();
            if(parentMap.get(parentSeq)!=null){
            	item = new HashMap<String, Object>();
        		if(checkedCodes.indexOf(loop.getSeq())!=-1){
        			item.put("checked", "checked");
        		}
                item.put("state", "open");
                JSONObject object = new JSONObject();
                object.put("siseq", loop.getSiSeq());
                object.put("cateseq", loop.getParentSeqList());
                object.put("level", loop.getLevel());
                object.put("siStatus", loop.getSiStatus());
                object.put("type", loop.getType());
                item.put("text", loop.getName());
                item.put("name", object.toJSONString());
                item.put("id", loop.getSeq());
            	Map<String, Object> parentItem = parentMap.get(parentSeq);
            	List<Map<String, Object>> childrenList = (List<Map<String, Object>>) parentItem.get("children");
            	childrenList.add(item);
            }
        }
        
        return result;
	}

}
