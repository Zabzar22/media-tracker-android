package edu.metrostate.ics342.mediatracker.data

interface UserRepository {
    suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): RegisterResult
}

sealed interface RegisterResult {
    data object Success : RegisterResult
    data object Conflict : RegisterResult
    data object NetworkError : RegisterResult
    data object UnknownError : RegisterResult
}
