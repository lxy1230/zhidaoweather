package com.example.zhidaoweather.service;

import com.example.zhidaoweather.util.HttpCallbackListener;
import com.example.zhidaoweather.util.HttpUtil;
import com.example.zhidaoweather.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service
{

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				updateWeather();
			}
		}.start();
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int forHour = 4*60*60*1000;
		long triggerAtMillis = SystemClock.elapsedRealtime()+ forHour;
		Intent i = new Intent(this,AutoUpdateService.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pi);
		return super.onStartCommand(intent, flags, startId);
	}
	
	/**
	 * 更新天气信息
	 */
	private void updateWeather()
	{
		SharedPreferences spfs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode = spfs.getString("weather_code", "");
		String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener()
		{
			
			@Override
			public void onFinish(String response)
			{
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
			}
			
			@Override
			public void onError(Exception e)
			{
				e.printStackTrace();
			}
		});
	}
}
