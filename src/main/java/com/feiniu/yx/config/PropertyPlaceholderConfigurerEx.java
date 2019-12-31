package com.feiniu.yx.config;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * @author tongwenhuan
 * 2017年2月16日 下午2:33:32
 */
public class PropertyPlaceholderConfigurerEx extends PropertyPlaceholderConfigurer {
	
	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
		//添加到系统变量中
		SystemEnv.addProperty(props);
		//初始化Log4j
		PropertyConfigurator.configure(props);
		
		SystemEnv.addProperty("webRootPath", getWebrootPath());
	}
	
	private static String getWebrootPath() {
		String root = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		try {
			root = new File(root).getParentFile().getParentFile().getCanonicalPath();
			root += File.separator;
			if(new File(root+"WebContent").exists()){
				root = root + "WebContent" + File.separator;
			}else if(new File(root+"WebRoot").exists()){
				root = root + "WebRoot" + File.separator;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return root;
	}
}
