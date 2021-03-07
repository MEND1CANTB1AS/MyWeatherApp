package com.example.weatherapp

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myweatherapp.R
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
//import kotlinx.android.synthetic.main.activity_main.*
import java.security.Permission
import java.security.Provider
import java.util.*

class MainActivity : AppCompatActivity() {

    val CITY: String = "rexburg,us"
    val API: String = "c9b5e5ab3376bc7f5a7d62f74e9f7998" // Use API key
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    val PERMISSION_ID = 1010


    fun CheckPermission():Boolean{
        //this function will return a boolean
        //true: if we have permission
        //false if not
        if(
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }

        return false

    }
    fun RequestPermission(){
        //this function will allows us to tell the user to requesut the necessary permsiion if they are not garented
        ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_ID
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherTask().execute()

    }

    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            /* Showing the ProgressBar, Making the main design GONE */
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=imperial&appid=$API").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val sight = jsonObj.getString("visibility")+ " m"

                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: "+ SimpleDateFormat(
                    "MM/dd/yyyy hh:mm a",
                    Locale.ENGLISH
                ).format(Date(updatedAt * 1000))
                val temp = main.getString("temp")+"°F"
                val tempMin = main.getString("temp_min")+"°F"
                val tempMax = main.getString("temp_max")+"°F"
                val pressure = main.getString("pressure")+" hPa"
                val humidity = main.getString("humidity")+" %"
                val windSpeed = wind.getString("speed")+" m/h"
                val weatherDescription = weather.getString("description")
//                val view_distance = sight.getString("visibility")
                val address = jsonObj.getString("name")+", "+sys.getString("country")

                /* Populating extracted data into our views */
                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text =  updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.toUpperCase()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity
                findViewById<TextView>(R.id.view_distance).text = sight

                val img = findViewById(R.id.weatherImage) as ImageView
                if (weatherDescription == "clear sky") {
                    img.setImageResource(R.drawable.sunny)
                }
                else if (weatherDescription == "few clouds" || weatherDescription == "broken clouds") {
                    img.setImageResource(R.drawable.partly_cloudy)
                }
                else if (weatherDescription == "shower rain" || weatherDescription == "rain") {
                    img.setImageResource(R.drawable.rainy)
                }
                else if (weatherDescription == "snow") {
                    img.setImageResource(R.drawable.snowy)
                }
                else if (weatherDescription == "scattered clouds") {
                    img.setImageResource(R.drawable.cloudy)
                }
                else if (weatherDescription == "thunderstorm") {
                    img.setImageResource(R.drawable.lightning)
                }
                /* Views populated, Hiding the loader, Showing the main design */
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }

        }
    }
}