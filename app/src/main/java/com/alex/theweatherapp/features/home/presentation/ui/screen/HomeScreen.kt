package com.alex.theweatherapp.features.home.presentation.ui.screen

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.alex.theweatherapp.R
import com.alex.theweatherapp.core.location.LocationState
import com.alex.theweatherapp.core.utils.providers.ResourceProvider
import com.alex.theweatherapp.core.utils.ui.Dimensions.paddingExtraSmall
import com.alex.theweatherapp.core.utils.ui.Dimensions.paddingMedium
import com.alex.theweatherapp.core.utils.ui.Dimensions.paddingSmall
import com.alex.theweatherapp.features.home.presentation.event.HomeEvent.ForceRefresh
import com.alex.theweatherapp.features.home.presentation.event.HomeEvent.HandleNoLocation
import com.alex.theweatherapp.features.home.presentation.state.HomeState
import com.alex.theweatherapp.features.home.presentation.ui.model.DailyForecastPresentation
import com.alex.theweatherapp.features.home.presentation.viewmodel.HomeViewModel
import com.alex.theweatherapp.ui.theme.DarkBackground
import com.alex.theweatherapp.ui.theme.DeepBlue
import com.alex.theweatherapp.ui.theme.StormGray
import com.alex.theweatherapp.ui.theme.White
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = hiltViewModel()
    val resourceProvider = viewModel.resourceProvider

    val homeState by viewModel.state.collectAsState()
    val cityName by viewModel.cityName.collectAsState()
    val networkStatus = viewModel.networkStatus.collectAsState().value
    val locationState = viewModel.locationState.collectAsState().value

    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    var pendingForceRefresh by remember { mutableStateOf(value = false) }

    LaunchedEffect(key1 = true) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(key1 = locationPermissionState.status.isGranted) {
        if (!locationPermissionState.status.isGranted) {
            viewModel.onEvent(event = HandleNoLocation)
        } else {
            if (pendingForceRefresh) {
                pendingForceRefresh = false
                viewModel.onEvent(event = ForceRefresh)
            }
        }
    }

    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = viewModel.errorEvent) {
        viewModel.errorEvent.collect { errorMessage ->
            snackBarHostState.showSnackbar(errorMessage)
        }
    }

    val locationStatusText = when (locationState) {
        is LocationState.Available -> resourceProvider.getString(R.string.locations_status_available)
        is LocationState.Unavailable -> resourceProvider.getString(R.string.location_status_unavailable)
        LocationState.Initial -> resourceProvider.getString(R.string.locations_status_checking)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = resourceProvider.getString(R.string.title_home),
                        color = White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlue,
                    titleContentColor = White,
                    actionIconContentColor = White
                ),
                actions = {
                    IconButton(onClick = {
                        if (!locationPermissionState.status.isGranted) {
                            pendingForceRefresh = true
                            locationPermissionState.launchPermissionRequest()
                        } else {
                            viewModel.onEvent(event = ForceRefresh)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = resourceProvider.getString(R.string.content_description_refresh),
                            tint = White
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground),
            contentPadding = PaddingValues(paddingMedium)
        ) {
            item {
                Text(
                    text = resourceProvider.getString(R.string.status_network, networkStatus),
                    modifier = Modifier.padding(paddingSmall),
                    color = White
                )
            }
            item {
                Text(
                    text = resourceProvider.getString(R.string.status_location, locationStatusText),
                    modifier = Modifier.padding(paddingSmall),
                    color = White
                )
            }
            item {
                Text(
                    text = resourceProvider.getString(
                        R.string.status_city,
                        cityName ?: ""
                    ),
                    modifier = Modifier.padding(paddingSmall),
                    color = White
                )
            }
            when (homeState) {
                HomeState.Loading -> {
                    item {
                        HomeLoadingState(resourceProvider = resourceProvider)
                    }
                }
                is HomeState.Success -> {
                    val successState = homeState as HomeState.Success
                    items(items = successState.results.dailyForecasts) { forecast ->
                        ForecastCard(
                            forecast = forecast,
                            resourceProvider = resourceProvider
                        )
                    }
                }
                is HomeState.Error -> {
                    item {
                        HomeErrorState(
                            errorMessage = (homeState as HomeState.Error).message,
                            resourceProvider = resourceProvider
                        )
                    }
                }
                HomeState.NoLocation -> {
                    item {
                        HomeNoLocationState(resourceProvider = resourceProvider)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeLoadingState(
    resourceProvider: ResourceProvider,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingMedium),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = resourceProvider.getString(R.string.status_loading),
            color = White
        )
    }
}

@Composable
fun ForecastCard(
    forecast: DailyForecastPresentation,
    resourceProvider: ResourceProvider,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = paddingSmall),
        elevation = CardDefaults.cardElevation(defaultElevation = paddingExtraSmall),
        colors = CardDefaults.cardColors(containerColor = StormGray)
    ) {
        Column(modifier = Modifier.padding(paddingMedium)) {
            Text(
                text = resourceProvider.getString(
                    R.string.forecast_date,
                    forecast.formattedDate
                ),
                color = White
            )
            Text(
                text = resourceProvider.getString(
                    R.string.forecast_min_temp_c,
                    forecast.tempMin
                ),
                color = White
            )
            Text(
                text = resourceProvider.getString(
                    R.string.forecast_max_temp_c,
                    forecast.tempMax
                ),
                color = White
            )
            Text(
                text = resourceProvider.getString(
                    R.string.forecast_weather,
                    forecast.weatherDescription
                ) + " " + forecast.weatherDescription.toWeatherEmoji(),
                color = White
            )
        }
    }
}

@Composable
fun HomeErrorState(
    errorMessage: String,
    resourceProvider: ResourceProvider,
    modifier: Modifier = Modifier
) {
    Text(
        text = resourceProvider.getString(R.string.home_state_error, errorMessage),
        color = White,
        modifier = modifier.padding(paddingMedium)
    )
}

@Composable
fun HomeNoLocationState(
    resourceProvider: ResourceProvider,
    modifier: Modifier = Modifier
) {
    Text(
        text = resourceProvider.getString(R.string.home_state_no_location),
        color = White,
        modifier = modifier.padding(paddingMedium)
    )
}

fun String.toWeatherEmoji(): String {
    val description = this.lowercase().trim()
    return when {
        description == "clear sky" -> "\u2600" // ☀ Clear sky
        description == "few clouds" -> "\u26C5" // ⛅ Few clouds
        description == "scattered clouds" -> "\u2601" // ☁ Scattered clouds
        description == "broken clouds" || description == "overcast clouds" -> "\u2601" // ☁ Cloud
        description.contains("rain") || description.contains("drizzle") -> "\u2614" // ☔ Rain
        description.contains("thunderstorm") || description.contains("thunder") -> "\u26C8" // ⛈ Thunderstorm
        description.contains("snow") -> "\u2744" // ❄ Snow
        description.contains("mist") ||
                description.contains("smoke") ||
                description.contains("haze") ||
                description.contains("fog") ||
                description.contains("dust") ||
                description.contains("sand") ||
                description.contains("volcanic ash") -> "\uD83C\uDF2B" // 🌫 Fog

        else -> this
    }
}