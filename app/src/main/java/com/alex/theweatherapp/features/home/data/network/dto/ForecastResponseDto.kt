package com.alex.theweatherapp.features.home.data.network.dto

data class ForecastResponseDto(
    val city: City,
    val cod: String,
    val message: Double,
    val cnt: Int,
    val list: List<Forecast>
)

data class City(
    val id: Int,
    val name: String,
    val coord: Coord,
    val country: String,
    val population: Int,
    val timezone: Int
)

data class Coord(
    val lon: Double,
    val lat: Double
)

data class Forecast(
    val dt: Long,
    val sunrise: Long,
    val sunset: Long,
    val temp: Temp,
    val feels_like: FeelsLike,
    val pressure: Int,
    val humidity: Int,
    val weather: List<Weather>,
    val speed: Double,
    val deg: Int,
    val gust: Double?,
    val clouds: Int,
    val pop: Double,
    val rain: Double?
)

data class Temp(
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class FeelsLike(
    val day: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)
