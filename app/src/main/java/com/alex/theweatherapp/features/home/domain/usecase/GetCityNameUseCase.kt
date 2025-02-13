package com.alex.theweatherapp.features.home.domain.usecase

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.alex.theweatherapp.R
import com.alex.theweatherapp.core.coroutine.IoDispatcher
import com.alex.theweatherapp.core.utils.providers.ResourceProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

class GetCityNameUseCase @Inject constructor(
    private val context: Context,
    private val resourceProvider: ResourceProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    open suspend operator fun invoke(location: Location): String = withContext(ioDispatcher) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    ) { addresses ->
                        cont.resume(value = addresses.firstOrNull()?.locality ?: resourceProvider.getString(R.string.unknown_city))
                    }
                }
            } else {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                addresses?.firstOrNull()?.locality
                    ?: resourceProvider.getString(R.string.unknown_city)
            }
        } catch (e: IOException) {
            resourceProvider.getString(R.string.unknown_city)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            resourceProvider.getString(R.string.unknown_city)
        }
    }
}