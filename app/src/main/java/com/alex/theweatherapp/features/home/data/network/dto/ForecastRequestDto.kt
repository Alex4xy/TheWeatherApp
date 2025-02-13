package com.alex.theweatherapp.features.home.data.network.dto

data class ForecastRequestDto(
    val lat: Double,
    val lon: Double,
    val cnt: Int = 7,
    val appid: String,
    val mode: String = "json",
    val units: String? = null,
    val lang: String? = null
) {
    fun toQueryMap(): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            put("lat", lat.toString())
            put("lon", lon.toString())
            put("cnt", cnt.toString())
            put("appid", appid)
            put("mode", mode)
            units?.let { put("units", it) }
            lang?.let { put("lang", it) }
        }
    }
}