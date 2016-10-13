package com.nemchina.explorer.db;

import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/** 
 * @Description: Database Operator
 * @author lu
 * @date 2016年8月5日
 */ 
public class DBOperator {

	private static ComboPooledDataSource dataSource = new ComboPooledDataSource("h2");
	
	public static void update(String sql, Object[] params){
		QueryRunner queryRunner = new QueryRunner(dataSource);
		try {
			queryRunner.update(sql, params);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void update(String sql){
		QueryRunner queryRunner = new QueryRunner(dataSource);
		try {
			queryRunner.update(sql);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void updateBatch(String sql, Object[][] params){
		QueryRunner queryRunner = new QueryRunner(dataSource);
		try {
			queryRunner.batch(sql, params);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static List<Map<String, Object>> query(String sql, Object... param){
		List<Map<String, Object>> result = null;
		QueryRunner queryRunner = new QueryRunner(dataSource);
		try {
			result = queryRunner.query(sql, new MapListHandler(), param);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	public static List<Map<String, Object>> query(String sql){
		List<Map<String, Object>> result = null;
		QueryRunner queryRunner = new QueryRunner(dataSource);
		try {
			result = queryRunner.query(sql, new MapListHandler());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
}
