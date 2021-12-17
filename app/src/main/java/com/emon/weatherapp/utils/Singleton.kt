package com.emon.weatherapp.utils

import com.emon.weatherapp.model.dataresponse

class Singleton {

    companion object {
        val INSTANCE = Singleton()
    }
    lateinit var cityDetails: dataresponse.CityList

}