package com.feiniu.b2b.pool.entity;

import java.util.Comparator;

@SuppressWarnings("rawtypes")
public class B2BPoolCommodityComparator implements Comparator{
	//排序类型 1：时间  2：价格(默认)
	private int orderType=2;
	//排序方向 1：desc(从大到小) 0:asc (从小(低)到大（高）)(默认)
	private int orderOrientation=0;

	public int compare(Object arg0, Object arg1) {
		B2BPoolCommodity commodity0=(B2BPoolCommodity)arg0;
		B2BPoolCommodity commodity1=(B2BPoolCommodity)arg1;
		 if(orderType==1){
			 if(orderOrientation==0){
				 return commodity0.getId().compareTo(commodity0.getId());
			 }else{
				 return commodity1.getId().compareTo(commodity0.getId());
			 }
			 
		 }else if(orderType==2){
			 Float price0 = commodity0.getPrice();
			 Float price1 = commodity1.getPrice();
			 if(orderOrientation==0){
				 return price0.compareTo(price1);
			 }else{
				 return price1.compareTo(price0);
			 }
			 
		 }else if(orderType == 3){
			 Long stock0 = commodity0.getStockSum(); 
			 Long stock1 = commodity1.getStockSum();
			 stock0 = stock0 > 0 ? 1l: 0;
			 stock1 = stock1 > 0 ? 1l: 0;
			 return stock1.compareTo(stock0);
		 }else{
			 return commodity0.getId().compareTo(commodity1.getId());
		 }
	}

	public int getOrderType() {
		return orderType;
	}

	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}

	public int getOrderOrientation() {
		return orderOrientation;
	}

	public void setOrderOrientation(int orderOrientation) {
		this.orderOrientation = orderOrientation;
	}
	 
}
