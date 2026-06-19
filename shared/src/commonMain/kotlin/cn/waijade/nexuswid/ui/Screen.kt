package cn.waijade.nexuswid.ui

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
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

        @Serializable
        object BuildInfo : Settings()

        @Serializable
        object GitHub : Settings()

        @Serializable
        object Widget : Settings()

        @Serializable
        object Debug : Settings()
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
    val icon: DrawableResource,
    val label: StringResource,
    val innerSettings: List<StringResource>
)
