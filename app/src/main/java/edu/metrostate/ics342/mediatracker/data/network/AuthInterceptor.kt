package edu.metrostate.ics342.mediatracker.data.network

import okhttp3.Interceptor
import okhttp3.Response

// Attaches the access token (saved to TokenStore at login) to every request that
// goes through the authenticated OkHttp client, so endpoints like GET /media send
// `Authorization: Bearer <token>` without each call having to add it by hand.
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenStore.accessToken
        val request = chain.request().newBuilder()
            .apply { if (token != null) addHeader("Authorization", "Bearer $token") }
            .build()
        return chain.proceed(request)
    }
}
