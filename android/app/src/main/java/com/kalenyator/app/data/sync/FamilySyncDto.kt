package com.kalenyator.app.data.sync

import com.google.gson.annotations.SerializedName
import com.kalenyator.app.data.local.FamilyEventEntity
import com.kalenyator.app.data.model.FamilyEventType

data class FamilySyncFile(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("app") val app: String = "kalenyator",
    @SerializedName("exportedAt") val exportedAt: Long = System.currentTimeMillis(),
    @SerializedName("events") val events: List<FamilyEventDto>
)

data class FamilyEventDto(
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String,
    @SerializedName("month") val month: Int,
    @SerializedName("day") val day: Int,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("updatedAt") val updatedAt: Long = System.currentTimeMillis()
)

fun FamilyEventEntity.toDto() = FamilyEventDto(
    title = title,
    type = type.name,
    month = month,
    day = day,
    year = year,
    note = note,
    updatedAt = updatedAt
)

fun FamilyEventDto.toEntity() = FamilyEventEntity(
    title = title,
    type = runCatching { FamilyEventType.valueOf(type) }.getOrDefault(FamilyEventType.OTHER),
    month = month,
    day = day,
    year = year,
    note = note,
    updatedAt = updatedAt
)
