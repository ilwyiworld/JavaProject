package com.znv.ws;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.znv.model.Student;
import com.znv.model.StudentForm;
import org.apache.shiro.authz.annotation.RequiresRoles;

@Path("/admin")
//@RequiresRoles("admin")
public class AdminSecretsResource {

	@GET
	@RequiresRoles("admin")
	public String tellSecret() {
		final String output = "Shh, the secret answer is 41.";
		return output;
	}

	@POST
	@Path("/saveSingle")
	@Consumes(MediaType.APPLICATION_JSON)
	public String saveStudent(Student student) {
		System.out.println(student);
		return student.toString();
	}

	@POST
	@Path("/saveMulti")
	@RequiresRoles("admin")   //只允许拥有admin角色的用户访问
	@Consumes(MediaType.APPLICATION_JSON)
	public String save(StudentForm form) {
		for (Student student : form.getList()) {
			System.out.println(student);
		}
		return "success";
	}
	
	@POST
	@Path("/form")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String formTest(Student student) {
		return "success";
	}

}
