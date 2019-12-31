package com.feiniu.b2b.pool.entity;

import java.util.Date;
import java.util.Map;

import com.feiniu.yx.util.YXBaseEntity;

public class B2BPoolCommodity extends YXBaseEntity {

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
	private Long periodId = 0L;
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
	
	private boolean exist =true; //true: 存在 ，false: 不存在
			
	private String storeNames;
	
	private String message;

	private String discount;
	
	private Integer originate;
	
	private String urlProperties="";
	
	private Integer classId =0;
	
	private Integer order =0;
	
	private String storeCodes ="";
	
	private String storeCode ="";//添加商品的时候存放商品的销售门店
	
	private String storeName="";
	
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
	//无线文字链是否标红
	private String remark= "0";
	
	//图片领券信息
	private Integer activityType = 0;
	
	private Integer minQuantity=0; //最小起订量
	
	private Integer bargaiIs;  // 是否特价1:是0:否
	
	private Integer saleType; // 商品售卖方式：1:最小起订量 2:成倍购买,默认0
	
	private Integer promGrade;  //促销等级0~9,0:当前不促销
	
	private String boxSpec; //箱规
	
	private Integer limitNum; //限购
	
	public String getBoxSpec() {
		return boxSpec;
	}

	public void setBoxSpec(String boxSpec) {
		this.boxSpec = boxSpec;
	}

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

	public B2BPoolCommodity(){
		
	}
	
	public boolean equals(Object obj) {   
        if (obj instanceof B2BPoolCommodity) {   
        	B2BPoolCommodity u = (B2BPoolCommodity) obj;   
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

	
	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
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

	public Integer getBargaiIs() {
		return bargaiIs;
	}

	public void setBargaiIs(Integer bargaiIs) {
		this.bargaiIs = bargaiIs;
	}

	public Integer getSaleType() {
		return saleType;
	}

	public void setSaleType(Integer saleType) {
		this.saleType = saleType;
	}

	public Integer getPromGrade() {
		return promGrade;
	}

	public void setPromGrade(Integer promGrade) {
		this.promGrade = promGrade;
	}

	public Long getPeriodId() {
		return periodId;
	}

	public void setPeriodId(Long periodId) {
		this.periodId = periodId;
	}

	public Integer getLimitNum() {
		return limitNum;
	}

	public void setLimitNum(Integer limitNum) {
		this.limitNum = limitNum;
	}

}
