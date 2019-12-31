package com.feiniu.yx.common.service;

import com.alibaba.fastjson.JSONObject;
import com.feiniu.yx.page.entity.Module;

import java.util.List;

/**
 * @author yehui
 */
public interface ModuleMutliTabData {
    /**
     * 获取每个tab当前可用期数
     * @param m
     * @param storeCode
     * @return
     */
     List<JSONObject> getModuleData(Module m , String storeCode, String previewTime);
}
