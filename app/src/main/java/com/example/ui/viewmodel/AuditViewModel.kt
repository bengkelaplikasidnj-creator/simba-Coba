package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.*
import com.example.data.remote.GoogleAppsScriptHub
import com.example.data.repository.AuditRepository
import com.example.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    LOGIN,
    MONITORING,
    LAPORAN_HARIAN,
    PREDIKSI_PART,
    SERVICE_TEKNIK,
    PENGERJAAN_TEKNIK,
    SERVICE_KEINDAHAN,
    PEKERJAAN_SERVICE_KEINDAHAN,
    RIWAYAT_SKI,
    BAN,
    KOPLING,
    BATERAY,
    RESERVASI,
    MASTER_KENDARAAN,
    ARGO,
    MASTER_USER,
    GOOGLE_SCRIPT_CONFIG,

    // Legacy compatibility screens
    INPUT_AUDIT,
    RIWAYAT_SERVICE,
    MONITORING_ALL_POOL,
    HASIL_AUDIT_ALL_POOL,
    MASTER_POOL,
    PENGATURAN_NAMAPHOTO
}

class AuditViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = AuditRepository(application, database)

    // Current User & Auth (Menggunakan sesi tersimpan di HP atau null jika belum login)
    private val _currentUser = MutableStateFlow<UserEntity?>(
        repository.sessionManager.getCurrentUser()
    )
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Default screen: langsung menampilkan MONITORING
    private val _currentScreen = MutableStateFlow(AppScreen.MONITORING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Data Streams from Repository
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKendaraan: StateFlow<List<MasterKendaraanEntity>> = repository.allKendaraan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLaporan: StateFlow<List<LaporanHarianEntity>> = repository.allLaporan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServiceTeknik: StateFlow<List<ServiceTeknikEntity>> = repository.allServiceTeknik
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServiceKeindahan: StateFlow<List<ServiceKeindahanEntity>> = repository.allServiceKeindahan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPekerjaanServiceKeindahan: StateFlow<List<PekerjaanServiceKeindahanEntity>> = repository.allPekerjaanServiceKeindahan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClaims: StateFlow<List<ClaimEntity>> = repository.allClaims
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPengerjaanCat: StateFlow<List<PengerjaanCatEntity>> = repository.allPengerjaanCat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBan: StateFlow<List<BanEntity>> = repository.allBan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKopling: StateFlow<List<KoplingEntity>> = repository.allKopling
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBateray: StateFlow<List<BaterayEntity>> = repository.allBateray
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReservasi: StateFlow<List<ReservasiEntity>> = repository.allReservasi
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArgo: StateFlow<List<ArgoEntity>> = repository.allArgo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPrediksiPart: StateFlow<List<PrediksiPartEntity>> = repository.allPrediksiPart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPengerjaanTeknik: StateFlow<List<PengerjaanTeknikEntity>> = repository.allPengerjaanTeknik
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRiwayatSki: StateFlow<List<RiwayatSkiEntity>> = repository.allRiwayatSki
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRiwayatClaim: StateFlow<List<RiwayatClaimEntity>> = repository.allRiwayatClaim
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Compatibility streams
    val allPools: StateFlow<List<MasterPoolEntity>> = repository.allPools
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAudits: StateFlow<List<DataAuditEntity>> = repository.allAudits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOnline: StateFlow<Boolean> = repository.networkMonitor.isOnline

    val unsyncedKeindahanCount: StateFlow<Int> = repository.pekerjaanKeindahanDao.getUnsyncedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unsyncedAuditCount: StateFlow<Int> = repository.auditDao.getUnsyncedAuditCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val photoSettings: StateFlow<List<PengaturanNamaPhotoEntity>> = repository.allPhotoSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Modal / Detailed Inspection on click NO_MOBIL
    private val _selectedKendaraanDetail = MutableStateFlow<MasterKendaraanEntity?>(null)
    val selectedKendaraanDetail: StateFlow<MasterKendaraanEntity?> = _selectedKendaraanDetail.asStateFlow()

    // Target Order and Detail for direct navigation to Pengerjaan Teknik & Keindahan
    private val _targetTeknikOrder = MutableStateFlow<String?>(null)
    val targetTeknikOrder: StateFlow<String?> = _targetTeknikOrder.asStateFlow()

    private val _targetTeknikItem = MutableStateFlow<PengerjaanTeknikEntity?>(null)
    val targetTeknikItem: StateFlow<PengerjaanTeknikEntity?> = _targetTeknikItem.asStateFlow()

    private val _targetSkiOrder = MutableStateFlow<String?>(null)
    val targetSkiOrder: StateFlow<String?> = _targetSkiOrder.asStateFlow()

    private val _targetSkiItem = MutableStateFlow<RiwayatSkiEntity?>(null)
    val targetSkiItem: StateFlow<RiwayatSkiEntity?> = _targetSkiItem.asStateFlow()

    // Target prefill for direct "Kerjakan" from Service Keindahan to Pekerjaan Service Keindahan
    private val _targetKeindahanPrefill = MutableStateFlow<Triple<String, String, String>?>(null)
    val targetKeindahanPrefill: StateFlow<Triple<String, String, String>?> = _targetKeindahanPrefill.asStateFlow()

    private val _orderNavigationChoice = MutableStateFlow<OrderChoiceState?>(null)
    val orderNavigationChoice: StateFlow<OrderChoiceState?> = _orderNavigationChoice.asStateFlow()

    fun clearTargetTeknik() {
        _targetTeknikOrder.value = null
        _targetTeknikItem.value = null
    }

    fun clearTargetKeindahanPrefill() {
        _targetKeindahanPrefill.value = null
    }

    fun clearTargetSki() {
        _targetSkiOrder.value = null
        _targetSkiItem.value = null
    }

    fun clearOrderChoice() {
        _orderNavigationChoice.value = null
    }

    // Sync States
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _keindahanUploadStatus = MutableStateFlow<String?>(null)
    val keindahanUploadStatus: StateFlow<String?> = _keindahanUploadStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(
        if (repository.sessionManager.lastSyncTimestamp > 0) {
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(repository.sessionManager.lastSyncTimestamp))
        } else "Belum sinkron"
    )
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    // Toast/Snackbar events
    private val _eventMessage = MutableSharedFlow<String>()
    val eventMessage = _eventMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            // 0. Bersihkan sesi login jika sebelumnya menggunakan akun demo guna mencegah data berbeda dari Google Sheet
            withContext(Dispatchers.IO) {
                val current = repository.sessionManager.getCurrentUser()
                if (current != null) {
                    val uName = current.username.trim().lowercase()
                    val uNip = current.nip.trim()
                    if (uName in setOf("superadmin", "admin", "kabeng", "gh", "sh", "teknik", "keindahan", "cat", "demo") ||
                        uNip in setOf("1001", "1002", "1003", "1004", "1005", "1006", "1007", "DEMO") ||
                        current.namaMekanik.contains("Demo", ignoreCase = true)
                    ) {
                        repository.sessionManager.clearSession()
                        _currentUser.value = null
                    }
                }
            }

            // 1. Bersihkan akun demo lama dari database lokal HP
            withContext(Dispatchers.IO) {
                try {
                    AppDatabase.seedDatabase(database)
                } catch (e: Throwable) {
                    android.util.Log.e("AuditViewModel", "Error cleaning database: ${e.message}", e)
                }
            }
            // 2. Prioritas Login: Otomatis download data user dari Google Sheets & simpan ke HP
            withContext(Dispatchers.IO) {
                try {
                    repository.syncUserFromGoogleSheets()
                } catch (e: Throwable) {
                    android.util.Log.e("AuditViewModel", "Gagal auto-download user awal: ${e.message}", e)
                }
            }
            // 2b. Auto-sync data Claim dari Google Sheets agar nama pengemudi segera terisi akurat
            withContext(Dispatchers.IO) {
                try {
                    repository.syncClaimFromGoogleSheets()
                } catch (e: Throwable) {
                    android.util.Log.e("AuditViewModel", "Gagal auto-download claim awal: ${e.message}", e)
                }
            }
            // 2c. Reset dan hapus data RESERVASI beserta komponen pendukung (Ban, Kopling, Baterai)
            withContext(Dispatchers.IO) {
                if (!repository.sessionManager.isReservasiAndKomponenCleared) {
                    try {
                        repository.clearReservasiAndKomponenPendukungData()
                        repository.sessionManager.sheetReservasi = com.example.data.session.SessionManager.DEFAULT_RESERVASI_SHEET_ID
                        repository.sessionManager.sheetReservasiName = com.example.data.session.SessionManager.DEFAULT_RESERVASI_SHEET_NAME
                        repository.sessionManager.isReservasiGmSheetCleared = true
                        repository.sessionManager.isReservasiAndKomponenCleared = true
                    } catch (e: Throwable) {
                        android.util.Log.e("AuditViewModel", "Gagal membersihkan data reservasi dan komponen pendukung: ${e.message}", e)
                    }
                }
            }
            // 2d. Reset dan update data RIWAYAT SERVICE SKI ke spreadsheet ID baru: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY
            withContext(Dispatchers.IO) {
                if (!repository.sessionManager.isRiwayatSkiUpdatedToNewId) {
                    try {
                        repository.clearRiwayatSkiData()
                        repository.sessionManager.sheetRiwayatSki = com.example.data.session.SessionManager.DEFAULT_RIWAYAT_SKI_SHEET_ID
                        repository.sessionManager.sheetRiwayatSkiName = com.example.data.session.SessionManager.DEFAULT_RIWAYAT_SKI_SHEET_NAME
                        repository.sessionManager.isRiwayatSkiUpdatedToNewId = true
                    } catch (e: Throwable) {
                        android.util.Log.e("AuditViewModel", "Gagal reset riwayat ski: ${e.message}", e)
                    }
                }
            }
            // 2e. Reset dan update data BATERAY / ACCU ke spreadsheet ID baru: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY
            withContext(Dispatchers.IO) {
                if (!repository.sessionManager.isBaterayUpdatedToNewId) {
                    try {
                        repository.clearBaterayData()
                        repository.sessionManager.sheetBateray = com.example.data.session.SessionManager.DEFAULT_BATERAY_SHEET_ID
                        repository.sessionManager.sheetBaterayName = com.example.data.session.SessionManager.DEFAULT_BATERAY_SHEET_NAME
                        repository.sessionManager.isBaterayUpdatedToNewId = true
                    } catch (e: Throwable) {
                        android.util.Log.e("AuditViewModel", "Gagal reset data baterai: ${e.message}", e)
                    }
                }
            }
            // 2f. Update nama sheet reservasi dari GM menjadi GOODMOVEN
            withContext(Dispatchers.IO) {
                try {
                    val currentReservasiSheet = repository.sessionManager.sheetReservasiName
                    if (currentReservasiSheet.equals("GM", ignoreCase = true) || currentReservasiSheet.isBlank() || !repository.sessionManager.isReservasiGoodmovenUpdated) {
                        repository.sessionManager.sheetReservasiName = com.example.data.session.SessionManager.DEFAULT_RESERVASI_SHEET_NAME
                        repository.sessionManager.isReservasiGoodmovenUpdated = true
                    }
                } catch (e: Throwable) {
                    android.util.Log.e("AuditViewModel", "Gagal update nama sheet reservasi ke GOODMOVEN: ${e.message}", e)
                }
            }
            // 2g. Update ID & Sheet Riwayat Service Keindahan ke ID baru 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30
            withContext(Dispatchers.IO) {
                try {
                    val currentPkId = repository.sessionManager.sheetPekerjaanServiceKeindahan
                    if (!repository.sessionManager.isPekerjaanKeindahanUpdatedToNewId || currentPkId.isBlank() || currentPkId == "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs") {
                        repository.sessionManager.sheetPekerjaanServiceKeindahan = com.example.data.session.SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID
                        repository.sessionManager.sheetPekerjaanServiceKeindahanName = com.example.data.session.SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME
                        repository.sessionManager.isPekerjaanKeindahanUpdatedToNewId = true
                    }
                } catch (e: Throwable) {
                    android.util.Log.e("AuditViewModel", "Gagal update ID Riwayat Service Keindahan: ${e.message}", e)
                }
            }
            // 2h. Update Google Apps Script Web App URL ke URL baru
            withContext(Dispatchers.IO) {
                try {
                    val currentScript = repository.sessionManager.scriptUrl
                    if (currentScript.isBlank() || currentScript.contains("AKfycbwjfZGwauVXpisbx7T8UPioBTJP802LJvKmvcD5e3smdSuuBOL2T3A_cOYtz2JxM4FZfg") || !repository.sessionManager.isScriptUrlUpdatedV3) {
                        repository.sessionManager.scriptUrl = com.example.data.session.SessionManager.DEFAULT_SCRIPT_URL
                        repository.sessionManager.scriptUrlLaporanHarian = com.example.data.session.SessionManager.DEFAULT_SCRIPT_URL_LAPORAN_HARIAN
                        repository.sessionManager.isScriptUrlUpdatedV3 = true
                    }
                } catch (e: Throwable) {
                    android.util.Log.e("AuditViewModel", "Gagal update URL Google Script: ${e.message}", e)
                }
            }
            // 2i. Update Folder Foto Google Drive ke ID baru 1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6
            withContext(Dispatchers.IO) {
                try {
                    val currentFolder = repository.sessionManager.folderPhotoKeindahanId
                    if (currentFolder.isBlank() || currentFolder == "1Exy7AawdwoV5byqucwVBPIbTxI6anAjk" || !repository.sessionManager.isFolderPhotoUpdatedV2) {
                        repository.sessionManager.folderPhotoKeindahanId = com.example.data.session.SessionManager.DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID
                        repository.sessionManager.isFolderPhotoUpdatedV2 = true
                    }
                } catch (e: Throwable) {
                    android.util.Log.e("AuditViewModel", "Gagal update ID Folder Foto: ${e.message}", e)
                }
            }
            // 2j. Update Database ID & Sheet ARGO ke ID baru 13iuagC5JY1NOkVRMwebz8-XvaEp-2q_qiyaDedGedNQ
            withContext(Dispatchers.IO) {
                try {
                    val currentArgo = repository.sessionManager.sheetArgo
                    if (currentArgo.isBlank() || currentArgo == "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU" || !repository.sessionManager.isArgoUpdatedToNewId) {
                        repository.sessionManager.sheetArgo = com.example.data.session.SessionManager.DEFAULT_ARGO_SHEET_ID
                        repository.sessionManager.sheetArgoName = com.example.data.session.SessionManager.DEFAULT_ARGO_SHEET_NAME
                        repository.sessionManager.isArgoUpdatedToNewId = true
                    }
                } catch (e: Throwable) {
                    android.util.Log.e("AuditViewModel", "Gagal update ID ARGO: ${e.message}", e)
                }
            }
            // 3. Sinkronisasi penuh 14 tabel hanya dijalankan jika user sudah login
            // agar layar Login tetap sangat ringan dan animasi keyboard (IME) tidak pernah lag/timeout
            if (_currentUser.value != null) {
                syncFromGoogleSheets(isManual = false)
            }
        }

        // 4. Pantau konektivitas jaringan: sinkronkan otomatis hasil inspeksi dari Room database cache saat kembali online
        viewModelScope.launch {
            repository.networkMonitor.isOnline.collect { online ->
                if (online && _currentUser.value != null) {
                    val pendingKeindahan = repository.pekerjaanKeindahanDao.getUnsyncedCountDirect()
                    val pendingAudit = repository.auditDao.getUnsyncedAudits().size
                    if (pendingKeindahan > 0 || pendingAudit > 0) {
                        _keindahanUploadStatus.value = "Koneksi online terdeteksi. Menyinkronkan ${pendingKeindahan + pendingAudit} hasil inspeksi dari cache lokal..."
                        val (success, failed) = repository.autoSyncPendingInspections { progress ->
                            _keindahanUploadStatus.value = progress
                        }
                        if (success > 0) {
                            val msg = "✓ Sinkronisasi otomatis selesai: $success hasil inspeksi di cache lokal berhasil diunggah ke cloud!"
                            _keindahanUploadStatus.value = msg
                            _eventMessage.emit(msg)
                            delay(4000)
                            if (_keindahanUploadStatus.value == msg) {
                                _keindahanUploadStatus.value = null
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Download otomatis data user dari Google Sheets langsung ke HP saat awal aplikasi / login
     */
    fun downloadUserToPhone(onComplete: ((Boolean, Int) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = repository.syncUserFromGoogleSheets()
            withContext(Dispatchers.Main) {
                res.onSuccess { count ->
                    onComplete?.invoke(true, count)
                }.onFailure {
                    onComplete?.invoke(false, 0)
                }
            }
        }
    }

    /**
     * Cek seketika apakah ada perubahan di Google Sheets (misal saat berpindah layar atau membuka tab)
     * Tidak menjalankan background loop terus-menerus guna menghemat baterai & menjaga HP tetap dingin.
     */
    fun checkForSheetChanges() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!_isSyncing.value && repository.checkGoogleSheetsHasChanged()) {
                    _eventMessage.emit("Terdeteksi perubahan data di Google Sheets, menyinkronkan...")
                    syncFromGoogleSheets(isManual = false)
                }
            } catch (_: Exception) {}
        }
    }

    fun syncFromGoogleSheets(isManual: Boolean = true) {
        if (_isSyncing.value) return
        _isSyncing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = repository.syncFromGoogleSheets()
                _isSyncing.value = false
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                _lastSyncTime.value = timeStr
                if (result.isSuccess) {
                    if (isManual) {
                        _eventMessage.emit("Sinkronisasi Google Sheets berhasil pukul $timeStr!")
                    } else {
                        _eventMessage.emit("Sinkronisasi awal selesai ($timeStr)")
                    }
                } else {
                    if (isManual) {
                        _eventMessage.emit("Gagal sinkron data: ${result.exceptionOrNull()?.message ?: "Periksa koneksi internet"}")
                    }
                }
            } catch (e: Throwable) {
                _isSyncing.value = false
                android.util.Log.e("AuditViewModel", "Sync exception: ${e.message}", e)
                if (isManual) {
                    _eventMessage.emit("Gagal sinkron data: ${e.message ?: "Periksa koneksi internet"}")
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (screen == AppScreen.LOGIN && repository.sessionManager.isSessionValid()) {
            return
        }
        if (screen == AppScreen.RESERVASI) {
            val user = _currentUser.value
            val level = user?.level?.trim()?.uppercase().orEmpty()
            val isSuperAdmin = user != null && (
                level == "SUPER ADMIN" ||
                level == "SUPERADMIN" ||
                level.contains("SUPER ADMIN") ||
                user.username.trim().equals("superadmin", ignoreCase = true)
            )
            if (!isSuperAdmin) {
                viewModelScope.launch {
                    _eventMessage.emit("Akses ditolak: Menu Reservasi hanya dapat diakses oleh Super Admin.")
                }
                return
            }
        }
        _currentScreen.value = screen
        checkForSheetChanges()
    }

    /**
     * Otomatis mengarahkan ke Pengerjaan Teknik maupun ke Detail Pengerjaan Keindahan
     * berdasarkan Nomor Order yang diklik dari DATA RESERVASI / Rincian Unit
     */
    fun openOrderByOrderNo(orderNo: String) {
        val cleanOrder = orderNo.trim()
        if (cleanOrder.isBlank() || cleanOrder == "-") {
            viewModelScope.launch {
                _eventMessage.emit("Nomor order tidak valid.")
            }
            return
        }

        val teknikMatch = allPengerjaanTeknik.value.find { isOrderMatch(it.orderNo, cleanOrder) }
        val skiMatch = allRiwayatSki.value.find { isOrderMatch(it.orderNo, cleanOrder) }

        val matches = listOfNotNull(
            teknikMatch?.let { "teknik" },
            skiMatch?.let { "ski" }
        )

        when {
            matches.size == 1 && teknikMatch != null -> {
                // Langsung ke Pengerjaan Teknik (SKD) dengan detailnya terbuka
                _targetTeknikItem.value = teknikMatch
                _targetTeknikOrder.value = cleanOrder
                _orderNavigationChoice.value = null
                _currentScreen.value = AppScreen.PENGERJAAN_TEKNIK
                viewModelScope.launch {
                    _eventMessage.emit("Membuka Riwayat Service Teknik (SKD) untuk Order #$cleanOrder")
                }
            }
            matches.size == 1 && skiMatch != null -> {
                // Langsung ke Riwayat Service SKI dengan detailnya terbuka
                _targetSkiItem.value = skiMatch
                _targetSkiOrder.value = cleanOrder
                _orderNavigationChoice.value = null
                _currentScreen.value = AppScreen.RIWAYAT_SKI
                viewModelScope.launch {
                    _eventMessage.emit("Membuka Riwayat Service SKI untuk Order #$cleanOrder")
                }
            }
            matches.size > 1 -> {
                // Order tercatat di beberapa modul
                _orderNavigationChoice.value = OrderChoiceState(
                    orderNo = cleanOrder,
                    teknikItem = teknikMatch,
                    skiItem = skiMatch
                )
            }
            else -> {
                // Belum ada data di list - tampilkan dialog pencarian
                _orderNavigationChoice.value = OrderChoiceState(
                    orderNo = cleanOrder,
                    teknikItem = null,
                    skiItem = null
                )
            }
        }
    }

    fun navigateToTeknikWithOrder(orderNo: String, item: PengerjaanTeknikEntity? = null) {
        val cleanOrder = orderNo.trim()
        val found = item ?: allPengerjaanTeknik.value.find { isOrderMatch(it.orderNo, cleanOrder) }
        _targetTeknikOrder.value = cleanOrder
        _targetTeknikItem.value = found
        _orderNavigationChoice.value = null
        _currentScreen.value = AppScreen.PENGERJAAN_TEKNIK
    }

    fun navigateToSkiWithOrder(orderNo: String, item: RiwayatSkiEntity? = null) {
        val cleanOrder = orderNo.trim()
        val found = item ?: allRiwayatSki.value.find { isOrderMatch(it.orderNo, cleanOrder) }
        _targetSkiOrder.value = cleanOrder
        _targetSkiItem.value = found
        _orderNavigationChoice.value = null
        _currentScreen.value = AppScreen.RIWAYAT_SKI
    }

    fun navigateToPekerjaanKeindahanWithPrefill(noMobil: String, jenisService: String, notifNo: String) {
        val cleanMob = noMobil.trim()
        val cleanService = jenisService.trim()
        val cleanNotif = notifNo.trim()
        _targetKeindahanPrefill.value = Triple(cleanMob, cleanService, cleanNotif)
        _currentScreen.value = AppScreen.PEKERJAAN_SERVICE_KEINDAHAN
        viewModelScope.launch {
            _eventMessage.emit("Membuka Form Pekerjaan Service Keindahan Mobil #$cleanMob")
        }
    }

    fun inspectKendaraanByNoMobil(noMobil: String) {
        viewModelScope.launch {
            val vehicle = repository.getKendaraanByNoMobil(noMobil.trim())
            if (vehicle != null) {
                _selectedKendaraanDetail.value = vehicle
            } else {
                // If not found in DB, create quick info object
                _selectedKendaraanDetail.value = MasterKendaraanEntity(
                    noMobil = noMobil,
                    noPolisi = "Belum terdaftar",
                    tipe = "Armada Bengkel",
                    tahunPembuatan = "-",
                    noChassis = "-",
                    jadwalMobil = "Jadwal Reguler",
                    engineType = "-",
                    equipment = "-"
                )
            }
        }
    }

    fun clearSelectedKendaraanDetail() {
        _selectedKendaraanDetail.value = null
    }

    fun login(username: String, password: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(username, password)
            if (result.isSuccess) {
                val user = result.getOrNull()
                _currentUser.value = user
                _currentScreen.value = AppScreen.MONITORING
                _eventMessage.emit("Selamat datang, ${user?.namaMekanik} (${user?.level})!")
                onComplete(true, null)
                // Jalankan sinkronisasi data di latar belakang setelah berhasil masuk
                syncFromGoogleSheets(isManual = false)
            } else {
                val errMsg = result.exceptionOrNull()?.message ?: "Login gagal. Periksa Username/NIP dan Password."
                _eventMessage.emit(errMsg)
                onComplete(false, errMsg)
            }
        }
    }

    fun logout() {
        repository.sessionManager.clearSession()
        _currentUser.value = null
        _currentScreen.value = AppScreen.LOGIN
        _targetTeknikOrder.value = null
        _targetTeknikItem.value = null
        _targetSkiOrder.value = null
        _targetSkiItem.value = null
        _orderNavigationChoice.value = null
        viewModelScope.launch {
            _eventMessage.emit("Sesi login telah dibersihkan. Anda telah keluar akun.")
        }
    }

    private var syncDebounceJob: kotlinx.coroutines.Job? = null

    /**
     * Sinkronisasi otomatis setiap ada perubahan data (tambah, edit, hapus).
     * Diberikan debounce 1 detik agar jika ada beberapa perubahan sekaligus,
     * seluruh pembaruan dikirimkan dalam 1 sesi sinkronisasi yang efisien.
     */
    fun triggerSyncOnChange() {
        syncDebounceJob?.cancel()
        syncDebounceJob = viewModelScope.launch {
            delay(1000)
            syncFromGoogleSheets(isManual = false)
        }
    }

    // CRUD Laporan Harian
    fun saveLaporan(laporan: LaporanHarianEntity) {
        viewModelScope.launch {
            val res = repository.saveLaporan(laporan)
            if (res.isSuccess) {
                _eventMessage.emit(res.getOrNull() ?: "Laporan berhasil disimpan dan diperbarui ke Google Sheets!")
            } else {
                _eventMessage.emit("⚠️ ${res.exceptionOrNull()?.message ?: "Gagal update Google Sheets"}")
            }
        }
    }

    fun testGoogleScriptUrl(scriptUrl: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = com.example.data.remote.GoogleAppsScriptHub.testGoogleScriptUrl(scriptUrl)
            if (res.isSuccess) {
                onResult(true, res.getOrNull() ?: "Koneksi berhasil!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Gagal terhubung")
            }
        }
    }

    fun testGoogleScriptDrivePermission(scriptUrl: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = com.example.data.remote.GoogleAppsScriptHub.testGoogleScriptDrivePermission(scriptUrl)
            if (res.isSuccess) {
                onResult(true, res.getOrNull() ?: "Izin Google Drive Aktif!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Izin Google Drive belum aktif")
            }
        }
    }

    fun deleteLaporan(id: Long) {
        viewModelScope.launch {
            repository.deleteLaporan(id)
            _eventMessage.emit("Laporan berhasil dihapus.")
            triggerSyncOnChange()
        }
    }

    // CRUD Service Teknik
    fun saveServiceTeknik(service: ServiceTeknikEntity) {
        viewModelScope.launch {
            repository.saveServiceTeknik(service)
            _eventMessage.emit("Service Teknik Mobil #${service.noMobil} berhasil disimpan.")
        }
    }

    fun deleteServiceTeknik(id: Long) {
        viewModelScope.launch {
            repository.deleteServiceTeknik(id)
            _eventMessage.emit("Data Service Teknik dihapus.")
        }
    }

    // CRUD Service Keindahan
    fun saveServiceKeindahan(service: ServiceKeindahanEntity) {
        viewModelScope.launch {
            repository.saveServiceKeindahan(service)
            _eventMessage.emit("Service Keindahan Mobil #${service.noMobil} berhasil disimpan.")
        }
    }

    fun deleteServiceKeindahan(id: Long) {
        viewModelScope.launch {
            repository.deleteServiceKeindahan(id)
            _eventMessage.emit("Data Service Keindahan dihapus.")
        }
    }

    // CRUD PEKERJAAN SERVICE KEINDAHAN
    // ID | TANGGAL | NO_MOBIL | JENIS_SERVICE | NOTIF_NO | ORDER | KM | PHOTO_DEPAN | PHOTO_SAMPING_LH | PHOTO_INTERIOR | PHOTO_BELAKANG | PHOTO_SAMPING_RH
    // Database ID: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30
    // Folder Foto Google Drive: 1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6
    fun uploadPhotoToDrive(
        context: Context,
        filePathOrUri: String,
        fileName: String,
        onResult: (Boolean, String, String) -> Unit = { _, _, _ -> }
    ) {
        uploadPhotoToDrive(context, filePathOrUri, fileName, isHighRes4K = true, onResult = onResult)
    }

    fun uploadPhotoToDrive(
        context: Context,
        filePathOrUri: String,
        fileName: String,
        isHighRes4K: Boolean = true,
        onResult: (Boolean, String, String) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch {
            val scriptUrl = repository.sessionManager.scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
            val folderId = repository.sessionManager.folderPhotoKeindahanId.trim().ifBlank { SessionManager.DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID }
            val res = GoogleAppsScriptHub.uploadImageToGoogleDrive(
                context = context,
                scriptUrl = scriptUrl,
                filePath = filePathOrUri,
                fileName = fileName,
                folderId = folderId,
                folderName = "FOTO_AUDIT_KENDARAAN",
                maxDimension = if (isHighRes4K) 3840 else 2560,
                quality = if (isHighRes4K) 92 else 88
            )
            if (res.isSuccess) {
                val url = res.getOrNull() ?: ""
                val msg = "Foto resolusi tinggi HD/4K berhasil diunggah ke Google Drive!"
                _eventMessage.emit(msg)
                onResult(true, url, msg)
            } else {
                val err = res.exceptionOrNull()?.message ?: "Gagal upload ke Google Drive"
                _eventMessage.emit("⚠️ $err")
                onResult(false, "", err)
            }
        }
    }

    fun testUploadSamplePhotoToDrive(
        context: Context,
        onResult: (Boolean, String, String) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                // Buat bitmap uji coba 400x300 dengan teks watermark
                val bmp = android.graphics.Bitmap.createBitmap(400, 300, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                canvas.drawColor(android.graphics.Color.rgb(0, 51, 102)) // Biru Bengkel

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 20f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                canvas.drawText("SIMBA BC", 200f, 130f, paint)

                val paintSub = android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(187, 222, 251)
                    textSize = 14f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                canvas.drawText("TEST UPLOAD GOOGLE DRIVE", 200f, 165f, paintSub)
                canvas.drawText(dateStr, 200f, 195f, paintSub)

                val scriptUrl = repository.sessionManager.scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
                val folderId = repository.sessionManager.folderPhotoKeindahanId.trim().ifBlank { SessionManager.DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID }
                val fName = "TEST_DRIVE_${System.currentTimeMillis()}.jpg"

                val res = GoogleAppsScriptHub.uploadBitmapToGoogleDrive(
                    scriptUrl = scriptUrl,
                    bitmap = bmp,
                    fileName = fName,
                    folderId = folderId,
                    folderName = "FOTO_AUDIT_KENDARAAN"
                )
                bmp.recycle()

                if (res.isSuccess) {
                    val url = res.getOrNull() ?: ""
                    val msg = "✅ Berhasil upload ke Google Drive! URL: $url"
                    _eventMessage.emit(msg)
                    onResult(true, url, msg)
                } else {
                    val err = res.exceptionOrNull()?.message ?: "Gagal upload"
                    _eventMessage.emit("⚠️ $err")
                    onResult(false, "", err)
                }
            } catch (e: Exception) {
                val err = "Error: ${e.message}"
                _eventMessage.emit(err)
                onResult(false, "", err)
            }
        }
    }

    fun savePekerjaanServiceKeindahan(item: PekerjaanServiceKeindahanEntity, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSyncing.value = true
            val online = repository.networkMonitor.isOnline()
            val initialStatus = if (online) {
                "Menyimpan data inspeksi & menyinkronkan foto Mobil #${item.noMobil}..."
            } else {
                "Menyimpan data inspeksi ke Room Cache Lokal (Mode Offline)..."
            }
            _keindahanUploadStatus.value = initialStatus
            val res = repository.savePekerjaanServiceKeindahan(item) { progressText ->
                _keindahanUploadStatus.value = progressText
            }
            _isSyncing.value = false
            val actionText = if (item.id == 0L) "ditambahkan" else "diperbarui"
            val savedEntity = res.getOrNull()
            if (res.isSuccess && savedEntity != null) {
                val msg = if (savedEntity.isSynced) {
                    "✅ Pekerjaan Keindahan Mobil #${item.noMobil} berhasil $actionText & disinkronkan ke cloud!"
                } else {
                    "💾 Pekerjaan Keindahan Mobil #${item.noMobil} tersimpan di Room Cache Lokal (Mode Offline). Auditor dapat langsung lanjut bekerja, sinkronisasi berjalan otomatis saat online."
                }
                _keindahanUploadStatus.value = msg
                _eventMessage.emit(msg)
                onResult(true, msg)
                delay(3500)
                if (_keindahanUploadStatus.value == msg) {
                    _keindahanUploadStatus.value = null
                }
            } else {
                val msg = "⚠️ ${res.exceptionOrNull()?.message ?: "Gagal simpan"}"
                _keindahanUploadStatus.value = msg
                _eventMessage.emit(msg)
                onResult(false, msg)
                delay(4000)
                if (_keindahanUploadStatus.value == msg) {
                    _keindahanUploadStatus.value = null
                }
            }
        }
    }

    /**
     * Memaksa sinkronisasi seluruh data inspeksi yang masih tertahan di Room database cache lokal
     */
    fun syncUnsyncedInspections() {
        viewModelScope.launch {
            if (!repository.networkMonitor.isOnline()) {
                _eventMessage.emit("Tidak ada koneksi internet. Data inspeksi tetap aman tersimpan di cache lokal.")
                return@launch
            }
            _isSyncing.value = true
            _keindahanUploadStatus.value = "Menyinkronkan data inspeksi dari cache lokal ke cloud..."
            val (success, failed) = repository.autoSyncPendingInspections { progress ->
                _keindahanUploadStatus.value = progress
            }
            _isSyncing.value = false
            val msg = if (success > 0) {
                "✓ Sinkronisasi berhasil: $success data inspeksi telah dikirim ke cloud!"
            } else if (failed > 0) {
                "⚠️ $failed data belum berhasil disinkronkan, akan dicoba kembali otomatis saat jaringan stabil."
            } else {
                "Semua data inspeksi di cache lokal sudah tersinkron."
            }
            _keindahanUploadStatus.value = msg
            _eventMessage.emit(msg)
            delay(3500)
            if (_keindahanUploadStatus.value == msg) {
                _keindahanUploadStatus.value = null
            }
        }
    }

    fun deletePekerjaanServiceKeindahan(id: Long, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val res = repository.deletePekerjaanServiceKeindahan(id)
            if (res.isSuccess) {
                val msg = "Data Pekerjaan Service Keindahan berhasil dihapus."
                _eventMessage.emit(msg)
                onResult(true, msg)
            } else {
                val msg = "Gagal menghapus: ${res.exceptionOrNull()?.message}"
                _eventMessage.emit(msg)
                onResult(false, msg)
            }
        }
    }

    fun syncPekerjaanServiceKeindahan(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSyncing.value = true
            // Unggah otomatis foto yang belum terupload ke Google Drive
            repository.autoUploadPendingKeindahanPhotos { progress ->
                _keindahanUploadStatus.value = progress
            }
            val res = repository.syncPekerjaanServiceKeindahanFromGoogleSheets()
            _isSyncing.value = false
            if (res.isSuccess) {
                val count = res.getOrNull() ?: 0
                val msg = "Berhasil sinkronisasi $count data Pekerjaan Service Keindahan dari Google Sheets!"
                _eventMessage.emit(msg)
                onResult(true, msg)
            } else {
                val msg = "Gagal sinkronisasi: ${res.exceptionOrNull()?.message}"
                _eventMessage.emit(msg)
                onResult(false, msg)
            }
        }
    }

    fun inisialisasiPengerjaanKeindahan(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isSyncing.value = true
            val scriptUrl = repository.sessionManager.scriptUrl.trim()
            val sheetId = repository.sessionManager.sheetPekerjaanServiceKeindahan.trim()
            val sheetName = repository.sessionManager.sheetPekerjaanServiceKeindahanName.trim()
            val folderId = repository.sessionManager.folderPhotoKeindahanId.trim()
            val res = com.example.data.remote.GoogleAppsScriptHub.inisialisasiPengerjaanKeindahan(
                scriptUrl = scriptUrl,
                spreadsheetId = sheetId,
                sheetName = sheetName,
                folderId = folderId
            )
            _isSyncing.value = false
            if (res.isSuccess) {
                val msg = res.getOrNull() ?: "Inisialisasi Sheet SERVICE_KEINDAHAN & Folder Google Drive Berhasil!"
                _eventMessage.emit(msg)
                onResult(true, msg)
                syncFromGoogleSheets(isManual = false)
            } else {
                val msg = "Gagal inisialisasi: ${res.exceptionOrNull()?.message}"
                _eventMessage.emit(msg)
                onResult(false, msg)
            }
        }
    }

    fun testDrivePermission(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val scriptUrl = repository.sessionManager.scriptUrl.trim()
            val res = com.example.data.remote.GoogleAppsScriptHub.testGoogleScriptDrivePermission(scriptUrl)
            if (res.isSuccess) {
                val msg = res.getOrNull() ?: "✅ Izin Google Drive AKTIF! Web App siap mengunggah foto."
                _eventMessage.emit(msg)
                onResult(true, msg)
            } else {
                val err = res.exceptionOrNull()?.message ?: "Izin Google Drive belum aktif pada Web App."
                _eventMessage.emit(err)
                onResult(false, err)
            }
        }
    }

    fun getCurrentScriptUrl(): String = repository.sessionManager.scriptUrl

    fun updateScriptUrl(newUrl: String) {
        repository.sessionManager.scriptUrl = newUrl.trim()
        viewModelScope.launch {
            _eventMessage.emit("URL Google Apps Script diperbarui!")
        }
    }

    // CRUD Claim
    fun saveClaim(claim: ClaimEntity) {
        viewModelScope.launch {
            repository.saveClaim(claim)
            _eventMessage.emit("Data Klaim ${claim.nomerRegistrasi} (Mobil #${claim.noMobil}) disimpan.")
        }
    }

    fun deleteClaim(id: Long) {
        viewModelScope.launch {
            repository.deleteClaim(id)
            _eventMessage.emit("Data klaim berhasil dihapus.")
        }
    }

    // CRUD Pengerjaan Cat
    fun savePengerjaanCat(
        cat: PengerjaanCatEntity,
        isEdit: Boolean = false,
        oldNoRegistrasi: String = "",
        oldNoMobil: String = "",
        oldTimestamp: String = ""
    ) {
        viewModelScope.launch {
            val res = repository.savePengerjaanCat(cat, isEdit, oldNoRegistrasi, oldNoMobil, oldTimestamp)
            val actionText = if (isEdit) "diperbarui" else "ditambahkan"
            if (res.isSuccess) {
                _eventMessage.emit("Data Pengerjaan Cat Mobil #${cat.noMobil} berhasil $actionText & tersinkron ke Google Sheets!")
            } else {
                _eventMessage.emit("⚠️ Data Cat tersimpan di HP, tapi gagal ke Sheets: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun syncPengerjaanCat() {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.syncPengerjaanCatFromGoogleSheets()
            _isSyncing.value = false
            res.onSuccess { count ->
                _eventMessage.emit("Berhasil sinkronisasi $count data Pengerjaan Cat dari Google Sheets!")
            }.onFailure { err ->
                _eventMessage.emit("Gagal sinkronisasi Cat: ${err.message}")
            }
        }
    }

    fun deletePengerjaanCat(id: Long) {
        viewModelScope.launch {
            repository.deletePengerjaanCat(id)
            _eventMessage.emit("Data pengerjaan cat berhasil dihapus.")
        }
    }

    // CRUD Ban, Kopling, Baterai
    fun saveBan(ban: BanEntity) {
        viewModelScope.launch {
            repository.saveBan(ban)
            _eventMessage.emit("Data Ban Mobil #${ban.noMobil} disimpan.")
            triggerSyncOnChange()
        }
    }

    fun deleteBan(id: Long) {
        viewModelScope.launch {
            repository.deleteBan(id)
            _eventMessage.emit("Data ban dihapus.")
            triggerSyncOnChange()
        }
    }

    fun saveKopling(kopling: KoplingEntity) {
        viewModelScope.launch {
            repository.saveKopling(kopling)
            _eventMessage.emit("Data Kopling Mobil #${kopling.noMobil} disimpan.")
            triggerSyncOnChange()
        }
    }

    fun deleteKopling(id: Long) {
        viewModelScope.launch {
            repository.deleteKopling(id)
            _eventMessage.emit("Data kopling dihapus.")
            triggerSyncOnChange()
        }
    }

    fun saveBateray(bateray: BaterayEntity) {
        viewModelScope.launch {
            repository.saveBateray(bateray)
            _eventMessage.emit("Data Baterai Mobil #${bateray.noMobil} disimpan.")
            triggerSyncOnChange()
        }
    }

    fun deleteBateray(id: Long) {
        viewModelScope.launch {
            repository.deleteBateray(id)
            _eventMessage.emit("Data baterai dihapus.")
            triggerSyncOnChange()
        }
    }

    fun saveReservasi(reservasi: ReservasiEntity) {
        viewModelScope.launch {
            repository.saveReservasi(reservasi)
            _eventMessage.emit("Data Reservasi Mobil #${reservasi.noMobil} disimpan.")
            triggerSyncOnChange()
        }
    }

    fun deleteReservasi(id: Long) {
        viewModelScope.launch {
            repository.deleteReservasi(id)
            _eventMessage.emit("Data reservasi dihapus.")
            triggerSyncOnChange()
        }
    }

    fun clearAllReservasiAndSync() {
        viewModelScope.launch {
            try {
                repository.clearReservasiAndKomponenPendukungData()
                _eventMessage.emit("Data reservasi beserta komponen pendukung berhasil dihapus. Menyinkronkan ulang...")
                syncFromGoogleSheets(isManual = true)
            } catch (e: Throwable) {
                _eventMessage.emit("Gagal menghapus data: ${e.message}")
            }
        }
    }

    fun clearAllReservasiAndKomponenPendukung() {
        viewModelScope.launch {
            try {
                repository.clearReservasiAndKomponenPendukungData()
                _eventMessage.emit("Data reservasi beserta komponen pendukung (Ban, Kopling, Bateray) berhasil dihapus.")
            } catch (e: Throwable) {
                _eventMessage.emit("Gagal menghapus data: ${e.message}")
            }
        }
    }

    fun saveArgo(argo: ArgoEntity) {
        viewModelScope.launch {
            repository.saveArgo(argo)
            _eventMessage.emit("Data ARGO Mobil #${argo.noMobil} disimpan.")
            triggerSyncOnChange()
        }
    }

    fun deleteArgo(id: Long) {
        viewModelScope.launch {
            repository.deleteArgo(id)
            _eventMessage.emit("Data ARGO dihapus.")
            triggerSyncOnChange()
        }
    }

    fun savePrediksiPart(item: PrediksiPartEntity, onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            val isApproved = item.aprovel.trim().equals("Disetujui", ignoreCase = true) ||
                item.aprovel.contains("SETUJU", ignoreCase = true) ||
                item.aprovel.contains("APPROV", ignoreCase = true) ||
                item.aprovel.contains("ACC", ignoreCase = true)
            if (isApproved && item.status.trim().equals("SELESAI", ignoreCase = true) && item.keterangan.trim().isBlank()) {
                val errorMsg = "Keterangan wajib diisi jika Status SELESAI!"
                _eventMessage.emit(errorMsg)
                onResult?.invoke(false, errorMsg)
                return@launch
            }

            val isSelesai = item.status.trim().equals("SELESAI", ignoreCase = true) ||
                item.status.contains("SELESAI", ignoreCase = true) ||
                item.status.contains("DONE", ignoreCase = true)

            // Pastikan ARGO_SEKARANG tidak diupdate otomatis jika status SELESAI
            val matchingArgo = if (!isSelesai) allArgo.value.find { com.example.ui.screens.isVehicleMatch(it.noMobil, item.noMobil) } else null
            val latestArgoKm = matchingArgo?.km?.trim().orEmpty()
            val finalArgo = if (!isSelesai && latestArgoKm.isNotBlank()) latestArgoKm else item.argoSekarang.trim()

            val kmInputLong = item.kmInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
            val prediksiKmLong = item.prediksiKm.filter { it.isDigit() }.toLongOrNull() ?: 0L
            val existingTargetLong = item.kmTarget.filter { it.isDigit() }.toLongOrNull()
            val targetLong = if (existingTargetLong != null && existingTargetLong > 0L) {
                existingTargetLong
            } else if (kmInputLong > 0L || prediksiKmLong > 0L) {
                kmInputLong + prediksiKmLong
            } else null
            val targetStr = targetLong?.toString() ?: item.kmTarget.trim()

            val targetForCalc = targetStr.filter { it.isDigit() || it == '-' }.toLongOrNull() ?: targetLong
            val argoLong = finalArgo.filter { it.isDigit() || it == '-' }.toLongOrNull()
            val estimasiStr = if (targetForCalc != null && argoLong != null) {
                (argoLong - targetForCalc).toString()
            } else {
                item.estimasiKm.trim()
            }

            val normalizedAprovel = when {
                item.aprovel.contains("SETUJU", ignoreCase = true) || item.aprovel.contains("APPROV", ignoreCase = true) || item.aprovel.contains("ACC", ignoreCase = true) -> "Disetujui"
                item.aprovel.contains("TOLAK", ignoreCase = true) || item.aprovel.contains("REJECT", ignoreCase = true) -> "Ditolak"
                item.aprovel.isNotBlank() && (item.aprovel.contains("TUNGGU", ignoreCase = true) || item.aprovel.contains("PENDING", ignoreCase = true) || item.aprovel.contains("PROSES", ignoreCase = true)) -> "Menunggu"
                item.aprovel.isNotBlank() -> item.aprovel
                else -> "Menunggu"
            }

            val finalKmInput = if (item.kmInput.isNotBlank()) item.kmInput.trim() else latestArgoKm

            val finalItem = item.copy(
                kmInput = finalKmInput,
                kmTarget = targetStr,
                argoSekarang = finalArgo,
                estimasiKm = estimasiStr,
                aprovel = normalizedAprovel
            )

            val res = repository.savePrediksiPart(finalItem)
            if (res.isSuccess) {
                _eventMessage.emit("Data Prediksi Part Mobil #${finalItem.noMobil} berhasil disimpan & diupdate ke Google Sheets!")
                onResult?.invoke(true, "Data Prediksi Part Mobil #${finalItem.noMobil} berhasil disimpan & diupdate ke Google Sheets!")
            } else {
                val errMsg = res.exceptionOrNull()?.message ?: "Gagal update Google Sheets"
                _eventMessage.emit(errMsg)
                onResult?.invoke(false, errMsg)
            }
            triggerSyncOnChange()
        }
    }

    fun deletePrediksiPart(id: Long, onResult: ((Boolean, String) -> Unit)? = null) {
        viewModelScope.launch {
            val res = repository.deletePrediksiPart(id)
            if (res.isSuccess) {
                _eventMessage.emit("Data Prediksi Part berhasil dihapus dari HP & Google Sheets.")
                onResult?.invoke(true, "Data Prediksi Part berhasil dihapus.")
            } else {
                _eventMessage.emit("Data Prediksi Part dihapus dari HP.")
                onResult?.invoke(true, "Data Prediksi Part dihapus dari HP.")
            }
            triggerSyncOnChange()
        }
    }

    fun savePengerjaanTeknik(item: PengerjaanTeknikEntity) {
        viewModelScope.launch {
            repository.savePengerjaanTeknik(item)
            _eventMessage.emit("Data Riwayat Service Teknik Mobil #${item.noMobil} disimpan.")
            triggerSyncOnChange()
        }
    }

    fun deletePengerjaanTeknik(id: Long) {
        viewModelScope.launch {
            repository.deletePengerjaanTeknik(id)
            _eventMessage.emit("Data Riwayat Service Teknik dihapus.")
            triggerSyncOnChange()
        }
    }

    // CRUD Master Kendaraan
    fun saveKendaraan(kendaraan: MasterKendaraanEntity) {
        viewModelScope.launch {
            repository.saveKendaraan(kendaraan)
            _eventMessage.emit("Master Kendaraan #${kendaraan.noMobil} berhasil disimpan.")
            triggerSyncOnChange()
        }
    }

    fun deleteKendaraan(id: Int) {
        viewModelScope.launch {
            repository.deleteKendaraan(id)
            _eventMessage.emit("Kendaraan berhasil dihapus.")
            triggerSyncOnChange()
        }
    }

    // CRUD User
    fun saveUser(
        user: UserEntity,
        originalNip: String = "",
        originalUsername: String = ""
    ) {
        viewModelScope.launch {
            val res = repository.saveUser(
                user = user,
                originalNip = originalNip,
                originalUsername = originalUsername
            )
            res.onSuccess { msg ->
                _eventMessage.emit(msg)
            }.onFailure { err ->
                _eventMessage.emit("⚠️ ${err.message ?: "Gagal menyimpan user"}")
            }
            repository.syncUserFromGoogleSheets()
            _currentUser.value = repository.sessionManager.getCurrentUser()
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            val res = repository.deleteUser(id)
            res.onSuccess { msg ->
                _eventMessage.emit(msg)
            }.onFailure { err ->
                _eventMessage.emit("⚠️ ${err.message ?: "Gagal menghapus user"}")
            }
            repository.syncUserFromGoogleSheets()
        }
    }

    /**
     * Memantau perubahan pada sheet USER di Google Sheets secara otomatis.
     * Setiap di Google Sheets ada perubahan (tambah, edit, atau hapus baris/sel),
     * data di aplikasi akan langsung melakukan perubahan agar selalu sama persis.
     */
    suspend fun checkAndAutoSyncUserSheet() {
        try {
            if (repository.checkUserSheetHasChanged()) {
                val res = repository.syncUserFromGoogleSheets()
                res.onSuccess { count ->
                    _currentUser.value = repository.sessionManager.getCurrentUser()
                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                    _lastSyncTime.value = timeStr
                    _eventMessage.emit("Perubahan di Google Sheet USER terdeteksi — $count user otomatis diperbarui ($timeStr)")
                }
            }
        } catch (_: Exception) {}
    }

    fun syncUserFromGoogleSheets(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _isSyncing.value = true
            val res = repository.syncUserFromGoogleSheets()
            if (!silent) _isSyncing.value = false
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            _lastSyncTime.value = timeStr
            _currentUser.value = repository.sessionManager.getCurrentUser()
            if (!silent) {
                res.onSuccess { count ->
                    _eventMessage.emit("Data User Bengkel disinkronkan dengan Google Sheets ($count user, pukul $timeStr)!")
                }.onFailure { err ->
                    _eventMessage.emit("Gagal sinkronisasi user: ${err.message}")
                }
            }
        }
    }

    fun initializeDefaultUsers() {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.initializeDefaultUsers()
            _isSyncing.value = false
            res.onSuccess { count ->
                _eventMessage.emit("Berhasil sinkronisasi $count data User Bengkel dari Google Sheets!")
            }.onFailure { err ->
                _eventMessage.emit("Gagal sinkronisasi user: ${err.message}")
            }
        }
    }

    fun saveGoogleSheetsSettings(
        laporanId: String,
        laporanSheetName: String = "LAPORAN KENDARAAN",
        teknikId: String = SessionManager.DEFAULT_SERVICE_TEKNIK_SHEET_ID,
        teknikSheetName: String = SessionManager.DEFAULT_SERVICE_TEKNIK_SHEET_NAME,
        keindahanId: String = SessionManager.DEFAULT_SERVICE_KEINDAHAN_SHEET_ID,
        keindahanSheetName: String = SessionManager.DEFAULT_SERVICE_KEINDAHAN_SHEET_NAME,
        banId: String = SessionManager.DEFAULT_BAN_SHEET_ID,
        banSheetName: String = SessionManager.DEFAULT_BAN_SHEET_NAME,
        koplingId: String = SessionManager.DEFAULT_KOPLING_SHEET_ID,
        koplingSheetName: String = SessionManager.DEFAULT_KOPLING_SHEET_NAME,
        baterayId: String = SessionManager.DEFAULT_BATERAY_SHEET_ID,
        bateraySheetName: String = SessionManager.DEFAULT_BATERAY_SHEET_NAME,
        argoId: String = SessionManager.DEFAULT_ARGO_SHEET_ID,
        argoSheetName: String = SessionManager.DEFAULT_ARGO_SHEET_NAME,
        reservasiId: String = SessionManager.DEFAULT_RESERVASI_SHEET_ID,
        reservasiSheetName: String = SessionManager.DEFAULT_RESERVASI_SHEET_NAME,
        pengerjaanTeknikId: String = SessionManager.DEFAULT_PENGERJAAN_TEKNIK_SHEET_ID,
        pengerjaanTeknikSheetName: String = SessionManager.DEFAULT_PENGERJAAN_TEKNIK_SHEET_NAME,
        pekerjaanKeindahanId: String = SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID,
        pekerjaanKeindahanSheetName: String = SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME,
        riwayatSkiId: String = SessionManager.DEFAULT_RIWAYAT_SKI_SHEET_ID,
        riwayatSkiSheetName: String = SessionManager.DEFAULT_RIWAYAT_SKI_SHEET_NAME,
        userId: String,
        kendaraanId: String,
        kendaraanSheetName: String = "MASTER_KENDARAAN",
        scriptUrl: String,
        folderPhotoKeindahanId: String = SessionManager.DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID
    ) {
        repository.sessionManager.sheetLaporanHarian = laporanId
        repository.sessionManager.sheetLaporanHarianName = laporanSheetName
        repository.sessionManager.sheetServiceTeknik = teknikId
        repository.sessionManager.sheetServiceTeknikName = teknikSheetName
        repository.sessionManager.sheetServiceKeindahan = keindahanId
        repository.sessionManager.sheetServiceKeindahanName = keindahanSheetName
        repository.sessionManager.sheetBan = banId
        repository.sessionManager.sheetBanName = banSheetName
        repository.sessionManager.sheetKopling = koplingId
        repository.sessionManager.sheetKoplingName = koplingSheetName
        repository.sessionManager.sheetBateray = baterayId
        repository.sessionManager.sheetBaterayName = bateraySheetName
        val cleanArgoId = if (argoId.isBlank() || argoId == "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU") SessionManager.DEFAULT_ARGO_SHEET_ID else argoId
        val cleanArgoSheetName = if (argoSheetName.isBlank()) SessionManager.DEFAULT_ARGO_SHEET_NAME else argoSheetName
        repository.sessionManager.sheetArgo = cleanArgoId
        repository.sessionManager.sheetArgoName = cleanArgoSheetName
        val cleanReservasiSheet = if (reservasiSheetName.isBlank() || reservasiSheetName.equals("GM", ignoreCase = true) || reservasiSheetName.equals("RESERVASI", ignoreCase = true)) {
            SessionManager.DEFAULT_RESERVASI_SHEET_NAME
        } else {
            reservasiSheetName
        }
        repository.sessionManager.sheetReservasi = reservasiId
        repository.sessionManager.sheetReservasiName = cleanReservasiSheet
        repository.sessionManager.sheetPengerjaanTeknik = pengerjaanTeknikId
        repository.sessionManager.sheetPengerjaanTeknikName = pengerjaanTeknikSheetName
        repository.sessionManager.sheetPekerjaanServiceKeindahan = if (pekerjaanKeindahanId.isBlank()) SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID else pekerjaanKeindahanId
        repository.sessionManager.sheetPekerjaanServiceKeindahanName = if (pekerjaanKeindahanSheetName.isBlank()) SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME else pekerjaanKeindahanSheetName
        repository.sessionManager.sheetRiwayatSki = riwayatSkiId
        repository.sessionManager.sheetRiwayatSkiName = riwayatSkiSheetName
        repository.sessionManager.sheetUser = userId
        repository.sessionManager.sheetKendaraan = kendaraanId
        repository.sessionManager.sheetKendaraanName = kendaraanSheetName
        repository.sessionManager.scriptUrl = scriptUrl
        repository.sessionManager.folderPhotoKeindahanId = folderPhotoKeindahanId
        viewModelScope.launch {
            _eventMessage.emit("Pengaturan ID Google Sheets berhasil disimpan!")
        }
    }

    // CRUD Riwayat Service SKI
    fun saveRiwayatSki(item: RiwayatSkiEntity) {
        viewModelScope.launch {
            repository.saveRiwayatSki(item)
            _eventMessage.emit("Data Riwayat Service SKI berhasil disimpan!")
        }
    }

    fun deleteRiwayatSki(id: Long) {
        viewModelScope.launch {
            repository.deleteRiwayatSki(id)
            _eventMessage.emit("Data Riwayat Service SKI berhasil dihapus!")
        }
    }

    // CRUD Riwayat Claim
    fun saveRiwayatClaim(item: RiwayatClaimEntity) {
        viewModelScope.launch {
            repository.saveRiwayatClaim(item)
            _eventMessage.emit("Data Riwayat Claim berhasil disimpan!")
        }
    }

    fun deleteRiwayatClaim(id: Long) {
        viewModelScope.launch {
            repository.deleteRiwayatClaim(id)
            _eventMessage.emit("Data Riwayat Claim berhasil dihapus!")
        }
    }

    // Legacy wrappers
    fun addOrUpdateKendaraan(kendaraan: MasterKendaraanEntity) = saveKendaraan(kendaraan)
    fun addOrUpdateUser(user: UserEntity) = saveUser(user)
    fun deleteUser(nip: String) = viewModelScope.launch { repository.deleteUser(nip) }
    fun addOrUpdatePool(pool: MasterPoolEntity) = viewModelScope.launch { repository.insertPool(pool) }
    fun deletePool(id: Int) = viewModelScope.launch { repository.deletePool(id) }
    fun updatePhotoSetting(setting: PengaturanNamaPhotoEntity) = viewModelScope.launch { repository.updatePhotoSetting(setting) }
    fun downloadMasterData() = syncFromGoogleSheets(isManual = true)
    fun syncPendingAudits() = syncFromGoogleSheets(isManual = true)
}

data class OrderChoiceState(
    val orderNo: String,
    val teknikItem: PengerjaanTeknikEntity?,
    val skiItem: RiwayatSkiEntity? = null
)

/**
 * Helper komparasi nomor order yang fleksibel (mendukung format numerik, spasi, prefix)
 */
fun isOrderMatch(orderA: String, orderB: String): Boolean {
    val a = orderA.trim()
    val b = orderB.trim()
    if (a.isBlank() || b.isBlank()) return false
    if (a.equals(b, ignoreCase = true)) return true

    val cleanA = a.removePrefix("#").removePrefix("ORD-").removePrefix("WO-").trim()
    val cleanB = b.removePrefix("#").removePrefix("ORD-").removePrefix("WO-").trim()
    if (cleanA.equals(cleanB, ignoreCase = true)) return true

    val numA = cleanA.toLongOrNull()
    val numB = cleanB.toLongOrNull()
    if (numA != null && numB != null && numA == numB) return true

    return false
}
