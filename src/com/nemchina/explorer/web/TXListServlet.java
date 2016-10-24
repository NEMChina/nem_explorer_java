package com.nemchina.explorer.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nemchina.explorer.http.Transaction;
import com.nemchina.explorer.util.CommonUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Load Transactions Servlet
 * @author lu
 * @date 2016年8月7日
 */ 
@WebServlet("/txList")
public class TXListServlet extends HttpServlet {
	
	private static final int LISTAMOUNT= 10;
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//paging
		int page = CommonUtil.getPageParamFromRequest(request, "page");
		JSONArray outputTransactionListJson = new JSONArray();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			//get max transactions no from NIS
			int maxTransactioinNO = Transaction.queryMaxTransactionNO();
			if(maxTransactioinNO<=0){
				out.print(outputTransactionListJson);
				return;
			}
			int endIndex = maxTransactioinNO - (LISTAMOUNT*(page-1));
			int startIndex = maxTransactioinNO - (LISTAMOUNT*(page)) + 1;
			if(endIndex<1){
				endIndex = 1;
			}
			if(startIndex<1){
				endIndex = 1;
			}
			DecimalFormat decimalFormat = new DecimalFormat("0.##");
			List<Map<String, Object>> transactionList = Transaction.queryTransactions(startIndex, endIndex);
			JSONObject outputTransactionJSON = null;
			Map<String, Object> transactionMap = null;
			for(int i=0;i<transactionList.size();i++){
				//validate transaction
				if(transactionList.get(i)==null){
					continue;
				}
				transactionMap = transactionList.get(i);
				outputTransactionJSON = new JSONObject();
				outputTransactionJSON.put("no", CommonUtil.mapInt(transactionMap, "c_no"));
				outputTransactionJSON.put("hash", CommonUtil.mapString(transactionMap, "c_hash"));
				outputTransactionJSON.put("height", CommonUtil.mapInt(transactionMap, "c_height"));
				outputTransactionJSON.put("sender", CommonUtil.mapString(transactionMap, "c_sender"));
				outputTransactionJSON.put("recipient", CommonUtil.mapString(transactionMap, "c_recipient"));
				outputTransactionJSON.put("amount", decimalFormat.format(CommonUtil.mapLong(transactionMap, "c_amount")/1000000));
				outputTransactionJSON.put("fee", decimalFormat.format(CommonUtil.mapLong(transactionMap, "c_fee")/1000000));
				outputTransactionJSON.put("timeStamp", CommonUtil.mapInt(transactionMap, "c_timeStamp"));
				outputTransactionJSON.put("signature", CommonUtil.mapString(transactionMap, "c_signature"));
				outputTransactionJSON.put("type", CommonUtil.mapInt(transactionMap, "c_type"));
				outputTransactionListJson.add(outputTransactionJSON);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		out.print(outputTransactionListJson);
		return;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
