package com.feiniu.yx.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class MD5Util {
	
	public static final String COUPON_MD5_KEY = "cms_module_coupon_20190121_^@$";
	
	
	public static String getMD5(String str) {
		return DigestUtils.md5Hex(str);
	}


	public static boolean checkMD5(String md5, String str) {
		return StringUtils.isNotBlank(md5) && StringUtils.isNotBlank(str) && md5.equals(getMD5(str));
	}
}
