package com.alex.theweatherapp

import com.alex.theweatherapp.features.home.data.local.room.dao.ForecastDao
import com.alex.theweatherapp.features.home.data.local.room.entity.ForecastEntity
import com.alex.theweatherapp.features.home.data.network.HomeApi
import com.alex.theweatherapp.features.home.data.network.dto.City
import com.alex.theweatherapp.features.home.data.network.dto.Coord
import com.alex.theweatherapp.features.home.data.network.dto.ForecastRequestDto
import com.alex.theweatherapp.features.home.data.network.dto.ForecastResponseDto
import com.alex.theweatherapp.features.home.domain.repository.HomeRepositoryImpl
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class HomeRepositoryImplTest {

    private lateinit var homeRepository: HomeRepositoryImpl
    private val homeApi: HomeApi = mock()
    private val forecastDao: ForecastDao = mock()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        homeRepository = HomeRepositoryImpl(
            homeApi = homeApi,
            forecastDao = forecastDao
        )
    }

    @Test
    fun `getCachedForecast calls forecastDao and returns forecasts`() = runTest(testDispatcher) {
        // Arrange
        val city = "Mountain View"
        val cachedForecasts = listOf(
            ForecastEntity(
                id = 1,
                city = city,
                date = 123L,
                tempMin = 10.0,
                tempMax = 20.0,
                weatherDescription = "Clear sky",
                fetchedAt = System.currentTimeMillis()
            )
        )
        whenever(forecastDao.getForecastsByCity(city)).thenReturn(cachedForecasts)

        // Act
        val result = homeRepository.getCachedForecast(city)

        // Assert
        verify(forecastDao).getForecastsByCity(city)
        assertEquals(cachedForecasts, result)
    }

    @Test
    fun `fetchForecastFromNetwork calls homeApi and returns forecast response`() =
        runTest(testDispatcher) {
            // Arrange
            val request = ForecastRequestDto(
                lat = 37.3861,
                lon = -122.0839,
                cnt = 7,
                appid = "test_app_id",
                mode = "json",
                units = "metric",
                lang = "en"
            )
            val response = ForecastResponseDto(
                city = City(
                    id = 1,
                    name = "Mountain View",
                    coord = Coord(37.3861, -122.0839),
                    country = "US",
                    population = 100000,
                    timezone = -25200
                ),
                cod = "200",
                message = 0.0,
                cnt = 7,
                list = emptyList()
            )
            whenever(homeApi.getForecast(request.toQueryMap())).thenReturn(response)

            // Act
            val result = homeRepository.fetchForecastFromNetwork(request)

            // Assert
            verify(homeApi).getForecast(request.toQueryMap())
            assertEquals(response, result)
        }

    @Test
    fun `saveForecast deletes old forecasts and inserts new ones`() = runTest(testDispatcher) {
        // Arrange
        val city = "Mountain View"
        val forecasts = listOf(
            ForecastEntity(
                id = 1,
                city = city,
                date = 123L,
                tempMin = 10.0,
                tempMax = 20.0,
                weatherDescription = "Clear sky",
                fetchedAt = System.currentTimeMillis()
            )
        )

        // Act
        homeRepository.saveForecast(city, forecasts)

        // Assert
        verify(forecastDao).deleteForecastsByCity(city)
        verify(forecastDao).insertForecasts(forecasts)
    }
}