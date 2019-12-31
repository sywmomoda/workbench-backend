package com.feiniu.b2b.core.jobhandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.feiniu.b2b.pool.service.B2BCategoryService;
import com.flygear.job.core.biz.model.ReturnT;
import com.flygear.job.core.handler.IJobHandler;
import com.flygear.job.core.handler.annotation.JobHandler;
import com.flygear.job.core.log.FlyGearJobLogger;

/**
   优鲜类目同步
 * @author tongwenhuan
 *
 */
@JobHandler(value="b2bCategoryJobHandler")
@Component
public class B2BCategoryJobHandler extends IJobHandler {

	@Autowired
	private B2BCategoryService b2bCategoryService;

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		FlyGearJobLogger.log("Category-JOB.");
		b2bCategoryService.sync("1,2,3,4,5");
		return new ReturnT<String>(200, "ok");
	}
	
}
