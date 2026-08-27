package com.eddie.atletic.data.model

import androidx.compose.ui.graphics.Color
import com.eddie.atletic.ui.theme.BeltBlack
import com.eddie.atletic.ui.theme.BeltBlue
import com.eddie.atletic.ui.theme.BeltBrown
import com.eddie.atletic.ui.theme.BeltCoralRed
import com.eddie.atletic.ui.theme.BeltCoralWhite
import com.eddie.atletic.ui.theme.BeltGreen
import com.eddie.atletic.ui.theme.BeltGrey
import com.eddie.atletic.ui.theme.BeltOrange
import com.eddie.atletic.ui.theme.BeltPurple
import com.eddie.atletic.ui.theme.BeltRed
import com.eddie.atletic.ui.theme.BeltWhite
import com.eddie.atletic.ui.theme.BeltYellow

enum class Belt(
    val displayName: String,
    val color: Color,
    val sleeveColor: Color,
    val stripeColor: Color = Color.White,
    val maxDegrees: Int = 4,
    val isCoral: Boolean = false
) {
    WHITE("Branca", BeltWhite, Color(0xFF1E293B), Color.White, 4),
    GREY("Cinza", BeltGrey, Color(0xFF1E293B), Color.White, 4),
    YELLOW("Amarela", BeltYellow, Color(0xFF1E293B), Color.White, 4),
    ORANGE("Laranja", BeltOrange, Color(0xFF1E293B), Color.White, 4),
    GREEN("Verde", BeltGreen, Color(0xFF1E293B), Color.White, 4),
    BLUE("Azul", BeltBlue, Color(0xFF1E293B), Color.White, 4),
    PURPLE("Roxa", BeltPurple, Color(0xFF1E293B), Color.White, 4),
    BROWN("Marrom", BeltBrown, Color(0xFF1E293B), Color.White, 4),
    BLACK("Preta", BeltBlack, Color(0xFFDC2626), Color.White, 6),
    CORAL("Coral (Mestre)", BeltCoralRed, Color(0xFF1E293B), Color.White, 7, isCoral = true),
    RED("Vermelha (Grão-Mestre)", BeltRed, Color(0xFFF8FAFC), Color(0xFFDC2626), 10);

    fun getFullGraduationTitle(degrees: Int): String {
        return if (degrees <= 0) {
            "Faixa $displayName"
        } else {
            "Faixa $displayName - ${degrees}º Grau"
        }
    }

    companion object {
        fun fromString(name: String?): Belt {
            if (name.isNullOrBlank()) return WHITE
            return try {
                valueOf(name.uppercase().trim())
            } catch (e: Exception) {
                // Try matching display name
                entries.firstOrNull { it.displayName.equals(name.trim(), ignoreCase = true) } ?: WHITE
            }
        }
    }
}

