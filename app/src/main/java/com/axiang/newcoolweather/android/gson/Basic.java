package com.axiang.newcoolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by a2389 on 2017/3/8.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;

    }

}
