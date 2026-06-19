package edu.metrostate.ics342.mediatracker.data

interface UserRepository {
    suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): RegisterResult

    suspend fun login(
        email: String,
        password: String
    ): LoginResult
}

sealed interface RegisterResult {
    data object Success : RegisterResult
    data object Conflict : RegisterResult
    data object NetworkError : RegisterResult
    data object UnknownError : RegisterResult
}

sealed interface LoginResult {
    data object Success : LoginResult
    data object InvalidCredentials : LoginResult
    data object NetworkError : LoginResult
    data object UnknownError : LoginResult
}
