package com.example.ghost.neweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ghost on 2018/2/2.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
}
