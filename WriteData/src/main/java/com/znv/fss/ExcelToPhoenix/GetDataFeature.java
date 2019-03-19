package com.znv.fss.ExcelToPhoenix;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.List;

public class GetDataFeature {

	/**
	 * 通过图片二进制数据流获取图片特征值或特征属性
	 * 
	 * @param fileName
	 *            图片的名称，也可填为任意值
	 * @param data
	 *            图片二进制数据
	 * @param url
	 *            获取特征值的接口
	 * @return 获取到的特征值或特征属性
	 */
	public static String getFeature(String fileName, byte[] data, String url) {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);

		HttpEntity entity = MultipartEntityBuilder.create()
				.addBinaryBody("imageData", data, ContentType.DEFAULT_BINARY, fileName).build();

		httpPost.setEntity(entity);

		CloseableHttpResponse response = null;
		HttpEntity resEntity = null;
		String result = httpCommon(httpPost, response, resEntity, client);
		return result;
	}

	/**
	 * 通过图片路径获取特征值或特征属性
	 * 
	 * @param filePath
	 *            图片的路径
	 * @param url
	 *            获取特征值或特征属性的接口
	 * @return 获取到的特征值或特征属性
	 */
	public static String getImageFeature(String filePath, String url) {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);

		HttpEntity entity = MultipartEntityBuilder.create().addPart("imageData", new FileBody(new File(filePath)))
				.build();

		httpPost.setEntity(entity);
		CloseableHttpResponse response = null;
		HttpEntity resEntity = null;

		String result = httpCommon(httpPost, response, resEntity, client);
		return result;
	}

	/**
	 * 批量获取本地图片特征值
	 * 
	 * @param path
	 *            本地图片的目录
	 * @param url
	 *            批量获取特征值的接口
	 * @return 返回的批量特征值结果数组 JSONArray
	 */
	public static JSONArray getBatchFeature(String path, String url) {
		String result = null;
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);

		HashMap<String, byte[]> fileMap = null;

		fileMap = getFileMap(path);

		MultipartEntityBuilder entity = MultipartEntityBuilder.create();

		int count = 0;
		JSONObject featureJson = new JSONObject();
		JSONArray array = new JSONArray();
		for (String fileName : fileMap.keySet()) {
			byte[] filedata = fileMap.getOrDefault(fileName, null);
			entity.addBinaryBody("imageDatas", filedata, ContentType.DEFAULT_BINARY, fileName);

			// 阈值判断，超过50张的分批发送
			if (++count % 50 == 0) {
				httpPost.setEntity(entity.build());
				CloseableHttpResponse response = null;
				HttpEntity resEntity = null;
				try {
					response = client.execute(httpPost);
					resEntity = response.getEntity();
					result = EntityUtils.toString(resEntity);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				array.add(result);
				entity = MultipartEntityBuilder.create();
				// continue;
			}
		}

		httpPost.setEntity(entity.build());

		CloseableHttpResponse response = null;
		HttpEntity resEntity = null;
		result = httpCommon(httpPost, response, resEntity, client);
		featureJson.put(String.valueOf(count), result);
		array.add(result);
		System.out.println("count" + count);
		return array;
	}

	// 获取指定路径下的所有图片并转为二进制流放入map中
	public static HashMap<String, byte[]> getFileMap(String path) {
		HashMap<String, byte[]> fileMap = new HashMap<String, byte[]>();
		File fDir = new File(path);
		File[] files = fDir.listFiles();
		if (files != null) {
			for (File file : files) {
				String fileName = file.getName();
				if (file.isFile()) {
					byte[] data = PictureUtils.image2byte(path + "\\" + fileName);
					fileMap.put(fileName, data);
				}
			}
		}
		return fileMap;
	}

	// http通用模块
	public static String httpCommon(HttpPost httpPost, CloseableHttpResponse response, HttpEntity resEntity,
			CloseableHttpClient httpClient) {
		String result = null;
		try {
			response = httpClient.execute(httpPost);
			resEntity = response.getEntity();
			result = EntityUtils.toString(resEntity, "UTF-8");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != response) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != resEntity) {
				try {
					EntityUtils.consume(resEntity);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != httpPost) {
				httpPost.releaseConnection();
			}
		}
		return result;
	}

	/**
	 * 批量获取图片特征值
	 * 
	 * @param jsonInfo
	 *            jsonInfo中的JSONObject中有图片名和图片二进制数据
	 * @param url
	 *            批量获取特征值接口url
	 * @return JSONArray 格式为
	 *         [{"result":"success","success":[{"图片名":"特征"},{}..]},{},..,{}]
	 */
	public static JSONArray getBatchFeature2(List<JSONObject> jsonInfo, String url) {
		String result = null;
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		MultipartEntityBuilder entity = MultipartEntityBuilder.create();

		int count = 0;
		JSONObject featureJson = new JSONObject();
		JSONArray array = new JSONArray();

		for (JSONObject json : jsonInfo) {
			byte[] filedata = json.getBytes("image_data");
			String fileName = json.getString("image_name");

			entity.addBinaryBody("imageDatas", filedata, ContentType.DEFAULT_BINARY, fileName);
			// 阈值判断，超过50张的分批发送
			if (++count % 50 == 0) {
				httpPost.setEntity(entity.build());
				CloseableHttpResponse response = null;
				HttpEntity resEntity = null;
				try {
					response = client.execute(httpPost);
					resEntity = response.getEntity();
					result = EntityUtils.toString(resEntity);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				array.add(JSON.parseObject(result));
				entity = MultipartEntityBuilder.create();
				continue;
			}

			if (count % 1000 == 0) {
				System.out.println("\n\tget fearure number : " + count);
			}
		}

		httpPost.setEntity(entity.build());

		CloseableHttpResponse response = null;
		HttpEntity resEntity = null;
		result = httpCommon(httpPost, response, resEntity, client);
		featureJson.put(String.valueOf(count), result);
		array.add(JSON.parseObject(result));
		return array;
	}

	public static JSONArray getImageBatchFeature(List<JSONObject> jsonInfo, String url) {
		String result = null;
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		MultipartEntityBuilder entity = MultipartEntityBuilder.create();

		int count = 0;
		JSONObject featureJson = new JSONObject();
		JSONArray array = new JSONArray();

		for (JSONObject json : jsonInfo) {
			String filePath = json.getString("real_name");

			entity.addPart("imageDatas",
					new FileBody(new File(filePath), ContentType.DEFAULT_BINARY, json.getString("image_name")));

			// 阈值判断，超过50张的分批发送
			if (++count % 50 == 0) {
				httpPost.setEntity(entity.build());
				CloseableHttpResponse response = null;
				HttpEntity resEntity = null;
				try {
					response = client.execute(httpPost);
					resEntity = response.getEntity();
					result = EntityUtils.toString(resEntity);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				array.add(JSON.parseObject(result));
				entity = MultipartEntityBuilder.create();
				continue;
			}

			if (count % 1000 == 0) {
				System.out.println("\n\tget fearure number : " + count);
			}
		}

		httpPost.setEntity(entity.build());

		CloseableHttpResponse response = null;
		HttpEntity resEntity = null;
		result = httpCommon(httpPost, response, resEntity, client);
		featureJson.put(String.valueOf(count), result);
		array.add(JSON.parseObject(result));
		return array;
	}

	// 数据批量导入时使用
	public static JSONObject getImageBatchFeature2(HashMap<String, String> nameMap, String url) {
		String result = null;
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		MultipartEntityBuilder entity = MultipartEntityBuilder.create();

		for (String filePath : nameMap.keySet()) {
			String uuidName = nameMap.getOrDefault(filePath, null);

			entity.addPart("imageDatas", new FileBody(new File(filePath), ContentType.DEFAULT_BINARY, uuidName));
		}

		httpPost.setEntity(entity.build());

		CloseableHttpResponse response = null;
		HttpEntity resEntity = null;
		result = httpCommon(httpPost, response, resEntity, client);
		return JSON.parseObject(result);
	}

	public static JSONObject getImageBatchFeature3(HttpEntity entity, String url) {
		String result = null;
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);

		JSONObject featureJson = new JSONObject();

		httpPost.setEntity(entity);

		CloseableHttpResponse response = null;
		HttpEntity resEntity = null;
		result = httpCommon(httpPost, response, resEntity, client);
		// featureJson.put(String.valueOf(count), result);
		// array.add(JSON.parseObject(result));
		featureJson = JSON.parseObject(result);
		return featureJson;
	}
}
