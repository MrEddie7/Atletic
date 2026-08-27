package com.eddie.atletic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eddie.atletic.data.model.RenewalInfo
import com.eddie.atletic.data.model.RenewalState

@Composable
fun RenewalStatusBadge(
    renewalInfo: RenewalInfo,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (backgroundColor, contentColor, icon) = when (renewalInfo.state) {
        RenewalState.ACTIVE -> Triple(
            Color(0xFF10B981).copy(alpha = 0.15f),
            Color(0xFF10B981),
            Icons.Default.CheckCircle
        )
        RenewalState.EXPIRING_SOON -> Triple(
            Color(0xFFF97316).copy(alpha = 0.15f),
            Color(0xFFF97316),
            Icons.Default.Warning
        )
        RenewalState.EXPIRED -> Triple(
            Color(0xFFEF4444).copy(alpha = 0.15f),
            Color(0xFFEF4444),
            Icons.Default.Error
        )
    }

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, contentColor.copy(alpha = 0.4f), shape)
            .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 3.dp else 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = renewalInfo.state.title,
                tint = contentColor,
                modifier = Modifier.size(if (compact) 12.dp else 14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = renewalInfo.badgeText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = if (compact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor
            )
        }
    }
}

