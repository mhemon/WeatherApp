package com.emon.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.emon.weatherapp.databinding.ActivityMapviewBinding
import com.emon.weatherapp.utils.Singleton
import com.emon.weatherapp.model.dataresponse
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapviewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding:ActivityMapviewBinding
    private lateinit var cityDetails: dataresponse.CityList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        cityDetails = Singleton.INSTANCE.cityDetails
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        loadview()
    }

    private fun loadview() {
        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.cityName.text = cityDetails.name
        binding.temperature.text = ((cityDetails.main?.temp!! - 273.15F).toInt().toString())+"°c"
        binding.maxTemp.text = "Max temp: "+((cityDetails.main?.tempMax!! - 273.15F).toInt().toString())+"°c"
        binding.minTemp.text = "Min temp: "+((cityDetails.main?.tempMin!! - 273.15F).toInt().toString())+"°c"
        binding.humidity.text = "Humidity: "+ cityDetails.main?.humidity.toString()
        binding.description.text = cityDetails.weather!![0].description
        binding.speed.text = "Wind speed: "+cityDetails.wind?.speed.toString()
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        val city = LatLng(cityDetails.coord?.lat!!, cityDetails.coord!!.lon!!)
        mMap.addMarker(MarkerOptions().position(city).title(cityDetails.name))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(city, 10f))
    }
}