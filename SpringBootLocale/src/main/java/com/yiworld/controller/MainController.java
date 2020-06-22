package com.yiworld.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import com.google.common.io.ByteStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@Autowired
	private MessageSource messageSource;

	@GetMapping("/")
	public String index(Model model) throws IOException {
		ClassPathResource classPathResource = new ClassPathResource("no-car.txt");
		InputStream inputStream = classPathResource.getInputStream();
		System.out.println( new String(ByteStreams.toByteArray(inputStream)));
		Locale locale = LocaleContextHolder.getLocale();
		// MessageSource 类可以获取 messages 的内容
		model.addAttribute("world", messageSource.getMessage("world", null, locale));
		return "index";
	}

}
