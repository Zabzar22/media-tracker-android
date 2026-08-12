package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Review
import retrofit2.HttpException

class DefaultReviewRepository(
    private val api: ReviewApiService = RetrofitInstance.reviewApiService
) {
    // Reviews for one item. Newest-first is the server's doing - /reviews orders by
    // created_at descending - so we hand the list straight through instead of sorting it
    // again here. Nothing reviewed yet comes back as an empty list, not a 404.
    suspend fun getReviews(mediaId: Int): List<Review> {
        val response = api.getReviews(mediaId)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body() ?: emptyList()
    }

    // Post a review. Hands back the Review the server built, because that's the only place
    // the new id comes from and PUT /reviews/{id} and DELETE /reviews/{id} both need it.
    //
    // 409 gets its own exception rather than a generic HttpException. The server sends it
    // when you've already reviewed this item, which is the one-review-per-item rule working
    // as intended — same idea as the 404s the library and favorites checks swallow, except
    // here the screen does have something to say about it.
    suspend fun createReview(
        mediaId: Int,
        rating: Int,
        reviewText: String? = null,
        shareToFeed: Boolean = true
    ): Review {
        val response = api.createReview(
            CreateReviewRequest(
                mediaId     = mediaId,
                rating      = rating,
                reviewText  = reviewText,
                shareToFeed = shareToFeed
            )
        )
        if (response.code() == 409) {
            throw AlreadyReviewedException(
                response.errorMessage() ?: "You've already reviewed this item."
            )
        }
        if (!response.isSuccessful) throw HttpException(response)
        // A 2xx with no body would mean we can't hand back an id, so treat it as a failure
        // rather than inventing one.
        return response.body() ?: throw HttpException(response)
    }
}
