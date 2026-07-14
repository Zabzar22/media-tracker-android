package edu.metrostate.ics342.mediatracker.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitInstance {

    // internal (was private) so repositories can reuse it to read error bodies —
    // Retrofit only parses successful ones for us.
    internal val json = Json { ignoreUnknownKeys = true }

    private fun loggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val converterFactory =
        json.asConverterFactory("application/json".toMediaType())

    // Unauthenticated client — used for register + login (POST /users, POST /tokens),
    // the only endpoints that don't need a bearer token.
    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConstants.BASE_URL)
        .client(OkHttpClient.Builder().addInterceptor(loggingInterceptor()).build())
        .addConverterFactory(converterFactory)
        .build()

    val userApiService: UserApiService = retrofit.create(UserApiService::class.java)

    // Authenticated client — AuthInterceptor attaches the token from TokenStore, so
    // every call it makes (e.g. GET /media) is authorized automatically.
    private val authedRetrofit = Retrofit.Builder()
        .baseUrl(ApiConstants.BASE_URL)
        .client(
            OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .addInterceptor(loggingInterceptor())
                .build()
        )
        .addConverterFactory(converterFactory)
        .build()

    val mediaApiService: MediaApiService = authedRetrofit.create(MediaApiService::class.java)

    // Library calls need the bearer token too, so they go through the authed client.
    val libraryApiService: LibraryApiService = authedRetrofit.create(LibraryApiService::class.java)

    // Same for reviews (GET /reviews is behind auth like everything except register/login).
    val reviewApiService: ReviewApiService = authedRetrofit.create(ReviewApiService::class.java)
}
