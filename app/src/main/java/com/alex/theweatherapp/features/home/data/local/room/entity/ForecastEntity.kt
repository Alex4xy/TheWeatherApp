package com.alex.theweatherapp.features.home.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Forecast")
data class ForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val city: String,
    val date: Long,
    val tempMin: Double,
    val tempMax: Double,
    val weatherDescription: String,
    val fetchedAt: Long
)