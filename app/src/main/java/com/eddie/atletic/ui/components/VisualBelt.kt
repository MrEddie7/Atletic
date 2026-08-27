package com.eddie.atletic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eddie.atletic.data.model.Belt
import com.eddie.atletic.ui.theme.BeltCoralRed
import com.eddie.atletic.ui.theme.BeltCoralWhite
import com.eddie.atletic.ui.theme.GoldAccent

/**
 * Composable visual que renderiza realisticamente uma faixa marcial (BJJ/Judô/Karatê)
 * com sua cor principal, ponteira (preta/vermelha/branca) e graus (listras brancas).
 */
@Composable
fun VisualBelt(
    belt: Belt,
    degrees: Int,
    modifier: Modifier = Modifier,
    height: Dp = 26.dp,
    showLabel: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val shape = RoundedCornerShape(4.dp)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .shadow(elevation = 2.dp, shape = shape)
                .clip(shape)
                .background(
                    if (belt.isCoral) {
                        Brush.horizontalGradient(
                            listOf(
                                BeltCoralRed, BeltCoralRed,
                                BeltCoralWhite, BeltCoralWhite,
                                BeltCoralRed, BeltCoralRed,
                                BeltCoralWhite, BeltCoralWhite,
                                BeltCoralRed
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                belt.color,
                                belt.color.copy(alpha = 0.88f),
                                belt.color
                            )
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (belt == Belt.WHITE) Color(0xFF94A3B8) else Color(0x33000000),
                    shape = shape
                )
        ) {
            // Costuras centrais da faixa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.Center)
                    .background(Color(0x22000000))
            )

            // Ponteira da faixa (sleeve) à direita
            val sleeveWidth = 44.dp
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(sleeveWidth)
                    .fillMaxHeight()
                    .background(belt.sleeveColor)
                    .border(
                        width = 1.dp,
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Graus / Listras dentro da ponteira
                if (degrees > 0) {
                    val actualDegrees = degrees.coerceIn(1, belt.maxDegrees)
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(actualDegrees) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(belt.stripeColor)
                            )
                        }
                    }
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = belt.getFullGraduationTitle(degrees),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Seletor de Faixa para o formulário de cadastro/edição
 */
@Composable
fun BeltPickerItem(
    belt: Belt,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, GoldAccent)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            VisualBelt(
                belt = belt,
                degrees = 0,
                modifier = Modifier.width(64.dp),
                height = 14.dp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = belt.displayName,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

