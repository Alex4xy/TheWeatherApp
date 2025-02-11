package com.alex.theweatherapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alex.theweatherapp.features.home.data.local.dao.ForecastDao
import com.alex.theweatherapp.features.home.data.local.entity.ForecastEntity

@Database(entities = [ForecastEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun forecastDao(): ForecastDao
}