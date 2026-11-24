package com.mindsparq.os.features.schools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.mindsparq.os.core.theme.LocalMindSparqColors
import com.mindsparq.os.core.ui.MindSparqIconButton
import com.mindsparq.os.core.ui.StateContainer
import com.mindsparq.os.core.ui.UiState
import kotlinx.coroutines.launch

private enum class SchoolDetailTab { OVERVIEW, CONTACTS, CLASSES }

@Composable
fun SchoolsScreen(isDesktop: Boolean) {
    val colors = LocalMindSparqColors.current
    val repo = remember { SchoolRepository() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<SchoolStatus?>(null) }
    var schoolsState by remember { mutableStateOf<UiState<List<SchoolDto>>>(UiState.Loading) }
    var selectedSchool by remember { mutableStateOf<SchoolDto?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            schoolsState = UiState.Loading
            repo.listSchools(searchQuery, statusFilter)
                .onSuccess { list ->
                    schoolsState = if (list.isEmpty()) {
                        UiState.Empty(
                            title = "No Schools Found",
                            subtitle = "Add a school record with the + action to start managing contacts and programs.",
                        )
                    } else {
                        UiState.Success(list)
                    }
                    if (selectedSchool != null) {
                        selectedSchool = list.firstOrNull { it.id == selectedSchool?.id }
                    }
                }
                .onFailure { err ->
                    schoolsState = UiState.Error(err.message ?: "Failed to load schools") { reload() }
                }
        }
    }

    LaunchedEffect(statusFilter) { reload() }

    Column(modifier = Modifier.fillMaxSize()) {
        // Minimal icon-first toolbar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search school name, code, district...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.weight(1f).height(48.dp),
            )
            MindSparqIconButton(
                icon = Icons.Outlined.Search,
                tooltip = "Apply Search",
                outlined = true,
                onClick = { reload() },
            )
            MindSparqIconButton(
                icon = Icons.Outlined.Refresh,
                tooltip = "Refresh Schools",
                outlined = true,
                onClick = { reload() },
            )
            MindSparqIconButton(
                icon = Icons.Outlined.Add,
                tooltip = "Add New School",
                shortcutHint = "N",
                outlined = true,
                tint = colors.accentPrimary,
                onClick = { showCreateDialog = true },
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)

        if (isDesktop) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(0.42f).fillMaxHeight()) {
                    StateContainer(state = schoolsState) { list ->
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize().padding(14.dp),
                        ) {
                            items(list, key = { it.id }) { school ->
                                SchoolRowCard(
                                    school = school,
                                    selected = selectedSchool?.id == school.id,
                                    onClick = { selectedSchool = school },
                                )
                            }
                        }
                    }
                }
                VerticalDivider(thickness = 1.dp, color = colors.borderHairline)
                Box(modifier = Modifier.weight(0.58f).fillMaxHeight()) {
                    val active = selectedSchool
                    if (active == null) {
                        StateContainer<Unit>(
                            state = UiState.Empty(
                                title = "Select a School",
                                subtitle = "Choose a school from the list to inspect overview, contacts, and classes.",
                            ),
                        ) {}
                    } else {
                        SchoolDetailPane(school = active, repo = repo, onBack = null)
                    }
                }
            }
        } else {
            val active = selectedSchool
            if (active != null) {
                SchoolDetailPane(school = active, repo = repo, onBack = { selectedSchool = null })
            } else {
                StateContainer(state = schoolsState) { list ->
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize().padding(14.dp),
                    ) {
                        items(list, key = { it.id }) { school ->
                            SchoolRowCard(
                                school = school,
                                selected = false,
                                onClick = { selectedSchool = school },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateSchoolDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { req, onErr ->
                scope.launch {
                    repo.createSchool(req)
                        .onSuccess { created ->
                            showCreateDialog = false
                            selectedSchool = created
                            reload()
                        }
                        .onFailure { onErr(it.message ?: "Failed to create school") }
                }
            },
        )
    }
}

@Composable
private fun SchoolRowCard(
    school: SchoolDto,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalMindSparqColors.current
    val status = SchoolStatus.fromDb(school.status)
    Surface(
        color = if (selected) colors.accentPrimary.copy(alpha = 0.08f) else colors.bgSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) colors.accentPrimary.copy(alpha = 0.45f) else colors.borderHairline,
        ),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(school.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Spacer(Modifier.height(2.dp))
                val meta = listOfNotNull(school.schoolCode, school.district, school.municipality)
                    .joinToString(" • ")
                    .ifBlank { "No location metadata" }
                Text(meta, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.accentSecondary,
                modifier = Modifier
                    .background(colors.accentSecondary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun SchoolDetailPane(
    school: SchoolDto,
    repo: SchoolRepository,
    onBack: (() -> Unit)?,
) {
    val colors = LocalMindSparqColors.current
    var tab by remember(school.id) { mutableStateOf(SchoolDetailTab.OVERVIEW) }
    var contactsState by remember(school.id) { mutableStateOf<UiState<List<SchoolContactDto>>>(UiState.Loading) }
    var classesState by remember(school.id) { mutableStateOf<UiState<List<SchoolClassDto>>>(UiState.Loading) }

    LaunchedEffect(school.id, tab) {
        when (tab) {
            SchoolDetailTab.CONTACTS -> {
                contactsState = UiState.Loading
                repo.listContacts(school.id)
                    .onSuccess { list ->
                        contactsState = if (list.isEmpty()) {
                            UiState.Empty("No Contacts", "No contacts recorded for ${school.name}.")
                        } else UiState.Success(list)
                    }
                    .onFailure { contactsState = UiState.Error(it.message ?: "Failed to load contacts") }
            }
            SchoolDetailTab.CLASSES -> {
                classesState = UiState.Loading
                repo.listClasses(school.id)
                    .onSuccess { list ->
                        classesState = if (list.isEmpty()) {
                            UiState.Empty("No Classes", "No classes/grades configured for ${school.name}.")
                        } else UiState.Success(list)
                    }
                    .onFailure { classesState = UiState.Error(it.message ?: "Failed to load classes") }
            }
            SchoolDetailTab.OVERVIEW -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onBack != null) {
                    MindSparqIconButton(
                        icon = Icons.AutoMirrored.Outlined.ArrowBack,
                        tooltip = "Back to Schools",
                        outlined = true,
                        onClick = onBack,
                    )
                }
                Column {
                    Text(school.name, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                    Text(
                        text = school.schoolCode ?: "Unassigned Code",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MindSparqIconButton(
                    icon = Icons.Outlined.Info,
                    tooltip = "Overview",
                    selected = tab == SchoolDetailTab.OVERVIEW,
                    onClick = { tab = SchoolDetailTab.OVERVIEW },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.Contacts,
                    tooltip = "Contacts",
                    selected = tab == SchoolDetailTab.CONTACTS,
                    onClick = { tab = SchoolDetailTab.CONTACTS },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.Class,
                    tooltip = "Classes & Sections",
                    selected = tab == SchoolDetailTab.CLASSES,
                    onClick = { tab = SchoolDetailTab.CLASSES },
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)
        Spacer(Modifier.height(14.dp))

        when (tab) {
            SchoolDetailTab.OVERVIEW -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoRow("Status", SchoolStatus.fromDb(school.status).label)
                    InfoRow("Principal", school.principalName ?: "—")
                    InfoRow("Coordinator", school.coordinatorName ?: "—")
                    InfoRow("Phone", school.phone ?: "—")
                    InfoRow("Email", school.email ?: "—")
                    InfoRow("Location", listOfNotNull(school.address, school.municipality, school.district).joinToString(", ").ifBlank { "—" })
                }
            }
            SchoolDetailTab.CONTACTS -> {
                StateContainer(state = contactsState) { contacts ->
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(contacts, key = { it.id }) { c ->
                            Surface(
                                color = colors.bgSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.borderHairline),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(c.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                    Text(
                                        listOfNotNull(c.designation, c.phone, c.email).joinToString(" • "),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.textSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            SchoolDetailTab.CLASSES -> {
                StateContainer(state = classesState) { classes ->
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(classes, key = { it.id }) { cls ->
                            Surface(
                                color = colors.bgSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.borderHairline),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    cls.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                    modifier = Modifier.padding(12.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = LocalMindSparqColors.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
    }
}

@Composable
private fun CreateSchoolDialog(
    onDismiss: () -> Unit,
    onSubmit: (SchoolCreateRequest, onError: (String) -> Unit) -> Unit,
) {
    val colors = LocalMindSparqColors.current
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var principal by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New School") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it; errorMsg = null }, label = { Text("School Name *") }, singleLine = true)
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("School Code") }, singleLine = true)
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, singleLine = true)
                OutlinedTextField(value = principal, onValueChange = { principal = it }, label = { Text("Principal Name") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true)
                if (errorMsg != null) {
                    Text(errorMsg!!, style = MaterialTheme.typography.labelMedium, color = colors.statusDanger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        SchoolCreateRequest(
                            name = name.trim(),
                            schoolCode = code.trim().ifBlank { null },
                            district = district.trim().ifBlank { null },
                            principalName = principal.trim().ifBlank { null },
                            phone = phone.trim().ifBlank { null },
                        ),
                    ) { errorMsg = it }
                },
                enabled = name.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
