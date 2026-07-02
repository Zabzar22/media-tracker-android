package edu.metrostate.ics342.mediatracker.navigation

import androidx.compose.material.icons.Icons
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.compose.material.icons.automirrored.filled.Feed
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Feed
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.res.stringResource

data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit,
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.ACTIVITY_FEED, edu.metrostate.ics342.mediatracker.R.string.nav_feed,
        selectedIcon   = { Icon(Icons.AutoMirrored.Filled.Feed,     stringResource(edu.metrostate.ics342.mediatracker.R.string.nav_feed)) },
        unselectedIcon = { Icon(Icons.AutoMirrored.Outlined.Feed,   stringResource(edu.metrostate.ics342.mediatracker.R.string.nav_feed)) }),
    BottomNavItem(Routes.SEARCH, edu.metrostate.ics342.mediatracker.R.string.nav_search,
        selectedIcon   = { Icon(Icons.Filled.Search,   stringResource(edu.metrostate.ics342.mediatracker.R.string.nav_search)) },
        unselectedIcon = { Icon(Icons.Outlined.Search, stringResource(edu.metrostate.ics342.mediatracker.R.string.nav_search)) }),
    BottomNavItem(Routes.LIBRARY, edu.metrostate.ics342.mediatracker.R.string.nav_library,
        selectedIcon   = { Icon(Icons.AutoMirrored.Filled.MenuBook,   stringResource(edu.metrostate.ics342.mediatracker.R.string.nav_library)) },
        unselectedIcon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, stringResource(edu.metrostate.ics342.mediatracker.R.string.nav_library)) }),
    BottomNavItem(Routes.CONNECTIONS, edu.metrostate.ics342.mediatracker.R.string.nav_people,
        selectedIcon   = { Icon(Icons.Filled.Group,    stringResource(edu.metrostate.ics342.mediatracker.R.string.nav_people)) },
        unselectedIcon = { Icon(Icons.Outlined.Group,  stringResource(edu.metrostate.ics342.mediatracker.R.string.nav_people)) }),
    BottomNavItem(Routes.MY_PROFILE, edu.metrostate.ics342.mediatracker.R.string.nav_profile,
        selectedIcon   = { Icon(Icons.Filled.Person,   stringResource(edu.metrostate.ics342.mediatracker.R.string.nav_profile)) },
        unselectedIcon = { Icon(Icons.Outlined.Person, stringResource(edu.metrostate.ics342.mediatracker.R.string.nav_profile)) }),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = isSelected,
                onClick  = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon  = { if (isSelected) item.selectedIcon() else item.unselectedIcon() },
                label = { Text(stringResource(item.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    selectedTextColor   = MaterialTheme.colorScheme.primary,
                    indicatorColor      = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
