package com.feiniu.yx.util;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

/**
 * @author tongwenhuan
 * 2017年2月16日 下午2:31:47
 */
public class ControllerUtil {
	
	private static Logger logger = Logger.getLogger(ControllerUtil.class);
	
	public static void writeJson(HttpServletResponse response) {
		PrintWriter pw = null;
		try {
			JSONObject result = new JSONObject();
			result.put("code", 100);
			result.put("msg", "success");
			response.setContentType("text/json;charset=utf-8");
			pw = response.getWriter();
			pw.write(result.toJSONString());
		} catch (Exception e) {
			logger.error("writeJson eror", e);
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	public static void writeJson(HttpServletResponse response, String json) {
		PrintWriter pw = null;
		try {
			response.setContentType("text/json;charset=utf-8");
			pw = response.getWriter();
			pw.write(json);
		} catch (Exception e) {
			logger.error("writeJson eror", e);
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
	
	public static void writeJsonString(HttpServletResponse response, String json) {
		PrintWriter pw = null;
		try {
			JSONObject result = new JSONObject();
		   	result.put("msg", json);
			response.setContentType("text/json;charset=utf-8");
			pw = response.getWriter();
			pw.write(result.toJSONString());
		} catch (Exception e) {
			logger.error("writeJson eror", e);
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
}
