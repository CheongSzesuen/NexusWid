package cn.waijade.nexuswid.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed class Screen : NavKey {
    @Serializable
    object Home : Screen()

    @Serializable
    object Settings : Screen()
}

data class NavItem(
    val route: Screen,
    val unselectedIcon: DrawableResource,
    val selectedIcon: DrawableResource,
    val label: StringResource,
    val onNavigateHome: () -> Unit
)
