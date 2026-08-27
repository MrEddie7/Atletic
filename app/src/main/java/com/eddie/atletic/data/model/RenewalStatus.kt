package com.eddie.atletic.data.model

import androidx.compose.ui.graphics.Color
import com.eddie.atletic.ui.theme.AmberWarning
import com.eddie.atletic.ui.theme.GreenActive
import com.eddie.atletic.ui.theme.RedExpired
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class RenewalState(
    val title: String,
    val color: Color
) {
    ACTIVE("Regular / Em Dia", GreenActive),
    EXPIRING_SOON("A Vencer", AmberWarning),
    EXPIRED("Vencida", RedExpired)
}

data class RenewalInfo(
    val state: RenewalState,
    val daysRemaining: Long,
    val renewalDateFormatted: String,
    val badgeText: String,
    val detailMessage: String
)

object RenewalCalculator {
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val brDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun parseDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            if (dateStr.contains("-")) {
                isoDateFormat.parse(dateStr.trim())
            } else {
                brDateFormat.parse(dateStr.trim())
            }
        } catch (e: Exception) {
            null
        }
    }

    fun formatDateToDisplay(dateStr: String?): String {
        val date = parseDate(dateStr) ?: return dateStr ?: "--/--/----"
        return brDateFormat.format(date)
    }

    fun formatDateToIso(date: Date): String {
        return isoDateFormat.format(date)
    }

    fun calculate(renewalDateStr: String?): RenewalInfo {
        val renewalDate = parseDate(renewalDateStr)
        if (renewalDate == null) {
            return RenewalInfo(
                state = RenewalState.EXPIRED,
                daysRemaining = -999,
                renewalDateFormatted = "Não informada",
                badgeText = "Sem Data",
                detailMessage = "Data de renovação anual não cadastrada"
            )
        }

        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val today = todayCalendar.time

        val targetCalendar = Calendar.getInstance().apply {
            time = renewalDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val target = targetCalendar.time

        val diffMillis = target.time - today.time
        val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis)
        val formattedDate = brDateFormat.format(renewalDate)

        return when {
            daysRemaining < 0 -> {
                val overdueDays = -daysRemaining
                RenewalInfo(
                    state = RenewalState.EXPIRED,
                    daysRemaining = daysRemaining,
                    renewalDateFormatted = formattedDate,
                    badgeText = "Vencida ($overdueDays d)",
                    detailMessage = "Matrícula anual vencida há $overdueDays ${if (overdueDays == 1L) "dia" else "dias"}"
                )
            }
            daysRemaining == 0L -> {
                RenewalInfo(
                    state = RenewalState.EXPIRING_SOON,
                    daysRemaining = 0,
                    renewalDateFormatted = formattedDate,
                    badgeText = "Vence Hoje",
                    detailMessage = "A matrícula anual vence hoje!"
                )
            }
            daysRemaining <= 30 -> {
                RenewalInfo(
                    state = RenewalState.EXPIRING_SOON,
                    daysRemaining = daysRemaining,
                    renewalDateFormatted = formattedDate,
                    badgeText = "Vence em $daysRemaining d",
                    detailMessage = "Matrícula anual a vencer em $daysRemaining ${if (daysRemaining == 1L) "dia" else "dias"}"
                )
            }
            else -> {
                RenewalInfo(
                    state = RenewalState.ACTIVE,
                    daysRemaining = daysRemaining,
                    renewalDateFormatted = formattedDate,
                    badgeText = "Em Dia",
                    detailMessage = "Matrícula ativa e regular até $formattedDate"
                )
            }
        }
    }

    fun addOneYear(currentDateStr: String?): String {
        val current = parseDate(currentDateStr) ?: Date()
        val cal = Calendar.getInstance().apply {
            time = current
            add(Calendar.YEAR, 1)
        }
        return isoDateFormat.format(cal.time)
    }

    fun todayIso(): String {
        return isoDateFormat.format(Date())
    }

    fun oneYearFromTodayIso(): String {
        val cal = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }
        return isoDateFormat.format(cal.time)
    }
}

