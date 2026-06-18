package cn.waijade.nexuswid.ui

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

sealed class Screen : NavKey {
    @Serializable
    object Home : Screen()

    @Serializable
    sealed class Settings : Screen() {
        @Serializable
        object Main : Settings()

        @Serializable
        object About : Settings()

        @Serializable
        object Appearance : Settings()
    }
}

data class NavItem(
    val route: Screen,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val label: StringResource,
    val onNavigateHome: () -> Unit
)

data class SettingsNavItem(
    val route: Screen.Settings,
    val icon: ImageVector,
    val label: StringResource,
    val innerSettings: List<StringResource>
)
