package com.alex.theweatherapp.features.home.domain.repository

import com.alex.theweatherapp.features.home.data.local.room.dao.ForecastDao
import com.alex.theweatherapp.features.home.data.local.room.entity.ForecastEntity
import com.alex.theweatherapp.features.home.data.network.HomeApi
import com.alex.theweatherapp.features.home.data.network.dto.ForecastRequestDto
import com.alex.theweatherapp.features.home.data.network.dto.ForecastResponseDto
import com.alex.theweatherapp.features.home.data.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeApi: HomeApi,
    private val forecastDao: ForecastDao
) : HomeRepository {

    override suspend fun getCachedForecast(city: String): List<ForecastEntity> {
        return forecastDao.getForecastsByCity(city = city)
    }

    override suspend fun fetchForecastFromNetwork(request: ForecastRequestDto): ForecastResponseDto {
        return homeApi.getForecast(request.toQueryMap())
    }

    override suspend fun saveForecast(city: String, entities: List<ForecastEntity>) {
        forecastDao.deleteForecastsByCity(city = city)
        forecastDao.insertForecasts(forecasts = entities)
    }
}