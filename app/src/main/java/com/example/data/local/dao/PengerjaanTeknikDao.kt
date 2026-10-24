package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.PengerjaanTeknikEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PengerjaanTeknikDao {
    @Query("SELECT * FROM pengerjaan_teknik ORDER BY id DESC")
    fun getAllPengerjaanTeknik(): Flow<List<PengerjaanTeknikEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPengerjaanTeknik(item: PengerjaanTeknikEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPengerjaanTeknikList(items: List<PengerjaanTeknikEntity>)

    @Update
    suspend fun updatePengerjaanTeknik(item: PengerjaanTeknikEntity)

    @Query("DELETE FROM pengerjaan_teknik WHERE id = :id")
    suspend fun deletePengerjaanTeknikById(id: Long)

    @Query("DELETE FROM pengerjaan_teknik")
    suspend fun deleteAllPengerjaanTeknik()

    @Query("SELECT COUNT(*) FROM pengerjaan_teknik")
    suspend fun getPengerjaanTeknikCount(): Int
}
