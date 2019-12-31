package com.feiniu.yx.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.HttpTookit;

/**
 * 商品工具类
 * @author:tongwenhuan
 * @time:2018年11月1日 下午1:56:39
 */
public class GoodsUtils {
	
	private static Logger logger = Logger.getLogger(GoodsUtils.class);
	
	//商品基础信息
	private static String COMMDOITY_INFO_API = SystemEnv.getProperty("yxCommodity.url") + "rest/cms/goodsInfo";
	//商品取价格库存接口
	private static String PRICE_URL = SystemEnv.getProperty("yxCommodity.url") + "rest/cms/smQty";

    private static String COLD_CAGEGORY_URL= SystemEnv.getProperty("yxCommodity.category.host") + "/rest/category/batchGetAvNameAndAtNameBySeq";
	private static final int total = 100;
	
	/**
	 * 查询商品基本信息
	 * @param ids
	 * @return
	 * {
			"brandSeq": "427565",
			"goodsNo": "P3117050000013383",
			"picUrl": "/pic/b696133bd5191b9355d8/kTq2nn7nDzLdKMZgfT/1YmGoyZRVYOac9/CsmRr1ez2i2AQ7uMAAGAja6bvoM446.jpg",
			"sellPt": "无添加防腐剂及人工色素，活性动态锁鲜技术",
			"title": "三全儿童虾仁玉米馄饨210克/盒"
		}
	 */
	public static Map<String, JSONObject> getMapByInfoAPI(String[] ids) {
		Map<String, JSONObject> m = new HashMap<String, JSONObject>();
		if(ids == null || ids.length <= 0) {
			return m;
		}
		List<String[]> list = splitIds(ids);
		for (String[] ss : list) {
			Map<String, JSONObject> infos = getGoodsInfoByAPI(ss);
			if (infos != null && infos.size() > 0) {
				m.putAll(infos);
			}
		}
		return m;
	}
	
	/**
	 * 价格接口取数据
	 * @param ids
	 * @param storeCode
	 * @return
	 *  {
			"costPrice": 93,  成本价
			"goodsNo": "P2617080000019794", 商品编码
			"goodsType": 0, 0为标品 1为称重品，称重品单位由售卖量+售卖单位组成
			"isAbn": 0, 商品是否异常 0 正常
			"isNew": 0, 是否新品
			"isSale": 1, 是否该区域可卖 1 可卖
			"limitQty": 0, 是否限购
			"minQuantity": 0,
			"price": 93, 价格
			"priceType": 0, 0正常价，1促销价
			"saleLevel": 0, 促销等级
			"saleQty": 0, 库存 0没库存
			"saleSchedule": 0,
			"saleWay": 0, 0 为纯称重品，纯称重品需设置最小起订量和售卖单位
			"showStatus": 0, 商品是否显示  1显示
			"spec": "800g/盒",
			"specWeight": 0, 称重品每件的重量
			"status": 9, 商品状态 9 下架
			"storeCode": "1002", 门店编码
			"suNum": 1,
			"suUnit": "盒"
		}
	 */
	public static Map<String, JSONObject> getMapByPriceAPI(String[] ids, String storeCode) {
		Map<String, JSONObject> m = new HashMap<String, JSONObject>();
		if(ids == null || ids.length <= 0 || StringUtils.isBlank(storeCode)) {
			return m;
		}
		List<String[]> list = splitIds(ids);
		for (String[] ss : list) {
			Map<String, JSONObject> infos = getGoodsPriceByAPI(ss, storeCode);
			if (infos != null && infos.size() > 0) {
				m.putAll(infos);
			}
		}
		return m;
	}
	
	private static Map<String, JSONObject> getGoodsPriceByAPI(String[] ids, String storeCode) {
		JSONObject params = new JSONObject();
		params.put("goodsNo", ids);
		params.put("storeCode", storeCode);
		String result = null;
		try {
			result = HttpTookit.doPost(PRICE_URL,"data", params.toJSONString());
		}catch(Exception e){
	         logger.error("getMapByPriceAPI error", e);
		}
		JSONObject jo = JSON.parseObject(result);
		if (jo == null) {
			return null;
		}
		String success = jo.getString("success");
        if(!"1".equals(success)){
        	logger.error(jo.toJSONString());
            return null;
        }
        JSONArray arr = null;
        try {
        	arr = jo.getJSONArray("data");
        } catch(Exception e) {
        	
        }
        if (arr == null || arr.size() == 0) {
        	return null;
        }
        Map<String, JSONObject> m = new HashMap<String, JSONObject>();
        for (int i = 0; i < arr.size(); i++) {
        	JSONObject o = arr.getJSONObject(i);
        	String goodsNo = o.getString("goodsNo");
        	m.put(goodsNo, o);
        }
        return m;
	}
	
	private static Map<String, JSONObject> getGoodsInfoByAPI(String[] ids) {
		JSONObject params = new JSONObject();
		params.put("goodsNos", ids);
		String result = null;
		try {
			result = HttpTookit.doPost(COMMDOITY_INFO_API,"data", params.toJSONString());
		}catch(Exception e){
	         logger.error("getMapByInfoAPI error", e);
		}
		JSONObject jo = JSON.parseObject(result);
		if (jo == null) {
			return null;
		}
		String success = jo.getString("success");
        if(!"1".equals(success)){
            return null;
        }
        JSONArray arr = null;
        try {
        	arr = jo.getJSONArray("data");
        } catch(Exception e) {
        	
        }
        if (arr == null || arr.size() == 0) {
        	return null;
        }
        Map<String, JSONObject> m = new HashMap<String, JSONObject>();
        for (int i = 0; i < arr.size(); i++) {
        	JSONObject o = arr.getJSONObject(i);
        	String goodsNo = o.getString("goodsNo");
        	m.put(goodsNo, o);
        }
        return m;
	}
	
	/**
	 * 按照total拆分数组
	 * 先去掉原数组中的空字符
	 * @param ids
	 * @return
	 */
	private static List<String[]> splitIds(String[] ids){
		List<String[]> setIds = new ArrayList<String[]>();
		//先去掉数组中的空字符串
		List<String> newList = new ArrayList<String>();
		for (String id : ids) {
			if (StringUtils.isBlank(id)) {
				continue;
			}
			newList.add(id);
		}
		if (newList.size() == 0) {
			return setIds;
		}
		String[] newids = newList.toArray(new String[newList.size()]);
		int length = newList.size();
		int multiple = length / total ;
		int remainder = length % total;
		for(int j = 1; j <= multiple; j++){
			String[]  subIds = Arrays.copyOfRange(newids, total*(j-1), (j)*total);
			setIds.add(subIds);
		}
		if(remainder > 0){
			String[]  subIds = Arrays.copyOfRange(newids, total*multiple, total*multiple+remainder);
			setIds.add(subIds);
		}
		return setIds;
	}

    /**
     * "cpSeq:分类ID
     * atSeq:属性项ID
     * avSeq:属性值ID"
     * "{"cpSeq":"","seqList":[{"atSeq":"","avSeq":""}]}"
     *
     * 0-无;1-冷藏;2冷冻
     * @param param
     * @return
     */
	public static int getIsColdAttribute(JSONObject param){
            String result = null;
            try {
                result = HttpTookit.doPost(COLD_CAGEGORY_URL,"data", param.toJSONString());
            }catch(Exception e){
                logger.error("getIsColdAttribute error", e);
            }
            JSONObject jo = JSON.parseObject(result);
            if(null == jo){
                return 0;
            }
            String data = jo.getString("data");
            if(StringUtils.isBlank(data)){
                return 0;
            }
            String lcConst ="冷藏";
            String ldConst ="冷冻";
            if(data.contains(lcConst)){
              return 1;
            }
            if(data.contains(ldConst)){
                return 2;
            }
            return 0;
    }

}
