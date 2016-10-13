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

import com.nemchina.explorer.http.SuperNode;
import com.nemchina.explorer.util.CommonUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Load Supernode Payout List Servlet
 * @author lu
 * @date 2016年8月24日
 */ 
@WebServlet("/supernodePayoutList")
public class SuperNodePayoutListServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int round = CommonUtil.convertStrToInt(request.getParameter("round"));
		JSONArray outputRoundListJson = new JSONArray();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			List<Map<String, Object>> payoutList = SuperNode.querySuperNodePayOut(round);
			JSONObject outputPayoutJson = null;
			DecimalFormat decimalFormat = new DecimalFormat("0.##");
			for(Map<String, Object> payoutMap:payoutList){
				if(payoutMap==null){
					continue;
				}
				outputPayoutJson = new JSONObject();
				outputPayoutJson.put("round", CommonUtil.mapString(payoutMap, "c_round"));
				outputPayoutJson.put("sender", CommonUtil.mapString(payoutMap, "c_sender"));
				outputPayoutJson.put("recipient", CommonUtil.mapString(payoutMap, "c_recipient"));
				outputPayoutJson.put("amount", decimalFormat.format(CommonUtil.mapLong(payoutMap, "c_amount")/1000000));
				outputPayoutJson.put("fee", decimalFormat.format(CommonUtil.mapLong(payoutMap, "c_fee")/1000000));
				outputPayoutJson.put("timeStamp", CommonUtil.mapString(payoutMap, "c_timestamp"));
				outputRoundListJson.add(outputPayoutJson);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		out.print(outputRoundListJson);
		return;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
