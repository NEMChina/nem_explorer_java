package com.nemchina.explorer.util;

import java.util.Map;

import net.sf.json.JSONObject;

/** 
 * @Description: Common Util
 * @author lu
 * @date 2016年8月7日
 */ 
public class CommonUtil {

	public static final long NEM_EPOCH = 1427587585; //UTC(2015, 2, 29, 0, 6, 25, 0);
	
	public static String checkString(Object object){
		return object==null?"":object.toString();
	}
	
	public static int checkInt(Object object){
		if(object==null){
			return 0;
		}
		try{
			return Integer.valueOf(object.toString()).intValue();
		} catch(Exception ex) {
			return 0;
		}
	}
	
	public static long checkLong(Object object){
		if(object==null){
			return 0;
		}
		try{
			return Long.valueOf(object.toString()).longValue();
		} catch(Exception ex) {
			return 0;
		}
	}
	
	/**
	 * decode payload message (for type 1)
	 * @param message
	 * @return
	 */
	public static String decodeMessage(String message){
		if(message==null){
			return "";
		}
		String result = "";
        byte[] bytes = HexEncoder.getBytes(message);
        try {
        	result = new String(bytes, "UTF-8");
        } catch(Exception e) {
        	e.printStackTrace();
        }
        return result;
	}
	
	public static String jsonString(JSONObject object, String key){
		if(object.containsKey(key)){
			return object.getString(key);
		}
		return "";
	}
	
	public static int jsonInt(JSONObject object, String key){
		if(object.containsKey(key)){
			return object.getInt(key);
		}
		return 0;
	}
	
	public static long jsonLong(JSONObject object, String key){
		if(object.containsKey(key)){
			return object.getLong(key);
		}
		return 0;
	}
	
	public static double jsonDouble(JSONObject object, String key){
		if(object.containsKey(key)){
			return object.getDouble(key);
		}
		return 0;
	}
	
	public static String mapString(Map<String, Object> map, String key){
		if(map.get(key)!=null){
			return map.get(key).toString();
		}
		return "";
	}
	
	public static int mapInt(Map<String, Object> map, String key){
		int result = 0;
		if(map.get(key)!=null){
			try{
				result = Integer.parseInt(map.get(key).toString());
			} catch(NumberFormatException ex){}
		}
		return result;
	}
	
	public static long mapLong(Map<String, Object> map, String key){
		long result = 0;
		if(map.get(key)!=null){
			try{
				result = Long.parseLong(map.get(key).toString());
			} catch(NumberFormatException ex){}
		}
		return result;
	}
	
	public static int convertStrToInt(String str){
		int result = 0;
		if(str==null || str.trim().length()==0){
			return result;
		}
		try {
			result = Integer.parseInt(str);
		} catch (NumberFormatException numberEx) { }
		return result;
	}
}
