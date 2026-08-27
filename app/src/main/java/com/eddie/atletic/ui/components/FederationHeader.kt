package com.eddie.atletic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eddie.atletic.data.model.RenewalState
import com.eddie.atletic.ui.theme.BluePrimary
import com.eddie.atletic.ui.theme.GoldAccent
import com.eddie.atletic.ui.theme.GreenActive
import com.eddie.atletic.ui.theme.NavyCard
import com.eddie.atletic.ui.theme.NavyCardElevated
import com.eddie.atletic.ui.theme.RedExpired

@Composable
fun FederationHeader(
    totalAthletes: Int,
    activeCount: Int,
    expiringCount: Int,
    expiredCount: Int,
    modifier: Modifier = Modifier,
    selectedFilter: RenewalState? = null,
    onFilterSelect: (RenewalState?) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = NavyCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            NavyCardElevated,
                            NavyCard
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header Top with Federation Shield and Brand Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BluePrimary, GoldAccent)
                            )
                        )
                        .border(1.5.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Federação",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ATLETIC",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldAccent)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "FEDERAÇÃO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                ),
                                color = Color.Black
                            )
                        }
                    }
                    Text(
                        text = "Gestão de Atletas & Graduações",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMetricPill(
                    title = "Total",
                    count = totalAthletes,
                    icon = Icons.Default.People,
                    color = BluePrimary,
                    isSelected = selectedFilter == null,
                    onClick = { onFilterSelect(null) },
                    modifier = Modifier.weight(1f)
                )

                StatMetricPill(
                    title = "Em Dia",
                    count = activeCount,
                    icon = Icons.Default.CheckCircle,
                    color = GreenActive,
                    isSelected = selectedFilter == RenewalState.ACTIVE,
                    onClick = { onFilterSelect(RenewalState.ACTIVE) },
                    modifier = Modifier.weight(1f)
                )

                StatMetricPill(
                    title = "A Vencer",
                    count = expiringCount,
                    icon = Icons.Default.Warning,
                    color = GoldAccent,
                    isSelected = selectedFilter == RenewalState.EXPIRING_SOON,
                    onClick = { onFilterSelect(RenewalState.EXPIRING_SOON) },
                    modifier = Modifier.weight(1f)
                )

                StatMetricPill(
                    title = "Vencidas",
                    count = expiredCount,
                    icon = Icons.Default.Error,
                    color = RedExpired,
                    isSelected = selectedFilter == RenewalState.EXPIRED,
                    onClick = { onFilterSelect(RenewalState.EXPIRED) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatMetricPill(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (isSelected) color.copy(alpha = 0.25f) else Color(0xFF0F172A).copy(alpha = 0.5f))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) color else Color(0xFF334155),
                shape = shape
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = Color.White
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (isSelected) color else Color(0xFF94A3B8)
            )
        }
    }
}

