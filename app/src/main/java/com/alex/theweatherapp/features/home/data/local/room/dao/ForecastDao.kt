package com.alex.theweatherapp.features.home.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alex.theweatherapp.features.home.data.local.room.entity.ForecastEntity

@Dao
interface ForecastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecasts(forecasts: List<ForecastEntity>)

    @Query("SELECT * FROM Forecast WHERE city = :city ORDER BY fetchedAt DESC")
    suspend fun getForecastsByCity(city: String): List<ForecastEntity>

    @Query("DELETE FROM Forecast WHERE city = :city")
    suspend fun deleteForecastsByCity(city: String)
}