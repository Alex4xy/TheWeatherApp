package com.alex.theweatherapp.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.alex.theweatherapp.core.location.LocationState.Available
import com.alex.theweatherapp.core.location.LocationState.Initial
import com.alex.theweatherapp.core.location.LocationState.Unavailable
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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FusedLocationObserver @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationObserver {

    @Volatile
    private var lastLocationTimestamp: Long = 0L

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationTimeoutMillis = 15000L
    private val initialWaitMillis = 3000L

    @SuppressLint("MissingPermission")
    override fun observeLocationUpdates(
        interval: Long,
        fastestInterval: Long
    ): Flow<LocationState> {
        val locationFlow = callbackFlow {
            while (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                trySend(element = Unavailable)
                delay(500)
            }

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
                .setMinUpdateIntervalMillis(fastestInterval)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.locations.forEach { location ->
                        lastLocationTimestamp = System.currentTimeMillis()
                        trySend(element = Available(location = location))
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())

            awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
        }

        val checkerFlow = flow {
            delay(initialWaitMillis)
            while (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                emit(value = Unavailable)
                delay(500)
            }
            if (lastLocationTimestamp == 0L) {
                val lastKnown = suspendCancellableCoroutine { cont ->
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            cont.resumeWith(Result.success(value = location))
                        }
                        .addOnFailureListener {
                            cont.resumeWith(Result.success(value = null))
                        }
                }
                if (lastKnown != null) {
                    lastLocationTimestamp = System.currentTimeMillis()
                    emit(Available(location = lastKnown))
                } else {
                    emit(value = Unavailable)
                }
            }
            while (true) {
                delay(1000)
                if (lastLocationTimestamp != 0L && (System.currentTimeMillis() - lastLocationTimestamp >= locationTimeoutMillis)
                ) {
                    emit(value = Unavailable)
                }
            }
        }

        return merge(locationFlow, checkerFlow)
            .onStart { emit(value = Initial) }
            .distinctUntilChanged()
    }
}


