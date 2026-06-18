package cn.waijade.nexuswid.ui.settingsScreen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.jetbrains.compose.resources.stringResource
import cn.waijade.nexuswid.settingsScreens
import cn.waijade.nexuswid.ui.Screen
import cn.waijade.nexuswid.ui.mergePaddingValues
import cn.waijade.nexuswid.ui.theme.CustomColors.listItemColors
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.theme.LocalAppFonts
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.segmentedListItemShapes
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.about
import nexuswid.shared.generated.resources.settings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsMainScreen(
    contentPadding: PaddingValues,
    currentScreen: Screen.Settings,
    onNavigate: (Screen.Settings) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.settings),
                        style = LocalTextStyle.current.copy(
                            fontFamily = LocalAppFonts.current.topBarTitle,
                            fontSize = 32.sp,
                            lineHeight = 32.sp
                        )
                    )
                },
                subtitle = {},
                colors = topBarColors,
                titleHorizontalAlignment = Alignment.CenterHorizontally,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = insets,
            modifier = Modifier
                .background(topBarColors.containerColor)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(Modifier.height(14.dp)) }

            itemsIndexed(settingsScreens) { index, item ->
                SegmentedListItem(
                    leadingContent = {
                        Icon(item.icon, null)
                    },
                    trailingContent = if (!widthExpanded) {
                        { Icon(Icons.Default.ArrowForward, null) }
                    } else null,
                    shapes = segmentedListItemShapes(index, settingsScreens.size),
                    colors = listItemColors,
                    selected = currentScreen == item.route,
                    onClick = { onNavigate(item.route) }
                ) { Text(stringResource(item.label)) }
            }

            item {
                Spacer(Modifier.height(12.dp))
            }

            item {
                SegmentedListItem(
                    leadingContent = {
                        Icon(Icons.Default.Info, null)
                    },
                    trailingContent = if (!widthExpanded) {
                        { Icon(Icons.Default.ArrowForward, null) }
                    } else null,
                    selected = currentScreen == Screen.Settings.About,
                    shapes = segmentedListItemShapes(0, 1),
                    colors = listItemColors,
                    onClick = { onNavigate(Screen.Settings.About) }
                ) { Text(stringResource(Res.string.about)) }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}
