@file:OptIn(ExperimentalCoroutinesApi::class)

package com.alex.theweatherapp

import android.content.Context
import android.location.Location
import androidx.annotation.StringRes
import com.alex.theweatherapp.core.network.NetworkStatus
import com.alex.theweatherapp.core.utils.StateResource
import com.alex.theweatherapp.core.utils.providers.ResourceProvider
import com.alex.theweatherapp.features.home.data.local.room.entity.ForecastEntity
import com.alex.theweatherapp.features.home.data.network.dto.City
import com.alex.theweatherapp.features.home.data.network.dto.Coord
import com.alex.theweatherapp.features.home.data.network.dto.FeelsLike
import com.alex.theweatherapp.features.home.data.network.dto.Forecast
import com.alex.theweatherapp.features.home.data.network.dto.ForecastResponseDto
import com.alex.theweatherapp.features.home.data.network.dto.Temp
import com.alex.theweatherapp.features.home.data.network.dto.Weather
import com.alex.theweatherapp.features.home.data.repository.HomeRepository
import com.alex.theweatherapp.features.home.domain.usecase.ForecastUseCase
import com.alex.theweatherapp.features.home.domain.usecase.GetCityNameUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FakeResourceProvider(context: Context) : ResourceProvider(context) {
    override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return "dummy string"
    }
}

class ForecastUseCaseTest {
    private val homeRepository: HomeRepository = mock()
    private val getCityNameUseCase: GetCityNameUseCase = mock()

    private val fakeContext: Context = mock {
        on { getString(any(), anyVararg()) } doReturn "dummy string"
    }

    private val resourceProvider: ResourceProvider = FakeResourceProvider(fakeContext)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var forecastUseCase: ForecastUseCase

    @Before
    fun setUp() {
        forecastUseCase = ForecastUseCase(
            homeRepository = homeRepository,
            getCityNameUseCase = getCityNameUseCase,
            resourceProvider = resourceProvider,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `returns cached forecast when cache is fresh`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        forecastUseCase = ForecastUseCase(
            homeRepository = homeRepository,
            getCityNameUseCase = getCityNameUseCase,
            resourceProvider = resourceProvider,
            ioDispatcher = testDispatcher
        )

        val location: Location = mock {
            on { latitude } doReturn 10.0
            on { longitude } doReturn 20.0
        }
        whenever(getCityNameUseCase.invoke(location)).thenReturn("TestCity")

        val now = System.currentTimeMillis()
        val freshEntity = ForecastEntity(
            id = 1,
            city = "TestCity",
            date = now,
            tempMin = 10.0,
            tempMax = 20.0,
            weatherDescription = "Clear sky",
            fetchedAt = now - (5 * 60 * 1000L) // 5 minutes old
        )
        whenever(homeRepository.getCachedForecast("TestCity")).thenReturn(listOf(freshEntity))

        // Act
        val result = forecastUseCase(
            newLocation = location,
            networkStatus = NetworkStatus.Available,
            forceRefresh = false
        )

        // Assert
        assertThat(result).isInstanceOf(StateResource.Success::class.java)
        val domain = (result as StateResource.Success).data
        assertThat(domain.city).isEqualTo("TestCity")
        assertThat(domain.dailyForecasts.first().weatherDescription).isEqualTo("Clear sky")
    }

    @Test
    fun `fetches forecast from network when cache is empty`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        forecastUseCase = ForecastUseCase(
            homeRepository = homeRepository,
            getCityNameUseCase = getCityNameUseCase,
            resourceProvider = resourceProvider,
            ioDispatcher = testDispatcher
        )

        val location: Location = mock {
            on { latitude } doReturn 10.0
            on { longitude } doReturn 20.0
        }
        whenever(getCityNameUseCase.invoke(location)).thenReturn("TestCity")
        whenever(homeRepository.getCachedForecast("TestCity")).thenReturn(emptyList())

        val dummyResponse = ForecastResponseDto(
            city = City(
                id = 1,
                name = "TestCity",
                coord = Coord(lon = 10.0, lat = 20.0),
                country = "TC",
                population = 100000,
                timezone = 3600
            ),
            cod = "200",
            message = 0.0,
            cnt = 7,
            list = listOf(
                Forecast(
                    dt = 123L,
                    sunrise = 120L,
                    sunset = 300L,
                    temp = Temp(
                        day = 15.0,
                        min = 10.0,
                        max = 20.0,
                        night = 10.0,
                        eve = 15.0,
                        morn = 10.0
                    ),
                    feels_like = FeelsLike(day = 15.0, night = 10.0, eve = 15.0, morn = 10.0),
                    pressure = 1000,
                    humidity = 50,
                    weather = listOf(
                        Weather(
                            id = 1,
                            main = "Clear",
                            description = "network",
                            icon = "icon"
                        )
                    ),
                    speed = 5.0,
                    deg = 180,
                    gust = 5.0,
                    clouds = 10,
                    pop = 0.1,
                    rain = null
                )
            )
        )
        whenever(homeRepository.fetchForecastFromNetwork(any())).thenReturn(dummyResponse)

        // Act
        val result = forecastUseCase(
            newLocation = location,
            networkStatus = NetworkStatus.Available,
            forceRefresh = false
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertThat(result).isInstanceOf(StateResource.Success::class.java)
        val domain = (result as StateResource.Success).data
        assertThat(domain.city).isEqualTo("TestCity")
        assertThat(domain.dailyForecasts.first().weatherDescription).isEqualTo("network")
    }

    @Test
    fun `uses fresh cache when network is unavailable`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        forecastUseCase = ForecastUseCase(
            homeRepository = homeRepository,
            getCityNameUseCase = getCityNameUseCase,
            resourceProvider = resourceProvider,
            ioDispatcher = testDispatcher
        )

        val location: Location = mock {
            on { latitude } doReturn 10.0
            on { longitude } doReturn 20.0
        }
        whenever(getCityNameUseCase.invoke(location)).thenReturn("TestCity")

        val now = System.currentTimeMillis()
        val freshEntity = ForecastEntity(
            id = 1,
            city = "TestCity",
            date = now,
            tempMin = 10.0,
            tempMax = 20.0,
            weatherDescription = "Clear sky",
            fetchedAt = now - (5 * 60 * 1000L) // 5 minutes old
        )
        whenever(homeRepository.getCachedForecast("TestCity")).thenReturn(listOf(freshEntity))

        // Act
        val result = forecastUseCase(
            newLocation = location,
            networkStatus = NetworkStatus.Unavailable,
            forceRefresh = false
        )

        // Assert
        assertThat(result).isInstanceOf(StateResource.Success::class.java)
        val domain = (result as StateResource.Success).data
        assertThat(domain.city).isEqualTo("TestCity")
        assertThat(domain.dailyForecasts.first().weatherDescription).isEqualTo("Clear sky")
    }

    @Test
    fun `returns error when network is unavailable and cache is empty`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        forecastUseCase = ForecastUseCase(
            homeRepository = homeRepository,
            getCityNameUseCase = getCityNameUseCase,
            resourceProvider = resourceProvider,
            ioDispatcher = testDispatcher
        )

        val location: Location = mock {
            on { latitude } doReturn 10.0
            on { longitude } doReturn 20.0
        }
        whenever(getCityNameUseCase.invoke(location)).thenReturn("TestCity")
        whenever(homeRepository.getCachedForecast("TestCity")).thenReturn(emptyList())

        // Act
        val result = forecastUseCase(
            newLocation = location,
            networkStatus = NetworkStatus.Unavailable,
            forceRefresh = false
        )

        // Assert
        assertThat(result).isInstanceOf(StateResource.Error::class.java)
        val error = result as StateResource.Error
        assertThat(error.message).isEqualTo("dummy string")
    }

    @Test
    fun `force refresh ignores fresh cache and fetches from network`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        forecastUseCase = ForecastUseCase(
            homeRepository = homeRepository,
            getCityNameUseCase = getCityNameUseCase,
            resourceProvider = resourceProvider,
            ioDispatcher = testDispatcher
        )

        val location: Location = mock {
            on { latitude } doReturn 10.0
            on { longitude } doReturn 20.0
        }
        whenever(getCityNameUseCase.invoke(location)).thenReturn("TestCity")

        val now = System.currentTimeMillis()
        val freshEntity = ForecastEntity(
            id = 1,
            city = "TestCity",
            date = now,
            tempMin = 10.0,
            tempMax = 20.0,
            weatherDescription = "Clear sky",
            fetchedAt = now - (5 * 60 * 1000L) // 5 minutes old
        )
        whenever(homeRepository.getCachedForecast("TestCity")).thenReturn(listOf(freshEntity))

        val dummyResponse = ForecastResponseDto(
            city = City(
                id = 1,
                name = "TestCity",
                coord = Coord(lon = 10.0, lat = 20.0),
                country = "TC",
                population = 100000,
                timezone = 3600
            ),
            cod = "200",
            message = 0.0,
            cnt = 7,
            list = listOf(
                Forecast(
                    dt = 123L,
                    sunrise = 120L,
                    sunset = 300L,
                    temp = Temp(day = 15.0, min = 10.0, max = 20.0, night = 10.0, eve = 15.0, morn = 10.0),
                    feels_like = FeelsLike(day = 15.0, night = 10.0, eve = 15.0, morn = 10.0),
                    pressure = 1000,
                    humidity = 50,
                    weather = listOf(Weather(id = 1, main = "Clear", description = "network", icon = "icon")),
                    speed = 5.0,
                    deg = 180,
                    gust = 5.0,
                    clouds = 10,
                    pop = 0.1,
                    rain = null
                )
            )
        )
        whenever(homeRepository.fetchForecastFromNetwork(any())).thenReturn(dummyResponse)

        // Act
        val result = forecastUseCase(
            newLocation = location,
            networkStatus = NetworkStatus.Available,
            forceRefresh = true
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertThat(result).isInstanceOf(StateResource.Success::class.java)
        val domain = (result as StateResource.Success).data
        assertThat(domain.city).isEqualTo("TestCity")
        assertThat(domain.dailyForecasts.first().weatherDescription).isEqualTo("network")
    }
}