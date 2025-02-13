package com.alex.theweatherapp.features.home.presentation.ui.mappers

import com.alex.theweatherapp.features.home.domain.model.DailyForecast
import com.alex.theweatherapp.features.home.domain.model.ForecastDomain
import com.alex.theweatherapp.features.home.presentation.ui.model.DailyForecastPresentation
import com.alex.theweatherapp.features.home.presentation.ui.model.ForecastPresentation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun DailyForecast.toPresentation(): DailyForecastPresentation {
    val date = Date(this.dt * 1000)
    val dateFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(date)

    return DailyForecastPresentation(
        formattedDate = formattedDate,
        tempMin = this.tempMin,
        tempMax = this.tempMax,
        weatherDescription = this.weatherDescription
    )
}

fun ForecastDomain.toPresentation(): ForecastPresentation {
    return ForecastPresentation(
        city = this.city,
        dailyForecasts = this.dailyForecasts.map { it.toPresentation() }
    )
}