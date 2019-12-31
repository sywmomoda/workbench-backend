package com.feiniu.yx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.feiniu.hasauth.HasAuthInterceptor;

/**
 * @author tongwenhuan
 * @date 2019年6月14日
 */

@Configuration
@PropertySource(value={"file:E:\\cmsbackend-yx\\src\\main\\resources\\pro.properties",
"file:E:\\cmsbackend-yx\\src\\main\\resources\\sso.properties"}, ignoreResourceNotFound = true)
public class SSOConfig extends WebMvcConfigurerAdapter {

	@Bean(name = "hasAuthInterceptor")
	public HasAuthInterceptor hasAuthInterceptor() {
		return new HasAuthInterceptor();
	}
	
	@Override
	public void addInterceptors (InterceptorRegistry registry) {
		registry.addInterceptor(hasAuthInterceptor());
		//InterceptorRegistration addInterceptor =  registry.addInterceptor(hasAuthInterceptor());
		// 不拦截
        //addInterceptor.excludePathPatterns("/test");
		// 拦截 /**拦截所有的，多个拦截规则可以传入多个参数
		//addInterceptor.addPathPatterns("/**");
        //addInterceptor.addPathPatterns("/**/listPage.do**", "/**/getListView.do**");
		super.addInterceptors(registry);
	}
	
}
