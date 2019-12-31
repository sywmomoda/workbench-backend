package com.feiniu.yx.common.interceptor;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.feiniu.yx.common.YXConstant;
import com.feiniu.yx.common.dao.YXLogDao;
import com.feiniu.yx.common.entity.YXLog;

/**
 * @author zhangdunyao
 *
 */
@Service
public class LogTraceService{
	
    private static final Logger LOGGER = LoggerFactory.getLogger(LogTraceService.class);

    @Autowired
    private YXLogDao cmsLogDao;

    public void sendLogger(String trueName, String userId, String clientIp, String operationMsg,String logType, Long protoId) {
        try {
        	YXLog cmsLog = new YXLog();
        	cmsLog.setUserId(userId);
        	cmsLog.setTrueName(trueName);
        	cmsLog.setClientIp(clientIp);
        	cmsLog.setOperationMsg(operationMsg);
        	if(cmsLog.getOperationMsg().length()>1000){
        		cmsLog.setOperationMsg(cmsLog.getOperationMsg().substring(0,990)+"...");
        	}
        	cmsLog.setCreateId("sys");
        	cmsLog.setUpdateId("sys");
        	cmsLog.setLogType(logType);
        	cmsLog.setProtoId(protoId);
        	cmsLogDao.addYXLog(cmsLog);
        }catch(Exception e) {
        	LOGGER.error("cms log error",e);
        }
    }
    
    
    public void sendLogger(String operationMsg,String logType, Long protoId){
    	sendLogger(operationMsg,logType,protoId,getRequestObject());
    } 
    
    public void sendLogger(String operationMsg,String logType, Long protoId,HttpServletRequest request){
    	String userName = (String) request.getAttribute(YXConstant.PORTAL_CURRENT_USERNAME);
//    	String userName = UserUtil.getUserId();
    	 try {
    		 
         	YXLog cmsLog = new YXLog();
         	String clientIp =getIpAddress(request);
         	cmsLog.setUserId(userName);
         	cmsLog.setTrueName(userName);
         	cmsLog.setClientIp(clientIp);
         	cmsLog.setOperationMsg(operationMsg);
         	if(cmsLog.getOperationMsg().length()>1000){
         		cmsLog.setOperationMsg(cmsLog.getOperationMsg().substring(0,990)+"...");
         	}
         	cmsLog.setCreateId("sys");
         	cmsLog.setUpdateId("sys");
         	cmsLog.setLogType(logType);
         	cmsLog.setProtoId(protoId);
         	cmsLogDao.addYXLog(cmsLog);
         }catch(Exception e) {
         	LOGGER.error("cms log error",e);
         }
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
    
    private HttpServletRequest getRequestObject(){
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
		return request;
    }
    
}