package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ArgoEntity
import com.example.data.local.entity.BanEntity
import com.example.data.local.entity.BaterayEntity
import com.example.data.local.entity.ClaimEntity
import com.example.data.local.entity.DataAuditEntity
import com.example.data.local.entity.KoplingEntity
import com.example.data.local.entity.LaporanHarianEntity
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.data.local.entity.MasterPoolEntity
import com.example.data.local.entity.PengaturanNamaPhotoEntity
import com.example.data.local.entity.PengerjaanCatEntity
import com.example.data.local.entity.PrediksiPartEntity
import com.example.data.local.entity.ReservasiEntity
import com.example.data.local.entity.ServiceKeindahanEntity
import com.example.data.local.entity.ServiceTeknikEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAllUsersList(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(:username)) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(TRIM(nip)) = LOWER(TRIM(:nip)) LIMIT 1")
    suspend fun getUserByNip(nip: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(:input)) OR LOWER(TRIM(nip)) = LOWER(TRIM(:input)) OR LOWER(TRIM(namaMekanik)) = LOWER(TRIM(:input)) ORDER BY CASE WHEN TRIM(password) != '' THEN 0 ELSE 1 END, id ASC")
    suspend fun getUsersMatchingLogin(input: String): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Int)

    @Query("DELETE FROM users WHERE nip = :nip")
    suspend fun deleteUserByNip(nip: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface MasterKendaraanDao {
    @Query("SELECT * FROM master_kendaraan ORDER BY id ASC")
    fun getAllKendaraan(): Flow<List<MasterKendaraanEntity>>

    @Query("SELECT * FROM master_kendaraan WHERE noMobil = :noMobil LIMIT 1")
    suspend fun getKendaraanByNoMobil(noMobil: String): MasterKendaraanEntity?

    @Query("SELECT * FROM master_kendaraan WHERE noMobil LIKE '%' || :query || '%' OR noPolisi LIKE '%' || :query || '%' OR tipe LIKE '%' || :query || '%' OR equipment LIKE '%' || :query || '%' OR noChassis LIKE '%' || :query || '%' OR tahunPembuatan LIKE '%' || :query || '%' OR stikerBandara LIKE '%' || :query || '%' OR engineType LIKE '%' || :query || '%'")
    fun searchKendaraan(query: String): Flow<List<MasterKendaraanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKendaraan(kendaraan: MasterKendaraanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKendaraanList(list: List<MasterKendaraanEntity>)

    @Update
    suspend fun updateKendaraan(kendaraan: MasterKendaraanEntity)

    @Delete
    suspend fun deleteKendaraan(kendaraan: MasterKendaraanEntity)

    @Query("DELETE FROM master_kendaraan WHERE id = :id")
    suspend fun deleteKendaraanById(id: Int)

    @Query("DELETE FROM master_kendaraan WHERE noMobil = :noMobil")
    suspend fun deleteKendaraanByNoMobil(noMobil: String)

    @Query("DELETE FROM master_kendaraan")
    suspend fun deleteAllKendaraan()

    @Query("SELECT COUNT(*) FROM master_kendaraan")
    suspend fun getKendaraanCount(): Int
}

@Dao
interface LaporanHarianDao {
    @Query("SELECT * FROM laporan_harian ORDER BY id DESC")
    fun getAllLaporan(): Flow<List<LaporanHarianEntity>>

    @Query("SELECT * FROM laporan_harian WHERE noMobil = :noMobil ORDER BY id DESC")
    fun getLaporanByNoMobil(noMobil: String): Flow<List<LaporanHarianEntity>>

    @Query("SELECT * FROM laporan_harian WHERE status = :status ORDER BY id DESC")
    fun getLaporanByStatus(status: String): Flow<List<LaporanHarianEntity>>

    @Query("SELECT * FROM laporan_harian WHERE id = :id LIMIT 1")
    suspend fun getLaporanById(id: Long): LaporanHarianEntity?

    @Query("SELECT * FROM laporan_harian WHERE timestamp = :timestamp LIMIT 1")
    suspend fun getLaporanByTimestamp(timestamp: String): LaporanHarianEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaporan(laporan: LaporanHarianEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaporanList(list: List<LaporanHarianEntity>)

    @Update
    suspend fun updateLaporan(laporan: LaporanHarianEntity)

    @Delete
    suspend fun deleteLaporan(laporan: LaporanHarianEntity)

    @Query("DELETE FROM laporan_harian WHERE id = :id")
    suspend fun deleteLaporanById(id: Long)

    @Query("UPDATE laporan_harian SET isSynced = 1, syncTimestamp = :syncTime WHERE id = :id")
    suspend fun markAsSynced(id: Long, syncTime: Long = System.currentTimeMillis())

    @Query("SELECT * FROM laporan_harian WHERE isSynced = 0")
    suspend fun getUnsyncedLaporan(): List<LaporanHarianEntity>

    @Query("SELECT * FROM laporan_harian ORDER BY id DESC")
    suspend fun getAllLaporanList(): List<LaporanHarianEntity>

    @Query("DELETE FROM laporan_harian")
    suspend fun deleteAllLaporan()

    @Query("SELECT COUNT(*) FROM laporan_harian")
    suspend fun getLaporanCount(): Int
}

@Dao
interface ServiceTeknikDao {
    @Query("SELECT * FROM service_teknik ORDER BY id ASC")
    fun getAllServiceTeknik(): Flow<List<ServiceTeknikEntity>>

    @Query("SELECT * FROM service_teknik WHERE noMobil = :noMobil ORDER BY id DESC")
    fun getServiceTeknikByNoMobil(noMobil: String): Flow<List<ServiceTeknikEntity>>

    @Query("SELECT * FROM service_teknik WHERE id = :id LIMIT 1")
    suspend fun getServiceTeknikById(id: Long): ServiceTeknikEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceTeknik(service: ServiceTeknikEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceTeknikList(list: List<ServiceTeknikEntity>)

    @Update
    suspend fun updateServiceTeknik(service: ServiceTeknikEntity)

    @Delete
    suspend fun deleteServiceTeknik(service: ServiceTeknikEntity)

    @Query("DELETE FROM service_teknik WHERE id = :id")
    suspend fun deleteServiceTeknikById(id: Long)

    @Query("DELETE FROM service_teknik")
    suspend fun deleteAllServiceTeknik()

    @Query("SELECT COUNT(*) FROM service_teknik")
    suspend fun getServiceTeknikCount(): Int
}

@Dao
interface ServiceKeindahanDao {
    @Query("SELECT * FROM service_keindahan ORDER BY id ASC")
    fun getAllServiceKeindahan(): Flow<List<ServiceKeindahanEntity>>

    @Query("SELECT * FROM service_keindahan WHERE noMobil = :noMobil ORDER BY id ASC")
    fun getServiceKeindahanByNoMobil(noMobil: String): Flow<List<ServiceKeindahanEntity>>

    @Query("SELECT * FROM service_keindahan WHERE id = :id LIMIT 1")
    suspend fun getServiceKeindahanById(id: Long): ServiceKeindahanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceKeindahan(service: ServiceKeindahanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceKeindahanList(list: List<ServiceKeindahanEntity>)

    @Update
    suspend fun updateServiceKeindahan(service: ServiceKeindahanEntity)

    @Delete
    suspend fun deleteServiceKeindahan(service: ServiceKeindahanEntity)

    @Query("DELETE FROM service_keindahan WHERE id = :id")
    suspend fun deleteServiceKeindahanById(id: Long)

    @Query("DELETE FROM service_keindahan")
    suspend fun deleteAllServiceKeindahan()

    @Query("SELECT COUNT(*) FROM service_keindahan")
    suspend fun getServiceKeindahanCount(): Int
}

@Dao
interface ClaimDao {
    @Query("SELECT * FROM claim ORDER BY timestamp DESC, id DESC")
    fun getAllClaims(): Flow<List<ClaimEntity>>

    @Query("SELECT * FROM claim WHERE nomerRegistrasi = :regNo LIMIT 1")
    suspend fun getClaimByRegistrasi(regNo: String): ClaimEntity?

    @Query("SELECT * FROM claim WHERE id = :id LIMIT 1")
    suspend fun getClaimById(id: Long): ClaimEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: ClaimEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaimList(list: List<ClaimEntity>)

    @Update
    suspend fun updateClaim(claim: ClaimEntity)

    @Delete
    suspend fun deleteClaim(claim: ClaimEntity)

    @Query("DELETE FROM claim WHERE id = :id")
    suspend fun deleteClaimById(id: Long)

    @Query("DELETE FROM claim")
    suspend fun deleteAllClaims()

    @Query("SELECT COUNT(*) FROM claim")
    suspend fun getClaimCount(): Int
}

@Dao
interface PengerjaanCatDao {
    @Query("SELECT * FROM pengerjaan_cat ORDER BY id DESC")
    fun getAllPengerjaanCat(): Flow<List<PengerjaanCatEntity>>

    @Query("SELECT * FROM pengerjaan_cat ORDER BY id DESC")
    suspend fun getAllPengerjaanCatList(): List<PengerjaanCatEntity>

    @Query("SELECT * FROM pengerjaan_cat WHERE id = :id LIMIT 1")
    suspend fun getPengerjaanCatById(id: Long): PengerjaanCatEntity?

    @Query("SELECT * FROM pengerjaan_cat WHERE noRegistrasi = :noRegistrasi LIMIT 1")
    suspend fun getPengerjaanCatByNoRegistrasi(noRegistrasi: String): PengerjaanCatEntity?

    @Query("SELECT * FROM pengerjaan_cat WHERE timestamp = :timestamp LIMIT 1")
    suspend fun getPengerjaanCatByTimestamp(timestamp: String): PengerjaanCatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPengerjaanCat(cat: PengerjaanCatEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPengerjaanCatList(list: List<PengerjaanCatEntity>)

    @Update
    suspend fun updatePengerjaanCat(cat: PengerjaanCatEntity)

    @Delete
    suspend fun deletePengerjaanCat(cat: PengerjaanCatEntity)

    @Query("DELETE FROM pengerjaan_cat WHERE id = :id")
    suspend fun deletePengerjaanCatById(id: Long)

    @Query("DELETE FROM pengerjaan_cat")
    suspend fun deleteAllPengerjaanCat()

    @Query("SELECT COUNT(*) FROM pengerjaan_cat")
    suspend fun getPengerjaanCatCount(): Int

    @Query("SELECT * FROM pengerjaan_cat WHERE isSynced = 0")
    suspend fun getUnsyncedCat(): List<PengerjaanCatEntity>

    @Query("UPDATE pengerjaan_cat SET isSynced = 1, syncTimestamp = :syncTime WHERE id = :id")
    suspend fun markAsSynced(id: Long, syncTime: Long = System.currentTimeMillis())
}

@Dao
interface BanDao {
    @Query("SELECT * FROM ban ORDER BY id ASC")
    fun getAllBan(): Flow<List<BanEntity>>

    @Query("SELECT * FROM ban WHERE noMobil = :noMobil")
    fun getBanByNoMobil(noMobil: String): Flow<List<BanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBan(ban: BanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanList(list: List<BanEntity>)

    @Update
    suspend fun updateBan(ban: BanEntity)

    @Query("DELETE FROM ban WHERE id = :id")
    suspend fun deleteBanById(id: Long)

    @Query("DELETE FROM ban")
    suspend fun deleteAllBan()

    @Query("SELECT COUNT(*) FROM ban")
    suspend fun getBanCount(): Int
}

@Dao
interface KoplingDao {
    @Query("SELECT * FROM kopling ORDER BY id ASC")
    fun getAllKopling(): Flow<List<KoplingEntity>>

    @Query("SELECT * FROM kopling WHERE noMobil = :noMobil")
    fun getKoplingByNoMobil(noMobil: String): Flow<List<KoplingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKopling(kopling: KoplingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKoplingList(list: List<KoplingEntity>)

    @Update
    suspend fun updateKopling(kopling: KoplingEntity)

    @Query("DELETE FROM kopling WHERE id = :id")
    suspend fun deleteKoplingById(id: Long)

    @Query("DELETE FROM kopling")
    suspend fun deleteAllKopling()

    @Query("SELECT COUNT(*) FROM kopling")
    suspend fun getKoplingCount(): Int
}

@Dao
interface BaterayDao {
    @Query("SELECT * FROM bateray ORDER BY id ASC")
    fun getAllBateray(): Flow<List<BaterayEntity>>

    @Query("SELECT * FROM bateray WHERE noMobil = :noMobil")
    fun getBaterayByNoMobil(noMobil: String): Flow<List<BaterayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBateray(bateray: BaterayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBaterayList(list: List<BaterayEntity>)

    @Update
    suspend fun updateBateray(bateray: BaterayEntity)

    @Query("DELETE FROM bateray WHERE id = :id")
    suspend fun deleteBaterayById(id: Long)

    @Query("DELETE FROM bateray")
    suspend fun deleteAllBateray()

    @Query("SELECT COUNT(*) FROM bateray")
    suspend fun getBaterayCount(): Int
}

@Dao
interface ReservasiDao {
    @Query("SELECT * FROM reservasi ORDER BY materialDescription COLLATE NOCASE ASC, id ASC")
    fun getAllReservasi(): Flow<List<ReservasiEntity>>

    @Query("SELECT * FROM reservasi WHERE noMobil = :noMobil ORDER BY materialDescription COLLATE NOCASE ASC, id ASC")
    fun getReservasiByNoMobil(noMobil: String): Flow<List<ReservasiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservasi(reservasi: ReservasiEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservasiList(list: List<ReservasiEntity>)

    @Update
    suspend fun updateReservasi(reservasi: ReservasiEntity)

    @Query("DELETE FROM reservasi WHERE id = :id")
    suspend fun deleteReservasiById(id: Long)

    @Query("DELETE FROM reservasi")
    suspend fun deleteAllReservasi()

    @Query("SELECT COUNT(*) FROM reservasi")
    suspend fun getReservasiCount(): Int
}

@Dao
interface ArgoDao {
    @Query("SELECT * FROM argo ORDER BY id ASC")
    fun getAllArgo(): Flow<List<ArgoEntity>>

    @Query("SELECT * FROM argo WHERE noMobil = :noMobil ORDER BY id ASC")
    fun getArgoByNoMobil(noMobil: String): Flow<List<ArgoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArgo(argo: ArgoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArgoList(list: List<ArgoEntity>)

    @Update
    suspend fun updateArgo(argo: ArgoEntity)

    @Query("DELETE FROM argo WHERE id = :id")
    suspend fun deleteArgoById(id: Long)

    @Query("DELETE FROM argo")
    suspend fun deleteAllArgo()

    @Query("SELECT COUNT(*) FROM argo")
    suspend fun getArgoCount(): Int
}

@Dao
interface PrediksiPartDao {
    @Query("SELECT * FROM prediksi_part ORDER BY id ASC")
    fun getAllPrediksiPart(): Flow<List<PrediksiPartEntity>>

    @Query("SELECT * FROM prediksi_part WHERE noMobil = :noMobil ORDER BY id ASC")
    fun getPrediksiPartByNoMobil(noMobil: String): Flow<List<PrediksiPartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediksiPart(item: PrediksiPartEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediksiPartList(list: List<PrediksiPartEntity>)

    @Update
    suspend fun updatePrediksiPart(item: PrediksiPartEntity)

    @Query("SELECT * FROM prediksi_part WHERE id = :id LIMIT 1")
    suspend fun getPrediksiPartById(id: Long): PrediksiPartEntity?

    @Query("SELECT * FROM prediksi_part WHERE isSynced = 0")
    suspend fun getUnsyncedPrediksiPart(): List<PrediksiPartEntity>

    @Query("DELETE FROM prediksi_part WHERE id = :id")
    suspend fun deletePrediksiPartById(id: Long)

    @Query("DELETE FROM prediksi_part")
    suspend fun deleteAllPrediksiPart()

    @Query("SELECT COUNT(*) FROM prediksi_part")
    suspend fun getPrediksiPartCount(): Int
}

// Backwards compatibility DAOs
@Dao
interface MasterPoolDao {
    @Query("SELECT * FROM master_pool ORDER BY kodePool ASC")
    fun getAllPools(): Flow<List<MasterPoolEntity>>

    @Query("SELECT * FROM master_pool WHERE kodePool = :kodePool LIMIT 1")
    suspend fun getPoolByKode(kodePool: String): MasterPoolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPool(pool: MasterPoolEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPools(pools: List<MasterPoolEntity>)

    @Update
    suspend fun updatePool(pool: MasterPoolEntity)

    @Query("DELETE FROM master_pool WHERE id = :id")
    suspend fun deletePoolById(id: Int)

    @Query("SELECT COUNT(*) FROM master_pool")
    suspend fun getPoolCount(): Int
}

@Dao
interface PengaturanNamaPhotoDao {
    @Query("SELECT * FROM pengaturan_namaphoto ORDER BY id ASC")
    fun getAllNamaPhoto(): Flow<List<PengaturanNamaPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNamaPhoto(item: PengaturanNamaPhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PengaturanNamaPhotoEntity>)

    @Update
    suspend fun updateNamaPhoto(item: PengaturanNamaPhotoEntity)

    @Query("SELECT COUNT(*) FROM pengaturan_namaphoto")
    suspend fun getCount(): Int
}

@Dao
interface DataAuditDao {
    @Query("SELECT * FROM data_audit ORDER BY timestamp DESC")
    fun getAllAudits(): Flow<List<DataAuditEntity>>

    @Query("SELECT * FROM data_audit WHERE nip = :nip ORDER BY timestamp DESC")
    fun getAuditsByNip(nip: String): Flow<List<DataAuditEntity>>

    @Query("SELECT * FROM data_audit WHERE pool = :pool ORDER BY timestamp DESC")
    fun getAuditsByPool(pool: String): Flow<List<DataAuditEntity>>

    @Query("SELECT * FROM data_audit WHERE id = :id LIMIT 1")
    suspend fun getAuditById(id: Long): DataAuditEntity?

    @Query("SELECT * FROM data_audit WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedAudits(): List<DataAuditEntity>

    @Query("SELECT COUNT(*) FROM data_audit WHERE isSynced = 0")
    fun getUnsyncedAuditCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: DataAuditEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudits(audits: List<DataAuditEntity>)

    @Update
    suspend fun updateAudit(audit: DataAuditEntity)

    @Delete
    suspend fun deleteAudit(audit: DataAuditEntity)

    @Query("DELETE FROM data_audit WHERE id = :id")
    suspend fun deleteAuditById(id: Long)

    @Query("UPDATE data_audit SET isSynced = 1, syncTimestamp = :syncTime, syncError = null WHERE id = :id")
    suspend fun markAsSynced(id: Long, syncTime: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM data_audit")
    suspend fun getTotalAuditCount(): Int

    @Query("SELECT COUNT(*) FROM data_audit WHERE isSynced = 0")
    suspend fun getUnsyncedCount(): Int
}
