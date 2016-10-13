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

import com.nemchina.explorer.http.Account;
import com.nemchina.explorer.util.CommonUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Load Account Servlet
 * @author lu
 * @date 2016年8月15日
 */ 
@WebServlet("/accountList")
public class AccountListServlet extends HttpServlet {
	
	private static final int LISTAMOUNT = 100;
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONArray outputAccountListJson = new JSONArray();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			List<Map<String, Object>> accountListFromDB = Account.queryAccountOrderByBalanceByPaging(0, LISTAMOUNT);
			JSONObject outputAccountJson = null;
			DecimalFormat poiFormat = new DecimalFormat("0.#####%");
			DecimalFormat balanceFormat = new DecimalFormat("0.##");
			for(Map<String, Object> accountMap:accountListFromDB){
				outputAccountJson = new JSONObject();
				if(accountMap==null || !accountMap.containsKey("c_account")){
					continue;
				}
				String account = CommonUtil.mapString(accountMap, "c_account");
				JSONObject accountFromNIS = Account.accountGet(account);
				if(accountFromNIS==null || !accountFromNIS.containsKey("account")){
					continue;
				}
				accountFromNIS = accountFromNIS.getJSONObject("account");
				outputAccountJson.put("account", account);
				outputAccountJson.put("importance", poiFormat.format(accountFromNIS.getDouble("importance")));
				outputAccountJson.put("balance", balanceFormat.format(CommonUtil.jsonLong(accountFromNIS, "balance")/1000000));
				outputAccountJson.put("timeStamp", CommonUtil.mapInt(accountMap, "c_timestamp"));
				outputAccountListJson.add(outputAccountJson);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		out.print(outputAccountListJson);
		return;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
