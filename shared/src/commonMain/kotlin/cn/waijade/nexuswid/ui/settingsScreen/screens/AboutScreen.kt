package cn.waijade.nexuswid.ui.settingsScreen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.jetbrains.compose.resources.stringResource
import cn.waijade.nexuswid.ui.mergePaddingValues
import cn.waijade.nexuswid.ui.theme.CustomColors.detailPaneTopBarColors
import cn.waijade.nexuswid.ui.theme.CustomColors.listItemColors
import cn.waijade.nexuswid.ui.theme.CustomColors.topBarColors
import cn.waijade.nexuswid.ui.theme.LocalAppFonts
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.PANE_MAX_WIDTH
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.bottomListItemShape
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.segmentedListItemShapes
import cn.waijade.nexuswid.ui.theme.NexusShapeDefaults.topListItemShape
import cn.waijade.nexuswid.ui.theme.NexusTheme
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.about
import nexuswid.shared.generated.resources.app_name
import nexuswid.shared.generated.resources.back
import nexuswid.shared.generated.resources.developer
import nexuswid.shared.generated.resources.license
import nexuswid.shared.generated.resources.version

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    var showLicense by rememberSaveable { mutableStateOf(false) }

    val barColors = if (widthExpanded) detailPaneTopBarColors
    else topBarColors

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(barColors.containerColor)
    ) {
        Scaffold(
            topBar = {
                LargeFlexibleTopAppBar(
                    title = {
                        Text(
                            stringResource(Res.string.about),
                            fontFamily = LocalAppFonts.current.topBarTitle
                        )
                    },
                    subtitle = {
                        Text(stringResource(Res.string.app_name))
                    },
                    navigationIcon = {
                        if (!widthExpanded)
                            FilledTonalIconButton(
                                onClick = onBack,
                                shapes = IconButtonDefaults.shapes(),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = listItemColors.containerColor
                                )
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    stringResource(Res.string.back)
                                )
                            }
                    },
                    colors = barColors,
                    scrollBehavior = scrollBehavior
                )
            },
            containerColor = barColors.containerColor,
            modifier = modifier
                .widthIn(max = PANE_MAX_WIDTH)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            val insets = mergePaddingValues(innerPadding, contentPadding)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = insets,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Box(Modifier.background(listItemColors.containerColor, topListItemShape)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Spacer(
                                Modifier
                                    .size(64.dp)
                                    .background(
                                        colorScheme.primaryContainer,
                                        MaterialShapes.Cookie12Sided.toShape()
                                    )
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    stringResource(Res.string.app_name),
                                    color = colorScheme.onSurface,
                                    style = typography.titleLarge,
                                    fontFamily = typography.bodyLarge.fontFamily
                                )
                                Text(
                                    text = "0.0.1",
                                    style = typography.labelLarge,
                                    color = colorScheme.primary
                                )
                            }
                        }
                    }
                }
                item {
                    Box(Modifier.background(listItemColors.containerColor, bottomListItemShape)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(
                                    Modifier
                                        .size(64.dp)
                                        .background(
                                            colorScheme.secondaryContainer,
                                            MaterialShapes.Square.toShape()
                                        )
                                        .padding(8.dp)
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        "NexusWid Team",
                                        style = typography.titleLarge,
                                        color = colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        stringResource(Res.string.developer),
                                        style = typography.labelLarge,
                                        color = colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }

                item {
                    SegmentedListItem(
                        onClick = { showLicense = true },
                        leadingContent = { Icon(Icons.Default.Info, null) },
                        content = { Text(stringResource(Res.string.license)) },
                        supportingContent = { Text("GNU General Public License Version 3") },
                        selected = showLicense,
                        shapes = segmentedListItemShapes(0, 1),
                        colors = listItemColors
                    )
                }
            }
        }
    }

    if (showLicense) {
        // TODO: LicenseBottomSheet
    }
}

@Preview
@Composable
private fun AboutScreenPreview() {
    NexusTheme(dynamicColor = false) {
        AboutScreen(
            contentPadding = PaddingValues(),
            onBack = {}
        )
    }
}
