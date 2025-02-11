package com.alex.theweatherapp.core.location

import android.location.Location

sealed class LocationState {
    data class Available(val location: Location) : LocationState()
    data object Unavailable : LocationState()
}