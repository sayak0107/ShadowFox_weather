package com.example.weatherapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.SearchView
import com.example.weatherapplication.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val API_KEY = "c1ff33440ee63255ca2819a600bd2258"

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData("Medinipur")  // Example city, replace with actual city name
        setupSearchCity()
    }

    private fun setupSearchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    // Function to fetch weather data from OpenWeather API
    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)
        val response = retrofit.getWeatherData(cityName, API_KEY, "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    // Get weather data from the response
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val condition = responseBody.weather.firstOrNull()?.main ?: "Unknown"
                    val city = responseBody.name
                    val sunRise = responseBody.sys.sunrise
                    val sunSet = responseBody.sys.sunset
                    val rainInfo = responseBody.rain?.`1h` ?: 0.0 // Rain info, if available
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min

                    // Update the UI with the fetched weather data
                    binding.weather.text = condition
                    binding.maxTemp.text = "Max Temp: $maxTemp °C"
                    binding.minTemp.text = "Min Temp: $minTemp °C"
                    binding.temp.text = "$temperature °C"
                    binding.humidity.text = "$humidity %"
                    binding.windSpeed.text = "$windSpeed Km/h"
                    binding.sunrise.text = convertUnixToTime(sunRise.toLong())
                    binding.sunset.text = convertUnixToTime(sunSet.toLong())
                    binding.date.text = currentDate()
                    binding.day.text = currentDayName()
                    binding.cityName.text = city
                    binding.condition.text = condition

                    // Optionally, log rain information if available
                    Log.d("MainActivity", "Rain info: ${rainInfo}mm/h in the last 1 hours")

                    changeImagesAccordingToWeatherCondition(condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.e("MainActivity", "Failed to fetch weather data: ${t.message}")
            }
        })
    }

    private fun changeImagesAccordingToWeatherCondition(condition: String) {
        when (condition) {
            "Haze", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.weathericon.setImageResource(R.drawable.cloudy)
                binding.icon.setImageResource(R.drawable.cloudy)
            }
            "Clear Sky", "Sunny", "Clear" -> {
                binding.weathericon.setImageResource(R.drawable.sunny)
                binding.icon.setImageResource(R.drawable.sunny)
            }
            "Rain", "Showers", "Heavy Rain", "Moderate Rain", "Light Rain", "Drizzle" -> {
                binding.weathericon.setImageResource(R.drawable.rainy)
                binding.icon.setImageResource(R.drawable.rainy)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.weathericon.setImageResource(R.drawable.snowy)
                binding.icon.setImageResource(R.drawable.snowy)
            }
            "Storm" -> {
                binding.weathericon.setImageResource(R.drawable.storm)
                binding.icon.setImageResource(R.drawable.storm)
            }
            "Partly Cloudy", "Cloudy Sunny" -> {
                binding.weathericon.setImageResource(R.drawable.cloudy_sunny)
                binding.icon.setImageResource(R.drawable.cloudy_sunny)
            }
        }
    }

    // Function to convert Unix timestamp (in seconds) to a readable time format (HH:mm a)
    private fun convertUnixToTime(time: Long): String {
        val date = Date(time * 1000L)  // Convert seconds to milliseconds
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(date)
    }

    // Function to get the current date in a readable format
    private fun currentDate(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    // Function to get the current day name
    private fun currentDayName(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }
}






























// final code