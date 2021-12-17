package com.emon.weatherapp.api

import com.emon.weatherapp.model.dataresponse
import com.emon.weatherapp.model.tempResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface apiset {

     @GET("2.5/find")
     fun getCity(
         @Query("lat") lat: String,
         @Query("lon") lon: String,
         @Query("cnt") cnt: String,
         @Query("appid") key: String
     ): Call<dataresponse>

    @GET("2.5/weather")
    fun getTemp(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") key: String
    ): Call<tempResponse>
}