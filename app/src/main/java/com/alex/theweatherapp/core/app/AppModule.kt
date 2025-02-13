package com.alex.theweatherapp.core.app

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.alex.theweatherapp.core.utils.providers.ResourceProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.homeDataStore: DataStore<Preferences> by preferencesDataStore(name = "home_data_store")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideResourceProvider(context: Context): ResourceProvider {
        return ResourceProvider(context)
    }

    @Provides
    @Singleton
    fun provideHomeDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.homeDataStore
    }

}