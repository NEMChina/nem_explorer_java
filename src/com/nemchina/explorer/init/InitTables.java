package com.nemchina.explorer.init;

import java.io.File;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.mchange.io.FileUtils;
import com.nemchina.explorer.db.DBOperator;

/** 
 * @Description: Init Database Table
 * @author lu
 * @date 2016年8月6日
 */ 
public class InitTables extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public void init() throws ServletException {
		try{
			System.out.println("Init table start");
			File sqlFileDir = new File(this.getServletContext().getRealPath("/")+"sql");
			File[] sqlFiles = sqlFileDir.listFiles();
			String sql = null;
			for(File sqlFile:sqlFiles){
				System.out.println("    Check ["+sqlFile.getName()+"]");
				sql = FileUtils.getContentsAsString(sqlFile);
				DBOperator.update(sql);
			}
			System.out.println("Init table end");
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
