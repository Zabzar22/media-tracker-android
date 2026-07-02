package edu.metrostate.ics342.mediatracker.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import edu.metrostate.ics342.mediatracker.ui.activity.ActivityFeedScreen
import edu.metrostate.ics342.mediatracker.ui.auth.LoginScreen
import edu.metrostate.ics342.mediatracker.ui.auth.RegisterScreen
import edu.metrostate.ics342.mediatracker.ui.connections.ConnectionsScreen
import edu.metrostate.ics342.mediatracker.ui.detail.MediaDetailScreen
import edu.metrostate.ics342.mediatracker.ui.library.LibraryScreen
import edu.metrostate.ics342.mediatracker.ui.profile.EditProfileScreen
import edu.metrostate.ics342.mediatracker.ui.profile.MyProfileScreen
import edu.metrostate.ics342.mediatracker.ui.profile.UserProfileScreen
import edu.metrostate.ics342.mediatracker.ui.review.WriteReviewScreen
import edu.metrostate.ics342.mediatracker.ui.search.SearchResultsScreen
import edu.metrostate.ics342.mediatracker.ui.search.SearchScreen
import edu.metrostate.ics342.mediatracker.ui.settings.SettingsScreen

private val bottomNavRoutes = setOf(
    Routes.ACTIVITY_FEED,
    Routes.SEARCH,
    Routes.SEARCH_RESULTS,
    Routes.LIBRARY,
    Routes.CONNECTIONS,
    Routes.MY_PROFILE,
)

@Composable
fun MediaTrackerNavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Routes.LOGIN,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess       = {
                        navController.navigate(Routes.ACTIVITY_FEED) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.ACTIVITY_FEED) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            composable(Routes.ACTIVITY_FEED) {
                ActivityFeedScreen(
                    onMediaClick   = { mediaId -> navController.navigate("media_detail/$mediaId") },
                    onUserClick    = { userId  -> navController.navigate("user_profile/$userId") },
                    onProfileClick = { navController.navigate(Routes.MY_PROFILE) }
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onSearch = { query ->
                        navController.navigate("search_results?query=${Uri.encode(query)}")
                    },
                    onMediaClick = { mediaId -> navController.navigate("media_detail/$mediaId") }
                )
            }

            composable(
                route = Routes.SEARCH_RESULTS,
                arguments = listOf(navArgument("query") {
                    type         = NavType.StringType
                    defaultValue = ""
                })
            ) { backStackEntry ->
                val query = backStackEntry.arguments?.getString("query") ?: ""
                SearchResultsScreen(
                    initialQuery = query,
                    onBack       = { navController.popBackStack() },
                    onMediaClick = { mediaId -> navController.navigate("media_detail/$mediaId") }
                )
            }

            composable(Routes.LIBRARY) {
                LibraryScreen(
                    onMediaClick = { mediaId -> navController.navigate("media_detail/$mediaId") }
                )
            }

            composable(route = Routes.MEDIA_DETAIL) {
                MediaDetailScreen(
                    mediaId        = -1,
                    onNavigateBack = { navController.popBackStack() },
                    onWriteReview  = { mediaId -> navController.navigate("write_review/$mediaId") }
                )
            }

            composable(
                route     = Routes.WRITE_REVIEW,
                arguments = listOf(navArgument("mediaId") { type = NavType.IntType })
            ) { backStackEntry ->
                val mediaId = backStackEntry.arguments?.getInt("mediaId") ?: return@composable
                WriteReviewScreen(
                    mediaId        = mediaId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.MY_PROFILE) {
                MyProfileScreen(
                    onEditProfile   = { navController.navigate(Routes.EDIT_PROFILE) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                )
            }

            composable(
                route     = Routes.USER_PROFILE,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                UserProfileScreen(
                    userId         = userId,
                    onNavigateBack = { navController.popBackStack() },
                    onMediaClick   = { mediaId -> navController.navigate("media_detail/$mediaId") }
                )
            }

            composable(Routes.EDIT_PROFILE) {
                EditProfileScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(Routes.CONNECTIONS) {
                ConnectionsScreen(
                    onUserClick = { userId -> navController.navigate("user_profile/$userId") }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onSignOut = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
