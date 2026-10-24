package com.example.data.repository

import android.content.Context
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.*
import com.example.data.remote.GoogleAppsScriptHub
import com.example.data.session.SessionManager
import com.example.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuditRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context),
    val sessionManager: SessionManager = SessionManager(context)
) {
    val networkMonitor: NetworkMonitor = NetworkMonitor(context)
    val userDao = database.userDao()
    val kendaraanDao = database.masterKendaraanDao()
    val laporanDao = database.laporanHarianDao()
    val teknikDao = database.serviceTeknikDao()
    val keindahanDao = database.serviceKeindahanDao()
    val claimDao = database.claimDao()
    val catDao = database.pengerjaanCatDao()
    val banDao = database.banDao()
    val koplingDao = database.koplingDao()
    val baterayDao = database.baterayDao()
    val reservasiDao = database.reservasiDao()
    val argoDao = database.argoDao()
    val prediksiPartDao = database.prediksiPartDao()
    val pengerjaanTeknikDao = database.pengerjaanTeknikDao()
    val riwayatSkiDao = database.riwayatSkiDao()
    val riwayatClaimDao = database.riwayatClaimDao()
    val pekerjaanKeindahanDao = database.pekerjaanServiceKeindahanDao()
    val poolDao = database.masterPoolDao()
    val photoSettingDao = database.pengaturanNamaPhotoDao()
    val auditDao = database.dataAuditDao()

    // Flows
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allKendaraan: Flow<List<MasterKendaraanEntity>> = kendaraanDao.getAllKendaraan()
    val allLaporan: Flow<List<LaporanHarianEntity>> = laporanDao.getAllLaporan()
    val allServiceTeknik: Flow<List<ServiceTeknikEntity>> = teknikDao.getAllServiceTeknik()
    val allServiceKeindahan: Flow<List<ServiceKeindahanEntity>> = keindahanDao.getAllServiceKeindahan()
    val allPekerjaanServiceKeindahan: Flow<List<PekerjaanServiceKeindahanEntity>> = pekerjaanKeindahanDao.getAllPekerjaanServiceKeindahan()
    val allClaims: Flow<List<ClaimEntity>> = claimDao.getAllClaims()
    val allPengerjaanCat: Flow<List<PengerjaanCatEntity>> = catDao.getAllPengerjaanCat()
    val allBan: Flow<List<BanEntity>> = banDao.getAllBan()
    val allKopling: Flow<List<KoplingEntity>> = koplingDao.getAllKopling()
    val allBateray: Flow<List<BaterayEntity>> = baterayDao.getAllBateray()
    val allReservasi: Flow<List<ReservasiEntity>> = reservasiDao.getAllReservasi()
    val allArgo: Flow<List<ArgoEntity>> = argoDao.getAllArgo()
    val allPrediksiPart: Flow<List<PrediksiPartEntity>> = prediksiPartDao.getAllPrediksiPart()
    val allPengerjaanTeknik: Flow<List<PengerjaanTeknikEntity>> = pengerjaanTeknikDao.getAllPengerjaanTeknik()
    val allRiwayatSki: Flow<List<RiwayatSkiEntity>> = riwayatSkiDao.getAllRiwayatSki()
    val allRiwayatClaim: Flow<List<RiwayatClaimEntity>> = riwayatClaimDao.getAllRiwayatClaim()
    val allPools: Flow<List<MasterPoolEntity>> = poolDao.getAllPools()
    val allPhotoSettings: Flow<List<PengaturanNamaPhotoEntity>> = photoSettingDao.getAllNamaPhoto()
    val allAudits: Flow<List<DataAuditEntity>> = auditDao.getAllAudits()

    // Authentication for User Bengkel (Mendukung Username, NIP, maupun Nama Mekanik)
    suspend fun login(username: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanInput = username.trim()
        val cleanPass = password.trim()



        // Helper untuk mencari user yang cocok dari seluruh daftar user di database lokal
        // Menangani kasus di mana di Google Sheets terdapat lebih dari 1 baris dengan NIP/Username/Nama yang sama
        // (misal 1 baris belum diisi password dan 1 baris lagi sudah ada password-nya, atau user login dengan NIP == Password)
        suspend fun findAuthenticatedUser(): Pair<UserEntity?, List<UserEntity>> {
            val allUsersList = userDao.getAllUsersList()
            val normalizedInput = cleanInput.lowercase().replace("\\s+".toRegex(), " ")
            val compactInput = cleanInput.lowercase().replace("[^a-z0-9]".toRegex(), "")

            val candidates = allUsersList.filter { u ->
                val uUser = u.username.trim().lowercase()
                val uNip = u.nip.trim().lowercase()
                val uNama = u.namaMekanik.trim().lowercase().replace("\\s+".toRegex(), " ")
                val uNamaCompact = u.namaMekanik.trim().lowercase().replace("[^a-z0-9]".toRegex(), "")

                uUser == normalizedInput ||
                    uNip == normalizedInput ||
                    uNama == normalizedInput ||
                    (compactInput.isNotBlank() && (uUser == compactInput || uNip == compactInput || uNamaCompact == compactInput))
            }

            if (candidates.isEmpty()) return Pair(null, emptyList())

            // 1. Prioritas utama: Baris yang kolom PASSWORD-nya sama persis dengan input password (case-insensitive & trim)
            val exactPassMatch = candidates.firstOrNull { u ->
                val dbPass = u.password.trim()
                dbPass.isNotBlank() && (dbPass == cleanPass || dbPass.equals(cleanPass, ignoreCase = true))
            }
            if (exactPassMatch != null) return Pair(exactPassMatch, candidates)

            // 2. Jika di Google Sheets kolom PASSWORD masih kosong (""):
            //    Izinkan masuk jika:
            //    - User mengosongkan password, ATAU
            //    - User mengisi password yang sama dengan Username / NIP / Nama Mekanik (misal user & password diisi sama)
            val blankPassCandidate = candidates.firstOrNull { it.password.trim().isBlank() }
            if (blankPassCandidate != null) {
                val uUser = blankPassCandidate.username.trim()
                val uNip = blankPassCandidate.nip.trim()
                if (cleanPass.isBlank() ||
                    cleanPass.equals(cleanInput, ignoreCase = true) ||
                    cleanPass.equals(uUser, ignoreCase = true) ||
                    cleanPass.equals(uNip, ignoreCase = true)
                ) {
                    return Pair(blankPassCandidate, candidates)
                }
            }

            return Pair(null, candidates)
        }

        // Cek terlebih dahulu dengan data lokal saat ini
        var (matchedUser, candidates) = findAuthenticatedUser()

        // Jika belum cocok, selalu paksa sinkronisasi terbaru dari Google Sheets (Sheet: USER)
        // agar perubahan baru di Google Sheets langsung terbaca saat itu juga
        if (matchedUser == null) {
            try {
                syncUserFromGoogleSheets()
                val afterSync = findAuthenticatedUser()
                matchedUser = afterSync.first
                candidates = afterSync.second
            } catch (e: Exception) {
                android.util.Log.e("AuditRepository", "Gagal auto-download user saat login: ${e.message}")
            }
        }

        if (matchedUser != null) {
            sessionManager.saveSession(matchedUser)
            Result.success(matchedUser)
        } else if (candidates.isNotEmpty()) {
            Result.failure(Exception("Password salah untuk user '${candidates.first().namaMekanik}'!"))
        } else {
            Result.failure(Exception("Username / NIP $cleanInput tidak ditemukan di Database"))
        }
    }

    // CRUD Laporan Harian (ID & Timestamp sebagai Identitas Unik agar edit data mengubah baris yang tepat)
    suspend fun saveLaporan(laporan: LaporanHarianEntity): Result<String> = withContext(Dispatchers.IO) {
        val existingById = if (laporan.id != 0L) laporanDao.getLaporanById(laporan.id) else null
        val existingByTimestamp = if (existingById == null && laporan.timestamp.isNotBlank()) {
            laporanDao.getLaporanByTimestamp(laporan.timestamp.trim())
        } else null
        val existing = existingById ?: existingByTimestamp

        val isEdit = existing != null || laporan.id != 0L
        val savedId = if (existing != null) {
            val toUpdate = laporan.copy(id = existing.id, isSynced = false)
            laporanDao.updateLaporan(toUpdate)
            existing.id
        } else if (laporan.id != 0L) {
            laporanDao.insertLaporan(laporan.copy(isSynced = false))
            laporan.id
        } else {
            laporanDao.insertLaporan(laporan.copy(isSynced = false))
        }

        val currentEntity = laporan.copy(id = savedId)

        // Langsung kirim perubahan ke Google Sheets agar data di Google Sheets ikut terupdate
        val scriptUrl = sessionManager.scriptUrl.trim()
        val sheetName = sessionManager.sheetLaporanHarianName.trim().ifBlank { "LAPORAN KENDARAAN" }
        val sheetId = sessionManager.sheetLaporanHarian.trim().ifBlank { SessionManager.DEFAULT_LAPORAN_HARIAN_SHEET_ID }
        if (scriptUrl.isNotBlank()) {
            val remoteResult = GoogleAppsScriptHub.sendLaporanHarianToGoogleSheet(
                scriptUrl = scriptUrl,
                laporan = currentEntity,
                sheetName = sheetName,
                spreadsheetId = sheetId,
                isEditMode = isEdit
            )
            if (remoteResult.isSuccess) {
                laporanDao.markAsSynced(savedId)
                val actionDesc = if (isEdit) "diperbarui pada ID #$savedId" else "disimpan"
                Result.success("Laporan Mobil #${laporan.noMobil} berhasil $actionDesc di HP & Google Sheets ($sheetName)!")
            } else {
                val err = remoteResult.exceptionOrNull()?.message ?: "Gagal terhubung ke Google Sheets"
                Result.failure(Exception("Tersimpan di HP (Lokal), tetapi GAGAL update Google Sheets: $err"))
            }
        } else {
            Result.failure(Exception("Tersimpan di HP (Lokal). Untuk sinkron ke Google Sheets, pasang URL Web App di menu PENGATURAN GOOGLE SCRIPT."))
        }
    }

    private var lastKnownLaporanSig: String = ""
    private var lastKnownUserSig: String = ""

    /**
     * Helper untuk mengambil signature (sig) perubahan Google Sheets melalui endpoint GViz yang sangat ringan.
     */
    private fun fetchSheetGvizSig(sheetId: String, sheetName: String): String {
        return try {
            val cleanId = GoogleAppsScriptHub.cleanSheetId(sheetId)
            if (cleanId.isBlank()) return ""
            val encodedSheet = java.net.URLEncoder.encode(sheetName, "UTF-8")
            val cacheBuster = System.currentTimeMillis()
            val url = "https://docs.google.com/spreadsheets/d/$cleanId/gviz/tq?tqx=out:json&sheet=$encodedSheet&tq=select%20count(A)&_cb=$cacheBuster"

            val request = okhttp3.Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", "Mozilla/5.0")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .build()

            GoogleAppsScriptHub.httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful && body.contains("google.visualization.Query.setResponse")) {
                    val sigMatch = Regex(""""sig"\s*:\s*"([^"]+)"""").find(body)?.groupValues?.getOrNull(1)
                    val countMatch = Regex(""""v"\s*:\s*([0-9.]+)""").find(body)?.groupValues?.getOrNull(1)
                    if (!sigMatch.isNullOrBlank() || !countMatch.isNullOrBlank()) {
                        "${sigMatch ?: ""}_${countMatch ?: ""}"
                    } else ""
                } else ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Memeriksa khusus apakah ada perubahan di Google Sheets pada tab USER BENGKEL.
     * Setiap ada perubahan di Google Sheet USER (tambah, edit, atau hapus baris/sel),
     * nilai sig akan berubah sehingga aplikasi langsung menyinkronkan data USER.
     */
    suspend fun checkUserSheetHasChanged(): Boolean = withContext(Dispatchers.IO) {
        try {
            val userSheetId = sessionManager.sheetUser.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_ID }
            val userSheetName = sessionManager.sheetUserName.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_NAME }
            val currentSig = fetchSheetGvizSig(userSheetId, userSheetName)
            if (currentSig.isNotBlank()) {
                if (lastKnownUserSig.isNotBlank() && currentSig != lastKnownUserSig) {
                    lastKnownUserSig = currentSig
                    return@withContext true
                }
                lastKnownUserSig = currentSig
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Memeriksa apakah ada perubahan di Google Sheets tanpa harus mengunduh data CSV penuh.
     * Menggunakan query Google Visualization (GViz) yang sangat ringan (< 200 bytes) dan kilat (< 100ms).
     * Jika terjadi perubahan (penambahan, penghapusan, atau pengeditan baris di Google Sheets),
     * GViz menghasilkan signature "sig" yang berbeda atau jumlah baris yang berbeda.
     */
    suspend fun checkGoogleSheetsHasChanged(): Boolean = withContext(Dispatchers.IO) {
        try {
            var changed = false

            val laporanSheetId = sessionManager.sheetLaporanHarian.trim().ifBlank { SessionManager.DEFAULT_LAPORAN_HARIAN_SHEET_ID }
            val laporanSheetName = sessionManager.sheetLaporanHarianName.trim().ifBlank { "LAPORAN KENDARAAN" }
            val currentLaporanSig = fetchSheetGvizSig(laporanSheetId, laporanSheetName)
            if (currentLaporanSig.isNotBlank()) {
                if (lastKnownLaporanSig.isNotBlank() && currentLaporanSig != lastKnownLaporanSig) {
                    changed = true
                }
                lastKnownLaporanSig = currentLaporanSig
            }

            if (checkUserSheetHasChanged()) {
                changed = true
            }

            changed
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteLaporan(id: Long) = withContext(Dispatchers.IO) {
        laporanDao.deleteLaporanById(id)
    }

    // CRUD Service Teknik
    suspend fun saveServiceTeknik(service: ServiceTeknikEntity): Long = withContext(Dispatchers.IO) {
        if (service.id == 0L) teknikDao.insertServiceTeknik(service) else {
            teknikDao.updateServiceTeknik(service)
            service.id
        }
    }

    suspend fun deleteServiceTeknik(id: Long) = withContext(Dispatchers.IO) {
        teknikDao.deleteServiceTeknikById(id)
    }

    // CRUD Service Keindahan
    suspend fun saveServiceKeindahan(service: ServiceKeindahanEntity): Long = withContext(Dispatchers.IO) {
        if (service.id == 0L) keindahanDao.insertServiceKeindahan(service) else {
            keindahanDao.updateServiceKeindahan(service)
            service.id
        }
    }

    suspend fun deleteServiceKeindahan(id: Long) = withContext(Dispatchers.IO) {
        keindahanDao.deleteServiceKeindahanById(id)
    }

    // CRUD PEKERJAAN SERVICE KEINDAHAN (ROOM DATABASE CACHE & AUTO-SYNC)
    // Database ID: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30 | Sheet: SERVICE_KEINDAHAN | Folder Drive Foto: 1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6
    suspend fun savePekerjaanServiceKeindahan(
        item: PekerjaanServiceKeindahanEntity,
        onProgress: (String) -> Unit = {}
    ): Result<PekerjaanServiceKeindahanEntity> = withContext(Dispatchers.IO) {
        val scriptUrl = sessionManager.scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        val sheetName = sessionManager.sheetPekerjaanServiceKeindahanName.trim().ifBlank { SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME }
        val sheetId = sessionManager.sheetPekerjaanServiceKeindahan.trim().ifBlank { SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID }
        val folderId = sessionManager.folderPhotoKeindahanId.trim().ifBlank { SessionManager.DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID }

        val mob = if (item.noMobil.isNotBlank()) item.noMobil.trim().uppercase() else "MOBIL"
        var pDepan = item.photoDepan.trim()
        var pLh = item.photoSampingLh.trim()
        var pInterior = item.photoInterior.trim()
        var pBelakang = item.photoBelakang.trim()
        var pRh = item.photoSampingRh.trim()

        // 1. Simpan segera ke Room database cache lokal agar data inspeksi langsung tersimpan aman di HP
        val unixId = if (item.id <= 0L) System.currentTimeMillis() else item.id
        var currentEntity = item.copy(
            id = unixId,
            photoDepan = pDepan,
            photoSampingLh = pLh,
            photoInterior = pInterior,
            photoBelakang = pBelakang,
            photoSampingRh = pRh,
            isSynced = false
        )
        pekerjaanKeindahanDao.insertPekerjaanServiceKeindahan(currentEntity)
        val savedId = unixId

        // 2. Jika offline (tidak ada internet), selesai seketika di Room cache tanpa blocking
        val isOnline = networkMonitor.isOnline()
        if (!isOnline) {
            onProgress("Tersimpan di Room Database Cache lokal (Mode Offline). Auditor dapat langsung lanjut bekerja.")
            return@withContext Result.success(currentEntity)
        }

        // 3. Jika online, coba unggah foto lokal ke Google Drive
        val photoSlots = listOf(
            Triple("Depan", pDepan, "DEPAN"),
            Triple("Samping LH", pLh, "LH"),
            Triple("Interior", pInterior, "INTERIOR"),
            Triple("Belakang", pBelakang, "BELAKANG"),
            Triple("Samping RH", pRh, "RH")
        )
        val pendingCount = photoSlots.count { it.second.isNotBlank() && !it.second.startsWith("http", ignoreCase = true) }

        if (scriptUrl.isNotBlank() && pendingCount > 0) {
            var step = 0

            suspend fun uploadSlotFast(
                localPath: String,
                slotName: String,
                prefix: String
            ): String {
                if (localPath.isBlank() || localPath.startsWith("http", ignoreCase = true)) {
                    return localPath
                }
                if (!networkMonitor.isOnline()) return localPath

                step++
                var uploadedUrl = localPath
                var attempt = 0
                val maxAttempts = 2 // Cepat tanpa membuat auditor menunggu lama
                while (attempt < maxAttempts) {
                    attempt++
                    onProgress("Mengunggah Foto $slotName ($step/$pendingCount) Mobil #$mob ke Google Drive...")
                    val up = GoogleAppsScriptHub.uploadImageToGoogleDrive(
                        context = context,
                        scriptUrl = scriptUrl,
                        filePath = localPath,
                        fileName = "KEINDAHAN_${mob}_${prefix}_${System.currentTimeMillis()}.jpg",
                        folderId = folderId
                    )
                    if (up.isSuccess && !up.getOrNull().isNullOrBlank()) {
                        uploadedUrl = up.getOrNull()!!
                        onProgress("✓ Foto $slotName ($step/$pendingCount) berhasil diunggah ke Google Drive.")
                        break
                    } else {
                        if (attempt < maxAttempts && networkMonitor.isOnline()) {
                            delay(1000)
                        }
                    }
                }
                return uploadedUrl
            }

            // Upload Foto Depan
            if (pDepan.isNotBlank() && !pDepan.startsWith("http", ignoreCase = true)) {
                val newUrl = uploadSlotFast(pDepan, "Depan", "DEPAN")
                if (newUrl.startsWith("http", ignoreCase = true)) {
                    pDepan = newUrl
                    currentEntity = currentEntity.copy(photoDepan = pDepan)
                    pekerjaanKeindahanDao.updatePekerjaanServiceKeindahan(currentEntity)
                }
            }

            // Upload Foto Samping LH
            if (pLh.isNotBlank() && !pLh.startsWith("http", ignoreCase = true)) {
                val newUrl = uploadSlotFast(pLh, "Samping LH", "LH")
                if (newUrl.startsWith("http", ignoreCase = true)) {
                    pLh = newUrl
                    currentEntity = currentEntity.copy(photoSampingLh = pLh)
                    pekerjaanKeindahanDao.updatePekerjaanServiceKeindahan(currentEntity)
                }
            }

            // Upload Foto Interior
            if (pInterior.isNotBlank() && !pInterior.startsWith("http", ignoreCase = true)) {
                val newUrl = uploadSlotFast(pInterior, "Interior", "INTERIOR")
                if (newUrl.startsWith("http", ignoreCase = true)) {
                    pInterior = newUrl
                    currentEntity = currentEntity.copy(photoInterior = pInterior)
                    pekerjaanKeindahanDao.updatePekerjaanServiceKeindahan(currentEntity)
                }
            }

            // Upload Foto Belakang
            if (pBelakang.isNotBlank() && !pBelakang.startsWith("http", ignoreCase = true)) {
                val newUrl = uploadSlotFast(pBelakang, "Belakang", "BELAKANG")
                if (newUrl.startsWith("http", ignoreCase = true)) {
                    pBelakang = newUrl
                    currentEntity = currentEntity.copy(photoBelakang = pBelakang)
                    pekerjaanKeindahanDao.updatePekerjaanServiceKeindahan(currentEntity)
                }
            }

            // Upload Foto Samping RH
            if (pRh.isNotBlank() && !pRh.startsWith("http", ignoreCase = true)) {
                val newUrl = uploadSlotFast(pRh, "Samping RH", "RH")
                if (newUrl.startsWith("http", ignoreCase = true)) {
                    pRh = newUrl
                    currentEntity = currentEntity.copy(photoSampingRh = pRh)
                    pekerjaanKeindahanDao.updatePekerjaanServiceKeindahan(currentEntity)
                }
            }
        }

        val itemWithDriveUrls = currentEntity.copy(
            id = savedId,
            photoDepan = pDepan,
            photoSampingLh = pLh,
            photoInterior = pInterior,
            photoBelakang = pBelakang,
            photoSampingRh = pRh
        )
        pekerjaanKeindahanDao.updatePekerjaanServiceKeindahan(itemWithDriveUrls.copy(isSynced = false))

        // 4. Sinkronisasi data ke Google Sheets jika online
        if (scriptUrl.isNotBlank() && networkMonitor.isOnline()) {
            onProgress("Menyinkronkan data Mobil #$mob ke Google Sheets ($sheetName)...")
            val remoteRes = GoogleAppsScriptHub.sendPekerjaanServiceKeindahanToGoogleSheet(
                scriptUrl = scriptUrl,
                item = itemWithDriveUrls,
                sheetName = sheetName,
                spreadsheetId = sheetId,
                folderId = folderId
            )
            if (remoteRes.isSuccess) {
                val syncedEntity = remoteRes.getOrNull() ?: itemWithDriveUrls.copy(isSynced = true, syncTimestamp = System.currentTimeMillis())
                pekerjaanKeindahanDao.updatePekerjaanServiceKeindahan(syncedEntity.copy(isSynced = true))
                Result.success(syncedEntity.copy(isSynced = true))
            } else {
                // Tetap aman di Room cache lokal jika transmisi cloud gagal
                Result.success(itemWithDriveUrls.copy(isSynced = false))
            }
        } else {
            // Tersimpan di Room cache lokal, siap di-sync saat online
            Result.success(itemWithDriveUrls.copy(isSynced = false))
        }
    }

    suspend fun autoUploadPendingKeindahanPhotos(
        onProgress: (String) -> Unit = {}
    ): Int = withContext(Dispatchers.IO) {
        val (success, _) = autoSyncPendingInspections(onProgress)
        success
    }

    suspend fun autoSyncPendingInspections(
        onProgress: (String) -> Unit = {}
    ): Pair<Int, Int> = withContext(Dispatchers.IO) {
        if (!networkMonitor.isOnline()) return@withContext Pair(0, 0)

        var totalSuccess = 0
        var totalFailed = 0

        // 1. Sync pending pekerjaan keindahan (inspeksi fisik & foto kendaraan)
        val unsyncedKeindahan = pekerjaanKeindahanDao.getUnsyncedPekerjaanServiceKeindahan()
        if (unsyncedKeindahan.isNotEmpty()) {
            onProgress("Menyinkronkan ${unsyncedKeindahan.size} hasil inspeksi dari cache lokal...")
            for (item in unsyncedKeindahan) {
                if (!networkMonitor.isOnline()) break
                val res = savePekerjaanServiceKeindahan(item, onProgress)
                if (res.isSuccess && res.getOrNull()?.isSynced == true) {
                    totalSuccess++
                } else {
                    totalFailed++
                }
            }
        }

        // 2. Sync pending data audit
        val unsyncedAudits = auditDao.getUnsyncedAudits()
        if (unsyncedAudits.isNotEmpty()) {
            onProgress("Menyinkronkan ${unsyncedAudits.size} data audit dari cache...")
            val (audSuccess, audFailed) = syncAllPendingAudits()
            totalSuccess += audSuccess
            totalFailed += audFailed
        }

        Pair(totalSuccess, totalFailed)
    }

    suspend fun uploadPhotoToGoogleDrive(filePathOrUri: String, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        val scriptUrl = sessionManager.scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        val folderId = sessionManager.folderPhotoKeindahanId.trim().ifBlank { SessionManager.DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID }
        GoogleAppsScriptHub.uploadImageToGoogleDrive(context, scriptUrl, filePathOrUri, fileName, folderId)
    }

    suspend fun deletePekerjaanServiceKeindahan(id: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        pekerjaanKeindahanDao.deletePekerjaanServiceKeindahanById(id)
        val scriptUrl = sessionManager.scriptUrl.trim()
        val sheetName = sessionManager.sheetPekerjaanServiceKeindahanName.trim().ifBlank { SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME }
        val sheetId = sessionManager.sheetPekerjaanServiceKeindahan.trim().ifBlank { SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID }
        if (scriptUrl.isNotBlank()) {
            try {
                GoogleAppsScriptHub.deletePekerjaanServiceKeindahanFromGoogleSheet(
                    scriptUrl = scriptUrl,
                    id = id,
                    spreadsheetId = sheetId,
                    sheetName = sheetName
                )
            } catch (_: Exception) {}
        }
        Result.success(true)
    }

    suspend fun syncPekerjaanServiceKeindahanFromGoogleSheets(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val sheetId = sessionManager.sheetPekerjaanServiceKeindahan.trim().ifBlank { SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID }
            val sheetName = sessionManager.sheetPekerjaanServiceKeindahanName.trim().ifBlank { SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME }
            val csv = GoogleAppsScriptHub.fetchCsvFromSheetId(sheetId, sheetName)
            if (!csv.isNullOrBlank()) {
                val parsed = GoogleAppsScriptHub.parsePekerjaanServiceKeindahanCsv(csv)
                if (parsed.isNotEmpty()) {
                    val unsynced = pekerjaanKeindahanDao.getUnsyncedPekerjaanServiceKeindahan()
                    pekerjaanKeindahanDao.deleteAllPekerjaanServiceKeindahan()
                    val chunkSize = 50
                    parsed.chunked(chunkSize).forEach { batch ->
                        pekerjaanKeindahanDao.insertPekerjaanServiceKeindahanList(batch)
                        kotlinx.coroutines.yield()
                    }
                    if (unsynced.isNotEmpty()) {
                        unsynced.forEach {
                            pekerjaanKeindahanDao.insertPekerjaanServiceKeindahan(it)
                        }
                    }
                    Result.success(parsed.size)
                } else {
                    Result.success(0)
                }
            } else {
                Result.failure(Exception("Gagal mengambil CSV sheet $sheetName dari Spreadsheet ID: $sheetId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CRUD Claim
    suspend fun saveClaim(claim: ClaimEntity): Long = withContext(Dispatchers.IO) {
        if (claim.id == 0L) claimDao.insertClaim(claim) else {
            claimDao.updateClaim(claim)
            claim.id
        }
    }

    suspend fun deleteClaim(id: Long) = withContext(Dispatchers.IO) {
        claimDao.deleteClaimById(id)
    }

    suspend fun getClaimByRegistrasi(regNo: String): ClaimEntity? = withContext(Dispatchers.IO) {
        claimDao.getClaimByRegistrasi(regNo.trim())
    }

    suspend fun syncClaimFromGoogleSheets(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val claimSheetId = sessionManager.sheetClaim.trim().ifBlank { SessionManager.DEFAULT_CLAIM_SHEET_ID }
            val claimSheetName = sessionManager.sheetClaimName.trim().ifBlank { SessionManager.DEFAULT_CLAIM_SHEET_NAME }
            if (claimSheetId.isBlank()) return@withContext Result.failure(Exception("ID Sheet Claim kosong"))
            val csv = GoogleAppsScriptHub.fetchCsvFromSheetId(claimSheetId, claimSheetName)
            if (!csv.isNullOrBlank()) {
                val parsed = GoogleAppsScriptHub.parseClaimCsv(csv)
                if (parsed.isNotEmpty()) {
                    claimDao.deleteAllClaims()
                    val chunkSize = 50
                    parsed.chunked(chunkSize).forEach { batch ->
                        claimDao.insertClaimList(batch)
                        kotlinx.coroutines.yield()
                    }
                    return@withContext Result.success(true)
                }
            }
            Result.failure(Exception("Data Claim tidak ditemukan di Google Sheets"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CRUD Pengerjaan Cat (ID: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs, Sheet: PENGERJAAN_CAT)
    suspend fun savePengerjaanCat(
        cat: PengerjaanCatEntity,
        isEdit: Boolean = false,
        oldNoRegistrasi: String = "",
        oldNoMobil: String = "",
        oldTimestamp: String = ""
    ): Result<Long> = withContext(Dispatchers.IO) {
        val scriptUrl = sessionManager.scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        val catSheetName = sessionManager.sheetPengerjaanCatName.trim().ifBlank { SessionManager.DEFAULT_PENGERJAAN_CAT_SHEET_NAME }
        val catSheetId = sessionManager.sheetPengerjaanCat.trim().ifBlank { SessionManager.DEFAULT_PENGERJAAN_CAT_SHEET_ID }
        val folderId = sessionManager.folderPhotoKeindahanId.trim().ifBlank { SessionManager.DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID }

        // PERLINDUNGAN ID UNIK (HINDARI PENIMPAAN DATA DENGAN ID YANG SAMA):
        // Pisahkan penanganan antara Mode EDIT dan Mode TAMBAH DATA BARU (isEdit == false)
        val allCats = try { catDao.getAllPengerjaanCatList() } catch (e: Exception) { emptyList() }
        val existingIds = allCats.map { it.id }.toSet()
        val maxExistingId = allCats.maxOfOrNull { it.id } ?: 0L

        val targetId: Long
        val finalIsEdit: Boolean
        val existing: PengerjaanCatEntity?

        if (isEdit) {
            // Mode EDIT: cari record yang mau diedit berdasarkan ID atau Registrasi
            val existingById = if (cat.id != 0L) {
                try { catDao.getPengerjaanCatById(cat.id) } catch (e: Exception) { null }
            } else null
            val existingByOldReg = if (oldNoRegistrasi.isNotBlank()) {
                try { catDao.getPengerjaanCatByNoRegistrasi(oldNoRegistrasi) } catch (e: Exception) { null }
            } else null
            val existingByReg = if (cat.noRegistrasi.isNotBlank()) {
                try { catDao.getPengerjaanCatByNoRegistrasi(cat.noRegistrasi) } catch (e: Exception) { null }
            } else null

            existing = existingById ?: existingByOldReg ?: existingByReg
            finalIsEdit = true
            targetId = if (cat.id != 0L) cat.id else (existing?.id ?: cat.id)
        } else {
            // Mode PENAMBAHAN DATA BARU:
            // JANGAN PERNAH menimpa data lama dengan ID yang sama!
            // Pastikan ID benar-benar unik dan belum pernah dipakai di database lokal
            existing = null
            finalIsEdit = false

            var newId = if (cat.id > 0L) cat.id else System.currentTimeMillis()
            if (newId in existingIds || newId <= 0L) {
                newId = maxOf(System.currentTimeMillis(), maxExistingId + 1)
                while (newId in existingIds) {
                    newId++
                }
            }
            targetId = newId
        }

        val finalOldNoReg = if (finalIsEdit) (if (oldNoRegistrasi.isNotBlank()) oldNoRegistrasi else (existing?.noRegistrasi ?: "")) else ""
        val finalOldNoMob = if (finalIsEdit) (if (oldNoMobil.isNotBlank()) oldNoMobil else (existing?.noMobil ?: "")) else ""
        val finalOldTs = if (finalIsEdit) (if (oldTimestamp.isNotBlank()) oldTimestamp else (existing?.timestamp ?: "")) else ""

        // Upload foto lokal ke Google Drive jika ada
        val mob = if (cat.noMobil.isNotBlank()) cat.noMobil.trim().uppercase() else "MOBIL"
        var pKerusakan = cat.photoKerusakan.trim()
        var pNotif = cat.photoNotif.trim()
        var pGambar = cat.gambarFotoHasilPengecatan.trim()
        var pLasKetok = cat.photoLasKetok.trim()
        var pHasil2 = cat.fotoHasilPengecatan2.trim()
        var pHasil3 = cat.fotoHasilPengecatan3.trim()

        if (scriptUrl.isNotBlank()) {
            if (pKerusakan.isNotBlank() && !pKerusakan.startsWith("http", ignoreCase = true)) {
                val up = GoogleAppsScriptHub.uploadImageToGoogleDrive(context, scriptUrl, pKerusakan, "CAT_${mob}_KERUSAKAN_${System.currentTimeMillis()}.jpg", folderId)
                if (up.isSuccess) pKerusakan = up.getOrNull() ?: pKerusakan
            }
            if (pNotif.isNotBlank() && !pNotif.startsWith("http", ignoreCase = true)) {
                val up = GoogleAppsScriptHub.uploadImageToGoogleDrive(context, scriptUrl, pNotif, "CAT_${mob}_NOTIF_${System.currentTimeMillis()}.jpg", folderId)
                if (up.isSuccess) pNotif = up.getOrNull() ?: pNotif
            }
            if (pGambar.isNotBlank() && !pGambar.startsWith("http", ignoreCase = true)) {
                val up = GoogleAppsScriptHub.uploadImageToGoogleDrive(context, scriptUrl, pGambar, "CAT_${mob}_HASIL_${System.currentTimeMillis()}.jpg", folderId)
                if (up.isSuccess) pGambar = up.getOrNull() ?: pGambar
            }
            if (pLasKetok.isNotBlank() && !pLasKetok.startsWith("http", ignoreCase = true)) {
                val up = GoogleAppsScriptHub.uploadImageToGoogleDrive(context, scriptUrl, pLasKetok, "CAT_${mob}_LASKETOK_${System.currentTimeMillis()}.jpg", folderId)
                if (up.isSuccess) pLasKetok = up.getOrNull() ?: pLasKetok
            }
            if (pHasil2.isNotBlank() && !pHasil2.startsWith("http", ignoreCase = true)) {
                val up = GoogleAppsScriptHub.uploadImageToGoogleDrive(context, scriptUrl, pHasil2, "CAT_${mob}_HASIL2_${System.currentTimeMillis()}.jpg", folderId)
                if (up.isSuccess) pHasil2 = up.getOrNull() ?: pHasil2
            }
            if (pHasil3.isNotBlank() && !pHasil3.startsWith("http", ignoreCase = true)) {
                val up = GoogleAppsScriptHub.uploadImageToGoogleDrive(context, scriptUrl, pHasil3, "CAT_${mob}_HASIL3_${System.currentTimeMillis()}.jpg", folderId)
                if (up.isSuccess) pHasil3 = up.getOrNull() ?: pHasil3
            }
        }

        val catWithDriveUrls = cat.copy(
            id = targetId,
            photoKerusakan = pKerusakan,
            photoNotif = pNotif,
            gambarFotoHasilPengecatan = pGambar,
            photoLasKetok = pLasKetok,
            fotoHasilPengecatan2 = pHasil2,
            fotoHasilPengecatan3 = pHasil3
        )

        val savedId = if (catWithDriveUrls.id == 0L) {
            catDao.insertPengerjaanCat(catWithDriveUrls.copy(isSynced = false))
        } else {
            catDao.insertPengerjaanCat(catWithDriveUrls.copy(isSynced = false))
            catWithDriveUrls.id
        }

        // Hapus record lama HANYA JIKA dalam mode EDIT dan ID lokal sebelumnya berbeda
        if (finalIsEdit && existing != null && existing.id != savedId && existing.id != 0L) {
            try { catDao.deletePengerjaanCatById(existing.id) } catch (e: Exception) {}
        }

        // Langsung kirim perubahan ke Google Sheets agar data di Google Sheets ikut terupdate
        if (scriptUrl.isNotBlank()) {
            try {
                val entityToSync = if (catWithDriveUrls.id == 0L) catWithDriveUrls.copy(id = savedId) else catWithDriveUrls
                val remoteRes = GoogleAppsScriptHub.sendPengerjaanCatToGoogleSheet(
                    scriptUrl = scriptUrl,
                    cat = entityToSync,
                    sheetName = catSheetName,
                    spreadsheetId = catSheetId,
                    isEdit = finalIsEdit,
                    oldNoRegistrasi = finalOldNoReg,
                    oldNoMobil = finalOldNoMob,
                    oldTimestamp = finalOldTs
                )
                if (remoteRes.isSuccess) {
                    catDao.insertPengerjaanCat(entityToSync.copy(isSynced = true, syncTimestamp = System.currentTimeMillis()))
                    Result.success(savedId)
                } else {
                    val err = remoteRes.exceptionOrNull()?.message ?: "Gagal kirim ke Google Sheets"
                    Result.failure(Exception("Tersimpan di HP, tetapi GAGAL update Google Sheets: $err"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Tersimpan di HP, tetapi GAGAL update Google Sheets: ${e.message}"))
            }
        } else {
            Result.success(savedId)
        }
    }

    suspend fun deletePengerjaanCat(id: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        val existing = try { catDao.getPengerjaanCatById(id) } catch (e: Exception) { null }
        catDao.deletePengerjaanCatById(id)
        if (existing != null) {
            val scriptUrl = sessionManager.scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
            val catSheetName = sessionManager.sheetPengerjaanCatName.trim().ifBlank { SessionManager.DEFAULT_PENGERJAAN_CAT_SHEET_NAME }
            val catSheetId = sessionManager.sheetPengerjaanCat.trim().ifBlank { SessionManager.DEFAULT_PENGERJAAN_CAT_SHEET_ID }
            if (scriptUrl.isNotBlank()) {
                try {
                    GoogleAppsScriptHub.deletePengerjaanCatFromGoogleSheet(
                        scriptUrl = scriptUrl,
                        cat = existing,
                        sheetName = catSheetName,
                        spreadsheetId = catSheetId
                    )
                } catch (_: Exception) {}
            }
        }
        Result.success(true)
    }

    // CRUD Ban, Kopling, Baterai
    suspend fun saveBan(ban: BanEntity): Long = withContext(Dispatchers.IO) {
        if (ban.id == 0L) banDao.insertBan(ban) else {
            banDao.updateBan(ban)
            ban.id
        }
    }

    suspend fun deleteBan(id: Long) = withContext(Dispatchers.IO) {
        banDao.deleteBanById(id)
    }

    suspend fun saveKopling(kopling: KoplingEntity): Long = withContext(Dispatchers.IO) {
        if (kopling.id == 0L) koplingDao.insertKopling(kopling) else {
            koplingDao.updateKopling(kopling)
            kopling.id
        }
    }

    suspend fun deleteKopling(id: Long) = withContext(Dispatchers.IO) {
        koplingDao.deleteKoplingById(id)
    }

    suspend fun saveBateray(bateray: BaterayEntity): Long = withContext(Dispatchers.IO) {
        if (bateray.id == 0L) baterayDao.insertBateray(bateray) else {
            baterayDao.updateBateray(bateray)
            bateray.id
        }
    }

    suspend fun deleteBateray(id: Long) = withContext(Dispatchers.IO) {
        baterayDao.deleteBaterayById(id)
    }

    // CRUD Reservasi
    suspend fun saveReservasi(reservasi: ReservasiEntity): Long = withContext(Dispatchers.IO) {
        if (reservasi.id == 0L) reservasiDao.insertReservasi(reservasi) else {
            reservasiDao.updateReservasi(reservasi)
            reservasi.id
        }
    }

    suspend fun deleteReservasi(id: Long) = withContext(Dispatchers.IO) {
        reservasiDao.deleteReservasiById(id)
    }

    suspend fun clearReservasiData() = withContext(Dispatchers.IO) {
        reservasiDao.deleteAllReservasi()
    }

    suspend fun clearReservasiAndKomponenPendukungData() = withContext(Dispatchers.IO) {
        reservasiDao.deleteAllReservasi()
        banDao.deleteAllBan()
        koplingDao.deleteAllKopling()
        baterayDao.deleteAllBateray()
    }

    suspend fun clearRiwayatSkiData() = withContext(Dispatchers.IO) {
        riwayatSkiDao.deleteAllRiwayatSki()
    }

    suspend fun clearBaterayData() = withContext(Dispatchers.IO) {
        baterayDao.deleteAllBateray()
    }

    // CRUD ARGO
    suspend fun saveArgo(argo: ArgoEntity): Long = withContext(Dispatchers.IO) {
        if (argo.id == 0L) argoDao.insertArgo(argo) else {
            argoDao.updateArgo(argo)
            argo.id
        }
    }

    suspend fun deleteArgo(id: Long) = withContext(Dispatchers.IO) {
        argoDao.deleteArgoById(id)
    }

    // CRUD PREDIKSI PART
    suspend fun savePrediksiPart(item: PrediksiPartEntity): Result<Long> = withContext(Dispatchers.IO) {
        val uniqueId = if (item.id <= 0L) System.currentTimeMillis() else item.id
        val entityToSave = item.copy(id = uniqueId, isSynced = false)

        val existing = try { prediksiPartDao.getPrediksiPartById(uniqueId) } catch (e: Exception) { null }
        if (existing == null) {
            prediksiPartDao.insertPrediksiPart(entityToSave)
        } else {
            prediksiPartDao.updatePrediksiPart(entityToSave)
        }
        val savedId = uniqueId

        // Langsung kirim perubahan ke Google Sheets agar data di Google Sheets otomatis terupdate
        val scriptUrl = sessionManager.scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        val sheetId = sessionManager.sheetPrediksiPart.trim().ifBlank { SessionManager.DEFAULT_PREDIKSI_PART_SHEET_ID }
        val sheetName = sessionManager.sheetPrediksiPartName.trim().ifBlank { SessionManager.DEFAULT_PREDIKSI_PART_SHEET_NAME }

        if (scriptUrl.isNotBlank()) {
            try {
                val entityToSync = entityToSave
                val res = GoogleAppsScriptHub.sendPrediksiPartToGoogleSheet(
                    scriptUrl = scriptUrl,
                    item = entityToSync,
                    sheetName = sheetName,
                    spreadsheetId = sheetId
                )
                if (res.isSuccess) {
                    prediksiPartDao.updatePrediksiPart(entityToSync.copy(isSynced = true))
                    Result.success(savedId)
                } else {
                    val err = res.exceptionOrNull()?.message ?: "Gagal update Google Sheets"
                    Result.failure(Exception("Tersimpan di HP, tetapi gagal update Google Sheets: $err"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Tersimpan di HP, tetapi gagal update Google Sheets: ${e.message}"))
            }
        } else {
            Result.success(savedId)
        }
    }

    suspend fun deletePrediksiPart(id: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        val existing = try { prediksiPartDao.getPrediksiPartById(id) } catch (e: Exception) { null }
        prediksiPartDao.deletePrediksiPartById(id)
        if (existing != null) {
            val scriptUrl = sessionManager.scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
            val sheetId = sessionManager.sheetPrediksiPart.trim().ifBlank { SessionManager.DEFAULT_PREDIKSI_PART_SHEET_ID }
            val sheetName = sessionManager.sheetPrediksiPartName.trim().ifBlank { SessionManager.DEFAULT_PREDIKSI_PART_SHEET_NAME }
            if (scriptUrl.isNotBlank()) {
                try {
                    GoogleAppsScriptHub.deletePrediksiPartFromGoogleSheet(
                        scriptUrl = scriptUrl,
                        item = existing,
                        sheetName = sheetName,
                        spreadsheetId = sheetId
                    )
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
        Result.success(true)
    }

    // CRUD Pengerjaan Teknik
    suspend fun savePengerjaanTeknik(item: PengerjaanTeknikEntity): Long = withContext(Dispatchers.IO) {
        if (item.id == 0L) pengerjaanTeknikDao.insertPengerjaanTeknik(item) else {
            pengerjaanTeknikDao.updatePengerjaanTeknik(item)
            item.id
        }
    }

    suspend fun deletePengerjaanTeknik(id: Long) = withContext(Dispatchers.IO) {
        pengerjaanTeknikDao.deletePengerjaanTeknikById(id)
    }

    // CRUD Riwayat Service SKI
    suspend fun saveRiwayatSki(item: RiwayatSkiEntity): Long = withContext(Dispatchers.IO) {
        if (item.id == 0L) riwayatSkiDao.insertRiwayatSki(item) else {
            riwayatSkiDao.updateRiwayatSki(item)
            item.id
        }
    }

    suspend fun deleteRiwayatSki(id: Long) = withContext(Dispatchers.IO) {
        riwayatSkiDao.deleteRiwayatSkiById(id)
    }

    // CRUD Riwayat Claim
    suspend fun saveRiwayatClaim(item: RiwayatClaimEntity): Long = withContext(Dispatchers.IO) {
        if (item.id == 0L) riwayatClaimDao.insertRiwayatClaim(item) else {
            riwayatClaimDao.updateRiwayatClaim(item)
            item.id
        }
    }

    suspend fun deleteRiwayatClaim(id: Long) = withContext(Dispatchers.IO) {
        riwayatClaimDao.deleteRiwayatClaimById(id)
    }

    // CRUD Master Kendaraan
    suspend fun saveKendaraan(kendaraan: MasterKendaraanEntity): Long = withContext(Dispatchers.IO) {
        if (kendaraan.id == 0) kendaraanDao.insertKendaraan(kendaraan) else {
            kendaraanDao.updateKendaraan(kendaraan)
            kendaraan.id.toLong()
        }
    }

    suspend fun deleteKendaraan(id: Int) = withContext(Dispatchers.IO) {
        kendaraanDao.deleteKendaraanById(id)
    }

    suspend fun getKendaraanByNoMobil(noMobil: String): MasterKendaraanEntity? = withContext(Dispatchers.IO) {
        kendaraanDao.getKendaraanByNoMobil(noMobil.trim())
    }

    // CRUD User
    suspend fun saveUser(
        user: UserEntity,
        originalNip: String = "",
        originalUsername: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val existingById = if (user.id != 0) userDao.getUserById(user.id) else null
        val prevNip = originalNip.trim().ifBlank { existingById?.nip ?: "" }
        val prevUsername = originalUsername.trim().ifBlank { existingById?.username ?: "" }

        if (existingById != null) {
            userDao.updateUser(user.copy(id = existingById.id))
        } else {
            val existing = userDao.getUserByNip(user.nip) ?: userDao.getUserByUsername(user.username)
            if (existing != null) {
                userDao.updateUser(user.copy(id = existing.id))
            } else {
                userDao.insertUser(user)
            }
        }

        // Langsung kirim perubahan ke Google Sheets agar data di Google Sheets dan Aplikasi selalu sama
        val scriptUrl = sessionManager.scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        val userSheetName = sessionManager.sheetUserName.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_NAME }
        val userSheetId = sessionManager.sheetUser.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_ID }
        if (scriptUrl.isNotBlank()) {
            try {
                val res = GoogleAppsScriptHub.sendUserToGoogleSheet(
                    scriptUrl = scriptUrl,
                    user = user,
                    sheetName = userSheetName,
                    spreadsheetId = userSheetId,
                    originalNip = prevNip,
                    originalUsername = prevUsername
                )
                if (res.isSuccess) {
                    Result.success("User ${user.namaMekanik} (${user.level}) berhasil disimpan & disinkronkan dengan Google Sheets!")
                } else {
                    val err = res.exceptionOrNull()?.message ?: "Gagal update Google Sheets"
                    Result.failure(Exception("Tersimpan di HP, tetapi gagal update Google Sheets: $err"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Tersimpan di HP, tetapi gagal update Google Sheets: ${e.message}"))
            }
        } else {
            Result.success("User ${user.namaMekanik} disimpan di HP.")
        }
    }

    suspend fun deleteUser(id: Int): Result<String> = withContext(Dispatchers.IO) {
        val existing = userDao.getUserById(id)
        userDao.deleteUserById(id)

        val scriptUrl = sessionManager.scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        val userSheetName = sessionManager.sheetUserName.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_NAME }
        val userSheetId = sessionManager.sheetUser.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_ID }
        if (existing != null && scriptUrl.isNotBlank()) {
            try {
                val res = GoogleAppsScriptHub.deleteUserFromGoogleSheet(
                    scriptUrl = scriptUrl,
                    user = existing,
                    sheetName = userSheetName,
                    spreadsheetId = userSheetId
                )
                if (res.isSuccess) {
                    return@withContext Result.success("User ${existing.namaMekanik} (NIP: ${existing.nip}) berhasil dihapus dari Aplikasi & Google Sheets!")
                }
            } catch (_: Exception) {}
        }
        Result.success("User berhasil dihapus.")
    }

    /**
     * Sinkronisasi User Bengkel dari Google Sheets (Sheet: USER):
     * Mengambil data asli dari Google Sheets tanpa menyisipkan data demo.
     */
    suspend fun initializeDefaultUsers(): Result<Int> = withContext(Dispatchers.IO) {
        syncUserFromGoogleSheets()
    }

    // Backward compatibility methods
    suspend fun insertUser(user: UserEntity) = saveUser(user)
    suspend fun deleteUser(nip: String) = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByNip(nip)
        userDao.deleteUserByNip(nip)
        val scriptUrl = sessionManager.scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        val userSheetName = sessionManager.sheetUserName.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_NAME }
        val userSheetId = sessionManager.sheetUser.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_ID }
        if (existing != null && scriptUrl.isNotBlank()) {
            try {
                GoogleAppsScriptHub.deleteUserFromGoogleSheet(scriptUrl, existing, userSheetName, userSheetId)
            } catch (_: Exception) {}
        }
    }
    suspend fun insertKendaraan(kendaraan: MasterKendaraanEntity) = saveKendaraan(kendaraan)
    suspend fun insertPool(pool: MasterPoolEntity) = withContext(Dispatchers.IO) { poolDao.insertPool(pool) }
    suspend fun deletePool(id: Int) = withContext(Dispatchers.IO) { poolDao.deletePoolById(id) }
    suspend fun updatePhotoSetting(setting: PengaturanNamaPhotoEntity) = withContext(Dispatchers.IO) { photoSettingDao.updateNamaPhoto(setting) }
    suspend fun saveAudit(audit: DataAuditEntity): Long = withContext(Dispatchers.IO) {
        val id = if (audit.id == 0L) auditDao.insertAudit(audit.copy(isSynced = false)) else {
            auditDao.updateAudit(audit.copy(isSynced = false))
            audit.id
        }
        val scriptUrl = sessionManager.scriptUrl.trim()
        if (networkMonitor.isOnline() && scriptUrl.isNotBlank()) {
            try {
                val syncRes = GoogleAppsScriptHub.sendAuditToGoogleSheet(
                    scriptUrl = scriptUrl,
                    audit = audit.copy(id = id),
                    sheetName = "INPUT_AUDIT"
                )
                if (syncRes.isSuccess) {
                    val synced = syncRes.getOrNull()
                    if (synced != null) {
                        auditDao.updateAudit(synced)
                    }
                }
            } catch (_: Exception) {}
        }
        id
    }

    suspend fun updateAudit(audit: DataAuditEntity) = withContext(Dispatchers.IO) { auditDao.updateAudit(audit) }
    suspend fun deleteAudit(id: Long) = withContext(Dispatchers.IO) { auditDao.deleteAuditById(id) }

    suspend fun syncAllPendingAudits(): Pair<Int, Int> = withContext(Dispatchers.IO) {
        if (!networkMonitor.isOnline()) return@withContext Pair(0, 0)
        val scriptUrl = sessionManager.scriptUrl.trim()
        if (scriptUrl.isBlank()) return@withContext Pair(0, 0)
        val pending = auditDao.getUnsyncedAudits()
        var success = 0
        var failed = 0
        for (item in pending) {
            val res = GoogleAppsScriptHub.sendAuditToGoogleSheet(
                scriptUrl = scriptUrl,
                audit = item,
                sheetName = "INPUT_AUDIT"
            )
            if (res.isSuccess) {
                val updated = res.getOrNull()
                if (updated != null) {
                    auditDao.updateAudit(updated)
                }
                success++
            } else {
                failed++
            }
        }
        Pair(success, failed)
    }
    suspend fun downloadMasterDataFromCloud(): Result<Boolean> = syncFromGoogleSheets()

    suspend fun clearAllLocalDataExceptUsers() = withContext(Dispatchers.IO) {
        teknikDao.deleteAllServiceTeknik()
        laporanDao.deleteAllLaporan()
        kendaraanDao.deleteAllKendaraan()
        keindahanDao.deleteAllServiceKeindahan()
        claimDao.deleteAllClaims()
        catDao.deleteAllPengerjaanCat()
        banDao.deleteAllBan()
        koplingDao.deleteAllKopling()
        baterayDao.deleteAllBateray()
        reservasiDao.deleteAllReservasi()
        argoDao.deleteAllArgo()
        prediksiPartDao.deleteAllPrediksiPart()
        pengerjaanTeknikDao.deleteAllPengerjaanTeknik()
        riwayatSkiDao.deleteAllRiwayatSki()
        riwayatClaimDao.deleteAllRiwayatClaim()
        pekerjaanKeindahanDao.deleteAllPekerjaanServiceKeindahan()
    }

    /**
     * SINKRONISASI DARI GOOGLE SHEETS
     * Mengambil data dari ID Google Sheets yang telah diatur
     * Laporan Harian: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs
     * Service Teknik: 1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4 (Sheet: TEKNIK)
     *
     * Hapus semua data lokal sebelumnya agar HANYA data yang berada di Google Sheets yang tampil.
     */
    suspend fun syncFromGoogleSheets(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            var anySynced = false

            // 1. Sync Laporan Harian
            val laporanSheetId = sessionManager.sheetLaporanHarian.trim()
            val laporanSheetName = sessionManager.sheetLaporanHarianName.trim().ifBlank { "LAPORAN KENDARAAN" }
            if (laporanSheetId.isNotBlank()) {
                // Coba sinkronkan dulu laporan lokal yang belum tersinkron jika ada scriptUrl
                val scriptUrl = sessionManager.scriptUrl.trim()
                val pendingList = laporanDao.getUnsyncedLaporan()
                if (scriptUrl.isNotBlank() && pendingList.isNotEmpty()) {
                    pendingList.forEach { pending ->
                        val res = GoogleAppsScriptHub.sendLaporanHarianToGoogleSheet(scriptUrl, pending, laporanSheetName, laporanSheetId)
                        if (res.isSuccess) {
                            laporanDao.markAsSynced(pending.id)
                        }
                    }
                }

                // Coba sinkronkan juga Pengerjaan Cat lokal yang belum tersinkron
                val pendingCat = catDao.getUnsyncedCat()
                val catSheetName = sessionManager.sheetPengerjaanCatName.trim().ifBlank { SessionManager.DEFAULT_PENGERJAAN_CAT_SHEET_NAME }
                val catSheetId = sessionManager.sheetPengerjaanCat.trim().ifBlank { SessionManager.DEFAULT_PENGERJAAN_CAT_SHEET_ID }
                if (scriptUrl.isNotBlank() && pendingCat.isNotEmpty()) {
                    pendingCat.forEach { cat ->
                        val res = GoogleAppsScriptHub.sendPengerjaanCatToGoogleSheet(scriptUrl, cat, catSheetName, catSheetId)
                        if (res.isSuccess) {
                            catDao.markAsSynced(cat.id)
                        }
                    }
                }

                val csv = GoogleAppsScriptHub.fetchCsvFromSheetId(laporanSheetId, laporanSheetName)
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parseLaporanHarianCsv(csv)
                    if (parsed.isNotEmpty()) {
                        // Amankan data lokal yang masih belum tersinkron agar tidak hilang
                        val stillUnsynced = laporanDao.getUnsyncedLaporan()
                        laporanDao.deleteAllLaporan()
                        laporanDao.insertLaporanList(parsed)
                        if (stillUnsynced.isNotEmpty()) {
                            stillUnsynced.forEach { unsynced ->
                                laporanDao.insertLaporan(unsynced)
                            }
                        }
                        anySynced = true
                    }
                }
            }

            // 2. Sync Service Teknik (Spreadsheet ID: 1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4, Sheet: TEKNIK)
            val teknikSheetId = sessionManager.sheetServiceTeknik.trim()
            val teknikSheetName = sessionManager.sheetServiceTeknikName.trim()
            if (teknikSheetId.isNotBlank()) {
                var csv = GoogleAppsScriptHub.fetchCsvFromSheetId(teknikSheetId, teknikSheetName)
                if (csv.isNullOrBlank() && sessionManager.scriptUrl.isNotBlank()) {
                    csv = GoogleAppsScriptHub.fetchCsvViaScript(sessionManager.scriptUrl, teknikSheetId, teknikSheetName)
                }
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parseServiceTeknikCsv(csv)
                    if (parsed.isNotEmpty()) {
                        teknikDao.deleteAllServiceTeknik()
                        teknikDao.insertServiceTeknikList(parsed)
                        anySynced = true
                    }
                }
            }



            // 4. Sync Service Keindahan (Spreadsheet ID: 1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4, Sheet: KEINDAHAN)
            // Sinkronisasi bertahap (chunked batch insert) agar proses ringan dan responsif
            val keindahanSheetId = sessionManager.sheetServiceKeindahan.trim()
            val keindahanSheetName = sessionManager.sheetServiceKeindahanName.trim()
            if (keindahanSheetId.isNotBlank()) {
                var csv = GoogleAppsScriptHub.fetchCsvFromSheetId(keindahanSheetId, keindahanSheetName)
                if (csv.isNullOrBlank() && sessionManager.scriptUrl.isNotBlank()) {
                    csv = GoogleAppsScriptHub.fetchCsvViaScript(sessionManager.scriptUrl, keindahanSheetId, keindahanSheetName)
                }
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parseServiceKeindahanCsv(csv)
                    if (parsed.isNotEmpty()) {
                        keindahanDao.deleteAllServiceKeindahan()
                        val chunkSize = 50
                        parsed.chunked(chunkSize).forEach { batch ->
                            keindahanDao.insertServiceKeindahanList(batch)
                            kotlinx.coroutines.yield()
                        }
                        anySynced = true
                    }
                }
            }

            // 5. Sync Master Kendaraan (Spreadsheet ID: 1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU, Sheet: MASTER_KENDARAAN)
            val kendaraanSheetId = sessionManager.sheetKendaraan.trim()
            val kendaraanSheetName = sessionManager.sheetKendaraanName.trim()
            if (kendaraanSheetId.isNotBlank()) {
                val csv = GoogleAppsScriptHub.fetchCsvFromSheetId(kendaraanSheetId, kendaraanSheetName)
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parseMasterKendaraanCsv(csv)
                    if (parsed.isNotEmpty()) {
                        kendaraanDao.deleteAllKendaraan()
                        val chunkSize = 50
                        parsed.chunked(chunkSize).forEach { batch ->
                            kendaraanDao.insertKendaraanList(batch)
                            kotlinx.coroutines.yield()
                        }
                        anySynced = true
                    }
                }
            }

            // 6. Sync Kopling (Spreadsheet ID: 1qBtcaJP1adyawgZ2hbAIvlld-usZ3CV5YB6QSRpVxd4, Sheet: KOPLING)
            val koplingSheetId = sessionManager.sheetKopling.trim()
            val koplingSheetName = sessionManager.sheetKoplingName.trim()
            if (koplingSheetId.isNotBlank()) {
                val csv = GoogleAppsScriptHub.fetchCsvFromSheetId(koplingSheetId, koplingSheetName)
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parseKoplingCsv(csv)
                    if (parsed.isNotEmpty()) {
                        koplingDao.deleteAllKopling()
                        val chunkSize = 50
                        parsed.chunked(chunkSize).forEach { batch ->
                            koplingDao.insertKoplingList(batch)
                            kotlinx.coroutines.yield()
                        }
                        anySynced = true
                    }
                }
            }

            // 7. Sync BAN (Spreadsheet ID: 17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo, Sheet: DATA_BAN)
            val banSheetId = sessionManager.sheetBan.trim()
            val banSheetName = sessionManager.sheetBanName.trim()
            if (banSheetId.isNotBlank()) {
                var csv = GoogleAppsScriptHub.fetchCsvFromSheetId(banSheetId, banSheetName)
                if (csv.isNullOrBlank() && (banSheetName.contains(" ") || banSheetName.contains("_"))) {
                    val altName = if (banSheetName.contains(" ")) banSheetName.replace(" ", "_") else banSheetName.replace("_", " ")
                    csv = GoogleAppsScriptHub.fetchCsvFromSheetId(banSheetId, altName)
                }
                if (csv.isNullOrBlank() && sessionManager.scriptUrl.isNotBlank()) {
                    csv = GoogleAppsScriptHub.fetchCsvViaScript(sessionManager.scriptUrl, banSheetId, banSheetName)
                }
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parseBanCsv(csv)
                    if (parsed.isNotEmpty()) {
                        banDao.deleteAllBan()
                        val chunkSize = 50
                        parsed.chunked(chunkSize).forEach { batch ->
                            banDao.insertBanList(batch)
                            kotlinx.coroutines.yield()
                        }
                        anySynced = true
                    }
                }
            }

            // 8. Sync BATERAY / ACCU (Spreadsheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Sheet: ACCU)
            val bateraySheetId = sessionManager.sheetBateray.trim().ifBlank { SessionManager.DEFAULT_BATERAY_SHEET_ID }
            val bateraySheetName = sessionManager.sheetBaterayName.trim().ifBlank { SessionManager.DEFAULT_BATERAY_SHEET_NAME }
            if (bateraySheetId.isNotBlank()) {
                var csv = GoogleAppsScriptHub.fetchCsvFromSheetId(bateraySheetId, bateraySheetName)
                if (csv.isNullOrBlank() && sessionManager.scriptUrl.isNotBlank()) {
                    csv = GoogleAppsScriptHub.fetchCsvViaScript(sessionManager.scriptUrl, bateraySheetId, bateraySheetName)
                }
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parseAccuCsv(csv)
                    if (parsed.isNotEmpty()) {
                        baterayDao.deleteAllBateray()
                        val chunkSize = 50
                        parsed.chunked(chunkSize).forEach { batch ->
                            baterayDao.insertBaterayList(batch)
                            kotlinx.coroutines.yield()
                        }
                        anySynced = true
                    }
                }
            }

            // 9. Sync RESERVASI (Spreadsheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Sheet: GOODMOVEN)
            val reservasiSheetId = sessionManager.sheetReservasi.trim()
            val rawReservasiSheet = sessionManager.sheetReservasiName.trim()
            val reservasiSheetName = if (rawReservasiSheet.isBlank() || rawReservasiSheet.equals("GM", ignoreCase = true) || rawReservasiSheet.equals("RESERVASI", ignoreCase = true)) {
                SessionManager.DEFAULT_RESERVASI_SHEET_NAME
            } else {
                rawReservasiSheet
            }
            if (reservasiSheetId.isNotBlank()) {
                var csv = GoogleAppsScriptHub.fetchCsvFromSheetId(reservasiSheetId, reservasiSheetName)
                if (csv.isNullOrBlank() && sessionManager.scriptUrl.isNotBlank()) {
                    csv = GoogleAppsScriptHub.fetchCsvViaScript(sessionManager.scriptUrl, reservasiSheetId, reservasiSheetName)
                }
                // Fallback jika tab di Google Sheets masih menggunakan nama GM
                if (csv.isNullOrBlank()) {
                    val fallbackCsv = GoogleAppsScriptHub.fetchCsvFromSheetId(reservasiSheetId, "GM")
                        ?: if (sessionManager.scriptUrl.isNotBlank()) GoogleAppsScriptHub.fetchCsvViaScript(sessionManager.scriptUrl, reservasiSheetId, "GM") else null
                    if (!fallbackCsv.isNullOrBlank()) {
                        csv = fallbackCsv
                    }
                }
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parseReservasiCsv(csv)
                    if (parsed.isNotEmpty()) {
                        reservasiDao.deleteAllReservasi()
                        val chunkSize = 50
                        parsed.chunked(chunkSize).forEach { batch ->
                            reservasiDao.insertReservasiList(batch)
                            kotlinx.coroutines.yield()
                        }
                        anySynced = true
                    }
                }
            }

            // 9b. Sync ARGO (Spreadsheet ID: 13iuagC5JY1NOkVRMwebz8-XvaEp-2q_qiyaDedGedNQ, Sheet: ARGO)
            val argoSheetId = sessionManager.sheetArgo.trim()
            val argoSheetName = sessionManager.sheetArgoName.trim()
            if (argoSheetId.isNotBlank()) {
                var csv = GoogleAppsScriptHub.fetchCsvFromSheetId(argoSheetId, argoSheetName)
                if (csv.isNullOrBlank() && sessionManager.scriptUrl.isNotBlank()) {
                    csv = GoogleAppsScriptHub.fetchCsvViaScript(sessionManager.scriptUrl, argoSheetId, argoSheetName)
                }
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parseArgoCsv(csv)
                    if (parsed.isNotEmpty()) {
                        argoDao.deleteAllArgo()
                        val chunkSize = 50
                        parsed.chunked(chunkSize).forEach { batch ->
                            argoDao.insertArgoList(batch)
                            kotlinx.coroutines.yield()
                        }
                        anySynced = true
                    }
                }
            }

            // 10. Sync RIWAYAT SERVICE TEKNIK (Spreadsheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Sheet: SKD)
            val pengerjaanTeknikSheetId = sessionManager.sheetPengerjaanTeknik.trim()
            val pengerjaanTeknikSheetName = sessionManager.sheetPengerjaanTeknikName.trim()
            if (pengerjaanTeknikSheetId.isNotBlank()) {
                val csv = GoogleAppsScriptHub.fetchCsvFromSheetId(pengerjaanTeknikSheetId, pengerjaanTeknikSheetName)
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parsePengerjaanTeknikCsv(csv)
                    if (parsed.isNotEmpty()) {
                        pengerjaanTeknikDao.deleteAllPengerjaanTeknik()
                        val chunkSize = 50
                        parsed.chunked(chunkSize).forEach { batch ->
                            pengerjaanTeknikDao.insertPengerjaanTeknikList(batch)
                            kotlinx.coroutines.yield()
                        }
                        anySynced = true
                    }
                }
            }

            // 11. Sync RIWAYAT SERVICE SKI (Spreadsheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Sheet: SKI)
            val riwayatSkiSheetId = sessionManager.sheetRiwayatSki.trim().ifBlank { SessionManager.DEFAULT_RIWAYAT_SKI_SHEET_ID }
            val riwayatSkiSheetName = sessionManager.sheetRiwayatSkiName.trim().ifBlank { SessionManager.DEFAULT_RIWAYAT_SKI_SHEET_NAME }
            if (riwayatSkiSheetId.isNotBlank()) {
                var csv = GoogleAppsScriptHub.fetchCsvFromSheetId(riwayatSkiSheetId, riwayatSkiSheetName)
                if (csv.isNullOrBlank() && sessionManager.scriptUrl.isNotBlank()) {
                    csv = GoogleAppsScriptHub.fetchCsvViaScript(sessionManager.scriptUrl, riwayatSkiSheetId, riwayatSkiSheetName)
                }
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parseRiwayatSkiCsv(csv)
                    if (parsed.isNotEmpty()) {
                        riwayatSkiDao.deleteAllRiwayatSki()
                        val chunkSize = 50
                        parsed.chunked(chunkSize).forEach { batch ->
                            riwayatSkiDao.insertRiwayatSkiList(batch)
                            kotlinx.coroutines.yield()
                        }
                        anySynced = true
                    }
                }
            }



            // 12. Sync USER BENGKEL (Spreadsheet ID: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30, Sheet: USER)
            val userSheetId = sessionManager.sheetUser.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_ID }
            val userSheetName = sessionManager.sheetUserName.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_NAME }
            if (userSheetId.isNotBlank()) {
                val csv = GoogleAppsScriptHub.fetchCsvFromSheetId(userSheetId, userSheetName)
                if (csv != null) {
                    val parsed = GoogleAppsScriptHub.parseUserCsv(csv)
                    // Data di aplikasi selalu mengikuti persis isi Google Sheets (termasuk jika ada perubahan/penghapusan di sheet)
                    userDao.deleteAllUsers()
                    if (parsed.isNotEmpty()) {
                        val withIds = parsed.mapIndexed { index, u -> u.copy(id = index + 1) }
                        userDao.insertUsers(withIds)
                    }
                    anySynced = true
                }
            }



            // 14. Sync PEKERJAAN SERVICE KEINDAHAN (Spreadsheet ID: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30, Sheet: SERVICE_KEINDAHAN)
            val pkSheetId = sessionManager.sheetPekerjaanServiceKeindahan.trim().ifBlank { SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID }
            val pkSheetName = sessionManager.sheetPekerjaanServiceKeindahanName.trim().ifBlank { SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME }
            if (pkSheetId.isNotBlank()) {
                var csv = GoogleAppsScriptHub.fetchCsvFromSheetId(pkSheetId, pkSheetName)
                if (csv.isNullOrBlank() && sessionManager.scriptUrl.isNotBlank()) {
                    csv = GoogleAppsScriptHub.fetchCsvViaScript(sessionManager.scriptUrl, pkSheetId, pkSheetName)
                }
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parsePekerjaanServiceKeindahanCsv(csv)
                    if (parsed.isNotEmpty()) {
                        val unsynced = pekerjaanKeindahanDao.getUnsyncedPekerjaanServiceKeindahan()
                        pekerjaanKeindahanDao.deleteAllPekerjaanServiceKeindahan()
                        val chunkSize = 50
                        parsed.chunked(chunkSize).forEach { batch ->
                            pekerjaanKeindahanDao.insertPekerjaanServiceKeindahanList(batch)
                            kotlinx.coroutines.yield()
                        }
                        if (unsynced.isNotEmpty()) {
                            unsynced.forEach {
                                pekerjaanKeindahanDao.insertPekerjaanServiceKeindahan(it)
                            }
                        }
                        anySynced = true
                    }
                }
            }

            // 15. Sync PREDIKSI PART (Spreadsheet ID: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs, Sheet: PREDIKSI_PART)
            val prediksiPartSheetId = sessionManager.sheetPrediksiPart.trim().ifBlank { SessionManager.DEFAULT_PREDIKSI_PART_SHEET_ID }
            val prediksiPartSheetName = sessionManager.sheetPrediksiPartName.trim().ifBlank { SessionManager.DEFAULT_PREDIKSI_PART_SHEET_NAME }
            if (prediksiPartSheetId.isNotBlank()) {
                val csv = GoogleAppsScriptHub.fetchCsvFromSheetId(prediksiPartSheetId, prediksiPartSheetName)
                if (!csv.isNullOrBlank()) {
                    val parsed = GoogleAppsScriptHub.parsePrediksiPartCsv(csv)
                    if (parsed.isNotEmpty()) {
                        prediksiPartDao.deleteAllPrediksiPart()
                        val chunkSize = 50
                        parsed.chunked(chunkSize).forEach { batch ->
                            prediksiPartDao.insertPrediksiPartList(batch)
                            kotlinx.coroutines.yield()
                        }
                        anySynced = true
                    }
                }
            }

            sessionManager.lastSyncTimestamp = System.currentTimeMillis()
            Result.success(anySynced)
        } catch (e: Exception) {
            sessionManager.lastSyncTimestamp = System.currentTimeMillis()
            Result.failure(e)
        }
    }

    /**
     * Sinkronisasi Khusus Pengerjaan Cat dari Google Sheets:
     * ID: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs | Sheet: PENGERJAAN_CAT
     */
    suspend fun syncPengerjaanCatFromGoogleSheets(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val catSheetId = sessionManager.sheetPengerjaanCat.trim().ifBlank { SessionManager.DEFAULT_PENGERJAAN_CAT_SHEET_ID }
            val catSheetName = sessionManager.sheetPengerjaanCatName.trim().ifBlank { SessionManager.DEFAULT_PENGERJAAN_CAT_SHEET_NAME }
            val csv = GoogleAppsScriptHub.fetchCsvFromSheetId(catSheetId, catSheetName)
            if (!csv.isNullOrBlank()) {
                val parsed = GoogleAppsScriptHub.parsePengerjaanCatCsv(csv)
                if (parsed.isNotEmpty()) {
                    catDao.deleteAllPengerjaanCat()
                    val chunkSize = 50
                    parsed.chunked(chunkSize).forEach { batch ->
                        catDao.insertPengerjaanCatList(batch)
                        kotlinx.coroutines.yield()
                    }
                    Result.success(parsed.size)
                } else {
                    Result.success(0)
                }
            } else {
                Result.failure(Exception("Gagal mengunduh CSV Pengerjaan Cat dari Google Sheets."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sinkronisasi Khusus User Bengkel dari Google Sheets:
     * ID: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30 | Sheet: USER
     * Data di aplikasi akan selalu sama persis dengan Google Sheets.
     * Setiap ada perubahan (tambah, edit, atau hapus) di Google Sheets, database lokal akan diperbarui.
     */
    suspend fun syncUserFromGoogleSheets(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val userSheetId = sessionManager.sheetUser.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_ID }
            val userSheetName = sessionManager.sheetUserName.trim().ifBlank { SessionManager.DEFAULT_USER_SHEET_NAME }
            val csv = GoogleAppsScriptHub.fetchCsvFromSheetId(userSheetId, userSheetName)
            if (csv == null) {
                return@withContext Result.failure(Exception("Gagal mengambil data dari Google Sheets. Pastikan Spreadsheet ID dan nama Sheet USER benar."))
            }
            val parsed = GoogleAppsScriptHub.parseUserCsv(csv)
            val withIds = parsed.mapIndexed { index, u ->
                u.copy(id = index + 1)
            }

            // Cek apakah ada perbedaan antara data di HP dengan data terbaru di Google Sheets
            val currentLocal = userDao.getAllUsersList()
            val isIdentical = currentLocal.size == withIds.size && currentLocal.zip(withIds).all { (a, b) ->
                a.nip == b.nip &&
                    a.namaMekanik == b.namaMekanik &&
                    a.username == b.username &&
                    a.password == b.password &&
                    a.level == b.level
            }

            if (!isIdentical) {
                // Data di HP selalu mengikuti data di Google Sheets secara persis
                userDao.deleteAllUsers()
                if (withIds.isNotEmpty()) {
                    userDao.insertUsers(withIds)
                }
            }

            // Perbarui juga sesi user yang sedang login jika datanya berubah di Google Sheets
            val loggedIn = sessionManager.getCurrentUser()
            if (loggedIn != null) {
                val updatedLoggedIn = withIds.find {
                    it.nip.equals(loggedIn.nip, ignoreCase = true) ||
                        it.username.equals(loggedIn.username, ignoreCase = true)
                }
                if (updatedLoggedIn != null && updatedLoggedIn != loggedIn) {
                    sessionManager.saveSession(updatedLoggedIn)
                }
            }

            // Perbarui signature GViz terakhir agar sinkron
            val latestSig = fetchSheetGvizSig(userSheetId, userSheetName)
            if (latestSig.isNotBlank()) {
                lastKnownUserSig = latestSig
            }

            Result.success(withIds.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
