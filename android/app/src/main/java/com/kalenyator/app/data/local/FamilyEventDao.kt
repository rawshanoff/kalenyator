package com.kalenyator.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyEventDao {
    @Query("SELECT * FROM family_events ORDER BY month, day, title")
    fun observeAll(): Flow<List<FamilyEventEntity>>

    @Query("SELECT * FROM family_events WHERE month = :month AND day = :day")
    fun observeForDate(month: Int, day: Int): Flow<List<FamilyEventEntity>>

    @Query("SELECT * FROM family_events ORDER BY month, day, title")
    suspend fun getAll(): List<FamilyEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: FamilyEventEntity): Long

    @Update
    suspend fun update(event: FamilyEventEntity)

    @Delete
    suspend fun delete(event: FamilyEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<FamilyEventEntity>)
}
