package com.alex.theweatherapp.features.home.presentation.state

import com.alex.theweatherapp.features.home.domain.model.Forecast

sealed class HomeState {
    data object Loading : HomeState()
    data class Success(val results: Forecast) : HomeState()
    data class Error(val message: String) : HomeState()
    data object NoLocation : HomeState()
}