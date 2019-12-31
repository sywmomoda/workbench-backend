package com.feiniu.yx.page.service;

import com.feiniu.yx.page.entity.ModuleMultiTab;

import java.util.List;

public interface ModuleMultiTabService {
   Long insert(ModuleMultiTab multiTab);
   void update(ModuleMultiTab multiTab);
   void delete(Long id);
   ModuleMultiTab selectById(Long id);
   List<ModuleMultiTab> selectListByIds(String ids);
   void updateStoreCode(String ids,String storeCode);
    void deleteByIds(String ids);

    /**
     *
     * @param ids
     */
    void syncModuleMultiTab(String ids);
}
