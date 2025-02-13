package com.alex.theweatherapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alex.theweatherapp.features.home.data.local.room.dao.ForecastDao
import com.alex.theweatherapp.features.home.data.local.room.entity.ForecastEntity

@Database(
    entities = [ForecastEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun forecastDao(): ForecastDao
}