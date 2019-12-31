package com.feiniu.yx.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import net.coobird.thumbnailator.Thumbnails;

/**
 * 类       名: ThumbnailatorUtil <br/> 
 * 功       能: {@code }<br/> 
 * 创建日期: 2017年11月7日 下午4:55:31 <br/> 
 *
 * @author yong.jiang
 * @since Jdk 1.7
 * @see       
 *
 */
public class ThumbnailatorUtil {
	
	private static Logger logger = Logger.getLogger(ThumbnailatorUtil.class);

	public static byte[] iphoneXCut(String srcUrl,int dVaule){
		ByteArrayOutputStream bos = null;
		try {
			BufferedImage srcImage = null;
			URL src = new URL(srcUrl);
			srcImage = ImageIO.read(src);
			int srcImageHeight = srcImage.getHeight();
			int srcImageWidth = srcImage.getWidth();
			bos = new ByteArrayOutputStream();
			Thumbnails.of(src).sourceRegion(0,dVaule,srcImageWidth,srcImageHeight-dVaule).size(srcImageWidth, srcImageHeight-dVaule).toOutputStream(bos);
			byte[] content = null;
			if( bos != null){
				content = bos.toByteArray();
			}
			return content;
		} catch (IOException e) {
			logger.error("iphoneXCut EORROR", e);
		} finally {
			if( bos != null){
				try {
					bos.close();
				} catch (IOException e) {
					logger.error("iphoneXCut EORROR", e);
				}
			}
		}
		return null;  
	}
	
	public static void main(String[] args) {
		System.out.println(iphoneXCut("http://img17.fn-mart.com/pic/b17a133c913c18a9b434/B28nnn7z_2CMBguljz/7ieySGvGtihiuy/CsmRslnyk2-AEws9AAH5UVy4tnU503.jpg",200));
	}
}
