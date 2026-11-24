package com.mindsparq.os.features.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindsparq.os.core.auth.AuthenticatedSession
import com.mindsparq.os.core.i18n.AppLanguage
import com.mindsparq.os.core.i18n.CalendarSystem
import com.mindsparq.os.core.theme.LocalMindSparqColors
import com.mindsparq.os.core.theme.ThemeMode
import com.mindsparq.os.core.ui.MindSparqIconButton

@Composable
fun SettingsScreen(
    session: AuthenticatedSession,
    themeMode: ThemeMode,
    calendar: CalendarSystem,
    onToggleTheme: () -> Unit,
    onToggleCalendar: () -> Unit,
    onToggleLanguage: () -> Unit,
    onSignOut: () -> Unit,
) {
    val colors = LocalMindSparqColors.current
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Surface(
            color = colors.bgSurface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, colors.borderHairline),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(session.fullName, style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(session.email, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Roles: ${session.roles.joinToString(", ") { it.dbCode }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accentSecondary,
                )
            }
        }

        Surface(
            color = colors.bgSurface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, colors.borderHairline),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp),
            ) {
                Text("Preferences & Session", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MindSparqIconButton(
                        icon = if (themeMode == ThemeMode.DARK) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        tooltip = "Toggle Day / Night Theme",
                        outlined = true,
                        onClick = onToggleTheme,
                    )
                    MindSparqIconButton(
                        icon = Icons.Outlined.CalendarMonth,
                        tooltip = "Calendar: ${calendar.name} (Click to switch)",
                        outlined = true,
                        onClick = onToggleCalendar,
                    )
                    MindSparqIconButton(
                        icon = Icons.Outlined.Translate,
                        tooltip = "Switch English / Nepali",
                        outlined = true,
                        onClick = onToggleLanguage,
                    )
                    MindSparqIconButton(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        tooltip = "Sign Out",
                        outlined = true,
                        tint = colors.statusDanger,
                        onClick = onSignOut,
                    )
                }
            }
        }
    }
}
