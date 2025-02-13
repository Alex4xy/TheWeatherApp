package com.alex.theweatherapp.features.home.presentation.event

sealed class HomeEvent {
    data object FetchContent : HomeEvent()
    data object Retry : HomeEvent()
    data object HandleNoLocation : HomeEvent()
    data object ForceRefresh : HomeEvent()
}