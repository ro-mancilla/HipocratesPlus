package com.example.hipocrates.data

import com.example.hipocrates.model.WeatherResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Documentación https://open-meteo.com/en/docs
// Agarrar JSON a través de Github y leerle sin necesidad de AWS

interface WeatherAPI {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weathercode",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/"

        fun create(): WeatherAPI {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(WeatherAPI::class.java)
        }
    }
}
