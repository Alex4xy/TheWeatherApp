package com.alex.theweatherapp.features.home.domain.usecase

import android.location.Location
import com.alex.theweatherapp.R
import com.alex.theweatherapp.core.coroutine.IoDispatcher
import com.alex.theweatherapp.core.network.NetworkStatus
import com.alex.theweatherapp.core.utils.StateResource
import com.alex.theweatherapp.core.utils.providers.ResourceProvider
import com.alex.theweatherapp.features.home.data.network.dto.ForecastRequestDto
import com.alex.theweatherapp.features.home.data.repository.HomeRepository
import com.alex.theweatherapp.features.home.domain.mapper.toEntities
import com.alex.theweatherapp.features.home.domain.mapper.toWeeklyDomainFromCache
import com.alex.theweatherapp.features.home.domain.mapper.toWeeklyDomainFromResponse
import com.alex.theweatherapp.features.home.domain.model.ForecastDomain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class ForecastUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
    private val getCityNameUseCase: GetCityNameUseCase,
    private val resourceProvider: ResourceProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val dataThreshold = 15 * 60 * 1000L

    suspend operator fun invoke(
        newLocation: Location,
        networkStatus: NetworkStatus,
        forceRefresh: Boolean = false
    ): StateResource<ForecastDomain> = withContext(ioDispatcher) {
        val city = getCityNameUseCase(location = newLocation)
        val forecastRequest = ForecastRequestDto(
            lat = newLocation.latitude,
            lon = newLocation.longitude,
            cnt = 7,
            appid = "", //ADD Open weather key here
            mode = "json",
            units = "metric",
            lang = "en"
        )

        val cachedEntities = homeRepository.getCachedForecast(city = city)

        if (!forceRefresh && cachedEntities.isNotEmpty()) {
            val age = System.currentTimeMillis() - cachedEntities.first().fetchedAt
            if (age < dataThreshold) {
                cachedEntities.toWeeklyDomainFromCache()?.let {
                    return@withContext StateResource.Success(data = it)
                }
            }
        }

        if (networkStatus == NetworkStatus.Available) {
            try {
                val response = homeRepository.fetchForecastFromNetwork(request = forecastRequest)
                val weeklyDomain = response.toWeeklyDomainFromResponse()
                if (weeklyDomain != null) {
                    homeRepository.saveForecast(city = city, entities = weeklyDomain.toEntities())
                    return@withContext StateResource.Success(weeklyDomain)
                } else {
                    return@withContext StateResource.Error(message = resourceProvider.getString(R.string.error_no_forecast_data))
                }
            } catch (e: retrofit2.HttpException) {
                return@withContext StateResource.Error(
                    message = resourceProvider.getString(R.string.error_network, e.message()),
                    throwable = e
                )
            } catch (e: IOException) {
                return@withContext StateResource.Error(
                    message = resourceProvider.getString(R.string.error_io, e.localizedMessage ?: e.stackTraceToString()),
                    throwable = e
                )
            } catch (e: Exception) {
                return@withContext StateResource.Error(
                    message = resourceProvider.getString(R.string.error_unexpected, e.localizedMessage ?: e.stackTraceToString()),
                    throwable = e
                )
            }
        } else {
            cachedEntities.toWeeklyDomainFromCache()?.let {
                return@withContext StateResource.Success(data = it)
            }
            return@withContext StateResource.Error(message = resourceProvider.getString(R.string.error_network_and_no_cache))
        }
    }
}
