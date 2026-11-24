package com.mindsparq.os

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mindsparq.os.core.auth.AppDestination
import com.mindsparq.os.core.auth.AuthRepository
import com.mindsparq.os.core.auth.AuthenticatedSession
import com.mindsparq.os.core.auth.allowedDestinations
import com.mindsparq.os.core.i18n.AppLanguage
import com.mindsparq.os.core.i18n.AppStrings
import com.mindsparq.os.core.i18n.CalendarSystem
import com.mindsparq.os.core.i18n.stringsFor
import com.mindsparq.os.core.network.SupabaseConfig
import com.mindsparq.os.core.theme.LocalMindSparqColors
import com.mindsparq.os.core.theme.MindSparqTheme
import com.mindsparq.os.core.theme.ThemeMode
import com.mindsparq.os.core.ui.MindSparqBrandMark
import com.mindsparq.os.core.ui.MindSparqIconButton
import com.mindsparq.os.core.ui.StateContainer
import com.mindsparq.os.core.ui.UiState
import com.mindsparq.os.features.academic.AcademicScreen
import com.mindsparq.os.features.dashboard.DashboardScreen
import com.mindsparq.os.features.fieldops.FieldOpsScreen
import com.mindsparq.os.features.finance.FinanceScreen
import com.mindsparq.os.features.inventory.InventoryScreen
import com.mindsparq.os.features.reports.ReportsScreen
import com.mindsparq.os.features.schools.SchoolsScreen
import com.mindsparq.os.features.settings.SettingsScreen
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
fun MindSparqApp(isDesktop: Boolean) {
    var themeMode by remember { mutableStateOf(ThemeMode.DARK) }
    var language by remember { mutableStateOf(AppLanguage.EN) }
    var calendar by remember { mutableStateOf(CalendarSystem.BS) }
    var session by remember { mutableStateOf<AuthenticatedSession?>(null) }
    var authChecked by remember { mutableStateOf(false) }
    var configRevision by remember { mutableStateOf(0) }

    val authRepo = remember { AuthRepository() }
    val strings = remember(language) { stringsFor(language) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(configRevision) {
        if (SupabaseConfig.isConfigured) {
            session = runCatching { authRepo.loadCurrentSession() }.getOrNull()
        }
        authChecked = true
    }

    MindSparqTheme(themeMode = themeMode) {
        val colors = LocalMindSparqColors.current
        Surface(
            color = colors.bgCanvas,
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        ) {
            when {
                !SupabaseConfig.isConfigured -> {
                    WorkspaceSetupScreen(
                        themeMode = themeMode,
                        onToggleTheme = {
                            themeMode = if (themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
                        },
                        onConfigured = { url, anonKey ->
                            SupabaseConfig.configureRuntime(url, anonKey)
                            configRevision++
                        },
                    )
                }
                !authChecked -> {
                    StateContainer<Unit>(state = UiState.Loading) {}
                }
                session == null -> {
                    SignInScreen(
                        strings = strings,
                        themeMode = themeMode,
                        onToggleTheme = {
                            themeMode = if (themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
                        },
                        onToggleLanguage = {
                            language = if (language == AppLanguage.EN) AppLanguage.NE else AppLanguage.EN
                        },
                        onSignIn = { email, password, onError ->
                            scope.launch {
                                authRepo.signIn(email, password)
                                    .onSuccess { session = it }
                                    .onFailure { onError(it.message ?: "Authentication failed") }
                            }
                        },
                    )
                }
                else -> {
                    MainAdaptiveShell(
                        session = session!!,
                        isDesktop = isDesktop,
                        strings = strings,
                        themeMode = themeMode,
                        calendar = calendar,
                        onToggleTheme = {
                            themeMode = if (themeMode == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
                        },
                        onToggleCalendar = {
                            calendar = if (calendar == CalendarSystem.BS) CalendarSystem.AD else CalendarSystem.BS
                        },
                        onToggleLanguage = {
                            language = if (language == AppLanguage.EN) AppLanguage.NE else AppLanguage.EN
                        },
                        onSignOut = {
                            scope.launch {
                                authRepo.signOut()
                                session = null
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkspaceSetupScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onConfigured: (url: String, anonKey: String) -> Unit,
) {
    val colors = LocalMindSparqColors.current
    var url by remember { mutableStateOf("") }
    var anonKey by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        MindSparqIconButton(
            icon = if (themeMode == ThemeMode.DARK) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
            tooltip = "Switch Day/Night theme",
            onClick = onToggleTheme,
            outlined = true,
            modifier = Modifier.align(Alignment.TopEnd),
        )

        Surface(
            color = colors.bgSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colors.borderHairline),
            modifier = Modifier.align(Alignment.Center).widthIn(max = 440.dp).fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(28.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MindSparqBrandMark(size = 34.dp)
                    Column {
                        Text("Connect Workspace", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                        Text(
                            "Enter your MindSparQ Supabase server URL & public anon key",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                        )
                    }
                }
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it; errorMsg = null },
                    label = { Text("Supabase URL (https://...supabase.co)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = anonKey,
                    onValueChange = { anonKey = it; errorMsg = null },
                    label = { Text("Public Anon Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (errorMsg != null) {
                    Text(errorMsg!!, style = MaterialTheme.typography.labelMedium, color = colors.statusDanger)
                }
                Button(
                    onClick = {
                        if (!url.trim().startsWith("https://")) {
                            errorMsg = "URL must start with https://"
                        } else if (anonKey.isBlank()) {
                            errorMsg = "Anon key is required"
                        } else {
                            onConfigured(url.trim(), anonKey.trim())
                        }
                    },
                    enabled = url.isNotBlank() && anonKey.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                ) {
                    Text("Connect")
                }
            }
        }
    }
}

@Composable
private fun SignInScreen(
    strings: AppStrings,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSignIn: (email: String, password: String, onError: (String) -> Unit) -> Unit,
) {
    val colors = LocalMindSparqColors.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            MindSparqIconButton(
                icon = Icons.Outlined.Translate,
                tooltip = strings.toggleLanguage,
                onClick = onToggleLanguage,
                outlined = true,
            )
            MindSparqIconButton(
                icon = if (themeMode == ThemeMode.DARK) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                tooltip = strings.toggleTheme,
                onClick = onToggleTheme,
                outlined = true,
            )
        }

        Surface(
            color = colors.bgSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colors.borderHairline),
            modifier = Modifier.align(Alignment.Center).widthIn(max = 400.dp).fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(28.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MindSparqBrandMark(size = 36.dp)
                    Text(
                        text = strings.signInTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary,
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMsg = null },
                    label = { Text(strings.emailLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMsg = null },
                    label = { Text(strings.passwordLabel) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.statusDanger,
                    )
                }
                Button(
                    onClick = { onSignIn(email, password) { errorMsg = it } },
                    enabled = email.isNotBlank() && password.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                ) {
                    Text(strings.signInAction)
                }
            }
        }
    }
}

@Composable
private fun MainAdaptiveShell(
    session: AuthenticatedSession,
    isDesktop: Boolean,
    strings: AppStrings,
    themeMode: ThemeMode,
    calendar: CalendarSystem,
    onToggleTheme: () -> Unit,
    onToggleCalendar: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSignOut: () -> Unit,
) {
    val colors = LocalMindSparqColors.current
    val destinations = remember(session.roles, isDesktop) {
        allowedDestinations(session.roles, isDesktop)
    }
    var currentDest by remember(destinations) {
        mutableStateOf(destinations.firstOrNull() ?: AppDestination.DASHBOARD)
    }
    var showCommandSearch by remember { mutableStateOf(false) }
    var showQuickCreate by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    val topBar: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(colors.bgSurface)
                .padding(horizontal = 16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!isDesktop) {
                    MindSparqBrandMark(size = 28.dp)
                }
                Text(
                    text = destinationLabel(currentDest, strings),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Text(
                    text = if (calendar == CalendarSystem.BS) "BS" else "AD",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accentSecondary,
                    modifier = Modifier
                        .background(colors.accentSecondary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MindSparqIconButton(
                    icon = Icons.Outlined.Search,
                    tooltip = strings.searchHint,
                    shortcutHint = "⌘K",
                    onClick = { showCommandSearch = true },
                    outlined = true,
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.Add,
                    tooltip = "Quick Create",
                    shortcutHint = "⌘N",
                    onClick = { showQuickCreate = true },
                    outlined = true,
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.CalendarMonth,
                    tooltip = strings.toggleCalendar,
                    shortcutHint = "⌘D",
                    onClick = onToggleCalendar,
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.Translate,
                    tooltip = strings.toggleLanguage,
                    onClick = onToggleLanguage,
                )
                MindSparqIconButton(
                    icon = if (themeMode == ThemeMode.DARK) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    tooltip = strings.toggleTheme,
                    shortcutHint = "⌘⇧L",
                    onClick = onToggleTheme,
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.Notifications,
                    tooltip = strings.notifications,
                    onClick = { showNotifications = true },
                )
                MindSparqIconButton(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    tooltip = strings.signOutAction,
                    onClick = onSignOut,
                )
            }
        }
    }

    if (isDesktop) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Collapsed 72dp icon-only NavigationRail with hover tooltips
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .width(72.dp)
                    .fillMaxHeight()
                    .background(colors.bgSurface)
                    .padding(vertical = 12.dp),
            ) {
                MindSparqBrandMark(size = 34.dp)
                Spacer(Modifier.height(8.dp))
                destinations.forEach { dest ->
                    MindSparqIconButton(
                        icon = destinationIcon(dest),
                        tooltip = destinationLabel(dest, strings),
                        selected = currentDest == dest,
                        size = 42.dp,
                        iconSize = 20.dp,
                        onClick = { currentDest = dest },
                    )
                }
            }
            VerticalDivider(thickness = 1.dp, color = colors.borderHairline)
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                topBar()
                HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)
                DestinationWorkspace(
                    destination = currentDest,
                    isDesktop = isDesktop,
                    session = session,
                    themeMode = themeMode,
                    calendar = calendar,
                    onToggleTheme = onToggleTheme,
                    onToggleCalendar = onToggleCalendar,
                    onToggleLanguage = onToggleLanguage,
                    onSignOut = onSignOut,
                )
            }
        }
    } else {
        Scaffold(
            containerColor = colors.bgCanvas,
            topBar = {
                Column {
                    topBar()
                    HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)
                }
            },
            bottomBar = {
                Column {
                    HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .background(colors.bgSurface),
                    ) {
                        destinations.forEach { dest ->
                            MindSparqIconButton(
                                icon = destinationIcon(dest),
                                tooltip = destinationLabel(dest, strings),
                                selected = currentDest == dest,
                                size = 42.dp,
                                onClick = { currentDest = dest },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                DestinationWorkspace(
                    destination = currentDest,
                    isDesktop = isDesktop,
                    session = session,
                    themeMode = themeMode,
                    calendar = calendar,
                    onToggleTheme = onToggleTheme,
                    onToggleCalendar = onToggleCalendar,
                    onToggleLanguage = onToggleLanguage,
                    onSignOut = onSignOut,
                )
            }
        }
    }

    if (showCommandSearch) {
        CommandSearchDialog(
            destinations = destinations,
            strings = strings,
            onSelect = { dest ->
                currentDest = dest
                showCommandSearch = false
            },
            onDismiss = { showCommandSearch = false },
        )
    }

    if (showQuickCreate) {
        QuickCreateDialog(
            destinations = destinations,
            strings = strings,
            onNavigate = { dest ->
                currentDest = dest
                showQuickCreate = false
            },
            onDismiss = { showQuickCreate = false },
        )
    }

    if (showNotifications) {
        NotificationsDialog(
            userId = session.userId,
            onDismiss = { showNotifications = false },
        )
    }
}

@Serializable
private data class NotificationRow(
    val id: String,
    val title: String,
    val body: String,
    val category: String? = null,
)

@Composable
private fun CommandSearchDialog(
    destinations: List<AppDestination>,
    strings: AppStrings,
    onSelect: (AppDestination) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalMindSparqColors.current
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, destinations) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) destinations
        else destinations.filter { destinationLabel(it, strings).lowercase().contains(q) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.searchHint) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Jump to module...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.height(220.dp)) {
                    items(filtered) { dest ->
                        Surface(
                            color = colors.bgElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(dest) },
                        ) {
                            Text(
                                text = destinationLabel(dest, strings),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun QuickCreateDialog(
    destinations: List<AppDestination>,
    strings: AppStrings,
    onNavigate: (AppDestination) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalMindSparqColors.current
    val createTargets = remember(destinations) {
        destinations.filter {
            it in setOf(
                AppDestination.SCHOOLS,
                AppDestination.ACADEMIC,
                AppDestination.FINANCE,
                AppDestination.INVENTORY,
                AppDestination.FIELD_OPS,
            )
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Create") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                createTargets.forEach { dest ->
                    Surface(
                        color = colors.bgElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onNavigate(dest) },
                    ) {
                        Text(
                            text = "Open ${destinationLabel(dest, strings)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun NotificationsDialog(
    userId: String,
    onDismiss: () -> Unit,
) {
    val colors = LocalMindSparqColors.current
    var state by remember { mutableStateOf<UiState<List<NotificationRow>>>(UiState.Loading) }

    LaunchedEffect(userId) {
        runCatching {
            val client = SupabaseConfig.client ?: error("Supabase is not configured.")
            client.from("notifications")
                .select {
                    filter { eq("user_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<NotificationRow>()
        }
            .onSuccess { list ->
                state = if (list.isEmpty()) {
                    UiState.Empty("All Caught Up", "No unread notifications.")
                } else UiState.Success(list)
            }
            .onFailure { state = UiState.Error(it.message ?: "Failed to load notifications") }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notifications") },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                StateContainer(state = state) { items ->
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(items, key = { it.id }) { n ->
                            Surface(
                                color = colors.bgElevated,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(n.title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                    Text(n.body, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun DestinationWorkspace(
    destination: AppDestination,
    isDesktop: Boolean,
    session: AuthenticatedSession,
    themeMode: ThemeMode,
    calendar: CalendarSystem,
    onToggleTheme: () -> Unit,
    onToggleCalendar: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSignOut: () -> Unit,
) {
    when (destination) {
        AppDestination.DASHBOARD -> DashboardScreen()
        AppDestination.SCHOOLS -> SchoolsScreen(isDesktop = isDesktop)
        AppDestination.ACADEMIC -> AcademicScreen()
        AppDestination.FINANCE -> FinanceScreen()
        AppDestination.INVENTORY -> InventoryScreen()
        AppDestination.FIELD_OPS -> FieldOpsScreen(session = session)
        AppDestination.REPORTS -> ReportsScreen()
        AppDestination.SETTINGS -> SettingsScreen(
            session = session,
            themeMode = themeMode,
            calendar = calendar,
            onToggleTheme = onToggleTheme,
            onToggleCalendar = onToggleCalendar,
            onToggleLanguage = onToggleLanguage,
            onSignOut = onSignOut,
        )
    }
}

private fun destinationIcon(dest: AppDestination): ImageVector = when (dest) {
    AppDestination.DASHBOARD -> Icons.Outlined.Dashboard
    AppDestination.SCHOOLS -> Icons.Outlined.School
    AppDestination.ACADEMIC -> Icons.AutoMirrored.Outlined.MenuBook
    AppDestination.FINANCE -> Icons.Outlined.AccountBalanceWallet
    AppDestination.INVENTORY -> Icons.Outlined.Inventory2
    AppDestination.FIELD_OPS -> Icons.Outlined.LocationOn
    AppDestination.REPORTS -> Icons.Outlined.Assessment
    AppDestination.SETTINGS -> Icons.Outlined.Settings
}

private fun destinationLabel(dest: AppDestination, strings: AppStrings): String = when (dest) {
    AppDestination.DASHBOARD -> strings.dashboard
    AppDestination.SCHOOLS -> strings.schools
    AppDestination.ACADEMIC -> strings.academic
    AppDestination.FINANCE -> strings.finance
    AppDestination.INVENTORY -> strings.inventory
    AppDestination.FIELD_OPS -> strings.fieldOps
    AppDestination.REPORTS -> strings.reports
    AppDestination.SETTINGS -> strings.settings
}
