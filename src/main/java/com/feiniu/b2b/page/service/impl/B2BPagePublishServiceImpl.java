package com.feiniu.b2b.page.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.b2b.common.B2BConstant;
import com.feiniu.b2b.page.service.B2BModuleProperPlusService;
import com.feiniu.yx.page.dao.ModuleDao;
import com.feiniu.yx.page.dao.ModuleOnlineDao;
import com.feiniu.yx.page.dao.PageDao;
import com.feiniu.yx.page.dao.PageOnlineDao;
import com.feiniu.yx.page.entity.Module;
import com.feiniu.yx.page.entity.Page;
import com.feiniu.yx.page.service.PagePublishService;
import com.feiniu.yx.pool.service.SyncPoolService;
import com.feiniu.yx.template.entity.YXTemplate;
import com.feiniu.yx.template.service.YXTemplateService;
import com.feiniu.yx.util.HttpTookit;

/**
 * @author tongwenhuan
 * 2017年2月28日 上午9:20:38
 */
@Service(value="b2bPagePublishService")
public class B2BPagePublishServiceImpl implements PagePublishService {
	
	@Autowired
	private PageDao pageDao;
	@Autowired
	private PageOnlineDao pageOnlineDao;
	
	@Autowired
	private ModuleDao moduleDao;
	@Autowired
	private ModuleOnlineDao moduleOnlineDao;
	
	@Autowired
	private YXTemplateService templateService;
	
	@Autowired
	private SyncPoolService syncPoolService;
	
	@Autowired
	private B2BModuleProperPlusService moduleProperPlusService;

	@Override
	public String publish(Long id) {
		Page p = pageDao.queryCMSPageByID(id);
		//onlinepage
		Page op = pageOnlineDao.findOne(id);
		//第一次发布，更改状态
		if(p.getStatus().intValue() == 2) {
			p.setStatus(1); //已发布
			pageDao.updatePageStatus(p);
		}
		//第一次发布
		if(op == null){
			pageOnlineDao.insertOne(p);
		}else {
			Date upd_time = p.getUpdateTime();
			Date upd_time_online = op.getUpdateTime();
			//判断更新时间，线下数据更新时间大于online，进行更新
			if (upd_time.getTime() >= upd_time_online.getTime()) {
				pageOnlineDao.updateOne(p);
			}
		}
		
		String state = syncModule(p.getModules());
		Long templateId =p.getTemplateId();
		YXTemplate tt = templateService.getYXTemplateById(templateId);
		//优鲜首页不发布页面，只同步数据到online表
		if(tt!=null && !tt.getCode().equals("index")){
			HttpTookit.doGet(B2BConstant.PUBLISH + p.getId());
		}
		return state;
	}
    
	private String syncModule(String moduleIds) {
		List<Module> moduleList = moduleDao.queryModulesByIds(moduleIds.split(","));
        for (Module module : moduleList) {
            if (module != null) {
            	moduleOnlineDao.insertOrUpdateCMSModuleOnline(module);
            	JSONObject proJo = JSONObject.parseObject(module.getModuleProperties());
            	if(proJo.containsKey("poolId")){//同步池数据
        			String poolId = proJo.get("poolId").toString();
        			if(!"".equals(poolId)){
        				Long id = Long.parseLong(poolId);
        				syncPoolService.syncPool(id);
        			}
        		}
        		if(proJo.containsKey("poolIds")){
        			Object poolIds = proJo.get("poolIds");
        			if(poolIds instanceof List){
        		        List<?> tPoolId = (List<?>) poolIds;
        				if(tPoolId!=null&& tPoolId.size()>0){
        					for(int i=0;i<tPoolId.size();i++){
        						String tpid = tPoolId.get(i).toString();
        						if(StringUtils.isNotBlank(tpid)){
        							Long id = Long.parseLong(tpid);
        							syncPoolService.syncPool(id);
        						}
        					}
        				}
        			}
        		}
        		
        		moduleProperPlusService.syncModuleProperPlus(module.getId());
            }
        }
        return "OK";
	}
}
