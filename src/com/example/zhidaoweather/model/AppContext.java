package com.example.zhidaoweather.model;

import android.app.Application;
import android.content.Context;

public class AppContext extends Application
{
	private static Context context;
	
	@Override
	public void onCreate()
	{
		context = getApplicationContext();
	}
	
	public static Context getIntance()
	{
		return context;
	}
}
