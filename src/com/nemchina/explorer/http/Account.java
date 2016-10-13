package com.nemchina.explorer.http;

import java.util.List;
import java.util.Map;

import com.nemchina.explorer.db.DBOperator;

import net.sf.json.JSONObject;

/** 
 * @Description: Account Util
 * @author lu
 * @date 2016年8月4日
 */ 
public class Account {

    /**
     * query the account data from public key
     * @param publicKey
     * @return
     */
    public static String accountGetFromPublicKey(String publicKey){
    	return HttpUtil.httpGet("/account/get/from-public-key?publicKey="+publicKey);
    }
    
    /**
	 * Get Address From Public Key
	 * @param publicKey
	 * @return
	 */
	public static String getAccountAddressFromPublicKey(String publicKey){
		String accountString = Account.accountGetFromPublicKey(publicKey);
		if("".equals(accountString)){
			return "";
		}
		JSONObject account = JSONObject.fromObject(accountString);
		if(account==null){
			return "";
		}
		JSONObject accountSub = account.getJSONObject("account");
		if(accountSub==null || accountSub.getString("address")==null){
			return "";
		}
		return accountSub.getString("address");
	}
	
	/**
	 * Query the specific Account data by public key
	 * @param publicKey
	 * @return
	 */
	public static JSONObject getAccountJSONFromPublicKey(String publicKey){
		JSONObject accountJSON = null;
		String accountJSONString = Account.accountGetFromPublicKey(publicKey);
		if(accountJSONString==null || "".equals(accountJSONString.trim())){
			System.out.println("fail to get the Account data!");
			return null;
		}
		accountJSON = JSONObject.fromObject(accountJSONString);
		if(accountJSON==null){
			System.out.println("fail to get the Account data!");
			return null;
		}
		return accountJSON;
	}
	
	/**
     * Query the specific Account data by account
     * @param account
     * @return
     */
    public static JSONObject accountGet(String account){
    	JSONObject accountJSON = null;
		String accountJSONString = HttpUtil.httpGet("/account/get?address="+account);
		if(accountJSONString==null || "".equals(accountJSONString.trim())){
			System.out.println("fail to get the Account data!");
			return null;
		}
		accountJSON = JSONObject.fromObject(accountJSONString);
		if(accountJSON==null){
			System.out.println("fail to get the Account data!");
			return null;
		}
		return accountJSON;
    }
    
    /**
     * Query the specific Account Harvest data by account
     * @param account
     * @param id
     * @return
     */
    public static JSONObject accountHarvests(String account, int id){
    	JSONObject accountJSON = null;
    	String accountJSONString = null;
    	if(id == 0){
    		accountJSONString = HttpUtil.httpGet("/account/harvests?address="+account);
    	} else {
    		accountJSONString = HttpUtil.httpGet("/account/harvests?address="+account+"&id="+id);
    	}
		if(accountJSONString==null || "".equals(accountJSONString.trim())){
			System.out.println("fail to get the Account data!");
			return null;
		}
		accountJSON = JSONObject.fromObject(accountJSONString);
		if(accountJSON==null){
			System.out.println("fail to get the Account data!");
			return null;
		}
		return accountJSON;
    }
    
    /**
     * Query the specific Account Transfers
     * @param account
     * @return
     */
    public static JSONObject accountTransfersAll(String account){
    	JSONObject accountJSON = null;
    	String accountJSONString = HttpUtil.httpGet("/account/transfers/all?address="+account);
		if(accountJSONString==null || "".equals(accountJSONString.trim())){
			System.out.println("fail to get the Account data!");
			return null;
		}
		accountJSON = JSONObject.fromObject(accountJSONString);
		if(accountJSON==null){
			System.out.println("fail to get the Account data!");
			return null;
		}
		return accountJSON;
    }
    
    /**
     * Query the specific Account Transfers
     * @param account
     * @param id
     * @return
     */
    public static JSONObject accountTransfersAll(String account, String lastID){
    	JSONObject accountJSON = null;
    	String accountJSONString = HttpUtil.httpGet("/account/transfers/all?address="+account+"&id="+lastID);
		if(accountJSONString==null || "".equals(accountJSONString.trim())){
			System.out.println("fail to get the Account data!");
			return null;
		}
		accountJSON = JSONObject.fromObject(accountJSONString);
		if(accountJSON==null){
			System.out.println("fail to get the Account data!");
			return null;
		}
		return accountJSON;
    }
    
    /**
     * Query the specific Account outgoing
     * @param account
     * @return
     */
    public static JSONObject accountTransfersOutgoing(String account){
    	JSONObject accountJSON = null;
    	String accountJSONString = HttpUtil.httpGet("/account/transfers/outgoing?address="+account);
		if(accountJSONString==null || "".equals(accountJSONString.trim())){
			System.out.println("fail to get the Account data!");
			return null;
		}
		accountJSON = JSONObject.fromObject(accountJSONString);
		if(accountJSON==null){
			System.out.println("fail to get the Account data!");
			return null;
		}
		return accountJSON;
    }
    
    /**
     * Query the specific Account outgoing
     * @param account
     * @param id
     * @return
     */
    public static JSONObject accountTransfersOutgoing(String account, String lastID){
    	JSONObject accountJSON = null;
    	String accountJSONString = HttpUtil.httpGet("/account/transfers/outgoing?address="+account+"&id="+lastID);
		if(accountJSONString==null || "".equals(accountJSONString.trim())){
			System.out.println("fail to get the Account data!");
			return null;
		}
		accountJSON = JSONObject.fromObject(accountJSONString);
		if(accountJSON==null){
			System.out.println("fail to get the Account data!");
			return null;
		}
		return accountJSON;
    }
    
    /**
     * create account
     * @param params
     * @return
     */
    public static void createAccount(Object[] params){
    	String sql = "INSERT INTO NEM_ACCOUNT (C_PUBLICKEY, C_BALANCE, C_BLOCKS, C_LASTBLOCK, C_FEES, C_TIMESTAMP, C_LABEL, C_ACCOUNT) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		DBOperator.update(sql, params);
    }
    
    /**
     * update account
     * @param params
     * @return
     */
    public static void updateAccount(Object[] params){
    	String sql = "UPDATE NEM_ACCOUNT SET C_PUBLICKEY=?, C_BALANCE=?, C_BLOCKS=?, C_LASTBLOCK=?, C_FEES=?, C_TIMESTAMP=?, C_LABEL=? WHERE C_ACCOUNT=?";
		DBOperator.update(sql, params);
    }
    
    /**
     * check if account exists
     * @param account
     * @return
     */
    public static boolean checkIfAccountExist(String account){
    	String sql = "SELECT C_ACCOUNT FROM NEM_ACCOUNT WHERE C_ACCOUNT = ?";
		if(DBOperator.query(sql, account).size()>0){
			return true;
		} else {
			return false;
		}
    }
    
    /**
     * check if account exists by publicKey
     * @param publicKey
     * @return
     */
    public static boolean checkIfAccountExistByPublicKey(String publicKey){
    	String sql = "SELECT C_ACCOUNT FROM NEM_ACCOUNT WHERE C_PUBLICKEY = ?";
		if(DBOperator.query(sql, publicKey).size()>0){
			return true;
		} else {
			return false;
		}
    }
    
    /**
     * query account (paging)
     * @param index
     * @param amount
     * @return
     */
    public static List<Map<String, Object>> queryAccountByPaging(int index, int amount){
    	String sql = "SELECT C_ACCOUNT FROM NEM_ACCOUNT LIMIT ?, ?";
    	return DBOperator.query(sql, new Object[]{index, amount});
    }
    
    /**
     * query account (paging)
     * @param index
     * @param amount
     * @return
     */
    public static List<Map<String, Object>> queryAccountOrderByBalanceByPaging(int index, int amount){
    	String sql = "SELECT C_ACCOUNT, C_TIMESTAMP FROM NEM_ACCOUNT ORDER BY C_BALANCE DESC LIMIT ?, ?";
    	return DBOperator.query(sql, new Object[]{index, amount});
    }
    
    /**
     * query account (paging)
     * @param index
     * @param amount
     * @return
     */
    public static List<Map<String, Object>> queryAccountOrderByBlocksByPaging(int index, int amount){
    	String sql = "SELECT C_ACCOUNT, C_BLOCKS, C_LASTBLOCK, C_FEES, C_TIMESTAMP FROM NEM_ACCOUNT ORDER BY C_BLOCKS DESC LIMIT ?, ?";
    	return DBOperator.query(sql, new Object[]{index, amount});
    }
    
    /**
     * query account by address
     * @param address
     * @return
     */
    public static Map<String, Object> queryAccountByAddress(String address){
    	String sql = "SELECT C_ACCOUNT, C_BLOCKS, C_LASTBLOCK, C_FEES, C_TIMESTAMP FROM NEM_ACCOUNT WHERE C_ACCOUNT = ?";
    	List<Map<String, Object>> list = DBOperator.query(sql, new Object[]{address});
    	if(list.size()==0){
    		return null;
    	}
    	return list.get(0);
    }
}
