package com.nemchina.explorer.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nemchina.explorer.http.Node;
import com.nemchina.explorer.http.SuperNode;
import com.nemchina.explorer.util.CommonUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Load Node Servlet
 * @author lu
 * @date 2016年8月20日
 */ 
@WebServlet("/nodeList")
public class NodeListServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONArray outputAccountListJson = new JSONArray();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			JSONArray nodes = Node.nodePeerListReachable();
			JSONObject node = null;
			JSONObject outputAccountJson = null;
			JSONObject metaData = null;
			JSONObject endpoint = null;
			JSONObject identity = null;
			Map<String, String> superNodeHostMap = SuperNode.getSuperNodeHostMap();
			Map<String, String> superNodeNameMap = SuperNode.getSuperNodeNameMap();
			for(int i=0;i<nodes.size();i++){
				node = nodes.getJSONObject(i);
				if(node==null || !node.containsKey("metaData") || !node.containsKey("endpoint") || !node.containsKey("identity")){
					continue;
				}
				metaData = node.getJSONObject("metaData");
				endpoint = node.getJSONObject("endpoint");
				identity = node.getJSONObject("identity");
				outputAccountJson = new JSONObject();
				String host = CommonUtil.jsonString(endpoint, "host");
				if(host.matches("[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}")){
					host = host.substring(0, host.lastIndexOf(".")+1) + "XX";
				} else {
					host = "XX" + host.substring(host.indexOf("."), host.length());
				}
				outputAccountJson.put("host", host);
				outputAccountJson.put("name", CommonUtil.jsonString(identity, "name"));
				outputAccountJson.put("version", CommonUtil.jsonString(metaData, "version"));
				if(superNodeHostMap.keySet().contains(CommonUtil.jsonString(endpoint, "host"))){
					outputAccountJson.put("superNodeID", superNodeHostMap.get(CommonUtil.jsonString(endpoint, "host")));
				} else if(superNodeNameMap.keySet().contains(CommonUtil.jsonString(identity, "name"))){
					outputAccountJson.put("superNodeID", superNodeHostMap.get(CommonUtil.jsonString(identity, "name")));
				}
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
