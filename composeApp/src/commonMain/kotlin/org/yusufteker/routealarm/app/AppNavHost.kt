package org.yusufteker.routealarm.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.feature.alarm.presentation.SharedAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.AddAlarmAction
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.AddAlarmScreenRoot
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.AddAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addstops.StopPickerScreenRoot
import org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail.AlarmDetailScreenRoot
import org.yusufteker.routealarm.feature.alarm.presentation.home.HomeScreenRoot
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.BottomNavigationBar
import org.yusufteker.routealarm.feature.onboarding.presentation.welcome.WelcomeScreen
import org.yusufteker.routealarm.settings.SettingsManager
import org.yusufteker.routealarm.settings.SettingsScreenRoot

@Composable
fun AppNavHost(
    navController: NavHostController,
    settingsManager: SettingsManager = koinInject()
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val scope = rememberCoroutineScope()

    val systemBars = WindowInsets.systemBars
    val bottomPadding = with(LocalDensity.current) { systemBars.getBottom(this).toDp() }

    val showBottomBar = currentDestination?.let { dest ->
        dest.hasRoute<Routes.HomeScreen>() ||
            dest.hasRoute<Routes.AddAlarmScreen>() ||
            dest.hasRoute<Routes.SettingsScreen>()
    } ?: false

    val onboardingDone by produceState<Boolean?>(initialValue = null) {
        value = settingsManager.isOnboardingCompleted()
    }

    if (onboardingDone == null) {
        Box(
            Modifier.fillMaxSize().background(AppColors.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppColors.accent)
        }
        return
    }

    val startDestination =
        if (onboardingDone == true) Routes.MainGraph else Routes.OnboardingGraph

    Scaffold(
        containerColor = AppColors.background,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    modifier = Modifier.padding(bottom = bottomPadding),
                    currentDestination = currentDestination,
                    onItemSelected = { route ->
                        val isAlreadyHere = when (route) {
                            Routes.HomeScreen -> currentDestination?.hasRoute<Routes.HomeScreen>() == true
                            Routes.AddAlarmScreen -> currentDestination?.hasRoute<Routes.AddAlarmScreen>() == true
                            Routes.SettingsScreen -> currentDestination?.hasRoute<Routes.SettingsScreen>() == true
                            else -> false
                        }
                        if (!isAlreadyHere) {
                            navController.navigate(route) {
                                popUpTo<Routes.HomeScreen> {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = startDestination) {

            navigation<Routes.OnboardingGraph>(
                startDestination = Routes.WelcomeScreen
            ) {
                composable<Routes.WelcomeScreen> {
                    WelcomeScreen(
                        onContinue = {
                            scope.launch {
                                settingsManager.setOnboardingCompleted(true)
                                navController.navigate(Routes.MainGraph) {
                                    popUpTo<Routes.OnboardingGraph> { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }

            navigation<Routes.MainGraph>(
                startDestination = Routes.HomeScreen
            ) {
                composable<Routes.HomeScreen> {
                    HomeScreenRoot(
                        contentPadding = innerPadding,
                        onNavigateToAlarmDetail = { alarmId ->
                            navController.navigate(Routes.AlarmDetailScreen(alarmId = alarmId))
                        },
                        navigateToAddAlarm = {
                            navController.navigate(Routes.AddAlarmScreen) {
                                popUpTo<Routes.HomeScreen> { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable<Routes.AddAlarmScreen> { entry ->
                    val viewModel = koinViewModel<AddAlarmViewModel>()
                    val sharedViewModel =
                        entry.sharedKoinViewModel<SharedAlarmViewModel>(navController = navController)

                    val stops by sharedViewModel.stops.collectAsStateWithLifecycle()
                    androidx.compose.runtime.LaunchedEffect(stops) {
                        viewModel.onAction(AddAlarmAction.OnStopsChange(stops))
                    }

                    AddAlarmScreenRoot(
                        viewModel = viewModel,
                        sharedViewModel = sharedViewModel,
                        contentPadding = innerPadding,
                        navigateToStopPicker = {
                            navController.navigate(Routes.StopPickerScreen)
                        },
                        navigateToHome = {
                            sharedViewModel.clearStops()
                            navController.navigate(Routes.HomeScreen) {
                                popUpTo<Routes.HomeScreen> { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable<Routes.StopPickerScreen> { entry ->
                    val sharedViewModel =
                        entry.sharedKoinViewModel<SharedAlarmViewModel>(navController = navController)

                    StopPickerScreenRoot(
                        contentPadding = innerPadding,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        navigateToAddAlarm = { stop ->
                            sharedViewModel.addStop(stop)
                            navController.popBackStack()
                        }
                    )
                }

                composable<Routes.AlarmDetailScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<Routes.AlarmDetailScreen>()
                    AlarmDetailScreenRoot(
                        alarmId = args.alarmId,
                        contentPadding = innerPadding,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Routes.SettingsScreen> {
                    SettingsScreenRoot(contentPadding = innerPadding)
                }
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController
): T {
    val navGraphRoute = this.destination.parent?.route ?: return koinViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(viewModelStoreOwner = parentEntry)
}
