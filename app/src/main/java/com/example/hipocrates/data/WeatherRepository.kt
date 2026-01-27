package com.example.hipocrates.data

import com.example.hipocrates.model.WeatherData
import com.example.hipocrates.model.getWeatherDescription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val apiService: WeatherApiService) {

    // Coordenadas para Valparaiso
    private val valparaisoLatitude = -33.0472
    private val valparaisoLongitude = -71.6127

    suspend fun getValparaisoWeather(): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentWeather(
                latitude = valparaisoLatitude,
                longitude = valparaisoLongitude
            )

            val weatherData = WeatherData(
                temperature = response.current.temperature_2m,
                weatherCode = response.current.weathercode,
                weatherDescription = getWeatherDescription(response.current.weathercode)
            )

            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var instance: WeatherRepository? = null

        fun getInstance(): WeatherRepository {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepository(WeatherApiService.create()).also { instance = it }
            }
        }
    }
}
