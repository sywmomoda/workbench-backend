package com.feiniu.yx.common.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.feiniu.image.model.ImageItem;
import com.feiniu.image.service.ImageService;
import com.feiniu.yx.common.service.YXImageService;
import com.feiniu.yx.util.ImageUtils;

/**
 * @author tongwenhuan
 * 2017年2月20日 下午2:55:38
 */
@Component
public class YXImageServiceImpl implements YXImageService {
	
	private static final Logger logger =Logger.getLogger(YXImageServiceImpl.class);

	@Autowired
	private ImageService imageService;

	@Override
	public String upload(String fileName, byte[] content) {
		//1、构造上传文件实体
		ImageItem imageItem = new ImageItem();
		//图片名称 必填
		imageItem.setFileName(fileName);
		//图片字节流 必填
		imageItem.setContent(content);
		//图片所属业务分类 必填（用于管理中心查询统计，分类名称要标识业务名称）
		imageItem.setCategory("cms");
		//图片关键字 可选 （用于管理中心查询、搜索）
		imageItem.setKeywords("cms");
		//图片创建人 可选
		imageItem.setCreateBy("cms");
		
		//2、上传图片
		String imageUrl="";
		try{
			imageUrl = ImageUtils.getImageUrl(imageService.upload(imageItem));
		}catch(Exception ex){
			logger.error("upload img error", ex);
		}
		return imageUrl;
	}
	
}
