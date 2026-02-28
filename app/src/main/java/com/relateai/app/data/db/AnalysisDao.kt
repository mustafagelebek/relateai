package com.relateai.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AnalysisRecord): Long

    @Query("SELECT * FROM analysis_history ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AnalysisRecord>>

    @Query("SELECT COUNT(*) FROM analysis_history")
    suspend fun getCount(): Int

    @Delete
    suspend fun delete(record: AnalysisRecord)

    @Query("DELETE FROM analysis_history")
    suspend fun deleteAll()
}
