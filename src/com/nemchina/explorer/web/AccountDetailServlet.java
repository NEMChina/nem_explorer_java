package com.nemchina.explorer.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nemchina.explorer.http.Account;
import com.nemchina.explorer.util.CommonUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Load Account Detail Servlet
 * @author lu
 * @date 2016年8月21日
 */ 
@WebServlet("/accountDetail")
public class AccountDetailServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String account = CommonUtil.checkString(request.getParameter("account"));
		JSONObject outputAccountJson = new JSONObject();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			if("".equals(account)){
				out.print(outputAccountJson);
				return;
			}
			account = account.replaceAll("-", "");
			JSONObject accountJSON = Account.accountGet(account);
			if(accountJSON==null || !accountJSON.containsKey("account") || !accountJSON.containsKey("meta")){
				out.print(outputAccountJson);
				return;
			}
			//query account info
			JSONObject meta = accountJSON.getJSONObject("meta");
			JSONObject accountSub = accountJSON.getJSONObject("account");
			DecimalFormat poiFormat = new DecimalFormat("0.#####%");
			DecimalFormat decimalFormat = new DecimalFormat("0.##");
			outputAccountJson.put("account", CommonUtil.jsonString(accountSub, "address"));
			outputAccountJson.put("publicKey", CommonUtil.jsonString(accountSub, "publicKey"));
			outputAccountJson.put("balance", decimalFormat.format(CommonUtil.jsonLong(accountSub, "balance")/1000000));
			outputAccountJson.put("importance", poiFormat.format(CommonUtil.jsonDouble(accountSub, "importance")));
			Map<String, Object> accountMap = Account.queryAccountByAddress(account);
			if(accountMap!=null){
				outputAccountJson.put("timeStamp", CommonUtil.mapLong(accountMap, "c_timestamp"));
				outputAccountJson.put("blocks", CommonUtil.mapInt(accountMap, "c_blocks"));
				outputAccountJson.put("fees", decimalFormat.format(CommonUtil.mapLong(accountMap, "c_fees")));
			}
			String label = CommonUtil.jsonString(accountSub, "label");
			if("null".equals(label)){
				label = "";
			}
			outputAccountJson.put("label", label);
			outputAccountJson.put("remoteStatus", CommonUtil.jsonString(meta, "remoteStatus"));
			if(meta.containsKey("cosignatories") && meta.getJSONArray("cosignatories").size()>0){
				outputAccountJson.put("multisig", 1);
				StringBuffer cosignatories = new StringBuffer();
				for(int i=0;i<meta.getJSONArray("cosignatories").size();i++){
					JSONObject cosignatory = meta.getJSONArray("cosignatories").getJSONObject(i);
					if("".equals(cosignatories.toString())){
						cosignatories.append(CommonUtil.jsonString(cosignatory, "address"));
					} else {
						cosignatories.append("<br/>").append(CommonUtil.jsonString(cosignatory, "address"));
					}
				}
				outputAccountJson.put("cosignatories", cosignatories.toString());
			}
			//query transactions of this account
			JSONObject accountTransfersAll = Account.accountTransfersAll(account);
			JSONArray accountTransactions = new JSONArray();
			JSONObject outAccountTx = null;
			JSONArray outAccountArray = new JSONArray();
			if(accountTransfersAll!=null && accountTransfersAll.containsKey("data")){
				accountTransactions = accountTransfersAll.getJSONArray("data");
				for(int i=0;i<accountTransactions.size();i++){
					if(!accountTransactions.getJSONObject(i).containsKey("meta") || !accountTransactions.getJSONObject(i).containsKey("transaction")){
						continue;
					}
					JSONObject transactionMeta = accountTransactions.getJSONObject(i).getJSONObject("meta");
					JSONObject transaction = accountTransactions.getJSONObject(i).getJSONObject("transaction");
					outAccountTx = new JSONObject();
					outAccountTx.put("id", CommonUtil.jsonInt(transactionMeta, "id"));
					outAccountTx.put("timeStamp", CommonUtil.jsonInt(transaction, "timeStamp"));
					outAccountTx.put("amount", decimalFormat.format(CommonUtil.jsonLong(transaction, "amount")/1000000));
					outAccountTx.put("fee", decimalFormat.format(CommonUtil.jsonLong(transaction, "fee")/1000000));
					outAccountTx.put("sender", Account.getAccountAddressFromPublicKey(CommonUtil.jsonString(transaction, "signer")));
					outAccountTx.put("recipient", CommonUtil.jsonString(transaction, "recipient"));
					outAccountTx.put("height", CommonUtil.jsonInt(transactionMeta, "height"));
					outAccountTx.put("signature", CommonUtil.jsonString(transaction, "signature"));
					if(transactionMeta.containsKey("hash") || transactionMeta.getJSONObject("hash").containsKey("data")){
						outAccountTx.put("hash", CommonUtil.jsonString(transactionMeta.getJSONObject("hash"), "data"));
					}
					outAccountArray.add(outAccountTx);
				}
				outputAccountJson.put("txes", outAccountArray);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		out.print(outputAccountJson);
		return;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
