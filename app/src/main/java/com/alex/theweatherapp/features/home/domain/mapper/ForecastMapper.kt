package com.alex.theweatherapp.features.home.domain.mapper

import com.alex.theweatherapp.features.home.data.local.room.entity.ForecastEntity
import com.alex.theweatherapp.features.home.data.network.dto.ForecastResponseDto
import com.alex.theweatherapp.features.home.domain.model.DailyForecast
import com.alex.theweatherapp.features.home.domain.model.ForecastDomain

fun ForecastResponseDto.toWeeklyDomainFromResponse(): ForecastDomain? {
    if (list.isEmpty()) return null

    val dailyForecasts = list.map { forecast ->
        DailyForecast(
            dt = forecast.dt,
            tempMin = forecast.temp.min,
            tempMax = forecast.temp.max,
            weatherDescription = forecast.weather.firstOrNull()?.description ?: ""
        )
    }

    return ForecastDomain(
        city = city.name,
        dailyForecasts = dailyForecasts
    )
}

fun ForecastDomain.toEntities(): List<ForecastEntity> {
    val currentTime = System.currentTimeMillis()
    return dailyForecasts.map { daily ->
        ForecastEntity(
            city = city,
            date = daily.dt,
            tempMin = daily.tempMin,
            tempMax = daily.tempMax,
            weatherDescription = daily.weatherDescription,
            fetchedAt = currentTime
        )
    }
}

fun List<ForecastEntity>.toWeeklyDomainFromCache(): ForecastDomain? {
    if (isEmpty()) return null

    val cityName = first().city
    val dailyForecasts = this.map { entity ->
        DailyForecast(
            dt = entity.date,
            tempMin = entity.tempMin,
            tempMax = entity.tempMax,
            weatherDescription = entity.weatherDescription
        )
    }

    return ForecastDomain(
        city = cityName,
        dailyForecasts = dailyForecasts
    )
}
