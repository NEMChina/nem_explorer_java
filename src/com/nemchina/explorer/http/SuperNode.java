package com.nemchina.explorer.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nemchina.explorer.db.DBOperator;
import com.nemchina.explorer.util.CommonUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: SuperNode Util
 * @author lu
 * @date 2016年8月20日
 */ 
public class SuperNode {

	public static String superNodeURL = "http://supernodes.nem.io";
	public static JSONArray superNodes = null;
	public static Map<String, String> superNodeHostMap = null;
	public static Map<String, String> superNodeNameMap = null;
	public static String superNodePayOutAccount = "NCPAYOUTH2BGEGT3Q7K75PV27QKMVNN2IZRVZWMD";
	
	public static JSONArray getSuperNodes(){
		if(superNodes==null){
			fetchSuperNodes();
		}
		return superNodes;
	}
	
	public static Map<String, String> getSuperNodeHostMap(){
		if(superNodeHostMap==null){
			fetchSuperNodes();
		}
		return superNodeHostMap;
	}
	
	public static Map<String, String> getSuperNodeNameMap(){
		if(superNodeNameMap==null){
			fetchSuperNodes();
		}
		return superNodeNameMap;
	}
	
	public static void fetchSuperNodes(){
		try{
			//fetch supernode info from URL
			String content = HttpUtil.httpGetByFullURL(superNodeURL);
			//match the supernode properties item
			content = content.replaceAll("\r\n", "");
			content = content.replaceAll(">(\\s)+<", "><");
			StringBuffer regex = new StringBuffer();
			regex.append("<tr>");
			regex.append("<td scope=\"row\"><a href=\"details/(\\d+)\" style=\"color:#337AB7\">(\\d+)</a></td>");
			regex.append("<td>(.{1,50})</td>");
			regex.append("<td><a href=\"details/(\\d+)\" style=\"color:#DD4814\">(.{1,50})</a></td>");
			regex.append("<td style=\"color:(green|red)\">(Active|Deactivated)</td>");
			regex.append("</tr>");
			Pattern p = Pattern.compile(regex.toString());
			Matcher m = p.matcher(content);
			//save supernode info to the static vars
			superNodes = new JSONArray();
			superNodeHostMap = new HashMap<String, String>();
			superNodeNameMap = new HashMap<String, String>();
			JSONObject superNode = null;
			while (m.find()) {
				if(!"Active".equals(m.group(7).trim())){
					continue;
				}
				superNode = new JSONObject();
				superNode.put("id", m.group(1).trim());
				superNode.put("host", m.group(3).trim());
				superNode.put("name", m.group(5).trim());
				superNodes.add(superNode);
				superNodeHostMap.put(m.group(3).trim(), m.group(1).trim());
				superNodeNameMap.put(m.group(5).trim(), m.group(1).trim());
	        }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void fetchSuperNodePayOut(){
		System.out.println("    Supernode payout start ");
		int maxRoundInDB = querySuperNodePayOutMaxRound();
		System.out.println("    Supernode payout start [" + maxRoundInDB + "]");
		String lastID = null;
		JSONObject accountTransfersAll = null;
		while(true){
			if(lastID==null){
				accountTransfersAll = Account.accountTransfersOutgoing(SuperNode.superNodePayOutAccount);
			} else {
				accountTransfersAll = Account.accountTransfersOutgoing(SuperNode.superNodePayOutAccount, lastID);
			}
			if(accountTransfersAll==null || !accountTransfersAll.containsKey("data")){
				return;
			}
			JSONArray accountTransactions = accountTransfersAll.getJSONArray("data");
			if(accountTransactions.size()==0){
				return;
			}
			JSONObject accountTX = null;
			String message = null;
			JSONObject messageJSON = null;
			for(int i=0;i<accountTransactions.size();i++){
				accountTX = accountTransactions.getJSONObject(i);
				if(!accountTX.containsKey("meta") || !accountTX.containsKey("transaction")){
					continue;
				}
				messageJSON = accountTX.getJSONObject("transaction").getJSONObject("message");
				if(!messageJSON.containsKey("payload") || CommonUtil.jsonInt(messageJSON, "type")!=1){ //unencrypted
					continue;
				}
				message = CommonUtil.decodeMessage(CommonUtil.jsonString(messageJSON, "payload"));
				Pattern p = Pattern.compile("Node rewards payout: round (\\d+)-(\\d+)");
				Matcher m = p.matcher(message);
				if (m.find()) {
					if(Integer.valueOf(m.group(2)).intValue() <= maxRoundInDB){
						return;
					} else {
						Object[] params = new Object[7];
						params[0] = querySuperNodePayOutMaxNO()+1;
						params[1] = Integer.valueOf(m.group(2)).intValue();
						params[2] = Account.getAccountAddressFromPublicKey(CommonUtil.jsonString(accountTX.getJSONObject("transaction"), "signer"));
						params[3] = CommonUtil.jsonString(accountTX.getJSONObject("transaction"), "recipient");
						params[4] = CommonUtil.jsonLong(accountTX.getJSONObject("transaction"), "amount");
						params[5] = CommonUtil.jsonLong(accountTX.getJSONObject("transaction"), "fee");
						params[6] = CommonUtil.jsonInt(accountTX.getJSONObject("transaction"), "timeStamp");
						lastID = "" + CommonUtil.jsonInt(accountTX.getJSONObject("meta"), "id");
						insertSuperNodePayOut(params);
						System.out.println("    Supernode payout round [" + m.group(1)  + " - " + m.group(2) + "] to " 
								+ CommonUtil.jsonString(accountTX.getJSONObject("transaction"), "recipient"));
					}
				}
			}
		}
	}
	
	public static int querySuperNodePayOutMaxRound(){
		String sql = "SELECT MAX(C_ROUND) maxround FROM NEM_SUPERNODE_PAYOUT";
    	List<Map<String, Object>> list = DBOperator.query(sql);
    	if(list==null || list.size()==0){
    		return 0;
    	}
    	return CommonUtil.mapInt(list.get(0), "maxround");
	}
	
	public static int querySuperNodePayOutMaxNO(){
		String sql = "SELECT MAX(C_NO) no FROM NEM_SUPERNODE_PAYOUT";
    	List<Map<String, Object>> list = DBOperator.query(sql);
    	if(list==null || list.size()==0){
    		return 0;
    	}
    	return CommonUtil.mapInt(list.get(0), "no");
	}
	
	public static void insertSuperNodePayOut(Object[] params){
		String sql = "INSERT INTO NEM_SUPERNODE_PAYOUT VALUES (?, ?, ?, ?, ?, ?, ?)";
		DBOperator.update(sql, params);
	}
	
	public static List<Map<String, Object>> querySuperNodePayOutRounds(int amount){
		String sql = "SELECT C_ROUND FROM NEM_SUPERNODE_PAYOUT GROUP BY C_ROUND ORDER BY C_ROUND DESC LIMIT 0, ?";
    	return DBOperator.query(sql, new Object[]{amount});
	}
	
	public static List<Map<String, Object>> querySuperNodePayOut(int round){
		String sql = "SELECT C_ROUND, C_SENDER, C_RECIPIENT, C_AMOUNT, C_FEE, C_TIMESTAMP FROM NEM_SUPERNODE_PAYOUT WHERE C_ROUND = ?";
    	return DBOperator.query(sql, new Object[]{round});
	}
}
