package com.alex.theweatherapp.features.home.data.network

import com.alex.theweatherapp.features.home.data.network.dto.ForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface HomeApi {
    @GET("data/2.5/forecast/daily")
    suspend fun getForecast(
        @QueryMap options: Map<String, String>
    ): ForecastResponseDto
}