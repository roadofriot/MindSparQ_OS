package com.mindsparq.os.features.users

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
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
import com.mindsparq.os.core.auth.RoleCode
import com.mindsparq.os.core.auth.UserProfileDto
import com.mindsparq.os.core.network.SupabaseConfig
import com.mindsparq.os.core.theme.LocalMindSparqColors
import com.mindsparq.os.core.ui.MindSparqIconButton
import com.mindsparq.os.core.ui.StateContainer
import com.mindsparq.os.core.ui.UiState
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
private data class UserRoleAssignmentDto(
    @SerialName("user_id") val userId: String,
    val roles: RoleCodeOnly? = null,
)

@Serializable
private data class RoleCodeOnly(val code: String)

data class ManagedUserItem(
    val profile: UserProfileDto,
    val assignedRoles: Set<RoleCode>,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UsersAndRolesScreen() {
    val colors = LocalMindSparqColors.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UiState<List<ManagedUserItem>>>(UiState.Loading) }
    var actionError by remember { mutableStateOf<String?>(null) }

    fun reload() {
        scope.launch {
            state = UiState.Loading
            actionError = null
            runCatching {
                val client = SupabaseConfig.client ?: error("Supabase is not configured.")
                val profiles = client.from("profiles")
                    .select { order("created_at", Order.DESCENDING) }
                    .decodeList<UserProfileDto>()
                val roleLinks = client.from("user_roles")
                    .select(Columns.raw("user_id, roles(code)"))
                    .decodeList<UserRoleAssignmentDto>()

                val rolesByUser = roleLinks.groupBy { it.userId }.mapValues { (_, rows) ->
                    rows.mapNotNull { it.roles?.code?.let(RoleCode::fromDb) }.toSet()
                }
                profiles.map { p ->
                    ManagedUserItem(
                        profile = p,
                        assignedRoles = rolesByUser[p.id] ?: emptySet(),
                    )
                }
            }
                .onSuccess { list ->
                    state = if (list.isEmpty()) {
                        UiState.Empty("No Users", "Authenticated users will appear here once they sign in.")
                    } else UiState.Success(list)
                }
                .onFailure { state = UiState.Error(it.message ?: "Failed to load users") { reload() } }
        }
    }

    fun toggleRole(userId: String, role: RoleCode, currentlyAssigned: Boolean) {
        scope.launch {
            actionError = null
            runCatching {
                val client = SupabaseConfig.client ?: error("Supabase is not configured.")
                val params = buildJsonObject {
                    put("p_target_user_id", userId)
                    put("p_role_code", role.dbCode)
                    put("p_assign", !currentlyAssigned)
                }
                client.postgrest.rpc("assign_user_role", params)
            }
                .onSuccess { reload() }
                .onFailure { actionError = it.message ?: "Failed to update role" }
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
                MindSparqIconButton(
                    icon = Icons.Outlined.AdminPanelSettings,
                    tooltip = "Super Admin Role Assignment",
                    selected = true,
                    onClick = {},
                )
                Text("Users & Role Assignment", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            }
            MindSparqIconButton(
                icon = Icons.Outlined.Refresh,
                tooltip = "Refresh Users",
                outlined = true,
                onClick = { reload() },
            )
        }
        HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)

        if (actionError != null) {
            Text(
                text = actionError!!,
                color = colors.statusDanger,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        StateContainer(state = state) { users ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize().padding(16.dp),
            ) {
                items(users, key = { it.profile.id }) { item ->
                    Surface(
                        color = colors.bgSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.borderHairline),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column {
                                    Text(item.profile.fullName, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                    Text(item.profile.email ?: item.profile.id, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                                }
                                Text(
                                    text = if (item.assignedRoles.isEmpty()) "pending_role" else item.profile.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (item.assignedRoles.isEmpty()) colors.statusWarning else colors.statusSuccess,
                                    modifier = Modifier
                                        .background(
                                            (if (item.assignedRoles.isEmpty()) colors.statusWarning else colors.statusSuccess).copy(alpha = 0.12f),
                                            RoundedCornerShape(6.dp),
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                RoleCode.entries.forEach { role ->
                                    val active = role in item.assignedRoles
                                    Text(
                                        text = role.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (active) colors.accentPrimary else colors.textSecondary,
                                        modifier = Modifier
                                            .background(
                                                if (active) colors.accentPrimary.copy(alpha = 0.14f) else colors.bgElevated,
                                                RoundedCornerShape(8.dp),
                                            )
                                            .clickable { toggleRole(item.profile.id, role, active) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
