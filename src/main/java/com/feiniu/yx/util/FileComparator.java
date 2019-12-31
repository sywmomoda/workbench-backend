package com.feiniu.yx.util;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.regex.Pattern;

import com.feiniu.yx.common.entity.FileList;


public class FileComparator implements Comparator<FileList> {	
	@Override
	public int compare(FileList arg0, FileList arg1) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if("文件夹".equals(arg0.getFileType())){
			if("文件夹".equals(arg1.getFileType())){
				try {
					return (sdf.parse((arg1.getLastEditDate()))).compareTo(sdf.parse(arg0.getLastEditDate()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}else{
				return -1;
			}
		}else{
			if("文件夹".equals(arg1.getFileType())){
				return 1;
			}else{
				try {
//					if((sdf.parse((arg0.getLastEditDate()))).compareTo(sdf.parse(arg1.getLastEditDate())) >0){
//						return -1;
//					}else{
//						return 1;
//					}
					return (sdf.parse((arg1.getLastEditDate()))).compareTo(sdf.parse(arg0.getLastEditDate()));
				} catch (ParseException e) {
					e.printStackTrace();
				}		
			}
		}
		return 0;
	}
	
	public boolean isNumeric(String str){ 
	    Pattern pattern = Pattern.compile("[0-9]*"); 
	    return pattern.matcher(str).matches();    
	 }

}
