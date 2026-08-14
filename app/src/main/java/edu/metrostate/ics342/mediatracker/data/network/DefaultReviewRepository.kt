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
        val response = api.getReviews(mediaId, null)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body() ?: emptyList()
    }

    // "Have I already reviewed this?" — the question the review form opens with. Null means
    // no, and the form starts blank; a Review means yes, and the form fills itself in and
    // saves with PUT instead of POST.
    //
    // The server does the filtering (?mediaId=&userId=) rather than us pulling all 20
    // reviews back and picking ours out of them. One review per person per item is a rule
    // the server enforces, so at most one row can come back.
    suspend fun getMyReview(mediaId: Int, userId: String): Review? {
        val response = api.getReviews(mediaId, userId)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body()?.firstOrNull()
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

    // Save an edit. reviewId is the Review's own id, not the mediaId - see the note in
    // ReviewApiService. Returns the updated Review so a caller can keep showing fresh data
    // without a second GET.
    //
    // reviewText is nullable on purpose: passing null clears the text off a review that
    // used to have some, which is what emptying the box on the form should do.
    suspend fun updateReview(reviewId: Int, rating: Int, reviewText: String?): Review {
        val response = api.updateReview(reviewId, UpdateReviewRequest(rating, reviewText))
        if (!response.isSuccessful) throw HttpException(response)
        return response.body() ?: throw HttpException(response)
    }

    // Delete a review. Nothing comes back (204), so nothing is returned.
    //
    // A 404 means it's already gone - deleted on another device, or a double tap that got
    // two requests out. Either way the end state is the one we wanted, so we let it through
    // instead of showing an error for work that's already done.
    suspend fun deleteReview(reviewId: Int) {
        val response = api.deleteReview(reviewId)
        if (!response.isSuccessful && response.code() != 404) {
            throw HttpException(response)
        }
    }
}
