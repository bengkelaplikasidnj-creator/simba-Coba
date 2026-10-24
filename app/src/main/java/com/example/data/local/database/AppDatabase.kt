package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ArgoDao
import com.example.data.local.dao.BanDao
import com.example.data.local.dao.BaterayDao
import com.example.data.local.dao.ClaimDao
import com.example.data.local.dao.DataAuditDao
import com.example.data.local.dao.KoplingDao
import com.example.data.local.dao.LaporanHarianDao
import com.example.data.local.dao.MasterKendaraanDao
import com.example.data.local.dao.MasterPoolDao
import com.example.data.local.dao.PengaturanNamaPhotoDao
import com.example.data.local.dao.PengerjaanCatDao
import com.example.data.local.dao.PengerjaanTeknikDao
import com.example.data.local.dao.PekerjaanServiceKeindahanDao
import com.example.data.local.dao.PrediksiPartDao
import com.example.data.local.dao.ReservasiDao
import com.example.data.local.dao.RiwayatClaimDao
import com.example.data.local.dao.RiwayatSkiDao
import com.example.data.local.dao.ServiceKeindahanDao
import com.example.data.local.dao.ServiceTeknikDao
import com.example.data.local.dao.UserDao
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
import com.example.data.local.entity.PengerjaanTeknikEntity
import com.example.data.local.entity.PekerjaanServiceKeindahanEntity
import com.example.data.local.entity.PrediksiPartEntity
import com.example.data.local.entity.ReservasiEntity
import com.example.data.local.entity.RiwayatClaimEntity
import com.example.data.local.entity.RiwayatSkiEntity
import com.example.data.local.entity.ServiceKeindahanEntity
import com.example.data.local.entity.ServiceTeknikEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        MasterKendaraanEntity::class,
        LaporanHarianEntity::class,
        ServiceTeknikEntity::class,
        ServiceKeindahanEntity::class,
        ClaimEntity::class,
        PengerjaanCatEntity::class,
        BanEntity::class,
        KoplingEntity::class,
        BaterayEntity::class,
        ReservasiEntity::class,
        ArgoEntity::class,
        PrediksiPartEntity::class,
        PengerjaanTeknikEntity::class,
        PekerjaanServiceKeindahanEntity::class,
        RiwayatSkiEntity::class,
        RiwayatClaimEntity::class,
        MasterPoolEntity::class,
        PengaturanNamaPhotoEntity::class,
        DataAuditEntity::class
    ],
    version = 29,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun masterKendaraanDao(): MasterKendaraanDao
    abstract fun laporanHarianDao(): LaporanHarianDao
    abstract fun serviceTeknikDao(): ServiceTeknikDao
    abstract fun serviceKeindahanDao(): ServiceKeindahanDao
    abstract fun claimDao(): ClaimDao
    abstract fun pengerjaanCatDao(): PengerjaanCatDao
    abstract fun banDao(): BanDao
    abstract fun koplingDao(): KoplingDao
    abstract fun baterayDao(): BaterayDao
    abstract fun reservasiDao(): ReservasiDao
    abstract fun argoDao(): ArgoDao
    abstract fun prediksiPartDao(): PrediksiPartDao
    abstract fun pengerjaanTeknikDao(): PengerjaanTeknikDao
    abstract fun pekerjaanServiceKeindahanDao(): PekerjaanServiceKeindahanDao
    abstract fun riwayatSkiDao(): RiwayatSkiDao
    abstract fun riwayatClaimDao(): RiwayatClaimDao
    abstract fun masterPoolDao(): MasterPoolDao
    abstract fun pengaturanNamaPhotoDao(): PengaturanNamaPhotoDao
    abstract fun dataAuditDao(): DataAuditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `prediksi_part` ADD COLUMN `nipNama` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `pekerjaan_service_keindahan` ADD COLUMN `nipNama` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `pekerjaan_service_keindahan` ADD COLUMN `approvel` TEXT NOT NULL DEFAULT 'Menunggu'")
            }
        }

        val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `master_kendaraan` ADD COLUMN `stikerBandara` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `pengerjaan_teknik` ADD COLUMN `keterangan` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `pengerjaan_teknik` ADD COLUMN `counter` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `pengerjaan_teknik` ADD COLUMN `tipe` TEXT NOT NULL DEFAULT ''")
            }
        }

        private fun buildRoomDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bengkel_mobile_mw.db"
            )
                .addMigrations(MIGRATION_21_22, MIGRATION_22_23, MIGRATION_27_28, MIGRATION_28_29)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(DatabaseCallback())
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .build()
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: try {
                    val db = buildRoomDatabase(context)
                    // Trigger database open to verify schema validity eagerly
                    db.openHelper.writableDatabase
                    db
                } catch (e: Throwable) {
                    android.util.Log.e("AppDatabase", "Database open error, wiping and recreating: ${e.message}", e)
                    try {
                        context.applicationContext.deleteDatabase("bengkel_mobile_mw.db")
                    } catch (_: Throwable) {}
                    val cleanDb = buildRoomDatabase(context)
                    try {
                        cleanDb.openHelper.writableDatabase
                    } catch (_: Throwable) {}
                    cleanDb
                }.also { INSTANCE = it }
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                triggerSeed()
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                triggerSeed()
            }

            private fun triggerSeed() {
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            seedDatabase(database)
                        } catch (e: Throwable) {
                            android.util.Log.e("AppDatabase", "Error during triggerSeed: ${e.message}", e)
                        }
                    }
                }
            }
        }

        suspend fun clearAllLocalDataExceptUsers(database: AppDatabase) {
            try { database.serviceTeknikDao().deleteAllServiceTeknik() } catch (_: Throwable) {}
            try { database.laporanHarianDao().deleteAllLaporan() } catch (_: Throwable) {}
            try { database.masterKendaraanDao().deleteAllKendaraan() } catch (_: Throwable) {}
            try { database.serviceKeindahanDao().deleteAllServiceKeindahan() } catch (_: Throwable) {}
            try { database.claimDao().deleteAllClaims() } catch (_: Throwable) {}
            try { database.pengerjaanCatDao().deleteAllPengerjaanCat() } catch (_: Throwable) {}
            try { database.banDao().deleteAllBan() } catch (_: Throwable) {}
            try { database.koplingDao().deleteAllKopling() } catch (_: Throwable) {}
            try { database.baterayDao().deleteAllBateray() } catch (_: Throwable) {}
            try { database.reservasiDao().deleteAllReservasi() } catch (_: Throwable) {}
            try { database.pengerjaanTeknikDao().deleteAllPengerjaanTeknik() } catch (_: Throwable) {}
            try { database.pekerjaanServiceKeindahanDao().deleteAllPekerjaanServiceKeindahan() } catch (_: Throwable) {}
            try { database.riwayatSkiDao().deleteAllRiwayatSki() } catch (_: Throwable) {}
        }

        suspend fun seedDatabase(database: AppDatabase) {
            // Bersihkan akun demo bawaan lama guna mencegah masuk aplikasi dengan data yang berbeda dari Google Sheet
            try {
                val demoUsernames = setOf("superadmin", "admin", "kabeng", "gh", "sh", "teknik", "keindahan", "cat", "demo")
                val demoNips = setOf("1001", "1002", "1003", "1004", "1005", "1006", "1007", "DEMO")
                val existingUsers = database.userDao().getAllUsersList()
                existingUsers.forEach { u ->
                    val uName = u.username.trim().lowercase()
                    val uNip = u.nip.trim()
                    if (uName in demoUsernames || uNip in demoNips ||
                        u.namaMekanik.contains("Demo", ignoreCase = true) ||
                        u.namaMekanik.contains("(Super Admin)", ignoreCase = true) ||
                        u.namaMekanik.contains("(Kabeng)", ignoreCase = true) ||
                        u.namaMekanik.contains("(Group Head)", ignoreCase = true) ||
                        u.namaMekanik.contains("(Section Head)", ignoreCase = true) ||
                        u.namaMekanik.contains("(Mekanik Teknik)", ignoreCase = true) ||
                        u.namaMekanik.contains("(Mekanik Keindahan)", ignoreCase = true) ||
                        u.namaMekanik.contains("(Mekanik Cat)", ignoreCase = true)
                    ) {
                        database.userDao().deleteUserById(u.id)
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("AppDatabase", "Error cleaning demo users: ${e.message}", e)
            }

            // Seed initial data Riwayat Service SKI jika masih kosong
            try {
                if (database.riwayatSkiDao().getRiwayatSkiCount() == 0) {
                    val defaultSkiList = listOf(
                        RiwayatSkiEntity(
                            id = 1,
                            tanggal = "2026-09-30",
                            orderNo = "50021490",
                            noMobil = "MWD118",
                            description = "Full salon luar dalam & nano ceramic coating",
                            service = "SALON LENGKAP",
                            kmService = "49800",
                            isSynced = true
                        ),
                        RiwayatSkiEntity(
                            id = 2,
                            tanggal = "2026-09-30",
                            orderNo = "50021455",
                            noMobil = "MWD112",
                            description = "Engine wash & detailing ruang mesin",
                            service = "CUCI MESIN",
                            kmService = "61400",
                            isSynced = true
                        ),
                        RiwayatSkiEntity(
                            id = 3,
                            tanggal = "2026-09-29",
                            orderNo = "50021410",
                            noMobil = "MWD105",
                            description = "Deep cleaning jok interior & semir ban",
                            service = "CLEANING & POLISH",
                            kmService = "38900",
                            isSynced = true
                        ),
                        RiwayatSkiEntity(
                            id = 4,
                            tanggal = "2026-09-29",
                            orderNo = "50021389",
                            noMobil = "MWD102",
                            description = "Salon interior & poles kaca film",
                            service = "SERVICE KEINDAHAN",
                            kmService = "52100",
                            isSynced = true
                        ),
                        RiwayatSkiEntity(
                            id = 5,
                            tanggal = "2026-09-28",
                            orderNo = "50021345",
                            noMobil = "MWD101",
                            description = "Perawatan keindahan berkala dan poles body",
                            service = "CUCI MESIN & SALON",
                            kmService = "45200",
                            isSynced = true
                        ),
                        RiwayatSkiEntity(
                            id = 6,
                            tanggal = "2026-09-27",
                            orderNo = "50021290",
                            noMobil = "MWD125",
                            description = "Pembersihan plafon, karpet dasar, dan door trim",
                            service = "INTERIOR CLEANING",
                            kmService = "58300",
                            isSynced = true
                        ),
                        RiwayatSkiEntity(
                            id = 7,
                            tanggal = "2026-09-26",
                            orderNo = "50021215",
                            noMobil = "MWD108",
                            description = "Poles jamur kaca depan dan kaca samping",
                            service = "POLES KACA",
                            kmService = "41500",
                            isSynced = true
                        )
                    )
                    database.riwayatSkiDao().insertRiwayatSkiList(defaultSkiList)
                }
            } catch (e: Throwable) {
                android.util.Log.e("AppDatabase", "Error seeding RiwayatSki: ${e.message}", e)
            }
        }
    }
}
