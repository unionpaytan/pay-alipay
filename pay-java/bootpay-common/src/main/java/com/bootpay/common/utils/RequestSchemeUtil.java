package com.bootpay.common.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


public class RequestSchemeUtil {

	public static String getHttpRequestName(HttpServletRequest request){

		return request.getServerName();
	}
	/**
	 * 获取本地tomcat请求域名
	 * @param request
	 * @return
	 */
    public static String getHttpRequestURL(HttpServletRequest request){
        int port = request.getServerPort();
        String tp = "";
        if (port != 80) {
            tp = ":" + port;
        }
        String ctx = request.getScheme() + "://" + request.getServerName() + tp
                + request.getContextPath();
        return ctx;
    }
    
    @SuppressWarnings("rawtypes")
	public static Map<String,String> getHttpRequestParam(HttpServletRequest request){
        Map properties = request.getParameterMap();
        // 返回值Map
        Map<String,String> returnMap = new HashMap<String,String>();
        Iterator entries = properties.entrySet().iterator();
        Map.Entry entry;
        String name = "";
        String value = "";
        while (entries.hasNext()) {
        	entry = (Map.Entry) entries.next();
        	name = (String) entry.getKey();
        	Object valueObj = entry.getValue();
        	if(null == valueObj){
        		value = "";
        	}else if(valueObj instanceof String[]){
        		String[] values = (String[])valueObj;
        		for(int i=0;i<values.length;i++){
        			value = values[i] + ",";
        		}
        		value = value.substring(0, value.length()-1);
        	}else{
        		value = valueObj.toString();
        	}
        	returnMap.put(name, value);
        }
        return returnMap;
    }
    
    @SuppressWarnings("rawtypes")
	public static Map<String,Object> getHttpRequestParamobj(HttpServletRequest request){
        Map properties = request.getParameterMap();
        // 返回值Map
        Map<String,Object> returnMap = new HashMap<String,Object>();
        Iterator entries = properties.entrySet().iterator();
        Map.Entry entry;
        String name = "";
        String value = "";
        while (entries.hasNext()) {
        	entry = (Map.Entry) entries.next();
        	 name = (String) entry.getKey();
        	Object valueObj = entry.getValue();
        	if(null == valueObj){
        		value = "";
        	}else if(valueObj instanceof String[]){
        		String[] values = (String[])valueObj;
        		for(int i=0;i<values.length;i++){
        			value = values[i] + ",";
        		}
        		value = value.substring(0, value.length()-1);
        	}else{
        		value = valueObj.toString();
        	}
        	returnMap.put(name, value);
        }
        return returnMap;
    }
    
    @SuppressWarnings("rawtypes")
	public static Map<String,Object> getHttpRequestParamMap(HttpServletRequest request){
        Map<String,Object> map = new HashMap<String,Object>();  
        Enumeration paramNames = request.getParameterNames();  
        while (paramNames.hasMoreElements()) {  
            String paramName = (String) paramNames.nextElement();  
            String[] paramValues = request.getParameterValues(paramName);  
            if (paramValues.length == 1) {  
                String paramValue = paramValues[0];  
                if (paramValue.length() != 0) {  
                    map.put(paramName, paramValue);  
                }  
            }  
        }
        return map;
    }
    
    
    /**
     * 用流的读取请求参数
     * @param request
     * @return
     */
   public static String getHttpRequestParamInput(HttpServletRequest request){
	   	BufferedReader in =null;
		try {
			in = new BufferedReader(new InputStreamReader(request.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   	StringBuilder sb = new StringBuilder();   
	   	String line = null;  
	   	try {
			while ((line = in.readLine()) != null) {   
			        sb.append(line);   
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	   	return sb.toString();
	   	
   }
   

    
   
}
