package com.alex.theweatherapp.core.location

import kotlinx.coroutines.flow.Flow

interface LocationObserver {
    fun observeLocationUpdates(
        interval: Long = DEFAULT_INTERVAL,
        fastestInterval: Long = FASTEST_INTERVAL
    ): Flow<LocationState>
}

const val DEFAULT_INTERVAL = 10_000L
const val FASTEST_INTERVAL = 5_000L