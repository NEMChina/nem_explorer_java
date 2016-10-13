package com.nemchina.explorer.web;

import java.io.IOException;
import java.io.PrintWriter;
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
 * @Description: Load Supernode Payout Round List Servlet
 * @author lu
 * @date 2016年8月24日
 */ 
@WebServlet("/supernodePayoutRoundList")
public class SuperNodePayoutRoundListServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final int ROUNDAMOUNT = 10;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONArray outputRoundListJson = new JSONArray();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			List<Map<String, Object>> roundList = SuperNode.querySuperNodePayOutRounds(ROUNDAMOUNT);
			JSONObject outputRoundJson = null;
			for(Map<String, Object> roundMap:roundList){
				if(roundMap==null){
					continue;
				}
				outputRoundJson = new JSONObject();
				int round = CommonUtil.mapInt(roundMap, "c_round");
				outputRoundJson.put("key", "" + (round-3) + "-" + round);
				outputRoundJson.put("value", round);
				outputRoundListJson.add(outputRoundJson);
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
