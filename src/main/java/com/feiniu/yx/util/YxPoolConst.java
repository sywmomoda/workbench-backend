package com.feiniu.yx.util;

import com.feiniu.yx.config.SystemEnv;

public class YxPoolConst {
	//优鲜池商品类型
	/**
	 * 商品
	 */
	public static final int YX_COMMODITY_TYPE_COMMODITY =1;
	/**
	 * 无线素材
	 */
	public static final int YX_COMMODITY_TYPE_PIC =2;
	/**
	 * 无线文字链
	 */
	public static final int YX_COMMODITY_TYPE_TEXT =3;

	
	/**
	 * 前台展示池中数据类型，商品 1
	 */
	public static final String COMMODITY_TYPE_TOSHOW_COMMODITY ="commodity";
	/**
	 * 前台展示池中数据类型，素材 2
	 */
	public static final String COMMODITY_TYPE_TOSHOW_PIC ="pic";
	/**
	 * 前台展示池中数据类型，文字链 3
	 */
	public static final String COMMODITY_TYPE_TOSHOW_TEXT ="text";

	/**
	 * 前台展示池中数据类型，全部1,2,3
	 */
	public static final String COMMODITY_TYPE_TOSHOW_ALLINONE ="all";
	
	/**
	 * 前台展示池中数据类型商品+素材1,2
	 */
	public static final String COMMODITY_TYPE_TOSHOW_COMMODITYPIC ="commoditypic";
	
	/**
	 * 前台展示池中数据类型商品+素材1,3
	 */
	public static final String COMMODITY_TYPE_TOSHOW_COMMODITYTEXT ="commoditytext";
	
	/**
	 * 前台展示池中数据类型图片+素材2,3
	 */
	public static final String COMMODITY_TYPE_TOSHOW_PICTEXT ="pictext";
	
	/**
	 * b2b商品基础信息接口前缀
	 */
	public static final String B2B_COMMODITY_HOST_DOMAIN = SystemEnv.getProperty("b2bcommodity.domain");
}
