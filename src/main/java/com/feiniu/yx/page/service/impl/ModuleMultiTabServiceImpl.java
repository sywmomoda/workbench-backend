package com.feiniu.yx.page.service.impl;

import com.feiniu.yx.page.dao.ModuleMultiTabDao;
import com.feiniu.yx.page.dao.ModuleMultiTabOnlineDao;
import com.feiniu.yx.page.entity.ModuleMultiTab;
import com.feiniu.yx.page.service.ModuleMultiTabService;
import com.feiniu.yx.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author yehui
 */
@Service
public class ModuleMultiTabServiceImpl implements ModuleMultiTabService {

    @Autowired
    private ModuleMultiTabDao multiTabDao;

    @Autowired
    private ModuleMultiTabOnlineDao multiTabOnlineDaoDao;

    @Override
    public Long insert(ModuleMultiTab multiTab) {
        String userId =  UserUtil.getUserId();
        Date now = new Date();
        multiTab.setUpdateId(userId);
        multiTab.setCreateId(userId);
        multiTab.setCreateTime(now);
        multiTab.setUpdateTime(now);
        return multiTabDao.insert(multiTab);
    }

    @Override
    public void update(ModuleMultiTab multiTab) {
        multiTab.setUpdateId(UserUtil.getUserId());
        multiTab.setUpdateTime(new Date());
        multiTabDao.update(multiTab);
    }

    @Override
    public void updateStoreCode(String  ids,String storeCode){
        if(StringUtils.isBlank(storeCode)){
            return;
        }
        if(StringUtils.isBlank(ids)){
            return;
        }
        String[] idArray = ids.split(",");
        for(String id  : idArray){
            if(StringUtils.isBlank(id)){
                continue;
            }
            ModuleMultiTab  mt =  multiTabDao.selectById(Long.valueOf(id));
            mt.setStoreCode(storeCode);
            multiTabDao.update(mt);
        }
    }

    @Override
    public void delete(Long id) {
        multiTabDao.delete(id);
    }
    @Override
    public void deleteByIds(String ids) {
        if(StringUtils.isBlank(ids)){
           return;
        }
        String[] idArray = ids.split(",");
        for(String id : idArray){
            if(StringUtils.isBlank(id)){
                continue;
            }
            multiTabDao.delete(Long.valueOf(id));
        }
    }

    @Override
    public ModuleMultiTab selectById(Long id) {
        return multiTabDao.selectById(id);
    }

    @Override
    public List<ModuleMultiTab> selectListByIds(String ids) {
        return multiTabDao.selectListByIds(ids);
    }

    @Override
    public void syncModuleMultiTab(String ids){
        if(StringUtils.isBlank(ids)){
            return;
        }
        List<ModuleMultiTab> tabList = multiTabDao.selectListByIds(ids);
        if(null == tabList || tabList.size() == 0){
            return;
        }
        multiTabOnlineDaoDao.delete(ids.split(","));
        multiTabOnlineDaoDao.insert(tabList);
    }
}
