package com.kalenyator.app.util

import android.content.Context
import com.kalenyator.app.R
import com.kalenyator.app.data.local.FamilyEventEntity
import com.kalenyator.app.data.model.FamilyEventType

data class GreetingTemplate(
    val id: Int,
    val text: String
)

enum class GreetingAgeGroup {
    CHILD,
    TEEN,
    YOUNG,
    ADULT,
    SENIOR,
    UNKNOWN
}

object GreetingTemplates {
    private const val STAR_OF_WEEK_MAX_DAYS = 7L

    fun isStarOfWeek(daysUntil: Long): Boolean = daysUntil in 0 until STAR_OF_WEEK_MAX_DAYS

    fun ageGroup(age: Int?): GreetingAgeGroup = when (age) {
        null -> GreetingAgeGroup.UNKNOWN
        in 0..12 -> GreetingAgeGroup.CHILD
        in 13..17 -> GreetingAgeGroup.TEEN
        in 18..29 -> GreetingAgeGroup.YOUNG
        in 30..59 -> GreetingAgeGroup.ADULT
        else -> GreetingAgeGroup.SENIOR
    }

    fun all(
        context: Context,
        event: FamilyEventEntity,
        daysUntil: Long,
        age: Int?
    ): List<GreetingTemplate> {
        val isToday = daysUntil == 0L
        val date = DateFormatUtil.format(event.day, event.month, event.year)
        val arrayRes = when (event.type) {
            FamilyEventType.BIRTHDAY -> birthdayArray(isToday, ageGroup(age))
            else -> if (isToday) R.array.greeting_anniv_today else R.array.greeting_anniv_soon
        }
        return context.resources.getStringArray(arrayRes).mapIndexed { index, raw ->
            GreetingTemplate(
                id = index,
                text = formatTemplate(raw, event.title, daysUntil, age, date, isToday, event.type)
            )
        }
    }

    fun build(context: Context, event: FamilyEventEntity, daysUntil: Long, age: Int?): String =
        all(context, event, daysUntil, age).firstOrNull()?.text
            ?: context.getString(R.string.greeting_today_event, event.title, DateFormatUtil.format(event.day, event.month, event.year))

    private fun birthdayArray(isToday: Boolean, group: GreetingAgeGroup): Int = when (group) {
        GreetingAgeGroup.CHILD -> if (isToday) R.array.greeting_bday_child_today else R.array.greeting_bday_child_soon
        GreetingAgeGroup.TEEN -> if (isToday) R.array.greeting_bday_teen_today else R.array.greeting_bday_teen_soon
        GreetingAgeGroup.YOUNG -> if (isToday) R.array.greeting_bday_young_today else R.array.greeting_bday_young_soon
        GreetingAgeGroup.ADULT -> if (isToday) R.array.greeting_bday_adult_today else R.array.greeting_bday_adult_soon
        GreetingAgeGroup.SENIOR -> if (isToday) R.array.greeting_bday_senior_today else R.array.greeting_bday_senior_soon
        GreetingAgeGroup.UNKNOWN -> if (isToday) R.array.greeting_bday_unknown_today else R.array.greeting_bday_unknown_soon
    }

    private fun formatTemplate(
        raw: String,
        name: String,
        daysUntil: Long,
        age: Int?,
        date: String,
        isToday: Boolean,
        type: FamilyEventType
    ): String = when {
        type == FamilyEventType.BIRTHDAY && isToday && age != null ->
            String.format(raw, name, age)
        type == FamilyEventType.BIRTHDAY && !isToday && age != null ->
            String.format(raw, name, daysUntil.toInt(), age)
        type == FamilyEventType.BIRTHDAY && isToday ->
            String.format(raw, name)
        type == FamilyEventType.BIRTHDAY ->
            String.format(raw, name, daysUntil.toInt())
        isToday ->
            String.format(raw, name, date)
        else ->
            String.format(raw, name, date, daysUntil.toInt())
    }
}
