package com.yiworld.weather.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 昨日天气.
 */
@Data
public class Yeaterday implements Serializable {
	private static final long serialVersionUID = 1L;
	private String date;
	private String high;
	private String fx;
	private String low;
	private String fl;
	private String type;
}
