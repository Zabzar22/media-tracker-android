package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Review
import retrofit2.HttpException

class DefaultReviewRepository(
    private val api: ReviewApiService = RetrofitInstance.reviewApiService
) {
    // Reviews for one item, newest first. Nothing to review yet = empty list, not a 404.
    suspend fun getReviews(mediaId: Int): List<Review> {
        val response = api.getReviews(mediaId)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body() ?: emptyList()
    }
}
