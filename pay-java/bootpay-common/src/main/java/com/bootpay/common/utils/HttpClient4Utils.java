package com.bootpay.common.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * httpClient4 工具类
 */

public class HttpClient4Utils {

	private static String trongridApiKey = "38cf0025-8912-4d02-ba32-5097042a769f";
	//设置默认超时时间为60s
	public static final int DEFAULT_TIME_OUT	= 60*1000;
	public static final String CHARSET	= "UTF-8";
	//http请求
	public static String sendHttpRequest(String url, Map<String, Object> paramMap, String charset, boolean isPost) {
		return sendHttpRequest(url, paramMap, charset, isPost, DEFAULT_TIME_OUT);
	}
	
	//http请求
	public static String sendHttpRequest(String url, Map<String, Object> paramMap, String charset, boolean isPost, int timeout) {
		if(isPost) {
			return httpPost(url, paramMap, charset, timeout);
		}
		
		return httpGet(url, paramMap, charset, timeout);
	}
	public static String sendGet(String url, Map<String, Object> paramMap) {
		return httpGet(url, paramMap, CHARSET, DEFAULT_TIME_OUT);
	}
	public static String sendPost(String url, Map<String, Object> paramMap) {
		return httpPost(url, paramMap, CHARSET, DEFAULT_TIME_OUT);
	}
	//post请求
	public static String httpPost(String url, Map<String, Object> params, String charset, int timeout) {
		
		if(url == null || url.equals("")) {
			return null;
		}
		
		String result		= null;
		
		//超时设置
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build();
		
		//参数组装
		List<NameValuePair> pairs	= new ArrayList<NameValuePair>();
		for(Entry<String, Object> entry : params.entrySet()) {
			String key		= entry.getKey();
			Object value	= entry.getValue();
			pairs.add(new BasicNameValuePair(key, formatStr(value)));
		}

		CloseableHttpClient httpClient 	= HttpClients.createDefault();
		HttpPost httpPost 				= null;
		String responseBody 			= null;
		CloseableHttpResponse response	= null;

		try {
			httpPost = new HttpPost(url);
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(new UrlEncodedFormEntity(pairs,charset));
			response = httpClient.execute(httpPost);
			
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpPost.abort();
				throw new RuntimeException("HttpClient,error status code :" + statusCode);
			}
			
			HttpEntity entity 	= response.getEntity();
			responseBody 		= EntityUtils.toString(entity, charset);
			result				= responseBody; 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭连接,释放资源  
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}

	public static String httpPostWithExtraHeaderAndCookie(String url, Map<String, Object> params, String charset, int timeout,String cookie) {

		if(url == null || url.equals("")) {
			return null;
		}

		String result		= null;

		//超时设置
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build();

		//参数组装
		List<NameValuePair> pairs	= new ArrayList<NameValuePair>();
		for(Entry<String, Object> entry : params.entrySet()) {
			String key		= entry.getKey();
			Object value	= entry.getValue();
			pairs.add(new BasicNameValuePair(key, formatStr(value)));
		}

		CloseableHttpClient httpClient 	= HttpClients.createDefault();
		HttpPost httpPost 				= null;
		String responseBody 			= null;
		CloseableHttpResponse response	= null;

		try {
			httpPost = new HttpPost(url);
			URI uri = new URI(url);
			String host = uri.getHost();
			System.out.println("host -" + host);
			httpPost.addHeader("Host", host);//添加 host mbillexprod.alipay.com
			httpPost.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
			httpPost.addHeader("Accept-Encoding", "gzip, deflate, br");
			httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
			httpPost.addHeader("Connection", "keep-alive");
			httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;");
			httpPost.addHeader("Cookie",cookie);
			httpPost.addHeader("Origin","https://b.alipay.com");
			httpPost.addHeader("Referer","https://b.alipay.com/");
			httpPost.addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
			httpPost.setConfig(requestConfig);
			httpPost.setEntity(new UrlEncodedFormEntity(pairs,charset));
			response = httpClient.execute(httpPost);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpPost.abort();
				throw new RuntimeException("HttpClient,error status code :" + statusCode);
			}

			HttpEntity entity 	= response.getEntity();
			responseBody 		= EntityUtils.toString(entity, charset);
			result				= responseBody;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭连接,释放资源
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}


	//get请求
	public static String httpGet(String url, Map<String, Object> params, String charset, int timeout) {
		
		if(url == null || url.equals("")) {
			return null;
		}
		
		String result = null;
		
		CloseableHttpClient httpClient 	= HttpClients.createDefault();
		HttpGet httpGet 				= null;
		String responseBody 			= null;
		CloseableHttpResponse response	= null;

		try {
			
			if(params != null && !params.isEmpty()) {
				List<NameValuePair> pairs	= new ArrayList<NameValuePair>();
				for(Entry<String, Object> entry : params.entrySet()) {
					String key		= entry.getKey();
					Object value	= entry.getValue();
					pairs.add(new BasicNameValuePair(key, formatStr(value)));
				}
				url = url + "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
			}
			
			httpGet		= new HttpGet(url);
			httpGet.addHeader("TRON-PRO-API-KEY",trongridApiKey);
			httpGet.addHeader("Content-Type","application/json");
			response	= httpClient.execute(httpGet);
			
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpGet.abort();
				throw new RuntimeException("HttpClient,error status code :" + statusCode);
			}

			HttpEntity entity 	= response.getEntity();
			responseBody 		= EntityUtils.toString(entity, charset);
			result				= responseBody; 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭连接,释放资源  
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static String httpGetWithHeader(String url, Map<String, Object> params, String charset, int timeout,String referer) {

		if(url == null || url.equals("")) {
			return null;
		}

		String result = null;

		CloseableHttpClient httpClient 	= HttpClients.createDefault();
		HttpGet httpGet 				= null;
		String responseBody 			= null;
		CloseableHttpResponse response	= null;

		try {

			if(params != null && !params.isEmpty()) {
				List<NameValuePair> pairs	= new ArrayList<NameValuePair>();
				for(Entry<String, Object> entry : params.entrySet()) {
					String key		= entry.getKey();
					Object value	= entry.getValue();
					pairs.add(new BasicNameValuePair(key, formatStr(value)));
				}
				url = url + "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
			}

			httpGet		= new HttpGet(url);
			URI uri = new URI(url);
			String host = uri.getHost();
			httpGet.addHeader("Host", host);//添加 host
			httpGet.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
			httpGet.addHeader("Accept-Encoding", "gzip, deflate, br");
			httpGet.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
			httpGet.addHeader("Connection", "keep-alive");
			httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
//			httpPost.addHeader("Cookie",cookie);
//			httpGet.addHeader("Origin","https://b.alipay.com");
			httpGet.addHeader("Referer",referer);
			httpGet.addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
			response	= httpClient.execute(httpGet);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpGet.abort();
				throw new RuntimeException("HttpClient,error status code :" + statusCode);
			}

			HttpEntity entity 	= response.getEntity();
			responseBody 		= EntityUtils.toString(entity, charset);
			result				= responseBody;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 关闭连接,释放资源
				if (response != null) {
					response.close();
				}
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}


	public static String formatStr(Object text) {
		return (text == null ? "" : text.toString().trim());
	}

	public static String postJSON(String url, String payload){
		String result = "";
		try {
			URL postURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) postURL.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setConnectTimeout(20 * 1000);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(true);
			// json格式上传的模式
			//conn.setRequestProperty("X-Forwarded-For","119.75.217.109");
			conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
			//解决中文乱码
			PrintWriter osw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"));
			osw.write(payload);
			osw.flush();
			osw.close();
			//System.out.println(conn.getResponseCode());
			if (conn.getResponseCode() == 200) {
				InputStreamReader isr = new InputStreamReader(conn.getInputStream(),"utf-8");
				BufferedReader br = new BufferedReader(isr);
				String inputLine = null;
				while ((inputLine = br.readLine()) != null) {
					result += inputLine;
				}
				// System.out.println("result - " + result);
				isr.close();
				conn.disconnect();
				return result;
			}
			else {
				//如果出错，一定要检查URL对没有！
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
				String err = br.readLine();
				System.out.println("Error:"+ err);
				br.close();
				conn.disconnect();
				return result;
			}

		}
		catch (Exception e){
			System.out.println("Exception:" + e.getMessage());
			return result;
		}

	}

	public static String postJSON(String url, String payload,String cookie){
		String result = "";
		try {
			URL postURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) postURL.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setConnectTimeout(20 * 1000);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setInstanceFollowRedirects(true);
			// json格式上传的模式
			//conn.setRequestProperty("X-Forwarded-For","119.75.217.109");
			conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
			conn.setRequestProperty("Cookie", cookie);
			conn.setRequestProperty("Origin", "https://b.alipay.com");
			conn.setRequestProperty("Referer", "https://b.alipay.com/");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
			//解决中文乱码
			PrintWriter osw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(),"utf-8"));
			osw.write(payload);
			osw.flush();
			osw.close();
			//System.out.println(conn.getResponseCode());
			if (conn.getResponseCode() == 200) {
				InputStreamReader isr = new InputStreamReader(conn.getInputStream(),"utf-8");
				BufferedReader br = new BufferedReader(isr);
				String inputLine = null;
				while ((inputLine = br.readLine()) != null) {
					result += inputLine;
				}
				// System.out.println("result - " + result);
				isr.close();
				conn.disconnect();
				return result;
			}
			else {
				//如果出错，一定要检查URL对没有！
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
				String err = br.readLine();
				System.out.println("Error:"+ err);
				br.close();
				conn.disconnect();
				return result;
			}

		}
		catch (Exception e){
			System.out.println("Exception:" + e.getMessage());
			return result;
		}

	}


	public static String getJSON(String queryUrl,Map<String, Object> params,String method) {
		try {

			String randomIP = getRandomIP();
			// 构建URL
			String url = buildUrl(queryUrl, params);

			// 打开连接
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

			// 设置请求头
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			connection.setRequestProperty("X-Forwarded-For", randomIP);

			// 发送GET请求
			connection.setRequestMethod(method);

			// 获取响应
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			String line;
			StringBuilder response = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			// 输出响应
//            System.out.println(response.toString());
			// 关闭连接
			connection.disconnect();
			return response.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private static String getRandomIP() {
		Random random = new Random();
		StringBuilder ipBuilder = new StringBuilder();

		for (int i = 0; i < 4; i++) {
			ipBuilder.append(random.nextInt(256));
			if (i < 3) {
				ipBuilder.append('.');
			}
		}

		return ipBuilder.toString();
	}

	// 构建带参数的URL
	private static String buildUrl(String baseUrl, Map<String, Object> params) {
		StringBuilder urlBuilder = new StringBuilder(baseUrl);

		if (!params.isEmpty()) {
			urlBuilder.append("?");
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
			}
			urlBuilder.deleteCharAt(urlBuilder.length() - 1); // 移除最后一个多余的"&"
		}

		return urlBuilder.toString();
	}


}
