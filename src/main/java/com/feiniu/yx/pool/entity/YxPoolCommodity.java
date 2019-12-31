package com.feiniu.yx.pool.entity;

import java.util.Date;
import java.util.Map;

import com.feiniu.yx.util.YXBaseEntity;

public class YxPoolCommodity extends YXBaseEntity {

	private Long id;
	// 商品ID
	private Long poolId;
	
	private String commodityId = "";
	// 商品名称
	private String title = "";
	// 商品图片地址
	private String picUrl = "";
	// 商品翻转图片地址
	private String picTurnUrl = "";
	// 市场价
	private Float marketPrice =0.0f;
	// 飞牛价
	private Float price = 0f;
	// 期数,考虑到一个商品可以在属于多个期数，增加关联表或设为组合PK
	private Long periods= 0L;
	// 商品url
	private String urlType ="0";
	// 商品自定义url 
	private String customUrl = "";
	// 促销语
	private String promoteText = "";
	
	private Long stockSum= 0L;

	private Date updateTime;

	private String updateId = "";
	
	private Date createTime;

	private String createId = "";
	
	private boolean isSSM;
	
	private boolean isDPTJ = false; //单品特价
	
	private Integer dptjCard;
	
	private String dptjScript; //新人专享角标
	
	private boolean exist =true; //true: 存在 ，false: 不存在
			
	private String storeNames;
	
	private String message;

	private String discount;
	
	private Integer originate; //1:商品 2:图片 3:文件
	
	private String urlProperties="";
	
	private Integer classId =0;//0：默认添加  2：接口插入 
	
	private Integer order =0;
	
	private String storeCodes ="";
	
	private String storeCode ="";//添加商品的时候存放商品的销售门店
	
	private String groupIds;
	
	private String groupNames;
	//品牌名称
	private String brandName ="";
	//组合商品子商品原生卖场ID	
	private String childMcId ="";
	
	//图片领券信息
	private String couponProperties = "";
	/**
	*couponId(string):优惠券ID,
	*couponType(string):优惠券来源，mallcoupon-商城; coupon-自营
	*reductionPrice(string): 优惠券金额,
	*couponDoorSill (string): 优惠券门槛
	*/
	 //领券信息Map
	private Map<String,String> couponPropertiesMap;
		
	private String description="";
	private String sellPoint= "";
	
	private String unit= "";
	//无线文字链是否标红 ,商品用于存储RT货号
	private String remark= "0";
	//是否有服务
	private String hasService= "0";
	
	//图片领券信息
	private Integer activityType = 0;

	
	private Integer minQuantity=0;
	
	//限购数量，0：不限购
	private Integer limitQty = 0;
	
	private String rtNo;
	
	//specWeight
    private String specWeight;
    
    private Integer priceType = 0;//促销级别
    private Date addOnDate;//上新时间
    
    private Integer isNew = 0; //是否为新品
    
    private String scriptTitle; //角标文字
 	
    private String[] propertyTitle = new String[]{}; //属性打标
    //品牌id
    private String brandSeq;
    
    //线上专享
    private String onlineOwnTitle;
    
    //商品券打标
    private Map<String,String> commodityCouponMap;

    /**
     * 0-无;1-有
     * 冷藏打标
     */
    private Integer isClod = 0;
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	public Long getPoolId() {
		return poolId;
	}

	public void setPoolId(Long poolId) {
		this.poolId = poolId;
	}

	public String getCommodityId() {
		return commodityId;
	}

	public void setCommodityId(String commodityId) {
		this.commodityId = commodityId;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public Float getMarketPrice() {
		return marketPrice;
	}

	public void setMarketPrice(Float marketPrice) {
		this.marketPrice = marketPrice;
	}
	
	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public Long getPeriods() {
		return periods;
	}

	public void setPeriods(Long periods) {
		this.periods = periods;
	}
	
	public String getCustomUrl() {
		return customUrl;
	}

	public void setCustomUrl(String customUrl) {
		this.customUrl = customUrl;
	}

	public String getPromoteText() {
		return promoteText;
	}
	
	public void setPromoteText(String promoteText) {
		this.promoteText = promoteText;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getUrlType() {
		return urlType;
	}

	public void setUrlType(String urlType) {
		this.urlType = urlType;
	}

	public String getUpdateId() {
		return updateId;
	}
			
	public String getCreateId() {
		return createId;
	}

	public void setCreateId(String createId) {
		this.createId = createId;
	}

	public void setUpdateId(String updateId) {
		this.updateId = updateId;
	}

	public Long getStockSum() {
		return stockSum;
	}

	public void setStockSum(Long stockSum) {
		this.stockSum = stockSum;
	}


	public boolean getIsSSM() {
		return isSSM;
	}

	public void setIsSSM(boolean isSSM) {
		this.isSSM = isSSM;
	}

	public boolean isExist() {
		return exist;
	}

	public void setExist(boolean exist) {
		this.exist = exist;
	}
	

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPicTurnUrl() {
		return picTurnUrl;
	}

	public void setPicTurnUrl(String picTurnUrl) {
		this.picTurnUrl = picTurnUrl;
	}

	public YxPoolCommodity(){
		
	}
	
	@Override
    public boolean equals(Object obj) {
        if (obj instanceof YxPoolCommodity) {   
        	YxPoolCommodity u = (YxPoolCommodity) obj;   
            return this.commodityId.equals(u.commodityId);  
        }   
        return super.equals(obj);
	}

	public String getDiscount() {
		return discount;
	}

	public void setDiscount(String discount) {
		this.discount = discount;
	}
	
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getSellPoint() {
		return sellPoint;
	}

	public void setSellPoint(String sellPoint) {
		this.sellPoint = sellPoint;
	}

	public Integer getOriginate() {
		return originate;
	}

	public void setOriginate(Integer originate) {
		this.originate = originate;
	}

	public String getUrlProperties() {
		return urlProperties;
	}

	public void setUrlProperties(String urlProperties) {
		this.urlProperties = urlProperties;
	}

	public Integer getClassId() {
		return classId;
	}

	public void setClassId(Integer classId) {
		this.classId = classId;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getChildMcId() {
		return childMcId;
	}

	public void setChildMcId(String childMcId) {
		this.childMcId = childMcId;
	}


	public String getCouponProperties() {
		return couponProperties;
	}

	public void setCouponProperties(String couponProperties) {
		this.couponProperties = couponProperties;
	}

	public Map<String, String> getCouponPropertiesMap() {
		return couponPropertiesMap;
	}

	public void setCouponPropertiesMap(Map<String, String> couponPropertiesMap) {
		this.couponPropertiesMap = couponPropertiesMap;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStoreCodes() {
		return storeCodes;
	}

	public void setStoreCodes(String storeCodes) {
		this.storeCodes = storeCodes;
	}

	public String getStoreNames() {
		return storeNames;
	}

	public void setStoreNames(String storeNames) {
		this.storeNames = storeNames;
	}

	public String getStoreCode() {
		return storeCode;
	}

	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public String getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(String groupIds) {
		this.groupIds = groupIds;
	}

	public String getGroupNames() {
		return groupNames;
	}

	public void setGroupNames(String groupNames) {
		this.groupNames = groupNames;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getHasService() {
		return hasService;
	}

	public void setHasService(String hasService) {
		this.hasService = hasService;
	}

	public Integer getActivityType() {
		return activityType;
	}

	public void setActivityType(Integer activityType) {
		this.activityType = activityType;
	}

	public Integer getMinQuantity() {
		return minQuantity;
	}

	public void setMinQuantity(Integer minQuantity) {
		this.minQuantity = minQuantity;
	}

	public Integer getLimitQty() {
		return limitQty;
	}

	public void setLimitQty(Integer limitQty) {
		this.limitQty = limitQty;
	}

	public String getRtNo() {
		return rtNo;
	}

	public void setRtNo(String rtNo) {
		this.rtNo = rtNo;
	}

	public String getSpecWeight() {
		return specWeight;
	}

	public void setSpecWeight(String specWeight) {
		this.specWeight = specWeight;
	}

	public Integer getPriceType() {
		return priceType;
	}

	public void setPriceType(Integer priceType) {
		this.priceType = priceType;
	}

	public Date getAddOnDate() {
		return addOnDate;
	}

	public void setAddOnDate(Date addOnDate) {
		this.addOnDate = addOnDate;
	}

	public Integer getIsNew() {
		return isNew;
	}

	public void setIsNew(Integer isNew) {
		this.isNew = isNew;
	}

	public String getScriptTitle() {
		return scriptTitle;
	}

	public void setScriptTitle(String scriptTitle) {
		this.scriptTitle = scriptTitle;
	}

	public String[] getPropertyTitle() {
		return propertyTitle;
	}

	public void setPropertyTitle(String[] propertyTitle) {
		this.propertyTitle = propertyTitle;
	}

	public Map<String, String> getCommodityCouponMap() {
		return commodityCouponMap;
	}

	public void setCommodityCouponMap(Map<String, String> commodityCouponMap) {
		this.commodityCouponMap = commodityCouponMap;
	}

	public String getBrandSeq() {
		return brandSeq;
	}

	public void setBrandSeq(String brandSeq) {
		this.brandSeq = brandSeq;
	}
    
	public boolean isDPTJ() {
		return isDPTJ;
	}

	public void setDPTJ(boolean isDPTJ) {
		this.isDPTJ = isDPTJ;
	}

	public Integer getDptjCard() {
		return dptjCard;
	}

	public void setDptjCard(Integer dptjCard) {
		this.dptjCard = dptjCard;
	}

	public String getDptjScript() {
		return dptjScript;
	}

	public void setDptjScript(String dptjScript) {
		this.dptjScript = dptjScript;
	}

	public String getOnlineOwnTitle() {
		return onlineOwnTitle;
	}

	public void setOnlineOwnTitle(String onlineOwnTitle) {
		this.onlineOwnTitle = onlineOwnTitle;
	}


    public Integer getClod() {
        return isClod;
    }

    public void setClod(Integer clod) {
        isClod = clod;
    }
}
