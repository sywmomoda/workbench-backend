package com.feiniu.yx.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.feiniu.yx.common.YXConstant;
import com.feiniu.yx.common.dao.ResDao;
import com.feiniu.yx.common.entity.Res;
 
/** 
* 复制文件或文件夹 
*  
* lzy
*/  
public class CopyFileUtil {  
	private static Logger logger = Logger.getLogger(CopyFileUtil.class);
	
	private static ResDao resDao; 
	private static Map<String, Res> resMap;
	
	static{
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
		resDao = (ResDao) wac.getBean("resDao");
		resetResMap();
	}
	
	/**
	 * 将资源文件转map
	 * 文件路径作为key
	 */
	private static void resetResMap(){
		resMap = new HashMap<String, Res>();
		List<Res> allList = resDao.findAll();
		for(Res res : allList){
			//文件路径
			String resPath = YXConstant.STATICPATH + res.getUrl() + res.getName();
			if (File.separatorChar != '/')
				resPath = resPath.replace('/', File.separatorChar);
			resMap.put(resPath, res);
		}
	}
	
	/**
	 * 更新文件版本
	 * @author tongwenhuan
	 * 2017年6月7日
	 * @param destFile 目标文件
	 * @param srcFile 源文件
	 */
	private static void updFileTemp(File destFile, File srcFile){
		Res res = resMap.get(destFile.getPath());
		if (res == null)return;
		if (srcFile.lastModified() != res.getTemp()){
			res.setTemp(srcFile.lastModified());
			resDao.updOneTemp(res);
		}
	}
	
   /** 
    * 复制单个文件 
    *  
    * @param srcFileName 
    *            待复制的文件名 
    * @param descFileName 
    *            目标文件名 
    * @param overlay 
    *            如果目标文件存在，是否覆盖 
    * @return 如果复制成功返回true，否则返回false 
    */  
   public static boolean copyFile(String srcFileName, String destFileName,  
           boolean overlay) {  
       File srcFile = new File(srcFileName);  
 
       // 判断源文件是否存在  
       if (!srcFile.exists()) {  
    	   logger.error("源文件：" + srcFileName + "不存在！");  
           return false;  
       } else if (!srcFile.isFile()) {  
    	   logger.error("复制文件失败，源文件：" + srcFileName + "不是一个文件！");  
           return false;  
       }  
 
       // 判断目标文件是否存在  
       File destFile = new File(destFileName);  
       if (destFile.exists()) {  
           // 如果目标文件存在并允许覆盖  
           if (overlay) {  
               // 删除已经存在的目标文件，无论目标文件是目录还是单个文件  
               new File(destFileName).delete();  
           }  
       } else {  
           // 如果目标文件所在目录不存在，则创建目录  
           if (!destFile.getParentFile().exists()) {  
               // 目标文件所在目录不存在  
               if (!destFile.getParentFile().mkdirs()) {  
                   // 复制文件失败：创建目标文件所在目录失败  
                   return false;  
               }  
           }  
       }  
 
       // 复制文件  
       int byteread = 0; // 读取的字节数  
       InputStream in = null;  
       OutputStream out = null;  
 
       try {  
           in = new FileInputStream(srcFile);  
           out = new FileOutputStream(destFile);  
           byte[] buffer = new byte[1024];  
 
           while ((byteread = in.read(buffer)) != -1) {  
               out.write(buffer, 0, byteread);  
           }
           updFileTemp(destFile,srcFile);
           return true;  
       } catch (FileNotFoundException e) {  
           return false;  
       } catch (IOException e) {  
           return false;  
       } finally {  
           try {  
               if (out != null){  
                   out.close();  
               }
           } catch (IOException e) {  
               e.printStackTrace();  
           } finally{
        	   try{
	        	   if (in != null)  {
	                   in.close();
	        	   }
        	   }catch(IOException e) {  
                   e.printStackTrace();  
               }
           }
       }  
   }  
 
   /** 
    * 复制整个目录的内容 
    *  
    * @param srcDirName 
    *            待复制目录的目录名 
    * @param destDirName 
    *            目标目录名 
    * @param overlay 
    *            如果目标目录存在，是否覆盖 
    * @return 如果复制成功返回true，否则返回false 
    */  
   public static boolean copyDirectory(String srcDirName, String destDirName,  
           boolean overlay) {  
       // 判断源目录是否存在  
       File srcDir = new File(srcDirName);  
       if (!srcDir.exists()) {  
    	   logger.error("复制目录失败：源目录" + srcDirName + "不存在！");  
           return false;  
       } else if (!srcDir.isDirectory()) {  
    	   logger.error("复制目录失败：" + srcDirName + "不是目录！");  
           return false;  
       }  
       resetResMap();
       // 如果目标目录名不是以文件分隔符结尾，则加上文件分隔符  
       if (!destDirName.endsWith(File.separator)) {  
           destDirName = destDirName + File.separator;  
       }  
       File destDir = new File(destDirName);  
       // 如果目标文件夹存在  
       if (destDir.exists()) {  
           // 如果允许覆盖则删除已存在的目标目录  
           if (overlay) {  
               new File(destDirName).delete();  
           } else {  
        	   logger.error("复制目录失败：目的目录" + destDirName + "已存在！");  
               //JOptionPane.showMessageDialog(null, MESSAGE);  
               return false;  
           }  
       } else {  
           // 创建目的目录  
    	   logger.info("目的目录"+destDirName+"不存在，准备创建!");  
           if (!destDir.mkdirs()) {  	
        	   logger.error("复制目录失败：创建目的目录"+destDirName+"失败！");  
               return false;  
           }  
       }  
 
       boolean flag = true;  
       File[] files = srcDir.listFiles();  
       for (int i = 0; i < files.length; i++) {  
           // 复制文件  
           if (files[i].isFile()) {  
               flag = CopyFileUtil.copyFile(files[i].getAbsolutePath(),  
                       destDirName + files[i].getName(), overlay);  
               if (!flag)  
                   break;  
           } else if (files[i].isDirectory()) {  
               flag = CopyFileUtil.copyDirectory(files[i].getAbsolutePath(),  
                       destDirName + files[i].getName(), overlay);  
               if (!flag)  
                   break;  
           }  
       }  
       if (!flag) {  
    	   logger.error( "复制目录" + srcDirName + "至" + destDirName + "失败！");  
           return false;  
       } else {  
           return true;  
       }  
   }  
 
   public static void main(String[] args) {  
       String srcDirName = "F:/ROOT/static";  
       String destDirName = "F:/opt/cmsPage/static";  
       CopyFileUtil.copyDirectory(srcDirName, destDirName, true);  
   }  
}  