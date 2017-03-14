package com.axiang.newcoolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by a2389 on 2017/3/8.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    public Cond cond;

    public class Cond {

        @SerializedName("txt")
        public String info;

    }

}
