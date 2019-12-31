package com.feiniu.yx.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtil {
     
	private static final String DEFAUL_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	public static int getHour(Calendar calendar){
		return calendar.get(Calendar.HOUR_OF_DAY);
	}
	
	public static int getMonth(Calendar calendar){		
		return calendar.get(Calendar.MONTH)+1;
	}
	
	public static int getYear(Calendar calendar){
		return calendar.get(Calendar.YEAR);	
	}
	
	public static int getDay(Calendar calendar){
		return calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	
	/**
	 * 返回日期格式20460801
	 *@author yehui
	 *Aug 3, 2016
	 *@param calendar
	 *@return
	 */
	public static String getYesterday(){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return getDefindedDate(calendar);
	} 
	
	public static String getYesterday(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return getDefindedDate(calendar);
	}
	
	public static String getYesterday(Date date ,String pattern) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return getDate(calendar, pattern);
	}
	
	public static String getYesterday(String pattern) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return getDate(calendar, pattern);
	}
	
	/**
	 * 获取与当前日期相隔几天
	 *@author yehui
	 *Aug 9, 2016
	 *@param calendar
	 *@param pattern
	 *@param day
	 *@return
	 */
	public static String getDate(Calendar calendar,String pattern,int day){
		calendar.add(Calendar.DAY_OF_MONTH, day);
		return getDate(calendar,pattern);
	}
	
	public static Date getDate(Date date,int day){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, day);
		return calendar.getTime();
	}
	
	
   public static String getDate(Calendar calendar,String pattern){		
	    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		String date = sdf.format(calendar.getTime());
		return date;
   }
   
   public static Date getDate(String dateStr, String pattern) {
		SimpleDateFormat df = new SimpleDateFormat(pattern);
		Date output;
		try {
			output = df.parse(dateStr);
		} catch (Exception ex) {
			output = null;
		}
		return output;
   }
   
   
   public  static String getDate(Date date){
	   SimpleDateFormat sdf = new SimpleDateFormat(DEFAUL_PATTERN);
	   String dt = sdf.format(date);
   	   return dt;  
   }
   
   public static String getDate(Date date,String pattern){
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		String dt = sdf.format(date);
    	return dt;
   }
    
    public static String getDefindedDate(Date date){
    	return getDate(date,"yyyyMMdd");
    }
   
   public static String getDefindedDate(Calendar calendar){		
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String date = sdf.format(calendar.getTime());
		return date;
   }
   
   public static int getDayOfMonth(Calendar calendar){
	  return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
   }
   
   public static Date getDate(String date){
	    date = date.replace("-", "").replace(":", "");
	    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd"); 
		Date d = null;
		try {
			d = format.parse(String.valueOf(date));
		} catch (Exception e) {
			d = new Date();
		  return d;
		}
		return d;
   }
	
	/**
	 * 
	 *@author yehui
	 *Aug 3, 2016
	 *@param date  20160806  或 2016-08-06
	 *@return
	 */
	public static boolean isNowDay(String date){
		if(null == date){
			return false;
		}
		date = date.replace("-", "");
		long dd = Long.valueOf(date);
		
		Calendar calendar = Calendar.getInstance();;
		long now = Long.valueOf(getDefindedDate(calendar));
		if(now == dd){
			return true;
		}	
		return false;
		
	}
	
	/**
	 * 列出相隔日期的所有日期
	 *@author yehui
	 *Aug 3, 2016
	 *@param beginDate
	 *@param endDate
	 *@return
	 */
	public  static List<Integer>  getDatesBetweenTwoDate(Date beginDate, Date endDate) {    
	    List<Date> lDate = new ArrayList<Date>();
	    //lDate.add(beginDate);//把开始时间加入集合
	    Calendar cal = Calendar.getInstance();
	    //使用给定的 Date 设置此 Calendar 的时间
	    cal.setTime(beginDate);
	    boolean bContinue = true;
	    while (bContinue) {
	        //根据日历的规则，为给定的日历字段添加或减去指定的时间量
	        cal.add(Calendar.DAY_OF_MONTH, 1);
	        // 测试此日期是否在指定日期之后
	        if (endDate.after(cal.getTime())) {
	            lDate.add(cal.getTime());
	        } else {
	            break;
	        }
	    }
	    lDate.add(endDate);//把结束时间加入集合
	    
	    List<Integer> resultDate = new ArrayList<Integer>();
	    DateFormat format = new SimpleDateFormat("yyyyMMdd"); 
	    for(Date d  : lDate){
	    	resultDate.add(Integer.valueOf(format.format(d)));
	    }
	    
	    return resultDate;
	}
	/** 
     * 得到几天前的时间 
     *  
     * @param d 
     * @param day 
     * @return 
     */  
    public static Date getDateBefore(Date d, int day) {
        Calendar now = Calendar.getInstance();  
        now.setTime(d);  
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);  
        return now.getTime();  
    }
    
    /** 
     * 得到几天前的时间 
     *  
     * @param d 
     * @param day 
     * @return 
     */  
    public static String getDateBefore(Date d, int day,String pattern) {
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Calendar now = Calendar.getInstance();  
        now.setTime(d);  
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);  
		String dt = sdf.format(now.getTime());
        return dt;  
    }
    
    public static Date getDayStart(){
		Date now = new Date();
		String s = getDateString(now, "yyyy-MM-dd");
		return getDate(s + " 00:00:00",DEFAUL_PATTERN);
	}
    
    public static String getDateString(Date date, String pattern){
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}
    
    /**
     * 判断时间段是否有重合
     * @param beginTime1
     * @param entTime1
     * @param begintTime2
     * @param endTime2
     * @return
     */
    public static Boolean comparePeriodsOfTime(Date beginTime1,Date entTime1, Date begintTime2 ,Date endTime2){
    	if(beginTime1.getTime() >= begintTime2.getTime()&& beginTime1.getTime() < endTime2.getTime())  {
    		return true;
    	}
    	else if (beginTime1.getTime() > begintTime2.getTime() && beginTime1.getTime() <= endTime2.getTime()) {
    		return true;
    	} 
    	else if (begintTime2.getTime() >= beginTime1.getTime() && begintTime2.getTime() < entTime1.getTime()){
    		return true;
    	}  
    	else if (begintTime2.getTime() > beginTime1.getTime() && begintTime2.getTime() <= entTime1.getTime()){
    		return true;
    	}
    	
    	return false;
    }
    
    /**
     * 判断当前在时间段的状态
     * @param beginTime
     * @param endTime
     * @return
     */
    public static int isNowTime(Date beginTime,Date endTime){
    	Date now = new Date();
    	
    	if(beginTime.after(now)){
			return  1; //未开始
		}else if(beginTime.before(now) && now.before(endTime)){
			return  2;  //进行中
		}else if(endTime.before(now)){
			return 0; //已经过期
		}
    	return 0;
    }
  
}
