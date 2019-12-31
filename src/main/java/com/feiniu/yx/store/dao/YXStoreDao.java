package com.feiniu.yx.store.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.yx.store.entity.YXStore;
@Repository
public class YXStoreDao {
	@Autowired
	private SqlSession sqlSession;
	
	public List<YXStore> getStoreList(){
		List<YXStore> list  = sqlSession.selectList("selectAllStore");
		return list;
	}
	
	public List<YXStore> getStoreByIds(String[] id){
		List<YXStore> list  = sqlSession.selectList("selectStoreByIds",id);
		return list;
	}
	
	public List<YXStore> getStoreByCodes(String[] code){
		List<YXStore> list  = sqlSession.selectList("selectStoreByCodes",code);
		return list;
	}
	
	public YXStore getStoreByCode(String code){
		return sqlSession.selectOne("selectStoreByCode",code);
	}
	
	public YXStore getStoreById(Long id){
		return sqlSession.selectOne("selectYXStoreById",id);
	}
	
	public void insertStoreBatch(List<YXStore> list){
		sqlSession.insert("insertStoreBatch", list);
	}
	
	public void deleteStoreAll(){
		sqlSession.insert("deleteStoreAll");
	}
	
	public List<YXStore> searchStoreByNameOrCode(YXStore s) {
		List<YXStore> list  = sqlSession.selectList("selectYXStoreByNameOrCode",s);
		return list;
	}

	public List<YXStore> searchStoreByNameCode(YXStore s) {
		List<YXStore> list  = sqlSession.selectList("selectYXStoreByNameCode",s);
		return list;
	}
	
    public List<YXStore> getStoreByPgSeq(String pgSeq){
    	List<YXStore> list  = sqlSession.selectList("selectStoreByPgSeq",pgSeq);
		return list;
    }
}
