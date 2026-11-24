package com.mindsparq.os.features.schools

import com.mindsparq.os.core.network.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class SchoolStatus(val dbValue: String, val label: String) {
    PROSPECT("prospect", "Prospect"),
    NEGOTIATION("negotiation", "Negotiation"),
    CONTRACT_PENDING("contract_pending", "Contract Pending"),
    ACTIVE("active", "Active"),
    INACTIVE("inactive", "Inactive"),
    SUSPENDED("suspended", "Suspended"),
    EXPIRED("expired", "Expired");

    companion object {
        fun fromDb(value: String): SchoolStatus =
            entries.firstOrNull { it.dbValue == value } ?: PROSPECT
    }
}

@Serializable
data class SchoolDto(
    val id: String,
    @SerialName("school_code") val schoolCode: String? = null,
    val name: String,
    @SerialName("school_type") val schoolType: String? = null,
    val status: String = "prospect",
    val address: String? = null,
    val district: String? = null,
    val municipality: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("principal_name") val principalName: String? = null,
    @SerialName("coordinator_name") val coordinatorName: String? = null,
    val notes: String? = null,
)

@Serializable
data class SchoolCreateRequest(
    @SerialName("school_code") val schoolCode: String? = null,
    val name: String,
    @SerialName("school_type") val schoolType: String? = null,
    val status: String = "prospect",
    val address: String? = null,
    val district: String? = null,
    val municipality: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("principal_name") val principalName: String? = null,
)

@Serializable
data class SchoolContactDto(
    val id: String,
    @SerialName("school_id") val schoolId: String,
    val name: String,
    val designation: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("is_primary") val isPrimary: Boolean = false,
)

@Serializable
data class SchoolContactCreateRequest(
    @SerialName("school_id") val schoolId: String,
    val name: String,
    val designation: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("is_primary") val isPrimary: Boolean = false,
)

@Serializable
data class SchoolClassDto(
    val id: String,
    @SerialName("school_id") val schoolId: String,
    val name: String,
    @SerialName("sort_order") val sortOrder: Int? = null,
)

class SchoolRepository {
    suspend fun listSchools(
        query: String = "",
        statusFilter: SchoolStatus? = null,
    ): Result<List<SchoolDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        val all = client.from("schools")
            .select {
                if (statusFilter != null) {
                    filter { eq("status", statusFilter.dbValue) }
                }
                order("name", Order.ASCENDING)
            }
            .decodeList<SchoolDto>()

        val q = query.trim().lowercase()
        if (q.isEmpty()) all
        else all.filter {
            it.name.lowercase().contains(q) ||
                (it.schoolCode?.lowercase()?.contains(q) == true) ||
                (it.district?.lowercase()?.contains(q) == true)
        }
    }

    suspend fun createSchool(request: SchoolCreateRequest): Result<SchoolDto> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        require(request.name.isNotBlank()) { "School name is required." }
        client.from("schools")
            .insert(request) { select() }
            .decodeSingle<SchoolDto>()
    }

    suspend fun listContacts(schoolId: String): Result<List<SchoolContactDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("school_contacts")
            .select {
                filter { eq("school_id", schoolId) }
                order("is_primary", Order.DESCENDING)
            }
            .decodeList<SchoolContactDto>()
    }

    suspend fun addContact(request: SchoolContactCreateRequest): Result<SchoolContactDto> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        require(request.name.isNotBlank()) { "Contact name is required." }
        client.from("school_contacts")
            .insert(request) { select() }
            .decodeSingle<SchoolContactDto>()
    }

    suspend fun listClasses(schoolId: String): Result<List<SchoolClassDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("school_classes")
            .select {
                filter { eq("school_id", schoolId) }
                order("sort_order", Order.ASCENDING)
            }
            .decodeList<SchoolClassDto>()
    }
}
