package com.alex.theweatherapp.features.home.presentation.viewmodel

import android.location.Location
import androidx.lifecycle.viewModelScope
import com.alex.theweatherapp.core.app.BaseViewModel
import com.alex.theweatherapp.core.coroutine.DefaultDispatcher
import com.alex.theweatherapp.core.coroutine.IoDispatcher
import com.alex.theweatherapp.core.coroutine.MainDispatcher
import com.alex.theweatherapp.core.location.LocationObserver
import com.alex.theweatherapp.core.location.LocationState
import com.alex.theweatherapp.core.network.NetworkObserver
import com.alex.theweatherapp.core.utils.providers.ResourceProvider
import com.alex.theweatherapp.features.home.domain.usecase.HomeUseCase
import com.alex.theweatherapp.features.home.presentation.event.HomeEvent
import com.alex.theweatherapp.features.home.presentation.state.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeUseCase: HomeUseCase,
    resourceProvider: ResourceProvider,
    private val locationObserver: LocationObserver,
    networkObserver: NetworkObserver,
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

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Unavailable)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    init {
        // Automatically listen for location updates and trigger actions based on the new state.
        viewModelScope.launch {
            locationObserver.observeLocationUpdates().collect { locState ->
                _locationState.value = locState
                when (locState) {
                    is LocationState.Available -> {
                        println("View Model location available")
                        // Immediately fetch data based on the available location.
                        fetchDataForLocation(locState.location)
                    }

                    is LocationState.Unavailable -> {
                        println("Show message that location is not available")
                        println("View Model location unavailable")
                        // Immediately handle no location scenario.
                        handleNoLocation()
                    }
                }
            }
        }

        viewModelScope.launch {
            networkObserver.observe().collect { status ->
                _networkStatus.value = status
                println("Network status: $status")
            }
        }
    }


    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.HandleNoLocation -> handleNoLocation()
            is HomeEvent.FetchContent -> fetchContent()
            is HomeEvent.Retry -> fetchContent()
        }
    }


    private fun fetchDataForLocation(newLocation: Location) {
        // Use reverse geocoding (or any method) to extract the city or region from newLocation.
        /*   val newCity = getCityFromLocation(newLocation)

           // Check if we already have data for this city and it's fresh.
           if (lastFetchedCity != null &&
               lastFetchedCity == newCity &&
               (System.currentTimeMillis() - lastFetchTimestamp < DATA_VALIDITY_THRESHOLD)
           ) {
               // Cached data is valid, so just return without updating the UI.
               // The UI should already be displaying the cached data.
               return
           }
   */
        // Otherwise, fetch new data.
        /*     viewModelScope.launch {
                 _state.value = HomeState.Loading
                 try {
                     // Replace with your actual network call.
                     val data = homeUseCase.fetchDataFromNetwork(newLocation)
                     // Update the cached values.
                     lastFetchedCity = newCity
                     lastFetchTimestamp = System.currentTimeMillis()
                     // Optionally, store the data locally.
                     repository.saveData(data)
                     _homeState.value = HomeState.Success(data)
                 } catch (e: Exception) {
                     _homeState.value = HomeState.Error(e.message)
                 }
             }*/
    }

    private fun handleNoLocation() {
        viewModelScope.launch {
            // Attempt to load the last-known data from the database.
            /*    val lastData = repository.getLastKnownData()
                _state.value = if (lastData != null) {
                    HomeState.Success(lastData)
                } else {
                    HomeState.NoLocation
                }*/
        }
    }

    private fun fetchContent() {
        viewModelScope.launch {
            _state.value = HomeState.Loading
            // Additional content fetching logic can go here.
        }
    }
}
