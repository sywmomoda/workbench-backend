package com.feiniu.yx.common.jobHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.store.service.YXStoreService;
import com.flygear.job.core.biz.model.ReturnT;
import com.flygear.job.core.handler.IJobHandler;
import com.flygear.job.core.handler.annotation.JobHandler;
import com.flygear.job.core.log.FlyGearJobLogger;

/**
 * 优鲜门店同步定时任务
 * @author:tongwenhuan
 * @time:2018年10月8日 下午2:45:54
 */
@JobHandler(value="storeJobHandler")
@Component
public class StoreJobHandler extends IJobHandler {
	
	@Autowired
	private YXStoreService yXStoreService;

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		FlyGearJobLogger.log("store-JOB.");
		JSONObject r = yXStoreService.synchroRemoteStoreInfo(param);
		int code = r.getIntValue("code");
		if (code == 1) {
			code = 200;
		}
		return new ReturnT<String>(code, r.getString("msg"));
	}
	
	
}
