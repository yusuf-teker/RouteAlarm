package org.yusufteker.routealarm.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import io.github.aakira.napier.Napier
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.core.presentation.AppColors
import org.yusufteker.routealarm.feature.alarm.presentation.SharedAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.AddAlarmAction
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.AddAlarmScreenRoot
import org.yusufteker.routealarm.feature.alarm.presentation.addalarm.AddAlarmViewModel
import org.yusufteker.routealarm.feature.alarm.presentation.addstops.StopPickerScreenRoot
import org.yusufteker.routealarm.feature.alarm.presentation.alarmDetail.AlarmDetailScreenRoot
import org.yusufteker.routealarm.feature.onboarding.presentation.welcome.WelcomeScreen
import org.yusufteker.routealarm.feature.alarm.presentation.home.HomeScreenRoot
import org.yusufteker.routealarm.feature.alarm.presentation.home.components.BottomNavigationBar
import org.yusufteker.routealarm.settings.SettingsManager
import org.yusufteker.routealarm.settings.SettingsScreenRoot


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    settingsManager: SettingsManager = koinInject<SettingsManager>()
) {

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route?.substringAfterLast(".")
    val systemBars = WindowInsets.systemBars
    val bottomPadding = with(LocalDensity.current) { systemBars.getBottom(this).toDp() }
    val showBottomBar = currentRoute in listOf(
        Routes.MainGraph.toString(),
        Routes.HomeScreen.toString(),
        Routes.AddAlarmScreen.toString(),
        Routes.SettingsScreen.toString(),
        Routes.ActiveAlarmScreen.toString(),
    )

    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
           Napier.d("-- Navigated to: $route --", tag = "ScreenNavigation" )
        }
    }

    val onboardingDone by produceState<Boolean?>(initialValue = null) {
        value = settingsManager.isOnboardingCompleted()
    }

    if (onboardingDone == null) {
        Box(
            Modifier.fillMaxSize().background(AppColors.background),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    val startDestination =
        if (onboardingDone == true) Routes.MainGraph else Routes.OnboardingGraph

    Scaffold(
        modifier = Modifier.background(AppColors.cardBackground),
        bottomBar = {
            if (showBottomBar) {

                BottomNavigationBar(
                    modifier = Modifier.padding(bottom = bottomPadding),
                    currentRoute = currentRoute ?: "",
                    onItemSelected = { route ->
                        if (route.toString() != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Routes.HomeScreen.toString()) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    })
            }
        }) { innerPadding ->
        NavHost(navController = navController, startDestination = startDestination) {

            // Onboarding Graph
            navigation<Routes.OnboardingGraph>(
                startDestination = Routes.WelcomeScreen
            ) {

                composable<Routes.WelcomeScreen> {
                    WelcomeScreen(onContinue = {
                        // Onboarding'den sonra ana ekrana geçiş
                        navController.navigate(Routes.MainGraph) {
                            popUpTo(Routes.OnboardingGraph) { inclusive = true }
                            launchSingleTop = true
                        }


                    })
                    LaunchedEffect(Unit) {
                        settingsManager.setOnboardingCompleted(true)
                    }
                }

                // Eğer AuthScreen eklenirse buraya gelecek
                // composable(Routes.AuthScreen::class.simpleName!!) {
                //     AuthScreen(onContinue = { navController.navigate(Routes.HomeScreen::class.simpleName!!) })
                // }
            }

            // Main Graph (Ana ekran ve yönlendirmeler)
            navigation<Routes.MainGraph>(
                startDestination = Routes.HomeScreen
            ) {

                composable<Routes.HomeScreen> {
                    HomeScreenRoot(
                        contentPadding = innerPadding,
                        onNavigateToAlarmDetail = { alarm ->
                            navController.navigate(Routes.AlarmDetailScreen(alarmId = alarm))
                        },
                        navigateToAddAlarm = {
                            navController.navigate(Routes.AddAlarmScreen)
                        },
                    )

                }

                composable<Routes.AddAlarmScreen> { entry ->

                    val viewModel = koinViewModel<AddAlarmViewModel>()
                    val sharedViewModel =
                        entry.sharedKoinViewModel<SharedAlarmViewModel>(navController = navController)

                    val stops by sharedViewModel.stops.collectAsStateWithLifecycle()
                    LaunchedEffect(stops) {
                        stops.let {
                            viewModel.onAction(AddAlarmAction.OnStopsChange(it))
                        }
                    }

                    AddAlarmScreenRoot(
                        viewModel = viewModel,
                        contentPadding = innerPadding,
                        navigateToStopPicker = {
                            navController.navigate(Routes.StopPickerScreen)
                        },
                        navigateToHome = {
                            sharedViewModel.clearStops()
                            navController.navigate(Routes.HomeScreen)
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
                            navigateToAddAlarm = {
                                sharedViewModel.addStop(it)
                                navController.popBackStack()
                            }
                        )

                    }

                composable<Routes.AlarmDetailScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<Routes.AlarmDetailScreen>()
                    val alarmIdd = args.alarmId
                    AlarmDetailScreenRoot(
                        alarmId = alarmIdd,
                        contentPadding = innerPadding,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Routes.ActiveAlarmScreen> {

                }

                composable<Routes.SettingsScreen> {

                    SettingsScreenRoot(contentPadding = innerPadding,)
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

