package com.alex.theweatherapp.features.home.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.alex.theweatherapp.core.location.RequestLocationPermission
import com.alex.theweatherapp.features.home.presentation.state.HomeState
import com.alex.theweatherapp.features.home.presentation.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = hiltViewModel()
    val homeState by viewModel.state.collectAsState()
    val resourceProvider = viewModel.resourceProvider
    val networkStatus = viewModel.networkStatus.collectAsState().value

    RequestLocationPermission { hasLocationPermission ->
        if (!hasLocationPermission) {
            Toast.makeText(
                context,
                "Location permission not granted. Accurate data need's location to be enabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Weekly weather")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Display the current network status.
            Text(text = "Network Status: $networkStatus")
            when (homeState) {
                HomeState.Loading -> Text(text = "Loading content...")
                is HomeState.Success -> Text(text = "Content: ${(homeState as HomeState.Success)}")
                is HomeState.Error -> Text(text = "Error: ${(homeState as HomeState.Error).message}")
                HomeState.NoLocation -> Text(text = "No location available and no local data found.")
            }
        }

    }

}