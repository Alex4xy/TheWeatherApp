package com.alex.theweatherapp.features.home.data.repository

import com.alex.theweatherapp.features.home.data.local.room.entity.ForecastEntity
import com.alex.theweatherapp.features.home.data.network.dto.ForecastRequestDto
import com.alex.theweatherapp.features.home.data.network.dto.ForecastResponseDto

interface HomeRepository {
    suspend fun getCachedForecast(city: String): List<ForecastEntity>
    suspend fun fetchForecastFromNetwork(request: ForecastRequestDto): ForecastResponseDto
    suspend fun saveForecast(city: String, entities: List<ForecastEntity>)
}