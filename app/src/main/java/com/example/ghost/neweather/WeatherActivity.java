package com.example.ghost.neweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ghost.neweather.gson.Forecast;
import com.example.ghost.neweather.gson.Weather;
import com.example.ghost.neweather.service.UpdateService;
import com.example.ghost.neweather.util.HttpUtil;
import com.example.ghost.neweather.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    private Button homebtn;
    public SwipeRefreshLayout swipeRefreshLayout;
    private String mWeatherId;

    private ImageView bingying;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleupdatetime;
    private TextView degreetext;
    private TextView weatherinfoText;
    private TextView aqitext;
    private TextView pm25text;
    private TextView comforttext;
    private TextView cartext;
    private TextView sporttext;
    private LinearLayout forecastLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (Build.VERSION.SDK_INT >= 21){
            View decorview = getWindow().getDecorView();
            decorview.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        //初始化
        bingying=(ImageView)findViewById(R.id.bingying_img);
        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleupdatetime=(TextView)findViewById(R.id.title_update);
        degreetext=(TextView)findViewById(R.id.degree_t);
        weatherinfoText=(TextView)findViewById(R.id.weather_info);
        forecastLayout=(LinearLayout)findViewById(R.id.forecast);
        aqitext=(TextView)findViewById(R.id.aqi_text);
        pm25text=(TextView)findViewById(R.id.pm25_text);
        comforttext=(TextView)findViewById(R.id.comfort_text);
        cartext=(TextView)findViewById(R.id.car_text);
        sporttext=(TextView)findViewById(R.id.sport_text);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        homebtn=(Button)findViewById(R.id.home_btn);
        drawerLayout=(DrawerLayout)findViewById(R.id.draw_layout);
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather",null);
        if (weatherString!=null){
            //有缓存直接解析数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWweatherinfo(weather);
        }else {
            //无缓存服务器查询
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        String bing=preferences.getString("bing",null);
        if (bing!=null){
            Glide.with(this).load(bing).into(bingying);
        }else {
            loadbing();
        }
    }


    public void requestWeather(final String weatherId) {
        String weatherUrl="http://guolin.tech/api/weather?cityid=" + weatherId + "&key=7df031c83b9544ea89d1c6f4a67c0852";
       // this.mWeatherId=weatherId;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String responseText = response.body().string();
            final Weather weather=Utility.handleWeatherResponse(responseText);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (weather !=null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor =PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather",responseText);
                        editor.apply();
                        mWeatherId=weather.basic.weatherId;
                        showWweatherinfo(weather);
                    }else {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                });
                loadbing();
            }
        });
    }
    //加载背景图片
    private void loadbing() {
        String requestBing = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBing, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final  String bing = response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing",bing);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bing).into(bingying);
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }


        });
    }
//处理Wearther实体类中的数据
    private void showWweatherinfo(Weather weather) {
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature + "°C";
        String weatherInfo =weather.now.more.info;
        titleCity.setText(cityName);
        titleupdatetime.setText(updateTime);
        degreetext.setText(degree);
        weatherinfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi !=null){
            aqitext.setText(weather.aqi.city.aqi);
            pm25text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度" + weather.suggestion.comfort.info;
        String carWash = "洗车指数" + weather.suggestion.carWash.info;
        String sport = "运动建议" + weather.suggestion.sport.info;
        comforttext.setText(comfort);
        cartext.setText(carWash);
        sporttext.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, UpdateService.class);
        startService(intent);
    }
}
