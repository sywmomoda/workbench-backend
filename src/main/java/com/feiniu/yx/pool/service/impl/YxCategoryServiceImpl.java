package com.feiniu.yx.pool.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.pool.dao.YxCategoryDao;
import com.feiniu.yx.pool.entity.YxCategory;
import com.feiniu.yx.pool.service.YxCategoryService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class YxCategoryServiceImpl implements YxCategoryService{
	
    public static final Logger     LOGGER    = LoggerFactory.getLogger(YxCategoryServiceImpl.class);
    
    @Autowired
    private YxCategoryDao yxCategoryDao;

    public List<Map<String, Object>> getLocalCategory(String parentCateogryCode,String storeCode) {
    	YxCategory yxCategory = new YxCategory();
    	yxCategory.setSiPseq(parentCateogryCode);
    	if(StringUtils.isNotBlank(parentCateogryCode)){
    		yxCategory.setParentSeq("");
    	}
    	yxCategory.setStoreCode(storeCode);
        List<YxCategory> children = yxCategoryDao.queryYxCategorys(yxCategory);
        List<Map<String, Object>> result = new ArrayList<>(children.size());

        Map<String, Object> item = null;
        int childrenCount = 0;
        for (YxCategory loop : children) {
            item = new HashMap<String, Object>();
            childrenCount = yxCategoryDao.queryYxCategoryCount(loop.getSeq());
            item.put("state", childrenCount > 0 ? "closed" : "open");
            JSONObject object = new JSONObject();
            object.put("siseq", loop.getSiSeq());
            object.put("seq", loop.getSeq());
            object.put("cateseq", loop.getParentSeqList());
            object.put("level", loop.getLevel());
            object.put("type", loop.getType());
            item.put("text", loop.getName());
            item.put("name", object.toJSONString());
            item.put("id", loop.getSiSeq());
            result.add(item);
        }
        return result;

    }

	@Override
	public List<Map<String, Object>> getLocalCategoryTree(String areaCode,
			String checkedCodes, Integer level) {
		YxCategory yxCategory = new YxCategory();
		yxCategory.setParentSeq("");
    	//yxCategory.setLevel(level);
    	yxCategory.setStoreCode(areaCode);
        List<YxCategory> categoryList = yxCategoryDao.queryYxCategorys(yxCategory);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        Map<String, Object> item = null;
        Map<String, Map<String, Object>> parentMap = new HashMap<String, Map<String, Object>>();
        Map<String, Map<String, Object>> parentLevel2Map = new HashMap<String, Map<String, Object>>();
        for (YxCategory loop : categoryList) {
        	if(loop.getLevel()==1){
        		item = new HashMap<String, Object>();
                item.put("state", "open");
                if(checkedCodes.indexOf(loop.getSeq())!=-1){
        			item.put("checked", "checked");
        		}
                JSONObject object = new JSONObject();
                object.put("siseq", loop.getSiSeq());
                object.put("cateseq", loop.getParentSeqList());
                object.put("seq", loop.getSeq());
                object.put("level", loop.getLevel());
                object.put("siStatus", loop.getSiStatus());
                object.put("type", loop.getType());
                item.put("children", new ArrayList<Map<String, Object>>());
                item.put("text", loop.getName());
                item.put("name", object.toJSONString());
                item.put("id", loop.getSeq());
                //parentMap.put(loop.getSeq(), item);
                parentMap.put(loop.getSiSeq(), item);
                result.add(item);
        	}
            if(loop.getLevel()==2 && level==3){
                item = new HashMap<String, Object>();
                item.put("state", "open");
                if(checkedCodes.indexOf(loop.getSeq())!=-1){
                    item.put("checked", "checked");
                }
                JSONObject object = new JSONObject();
                object.put("siseq", loop.getSiSeq());
                object.put("cateseq", loop.getParentSeqList());
                object.put("seq", loop.getSeq());
                object.put("level", loop.getLevel());
                object.put("siStatus", loop.getSiStatus());
                object.put("type", loop.getType());
                item.put("children", new ArrayList<Map<String, Object>>());
                item.put("text", loop.getName());
                item.put("name", object.toJSONString());
                item.put("id", loop.getSeq());
                //parentMap.put(loop.getSeq(), item);
                parentLevel2Map.put(loop.getSiSeq(), item);
            }
        }
        for (YxCategory loop : categoryList) {
            //String parentSeq = loop.getParentSeq();
            String parentSeq = loop.getSiPseq();
            if(loop.getLevel()==3){
                if(parentLevel2Map.get(parentSeq)!=null){
                    item = new HashMap<String, Object>();
                    if(checkedCodes.indexOf(loop.getSeq())!=-1){
                        item.put("checked", "checked");
                    }
                    item.put("state", "open");
                    JSONObject object = new JSONObject();
                    object.put("siseq", loop.getSiSeq());
                    object.put("cateseq", loop.getParentSeqList());
                    object.put("seq", loop.getSeq());
                    object.put("level", loop.getLevel());
                    object.put("siStatus", loop.getSiStatus());
                    object.put("type", loop.getType());
                    item.put("text", loop.getName());
                    item.put("name", object.toJSONString());
                    item.put("id", loop.getSeq());
                    Map<String, Object> parentItem = parentLevel2Map.get(parentSeq);
                    List<Map<String, Object>> childrenList = (List<Map<String, Object>>) parentItem.get("children");
                    childrenList.add(item);
                }
            }

        }


        for (YxCategory loop : categoryList) {
            //String parentSeq = loop.getParentSeq();
        	String parentSeq = loop.getSiPseq();
        	if(loop.getLevel()==2){
                if(parentMap.get(parentSeq)!=null){
                    item = new HashMap<String, Object>();
                    if(checkedCodes.indexOf(loop.getSeq())!=-1){
                        item.put("checked", "checked");
                    }
                    item.put("state", "open");
                    JSONObject object = new JSONObject();
                    object.put("siseq", loop.getSiSeq());
                    object.put("cateseq", loop.getParentSeqList());
                    object.put("seq", loop.getSeq());
                    object.put("level", loop.getLevel());
                    object.put("siStatus", loop.getSiStatus());
                    object.put("type", loop.getType());
                    item.put("text", loop.getName());
                    item.put("name", object.toJSONString());
                    item.put("id", loop.getSeq());
                    item.put("children",parentLevel2Map.get(loop.getSiSeq()).get("children"));
                    Map<String, Object> parentItem = parentMap.get(parentSeq);
                    List<Map<String, Object>> childrenList = (List<Map<String, Object>>) parentItem.get("children");
                    childrenList.add(item);
                }
            }

        }
        
        return result;
	}

}
