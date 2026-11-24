package com.mindsparq.os.features.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.mindsparq.os.core.i18n.formatNpr
import com.mindsparq.os.core.network.SupabaseConfig
import com.mindsparq.os.core.theme.LocalMindSparqColors
import com.mindsparq.os.core.ui.MindSparqIconButton
import com.mindsparq.os.core.ui.StateContainer
import com.mindsparq.os.core.ui.UiState
import com.mindsparq.os.features.academic.ProgramDto
import com.mindsparq.os.features.finance.ReceivableDto
import com.mindsparq.os.features.schools.SchoolDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
private data class TaskIdOnly(val id: String, val status: String = "pending")

data class DashboardMetrics(
    val activeSchools: Int,
    val totalSchools: Int,
    val activePrograms: Int,
    val openReceivablesNpr: Double,
    val pendingTasks: Int,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen() {
    val colors = LocalMindSparqColors.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UiState<DashboardMetrics>>(UiState.Loading) }

    fun reload() {
        scope.launch {
            state = UiState.Loading
            runCatching {
                val client = SupabaseConfig.client ?: error("Supabase is not configured.")
                val schools = client.from("schools").select().decodeList<SchoolDto>()
                val programs = client.from("programs").select().decodeList<ProgramDto>()
                val receivables = client.from("receivables").select().decodeList<ReceivableDto>()
                val tasks = client.from("tasks").select().decodeList<TaskIdOnly>()

                DashboardMetrics(
                    activeSchools = schools.count { it.status == "active" },
                    totalSchools = schools.size,
                    activePrograms = programs.count { it.status == "active" },
                    openReceivablesNpr = receivables
                        .filter { it.status in setOf("pending", "partial", "overdue") }
                        .sumOf { it.amountDue },
                    pendingTasks = tasks.count { it.status in setOf("pending", "in_progress", "overdue") },
                )
            }
                .onSuccess { state = UiState.Success(it) }
                .onFailure { state = UiState.Error(it.message ?: "Failed to load dashboard") { reload() } }
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Operational Overview",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            MindSparqIconButton(
                icon = Icons.Outlined.Refresh,
                tooltip = "Refresh Metrics",
                outlined = true,
                onClick = { reload() },
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)

        StateContainer(state = state) { metrics ->
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(16.dp),
            ) {
                KpiCard(
                    icon = Icons.Outlined.School,
                    label = "Active Schools",
                    value = "${metrics.activeSchools} / ${metrics.totalSchools}",
                )
                KpiCard(
                    icon = Icons.AutoMirrored.Outlined.EventNote,
                    label = "Active Programs",
                    value = "${metrics.activePrograms}",
                )
                KpiCard(
                    icon = Icons.Outlined.AccountBalanceWallet,
                    label = "Open Receivables",
                    value = formatNpr(metrics.openReceivablesNpr),
                )
                KpiCard(
                    icon = Icons.Outlined.TaskAlt,
                    label = "Open Tasks",
                    value = "${metrics.pendingTasks}",
                )
            }
        }
    }
}

@Composable
private fun KpiCard(icon: ImageVector, label: String, value: String) {
    val colors = LocalMindSparqColors.current
    Surface(
        color = colors.bgSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colors.borderHairline),
        modifier = Modifier.widthIn(min = 210.dp, max = 280.dp).fillMaxWidth(0.48f),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                Icon(icon, contentDescription = null, tint = colors.accentPrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
        }
    }
}
