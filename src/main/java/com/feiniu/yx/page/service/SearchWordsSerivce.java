package com.feiniu.yx.page.service;

import com.feiniu.yx.common.entity.ReturnT;
import com.feiniu.yx.page.entity.SearchWords;

import java.util.List;

public interface SearchWordsSerivce {
  List<SearchWords> list(SearchWords searchWords);
  ReturnT<String> insert(SearchWords searchWords);
  SearchWords getSearchWordsById(Long id);
  ReturnT<String> update(SearchWords searchWords);
  ReturnT<String> delete(Long id);
  ReturnT<String> publishSearchWords(Long id);
}
