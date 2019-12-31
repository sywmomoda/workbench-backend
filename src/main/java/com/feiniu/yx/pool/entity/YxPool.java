package com.feiniu.yx.pool.entity;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.feiniu.yx.util.YXBaseEntity;

/***
 * @author lizhiyong
 * 优鲜池：可加自营商品、无线素材、无线文字链
 */
public class YxPool extends YXBaseEntity{

    private Long                    id;
    // 池名称
    private String                  name           = "";
    // 当前期数
    // 与cms_pool_periods表的number字段对应
    private Long                    currentPeriods = 0L;
    // 期数
    private int                     periods        = 0;
    // 库存容错
    private Integer                 stockFt         = 0;
    // 排序类型:1、录入顺序，2、价格，3、库存
    private Integer                 orderType      = 0;
    // 排序规则:1、从大到小，2、从小到大
    private Integer                 orderRule      = 0;

    private String                  administrator  = "";
    // 备注
    private String                  remark         = "";
    @JSONField (format="yyyy-MM-dd HH:mm:ss")  
    private Date                    createTime;
    @JSONField (format="yyyy-MM-dd HH:mm:ss")  
    private Date                    updateTime;

    private String                  createId       = "";

    private String                  updateId       = "";
    // 是否启用同款替换 0：否 1：是
    private Integer                 isReplace      = 0;
    
    private Integer type = 1; //1:优鲜池;2:B2B池;
    
    private List<YxPoolPeriods> yppList;
    
    private YxPoolPeriods yxPoolPeriods; //pool的当前期
    
    /**
	 * @return the unitePoolPeriods
	 */
	public YxPoolPeriods getYxPoolPeriods() {
		return yxPoolPeriods;
	}

	/**
	 * @param unitePoolPeriods the unitePoolPeriods to set
	 */
	public void setYxPoolPeriods(YxPoolPeriods yxPoolPeriods) {
		this.yxPoolPeriods = yxPoolPeriods;
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public Integer getOrderRule() {
        return orderRule;
    }

    public void setOrderRule(Integer orderRule) {
        this.orderRule = orderRule;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getCurrentPeriods() {
    	if (yxPoolPeriods != null) {
    		return yxPoolPeriods.getId();
    	}
        return currentPeriods;
    }

    public void setCurrentPeriods(Long currentPeriods) {
        this.currentPeriods = currentPeriods;
    }

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId;
    }

    public String getUpdateId() {
        return updateId;
    }

    public void setUpdateId(String updateId) {
        this.updateId = updateId;
    }
    
    public Integer getIsReplace() {
        return isReplace;
    }

    public void setIsReplace(Integer isReplace) {
        this.isReplace = isReplace;
    }
    
    public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getAdministrator() {
        return administrator;
    }

    public void setAdministrator(String administrator) {
        this.administrator = administrator;
    }

	public int getPeriods() {
		return periods;
	}

	public void setPeriods(int periods) {
		this.periods = periods;
	}
	public Integer getStockFt() {
		return stockFt;
	}

	public void setStockFt(Integer stockFt) {
		this.stockFt = stockFt;
	}

	public List<YxPoolPeriods> getYppList() {
		return yppList;
	}

	public void setYppList(List<YxPoolPeriods> cppList) {
		this.yppList = cppList;
	}
}
