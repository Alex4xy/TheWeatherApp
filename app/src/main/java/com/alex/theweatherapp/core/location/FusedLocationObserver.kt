package com.alex.theweatherapp.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FusedLocationObserver @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationObserver {
    @Volatile
    private var lastLocationTimestamp: Long = 0

    @Volatile
    private var firstUpdateReceived = false

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationTimeoutMillis = 30000L
    private val initialWaitMillis = 3000L

    @SuppressLint("MissingPermission")
    override fun observeLocationUpdates(
        interval: Long,
        fastestInterval: Long
    ): Flow<LocationState> {
        val locationFlow = callbackFlow<LocationState> {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
                .setMinUpdateIntervalMillis(fastestInterval)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.locations.forEach { location ->
                        firstUpdateReceived = true
                        lastLocationTimestamp = System.currentTimeMillis()
                        trySend(LocationState.Available(location))
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper()
            )

            awaitClose {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }

        val checkerFlow = flow<LocationState> {
            delay(initialWaitMillis)
            if (!firstUpdateReceived) {
                emit(LocationState.Unavailable)
            }
            while (true) {
                delay(1000)
                if (firstUpdateReceived && (System.currentTimeMillis() - lastLocationTimestamp >= locationTimeoutMillis)) {
                    emit(LocationState.Unavailable)
                }
            }
        }

        return merge(locationFlow, checkerFlow)
            .distinctUntilChanged()
    }
}
