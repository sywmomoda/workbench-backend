package com.feiniu.yx.welcome.service.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.util.DateUtil;
import com.feiniu.yx.util.UserUtil;
import com.feiniu.yx.welcome.dao.YXWelcomeDao;
import com.feiniu.yx.welcome.dao.YXWelcomeImgDao;
import com.feiniu.yx.welcome.entity.YXWelcome;
import com.feiniu.yx.welcome.entity.YXWelcomeImg;
import com.feiniu.yx.welcome.service.YXWelcomeService;

@Service
public class YXWelcomeServiceImpl implements YXWelcomeService {

	@Autowired
	private YXWelcomeDao welcomeDao;
	
	@Autowired
	private YXWelcomeImgDao welcomeImgDao;
	
	@Override
	public List<YXWelcome> getWelcomeList(YXWelcome welcome) {
		 List<YXWelcome> list =  welcomeDao.getWelcomeList(welcome);
		 setWelcomStatus(list);
		 return list;
	}
	
	/*
	 * 设置档期的状态
	 */
	private void setWelcomStatus(YXWelcome welcome){
		List<YXWelcome> list = new ArrayList<YXWelcome>();
		list.add(welcome);
		setWelcomStatus(list);
	}
	/*
	 * 设置档期的状态
	 */
	private void setWelcomStatus(List<YXWelcome> list){
		if(null == list){
			return;
		}
		for(YXWelcome wel : list){
			Date beginTime = wel.getBeginTime();
			Date endTime = wel.getEndTime();
			Date now = new Date();
			if(null ==beginTime && null ==endTime){
				continue;
			}
			if(wel.getStatus() == -1){  //被强制停止
				continue;
			}
			if(beginTime.after(now)){
				wel.setStatus(2);   //未开始
			}else if(beginTime.before(now) && now.before(endTime)){
				wel.setStatus(1);  //进行中
			}else if(endTime.before(now)){
				wel.setStatus(0); //已经过期
			}
		}
	}
	
	@Override
	public JSONObject checkDate(YXWelcome welcome) {
		JSONObject result= new JSONObject();
		List<YXWelcome> welcomeList = welcomeDao.getWelcomeListAfterNow();
		Date wcBt = welcome.getBeginTime();
		Date wcEt = welcome.getEndTime();
        for (YXWelcome w : welcomeList) {
        	//排除本身与新增时ID
        	if(welcome.getId() != null && (w.getId()-welcome.getId()) == 0){
        		continue;
        	}
            Date tBt = w.getBeginTime();
            Date tEt = w.getEndTime();
            if(DateUtil.comparePeriodsOfTime(wcBt, wcEt, tBt, tEt)){
            	result.put("status", -1);
        		result.put("message","档期的日期有重合");
        		return result;
            }
        }
        result.put("status", 1);
        result.put("message","正常");
        return result;
	}
	
	/**
	 * 活动失效
	 */
	@Override
	public void updateYXWelcomeStatus(Long id) {
		welcomeDao.updateYXWelComeStatus(id);
	}
	
	@Override
	public void deleteYXWelcome(Long id) {
		welcomeDao.delete(id);
	}
	
	@Override
	public YXWelcome getWelcomeById(Long id) {
		YXWelcome welcome = welcomeDao.getWelcomeById(id);
		setWelcomStatus(welcome);
		return welcome;
	}
	
	@Override
	public JSONObject getWelcomeOfImgs(Long id){
	  List<YXWelcomeImg> list =  welcomeImgDao.getYXWelcomeImgsOfByYxWelcomeId(id);
	   JSONObject result  = new JSONObject();
	   
	   if(list.size() == 0){
		   return result;
	   }
	   JSONArray resultImg = new JSONArray();
	   JSONArray resultBtnImg = new JSONArray();
	   JSONArray resultUrl = new JSONArray();
	   JSONArray resultType = new JSONArray();
	   int[] length = new int[list.size()];
	  for(int i = 0; i < list.size(); i++){
		  YXWelcomeImg img = list.get(i);
		  String imgUrl = img.getImgUrl();
		  //String imgSize = img.getImgSize();
		  String btnImgUrl = img.getBtnImgUrl();
		  String btnImgSize = img.getBtnImgSize();
		  JSONArray imgUrlArray = JSONArray.parseArray(imgUrl);
		  //JSONArray imgSizeArray = JSONArray.parseArray(imgSize);
		  JSONArray btnImgUrlArray = JSONArray.parseArray(btnImgUrl);
		  JSONArray btnImgSizeArray = JSONArray.parseArray(btnImgSize);
		  
		  for(int j = 0; j < imgUrlArray.size(); j++){
			  JSONObject imgObject = new JSONObject();
			  String picUrl  = imgUrlArray.getString(j);
			  imgObject.put("picUrl", picUrl);
			  String imgSize = getImgSize(picUrl);
			  imgObject.put("size",imgSize);
			  imgObject.put("index", j+1);
			  imgObject.put("indexBig", i+1);
			  resultImg.add(imgObject);
		  }
		  
		  for(int j = 0; j < btnImgUrlArray.size(); j++){
			  JSONObject btnImgObject = new JSONObject();
			  btnImgObject.put("picUrl", btnImgUrlArray.getString(j));
			  btnImgObject.put("size", btnImgSizeArray.getString(j));
			  btnImgObject.put("index", j+1);
			  btnImgObject.put("indexBig", i+1);
			  resultBtnImg.add(btnImgObject);
		  }
		  
		  JSONObject btnCustomUrlObject = JSONObject.parseObject(img.getBtnCustomUrl());
		  resultUrl.add(btnCustomUrlObject.get("url"));
		  resultType.add(btnCustomUrlObject.get("type"));
		  length[i] = i+1;
	  }
	  
	  result.put("picUrl", resultImg);
	  result.put("btnPicUrl", resultBtnImg);
	  result.put("url", resultUrl);
	  result.put("type", resultType);
	  result.put("length", length);
	  result.put("totalCount", length.length);
	  return result;
	}
	
	@Override
	public void insertYXWelcome(String data) {
		YXWelcome  welcome = JSONObject.parseObject(data, YXWelcome.class);
		JSONObject object = JSONObject.parseObject(data);
		List<YXWelcomeImg>  welcomeImgs =  new ArrayList<YXWelcomeImg>();
		JSONArray picUrlArray  = new JSONArray();
		if(object.get("picUrl") instanceof List){
			picUrlArray.addAll(object.getJSONArray("picUrl"));
		}else{
			String picUrl = object.getString("picUrl");
		    JSONObject picUrlObj = JSONObject.parseObject(picUrl);
		    picUrlArray.add(picUrlObj);
		}

		JSONArray btnPicUrlArray  = new JSONArray();
		
		if(object.get("btnPicUrl") instanceof List){
			btnPicUrlArray.addAll(object.getJSONArray("btnPicUrl"));
		}else{
			String btnPicUrl = object.getString("btnPicUrl");
		    JSONObject picUrlObj = JSONObject.parseObject(btnPicUrl);
		    btnPicUrlArray.add(picUrlObj);
		}
		
		 JSONObject btnCustomUrl = object.getJSONObject("btnCustomUrl");
		 int size = picUrlArray.size();
		 Integer totalCount = object.getInteger("totalCount");
		 String name = UserUtil.getUserId();
		 welcome.setUpdateId(name);
		 Long welcomeId = null;
		 if(welcome.getId() ==null){
			 welcome.setCreateId(name);
			 welcomeId= welcomeDao.insert(welcome); 
		 }else{
			 welcomeDao.update(welcome);
			 welcomeId = welcome.getId();
		 }
		 
		 for(int i = 1 ; i <= totalCount; i ++){
		    YXWelcomeImg img =  getImgs(picUrlArray,btnPicUrlArray,btnCustomUrl,i,size); 
		    img.setWelcomeId(welcomeId);
		    img.setCreateId(name);
		    img.setUpdateId(name);
		    welcomeImgs.add(img);
		 }
		 
		 welcomeImgDao.insertBatch(welcomeImgs);

	}
	
	@Override
	public void updateYXWelcome(String data){
		JSONObject object = JSONObject.parseObject(data);
		Long welcomeId = object.getLong("id");
		welcomeImgDao.deleteYXWelcomeImgsOfByYXwelcomeId(welcomeId);
		insertYXWelcome(data);
	}
	
	private YXWelcomeImg getImgs(JSONArray picUrlArray,JSONArray btnPicUrlArray,JSONObject btnCustomUrl,Integer key,int size){
	   
		//String[] arrayImgs = new String[]{"","","","","","","","",""};
		//String[] arraySizes = new String[]{"1242*2688","1242*2208","1080*1920","720*1280","640*1136","750*1334","640*960","480*800","750*1624"};
		List<String> arrayImgs = new ArrayList<String>();
		List<String> arraySizes = new ArrayList<String>();
		for(int i =0; i < size; i++){
			String picUrlJSON = picUrlArray.getString(i);
			if(StringUtils.isBlank(picUrlJSON)){
				continue;
			}
			JSONObject object = JSONObject.parseObject(picUrlJSON);
			Integer indexBig = object.getInteger("indexBig");  //图片位置顺序
			//Integer index = object.getInteger("index");  //单张图片大小索引
			if(indexBig == key){
				//arrayImgs[index-1] = object.getString("picUrl");
				String picUrl = object.getString("picUrl");
				arrayImgs.add(picUrl);
				//arraySizes[index-1] = object.getString("size");
				arraySizes.add(getImgSize(picUrl));
			}
		}
		
		YXWelcomeImg img = new YXWelcomeImg();
		img.setImgUrl(JSONObject.toJSONString(arrayImgs));
		img.setImgSize(JSONObject.toJSONString(arraySizes));
		
		String[] arrayBtnImgs = new String[]{"","",""};
		
		String[] arrayBtnSizes = new String[]{"428*132","334*103","214*66"};
        if(btnPicUrlArray.size() >0){
        	for(int i =0; i < btnPicUrlArray.size(); i++){
        		String btnPicUrl = btnPicUrlArray.getString(i);
    			if(StringUtils.isBlank(btnPicUrl)){
    				continue;
    			}
    			JSONObject objectBtn = JSONObject.parseObject(btnPicUrl);
    			Integer indexBig = objectBtn.getInteger("indexBig");//图片位置顺序
    			Integer index = objectBtn.getInteger("index");//单张图片大小索引
    			if(indexBig == key){
    				arrayBtnImgs[index-1] = objectBtn.getString("picUrl");
    				//arrayBtnSizes[index-1] = objectBtn.getString("size");
    			}
    		}
		}
        img.setBtnImgUrl(JSONObject.toJSONString(arrayBtnImgs));
        img.setBtnImgSize(JSONObject.toJSONString(arrayBtnSizes));
        
        JSONObject btnObject = new JSONObject();
        JSONArray btnType = new JSONArray();
        JSONArray btnUrl = new JSONArray();
        if(btnCustomUrl.get("type") instanceof List ){
        	 btnType = btnCustomUrl.getJSONArray("type");
             btnUrl = btnCustomUrl.getJSONArray("url");
        }else{
        	btnType.add(btnCustomUrl.getString("type"));
        	btnUrl.add(btnCustomUrl.getString("url"));
        }       
       
        btnObject.put("type", btnType.get(key-1));
        btnObject.put("url", btnUrl.get(key-1));
        
        img.setBtnCustomUrl(btnObject.toJSONString());
		return img;
	}
	
	
	private String getImgSize(String picUrl) {
		BufferedImage srcImage = null;
		try {
			URL src = new URL(picUrl);
			srcImage = ImageIO.read(src);
		} catch (IOException e) {
		}
		if (null == srcImage) {
			return null;
		}
		int srcImageHeight = srcImage.getHeight();
		int srcImageWidth = srcImage.getWidth();
      return srcImageWidth+"*"+srcImageHeight;
	}
}
