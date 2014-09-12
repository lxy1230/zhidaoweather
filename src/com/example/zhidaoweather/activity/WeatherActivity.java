package com.example.zhidaoweather.activity;

import java.net.URL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.zhidaoweather.R;
import com.example.zhidaoweather.service.AutoUpdateService;
import com.example.zhidaoweather.util.HttpCallbackListener;
import com.example.zhidaoweather.util.HttpUtil;
import com.example.zhidaoweather.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener
{
	private LinearLayout ll_weatherInfo;
	private TextView tv_cityName;
	private TextView tv_publishTime;
	private TextView tv_weatherDesc;
	private TextView tv_temp1;
	private TextView tv_temp2;
	private ImageView iv_img1;
	private ImageView iv_img2;
	private TextView tv_currentDate;
	private Button bt_switchCity;
	private Button bt_refreshWeather;
	private Bitmap bmp1,bmp2;
	
	@SuppressLint("HandlerLeak") private Handler handler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			if(msg.what == 1)
			{
				iv_img1.setImageBitmap(bmp1);
				iv_img2.setImageBitmap(bmp2);
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		
		initView();
		String countyCode = getIntent().getStringExtra("county_code");
		if(!TextUtils.isEmpty(countyCode))
		{
			//有县级代号就去查询天气
			tv_publishTime.setText("同步中...");
			ll_weatherInfo.setVisibility(View.INVISIBLE);
			tv_cityName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}
		else{
			//没有县级代码就直接显示本地天气
			showWeather();
		}
		bt_switchCity.setOnClickListener(this);
		bt_refreshWeather.setOnClickListener(this);
	}
	/**
	 * 初始化各个控件
	 */
	private void initView()
	{
		ll_weatherInfo = (LinearLayout) findViewById(R.id.ll_weatherInfo);
		tv_cityName = (TextView) findViewById(R.id.tv_cityName);
		tv_currentDate = (TextView) findViewById(R.id.tv_currentDate);
		tv_publishTime = (TextView) findViewById(R.id.tv_publishTime);
		tv_temp1 = (TextView) findViewById(R.id.tv_temp1);
		tv_temp2 = (TextView) findViewById(R.id.tv_temp2);
		iv_img1 = (ImageView) findViewById(R.id.iv_img1);
		iv_img2 = (ImageView) findViewById(R.id.iv_img2);
		tv_weatherDesc = (TextView) findViewById(R.id.tv_weatherDesc);
		bt_switchCity = (Button) findViewById(R.id.bt_switchCity);
		bt_refreshWeather = (Button) findViewById(R.id.bt_refreshWeather);
	}
	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.bt_switchCity:
				Intent intent = new Intent(this, ChooseAreaActivity.class);
				intent.putExtra("from_weather_activity", true);
				startActivity(intent);
				finish();
				break;
			case R.id.bt_refreshWeather:
				tv_publishTime.setText("同步中...");
				SharedPreferences sdf = PreferenceManager.getDefaultSharedPreferences(this);
				String weatherCode = sdf.getString("weather_code", "");
				if(!TextUtils.isEmpty(weatherCode))
				{
					queryWeatherInfo(weatherCode);
				}
				break;
			default:
				break;
		}
	}
	
	/**
	 * 查询县级代号所对应的天气
	 */
	private void queryWeatherCode(String countyCode)
	{
		String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		queryFromServer(address,"countyCode");
	}
	
	/**
	 * 查询天气代码所对应的天气
	 */
	private void queryWeatherInfo(String weatherCode)
	{
		String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFromServer(address,"weatherCode");
	}
	
	/**
	 * 根据传入的地址和数据类型向服务器查询天气代码或者天气信息
	 */
	private void queryFromServer(final String address, final String type)
	{
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener()
		{
			
			@Override
			public void onFinish(String response)
			{
				if("countyCode".equals(type))
				{
					if(!TextUtils.isEmpty(response))
					{
						//从服务器返回的数据解析出天气代码
						String[] array = response.split("\\|");
						if(array!=null && array.length == 2)
						{
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}
				else if("weatherCode".equals(type))
				{
					//处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable()
					{
						
						@Override
						public void run()
						{
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e)
			{
				tv_publishTime.setText("同步失败");
			}
		});
	}
	/**
	 * 从SharedPreferences中读取存储的天气信息，并显示到列表中
	 */
	private void showWeather()
	{
		SharedPreferences sdf = PreferenceManager.getDefaultSharedPreferences(this);
		tv_cityName.setText(sdf.getString("city_name", ""));
		tv_temp1.setText(sdf.getString("temp1", ""));
		tv_temp2.setText(sdf.getString("temp2", ""));
		setImage(sdf);
		tv_weatherDesc.setText(sdf.getString("weather_desc", ""));
		tv_publishTime.setText("发布时间: "+sdf.getString("publish_time", ""));
		tv_currentDate.setText(sdf.getString("current_date", ""));
		ll_weatherInfo.setVisibility(View.VISIBLE);
		tv_cityName.setVisibility(View.VISIBLE);
		Intent service = new Intent(this,AutoUpdateService.class);
		startService(service);
	}
	private void setImage(final SharedPreferences sdf)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				Message msg = new Message();
				msg.what = 1;
				String imgUrl1 = "http://m.weather.com.cn/img/"+sdf.getString("img1", null);
				String imgUrl2 = "http://m.weather.com.cn/img/"+sdf.getString("img2", null);
				try
				{
					URL imgURL1 = new URL(imgUrl1);
					URL imgURL2 = new URL(imgUrl2);
					bmp1 = BitmapFactory.decodeStream(imgURL1.openStream());
					bmp2 = BitmapFactory.decodeStream(imgURL2.openStream());
					handler.sendMessage(msg);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}
}
