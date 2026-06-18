package cn.waijade.nexuswid.ui.settingsScreen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.toShape
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import cn.waijade.nexuswid.ui.Screen
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
import nexuswid.shared.generated.resources.arrow_forward_big
import nexuswid.shared.generated.resources.back
import nexuswid.shared.generated.resources.gavel
import nexuswid.shared.generated.resources.ic_afdian
import nexuswid.shared.generated.resources.ic_bandbbs
import nexuswid.shared.generated.resources.ic_github
import nexuswid.shared.generated.resources.ic_globe
import nexuswid.shared.generated.resources.ic_launcher_monochrome
import nexuswid.shared.generated.resources.license
import nexuswid.shared.generated.resources.more
import nexuswid.shared.generated.resources.version

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onNavigate: (Screen.Settings) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uriHandler = LocalUriHandler.current

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
                                    Icons.AutoMirrored.Filled.ArrowBack,
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
                            Icon(
                                painterResource(Res.drawable.ic_launcher_monochrome),
                                tint = colorScheme.onPrimaryContainer,
                                contentDescription = null,
                                modifier = Modifier
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
                                    text = "0.0.1 (1)",
                                    style = typography.labelLarge,
                                    color = colorScheme.primary
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                FilledTonalIconButton(
                                    onClick = {
                                        uriHandler.openUri("https://github.com/CheongSzesuen/NexusWid")
                                    },
                                    shapes = IconButtonDefaults.shapes()
                                ) {
                                    Icon(
                                        painterResource(Res.drawable.ic_github),
                                        contentDescription = "GitHub",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Box(Modifier.background(listItemColors.containerColor, topListItemShape)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = "https://www.bandbbs.cn/data/avatars/o/344/344224.jpg?1757132574",
                                    contentDescription = "WaiJade的头像",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "WaiJade",
                                        style = typography.titleLarge,
                                        color = colorScheme.onSurface,
                                        fontFamily = typography.bodyLarge.fontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Developer",
                                        style = typography.labelLarge,
                                        color = colorScheme.secondary
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Spacer(Modifier.width((64 + 16).dp))
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf(
                                        Triple(Res.drawable.ic_github, "https://github.com/CheongSzesuen", "GitHub"),
                                        Triple(Res.drawable.ic_globe, "https://waijade.cn/", "个人网站"),
                                        Triple(Res.drawable.ic_bandbbs, "https://www.bandbbs.cn/members/344224/", "米坛社区"),
                                        Triple(Res.drawable.ic_afdian, "https://afdian.com/a/waijade", "爱发电")
                                    ).forEach { (icon, url, desc) ->
                                        FilledTonalIconButton(
                                            onClick = { uriHandler.openUri(url) },
                                            shapes = IconButtonDefaults.shapes(),
                                            modifier = Modifier.width(52.dp)
                                        ) {
                                            Icon(
                                                painterResource(icon),
                                                contentDescription = desc,
                                                modifier = Modifier.size(ButtonDefaults.SmallIconSize)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Box(Modifier.background(listItemColors.containerColor, bottomListItemShape)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = "https://www.bandbbs.cn/data/avatars/o/157/157218.jpg?1752559526",
                                    contentDescription = "Zaona的头像",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Zaona",
                                        style = typography.titleLarge,
                                        color = colorScheme.onSurface,
                                        fontFamily = typography.bodyLarge.fontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Developer",
                                        style = typography.labelLarge,
                                        color = colorScheme.secondary
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Spacer(Modifier.width((64 + 16).dp))
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf(
                                        Triple(Res.drawable.ic_github, "https://github.com/zaona", "GitHub"),
                                        Triple(Res.drawable.ic_globe, "https://zaona.top/", "个人网站"),
                                        Triple(Res.drawable.ic_bandbbs, "https://www.bandbbs.cn/members/157218/", "米坛社区"),
                                        Triple(Res.drawable.ic_afdian, "https://afdian.com/a/zaona", "爱发电")
                                    ).forEach { (icon, url, desc) ->
                                        FilledTonalIconButton(
                                            onClick = { uriHandler.openUri(url) },
                                            shapes = IconButtonDefaults.shapes(),
                                            modifier = Modifier.width(52.dp)
                                        ) {
                                            Icon(
                                                painterResource(icon),
                                                contentDescription = desc,
                                                modifier = Modifier.size(ButtonDefaults.SmallIconSize)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }

                item {
                    Text(
                        stringResource(Res.string.more),
                        style = typography.labelLarge,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                }

                item {
                    SegmentedListItem(
                        onClick = { onNavigate(Screen.Settings.BuildInfo) },
                        leadingContent = {
                            Icon(
                                painterResource(Res.drawable.ic_github),
                                null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                        },
                        content = { Text("构建信息") },
                        supportingContent = { Text("版本详情、平台环境与依赖") },
                        trailingContent = { Icon(painterResource(Res.drawable.arrow_forward_big), null) },
                        selected = false,
                        shapes = segmentedListItemShapes(0, 2),
                        colors = listItemColors
                    )
                }

                item {
                    SegmentedListItem(
                        onClick = { showLicense = true },
                        leadingContent = { Icon(painterResource(Res.drawable.gavel), null) },
                        content = { Text(stringResource(Res.string.license)) },
                        supportingContent = { Text("GNU General Public License Version 3") },
                        selected = showLicense,
                        shapes = segmentedListItemShapes(1, 2),
                        colors = listItemColors
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }
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
