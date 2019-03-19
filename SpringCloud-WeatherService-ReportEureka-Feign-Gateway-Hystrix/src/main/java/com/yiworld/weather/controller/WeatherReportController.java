package com.yiworld.weather.controller;

import java.util.List;
import com.yiworld.weather.service.DataClient;
import com.yiworld.weather.service.WeatherReportService;
import com.yiworld.weather.vo.City;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
/**
 * Weather Report Controller.
 */
@RestController
@RequestMapping("/report")
public class WeatherReportController {
	private final static Logger logger = LoggerFactory.getLogger(WeatherReportController.class);  

	@Autowired
	private WeatherReportService weatherReportService;

	@Autowired
	private DataClient dataClient;
	
	@GetMapping("/cityId/{cityId}")
	public ModelAndView getReportByCityId(@PathVariable("cityId") String cityId, Model model) throws Exception {
		// 获取城市ID列表
		// 改为由网关API来提供数据
		List<City> cityList = null;

		try {
			// 由网关API提供数据
			cityList = dataClient.listCity();
		} catch (Exception e) {
			logger.error("Exception!", e);
		}
		
		model.addAttribute("title", "yiworld的天气预报");
		model.addAttribute("cityId", cityId);
		model.addAttribute("cityList", cityList);
		model.addAttribute("report", weatherReportService.getDataByCityId(cityId));
		return new ModelAndView("weather/report", "reportModel", model);
	}

}
