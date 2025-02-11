package com.alex.theweatherapp.core.repository

import com.alex.theweatherapp.features.home.data.repository.HomeRepository
import com.alex.theweatherapp.features.home.domain.repository.HomeRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindHomeRepository(
        impl: HomeRepositoryImpl
    ): HomeRepository
}