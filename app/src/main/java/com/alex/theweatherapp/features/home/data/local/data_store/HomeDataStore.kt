package com.alex.theweatherapp.features.home.data.local.data_store

import android.content.Context
import android.location.Location
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import com.alex.theweatherapp.core.app.homeDataStore
import com.alex.theweatherapp.core.coroutine.DefaultDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {
    companion object {
        private val LAST_LATITUDE = floatPreferencesKey("last_latitude")
        private val LAST_LONGITUDE = floatPreferencesKey("last_longitude")
    }

    fun getLastLocation(): Flow<Location?> {
        return context.homeDataStore.data.map { preferences ->
            val lat = preferences[LAST_LATITUDE]
            val lon = preferences[LAST_LONGITUDE]
            if (lat != null && lon != null) {
                Location("data_store").apply {
                    latitude = lat.toDouble()
                    longitude = lon.toDouble()
                }
            } else {
                null
            }
        }
    }

    suspend fun saveLastLocation(location: Location) {
        withContext(dispatcher) {
            context.homeDataStore.edit { preferences ->
                preferences[LAST_LATITUDE] = location.latitude.toFloat()
                preferences[LAST_LONGITUDE] = location.longitude.toFloat()
            }
        }
    }
}
