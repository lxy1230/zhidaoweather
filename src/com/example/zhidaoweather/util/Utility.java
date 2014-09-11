package com.example.zhidaoweather.util;

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
}
