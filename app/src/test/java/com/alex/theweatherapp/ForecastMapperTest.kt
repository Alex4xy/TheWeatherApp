package com.alex.theweatherapp

import com.alex.theweatherapp.features.home.data.local.room.entity.ForecastEntity
import com.alex.theweatherapp.features.home.data.network.dto.City
import com.alex.theweatherapp.features.home.data.network.dto.Coord
import com.alex.theweatherapp.features.home.data.network.dto.FeelsLike
import com.alex.theweatherapp.features.home.data.network.dto.Forecast
import com.alex.theweatherapp.features.home.data.network.dto.ForecastResponseDto
import com.alex.theweatherapp.features.home.data.network.dto.Temp
import com.alex.theweatherapp.features.home.data.network.dto.Weather
import com.alex.theweatherapp.features.home.domain.mapper.toEntities
import com.alex.theweatherapp.features.home.domain.mapper.toWeeklyDomainFromCache
import com.alex.theweatherapp.features.home.domain.mapper.toWeeklyDomainFromResponse
import com.alex.theweatherapp.features.home.domain.model.DailyForecast
import com.alex.theweatherapp.features.home.domain.model.ForecastDomain
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ForecastMapperTest {

    @Test
    fun `toWeeklyDomainFromResponse returns null when list is empty`() {
        // Arrange
        val responseDto = ForecastResponseDto(
            city = City(
                id = 1,
                name = "TestCity",
                coord = Coord(lon = -122.0839, lat = 37.3861),
                country = "US",
                population = 100000,
                timezone = -25200
            ),
            cod = "200",
            message = 0.0,
            cnt = 1,
            list = emptyList()
        )

        // Act
        val result = responseDto.toWeeklyDomainFromResponse()

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun `toWeeklyDomainFromResponse maps data correctly when list is not empty`() {
        // Arrange
        val responseDto = ForecastResponseDto(
            city = City(
                id = 1,
                name = "TestCity",
                coord = Coord(lon = -122.0839, lat = 37.3861),
                country = "US",
                population = 100000,
                timezone = -25200
            ),
            cod = "200",
            message = 0.0,
            cnt = 1,
            list = listOf(
                Forecast(
                    dt = 1234567890L,
                    sunrise = 1234500000L,
                    sunset = 1234600000L,
                    temp = Temp(
                        day = 15.0,
                        min = 10.0,
                        max = 20.0,
                        night = 12.0,
                        eve = 18.0,
                        morn = 14.0
                    ),
                    feels_like = FeelsLike(
                        day = 14.0,
                        night = 11.0,
                        eve = 17.0,
                        morn = 13.0
                    ),
                    pressure = 1013,
                    humidity = 60,
                    weather = listOf(
                        Weather(
                            id = 800,
                            main = "Clear",
                            description = "clear sky",
                            icon = "01d"
                        )
                    ),
                    speed = 5.0,
                    deg = 180,
                    gust = 7.0,
                    clouds = 10,
                    pop = 0.1,
                    rain = null
                )
            )
        )

        // Act
        val result = responseDto.toWeeklyDomainFromResponse()

        // Assert
        assertThat(result).isNotNull()
        assertThat(result?.city).isEqualTo("TestCity")
        assertThat(result?.dailyForecasts).hasSize(1)
        val dailyForecast = result?.dailyForecasts?.first()
        assertThat(dailyForecast?.dt).isEqualTo(1234567890L)
        assertThat(dailyForecast?.tempMin).isEqualTo(10.0)
        assertThat(dailyForecast?.tempMax).isEqualTo(20.0)
        assertThat(dailyForecast?.weatherDescription).isEqualTo("clear sky")
    }


    @Test
    fun `toEntities maps data correctly`() {
        // Arrange
        val forecastDomain = ForecastDomain(
            city = "TestCity",
            dailyForecasts = listOf(
                DailyForecast(
                    dt = 123L,
                    tempMin = 10.0,
                    tempMax = 20.0,
                    weatherDescription = "Clear sky"
                )
            )
        )

        // Act
        val result = forecastDomain.toEntities()

        // Assert
        assertThat(result).hasSize(1)
        val entity = result.first()
        assertThat(entity.city).isEqualTo("TestCity")
        assertThat(entity.date).isEqualTo(123L)
        assertThat(entity.tempMin).isEqualTo(10.0)
        assertThat(entity.tempMax).isEqualTo(20.0)
        assertThat(entity.weatherDescription).isEqualTo("Clear sky")
        assertThat(entity.fetchedAt).isGreaterThan(0L) // Ensure fetchedAt is set to current time
    }

    @Test
    fun `toEntities returns empty list when dailyForecasts is empty`() {
        // Arrange
        val forecastDomain = ForecastDomain(city = "TestCity", dailyForecasts = emptyList())

        // Act
        val result = forecastDomain.toEntities()

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `toWeeklyDomainFromCache returns null when list is empty`() {
        // Arrange
        val entities = emptyList<ForecastEntity>()

        // Act
        val result = entities.toWeeklyDomainFromCache()

        // Assert
        assertThat(result).isNull()
    }

    @Test
    fun `toWeeklyDomainFromCache maps data correctly`() {
        // Arrange
        val entities = listOf(
            ForecastEntity(
                city = "TestCity",
                date = 123L,
                tempMin = 10.0,
                tempMax = 20.0,
                weatherDescription = "Clear sky",
                fetchedAt = System.currentTimeMillis()
            )
        )

        // Act
        val result = entities.toWeeklyDomainFromCache()

        // Assert
        assertThat(result).isNotNull()
        assertThat(result?.city).isEqualTo("TestCity")
        assertThat(result?.dailyForecasts).hasSize(1)
        val dailyForecast = result?.dailyForecasts?.first()
        assertThat(dailyForecast?.dt).isEqualTo(123L)
        assertThat(dailyForecast?.tempMin).isEqualTo(10.0)
        assertThat(dailyForecast?.tempMax).isEqualTo(20.0)
        assertThat(dailyForecast?.weatherDescription).isEqualTo("Clear sky")
    }

    @Test
    fun `toWeeklyDomainFromCache handles multiple entities`() {
        // Arrange
        val entities = listOf(
            ForecastEntity(
                city = "TestCity",
                date = 123L,
                tempMin = 10.0,
                tempMax = 20.0,
                weatherDescription = "Clear sky",
                fetchedAt = System.currentTimeMillis()
            ),
            ForecastEntity(
                city = "TestCity",
                date = 124L,
                tempMin = 15.0,
                tempMax = 25.0,
                weatherDescription = "Rainy",
                fetchedAt = System.currentTimeMillis()
            )
        )

        // Act
        val result = entities.toWeeklyDomainFromCache()

        // Assert
        assertThat(result).isNotNull()
        assertThat(result?.city).isEqualTo("TestCity")
        assertThat(result?.dailyForecasts).hasSize(2)
        val firstForecast = result?.dailyForecasts?.first()
        assertThat(firstForecast?.dt).isEqualTo(123L)
        assertThat(firstForecast?.tempMin).isEqualTo(10.0)
        assertThat(firstForecast?.tempMax).isEqualTo(20.0)
        assertThat(firstForecast?.weatherDescription).isEqualTo("Clear sky")
    }
}
