package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.BuildConfig

object ApiConstants {
    const val BASE_URL = "https://wjtzkgpxmxtzcczzbvrz.supabase.co/functions/v1/"
    val CLIENT_ID: String = BuildConfig.API_CLIENT_ID
    val CLIENT_SECRET: String = BuildConfig.API_CLIENT_SECRET
}
