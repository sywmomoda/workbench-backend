package com.feiniu.yx.util;

import java.util.List;


import com.alibaba.fastjson.JSONObject;

public class LayuiDataUtil {

	public static <T> JSONObject convertData(List<T> list,YXBaseEntity entity) {
		JSONObject resData = new JSONObject();
		resData.put("code", 0);
		resData.put("data", list);
		resData.put("count", entity.getTotalRows());
		resData.put("msg", "");
		return resData;
	}
}
