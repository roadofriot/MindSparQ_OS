package com.mindsparq.os.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mindsparq.os.core.theme.LocalMindSparqColors

/**
 * Compact icon-only action button with hover/long-press tooltip revelation.
 * Enforces clean, minimal chrome with zero long text labels on buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindSparqIconButton(
    icon: ImageVector,
    tooltip: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shortcutHint: String? = null,
    selected: Boolean = false,
    outlined: Boolean = false,
    size: Dp = 38.dp,
    iconSize: Dp = 19.dp,
    tint: Color? = null,
    enabled: Boolean = true,
) {
    val colors = LocalMindSparqColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val bg = when {
        selected -> colors.accentPrimary.copy(alpha = 0.14f)
        isHovered && enabled -> colors.bgElevated
        outlined -> colors.bgSurface
        else -> Color.Transparent
    }
    val resolvedTint = when {
        !enabled -> colors.textSecondary.copy(alpha = 0.45f)
        tint != null -> tint
        selected -> colors.accentPrimary
        isHovered -> colors.textPrimary
        else -> colors.textSecondary
    }
    val borderColor = when {
        selected -> colors.accentPrimary.copy(alpha = 0.4f)
        outlined || isHovered -> colors.borderHairline
        else -> Color.Transparent
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip(
                containerColor = colors.bgElevated,
                contentColor = colors.textPrimary,
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = tooltip,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    if (shortcutHint != null) {
                        Text(
                            text = shortcutHint,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            modifier = Modifier
                                .background(colors.bgSurface, RoundedCornerShape(4.dp))
                                .border(1.dp, colors.borderHairline, RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                        )
                    }
                }
            }
        },
        state = rememberTooltipState(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(10.dp))
                .background(bg)
                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .hoverable(interactionSource = interactionSource, enabled = enabled)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .semantics { contentDescription = tooltip },
        ) {
            Icon(
                imageVector = icon,
                contentDescription = tooltip,
                tint = resolvedTint,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

/**
 * Geometric M-Spark brand mark rendered crisply at any DPI.
 */
@Composable
fun MindSparqBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val corner = w * 0.23f

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF0B0F17), Color(0xFF131A26)),
            ),
            cornerRadius = CornerRadius(corner, corner),
        )
        drawRoundRect(
            color = Color(0xFF1E293B),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = w * 0.03f),
        )

        val mPath = Path().apply {
            moveTo(w * 0.25f, h * 0.72f)
            lineTo(w * 0.25f, h * 0.35f)
            lineTo(w * 0.50f, h * 0.56f)
            lineTo(w * 0.75f, h * 0.35f)
            lineTo(w * 0.75f, h * 0.72f)
        }
        drawPath(
            path = mPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF3B82F6), Color(0xFF14B8A6)),
                start = Offset(w * 0.2f, h * 0.2f),
                end = Offset(w * 0.8f, h * 0.8f),
            ),
            style = Stroke(
                width = w * 0.085f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        val sparkPath = Path().apply {
            moveTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.53f, h * 0.25f)
            lineTo(w * 0.62f, h * 0.28f)
            lineTo(w * 0.53f, h * 0.31f)
            lineTo(w * 0.50f, h * 0.40f)
            lineTo(w * 0.47f, h * 0.31f)
            lineTo(w * 0.38f, h * 0.28f)
            lineTo(w * 0.47f, h * 0.25f)
            close()
        }
        drawPath(path = sparkPath, color = Color(0xFF14B8A6))
    }
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Empty(val title: String, val subtitle: String) : UiState<Nothing>
    data class Error(val message: String, val onRetry: (() -> Unit)? = null) : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
}

@Composable
fun <T> StateContainer(
    state: UiState<T>,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    val colors = LocalMindSparqColors.current
    when (state) {
        is UiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = colors.accentPrimary,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        is UiState.Empty -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(
                    color = colors.bgSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.borderHairline),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inbox,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(32.dp),
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(state.title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(state.subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                    }
                }
            }
        }
        is UiState.Error -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Surface(
                    color = colors.bgSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colors.statusDanger.copy(alpha = 0.35f)),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(20.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = colors.statusDanger,
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (state.onRetry != null) {
                            Spacer(Modifier.width(12.dp))
                            MindSparqIconButton(
                                icon = Icons.Outlined.Refresh,
                                tooltip = "Retry",
                                onClick = state.onRetry,
                                outlined = true,
                            )
                        }
                    }
                }
            }
        }
        is UiState.Success -> content(state.data)
    }
}
