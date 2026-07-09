package edu.metrostate.ics342.mediatracker.data

import edu.metrostate.ics342.mediatracker.data.model.*

/**
 * Hardcoded fake data used throughout the app while real API integration is built out.
 *
 * This object stays in the project all semester. If a student falls behind on API wiring,
 * their app still runs and shows real-looking content.
 */
object FakeMediaRepository {

    val currentUser = UserProfile(
        id           = "user-001",
        email        = "alex@example.com",
        username     = "alexreads",
        displayName  = "Alex Chen",
        bio          = "Avid reader and film buff. Always looking for the next great story.",
        avatarUrl    = null,
        followerCount  = 12,
        followingCount = 8,
        trackedCount   = 47,
        createdAt    = "2023-09-01T00:00:00Z"
    )

    val mediaList = listOf(
        Media(id = 1,  mediaType = "book",  title = "The Hitchhiker's Guide to the Galaxy",
            author = "Douglas Adams", publishedYear = 1979,
            averageRating = 4.7f, ratingCount = 312,
            genres = listOf("Science Fiction", "Comedy"), pageCount = 224,
            description = "Seconds before Earth is demolished for a hyperspace bypass, " +
                "Arthur Dent is swept off the planet by his friend Ford Prefect and " +
                "begins an absurd, galaxy-spanning misadventure."),
        Media(id = 2,  mediaType = "book",  title = "Project Hail Mary",
            author = "Andy Weir", publishedYear = 2021,
            averageRating = 4.9f, ratingCount = 478,
            genres = listOf("Science Fiction", "Adventure"), pageCount = 476,
            description = "A lone astronaut wakes with no memory aboard a spacecraft, " +
                "the sole survivor of a desperate mission to save humanity from extinction."),
        Media(id = 3,  mediaType = "book",  title = "The Name of the Wind",
            author = "Patrick Rothfuss", publishedYear = 2007,
            averageRating = 4.6f, ratingCount = 291,
            genres = listOf("Fantasy", "Adventure"), pageCount = 662,
            description = "Kvothe, a legendary magician turned humble innkeeper, recounts " +
                "the true story of his rise from orphaned street child to infamous hero."),
        Media(id = 4,  mediaType = "book",  title = "Dune",
            author = "Frank Herbert", publishedYear = 1965,
            averageRating = 4.8f, ratingCount = 521,
            genres = listOf("Science Fiction", "Epic"), pageCount = 412,
            description = "A noble family becomes embroiled in a war for control over the " +
                "most valuable substance in the universe on the desert planet Arrakis."),
        Media(id = 5,  mediaType = "movie", title = "Arrival",
            director = "Denis Villeneuve", publishedYear = 2016,
            averageRating = 4.5f, ratingCount = 263,
            genres = listOf("Science Fiction", "Drama"), runtimeMinutes = 116,
            description = "When mysterious spacecraft touch down across the globe, a " +
                "linguist is recruited to find a way to communicate with the visitors."),
        Media(id = 6,  mediaType = "movie", title = "Everything Everywhere All at Once",
            director = "Daniel Kwan, Daniel Scheinert", publishedYear = 2022,
            averageRating = 4.8f, ratingCount = 389,
            genres = listOf("Science Fiction", "Comedy", "Drama"), runtimeMinutes = 139,
            description = "An overwhelmed laundromat owner discovers she must connect with " +
                "parallel versions of herself to stop a threat spanning the multiverse."),
        Media(id = 7,  mediaType = "movie", title = "Interstellar",
            director = "Christopher Nolan", publishedYear = 2014,
            averageRating = 4.6f, ratingCount = 441,
            genres = listOf("Science Fiction", "Adventure"), runtimeMinutes = 169,
            description = "With Earth dying, a team of explorers travels through a wormhole " +
                "in search of a new home for humanity among the stars."),
        Media(id = 8,  mediaType = "show",  title = "Severance",
            creator = "Dan Erickson", network = "Apple TV+", publishedYear = 2022,
            averageRating = 4.9f, ratingCount = 317,
            genres = listOf("Thriller", "Science Fiction", "Drama"),
            seasonCount = 2, episodeCount = 19,
            description = "Employees at a mysterious corporation undergo a procedure that " +
                "surgically divides their work memories from their personal lives."),
        Media(id = 9,  mediaType = "show",  title = "The Bear",
            creator = "Christopher Storer", network = "FX on Hulu", publishedYear = 2022,
            averageRating = 4.8f, ratingCount = 298,
            genres = listOf("Drama", "Comedy"),
            seasonCount = 3, episodeCount = 28,
            description = "A young fine-dining chef returns home to run his family's chaotic " +
                "Chicago sandwich shop after a devastating loss."),
        Media(id = 10, mediaType = "show",  title = "Andor",
            creator = "Tony Gilroy", network = "Disney+", publishedYear = 2022,
            averageRating = 4.7f, ratingCount = 276,
            genres = listOf("Science Fiction", "Drama", "Action"),
            seasonCount = 2, episodeCount = 24,
            description = "In the years before the rebellion, a cynical thief is drawn into " +
                "the fight against the Galactic Empire and becomes a reluctant revolutionary."),
        // Lower-rated title so the star row visibly rounds down (3.4 -> 3 stars).
        Media(id = 11, mediaType = "book",  title = "The Casual Vacancy",
            author = "J.K. Rowling", publishedYear = 2012,
            averageRating = 3.4f, ratingCount = 158,
            genres = listOf("Literary Fiction", "Drama"), pageCount = 503,
            description = "When a parish council member dies suddenly, the seemingly idyllic " +
                "town of Pagford erupts into a bitter war over the empty seat he leaves behind."),
    )

    val libraryItems = listOf(
        // Finished
        LibraryItem("user-001", 1, LibraryStatus.FINISHED,
            "2024-01-10T10:00:00Z", "2024-01-15T10:00:00Z", mediaList[0]),
        LibraryItem("user-001", 4, LibraryStatus.FINISHED,
            "2024-01-12T10:00:00Z", "2024-01-19T10:00:00Z", mediaList[3]),
        LibraryItem("user-001", 7, LibraryStatus.FINISHED,
            "2024-01-14T10:00:00Z", "2024-01-21T10:00:00Z", mediaList[6]),
        LibraryItem("user-001", 6, LibraryStatus.FINISHED,
            "2024-01-16T10:00:00Z", "2024-01-23T10:00:00Z", mediaList[5]),
        // In progress
        LibraryItem("user-001", 5, LibraryStatus.IN_PROGRESS,
            "2024-01-18T10:00:00Z", "2024-01-18T10:00:00Z", mediaList[4]),
        LibraryItem("user-001", 3, LibraryStatus.IN_PROGRESS,
            "2024-01-19T10:00:00Z", "2024-01-22T10:00:00Z", mediaList[2]),
        LibraryItem("user-001", 9, LibraryStatus.IN_PROGRESS,
            "2024-01-21T10:00:00Z", "2024-01-24T10:00:00Z", mediaList[8]),
        // Want to
        LibraryItem("user-001", 8, LibraryStatus.WANT_TO,
            "2024-01-20T10:00:00Z", "2024-01-20T10:00:00Z", mediaList[7]),
        LibraryItem("user-001", 2, LibraryStatus.WANT_TO,
            "2024-01-22T10:00:00Z", "2024-01-22T10:00:00Z", mediaList[1]),
        LibraryItem("user-001", 10, LibraryStatus.WANT_TO,
            "2024-01-23T10:00:00Z", "2024-01-23T10:00:00Z", mediaList[9]),
        LibraryItem("user-001", 11, LibraryStatus.FINISHED,
            "2024-01-24T10:00:00Z", "2024-01-25T10:00:00Z", mediaList[10]),
    )

    private val userJordan = UserProfile("user-002", "j@example.com", "jsmith",   "Jordan Smith",  followerCount = 5,  followingCount = 10)
    private val userPriya  = UserProfile("user-003", "p@example.com", "priya_r", "Priya Patel",   followerCount = 23, followingCount = 15)
    private val userMarco  = UserProfile("user-004", "m@example.com", "mramos",  "Marco Ramos",   followerCount = 8,  followingCount = 4)
    private val userSarah  = UserProfile("user-005", "s@example.com", "sarahk",  "Sarah Kim",     followerCount = 31, followingCount = 22)

    val activityFeed = listOf(
        ActivityEvent(1, "user-002", "finished", 5, createdAt = "2024-01-22T14:30:00Z",
            timeAgo = "2 hours ago", user = userJordan, media = mediaList[4]),
        ActivityEvent(2, "user-003", "review",   8, rating = 5,
            reviewText = "Absolutely gripping from start to finish.",
            createdAt = "2024-01-22T11:15:00Z", timeAgo = "5 hours ago", user = userPriya, media = mediaList[7]),
        ActivityEvent(3, "user-004", "added",    10, createdAt = "2024-01-21T20:00:00Z",
            timeAgo = "8 hours ago", user = userMarco, media = mediaList[9]),
        ActivityEvent(4, "user-002", "started",  9, createdAt = "2024-01-21T18:45:00Z",
            timeAgo = "1 day ago", user = userJordan, media = mediaList[8]),
        ActivityEvent(5, "user-003", "review",   1, rating = 4,
            createdAt = "2024-01-20T09:00:00Z", timeAgo = "2 days ago", user = userPriya, media = mediaList[0]),
    )

    val followers = listOf(userJordan, userPriya)
    val following = listOf(userMarco, userSarah)
}
