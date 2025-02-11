package com.alex.theweatherapp.core.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.theweatherapp.core.coroutine.DefaultDispatcher
import com.alex.theweatherapp.core.coroutine.IoDispatcher
import com.alex.theweatherapp.core.coroutine.MainDispatcher
import com.alex.theweatherapp.core.network.NetworkObserver
import com.alex.theweatherapp.core.network.NetworkStatus
import com.alex.theweatherapp.core.utils.providers.ResourceProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

open class BaseViewModel @Inject constructor(
    private val networkObserver: NetworkObserver,
    val resourceProvider: ResourceProvider,
    @MainDispatcher protected val mainDispatcher: CoroutineDispatcher,
    @IoDispatcher protected val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher protected val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    protected val _networkStatus = MutableStateFlow(NetworkStatus.Unavailable)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus

    init {
        observeNetworkStatus()
    }

    private fun observeNetworkStatus() {
        // Launch a coroutine on the main dispatcher to observe network changes.
        viewModelScope.launch(mainDispatcher) {
            networkObserver.observe().collect { status ->
                _networkStatus.value = status
                println("Network status updated: $status")
            }
        }
    }

    /**
     * Launches a coroutine safely on the Main dispatcher.
     */
    protected fun launchMainSafe(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(mainDispatcher) {
            try {
                block()
            } catch (e: Exception) {
                println("Error on Main dispatcher: ${e.localizedMessage}")
                // Optionally update a shared error state here.
            }
        }
    }

    /**
     * Launches a coroutine safely on the IO dispatcher.
     */
    protected fun launchIoSafe(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            try {
                block()
            } catch (e: Exception) {
                println("Error on IO dispatcher: ${e.localizedMessage}")
                // Optionally update a shared error state here.
            }
        }
    }

    /**
     * Launches a coroutine safely on the Default dispatcher.
     */
    protected fun launchDefaultSafe(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(defaultDispatcher) {
            try {
                block()
            } catch (e: Exception) {
                println("Error on Default dispatcher: ${e.localizedMessage}")
                // Optionally update a shared error state here.
            }
        }
    }
}