package com.feiniu.yx.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;

/**
 * @author tongwenhuan
 * 2017年2月13日 下午2:27:27
 */
public class HttpTookit {
	
	private static Logger logger = Logger.getLogger(HttpTookit.class);
	
	private static PoolingHttpClientConnectionManager manager = null;
	private static CloseableHttpClient httpclient = null;
	
	public static synchronized CloseableHttpClient getHttpClient() {
		if (httpclient == null) {
			//注册访问协议相关的Socket工厂
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
					.register("http", PlainConnectionSocketFactory.INSTANCE)
					.register("https", SSLConnectionSocketFactory.getSystemSocketFactory())
					.build();
			//httpConnection工厂：配置写请求/解析响应处理器
			HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(DefaultHttpRequestWriterFactory.INSTANCE, DefaultHttpResponseParserFactory.INSTANCE);
			//DNS解析器
			DnsResolver dnsResolver = SystemDefaultDnsResolver.INSTANCE;
			manager = new PoolingHttpClientConnectionManager(socketFactoryRegistry, connFactory, dnsResolver);
			//默认为socket配置
			SocketConfig defaultSocketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
			manager.setDefaultSocketConfig(defaultSocketConfig);
			//设置整个链接池的最大连接数
			manager.setMaxTotal(300);
			//每个路由的默认最大连接，每个路由实际最大连接数默认为DefaultMaxPerRoute控制
			//而MaxTotal是控制整个池子最大数
			//设置过小无法支持大并发(ConnectionPoolTimeoutException:Timeout waiting for connection from pool)
			//路由是对maxTotal的细分,每个路由的最大连接数
			manager.setDefaultMaxPerRoute(200);
			//在从连接池获取连接时，链接不活跃多长时间后需要进行一次验证，默认为2s
			manager.setValidateAfterInactivity(5000);
			
			//默认请求配置
			RequestConfig defaultRequestConfig = RequestConfig.custom()
					.setConnectTimeout(2000) //设置连接超时时间 2s
					.setSocketTimeout(5000)  //设置等待数据超时时间 10s
					.setConnectionRequestTimeout(2000) //设置从链接池获取连接的等待超时时间 2s
					.build();
			
			//创建HttpClient
			httpclient = HttpClients.custom()
					.setConnectionManager(manager)
					.setConnectionManagerShared(false) //连接池不是共享模式
					.evictIdleConnections(60, TimeUnit.SECONDS) //定期回收空闲连接
					.evictExpiredConnections() //定期回收过期连接
					.setConnectionTimeToLive(60, TimeUnit.SECONDS) //连接存活时间，如果不设置，则根据长连接信息决定
					.setDefaultRequestConfig(defaultRequestConfig) //设置默认请求配置
					.setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE) //连接重用策略，即是否能keepAlive
					.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE) //长连接配置，即长连接生存多长时间
					.setRetryHandler(new DefaultHttpRequestRetryHandler(1, true)) //设置重试1次,connect reset重试
					.build();
			
			//JVM停止或者重启时，关闭连接池释放掉连接（跟数据库连接池类似）
			Runtime.getRuntime().addShutdownHook(new Thread() {
				
				@Override
				public void run() {
					try {
						httpclient.close();
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
					
		}
		return httpclient;
	}
	
	 /** 
     * 执行一个HTTP GET请求，返回请求响应的HTML 
     * 
     * @param url 请求的URL地址 
     */ 
    public static String doGet(String url) {
    	long st = System.currentTimeMillis();
    	String result = null;
    	try {
    		HttpGet get = new HttpGet(url);
    		result = remoteCall(get);
    	}catch (Exception e) {
    		long et = System.currentTimeMillis() - st;
    		logger.error("doGet ERROR("+et+"): url is " + url, e);
		}
    	return result; 
    } 

	/**
	 * post请求
	 * @author tongwenhuan
	 * 2016年6月15日
	 * @param url 
	 * @param value 请求实体的value，name默认为data
	 * @return
	 */
	public static String doPost(String url, String name, String value) {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();  
    	formparams.add(new BasicNameValuePair(name, value)); 
		return post(url, formparams);
	}
	
	public static String doPost(String url, Map<String, String> map) {
		if(map == null)return null;
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();  
		for(Entry<String, String> entry : map.entrySet()){
			formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue())); 
		}
		return post(url, formparams);
	}
	
	public static String doPost(String url) {
		return post(url, null);
	}
	
	public static String doPost(String url, String param) {
		long st = System.currentTimeMillis();
		String result = null;
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Content-type", "application/json");
			if (param != null) {
				httpPost.setEntity(new StringEntity(param, "UTF-8"));
			}
			result = remoteCall(httpPost);
		} catch(Exception e) {
			long et = System.currentTimeMillis() - st;
			logger.error("doPost ERROR("+et+"): url is " + url + ", params is " + param, e);
		}
		return result;
	}
	
	private static String post(String url,List<NameValuePair> formparams) {
		long st = System.currentTimeMillis();
		String result = null;
		try {
			HttpPost httpPost = new HttpPost(url);
			if (formparams != null) {
				httpPost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			}
			result = remoteCall(httpPost);
		} catch(Exception e) {
			long et = System.currentTimeMillis() - st;
			logger.error("doPost ERROR("+et+"): url is " + url + ", params is "+JSON.toJSONString(formparams), e);
		}
		return result;
	}
	
	private static String remoteCall(HttpRequestBase remoteRequest) throws Exception {
		String result = null;
		HttpResponse response = null;
		try {
			response = getHttpClient().execute(remoteRequest);
			int statusCode = response.getStatusLine().getStatusCode();
    		if (statusCode != HttpStatus.SC_OK) {
    			EntityUtils.consume(response.getEntity());
    		} else {
    			result = EntityUtils.toString(response.getEntity(), "UTF-8");
    		}
		}catch (Exception e) {
			if (response != null) {
    			try {
					EntityUtils.consume(response.getEntity());
				} catch (IOException e1) {
					throw e1;
				}
    		}
			throw e;
		}
        return result;
	}
}
