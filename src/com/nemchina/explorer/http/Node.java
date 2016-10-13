package com.nemchina.explorer.http;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Node Util
 * @author lu
 * @date 2016年8月20日
 */ 
public class Node {

    /**
     * Query peer-list reachable nodes
     * @return
     */
    public static JSONArray nodePeerListReachable(){
    	JSONObject result = null;
		String blockString = HttpUtil.httpGet("/node/peer-list/reachable");
		if(blockString==null || "".equals(blockString.trim())){
			System.out.println("fail to get the nodes data!");
			return null;
		}
		result = JSONObject.fromObject(blockString);
		if(result==null || !result.containsKey("data")){
			System.out.println("fail to get the nodes data!");
			return null;
		}
		return result.getJSONArray("data");
    }
    
    /**
     * Query peer-list active nodes
     * @return
     */
    public static JSONArray nodePeerListActive(){
    	JSONObject result = null;
		String blockString = HttpUtil.httpGet("/node/peer-list/active");
		if(blockString==null || "".equals(blockString.trim())){
			System.out.println("fail to get the nodes data!");
			return null;
		}
		result = JSONObject.fromObject(blockString);
		if(result==null || !result.containsKey("data")){
			System.out.println("fail to get the nodes data!");
			return null;
		}
		return result.getJSONArray("data");
    }
}
