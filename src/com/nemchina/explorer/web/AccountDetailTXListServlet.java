package com.nemchina.explorer.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

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
 * @Description: Load Account Detail TX List Servlet
 * @author lu
 * @date 2016年10月24日
 */ 
@WebServlet("/accountDetailTXList")
public class AccountDetailTXListServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String account = CommonUtil.checkString(request.getParameter("account"));
		String lastID = CommonUtil.checkString(request.getParameter("lastID"));
		JSONArray outputTXListJson = new JSONArray();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			if("".equals(account)){
				out.print(outputTXListJson);
				return;
			}
			account = account.replaceAll("-", "");
			//query transactions of this account
			DecimalFormat decimalFormat = new DecimalFormat("0.##");
			JSONObject accountTransfersAll = Account.accountTransfersAll(account, lastID);
			JSONArray accountTransactions = new JSONArray();
			JSONObject outAccountTx = null;
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
					outputTXListJson.add(outAccountTx);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		out.print(outputTXListJson);
		return;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
