package com.alex.theweatherapp.features.home.presentation.viewmodel

import android.location.Location
import com.alex.theweatherapp.R
import com.alex.theweatherapp.core.app.BaseViewModel
import com.alex.theweatherapp.core.coroutine.DefaultDispatcher
import com.alex.theweatherapp.core.coroutine.IoDispatcher
import com.alex.theweatherapp.core.coroutine.MainDispatcher
import com.alex.theweatherapp.core.location.LocationObserver
import com.alex.theweatherapp.core.location.LocationState
import com.alex.theweatherapp.core.network.NetworkObserver
import com.alex.theweatherapp.core.utils.StateResource
import com.alex.theweatherapp.core.utils.providers.ResourceProvider
import com.alex.theweatherapp.features.home.data.local.data_store.HomeDataStore
import com.alex.theweatherapp.features.home.domain.usecase.ForecastUseCase
import com.alex.theweatherapp.features.home.domain.usecase.GetCityNameUseCase
import com.alex.theweatherapp.features.home.presentation.event.HomeEvent
import com.alex.theweatherapp.features.home.presentation.event.HomeEvent.FetchContent
import com.alex.theweatherapp.features.home.presentation.event.HomeEvent.ForceRefresh
import com.alex.theweatherapp.features.home.presentation.event.HomeEvent.HandleNoLocation
import com.alex.theweatherapp.features.home.presentation.event.HomeEvent.Retry
import com.alex.theweatherapp.features.home.presentation.state.HomeState
import com.alex.theweatherapp.features.home.presentation.state.HomeState.Error
import com.alex.theweatherapp.features.home.presentation.state.HomeState.Loading
import com.alex.theweatherapp.features.home.presentation.state.HomeState.NoLocation
import com.alex.theweatherapp.features.home.presentation.state.HomeState.Success
import com.alex.theweatherapp.features.home.presentation.ui.mappers.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val forecastUseCase: ForecastUseCase,
    private val getCityNameUseCase: GetCityNameUseCase,
    private val locationObserver: LocationObserver,
    private val networkObserver: NetworkObserver,
    private val homeDataStore: HomeDataStore,
    resourceProvider: ResourceProvider,
    @MainDispatcher mainDispatcher: CoroutineDispatcher,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) : BaseViewModel(
    networkObserver,
    resourceProvider,
    mainDispatcher,
    ioDispatcher,
    defaultDispatcher
) {
    private val _state = MutableStateFlow<HomeState>(Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Initial)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _cityName = MutableStateFlow<String?>(null)
    val cityName: StateFlow<String?> = _cityName.asStateFlow()

    private var lastKnownLocation: Location? = null

    init {
        launchMainSafe {
            locationObserver.observeLocationUpdates().collect { locState ->
                _locationState.value = locState
                when (locState) {
                    is LocationState.Available -> {
                        lastKnownLocation = locState.location
                        launchIoSafe {
                            homeDataStore.saveLastLocation(location = locState.location)
                        }
                        launchIoSafe {
                            val city = getCityNameUseCase(location = locState.location)
                            launchMainSafe { _cityName.value = city }
                        }
                        onEvent(event = FetchContent)
                    }

                    is LocationState.Unavailable -> {
                        if (lastKnownLocation != null) {
                            onEvent(event = FetchContent)
                        } else {
                            onEvent(event = HandleNoLocation)
                        }
                    }

                    LocationState.Initial -> { /* Do nothing */
                    }
                }
            }
        }

        launchMainSafe {
            networkObserver.observe().collect { status ->
                _networkStatus.value = status
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HandleNoLocation -> handleNoLocation()
            is FetchContent,
            is Retry -> {
                lastKnownLocation?.let {
                    fetchDataForLocation(newLocation = it)
                } ?: handleNoLocation()
            }

            is ForceRefresh -> {
                lastKnownLocation?.let {
                    fetchDataForLocation(
                        newLocation = it,
                        forceRefresh = true
                    )
                } ?: handleNoLocation()
            }
        }
    }

    private fun fetchDataForLocation(newLocation: Location, forceRefresh: Boolean = false) {
        launchIoSafe {
            if (forceRefresh || _state.value !is Success) {
                launchMainSafe { _state.value = Loading }
            }
            try {
                val result = forecastUseCase(newLocation, _networkStatus.value, forceRefresh)
                launchMainSafe {
                    when (result) {
                        is StateResource.Success -> {
                            val presentation = result.data.toPresentation()
                            _state.value = Success(presentation)
                        }

                        is StateResource.Error -> _state.value = Error(result.message)
                    }
                }
            } catch (e: Exception) {
                launchMainSafe {
                    _state.value = Error(
                        e.localizedMessage
                            ?: resourceProvider.getString(R.string.generic_unknown_error)
                    )
                }
            }
        }
    }

    private fun handleNoLocation() {
        launchMainSafe {
            if (lastKnownLocation == null) {
                val savedLocation = homeDataStore.getLastLocation().firstOrNull()
                if (savedLocation != null) {
                    lastKnownLocation = savedLocation
                    fetchDataForLocation(newLocation = savedLocation)
                } else {
                    _state.value = NoLocation
                }
            } else {
                _state.value = NoLocation
            }
        }
    }
}
