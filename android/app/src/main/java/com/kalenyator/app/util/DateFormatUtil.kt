package com.kalenyator.app.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateFormatUtil {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun format(day: Int, month: Int, year: Int? = null): String {
        val dd = day.toString().padStart(2, '0')
        val mm = month.toString().padStart(2, '0')
        return if (year != null) "$dd.$mm.$year" else "$dd.$mm."
    }

    fun format(date: LocalDate): String = date.format(dateFormatter)

    /** Open-Meteo: `2024-06-02T14:30` → `02.06.2024 14:30` */
    fun formatDateTime(iso: String): String = runCatching {
        LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME).format(dateTimeFormatter)
    }.getOrElse { iso }
}
