package com.znv.client;

import javax.ws.rs.core.MediaType;
import com.alibaba.fastjson.JSONObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.znv.model.Student;
import com.znv.model.StudentForm;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.core.Response.Status;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ClientDemo {

	public static void main(String[] args) {
		Client client = Client.create();
		WebResource webResource1 = client.resource("http://localhost:8090/admin/saveSingle");
		WebResource webResource2 = client.resource("http://localhost:8090/admin/saveMulti");

		String username = "root";
		String password = "secret";
		final HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter(username, password);
		client.addFilter(authFilter);

		JSONObject obj=new JSONObject();
		obj.put("column1","12");
		obj.put("column2","测点");

		Student stu1 = new Student();
		stu1.setId(1);
		stu1.setName("梅西");
		stu1.setDept("soccer");
		Student stu2 = new Student();
		stu1.setId(2);
		stu1.setName("易亮");
		stu1.setDept("java");

		List<Student> list=new ArrayList<>();
		list.add(stu1);
		list.add(stu2);

		StudentForm form = new StudentForm();
		form.setList(list);

		//String result1= webResource1.type(MediaType.APPLICATION_JSON).post(String.class,stu1);
		//String result2= webResource2.type(MediaType.APPLICATION_JSON).post(String.class,form);
		//System.out.println(result1);
		//System.out.println(result2);
		System.out.println(sendPutData("http://localhost:8090/Jersey_Shiro_Demo/demo/testAuth",obj.toJSONString()));
		client.destroy();
	}

	public static String sendPostData(String url, String data) {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		final HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter("guest", "guest");
		client.addFilter(authFilter);
		WebResource resource = client.resource(url);
		try {
			//设置json格式
			return resource.type("application/json").accept(MediaType.APPLICATION_JSON).post(String.class,data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}


	/**
	 * 修改和添加
	 */
	public static String sendPutData(String url, String data) {
		HttpPut put = new HttpPut(url);
		StringEntity se = new StringEntity(data, ContentType.create("application/json", "UTF-8"));
		put.setEntity(se);
		CloseableHttpClient http = HttpClients.createDefault();

		//Basic Auth认证
		HttpClientContext context = HttpClientContext.create();
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		UsernamePasswordCredentials usernamePassword = new UsernamePasswordCredentials(
				"guest", "guest");		//用户名密码
		credsProvider.setCredentials(AuthScope.ANY, usernamePassword);
		context.setCredentialsProvider(credsProvider);

		CloseableHttpResponse response = null;
		try {
			//response = http.execute(put);
			response = http.execute(put,context);
			if(response.toString().contains("403 Forbidden")){
				//自行处理用户名密码错误的请求
				JSONObject exception=new JSONObject();
				exception.put("error", Status.FORBIDDEN);
				exception.put("message","该用户无权限");
				return exception.toJSONString();
			}
			return EntityUtils.toString(response.getEntity(), Charset.forName("utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";

	}

	/**
	 * 删除
	 */
	public static String sendDeleteData(String url, String data) {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		//添加用户名密码认证
		final HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter("guest", "guest");
		client.addFilter(authFilter);
		WebResource resource = client.resource(url);
		try {
			//设置json格式
			return resource.type("application/json").accept(MediaType.APPLICATION_JSON).delete(String.class,data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 返回客户端请求
	 * 例如：返回请求结果状态“200 OK”。
	 */
	private static String getClientResponse(WebResource resource) {
		return resource.accept(MediaType.TEXT_XML).get(ClientResponse.class).toString();
	}

	/**
	 * 返回请求结果
	 */
	private static String getResponse(WebResource resource) {
		return resource.accept(MediaType.TEXT_XML).get(String.class);
	}
}
