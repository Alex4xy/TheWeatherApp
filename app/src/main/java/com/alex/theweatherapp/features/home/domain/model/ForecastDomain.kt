package com.alex.theweatherapp.features.home.domain.model

data class ForecastDomain(
    val city: String,
    val dailyForecasts: List<DailyForecast>
)

data class DailyForecast(
    val dt: Long,
    val tempMin: Double,
    val tempMax: Double,
    val weatherDescription: String
)