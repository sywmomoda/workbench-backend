package com.feiniu.yx.core.utils;

import com.feiniu.yx.page.entity.Page;

/**
 * 线程常量
 * @author:tongwenhuan
 * @time:2018年12月12日 下午4:16:26
 */
public class ThreadLocalUtil {
	
	private final static ThreadLocal<Page> pageLocal = new ThreadLocal<>();
	
	public static Page getPage() {
		return pageLocal.get();
	}
	
	public static void setPage(Page page) {
		pageLocal.set(page);
	}

	public static void remove() {
		pageLocal.remove();
	}
}
