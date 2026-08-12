package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Review
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReviewApiService {
    // GET /reviews?mediaId={id} — reviews for the detail screen. It pages like /media
    // does, but the server's default of 20 is plenty for one screen so I'm not paging yet.
    @GET("reviews")
    suspend fun getReviews(@Query("mediaId") mediaId: Int): Response<List<Review>>

    // POST /reviews — what the Write Review form calls. Comes back as a full Review with
    // the id and createdAt the server assigned, so we return Response<Review> rather than
    // Response<Unit>. A 409 here is a rule ("one review per item"), not a breakage, so the
    // repository reads the code itself instead of letting a non-2xx throw.
    @POST("reviews")
    suspend fun createReview(@Body body: CreateReviewRequest): Response<Review>
}
