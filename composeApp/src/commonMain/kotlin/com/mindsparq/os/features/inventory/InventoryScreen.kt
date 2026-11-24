package com.mindsparq.os.features.inventory

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SwapVert
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

private enum class InventoryTab { PRODUCTS, LEDGER, PURCHASE_ORDERS, DISTRIBUTIONS }

@Composable
fun InventoryScreen() {
    val colors = LocalMindSparqColors.current
    val repo = remember { InventoryRepository() }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(InventoryTab.PRODUCTS) }
    var productsState by remember { mutableStateOf<UiState<List<ProductDto>>>(UiState.Loading) }
    var movementsState by remember { mutableStateOf<UiState<List<StockMovementDto>>>(UiState.Loading) }
    var poState by remember { mutableStateOf<UiState<List<PurchaseOrderDto>>>(UiState.Loading) }
    var distState by remember { mutableStateOf<UiState<List<DistributionDto>>>(UiState.Loading) }
    var showCreateProduct by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            when (activeTab) {
                InventoryTab.PRODUCTS -> {
                    productsState = UiState.Loading
                    repo.listProducts()
                        .onSuccess { list ->
                            productsState = if (list.isEmpty()) {
                                UiState.Empty("No Products", "Add books, abacus kits, or learning materials with +.")
                            } else UiState.Success(list)
                        }
                        .onFailure { productsState = UiState.Error(it.message ?: "Failed to load products") { reload() } }
                }
                InventoryTab.LEDGER -> {
                    movementsState = UiState.Loading
                    repo.listMovements()
                        .onSuccess { list ->
                            movementsState = if (list.isEmpty()) {
                                UiState.Empty("No Stock Movements", "Stock ledger is append-only and currently empty.")
                            } else UiState.Success(list)
                        }
                        .onFailure { movementsState = UiState.Error(it.message ?: "Failed to load stock ledger") { reload() } }
                }
                InventoryTab.PURCHASE_ORDERS -> {
                    poState = UiState.Loading
                    repo.listPurchaseOrders()
                        .onSuccess { list ->
                            poState = if (list.isEmpty()) {
                                UiState.Empty("No Purchase Orders", "No supplier purchase orders recorded yet.")
                            } else UiState.Success(list)
                        }
                        .onFailure { poState = UiState.Error(it.message ?: "Failed to load purchase orders") { reload() } }
                }
                InventoryTab.DISTRIBUTIONS -> {
                    distState = UiState.Loading
                    repo.listDistributions()
                        .onSuccess { list ->
                            distState = if (list.isEmpty()) {
                                UiState.Empty("No Distributions", "No kit distributions to schools recorded yet.")
                            } else UiState.Success(list)
                        }
                        .onFailure { distState = UiState.Error(it.message ?: "Failed to load distributions") { reload() } }
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
                    icon = Icons.Outlined.Category,
                    tooltip = "Product Catalog",
                    selected = activeTab == InventoryTab.PRODUCTS,
                    onClick = { activeTab = InventoryTab.PRODUCTS },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.SwapVert,
                    tooltip = "Stock Movement Ledger",
                    selected = activeTab == InventoryTab.LEDGER,
                    onClick = { activeTab = InventoryTab.LEDGER },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.ShoppingCart,
                    tooltip = "Purchase Orders",
                    selected = activeTab == InventoryTab.PURCHASE_ORDERS,
                    onClick = { activeTab = InventoryTab.PURCHASE_ORDERS },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.LocalShipping,
                    tooltip = "Distributions",
                    selected = activeTab == InventoryTab.DISTRIBUTIONS,
                    onClick = { activeTab = InventoryTab.DISTRIBUTIONS },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MindSparqIconButton(
                    icon = Icons.Outlined.Refresh,
                    tooltip = "Refresh",
                    outlined = true,
                    onClick = { reload() },
                )
                if (activeTab == InventoryTab.PRODUCTS) {
                    MindSparqIconButton(
                        icon = Icons.Outlined.Add,
                        tooltip = "New Product",
                        outlined = true,
                        tint = colors.accentPrimary,
                        onClick = { showCreateProduct = true },
                    )
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)

        when (activeTab) {
            InventoryTab.PRODUCTS -> StateContainer(state = productsState) { list ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    items(list, key = { it.id }) { p ->
                        InventoryRowCard(
                            title = p.name,
                            subtitle = listOfNotNull(p.sku, p.category, p.unit).joinToString(" • "),
                            trailing = "Min: ${p.minimumStock}",
                            badge = if (p.active) "active" else "inactive",
                        )
                    }
                }
            }
            InventoryTab.LEDGER -> StateContainer(state = movementsState) { list ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    items(list, key = { it.id }) { m ->
                        InventoryRowCard(
                            title = m.movementType.uppercase(),
                            subtitle = m.notes ?: m.movementDate.take(10),
                            trailing = "${m.quantity}",
                            badge = m.movementDate.take(10),
                        )
                    }
                }
            }
            InventoryTab.PURCHASE_ORDERS -> StateContainer(state = poState) { list ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    items(list, key = { it.id }) { po ->
                        InventoryRowCard(
                            title = po.poNumber,
                            subtitle = "Order Date: ${po.orderDate}",
                            trailing = formatNpr(po.totalAmount),
                            badge = po.status,
                        )
                    }
                }
            }
            InventoryTab.DISTRIBUTIONS -> StateContainer(state = distState) { list ->
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    items(list, key = { it.id }) { d ->
                        InventoryRowCard(
                            title = d.distributionNo,
                            subtitle = d.recipientName ?: "School Distribution",
                            trailing = d.distributedAt.take(10),
                            badge = d.status,
                        )
                    }
                }
            }
        }
    }

    if (showCreateProduct) {
        CreateProductDialog(
            onDismiss = { showCreateProduct = false },
            onSubmit = { req, onErr ->
                scope.launch {
                    repo.createProduct(req)
                        .onSuccess {
                            showCreateProduct = false
                            reload()
                        }
                        .onFailure { onErr(it.message ?: "Failed to create product") }
                }
            },
        )
    }
}

@Composable
private fun InventoryRowCard(title: String, subtitle: String, trailing: String, badge: String) {
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
                Text(trailing, style = MaterialTheme.typography.labelSmall, color = colors.textPrimary)
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
private fun CreateProductDialog(
    onDismiss: () -> Unit,
    onSubmit: (ProductCreateRequest, onError: (String) -> Unit) -> Unit,
) {
    val colors = LocalMindSparqColors.current
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("piece") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it; errorMsg = null }, label = { Text("Product Name *") }, singleLine = true)
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU") }, singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, singleLine = true)
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (piece/kit/book)") }, singleLine = true)
                if (errorMsg != null) {
                    Text(errorMsg!!, style = MaterialTheme.typography.labelMedium, color = colors.statusDanger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        ProductCreateRequest(
                            name = name.trim(),
                            sku = sku.trim().ifBlank { null },
                            category = category.trim().ifBlank { null },
                            unit = unit.trim().ifBlank { "piece" },
                        ),
                    ) { errorMsg = it }
                },
                enabled = name.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
