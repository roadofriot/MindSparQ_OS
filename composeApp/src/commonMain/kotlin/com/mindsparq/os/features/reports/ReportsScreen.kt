package com.mindsparq.os.features.reports

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
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import com.mindsparq.os.core.i18n.formatNpr
import com.mindsparq.os.core.network.SupabaseConfig
import com.mindsparq.os.core.theme.LocalMindSparqColors
import com.mindsparq.os.core.ui.MindSparqIconButton
import com.mindsparq.os.core.ui.StateContainer
import com.mindsparq.os.core.ui.UiState
import com.mindsparq.os.features.finance.PaymentDto
import com.mindsparq.os.features.finance.ReceivableDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

data class ReportSummaryItem(
    val title: String,
    val detail: String,
    val metric: String,
)

@Composable
fun ReportsScreen() {
    val colors = LocalMindSparqColors.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UiState<List<ReportSummaryItem>>>(UiState.Loading) }

    fun reload() {
        scope.launch {
            state = UiState.Loading
            runCatching {
                val client = SupabaseConfig.client ?: error("Supabase is not configured.")
                val receivables = client.from("receivables").select().decodeList<ReceivableDto>()
                val payments = client.from("payments").select().decodeList<PaymentDto>()

                val overdue = receivables.filter { it.status == "overdue" }.sumOf { it.amountDue }
                val pending = receivables.filter { it.status in setOf("pending", "partial") }.sumOf { it.amountDue }
                val collected = payments.sumOf { it.amount }

                listOf(
                    ReportSummaryItem("Total Collections", "${payments.size} posted receipts", formatNpr(collected)),
                    ReportSummaryItem("Pending / Partial Receivables", "Open scheduled balances", formatNpr(pending)),
                    ReportSummaryItem("Overdue Receivables", "Requires follow-up", formatNpr(overdue)),
                )
            }
                .onSuccess { state = UiState.Success(it) }
                .onFailure { state = UiState.Error(it.message ?: "Failed to generate report") { reload() } }
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MindSparqIconButton(icon = Icons.Outlined.Assessment, tooltip = "Financial & Ops Reports", selected = true, onClick = {})
                Text("Live Reports", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            }
            MindSparqIconButton(icon = Icons.Outlined.Refresh, tooltip = "Refresh Reports", outlined = true, onClick = { reload() })
        }
        HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)

        StateContainer(state = state) { items ->
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(items, key = { it.title }) { item ->
                    Surface(
                        color = colors.bgSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.borderHairline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Column {
                                Text(item.title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text(item.detail, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                            }
                            Text(item.metric, style = MaterialTheme.typography.titleMedium, color = colors.accentPrimary)
                        }
                    }
                }
            }
        }
    }
}
