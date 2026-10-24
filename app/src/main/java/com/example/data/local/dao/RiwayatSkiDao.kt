package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.RiwayatSkiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RiwayatSkiDao {
    @Query("SELECT * FROM riwayat_ski ORDER BY id ASC")
    fun getAllRiwayatSki(): Flow<List<RiwayatSkiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiwayatSki(item: RiwayatSkiEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiwayatSkiList(items: List<RiwayatSkiEntity>)

    @Update
    suspend fun updateRiwayatSki(item: RiwayatSkiEntity)

    @Query("DELETE FROM riwayat_ski WHERE id = :id")
    suspend fun deleteRiwayatSkiById(id: Long)

    @Query("DELETE FROM riwayat_ski")
    suspend fun deleteAllRiwayatSki()

    @Query("SELECT COUNT(*) FROM riwayat_ski")
    suspend fun getRiwayatSkiCount(): Int
}
