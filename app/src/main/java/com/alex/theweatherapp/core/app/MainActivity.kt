package com.alex.theweatherapp.core.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.alex.theweatherapp.core.navigation.AppNavGraph
import com.alex.theweatherapp.core.utils.providers.ResourceProvider
import com.alex.theweatherapp.ui.theme.TheWeatherAppTheme
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    @Inject
    lateinit var resourceProvider: ResourceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheWeatherAppTheme {
                val navController = rememberNavController()
                AppNavGraph(navController)
            }
        }
    }
}
