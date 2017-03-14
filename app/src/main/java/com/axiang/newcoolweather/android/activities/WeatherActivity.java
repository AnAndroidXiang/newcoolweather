package com.axiang.newcoolweather.android.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.axiang.newcoolweather.android.R;
import com.axiang.newcoolweather.android.gson.Forecast;
import com.axiang.newcoolweather.android.gson.Weather;
import com.axiang.newcoolweather.android.services.AutoUpdateService;
import com.axiang.newcoolweather.android.util.HttpUtil;
import com.axiang.newcoolweather.android.util.Utility;
import com.bumptech.glide.Glide;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private LinearLayout forecastLinear;

    private TextView headCityName;

    private TextView headUpdateTime;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView confortText;

    private TextView carWashText;

    private TextView sportText;

    private TextView degreeText;

    private TextView weatherInfoText;

    private ImageView bingSicImg;

    public SwipeRefreshLayout refreshLayout;

    public DrawerLayout drawerLayout;

    private ImageView moreImg;

    public String weatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21) {
            View desorView = getWindow().getDecorView();
            desorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initView();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sharedPreferences.getString("weather", null);
        String bingSicUrl = sharedPreferences.getString("bingSicUrl", null);
        if(bingSicUrl != null) {
            Glide.with(this).load(bingSicUrl).into(bingSicImg);
        } else {
            loadBingSic();
        }
        if(weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeather(weather);
        } else {
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        refreshLayout.setColorSchemeColors(Color.GREEN);
        refreshLayout.setOnRefreshListener(() -> {
            requestWeather(weatherId);
        });
        moreImg.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });
    }

    private void loadBingSic() {
        String address = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, R.string.bg_pic_fail,
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                String bingSicUrl = response.body().string();
                runOnUiThread(() -> {
                    SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences
                                (WeatherActivity.this).edit();
                    editor.putString("bingSicUrl", bingSicUrl);
                    editor.apply();
                    Glide.with(WeatherActivity.this).load(bingSicUrl)
                            .into(bingSicImg);
                });
            }
        });
    }

    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=ed180da08b2542ae85714c2d9d6d3c7f";
        HttpUtil.sendHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    refreshLayout.setRefreshing(false);
                    Toast.makeText(WeatherActivity.this, R.string.weather_fail,
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                final String weatherInfo = response.body().string();
                final Weather weather = Utility
                        .handleWeatherResponse(weatherInfo);
                runOnUiThread(() -> {
                    if(weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences
                                        (WeatherActivity.this).edit();
                        editor.putString("weather", weatherInfo);
                        editor.apply();
                        showWeather(weather);
                        refreshLayout.setRefreshing(false);
                        Intent intent = new Intent(WeatherActivity.this ,
                                AutoUpdateService.class);
                        startService(intent);
                    } else {
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(WeatherActivity.this, R.string
                                .weather_fail, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showWeather(Weather weather) {
        Resources resources = getResources();
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature
                + resources.getText(R.string.degrees);
        String weatherInfo = weather.now.cond.info;
        headCityName.setText(cityName);
        headUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLinear.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(
                    R.layout.forecast_item, forecastLinear, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLinear.addView(view);
        }
        if(weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        confortText.setText(resources.getText(R.string.comfortable) +
                weather.suggestion.confort.info);
        carWashText.setText(resources.getText(R.string.clear_car) +
                weather.suggestion.carwash.info);
        sportText.setText(resources.getText(R.string.sport) +
                weather.suggestion.sport.info);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    private void initView() {
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        headCityName = (TextView) findViewById(R.id.head_city_name);
        headUpdateTime = (TextView) findViewById(R.id.head_update_time);
        forecastLinear = (LinearLayout) findViewById(R.id.forecast_linear);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        confortText = (TextView) findViewById(R.id.confort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        bingSicImg = (ImageView) findViewById(R.id.bing_sic_img);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        moreImg = (ImageView) findViewById(R.id.img_more);
    }

}
