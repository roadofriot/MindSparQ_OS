package com.mindsparq.os.features.fieldops

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.mindsparq.os.core.auth.AuthenticatedSession
import com.mindsparq.os.core.network.SupabaseConfig
import com.mindsparq.os.core.theme.LocalMindSparqColors
import com.mindsparq.os.core.ui.MindSparqIconButton
import com.mindsparq.os.core.ui.StateContainer
import com.mindsparq.os.core.ui.UiState
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRecordDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("attendance_type") val attendanceType: String,
    @SerialName("check_in") val checkIn: String? = null,
    @SerialName("verification_status") val verificationStatus: String = "pending",
    val status: String = "present",
    val notes: String? = null,
)

@Serializable
data class AttendanceCheckInRequest(
    @SerialName("user_id") val userId: String,
    @SerialName("attendance_type") val attendanceType: String = "office",
    @SerialName("verification_status") val verificationStatus: String = "manual_exception",
    val notes: String? = null,
)

@Serializable
data class VisitDto(
    val id: String,
    val purpose: String,
    @SerialName("planned_at") val plannedAt: String? = null,
    val status: String = "planned",
)

@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val priority: String = "normal",
    val status: String = "pending",
)

@Serializable
data class TaskCreateRequest(
    val title: String,
    val description: String? = null,
    @SerialName("assigned_to") val assignedTo: String,
    @SerialName("assigned_by") val assignedBy: String,
)

private enum class FieldTab { ATTENDANCE, VISITS, TASKS }

@Composable
fun FieldOpsScreen(session: AuthenticatedSession) {
    val colors = LocalMindSparqColors.current
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(FieldTab.ATTENDANCE) }
    var attendanceState by remember { mutableStateOf<UiState<List<AttendanceRecordDto>>>(UiState.Loading) }
    var visitsState by remember { mutableStateOf<UiState<List<VisitDto>>>(UiState.Loading) }
    var tasksState by remember { mutableStateOf<UiState<List<TaskDto>>>(UiState.Loading) }
    var showTaskDialog by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            val client = SupabaseConfig.client
            if (client == null) {
                val err = UiState.Error("Supabase is not configured.")
                attendanceState = err; visitsState = err; tasksState = err
                return@launch
            }
            when (activeTab) {
                FieldTab.ATTENDANCE -> {
                    attendanceState = UiState.Loading
                    runCatching {
                        client.from("attendance_records")
                            .select { order("created_at", Order.DESCENDING) }
                            .decodeList<AttendanceRecordDto>()
                    }
                        .onSuccess { list ->
                            attendanceState = if (list.isEmpty()) {
                                UiState.Empty("No Attendance Records", "Use Check-In to log office or school attendance.")
                            } else UiState.Success(list)
                        }
                        .onFailure { attendanceState = UiState.Error(it.message ?: "Failed to load attendance") { reload() } }
                }
                FieldTab.VISITS -> {
                    visitsState = UiState.Loading
                    runCatching {
                        client.from("visits")
                            .select { order("created_at", Order.DESCENDING) }
                            .decodeList<VisitDto>()
                    }
                        .onSuccess { list ->
                            visitsState = if (list.isEmpty()) {
                                UiState.Empty("No School Visits", "No planned or completed visits recorded.")
                            } else UiState.Success(list)
                        }
                        .onFailure { visitsState = UiState.Error(it.message ?: "Failed to load visits") { reload() } }
                }
                FieldTab.TASKS -> {
                    tasksState = UiState.Loading
                    runCatching {
                        client.from("tasks")
                            .select { order("created_at", Order.DESCENDING) }
                            .decodeList<TaskDto>()
                    }
                        .onSuccess { list ->
                            tasksState = if (list.isEmpty()) {
                                UiState.Empty("No Tasks", "Add a task using the + button.")
                            } else UiState.Success(list)
                        }
                        .onFailure { tasksState = UiState.Error(it.message ?: "Failed to load tasks") { reload() } }
                }
            }
        }
    }

    LaunchedEffect(activeTab) { reload() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MindSparqIconButton(
                    icon = Icons.Outlined.Fingerprint,
                    tooltip = "Attendance",
                    selected = activeTab == FieldTab.ATTENDANCE,
                    onClick = { activeTab = FieldTab.ATTENDANCE },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.LocationOn,
                    tooltip = "School Visits",
                    selected = activeTab == FieldTab.VISITS,
                    onClick = { activeTab = FieldTab.VISITS },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.TaskAlt,
                    tooltip = "Tasks",
                    selected = activeTab == FieldTab.TASKS,
                    onClick = { activeTab = FieldTab.TASKS },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MindSparqIconButton(
                    icon = Icons.Outlined.Refresh,
                    tooltip = "Refresh",
                    outlined = true,
                    onClick = { reload() },
                )
                if (activeTab == FieldTab.ATTENDANCE) {
                    MindSparqIconButton(
                        icon = Icons.Outlined.Fingerprint,
                        tooltip = "Submit Check-In",
                        outlined = true,
                        tint = colors.accentPrimary,
                        onClick = {
                            scope.launch {
                                val client = SupabaseConfig.client ?: return@launch
                                runCatching {
                                    client.from("attendance_records").insert(
                                        AttendanceCheckInRequest(
                                            userId = session.userId,
                                            attendanceType = "office",
                                            verificationStatus = "manual_exception",
                                            notes = "Explicit manual check-in",
                                        ),
                                    )
                                }.onSuccess { reload() }
                            }
                        },
                    )
                }
                if (activeTab == FieldTab.TASKS) {
                    MindSparqIconButton(
                        icon = Icons.Outlined.AddTask,
                        tooltip = "New Task",
                        outlined = true,
                        tint = colors.accentPrimary,
                        onClick = { showTaskDialog = true },
                    )
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)

        when (activeTab) {
            FieldTab.ATTENDANCE -> StateContainer(state = attendanceState) { list ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    items(list, key = { it.id }) { a ->
                        FieldRowCard(a.attendanceType.uppercase(), a.notes ?: "Check-in recorded", a.verificationStatus)
                    }
                }
            }
            FieldTab.VISITS -> StateContainer(state = visitsState) { list ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    items(list, key = { it.id }) { v ->
                        FieldRowCard(v.purpose, v.plannedAt ?: "Unscheduled", v.status)
                    }
                }
            }
            FieldTab.TASKS -> StateContainer(state = tasksState) { list ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    items(list, key = { it.id }) { t ->
                        FieldRowCard(t.title, t.description ?: "Priority: ${t.priority}", t.status)
                    }
                }
            }
        }
    }

    if (showTaskDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var err by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showTaskDialog = false },
            title = { Text("New Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it; err = null }, label = { Text("Task Title *") }, singleLine = true)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                    if (err != null) Text(err!!, color = colors.statusDanger, style = MaterialTheme.typography.labelMedium)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val client = SupabaseConfig.client ?: return@launch
                            runCatching {
                                client.from("tasks").insert(
                                    TaskCreateRequest(
                                        title = title.trim(),
                                        description = desc.trim().ifBlank { null },
                                        assignedTo = session.userId,
                                        assignedBy = session.userId,
                                    ),
                                )
                            }.onSuccess {
                                showTaskDialog = false
                                reload()
                            }.onFailure { err = it.message ?: "Failed to create task" }
                        }
                    },
                    enabled = title.isNotBlank(),
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showTaskDialog = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun FieldRowCard(title: String, subtitle: String, badge: String) {
    val colors = LocalMindSparqColors.current
    Surface(
        color = colors.bgSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.borderHairline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
            }
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall,
                color = colors.accentSecondary,
                modifier = Modifier
                    .background(colors.accentSecondary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
