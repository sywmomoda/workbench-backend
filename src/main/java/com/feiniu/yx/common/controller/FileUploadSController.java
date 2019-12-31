package com.feiniu.yx.common.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.YXImageService;
import com.feiniu.yx.common.service.YXVideoSerice;
import com.feiniu.yx.util.ControllerUtil;

/**
 * @author tongwenhuan
 * 2017年2月20日 下午2:31:53
 */
@Controller
@RequestMapping("/fileUploadS")
public class FileUploadSController {
	private static final Logger logger =Logger.getLogger(FileUploadSController.class);

	@Autowired
	private YXImageService yXImageService;
	
	@Autowired
	private YXVideoSerice videoSerice;
	
	@RequestMapping(value = "/saveByForm", method = RequestMethod.POST)
	public ModelAndView saveDriverByForm(@RequestParam(value = "file", required = false) MultipartFile file,
			String imgId, HttpServletRequest request,HttpServletResponse response) {
		// 保存相对路径到数据库 图片写入服务器
		ModelAndView mv = new ModelAndView("/uploadFile/saveUpload");
		try {
			String url = yXImageService.upload(file.getOriginalFilename(),file.getBytes());
			mv.addObject("imgId", imgId);
			mv.addObject("url", url);
		} catch (IOException e) {
			logger.error(e.toString(),e);
		}
		return mv;
	}
	
	@RequestMapping(value = "/picFileUpload", method = RequestMethod.POST)
	public void picFileUpload(@RequestParam(value = "file", required = false) MultipartFile file,
			String imgId,HttpServletRequest request,HttpServletResponse response) {
		// 保存相对路径到数据库 图片写入服务器
		JSONObject object = new JSONObject();
		try {
			String url = yXImageService.upload(file.getOriginalFilename(),file.getBytes());
			object.put("imgId", imgId);
			object.put("url", url);
			object.put("result", "success");
		} catch (IOException e) {
			logger.error(e.toString(),e);
			object.put("result", "error");
		}
		ControllerUtil.writeJson(response, object.toJSONString());
	}
	
	@RequestMapping(value = "/saveByVideoForm", method = RequestMethod.POST)
	public ModelAndView saveDriverByVideoForm(@RequestParam(value = "file", required = false) MultipartFile file,
			String imgId,String conId,HttpServletRequest request,HttpServletResponse response) {
		// 保存相对路径到数据库 图片写入服务器
		ModelAndView mv = new ModelAndView("/video/saveUpload");
		String  fileName= file.getOriginalFilename();
		String[] nameArray = fileName.split("\\.");
		if(nameArray.length == 1 ||!nameArray[1].toLowerCase().equals("mp4")) {
			mv.addObject("msg","请上传mp4格式文件");
		    return mv;
		}
		
		if(file.getSize() > 100 *1024*1024) {
			//100大于100M
			mv.addObject("msg","请上传小于100M文件");
		    return mv;
		}
		try {
			JSONObject res = videoSerice.upload(file);
			int code = res.getIntValue("code");
			String url = code ==1 ? res.getString("data") : "";
			mv.addObject("imgId", imgId);
			mv.addObject("url",url);
			mv.addObject("contentId",conId);
			mv.addObject("content",res.getString("contentId"));
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
		return mv;
	}
	
	@RequestMapping(value = "/saveByVideoFormV2", method = RequestMethod.POST)
	public void saveDriverByVideoFormV2(@RequestParam(value = "file", required = false) MultipartFile file,
			String imgId,String conId,HttpServletRequest request,HttpServletResponse response) {
		// 保存相对路径到数据库 图片写入服务器
		JSONObject res = new JSONObject();
		long startTime=System.currentTimeMillis();   //获取开始时间  
		try {
			res = videoSerice.upload(file);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}
		long endTime=System.currentTimeMillis(); //获取结束时间  
		logger.debug("程序运行时间： "+(endTime-startTime)); 
		ControllerUtil.writeJsonString(response, res.toJSONString());
	}
	
	@RequestMapping("/getVideoPorgress")
	public void getVideoProgress(String contentIds,HttpServletResponse response){
		Map<String, JSONObject> map = new HashMap<String,JSONObject>();
		String[] ids = contentIds.split(",");
		for(String id: ids) {
			JSONObject sin = videoSerice.videoProgress(id);
			if(sin.size() > 0) {
				map.put(id, sin);	
			}
		}
		ControllerUtil.writeJson(response, JSONObject.toJSONString(map));
	}
	
}
