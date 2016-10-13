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
import com.nemchina.explorer.http.Block;
import com.nemchina.explorer.util.CommonUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @Description: Load Block Servlet
 * @author lu
 * @date 2016年7月30日
 */ 
@WebServlet("/blockList")
public class BlockListServlet extends HttpServlet {
	
	private static final int LISTAMOUNT = 10;
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//paging
		String pageString = request.getParameter("page");
		int page = 1;
		try {
			page = Integer.parseInt(pageString);
		} catch (NumberFormatException numberEx) { }
		JSONArray outputBlockListJson = new JSONArray();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			//get current height from NIS
			int height = Block.chainHeight();
			if(height<=0){
				out.print(outputBlockListJson);
				return;
			}
			height = height - (10+LISTAMOUNT*(page-1));
			if(height<1){
				height = 1;
			}
			//query blocks
			JSONArray dataArray = Block.localChainBlocksAfter(height);
			if(dataArray.size()==0){
				out.print(outputBlockListJson);
				return;
			}
			JSONObject data = null;
			JSONObject block = null;
			JSONObject outputBlockJSON = null;
			JSONArray txes = null;
			JSONObject tx = null;
			DecimalFormat decimalFormat = new DecimalFormat("0.##");
			for(int i=dataArray.size()-1;i>=0;i--){
				//validate block data
				data = dataArray.getJSONObject(i);
				if(data==null){
					continue;
				}
				if(!data.containsKey("block")){
					continue;
				}
				block = data.getJSONObject("block");
				if(block==null){
					continue;
				}
				//count tx amount and tx fees
				int txAmount = 0;
				double txFee = 0;
				txes = new JSONArray();
				if(data.containsKey("txes")){
					txes = data.getJSONArray("txes");
					for(int j=0;j<txes.size();j++){
						if(txes.get(j)==null || !txes.getJSONObject(j).containsKey("tx")){
							continue;
						}
						tx = txes.getJSONObject(j).getJSONObject("tx");
						if(!tx.containsKey("fee")){
							continue;
						}
						txFee += CommonUtil.jsonLong(tx, "fee");
						tx.put("signerAccount", Account.getAccountAddressFromPublicKey(CommonUtil.jsonString(tx, "signer")));
						tx.put("amount", decimalFormat.format(CommonUtil.jsonLong(tx, "amount")/1000000));
						tx.put("fee", decimalFormat.format(CommonUtil.jsonLong(tx, "fee")/1000000));
						tx.put("height", CommonUtil.jsonInt(block, "height"));
						tx.put("signature", CommonUtil.jsonString(tx, "signature"));
						if(CommonUtil.jsonInt(tx, "type")==2049){
							tx.put("recipient", Account.getAccountAddressFromPublicKey(CommonUtil.jsonString(tx, "remoteAccount")));
						}
						txAmount++;
					}
				}
				//get harvester account
				String harvester = "";
				if(block.containsKey("signer")){
					harvester = Account.getAccountAddressFromPublicKey(CommonUtil.jsonString(block, "signer"));
				}
				outputBlockJSON = new JSONObject();
				outputBlockJSON.put("hash", CommonUtil.jsonString(data, "hash"));
				outputBlockJSON.put("height", CommonUtil.jsonInt(block, "height"));
				outputBlockJSON.put("timeStamp", CommonUtil.jsonInt(block, "timeStamp"));
				outputBlockJSON.put("txAmount", txAmount);
				outputBlockJSON.put("txFee", decimalFormat.format(txFee/1000000));
				outputBlockJSON.put("harvester", harvester);
				outputBlockJSON.put("txes", txes);
				outputBlockListJson.add(outputBlockJSON);
				if(i<=10-LISTAMOUNT){
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		out.print(outputBlockListJson);
		return;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
