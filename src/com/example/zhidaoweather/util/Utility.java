package com.example.zhidaoweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.zhidaoweather.model.City;
import com.example.zhidaoweather.model.County;
import com.example.zhidaoweather.model.Province;
import com.example.zhidaoweather.model.WeatherDB;

public class Utility
{
	/**
	 * 解析和处理服务器返回的省级数据
	 */
	public synchronized static boolean handleProvincesResponse(WeatherDB weatherDB, String response)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] provinces = response.split(",");
			if(provinces != null && provinces.length>0)
			{
				for (String item : provinces)
				{
					String[] entity = item.split("\\|");
					Province province = new Province();
					province.setProvinceCode(entity[0]);
					province.setProvinceName(entity[1]);
					//将解析出来的省份信息保存到数据库中
					weatherDB.saveProvince(province);
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 解析和处理服务器返回的市级数据
	 */
	public static boolean handleCitiesResponse(WeatherDB weatherDB, String response, int provinceId)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] cities = response.split(",");
			if(cities != null && cities.length>0)
			{
				for (String item : cities)
				{
					String[] entity = item.split("\\|");
					City city = new City();
					city.setCityCode(entity[0]);
					city.setCityName(entity[1]);
					city.setProvinceId(provinceId);
					weatherDB.saveCity(city);
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 解析并处理服务器返回的县级数据
	 */
	public static boolean handleCountiesResponse(WeatherDB weatherDB, String response, int cityId)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] counties = response.split(",");
			if(counties != null && counties.length >0)
			{
				for (String item : counties)
				{
					String[] entity = item.split("\\|");
					County county = new County();
					county.setCountyCode(entity[0]);
					county.setCountyName(entity[1]);
					county.setCityId(cityId);
					weatherDB.saveCounty(county);
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 解析并处理服务器返回的json数据,并保存到本地
	 */
	public static void handleWeatherResponse(Context context, String response)
	{
		try
		{
			JSONObject job = new JSONObject(response);
			JSONObject weatherInfo = job.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesc = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesc,publishTime);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
	}

	/**
	 * 
	 * 将服务器返回并解析的JSON数据保存到SharedPreferences文件中
	 */
	private static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesc,
			String publishTime)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日    E",Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desc", weatherDesc);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}
}
