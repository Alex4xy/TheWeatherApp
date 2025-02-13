package com.alex.theweatherapp.core.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.theweatherapp.R
import com.alex.theweatherapp.core.coroutine.DefaultDispatcher
import com.alex.theweatherapp.core.coroutine.IoDispatcher
import com.alex.theweatherapp.core.coroutine.MainDispatcher
import com.alex.theweatherapp.core.network.NetworkObserver
import com.alex.theweatherapp.core.network.NetworkStatus
import com.alex.theweatherapp.core.utils.providers.ResourceProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _errorEvent = MutableSharedFlow<String>(replay = 0)
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    protected fun launchMainSafe(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(mainDispatcher) {
            try {
                block()
            } catch (e: Exception) {
                val errorMessage = e.localizedMessage ?: resourceProvider.getString(R.string.generic_unknown_error)
                _errorEvent.emit(errorMessage)
            }
        }
    }

    protected fun launchIoSafe(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            try {
                block()
            } catch (e: Exception) {
                val errorMessage = e.localizedMessage ?: resourceProvider.getString(R.string.generic_unknown_error)
                _errorEvent.emit(errorMessage)
            }
        }
    }

    protected fun launchDefaultSafe(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(defaultDispatcher) {
            try {
                block()
            } catch (e: Exception) {
                val errorMessage = e.localizedMessage ?: resourceProvider.getString(R.string.generic_unknown_error)
                _errorEvent.emit(errorMessage)
            }
        }
    }
}