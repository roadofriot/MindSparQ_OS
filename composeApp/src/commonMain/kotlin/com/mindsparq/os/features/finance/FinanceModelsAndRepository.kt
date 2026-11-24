package com.mindsparq.os.features.finance

import com.mindsparq.os.core.network.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.random.Random

@Serializable
data class ReceivableDto(
    val id: String,
    @SerialName("school_id") val schoolId: String,
    val description: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("amount_due") val amountDue: Double,
    val currency: String = "NPR",
    val status: String = "pending",
)

@Serializable
data class PaymentDto(
    val id: String,
    @SerialName("receipt_no") val receiptNo: String? = null,
    @SerialName("school_id") val schoolId: String? = null,
    @SerialName("account_id") val accountId: String,
    val amount: Double,
    val currency: String = "NPR",
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("payment_date") val paymentDate: String,
    val notes: String? = null,
)

@Serializable
data class AccountDto(
    val id: String,
    val name: String,
    @SerialName("account_type") val accountType: String,
    val currency: String = "NPR",
    @SerialName("opening_balance") val openingBalance: Double = 0.0,
    val active: Boolean = true,
)

@Serializable
data class ExpenseDto(
    val id: String,
    val description: String,
    val amount: Double,
    val currency: String = "NPR",
    @SerialName("expense_date") val expenseDate: String,
    val vendor: String? = null,
    val status: String = "pending",
)

class FinanceRepository {
    suspend fun listReceivables(): Result<List<ReceivableDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("receivables")
            .select { order("due_date", Order.ASCENDING) }
            .decodeList<ReceivableDto>()
    }

    suspend fun listPayments(): Result<List<PaymentDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("payments")
            .select { order("payment_date", Order.DESCENDING) }
            .decodeList<PaymentDto>()
    }

    suspend fun listAccounts(): Result<List<AccountDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("accounts")
            .select { order("name", Order.ASCENDING) }
            .decodeList<AccountDto>()
    }

    suspend fun listExpenses(): Result<List<ExpenseDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("expenses")
            .select { order("expense_date", Order.DESCENDING) }
            .decodeList<ExpenseDto>()
    }

    suspend fun recordPaymentAtomic(
        schoolId: String?,
        accountId: String,
        amount: Double,
        paymentMethod: String,
        reference: String?,
        notes: String?,
        idempotencyKey: String = "pay-${Random.nextLong()}",
    ): Result<String> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        require(amount > 0.0) { "Payment amount must be greater than 0." }
        require(accountId.isNotBlank()) { "Account ID is required." }

        val params = buildJsonObject {
            if (schoolId != null) put("p_school_id", schoolId) else put("p_school_id", null as String?)
            put("p_account_id", accountId)
            put("p_amount", amount)
            put("p_payment_method", paymentMethod)
            put("p_transaction_reference", reference)
            put("p_notes", notes)
            put("p_idempotency_key", idempotencyKey)
            put("p_allocations", JsonArray(emptyList()))
        }
        client.postgrest.rpc("record_payment_atomic", params).data
    }

    suspend fun transferBetweenAccounts(
        fromAccountId: String,
        toAccountId: String,
        amount: Double,
        referenceNo: String?,
        notes: String?,
        idempotencyKey: String = "trf-${Random.nextLong()}",
    ): Result<String> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        require(fromAccountId != toAccountId) { "Source and destination accounts must differ." }
        require(amount > 0.0) { "Transfer amount must be greater than 0." }

        val params = buildJsonObject {
            put("p_from_account_id", fromAccountId)
            put("p_to_account_id", toAccountId)
            put("p_amount", amount)
            put("p_reference_no", referenceNo)
            put("p_notes", notes)
            put("p_idempotency_key", idempotencyKey)
        }
        client.postgrest.rpc("transfer_between_accounts", params).data
    }
}
