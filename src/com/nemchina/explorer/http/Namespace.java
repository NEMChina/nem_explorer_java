package com.nemchina.explorer.http;

import java.util.List;
import java.util.Map;

import com.nemchina.explorer.db.DBOperator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Namespace Util
 * @author lu
 * @date 2016年10月19日
 */ 
public class Namespace {
	
	/**
	 * query the max namespace no
     * @return
     */
    public static int queryMaxNamespaceNO(){
    	String sql = "SELECT MAX(C_NO) as c_no FROM NEM_NAMESPACE";
		List<Map<String, Object>> list = DBOperator.query(sql);
		if(list.size()==0 || list.get(0)==null || list.get(0).get("c_no")==null){
			return 0;
		}
		return Double.valueOf(list.get(0).get("c_no").toString()).intValue();
    }
	
    /**
     * create namespace
     * @param params
     */
    public static void createNamespace(Object[] params){
    	String sql = "INSERT INTO NEM_NAMESPACE (C_NO, C_NAME, C_MOSAICS, C_TIMESTAMP, C_HEIGHT, C_CREATOR) VALUES (?, ?, ?, ?, ?, ?)";
		DBOperator.update(sql, params);
    }
    
    /**
     * query namespace by paging
     * @param index
     * @param amount
     * @return
     */
    public static List<Map<String, Object>> queryNamespaceByPaging(int index, int amount){
    	String sql = "SELECT C_NO, C_NAME, C_MOSAICS, C_TIMESTAMP, C_HEIGHT, C_CREATOR FROM NEM_NAMESPACE ORDER BY C_TIMESTAMP DESC LIMIT ?, ?";
    	return DBOperator.query(sql, new Object[]{index, amount});
    }
    
    /**
     * update namespace mosaics amount
     * @param namespace
     */
    public static void updateNamespaceMosaicsAmount(String namespace){
    	String sql = "UPDATE NEM_NAMESPACE SET C_MOSAICS = C_MOSAICS + 1 WHERE C_NAME = ?";
		DBOperator.update(sql, new Object[]{namespace});
    }
    
    /**
     * query the mosaic data from namespace
     * @param publicKey
     * @return
     */
    public static JSONArray mosaicListFromNamespace(String namespace){
    	String mosaicString = HttpUtil.httpGet("/namespace/mosaic/definition/page?namespace="+namespace);
    	if(mosaicString==null || "".equals(mosaicString.trim())){
			System.out.println("fail to get the Nemesis block data!");
			return null;
		}
    	return JSONObject.fromObject(mosaicString).getJSONArray("data");
    }
	
}
