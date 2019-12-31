package com.feiniu.yx.util;

import java.io.File;
import java.io.FileFilter;
import java.util.Calendar;
import java.util.Date;

public class TimeFileFilter implements FileFilter{

	@Override
	public boolean accept(File file) {
		if(file!=null){
			Date dNow = new Date(); //当前时间
			Date dBefore = new Date();
			Calendar calendar = Calendar.getInstance(); //得到日历
			calendar.setTime(dNow);//把当前时间赋给日历
			calendar.add(Calendar.MONTH, -3); //设置为前3月
			dBefore = calendar.getTime(); //得到前3月的时间
			if(file.isDirectory() || dBefore.after(new Date(file.lastModified()))){
				return true;
			}
		}
		return false;
	}
}
