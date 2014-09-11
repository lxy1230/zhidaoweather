package com.example.zhidaoweather.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zhidaoweather.R;
import com.example.zhidaoweather.model.City;
import com.example.zhidaoweather.model.County;
import com.example.zhidaoweather.model.Province;
import com.example.zhidaoweather.model.WeatherDB;
import com.example.zhidaoweather.util.HttpCallbackListener;
import com.example.zhidaoweather.util.HttpUtil;
import com.example.zhidaoweather.util.Utility;

public class ChooseAreaActivity extends Activity
{
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView tv_title;
	private ListView lv_list;
	private ArrayAdapter<String> adapter;
	private WeatherDB weatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	private List<Province> provinceList; //省列表
	private List<City> cityList; //省列表
	private List<County> countyList; //省列表
	
	private Province selectedProvince; //选中的省
	private City selectedCity; //选中的市
	
	private int currentLevel; //当前选中的级别
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		tv_title = (TextView) findViewById(R.id.tv_title);
		lv_list = (ListView) findViewById(R.id.lv_list);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,dataList);
		lv_list.setAdapter(adapter);
		weatherDB = WeatherDB.getInstance(this);
		queryProvinces();//查询省级列表
		lv_list.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if(currentLevel == LEVEL_PROVINCE)
				{
					selectedProvince = provinceList.get(position);
					queryCities();//查询城市列表
				}
				else if(currentLevel == LEVEL_CITY)
				{
					selectedCity = cityList.get(position);
					queryCounties();//查询县级列表
				}
			}
		});
	}
	
	/**
	 * 查询全国所有省
	 */
	private void queryProvinces()
	{
		provinceList = weatherDB.loadProvinces();
		if(provinceList.size()>0)
		{
			dataList.clear();
			for (Province province : provinceList)
			{
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			lv_list.setSelection(0);
			tv_title.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}
		else{
			queryFromServer(null, "province");
		}
	}
	
	/**
	 * 查询选中省内所有的市
	 */
	private void queryCities()
	{
		cityList = weatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0)
		{
			dataList.clear();
			for (City city : cityList)
			{
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			lv_list.setSelection(0);
			tv_title.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}
		else{
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/**
	 * 查询选中市内所有的县
	 */
	private void queryCounties()
	{
		countyList = weatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0)
		{
			dataList.clear();
			for (County county : countyList)
			{
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			lv_list.setSelection(0);
			tv_title.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}
		else{
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}

	/**
	 * 根据传入的代码和类型从服务器上查询省市县数据
	 * @param cityCode
	 * @param string
	 */
	private void queryFromServer(final String code, final String type)
	{
		String address;
		if(!TextUtils.isEmpty(code))
		{
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		}
		else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener()
		{
			
			@Override
			public void onFinish(String response)
			{
				boolean result = false;
				if("province".equals(type))
				{
					result = Utility.handleProvincesResponse(weatherDB, response);
				}
				else if("city".equals(type))
				{
					result = Utility.handleCitiesResponse(weatherDB, response, selectedProvince.getId());
				}
				else if("county".equals(type))
				{
					result = Utility.handleCountiesResponse(weatherDB, response, selectedCity.getId());
				}
				if(result)
				{
					//通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable()
					{
						public void run()
						{
							closeProgressDialog();
							if("province".equals(type))
							{
								queryProvinces();
							}
							else if("city".equals(type))
							{
								queryCities();
							}
							else if("county".equals(type))
							{
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e)
			{
				// 通过runOnUiThread()方法返回主线程
				runOnUiThread(new Runnable()
				{
					
					@Override
					public void run()
					{
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载数据失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	
	/**
	 * 显示进度条
	 */
	private void showProgressDialog()
	{
		if(progressDialog == null)
		{
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * 取消进度条
	 */
	private void closeProgressDialog()
	{
		if(progressDialog != null)
		{
			progressDialog.dismiss();
		}
	}
	
	/**
	 * 捕获back按键，根据当前所选级别来判断，此时应该返回市列表、省列表、还是直接退出
	 */
	@Override
	public void onBackPressed()
	{
		if(currentLevel == LEVEL_COUNTY)
		{
			queryCities();
		}
		else if(currentLevel == LEVEL_CITY)
		{
			queryProvinces();
		}
		else{
			finish();
		}
	}
}
