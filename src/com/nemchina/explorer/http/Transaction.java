package com.nemchina.explorer.http;

import java.util.List;
import java.util.Map;

import com.nemchina.explorer.db.DBOperator;

/** 
 * @Description: Transaction Util
 * @author lu
 * @date 2016年8月4日
 */ 
public class Transaction {

	/**
	 * create transaction
     * @param params
     * @return
     */
    public static void createTransaction(Object[] params){
    	String sql = "INSERT INTO NEM_TRANSACTION VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		DBOperator.update(sql, params);
    }
    
    /**
	 * query the max transaction no
     * @return
     */
    public static int queryMaxTransactionNO(){
    	String sql = "SELECT MAX(C_NO) as c_no FROM NEM_TRANSACTION";
		List<Map<String, Object>> list = DBOperator.query(sql);
		if(list.size()==0 || list.get(0)==null || list.get(0).get("c_no")==null){
			return 0;
		}
		return Double.valueOf(list.get(0).get("c_no").toString()).intValue();
    }
    
    /**
	 * query transactions
     * @param startIndex
     * @param endIndex
     * @return
     */
    public static List<Map<String, Object>> queryTransactions(int startIndex, int endIndex){
    	StringBuffer sql = new StringBuffer();
    	sql.append(" SELECT C_NO, C_HASH, C_HEIGHT, C_SENDER, C_RECIPIENT, C_AMOUNT, C_FEE, C_TIMESTAMP, C_DEADLINE, C_SIGNATURE, C_TYPE ");
    	sql.append(" FROM NEM_TRANSACTION ");
    	sql.append(" WHERE C_NO >= ? AND C_NO <= ? ");
    	sql.append(" ORDER BY C_NO DESC ");
		return DBOperator.query(sql.toString(), new Object[]{startIndex, endIndex});
    }
    
    /**
	 * query transaction by hash
     * @param hash
     * @return
     */
    public static Map<String, Object> queryTransactionByHash(String hash){
    	StringBuffer sql = new StringBuffer();
    	sql.append(" SELECT C_NO, C_HASH, C_HEIGHT, C_SENDER, C_RECIPIENT, C_AMOUNT, C_FEE, C_TIMESTAMP, C_DEADLINE, C_SIGNATURE, C_TYPE ");
    	sql.append(" FROM NEM_TRANSACTION ");
    	sql.append(" WHERE C_HASH = ? ");
		List<Map<String, Object>> list = DBOperator.query(sql.toString(), new Object[]{hash});
		if(list==null || list.size()==0){
			return null;
		}
		return list.get(0);
    }
    
}
