package com.feiniu.yx.common.jobHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.common.service.SyncCategoryService;
import com.flygear.job.core.biz.model.ReturnT;
import com.flygear.job.core.handler.IJobHandler;
import com.flygear.job.core.handler.annotation.JobHandler;
import com.flygear.job.core.log.FlyGearJobLogger;

/**
   优鲜类目同步
 * @author tongwenhuan
 *
 */
@JobHandler(value="categoryJobHandler")
@Component
public class CategoryJobHandler extends IJobHandler {

	@Autowired
	private SyncCategoryService syncCategoryService;

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		FlyGearJobLogger.log("Category-JOB.");
		JSONObject r = syncCategoryService.SyncCategory("CPG1,CPG2,CPG3,CPG4,CPG5,CPG6");
		int code = 0;
		if (r != null && r.getBooleanValue("success")) {
			code = 200;
		}
		return new ReturnT<String>(code, "ok");
	}
	
	
	
	
}
