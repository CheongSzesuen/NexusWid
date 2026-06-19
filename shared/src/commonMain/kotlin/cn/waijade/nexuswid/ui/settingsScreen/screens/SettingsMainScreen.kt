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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import cn.waijade.nexuswid.githubSettingsScreen
import cn.waijade.nexuswid.settingsScreens
import cn.waijade.nexuswid.ui.Screen
import cn.waijade.nexuswid.ui.mergePaddingValues
import cn.waijade.nexuswid.ui.theme.CustomColors.listItemColors
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.theme.LocalAppFonts
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.segmentedListItemShapes
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.about
import nexuswid.shared.generated.resources.app_name
import nexuswid.shared.generated.resources.arrow_forward_big
import nexuswid.shared.generated.resources.settings

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsMainScreen(
    contentPadding: PaddingValues,
    currentScreen: Screen.Settings,
    onNavigate: (Screen.Settings) -> Unit,
    isDebug: Boolean,
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

            item {
                val item = githubSettingsScreen
                SegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(item.icon), null)
                    },
                    supportingContent = {
                        val innerStrings = item.innerSettings.map { stringResource(it) }
                        val joinedText = remember(innerStrings) { innerStrings.joinToString(", ") }
                        Text(
                            joinedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = if (!widthExpanded) {
                        { Icon(painterResource(Res.drawable.arrow_forward_big), null) }
                    } else null,
                    shapes = segmentedListItemShapes(0, 1),
                    colors = listItemColors,
                    selected = currentScreen == item.route,
                    onClick = { onNavigate(item.route) }
                ) { Text(stringResource(item.label)) }
            }

            item { Spacer(Modifier.height(12.dp)) }

            itemsIndexed(settingsScreens) { index, item ->
                SegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(item.icon), null)
                    },
                    supportingContent = {
                        val innerStrings = item.innerSettings.map { stringResource(it) }
                        val joinedText = remember(innerStrings) { innerStrings.joinToString(", ") }
                        Text(
                            joinedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = if (!widthExpanded) {
                        { Icon(painterResource(Res.drawable.arrow_forward_big), null) }
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
                    supportingContent = {
                        Text(stringResource(Res.string.app_name) + " 0.0.1")
                    },
                    trailingContent = if (!widthExpanded) {
                        { Icon(painterResource(Res.drawable.arrow_forward_big), null) }
                    } else null,
                    selected = currentScreen == Screen.Settings.About,
                    shapes = segmentedListItemShapes(0, 1),
                    colors = listItemColors,
                    onClick = { onNavigate(Screen.Settings.About) }
                ) { Text(stringResource(Res.string.about)) }
            }
            item {
                Spacer(Modifier.height(12.dp))
            }

            if (isDebug) {
                item {
                    SegmentedListItem(
                        leadingContent = {
                            Icon(Icons.Default.Info, null)
                        },
                        supportingContent = {
                            Text("调试工具")
                        },
                        trailingContent = if (!widthExpanded) {
                            { Icon(painterResource(Res.drawable.arrow_forward_big), null) }
                        } else null,
                        selected = currentScreen == Screen.Settings.Debug,
                        shapes = segmentedListItemShapes(0, 1),
                        colors = listItemColors,
                        onClick = { onNavigate(Screen.Settings.Debug) }
                    ) { Text("Debug") }
                }

                item {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}