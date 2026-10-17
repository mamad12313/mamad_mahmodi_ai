package com.mohammadpatrick.assistant.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Lock : Screen("lock")
    object SetupWizard : Screen("setup_wizard")
    object Dashboard : Screen("dashboard")
    object Chat : Screen("chat?conversationId={conversationId}") {
        fun createRoute(conversationId: String = "") = "chat?conversationId=$conversationId"
    }
    object Voice : Screen("voice")
    object Memory : Screen("memory")
    object Reminders : Screen("reminders")
    object Settings : Screen("settings")
    object SamsungA23Guide : Screen("samsung_a23_guide")
    object About : Screen("about")
}
