package com.feiniu.yx.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * 取登陆用户信息
 * @author tongwenhuan
 *
 */
public class UserUtil {
	
	private static final ThreadLocal<String> threadLocal = new ThreadLocal<String>();
	
	public static String getUserId() {
		String id = threadLocal.get();
		if (id == null) {
			return "";
		}
		return id;
	}

	public static void setUserId(String userName) {
		threadLocal.set(userName);
	}
	
	/**
	 * 判读是否管理员
	 * @param admins
	 * @return
	 */
	public static boolean authCheck(String admins) {
		String userName = getUserId();
		if (StringUtils.isBlank(userName)) return false;
		if (!(","+admins+",").contains(","+userName+",")) return false; 
		return true;
	}
	
	//取官微秒杀用户id
	public static String getGWMSUserId(HttpServletRequest request) { 
		return getUserId(request, "USR");
	}
	
	private static String getUserId(HttpServletRequest request, String key) {
		String v = "";
		Cookie[] cookies = request.getCookies();
		if(cookies == null) return "";
		for (Cookie c :cookies) {
			if(c.getName().equals(key)) {
				v = c.getValue();
				break;
			}
		}
		return v;
	}
	
}
