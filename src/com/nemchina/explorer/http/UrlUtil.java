package com.nemchina.explorer.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/** 
 * @Description: Get URL Content by UrlUtil
 * @author lu
 * @date 2016年11月28日
 */ 
public class UrlUtil {

    public static String getContent(String fullURL){
    	StringBuffer content = null;
    	HttpURLConnection conn = null;
    	InputStreamReader isr = null;
    	BufferedReader br = null;
    	try {
			content = new StringBuffer();
			URL url = new URL(fullURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			isr = new InputStreamReader(conn.getInputStream());
			br = new BufferedReader(isr);
			String temp = null;
			while ((temp = br.readLine()) != null) {
				content.append(temp).append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(" --- error --- ");
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				isr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	return content.toString();
    }
}
