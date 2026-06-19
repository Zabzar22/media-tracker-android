package edu.metrostate.ics342.mediatracker.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApiService {
    @POST("users")
    suspend fun createUser(@Body body: RegisterRequest): Response<Unit>
}
