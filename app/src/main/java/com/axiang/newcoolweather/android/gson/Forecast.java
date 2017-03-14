package com.axiang.newcoolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by a2389 on 2017/3/8.
 */

public class Forecast {

    public String date;

    public Cond cond;

    @SerializedName("tmp")
    public Temperature temperature;

    public class Cond {

        @SerializedName("txt_d")
        public String info;

    }

    public class Temperature {

        public String max;

        public String min;

    }

}
