package com.mindsparq.os.core.auth

import com.mindsparq.os.core.network.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

enum class RoleCode(val dbCode: String, val displayName: String) {
    SUPER_ADMIN("super_admin", "Super Admin"),
    ADMIN("admin", "Admin"),
    MANAGEMENT("management", "Management"),
    FINANCE("finance", "Finance"),
    INVENTORY("inventory", "Inventory"),
    TEACHER("teacher", "Instructor / Teacher"),
    AUDITOR("auditor", "Auditor");

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
    USERS,
    SETTINGS,
}

fun allowedDestinations(roles: Set<RoleCode>, isDesktop: Boolean): List<AppDestination> {
    if (roles.isEmpty()) return emptyList()
    val full = if (RoleCode.SUPER_ADMIN in roles) {
        AppDestination.entries
    } else if (RoleCode.ADMIN in roles) {
        AppDestination.entries.filter { it != AppDestination.USERS }
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
    return if (isDesktop) {
        full
    } else {
        full.filter {
            it in setOf(
                AppDestination.DASHBOARD,
                AppDestination.SCHOOLS,
                AppDestination.FIELD_OPS,
                AppDestination.USERS,
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
    @SerialName("avatar_path") val avatarPath: String? = null,
    @SerialName("preferred_language") val preferredLanguage: String = "en",
    val status: String = "active",
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
    val avatarUrl: String?,
    val roles: Set<RoleCode>,
)

class AuthRepository {
    suspend fun signIn(email: String, password: String): Result<AuthenticatedSession> = runCatching {
        val client = SupabaseConfig.client
            ?: error("Supabase is not configured.")
        client.auth.signInWith(Email) {
            this.email = email.trim()
            this.password = password
        }
        loadCurrentSession() ?: error("Authenticated user profile could not be loaded.")
    }

    suspend fun signUp(fullName: String, email: String, password: String): Result<AuthenticatedSession?> = runCatching {
        val client = SupabaseConfig.client
            ?: error("Supabase is not configured.")
        require(fullName.isNotBlank()) { "Full name is required." }
        require(password.length >= 6) { "Password must be at least 6 characters." }
        client.auth.signUpWith(Email) {
            this.email = email.trim()
            this.password = password
            this.data = buildJsonObject {
                put("full_name", fullName.trim())
            }
        }
        loadCurrentSession()
    }

    suspend fun signInWithGoogle(): Result<Unit> = runCatching {
        val client = SupabaseConfig.client
            ?: error("Supabase is not configured.")
        client.auth.signInWith(Google)
    }

    suspend fun loadCurrentSession(): AuthenticatedSession? {
        val client = SupabaseConfig.client ?: return null
        val user = client.auth.currentUserOrNull() ?: return null
        val profile = client.from("profiles")
            .select { filter { eq("id", user.id) } }
            .decodeSingleOrNull<UserProfileDto>()
        val roleRows = client.from("user_roles")
            .select(Columns.raw("role_id, roles(code)")) {
                filter { eq("user_id", user.id) }
            }
            .decodeList<UserRoleRow>()

        val roles = roleRows.mapNotNull { it.roles?.code?.let(RoleCode::fromDb) }.toSet()
        return AuthenticatedSession(
            userId = user.id,
            fullName = profile?.fullName ?: (user.email ?: "User"),
            email = profile?.email ?: (user.email ?: ""),
            avatarUrl = profile?.avatarPath,
            roles = roles,
        )
    }

    suspend fun signOut() {
        SupabaseConfig.client?.auth?.signOut()
    }
}
