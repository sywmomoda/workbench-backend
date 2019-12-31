package com.feiniu.yx.common.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.core.tools.picocli.CommandLine.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.druid.support.json.JSONParser;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.entity.YXLog;
import com.feiniu.yx.common.service.YXLogService;
import com.feiniu.yx.util.ControllerUtil;


/**
 * YXLog
 * @author tongwenhuan
 *
 */
@Controller
@RequestMapping("/yxLog")
public class YXLogController {
	
	@Autowired
	private YXLogService yxLogService;

	@RequestMapping("queryLogs")
    public ModelAndView queryLogs(HttpServletRequest request, HttpServletResponse response,  @ModelAttribute YXLog yxLog) {
        ModelAndView mv = new ModelAndView("cmslog/logList");
        List<YXLog> loglist = yxLogService.queryLogs(yxLog);
        mv.addObject("loglist", loglist);
        mv.addObject("yxLog", yxLog);
        return mv;
    }
	@RequestMapping("deleteOutOfDateLog")
	public void deleteOutOfDateLog(HttpServletRequest request, HttpServletResponse response,@RequestParam(defaultValue ="3") int month ) throws Exception{
		boolean flag = true;
		try{
			yxLogService.deleteLogs(month);
		}catch(Exception e){
			flag =false;
		}
		if(flag){
			ControllerUtil.writeJson(response, "success");
		}else{
			ControllerUtil.writeJson(response, "failed");
		}
	}
	
	@RequestMapping("/queryLogList")
	public void queryLogList(@ModelAttribute YXLog yxLog,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		/*YXLog yxLog = new YXLog();
		yxLog.setLogType(logType);
		yxLog.setProtoId(protoId);*/
		yxLog.setPageRows(20);
		List<YXLog> logList = yxLogService.queryLogs(yxLog);
		Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("logList", logList);
        resultMap.put("yxLog", yxLog);
        String moduleJson = JSONObject.toJSONString(resultMap);
        ControllerUtil.writeJson(response, moduleJson);
	}
	
	@RequestMapping("/queryLogListV2")
	public void queryLogList( @RequestParam String data,
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		YXLog yxLog = JSONObject.parseObject(data, YXLog.class);
		yxLog.setCurPage(page);
		yxLog.setPageRows(limit);
		List<YXLog> logList = yxLogService.queryLogs(yxLog);
		Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("data", logList);
        resultMap.put("code", 0);
        resultMap.put("msg", "success");
        resultMap.put("count", yxLog.getTotalRows());
        String moduleJson = JSONObject.toJSONString(resultMap);
        ControllerUtil.writeJson(response, moduleJson);
	}
	
	@RequestMapping("/queryPeroidsLogs")
    public ModelAndView queryPeroidsLogs(HttpServletRequest request, HttpServletResponse response,  @ModelAttribute YXLog yxLog) {
        ModelAndView mv = new ModelAndView("cmslog/periodsLogList");
        List<YXLog> loglist = yxLogService.queryLogs(yxLog);
        mv.addObject("loglist", loglist);
        mv.addObject("yxLog", yxLog);
        return mv;
    }
}
