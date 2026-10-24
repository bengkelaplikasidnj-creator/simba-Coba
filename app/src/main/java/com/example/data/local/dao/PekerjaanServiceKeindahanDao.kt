package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.PekerjaanServiceKeindahanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PekerjaanServiceKeindahanDao {
    @Query("SELECT * FROM pekerjaan_service_keindahan ORDER BY id DESC")
    fun getAllPekerjaanServiceKeindahan(): Flow<List<PekerjaanServiceKeindahanEntity>>

    @Query("SELECT * FROM pekerjaan_service_keindahan WHERE id = :id LIMIT 1")
    suspend fun getPekerjaanById(id: Long): PekerjaanServiceKeindahanEntity?

    @Query("SELECT * FROM pekerjaan_service_keindahan WHERE isSynced = 0")
    suspend fun getUnsyncedPekerjaanServiceKeindahan(): List<PekerjaanServiceKeindahanEntity>

    @Query("SELECT COUNT(*) FROM pekerjaan_service_keindahan WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pekerjaan_service_keindahan WHERE isSynced = 0")
    suspend fun getUnsyncedCountDirect(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPekerjaanServiceKeindahan(item: PekerjaanServiceKeindahanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPekerjaanServiceKeindahanList(items: List<PekerjaanServiceKeindahanEntity>)

    @Update
    suspend fun updatePekerjaanServiceKeindahan(item: PekerjaanServiceKeindahanEntity)

    @Query("UPDATE pekerjaan_service_keindahan SET isSynced = 1, syncTimestamp = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM pekerjaan_service_keindahan WHERE id = :id")
    suspend fun deletePekerjaanServiceKeindahanById(id: Long)

    @Query("DELETE FROM pekerjaan_service_keindahan")
    suspend fun deleteAllPekerjaanServiceKeindahan()

    @Query("SELECT COUNT(*) FROM pekerjaan_service_keindahan")
    suspend fun getPekerjaanServiceKeindahanCount(): Int
}
