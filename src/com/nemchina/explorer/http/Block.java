package com.nemchina.explorer.http;

import java.util.List;
import java.util.Map;

import com.nemchina.explorer.db.DBOperator;
import com.nemchina.explorer.util.CommonUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Block Util
 * @author lu
 * @date 2016年8月4日
 */ 
public class Block {

    /**
     * Query part of a chain (equal or less than 10 blocks)
     * @param height
     * @return
     */
    public static JSONArray localChainBlocksAfter(int height){
    	JSONObject urlParam = new JSONObject();
		urlParam.put("height", height);
		String blockString = HttpUtil.httpPostWithJSON("/local/chain/blocks-after", urlParam.toString());
		if(blockString==null || "".equals(blockString.trim())){
			System.out.println("fail to get the blocks ["+(height+1)+" - "+(height+11)+"]");
			return null;
		}
		JSONObject partChain = JSONObject.fromObject(blockString);
		if(partChain==null){
			System.out.println("fail to get the blocks ["+(height+1)+" - "+(height+11)+"]");
			return null;
		}
		JSONArray blockArr = partChain.getJSONArray("data");
		if(blockArr==null || blockArr.size()==0){
			return null;
		}
		return blockArr;
    }
    
    /**
     * Query the specific block data
     * @param height
     * @return
     */
    public static JSONObject blockAtPublic(int height){
    	JSONObject block = null;
    	JSONObject urlParam = new JSONObject();
		urlParam.put("height", height);
		String blockString = HttpUtil.httpPostWithJSON("/block/at/public", urlParam.toString());
		if(blockString==null || "".equals(blockString.trim())){
			System.out.println("fail to get the Nemesis block data!");
			return null;
		}
		block = JSONObject.fromObject(blockString);
		if(block==null){
			System.out.println("fail to get the Nemesis block data!");
			return null;
		}
		return block;
    }
    
    /**
     * Query current height from NIS
     * @param height
     * @return
     */
    public static int chainHeight(){
    	//query current height from NIS
		String heightString = HttpUtil.httpGet("/chain/height");
		if(heightString==null || "".equals(heightString.trim())){
			System.out.println("fail to get current height from NIS!");
			return -1;
		}
		JSONObject heightJSON = JSONObject.fromObject(heightString);
		if(heightJSON==null){
			System.out.println("fail to get current height from NIS!");
			return -1;
		}
		return heightJSON.getInt("height");
    }
    
    /**
     * Query current height from Database
     * @param height
     * @return
     */
    public static int chainHeightFromDatabase(){
    	int blockHeightInDatabase = 0;
		String sql = "SELECT MAX(C_HEIGHT) height FROM NEM_TRANSACTION";
		List<Map<String, Object>> blockList = DBOperator.query(sql);
		if(blockList.size()!=0){
			Map<String, Object> block = blockList.get(0);
			blockHeightInDatabase = CommonUtil.checkInt(block.get("height"));
		}
		return blockHeightInDatabase;
    }
    
    /**
     * create new block
     * @param height
     * @return
     */
    public static void createBlock(Object[] params){
    	String sql = "INSERT INTO NEM_BLOCK VALUES (?, ?, ?)";
		DBOperator.update(sql, params);
    }
    
    /**
     * create new block by batch
     * @param height
     * @return
     */
    public static void createBlock(Object[][] params){
    	String sql = "INSERT INTO NEM_BLOCK VALUES (?, ?, ?)";
		DBOperator.updateBatch(sql, params);
    }
    
}
