package com.feiniu.yx.common.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.feiniu.yx.common.dao.YXLogDao;
import com.feiniu.yx.common.entity.YXLog;

/**
 * 日志服务，
 * 1.查询日志
 * 2.记录日志
 * 3.删除日志 
 * @author tongwenhuan
 *
 */
@Service
public class YXLogService {
	private static Logger logger = Logger.getLogger(YXLogService.class);

	@Autowired
	private YXLogDao yxLogDao;
	
	public List<YXLog> queryLogs(YXLog YXLog) {
		return yxLogDao.queryLogs(YXLog);
	}
	
	public void addLogs(YXLog YXLog) {
		yxLogDao.addYXLog(YXLog);
	}
	
	@SuppressWarnings("static-access")
	public void deleteLogs(int month) throws Exception{
		Date dNow = new Date(); //当前时间
		Date dBefore = new Date();
		Calendar calendar = Calendar.getInstance(); //得到日历
		calendar.setTime(dNow);//把当前时间赋给日历
		calendar.add(calendar.MONTH, -month); //设置为前n月
		dBefore = calendar.getTime(); //得到前3月的时间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			dBefore = sdf.parse(sdf.format((dBefore)));
			YXLog cLog = new YXLog();
			cLog.setOperationTime(dBefore);
			yxLogDao.deleteLogs(cLog);
		} catch (ParseException e) {
			logger.error("data format error:",e);
			throw e;
		}
	}	
}
