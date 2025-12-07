package com.vrsabu.markme.data.remote.api

import com.vrsabu.markme.data.remote.models.coffee
import retrofit2.http.GET

interface ApiService {

    @GET("coffee/hot")
    suspend fun getCoffeeList(): List<coffee>
}