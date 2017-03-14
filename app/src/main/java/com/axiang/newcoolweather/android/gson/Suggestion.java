package com.axiang.newcoolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by a2389 on 2017/3/8.
 */

public class Suggestion {

    @SerializedName("comf")
    public Confort confort;

    @SerializedName("cw")
    public Carwash carwash;

    public Sport sport;

    public class Confort {

        @SerializedName("txt")
        public String info;

    }

    public class Carwash {

        @SerializedName("txt")
        public String info;

    }

    public class Sport {

        @SerializedName("txt")
        public String info;

    }

}
