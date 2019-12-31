package com.feiniu.yx.config;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * @author tongwenhuan
 * 2017年2月16日 下午2:34:11
 */
public class SystemEnv {
	
	public static final Properties systemProperties = new Properties();
	
	public static void addProperty(Properties properties){
		if(properties!=null){
			for(Object key : properties.keySet()){
				if(key!=null){
					systemProperties.put(key, properties.get(key));
				}
			}
		}
	}
	
	public static void addProperty(String key ,String value){
		systemProperties.put(key, value);
	}
	
	public static String getProperty(String key){
		return systemProperties.getProperty(key);
	}
	
	public static String getProperty(String key,String defaultValue){
		String prop = systemProperties.getProperty(key);
		if(StringUtils.isBlank(prop)){
			prop = defaultValue;
		}
		return prop;
	}
}
