package com.alex.theweatherapp.features.home.presentation.state

import com.alex.theweatherapp.features.home.presentation.ui.model.ForecastPresentation

sealed class HomeState {
    data object Loading : HomeState()
    data class Success(val results: ForecastPresentation) : HomeState()
    data class Error(val message: String) : HomeState()
    data object NoLocation : HomeState()
}