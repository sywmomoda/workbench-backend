package com.feiniu.yx.core.poolgc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flygear.job.core.biz.model.ReturnT;
import com.flygear.job.core.handler.IJobHandler;
import com.flygear.job.core.handler.annotation.JobHandler;

/**
 * 池定时清理任务
 * @author tongwenhuan
 * @date 2019年3月4日
 */
@JobHandler(value="poolGCJobHandler")
@Component
public class PoolGCJobHandler extends IJobHandler {
	
	@Autowired
	private PoolGCService poolGCService;
	
	@Override
	public ReturnT<String> execute(String param) throws Exception {
		poolGCService.doGC();
		return new ReturnT<String>(200, "ok");
	}



}
