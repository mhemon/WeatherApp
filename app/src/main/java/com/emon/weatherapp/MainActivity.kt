package com.emon.weatherapp

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log.d
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.emon.weatherapp.adapter.MyAdapter
import com.emon.weatherapp.api.apiset
import com.emon.weatherapp.databinding.ActivityMainBinding
import com.emon.weatherapp.model.dataresponse
import com.emon.weatherapp.model.tempResponse
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

const val BASE_URL = "http://api.openweathermap.org/data/"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var myAdapter: MyAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    val PERMISSION_ID = 1010

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.recyclerview.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerview.layoutManager = linearLayoutManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        RequestPermission()
        getLastLocation()
        getmyData()
        binding.pulltorefresh.setOnRefreshListener {
            getmyData()
            Handler().postDelayed(Runnable {
                binding.pulltorefresh.isRefreshing = false
                Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show()
            }, 4000)
        }
        createNotificationChannel()
    }

    fun CheckPermission(): Boolean {
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if (
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false

    }

    fun RequestPermission() {
        //this function will allows us to tell the user to requesut the necessary permsiion if they are not garented
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    fun isLocationEnabled(): Boolean {
        //this function will return to us the state of the location service
        //if the gps or the network provider is enabled then it will return true otherwise it will return false
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun getLastLocation() {
        if (CheckPermission()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        NewLocationData()
                    } else {
                        d("Debug:", "Your Location:" + location.longitude)
                        //textView.text = "You Current Location is : Long: "+ location.longitude + " , Lat: " + location.latitude + "\n" + getCityName(location.latitude,location.longitude)
                        var latit = location.latitude
                        var longi = location.longitude
                        scheduleNotification(latit, longi)
                    }
                }
            } else {
                Toast.makeText(this, "Please Turn on Your device Location", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            RequestPermission()
        }
    }

    fun NewLocationData() {
        var locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        Looper.myLooper()?.let {
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest, locationCallback, it
            )
        }
    }


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            d("Debug:", "your last last location: " + lastLocation.longitude.toString())
            var latit = lastLocation.latitude
            var longi = lastLocation.longitude
            scheduleNotification(latit, longi)
        }
    }


    private fun getmyData() {
        val retrofitbuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(apiset::class.java)

        val retrofitdata = retrofitbuilder.getCity("23.68","90.35","50","f4e93641e76ebfcde9f87984a60c7f5b")
        retrofitdata.enqueue(object : Callback<dataresponse?> {
            override fun onResponse(call: Call<dataresponse?>, response: Response<dataresponse?>) {
                val resposebody = response.body()!!
                myAdapter = MyAdapter(
                    baseContext,
                    resposebody.getList() as List<dataresponse.CityList>
                )
                myAdapter.notifyDataSetChanged()
                binding.recyclerview.adapter = myAdapter
            }

            override fun onFailure(call: Call<dataresponse?>, t: Throwable) {
                d("MainActivity", "onFailure " + t.message)
            }
        })
    }


    private fun createNotificationChannel()
    {
        val name = "Weather Notification"
        val desc = "Weather app notification Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun scheduleNotification(lateit: Double,longi: Double)
    {

        val retrofitbuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(apiset::class.java)

        val retrofitdata = retrofitbuilder.getTemp(
            lateit.toString(),
            longi.toString(),
            getString(R.string.weather_api_key)
        )

        retrofitdata.enqueue(object : Callback<tempResponse?> {
            override fun onResponse(
                call: Call<tempResponse?>,
                response: Response<tempResponse?>
            ) {
                val temp = (response.body()!!.main.temp - 273.15F).toInt().toString()
                val intent = Intent(applicationContext, Notification::class.java)
                d("temp", "Your Notification Temp:$temp")
                val title = "WeatherApp"
                val message = "Current Temperature is $temp" +resources.getString(R.string.icon
                )
                intent.putExtra(titleExtra, title)
                intent.putExtra(messageExtra, message)

                val pendingIntent = PendingIntent.getBroadcast(
                    applicationContext,
                    notificationID,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                //notification will be shown everyday
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),1000 * 60 * 60 * 24,pendingIntent)
            }

            override fun onFailure(call: Call<tempResponse?>, t: Throwable) {
                d("tempFailure", "onFailure " + t.message)
            }
        })


    }
}