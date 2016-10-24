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

import com.nemchina.explorer.http.Namespace;
import com.nemchina.explorer.util.CommonUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Load Namespace Servlet
 * @author lu
 * @date 2016年10月22日
 */ 
@WebServlet("/namespaceList")
public class NamespaceListServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final int LISTAMOUNT = 100; //page amount

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int page = CommonUtil.getPageParamFromRequest(request, "page");
		JSONArray outputNamespaceListJson = new JSONArray();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			int index = LISTAMOUNT * (page-1) + 1;
			List<Map<String, Object>> namespaceList = Namespace.queryNamespaceByPaging(index, LISTAMOUNT);
			if(namespaceList.size()==0){
				out.print(outputNamespaceListJson);
				return;
			}
			JSONObject outputNamespaceJSON = null;
			JSONObject outputMosaicJSON = null;
			JSONArray outputMosaicListJSON = null;
			JSONArray mosaicArray = null;
			JSONObject tempMosaic = null;
			JSONArray properties = null;
			Map<String, Object> namespaceMap = null;
			for(int i=0;i<namespaceList.size();i++){
				if(namespaceList.get(i)==null){
					continue;
				}
				namespaceMap = namespaceList.get(i);
				outputNamespaceJSON = new JSONObject();
				outputNamespaceJSON.put("no", CommonUtil.mapInt(namespaceMap, "c_no"));
				outputNamespaceJSON.put("name", CommonUtil.mapString(namespaceMap, "c_name"));
				outputNamespaceJSON.put("creator", CommonUtil.mapString(namespaceMap, "c_creator"));
				outputNamespaceJSON.put("timeStamp", CommonUtil.mapInt(namespaceMap, "c_timestamp"));
				outputNamespaceJSON.put("mosaicAmount", CommonUtil.mapInt(namespaceMap, "c_mosaics"));
				//the mosaics belong this namespace
				outputMosaicListJSON = new JSONArray();
				if(CommonUtil.mapInt(namespaceMap, "c_mosaics")>0){
					mosaicArray = Namespace.mosaicListFromNamespace(CommonUtil.mapString(namespaceMap, "c_name"));
					for(int j=0;j<mosaicArray.size();j++){
						tempMosaic = mosaicArray.getJSONObject(j).getJSONObject("mosaic");
						properties = tempMosaic.getJSONArray("properties");
						outputMosaicJSON = new JSONObject();
						outputMosaicJSON.put("no", j+1);
						outputMosaicJSON.put("name", CommonUtil.jsonString(tempMosaic.getJSONObject("id"), "name"));
						for(int k=0;k<properties.size();k++){
							if("initialSupply".equals(CommonUtil.jsonString(properties.getJSONObject(k), "name"))){
								outputMosaicJSON.put("initialSupply", CommonUtil.jsonLong(properties.getJSONObject(k), "value"));
							}
							if("transferable".equals(CommonUtil.jsonString(properties.getJSONObject(k), "name"))){
								outputMosaicJSON.put("transferable", CommonUtil.jsonString(properties.getJSONObject(k), "value"));
							}
						}
					}
					outputMosaicListJSON.add(outputMosaicJSON);
				}
				outputNamespaceJSON.put("mosaicList", outputMosaicListJSON); // add mosaic list into namespace item
				outputNamespaceListJson.add(outputNamespaceJSON);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		out.print(outputNamespaceListJson);
		return;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
