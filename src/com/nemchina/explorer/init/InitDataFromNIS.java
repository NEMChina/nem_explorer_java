package com.nemchina.explorer.init;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.nemchina.explorer.http.Account;
import com.nemchina.explorer.http.Block;
import com.nemchina.explorer.http.Namespace;
import com.nemchina.explorer.http.SuperNode;
import com.nemchina.explorer.http.Transaction;
import com.nemchina.explorer.util.CommonUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Fetch Data From NIS
 * @author lu
 * @date 2016年8月6日
 */ 
public class InitDataFromNIS extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private static boolean initEnd = false;
	private static int heightDB = 0;
	private static int heightNIS = 0;
	private static int lastLoadedHeight = 0;
	
	public void init() throws ServletException {
		try{
			//height from NIS
			heightNIS = Block.chainHeight();
			if(heightNIS==-1){
				return;
			}
			//height from Database
			heightDB = Block.chainHeightFromDatabase();
			if(heightDB<=440000){
				heightDB = 440000;
			}
			if(heightDB>=heightNIS){
				return;
			}
			new Thread(){
				public void run() {
					System.out.println("Init data start");
					System.out.println("height(NIS): " + heightNIS + "  height(database): " + heightDB);
					if(heightDB==0){
						loadNemesisBlock();
						loadBlocks(heightNIS, 1);
					} else {
						loadBlocks(heightNIS, heightDB);
					}
					initEnd = true;
					lastLoadedHeight = heightNIS;
					System.out.println("Int data end");
					System.out.println("Fetch data by schedule start");
				}
			}.start();
			//schedule fetch data from NIS
			ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
			pool.scheduleWithFixedDelay(new Runnable(){
				public void run() {
					if(initEnd){
						heightNIS = Block.chainHeight();
						if(heightNIS > lastLoadedHeight){
							loadBlocks(heightNIS, lastLoadedHeight);
							lastLoadedHeight = heightNIS;
							System.out.println("Fetch data by schedule block["+heightNIS+" - "+(lastLoadedHeight+1)+"]");
						}
					}
				};
			}, 20 * 1000, 20 * 1000, TimeUnit.MILLISECONDS);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Init Nemesis Block
	 */
	private void loadNemesisBlock(){
		JSONObject block = Block.blockAtPublic(1);
		if(block==null){
			return;
		}
		//create transactions
		JSONArray transactions = block.getJSONArray("transactions");
		JSONObject transaction = null;
		Object[] txParams = null;
		for(int i=0;i<transactions.size();i++){
			transaction = transactions.getJSONObject(i);
			if(transaction==null){
				continue;
			}
			txParams = new Object[11];
			txParams[0] = Transaction.queryMaxTransactionNO() + 1;
			txParams[1] = "";
			txParams[2] = 1;
			if(transaction.containsKey("signer")){
				txParams[3] = Account.getAccountAddressFromPublicKey(transaction.getString("signer"));
			} else {
				txParams[3] = "";
			}
			if(transaction.containsKey("recipient")){
				txParams[4] = transaction.getString("recipient");
			} else if(transaction.containsKey("remoteAccount")){ 
				txParams[4] = Account.getAccountAddressFromPublicKey(transaction.getString("remoteAccount"));
			} else {
				txParams[4] = "";
			}
			if(transaction.containsKey("amount")){
				txParams[5] = transaction.getLong("amount");
			} else {
				txParams[5] = 0;
			}
			if(transaction.containsKey("fee")){
				txParams[6] = transaction.getLong("fee");
			} else {
				txParams[6] = 0;
			}
			if(transaction.containsKey("timeStamp")){
				txParams[7] = transaction.getInt("timeStamp");
			} else {
				txParams[7] = 0;
			}
			if(transaction.containsKey("deadline")){
				txParams[8] = transaction.getInt("deadline");
			} else {
				txParams[8] = 0;
			}
			if(transaction.containsKey("signature")){
				txParams[9] = transaction.getString("signature");
			} else {
				txParams[9] = "";
			}
			if(transaction.containsKey("type")){
				txParams[10] = transaction.getString("type");
			} else {
				txParams[10] = 0;
			}
			Transaction.createTransaction(txParams);
			System.out.println("    Create transaction [" + (i+1) + "]");
			//create account
			if(transaction.containsKey("signer")){ //as a signer
				this.createOrUpdateAccount(null, transaction.getString("signer"), new HashSet<String>());
			}
			if(transaction.containsKey("recipient")){ //as a recipient
				this.createOrUpdateAccount(transaction.getString("recipient"), null, new HashSet<String>());
			}
			if(transaction.containsKey("cosignatoryAccount")){ //as a cosignatoryAccount
				this.createOrUpdateAccount(null, transaction.getString("cosignatoryAccount"), new HashSet<String>());
			}
			if(transaction.containsKey("otherAccount")){ //as a otherAccount
				this.createOrUpdateAccount(null, transaction.getString("otherAccount"), new HashSet<String>());
			}
		}
		System.out.println("Int Nemesis Block end");
	}
	
	/**
	 * Init Blocks
	 */
	public void loadBlocks(int heightNIS, int heightDB){
		Set<String> loadedAccountSet = new HashSet<String>();
		boolean isEnd = false;
		JSONArray blockArray = null;
		int height = heightDB;
		while(!isEnd){
			blockArray = Block.localChainBlocksAfter(height);
			if(blockArray==null || blockArray.size()==0){
				continue;
			}
			JSONObject block = null;
			JSONObject blockSub = null;
			Object[] blocks = (Object[]) blockArray.toArray();
			for(int i=0;i<blocks.length;i++){
				//create block
				block = (JSONObject) blocks[i];
				if(block==null){
					continue;
				}
				blockSub = block.getJSONObject("block");
				if(blockSub==null){
					continue;
				}
				if(blockSub.getInt("height")>=heightNIS){
					isEnd = true;
					break;
				}
				JSONArray txArray = block.getJSONArray("txes");
				//create or update account (block creator)
				if(blockSub.containsKey("signer")){
					this.createOrUpdateAccount(null, blockSub.getString("signer"), loadedAccountSet);
				}
				//create transactions
				JSONObject tx = null;
				JSONObject txSub = null;
				Object[] txParams = null;
				Object[] namespaceParams = null;
				for(int j=0;j<txArray.size();j++){
					tx = txArray.getJSONObject(j);
					if(tx==null){
						continue;
					}
					txSub = tx.getJSONObject("tx");
					if(txSub==null){
						continue;
					}
					txParams = new Object[11];
					txParams[0] = Transaction.queryMaxTransactionNO() + 1;
					txParams[1] = tx.getString("hash");
					txParams[2] = blockSub.getInt("height");
					if(txSub.containsKey("signer")){
						txParams[3] = Account.getAccountAddressFromPublicKey(txSub.getString("signer"));
					} else {
						txParams[3] = "";
					}
					if(txSub.containsKey("recipient")){
						txParams[4] = txSub.getString("recipient");
					} else if(txSub.containsKey("remoteAccount")){ 
						txParams[4] = Account.getAccountAddressFromPublicKey(txSub.getString("remoteAccount"));
					} else {
						txParams[4] = "";
					}
					if(txSub.containsKey("amount")){
						txParams[5] = txSub.getLong("amount");
					} else {
						txParams[5] = 0;
					}
					if(txSub.containsKey("fee")){
						txParams[6] = txSub.getLong("fee");
					} else {
						txParams[6] = 0;
					}
					if(txSub.containsKey("timeStamp")){
						txParams[7] = txSub.getInt("timeStamp");
					} else {
						txParams[7] = 0;
					}
					if(txSub.containsKey("deadline")){
						txParams[8] = txSub.getInt("deadline");
					} else {
						txParams[8] = 0;
					}
					if(txSub.containsKey("signature")){
						txParams[9] = txSub.getString("signature");
					} else {
						txParams[9] = "";
					}
					if(txSub.containsKey("type")){
						txParams[10] = txSub.getString("type");
					} else {
						txParams[10] = 0;
					}
					Transaction.createTransaction(txParams);
					//create namespace
					if(txSub.containsKey("type") && txSub.getInt("type")==8193){
						namespaceParams = new Object[6];
						namespaceParams[0] = Namespace.queryMaxNamespaceNO()+1;
						if(txSub.get("parent")==null || "null".equals(txSub.getString("parent"))){
							namespaceParams[1] = txSub.get("newPart");
						} else {
							namespaceParams[1] = txSub.getString("parent") + "." + txSub.get("newPart");
						}
						namespaceParams[2] = 0;
						namespaceParams[3] = txParams[7];
						namespaceParams[4] = txParams[2];
						namespaceParams[5] = txParams[3];
						Namespace.createNamespace(namespaceParams);
					}
					//update mosaics amount in specific namespace
					if(txSub.containsKey("type") && txSub.getInt("type")==16385){
						if(txSub.containsKey("mosaicDefinition") && txSub.getJSONObject("mosaicDefinition").containsKey("id")){
							String namespace = txSub.getJSONObject("mosaicDefinition").getJSONObject("id").getString("namespaceId");
							Namespace.updateNamespaceMosaicsAmount(namespace);
						}
					}
					//create supernode payout
					String payoutMessage = "";
					if(txParams[3].equals(SuperNode.superNodePayOutAccount) && txSub.containsKey("message")){
						JSONObject messageJSON = txSub.getJSONObject("message");
						if(messageJSON.containsKey("type") && messageJSON.getInt("type")==1){
							String message = CommonUtil.decodeMessage(CommonUtil.jsonString(messageJSON, "payload"));
							Pattern p = Pattern.compile("Node rewards payout: round (\\d+)-(\\d+)");
							Matcher m = p.matcher(message);
							if(m.find()){
								Object[] superNodeParams = new Object[7];
								superNodeParams[0] = SuperNode.querySuperNodePayOutMaxNO()+1;
								superNodeParams[1] = Integer.valueOf(m.group(2)).intValue();
								superNodeParams[2] = txParams[3];
								superNodeParams[3] = txParams[4];
								superNodeParams[4] = txParams[5];
								superNodeParams[5] = txParams[6];
								superNodeParams[6] = txParams[7];
								SuperNode.insertSuperNodePayOut(superNodeParams);
								payoutMessage = "payout round [" + Integer.valueOf(m.group(2)).intValue() + "]";
							}
						}
					}
					System.out.println("    Transaction [" + blockSub.getInt("height") + " - " + (j+1) + "] "+txSub.getInt("type")+ " " + payoutMessage);
					//create account
					if(txSub.containsKey("signer")){ //as a signer
						this.createOrUpdateAccount(null, txSub.getString("signer"), loadedAccountSet);
					}
					if(txSub.containsKey("recipient")){ //as a recipient
						this.createOrUpdateAccount(txSub.getString("recipient"), null, loadedAccountSet);
					}
					if(txSub.containsKey("cosignatoryAccount")){ //as a cosignatoryAccount
						this.createOrUpdateAccount(null, txSub.getString("cosignatoryAccount"), loadedAccountSet);
					}
					if(txSub.containsKey("otherAccount")){ //as a otherAccount
						this.createOrUpdateAccount(null, txSub.getString("otherAccount"), loadedAccountSet);
					}
				}
			}
			height += 10;
		}
	}
	
	/**
	 * create or update account
	 * @param account
	 * @param publicKey
	 */
	private void createOrUpdateAccount(String account, String publicKey, Set<String> loadedAccountSet){
		if(account==null && publicKey==null){
			return;
		}
		if(account!=null && loadedAccountSet.contains(account)){
			return;
		}
		if(publicKey!=null && loadedAccountSet.contains(publicKey)){
			return;
		}
		//get account info by NIS
		JSONObject accountJSON = null;
		Object[] params = new Object[8];
		params[7] = account;
		if(account!=null){
			loadedAccountSet.add(account);
			accountJSON = Account.accountGet(account);
		} else {
			loadedAccountSet.add(publicKey);
			accountJSON = Account.getAccountJSONFromPublicKey(publicKey);
		}
		if(accountJSON==null || !accountJSON.containsKey("account")){
			return;
		}
		accountJSON = accountJSON.getJSONObject("account");
		params[0] = CommonUtil.jsonString(accountJSON, "publicKey");
		params[1] = CommonUtil.jsonLong(accountJSON, "balance");
		params[2] = CommonUtil.jsonLong(accountJSON, "harvestedBlocks");
		params[6] = CommonUtil.jsonString(accountJSON, "label");
		//get account harvest info by NIS
		long fees = 0;
		int lastBlock = 0;
		int lastID = 0;
		JSONObject accountHarvestJSON = null;
		while(true){
			accountHarvestJSON = Account.accountHarvests(account, lastID);
			if(accountHarvestJSON==null || !accountHarvestJSON.containsKey("data")){
				break;
			}
			JSONArray accountHarvestArray = accountHarvestJSON.getJSONArray("data");
			if(accountHarvestArray.size()==0){
				break;
			}
			for(int i=0;i<accountHarvestArray.size();i++){
				JSONObject accountHarvestItem = accountHarvestArray.getJSONObject(i);
				if(!accountHarvestItem.containsKey("height")){
					continue;
				}
				if(lastBlock==0){
					lastBlock = CommonUtil.jsonInt(accountHarvestItem, "height");
				}
				fees += CommonUtil.jsonLong(accountHarvestItem, "totalFee");
				lastID = CommonUtil.jsonInt(accountHarvestItem, "id");
			}
		}
		params[3] = lastBlock;
		params[4] = fees;
		//query lastest transaction timeStamp
		long timeStamp = 0;
		JSONObject accountTransfersJSON = Account.accountTransfersAll(account);
		if(accountTransfersJSON==null || !accountTransfersJSON.containsKey("data")){
			return;
		}
		JSONArray accountTransfersArray = accountTransfersJSON.getJSONArray("data");
		for(int i=0;i<accountTransfersArray.size();i++){
			JSONObject accountTransfersItem = accountTransfersArray.getJSONObject(i);
			if(!accountTransfersItem.containsKey("transaction")){
				continue;
			}
			timeStamp = CommonUtil.jsonLong(accountTransfersItem.getJSONObject("transaction"), "timeStamp");
			break;
		}
		params[5] = timeStamp;
		if((account!=null && Account.checkIfAccountExist(account)) || (publicKey!=null && Account.checkIfAccountExistByPublicKey(publicKey))){ //update
			Account.updateAccount(params);
		} else { //create
			Account.createAccount(params);
		}
		System.out.println("    Account [" + account + "]");
	}
}
