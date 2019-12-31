package com.feiniu.yx.common.service.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.YXVideoSerice;
import com.feiniu.yx.config.SystemEnv;
import com.feiniu.yx.util.DateUtil;

@Service
public class YXVideoSericeImpl implements YXVideoSerice {
	private static final Logger logger =Logger.getLogger(YXCouponServiceImpl.class);

	private static final String UP_LOAD_URL =SystemEnv.getProperty("upload_ali_video")+"/upload.do?systemCode=cms&contentId=%s&uploadModel=1";
	private static final String PROGRESS_URL = SystemEnv.getProperty("upload_ali_video")+"/progress.do?systemCode=cms&contentId=%s";
	
	@Override
	public JSONObject upload(MultipartFile file) {
		StringBuffer sbContent = new StringBuffer();
		String contentId =sbContent.append("yxcms").append(DateUtil.getDate(new Date(), "yyyyMMddHHmmss")).toString();
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(String.format(UP_LOAD_URL, contentId));
		RequestConfig config = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(20000).build();
		post.setConfig(config);

		//FileBody fileBody = new FileBody(file);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		//builder.addPart("file", fileBody);
		// 相当于 <input type="file" class="file"
		// name="file">,匹配@RequestParam("file")
		// .addPart()可以设置模拟浏览器<input/>的表单提交
		//HttpEntity entity = builder.build();
		String result = "";
		CloseableHttpResponse response = null;
		try {
			HttpEntity entity = builder.setCharset(Charset.forName("UTF-8")).addBinaryBody("file", file.getInputStream(),ContentType.DEFAULT_TEXT,file.getOriginalFilename()).build();
			post.setEntity(entity);
			response = client.execute(post);
			HttpEntity resEntity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(resEntity);
			}
		} catch (IOException e) {
			logger.error(e.toString(),e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				logger.error(e.toString(),e);
			}
		}
		logger.debug(result);
		if(StringUtils.isBlank(result)) {
			return new JSONObject();
		}
		JSONObject  res = JSONObject.parseObject(result);
		res.put("contentId", contentId);
		return res;
	}
	
	
	@Override
	public JSONObject videoProgress(String contentId) {
		String result = getProgress(contentId);
		JSONObject res = new JSONObject();
		if(StringUtils.isBlank(result)) {
			return res;
		}
		JSONObject object = JSONObject.parseObject(result);
		String data = object.getString("data");
		if(StringUtils.isBlank(data)) {
			return res;
		}
		res = JSONObject.parseObject(data);
		return res;
	}
	
	/**
	 * getProgress请求，参数拼接在地址上
	 * 
	 * @param url
	 *            请求地址加参数
	 * @return 响应
	 */
	private static String getProgress(String contentId) {
		String result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet get = new HttpGet(String.format(PROGRESS_URL, contentId));
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(get);
			if (response != null && response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				result = entityToString(entity);
			}
			return result;
		} catch (IOException e) {
			logger.error(e.toString(), e);
		} finally {
			try {
				httpClient.close();
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				logger.error(e.toString(), e);
			}
		}
		return null;
	}

	public static String entityToString(HttpEntity entity) throws IOException {
		String result = null;
		if (entity != null) {
			long lenth = entity.getContentLength();
			if (lenth != -1 && lenth < 2048) {
				result = EntityUtils.toString(entity, "UTF-8");
			} else {
				InputStreamReader reader1 = new InputStreamReader(entity.getContent(), "UTF-8");
				CharArrayBuffer buffer = new CharArrayBuffer(2048);
				char[] tmp = new char[1024];
				int l;
				while ((l = reader1.read(tmp)) != -1) {
					buffer.append(tmp, 0, l);
				}
				result = buffer.toString();
			}
		}
		return result;
	}
}
