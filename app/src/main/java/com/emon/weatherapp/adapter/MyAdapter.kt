package com.emon.weatherapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emon.weatherapp.MapviewActivity
import com.emon.weatherapp.R
import com.emon.weatherapp.utils.Singleton
import com.emon.weatherapp.model.dataresponse

class MyAdapter(private val context:Context, private val cityList: List<dataresponse.CityList>):RecyclerView.Adapter<MyAdapter.viewholder>() {
    class viewholder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var cityname : TextView = itemView.findViewById(R.id.cityname)
        var cloud : TextView = itemView.findViewById(R.id.cloud)
        var temp : TextView = itemView.findViewById(R.id.temp)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item,parent,false)
        return viewholder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: viewholder, position: Int) {

        val city = cityList[position]
        holder.cityname.text = city.name.toString()
        holder.cloud.text = city.weather!![0].description
        holder.temp.text = (city.main?.temp!! - 273.15F).toInt().toString()+
                context.resources.getString(R.string.icon)

        holder.itemView.setOnClickListener {
            Singleton.INSTANCE.cityDetails = city
            val intent = Intent(context, MapviewActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return cityList.size
    }
}