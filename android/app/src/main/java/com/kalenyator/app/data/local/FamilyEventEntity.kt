package com.kalenyator.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kalenyator.app.data.model.FamilyEventType

@Entity(tableName = "family_events")
data class FamilyEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: FamilyEventType,
    val month: Int,
    val day: Int,
    val year: Int? = null,
    val note: String? = null,
    val photoPath: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
