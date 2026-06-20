package cn.waijade.nexuswid

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import cn.waijade.nexuswid.data.github.GitHubApiService
import cn.waijade.nexuswid.data.github.GitHubPreferences
import cn.waijade.nexuswid.data.github.RepoItem
import cn.waijade.nexuswid.ui.theme.NexusTheme
import cn.waijade.nexuswid.widget.ActionsWidget
import cn.waijade.nexuswid.widget.EXTRA_CONFIGURE_ACTIONS_WIDGET
import cn.waijade.nexuswid.widget.getActionsRepoForWidget
import cn.waijade.nexuswid.widget.setActionsRepoForWidget
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ActionsConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetId = intent.getStringExtra(EXTRA_CONFIGURE_ACTIONS_WIDGET) ?: return finish()
        setContent {
            NexusTheme {
                ActionsConfigScreen(widgetId, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionsConfigScreen(widgetId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { GitHubPreferences(context) }
    val token = prefs.token
    val currentRepo = remember { getActionsRepoForWidget(context, widgetId) }

    var searchQuery by remember { mutableStateOf("") }
    var userRepos by remember { mutableStateOf<List<RepoItem>?>(null) }
    var searchResults by remember { mutableStateOf<List<RepoItem>?>(null) }
    var loading by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        if (token.isBlank()) return@LaunchedEffect
        loading = true
        withContext(Dispatchers.IO) {
            val json = Json { ignoreUnknownKeys = true }
            val httpClient = HttpClient(OkHttp) {
                install(ContentNegotiation) { json(json) }
            }
            val api = GitHubApiService(httpClient, json)
            val repos = runCatching { api.getUserRepos(token).getOrDefault(emptyList()) }
                .getOrDefault(emptyList())
            httpClient.close()
            repos
        }.also {
            userRepos = it.filter { !it.isPrivate }
            loading = false
        }
    }

    fun doSearch(query: String) {
        if (query.isBlank() || token.isBlank()) {
            searchResults = null
            return
        }
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(300)
            loading = true
            val results = withContext(Dispatchers.IO) {
                val json = Json { ignoreUnknownKeys = true }
                val httpClient = HttpClient(OkHttp) {
                    install(ContentNegotiation) { json(json) }
                }
                val api = GitHubApiService(httpClient, json)
                val repos = runCatching { api.searchRepos(token, query).getOrDefault(emptyList()) }
                    .getOrDefault(emptyList())
                httpClient.close()
                repos
            }
            searchResults = results
            loading = false
        }
    }

    fun saveAndExit(repo: String) {
        setActionsRepoForWidget(context, widgetId, repo)
        Toast.makeText(context, "已设置: $repo", Toast.LENGTH_SHORT).show()
        scope.launch {
            val manager = GlanceAppWidgetManager(context)
            manager.getGlanceIds(ActionsWidget::class.java).forEach { id ->
                if (id.toString() == widgetId) {
                    ActionsWidget().update(context, id)
                }
            }
            onBack()
        }
    }

    val displayRepos = when {
        searchQuery.isNotBlank() -> searchResults
        else -> userRepos
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actions") },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    doSearch(it)
                },
                placeholder = { Text("搜索仓库 owner/repo") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (currentRepo.isNotBlank()) {
                Text(
                    text = "当前: $currentRepo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (loading && displayRepos == null) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(32.dp)
                )
            } else if (displayRepos.isNullOrEmpty()) {
                Text(
                    text = if (searchQuery.isNotBlank()) "未找到仓库" else "暂无仓库 (请先在设置中配置 Token)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayRepos, key = { it.fullName }) { repo ->
                        RepoRow(
                            repo = repo,
                            isSelected = repo.fullName == currentRepo,
                            onClick = { saveAndExit(repo.fullName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RepoRow(repo: RepoItem, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = repo.fullName,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (repo.isPrivate) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                val desc = repo.description
                if (!desc.isNullOrBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            val lang = repo.language
            if (!lang.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = lang,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
