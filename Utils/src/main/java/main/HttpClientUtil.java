package main;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {
    public static final String CHARSET = "UTF-8";
    private  static PoolingHttpClientConnectionManager cm = null;
    public static int SOCKET_TIMEOUT = 30000;//超时时间

    public static CloseableHttpClient getHttpClient() { 
    	RequestConfig config = RequestConfig.custom().setSocketTimeout(SOCKET_TIMEOUT)
                 .setConnectTimeout(SOCKET_TIMEOUT)
                 .setConnectionRequestTimeout(SOCKET_TIMEOUT).build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config)
                .setConnectionManager(cm)
                .build();          
        return httpClient;
    }
    
    public static String doGet(String url, Map<String, String> params,Map<String, String> headers){
        return doGet(url, params,CHARSET,headers);
    }
    public static String doPost(String url, Map<String, String> params,Map<String, String> headers){
        return doPost(url, params,CHARSET,headers);
    }
	public static String doPost(String url, String contentType, InputStream inputStream, String fileName) {
		HttpPost httpPost = new HttpPost(url);
		if (contentType != null) {
			httpPost.setHeader("Content-type", contentType);
		}
		HttpEntity multipartEntity;
		try {
			multipartEntity = MultipartEntityBuilder.create().addBinaryBody("file", inputStream, ContentType.DEFAULT_BINARY, fileName).build();
			httpPost.setEntity(multipartEntity);
			CloseableHttpResponse httpResponse = getHttpClient().execute(httpPost);
			HttpEntity entity = httpResponse.getEntity();
			String result = null;
			if (entity != null) {
				result = EntityUtils.toString(entity, "utf-8");
			}
			httpResponse.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    /**
     * HTTP Get 获取内容
     * @param url  请求的url地址 ?之前的地址
     * @param params	请求的参数
     * @param charset	编码格式
     * @return	页面内容
     */
    public static String doGet(String url,Map<String,String> params,String charset,Map<String, String> headers){
    	if(StringUtils.isBlank(url)){
    		return null;
    	}
    	try {
    		if(params != null && !params.isEmpty()){
    			List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
    			for(Map.Entry<String,String> entry : params.entrySet()){
    				String value = entry.getValue();
    				if(value != null){
    					pairs.add(new BasicNameValuePair(entry.getKey(),value));
    				}
    			}
    			url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset));
    		}
    		HttpGet httpGet = new HttpGet(url);
    		if(headers != null && !headers.isEmpty()){
    			for(Map.Entry<String,String> entry : headers.entrySet()){
    				String value = entry.getValue();
    				if(value != null){
    					httpGet.setHeader(entry.getKey(), value);
    				}
    			}
    		}
    		CloseableHttpResponse response = getHttpClient().execute(httpGet);
    		int statusCode = response.getStatusLine().getStatusCode();
    		if (statusCode != 200) {
    			httpGet.abort();
    			throw new RuntimeException("HttpClient,error status code :" + statusCode);
    		}
    		HttpEntity entity = response.getEntity();
    		String result = null;
    		if (entity != null){
    			result = EntityUtils.toString(entity, "utf-8");
    		}
    		EntityUtils.consume(entity);
    		response.close();
    		return result;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    /**
     * HTTP Post 获取内容
     * @param url  请求的url地址 ?之前的地址
     * @param params	请求的参数
     * @param charset	编码格式
     * @return	页面内容
     */
    public static String doPost(String url,Map<String,String> params,String charset,Map<String, String> headers){
    	if(StringUtils.isBlank(url)){
    		return null;
    	}
    	try {
    		List<NameValuePair> pairs = null;
    		if(params != null && !params.isEmpty()){
    			pairs = new ArrayList<NameValuePair>(params.size());
    			for(Map.Entry<String,String> entry : params.entrySet()){
    				String value = entry.getValue();
    				if(value != null){
    					pairs.add(new BasicNameValuePair(entry.getKey(),value));
    				}
    			}
    		}    		
    		HttpPost httpPost = new HttpPost(url);
    		if(pairs != null && pairs.size() > 0){
    			httpPost.setEntity(new UrlEncodedFormEntity(pairs,CHARSET));
    		}
    		if(headers != null && !headers.isEmpty()){
    			for(Map.Entry<String,String> entry : headers.entrySet()){
    				String value = entry.getValue();
    				if(value != null){
    		            httpPost.setHeader(entry.getKey(), value);
    				}
    			}
    		}
    		CloseableHttpResponse response = getHttpClient().execute(httpPost);
    		int statusCode = response.getStatusLine().getStatusCode();
    		if (statusCode != 200) {
    			httpPost.abort();
    			throw new RuntimeException("HttpClient,error status code :" + statusCode);
    		}
    		HttpEntity entity = response.getEntity();
    		String result = null;
    		if (entity != null){
    			result = EntityUtils.toString(entity, "utf-8");
    		}
    		EntityUtils.consume(entity);
    		response.close();
    		return result;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }

    public static String doPost(String url,String jsonStr,Map<String, String> headers){
    	if(StringUtils.isBlank(url)){
    		return null;
    	}
    	try {
    		HttpPost httpPost = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(jsonStr,"UTF-8");//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
    		if(headers != null && !headers.isEmpty()){
    			for(Map.Entry<String,String> entry : headers.entrySet()){
    				String value = entry.getValue();
    				if(value != null){
    		            httpPost.setHeader(entry.getKey(), value);
    				}
    			}
    		}
    		CloseableHttpResponse response = getHttpClient().execute(httpPost);
    		int statusCode = response.getStatusLine().getStatusCode();
    		if (statusCode != 200) {
    			httpPost.abort();
    			throw new RuntimeException("HttpClient,error status code :" + statusCode);
    		}
    		HttpEntity entity = response.getEntity();
    		String result = null;
    		if (entity != null){
    			result = EntityUtils.toString(entity, "utf-8");
    		}
    		EntityUtils.consume(entity);
    		response.close();
    		return result;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public static String doPost(String url,File file,Map<String,String> params,Map<String, String> headers){
    	if(StringUtils.isBlank(url)){
    		return null;
    	}
    	try {
            HttpPost httpPost = new HttpPost(url);
            FileBody bin = new FileBody(file);
            StringBody comment = new StringBody(file.getName());

            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("file", bin);// file为请求后台的File upload;属性
            reqEntity.addPart("filename", comment);// filename为请求后台的普通参数;属性
    		if(params != null && !params.isEmpty()){
    			for(Map.Entry<String,String> entry : params.entrySet()){
    				String value = entry.getValue();
    				if(value != null){
    					reqEntity.addPart(entry.getKey(),new StringBody(value));
    				}
    			}
    		}    	
            httpPost.setEntity(reqEntity);
    		if(headers != null && !headers.isEmpty()){
    			for(Map.Entry<String,String> entry : headers.entrySet()){
    				String value = entry.getValue();
    				if(value != null){
    		            httpPost.setHeader(entry.getKey(), value);
    				}
    			}
    		}
            CloseableHttpResponse response = getHttpClient().execute(httpPost);
    		int statusCode = response.getStatusLine().getStatusCode();
    		if (statusCode != 200) {
    			httpPost.abort();
    			throw new RuntimeException("HttpClient,error status code :" + statusCode);
    		}
    		HttpEntity entity = response.getEntity();
    		String result = null;
    		if (entity != null){
    			result = EntityUtils.toString(entity, "utf-8");
    		}
            System.out.println(result);// httpclient自带的工具类读取返回数据
    		EntityUtils.consume(entity);
    		response.close();
    		return result;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public static String doPost(String url, byte[] bytes, String contentType) {  
        HttpPost httpPost = new HttpPost(url);  
        CloseableHttpClient httpClient=getHttpClient();
        httpPost.setEntity(new ByteArrayEntity(bytes));  
        if (contentType != null)  
            httpPost.setHeader("Content-type", contentType);  
        CloseableHttpResponse httpResponse=null;
        try {  
            httpResponse = httpClient.execute(httpPost);  
            HttpEntity entity = httpResponse.getEntity();  
    		String result = null;
    		if (entity != null){
    			result = EntityUtils.toString(entity, "utf-8");
    		}
    		httpResponse.close();
    		return result;
        }catch(Exception e){
    		e.printStackTrace();
        } 
    	return null;
    }  
 
    public static String doPut(String url,String jsonStr){
    	if(StringUtils.isBlank(url)){
    		return null;
    	}
    	try {
    		HttpPut httpPut = new HttpPut(url);
            StringEntity stringEntity = new StringEntity(jsonStr,"UTF-8");//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPut.setEntity(stringEntity);
    		CloseableHttpResponse response = getHttpClient().execute(httpPut);
    		int statusCode = response.getStatusLine().getStatusCode();
    		if (statusCode != 200) {
    			httpPut.abort();
    			throw new RuntimeException("HttpClient,error status code :" + statusCode);
    		}
    		HttpEntity entity = response.getEntity();
    		String result = null;
    		if (entity != null){
    			result = EntityUtils.toString(entity, "utf-8");
    		}
    		EntityUtils.consume(entity);
    		response.close();
    		return result;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public static String doPut(String url,String jsonStr,Map<String, String> headers){
    	if(StringUtils.isBlank(url)){
    		return null;
    	}
    	try {
    		HttpPut httpPut = new HttpPut(url);
            StringEntity stringEntity = new StringEntity(jsonStr,"UTF-8");//解决中文乱码问题
            stringEntity.setContentEncoding("UTF-8");
            stringEntity.setContentType("application/json");
            httpPut.setEntity(stringEntity);
    		if(headers != null && !headers.isEmpty()){
    			for(Map.Entry<String,String> entry : headers.entrySet()){
    				String value = entry.getValue();
    				if(value != null){
    					httpPut.setHeader(entry.getKey(), value);
    				}
    			}
    		}
    		CloseableHttpResponse response = getHttpClient().execute(httpPut);
    		int statusCode = response.getStatusLine().getStatusCode();
    		if (statusCode != 200) {
    			httpPut.abort();
    			throw new RuntimeException("HttpClient,error status code :" + statusCode);
    		}
    		HttpEntity entity = response.getEntity();
    		String result = null;
    		if (entity != null){
    			result = EntityUtils.toString(entity, "utf-8");
    		}
    		EntityUtils.consume(entity);
    		response.close();
    		return result;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public static String doDelete(String url) {
    	if(StringUtils.isBlank(url)){
    		return null;
    	}
    	try {
    		HttpDelete httpDelete = new HttpDelete(url);

    		CloseableHttpResponse response = getHttpClient().execute(httpDelete);
    		int statusCode = response.getStatusLine().getStatusCode();
    		if (statusCode != 200) {
    			httpDelete.abort();
    			throw new RuntimeException("HttpClient,error status code :" + statusCode);
    		}
    		HttpEntity entity = response.getEntity();
    		String result = null;
    		if (entity != null){
    			result = EntityUtils.toString(entity, "utf-8");
    		}
    		EntityUtils.consume(entity);
    		response.close();
    		return result;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }

}
