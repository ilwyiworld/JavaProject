package com.yiworld.weather.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * Hello Controller.
 */
@RestController
public class HelloController {
	
	//@RequestMapping("/hello")
	@GetMapping("/hello")
	public String hello() {
		return "Hello World! Zuul";
	}
}
