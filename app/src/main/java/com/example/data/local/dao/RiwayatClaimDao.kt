package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.RiwayatClaimEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RiwayatClaimDao {
    @Query("SELECT * FROM riwayat_claim ORDER BY id ASC")
    fun getAllRiwayatClaim(): Flow<List<RiwayatClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiwayatClaim(item: RiwayatClaimEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiwayatClaimList(items: List<RiwayatClaimEntity>)

    @Update
    suspend fun updateRiwayatClaim(item: RiwayatClaimEntity)

    @Query("DELETE FROM riwayat_claim WHERE id = :id")
    suspend fun deleteRiwayatClaimById(id: Long)

    @Query("DELETE FROM riwayat_claim")
    suspend fun deleteAllRiwayatClaim()

    @Query("SELECT COUNT(*) FROM riwayat_claim")
    suspend fun getRiwayatClaimCount(): Int
}
