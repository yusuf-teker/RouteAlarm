package org.yusufteker.routealarm.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import org.yusufteker.routealarm.feature.onboarding.presentation.welcome.WelcomeScreen
import org.yusufteker.routealarm.feature.alarm.presentation.home.HomeScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Routes.OnboardingGraph = Routes.OnboardingGraph
) {
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

                HomeScreen(
                    onAddAlarm = { navController.navigate(Routes.AddAlarmScreen) },
                    onAlarmClick = { alarmId ->
                        navController.navigate(Routes.AlarmDetailScreen(alarmId = alarmId))
                    },
                    onSettingsClick = { navController.navigate(Routes.SettingsScreen) })

            }

            composable<Routes.AddAlarmScreen> {
                //AddAlarmScreen(onStopSelect = { navController.navigate(Routes.StopPickerScreen::class.simpleName!!) })
            }

            composable<Routes.StopPickerScreen> {
                //StopPickerScreen(onBack = { navController.popBackStack() })
            }

            composable<Routes.AlarmDetailScreen> { backStackEntry ->
                val args = backStackEntry.toRoute<Routes.AlarmDetailScreen>()
                val alarmId = args.alarmId
                //AlarmDetailScreen(alarmId = alarmId)
            }

            composable<Routes.ActiveAlarmScreen> {
                //ActiveAlarmScreen()
            }

            composable<Routes.SettingsScreen> {
                //SettingsScreen()
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

