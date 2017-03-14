package com.axiang.newcoolweather.android.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.axiang.newcoolweather.android.gson.Weather;
import com.axiang.newcoolweather.android.util.HttpUtil;
import com.axiang.newcoolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingSic();
        AlarmManager alarmManager = (AlarmManager)
                getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent updateIntent = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent
                .getService(this, 0, updateIntent, 0);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
                pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String weatherString = sharedPreferences.getString("weather", null);
        if(weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            if(weather != null && "ok".equals(weather.status)) {
                String weatherId = weather.basic.weatherId;
                String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                        weatherId + "&key=ed180da08b2542ae85714c2d9d6d3c7f";
                HttpUtil.sendHttpRequest(weatherUrl, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response)
                            throws IOException {
                        String weatherInfo = response.body().string();
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences
                                        (AutoUpdateService.this).edit();
                        editor.putString("weather", weatherInfo);
                        editor.apply();
                    }
                });
            }
        }
    }

    private void updateBingSic() {
        String bingSicUrl = "http://guolin.tech/api/bing_pic";
        if(bingSicUrl != null) {
            HttpUtil.sendHttpRequest(bingSicUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {
                    String bingSicInfo = response.body().string();
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences
                                    (AutoUpdateService.this).edit();
                    editor.putString("bingSicUrl", bingSicInfo);
                    editor.apply();
                }
            });
        }
    }

}
