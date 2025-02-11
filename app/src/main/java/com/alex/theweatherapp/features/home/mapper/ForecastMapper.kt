package com.alex.theweatherapp.features.home.mapper

import com.alex.theweatherapp.features.home.data.local.entity.ForecastEntity
import com.alex.theweatherapp.features.home.data.network.dto.forecast.ForecastResponseDto
import com.alex.theweatherapp.features.home.domain.model.Forecast

fun ForecastResponseDto.toDomainModel(): Forecast {
    return Forecast(
    //TODO;
    )
}

fun Forecast.toEntity(): ForecastEntity {
    return ForecastEntity(
    //TODO;
    )
}