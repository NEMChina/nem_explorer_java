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

import org.apache.commons.lang.StringUtils;

import com.nemchina.explorer.http.Account;
import com.nemchina.explorer.http.Block;
import com.nemchina.explorer.http.Transaction;
import com.nemchina.explorer.util.CommonUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Load Transaction Detail Servlet
 * @author lu
 * @date 2016年8月12日
 */ 
@WebServlet("/txDetail")
public class TXDetailServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int height = CommonUtil.convertStrToInt(request.getParameter("height"));
		String signature = CommonUtil.checkString(request.getParameter("signature"));
		String hash = CommonUtil.checkString(request.getParameter("hash"));
		JSONObject outputTransactionJson = new JSONObject();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			if(!StringUtils.isBlank(hash)){
				Map<String, Object> map = Transaction.queryTransactionByHash(hash);
				if(map!=null){
					height = CommonUtil.mapInt(map, "c_height");
					signature = CommonUtil.mapString(map, "c_signature");
				}
			}
			if(height<=0){
				out.print(outputTransactionJson);
				return;
			}
			JSONObject block = Block.blockAtPublic(height);
			if(block==null || !block.containsKey("transactions")){
				out.print(outputTransactionJson);
				return;
			}
			JSONArray transactionsInBlock = block.getJSONArray("transactions");
			JSONObject transaction = null;
			for(int i=0;i<transactionsInBlock.size();i++){
				transaction = transactionsInBlock.getJSONObject(i);
				if(transaction==null || !transaction.containsKey("signature")){
					continue;
				}
				if(signature.equals(transaction.getString("signature"))){
					DecimalFormat decimalFormat = new DecimalFormat("0.##");
					transaction.put("sender", Account.getAccountAddressFromPublicKey(transaction.getString("signer")));
					transaction.put("amount", decimalFormat.format(CommonUtil.jsonLong(transaction, "amount")/1000000));
					transaction.put("fee", decimalFormat.format(CommonUtil.jsonLong(transaction, "fee")/1000000));
					transaction.put("height", height);
					if(transaction.containsKey("message")){ //handle message
						JSONObject messageJSON = transaction.getJSONObject("message");
						if(messageJSON!=null && messageJSON.containsKey("type")){
							if(messageJSON.getInt("type")==1){ //unencrypted
								transaction.put("messageType", 1);
								transaction.put("messageContent", CommonUtil.decodeMessage(CommonUtil.jsonString(messageJSON, "payload")));
							} else if(messageJSON.getInt("type")==1) { //encrypted
								transaction.put("messageType", 2);
								transaction.put("messageContent", CommonUtil.jsonString(messageJSON, "payload"));
							}
						}
					}
					//type - 2049
					if(transaction.containsKey("remoteAccount")){
						transaction.put("remoteAccount", Account.getAccountAddressFromPublicKey(CommonUtil.jsonString(transaction, "remoteAccount")));
					}
					//type - 4097
					if(transaction.containsKey("modifications")){
						JSONArray cosignatoryArray = transaction.getJSONArray("modifications");
						for(int j=0;j<cosignatoryArray.size();j++){
							JSONObject cosignatory = cosignatoryArray.getJSONObject(j);
							cosignatory.put("cosignatoryAccount", Account.getAccountAddressFromPublicKey(CommonUtil.jsonString(cosignatory, "cosignatoryAccount")));
						}
					}
					//type - 4100
					if(transaction.containsKey("otherTrans")){
						JSONObject otherTrans = transaction.getJSONObject("otherTrans");
						otherTrans.put("sender", Account.getAccountAddressFromPublicKey(otherTrans.getString("signer")));
						if(otherTrans.containsKey("modifications")){
							JSONArray modifications = otherTrans.getJSONArray("modifications");
							JSONObject modification = null;
							for(int j=0;j<modifications.size();j++){
								modification = modifications.getJSONObject(j);
								modification.put("cosignatoryAccount", Account.getAccountAddressFromPublicKey(modification.getString("cosignatoryAccount")));
							}
						}
						if(otherTrans.containsKey("fee")){
							otherTrans.put("fee", decimalFormat.format(CommonUtil.jsonLong(otherTrans, "fee")/1000000));
						}
						otherTrans.put("recipient", CommonUtil.jsonString(otherTrans, "recipient"));
					}
					if(transaction.containsKey("signatures")){
						JSONArray signatures = transaction.getJSONArray("signatures");
						JSONObject sign = null;
						for(int j=0;j<signatures.size();j++){
							sign = signatures.getJSONObject(j);
							sign.put("sender", Account.getAccountAddressFromPublicKey(sign.getString("signer")));
						}
					}
					outputTransactionJson =  transaction;
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		out.print(outputTransactionJson);
		return;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
