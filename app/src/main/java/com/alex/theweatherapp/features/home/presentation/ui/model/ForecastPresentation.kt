package com.alex.theweatherapp.features.home.presentation.ui.model

data class DailyForecastPresentation(
    val formattedDate: String,
    val tempMin: Double,
    val tempMax: Double,
    val weatherDescription: String
)

data class ForecastPresentation(
    val city: String,
    val dailyForecasts: List<DailyForecastPresentation>
)