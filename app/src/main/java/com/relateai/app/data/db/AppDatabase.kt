package com.relateai.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AnalysisRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun analysisDao(): AnalysisDao
}
