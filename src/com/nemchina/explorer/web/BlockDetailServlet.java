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
 * @Description: Load Block Detail Servlet
 * @author lu
 * @date 2016年8月18日
 */ 
@WebServlet("/blockDetail")
public class BlockDetailServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int height = CommonUtil.convertStrToInt(request.getParameter("height"));
		JSONObject outputBlockJson = new JSONObject();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		try{
			if(height<=0){
				out.print(outputBlockJson);
				return;
			}
			DecimalFormat difficultyFormat = new DecimalFormat("###.##%");
			DecimalFormat feeFormat = new DecimalFormat("0.##");
			if(height==1){ //nemesis block
				JSONObject block = Block.blockAtPublic(1);
				JSONArray transactions = block.getJSONArray("transactions");
				outputBlockJson.put("height", 1);
				outputBlockJson.put("timeStamp", 0);
				outputBlockJson.put("difficulty", "100%");
				outputBlockJson.put("txAmount", transactions.size());
				outputBlockJson.put("txFee", 0);
				outputBlockJson.put("signer", "#");
				outputBlockJson.put("hash", "#");
				for(int i=0;i<transactions.size();i++){
					JSONObject tx = transactions.getJSONObject(i);
					if(tx==null || !tx.containsKey("tx")){
						continue;
					}
					tx = tx.getJSONObject("tx");
					tx.put("signerAccount", Account.getAccountAddressFromPublicKey(CommonUtil.jsonString(tx, "signer")));
					tx.put("amount", feeFormat.format(CommonUtil.jsonLong(tx, "amount")/1000000));
					tx.put("fee", feeFormat.format(CommonUtil.jsonLong(tx, "fee")/1000000));
					tx.put("height", 1);
					tx.put("signature", CommonUtil.jsonString(tx, "signature"));
				}
				outputBlockJson.put("txes", transactions);
			} else { //non nemsis block
				JSONArray blocks = Block.localChainBlocksAfter(height-1);
				if(blocks==null || blocks.size()==0){
					out.print(outputBlockJson);
					return;
				}
				JSONObject block = blocks.getJSONObject(0);
				JSONObject blockSub = block.getJSONObject("block");
				JSONArray txes = block.getJSONArray("txes");
				double difficulty = CommonUtil.jsonLong(block, "difficulty");
				difficulty = difficulty / Math.pow(10, 14);
				outputBlockJson.put("height", CommonUtil.jsonInt(blockSub, "height"));
				outputBlockJson.put("timeStamp", CommonUtil.jsonLong(blockSub, "timeStamp"));
				outputBlockJson.put("difficulty", difficultyFormat.format(difficulty));
				outputBlockJson.put("txAmount", txes.size());
				long txFee = 0;
				for(int i=0;i<txes.size();i++){
					JSONObject tx = txes.getJSONObject(i);
					if(tx==null || !tx.containsKey("tx")){
						continue;
					}
					tx = tx.getJSONObject("tx");
					txFee += CommonUtil.jsonLong(tx, "fee");
					tx.put("signerAccount", Account.getAccountAddressFromPublicKey(CommonUtil.jsonString(tx, "signer")));
					tx.put("amount", feeFormat.format(CommonUtil.jsonLong(tx, "amount")/1000000));
					tx.put("fee", feeFormat.format(CommonUtil.jsonLong(tx, "fee")/1000000));
					tx.put("height", CommonUtil.jsonInt(blockSub, "height"));
					tx.put("signature", CommonUtil.jsonString(tx, "signature"));
				}
				outputBlockJson.put("txFee", feeFormat.format(txFee/1000000));
				outputBlockJson.put("signer", Account.getAccountAddressFromPublicKey(CommonUtil.jsonString(blockSub, "signer")));
				outputBlockJson.put("hash", CommonUtil.jsonString(block, "hash"));
				outputBlockJson.put("txes", txes);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		out.print(outputBlockJson);
		return;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
