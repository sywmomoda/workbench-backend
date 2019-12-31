package com.feiniu.yx.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * 类描述： ArtTemplate 模板引擎工具类 创建人：zx 创建时间：2014年10月24日 下午3:53:25
 * 
 */
public class ArtTemplateUtil {
	
	private static Logger logger = Logger.getLogger(ArtTemplateUtil.class);
	
private static Invocable invokeEngine = null;
	
	static {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");
		try {
			engine.eval(new FileReader(ArtTemplateUtil.class.getResource("/arttemplate.js").getFile()));
			engine.eval("function cmsRender(escape, source, data) {"
					+ "template.config('escape', escape);"
					+ "var render = template.compile(source);"
					+ "var renderHtml = render(JSON.parse(data));"
					+ "return renderHtml;"
					+ "}");
			invokeEngine = (Invocable)engine;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public static String exeTemplateByPath(String path, String data) {
		return exeTemplateByPath(path, data, false, null);
	}
	
	public static String exeTemplateByPath(String path, String data, Map<String,String> replaceData) {
		return exeTemplateByPath(path, data, false, replaceData);
	}
	
	private static String exeTemplateByPath(String path, String data, boolean escape, Map<String,String> replaceData) {
		return exeTemplate(getFileContent(path), data, escape, replaceData);
	}

	
	
	private static String replaceTpl(String source,Map<String,String> map){
		if (null == map || map.size() <= 0) {
			return source;
		}
		for(Map.Entry<String, String> entry : map.entrySet()){
			String key = entry.getKey();
			if(!source.contains(key)){
				continue;
			}
			String path = entry.getValue();
			String tpl = getFileContent(path);
			if(StringUtils.isBlank(tpl)){
				continue;
			}
			source = source.replace(key, tpl);
		}
		return source;
	}
	
	/**
	 * 通过artTemplate模板和数据渲染出对应的HTML代码
	 * 
	 * @param artTemplatePath
	 *            artTemplate.js文件的路径 demoPath =
	 *            D:\\workspace64\\FeiniuStoreWeb\
	 *            \src\\main\\webapp\\static\\js\\arttemplate\\arttemplate.js
	 * @param source
	 *            模板内容 demoSource = "<h1>{{title}}</h1>";
	 * @param data
	 *            模板数据 demoData = "{title: 'test'}";
	 * @return
	 */
	private static String exeTemplate(String source, String data, Boolean escape,Map<String,String> replaceData) {
		escape = escape == null ? true : escape;
		String html = null;
		try {
			if (replaceData != null) {
				source = replaceTpl(source, replaceData);
			}
			html = (String)invokeEngine.invokeFunction("cmsRender", escape, source, data);
		} catch (Exception e) {
			logger.error("exeTemplate ERROR", e);
		}
		return html;
	}
	
	private static String getFileContent(String path) {
		StringBuilder content = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
			}
		} catch (IOException e) {
			logger.error("getFileContent error", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		}
		return content.toString();
	}
}
