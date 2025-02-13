package com.alex.theweatherapp

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.alex.theweatherapp.core.utils.providers.ResourceProvider
import com.alex.theweatherapp.features.home.domain.usecase.GetCityNameUseCase
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

class GetCityNameUseCaseTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)
    private val context: Context = mock()
    private val resourceProvider: ResourceProvider = mock()

    private lateinit var getCityNameUseCase: GetCityNameUseCase

    @Before
    fun setUp() {
        getCityNameUseCase = GetCityNameUseCase(
            context = context,
            resourceProvider = resourceProvider,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `returns unknown city on IOException`() = runTest(testScheduler) {
        // Arrange
        val location: Location = mock {
            on { latitude } doReturn 10.0
            on { longitude } doReturn 20.0
        }

        val geocoder: Geocoder = mock()
        whenever(context.getSystemService(Geocoder::class.java)).thenReturn(geocoder)
        whenever(resourceProvider.getString(R.string.unknown_city)).thenReturn("Unknown City")

        val listenerCaptor = argumentCaptor<Geocoder.GeocodeListener>()

        doAnswer {
            listenerCaptor.firstValue.onError(IOException("Geocoding failed").toString())
            null
        }.whenever(geocoder).getFromLocation(eq(10.0), eq(20.0), eq(1), listenerCaptor.capture())

        // Act
        val result = getCityNameUseCase(location)

        // Assert
        assertThat(result).isEqualTo("Unknown City")
    }

}

