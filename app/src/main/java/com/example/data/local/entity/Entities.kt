package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * USER (ID | NIP | NAMA_MEKANIK | USERNAME | PASSWORD | LEVEL)
 * 7 ROLES: SUPER ADMIN | KEPALA BENGKEL | GH | SH | TEKNIK | KEINDAHAN | CAT
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nip: String,
    val namaMekanik: String,
    val username: String,
    val password: String,
    val level: String, // "SUPER ADMIN", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "KEINDAHAN", "CAT"
    val kodePool: String = "BENGKEL-PUSAT"
) {
    // Convenience property for UI compatibility
    val nama: String get() = namaMekanik
    val namaPetugas: String get() = namaMekanik
    val levelAkses: String get() = level
}

/**
 * MASTER KENDARAAN (Susunan tabel database: | NO_MOBIL | NO_POLISI | TYPE | Equipment | ChassisNo | TAHUN | Stiker Bandara | Engine |)
 */
@Entity(tableName = "master_kendaraan")
data class MasterKendaraanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noMobil: String,
    val noPolisi: String,
    val tipe: String,
    val equipment: String = "",
    val noChassis: String = "",
    val tahunPembuatan: String = "",
    val stikerBandara: String = "",
    val engineType: String = "",
    val jadwalMobil: String = "Jadwal Reguler",
    // Retain compatibility field
    val pool: String = "BENGKEL-PUSAT",
    val warna: String = ""
) {
    val type: String get() = tipe
    val chassisNo: String get() = noChassis
    val tahun: String get() = tahunPembuatan
    val engine: String get() = engineType
    val platNomer: String get() = noPolisi
}

/**
 * LAPORAN KENDARAAN (LAPORAN HARIAN)
 * (ID | Timestamp | NO MOBIL | NAMA/NIP | KELUHAN KENDARAAN | KODE AKSES | TOKEN | STATUS | UPDATE DATA | POOL/VENDOR | NAMA MEKANIK | PENGERJAAN)
 * STATUS: "Belum Dikerjakan", "Sedang di kerjakan", "mobil tidak ke bengkel", "Selesai"
 */
@Entity(tableName = "laporan_harian")
data class LaporanHarianEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: String,
    val noMobil: String,
    val namaNip: String,
    val keluhanKendaraan: String,
    val kodeAkses: String = "",
    val token: String = "",
    val status: String = "Belum Dikerjakan", // Belum Dikerjakan, Sedang di kerjakan, mobil tidak ke bengkel, Selesai
    val updateData: String = "",
    val poolVendor: String = "",
    val namaMekanik: String = "", // Diambil dari data user NIP dan nama
    val pengerjaan: String = "",
    val isSynced: Boolean = false,
    val syncTimestamp: Long = 0L
)

/**
 * SERVICE KENDARAAN (TEKNIK)
 * Susunan Data:
 * 1. ID | 2. STATUS MOBIL | 3. No_Mobil | 4. SERVICE | 5. Selisih_KM | 6. Notification_Desc |
 * 7. Notif_No | 8. Status | 9. KM_Servis_Sebelumnya | 10. KM_Jatuh_Tempo | 11. Equipment_No |
 * 12. Notif_Date | 13. KM_Total | 14. Sticker_Bandara
 */
@Entity(tableName = "service_teknik")
data class ServiceTeknikEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val statusMobil: String = "",
    val noMobil: String = "",
    val service: String = "", // SERVICE (Jenis Service)
    val selisihKm: String = "",
    val notificationDesc: String = "",
    val notifNo: String = "",
    val status: String = "NOPR", // NOPR, OSNO
    val kmServisSebelumnya: String = "",
    val kmJatuhTempo: String = "",
    val equipmentNo: String = "",
    val notifDate: String = "",
    val kmTotal: String = "",
    val stickerBandara: String = "",
    val isSynced: Boolean = false,
    val syncTimestamp: Long = 0L
) {
    // Compatibility alias
    val jenisService: String get() = service
}

/**
 * SERVICE KEINDAHAN
 * (STATUS_MOBIL | NO_MOBIL | Jenis_Service | Selisih_KM | Notification_Desc | Notif_No | Status(NOPR,OSNO) | KM_Servis_Sebelumnya | Equipment_No | Sticker_Bandara)
 */
@Entity(tableName = "service_keindahan")
data class ServiceKeindahanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val statusMobil: String,
    val noMobil: String,
    val jenisService: String,
    val selisihKm: String,
    val notificationDesc: String,
    val notifNo: String,
    val status: String = "NOPR", // NOPR, OSNO
    val kmServisSebelumnya: String = "",
    val kmJatuhTempo: String = "",
    val equipmentNo: String = "",
    val notifDate: String = "",
    val kmTotal: String = "",
    val stickerBandara: String = "",
    val isSynced: Boolean = false,
    val syncTimestamp: Long = 0L
) {
    val service: String get() = jenisService
}

/**
 * CLAIM (Spreadsheet ID: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs, Sheet: MONITORING)
 * Susunan tabel:
 * | TIMESTAMP | NOMER REGISTRASI | TANGGAL KEJADIAN | NO MOBIL | NIP PENGEMUDI | NAMA PENGEMUDI | DETAIL KERUSAKAN MOBIL | FOTO KERUSAKAN MOBIL | FOTO HASIL PENGECATAN | PHOTO LC | PHOTO PENGERJAAN
 */
@Entity(tableName = "claim")
data class ClaimEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: String = "",
    val nomerRegistrasi: String = "",
    val tanggalKejadian: String = "",
    val noMobil: String = "",
    val nipPengemudi: String = "",
    val namaPengemudi: String = "",
    val detailKerusakanMobil: String = "",
    val fotoKerusakanMobil: String = "",
    val fotoHasilPengecatan: String = "",
    val photoLc: String = "",
    val photoPengerjaan: String = "",
    // Compatibility fields
    val pengerjaanClaim: String = "",
    val pengerjaanLc: String = "",
    val hasilPerbaikan: String = "",
    val estimasiClaim: String = "",
    val statusClaim: String = "Diajukan",
    val status: String = "Open",
    val isSynced: Boolean = false,
    val syncTimestamp: Long = 0L
)

/**
 * PENGERJAAN CAT
 * ID (kode uniq) | Timestamp | TANGGAL | NO MOBIL | KERUSAKAN | PHOTO KERUSAKAN | FOTO NOTIF | FOTO HASIL PENGECATAN | GAMBAR | MEKANIK | NO REGISTRASI CAT | PHOTO LAS KETOK | FOTO HASIL PENGECATAN_2 | FOTO HASIL PENGECATAN_3
 */
@Entity(tableName = "pengerjaan_cat")
data class PengerjaanCatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: String = "",
    val tanggal: String = "",
    val noMobil: String = "",
    val kerusakan: String = "",
    val photoKerusakan: String = "", // Ref from Claim / Kamera / Galeri
    val photoNotif: String = "", // Kamera / Galeri
    val fotoHasilPengecatan: String = "", // Keterangan Pengecatan
    val gambarFotoHasilPengecatan: String = "", // GAMBAR (Kamera / Galeri)
    val mekanik: String = "", // NIP / Nama User Login (Read-only)
    val noRegistrasi: String = "", // NO REGISTRASI CAT
    val photoLasKetok: String = "", // PHOTO LAS KETOK (Kamera / Galeri)
    val fotoHasilPengecatan2: String = "", // FOTO HASIL PENGECATAN_2 (Kamera / Galeri)
    val fotoHasilPengecatan3: String = "", // FOTO HASIL PENGECATAN_3 (Kamera / Galeri)
    val isSynced: Boolean = false,
    val syncTimestamp: Long = 0L
) {
    val noRegistrasiCat: String get() = noRegistrasi
}

/**
 * BAN (ID | No_Mobil | No_Polisi | EMBOS | KM_BAN)
 * Spreadsheet ID: 17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo, Sheet: DATA_BAN
 */
@Entity(tableName = "ban")
data class BanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noMobil: String,
    val platNomer: String = "",
    val embos: String = "",
    val kmTotal: String = "",
    val kmBan: String = "",
    val merkBan: String = "",
    val tanggalPasang: String = "",
    val posisiBan: String = "", // Depan Kanan, Depan Kiri, Belakang Kanan, Belakang Kiri
    val isSynced: Boolean = false
) {
    val displayKmTotal: String get() = if (kmBan.isNotBlank()) kmBan else kmTotal
    val displayKmBan: String get() = if (kmBan.isNotBlank()) kmBan else kmTotal
    val displayPlatNomer: String get() = platNomer
    val displayNoPolisi: String get() = platNomer
    val displayEmbos: String get() = if (embos.isNotBlank()) embos else merkBan
}

/**
 * KOPLING (ID | No Mobil | Tipe | KM_KOPLING)
 * Spreadsheet ID: 1qBtcaJP1adyawgZ2hbAIvlld-usZ3CV5YB6QSRpVxd4, Sheet: KOPLING
 */
@Entity(tableName = "kopling")
data class KoplingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noMobil: String,
    val jenisTipe: String = "",
    val totalKm: String = "",
    val kmKopling: String = "",
    val tanggalGanti: String = "",
    val catatan: String = "",
    val isSynced: Boolean = false
) {
    val displayTotalKm: String get() = if (kmKopling.isNotBlank()) kmKopling else totalKm
    val displayKmKopling: String get() = if (kmKopling.isNotBlank()) kmKopling else totalKm
    val displayTipe: String get() = jenisTipe
}

/**
 * BATERAY / ACCU (Spreadsheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Sheet: ACCU)
 * Susunan Kolom Tabel:
 * | NO_MOBIL | NO_POLISI | TYPE | RESERVASI | USIA | Tipe Accu |
 */
@Entity(tableName = "bateray")
data class BaterayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noMobil: String,
    val platNomer: String = "",
    val jenisTipe: String = "",
    val tanggal: String = "",
    val usia: String = "",
    val tipeAccu: String = "",
    val tanggalGanti: String = "",
    val merkBaterai: String = "",
    val voltaseKondisi: String = "12.6V - Baik",
    val isSynced: Boolean = false
) {
    val displayNoMobil: String get() = noMobil
    val displayPlatNomer: String get() = platNomer
    val displayTipe: String get() = if (jenisTipe.isNotBlank()) jenisTipe else "TRANSMOVER"
    val displayReservasi: String get() = if (tanggal.isNotBlank()) tanggal else tanggalGanti
    val displayUsia: String get() = usia
    val displayTipeAccu: String get() = if (tipeAccu.isNotBlank()) tipeAccu else merkBaterai
}

/**
 * RESERVASI (Spreadsheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Sheet: GOODMOVEN)
 * Kolom Tampilan:
 * NO_MOBIL | Order | Material | Material_Description | Doc_Date | Pstng_Date | Quantity | BUn | LC_Amount | Batch
 */
@Entity(tableName = "reservasi")
data class ReservasiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noMobil: String = "",
    val orderNo: String = "",
    val material: String = "",
    val materialDescription: String = "",
    val documentDate: String = "",
    val postingDate: String = "",
    val quantity: String = "",
    val baseUnitOfMeasure: String = "",
    val amtInLocCur: String = "",
    val batch: String = "",
    val isSynced: Boolean = false
) {
    val docDate: String get() = documentDate
    val pstngDate: String get() = postingDate
    val bun: String get() = baseUnitOfMeasure
    val lcAmount: String get() = amtInLocCur
}

/**
 * ARGO (Spreadsheet ID: 1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU, Sheet: ARGO)
 * Kolom: ID | NO MOBIL | KM KOSONG | KM ISI | RIT | DROP | KM | GOLONGAN
 */
@Entity(tableName = "argo")
data class ArgoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noMobil: String = "",
    val kmKosong: String = "",
    val kmIsi: String = "",
    val rit: String = "",
    val drop: String = "",
    val km: String = "",
    val golongan: String = "",
    val isSynced: Boolean = false
)

/**
 * Master Pool
 */
@Entity(tableName = "master_pool")
data class MasterPoolEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val kodePool: String,
    val namaPool: String,
    val alamatPool: String
)

/**
 * Pengaturan Foto
 */
@Entity(tableName = "pengaturan_namaphoto")
data class PengaturanNamaPhotoEntity(
    @PrimaryKey
    val id: Int,
    val nama: String,
    val tampilan: String
)

/**
 * Data Audit (Keindahan / Inspeksi)
 */
@Entity(tableName = "data_audit")
data class DataAuditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val tanggal: String = "",
    val nip: String = "",
    val namaPetugas: String = "",
    val noMobil: String = "",
    val platNomer: String = "",
    val tipe: String = "",
    val warna: String = "",
    val tahun: String = "",
    val pool: String = "",
    val photo1: String = "",
    val catatanPhoto1: String = "",
    val photo2: String = "",
    val catatanPhoto2: String = "",
    val photo3: String = "",
    val catatanPhoto3: String = "",
    val isSynced: Boolean = false,
    val syncTimestamp: Long = 0L,
    val syncError: String? = null
)

/**
 * RIWAYAT SERVICE TEKNIK (Spreadsheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Sheet: SKD)
 * Susunan Kolom Tabel:
 * | Created On | Order | Object Description | Description | KETERANGAN | COUNTER | TIPE |
 * Susunan Kolom Tampilan:
 * | Created On | Order | Object Description | Description | KETERANGAN | COUNTER |
 */
@Entity(tableName = "pengerjaan_teknik")
data class PengerjaanTeknikEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tanggal: String = "",
    val orderNo: String = "",
    val noMobil: String = "",
    val description: String = "",
    val service: String = "",
    val kmService: String = "",
    val keterangan: String = "",
    val counter: String = "",
    val tipe: String = "",
    val isSynced: Boolean = false
) {
    val createdOn: String get() = tanggal
    val order: String get() = orderNo
    val objectDescription: String get() = noMobil
    val displayKeterangan: String get() = if (keterangan.isNotBlank()) keterangan else service
    val displayCounter: String get() = if (counter.isNotBlank()) counter else kmService
    val displayTipe: String get() = tipe
}

/**
 * RIWAYAT SERVICE SKI (Spreadsheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Sheet: SKI)
 * Susunan Kolom Tabel:
 * Tanggal | Order | No Mobil | Description | SERVICE | Km Service
 */
@Entity(tableName = "riwayat_ski")
data class RiwayatSkiEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tanggal: String = "",
    val orderNo: String = "",
    val noMobil: String = "",
    val description: String = "",
    val service: String = "",
    val kmService: String = "",
    val isSynced: Boolean = false
)

/**
 * PEKERJAAN SERVICE KEINDAHAN
 * Database ID: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30
 * Sheet: SERVICE_KEINDAHAN
 * Folder Drive Foto: 1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6
 *
 * Susunan Kolom Data Base:
 * ID (kode unix) | TANGGAL (tanggal dan jam saat itu) | NO_MOBIL | JENIS_SERVICE | NIP/NAMA (sesuai Dengan user login dan tidakdapat di edit) | NOTIF_NO | ORDER | APPROVEL | PHOTO_DEPAN | PHOTO_SAMPING_LH | PHOTO_INTERIOR | PHOTO_BELAKANG | PHOTO_SAMPING_RH
 */
@Entity(tableName = "pekerjaan_service_keindahan")
data class PekerjaanServiceKeindahanEntity(
    @PrimaryKey
    val id: Long = 0, // ID (kode unix)
    val tanggal: String = "", // TANGGAL (tanggal dan jam saat itu)
    val noMobil: String = "",
    val jenisService: String = "",
    val nipNama: String = "", // NIP/NAMA (sesuai Dengan user login dan tidakdapat di edit)
    val notifNo: String = "",
    val orderNo: String = "",
    val approvel: String = "Menunggu", // APPROVEL
    val photoDepan: String = "",
    val photoSampingLh: String = "",
    val photoInterior: String = "",
    val photoBelakang: String = "",
    val photoSampingRh: String = "",
    val km: String = "",
    val isSynced: Boolean = false,
    val syncTimestamp: Long = 0L
) {
    val order: String get() = orderNo
    val approval: String get() = approvel
}

/**
 * PREDIKSI PART
 * Database ID: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs
 * Sheet: PREDIKSI_PART
 *
 * Susunan Tabel:
 * | ID | TANGGAL | NO_MOBIL | SERVICE | KM_MOBIL | NIP_NAMA | NAMA_PART | PREDIKSI_KM | KM_TARGET | ARGO_SEKARANG | ESTIMASI_KM | APROVEL | KETERANGAN | STATUS |
 *
 * Susunan Tampilan:
 * | TANGGAL | NO_MOBIL | SERVICE | NIP_NAMA | NAMA_PART | PREDIKSI_KM | ESTIMASI_KM | APROVEL | KETERANGAN | STATUS |
 */
@Entity(tableName = "prediksi_part")
data class PrediksiPartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tanggal: String = "",
    val noMobil: String = "",
    val service: String = "",
    val kmInput: String = "",
    val nipNama: String = "",
    val namaPart: String = "",
    val prediksiKm: String = "",
    val kmTarget: String = "",
    val argoSekarang: String = "",
    val estimasiKm: String = "",
    val aprovel: String = "Menunggu",
    val keterangan: String = "",
    val status: String = "",
    val isSynced: Boolean = false
)

/**
 * RIWAYAT CLAIM (Spreadsheet ID: 1hyvU96v-ZQlpHO_B1IWhVAeJI3Txpu_Bwl3yNOMpbsE, Sheet: SECURITY)
 *
 * Susunan Tabel:
 * ID | Timestamp | Nomer registrasi | Tanggal kejadian | NO MOBIL | Nip pengemudi | Nama pengemudi | Detail kerusakan mobil | Foto kerusakan mobil | Foto kerusakan | PENGERJAAN_CAT | PENGERJAAN_LC | HASIL
 *
 * Susunan Tampilan:
 * Timestamp | Nomer registrasi | Tanggal kejadian | NO MOBIL | Nip pengemudi | Nama pengemudi | Detail kerusakan mobil | Foto kerusakan mobil | HASIL
 *
 * Data diambil urut sesuai dengan ID.
 */
@Entity(tableName = "riwayat_claim")
data class RiwayatClaimEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: String = "",
    val nomerRegistrasi: String = "",
    val tanggalKejadian: String = "",
    val noMobil: String = "",
    val nipPengemudi: String = "",
    val namaPengemudi: String = "",
    val detailKerusakanMobil: String = "",
    val fotoKerusakanMobil: String = "",
    val fotoKerusakan: String = "",
    val pengerjaanCat: String = "",
    val pengerjaanLc: String = "",
    val hasil: String = "",
    val isSynced: Boolean = false,
    val syncTimestamp: Long = 0L
)



