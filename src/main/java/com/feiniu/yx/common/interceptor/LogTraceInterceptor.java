package com.feiniu.yx.common.interceptor;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.dao.YXLogDao;
import com.feiniu.yx.common.entity.YXLog;
import com.feiniu.yx.core.utils.ThreadLocalUtil;
import com.feiniu.yx.util.UserUtil;

public class LogTraceInterceptor extends HandlerInterceptorAdapter {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(LogTraceInterceptor.class);
    
    @Autowired
    private YXLogDao yxLogDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	Object[] formatMessager = logFormatMessager(request, handler);
        
        if (formatMessager==null || formatMessager.length==0) {
            return true;
        }
        String userName = UserUtil.getUserId();
        sendLogger(userName, userName, getIpAddress(request), new Date(), formatMessager);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    	//请求结束后，清空线程常量，防内存泄漏
    	ThreadLocalUtil.remove();
    }
    
    /**
     * 获得ip
     * @param request
     * @return
     */
    private static String getIpAddress(HttpServletRequest request) { 
    	String ip = request.getHeader("x-forwarded-for"); 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
          ip = request.getHeader("Proxy-Client-IP"); 
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
          ip = request.getHeader("WL-Proxy-Client-IP"); 
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
          ip = request.getHeader("HTTP_CLIENT_IP"); 
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
          ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
        } 
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
          ip = request.getRemoteAddr(); 
        } 
        return ip; 
    } 
    
    /**
     * 获得日志内容
     * @param request
     * @param handler
     * @return
     */
    private Object[] logFormatMessager(HttpServletRequest request, Object handler) {
        Object[] formatMessager = null;
        HandlerMethod handlerM = (HandlerMethod) handler;
		Method method = handlerM.getMethod();
		LogTrace logtrace = method.getAnnotation(LogTrace.class);
		if (logtrace == null) {
		   return null;
		}
		
        String[] msgFormatArray = logtrace.msgFomort();
        if (ArrayUtils.isEmpty(msgFormatArray)) {
            LOGGER.warn(handler.getClass().getName() + "@LogTrace未设置格式化操作语句<msgFomort>");
            return null;
        }
        formatMessager = new String[3];
        formatMessager[0] = msgFormatArray[0];
        if(msgFormatArray.length>1){
        	formatMessager[1] = msgFormatArray[1];
        }
        if(msgFormatArray.length>2 && !"0".equals(msgFormatArray[2])){
        	formatMessager[2] = msgFormatArray[2];
        }

		Map<String, String[]> parameters = request.getParameterMap();
        for (Entry<String, String[]> entry : parameters.entrySet()) {
            if (formatMessager[0].toString().contains("{" + entry.getKey() + "}")) {
            	String[] param = entry.getValue();
            	if(param != null && param.length > 0) {
            		formatMessager[0] = StringUtils.replace(formatMessager[0].toString(), "{" + entry.getKey() + "}",param[0].toString());
            	}
            }
            if (formatMessager[2]!=null && formatMessager[2].toString().contains("{" + entry.getKey() + "}")) {
            	String[] param = entry.getValue();
            	if(param != null && param.length > 0) {
            		formatMessager[2] = StringUtils.replace(formatMessager[2].toString(), "{" + entry.getKey() + "}",param[0].toString());
            	}
            }
            //处理json中提取ID
            if(msgFormatArray.length>3){
            	String[] dataParam = msgFormatArray[3].split(":");
            	if(dataParam.length>1 && dataParam[0].equals(entry.getKey())){
            		String[] param = entry.getValue();
            		if(param != null && param.length > 0) {
            			Map<?, ?> paramMap = JSONObject.parseObject(param[0].toString());
            			formatMessager[2] = String.valueOf(paramMap.get(dataParam[1]));
                	}
            	}
            }
            
        }
        
        
        
        
        return formatMessager;
    }
    
    /**
     * 保存日志
     * @param trueName
     * @param userId
     * @param clientIp
     * @param operationTime
     * @param operationMsg
     */
    private void sendLogger(String trueName, String userId, String clientIp, Date operationTime, Object[] operationMsg) {
        try {
        	YXLog cmsLog = new YXLog();
        	cmsLog.setUserId(userId);
        	cmsLog.setTrueName(trueName);
        	cmsLog.setClientIp(clientIp);
        	cmsLog.setOperationMsg(operationMsg[0].toString());
        	if(cmsLog.getOperationMsg().length()>1000){
        		cmsLog.setOperationMsg(cmsLog.getOperationMsg().substring(0,990)+"...");
        	}
        	cmsLog.setCreateId("sys");
        	cmsLog.setUpdateId("sys");
        	if(operationMsg[1]!=null){
        		cmsLog.setLogType(operationMsg[1].toString());
        	}else{
        		cmsLog.setLogType("");
        	}
        	if(operationMsg[2]!=null){
        		try{
        			cmsLog.setProtoId(Long.parseLong(operationMsg[2].toString()));
        		}catch(Exception e){
        			cmsLog.setProtoId(0L);
        		}
        	}else{
        		cmsLog.setProtoId(0L);
        	}
        	yxLogDao.addYXLog(cmsLog);
        }catch(Exception e) {
        	LOGGER.error("cms log error",e);
        }
    }

}