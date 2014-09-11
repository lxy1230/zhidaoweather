package com.example.zhidaoweather.util;

public interface HttpCallbackListener
{
	void onFinish(String response);
	
	void onError(Exception e);
}
