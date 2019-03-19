package com.znv.ws;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.model.Student;
import org.apache.shiro.authz.annotation.RequiresRoles;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/demo")
public class RestWsDemo {
	private List<Student> students;

	public RestWsDemo() {
		students = new ArrayList<>();

		Student s1 = new Student();
		s1.setId(1);
		s1.setName("梅西");
		s1.setDept("阿根廷");

		Student s2 = new Student();
		s2.setId(2);
		s2.setName("内马尔");
		s2.setDept("巴西");

		students.add(s1);
		students.add(s2);
	}

	// @GET表示方法会处理HTTP GET请求
	@GET
	// 这里@Path定义了类的层次路径。指定了资源类提供服务的URI路径。
	@Path("/name/{i}")
	// @Produces定义了资源类方法会生成的媒体类型。
	@Produces(MediaType.TEXT_XML)
	// @PathParam向@Path定义的表达式注入URI参数值。
	public String userName(@PathParam("i") String i) {
		String name = i;
		return "<User>" + "<Name>" + name + "</Name>" + "</User>";
	}

	@GET
	@Path("/hello/{message}")
	public Response sayHello(@PathParam("message") String message) {
		String output = "Jersey says : " + message;
		return Response.status(200).entity(output).build();
	}

	@GET
	@Path("/student")
	@Produces({ MediaType.APPLICATION_JSON })
	public List<Student> getAllStudents() {
		return students;
	}

	@POST
	@Path("/{firstName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)   //只接受json格式的数据
	public String post(@Context HttpServletRequest request,
					   @PathParam("firstName") String firstName, String data) {
		//System.out.println(request.getHeader("DIYHEADER"));
		JSONObject obj=new JSONObject();
		String result="";
		obj= JSON.parseObject(data);
		obj.put("firstName",firstName);
		result=obj.toJSONString();
		return result;
	}

	@DELETE
	@Path("/{delete}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@RequiresRoles("admin")
	public String delete(@PathParam("delete") String delete,String data) {
		JSONObject obj=JSON.parseObject(data);
		obj.put("delete",delete);
		return obj.toJSONString();
	}

	@PUT
	@Path("/{put}")
	@RequiresRoles("admin")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String put(@PathParam("put") String put,String data) {
		JSONObject obj=JSON.parseObject(data);
		obj.put("put",put);
		return obj.toJSONString();
	}

}
