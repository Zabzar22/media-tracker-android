package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Review
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApiService {
    // GET /reviews?mediaId={id} — reviews for the detail screen. It pages like /media
    // does, but the server's default of 20 is plenty for one screen so I'm not paging yet.
    //
    // userId is optional on the server. Pass null for "everyone's reviews of this item"
    // (Retrofit leaves a null query parameter off the URL entirely), or a user id to ask
    // the narrower question the review form asks: "have I already reviewed this?"
    @GET("reviews")
    suspend fun getReviews(
        @Query("mediaId") mediaId: Int,
        @Query("userId") userId: String?
    ): Response<List<Review>>

    // POST /reviews — what the Write Review form calls. Comes back as a full Review with
    // the id and createdAt the server assigned, so we return Response<Review> rather than
    // Response<Unit>. A 409 here is a rule ("one review per item"), not a breakage, so the
    // repository reads the code itself instead of letting a non-2xx throw.
    @POST("reviews")
    suspend fun createReview(@Body body: CreateReviewRequest): Response<Review>

    // PUT /reviews/{id} — the same form saving an edit. {id} is the *review's* id, not the
    // mediaId; they're both ints sitting next to each other in the response, and sending
    // the wrong one edits somebody else's review or 404s.
    @PUT("reviews/{id}")
    suspend fun updateReview(
        @Path("id") reviewId: Int,
        @Body body: UpdateReviewRequest
    ): Response<Review>

    // DELETE /reviews/{id} — same id again. The server answers 204 with no body, so
    // there's nothing to parse back; Response<Unit> just carries the status code.
    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") reviewId: Int): Response<Unit>
}
