package com.kalenyator.app.data.local

import androidx.room.TypeConverter
import com.kalenyator.app.data.model.FamilyEventType

class Converters {
    @TypeConverter
    fun fromFamilyEventType(value: FamilyEventType): String = value.name

    @TypeConverter
    fun toFamilyEventType(value: String): FamilyEventType = FamilyEventType.valueOf(value)
}
