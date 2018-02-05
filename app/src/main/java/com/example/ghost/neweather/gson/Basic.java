package com.example.ghost.neweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ghost on 2018/2/2.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
