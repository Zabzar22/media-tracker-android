package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Review
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ReviewApiService {
    // GET /reviews?mediaId={id} — reviews for the detail screen. It pages like /media
    // does, but the server's default of 20 is plenty for one screen so I'm not paging yet.
    @GET("reviews")
    suspend fun getReviews(@Query("mediaId") mediaId: Int): Response<List<Review>>
}
