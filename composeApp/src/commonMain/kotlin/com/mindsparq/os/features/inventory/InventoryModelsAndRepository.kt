package com.mindsparq.os.features.inventory

import com.mindsparq.os.core.network.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.random.Random

@Serializable
data class ProductDto(
    val id: String,
    val sku: String? = null,
    val name: String,
    val category: String? = null,
    val unit: String = "piece",
    @SerialName("minimum_stock") val minimumStock: Double = 0.0,
    val active: Boolean = true,
)

@Serializable
data class ProductCreateRequest(
    val sku: String? = null,
    val name: String,
    val category: String? = null,
    val unit: String = "piece",
    @SerialName("minimum_stock") val minimumStock: Double = 0.0,
)

@Serializable
data class StockMovementDto(
    val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("location_id") val locationId: String,
    @SerialName("movement_type") val movementType: String,
    val quantity: Double,
    @SerialName("movement_date") val movementDate: String,
    val notes: String? = null,
)

@Serializable
data class PurchaseOrderDto(
    val id: String,
    @SerialName("po_number") val poNumber: String,
    @SerialName("order_date") val orderDate: String,
    val status: String = "draft",
    @SerialName("total_amount") val totalAmount: Double = 0.0,
)

@Serializable
data class DistributionDto(
    val id: String,
    @SerialName("distribution_no") val distributionNo: String,
    @SerialName("recipient_name") val recipientName: String? = null,
    @SerialName("distributed_at") val distributedAt: String,
    val status: String = "issued",
)

class InventoryRepository {
    suspend fun listProducts(): Result<List<ProductDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("products")
            .select { order("name", Order.ASCENDING) }
            .decodeList<ProductDto>()
    }

    suspend fun createProduct(request: ProductCreateRequest): Result<ProductDto> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        require(request.name.isNotBlank()) { "Product name is required." }
        client.from("products")
            .insert(request) { select() }
            .decodeSingle<ProductDto>()
    }

    suspend fun listMovements(): Result<List<StockMovementDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("stock_movements")
            .select { order("movement_date", Order.DESCENDING) }
            .decodeList<StockMovementDto>()
    }

    suspend fun listPurchaseOrders(): Result<List<PurchaseOrderDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("purchase_orders")
            .select { order("order_date", Order.DESCENDING) }
            .decodeList<PurchaseOrderDto>()
    }

    suspend fun listDistributions(): Result<List<DistributionDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("distributions")
            .select { order("distributed_at", Order.DESCENDING) }
            .decodeList<DistributionDto>()
    }

    suspend fun distributeStockAtomic(
        sourceLocationId: String,
        schoolId: String?,
        recipientName: String,
        productId: String,
        quantity: Double,
        notes: String?,
        idempotencyKey: String = "dst-${Random.nextLong()}",
    ): Result<String> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        require(quantity > 0.0) { "Quantity must be greater than 0." }
        val lines = buildJsonArray {
            add(
                buildJsonObject {
                    put("product_id", productId)
                    put("quantity", quantity)
                },
            )
        }
        val params = buildJsonObject {
            put("p_source_location_id", sourceLocationId)
            put("p_school_id", schoolId)
            put("p_program_id", null as String?)
            put("p_recipient_name", recipientName)
            put("p_notes", notes)
            put("p_idempotency_key", idempotencyKey)
            put("p_lines", lines)
        }
        client.postgrest.rpc("distribute_stock_atomic", params).data
    }
}
