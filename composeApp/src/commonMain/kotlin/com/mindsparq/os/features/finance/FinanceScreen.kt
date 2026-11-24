package com.mindsparq.os.features.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.SwapHoriz
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
import com.mindsparq.os.core.i18n.formatNpr
import com.mindsparq.os.core.theme.LocalMindSparqColors
import com.mindsparq.os.core.ui.MindSparqIconButton
import com.mindsparq.os.core.ui.StateContainer
import com.mindsparq.os.core.ui.UiState
import kotlinx.coroutines.launch

private enum class FinanceTab { RECEIVABLES, PAYMENTS, ACCOUNTS, EXPENSES }

@Composable
fun FinanceScreen() {
    val colors = LocalMindSparqColors.current
    val repo = remember { FinanceRepository() }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(FinanceTab.RECEIVABLES) }
    var receivablesState by remember { mutableStateOf<UiState<List<ReceivableDto>>>(UiState.Loading) }
    var paymentsState by remember { mutableStateOf<UiState<List<PaymentDto>>>(UiState.Loading) }
    var accountsState by remember { mutableStateOf<UiState<List<AccountDto>>>(UiState.Loading) }
    var expensesState by remember { mutableStateOf<UiState<List<ExpenseDto>>>(UiState.Loading) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            when (activeTab) {
                FinanceTab.RECEIVABLES -> {
                    receivablesState = UiState.Loading
                    repo.listReceivables()
                        .onSuccess { list ->
                            receivablesState = if (list.isEmpty()) {
                                UiState.Empty("No Receivables", "No open or scheduled receivables found.")
                            } else UiState.Success(list)
                        }
                        .onFailure { receivablesState = UiState.Error(it.message ?: "Failed to load receivables") { reload() } }
                }
                FinanceTab.PAYMENTS -> {
                    paymentsState = UiState.Loading
                    repo.listPayments()
                        .onSuccess { list ->
                            paymentsState = if (list.isEmpty()) {
                                UiState.Empty("No Payments", "Record a payment to generate a receipt and ledger entry.")
                            } else UiState.Success(list)
                        }
                        .onFailure { paymentsState = UiState.Error(it.message ?: "Failed to load payments") { reload() } }
                }
                FinanceTab.ACCOUNTS -> {
                    accountsState = UiState.Loading
                    repo.listAccounts()
                        .onSuccess { list ->
                            accountsState = if (list.isEmpty()) {
                                UiState.Empty("No Accounts", "Configure cash, bank, or wallet accounts in Supabase.")
                            } else UiState.Success(list)
                        }
                        .onFailure { accountsState = UiState.Error(it.message ?: "Failed to load accounts") { reload() } }
                }
                FinanceTab.EXPENSES -> {
                    expensesState = UiState.Loading
                    repo.listExpenses()
                        .onSuccess { list ->
                            expensesState = if (list.isEmpty()) {
                                UiState.Empty("No Expenses", "No expense requests recorded yet.")
                            } else UiState.Success(list)
                        }
                        .onFailure { expensesState = UiState.Error(it.message ?: "Failed to load expenses") { reload() } }
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
                    icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                    tooltip = "Receivables",
                    selected = activeTab == FinanceTab.RECEIVABLES,
                    onClick = { activeTab = FinanceTab.RECEIVABLES },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.Payments,
                    tooltip = "Payments & Receipts",
                    selected = activeTab == FinanceTab.PAYMENTS,
                    onClick = { activeTab = FinanceTab.PAYMENTS },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.AccountBalance,
                    tooltip = "Cash & Bank Accounts",
                    selected = activeTab == FinanceTab.ACCOUNTS,
                    onClick = { activeTab = FinanceTab.ACCOUNTS },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.RequestQuote,
                    tooltip = "Expenses",
                    selected = activeTab == FinanceTab.EXPENSES,
                    onClick = { activeTab = FinanceTab.EXPENSES },
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MindSparqIconButton(
                    icon = Icons.Outlined.Refresh,
                    tooltip = "Refresh",
                    outlined = true,
                    onClick = { reload() },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.SwapHoriz,
                    tooltip = "Transfer Between Accounts",
                    outlined = true,
                    onClick = { showTransferDialog = true },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.AddCard,
                    tooltip = "Record Payment",
                    shortcutHint = "P",
                    outlined = true,
                    tint = colors.accentPrimary,
                    onClick = { showPaymentDialog = true },
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)

        when (activeTab) {
            FinanceTab.RECEIVABLES -> StateContainer(state = receivablesState) { list ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                ) {
                    items(list, key = { it.id }) { r ->
                        LedgerRowCard(
                            title = r.description ?: "Receivable",
                            subtitle = "Due: ${r.dueDate ?: "Unscheduled"}",
                            amountText = formatNpr(r.amountDue),
                            badge = r.status,
                        )
                    }
                }
            }
            FinanceTab.PAYMENTS -> StateContainer(state = paymentsState) { list ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                ) {
                    items(list, key = { it.id }) { p ->
                        LedgerRowCard(
                            title = p.receiptNo ?: "Receipt",
                            subtitle = "${p.paymentMethod} • ${p.paymentDate.take(10)}",
                            amountText = formatNpr(p.amount),
                            badge = "posted",
                        )
                    }
                }
            }
            FinanceTab.ACCOUNTS -> StateContainer(state = accountsState) { list ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                ) {
                    items(list, key = { it.id }) { a ->
                        LedgerRowCard(
                            title = a.name,
                            subtitle = "${a.accountType.uppercase()} • ${a.id.take(8)}",
                            amountText = formatNpr(a.openingBalance),
                            badge = if (a.active) "active" else "inactive",
                        )
                    }
                }
            }
            FinanceTab.EXPENSES -> StateContainer(state = expensesState) { list ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                ) {
                    items(list, key = { it.id }) { e ->
                        LedgerRowCard(
                            title = e.description,
                            subtitle = "${e.vendor ?: "Internal"} • ${e.expenseDate}",
                            amountText = formatNpr(e.amount),
                            badge = e.status,
                        )
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        RecordPaymentDialog(
            onDismiss = { showPaymentDialog = false },
            onSubmit = { accountId, amount, method, notes, onErr ->
                scope.launch {
                    repo.recordPaymentAtomic(
                        schoolId = null,
                        accountId = accountId,
                        amount = amount,
                        paymentMethod = method,
                        reference = null,
                        notes = notes,
                    )
                        .onSuccess {
                            showPaymentDialog = false
                            activeTab = FinanceTab.PAYMENTS
                            reload()
                        }
                        .onFailure { onErr(it.message ?: "Payment failed") }
                }
            },
        )
    }

    if (showTransferDialog) {
        TransferDialog(
            onDismiss = { showTransferDialog = false },
            onSubmit = { fromId, toId, amount, notes, onErr ->
                scope.launch {
                    repo.transferBetweenAccounts(fromId, toId, amount, null, notes)
                        .onSuccess {
                            showTransferDialog = false
                            activeTab = FinanceTab.ACCOUNTS
                            reload()
                        }
                        .onFailure { onErr(it.message ?: "Transfer failed") }
                }
            },
        )
    }
}

@Composable
private fun LedgerRowCard(title: String, subtitle: String, amountText: String, badge: String) {
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
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(amountText, style = MaterialTheme.typography.labelSmall, color = colors.textPrimary)
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accentSecondary,
                    modifier = Modifier
                        .background(colors.accentSecondary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun RecordPaymentDialog(
    onDismiss: () -> Unit,
    onSubmit: (accountId: String, amount: Double, method: String, notes: String?, onError: (String) -> Unit) -> Unit,
) {
    val colors = LocalMindSparqColors.current
    var accountId by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("bank_transfer") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = accountId, onValueChange = { accountId = it; errorMsg = null }, label = { Text("Account UUID *") }, singleLine = true)
                OutlinedTextField(value = amountText, onValueChange = { amountText = it; errorMsg = null }, label = { Text("Amount (NPR) *") }, singleLine = true)
                OutlinedTextField(value = method, onValueChange = { method = it }, label = { Text("Method (cash/bank_transfer/qr)") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
                if (errorMsg != null) {
                    Text(errorMsg!!, style = MaterialTheme.typography.labelMedium, color = colors.statusDanger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorMsg = "Enter a valid positive amount."
                    } else {
                        onSubmit(accountId.trim(), amt, method.trim(), notes.trim().ifBlank { null }) { errorMsg = it }
                    }
                },
                enabled = accountId.isNotBlank() && amountText.isNotBlank(),
            ) {
                Text("Post Receipt")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun TransferDialog(
    onDismiss: () -> Unit,
    onSubmit: (fromId: String, toId: String, amount: Double, notes: String?, onError: (String) -> Unit) -> Unit,
) {
    val colors = LocalMindSparqColors.current
    var fromId by remember { mutableStateOf("") }
    var toId by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transfer Between Accounts") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = fromId, onValueChange = { fromId = it; errorMsg = null }, label = { Text("From Account UUID *") }, singleLine = true)
                OutlinedTextField(value = toId, onValueChange = { toId = it; errorMsg = null }, label = { Text("To Account UUID *") }, singleLine = true)
                OutlinedTextField(value = amountText, onValueChange = { amountText = it; errorMsg = null }, label = { Text("Amount (NPR) *") }, singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") })
                if (errorMsg != null) {
                    Text(errorMsg!!, style = MaterialTheme.typography.labelMedium, color = colors.statusDanger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorMsg = "Enter a valid positive amount."
                    } else {
                        onSubmit(fromId.trim(), toId.trim(), amt, notes.trim().ifBlank { null }) { errorMsg = it }
                    }
                },
                enabled = fromId.isNotBlank() && toId.isNotBlank() && amountText.isNotBlank(),
            ) {
                Text("Execute Transfer")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
