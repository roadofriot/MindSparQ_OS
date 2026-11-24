package com.mindsparq.os.core.auth

import com.mindsparq.os.core.network.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class RoleCode(val dbCode: String) {
    SUPER_ADMIN("super_admin"),
    ADMIN("admin"),
    MANAGEMENT("management"),
    FINANCE("finance"),
    INVENTORY("inventory"),
    TEACHER("teacher"),
    AUDITOR("auditor");

    companion object {
        fun fromDb(code: String): RoleCode? = entries.firstOrNull { it.dbCode == code }
    }
}

enum class AppDestination {
    DASHBOARD,
    SCHOOLS,
    ACADEMIC,
    FINANCE,
    INVENTORY,
    FIELD_OPS,
    REPORTS,
    SETTINGS,
}

fun allowedDestinations(roles: Set<RoleCode>, isDesktop: Boolean): List<AppDestination> {
    if (roles.isEmpty()) return emptyList()
    val full = if (RoleCode.SUPER_ADMIN in roles || RoleCode.ADMIN in roles) {
        AppDestination.entries
    } else {
        buildList {
            add(AppDestination.DASHBOARD)
            add(AppDestination.SCHOOLS)
            if (RoleCode.MANAGEMENT in roles || RoleCode.TEACHER in roles || RoleCode.AUDITOR in roles) {
                add(AppDestination.ACADEMIC)
            }
            if (RoleCode.FINANCE in roles || RoleCode.MANAGEMENT in roles || RoleCode.AUDITOR in roles) {
                add(AppDestination.FINANCE)
            }
            if (RoleCode.INVENTORY in roles || RoleCode.MANAGEMENT in roles || RoleCode.AUDITOR in roles) {
                add(AppDestination.INVENTORY)
            }
            add(AppDestination.FIELD_OPS)
            add(AppDestination.REPORTS)
            add(AppDestination.SETTINGS)
        }
    }
    // Mobile (Android) focuses on field/teacher/management workflows without cramming heavy desktop finance/reports
    return if (isDesktop) {
        full
    } else {
        full.filter {
            it in setOf(
                AppDestination.DASHBOARD,
                AppDestination.SCHOOLS,
                AppDestination.FIELD_OPS,
                AppDestination.SETTINGS,
            )
        }
    }
}

@Serializable
data class UserProfileDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String? = null,
    @SerialName("preferred_language") val preferredLanguage: String = "en",
)

@Serializable
private data class UserRoleRow(
    @SerialName("role_id") val roleId: String,
    val roles: RoleCodeDto? = null,
)

@Serializable
private data class RoleCodeDto(val code: String)

data class AuthenticatedSession(
    val userId: String,
    val fullName: String,
    val email: String,
    val roles: Set<RoleCode>,
)

class AuthRepository {
    suspend fun signIn(email: String, password: String): Result<AuthenticatedSession> = runCatching {
        val client = SupabaseConfig.client
            ?: error("Supabase environment variables are not configured.")
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        loadCurrentSession() ?: error("Authenticated user profile could not be loaded.")
    }

    suspend fun loadCurrentSession(): AuthenticatedSession? {
        val client = SupabaseConfig.client ?: return null
        val user = client.auth.currentUserOrNull() ?: return null
        val profile = client.from("profiles")
            .select { filter { eq("id", user.id) } }
            .decodeSingleOrNull<UserProfileDto>()
        val roleRows = client.from("user_roles")
            .select(io.github.jan.supabase.postgrest.query.Columns.raw("role_id, roles(code)")) {
                filter { eq("user_id", user.id) }
            }
            .decodeList<UserRoleRow>()

        val roles = roleRows.mapNotNull { it.roles?.code?.let(RoleCode::fromDb) }.toSet()
        return AuthenticatedSession(
            userId = user.id,
            fullName = profile?.fullName ?: (user.email ?: "User"),
            email = profile?.email ?: (user.email ?: ""),
            roles = roles,
        )
    }

    suspend fun signOut() {
        SupabaseConfig.client?.auth?.signOut()
    }
}
