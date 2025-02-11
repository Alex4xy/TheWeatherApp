package com.alex.theweatherapp.features.home.domain.repository

import com.alex.theweatherapp.features.home.data.network.HomeApi
import com.alex.theweatherapp.features.home.data.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val homeApi: HomeApi
) : HomeRepository {

}
