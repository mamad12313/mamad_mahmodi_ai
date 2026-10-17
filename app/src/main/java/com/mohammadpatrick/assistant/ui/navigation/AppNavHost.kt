package com.mohammadpatrick.assistant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mohammadpatrick.assistant.MohammadPatrickApp
import com.mohammadpatrick.assistant.ui.screens.AboutScreen
import com.mohammadpatrick.assistant.ui.screens.ChatScreen
import com.mohammadpatrick.assistant.ui.screens.DashboardScreen
import com.mohammadpatrick.assistant.ui.screens.LockScreen
import com.mohammadpatrick.assistant.ui.screens.MemoryScreen
import com.mohammadpatrick.assistant.ui.screens.PermissionWizardScreen
import com.mohammadpatrick.assistant.ui.screens.RemindersScreen
import com.mohammadpatrick.assistant.ui.screens.SamsungA23GuideScreen
import com.mohammadpatrick.assistant.ui.screens.SettingsScreen
import com.mohammadpatrick.assistant.ui.screens.SplashScreen
import com.mohammadpatrick.assistant.ui.screens.VoiceScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    onBiometricRequest: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as MohammadPatrickApp
    val securityManager = app.securityManager
    val settings by app.settingsRepository.settingsFlow.collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = {
                    val isWizardDone = settings?.isSetupWizardCompleted == true
                    val isPinSet = securityManager.isPinConfigured()

                    if (!isPinSet) {
                        // Prompt user to set a master PIN first
                        navController.navigate(Screen.Lock.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else if (!isWizardDone) {
                        navController.navigate(Screen.SetupWizard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Lock.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Lock.route) {
            LockScreen(
                securityManager = securityManager,
                onUnlocked = {
                    val isWizardDone = settings?.isSetupWizardCompleted == true
                    if (!isWizardDone) {
                        navController.navigate(Screen.SetupWizard.route) {
                            popUpTo(Screen.Lock.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Lock.route) { inclusive = true }
                        }
                    }
                },
                onBiometricRequest = onBiometricRequest
            )
        }

        composable("change_password") {
            LockScreen(
                securityManager = securityManager,
                isSettingNewPin = true,
                onUnlocked = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.SetupWizard.route) {
            PermissionWizardScreen(
                onNavigateBack = { navController.popBackStack() },
                onCompleteWizard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.SetupWizard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToChat = { navController.navigate(Screen.Chat.createRoute()) },
                onNavigateToVoice = { navController.navigate(Screen.Voice.route) },
                onNavigateToMemory = { navController.navigate(Screen.Memory.route) },
                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToWizard = { navController.navigate(Screen.SetupWizard.route) },
                onNavigateToSamsungGuide = { navController.navigate(Screen.SamsungA23Guide.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onLockApp = {
                    securityManager.lockNow()
                    navController.navigate(Screen.Lock.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("conversationId") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val convId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatScreen(
                conversationIdArg = convId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Voice.route) {
            VoiceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Memory.route) {
            MemoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reminders.route) {
            RemindersScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWizard = { navController.navigate(Screen.SetupWizard.route) },
                onNavigateToSamsungGuide = { navController.navigate(Screen.SamsungA23Guide.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onChangePassword = { navController.navigate("change_password") }
            )
        }

        composable(Screen.SamsungA23Guide.route) {
            SamsungA23GuideScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
