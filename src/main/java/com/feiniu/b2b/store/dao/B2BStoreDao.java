package com.feiniu.b2b.store.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.feiniu.b2b.store.entity.B2BStore;
@Repository
public class B2BStoreDao {
	@Autowired
	private SqlSession sqlSession;
	
	public List<B2BStore> getStoreList(){
		List<B2BStore> list  = sqlSession.selectList("selectAllB2BStore");
		return list;
	}
	
	public List<B2BStore> getStoreByIds(String[] id){
		List<B2BStore> list  = sqlSession.selectList("selectB2BStoreByIds",id);
		return list;
	}
	
	public B2BStore getB2BStoreById(Long id){
		B2BStore store  = sqlSession.selectOne("B2BStore.selectB2BStoreById",id);
		return store;
	}
	
	public B2BStore getStoreByCode(String code){
		return sqlSession.selectOne("selectB2BStoreByCode",code);
	}
	
	public void insertStoreBatch(List<B2BStore> list){
		sqlSession.insert("insertB2BStoreBatch", list);
	}
	
	public void deleteStoreAll(){
		sqlSession.insert("deleteB2BStoreAll");
	}

	public List<B2BStore> searchB2BStore(B2BStore s) {
		List<B2BStore> list  = sqlSession.selectList("selectB2BStoreByNameCode",s);
		return list;
	}
	
	public List<B2BStore> getStoreByCodes(String[] code){
		List<B2BStore> list  = sqlSession.selectList("selectB2BStoreByCodes",code);
		return list;
	}
	
	public List<B2BStore> getStoreByPgSeq(String pgSeq) {
		List<B2BStore> list = sqlSession.selectList("selectB2BStoreByPgSeq", pgSeq);
		return list;
	}
	
	public List<B2BStore> getStoreBySuareaId(String subAreaId) {
		List<B2BStore> list = sqlSession.selectList("selectB2BStoreBySubareaId", subAreaId);
		return list;
	}
	
	public List<B2BStore> searchStoreByNameOrCode(B2BStore s) {
		List<B2BStore> list  = sqlSession.selectList("selectB2BStoreByNameOrCode",s);
		return list;
	}
}
