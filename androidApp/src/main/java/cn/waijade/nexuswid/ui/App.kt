package cn.waijade.nexuswid.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import org.jetbrains.compose.resources.stringResource
import cn.waijade.nexuswid.data.github.GitHubApiService
import cn.waijade.nexuswid.data.github.GitHubPreferences
import cn.waijade.nexuswid.data.github.IssueType
import cn.waijade.nexuswid.data.github.PullRequestType
import cn.waijade.nexuswid.data.afdian.AfdianApiService
import cn.waijade.nexuswid.data.afdian.AfdianPreferences
import cn.waijade.nexuswid.data.afdian.AfdianServiceConfig
import cn.waijade.nexuswid.ui.settingsScreen.SettingsScreenRoot
import cn.waijade.nexuswid.ui.liquidglass.LiquidBottomTab
import cn.waijade.nexuswid.widget.HeatmapWidgetDataStore
import cn.waijade.nexuswid.widget.WidgetDataCache
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import cn.waijade.nexuswid.ui.liquidglass.LiquidBottomTabs
import cn.waijade.nexuswid.ui.settingsScreen.viewModel.SettingsViewModel
import cn.waijade.nexuswid.ui.theme.NexusTheme
import cn.waijade.nexuswid.ui.utils.onBack
import cn.waijade.nexuswid.utils.toColor
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import nexuswid.shared.generated.resources.Res
import nexuswid.shared.generated.resources.home
import nexuswid.shared.generated.resources.settings
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun App() {
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val settingsState by settingsViewModel.settingsState.collectAsStateWithLifecycle()

    val darkTheme = when (settingsState.theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val seed = settingsState.colorScheme.toColor()

    NexusTheme(
        darkTheme = darkTheme,
        seedColor = seed,
        blackTheme = settingsState.blackTheme
    ) {
        AppScreen(
            settingsViewModel = settingsViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = koinViewModel()
) {
    val layoutDirection = LocalLayoutDirection.current
    val motionScheme = motionScheme
    val settingsState by settingsViewModel.settingsState.collectAsStateWithLifecycle()
    val darkTheme = when (settingsState.theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val systemBarsInsets = WindowInsets.systemBars.asPaddingValues()
    val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()
    val backdropSurfaceColor = colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(backdropSurfaceColor)
        drawContent()
    }

    val backStack = rememberNavBackStack(Screen.Home)
    val context = LocalContext.current

    val widgetData = remember { mutableStateOf(HomeWidgetData()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // 加载热力图数据
            val dataStore = HeatmapWidgetDataStore(context)
            val levels = dataStore.getContributionLevels()
            widgetData.value = widgetData.value.copy(
                contributionLevels = levels,
                isHeatmapLoaded = true
            )

            // 加载 PR 数据
            val prefs = GitHubPreferences(context)
            val types = prefs.selectedPullRequestTypes
            val token = prefs.token.takeIf { it.isNotBlank() }
            val prList = if (token != null) {
                val json = Json { ignoreUnknownKeys = true }
                val httpClient = HttpClient(OkHttp) {
                    install(ContentNegotiation) { json(json) }
                }
                val service = GitHubApiService(httpClient, json)
                val result = runCatching {
                    service.getPullRequestList(
                        token = token, pullRequestTypes = types,
                        limit = 20, withCheckStatus = 3
                    ).getOrThrow()
                }
                httpClient.close()
                result.getOrElse { WidgetDataCache.loadPullRequests(context) }
                    .also { if (result.isSuccess) WidgetDataCache.savePullRequests(context, it) }
            } else {
                WidgetDataCache.loadPullRequests(context)
            }
            widgetData.value = widgetData.value.copy(
                prItems = prList,
                prTypes = types,
                isPrLoaded = true
            )

            // 加载审查请求数量
            val count = if (token != null) {
                val json = Json { ignoreUnknownKeys = true }
                val httpClient = HttpClient(OkHttp) {
                    install(ContentNegotiation) { json(json) }
                }
                val service = GitHubApiService(httpClient, json)
                val result = runCatching {
                    service.getPullRequestCount(
                        token = token,
                        pullRequestTypes = setOf(PullRequestType.REVIEW_REQUESTED)
                    ).getOrThrow()
                }
                httpClient.close()
                result.getOrElse { WidgetDataCache.loadReviewsCount(context) }
                    .also { if (result.isSuccess) WidgetDataCache.saveReviewsCount(context, it) }
            } else {
                WidgetDataCache.loadReviewsCount(context)
            }
            widgetData.value = widgetData.value.copy(
                reviewsCount = count,
                isReviewsLoaded = true
            )

            // 加载 Issues 数据
            val issueTypes = prefs.selectedIssueTypes
            val issueList = if (token != null) {
                val json2 = Json { ignoreUnknownKeys = true }
                val httpClient2 = HttpClient(OkHttp) {
                    install(ContentNegotiation) { json(json2) }
                }
                val service2 = GitHubApiService(httpClient2, json2)
                val result = runCatching {
                    service2.getIssueList(
                        token = token, issueTypes = issueTypes, limit = 20
                    ).getOrThrow()
                }
                httpClient2.close()
                result.getOrElse { WidgetDataCache.loadIssues(context) }
                    .also { if (result.isSuccess) WidgetDataCache.saveIssues(context, it) }
            } else {
                WidgetDataCache.loadIssues(context)
            }
            widgetData.value = widgetData.value.copy(
                issueItems = issueList,
                issueTypes = issueTypes,
                isIssuesLoaded = true
            )

            // 加载 Actions 数据 - 使用用户第一个仓库
            val actionsRepo: String
            val actionsList = if (token != null) {
                val json3 = Json { ignoreUnknownKeys = true }
                val httpClient3 = HttpClient(OkHttp) {
                    install(ContentNegotiation) { json(json3) }
                }
                val service3 = GitHubApiService(httpClient3, json3)
                val repos = runCatching {
                    service3.getUserRepos(token, limit = 5).getOrThrow()
                }.getOrElse { emptyList() }
                val repo = repos.firstOrNull()?.fullName ?: ""
                actionsRepo = repo
                if (repo.isNotBlank()) {
                    val result = runCatching {
                        service3.getWorkflowRuns(token, repo, limit = 10).getOrThrow()
                    }
                    httpClient3.close()
                    result.getOrElse { WidgetDataCache.loadActions(context, "home") }
                        .also { if (result.isSuccess) WidgetDataCache.saveActions(context, "home", it) }
                } else {
                    httpClient3.close()
                    emptyList()
                }
            } else {
                actionsRepo = ""
                emptyList()
            }
            widgetData.value = widgetData.value.copy(
                actionRuns = actionsList,
                actionsRepo = actionsRepo,
                isActionsLoaded = true
            )

            // 加载 Notifications 数据
            val notifList = if (token != null) {
                val json4 = Json { ignoreUnknownKeys = true }
                val httpClient4 = HttpClient(OkHttp) {
                    install(ContentNegotiation) { json(json4) }
                }
                val service4 = GitHubApiService(httpClient4, json4)
                val result = runCatching {
                    service4.getNotifications(token, limit = 10).getOrThrow()
                }
                httpClient4.close()
                result.getOrElse { WidgetDataCache.loadNotifications(context) }
                    .also { if (result.isSuccess) WidgetDataCache.saveNotifications(context, it) }
            } else {
                WidgetDataCache.loadNotifications(context)
            }
            val unreadCount = notifList.count { it.isUnread }
            widgetData.value = widgetData.value.copy(
                notificationItems = notifList,
                notificationUnreadCount = unreadCount,
                isNotificationsLoaded = true,
                githubConfigured = token != null
            )

            // 加载 Afdian 数据
            val afdianPrefs = AfdianPreferences(context)
            val afdianConfigured = afdianPrefs.isConfigured
            widgetData.value = widgetData.value.copy(afdianConfigured = afdianConfigured)

            if (afdianConfigured) {
                val afJson = Json { ignoreUnknownKeys = true }
                val afHttpClient = HttpClient(OkHttp) {
                    install(ContentNegotiation) { json(afJson) }
                }
                val afService = AfdianApiService(afHttpClient, afJson)
                val config = AfdianServiceConfig(cookie = afdianPrefs.cookie)

                // 总收益 & 本月收益
                runCatching {
                    val earningsResult = afService.getEarnings(config)
                    if (earningsResult is cn.waijade.nexuswid.data.afdian.AfdianResult.Success) {
                        val e = earningsResult.earnings
                        widgetData.value = widgetData.value.copy(
                            afdianTotalEarnings = e.totalAmount,
                            afdianMonthlyEarnings = e.monthlyAmount
                        )
                    }
                }
                widgetData.value = widgetData.value.copy(
                    isAfdianTotalLoaded = true,
                    isAfdianMonthlyLoaded = true
                )

                // 未读数
                runCatching {
                    val unreadResult = afService.getUnreadCount(afdianPrefs.cookie)
                    if (unreadResult is cn.waijade.nexuswid.data.afdian.AfdianUnreadResult.Success) {
                        widgetData.value = widgetData.value.copy(
                            afdianUnreadCount = unreadResult.data.total
                        )
                    }
                }
                widgetData.value = widgetData.value.copy(isAfdianUnreadLoaded = true)

                // 商品收益
                runCatching {
                    val planId = afdianPrefs.selectedProductPlanId
                    if (planId.isNotBlank()) {
                        val plans = afService.getPlans(afdianPrefs.cookie)
                        val plan = plans.find { it.plan_id == planId }
                        if (plan != null) {
                            widgetData.value = widgetData.value.copy(
                                afdianProductData = cn.waijade.nexuswid.data.afdian.AfdianProductSummary(
                                    planId = plan.plan_id,
                                    name = plan.name,
                                    totalAmount = plan.total_amount.toDoubleOrNull() ?: 0.0,
                                    sponsorCount = plan.sponsor_count,
                                    price = plan.price
                                )
                            )
                        }
                    }
                }
                widgetData.value = widgetData.value.copy(isAfdianProductLoaded = true)

                // 每日收益图表
                runCatching {
                    val dailyStats = afService.getDailyStats(afdianPrefs.cookie)
                    widgetData.value = widgetData.value.copy(afdianDailyStats = dailyStats)
                }
                widgetData.value = widgetData.value.copy(isAfdianDailyLoaded = true)

                // 月度收益图表
                runCatching {
                    val monthlyIncome = afService.getMonthlyIncome(afdianPrefs.cookie)
                    widgetData.value = widgetData.value.copy(afdianMonthlyIncomes = monthlyIncome)
                }
                widgetData.value = widgetData.value.copy(isAfdianMonthlyIncomeLoaded = true)

                // 投诉数
                runCatching {
                    val complaintCount = afService.getComplaintCount(afdianPrefs.cookie)
                    widgetData.value = widgetData.value.copy(afdianComplaintCount = complaintCount)
                }
                widgetData.value = widgetData.value.copy(isAfdianComplaintLoaded = true)

                // 随机推荐创作者
                runCatching {
                    val creator = fetchAfdianRandomCreator(afdianPrefs)
                    widgetData.value = widgetData.value.copy(afdianRandomCreator = creator)
                }
                widgetData.value = widgetData.value.copy(isAfdianRandomCreatorLoaded = true)

                // 赞助月榜
                runCatching {
                    val userId = afdianPrefs.userId.ifBlank {
                        afService.getUserIdFromProfile(afdianPrefs.cookie)?.also {
                            afdianPrefs.userId = it
                        } ?: ""
                    }
                    if (userId.isNotBlank()) {
                        val sponsors = afService.getTopSponsors(userId)
                        widgetData.value = widgetData.value.copy(afdianTopSponsors = sponsors)
                    }
                }
                widgetData.value = widgetData.value.copy(isAfdianTopSponsorsLoaded = true)

                afHttpClient.close()
            } else {
                widgetData.value = widgetData.value.copy(
                    isAfdianTotalLoaded = true,
                    isAfdianMonthlyLoaded = true,
                    isAfdianUnreadLoaded = true,
                    isAfdianProductLoaded = true,
                    isAfdianDailyLoaded = true,
                    isAfdianMonthlyIncomeLoaded = true,
                    isAfdianComplaintLoaded = true,
                    isAfdianRandomCreatorLoaded = true,
                    isAfdianTopSponsorsLoaded = true
                )
            }
        }
    }

    val toolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(
        FloatingToolbarExitDirection.Bottom
    )

    val mainScreens = remember {
        listOf(
            NavItem(
                Screen.Home,
                Icons.Outlined.Home,
                Icons.Filled.Home,
                Res.string.home
            ) {},
            NavItem(
                Screen.Settings.Main,
                Icons.Outlined.Settings,
                Icons.Filled.Settings,
                Res.string.settings
            ) {}
        )
    }

    val showBottomBar by remember {
        derivedStateOf {
            val route = backStack.lastOrNull()
            route == Screen.Home || (route == Screen.Settings.Main && settingsState.isAtRoot)
        }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                showBottomBar,
                enter = slideInVertically(motionScheme.slowSpatialSpec()) { it },
                exit = slideOutVertically(motionScheme.slowSpatialSpec()) { it }
            ) {
                val wide = remember {
                    windowSizeClass.isWidthAtLeastBreakpoint(
                        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = cutoutInsets.calculateStartPadding(layoutDirection),
                            end = cutoutInsets.calculateEndPadding(layoutDirection)
                        ),
                    Alignment.Center
                ) {
                    if (settingsState.liquidGlassBottomBar) {
                        LiquidGlassBottomBar(
                            mainScreens = mainScreens,
                            selectedRoute = { backStack.lastOrNull() },
                            onNavigate = { item ->
                                if (backStack.lastOrNull() == item.route) item.onNavigateHome()
                                else if (backStack.size < 2) backStack.add(item.route)
                                else backStack[1] = item.route
                            },
                            wide = wide,
                            backdrop = backdrop,
                            bottomPadding = systemBarsInsets.calculateBottomPadding(),
                            isLightTheme = !darkTheme
                        )
                    } else {
                        Md3BottomBar(
                            mainScreens = mainScreens,
                            selectedRoute = { backStack.lastOrNull() },
                            onNavigate = { item ->
                                if (backStack.lastOrNull() == item.route) item.onNavigateHome()
                                else if (backStack.size < 2) backStack.add(item.route)
                                else backStack[1] = item.route
                            },
                            wide = wide,
                            scrollBehavior = toolbarScrollBehavior,
                            bottomPadding = systemBarsInsets.calculateBottomPadding()
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { contentPadding ->
        SharedTransitionLayout(
            modifier = if (settingsState.liquidGlassBottomBar) {
                Modifier
                    .fillMaxSize()
                    .layerBackdrop(backdrop)
            } else {
                Modifier.fillMaxSize()
            }
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = backStack::onBack,
                transitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                popTransitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                predictivePopTransitionSpec = {
                    fadeIn(motionScheme.defaultEffectsSpec())
                        .togetherWith(fadeOut(motionScheme.defaultEffectsSpec()))
                },
                entryProvider = entryProvider {
                    entry<Screen.Home> {
                        HomeScreen(
                            widgetData = widgetData.value,
                            contentPadding = contentPadding,
                            onWidgetClick = { widgetType ->
                                backStack.add(Screen.Widget(widgetType))
                            }
                        )
                    }

                    entry<Screen.Widget> { widget ->
                        WidgetDetailScreen(
                            widgetType = widget.widgetType,
                            widgetData = widgetData.value,
                            contentPadding = contentPadding,
                            onBack = { backStack.onBack() }
                        )
                    }

                    entry<Screen.Settings.Main> {
                        SettingsScreenRoot(
                            contentPadding = contentPadding,
                            settingsViewModel = settingsViewModel
                        )
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Md3BottomBar(
    mainScreens: List<NavItem>,
    selectedRoute: () -> Any?,
    onNavigate: (NavItem) -> Unit,
    wide: Boolean,
    scrollBehavior: FloatingToolbarScrollBehavior,
    bottomPadding: Dp,
) {
    val motionScheme = motionScheme

    HorizontalFloatingToolbar(
        expanded = true,
        scrollBehavior = scrollBehavior,
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
            toolbarContainerColor = colorScheme.primaryContainer,
            toolbarContentColor = colorScheme.onPrimaryContainer
        ),
        modifier = Modifier
            .padding(
                top = ScreenOffset,
                bottom = bottomPadding + ScreenOffset
            )
            .zIndex(1f)
    ) {
        mainScreens.fastForEach { item ->
            val selected by remember { derivedStateOf { selectedRoute() == item.route } }
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above
                ),
                tooltip = { PlainTooltip { Text(stringResource(item.label)) } },
                state = rememberTooltipState()
            ) {
                ToggleButton(
                    checked = selected,
                    onCheckedChange = { onNavigate(item) },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer,
                        checkedContainerColor = colorScheme.primary,
                        checkedContentColor = colorScheme.onPrimary
                    ),
                    shapes = ToggleButtonDefaults.shapes(
                        CircleShape,
                        CircleShape,
                        CircleShape
                    ),
                    modifier = Modifier.height(56.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Crossfade(selected) {
                            if (it) Icon(
                                item.selectedIcon,
                                stringResource(item.label)
                            )
                            else Icon(
                                item.unselectedIcon,
                                stringResource(item.label)
                            )
                        }
                        AnimatedVisibility(
                            visible = selected || wide,
                            enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                            exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                        ) {
                            Text(
                                text = stringResource(item.label),
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LiquidGlassBottomBar(
    mainScreens: List<NavItem>,
    selectedRoute: () -> Any?,
    onNavigate: (NavItem) -> Unit,
    wide: Boolean,
    backdrop: Backdrop,
    bottomPadding: Dp,
    isLightTheme: Boolean,
) {
    val contentColor = if (isLightTheme) Color.Black else Color.White
    val motionScheme = motionScheme
    val routeIndex = mainScreens.indexOfFirst { it.route == selectedRoute() }.coerceAtLeast(0)
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(routeIndex) }

    LaunchedEffect(routeIndex) {
        selectedTabIndex = routeIndex
    }

    fun selectTab(index: Int) {
        selectedTabIndex = index
        mainScreens.getOrNull(index)?.let(onNavigate)
    }

    LiquidBottomTabs(
        selectedTabIndex = { selectedTabIndex },
        onTabSelected = { selectTab(it) },
        backdrop = backdrop,
        tabsCount = mainScreens.size,
        isLightTheme = isLightTheme,
        modifier = Modifier
            .padding(
                top = ScreenOffset,
                bottom = bottomPadding + ScreenOffset,
                start = 16.dp,
                end = 16.dp
            )
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .zIndex(1f)
    ) {
        mainScreens.forEachIndexed { index, item ->
            val selected = selectedTabIndex == index

            LiquidBottomTab({ selectTab(index) }) {
                Crossfade(selected) {
                    Icon(
                        imageVector = if (it) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.label),
                        tint = contentColor
                    )
                }
                AnimatedVisibility(
                    visible = true,
                    enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                    exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                ) {
                    Text(
                        text = stringResource(item.label),
                        color = contentColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}


private suspend fun fetchAfdianRandomCreator(prefs: AfdianPreferences): cn.waijade.nexuswid.data.afdian.AfdianRandomCreator? {
    val json = Json { ignoreUnknownKeys = true }
    repeat(3) {
        runCatching {
            val page = (1..20).random()
            val url = java.net.URL("https://ifdian.net/api/creator/list?page=$page&type=hot")
            val text = url.readText()
            val response = json.decodeFromString(
                cn.waijade.nexuswid.data.afdian.AfdianCreatorListResponse.serializer(), text
            )
            if (response.ec != 200 || response.data?.list.isNullOrEmpty()) return@runCatching
            val list = response.data!!.list
            val item = list.random()
            val creatorInfo = item.creator
            return cn.waijade.nexuswid.data.afdian.AfdianRandomCreator(
                userId = item.user_id,
                name = item.name,
                avatar = item.avatar,
                urlSlug = item.url_slug,
                isVerified = item.is_verified != 0,
                doing = creatorInfo?.doing ?: "",
                categoryName = creatorInfo?.category?.name ?: ""
            )
        }
    }
    return null
}