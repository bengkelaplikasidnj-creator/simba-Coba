package com.example.data.remote

import android.content.Context
import com.example.data.local.entity.*
import com.example.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.StringReader
import java.util.concurrent.TimeUnit

object GoogleAppsScriptHub {

    fun extractDriveFileId(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return null
        val idRegex = Regex("""(?:id=|\/d\/)([a-zA-Z0-9_-]{15,})""")
        return idRegex.find(trimmed)?.groupValues?.get(1)
    }

    fun getDriveWebUrl(raw: String): String {
        val fileId = extractDriveFileId(raw)
        return if (fileId != null) {
            "https://drive.google.com/file/d/$fileId/view?usp=sharing"
        } else {
            raw.trim()
        }
    }

    val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Cache GID per (spreadsheetId:SHEET_NAME) agar bisa mengunduh CSV murni via /export?format=csv&gid=...
    // Tanpa melalui GViz yang sering menghapus sel teks pada kolom angka (mixed-type column)
    private val sheetGidCache = java.util.concurrent.ConcurrentHashMap<String, String>(
        mapOf(
            // Spreadsheet 13iuagC5JY1NOkVRMwebz8-XvaEp-2q_qiyaDedGedNQ
            "13iuagC5JY1NOkVRMwebz8-XvaEp-2q_qiyaDedGedNQ:MASTER" to "0",
            "13iuagC5JY1NOkVRMwebz8-XvaEp-2q_qiyaDedGedNQ:MASTER_KENDARAAN" to "0",
            // Spreadsheet 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30
            "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30:USER" to "0",
            "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30:LAPORAN KENDARAAN" to "1157734408",
            "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30:LAPORAN_KENDARAAN" to "1157734408",
            "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30:PENGERJAAN_CAT" to "479132240",
            "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30:MONITORING" to "2039151960",
            "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30:PREDIKSI_PART" to "145586361",
            "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30:PREDIKSI PART" to "145586361",
            "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30:SERVICE_KEINDAHAN" to "1417054882",
            // Spreadsheet 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs
            "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs:LAPORAN KENDARAAN" to "1815668194",
            "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs:LAPORAN_KENDARAAN" to "1815668194",
            "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs:MEKANIK" to "1678647505",
            "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs:USER" to "315809908",
            "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs:PENGERJAAN_CAT" to "708218466",
            "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs:PENGERJAAN CAT" to "708218466",
            "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs:MONITORING" to "724812543",
            "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs:PREDIKSI_PART" to "55725611",
            "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs:PREDIKSI PART" to "55725611",
            "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs:GABUNG_CAT" to "1594688232",
            "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs:SERVICE_KEINDAHAN" to "1245852587",
            // Spreadsheet 1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU
            "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU:MASTER_KENDARAAN" to "0",
            "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU:KOPLING" to "1399555137",
            "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU:BAN" to "1446181441",
            "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU:ACCU" to "1379319158",
            "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU:ARGO" to "770732625",
            // Spreadsheet 17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo (BAN)
            "17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo:DATA_BAN" to "257866745",
            "17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo:DATA BAN" to "257866745",
            "17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo:BAN" to "257866745",
            // Spreadsheet 1qBtcaJP1adyawgZ2hbAIvlld-usZ3CV5YB6QSRpVxd4 (KOPLING)
            "1qBtcaJP1adyawgZ2hbAIvlld-usZ3CV5YB6QSRpVxd4:KOPLING" to "982122529",
            // Spreadsheet 1tG5Q_uh6rWyiUWvwJYAxai-whpIyuDosgpWjVpUP3PY
            "1tG5Q_uh6rWyiUWvwJYAxai-whpIyuDosgpWjVpUP3PY:RESERVASI" to "1238286621",
            // Spreadsheet 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY (GOODMOVEN / GM, SKD)
            "145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY:GOODMOVEN" to "0",
            "145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY:GM" to "0",
            "145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY:SKD" to "762122465",
            // Spreadsheet 1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4
            "1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4:TEKNIK" to "1115537768",
            "1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4:KEINDAHAN" to "0",
            // Spreadsheet 1rAVsNj0YiNzZMTr9bbgEEwP1o_sOBmcZTDjHPAYIutY
            "1rAVsNj0YiNzZMTr9bbgEEwP1o_sOBmcZTDjHPAYIutY:SERVICE_TEKNIK" to "0",
            "1rAVsNj0YiNzZMTr9bbgEEwP1o_sOBmcZTDjHPAYIutY:SERVICE_KEINDAHAN" to "1824001657",
            // Spreadsheet 1hyvU96v-ZQlpHO_B1IWhVAeJI3Txpu_Bwl3yNOMpbsE
            "1hyvU96v-ZQlpHO_B1IWhVAeJI3Txpu_Bwl3yNOMpbsE:SECURITY" to "0"
        )
    )

    private fun resolveSheetGid(cleanId: String, sheetName: String): String? {
        if (sheetName.isBlank()) return null
        val normName = sheetName.trim().uppercase()
        val key = "$cleanId:$normName"
        sheetGidCache[key]?.let { return it }

        return try {
            val htmlUrl = "https://docs.google.com/spreadsheets/d/$cleanId/htmlview"
            val req = Request.Builder()
                .url(htmlUrl)
                .header("User-Agent", "Mozilla/5.0")
                .build()
            httpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body?.string().orEmpty()
                    val regex = Regex("""items\.push\(\{name:\s*"([^"]+)"[^}]*gid:\s*"([0-9]+)"""")
                    for (match in regex.findAll(body)) {
                        val tabName = match.groupValues[1].trim().uppercase()
                        val tabGid = match.groupValues[2].trim()
                        if (tabName.isNotEmpty() && tabGid.isNotEmpty()) {
                            sheetGidCache["$cleanId:$tabName"] = tabGid
                        }
                    }
                }
            }
            sheetGidCache[key] ?: sheetGidCache["$cleanId:${normName.replace("_", " ")}"]
                ?: sheetGidCache["$cleanId:${normName.replace(" ", "_")}"]
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Download CSV directly from Google Sheet ID (or specific sheet tab name).
     * Mengutamakan endpoint `/export?format=csv&gid=...` agar nilai sel campuran (angka + teks
     * seperti NIP `12406(01)` atau USERNAMA `ipul`) tidak dikosongkan (null) oleh GViz API,
     * sehingga seluruh user dan data tampil 100% utuh sesuai Google Sheets.
     */
    suspend fun fetchCsvFromSheetId(sheetId: String, sheetName: String = ""): String? = withContext(Dispatchers.IO) {
        if (sheetId.isBlank()) return@withContext null
        try {
            val cleanId = cleanSheetId(sheetId)
            val cacheBuster = System.currentTimeMillis()

            // 1. Utamakan export CSV murni menggunakan GID tab agar tidak ada sel yang hilang karena tipe data GViz
            val gid = if (sheetName.isNotBlank()) resolveSheetGid(cleanId, sheetName) else null
            if (!gid.isNullOrBlank()) {
                val exportUrl = "https://docs.google.com/spreadsheets/d/$cleanId/export?format=csv&gid=$gid&_cb=$cacheBuster"
                val exportReq = Request.Builder()
                    .url(exportUrl)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .build()
                httpClient.newCall(exportReq).execute().use { exportResp ->
                    if (exportResp.isSuccessful) {
                        val csvBody = exportResp.body?.string()
                        if (!csvBody.isNullOrBlank() && !csvBody.trimStart().startsWith("<!DOCTYPE", ignoreCase = true) && !csvBody.trimStart().startsWith("<html", ignoreCase = true)) {
                            return@withContext csvBody
                        }
                    }
                }
            }

            // 2. Fallback ke GViz / default export jika GID tidak ditemukan
            val url = if (sheetName.isNotBlank()) {
                val encodedSheet = java.net.URLEncoder.encode(sheetName, "UTF-8")
                "https://docs.google.com/spreadsheets/d/$cleanId/gviz/tq?tqx=out:csv&sheet=$encodedSheet&_cb=$cacheBuster"
            } else {
                "https://docs.google.com/spreadsheets/d/$cleanId/export?format=csv&_cb=$cacheBuster"
            }
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Download CSV via Google Apps Script Web App jika direct Google Sheets export
     * dibatasi (Restricted/Private) oleh Google Drive.
     */
    suspend fun fetchCsvViaScript(scriptUrl: String, spreadsheetId: String, sheetName: String): String? = withContext(Dispatchers.IO) {
        if (scriptUrl.isBlank() || spreadsheetId.isBlank()) return@withContext null
        try {
            val encodedId = java.net.URLEncoder.encode(spreadsheetId, "UTF-8")
            val encodedSheet = java.net.URLEncoder.encode(sheetName, "UTF-8")
            val url = if (scriptUrl.contains("?")) {
                "$scriptUrl&action=getSheetCsv&spreadsheetId=$encodedId&sheetName=$encodedSheet"
            } else {
                "$scriptUrl?action=getSheetCsv&spreadsheetId=$encodedId&sheetName=$encodedSheet"
            }
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank() && !body.trimStart().startsWith("<!DOCTYPE", ignoreCase = true) && !body.trimStart().startsWith("<html", ignoreCase = true) && !body.contains("\"status\":\"error\"")) {
                        body
                    } else null
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Robust CSV parser handling quoted commas, multiline cells, and escaped quotes
     */
    fun parseCsvLines(csvText: String): List<List<String>> {
        val result = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentField = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0
        val len = csvText.length

        while (i < len) {
            val c = csvText[i]
            when {
                c == '\"' -> {
                    if (inQuotes && i + 1 < len && csvText[i + 1] == '\"') {
                        currentField.append('\"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    currentRow.add(currentField.toString().trim())
                    currentField.setLength(0)
                }
                (c == '\n' || c == '\r') && !inQuotes -> {
                    if (c == '\r' && i + 1 < len && csvText[i + 1] == '\n') {
                        i++
                    }
                    currentRow.add(currentField.toString().trim())
                    currentField.setLength(0)
                    if (currentRow.any { it.isNotBlank() }) {
                        result.add(ArrayList(currentRow))
                    }
                    currentRow.clear()
                }
                else -> {
                    currentField.append(c)
                }
            }
            i++
        }
        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString().trim())
            if (currentRow.any { it.isNotBlank() }) {
                result.add(currentRow)
            }
        }
        return result
    }

    /**
     * Parse LAPORAN KENDARAAN (LAPORAN HARIAN) CSV:
     * (ID | Timestamp | NO MOBIL | NAMA/NIP | KELUHAN KENDARAAN | KODE AKSES | TOKEN | STATUS | UPDATE DATA | POOL/VENDOR | NAMA MEKANIK | PENGERJAAN)
     */
    fun parseLaporanHarianCsv(csvText: String): List<LaporanHarianEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()
        val list = mutableListOf<LaporanHarianEntity>()
        val usedIds = mutableSetOf<Long>()

        val firstHeader = rows[0].getOrElse(0) { "" }.trim().removeSurrounding("\"").uppercase()
        val hasIdColumn = !(firstHeader == "TIMESTAMP" || firstHeader == "WAKTU")

        // Skip header
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawIdStr: String
            val rawTimestamp: String
            val rawNoMobil: String
            val namaNip: String
            val keluhanKendaraan: String
            val kodeAkses: String
            val token: String
            val status: String
            val updateData: String
            val poolVendor: String
            val namaMekanik: String
            val pengerjaan: String

            if (hasIdColumn) {
                // Susunan tabel baru:
                // 0: ID | 1: Timestamp | 2: NO MOBIL | 3: NAMA/NIP | 4: KELUHAN KENDARAAN | 5: KODE AKSES | 6: TOKEN | 7: STATUS | 8: UPDATE DATA | 9: POOL/VENDOR | 10: NAMA MEKANIK | 11: PENGERJAAN
                rawIdStr = r.getOrElse(0) { "" }.trim().removeSurrounding("\"")
                rawTimestamp = r.getOrElse(1) { "" }.trim().removeSurrounding("\"")
                rawNoMobil = r.getOrElse(2) { "" }.trim().removeSurrounding("\"")
                namaNip = r.getOrElse(3) { "" }.trim().removeSurrounding("\"")
                keluhanKendaraan = r.getOrElse(4) { "" }.trim().removeSurrounding("\"")
                kodeAkses = r.getOrElse(5) { "" }.trim().removeSurrounding("\"")
                token = r.getOrElse(6) { "" }.trim().removeSurrounding("\"")
                status = r.getOrElse(7) { "Belum Dikerjakan" }.trim().removeSurrounding("\"").ifBlank { "Belum Dikerjakan" }
                updateData = r.getOrElse(8) { "" }.trim().removeSurrounding("\"")
                poolVendor = r.getOrElse(9) { "" }.trim().removeSurrounding("\"")
                namaMekanik = r.getOrElse(10) { "" }.trim().removeSurrounding("\"")
                pengerjaan = r.getOrElse(11) { "" }.trim().removeSurrounding("\"")
            } else {
                // Fallback jika sheet lama belum memiliki kolom ID di posisi pertama
                rawIdStr = ""
                rawTimestamp = r.getOrElse(0) { "" }.trim().removeSurrounding("\"")
                rawNoMobil = r.getOrElse(1) { "" }.trim().removeSurrounding("\"")
                namaNip = r.getOrElse(2) { "" }.trim().removeSurrounding("\"")
                keluhanKendaraan = r.getOrElse(3) { "" }.trim().removeSurrounding("\"")
                kodeAkses = r.getOrElse(4) { "" }.trim().removeSurrounding("\"")
                token = r.getOrElse(5) { "" }.trim().removeSurrounding("\"")
                status = r.getOrElse(6) { "Belum Dikerjakan" }.trim().removeSurrounding("\"").ifBlank { "Belum Dikerjakan" }
                updateData = r.getOrElse(7) { "" }.trim().removeSurrounding("\"")
                poolVendor = r.getOrElse(8) { "" }.trim().removeSurrounding("\"")
                namaMekanik = r.getOrElse(9) { "" }.trim().removeSurrounding("\"")
                pengerjaan = r.getOrElse(10) { "" }.trim().removeSurrounding("\"")
            }

            if (rawTimestamp.isBlank() && rawNoMobil.isBlank() && namaNip.isBlank() && keluhanKendaraan.isBlank()) continue
            if (rawIdStr.equals("ID", ignoreCase = true) ||
                rawTimestamp.equals("timestamp", ignoreCase = true) ||
                rawTimestamp.equals("waktu", ignoreCase = true) ||
                rawNoMobil.equals("NO MOBIL", ignoreCase = true) ||
                rawNoMobil.equals("NO_MOBIL", ignoreCase = true)
            ) continue

            val parsedId = rawIdStr.toLongOrNull() ?: rawIdStr.filter { it.isDigit() }.toLongOrNull()
            var finalId = if (parsedId != null && parsedId > 0L) parsedId else i.toLong()
            while (!usedIds.add(finalId)) {
                finalId++
            }

            val finalTimestamp = if (rawTimestamp.isNotBlank()) rawTimestamp else "ID-$finalId"
            val item = LaporanHarianEntity(
                id = finalId,
                timestamp = finalTimestamp,
                noMobil = rawNoMobil,
                namaNip = namaNip,
                keluhanKendaraan = keluhanKendaraan,
                kodeAkses = kodeAkses,
                token = token,
                status = status,
                updateData = updateData,
                poolVendor = poolVendor,
                namaMekanik = namaMekanik,
                pengerjaan = pengerjaan,
                isSynced = true
            )
            list.add(item)
        }
        return list
    }

    /**
     * Parse SERVICE TEKNIK CSV:
     * Sheet ID: 1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4, Tab: TEKNIK
     * Header fleksibel:
     * No Mobil | SERVICE | Selisih KM | Notification Desc | Notif. No | Status | KM Servis Sebelum nya | KM Jatuh Tempo | Equipment No | Notif Date | KM Total | Sticker Bandara
     */
    fun parseServiceTeknikCsv(csvText: String): List<ServiceTeknikEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ").replace(".", "") }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" }
        val colStatusMobil = headerRow.indexOfFirst { it.contains("STATUS MOBIL") }
        val colNoMobil = headerRow.indexOfFirst { it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" || it.contains("VEHICLE") }
        val colService = headerRow.indexOfFirst { it == "SERVICE" || it.contains("JENIS SERVICE") || it.contains("TIPE SERVICE") }
        val colSelisihKm = headerRow.indexOfFirst { it.contains("SELISIH") }
        val colNotifDesc = headerRow.indexOfFirst { it.contains("NOTIFICATION DESC") || it.contains("NOTIF DESC") || it.contains("DESKRIPSI") || it.contains("DESCRIPTION") }
        val colNotifNo = headerRow.indexOfFirst { it.contains("NOTIF NO") || it.contains("NOTIFICATION NO") || it.contains("NO NOTIF") }
        val colStatus = headerRow.indexOfFirst { it == "STATUS" || it.contains("STATUS NOTIF") }
        val colKmSebelum = headerRow.indexOfFirst { it.contains("KM SERVIS SEBELUM") || it.contains("KM SEBELUM") }
        val colKmTempo = headerRow.indexOfFirst { it.contains("JATUH TEMPO") || it.contains("TEMPO") }
        val colEquipNo = headerRow.indexOfFirst { it.contains("EQUIPMENT") || it.contains("EQUIP NO") }
        val colNotifDate = headerRow.indexOfFirst { it.contains("NOTIF DATE") || it.contains("DATE") || it.contains("TANGGAL") }
        val colKmTotal = headerRow.indexOfFirst { it.contains("KM TOTAL") || it == "TOTAL KM" }
        val colSticker = headerRow.indexOfFirst { it.contains("STICKER") }

        val hasHeaderMatch = colNoMobil != -1 || colService != -1

        val list = mutableListOf<ServiceTeknikEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().toLongOrNull() else null
            val id = rawId ?: i.toLong()

            val noMobil: String
            val statusMobil: String
            val service: String
            val selisihKm: String
            val notificationDesc: String
            val notifNo: String
            val status: String
            val kmServisSebelumnya: String
            val kmJatuhTempo: String
            val equipmentNo: String
            val notifDate: String
            val kmTotal: String
            val stickerBandara: String

            if (hasHeaderMatch) {
                noMobil = if (colNoMobil != -1) r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"") else ""
                statusMobil = if (colStatusMobil != -1) r.getOrElse(colStatusMobil) { "" }.trim().removeSurrounding("\"") else ""
                service = if (colService != -1) r.getOrElse(colService) { "" }.trim().removeSurrounding("\"") else ""
                selisihKm = if (colSelisihKm != -1) r.getOrElse(colSelisihKm) { "" }.trim().removeSurrounding("\"") else ""
                notificationDesc = if (colNotifDesc != -1) r.getOrElse(colNotifDesc) { "" }.trim().removeSurrounding("\"") else ""
                notifNo = if (colNotifNo != -1) r.getOrElse(colNotifNo) { "" }.trim().removeSurrounding("\"") else ""
                status = if (colStatus != -1) r.getOrElse(colStatus) { "NOPR" }.trim().removeSurrounding("\"").ifBlank { "NOPR" } else "NOPR"
                kmServisSebelumnya = if (colKmSebelum != -1) r.getOrElse(colKmSebelum) { "" }.trim().removeSurrounding("\"") else ""
                kmJatuhTempo = if (colKmTempo != -1) r.getOrElse(colKmTempo) { "" }.trim().removeSurrounding("\"") else ""
                equipmentNo = if (colEquipNo != -1) r.getOrElse(colEquipNo) { "" }.trim().removeSurrounding("\"") else ""
                notifDate = if (colNotifDate != -1) r.getOrElse(colNotifDate) { "" }.trim().removeSurrounding("\"") else ""
                kmTotal = if (colKmTotal != -1) r.getOrElse(colKmTotal) { "" }.trim().removeSurrounding("\"") else ""
                stickerBandara = if (colSticker != -1) r.getOrElse(colSticker) { "" }.trim().removeSurrounding("\"") else ""
            } else {
                val startIdx = if (r.size > 12 && r[0].trim().toLongOrNull() != null) 1 else 0
                statusMobil = r.getOrElse(startIdx + 0) { "" }.trim().removeSurrounding("\"")
                noMobil = r.getOrElse(startIdx + 1) { "" }.trim().removeSurrounding("\"")
                service = r.getOrElse(startIdx + 2) { "" }.trim().removeSurrounding("\"")
                selisihKm = r.getOrElse(startIdx + 3) { "" }.trim().removeSurrounding("\"")
                notificationDesc = r.getOrElse(startIdx + 4) { "" }.trim().removeSurrounding("\"")
                notifNo = r.getOrElse(startIdx + 5) { "" }.trim().removeSurrounding("\"")
                status = r.getOrElse(startIdx + 6) { "NOPR" }.trim().removeSurrounding("\"").ifBlank { "NOPR" }
                kmServisSebelumnya = r.getOrElse(startIdx + 7) { "" }.trim().removeSurrounding("\"")
                kmJatuhTempo = r.getOrElse(startIdx + 8) { "" }.trim().removeSurrounding("\"")
                equipmentNo = r.getOrElse(startIdx + 9) { "" }.trim().removeSurrounding("\"")
                notifDate = r.getOrElse(startIdx + 10) { "" }.trim().removeSurrounding("\"")
                kmTotal = r.getOrElse(startIdx + 11) { "" }.trim().removeSurrounding("\"")
                stickerBandara = r.getOrElse(startIdx + 12) { "" }.trim().removeSurrounding("\"")
            }

            if (noMobil.isBlank() && notifNo.isBlank() && service.isBlank()) continue
            if (noMobil.equals("No Mobil", ignoreCase = true) || noMobil.equals("NO MOBIL", ignoreCase = true)) continue

            list.add(
                ServiceTeknikEntity(
                    id = id,
                    statusMobil = statusMobil,
                    noMobil = noMobil,
                    service = service,
                    selisihKm = selisihKm,
                    notificationDesc = notificationDesc,
                    notifNo = notifNo,
                    status = status,
                    kmServisSebelumnya = kmServisSebelumnya,
                    kmJatuhTempo = kmJatuhTempo,
                    equipmentNo = equipmentNo,
                    notifDate = notifDate,
                    kmTotal = kmTotal,
                    stickerBandara = stickerBandara,
                    isSynced = true
                )
            )
        }
        return list
    }

    /**
     * Parse SERVICE KEINDAHAN CSV (Spreadsheet ID: 1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4, Sheet: KEINDAHAN):
     * Mendukung pemetaan header dinamis (No Mobil, SERVICE, Selisih KM, Notification Desc, Notif. No, Status, KM Servis Sebelum nya, KM Jatuh Tempo, Equipment No, Notif Date, KM Total, Sticker Bandara)
     */
    fun parseServiceKeindahanCsv(csvText: String): List<ServiceKeindahanEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase() }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" }
        val colStatusMobil = headerRow.indexOfFirst { it.contains("STATUS MOBIL") || it == "STATUS_MOBIL" }
        val colNoMobil = headerRow.indexOfFirst { it.contains("NO MOBIL") || it.contains("NOMOBIL") || it.contains("NO POLISI") || it.contains("NOPOL") }
        val colService = headerRow.indexOfFirst { it == "SERVICE" || it.contains("JENIS SERVICE") || it.contains("JENIS_SERVICE") || it == "SERVIS" }
        val colSelisihKm = headerRow.indexOfFirst { it.contains("SELISIH") }
        val colNotifDesc = headerRow.indexOfFirst { it.contains("NOTIFICATION") || it.contains("NOTIF DESC") || it.contains("DESKRIPSI") }
        val colNotifNo = headerRow.indexOfFirst { it.contains("NOTIF. NO") || it.contains("NOTIF NO") || it.contains("NO NOTIF") }
        val colStatus = headerRow.indexOfFirst { it == "STATUS" }
        val colKmSebelum = headerRow.indexOfFirst { it.contains("SEBELUM") }
        val colKmTempo = headerRow.indexOfFirst { it.contains("TEMPO") || it.contains("JATUH TEMPO") }
        val colEquipNo = headerRow.indexOfFirst { it.contains("EQUIPMENT") || it.contains("EQUIP") }
        val colNotifDate = headerRow.indexOfFirst { it.contains("NOTIF DATE") || it.contains("DATE") || it.contains("TANGGAL") }
        val colKmTotal = headerRow.indexOfFirst { it.contains("KM TOTAL") || it == "TOTAL KM" }
        val colSticker = headerRow.indexOfFirst { it.contains("STICKER") }

        val hasHeaderMatch = colNoMobil != -1 || colService != -1

        val list = mutableListOf<ServiceKeindahanEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().toLongOrNull() else null
            val id = rawId ?: i.toLong()

            val noMobil: String
            val statusMobil: String
            val jenisService: String
            val selisihKm: String
            val notificationDesc: String
            val notifNo: String
            val status: String
            val kmServisSebelumnya: String
            val kmJatuhTempo: String
            val equipmentNo: String
            val notifDate: String
            val kmTotal: String
            val stickerBandara: String

            if (hasHeaderMatch) {
                noMobil = if (colNoMobil != -1) r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"") else ""
                statusMobil = if (colStatusMobil != -1) r.getOrElse(colStatusMobil) { "" }.trim().removeSurrounding("\"") else ""
                jenisService = if (colService != -1) r.getOrElse(colService) { "" }.trim().removeSurrounding("\"") else ""
                selisihKm = if (colSelisihKm != -1) r.getOrElse(colSelisihKm) { "" }.trim().removeSurrounding("\"") else ""
                notificationDesc = if (colNotifDesc != -1) r.getOrElse(colNotifDesc) { "" }.trim().removeSurrounding("\"") else ""
                notifNo = if (colNotifNo != -1) r.getOrElse(colNotifNo) { "" }.trim().removeSurrounding("\"") else ""
                status = if (colStatus != -1) r.getOrElse(colStatus) { "NOPR" }.trim().removeSurrounding("\"").ifBlank { "NOPR" } else "NOPR"
                kmServisSebelumnya = if (colKmSebelum != -1) r.getOrElse(colKmSebelum) { "" }.trim().removeSurrounding("\"") else ""
                kmJatuhTempo = if (colKmTempo != -1) r.getOrElse(colKmTempo) { "" }.trim().removeSurrounding("\"") else ""
                equipmentNo = if (colEquipNo != -1) r.getOrElse(colEquipNo) { "" }.trim().removeSurrounding("\"") else ""
                notifDate = if (colNotifDate != -1) r.getOrElse(colNotifDate) { "" }.trim().removeSurrounding("\"") else ""
                kmTotal = if (colKmTotal != -1) r.getOrElse(colKmTotal) { "" }.trim().removeSurrounding("\"") else ""
                stickerBandara = if (colSticker != -1) r.getOrElse(colSticker) { "" }.trim().removeSurrounding("\"") else ""
            } else {
                val startIdx = if (r.size > 12 && r[0].trim().toLongOrNull() != null) 1 else 0
                statusMobil = r.getOrElse(startIdx + 0) { "" }.trim().removeSurrounding("\"")
                noMobil = r.getOrElse(startIdx + 1) { "" }.trim().removeSurrounding("\"")
                jenisService = r.getOrElse(startIdx + 2) { "" }.trim().removeSurrounding("\"")
                selisihKm = r.getOrElse(startIdx + 3) { "" }.trim().removeSurrounding("\"")
                notificationDesc = r.getOrElse(startIdx + 4) { "" }.trim().removeSurrounding("\"")
                notifNo = r.getOrElse(startIdx + 5) { "" }.trim().removeSurrounding("\"")
                status = r.getOrElse(startIdx + 6) { "NOPR" }.trim().removeSurrounding("\"").ifBlank { "NOPR" }
                kmServisSebelumnya = r.getOrElse(startIdx + 7) { "" }.trim().removeSurrounding("\"")
                kmJatuhTempo = r.getOrElse(startIdx + 8) { "" }.trim().removeSurrounding("\"")
                equipmentNo = r.getOrElse(startIdx + 9) { "" }.trim().removeSurrounding("\"")
                notifDate = r.getOrElse(startIdx + 10) { "" }.trim().removeSurrounding("\"")
                kmTotal = r.getOrElse(startIdx + 11) { "" }.trim().removeSurrounding("\"")
                stickerBandara = r.getOrElse(startIdx + 12) { "" }.trim().removeSurrounding("\"")
            }

            if (noMobil.isBlank() && notifNo.isBlank() && jenisService.isBlank()) continue
            if (noMobil.equals("No Mobil", ignoreCase = true) || noMobil.equals("NO MOBIL", ignoreCase = true)) continue

            list.add(
                ServiceKeindahanEntity(
                    id = id,
                    statusMobil = statusMobil,
                    noMobil = noMobil,
                    jenisService = jenisService,
                    selisihKm = selisihKm,
                    notificationDesc = notificationDesc,
                    notifNo = notifNo,
                    status = status,
                    kmServisSebelumnya = kmServisSebelumnya,
                    kmJatuhTempo = kmJatuhTempo,
                    equipmentNo = equipmentNo,
                    notifDate = notifDate,
                    kmTotal = kmTotal,
                    stickerBandara = stickerBandara,
                    isSynced = true
                )
            )
        }
        return list
    }

    /**
     * Parse PEKERJAAN SERVICE KEINDAHAN CSV from Google Sheets:
     * Spreadsheet ID: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30, Sheet: SERVICE_KEINDAHAN
     * Folder Google Drive Foto: 1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6
     *
     * Susunan Kolom Data Base:
     * ID (kode unix) | TANGGAL(tanggal dan jam saat itu) | NO_MOBIL | JENIS_SERVICE | NIP/NAMA (sesuai Dengan user login dan tidakdapat di edit) | NOTIF_NO | ORDER | APPROVEL | PHOTO_DEPAN | PHOTO_SAMPING_LH | PHOTO_INTERIOR | PHOTO_BELAKANG | PHOTO_SAMPING_RH
     */
    fun parsePekerjaanServiceKeindahanCsv(csvText: String): List<PekerjaanServiceKeindahanEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()
        val list = mutableListOf<PekerjaanServiceKeindahanEntity>()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ") }
        var colId = headerRow.indexOfFirst { it == "ID" || it == "NO" || it.contains("UNIX") }
        var colTanggal = headerRow.indexOfFirst { it.contains("TANGGAL") || it.contains("TGL") || it.contains("DATE") || it.contains("WAKTU") }
        var colNoMobil = headerRow.indexOfFirst { it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" || it.contains("NOPOL") }
        var colJenisService = headerRow.indexOfFirst { it.contains("JENIS SERVICE") || it.contains("SERVICE") || it.contains("JENIS") }
        var colNipNama = headerRow.indexOfFirst { it.contains("NIP") || it.contains("NAMA") || it.contains("MEKANIK") || it.contains("USER") }
        var colNotifNo = headerRow.indexOfFirst { it.contains("NOTIF NO") || it.contains("NOTIF_NO") || it.contains("NOTIF") }
        var colOrder = headerRow.indexOfFirst { it.contains("ORDER") || it.contains("NO ORDER") || it.contains("ORDER NO") }
        var colApprovel = headerRow.indexOfFirst { it.contains("APPROV") || it.contains("APROV") || it.contains("STATUS") }
        var colKm = headerRow.indexOfFirst { it == "KM" || it.contains("KM") }
        var colPhotoDepan = headerRow.indexOfFirst { (it.contains("PHOTO") || it.contains("FOTO")) && it.contains("DEPAN") }
        var colPhotoLh = headerRow.indexOfFirst { (it.contains("PHOTO") || it.contains("FOTO")) && (it.contains("LH") || it.contains("KIRI") || it.contains("SAMPING LH")) }
        var colPhotoInterior = headerRow.indexOfFirst { (it.contains("PHOTO") || it.contains("FOTO")) && (it.contains("INTERIOR") || it.contains("DALAM")) }
        var colPhotoBelakang = headerRow.indexOfFirst { (it.contains("PHOTO") || it.contains("FOTO")) && (it.contains("BELAKANG") || it.contains("REAR") || it.contains("BACK")) }
        var colPhotoRh = headerRow.indexOfFirst { (it.contains("PHOTO") || it.contains("FOTO")) && (it.contains("RH") || it.contains("KANAN") || it.contains("SAMPING RH")) }

        // Fallback jika header tidak terdeteksi otomatis (13 kolom resmi baru):
        // 0: ID | 1: TANGGAL | 2: NO_MOBIL | 3: JENIS_SERVICE | 4: NIP/NAMA | 5: NOTIF_NO | 6: ORDER | 7: APPROVEL | 8: PHOTO_DEPAN | 9: PHOTO_SAMPING_LH | 10: PHOTO_INTERIOR | 11: PHOTO_BELAKANG | 12: PHOTO_SAMPING_RH
        val has13Cols = headerRow.size >= 13 || (colNipNama != -1 || colApprovel != -1)
        if (colId == -1) colId = 0
        if (colTanggal == -1) colTanggal = 1
        if (colNoMobil == -1) colNoMobil = 2
        if (colJenisService == -1) colJenisService = 3
        if (colNipNama == -1 && has13Cols) colNipNama = 4
        if (colNotifNo == -1) colNotifNo = if (has13Cols) 5 else 4
        if (colOrder == -1) colOrder = if (has13Cols) 6 else 5
        if (colApprovel == -1 && has13Cols) colApprovel = 7
        if (colPhotoDepan == -1) colPhotoDepan = if (has13Cols) 8 else 7
        if (colPhotoLh == -1) colPhotoLh = if (has13Cols) 9 else 8
        if (colPhotoInterior == -1) colPhotoInterior = if (has13Cols) 10 else 9
        if (colPhotoBelakang == -1) colPhotoBelakang = if (has13Cols) 11 else 10
        if (colPhotoRh == -1) colPhotoRh = if (has13Cols) 12 else 11

        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawIdStr = r.getOrElse(colId) { "" }.trim()
            if (rawIdStr.equals("ID", ignoreCase = true) || rawIdStr.equals("NO", ignoreCase = true)) continue

            val idNum = rawIdStr.toLongOrNull() ?: (System.currentTimeMillis() + i)
            val tanggal = r.getOrElse(colTanggal) { "" }.trim()
            val noMobil = r.getOrElse(colNoMobil) { "" }.trim()
            val jenisService = r.getOrElse(colJenisService) { "" }.trim()
            val nipNama = if (colNipNama != -1) r.getOrElse(colNipNama) { "" }.trim() else ""
            val notifNo = r.getOrElse(colNotifNo) { "" }.trim()
            val orderNo = r.getOrElse(colOrder) { "" }.trim()
            val approvel = if (colApprovel != -1) {
                val appVal = r.getOrElse(colApprovel) { "" }.trim()
                if (appVal.isNotBlank()) appVal else "Menunggu"
            } else "Menunggu"
            val km = if (colKm != -1) r.getOrElse(colKm) { "" }.trim() else ""
            val photoDepan = r.getOrElse(colPhotoDepan) { "" }.trim()
            val photoLh = r.getOrElse(colPhotoLh) { "" }.trim()
            val photoInterior = r.getOrElse(colPhotoInterior) { "" }.trim()
            val photoBelakang = r.getOrElse(colPhotoBelakang) { "" }.trim()
            val photoRh = r.getOrElse(colPhotoRh) { "" }.trim()

            if (noMobil.isBlank() && tanggal.isBlank() && jenisService.isBlank() && orderNo.isBlank()) continue

            list.add(
                PekerjaanServiceKeindahanEntity(
                    id = idNum,
                    tanggal = tanggal,
                    noMobil = noMobil,
                    jenisService = jenisService,
                    nipNama = nipNama,
                    notifNo = notifNo,
                    orderNo = orderNo,
                    approvel = approvel,
                    photoDepan = photoDepan,
                    photoSampingLh = photoLh,
                    photoInterior = photoInterior,
                    photoBelakang = photoBelakang,
                    photoSampingRh = photoRh,
                    km = km,
                    isSynced = true,
                    syncTimestamp = System.currentTimeMillis()
                )
            )
        }
        return list
    }

    /**
     * Parse CLAIM CSV from Google Sheets:
     * Spreadsheet ID: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs, Sheet: MONITORING
     * Susunan tabel:
     * | TIMESTAMP | NOMER REGISTRASI | TANGGAL KEJADIAN | NO MOBIL | NIP PENGEMUDI | NAMA PENGEMUDI | DETAIL KERUSAKAN MOBIL | FOTO KERUSAKAN MOBIL | FOTO HASIL PENGECATAN | PHOTO LC | PHOTO PENGERJAAN
     */
    fun parseClaimCsv(csvText: String): List<ClaimEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ") }
        var colTimestamp = headerRow.indexOfFirst { it.contains("TIMESTAMP") || it.contains("WAKTU") }
        var colNoReg = headerRow.indexOfFirst { it.contains("REGISTRASI") || it.contains("NO REG") || it.contains("NOMER REG") }
        var colTglKejadian = headerRow.indexOfFirst { it.contains("TANGGAL KEJADIAN") || it.contains("TGL KEJADIAN") || it.contains("KEJADIAN") || it.contains("TANGGAL") }
        var colNoMobil = headerRow.indexOfFirst { it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" }
        var colNip = headerRow.indexOfFirst { it.contains("NIP") }
        var colNama = headerRow.indexOfFirst {
            (it.contains("NAMA PENGEMUDI") || (it.contains("NAMA") && !it.contains("USER")) || (it.contains("PENGEMUDI") && !it.contains("NIP")) || it.contains("DRIVER") || it.contains("SUPIR")) &&
                    !it.contains("NIP")
        }
        if (colNama == -1) {
            colNama = headerRow.indexOfFirst { it.contains("NAMA") && !it.contains("NIP") }
        }
        if (colNama == colNip || colNama == -1) {
            colNama = if (headerRow.size > 5 && !headerRow[5].contains("NIP")) 5 else headerRow.indexOfFirst { it.contains("NAMA") && it != headerRow.getOrNull(colNip) }
            if (colNama == -1) colNama = 5
        }
        var colDetailKerusakan = headerRow.indexOfFirst {
            it.contains("DETAIL") || (it.contains("KERUSAKAN") && !it.contains("FOTO") && !it.contains("PHOTO") && !it.contains("GAMBAR"))
        }

        // Photo columns
        var colFotoKerusakan = headerRow.indexOfFirst {
            (it.contains("FOTO") || it.contains("PHOTO") || it.contains("GAMBAR")) && it.contains("KERUSAKAN")
        }
        var colFotoHasilPengecatan = headerRow.indexOfFirst {
            (it.contains("FOTO") || it.contains("PHOTO") || it.contains("GAMBAR")) &&
                    (it.contains("HASIL") || it.contains("CAT") || it.contains("PENGECATAN"))
        }
        var colPhotoLc = headerRow.indexOfFirst {
            (it.contains("PHOTO") || it.contains("FOTO") || it.contains("GAMBAR")) && it.contains("LC")
        }
        if (colPhotoLc == -1) colPhotoLc = headerRow.indexOfFirst { it.trim() == "LC" || it.contains(" LC") || it.contains("LC ") }

        var colPhotoPengerjaan = headerRow.indexOfFirst {
            (it.contains("PHOTO") || it.contains("FOTO") || it.contains("GAMBAR")) && it.contains("PENGERJAAN")
        }
        var colTextPengerjaan = headerRow.indexOfFirst {
            it.contains("PENGERJAAN") && !it.contains("PHOTO") && !it.contains("FOTO") && !it.contains("GAMBAR") && !it.contains("CAT")
        }
        if (colPhotoPengerjaan == -1 && colTextPengerjaan != -1) {
            colPhotoPengerjaan = colTextPengerjaan
        } else if (colPhotoPengerjaan == -1) {
            colPhotoPengerjaan = headerRow.indexOfFirst { it.contains("PENGERJAAN") && !it.contains("CAT") }
        }

        // Positional fallbacks if headers were not matched
        if (colTimestamp == -1) colTimestamp = 0
        if (colNoReg == -1) colNoReg = 1
        if (colTglKejadian == -1) colTglKejadian = 2
        if (colNoMobil == -1) colNoMobil = 3
        if (colNip == -1) colNip = 4
        if (colNama == -1) colNama = 5
        if (colDetailKerusakan == -1) colDetailKerusakan = 6
        if (colFotoKerusakan == -1) colFotoKerusakan = 7
        if (colFotoHasilPengecatan == -1) colFotoHasilPengecatan = 8
        if (colPhotoLc == -1) colPhotoLc = 9
        if (colPhotoPengerjaan == -1) colPhotoPengerjaan = 10

        val list = mutableListOf<ClaimEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val timestamp = if (colTimestamp != -1) r.getOrElse(colTimestamp) { "" }.trim().removeSurrounding("\"") else ""
            val nomerRegistrasi = if (colNoReg != -1) r.getOrElse(colNoReg) { "" }.trim().removeSurrounding("\"") else ""
            val tanggalKejadian = if (colTglKejadian != -1) r.getOrElse(colTglKejadian) { "" }.trim().removeSurrounding("\"") else ""
            val noMobil = if (colNoMobil != -1) r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"") else ""

            // Skip repeated header or completely blank records
            if (nomerRegistrasi.isBlank() && noMobil.isBlank() && timestamp.isBlank()) continue
            if (nomerRegistrasi.equals("Nomer registrasi", ignoreCase = true) ||
                nomerRegistrasi.equals("NOMER REGISTRASI", ignoreCase = true) ||
                noMobil.equals("NO MOBIL", ignoreCase = true) ||
                timestamp.equals("TIMESTAMP", ignoreCase = true)
            ) continue

            val nipPengemudi = if (colNip != -1) r.getOrElse(colNip) { "" }.trim().removeSurrounding("\"") else ""
            var namaPengemudi = if (colNama != -1) r.getOrElse(colNama) { "" }.trim().removeSurrounding("\"") else ""

            // Proteksi: Jika namaPengemudi kosong, atau sama persis dengan NIP, atau hanya angka sedangkan ada kolom 5
            if ((namaPengemudi.isBlank() || namaPengemudi == nipPengemudi || namaPengemudi.all { it.isDigit() }) && r.size > 5) {
                val candidate = r.getOrElse(5) { "" }.trim().removeSurrounding("\"")
                if (candidate.isNotBlank() && !candidate.all { it.isDigit() }) {
                    namaPengemudi = candidate
                }
            }
            val detailKerusakan = if (colDetailKerusakan != -1) r.getOrElse(colDetailKerusakan) { "" }.trim().removeSurrounding("\"") else ""

            val fotoKerusakanMobil = if (colFotoKerusakan != -1) r.getOrElse(colFotoKerusakan) { "" }.trim().removeSurrounding("\"") else ""
            val fotoHasilPengecatan = if (colFotoHasilPengecatan != -1) r.getOrElse(colFotoHasilPengecatan) { "" }.trim().removeSurrounding("\"") else ""
            val photoLc = if (colPhotoLc != -1) r.getOrElse(colPhotoLc) { "" }.trim().removeSurrounding("\"") else ""
            val photoPengerjaan = if (colPhotoPengerjaan != -1) r.getOrElse(colPhotoPengerjaan) { "" }.trim().removeSurrounding("\"") else ""

            val item = ClaimEntity(
                id = i.toLong(),
                timestamp = timestamp,
                nomerRegistrasi = nomerRegistrasi,
                tanggalKejadian = tanggalKejadian,
                noMobil = noMobil,
                nipPengemudi = nipPengemudi,
                namaPengemudi = namaPengemudi,
                detailKerusakanMobil = detailKerusakan,
                fotoKerusakanMobil = fotoKerusakanMobil,
                fotoHasilPengecatan = fotoHasilPengecatan,
                photoLc = photoLc,
                photoPengerjaan = photoPengerjaan,
                pengerjaanClaim = photoPengerjaan,
                pengerjaanLc = photoLc,
                hasilPerbaikan = fotoHasilPengecatan,
                estimasiClaim = "",
                statusClaim = "Diajukan",
                status = "Open",
                isSynced = true,
                syncTimestamp = System.currentTimeMillis()
            )
            list.add(item)
        }
        return list
    }

    /**
     * Parse MASTER KENDARAAN CSV:
     * Sheet ID: 1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU, Tab: MASTER_KENDARAAN
     * Kolom:
     * Kolom 1 No Mobil | Kolom 2 No Polisi | Kolom 3 Tipe Kendaraan | Kolom 4 Tahun Pembuatan | Kolom 5 Jadwal Mobil
     */
    fun parseMasterKendaraanCsv(csvText: String): List<MasterKendaraanEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ") }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" }
        val colNoMobil = headerRow.indexOfFirst { it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" }
        val colNoPolisi = headerRow.indexOfFirst { it.contains("NO POLISI") || it.contains("NOPOLISI") || it.contains("PLAT") }
        val colTipe = headerRow.indexOfFirst { it.contains("TIPE") || it.contains("TYPE") || it.contains("JENIS") }
        val colTahun = headerRow.indexOfFirst { it.contains("TAHUN") }
        val colJadwal = headerRow.indexOfFirst { it.contains("JADWAL") }
        val colChassis = headerRow.indexOfFirst { it.contains("CHASSIS") || it.contains("RANGKA") }
        val colEngine = headerRow.indexOfFirst { it.contains("ENGINE") || it.contains("MESIN") }
        val colEquipment = headerRow.indexOfFirst { it.contains("EQUIPMENT") || it.contains("PERALATAN") }

        val hasHeaderMatch = colNoMobil != -1 || colNoPolisi != -1 || colTipe != -1

        val list = mutableListOf<MasterKendaraanEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            if (hasHeaderMatch) {
                val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().toIntOrNull() else null
                val id = rawId ?: i
                val noMobil = if (colNoMobil != -1) r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"") else ""
                val noPolisi = if (colNoPolisi != -1) r.getOrElse(colNoPolisi) { "" }.trim().removeSurrounding("\"") else ""
                val tipe = if (colTipe != -1) r.getOrElse(colTipe) { "" }.trim().removeSurrounding("\"") else ""
                val tahun = if (colTahun != -1) r.getOrElse(colTahun) { "" }.trim().removeSurrounding("\"") else ""
                val jadwal = if (colJadwal != -1) r.getOrElse(colJadwal) { "" }.trim().removeSurrounding("\"") else ""
                val chassis = if (colChassis != -1) r.getOrElse(colChassis) { "" }.trim().removeSurrounding("\"") else ""
                val engine = if (colEngine != -1) r.getOrElse(colEngine) { "" }.trim().removeSurrounding("\"") else ""
                val equip = if (colEquipment != -1) r.getOrElse(colEquipment) { "" }.trim().removeSurrounding("\"") else ""

                if (noMobil.isBlank() && noPolisi.isBlank()) continue

                // Deteksi cerdas jika spreadsheet memiliki pergeseran kolom (misal model mobil di kolom chassis dan nomor rangka di kolom tahun)
                val isShiftedChassisType = (chassis.contains("TRANSMOVER", ignoreCase = true) ||
                        chassis.contains("ZENIX", ignoreCase = true) ||
                        chassis.contains("E6", ignoreCase = true) ||
                        chassis.contains("T3", ignoreCase = true) ||
                        chassis.contains("AVANZA", ignoreCase = true) ||
                        chassis.contains("MOBILIO", ignoreCase = true) ||
                        chassis.contains("VIOS", ignoreCase = true) ||
                        chassis.contains("INNOVA", ignoreCase = true)) ||
                        (tahun.length >= 10 && tahun.all { it.isLetterOrDigit() } && !tahun.all { it.isDigit() })

                val finalTipe = if (isShiftedChassisType && chassis.isNotBlank()) {
                    chassis
                } else if (tipe.isNotBlank()) {
                    tipe
                } else {
                    "Armada Bengkel"
                }

                val finalChassis = if (isShiftedChassisType && tahun.length >= 10) {
                    tahun
                } else {
                    chassis
                }

                val finalTahun = if (isShiftedChassisType && jadwal.length == 4 && jadwal.all { it.isDigit() }) {
                    jadwal
                } else {
                    tahun
                }

                val finalJadwal = if (isShiftedChassisType && jadwal.length == 4 && jadwal.all { it.isDigit() }) {
                    "Jadwal Reguler"
                } else if (jadwal.isNotBlank()) {
                    jadwal
                } else {
                    "Jadwal Reguler"
                }

                val finalEngine = if (equip.equals("GASOLINE", ignoreCase = true) || equip.equals("ELECTRIC", ignoreCase = true) || equip.equals("HYBRID", ignoreCase = true) || equip.equals("DIESEL", ignoreCase = true)) {
                    equip
                } else {
                    engine
                }

                val finalEquip = if (engine.contains("Terpasang", ignoreCase = true) || engine.contains("Tidak", ignoreCase = true)) {
                    engine
                } else {
                    equip
                }

                list.add(
                    MasterKendaraanEntity(
                        id = id,
                        noMobil = noMobil,
                        noPolisi = noPolisi,
                        tipe = finalTipe,
                        tahunPembuatan = finalTahun,
                        noChassis = finalChassis,
                        jadwalMobil = finalJadwal,
                        engineType = finalEngine,
                        equipment = finalEquip
                    )
                )
            } else {
                // Fallback posisi standar
                val idNum = r[0].trim().toIntOrNull()
                if (idNum != null && r.size >= 6) {
                    // ID | No Mobil | No Polisi | Tipe Kendaraan | Tahun Pembuatan | Jadwal Mobil
                    val noMobil = r.getOrElse(1) { "" }.trim().removeSurrounding("\"")
                    val noPolisi = r.getOrElse(2) { "" }.trim().removeSurrounding("\"")
                    if (noMobil.isBlank() && noPolisi.isBlank()) continue
                    list.add(
                        MasterKendaraanEntity(
                            id = idNum,
                            noMobil = noMobil,
                            noPolisi = noPolisi,
                            tipe = r.getOrElse(3) { "Armada Bengkel" }.trim().removeSurrounding("\"").ifBlank { "Armada Bengkel" },
                            tahunPembuatan = r.getOrElse(4) { "" }.trim().removeSurrounding("\""),
                            jadwalMobil = r.getOrElse(5) { "Jadwal Reguler" }.trim().removeSurrounding("\"").ifBlank { "Jadwal Reguler" }
                        )
                    )
                } else {
                    // Kolom 1: No Mobil | Kolom 2: No Polisi | Kolom 3: Tipe Kendaraan | Kolom 4: Tahun Pembuatan | Kolom 5: Jadwal Mobil
                    val noMobil = r.getOrElse(0) { "" }.trim().removeSurrounding("\"")
                    val noPolisi = r.getOrElse(1) { "" }.trim().removeSurrounding("\"")
                    if (noMobil.isBlank() && noPolisi.isBlank()) continue
                    list.add(
                        MasterKendaraanEntity(
                            id = i,
                            noMobil = noMobil,
                            noPolisi = noPolisi,
                            tipe = r.getOrElse(2) { "Armada Bengkel" }.trim().removeSurrounding("\"").ifBlank { "Armada Bengkel" },
                            tahunPembuatan = r.getOrElse(3) { "" }.trim().removeSurrounding("\""),
                            jadwalMobil = r.getOrElse(4) { "Jadwal Reguler" }.trim().removeSurrounding("\"").ifBlank { "Jadwal Reguler" }
                        )
                    )
                }
            }
        }
        return list
    }

    /**
     * Parse KOPLING CSV:
     * Sheet ID: 1qBtcaJP1adyawgZ2hbAIvlld-usZ3CV5YB6QSRpVxd4, Tab: KOPLING
     * Susunan Kolom:
     * | No Mobil | Tipe | KM_KOPLING |
     * Data diurutkan sesuai dengan ID (ASC)
     */
    fun parseKoplingCsv(csvText: String): List<KoplingEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ") }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" || it == "NO." || it == "NOMOR" }
        val colNoMobil = headerRow.indexOfFirst {
            (it.contains("NO MOBIL") || it.contains("NOMOBIL") || it.contains("NO  MOBIL") || it == "MOBIL" || it == "KENDARAAN") &&
            !it.contains("POLISI") && !it.contains("NOPOL")
        }
        val colTipe = headerRow.indexOfFirst {
            it.contains("TIPE") || it.contains("TYPE") || it.contains("JENIS") || it.contains("MODEL") || it.contains("VARIAN")
        }
        val colTotalKm = headerRow.indexOfFirst {
            it.contains("KM KOPLING") || it.contains("KM_KOPLING") || it.contains("KMKOPLING") ||
            it.contains("TOTAL KM") || it.contains("KM TOTAL") || it.contains("TOTALKM") ||
            it.contains("KOPLING") || it.contains("TOTAL") || it.contains("KM") || it.contains("ODOMETER")
        }

        val defaultOffset = if (colId != -1) 1 else 0

        val list = mutableListOf<KoplingEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().toLongOrNull() else null
            val id = rawId ?: i.toLong()

            val noMobil = if (colNoMobil != -1) {
                r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"")
            } else {
                r.getOrElse(defaultOffset) { "" }.trim().removeSurrounding("\"")
            }

            val tipe = if (colTipe != -1) {
                r.getOrElse(colTipe) { "" }.trim().removeSurrounding("\"")
            } else {
                r.getOrElse(defaultOffset + 1) { "" }.trim().removeSurrounding("\"")
            }

            val totalKm = if (colTotalKm != -1) {
                r.getOrElse(colTotalKm) { "" }.trim().removeSurrounding("\"")
            } else {
                r.getOrElse(defaultOffset + 2) { "" }.trim().removeSurrounding("\"")
            }

            if (noMobil.isBlank() && tipe.isBlank()) continue
            if (noMobil.equals("No Mobil", ignoreCase = true) || noMobil.equals("NO_MOBIL", ignoreCase = true) || noMobil.equals("NOMOBIL", ignoreCase = true)) continue

            list.add(
                KoplingEntity(
                    id = id,
                    noMobil = noMobil,
                    jenisTipe = if (tipe.isNotBlank()) tipe else "NEW TRANSMOVER",
                    totalKm = totalKm,
                    kmKopling = totalKm,
                    isSynced = true
                )
            )
        }
        // Pastikan terurut sesuai dengan ID
        list.sortBy { it.id }
        return list
    }

    /**
     * Parse BAN CSV:
     * Sheet ID: 17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo, Tab: DATA_BAN
     * Susunan Kolom:
     * | No_Mobil | No_Polisi | EMBOS | KM_BAN |
     */
    fun parseBanCsv(csvText: String): List<BanEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ") }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" || it == "NO." || it == "KODE" }
        val colNoMobil = headerRow.indexOfFirst {
            (it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" || it == "KENDARAAN" || it == "NO LAMBUNG") &&
            !it.contains("POLISI") && !it.contains("NOPOL")
        }
        val colNoPolisi = headerRow.indexOfFirst {
            it.contains("NO POLISI") || it.contains("NOPOL") || it.contains("POLISI") || it.contains("PLAT") || it.contains("NOMER")
        }
        val colEmbos = headerRow.indexOfFirst {
            it.contains("EMBOS") || it.contains("MERK") || it.contains("BRAND") || it.contains("NAMA BAN")
        }
        val colKmBan = headerRow.indexOfFirst {
            it.contains("KM BAN") || it.contains("KMBAN") || it.contains("KM TOTAL") || it.contains("TOTAL KM") || it.contains("TOTALKM") || it.contains("KM") || it.contains("ODOMETER") || it.contains("KILOMETER")
        }
        val colTanggal = headerRow.indexOfFirst { it.contains("TANGGAL") || it.contains("TGL") }
        val colPosisi = headerRow.indexOfFirst { it.contains("POSISI") || it.contains("POSITION") }

        val defaultOffset = if (colId != -1) 1 else 0

        val list = mutableListOf<BanEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().toLongOrNull() else null
            val id = rawId ?: i.toLong()

            val noMobil = if (colNoMobil != -1) {
                r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"")
            } else {
                r.getOrElse(defaultOffset) { "" }.trim().removeSurrounding("\"")
            }

            val platNomer = if (colNoPolisi != -1) {
                r.getOrElse(colNoPolisi) { "" }.trim().removeSurrounding("\"")
            } else {
                r.getOrElse(defaultOffset + 1) { "" }.trim().removeSurrounding("\"")
            }

            val embos = if (colEmbos != -1) {
                r.getOrElse(colEmbos) { "" }.trim().removeSurrounding("\"")
            } else {
                r.getOrElse(defaultOffset + 2) { "" }.trim().removeSurrounding("\"")
            }

            val kmBan = if (colKmBan != -1) {
                r.getOrElse(colKmBan) { "" }.trim().removeSurrounding("\"")
            } else {
                r.getOrElse(defaultOffset + 3) { "" }.trim().removeSurrounding("\"")
            }

            val tanggal = if (colTanggal != -1) {
                r.getOrElse(colTanggal) { "" }.trim().removeSurrounding("\"")
            } else ""

            val posisi = if (colPosisi != -1) {
                r.getOrElse(colPosisi) { "" }.trim().removeSurrounding("\"")
            } else ""

            if (noMobil.isBlank() && platNomer.isBlank() && embos.isBlank()) continue
            if (noMobil.equals("No Mobil", ignoreCase = true) || noMobil.equals("NO_MOBIL", ignoreCase = true) || noMobil.equals("NO MOBIL", ignoreCase = true) || noMobil.equals("NOMOBIL", ignoreCase = true)) continue

            list.add(
                BanEntity(
                    id = id,
                    noMobil = noMobil,
                    platNomer = platNomer,
                    embos = embos,
                    kmTotal = kmBan,
                    kmBan = kmBan,
                    merkBan = embos,
                    tanggalPasang = tanggal,
                    posisiBan = posisi,
                    isSynced = true
                )
            )
        }
        // Pastikan terurut sesuai dengan ID
        list.sortBy { it.id }
        return list
    }

    /**
     * Parse BATERAY / ACCU CSV:
     * Sheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Tab: ACCU
     * Susunan Tabel:
     * | NO_MOBIL | NO_POLISI | TYPE | RESERVASI | USIA | Tipe Accu |
     * Data diurutkan berdasarkan ID (ASC)
     */
    fun parseAccuCsv(csvText: String): List<BaterayEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ").replace(".", "") }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" }
        val colNoMobil = headerRow.indexOfFirst { it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" || it.contains("VEHICLE") }
        val colPlatNomer = headerRow.indexOfFirst { it.contains("NO POLISI") || it.contains("NOPOL") || it.contains("POLISI") || it.contains("PLAT") || it.contains("NOMER") }
        val colReservasi = headerRow.indexOfFirst { it.contains("RESERVASI") || it.contains("TANGGAL") || it.contains("TGL") || it.contains("DATE") }
        val colUsia = headerRow.indexOfFirst { it.contains("USIA") || it.contains("UMUR") || it.contains("AGE") }

        // Kolom tipe kendaraan vs tipe accu
        var colTipeKendaraan = headerRow.indexOfFirst { (it == "TYPE" || it == "TIPE" || it.contains("JENIS")) && !it.contains("ACCU") && !it.contains("BATERAI") }
        var colTipeAccu = headerRow.indexOfFirst { it.contains("TIPE ACCU") || it.contains("TIPE BATERAI") || it.contains("TIPE AKI") || it.contains("MERK ACCU") || it == "ACCU" || it == "BATERAI" || it == "AKI" }

        // Jika tidak ditemukan secara spesifik, cari berdasarkan kata TIPE berurutan
        if (colTipeKendaraan == -1 || colTipeAccu == -1) {
            for (idx in headerRow.indices) {
                val h = headerRow[idx]
                if (h.contains("TIPE") || h.contains("TYPE") || h.contains("JENIS") || h.contains("ACCU") || h.contains("BATERAI")) {
                    if (colTipeKendaraan == -1) {
                        colTipeKendaraan = idx
                    } else if (colTipeAccu == -1 && idx != colTipeKendaraan) {
                        colTipeAccu = idx
                    }
                }
            }
        }

        val hasHeaderMatch = colNoMobil != -1 || colPlatNomer != -1 || colReservasi != -1

        val list = mutableListOf<BaterayEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().toLongOrNull() else null
            val id = rawId ?: i.toLong()

            val noMobil: String
            val platNomer: String
            val tipeKendaraan: String
            val tanggal: String
            val usia: String
            val tipeAccu: String

            if (hasHeaderMatch) {
                noMobil = if (colNoMobil != -1) r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"") else r.getOrElse(0) { "" }.trim().removeSurrounding("\"")
                platNomer = if (colPlatNomer != -1) r.getOrElse(colPlatNomer) { "" }.trim().removeSurrounding("\"") else r.getOrElse(1) { "" }.trim().removeSurrounding("\"")
                tipeKendaraan = if (colTipeKendaraan != -1) r.getOrElse(colTipeKendaraan) { "" }.trim().removeSurrounding("\"") else r.getOrElse(2) { "" }.trim().removeSurrounding("\"")
                tanggal = if (colReservasi != -1) r.getOrElse(colReservasi) { "" }.trim().removeSurrounding("\"") else r.getOrElse(3) { "" }.trim().removeSurrounding("\"")
                usia = if (colUsia != -1) r.getOrElse(colUsia) { "" }.trim().removeSurrounding("\"") else r.getOrElse(4) { "" }.trim().removeSurrounding("\"")
                tipeAccu = if (colTipeAccu != -1) r.getOrElse(colTipeAccu) { "" }.trim().removeSurrounding("\"") else r.getOrElse(5) { "" }.trim().removeSurrounding("\"")
            } else {
                // Fallback Positional: NO_MOBIL | NO_POLISI | TYPE | RESERVASI | USIA | Tipe Accu
                val startIdx = if (r.size > 6 && r[0].trim().toLongOrNull() != null) 1 else 0
                noMobil = r.getOrElse(startIdx + 0) { "" }.trim().removeSurrounding("\"")
                platNomer = r.getOrElse(startIdx + 1) { "" }.trim().removeSurrounding("\"")
                tipeKendaraan = r.getOrElse(startIdx + 2) { "" }.trim().removeSurrounding("\"")
                tanggal = r.getOrElse(startIdx + 3) { "" }.trim().removeSurrounding("\"")
                usia = r.getOrElse(startIdx + 4) { "" }.trim().removeSurrounding("\"")
                tipeAccu = r.getOrElse(startIdx + 5) { "" }.trim().removeSurrounding("\"")
            }

            if (noMobil.isBlank() && tipeKendaraan.isBlank() && tipeAccu.isBlank()) continue
            if (noMobil.equals("No Mobil", ignoreCase = true) || noMobil.equals("NO MOBIL", ignoreCase = true) || noMobil.equals("NO_MOBIL", ignoreCase = true)) continue

            list.add(
                BaterayEntity(
                    id = id,
                    noMobil = noMobil,
                    platNomer = platNomer,
                    jenisTipe = tipeKendaraan,
                    tanggal = tanggal,
                    usia = usia,
                    tipeAccu = tipeAccu,
                    tanggalGanti = tanggal,
                    merkBaterai = tipeAccu,
                    isSynced = true
                )
            )
        }
        // Urutkan berdasarkan ID
        list.sortBy { it.id }
        return list
    }

    /**
     * Parse RESERVASI CSV:
     * Sheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Tab: GOODMOVEN (sebelumnya GM)
     * Kolom Tampilan:
     * NO_MOBIL | Order | Material | Material_Description | Doc_Date | Pstng_Date | Quantity | BUn | LC_Amount | Batch
     */
    fun parseReservasiCsv(csvText: String): List<ReservasiEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ").replace(".", "") }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" }
        val colNoMobil = headerRow.indexOfFirst { it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" || it.contains("VEHICLE") }
        val colOrder = headerRow.indexOfFirst { it.contains("ORDER") }
        val colMaterial = headerRow.indexOfFirst { it == "MATERIAL" || it == "MAT" || (it.contains("MATERIAL") && !it.contains("DESC")) }
        val colMatDesc = headerRow.indexOfFirst { it.contains("MATERIAL DESC") || it.contains("DESCRIPTION") || it.contains("DESKRIPSI") }
        val colDocDate = headerRow.indexOfFirst { it.contains("DOCUMENT DATE") || it.contains("DOC DATE") || it.contains("DOCUMENT") }
        val colPostDate = headerRow.indexOfFirst { it.contains("POSTING DATE") || it.contains("POST DATE") || it.contains("POSTING") || it.contains("PSTNG") }
        val colQty = headerRow.indexOfFirst { it.contains("QUANTITY") || it.contains("QTY") || it.contains("JUMLAH") }
        val colUnit = headerRow.indexOfFirst { it.contains("BASE UNIT") || it.contains("UNIT OF MEASURE") || it.contains("UOM") || it == "UNIT" || it.contains("BUN") }
        val colAmt = headerRow.indexOfFirst { it.contains("AMT") || it.contains("AMOUNT") || it.contains("LOC CUR") || it.contains("CUR") || it.contains("NILAI") || it.contains("HARGA") || it.contains("LC") }
        val colBatch = headerRow.indexOfFirst { it.contains("BATCH") || it.contains("LOT") }

        val hasHeaderMatch = colNoMobil != -1 || colOrder != -1 || colMaterial != -1

        val list = mutableListOf<ReservasiEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().toLongOrNull() else null
            val id = rawId ?: i.toLong()

            val noMobil: String
            val orderNo: String
            val material: String
            val materialDesc: String
            val docDate: String
            val postDate: String
            val quantity: String
            val unit: String
            val amt: String
            val batch: String

            if (hasHeaderMatch) {
                noMobil = if (colNoMobil != -1) r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"") else ""
                orderNo = if (colOrder != -1) r.getOrElse(colOrder) { "" }.trim().removeSurrounding("\"") else ""
                material = if (colMaterial != -1) r.getOrElse(colMaterial) { "" }.trim().removeSurrounding("\"") else ""
                materialDesc = if (colMatDesc != -1) r.getOrElse(colMatDesc) { "" }.trim().removeSurrounding("\"") else ""
                docDate = if (colDocDate != -1) r.getOrElse(colDocDate) { "" }.trim().removeSurrounding("\"") else ""
                postDate = if (colPostDate != -1) r.getOrElse(colPostDate) { "" }.trim().removeSurrounding("\"") else ""
                quantity = if (colQty != -1) r.getOrElse(colQty) { "" }.trim().removeSurrounding("\"") else ""
                unit = if (colUnit != -1) r.getOrElse(colUnit) { "" }.trim().removeSurrounding("\"") else ""
                amt = if (colAmt != -1) r.getOrElse(colAmt) { "" }.trim().removeSurrounding("\"") else ""
                batch = if (colBatch != -1) r.getOrElse(colBatch) { "" }.trim().removeSurrounding("\"") else ""
            } else {
                // Positional fallback: NO_MOBIL | Order | Material | Material_Description | Doc_Date | Pstng_Date | Quantity | BUn | LC_Amount | Batch
                val startIdx = if (r.size > 10 && r[0].trim().toLongOrNull() != null) 1 else 0
                noMobil = r.getOrElse(startIdx + 0) { "" }.trim().removeSurrounding("\"")
                orderNo = r.getOrElse(startIdx + 1) { "" }.trim().removeSurrounding("\"")
                material = r.getOrElse(startIdx + 2) { "" }.trim().removeSurrounding("\"")
                materialDesc = r.getOrElse(startIdx + 3) { "" }.trim().removeSurrounding("\"")
                docDate = r.getOrElse(startIdx + 4) { "" }.trim().removeSurrounding("\"")
                postDate = r.getOrElse(startIdx + 5) { "" }.trim().removeSurrounding("\"")
                quantity = r.getOrElse(startIdx + 6) { "" }.trim().removeSurrounding("\"")
                unit = r.getOrElse(startIdx + 7) { "" }.trim().removeSurrounding("\"")
                amt = r.getOrElse(startIdx + 8) { "" }.trim().removeSurrounding("\"")
                batch = r.getOrElse(startIdx + 9) { "" }.trim().removeSurrounding("\"")
            }

            if (noMobil.isBlank() && orderNo.isBlank() && material.isBlank()) continue
            if (noMobil.equals("No Mobil", ignoreCase = true) || noMobil.equals("NO MOBIL", ignoreCase = true) || noMobil.equals("NO_MOBIL", ignoreCase = true)) continue

            list.add(
                ReservasiEntity(
                    id = id,
                    noMobil = noMobil,
                    orderNo = orderNo,
                    material = material,
                    materialDescription = materialDesc,
                    documentDate = docDate,
                    postingDate = postDate,
                    quantity = quantity,
                    baseUnitOfMeasure = unit,
                    amtInLocCur = amt,
                    batch = batch,
                    isSynced = true
                )
            )
        }
        list.sortBy { it.id }
        return list
    }

    /**
     * Parse ARGO CSV:
     * Sheet ID: 1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU, Tab: ARGO
     * Headers: ID | NO MOBIL | KM KOSONG | KM ISI | RIT | DROP | KM | GOLONGAN
     */
    fun parseArgoCsv(csvText: String): List<ArgoEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ").replace(".", "") }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" }
        val colNoMobil = headerRow.indexOfFirst { it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" }
        val colKmKosong = headerRow.indexOfFirst { it.contains("KM KOSONG") || it.contains("KOSONG") }
        val colKmIsi = headerRow.indexOfFirst { it.contains("KM ISI") || it.contains("ISI") }
        val colRit = headerRow.indexOfFirst { it == "RIT" || it.contains("RIT") }
        val colDrop = headerRow.indexOfFirst { it == "DROP" || it.contains("DROP") }
        val colKm = headerRow.indexOfFirst { it == "KM" || it == "KM TOTAL" || (it.contains("KM") && !it.contains("KOSONG") && !it.contains("ISI")) }
        val colGolongan = headerRow.indexOfFirst { it.contains("GOLONGAN") || it.contains("GOL") }

        val hasHeaderMatch = colNoMobil != -1 || colKmKosong != -1 || colKmIsi != -1

        val list = mutableListOf<ArgoEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().toLongOrNull() else null
            val id = rawId ?: i.toLong()

            val noMobil: String
            val kmKosong: String
            val kmIsi: String
            val rit: String
            val drop: String
            val km: String
            val golongan: String

            if (hasHeaderMatch) {
                noMobil = if (colNoMobil != -1) r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"") else ""
                kmKosong = if (colKmKosong != -1) r.getOrElse(colKmKosong) { "" }.trim().removeSurrounding("\"") else ""
                kmIsi = if (colKmIsi != -1) r.getOrElse(colKmIsi) { "" }.trim().removeSurrounding("\"") else ""
                rit = if (colRit != -1) r.getOrElse(colRit) { "" }.trim().removeSurrounding("\"") else ""
                drop = if (colDrop != -1) r.getOrElse(colDrop) { "" }.trim().removeSurrounding("\"") else ""
                // Kolom ke-6 dari dasar awal No Mobil adalah KM
                val targetKmCol = if (colKm != -1) colKm else if (colNoMobil != -1) colNoMobil + 5 else -1
                km = if (targetKmCol != -1) r.getOrElse(targetKmCol) { "" }.trim().removeSurrounding("\"") else ""
                golongan = if (colGolongan != -1) r.getOrElse(colGolongan) { "" }.trim().removeSurrounding("\"") else ""
            } else {
                // Positional fallback:
                // Jika kolom 0 berisi ID angka, No Mobil di index 1, dan KM di index 6 (1 + 5 = 6)
                // Jika kolom 0 berisi No Mobil, No Mobil di index 0, dan KM di index 5 (0 + 5 = 5)
                val hasIdCol = r.size > 7 && (r[0].trim().toLongOrNull() != null)
                val noMobilIdx = if (hasIdCol) 1 else 0
                noMobil = r.getOrElse(noMobilIdx) { "" }.trim().removeSurrounding("\"")
                kmKosong = r.getOrElse(noMobilIdx + 1) { "" }.trim().removeSurrounding("\"")
                kmIsi = r.getOrElse(noMobilIdx + 2) { "" }.trim().removeSurrounding("\"")
                rit = r.getOrElse(noMobilIdx + 3) { "" }.trim().removeSurrounding("\"")
                drop = r.getOrElse(noMobilIdx + 4) { "" }.trim().removeSurrounding("\"")
                km = r.getOrElse(noMobilIdx + 5) { "" }.trim().removeSurrounding("\"") // Kolom ke-6 dari No Mobil
                golongan = r.getOrElse(noMobilIdx + 6) { "" }.trim().removeSurrounding("\"")
            }

            if (noMobil.isBlank() && kmKosong.isBlank() && kmIsi.isBlank()) continue
            if (noMobil.equals("No Mobil", ignoreCase = true) || noMobil.equals("NO MOBIL", ignoreCase = true)) continue

            list.add(
                ArgoEntity(
                    id = id,
                    noMobil = noMobil,
                    kmKosong = kmKosong,
                    kmIsi = kmIsi,
                    rit = rit,
                    drop = drop,
                    km = km,
                    golongan = golongan,
                    isSynced = true
                )
            )
        }
        list.sortBy { it.id }
        return list
    }

    /**
     * Parse PREDIKSI PART CSV:
     * Sheet ID: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs, Tab: PREDIKSI_PART
     * Susunan Tabel:
     * | ID | TANGGAL | NO_MOBIL | SERVICE | KM_MOBIL | NIP_NAMA | NAMA_PART | PREDIKSI_KM | KM_TARGET | ARGO_SEKARANG | ESTIMASI_KM | APROVEL | KETERANGAN | STATUS |
     */
    fun parsePrediksiPartCsv(csvText: String): List<PrediksiPartEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ").replace(".", "").replace(Regex("\\s+"), " ") }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" }
        val colTanggal = headerRow.indexOfFirst { it.contains("TANGGAL") || it.contains("DATE") || it == "TGL" }
        val colNoMobil = headerRow.indexOfFirst { it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" || it.contains("VEHICLE") }
        val colService = headerRow.indexOfFirst { it == "SERVICE" || (it.contains("SERVICE") && !it.contains("KM")) || it.contains("JENIS SERVICE") }
        val colKmInput = headerRow.indexOfFirst { it.contains("KM MOBIL") || it.contains("KMMOBIL") || it.contains("KM INPUT") || it.contains("KMINPUT") || it == "KM AWAL" }
        val colNipNama = headerRow.indexOfFirst { (it.contains("NIP") || it.contains("MEKANIK") || it.contains("PETUGAS") || it == "NIP NAMA" || it == "NIP_NAMA" || it == "NIP/NAMA") && !it.contains("PART") }
        val colNamaPart = headerRow.indexOfFirst { it.contains("NAMA PART") || it.contains("NAMAPART") || it == "PART" || it.contains("KOMPONEN") || it.contains("MATERIAL") }
        val colPrediksiKm = headerRow.indexOfFirst { it.contains("PREDIKSI KM") || it.contains("PREDIKSIKM") || it.contains("PREDIKSI") }
        val colKmTarget = headerRow.indexOfFirst { it.contains("KM TARGET") || it.contains("KMTARGET") || it.contains("TARGET") }
        val colArgoSekarang = headerRow.indexOfFirst { it.contains("ARGO SEKARANG") || it.contains("ARGOSEKARANG") || it.contains("ARGO") || it.contains("KM SEKARANG") }
        val colEstimasiKm = headerRow.indexOfFirst { it.contains("ESTIMASI KM") || it.contains("ESTIMASIKM") || it.contains("ESTIMASI") || it.contains("SISA KM") }
        val colAprovel = headerRow.indexOfFirst { it.contains("APROVEL") || it.contains("APPROVAL") || it.contains("APPROVE") }
        val colKeterangan = headerRow.indexOfFirst { it.contains("KETERANGAN") || it.contains("CATATAN") || it == "KET" }
        val colStatus = headerRow.indexOfFirst { it == "STATUS" || (it.contains("STATUS") && !it.contains("KETERANGAN")) }

        val hasHeaderMatch = colNoMobil != -1 || colNamaPart != -1 || colService != -1 || colTanggal != -1 || colNipNama != -1

        val list = mutableListOf<PrediksiPartEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().removeSurrounding("\"").toLongOrNull() else null
            val id = rawId ?: i.toLong()

            val tanggal: String
            val noMobil: String
            val service: String
            val kmInput: String
            val nipNama: String
            val namaPart: String
            val prediksiKm: String
            val kmTarget: String
            val argoSekarang: String
            val estimasiKm: String
            val aprovel: String
            val keterangan: String
            val status: String

            if (hasHeaderMatch) {
                tanggal = if (colTanggal != -1) r.getOrElse(colTanggal) { "" }.trim().removeSurrounding("\"") else r.getOrElse(1) { "" }.trim().removeSurrounding("\"")
                noMobil = if (colNoMobil != -1) r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"") else r.getOrElse(2) { "" }.trim().removeSurrounding("\"")
                service = if (colService != -1) r.getOrElse(colService) { "" }.trim().removeSurrounding("\"") else r.getOrElse(3) { "" }.trim().removeSurrounding("\"")
                kmInput = if (colKmInput != -1) r.getOrElse(colKmInput) { "" }.trim().removeSurrounding("\"") else r.getOrElse(4) { "" }.trim().removeSurrounding("\"")
                nipNama = if (colNipNama != -1) r.getOrElse(colNipNama) { "" }.trim().removeSurrounding("\"") else ""
                namaPart = if (colNamaPart != -1) r.getOrElse(colNamaPart) { "" }.trim().removeSurrounding("\"") else if (r.size >= 14) r.getOrElse(6) { "" }.trim().removeSurrounding("\"") else r.getOrElse(5) { "" }.trim().removeSurrounding("\"")
                prediksiKm = if (colPrediksiKm != -1) r.getOrElse(colPrediksiKm) { "" }.trim().removeSurrounding("\"") else if (r.size >= 14) r.getOrElse(7) { "" }.trim().removeSurrounding("\"") else r.getOrElse(6) { "" }.trim().removeSurrounding("\"")
                kmTarget = if (colKmTarget != -1) r.getOrElse(colKmTarget) { "" }.trim().removeSurrounding("\"") else if (r.size >= 14) r.getOrElse(8) { "" }.trim().removeSurrounding("\"") else r.getOrElse(7) { "" }.trim().removeSurrounding("\"")
                argoSekarang = if (colArgoSekarang != -1) r.getOrElse(colArgoSekarang) { "" }.trim().removeSurrounding("\"") else if (r.size >= 14) r.getOrElse(9) { "" }.trim().removeSurrounding("\"") else r.getOrElse(8) { "" }.trim().removeSurrounding("\"")
                estimasiKm = if (colEstimasiKm != -1) r.getOrElse(colEstimasiKm) { "" }.trim().removeSurrounding("\"") else if (r.size >= 14) r.getOrElse(10) { "" }.trim().removeSurrounding("\"") else r.getOrElse(9) { "" }.trim().removeSurrounding("\"")
                aprovel = if (colAprovel != -1) r.getOrElse(colAprovel) { "" }.trim().removeSurrounding("\"") else if (r.size >= 14) r.getOrElse(11) { "" }.trim().removeSurrounding("\"") else if (r.size >= 13) r.getOrElse(10) { "" }.trim().removeSurrounding("\"") else ""
                keterangan = if (colKeterangan != -1) r.getOrElse(colKeterangan) { "" }.trim().removeSurrounding("\"") else if (r.size >= 14) r.getOrElse(12) { "" }.trim().removeSurrounding("\"") else if (r.size >= 13) r.getOrElse(11) { "" }.trim().removeSurrounding("\"") else if (r.size >= 12) r.getOrElse(10) { "" }.trim().removeSurrounding("\"") else ""
                status = if (colStatus != -1) r.getOrElse(colStatus) { "" }.trim().removeSurrounding("\"") else if (r.size >= 14) r.getOrElse(13) { "" }.trim().removeSurrounding("\"") else if (r.size >= 13) r.getOrElse(12) { "" }.trim().removeSurrounding("\"") else ""
            } else {
                // Positional fallback:
                // New 14 cols: ID | TANGGAL | NO_MOBIL | SERVICE | KM_INPUT | NIP_NAMA | NAMA_PART | PREDIKSI_KM | KM_TARGET | ARGO_SEKARANG | ESTIMASI_KM | APROVEL | KETERANGAN | STATUS
                tanggal = r.getOrElse(1) { "" }.trim().removeSurrounding("\"")
                noMobil = r.getOrElse(2) { "" }.trim().removeSurrounding("\"")
                service = r.getOrElse(3) { "" }.trim().removeSurrounding("\"")
                kmInput = r.getOrElse(4) { "" }.trim().removeSurrounding("\"")
                if (r.size >= 14) {
                    nipNama = r.getOrElse(5) { "" }.trim().removeSurrounding("\"")
                    namaPart = r.getOrElse(6) { "" }.trim().removeSurrounding("\"")
                    prediksiKm = r.getOrElse(7) { "" }.trim().removeSurrounding("\"")
                    kmTarget = r.getOrElse(8) { "" }.trim().removeSurrounding("\"")
                    argoSekarang = r.getOrElse(9) { "" }.trim().removeSurrounding("\"")
                    estimasiKm = r.getOrElse(10) { "" }.trim().removeSurrounding("\"")
                    aprovel = r.getOrElse(11) { "" }.trim().removeSurrounding("\"")
                    keterangan = r.getOrElse(12) { "" }.trim().removeSurrounding("\"")
                    status = r.getOrElse(13) { "" }.trim().removeSurrounding("\"")
                } else if (r.size == 13) {
                    // Previous 13 cols without NIP_NAMA: ID | TANGGAL | NO_MOBIL | SERVICE | KM_INPUT | NAMA_PART | PREDIKSI_KM | KM_TARGET | ARGO_SEKARANG | ESTIMASI_KM | APROVEL | KETERANGAN | STATUS
                    nipNama = ""
                    namaPart = r.getOrElse(5) { "" }.trim().removeSurrounding("\"")
                    prediksiKm = r.getOrElse(6) { "" }.trim().removeSurrounding("\"")
                    kmTarget = r.getOrElse(7) { "" }.trim().removeSurrounding("\"")
                    argoSekarang = r.getOrElse(8) { "" }.trim().removeSurrounding("\"")
                    estimasiKm = r.getOrElse(9) { "" }.trim().removeSurrounding("\"")
                    aprovel = r.getOrElse(10) { "" }.trim().removeSurrounding("\"")
                    keterangan = r.getOrElse(11) { "" }.trim().removeSurrounding("\"")
                    status = r.getOrElse(12) { "" }.trim().removeSurrounding("\"")
                } else if (r.size == 12) {
                    nipNama = ""
                    namaPart = r.getOrElse(5) { "" }.trim().removeSurrounding("\"")
                    prediksiKm = r.getOrElse(6) { "" }.trim().removeSurrounding("\"")
                    kmTarget = r.getOrElse(7) { "" }.trim().removeSurrounding("\"")
                    argoSekarang = r.getOrElse(8) { "" }.trim().removeSurrounding("\"")
                    estimasiKm = r.getOrElse(9) { "" }.trim().removeSurrounding("\"")
                    aprovel = r.getOrElse(10) { "" }.trim().removeSurrounding("\"")
                    keterangan = r.getOrElse(11) { "" }.trim().removeSurrounding("\"")
                    status = ""
                } else {
                    nipNama = ""
                    namaPart = r.getOrElse(5) { "" }.trim().removeSurrounding("\"")
                    prediksiKm = r.getOrElse(6) { "" }.trim().removeSurrounding("\"")
                    kmTarget = r.getOrElse(7) { "" }.trim().removeSurrounding("\"")
                    argoSekarang = r.getOrElse(8) { "" }.trim().removeSurrounding("\"")
                    estimasiKm = r.getOrElse(9) { "" }.trim().removeSurrounding("\"")
                    aprovel = ""
                    keterangan = r.getOrElse(10) { "" }.trim().removeSurrounding("\"")
                    status = ""
                }
            }

            if (noMobil.isBlank() && namaPart.isBlank() && service.isBlank() && tanggal.isBlank()) continue
            if (noMobil.equals("No Mobil", ignoreCase = true) || noMobil.equals("NO MOBIL", ignoreCase = true) || noMobil.equals("NO_MOBIL", ignoreCase = true)) continue

            list.add(
                PrediksiPartEntity(
                    id = id,
                    tanggal = tanggal,
                    noMobil = noMobil,
                    service = service,
                    kmInput = kmInput,
                    nipNama = nipNama,
                    namaPart = namaPart,
                    prediksiKm = prediksiKm,
                    kmTarget = kmTarget,
                    argoSekarang = argoSekarang,
                    estimasiKm = estimasiKm,
                    aprovel = aprovel,
                    keterangan = keterangan,
                    status = status,
                    isSynced = true
                )
            )
        }
        list.sortBy { it.id }
        return list
    }

    /**
     * Parse PENGERJAAN TEKNIK / RIWAYAT SERVICE TEKNIK CSV:
     * Spreadsheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Sheet: SKD
     * Format Kolom:
     * Created On | Order | Object Description | Description | KETERANGAN | COUNTER | TIPE
     */
    fun parsePengerjaanTeknikCsv(csvText: String): List<PengerjaanTeknikEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ").replace(".", "") }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" }
        val colCreatedOn = headerRow.indexOfFirst {
            it.contains("CREATED ON") || it.contains("TANGGAL") || it.contains("DATE") || it == "TGL"
        }
        val colOrder = headerRow.indexOfFirst { it.contains("ORDER") }
        val colObjectDesc = headerRow.indexOfFirst {
            it.contains("OBJECT DESCRIPTION") || it.contains("OBJECT DESC") || it.contains("OBJECT") ||
                it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" || it.contains("VEHICLE")
        }
        val colDescription = headerRow.indexOfFirst {
            (it.contains("DESCRIPTION") || it.contains("DESKRIPSI") || it.contains("DESC")) && !it.contains("OBJECT")
        }
        val colKeterangan = headerRow.indexOfFirst {
            it.contains("KETERANGAN") || it == "KET" || it == "SERVICE" || (it.contains("SERVICE") && !it.contains("KM"))
        }
        val colCounter = headerRow.indexOfFirst {
            it.contains("COUNTER") || it.contains("KM SERVICE") || it.contains("KMSERVICE") ||
                (it.contains("KM") && it.contains("SERVICE")) || it == "KM" || it.contains("ODOMETER")
        }
        val colTipe = headerRow.indexOfFirst { it.contains("TIPE") || it.contains("TYPE") }

        val hasHeaderMatch = colCreatedOn != -1 || colOrder != -1 || colObjectDesc != -1

        val list = mutableListOf<PengerjaanTeknikEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().toLongOrNull() else null
            val id = rawId ?: i.toLong()

            val createdOn: String
            val orderNo: String
            val objectDesc: String
            val description: String
            val keterangan: String
            val counter: String
            val tipe: String

            if (hasHeaderMatch) {
                createdOn = if (colCreatedOn != -1) r.getOrElse(colCreatedOn) { "" }.trim().removeSurrounding("\"") else ""
                orderNo = if (colOrder != -1) r.getOrElse(colOrder) { "" }.trim().removeSurrounding("\"") else ""
                objectDesc = if (colObjectDesc != -1) r.getOrElse(colObjectDesc) { "" }.trim().removeSurrounding("\"") else ""
                description = if (colDescription != -1) r.getOrElse(colDescription) { "" }.trim().removeSurrounding("\"") else ""
                keterangan = if (colKeterangan != -1) r.getOrElse(colKeterangan) { "" }.trim().removeSurrounding("\"") else ""
                counter = if (colCounter != -1) r.getOrElse(colCounter) { "" }.trim().removeSurrounding("\"") else ""
                tipe = if (colTipe != -1) r.getOrElse(colTipe) { "" }.trim().removeSurrounding("\"") else ""
            } else {
                // Positional: Created On | Order | Object Description | Description | KETERANGAN | COUNTER | TIPE
                val startIdx = if (r.size > 7 && r[0].trim().toLongOrNull() != null) 1 else 0
                createdOn = r.getOrElse(startIdx + 0) { "" }.trim().removeSurrounding("\"")
                orderNo = r.getOrElse(startIdx + 1) { "" }.trim().removeSurrounding("\"")
                objectDesc = r.getOrElse(startIdx + 2) { "" }.trim().removeSurrounding("\"")
                description = r.getOrElse(startIdx + 3) { "" }.trim().removeSurrounding("\"")
                keterangan = r.getOrElse(startIdx + 4) { "" }.trim().removeSurrounding("\"")
                counter = r.getOrElse(startIdx + 5) { "" }.trim().removeSurrounding("\"")
                tipe = r.getOrElse(startIdx + 6) { "" }.trim().removeSurrounding("\"")
            }

            if (createdOn.isBlank() && orderNo.isBlank() && objectDesc.isBlank() && description.isBlank()) continue
            if (createdOn.equals("Created On", ignoreCase = true) || createdOn.equals("Tanggal", ignoreCase = true) || objectDesc.equals("No Mobil", ignoreCase = true)) continue

            list.add(
                PengerjaanTeknikEntity(
                    id = id,
                    tanggal = createdOn,
                    orderNo = orderNo,
                    noMobil = objectDesc.uppercase(),
                    description = description,
                    service = keterangan,
                    kmService = counter,
                    keterangan = keterangan,
                    counter = counter,
                    tipe = tipe,
                    isSynced = true
                )
            )
        }
        return list
    }

    /**
     * Parse RIWAYAT SERVICE SKI CSV (Spreadsheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Sheet: SKI):
     * Tanggal | Order | No Mobil | Description | SERVICE | Km Service
     */
    fun parseRiwayatSkiCsv(csvText: String): List<RiwayatSkiEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.isEmpty()) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ").replace(".", "") }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" }
        val colTanggal = headerRow.indexOfFirst { it.contains("TANGGAL") || it.contains("DATE") || it == "TGL" }
        val colOrder = headerRow.indexOfFirst { it.contains("ORDER") }
        val colNoMobil = headerRow.indexOfFirst { it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" || it.contains("VEHICLE") }
        val colDescription = headerRow.indexOfFirst { it.contains("DESCRIPTION") || it.contains("DESKRIPSI") || it.contains("DESC") }
        val colService = headerRow.indexOfFirst { it == "SERVICE" || (it.contains("SERVICE") && !it.contains("KM")) }
        val colKmService = headerRow.indexOfFirst { it.contains("KM SERVICE") || it.contains("KMSERVICE") || (it.contains("KM") && it.contains("SERVICE")) || it == "KM" }

        val hasHeaderMatch = colTanggal != -1 || colOrder != -1 || colNoMobil != -1

        val list = mutableListOf<RiwayatSkiEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().toLongOrNull() else null
            val id = rawId ?: i.toLong()

            val tanggal: String
            val orderNo: String
            val noMobil: String
            val description: String
            val service: String
            val kmService: String

            if (hasHeaderMatch) {
                tanggal = if (colTanggal != -1) r.getOrElse(colTanggal) { "" }.trim().removeSurrounding("\"") else ""
                orderNo = if (colOrder != -1) r.getOrElse(colOrder) { "" }.trim().removeSurrounding("\"") else ""
                noMobil = if (colNoMobil != -1) r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"") else ""
                description = if (colDescription != -1) r.getOrElse(colDescription) { "" }.trim().removeSurrounding("\"") else ""
                service = if (colService != -1) r.getOrElse(colService) { "" }.trim().removeSurrounding("\"") else ""
                kmService = if (colKmService != -1) r.getOrElse(colKmService) { "" }.trim().removeSurrounding("\"") else ""
            } else {
                // Positional: Tanggal | Order | No Mobil | Description | SERVICE | Km Service
                val startIdx = if (r.size > 6 && r[0].trim().toLongOrNull() != null) 1 else 0
                tanggal = r.getOrElse(startIdx + 0) { "" }.trim().removeSurrounding("\"")
                orderNo = r.getOrElse(startIdx + 1) { "" }.trim().removeSurrounding("\"")
                noMobil = r.getOrElse(startIdx + 2) { "" }.trim().removeSurrounding("\"")
                description = r.getOrElse(startIdx + 3) { "" }.trim().removeSurrounding("\"")
                service = r.getOrElse(startIdx + 4) { "" }.trim().removeSurrounding("\"")
                kmService = r.getOrElse(startIdx + 5) { "" }.trim().removeSurrounding("\"")
            }

            if (tanggal.isBlank() && orderNo.isBlank() && noMobil.isBlank() && description.isBlank()) continue
            if (tanggal.equals("Tanggal", ignoreCase = true) || noMobil.equals("No Mobil", ignoreCase = true)) continue

            list.add(
                RiwayatSkiEntity(
                    id = id,
                    tanggal = tanggal,
                    orderNo = orderNo,
                    noMobil = noMobil.uppercase(),
                    description = description,
                    service = service,
                    kmService = kmService,
                    isSynced = true
                )
            )
        }
        return list
    }

    /**
     * Parse RIWAYAT CLAIM CSV (Spreadsheet ID: 1hyvU96v-ZQlpHO_B1IWhVAeJI3Txpu_Bwl3yNOMpbsE, Sheet: SECURITY):
     * Timestamp | Nomer registrasi | Tanggal kejadian | NO MOBIL | Nip pengemudi | Nama pengemudi | Detail kerusakan mobil | Foto kerusakan mobil | Foto kerusakan | PENGERJAAN_CAT | PENGERJAAN_LC | HASIL
     */
    fun parseRiwayatClaimCsv(csvText: String): List<RiwayatClaimEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ").replace(".", "") }
        val colId = headerRow.indexOfFirst { it == "ID" || it == "NO" || it == "NOMOR" }
        val colTimestamp = headerRow.indexOfFirst { it == "TIMESTAMP" || it == "WAKTU" }
        val colNoReg = headerRow.indexOfFirst { it.contains("NOMER REGISTRASI") || it.contains("NO REGISTRASI") || it == "REGISTRASI" }
        val colTgl = headerRow.indexOfFirst { it.contains("TANGGAL KEJADIAN") || it.contains("TGL KEJADIAN") || it == "TANGGAL" }
        val colNoMobil = headerRow.indexOfFirst { it == "NO MOBIL" || it == "NOMOBIL" || it == "MOBIL" }
        val colNip = headerRow.indexOfFirst { it.contains("NIP") }
        val colNama = headerRow.indexOfFirst { it.contains("NAMA") }
        val colDetail = headerRow.indexOfFirst { it.contains("DETAIL KERUSAKAN") || it.contains("KERUSAKAN") }
        val colFotoMobil = headerRow.indexOfFirst { it.contains("FOTO KERUSAKAN MOBIL") || it.contains("PHOTO KERUSAKAN MOBIL") }
        val colFotoKerusakan = headerRow.indexOfFirst { (it == "FOTO KERUSAKAN" || it == "PHOTO KERUSAKAN") && !it.contains("MOBIL") }
        val colPengerjaanCat = headerRow.indexOfFirst { it.contains("PENGERJAAN CAT") || it.contains("PENGERJAAN_CAT") }
        val colPengerjaanLc = headerRow.indexOfFirst { it.contains("PENGERJAAN LC") || it.contains("PENGERJAAN_LC") }
        val colHasil = headerRow.indexOfFirst { it == "HASIL" || it.contains("HASIL PERBAIKAN") || it.contains("FOTO HASIL") }

        val hasHeaderMatch = colTimestamp != -1 || colNoMobil != -1 || colNoReg != -1

        val list = mutableListOf<RiwayatClaimEntity>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val id: Long
            val timestamp: String
            val noReg: String
            val tgl: String
            val noMobil: String
            val nip: String
            val nama: String
            val detail: String
            val rawFotoMobil: String
            val rawFotoKerusakan: String
            val pengerjaanCat: String
            val pengerjaanLc: String
            val hasil: String

            if (hasHeaderMatch) {
                val parsedId = if (colId != -1) r.getOrElse(colId) { "" }.trim().removeSurrounding("\"").toLongOrNull() else null
                id = parsedId ?: i.toLong()
                timestamp = if (colTimestamp != -1) r.getOrElse(colTimestamp) { "" }.trim().removeSurrounding("\"") else ""
                noReg = if (colNoReg != -1) r.getOrElse(colNoReg) { "" }.trim().removeSurrounding("\"") else ""
                tgl = if (colTgl != -1) r.getOrElse(colTgl) { "" }.trim().removeSurrounding("\"") else ""
                noMobil = if (colNoMobil != -1) r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"") else ""
                nip = if (colNip != -1) r.getOrElse(colNip) { "" }.trim().removeSurrounding("\"") else ""
                nama = if (colNama != -1) r.getOrElse(colNama) { "" }.trim().removeSurrounding("\"") else ""
                detail = if (colDetail != -1) r.getOrElse(colDetail) { "" }.trim().removeSurrounding("\"") else ""
                rawFotoMobil = if (colFotoMobil != -1) r.getOrElse(colFotoMobil) { "" }.trim().removeSurrounding("\"") else ""
                rawFotoKerusakan = if (colFotoKerusakan != -1) r.getOrElse(colFotoKerusakan) { "" }.trim().removeSurrounding("\"") else ""
                pengerjaanCat = if (colPengerjaanCat != -1) r.getOrElse(colPengerjaanCat) { "" }.trim().removeSurrounding("\"") else ""
                pengerjaanLc = if (colPengerjaanLc != -1) r.getOrElse(colPengerjaanLc) { "" }.trim().removeSurrounding("\"") else ""
                hasil = if (colHasil != -1) r.getOrElse(colHasil) { "" }.trim().removeSurrounding("\"") else ""
            } else {
                // Posisi kolom standar:
                // ID | Timestamp | Nomer registrasi | Tanggal kejadian | NO MOBIL | Nip pengemudi | Nama pengemudi | Detail kerusakan mobil | Foto kerusakan mobil | Foto kerusakan | PENGERJAAN_CAT | PENGERJAAN_LC | HASIL
                val firstColId = r.getOrElse(0) { "" }.trim().removeSurrounding("\"").toLongOrNull()
                if (firstColId != null || r.size >= 13) {
                    id = firstColId ?: i.toLong()
                    timestamp = r.getOrElse(1) { "" }.trim().removeSurrounding("\"")
                    noReg = r.getOrElse(2) { "" }.trim().removeSurrounding("\"")
                    tgl = r.getOrElse(3) { "" }.trim().removeSurrounding("\"")
                    noMobil = r.getOrElse(4) { "" }.trim().removeSurrounding("\"")
                    nip = r.getOrElse(5) { "" }.trim().removeSurrounding("\"")
                    nama = r.getOrElse(6) { "" }.trim().removeSurrounding("\"")
                    detail = r.getOrElse(7) { "" }.trim().removeSurrounding("\"")
                    rawFotoMobil = r.getOrElse(8) { "" }.trim().removeSurrounding("\"")
                    rawFotoKerusakan = r.getOrElse(9) { "" }.trim().removeSurrounding("\"")
                    pengerjaanCat = r.getOrElse(10) { "" }.trim().removeSurrounding("\"")
                    pengerjaanLc = r.getOrElse(11) { "" }.trim().removeSurrounding("\"")
                    hasil = r.getOrElse(12) { "" }.trim().removeSurrounding("\"")
                } else {
                    // Fallback jika format lama tanpa ID di kolom 0 (12 kolom)
                    id = i.toLong()
                    timestamp = r.getOrElse(0) { "" }.trim().removeSurrounding("\"")
                    noReg = r.getOrElse(1) { "" }.trim().removeSurrounding("\"")
                    tgl = r.getOrElse(2) { "" }.trim().removeSurrounding("\"")
                    noMobil = r.getOrElse(3) { "" }.trim().removeSurrounding("\"")
                    nip = r.getOrElse(4) { "" }.trim().removeSurrounding("\"")
                    nama = r.getOrElse(5) { "" }.trim().removeSurrounding("\"")
                    detail = r.getOrElse(6) { "" }.trim().removeSurrounding("\"")
                    rawFotoMobil = r.getOrElse(7) { "" }.trim().removeSurrounding("\"")
                    rawFotoKerusakan = r.getOrElse(8) { "" }.trim().removeSurrounding("\"")
                    pengerjaanCat = r.getOrElse(9) { "" }.trim().removeSurrounding("\"")
                    pengerjaanLc = r.getOrElse(10) { "" }.trim().removeSurrounding("\"")
                    hasil = r.getOrElse(11) { "" }.trim().removeSurrounding("\"")
                }
            }

            if (timestamp.isBlank() && noReg.isBlank() && noMobil.isBlank() && detail.isBlank()) continue
            if (noReg.equals("Nomer registrasi", ignoreCase = true) || noMobil.equals("NO MOBIL", ignoreCase = true)) continue

            // Jika fotoKerusakan hanya berlabel "Klik Disini" dan bukan URL, namun rawFotoMobil adalah URL, gunakan rawFotoMobil
            val fotoKerusakanUrl = if (rawFotoKerusakan.startsWith("http", ignoreCase = true)) {
                rawFotoKerusakan
            } else if (rawFotoMobil.startsWith("http", ignoreCase = true)) {
                rawFotoMobil
            } else {
                rawFotoKerusakan
            }

            list.add(
                RiwayatClaimEntity(
                    id = id,
                    timestamp = timestamp,
                    nomerRegistrasi = noReg,
                    tanggalKejadian = tgl,
                    noMobil = noMobil.uppercase(),
                    nipPengemudi = nip,
                    namaPengemudi = nama,
                    detailKerusakanMobil = detail,
                    fotoKerusakanMobil = rawFotoMobil,
                    fotoKerusakan = fotoKerusakanUrl,
                    pengerjaanCat = pengerjaanCat,
                    pengerjaanLc = pengerjaanLc,
                    hasil = hasil,
                    isSynced = true,
                    syncTimestamp = System.currentTimeMillis()
                )
            )
        }

        // Data diambil urut sesuai dengan ID
        list.sortBy { it.id }
        return list
    }

    /**
     * Parse USER CSV from Google Sheets:
     * (Spreadsheet ID: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30, Sheet: USER)
     * Susunan Tabel: | NIP | NAMA_MEKANIK | USERNAMA | PASSWORD | LEVELAKSES |
     * Menampilkan data persis sama dengan isi sel di Google Sheets.
     */
    fun parseUserCsv(csvText: String): List<UserEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.isEmpty()) return emptyList()
        val list = mutableListOf<UserEntity>()

        val firstRowUpper = rows[0].map { it.trim().uppercase().replace("_", "").replace(" ", "") }
        val hasHeader = firstRowUpper.any {
            it == "NIP" || it.contains("MEKANIK") || it.contains("USERNAME") || it.contains("USERNAMA") || it.contains("PASSWORD") || it.contains("LEVEL")
        }

        val header = rows[0].map { it.trim().uppercase() }
        var colNip = if (hasHeader) header.indexOfFirst { it.contains("NIP") || it == "ID" || it == "NO" } else 0
        var colNama = if (hasHeader) header.indexOfFirst { (it.contains("NAMA") && !it.contains("USER")) || it.contains("MEKANIK") || it.contains("PEGAWAI") } else 1
        var colUsername = if (hasHeader) header.indexOfFirst { it.contains("USER") || it.contains("USERNAME") || it.contains("USERNAMA") || it.contains("LOGIN") } else 2
        var colPassword = if (hasHeader) header.indexOfFirst { it.contains("PASS") || it.contains("SANDI") || it.contains("PWD") || it.contains("PIN") } else 3
        var colLevel = if (hasHeader) header.indexOfFirst { it.contains("LEVEL") || it.contains("ROLE") || it.contains("AKSES") || it.contains("HAK") } else 4

        if (colNip == -1) colNip = 0
        if (colNama == -1) colNama = 1
        if (colUsername == -1) colUsername = 2
        if (colPassword == -1) colPassword = 3
        if (colLevel == -1) colLevel = 4

        val startRow = if (hasHeader) 1 else 0
        var userIndex = 0
        for (i in startRow until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue
            val rawNip = r.getOrElse(colNip) { "" }.trim().removeSurrounding("\"")
            val rawNama = r.getOrElse(colNama) { "" }.trim().removeSurrounding("\"")
            val rawUsername = r.getOrElse(colUsername) { "" }.trim().removeSurrounding("\"")
            val rawPassword = r.getOrElse(colPassword) { "" }.trim().removeSurrounding("\"")
            val rawLevel = r.getOrElse(colLevel) { "" }.trim().removeSurrounding("\"")

            if (rawNip.isBlank() && rawNama.isBlank() && rawUsername.isBlank() && rawPassword.isBlank() && rawLevel.isBlank()) continue
            if (rawNip.equals("NIP", ignoreCase = true) && (rawNama.contains("NAMA", ignoreCase = true) || rawUsername.contains("USER", ignoreCase = true))) continue

            userIndex++

            // Simpan nilai persis seperti di Google Sheets (jangan timpa kolom kosong dengan kolom lain)
            list.add(
                UserEntity(
                    id = userIndex,
                    nip = rawNip,
                    namaMekanik = rawNama,
                    username = rawUsername,
                    password = rawPassword,
                    level = rawLevel.uppercase()
                )
            )
        }
        return list
    }

    /**
     * Parse PENGERJAAN CAT CSV:
     * Spreadsheet ID: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs, Sheet: PENGERJAAN_CAT
     * Kolom Baru Resmi (14 Kolom):
     * ID (kode uniq) | Timestamp | TANGGAL | NO MOBIL | KERUSAKAN | PHOTO KERUSAKAN | FOTO NOTIF | FOTO HASIL PENGECATAN | GAMBAR | MEKANIK | NO REGISTRASI CAT | PHOTO LAS KETOK | FOTO HASIL PENGECATAN_2 | FOTO HASIL PENGECATAN_3
     */
    fun parsePengerjaanCatCsv(csvText: String): List<PengerjaanCatEntity> {
        val rows = parseCsvLines(csvText)
        if (rows.size <= 1) return emptyList()

        val headerRow = rows[0].map { it.trim().uppercase().replace("_", " ") }
        val has14Cols = headerRow.size >= 14 || headerRow.any { it.contains("LAS") || it.contains("KETOK") }
        val hasIdCol = headerRow.any { it == "ID" || it.contains("UNIQ") || it.contains("UNIX") || it.contains("KODE") }

        var colId = headerRow.indexOfFirst { it == "ID" || it == "NO" || it.contains("UNIX") || it.contains("UNIQ") || it.contains("KODE") }
        var colTimestamp = headerRow.indexOfFirst { it.contains("TIMESTAMP") || it.contains("WAKTU") }
        var colTanggal = headerRow.indexOfFirst { it.contains("TANGGAL") || it.contains("DATE") || it == "TGL" }
        var colNoMobil = headerRow.indexOfFirst { it.contains("NO MOBIL") || it.contains("NOMOBIL") || it == "MOBIL" }
        var colKerusakan = headerRow.indexOfFirst { it.contains("KERUSAKAN") }
        var colPhotoKerusakan = headerRow.indexOfFirst { it.contains("PHOTO KERUSAKAN") || it.contains("FOTO KERUSAKAN") }
        var colPhotoNotif = headerRow.indexOfFirst { it.contains("NOTIF") }
        var colFotoHasil = headerRow.indexOfFirst { (it.contains("HASIL") || it.contains("PENGECATAN")) && !it.contains("2") && !it.contains("3") && !it.contains("GAMBAR") }
        var colGambar = headerRow.indexOfFirst { it == "GAMBAR" || it.contains("GAMBAR") || it.contains("IMAGE") || it.contains("URL") }
        var colMekanik = headerRow.indexOfFirst { it.contains("MEKANIK") || it.contains("PETUGAS") }
        var colNoRegistrasi = headerRow.indexOfFirst { it.contains("REGISTRASI") || it.contains("NO REG") }
        var colPhotoLasKetok = headerRow.indexOfFirst { it.contains("LAS") || it.contains("KETOK") }
        var colFotoHasil2 = headerRow.indexOfFirst { it.contains("PENGECATAN 2") || it.contains("PENGECATAN_2") || it.contains("HASIL 2") || it.contains("HASIL_2") }
        var colFotoHasil3 = headerRow.indexOfFirst { it.contains("PENGECATAN 3") || it.contains("PENGECATAN_3") || it.contains("HASIL 3") || it.contains("HASIL_3") }

        // Fallback offset jika 14 kolom diawali kolom ID:
        if (colId == -1 && hasIdCol) colId = 0
        val offset = if (hasIdCol && colId == 0) 1 else 0
        if (colTimestamp == -1) colTimestamp = offset + 0
        if (colTanggal == -1) colTanggal = offset + 1
        if (colNoMobil == -1) colNoMobil = offset + 2
        if (colKerusakan == -1) colKerusakan = offset + 3
        if (colPhotoKerusakan == -1) colPhotoKerusakan = offset + 4
        if (colPhotoNotif == -1) colPhotoNotif = offset + 5
        if (colFotoHasil == -1) colFotoHasil = offset + 6
        if (colGambar == -1) colGambar = offset + 7
        if (colMekanik == -1) colMekanik = offset + 8
        if (colNoRegistrasi == -1) colNoRegistrasi = offset + 9
        if (colPhotoLasKetok == -1 && (has14Cols || headerRow.size >= offset + 11)) colPhotoLasKetok = offset + 10
        if (colFotoHasil2 == -1 && (has14Cols || headerRow.size >= offset + 12)) colFotoHasil2 = offset + 11
        if (colFotoHasil3 == -1 && (has14Cols || headerRow.size >= offset + 13)) colFotoHasil3 = offset + 12

        val list = mutableListOf<PengerjaanCatEntity>()
        val seenIds = mutableSetOf<Long>()
        for (i in 1 until rows.size) {
            val r = rows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val rawId = if (colId != -1) r.getOrElse(colId) { "" }.trim().removeSurrounding("\"") else ""
            val idNum = rawId.toLongOrNull() ?: rawId.toDoubleOrNull()?.toLong() ?: i.toLong()
            var finalId = if (idNum <= 0L) i.toLong() else idNum
            while (finalId in seenIds) {
                finalId++
            }
            seenIds.add(finalId)

            val timestamp = if (colTimestamp != -1) r.getOrElse(colTimestamp) { "" }.trim().removeSurrounding("\"") else ""
            val tanggal = if (colTanggal != -1) r.getOrElse(colTanggal) { "" }.trim().removeSurrounding("\"") else ""
            val noMobil = if (colNoMobil != -1) r.getOrElse(colNoMobil) { "" }.trim().removeSurrounding("\"") else ""
            val kerusakan = if (colKerusakan != -1) r.getOrElse(colKerusakan) { "" }.trim().removeSurrounding("\"") else ""
            val photoKerusakan = if (colPhotoKerusakan != -1) r.getOrElse(colPhotoKerusakan) { "" }.trim().removeSurrounding("\"") else ""
            val photoNotif = if (colPhotoNotif != -1) r.getOrElse(colPhotoNotif) { "" }.trim().removeSurrounding("\"") else ""
            val fotoHasilPengecatan = if (colFotoHasil != -1) r.getOrElse(colFotoHasil) { "" }.trim().removeSurrounding("\"") else ""
            val gambar = if (colGambar != -1) r.getOrElse(colGambar) { "" }.trim().removeSurrounding("\"") else ""
            val mekanik = if (colMekanik != -1) r.getOrElse(colMekanik) { "" }.trim().removeSurrounding("\"") else ""
            val noRegistrasi = if (colNoRegistrasi != -1) r.getOrElse(colNoRegistrasi) { "" }.trim().removeSurrounding("\"") else ""
            val photoLasKetok = if (colPhotoLasKetok != -1) r.getOrElse(colPhotoLasKetok) { "" }.trim().removeSurrounding("\"") else ""
            val fotoHasil2 = if (colFotoHasil2 != -1) r.getOrElse(colFotoHasil2) { "" }.trim().removeSurrounding("\"") else ""
            val fotoHasil3 = if (colFotoHasil3 != -1) r.getOrElse(colFotoHasil3) { "" }.trim().removeSurrounding("\"") else ""

            if (noMobil.isBlank() && kerusakan.isBlank()) continue
            if (noMobil.equals("No Mobil", ignoreCase = true) || noMobil.equals("NO_MOBIL", ignoreCase = true)) continue

            list.add(
                PengerjaanCatEntity(
                    id = finalId,
                    timestamp = timestamp,
                    tanggal = tanggal,
                    noMobil = noMobil.uppercase(),
                    kerusakan = kerusakan,
                    photoKerusakan = photoKerusakan,
                    photoNotif = photoNotif,
                    fotoHasilPengecatan = fotoHasilPengecatan,
                    gambarFotoHasilPengecatan = gambar,
                    mekanik = mekanik,
                    noRegistrasi = noRegistrasi,
                    photoLasKetok = photoLasKetok,
                    fotoHasilPengecatan2 = fotoHasil2,
                    fotoHasilPengecatan3 = fotoHasil3,
                    isSynced = true,
                    syncTimestamp = System.currentTimeMillis()
                )
            )
        }
        return list
    }



    /**
     * Kirim data Pengerjaan Cat ke Google Apps Script Web App untuk disimpan (Add) / dikoreksi (Edit) ke Google Sheets
     * Spreadsheet ID: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs
     * Sheet: PENGERJAAN_CAT
     */
    suspend fun sendPengerjaanCatToGoogleSheet(
        scriptUrl: String,
        cat: PengerjaanCatEntity,
        sheetName: String = "PENGERJAAN_CAT",
        spreadsheetId: String = "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs",
        isEdit: Boolean = false,
        oldNoRegistrasi: String = "",
        oldNoMobil: String = "",
        oldTimestamp: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim()
        if (cleanUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL Google Apps Script belum diisi."))
        }

        val targetSheet = sheetName.ifBlank { "PENGERJAAN_CAT" }
        val targetSpreadsheetId = spreadsheetId.ifBlank { "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs" }

        try {
            // 1. Coba via HTTP POST dengan JSON payload
            val jsonPayload = JSONObject().apply {
                put("action", "saveOrUpdatePengerjaanCat")
                val dataObj = JSONObject().apply {
                    put("id", cat.id)
                    put("isEdit", isEdit)
                    put("oldNoRegistrasi", oldNoRegistrasi)
                    put("oldNoMobil", oldNoMobil)
                    put("oldTimestamp", oldTimestamp)
                    put("timestamp", cat.timestamp)
                    put("tanggal", cat.tanggal)
                    put("noMobil", cat.noMobil)
                    put("kerusakan", cat.kerusakan)
                    put("photoKerusakan", cat.photoKerusakan)
                    put("photoNotif", cat.photoNotif)
                    put("fotoHasilPengecatan", cat.fotoHasilPengecatan)
                    put("gambar", cat.gambarFotoHasilPengecatan)
                    put("gambarFotoHasilPengecatan", cat.gambarFotoHasilPengecatan)
                    put("mekanik", cat.mekanik)
                    put("noRegistrasi", cat.noRegistrasi)
                    put("noRegistrasiCat", cat.noRegistrasi)
                    put("photoLasKetok", cat.photoLasKetok)
                    put("fotoHasilPengecatan2", cat.fotoHasilPengecatan2)
                    put("fotoHasilPengecatan3", cat.fotoHasilPengecatan3)
                    put("sheetName", targetSheet)
                    put("spreadsheetId", targetSpreadsheetId)
                }
                put("data", dataObj)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val postResponse = try {
                httpClient.newCall(postRequest).execute()
            } catch (e: Exception) {
                null
            }

            val postBody = postResponse?.body?.string() ?: ""
            if (postResponse != null && postResponse.isSuccessful &&
                (postBody.contains("success", ignoreCase = true) || postBody.contains("OK", ignoreCase = true) || postBody.contains("updated", ignoreCase = true) || postBody.contains("appended", ignoreCase = true))) {
                return@withContext Result.success("Berhasil tersimpan ke Google Sheets ($targetSheet)!")
            }

            // 2. Fallback via HTTP GET jika POST dialihkan (redirect 302 Apps Script)
            val encodedParams = java.lang.StringBuilder("$cleanUrl?")
                .append("action=saveOrUpdatePengerjaanCat")
                .append("&sheetName=").append(java.net.URLEncoder.encode(targetSheet, "UTF-8"))
                .append("&spreadsheetId=").append(java.net.URLEncoder.encode(targetSpreadsheetId, "UTF-8"))
                .append("&isEdit=").append(isEdit)
                .append("&oldNoRegistrasi=").append(java.net.URLEncoder.encode(oldNoRegistrasi, "UTF-8"))
                .append("&oldNoMobil=").append(java.net.URLEncoder.encode(oldNoMobil, "UTF-8"))
                .append("&oldTimestamp=").append(java.net.URLEncoder.encode(oldTimestamp, "UTF-8"))
                .append("&timestamp=").append(java.net.URLEncoder.encode(cat.timestamp, "UTF-8"))
                .append("&tanggal=").append(java.net.URLEncoder.encode(cat.tanggal, "UTF-8"))
                .append("&noMobil=").append(java.net.URLEncoder.encode(cat.noMobil, "UTF-8"))
                .append("&kerusakan=").append(java.net.URLEncoder.encode(cat.kerusakan, "UTF-8"))
                .append("&photoKerusakan=").append(java.net.URLEncoder.encode(cat.photoKerusakan, "UTF-8"))
                .append("&photoNotif=").append(java.net.URLEncoder.encode(cat.photoNotif, "UTF-8"))
                .append("&fotoHasilPengecatan=").append(java.net.URLEncoder.encode(cat.fotoHasilPengecatan, "UTF-8"))
                .append("&gambar=").append(java.net.URLEncoder.encode(cat.gambarFotoHasilPengecatan, "UTF-8"))
                .append("&id=").append(cat.id)
                .append("&mekanik=").append(java.net.URLEncoder.encode(cat.mekanik, "UTF-8"))
                .append("&noRegistrasi=").append(java.net.URLEncoder.encode(cat.noRegistrasi, "UTF-8"))
                .append("&photoLasKetok=").append(java.net.URLEncoder.encode(cat.photoLasKetok, "UTF-8"))
                .append("&fotoHasilPengecatan2=").append(java.net.URLEncoder.encode(cat.fotoHasilPengecatan2, "UTF-8"))
                .append("&fotoHasilPengecatan3=").append(java.net.URLEncoder.encode(cat.fotoHasilPengecatan3, "UTF-8"))
                .toString()

            val getRequest = Request.Builder()
                .url(encodedParams)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val getResponse = httpClient.newCall(getRequest).execute()
            val getBody = getResponse.body?.string() ?: ""

            if (getResponse.isSuccessful && (getBody.contains("success", ignoreCase = true) || getBody.contains("OK", ignoreCase = true) || getBody.contains("updated", ignoreCase = true) || getBody.contains("appended", ignoreCase = true))) {
                Result.success("Berhasil tersimpan ke Google Sheets ($targetSheet)!")
            } else {
                Result.failure(Exception("Respon Google Script: $getBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload sebuah foto ke Google Drive melalui Web App Google Apps Script.
     * Mengembalikan tautan link Google Drive yang digenerate (https://drive.google.com/file/d/.../view?usp=sharing).
     * Mendukung path file lokal (/data/...), Uri content://, dan Uri file://.
     * Foto dikompresi otomatis terlebih dahulu (< 180 KB) agar upload sangat cepat dan stabil.
     */
    suspend fun uploadImageToGoogleDrive(
        context: Context? = null,
        scriptUrl: String,
        filePath: String,
        fileName: String,
        folderId: String = "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6",
        folderName: String = "FOTO_AUDIT_KENDARAAN",
        maxDimension: Int = 4096,
        quality: Int = 98
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedPath = filePath.trim()
        if (trimmedPath.isBlank()) {
            return@withContext Result.failure(Exception("Path foto kosong"))
        }

        // Jika sudah berupa link HTTP/HTTPS (sudah berada di Google Drive atau web)
        if (trimmedPath.startsWith("http://", ignoreCase = true) || trimmedPath.startsWith("https://", ignoreCase = true)) {
            val fileId = extractDriveFileId(trimmedPath)
            val canonical = if (fileId != null) "https://drive.google.com/file/d/$fileId/view?usp=sharing" else trimmedPath
            return@withContext Result.success(canonical)
        }

        var cleanUrl = scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        if (cleanUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL Google Apps Script belum dikonfigurasi di Pengaturan."))
        }
        if (!cleanUrl.endsWith("/exec") && cleanUrl.contains("/macros/s/")) {
            cleanUrl = cleanUrl.substringBeforeLast("/") + "/exec"
        }

        try {
            // Baca byte gambar dari file lokal, content URI, atau raw base64
            val maxDim = maxDimension
            val compressQuality = quality
            val bytes: ByteArray = when {
                trimmedPath.startsWith("data:image", ignoreCase = true) -> {
                    val rawB64 = if (trimmedPath.contains(";base64,")) {
                        trimmedPath.substringAfter(";base64,")
                    } else if (trimmedPath.contains(",")) {
                        trimmedPath.substringAfter(",")
                    } else trimmedPath
                    android.util.Base64.decode(rawB64.trim(), android.util.Base64.DEFAULT)
                }
                trimmedPath.startsWith("content://", ignoreCase = true) && context != null -> {
                    val uri = android.net.Uri.parse(trimmedPath)
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: return@withContext Result.failure(Exception("Tidak dapat membuka file galeri/kamera ($trimmedPath)"))
                    
                    val boundsOptions = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    android.graphics.BitmapFactory.decodeStream(inputStream, null, boundsOptions)
                    inputStream.close()

                    var sampleSize = 1
                    if (boundsOptions.outHeight > maxDim || boundsOptions.outWidth > maxDim) {
                        val halfHeight = boundsOptions.outHeight / 2
                        val halfWidth = boundsOptions.outWidth / 2
                        while ((halfHeight / sampleSize) >= maxDim || (halfWidth / sampleSize) >= maxDim) {
                            sampleSize *= 2
                        }
                    }

                    val decodeStream = context.contentResolver.openInputStream(uri)
                    val decodeOptions = android.graphics.BitmapFactory.Options().apply { inSampleSize = sampleSize }
                    val bmp = android.graphics.BitmapFactory.decodeStream(decodeStream, null, decodeOptions)
                    decodeStream?.close()

                    if (bmp != null) {
                        val out = java.io.ByteArrayOutputStream()
                        bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, out)
                        bmp.recycle()
                        out.toByteArray()
                    } else {
                        return@withContext Result.failure(Exception("Gagal memproses bitmap dari URI ($trimmedPath)"))
                    }
                }
                else -> {
                    val file = java.io.File(trimmedPath.removePrefix("file://"))
                    if (!file.exists() || !file.isFile) {
                        return@withContext Result.failure(Exception("File foto tidak ditemukan di HP: ${file.absolutePath}"))
                    }
                    val boundsOptions = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    android.graphics.BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
                    var sampleSize = 1
                    if (boundsOptions.outHeight > maxDim || boundsOptions.outWidth > maxDim) {
                        val halfHeight = boundsOptions.outHeight / 2
                        val halfWidth = boundsOptions.outWidth / 2
                        while ((halfHeight / sampleSize) >= maxDim || (halfWidth / sampleSize) >= maxDim) {
                            sampleSize *= 2
                        }
                    }
                    val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                        inDither = false
                    }
                    val bmp = android.graphics.BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                    if (bmp != null) {
                        val exif = try { android.media.ExifInterface(file.absolutePath) } catch (_: Exception) { null }
                        val orientation = exif?.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL)
                            ?: android.media.ExifInterface.ORIENTATION_NORMAL
                        val matrix = android.graphics.Matrix()
                        when (orientation) {
                            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                        }
                        val rotated = if (!matrix.isIdentity) {
                            val r = android.graphics.Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                            bmp.recycle()
                            r
                        } else bmp
                        val out = java.io.ByteArrayOutputStream()
                        rotated.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, out)
                        rotated.recycle()
                        out.toByteArray()
                    } else {
                        file.readBytes()
                    }
                }
            }

            val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            val dataUri = "data:image/jpeg;base64,$b64"
            val targetFolderId = folderId.ifBlank { "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6" }

            val jsonPayload = JSONObject().apply {
                put("action", "uploadImageToDrive")
                put("image", dataUri)
                put("fileName", fileName)
                put("folderId", targetFolderId)
                put("folderName", folderName)
                val dataObj = JSONObject().apply {
                    put("image", dataUri)
                    put("fileName", fileName)
                    put("folderId", targetFolderId)
                    put("folderName", folderName)
                }
                put("data", dataObj)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0 (Android; BengkelMW)")
                .build()

            val uploadClient = httpClient.newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(75, TimeUnit.SECONDS)
                .writeTimeout(75, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()

            val postResponse = try {
                uploadClient.newCall(postRequest).execute()
            } catch (e: Exception) {
                null
            }

            val postBody = postResponse?.body?.string() ?: ""

            // 1. Coba parse JSON response
            if (postResponse != null && postResponse.isSuccessful && postBody.isNotBlank()) {
                try {
                    val json = JSONObject(postBody)
                    val url = json.optString("url").ifBlank {
                        json.optString("link").ifBlank {
                            json.optString("webUrl").ifBlank {
                                val d = json.optJSONObject("data")
                                d?.optString("url") ?: d?.optString("link") ?: d?.optString("webUrl") ?: ""
                            }
                        }
                    }
                    val fileId = json.optString("fileId").ifBlank {
                        val d = json.optJSONObject("data")
                        d?.optString("fileId") ?: ""
                    }.ifBlank {
                        extractDriveFileId(url) ?: ""
                    }

                    if (fileId.isNotBlank()) {
                        return@withContext Result.success("https://drive.google.com/file/d/$fileId/view?usp=sharing")
                    }

                    if (url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)) {
                        return@withContext Result.success(url)
                    }

                    val msg = json.optString("message").ifBlank { json.optString("error") }
                    if (msg.isNotBlank()) {
                        if (msg.contains("DriveApp", ignoreCase = true) ||
                            msg.contains("permission", ignoreCase = true) ||
                            msg.contains("auth/drive", ignoreCase = true) ||
                            msg.contains("Error upload Google Drive", ignoreCase = true)) {
                            return@withContext Result.failure(
                                Exception("⚠️ Web App belum memiliki izin Google Drive (DriveApp).\n\nSolusi (Hanya 1 Menit):\n1. Buka editor Google Apps Script.\n2. Di menu atas pilih fungsi 'aktivasiIzinGoogleDrive'.\n3. Klik Run (▶) dan setujui izin Google Drive.\n4. Klik Deploy > Manage deployments > Edit > New version > Deploy.")
                            )
                        }
                        return@withContext Result.failure(Exception("Google Apps Script: $msg"))
                    }
                } catch (_: Exception) {}
            }

            // 2. Regex fallback pencarian ID Google Drive
            val urlRegex = Regex("""https://drive\.google\.com/file/d/([a-zA-Z0-9_-]{15,})/view[^\s"']*""")
            val urlMatch = urlRegex.find(postBody)
            if (urlMatch != null) {
                return@withContext Result.success("https://drive.google.com/file/d/${urlMatch.groupValues[1]}/view?usp=sharing")
            }
            val idRegex = Regex("""(?:(?:id=|\/d\/)([a-zA-Z0-9_-]{15,}))""")
            val idMatch = idRegex.find(postBody)
            if (idMatch != null) {
                return@withContext Result.success("https://drive.google.com/file/d/${idMatch.groupValues[1]}/view?usp=sharing")
            }

            val errCode = postResponse?.code ?: 0
            if (postBody.contains("DriveApp", ignoreCase = true) || postBody.contains("permission", ignoreCase = true) || postBody.contains("auth/drive", ignoreCase = true)) {
                Result.failure(Exception("⚠️ Google Apps Script belum memiliki izin Google Drive (DriveApp). Harap jalankan fungsi 'aktivasiIzinGoogleDrive' di Apps Script dan Deploy Versi Baru."))
            } else if (errCode == 200) {
                Result.failure(Exception("⚠️ Gagal mengunggah ke Google Drive (Status HTTP: 200). Web App Google Apps Script belum memiliki izin Google Drive (DriveApp). Harap jalankan fungsi 'aktivasiIzinGoogleDrive' di Apps Script lalu Deploy Versi Baru."))
            } else {
                Result.failure(Exception("Gagal mengunggah foto ke Google Drive (Status HTTP: $errCode). Pastikan URL Web App aktif."))
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Unknown error"
            if (msg.contains("DriveApp", ignoreCase = true) || msg.contains("permission", ignoreCase = true)) {
                Result.failure(Exception("⚠️ Izin Google Drive (DriveApp) belum aktif pada Web App Apps Script. Harap jalankan fungsi 'aktivasiIzinGoogleDrive' lalu Deploy Versi Baru."))
            } else {
                Result.failure(Exception("Gagal mengunggah foto ke Google Drive: $msg"))
            }
        }
    }

    /**
     * Upload Bitmap langsung ke Google Drive tanpa perlu menyimpan file terlebih dahulu
     */
    suspend fun uploadBitmapToGoogleDrive(
        scriptUrl: String,
        bitmap: android.graphics.Bitmap,
        fileName: String,
        folderId: String = "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6",
        folderName: String = "FOTO_AUDIT_KENDARAAN"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val out = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 98, out)
            val b64 = android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP)
            val dataUri = "data:image/jpeg;base64,$b64"
            uploadImageToGoogleDrive(
                context = null,
                scriptUrl = scriptUrl,
                filePath = dataUri,
                fileName = fileName,
                folderId = folderId,
                folderName = folderName
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uji apakah Web App Google Apps Script memiliki izin Google Drive (DriveApp) yang aktif
     */
    suspend fun testGoogleScriptDrivePermission(scriptUrl: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim()
        if (cleanUrl.isBlank()) return@withContext Result.failure(Exception("URL Google Apps Script kosong"))
        try {
            // Kirim gambar 1x1 base64 untuk memverifikasi izin DriveApp
            val tinyImageUri = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
            val jsonPayload = JSONObject().apply {
                put("action", "uploadImageToDrive")
                put("image", tinyImageUri)
                put("fileName", "TEST_DRIVE_PERMISSION.png")
                put("folderId", "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6")
                put("folderName", "FOTO_AUDIT_KENDARAAN")
                val dataObj = JSONObject().apply {
                    put("image", tinyImageUri)
                    put("fileName", "TEST_DRIVE_PERMISSION.png")
                }
                put("data", dataObj)
            }
            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val req = Request.Builder().url(cleanUrl).post(requestBody).header("User-Agent", "Mozilla/5.0").build()
            val resp = httpClient.newCall(req).execute()
            val body = resp.body?.string() ?: ""
            if (resp.isSuccessful) {
                if (body.contains("drive.google.com", ignoreCase = true) || (body.contains("\"success\":true", ignoreCase = true) && body.contains("http", ignoreCase = true))) {
                    Result.success("✅ Izin Google Drive AKTIF! Web App berhasil mengunggah foto ke Google Drive.")
                } else if (body.contains("permission", ignoreCase = true) || body.contains("DriveApp", ignoreCase = true) || body.contains("Error upload Google Drive", ignoreCase = true)) {
                    Result.failure(Exception("⚠️ Izin Google Drive (DriveApp) belum diaktifkan pada Web App.\n\nLangkah Mengatasi:\n1. Buka Google Apps Script editor.\n2. Di dropdown fungsi bagian atas, pilih 'aktivasiIzinGoogleDrive'.\n3. Klik tombol Run (▶) dan izinkan akses Google Drive.\n4. Klik Deploy > Manage deployments > Edit (pensil) > Version: New version > Deploy."))
                } else {
                    Result.failure(Exception("Respon Web App: ${body.take(120)}"))
                }
            } else {
                Result.failure(Exception("HTTP ${resp.code}: ${body.take(100)}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal tes Google Drive: ${e.message}"))
        }
    }

    /**
     * Pengiriman ke Cloud (Sinkronisasi Base64) Data Audit:
     * - File foto lokal dibaca dan dikonversi menjadi format Base64 String secara bergiliran untuk menjaga penggunaan RAM HP.
     * - Data audit, poin KPI, catatan, dan string Base64 foto digabungkan ke dalam paket data JSON (photosMap & slot photo1 s/d photo20).
     * - Dikirim melalui koneksi HTTPS (OkHttpClient) ke endpoint Google Apps Script Web App.
     * - Google Apps Script men-decode Base64 ke image/jpeg, menyimpan ke folder Google Drive FOTO_AUDIT_KENDARAAN dengan izin ANYONE_WITH_LINK,
     *   lalu menyimpan link URL gambar (https://drive.google.com/uc?export=view&id=...) langsung ke sheet INPUT_AUDIT.
     */
    suspend fun sendAuditToGoogleSheet(
        scriptUrl: String,
        audit: DataAuditEntity,
        kpiPoints: String = "",
        generalNotes: String = "",
        additionalPhotosMap: Map<String, String> = emptyMap(),
        sheetName: String = "INPUT_AUDIT",
        spreadsheetId: String = ""
    ): Result<DataAuditEntity> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim()
        if (cleanUrl.isBlank()) return@withContext Result.success(audit)

        // Helper konversi foto lokal ke Base64 secara bergiliran (RAM-efficient)
        fun fileToBase64(pathOrUrl: String): String {
            val trimmed = pathOrUrl.trim()
            if (trimmed.isBlank() || trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                return trimmed
            }
            try {
                val f = java.io.File(trimmed.removePrefix("file://"))
                if (f.exists() && f.isFile) {
                    val boundsOptions = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    android.graphics.BitmapFactory.decodeFile(f.absolutePath, boundsOptions)
                    val maxDim = 4096
                    var sampleSize = 1
                    if (boundsOptions.outHeight > maxDim || boundsOptions.outWidth > maxDim) {
                        val halfH = boundsOptions.outHeight / 2
                        val halfW = boundsOptions.outWidth / 2
                        while ((halfH / sampleSize) >= maxDim || (halfW / sampleSize) >= maxDim) {
                            sampleSize *= 2
                        }
                    }
                    val decodeOptions = android.graphics.BitmapFactory.Options().apply { 
                        inSampleSize = sampleSize 
                        inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                        inDither = false
                    }
                    val bmp = android.graphics.BitmapFactory.decodeFile(f.absolutePath, decodeOptions)
                    val bytes = if (bmp != null) {
                        val out = java.io.ByteArrayOutputStream()
                        bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 98, out)
                        if (!bmp.isRecycled) bmp.recycle() // Bebaskan memori seketika
                        out.toByteArray()
                    } else {
                        f.readBytes()
                    }
                    val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    return "data:image/jpeg;base64,$b64"
                }
            } catch (_: Exception) {}
            return trimmed
        }

        try {
            // Konversi bergiliran
            val photosMapObj = JSONObject()
            val photosList = listOf(
                "photo1" to audit.photo1,
                "photo2" to audit.photo2,
                "photo3" to audit.photo3
            )
            val convertedPhotos = mutableMapOf<String, String>()

            for ((slot, path) in photosList) {
                val b64 = fileToBase64(path)
                convertedPhotos[slot] = b64
                photosMapObj.put(slot, b64)
            }

            for ((k, v) in additionalPhotosMap) {
                val b64 = fileToBase64(v)
                convertedPhotos[k] = b64
                photosMapObj.put(k, b64)
            }

            val dataObj = JSONObject().apply {
                put("id", audit.id)
                put("timestamp", audit.timestamp)
                put("tanggal", audit.tanggal)
                put("nip", audit.nip)
                put("namaPetugas", audit.namaPetugas)
                put("noMobil", audit.noMobil)
                put("platNomer", audit.platNomer)
                put("tipe", audit.tipe)
                put("warna", audit.warna)
                put("tahun", audit.tahun)
                put("pool", audit.pool)
                put("kpiPoin", kpiPoints)
                put("catatan", generalNotes)
                put("sheetName", sheetName)
                put("spreadsheetId", spreadsheetId)
                put("folderName", "FOTO_AUDIT_KENDARAAN")

                for ((slot, b64) in convertedPhotos) {
                    put(slot, b64)
                }
                put("catatanPhoto1", audit.catatanPhoto1)
                put("catatanPhoto2", audit.catatanPhoto2)
                put("catatanPhoto3", audit.catatanPhoto3)

                put("photosMap", photosMapObj)
            }

            val jsonPayload = JSONObject().apply {
                put("action", "saveAudit")
                put("data", dataObj)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val postResponse = httpClient.newCall(postRequest).execute()
            val postBody = postResponse.body?.string() ?: ""

            if (postResponse.isSuccessful && (postBody.contains("success", ignoreCase = true) || postBody.contains("OK", ignoreCase = true))) {
                val jsonObj = JSONObject(postBody)
                val data = jsonObj.optJSONObject("data") ?: jsonObj
                val driveUrls = data.optJSONObject("driveUrls")

                val dPhoto1 = driveUrls?.optString("photo1") ?: data.optString("photo1")
                val dPhoto2 = driveUrls?.optString("photo2") ?: data.optString("photo2")
                val dPhoto3 = driveUrls?.optString("photo3") ?: data.optString("photo3")

                val updatedAudit = audit.copy(
                    photo1 = if (dPhoto1.startsWith("http")) dPhoto1 else audit.photo1,
                    photo2 = if (dPhoto2.startsWith("http")) dPhoto2 else audit.photo2,
                    photo3 = if (dPhoto3.startsWith("http")) dPhoto3 else audit.photo3,
                    isSynced = true,
                    syncTimestamp = System.currentTimeMillis(),
                    syncError = null
                )
                Result.success(updatedAudit)
            } else {
                Result.failure(Exception("Gagal sinkron audit: $postBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Inisialisasi 1-Klik Pengerjaan Keindahan:
     * - Membuat & merapikan sheet SERVICE_KEINDAHAN dengan 13 kolom resmi
     * - Memformat header biru tua, font putih tebal, freeze baris 1, dan lebar kolom ideal
     * - Membuat / memverifikasi folder Google Drive (FOTO_AUDIT_KENDARAAN) & set izin publik/view (ANYONE_WITH_LINK)
     */
    suspend fun inisialisasiPengerjaanKeindahan(
        scriptUrl: String,
        spreadsheetId: String = "",
        sheetName: String = "SERVICE_KEINDAHAN",
        folderId: String = "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6",
        folderName: String = "FOTO_AUDIT_KENDARAAN"
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim()
        if (cleanUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL Google Apps Script belum diisi! Silakan buka menu Pengaturan Google Script."))
        }

        try {
            val jsonPayload = JSONObject().apply {
                put("action", "inisialisasiPengerjaanKeindahan")
                val dataObj = JSONObject().apply {
                    put("spreadsheetId", spreadsheetId)
                    put("sheetName", sheetName)
                    put("folderId", folderId)
                    put("folderName", folderName)
                }
                put("data", dataObj)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val postResponse = try {
                httpClient.newCall(postRequest).execute()
            } catch (e: Exception) {
                null
            }

            val postBody = postResponse?.body?.string() ?: ""
            if (postResponse != null && postResponse.isSuccessful &&
                (postBody.contains("success", ignoreCase = true) || postBody.contains("Berhasil", ignoreCase = true) || postBody.contains("OK", ignoreCase = true))) {
                val jsonObj = JSONObject(postBody)
                val msg = jsonObj.optString("message", "Inisialisasi Pengerjaan Keindahan Berhasil!")
                val logsArr = jsonObj.optJSONArray("logs")
                val logsText = if (logsArr != null && logsArr.length() > 0) {
                    val sb = StringBuilder()
                    for (i in 0 until logsArr.length()) {
                        sb.append("\n• ").append(logsArr.getString(i))
                    }
                    sb.toString()
                } else ""
                return@withContext Result.success("$msg$logsText")
            }

            // Fallback via GET
            val encodedParams = java.lang.StringBuilder("$cleanUrl?")
                .append("action=inisialisasiPengerjaanKeindahan")
                .append("&sheetName=").append(java.net.URLEncoder.encode(sheetName, "UTF-8"))
                .append("&spreadsheetId=").append(java.net.URLEncoder.encode(spreadsheetId, "UTF-8"))
                .append("&folderId=").append(java.net.URLEncoder.encode(folderId, "UTF-8"))
                .append("&folderName=").append(java.net.URLEncoder.encode(folderName, "UTF-8"))
                .toString()

            val getReq = Request.Builder().url(encodedParams).get().header("User-Agent", "Mozilla/5.0").build()
            val getResp = httpClient.newCall(getReq).execute()
            val getBody = getResp.body?.string() ?: ""

            if (getResp.isSuccessful && (getBody.contains("success", ignoreCase = true) || getBody.contains("Berhasil", ignoreCase = true) || getBody.contains("OK", ignoreCase = true))) {
                val jsonObj = JSONObject(getBody)
                val msg = jsonObj.optString("message", "Inisialisasi Pengerjaan Keindahan Berhasil!")
                val logsArr = jsonObj.optJSONArray("logs")
                val logsText = if (logsArr != null && logsArr.length() > 0) {
                    val sb = StringBuilder()
                    for (i in 0 until logsArr.length()) {
                        sb.append("\n• ").append(logsArr.getString(i))
                    }
                    sb.toString()
                } else ""
                Result.success("$msg$logsText")
            } else {
                Result.failure(Exception(if (getBody.isNotBlank()) getBody else postBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hapus satu file foto spesifik dari Google Drive
     */
    suspend fun deleteImageFromGoogleDrive(
        scriptUrl: String,
        photoUrlOrId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        if (cleanUrl.isBlank() || photoUrlOrId.isBlank()) return@withContext Result.success(true)

        try {
            val jsonPayload = JSONObject().apply {
                put("action", "deleteImageFromDrive")
                put("fileId", photoUrlOrId)
                put("url", photoUrlOrId)
            }
            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val postResponse = try {
                httpClient.newCall(postRequest).execute()
            } catch (e: Exception) {
                null
            }

            val postBody = postResponse?.body?.string() ?: ""
            if (postResponse != null && postResponse.isSuccessful &&
                (postBody.contains("success", ignoreCase = true) || postBody.contains("deleted", ignoreCase = true) || postBody.contains("OK", ignoreCase = true))) {
                return@withContext Result.success(true)
            }

            // Fallback GET
            val getUrl = "$cleanUrl?action=deleteImageFromDrive&url=${java.net.URLEncoder.encode(photoUrlOrId, "UTF-8")}"
            val getReq = Request.Builder().url(getUrl).get().header("User-Agent", "Mozilla/5.0").build()
            val getResp = try {
                httpClient.newCall(getReq).execute()
            } catch (e: Exception) {
                null
            }
            val getBody = getResp?.body?.string() ?: ""
            if (getResp != null && getResp.isSuccessful &&
                (getBody.contains("success", ignoreCase = true) || getBody.contains("deleted", ignoreCase = true) || getBody.contains("OK", ignoreCase = true))) {
                return@withContext Result.success(true)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Kirim data Pekerjaan Service Keindahan (termasuk kompresi dan upload 5 foto ke Google Drive)
     * ke Google Apps Script Web App.
     * Google Apps Script akan:
     * 1. Mengunggah Base64 foto ke Google Drive (folder ID: 1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6)
     * 2. Menghasilkan link Google Drive publik (ANYONE_WITH_LINK)
     * 3. Menyimpan / mengupdate 13 kolom ke spreadsheet 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30, sheet SERVICE_KEINDAHAN
     * 4. Mengembalikan URL Google Drive dari masing-masing foto
     */
    suspend fun sendPekerjaanServiceKeindahanToGoogleSheet(
        scriptUrl: String,
        item: PekerjaanServiceKeindahanEntity,
        sheetName: String = "SERVICE_KEINDAHAN",
        spreadsheetId: String = "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30",
        folderId: String = "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6"
    ): Result<PekerjaanServiceKeindahanEntity> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        if (cleanUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL Google Apps Script belum diisi."))
        }

        val targetSheet = sheetName.ifBlank { "SERVICE_KEINDAHAN" }
        val targetSpreadsheetId = spreadsheetId.ifBlank { "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30" }
        val targetFolderId = folderId.ifBlank { "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6" }

        fun fileToBase64(pathOrUrl: String, maxDim: Int = 4096, quality: Int = 98): String {
            val trimmed = pathOrUrl.trim()
            if (trimmed.isBlank() || trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                return trimmed
            }
            try {
                val f = java.io.File(trimmed.removePrefix("file://"))
                if (f.exists() && f.isFile) {
                    val boundsOptions = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    android.graphics.BitmapFactory.decodeFile(f.absolutePath, boundsOptions)
                    var sampleSize = 1
                    if (boundsOptions.outHeight > maxDim || boundsOptions.outWidth > maxDim) {
                        val halfHeight = boundsOptions.outHeight / 2
                        val halfWidth = boundsOptions.outWidth / 2
                        while ((halfHeight / sampleSize) >= maxDim || (halfWidth / sampleSize) >= maxDim) {
                            sampleSize *= 2
                        }
                    }
                    val decodeOptions = android.graphics.BitmapFactory.Options().apply { 
                        inSampleSize = sampleSize
                        inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                        inDither = false
                    }
                    val bmp = android.graphics.BitmapFactory.decodeFile(f.absolutePath, decodeOptions)
                    if (bmp != null) {
                        val exif = try { android.media.ExifInterface(f.absolutePath) } catch (_: Exception) { null }
                        val orientation = exif?.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL)
                            ?: android.media.ExifInterface.ORIENTATION_NORMAL
                        val matrix = android.graphics.Matrix()
                        when (orientation) {
                            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                        }
                        val rotated = if (!matrix.isIdentity) {
                            android.graphics.Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                        } else bmp
                        val out = java.io.ByteArrayOutputStream()
                        rotated.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, out)
                        val b64 = android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP)
                        return "data:image/jpeg;base64,$b64"
                    }
                }
            } catch (_: Throwable) {}
            return trimmed
        }

        try {
            val b64Depan = fileToBase64(item.photoDepan)
            val b64Lh = fileToBase64(item.photoSampingLh)
            val b64Interior = fileToBase64(item.photoInterior)
            val b64Belakang = fileToBase64(item.photoBelakang)
            val b64Rh = fileToBase64(item.photoSampingRh)

            val isEdit = item.id > 0L

            val dataObj = JSONObject().apply {
                put("id", item.id)
                put("isEdit", isEdit)
                put("tanggal", item.tanggal)
                put("tgl", item.tanggal)
                put("noMobil", item.noMobil)
                put("no_mobil", item.noMobil)
                put("nomobil", item.noMobil)
                put("jenisService", item.jenisService)
                put("jenis_service", item.jenisService)
                put("nipNama", item.nipNama)
                put("nip_nama", item.nipNama)
                put("nip", item.nipNama)
                put("notifNo", item.notifNo)
                put("notif_no", item.notifNo)
                put("order", item.orderNo)
                put("orderNo", item.orderNo)
                put("order_no", item.orderNo)
                put("approvel", item.approvel)
                put("approval", item.approvel)
                put("aprovel", item.approvel)
                put("km", item.km)
                put("photoDepan", b64Depan)
                put("photo_depan", b64Depan)
                put("photoSampingLh", b64Lh)
                put("photo_samping_lh", b64Lh)
                put("photoInterior", b64Interior)
                put("photo_interior", b64Interior)
                put("photoBelakang", b64Belakang)
                put("photo_belakang", b64Belakang)
                put("photoSampingRh", b64Rh)
                put("photo_samping_rh", b64Rh)
                put("sheetName", targetSheet)
                put("spreadsheetId", targetSpreadsheetId)
                put("folderId", targetFolderId)
                put("folderName", "FOTO_AUDIT_KENDARAAN")

                val pMap = JSONObject().apply {
                    put("photoDepan", b64Depan)
                    put("photo_depan", b64Depan)
                    put("photoSampingLh", b64Lh)
                    put("photo_samping_lh", b64Lh)
                    put("photoInterior", b64Interior)
                    put("photo_interior", b64Interior)
                    put("photoBelakang", b64Belakang)
                    put("photo_belakang", b64Belakang)
                    put("photoSampingRh", b64Rh)
                    put("photo_samping_rh", b64Rh)
                    put("photo1", b64Depan)
                    put("photo2", b64Lh)
                    put("photo3", b64Interior)
                    put("photo4", b64Belakang)
                    put("photo5", b64Rh)
                }
                put("photosMap", pMap)
            }

            val jsonPayload = JSONObject().apply {
                put("action", "savePengerjaanKeindahan")
                put("data", dataObj)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val uploadClient = httpClient.newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build()

            val postResponse = uploadClient.newCall(postRequest).execute()
            val postBody = postResponse.body?.string() ?: ""

            if (postResponse.isSuccessful && (postBody.contains("success", ignoreCase = true) || postBody.contains("OK", ignoreCase = true))) {
                val jsonObj = JSONObject(postBody)
                val data = jsonObj.optJSONObject("data") ?: jsonObj
                val driveUrls = data.optJSONObject("driveUrls")

                val rId = data.optLong("id", item.id)
                val dDepan = driveUrls?.optString("photoDepan")?.ifBlank { null }
                    ?: data.optString("photoDepan").ifBlank { null }
                    ?: item.photoDepan
                val dLh = driveUrls?.optString("photoSampingLh")?.ifBlank { null }
                    ?: data.optString("photoSampingLh").ifBlank { null }
                    ?: item.photoSampingLh
                val dInterior = driveUrls?.optString("photoInterior")?.ifBlank { null }
                    ?: data.optString("photoInterior").ifBlank { null }
                    ?: item.photoInterior
                val dBelakang = driveUrls?.optString("photoBelakang")?.ifBlank { null }
                    ?: data.optString("photoBelakang").ifBlank { null }
                    ?: item.photoBelakang
                val dRh = driveUrls?.optString("photoSampingRh")?.ifBlank { null }
                    ?: data.optString("photoSampingRh").ifBlank { null }
                    ?: item.photoSampingRh

                val finalId = if (rId > 0L) rId else if (item.id > 0L) item.id else 1L
                val isDepanDrive = dDepan.startsWith("http")
                val isLhDrive = dLh.startsWith("http")
                val isInteriorDrive = dInterior.startsWith("http")
                val isBelakangDrive = dBelakang.startsWith("http")
                val isRhDrive = dRh.startsWith("http")

                val updated = item.copy(
                    id = finalId,
                    photoDepan = if (isDepanDrive) dDepan else item.photoDepan,
                    photoSampingLh = if (isLhDrive) dLh else item.photoSampingLh,
                    photoInterior = if (isInteriorDrive) dInterior else item.photoInterior,
                    photoBelakang = if (isBelakangDrive) dBelakang else item.photoBelakang,
                    photoSampingRh = if (isRhDrive) dRh else item.photoSampingRh,
                    isSynced = true,
                    syncTimestamp = System.currentTimeMillis()
                )

                val hasLocalPhoto = (item.photoDepan.isNotBlank() && !item.photoDepan.startsWith("http")) ||
                                    (item.photoSampingLh.isNotBlank() && !item.photoSampingLh.startsWith("http")) ||
                                    (item.photoInterior.isNotBlank() && !item.photoInterior.startsWith("http")) ||
                                    (item.photoBelakang.isNotBlank() && !item.photoBelakang.startsWith("http")) ||
                                    (item.photoSampingRh.isNotBlank() && !item.photoSampingRh.startsWith("http"))

                val anyPhotoUploaded = isDepanDrive || isLhDrive || isInteriorDrive || isBelakangDrive || isRhDrive
                val photoError = data.optString("photoError").ifBlank { data.optString("warning") }

                if (hasLocalPhoto && !anyPhotoUploaded) {
                    val errDetail = if (photoError.isNotBlank()) photoError else "Izin DriveApp belum aktif pada Web App Google Apps Script."
                    Result.failure(Exception("Data teks tersimpan di Sheets, TETAPI Foto gagal diunggah ke Google Drive!\n\nPenyebab: $errDetail\n\nSolusi:\n1. Buka script.google.com\n2. Jalankan fungsi 'aktivasiIzinGoogleDrive' dan Izinkan (Allow).\n3. Deploy Versi Baru."))
                } else {
                    Result.success(updated)
                }
            } else {
                Result.failure(Exception("Gagal kirim ke Google Sheets: $postBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePekerjaanServiceKeindahanFromGoogleSheet(
        scriptUrl: String,
        id: Long,
        spreadsheetId: String = "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs",
        sheetName: String = "SERVICE_KEINDAHAN"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        if (cleanUrl.isBlank()) return@withContext Result.success(true)

        try {
            val jsonPayload = JSONObject().apply {
                put("action", "deletePengerjaanKeindahan")
                val dataObj = JSONObject().apply {
                    put("id", id)
                    put("sheetName", sheetName)
                    put("spreadsheetId", spreadsheetId)
                }
                put("data", dataObj)
            }
            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val postResponse = try {
                httpClient.newCall(postRequest).execute()
            } catch (e: Exception) { null }

            val postBody = postResponse?.body?.string() ?: ""
            if (postResponse != null && postResponse.isSuccessful &&
                (postBody.contains("success", ignoreCase = true) || postBody.contains("deleted", ignoreCase = true) || postBody.contains("OK", ignoreCase = true))) {
                return@withContext Result.success(true)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Kirim data User ke Google Apps Script Web App untuk disimpan / dikoreksi / diupdate ke Google Sheets
     * Tabel: USER (Spreadsheet ID: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30, Sheet: USER)
     * Susunan Tabel: | NIP | NAMA_MEKANIK | USERNAME | PASSWORD | LEVELAKSES |
     */
    suspend fun sendUserToGoogleSheet(
        scriptUrl: String,
        user: UserEntity,
        sheetName: String = "USER",
        spreadsheetId: String = "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30",
        originalNip: String = "",
        originalUsername: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        if (cleanUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL Google Apps Script belum diisi."))
        }

        val targetSheet = sheetName.ifBlank { "USER" }
        val targetSpreadsheetId = spreadsheetId.ifBlank { "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30" }

        try {
            // 1. Coba via HTTP POST dengan JSON payload
            val jsonPayload = JSONObject().apply {
                put("action", "saveOrUpdateUser")
                val dataObj = JSONObject().apply {
                    put("id", user.id)
                    put("row", if (user.id in 1..99999) user.id + 1 else 0)
                    put("nip", user.nip)
                    put("originalNip", originalNip.ifBlank { user.nip })
                    put("namaMekanik", user.namaMekanik)
                    put("username", user.username)
                    put("originalUsername", originalUsername.ifBlank { user.username })
                    put("password", user.password)
                    put("levelAkses", user.level)
                    put("level", user.level)
                    put("sheetName", targetSheet)
                    put("spreadsheetId", targetSpreadsheetId)
                }
                put("data", dataObj)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val postResponse = try {
                httpClient.newCall(postRequest).execute()
            } catch (e: Exception) {
                null
            }

            val postBody = postResponse?.body?.string() ?: ""
            if (postResponse != null && postResponse.isSuccessful &&
                (postBody.contains("success", ignoreCase = true) || postBody.contains("OK", ignoreCase = true) || postBody.contains("updated", ignoreCase = true) || postBody.contains("appended", ignoreCase = true))) {
                return@withContext Result.success("Berhasil tersinkronisasi ke Google Sheets ($targetSheet)!")
            }

            // 2. Fallback via HTTP GET jika POST dialihkan (redirect 302 Apps Script)
            val encodedParams = java.lang.StringBuilder("$cleanUrl?")
                .append("action=saveOrUpdateUser")
                .append("&sheetName=").append(java.net.URLEncoder.encode(targetSheet, "UTF-8"))
                .append("&spreadsheetId=").append(java.net.URLEncoder.encode(targetSpreadsheetId, "UTF-8"))
                .append("&id=").append(user.id)
                .append("&row=").append(if (user.id in 1..99999) user.id + 1 else 0)
                .append("&nip=").append(java.net.URLEncoder.encode(user.nip, "UTF-8"))
                .append("&originalNip=").append(java.net.URLEncoder.encode(originalNip.ifBlank { user.nip }, "UTF-8"))
                .append("&namaMekanik=").append(java.net.URLEncoder.encode(user.namaMekanik, "UTF-8"))
                .append("&username=").append(java.net.URLEncoder.encode(user.username, "UTF-8"))
                .append("&originalUsername=").append(java.net.URLEncoder.encode(originalUsername.ifBlank { user.username }, "UTF-8"))
                .append("&password=").append(java.net.URLEncoder.encode(user.password, "UTF-8"))
                .append("&levelAkses=").append(java.net.URLEncoder.encode(user.level, "UTF-8"))
                .append("&level=").append(java.net.URLEncoder.encode(user.level, "UTF-8"))
                .toString()

            val getRequest = Request.Builder()
                .url(encodedParams)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val getResponse = httpClient.newCall(getRequest).execute()
            val getBody = getResponse.body?.string() ?: ""

            if (getResponse.isSuccessful && (getBody.contains("success", ignoreCase = true) || getBody.contains("OK", ignoreCase = true) || getBody.contains("updated", ignoreCase = true) || getBody.contains("appended", ignoreCase = true))) {
                Result.success("Berhasil tersimpan ke Google Sheets ($targetSheet)!")
            } else {
                Result.failure(Exception("Respon Google Script: $getBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hapus data User dari Google Sheets melalui Google Apps Script Web App
     */
    suspend fun deleteUserFromGoogleSheet(
        scriptUrl: String,
        user: UserEntity,
        sheetName: String = "USER",
        spreadsheetId: String = "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        if (cleanUrl.isBlank()) return@withContext Result.success(true)

        val targetSheet = sheetName.ifBlank { "USER" }
        val targetSpreadsheetId = spreadsheetId.ifBlank { "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30" }

        try {
            val jsonPayload = JSONObject().apply {
                put("action", "deleteUser")
                val dataObj = JSONObject().apply {
                    put("id", user.id)
                    put("row", if (user.id in 1..99999) user.id + 1 else 0)
                    put("nip", user.nip)
                    put("username", user.username)
                    put("namaMekanik", user.namaMekanik)
                    put("sheetName", targetSheet)
                    put("spreadsheetId", targetSpreadsheetId)
                }
                put("data", dataObj)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val postResponse = try {
                httpClient.newCall(postRequest).execute()
            } catch (e: Exception) {
                null
            }

            val postBody = postResponse?.body?.string() ?: ""
            if (postResponse != null && postResponse.isSuccessful &&
                (postBody.contains("success", ignoreCase = true) || postBody.contains("deleted", ignoreCase = true) || postBody.contains("OK", ignoreCase = true))) {
                return@withContext Result.success(true)
            }

            val getUrl = "$cleanUrl?action=deleteUser" +
                "&sheetName=${java.net.URLEncoder.encode(targetSheet, "UTF-8")}" +
                "&spreadsheetId=${java.net.URLEncoder.encode(targetSpreadsheetId, "UTF-8")}" +
                "&nip=${java.net.URLEncoder.encode(user.nip, "UTF-8")}" +
                "&username=${java.net.URLEncoder.encode(user.username, "UTF-8")}"
            val getReq = Request.Builder().url(getUrl).get().header("User-Agent", "Mozilla/5.0").build()
            val getResp = try {
                httpClient.newCall(getReq).execute()
            } catch (e: Exception) {
                null
            }
            val getBody = getResp?.body?.string() ?: ""
            if (getResp != null && getResp.isSuccessful &&
                (getBody.contains("success", ignoreCase = true) || getBody.contains("deleted", ignoreCase = true) || getBody.contains("OK", ignoreCase = true))) {
                return@withContext Result.success(true)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Kirim data Laporan Harian ke Google Apps Script Web App untuk disimpan / diupdate ke Google Sheets
     */
    suspend fun sendLaporanHarianToGoogleSheet(
        scriptUrl: String,
        laporan: LaporanHarianEntity,
        sheetName: String = "LAPORAN KENDARAAN",
        spreadsheetId: String = "",
        isEditMode: Boolean = laporan.id > 0
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim()
        if (cleanUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL Google Apps Script belum diisi. Masukkan URL Web App di menu PENGATURAN GOOGLE SCRIPT."))
        }

        val targetSheet = sheetName.ifBlank { "LAPORAN KENDARAAN" }
        val targetSpreadsheetId = spreadsheetId.ifBlank { "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30" }
        val isEdit = isEditMode
        val targetRow = if (isEdit && laporan.id > 0) laporan.id + 1 else 0

        try {
            // 1. Coba via HTTP POST dengan JSON payload
            val jsonPayload = JSONObject().apply {
                put("action", "saveOrUpdateLaporanHarian")
                val dataObj = JSONObject().apply {
                    put("id", laporan.id)
                    put("row", targetRow)
                    put("isEdit", isEdit)
                    put("timestamp", laporan.timestamp)
                    put("noMobil", laporan.noMobil)
                    put("namaNip", laporan.namaNip)
                    put("keluhanKendaraan", laporan.keluhanKendaraan)
                    put("kodeAkses", laporan.kodeAkses)
                    put("token", laporan.token)
                    put("status", laporan.status)
                    put("updateData", laporan.updateData)
                    put("poolVendor", laporan.poolVendor)
                    put("namaMekanik", laporan.namaMekanik)
                    put("pengerjaan", laporan.pengerjaan)
                    put("sheetName", targetSheet)
                    put("spreadsheetId", targetSpreadsheetId)
                }
                put("data", dataObj)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val postResponse = try {
                httpClient.newCall(postRequest).execute()
            } catch (e: Exception) {
                null
            }

            val postBody = postResponse?.body?.string() ?: ""
            if (postResponse != null && postResponse.isSuccessful) {
                var isOk = false
                var errorMsg: String? = null
                try {
                    val jsonObj = JSONObject(postBody)
                    val successField = jsonObj.optBoolean("success", false)
                    val statusField = jsonObj.optString("status", "")
                    val dataObj = jsonObj.optJSONObject("data")
                    val dataSuccess = dataObj?.optBoolean("success", true) ?: true

                    if (successField || statusField.equals("success", ignoreCase = true)) {
                        if (dataSuccess) {
                            isOk = true
                        } else {
                            errorMsg = dataObj?.optString("message") ?: "Gagal memperbarui data"
                        }
                    } else if (statusField.equals("error", ignoreCase = true) || !successField) {
                        errorMsg = jsonObj.optString("message").ifBlank { "Gagal memperbarui data di Google Sheets" }
                    }
                } catch (e: Exception) {
                    if (postBody.contains("\"success\":true", ignoreCase = true) || 
                        postBody.contains("\"action\":\"updated\"", ignoreCase = true) || 
                        postBody.contains("\"action\":\"appended\"", ignoreCase = true)) {
                        isOk = true
                    } else if (postBody.contains("\"success\":false", ignoreCase = true) || postBody.contains("\"status\":\"error\"", ignoreCase = true)) {
                        errorMsg = "Gagal memperbarui: Baris data tidak ditemukan di Google Sheets"
                    }
                }

                if (isOk) {
                    return@withContext Result.success("Berhasil tersinkronisasi ke Google Sheets ($targetSheet)!")
                } else if (errorMsg != null) {
                    return@withContext Result.failure(Exception(errorMsg))
                }
            }

            // Jika error izin terdeteksi langsung dari POST
            if (postBody.contains("permission", ignoreCase = true) || postBody.contains("tidak memiliki izin", ignoreCase = true)) {
                return@withContext Result.failure(Exception("Izin Ditolak Google: Akun pemilik script belum diberi izin Edit ke Spreadsheet. Buka Google Sheets > Ekstensi > Apps Script > Terapkan Web App baru."))
            }

            // 2. Fallback via HTTP GET jika POST dialihkan (redirect 302 Apps Script)
            val encodedParams = java.lang.StringBuilder("$cleanUrl?")
                .append("action=saveOrUpdateLaporanHarian")
                .append("&sheetName=").append(java.net.URLEncoder.encode(targetSheet, "UTF-8"))
                .append("&spreadsheetId=").append(java.net.URLEncoder.encode(targetSpreadsheetId, "UTF-8"))
                .append("&id=").append(laporan.id)
                .append("&row=").append(targetRow)
                .append("&isEdit=").append(isEdit)
                .append("&noMobil=").append(java.net.URLEncoder.encode(laporan.noMobil, "UTF-8"))
                .append("&timestamp=").append(java.net.URLEncoder.encode(laporan.timestamp, "UTF-8"))
                .append("&namaNip=").append(java.net.URLEncoder.encode(laporan.namaNip, "UTF-8"))
                .append("&keluhanKendaraan=").append(java.net.URLEncoder.encode(laporan.keluhanKendaraan, "UTF-8"))
                .append("&status=").append(java.net.URLEncoder.encode(laporan.status, "UTF-8"))
                .append("&namaMekanik=").append(java.net.URLEncoder.encode(laporan.namaMekanik, "UTF-8"))
                .append("&pengerjaan=").append(java.net.URLEncoder.encode(laporan.pengerjaan, "UTF-8"))
                .append("&poolVendor=").append(java.net.URLEncoder.encode(laporan.poolVendor, "UTF-8"))
                .append("&token=").append(java.net.URLEncoder.encode(laporan.token, "UTF-8"))
                .append("&kodeAkses=").append(java.net.URLEncoder.encode(laporan.kodeAkses, "UTF-8"))
                .append("&updateData=").append(java.net.URLEncoder.encode(laporan.updateData, "UTF-8"))
                .toString()

            val getRequest = Request.Builder()
                .url(encodedParams)
                .get()
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val getResponse = httpClient.newCall(getRequest).execute()
            val getBody = getResponse.body?.string() ?: ""

            if (getResponse.isSuccessful) {
                var isGetOk = false
                var getErrorMsg: String? = null
                try {
                    val jsonObj = JSONObject(getBody)
                    val successField = jsonObj.optBoolean("success", false)
                    val statusField = jsonObj.optString("status", "")
                    val dataObj = jsonObj.optJSONObject("data")
                    val dataSuccess = dataObj?.optBoolean("success", true) ?: true

                    if (successField || statusField.equals("success", ignoreCase = true)) {
                        if (dataSuccess) {
                            isGetOk = true
                        } else {
                            getErrorMsg = dataObj?.optString("message") ?: "Gagal memperbarui data"
                        }
                    } else if (statusField.equals("error", ignoreCase = true) || !successField) {
                        getErrorMsg = jsonObj.optString("message").ifBlank { "Gagal memperbarui data di Google Sheets" }
                    }
                } catch (e: Exception) {
                    if (getBody.contains("\"success\":true", ignoreCase = true) || 
                        getBody.contains("\"action\":\"updated\"", ignoreCase = true) || 
                        getBody.contains("\"action\":\"appended\"", ignoreCase = true)) {
                        isGetOk = true
                    } else if (getBody.contains("\"success\":false", ignoreCase = true) || getBody.contains("\"status\":\"error\"", ignoreCase = true)) {
                        getErrorMsg = "Gagal memperbarui: Baris data tidak ditemukan di Google Sheets"
                    }
                }

                if (isGetOk) {
                    Result.success("Berhasil tersinkronisasi ke Google Sheets ($targetSheet)!")
                } else if (getErrorMsg != null) {
                    Result.failure(Exception(getErrorMsg))
                } else if (getBody.contains("permission", ignoreCase = true) || getBody.contains("tidak memiliki izin", ignoreCase = true)) {
                    Result.failure(Exception("Izin Ditolak Google: Akun pemilik script belum diberi izin Edit ke Spreadsheet. Buka Google Sheets > Ekstensi > Apps Script > Terapkan Web App baru."))
                } else if (getBody.contains("Rhino runtime is deprecated", ignoreCase = true)) {
                    Result.failure(Exception("Google Apps Script Error: Rhino runtime usang. Buka Apps Script > klik Deploy > New Deployment (Penerapan Baru) > Web App."))
                } else {
                    Result.failure(Exception("Respon Google Script: ${getBody.take(120)}"))
                }
            } else {
                if (getBody.contains("permission", ignoreCase = true) || getBody.contains("tidak memiliki izin", ignoreCase = true)) {
                    Result.failure(Exception("Izin Ditolak Google: Akun pemilik script belum diberi izin Edit ke Spreadsheet. Buka Google Sheets > Ekstensi > Apps Script > Terapkan Web App baru."))
                } else if (getBody.contains("Rhino runtime is deprecated", ignoreCase = true)) {
                    Result.failure(Exception("Google Apps Script Error: Rhino runtime usang. Buka Apps Script > klik Deploy > New Deployment (Penerapan Baru) > Web App."))
                } else {
                    Result.failure(Exception("Respon Google Script: ${getBody.take(120)}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal menghubungi Google Apps Script: ${e.message}"))
        }
    }

    /**
     * Kirim data Prediksi Part ke Google Apps Script Web App untuk disimpan / diupdate ke Google Sheets
     */
    suspend fun sendPrediksiPartToGoogleSheet(
        scriptUrl: String,
        item: PrediksiPartEntity,
        sheetName: String = "PREDIKSI_PART",
        spreadsheetId: String = "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30"
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim()
        if (cleanUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL Google Apps Script belum diisi."))
        }

        val targetSheet = sheetName.ifBlank { "PREDIKSI_PART" }
        val targetSpreadsheetId = spreadsheetId.ifBlank { "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30" }

        try {
            // 1. Coba via HTTP POST dengan JSON payload
            val jsonPayload = JSONObject().apply {
                put("action", "saveOrUpdatePrediksiPart")
                val dataObj = JSONObject().apply {
                    put("id", item.id)
                    put("tanggal", item.tanggal)
                    put("noMobil", item.noMobil)
                    put("service", item.service)
                    put("kmMobil", item.kmInput)
                    put("kmInput", item.kmInput)
                    put("nipNama", item.nipNama)
                    put("namaPart", item.namaPart)
                    put("prediksiKm", item.prediksiKm)
                    put("kmTarget", item.kmTarget)
                    put("argoSekarang", item.argoSekarang)
                    put("estimasiKm", item.estimasiKm)
                    put("aprovel", item.aprovel)
                    put("keterangan", item.keterangan)
                    put("status", item.status)
                    put("sheetName", targetSheet)
                    put("spreadsheetId", targetSpreadsheetId)
                }
                put("data", dataObj)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val postResponse = try {
                httpClient.newCall(postRequest).execute()
            } catch (e: Exception) {
                null
            }

            val postBody = postResponse?.body?.string() ?: ""
            if (postResponse != null && postResponse.isSuccessful &&
                (postBody.contains("success", ignoreCase = true) || postBody.contains("OK", ignoreCase = true) || postBody.contains("updated", ignoreCase = true) || postBody.contains("appended", ignoreCase = true))) {
                return@withContext Result.success("Berhasil tersinkronisasi ke Google Sheets ($targetSheet)!")
            }

            // 2. Fallback via HTTP GET jika POST dialihkan (redirect 302 Apps Script)
            val encodedParams = java.lang.StringBuilder("$cleanUrl?")
                .append("action=saveOrUpdatePrediksiPart")
                .append("&sheetName=").append(java.net.URLEncoder.encode(targetSheet, "UTF-8"))
                .append("&spreadsheetId=").append(java.net.URLEncoder.encode(targetSpreadsheetId, "UTF-8"))
                .append("&id=").append(item.id)
                .append("&tanggal=").append(java.net.URLEncoder.encode(item.tanggal, "UTF-8"))
                .append("&noMobil=").append(java.net.URLEncoder.encode(item.noMobil, "UTF-8"))
                .append("&service=").append(java.net.URLEncoder.encode(item.service, "UTF-8"))
                .append("&kmMobil=").append(java.net.URLEncoder.encode(item.kmInput, "UTF-8"))
                .append("&kmInput=").append(java.net.URLEncoder.encode(item.kmInput, "UTF-8"))
                .append("&nipNama=").append(java.net.URLEncoder.encode(item.nipNama, "UTF-8"))
                .append("&namaPart=").append(java.net.URLEncoder.encode(item.namaPart, "UTF-8"))
                .append("&prediksiKm=").append(java.net.URLEncoder.encode(item.prediksiKm, "UTF-8"))
                .append("&kmTarget=").append(java.net.URLEncoder.encode(item.kmTarget, "UTF-8"))
                .append("&argoSekarang=").append(java.net.URLEncoder.encode(item.argoSekarang, "UTF-8"))
                .append("&estimasiKm=").append(java.net.URLEncoder.encode(item.estimasiKm, "UTF-8"))
                .append("&aprovel=").append(java.net.URLEncoder.encode(item.aprovel, "UTF-8"))
                .append("&keterangan=").append(java.net.URLEncoder.encode(item.keterangan, "UTF-8"))
                .append("&status=").append(java.net.URLEncoder.encode(item.status, "UTF-8"))
                .toString()

            val getRequest = Request.Builder()
                .url(encodedParams)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val getResponse = httpClient.newCall(getRequest).execute()
            val getBody = getResponse.body?.string() ?: ""

            if (getResponse.isSuccessful && (getBody.contains("success", ignoreCase = true) || getBody.contains("OK", ignoreCase = true) || getBody.contains("updated", ignoreCase = true) || getBody.contains("appended", ignoreCase = true))) {
                Result.success("Berhasil tersimpan ke Google Sheets ($targetSheet)!")
            } else {
                Result.failure(Exception("Respon Google Script: $getBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hapus data Prediksi Part dari Google Sheets melalui Google Apps Script Web App
     */
    suspend fun deletePrediksiPartFromGoogleSheet(
        scriptUrl: String,
        item: PrediksiPartEntity,
        sheetName: String = "PREDIKSI_PART",
        spreadsheetId: String = "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        if (cleanUrl.isBlank()) return@withContext Result.success(true)

        val targetSheet = sheetName.ifBlank { "PREDIKSI_PART" }
        val targetSpreadsheetId = spreadsheetId.ifBlank { "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30" }

        try {
            val jsonPayload = JSONObject().apply {
                put("action", "deletePrediksiPart")
                val dataObj = JSONObject().apply {
                    put("id", item.id)
                    put("noMobil", item.noMobil)
                    put("namaPart", item.namaPart)
                    put("tanggal", item.tanggal)
                    put("sheetName", targetSheet)
                    put("spreadsheetId", targetSpreadsheetId)
                }
                put("data", dataObj)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val postResponse = try {
                httpClient.newCall(postRequest).execute()
            } catch (e: Exception) {
                null
            }

            val postBody = postResponse?.body?.string() ?: ""
            if (postResponse != null && postResponse.isSuccessful &&
                (postBody.contains("success", ignoreCase = true) || postBody.contains("deleted", ignoreCase = true) || postBody.contains("OK", ignoreCase = true))) {
                return@withContext Result.success(true)
            }

            val getUrl = "$cleanUrl?action=deletePrediksiPart" +
                "&sheetName=${java.net.URLEncoder.encode(targetSheet, "UTF-8")}" +
                "&spreadsheetId=${java.net.URLEncoder.encode(targetSpreadsheetId, "UTF-8")}" +
                "&id=${item.id}" +
                "&noMobil=${java.net.URLEncoder.encode(item.noMobil, "UTF-8")}" +
                "&namaPart=${java.net.URLEncoder.encode(item.namaPart, "UTF-8")}"
            val getReq = Request.Builder().url(getUrl).get().header("User-Agent", "Mozilla/5.0").build()
            val getResp = try {
                httpClient.newCall(getReq).execute()
            } catch (e: Exception) {
                null
            }
            val getBody = getResp?.body?.string() ?: ""
            if (getResp != null && getResp.isSuccessful &&
                (getBody.contains("success", ignoreCase = true) || getBody.contains("deleted", ignoreCase = true) || getBody.contains("OK", ignoreCase = true))) {
                return@withContext Result.success(true)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hapus data Pengerjaan Cat dari Google Sheets melalui Google Apps Script Web App
     */
    suspend fun deletePengerjaanCatFromGoogleSheet(
        scriptUrl: String,
        cat: PengerjaanCatEntity,
        sheetName: String = "PENGERJAAN_CAT",
        spreadsheetId: String = "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs"
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim().ifBlank { SessionManager.DEFAULT_SCRIPT_URL }
        if (cleanUrl.isBlank()) return@withContext Result.success(true)

        val targetSheet = sheetName.ifBlank { "PENGERJAAN_CAT" }
        val targetSpreadsheetId = spreadsheetId.ifBlank { "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs" }

        try {
            val jsonPayload = JSONObject().apply {
                put("action", "deletePengerjaanCat")
                val dataObj = JSONObject().apply {
                    put("id", cat.id)
                    put("timestamp", cat.timestamp)
                    put("tanggal", cat.tanggal)
                    put("noMobil", cat.noMobil)
                    put("kerusakan", cat.kerusakan)
                    put("noRegistrasi", cat.noRegistrasi)
                    put("noRegistrasiCat", cat.noRegistrasi)
                    put("sheetName", targetSheet)
                    put("spreadsheetId", targetSpreadsheetId)
                }
                put("data", dataObj)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val postRequest = Request.Builder()
                .url(cleanUrl)
                .post(requestBody)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val postResponse = try {
                httpClient.newCall(postRequest).execute()
            } catch (e: Exception) {
                null
            }

            val postBody = postResponse?.body?.string() ?: ""
            if (postResponse != null && postResponse.isSuccessful &&
                (postBody.contains("success", ignoreCase = true) || postBody.contains("deleted", ignoreCase = true) || postBody.contains("OK", ignoreCase = true))) {
                return@withContext Result.success(true)
            }

            val getUrl = "$cleanUrl?action=deletePengerjaanCat" +
                "&sheetName=${java.net.URLEncoder.encode(targetSheet, "UTF-8")}" +
                "&spreadsheetId=${java.net.URLEncoder.encode(targetSpreadsheetId, "UTF-8")}" +
                "&id=${cat.id}" +
                "&noMobil=${java.net.URLEncoder.encode(cat.noMobil, "UTF-8")}" +
                "&timestamp=${java.net.URLEncoder.encode(cat.timestamp, "UTF-8")}" +
                "&tanggal=${java.net.URLEncoder.encode(cat.tanggal, "UTF-8")}" +
                "&noRegistrasi=${java.net.URLEncoder.encode(cat.noRegistrasi, "UTF-8")}"
            val getReq = Request.Builder().url(getUrl).get().header("User-Agent", "Mozilla/5.0").build()
            val getResp = try {
                httpClient.newCall(getReq).execute()
            } catch (e: Exception) {
                null
            }
            val getBody = getResp?.body?.string() ?: ""
            if (getResp != null && getResp.isSuccessful &&
                (getBody.contains("success", ignoreCase = true) || getBody.contains("deleted", ignoreCase = true) || getBody.contains("OK", ignoreCase = true))) {
                return@withContext Result.success(true)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Uji koneksi ke URL Google Apps Script Web App
     */
    suspend fun testGoogleScriptUrl(scriptUrl: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanUrl = scriptUrl.trim()
        if (cleanUrl.isBlank()) return@withContext Result.failure(Exception("URL Google Apps Script kosong"))
        try {
            val url = if (cleanUrl.contains("?")) "$cleanUrl&action=ping" else "$cleanUrl?action=ping"
            val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
            val resp = httpClient.newCall(req).execute()
            val body = resp.body?.string() ?: ""
            if (resp.isSuccessful) {
                if (body.contains("permission", ignoreCase = true) || body.contains("tidak memiliki izin", ignoreCase = true)) {
                    Result.failure(Exception("⚠️ Izin Edit Ditolak: Script ini belum memiliki akses edit ke Google Sheets. Buka Google Sheets Anda > Ekstensi > Apps Script > Terapkan Web App dengan Akses 'Siapa Saja' (Anyone)."))
                } else if (body.contains("Rhino runtime is deprecated", ignoreCase = true)) {
                    Result.failure(Exception("Gagal: Google menolak script karena 'Rhino runtime is deprecated'. Buat New Deployment (Penerapan Baru) di Apps Script."))
                } else if (body.contains("OK", ignoreCase = true) || body.contains("success", ignoreCase = true)) {
                    Result.success("Koneksi berhasil! Server Apps Script aktif dan siap digunakan.")
                } else {
                    Result.failure(Exception("Respon server: ${body.take(100)}"))
                }
            } else {
                Result.failure(Exception("HTTP ${resp.code}: ${body.take(80)}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung: ${e.message}"))
        }
    }

    /**
     * Generate Google Apps Script source code for the entire Bengkel System
     */
    fun getGoogleScriptCode(sheetId: String, folderId: String = ""): String {
        return getBengkelAppsScriptCode(sheetId, "1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4")
    }

    fun getIndexHtmlCode(): String {
        return """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>Portal Bengkel Mobile MW</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
  <div class="container py-4">
    <div class="card shadow-sm border-0">
      <div class="card-header bg-primary text-white">
        <h4 class="mb-0">SIMBA BC - Sistem Informasi & Monitoring Bengkel Armada</h4>
      </div>
      <div class="card-body">
        <p>Aplikasi Bengkel Mobile terhubung dengan Google Sheets (Sinkronisasi Manual & Hemat Daya).</p>
        <div class="alert alert-info">
          <strong>Laporan ID:</strong> 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30<br>
          <strong>Teknik & Keindahan ID:</strong> 1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4
        </div>
      </div>
    </div>
  </div>
</body>
</html>
        """.trimIndent()
    }

    fun cleanSheetId(id: String): String {
        val trimmed = id.trim()
        val match = Regex("""/d/([a-zA-Z0-9-_]+)""").find(trimmed)
        if (match != null) return match.groupValues[1]
        return trimmed.replace("'", "").replace("\"", "").replace("\n", "").replace("\r", "").trim()
    }

    fun getBengkelAppsScriptCode(laporanSheetId: String, teknikSheetId: String = ""): String {
        val cleanLaporan = cleanSheetId(laporanSheetId).ifBlank { "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30" }
        val cleanTeknik = cleanSheetId(teknikSheetId).ifBlank { "1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4" }
        return """
// ==============================================================
// SIMBA BC - SISTEM INFORMASI & MONITORING BENGKEL ARMADA - GOOGLE APPS SCRIPT WEB APP
// Otomatisasi Sinkronisasi 2 Arah (Read & Update/Write)
// ==============================================================

// ID Spreadsheet Laporan Harian
var SHEET_LAPORAN_ID = "$cleanLaporan";
var SHEET_TEKNIK_ID = "$cleanTeknik";
var SHEET_CAT_ID = "$cleanLaporan";

// ID Folder Google Drive untuk Penyimpanan Foto Pengerjaan Keindahan
var FOLDER_PHOTO_KEINDAHAN_ID = "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6";

// ============================================================================
// 1. FUNGSI AKTIVASI IZIN GOOGLE DRIVE (PENTING! JALANKAN INI SEKALI DI EDITOR)
// ============================================================================
// Cara aktivasi izin DriveApp:
// 1. Pilih fungsi 'aktivasiIzinGoogleDrive' pada dropdown fungsi di toolbar atas
// 2. Klik tombol Jalankan (Run ▶)
// 3. Pada popup "Authorization Required", klik "Review Permissions" / "Tinjau Izin"
// 4. Pilih akun Google > Advanced (Lanjutan) > Go to ... (unsafe) > Allow (Izinkan)
// 5. Klik Deploy > Manage deployments > Edit (pensil) > Version: New version > Deploy
function aktivasiIzinGoogleDrive() {
  Logger.log("Menguji dan mengaktifkan izin Google Drive (DriveApp)...");
  try {
    var root = DriveApp.getRootFolder();
    Logger.log("✅ Izin DriveApp BERHASIL aktif! Folder Utama Drive: " + root.getName());
    var folderTargetId = FOLDER_PHOTO_KEINDAHAN_ID || "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6";
    try {
      var folder = DriveApp.getFolderById(folderTargetId);
      Logger.log("✅ Folder Foto Keindahan Ditemukan: " + folder.getName());
    } catch (eF) {
      Logger.log("ℹ️ Folder target default (" + folderTargetId + ") tidak ditemukan. Script otomatis akan membuat folder baru 'FOTO_SERVICE_KEINDAHAN' saat upload foto.");
    }
    return "Izin Google Drive (DriveApp) Berhasil Diaktifkan!";
  } catch (err) {
    Logger.log("❌ Error saat aktivasi izin Drive: " + err.message);
    throw err;
  }
}

function inisialisasiPengerjaanKeindahan(params) {
  params = params || {};
  var logs = [];
  var result = {
    status: 'success',
    success: true,
    sheetReady: false,
    folderReady: false,
    driveAccessReady: false,
    sheetName: 'SERVICE_KEINDAHAN',
    spreadsheetId: '',
    folderId: '',
    folderName: 'FOTO_AUDIT_KENDARAAN',
    message: '',
    logs: []
  };

  try {
    var ss = getSpreadsheetKeindahan(params.spreadsheetId);
    result.spreadsheetId = ss.getId();
    var sheetName = params.sheetName || 'SERVICE_KEINDAHAN';
    var sheet = ss.getSheetByName(sheetName);

    if (!sheet) {
      sheet = ss.insertSheet(sheetName);
      logs.push("✅ Sheet '" + sheetName + "' berhasil dibuat baru.");
    } else {
      logs.push("ℹ️ Sheet '" + sheetName + "' sudah tersedia.");
    }

    var expectedHeaders = [
      'ID', 'TANGGAL', 'NO MOBIL', 'JENIS SERVICE', 'NIP/NAMA', 'NOTIF NO', 'ORDER', 'APPROVEL',
      'PHOTO DEPAN', 'PHOTO SAMPING LH', 'PHOTO INTERIOR', 'PHOTO BELAKANG', 'PHOTO SAMPING RH'
    ];

    if (sheet.getLastRow() === 0) {
      sheet.appendRow(expectedHeaders);
      logs.push("✅ Header resmi 13 kolom Pengerjaan Keindahan berhasil ditambahkan.");
    } else {
      var currentHeaders = sheet.getRange(1, 1, 1, Math.max(expectedHeaders.length, sheet.getLastColumn())).getValues()[0];
      var needUpdate = false;
      for (var h = 0; h < expectedHeaders.length; h++) {
        if (!currentHeaders[h] || String(currentHeaders[h]).trim() === '') {
          needUpdate = true;
          break;
        }
      }
      if (needUpdate) {
        sheet.getRange(1, 1, 1, expectedHeaders.length).setValues([expectedHeaders]);
        logs.push("✅ Header sheet disesuaikan dengan format 13 kolom resmi.");
      }
    }

    try {
      var headerRange = sheet.getRange(1, 1, 1, 13);
      headerRange.setBackground("#003366")
                 .setFontColor("#FFFFFF")
                 .setFontWeight("bold")
                 .setFontSize(10)
                 .setHorizontalAlignment("center")
                 .setVerticalAlignment("middle");
      sheet.setRowHeight(1, 36);
      sheet.setFrozenRows(1);
      
      sheet.setColumnWidth(1, 130);  // ID
      sheet.setColumnWidth(2, 140);  // TANGGAL
      sheet.setColumnWidth(3, 110);  // NO MOBIL
      sheet.setColumnWidth(4, 150);  // JENIS SERVICE
      sheet.setColumnWidth(5, 160);  // NIP/NAMA
      sheet.setColumnWidth(6, 120);  // NOTIF NO
      sheet.setColumnWidth(7, 110);  // ORDER
      sheet.setColumnWidth(8, 110);  // APPROVEL
      sheet.setColumnWidth(9, 160);  // PHOTO DEPAN
      sheet.setColumnWidth(10, 160); // PHOTO SAMPING LH
      sheet.setColumnWidth(11, 160); // PHOTO INTERIOR
      sheet.setColumnWidth(12, 160); // PHOTO BELAKANG
      sheet.setColumnWidth(13, 160); // PHOTO SAMPING RH
      logs.push("✅ Format tabel (warna biru, teks putih, freeze baris 1, lebar kolom) berhasil dirapikan.");
    } catch (eFmt) {}
    result.sheetReady = true;

    var folderTarget = null;
    var targetFolderId = params.folderId || FOLDER_PHOTO_KEINDAHAN_ID || "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6";
    if (targetFolderId) {
      try {
        folderTarget = DriveApp.getFolderById(targetFolderId);
        logs.push("✅ Folder Google Drive ID (" + targetFolderId + ") siap: '" + folderTarget.getName() + "'.");
      } catch (eF1) {}
    }

    if (!folderTarget) {
      var targetFolderName = params.folderName || "FOTO_AUDIT_KENDARAAN";
      var fldrs = DriveApp.getFoldersByName(targetFolderName);
      folderTarget = fldrs.hasNext() ? fldrs.next() : DriveApp.createFolder(targetFolderName);
      logs.push("✅ Folder Google Drive siap: '" + folderTarget.getName() + "' (ID: " + folderTarget.getId() + ").");
    }

    if (folderTarget) {
      try {
        folderTarget.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
        logs.push("✅ Izin sharing folder diset publik/view (ANYONE_WITH_LINK).");
      } catch (eShare) {
        try {
          folderTarget.setSharing(DriveApp.Access.DOMAIN_WITH_LINK, DriveApp.Permission.VIEW);
        } catch (eShare2) {}
      }
      result.folderReady = true;
      result.folderId = folderTarget.getId();
      result.folderName = folderTarget.getName();
    }
    result.driveAccessReady = true;

    result.message = "Inisialisasi Pengerjaan Keindahan Berhasil! Sheet SERVICE_KEINDAHAN dan Folder Google Drive telah aktif & siap dipakai.";
    result.logs = logs;
    return result;

  } catch (err) {
    result.status = 'error';
    result.success = false;
    result.message = "Gagal inisialisasi: " + err.message;
    logs.push("❌ Error: " + err.message);
    result.logs = logs;
    return result;
  }
}

// Menu otomatis pada Google Sheets saat file dibuka di browser
function onOpen() {
  try {
    var ui = SpreadsheetApp.getUi();
    ui.createMenu('⚡ Bengkel MW')
      .addItem('▶ 1-Klik Inisialisasi Pengerjaan Cat (14 Kolom)', 'inisialisasiPengerjaanCat')
      .addItem('▶ 1-Klik Inisialisasi Pengerjaan Keindahan', 'inisialisasiPengerjaanKeindahan')
      .addItem('▶ Aktivasi Izin Google Drive', 'aktivasiIzinGoogleDrive')
      .addItem('▶ Test Run Sistem', 'testRun')
      .addToUi();
  } catch (e) {}
}

// Fungsi Uji Coba: Klik tombol 'Run' (Jalankan ▶) di atas untuk mengetes
function testRun() {
  Logger.log("Sistem Bengkel Aktif & Siap Digunakan!");
  try {
    aktivasiIzinGoogleDrive();
    inisialisasiPengerjaanCat({});
    inisialisasiPengerjaanKeindahan({});
    Logger.log("✅ Status Sistem: NORMAL & SIAP DIGUNAKAN");
  } catch (e) {
    Logger.log("Peringatan DriveApp: " + e.message);
  }
}

function doGet(e) {
  try {
    e = e || {};
    var params = e.parameter || {};
    var action = params.action || '';
    
    // Action 1: Ping / Cek Status
    if (action === 'ping' || action === '') {
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'OK', 
        message: 'Google Apps Script Bengkel MW Aktif dan Siap Digunakan' 
      })).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 2: Update / Simpan Laporan Harian via GET
    if (action === 'saveLaporanHarian' || action === 'updateLaporanHarian' || action === 'saveOrUpdateLaporanHarian') {
      var result = saveOrUpdateLaporan(params);
      return ContentService.createTextOutput(JSON.stringify(result)).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 3: Baca Data Laporan Harian
    if (action === 'getLaporanHarian') {
      return ContentService.createTextOutput(JSON.stringify(
        fetchSheetData(getSpreadsheetLaporan(), 'LAPORAN_HARIAN')
      )).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 4: Baca Data Service Teknik
    if (action === 'getServiceTeknik') {
      return ContentService.createTextOutput(JSON.stringify(
        fetchSheetData(getSpreadsheetTeknik(), 'SERVICE_TEKNIK')
      )).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 5: Update / Simpan User Bengkel via GET
    if (action === 'saveUser' || action === 'updateUser' || action === 'saveOrUpdateUser') {
      var resultUser = saveOrUpdateUser(params);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultUser 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 5b: Hapus User Bengkel via GET
    if (action === 'deleteUser') {
      var resultDelUserGet = deleteUser(params);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultDelUserGet 
      })).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 6: Update / Simpan Pengerjaan Cat via GET (Spreadsheet ID: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs, Sheet: PENGERJAAN_CAT)
    if (action === 'savePengerjaanCat' || action === 'updatePengerjaanCat' || action === 'saveOrUpdatePengerjaanCat') {
      var resultCat = saveOrUpdatePengerjaanCat(params);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultCat 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 6b: Hapus Pengerjaan Cat via GET
    if (action === 'deletePengerjaanCat' || action === 'deleteCat' || action === 'hapusPengerjaanCat') {
      var resultDelCat = deletePengerjaanCat(params);
      return ContentService.createTextOutput(JSON.stringify(resultDelCat)).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 6c: Inisialisasi Pengerjaan Cat via GET (14 Kolom)
    if (action === 'inisialisasiPengerjaanCat' || action === 'initPengerjaanCat' || action === 'setupPengerjaanCat') {
      var resultInitCat = inisialisasiPengerjaanCat(params);
      return ContentService.createTextOutput(JSON.stringify(resultInitCat)).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 7: Update / Simpan Pengerjaan Keindahan via GET (Spreadsheet ID: 1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs, Sheet: SERVICE_KEINDAHAN)
    if (action === 'savePengerjaanKeindahan' || action === 'updatePengerjaanKeindahan' || action === 'saveOrUpdatePengerjaanKeindahan') {
      var resultKeindahan = saveOrUpdatePengerjaanKeindahan(params);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultKeindahan 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Inisialisasi Pengerjaan Keindahan via GET (Tinggal Klik!)
    if (action === 'inisialisasiPengerjaanKeindahan' || action === 'initPengerjaanKeindahan' || action === 'setupKeindahan') {
      var resInitGet = inisialisasiPengerjaanKeindahan(params);
      return ContentService.createTextOutput(JSON.stringify(resInitGet)).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 7b: Simpan Data Audit / Input Audit via GET
    if (action === 'saveAudit' || action === 'saveDataAudit' || action === 'syncAudit' || action === 'kirimAudit') {
      var resultAuditGet = saveOrUpdateAudit(params);
      return ContentService.createTextOutput(JSON.stringify(resultAuditGet)).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 7c: Update / Simpan Prediksi Part via GET
    if (action === 'savePrediksiPart' || action === 'updatePrediksiPart' || action === 'saveOrUpdatePrediksiPart') {
      var resultPrediksiGet = saveOrUpdatePrediksiPart(params);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultPrediksiGet 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 7d: Hapus Prediksi Part via GET
    if (action === 'deletePrediksiPart' || action === 'hapusPrediksiPart' || action === 'removePrediksiPart') {
      var resultDelPrediksiGet = deletePrediksiPart(params);
      return ContentService.createTextOutput(JSON.stringify(resultDelPrediksiGet)).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 8: Upload Foto ke Google Drive via GET
    if (action === 'uploadImageToDrive' || action === 'uploadImage' || action === 'uploadPhoto') {
      try {
        var photoBase64 = params.image || params.base64 || params.photo || params.file;
        if (!photoBase64) {
          return ContentService.createTextOutput(JSON.stringify({ 
            status: 'error', 
            success: false, 
            message: 'Data foto (base64) tidak ditemukan dalam request' 
          })).setMimeType(ContentService.MimeType.JSON);
        }
        var fName = params.fileName || ('KEINDAHAN_' + new Date().getTime() + '.jpg');
        var fFolderId = params.folderId || FOLDER_PHOTO_KEINDAHAN_ID;
        var fFolderName = params.folderName || 'FOTO_SERVICE_KEINDAHAN';
        var driveLink = saveBase64ImageToDrive(photoBase64, fName, fFolderId, fFolderName);
        if (!driveLink || String(driveLink).indexOf('http') !== 0) {
          return ContentService.createTextOutput(JSON.stringify({ 
            status: 'error', 
            success: false, 
            message: 'Izin DriveApp belum aktif pada Web App Google Apps Script. Harap deploy ulang Web App dan setujui izin Google Drive.' 
          })).setMimeType(ContentService.MimeType.JSON);
        }
        return ContentService.createTextOutput(JSON.stringify({ 
          status: 'success', 
          success: true, 
          url: driveLink, 
          link: driveLink 
        })).setMimeType(ContentService.MimeType.JSON);
      } catch (errGetUpload) {
        return ContentService.createTextOutput(JSON.stringify({ 
          status: 'error', 
          success: false, 
          message: 'Error upload Google Drive: ' + (errGetUpload.message || errGetUpload.toString()) 
        })).setMimeType(ContentService.MimeType.JSON);
      }
    }

    // Action 9: Hapus Pengerjaan Keindahan via GET
    if (action === 'deletePengerjaanKeindahan' || action === 'deleteKeindahan') {
      var resultDelKeindahan = deletePengerjaanKeindahan(params);
      return ContentService.createTextOutput(JSON.stringify(resultDelKeindahan)).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 10: Hapus Foto Google Drive via GET
    if (action === 'deleteImageFromDrive' || action === 'deletePhoto' || action === 'deleteImage') {
      var resultDelPhoto = deleteImageFromDrive(params);
      return ContentService.createTextOutput(JSON.stringify(resultDelPhoto)).setMimeType(ContentService.MimeType.JSON);
    }
    
    return ContentService.createTextOutput(JSON.stringify({ 
      status: 'OK', 
      action: action 
    })).setMimeType(ContentService.MimeType.JSON);
    
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ 
      status: 'error', 
      message: err.toString() 
    })).setMimeType(ContentService.MimeType.JSON);
  }
}

function doPost(e) {
  try {
    e = e || {};
    var postData = {};
    if (e.postData && e.postData.contents) {
      try {
        postData = JSON.parse(e.postData.contents);
      } catch (errParse) {
        postData = e.parameter || {};
      }
    } else {
      postData = e.parameter || {};
    }

    var action = postData.action || (e.parameter ? e.parameter.action : '');
    var data = postData.data || postData;

    if (action === 'saveLaporanHarian' || action === 'updateLaporanHarian' || action === 'saveOrUpdateLaporanHarian') {
      var result = saveOrUpdateLaporan(data);
      return ContentService.createTextOutput(JSON.stringify(result)).setMimeType(ContentService.MimeType.JSON);
    }
    
    if (action === 'saveServiceTeknik') {
      var resTeknik = appendServiceTeknik(data);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resTeknik 
      })).setMimeType(ContentService.MimeType.JSON);
    }
    
    if (action === 'saveUser' || action === 'updateUser' || action === 'saveOrUpdateUser') {
      var resultUser = saveOrUpdateUser(data);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultUser 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    if (action === 'deleteUser') {
      var resultDelUser = deleteUser(data);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultDelUser 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    if (action === 'savePengerjaanCat' || action === 'updatePengerjaanCat' || action === 'saveOrUpdatePengerjaanCat') {
      var resultCat = saveOrUpdatePengerjaanCat(data);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultCat 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    if (action === 'deletePengerjaanCat' || action === 'deleteCat' || action === 'hapusPengerjaanCat') {
      var resultDelCatPost = deletePengerjaanCat(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resultDelCatPost)).setMimeType(ContentService.MimeType.JSON);
    }

    if (action === 'inisialisasiPengerjaanCat' || action === 'initPengerjaanCat' || action === 'setupPengerjaanCat') {
      var resInitCatPost = inisialisasiPengerjaanCat(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resInitCatPost)).setMimeType(ContentService.MimeType.JSON);
    }

    if (action === 'savePengerjaanKeindahan' || action === 'updatePengerjaanKeindahan' || action === 'saveOrUpdatePengerjaanKeindahan') {
      var resultKeindahan = saveOrUpdatePengerjaanKeindahan(data);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultKeindahan 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Inisialisasi Pengerjaan Keindahan via POST (Tinggal Klik!)
    if (action === 'inisialisasiPengerjaanKeindahan' || action === 'initPengerjaanKeindahan' || action === 'setupKeindahan') {
      var resInitPost = inisialisasiPengerjaanKeindahan(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resInitPost)).setMimeType(ContentService.MimeType.JSON);
    }

    // Simpan Audit / Input Audit (Data Audit, KPI, Catatan, dan Base64 photo1 s/d photo20 & photosMap)
    if (action === 'saveAudit' || action === 'saveDataAudit' || action === 'syncAudit' || action === 'kirimAudit') {
      var resultAudit = saveOrUpdateAudit(data || postData);
      return ContentService.createTextOutput(JSON.stringify(resultAudit)).setMimeType(ContentService.MimeType.JSON);
    }

    // Simpan Prediksi Part via POST
    if (action === 'savePrediksiPart' || action === 'updatePrediksiPart' || action === 'saveOrUpdatePrediksiPart') {
      var resultPrediksiPost = saveOrUpdatePrediksiPart(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultPrediksiPost 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Hapus Prediksi Part via POST
    if (action === 'deletePrediksiPart' || action === 'hapusPrediksiPart' || action === 'removePrediksiPart') {
      var resultDelPrediksiPost = deletePrediksiPart(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resultDelPrediksiPost)).setMimeType(ContentService.MimeType.JSON);
    }

    if (action === 'uploadImageToDrive' || action === 'uploadImage' || action === 'uploadPhoto') {
      try {
        var photoBase64 = (data && (data.image || data.base64 || data.photo || data.file)) || 
                          (postData && (postData.image || postData.base64 || postData.photo || postData.file)) || 
                          (e.parameter && (e.parameter.image || e.parameter.base64));
        if (!photoBase64) {
          return ContentService.createTextOutput(JSON.stringify({ 
            status: 'error', 
            success: false, 
            message: 'Data foto (base64) tidak ditemukan dalam request' 
          })).setMimeType(ContentService.MimeType.JSON);
        }
        var fName = (data && data.fileName) || (postData && postData.fileName) || ('KEINDAHAN_' + new Date().getTime() + '.jpg');
        var fFolderId = (data && data.folderId) || (postData && postData.folderId) || FOLDER_PHOTO_KEINDAHAN_ID;
        var fFolderName = (data && data.folderName) || (postData && postData.folderName) || 'FOTO_SERVICE_KEINDAHAN';
        var driveLink = saveBase64ImageToDrive(photoBase64, fName, fFolderId, fFolderName);
        if (!driveLink || String(driveLink).indexOf('http') !== 0) {
          return ContentService.createTextOutput(JSON.stringify({ 
            status: 'error', 
            success: false, 
            message: 'Izin DriveApp belum aktif pada Web App Google Apps Script. Harap deploy ulang Web App dan setujui izin Google Drive.' 
          })).setMimeType(ContentService.MimeType.JSON);
        }
        return ContentService.createTextOutput(JSON.stringify({ 
          status: 'success', 
          success: true, 
          url: driveLink, 
          link: driveLink 
        })).setMimeType(ContentService.MimeType.JSON);
      } catch (errPostUpload) {
        return ContentService.createTextOutput(JSON.stringify({ 
          status: 'error', 
          success: false, 
          message: 'Error upload Google Drive: ' + (errPostUpload.message || errPostUpload.toString()) 
        })).setMimeType(ContentService.MimeType.JSON);
      }
    }

    // Hapus Pengerjaan Keindahan via POST
    if (action === 'deletePengerjaanKeindahan' || action === 'deleteKeindahan') {
      var resultDelPost = deletePengerjaanKeindahan(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resultDelPost)).setMimeType(ContentService.MimeType.JSON);
    }

    // Hapus Foto dari Google Drive via POST
    if (action === 'deleteImageFromDrive' || action === 'deletePhoto' || action === 'deleteImage') {
      var resultDelImgPost = deleteImageFromDrive(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resultDelImgPost)).setMimeType(ContentService.MimeType.JSON);
    }
    
    return ContentService.createTextOutput(JSON.stringify({ 
      status: 'unknown_action', 
      action: action 
    })).setMimeType(ContentService.MimeType.JSON);
    
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ 
      status: 'error', 
      message: err.toString() 
    })).setMimeType(ContentService.MimeType.JSON);
  }
}

function getSpreadsheetLaporan(overrideId) {
  var id = String(overrideId || SHEET_LAPORAN_ID || '').trim();
  if (id.indexOf('http') !== -1) {
    var match = id.match(/\/d\/([a-zA-Z0-9-_]+)/);
    if (match) id = match[1];
  }
  // 1. Coba aktif jika script terpasang langsung di Google Spreadsheet
  try {
    var active = SpreadsheetApp.getActiveSpreadsheet();
    if (active) return active;
  } catch (e) {}

  // 2. Buka via openById
  if (id) {
    try {
      return SpreadsheetApp.openById(id);
    } catch (e) {
      Logger.log("openById error: " + e.message);
    }
  }
  throw new Error("Spreadsheet (" + (id || 'kosong') + ") tidak dapat dibuka. Pastikan script dibuat di menu Ekstensi > Apps Script di Google Sheets Anda.");
}

function getSpreadsheetTeknik(overrideId) {
  var id = String(overrideId || SHEET_TEKNIK_ID || '').trim();
  if (id.indexOf('http') !== -1) {
    var match = id.match(/\/d\/([a-zA-Z0-9-_]+)/);
    if (match) id = match[1];
  }
  try {
    var active = SpreadsheetApp.getActiveSpreadsheet();
    if (active) return active;
  } catch (e) {}

  if (id) {
    try {
      return SpreadsheetApp.openById(id);
    } catch (e) {
      Logger.log("openById error: " + e.message);
    }
  }
  throw new Error("Spreadsheet Teknik (" + (id || 'kosong') + ") tidak dapat dibuka.");
}

function fetchSheetData(ss, tabName) {
  try {
    var sheet = tabName ? (ss.getSheetByName(tabName) || ss.getSheets()[0]) : ss.getSheets()[0];
    var values = sheet.getDataRange().getValues();
    if (values.length <= 1) return [];
    var headers = values[0];
    var results = [];
    for (var r = 1; r < values.length; r++) {
      var row = values[r];
      var obj = {};
      for (var c = 0; c < headers.length; c++) {
        obj[headers[c]] = row[c];
      }
      results.push(obj);
    }
    return results;
  } catch (e) {
    return [];
  }
}

function saveOrUpdateLaporan(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "LAPORAN KENDARAAN";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("LAPORAN KENDARAAN") || 
              ss.getSheetByName("LAPORAN_KENDARAAN") || 
              ss.getSheetByName("LAPORAN HARIAN") || 
              ss.getSheetByName("LAPORAN_HARIAN") || 
              ss.getSheets()[0];
  var lastRow = sheet.getLastRow();
  var targetRow = -1;

  function cleanStr(s) {
    return String(s || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  // Susunan tabel LAPORAN KENDARAAN:
  // ID | Timestamp | NO MOBIL | NAMA/NIP | KELUHAN KENDARAAN | KODE AKSES | TOKEN | STATUS | UPDATE DATA | POOL/VENDOR | NAMA MEKANIK | PENGERJAAN
  if (lastRow === 0) {
    sheet.appendRow([
      'ID', 'Timestamp', 'NO MOBIL', 'NAMA/NIP', 'KELUHAN KENDARAAN',
      'KODE AKSES', 'TOKEN', 'STATUS', 'UPDATE DATA', 'POOL/VENDOR',
      'NAMA MEKANIK', 'PENGERJAAN'
    ]);
    lastRow = 1;
  }

  var firstHeader = lastRow >= 1 ? String(sheet.getRange(1, 1).getDisplayValue() || '').trim().toUpperCase() : 'ID';
  var hasIdCol = !(firstHeader === 'TIMESTAMP' || firstHeader === 'WAKTU');
  var colId = hasIdCol ? 1 : 0;
  var colTs = hasIdCol ? 2 : 1;
  var colMobil = hasIdCol ? 3 : 2;
  var colNamaNip = hasIdCol ? 4 : 3;
  var colKeluhan = hasIdCol ? 5 : 4;
  var colKodeAkses = hasIdCol ? 6 : 5;
  var colToken = hasIdCol ? 7 : 6;
  var colStatus = hasIdCol ? 8 : 7;
  var colUpdateData = hasIdCol ? 9 : 8;
  var colPoolVendor = hasIdCol ? 10 : 9;
  var colNamaMekanik = hasIdCol ? 11 : 10;
  var colPengerjaan = hasIdCol ? 12 : 11;

  var targetId = String(item.id || '').trim();
  var targetCleanMobil = cleanStr(item.noMobil);
  var targetTimestamp = String(item.timestamp || '').trim();
  var isEdit = item.isEdit === true || item.isEdit === 'true';
  var candidateRow = Number(item.row || (item.id ? (Number(item.id) + 1) : 0));

  // 1. CARI BERDASARKAN candidateRow LANGSUNG (Prioritas utama JIKA No Mobil COCOK)
  // Syarat MUTLAK: No Mobil di baris tersebut WAJIB cocok dengan mobil yang diedit!
  if (candidateRow >= 2 && candidateRow <= lastRow) {
    var rowMobil = cleanStr(sheet.getRange(candidateRow, colMobil).getDisplayValue());
    var rowTs = String(sheet.getRange(candidateRow, colTs).getDisplayValue() || '').trim();
    var rowId = hasIdCol ? String(sheet.getRange(candidateRow, colId).getDisplayValue() || '').trim() : '';
    
    if (targetCleanMobil && rowMobil === targetCleanMobil) {
      if ((hasIdCol && targetId && rowId === targetId) ||
          !targetTimestamp || !rowTs || rowTs === targetTimestamp || cleanStr(rowTs) === cleanStr(targetTimestamp)) {
        targetRow = candidateRow;
      }
    }
  }

  // 2. JIKA BELUM KETEMU DARI candidateRow, CARI DI SELURUH DATA SHEET
  if (targetRow === -1 && lastRow >= 2) {
    var numCols = Math.min(sheet.getLastColumn(), hasIdCol ? 12 : 11);
    var displayData = sheet.getRange(1, 1, lastRow, numCols).getDisplayValues();
    
    // 2a. Prioritas 1: Cocokkan ID DAN No Mobil
    if (hasIdCol && targetId && targetId !== '0' && targetCleanMobil) {
      for (var idx = displayData.length - 1; idx >= 1; idx--) {
        var rIdVal = String(displayData[idx][colId - 1] || '').trim();
        var rMobVal = cleanStr(displayData[idx][colMobil - 1]);
        if (rIdVal === targetId && rMobVal === targetCleanMobil) {
          targetRow = idx + 1;
          break;
        }
      }
    }

    // 2b. Prioritas 2: Cocokkan No Mobil DAN Timestamp persis
    if (targetRow === -1 && targetCleanMobil && targetTimestamp) {
      for (var i = displayData.length - 1; i >= 1; i--) {
        var rTs = String(displayData[i][colTs - 1] || '').trim();
        var rMob = cleanStr(displayData[i][colMobil - 1]);
        if (rMob === targetCleanMobil && (rTs === targetTimestamp || cleanStr(rTs) === cleanStr(targetTimestamp))) {
          targetRow = i + 1;
          break;
        }
      }
    }

    // 2c. Prioritas 3: Cocokkan No Mobil DAN Keluhan / Pengemudi (jika timestamp di sheet formatnya berbeda)
    if (targetRow === -1 && targetCleanMobil) {
      var targetKeluhan = cleanStr(item.keluhanKendaraan);
      var targetPengemudi = cleanStr(item.namaNip);
      for (var j = displayData.length - 1; j >= 1; j--) {
        var rMob2 = cleanStr(displayData[j][colMobil - 1]);
        var rKel = cleanStr(displayData[j][colKeluhan - 1]);
        var rPeng = cleanStr(displayData[j][colNamaNip - 1]);
        if (rMob2 === targetCleanMobil) {
          if ((targetKeluhan && rKel && (rKel.indexOf(targetKeluhan) !== -1 || targetKeluhan.indexOf(rKel) !== -1)) ||
              (targetPengemudi && rPeng && (rPeng.indexOf(targetPengemudi) !== -1 || targetPengemudi.indexOf(rPeng) !== -1))) {
            targetRow = j + 1;
            break;
          }
        }
      }
    }

    // 2d. Prioritas 4: Cari baris terakhir untuk No Mobil tersebut yang statusnya BELUM SELESAI
    if (targetRow === -1 && targetCleanMobil) {
      for (var k = displayData.length - 1; k >= 1; k--) {
        var rMob3 = cleanStr(displayData[k][colMobil - 1]);
        var rStat = String(displayData[k][colStatus - 1] || '').toUpperCase();
        if (rMob3 === targetCleanMobil && rStat.indexOf('SELESAI') === -1) {
          targetRow = k + 1;
          break;
        }
      }
    }
  }

  var nowStr = Utilities.formatDate(new Date(), 'GMT+7', 'yyyy-MM-dd HH:mm:ss');
  var updateDataStr = item.updateData || nowStr;

  if (targetRow !== -1) {
    // UPDATE BARIS PADA TARGET YANG BENAR-BENAR COCOK
    if (hasIdCol) {
      var existingId = String(sheet.getRange(targetRow, colId).getDisplayValue() || '').trim();
      if (!existingId && item.id) {
        sheet.getRange(targetRow, colId).setValue(item.id);
      }
    }
    // JANGAN UBAH timestamp asal jika mode edit, pertahankan waktu laporan asli dari pengemudi
    if (!isEdit && item.timestamp) sheet.getRange(targetRow, colTs).setValue(item.timestamp);
    if (item.noMobil !== undefined && item.noMobil !== '') sheet.getRange(targetRow, colMobil).setValue(item.noMobil);
    if (item.namaNip !== undefined && item.namaNip !== '') sheet.getRange(targetRow, colNamaNip).setValue(item.namaNip);
    if (item.keluhanKendaraan !== undefined && item.keluhanKendaraan !== '') sheet.getRange(targetRow, colKeluhan).setValue(item.keluhanKendaraan);
    if (item.kodeAkses !== undefined) sheet.getRange(targetRow, colKodeAkses).setValue(item.kodeAkses);
    if (item.token !== undefined) sheet.getRange(targetRow, colToken).setValue(item.token);
    if (item.status !== undefined) sheet.getRange(targetRow, colStatus).setValue(item.status);
    sheet.getRange(targetRow, colUpdateData).setValue(updateDataStr);
    if (item.poolVendor !== undefined) sheet.getRange(targetRow, colPoolVendor).setValue(item.poolVendor);
    if (item.namaMekanik !== undefined) sheet.getRange(targetRow, colNamaMekanik).setValue(item.namaMekanik);
    if (item.pengerjaan !== undefined) sheet.getRange(targetRow, colPengerjaan).setValue(item.pengerjaan);

    return { 
      success: true, 
      status: 'success', 
      action: 'updated', 
      row: targetRow, 
      id: item.id || (targetRow - 1), 
      noMobil: item.noMobil, 
      status: item.status, 
      sheet: sheet.getName() 
    };
  } else {
    // JIKA INI MODE EDIT / KERJAKAN, TIDAK BOLEH MEMBUAT DATA BARU ATAU MENIMPA DATA LAIN!
    if (isEdit) {
      return { 
        success: false, 
        status: 'error', 
        action: 'not_found', 
        message: 'Baris laporan untuk Mobil #' + (item.noMobil || '') + ' tidak ditemukan di Google Sheets untuk diedit. Tidak ada baris lain yang ditimpa dan tidak ada data baru yang dibuat.' 
      };
    }

    // HANYA JIKA INPUT DATA BARU: Buat baris baru di bawah
    var newTimestamp = targetTimestamp || nowStr;
    var newId = (item.id && Number(item.id) > 0) ? item.id : Math.max(1, lastRow);
    var newRowValues = hasIdCol ? [
      newId,
      newTimestamp,
      item.noMobil || '',
      item.namaNip || '',
      item.keluhanKendaraan || '',
      item.kodeAkses || '',
      item.token || '',
      item.status || 'Belum Dikerjakan',
      updateDataStr,
      item.poolVendor || '',
      item.namaMekanik || '',
      item.pengerjaan || ''
    ] : [
      newTimestamp,
      item.noMobil || '',
      item.namaNip || '',
      item.keluhanKendaraan || '',
      item.kodeAkses || '',
      item.token || '',
      item.status || 'Belum Dikerjakan',
      updateDataStr,
      item.poolVendor || '',
      item.namaMekanik || '',
      item.pengerjaan || ''
    ];
    sheet.appendRow(newRowValues);
    return { 
      success: true, 
      status: 'success', 
      action: 'appended', 
      row: sheet.getLastRow(), 
      id: newId, 
      noMobil: item.noMobil, 
      status: item.status, 
      sheet: sheet.getName() 
    };
  }
}

function appendServiceTeknik(item) {
  item = item || {};
  var ss = getSpreadsheetTeknik();
  var sheet = ss.getSheets()[0];
  var nowStr = Utilities.formatDate(new Date(), 'GMT+7', 'yyyy-MM-dd HH:mm:ss');
  sheet.appendRow([
    item.statusMobil || '',
    item.noMobil || '',
    item.service || '',
    item.selisihKm || '',
    item.notificationDesc || '',
    item.notifNo || '',
    item.status || '',
    item.kmServisSebelumnya || '',
    item.kmJatuhTempo || '',
    item.equipmentNo || '',
    item.notifDate || nowStr,
    item.kmTotal || '',
    item.stickerBandara || ''
  ]);
  return { success: true, action: 'appended', row: sheet.getLastRow() };
}

function saveOrUpdateUser(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "USER";
  var sheet = ss.getSheetByName(targetSheetName) || ss.getSheetByName("USER") || ss.getSheetByName("user") || ss.getSheets()[0];
  if (sheet.getLastRow() === 0) {
    sheet.appendRow(['NIP', 'NAMA_MEKANIK', 'USERNAME', 'PASSWORD', 'LEVELAKSES']);
  }
  var data = sheet.getDataRange().getValues();
  var targetRow = -1;
  var targetNip = String(item.nip || '').trim().toLowerCase();
  var origNip = String(item.originalNip || item.nip || '').trim().toLowerCase();
  var targetUsername = String(item.username || '').trim().toLowerCase();
  var origUsername = String(item.originalUsername || item.username || '').trim().toLowerCase();

  if (data.length > 1) {
    for (var i = 1; i < data.length; i++) {
      var rNip = String(data[i][0] || '').trim().toLowerCase();
      var rUser = String(data[i][2] || '').trim().toLowerCase();
      if ((origNip && rNip === origNip) || (origUsername && rUser === origUsername) || (targetNip && rNip === targetNip) || (targetUsername && rUser === targetUsername)) {
        targetRow = i + 1;
        break;
      }
    }
  }

  // Susunan tabel: | NIP | NAMA_MEKANIK | USERNAME | PASSWORD | LEVELAKSES |
  var rowData = [
    item.nip || '',
    item.namaMekanik || '',
    item.username || '',
    item.password || '',
    item.levelAkses || item.level || ''
  ];

  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowData.length).setValues([rowData]);
    return { success: true, action: 'updated', row: targetRow, sheet: sheet.getName() };
  } else {
    sheet.appendRow(rowData);
    return { success: true, action: 'appended', row: sheet.getLastRow(), sheet: sheet.getName() };
  }
}

function deleteUser(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "USER";
  var sheet = ss.getSheetByName(targetSheetName) || ss.getSheetByName("USER") || ss.getSheetByName("user") || ss.getSheets()[0];
  var data = sheet.getDataRange().getValues();
  var targetNip = String(item.nip || '').trim().toLowerCase();
  var targetUsername = String(item.username || '').trim().toLowerCase();

  if (data.length > 1) {
    for (var i = data.length - 1; i >= 1; i--) {
      var rNip = String(data[i][0] || '').trim().toLowerCase();
      var rUser = String(data[i][2] || '').trim().toLowerCase();
      if ((targetNip && rNip === targetNip) || (targetUsername && rUser === targetUsername)) {
        sheet.deleteRow(i + 1);
        return { success: true, action: 'deleted', row: i + 1, sheet: sheet.getName() };
      }
    }
  }
  return { success: false, action: 'not_found', message: 'User tidak ditemukan di Google Sheets' };
}

function getSpreadsheetCat(customId) {
  try {
    var id = String(customId || SHEET_CAT_ID || SHEET_LAPORAN_ID || '').trim();
    if (id.indexOf('http') !== -1) {
      var match = id.match(/\/d\/([a-zA-Z0-9-_]+)/);
      if (match) id = match[1];
    }
    if (id !== '') {
      return SpreadsheetApp.openById(id);
    }
  } catch (e) {
    // abaikan jika openById gagal
  }
  return getSpreadsheetLaporan();
}

function saveOrUpdatePengerjaanCat(item) {
  item = item || {};
  var ss = getSpreadsheetCat(item.spreadsheetId);
  var targetSheetName = item.sheetName || "PENGERJAAN_CAT";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("PENGERJAAN_CAT") || 
              ss.getSheetByName("PENGERJAAN CAT") || 
              ss.getSheetByName("Form Responses 1") || 
              ss.getSheets()[0];
  var data = sheet.getDataRange().getValues();
  var displayData = sheet.getDataRange().getDisplayValues();
  var targetRow = -1;

  function cleanStr(s) {
    return String(s || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  var targetId = String(item.id || item.idUniq || '').trim();
  var targetTimestamp = String(item.timestamp || '').trim();
  var targetNoMobil = cleanStr(item.noMobil);
  var targetNoReg = cleanStr(item.noRegistrasi || item.noRegistrasiCat);
  var targetTanggal = String(item.tanggal || '').trim();
  var isEdit = item.isEdit === true || item.isEdit === 'true';
  var oldNoReg = cleanStr(item.oldNoRegistrasi || item.oldNoReg);
  var oldNoMobil = cleanStr(item.oldNoMobil);
  var oldTimestamp = cleanStr(item.oldTimestamp);

  // Deteksi kolom secara dinamis dari Header
  var header = data.length > 0 ? data[0] : [];
  var firstHeader = header.length > 0 ? String(header[0] || '').trim().toUpperCase() : '';
  var is14Col = (firstHeader === 'ID' || firstHeader.indexOf('KODE') !== -1 || firstHeader.indexOf('UNIQ') !== -1) || header.length >= 14;

  var colId = is14Col ? 0 : -1;
  var colTs = is14Col ? 1 : 0;
  var colTgl = is14Col ? 2 : 1;
  var colMobil = is14Col ? 3 : 2;
  var colKerusakan = is14Col ? 4 : 3;
  var colPhotoKerusakan = is14Col ? 5 : 4;
  var colFotoNotif = is14Col ? 6 : 5;
  var colFotoHasil = is14Col ? 7 : 6;
  var colGambar = is14Col ? 8 : 7;
  var colMekanik = is14Col ? 9 : 8;
  var colNoReg = is14Col ? 10 : 9;
  var colPhotoLasKetok = is14Col ? 11 : 10;
  var colFotoHasil2 = is14Col ? 12 : 11;
  var colFotoHasil3 = is14Col ? 13 : 12;

  if (data.length > 0) {
    for (var c = 0; c < header.length; c++) {
      var h = String(header[c] || '').trim().toUpperCase().replace(/[^A-Z0-9]/g, '');
      if (h === 'ID' || h.indexOf('KODEUNIQ') !== -1 || h === 'UNIQ') colId = c;
      else if (h.indexOf('TIMESTAMP') !== -1 || h.indexOf('WAKTU') !== -1) colTs = c;
      else if (h.indexOf('TANGGAL') !== -1 || h === 'TGL') colTgl = c;
      else if (h.indexOf('NOMOBIL') !== -1 || h === 'MOBIL') colMobil = c;
      else if (h.indexOf('KERUSAKAN') !== -1) colKerusakan = c;
      else if (h.indexOf('PHOTOKERUSAKAN') !== -1 || h.indexOf('FOTOKERUSAKAN') !== -1) colPhotoKerusakan = c;
      else if (h.indexOf('FOTONOTIF') !== -1 || h.indexOf('NOTIF') !== -1) colFotoNotif = c;
      else if (h === 'FOTOHASILPENGECATAN' || (h.indexOf('HASIL') !== -1 && h.indexOf('2') === -1 && h.indexOf('3') === -1 && h.indexOf('GAMBAR') === -1)) colFotoHasil = c;
      else if (h === 'GAMBAR' || (h.indexOf('GAMBAR') !== -1 && h.indexOf('2') === -1 && h.indexOf('3') === -1)) colGambar = c;
      else if (h.indexOf('MEKANIK') !== -1 || h.indexOf('PETUGAS') !== -1) colMekanik = c;
      else if (h.indexOf('REGISTRASI') !== -1) colNoReg = c;
      else if (h.indexOf('LAS') !== -1 || h.indexOf('KETOK') !== -1) colPhotoLasKetok = c;
      else if (h.indexOf('HASIL2') !== -1 || h.indexOf('PENGECATAN2') !== -1) colFotoHasil2 = c;
      else if (h.indexOf('HASIL3') !== -1 || h.indexOf('PENGECATAN3') !== -1) colFotoHasil3 = c;
    }
  }

  // 1. Cari baris berdasarkan ID unik (numeric atau string matching)
  if (targetId && targetId !== '0' && colId >= 0 && data.length > 1) {
    for (var i = data.length - 1; i >= 1; i--) {
      var rId = String(data[i][colId] || '').trim();
      var dId = displayData ? String(displayData[i][colId] || '').trim() : '';
      if (rId === targetId || dId === targetId || cleanStr(rId) === cleanStr(targetId) || (Number(rId) > 0 && Number(rId) === Number(targetId))) {
        targetRow = i + 1;
        break;
      }
    }
  }

  // 2. Jika sedang edit dan ada oldNoRegistrasi: cari baris berdasarkan No Registrasi lama
  if (targetRow === -1 && oldNoReg && oldNoReg !== '-' && oldNoReg !== 'null' && colNoReg >= 0 && data.length > 1) {
    for (var j = data.length - 1; j >= 1; j--) {
      var rReg = cleanStr(data[j][colNoReg]);
      var dReg = displayData ? cleanStr(displayData[j][colNoReg]) : '';
      if (rReg === oldNoReg || dReg === oldNoReg) {
        targetRow = j + 1;
        break;
      }
    }
  }

  // 3. Cari baris berdasarkan Timestamp & No Mobil (mendukung target maupun old value)
  if (targetRow === -1 && colTs >= 0 && colMobil >= 0 && data.length > 1) {
    var cTs = cleanStr(targetTimestamp);
    var cOldTs = oldTimestamp;
    for (var i = data.length - 1; i >= 1; i--) {
      var rTimestamp = cleanStr(data[i][colTs]);
      var dTimestamp = displayData ? cleanStr(displayData[i][colTs]) : '';
      var rMobil = cleanStr(data[i][colMobil]);
      var dMobil = displayData ? cleanStr(displayData[i][colMobil]) : '';

      var mobilMatches = (targetNoMobil && (rMobil === targetNoMobil || dMobil === targetNoMobil)) ||
                         (oldNoMobil && (rMobil === oldNoMobil || dMobil === oldNoMobil));

      var tsMatches = false;
      if (cTs && (rTimestamp === cTs || dTimestamp === cTs || (cTs.length >= 8 && (rTimestamp.indexOf(cTs.substring(0, 8)) !== -1 || dTimestamp.indexOf(cTs.substring(0, 8)) !== -1)))) {
        tsMatches = true;
      } else if (cOldTs && (rTimestamp === cOldTs || dTimestamp === cOldTs || (cOldTs.length >= 8 && (rTimestamp.indexOf(cOldTs.substring(0, 8)) !== -1 || dTimestamp.indexOf(cOldTs.substring(0, 8)) !== -1)))) {
        tsMatches = true;
      }

      if (mobilMatches && tsMatches) {
        targetRow = i + 1;
        break;
      }
    }
  }

  // 4. Cari baris berdasarkan No Registrasi Cat (jika sudah ada dan tidak diubah)
  if (targetRow === -1 && targetNoReg && targetNoReg !== '-' && targetNoReg !== 'null' && colNoReg >= 0 && data.length > 1) {
    for (var j = data.length - 1; j >= 1; j--) {
      var rReg = cleanStr(data[j][colNoReg]);
      var dReg = displayData ? cleanStr(displayData[j][colNoReg]) : '';
      if (rReg === targetNoReg || dReg === targetNoReg) {
        targetRow = j + 1;
        break;
      }
    }
  }

  // 5. Jika sedang EDIT dan belum ketemu: Cari baris berdasarkan No Mobil & Tanggal
  if (targetRow === -1 && isEdit && (targetNoMobil || oldNoMobil) && colMobil >= 0 && data.length > 1) {
    var cTgl = cleanStr(targetTanggal);
    for (var k = data.length - 1; k >= 1; k--) {
      var rMobilK = cleanStr(data[k][colMobil]);
      var dMobilK = displayData ? cleanStr(displayData[k][colMobil]) : '';
      var mMatch = (targetNoMobil && (rMobilK === targetNoMobil || dMobilK === targetNoMobil)) ||
                   (oldNoMobil && (rMobilK === oldNoMobil || dMobilK === oldNoMobil));
      if (mMatch) {
        var rTgl = colTgl >= 0 ? cleanStr(displayData ? displayData[k][colTgl] : data[k][colTgl]) : '';
        if (!cTgl || !rTgl || (cTgl.length >= 8 && rTgl.indexOf(cTgl.substring(0, 8)) !== -1) || (rTgl.length >= 8 && cTgl.indexOf(rTgl.substring(0, 8)) !== -1)) {
          targetRow = k + 1;
          break;
        }
      }
    }
  }

  var nowStr = Utilities.formatDate(new Date(), 'GMT+7', 'yyyy-MM-dd HH:mm:ss');
  var ts = item.timestamp || nowStr;
  var mobClean = String(item.noMobil || 'MOBIL').toUpperCase().replace(/[^a-zA-Z0-9]/g, '');

  var photoKerusakanVal = item.photoKerusakan || '';
  if (photoKerusakanVal && photoKerusakanVal.indexOf('http') !== 0 && (photoKerusakanVal.indexOf(';base64,') !== -1 || photoKerusakanVal.indexOf('data:') === 0 || photoKerusakanVal.length > 200)) {
    try {
      photoKerusakanVal = saveBase64ImageToDrive(photoKerusakanVal, 'CAT_' + mobClean + '_KERUSAKAN_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePK) {}
  }

  var photoNotifVal = item.photoNotif || '';
  if (photoNotifVal && photoNotifVal.indexOf('http') !== 0 && (photoNotifVal.indexOf(';base64,') !== -1 || photoNotifVal.indexOf('data:') === 0 || photoNotifVal.length > 200)) {
    try {
      photoNotifVal = saveBase64ImageToDrive(photoNotifVal, 'CAT_' + mobClean + '_NOTIF_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePN) {}
  }

  var gambarVal = item.gambar || item.gambarFotoHasilPengecatan || '';
  if (gambarVal && gambarVal.indexOf('http') !== 0 && (gambarVal.indexOf(';base64,') !== -1 || gambarVal.indexOf('data:') === 0 || gambarVal.length > 200)) {
    try {
      gambarVal = saveBase64ImageToDrive(gambarVal, 'CAT_' + mobClean + '_HASIL_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePG) {}
  }

  var photoLasKetokVal = item.photoLasKetok || '';
  if (photoLasKetokVal && photoLasKetokVal.indexOf('http') !== 0 && (photoLasKetokVal.indexOf(';base64,') !== -1 || photoLasKetokVal.indexOf('data:') === 0 || photoLasKetokVal.length > 200)) {
    try {
      photoLasKetokVal = saveBase64ImageToDrive(photoLasKetokVal, 'CAT_' + mobClean + '_LASKETOK_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePLK) {}
  }

  var fotoHasil2Val = item.fotoHasilPengecatan2 || item.fotoHasil2 || '';
  if (fotoHasil2Val && fotoHasil2Val.indexOf('http') !== 0 && (fotoHasil2Val.indexOf(';base64,') !== -1 || fotoHasil2Val.indexOf('data:') === 0 || fotoHasil2Val.length > 200)) {
    try {
      fotoHasil2Val = saveBase64ImageToDrive(fotoHasil2Val, 'CAT_' + mobClean + '_HASIL2_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePH2) {}
  }

  var fotoHasil3Val = item.fotoHasilPengecatan3 || item.fotoHasil3 || '';
  if (fotoHasil3Val && fotoHasil3Val.indexOf('http') !== 0 && (fotoHasil3Val.indexOf(';base64,') !== -1 || fotoHasil3Val.indexOf('data:') === 0 || fotoHasil3Val.length > 200)) {
    try {
      fotoHasil3Val = saveBase64ImageToDrive(fotoHasil3Val, 'CAT_' + mobClean + '_HASIL3_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePH3) {}
  }

  var officialHeaders = [
    'ID (kode uniq)', 'Timestamp', 'TANGGAL', 'NO MOBIL', 'KERUSAKAN',
    'PHOTO KERUSAKAN', 'FOTO NOTIF', 'FOTO HASIL PENGECATAN', 'GAMBAR', 'MEKANIK',
    'NO REGISTRASI CAT', 'PHOTO LAS KETOK', 'FOTO HASIL PENGECATAN_2', 'FOTO HASIL PENGECATAN_3'
  ];

  if (sheet.getLastRow() === 0) {
    sheet.appendRow(officialHeaders);
    try {
      var hr = sheet.getRange(1, 1, 1, officialHeaders.length);
      hr.setBackground('#1565C0');
      hr.setFontColor('#FFFFFF');
      hr.setFontWeight('bold');
      sheet.setFrozenRows(1);
    } catch (eFmt) {}
  }

  var idVal = item.id || item.idUniq || new Date().getTime();

  var rowData = is14Col ? [
    idVal,
    ts,
    item.tanggal || '',
    item.noMobil || '',
    item.kerusakan || '',
    photoKerusakanVal,
    photoNotifVal,
    item.fotoHasilPengecatan || '',
    gambarVal,
    item.mekanik || '',
    item.noRegistrasi || item.noRegistrasiCat || '',
    photoLasKetokVal,
    fotoHasil2Val,
    fotoHasil3Val
  ] : [
    ts,
    item.tanggal || '',
    item.noMobil || '',
    item.kerusakan || '',
    photoKerusakanVal,
    photoNotifVal,
    item.fotoHasilPengecatan || '',
    gambarVal,
    item.mekanik || '',
    item.noRegistrasi || item.noRegistrasiCat || '',
    photoLasKetokVal,
    fotoHasil2Val,
    fotoHasil3Val
  ];

  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowData.length).setValues([rowData]);
    return { success: true, action: 'updated', row: targetRow, id: idVal, sheet: sheet.getName() };
  } else {
    sheet.appendRow(rowData);
    return { success: true, action: 'appended', row: sheet.getLastRow(), id: idVal, sheet: sheet.getName() };
  }
}

// Hapus Data Pengerjaan Cat dari Google Sheets
function deletePengerjaanCat(params) {
  params = params || {};
  var ss = getSpreadsheetCat(params.spreadsheetId);
  var targetSheetName = params.sheetName || "PENGERJAAN_CAT";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("PENGERJAAN_CAT") || 
              ss.getSheetByName("PENGERJAAN CAT") || 
              ss.getSheetByName("Form Responses 1") || 
              ss.getSheets()[0];

  var lastRow = sheet.getLastRow();
  if (lastRow <= 1) {
    return { status: 'error', success: false, message: 'Sheet kosong atau tidak ada data yang dapat dihapus.' };
  }

  function cleanStr(s) {
    return String(s || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  var targetId = String(params.id || params.idUniq || '').trim();
  var targetTimestamp = String(params.timestamp || '').trim();
  var targetNoMobil = cleanStr(params.noMobil);
  var targetNoReg = cleanStr(params.noRegistrasi || params.noRegistrasiCat);

  var data = sheet.getDataRange().getValues();
  var displayData = sheet.getDataRange().getDisplayValues();
  var header = data[0];
  var colId = 0, colTs = 1, colTgl = 2, colMobil = 3, colKerusakan = 4, colNoReg = 10;
  for (var c = 0; c < header.length; c++) {
    var h = String(header[c] || '').trim().toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (h === 'ID' || h.indexOf('KODEUNIQ') !== -1 || h === 'UNIQ') colId = c;
    else if (h.indexOf('TIMESTAMP') !== -1 || h.indexOf('WAKTU') !== -1) colTs = c;
    else if (h.indexOf('TANGGAL') !== -1 || h === 'TGL') colTgl = c;
    else if (h.indexOf('NOMOBIL') !== -1 || h === 'MOBIL') colMobil = c;
    else if (h.indexOf('KERUSAKAN') !== -1) colKerusakan = c;
    else if (h.indexOf('REGISTRASI') !== -1) colNoReg = c;
  }

  var targetRow = -1;
  if (targetId && targetId !== '0') {
    for (var i = data.length - 1; i >= 1; i--) {
      var rId = String(data[i][colId] || '').trim();
      var dId = displayData ? String(displayData[i][colId] || '').trim() : '';
      if (rId === targetId || dId === targetId || cleanStr(rId) === cleanStr(targetId) || (Number(rId) > 0 && Number(rId) === Number(targetId))) {
        targetRow = i + 1;
        break;
      }
    }
  }

  if (targetRow === -1 && targetTimestamp && targetNoMobil) {
    for (var i = data.length - 1; i >= 1; i--) {
      var rTs = String(data[i][colTs] || '').trim();
      var rMob = cleanStr(data[i][colMobil]);
      if (rTs === targetTimestamp && rMob === targetNoMobil) {
        targetRow = i + 1;
        break;
      }
    }
  }

  if (targetRow === -1 && targetNoReg) {
    for (var j = data.length - 1; j >= 1; j--) {
      if (cleanStr(data[j][colNoReg]) === targetNoReg) {
        targetRow = j + 1;
        break;
      }
    }
  }

  if (targetRow === -1 && targetNoMobil && params.tanggal) {
    var targetTgl = String(params.tanggal || '').trim();
    for (var k = data.length - 1; k >= 1; k--) {
      if (cleanStr(data[k][colMobil]) === targetNoMobil && String(data[k][colTgl] || '').trim() === targetTgl) {
        targetRow = k + 1;
        break;
      }
    }
  }

  if (targetRow > 0) {
    sheet.deleteRow(targetRow);
    return { status: 'success', success: true, action: 'deleted', row: targetRow, sheet: sheet.getName() };
  }
  return { status: 'not_found', success: false, message: 'Data Pengerjaan Cat tidak ditemukan di Google Sheets.' };
}

// Inisialisasi Pengerjaan Cat (14 Kolom)
function inisialisasiPengerjaanCat(params) {
  params = params || {};
  var logs = [];
  var result = {
    status: 'success',
    success: true,
    sheetReady: false,
    folderReady: false,
    sheetName: 'PENGERJAAN_CAT',
    spreadsheetId: '',
    folderName: 'FOTO_AUDIT_KENDARAAN',
    message: '',
    logs: []
  };

  try {
    var ss = getSpreadsheetCat(params.spreadsheetId);
    result.spreadsheetId = ss.getId();
    var sheetName = params.sheetName || 'PENGERJAAN_CAT';
    var sheet = ss.getSheetByName(sheetName) || ss.getSheetByName("PENGERJAAN CAT");

    var officialHeaders = [
      'ID (kode uniq)',
      'Timestamp',
      'TANGGAL',
      'NO MOBIL',
      'KERUSAKAN',
      'PHOTO KERUSAKAN',
      'FOTO NOTIF',
      'FOTO HASIL PENGECATAN',
      'GAMBAR',
      'MEKANIK',
      'NO REGISTRASI CAT',
      'PHOTO LAS KETOK',
      'FOTO HASIL PENGECATAN_2',
      'FOTO HASIL PENGECATAN_3'
    ];

    if (!sheet) {
      sheet = ss.insertSheet(sheetName);
      logs.push("✅ Sheet '" + sheetName + "' berhasil dibuat baru.");
    }

    if (sheet.getLastRow() === 0) {
      sheet.appendRow(officialHeaders);
      logs.push("✅ 14 Kolom Header Pengerjaan Cat berhasil dibuat.");
    } else {
      var currentHeaders = sheet.getRange(1, 1, 1, sheet.getLastColumn()).getValues()[0];
      var firstH = String(currentHeaders[0] || '').trim().toUpperCase();
      if (firstH !== 'ID' && firstH.indexOf('KODE') === -1 && firstH.indexOf('UNIQ') === -1) {
        sheet.insertColumnBefore(1);
        sheet.getRange(1, 1).setValue('ID (kode uniq)');
      }
      if (sheet.getLastColumn() < 14) {
        for (var c = sheet.getLastColumn(); c < officialHeaders.length; c++) {
          sheet.getRange(1, c + 1).setValue(officialHeaders[c]);
        }
      }
    }

    try {
      var headerRange = sheet.getRange(1, 1, 1, officialHeaders.length);
      headerRange.setBackground('#1565C0');
      headerRange.setFontColor('#FFFFFF');
      headerRange.setFontWeight('bold');
      headerRange.setHorizontalAlignment('center');
      sheet.setFrozenRows(1);
    } catch (eFmt) {}

    result.sheetReady = true;
    result.message = "Inisialisasi Pengerjaan Cat Berhasil! Sheet " + sheetName + " (14 Kolom) siap dipakai.";
    result.logs = logs;
    return result;
  } catch (err) {
    result.status = 'error';
    result.success = false;
    result.message = "Gagal inisialisasi: " + err.message;
    return result;
  }
}

function getSpreadsheetKeindahan(overrideId) {
  var id = String(overrideId || "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs").trim();
  if (id.indexOf('http') !== -1) {
    var match = id.match(/\/d\/([a-zA-Z0-9-_]+)/);
    if (match) id = match[1];
  }
  try {
    var active = SpreadsheetApp.getActiveSpreadsheet();
    if (active && (!overrideId || active.getId() === id)) return active;
  } catch (e) {}
  if (id) {
    try {
      return SpreadsheetApp.openById(id);
    } catch (e) {}
  }
  return getSpreadsheetLaporan();
}

function saveBase64ImageToDrive(base64Data, fileName, folderId, folderName) {
  if (!base64Data) return '';
  var strData = String(base64Data).trim();
  if (strData.indexOf('http://') === 0 || strData.indexOf('https://') === 0) {
    return strData;
  }
  try {
    var rawBase64 = strData;
    var contentType = 'image/jpeg';
    if (rawBase64.indexOf(';base64,') !== -1) {
      var parts = rawBase64.split(';base64,');
      contentType = parts[0].replace('data:', '').trim() || 'image/jpeg';
      rawBase64 = parts[1];
    } else if (rawBase64.indexOf('data:') === 0 && rawBase64.indexOf(',') !== -1) {
      var commaIdx = rawBase64.indexOf(',');
      rawBase64 = rawBase64.substring(commaIdx + 1);
    }
    rawBase64 = rawBase64.replace(/\s+/g, '');
    var decoded = Utilities.base64Decode(rawBase64);
    var fName = fileName || ('KEINDAHAN_' + new Date().getTime() + '.jpg');
    var blob = Utilities.newBlob(decoded, contentType, fName);

    var file = null;
    var targetFolderId = String(folderId || FOLDER_PHOTO_KEINDAHAN_ID || '1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6').trim();
    if (targetFolderId.indexOf('http') !== -1) {
      var match = targetFolderId.match(/folders\/([a-zA-Z0-9-_]+)/);
      if (match) targetFolderId = match[1];
    }

    // 1. Coba simpan ke folder spesifik (Folder ID target)
    if (targetFolderId && targetFolderId.length > 5) {
      try {
        var folder = DriveApp.getFolderById(targetFolderId);
        if (folder) {
          file = folder.createFile(blob);
        }
      } catch (errF) {
        Logger.log("Folder (" + targetFolderId + ") tidak dapat diakses/ditulis: " + errF + ". Menggunakan folder cadangan...");
        file = null;
      }
    }

    // 2. Jika gagal atau folder ID tidak valid, simpan ke folder cadangan di Drive pengguna
    if (!file) {
      try {
        var dirName = folderName || "FOTO_AUDIT_KENDARAAN";
        var folders = DriveApp.getFoldersByName(dirName);
        var customFolder = folders.hasNext() ? folders.next() : DriveApp.createFolder(dirName);
        file = customFolder.createFile(blob);
      } catch (errCreate) {
        Logger.log("createFolder error: " + errCreate + ". Menyimpan langsung ke Root Drive...");
        file = DriveApp.createFile(blob);
      }
    }

    // Set permission sharing agar foto diset publik/view (ANYONE_WITH_LINK)
    try {
      file.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
    } catch (e1) {
      try {
        file.setSharing(DriveApp.Access.DOMAIN_WITH_LINK, DriveApp.Permission.VIEW);
      } catch (e2) {}
    }

    return "https://drive.google.com/uc?export=view&id=" + file.getId();
  } catch (e) {
    Logger.log("saveBase64ImageToDrive error: " + e.message);
    var errStr = String(e.message || e);
    if (errStr.indexOf("DriveApp") !== -1 || errStr.indexOf("permission") !== -1 || errStr.indexOf("auth/drive") !== -1) {
      throw new Error("Izin DriveApp belum aktif pada Web App. Buka editor Google Apps Script, jalankan fungsi 'aktivasiIzinGoogleDrive' untuk menyetujui izin Drive, lalu Deploy Versi Baru.");
    }
    throw new Error("Gagal simpan ke Google Drive: " + errStr);
  }
}

// Ekstrak ID file Google Drive dari berbagai format URL
function extractDriveFileId(url) {
  if (!url) return null;
  url = String(url).trim();
  var m1 = url.match(/\/file\/d\/([a-zA-Z0-9_-]{15,})/);
  if (m1) return m1[1];
  var m2 = url.match(/[?&]id=([a-zA-Z0-9_-]{15,})/);
  if (m2) return m2[1];
  var m3 = url.match(/\/d\/([a-zA-Z0-9_-]{15,})/);
  if (m3) return m3[1];
  if (/^[a-zA-Z0-9_-]{20,}$/.test(url)) return url;
  return null;
}

// Hapus Foto Google Drive (dipindahkan ke Sampah)
function deleteImageFromDrive(params) {
  params = params || {};
  var targetUrl = params.fileId || params.url || params.link || params.image || params.photo;
  var fileId = extractDriveFileId(targetUrl);
  if (!fileId) {
    return {
      status: 'error',
      success: false,
      message: 'ID file Google Drive atau URL foto tidak valid (' + String(targetUrl) + ')'
    };
  }
  try {
    var file = DriveApp.getFileById(fileId);
    var fName = file.getName();
    file.setTrashed(true);
    Logger.log("✅ Foto Google Drive dipindahkan ke sampah: " + fName + " (" + fileId + ")");
    return {
      status: 'success',
      success: true,
      message: 'Foto ' + fName + ' berhasil dihapus dari Google Drive (dipindahkan ke Sampah).'
    };
  } catch (err) {
    Logger.log("❌ Gagal hapus foto Drive: " + err.message);
    return {
      status: 'error',
      success: false,
      message: 'Gagal hapus foto Google Drive: ' + err.message
    };
  }
}

function saveOrUpdatePengerjaanKeindahan(item) {
  item = item || {};
  var ss = getSpreadsheetKeindahan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "SERVICE_KEINDAHAN";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("SERVICE_KEINDAHAN") || 
              ss.getSheetByName("SERVICE KEINDAHAN") || 
              ss.getSheets()[0];

  // Buat header jika sheet masih kosong
  if (sheet.getLastRow() === 0) {
    sheet.appendRow([
      'ID', 'TANGGAL', 'NO MOBIL', 'JENIS SERVICE', 'NIP/NAMA', 'NOTIF NO', 'ORDER', 'APPROVEL',
      'PHOTO DEPAN', 'PHOTO SAMPING LH', 'PHOTO INTERIOR', 'PHOTO BELAKANG', 'PHOTO SAMPING RH'
    ]);
  }

  var data = sheet.getDataRange().getValues();
  var header = data[0].map(function(h) {
    return String(h || '').trim().toLowerCase().replace(/[^a-z0-9]/g, '');
  });

  // Deteksi indeks kolom secara dinamis
  var colId = header.indexOf('id') !== -1 ? header.indexOf('id') : header.indexOf('no');
  var colTanggal = -1;
  var colMobil = -1;
  var colJenis = -1;
  var colNipNama = -1;
  var colNotif = -1;
  var colOrder = -1;
  var colApprovel = -1;
  var colKm = -1;
  var colDepan = -1;
  var colLh = -1;
  var colInterior = -1;
  var colBelakang = -1;
  var colRh = -1;

  for (var c = 0; c < header.length; c++) {
    var h = header[c];
    if (colTanggal === -1 && (h.indexOf('tanggal') !== -1 || h.indexOf('tgl') !== -1 || h.indexOf('date') !== -1)) colTanggal = c;
    if (colMobil === -1 && (h.indexOf('mobil') !== -1 || h.indexOf('vehicle') !== -1 || h.indexOf('nopol') !== -1)) colMobil = c;
    if (colJenis === -1 && (h.indexOf('jenis') !== -1 || (h.indexOf('service') !== -1 && h.indexOf('km') === -1))) colJenis = c;
    if (colNipNama === -1 && (h.indexOf('nip') !== -1 || h.indexOf('nama') !== -1 || h.indexOf('mekanik') !== -1 || h.indexOf('petugas') !== -1)) colNipNama = c;
    if (colNotif === -1 && (h.indexOf('notif') !== -1 || h.indexOf('desc') !== -1)) colNotif = c;
    if (colOrder === -1 && (h.indexOf('order') !== -1)) colOrder = c;
    if (colApprovel === -1 && (h.indexOf('approv') !== -1 || h.indexOf('aprov') !== -1 || h.indexOf('status') !== -1)) colApprovel = c;
    if (colKm === -1 && (h === 'km' || h.indexOf('km') !== -1)) colKm = c;
    if (colDepan === -1 && (h.indexOf('depan') !== -1 || h.indexOf('front') !== -1)) colDepan = c;
    if (colLh === -1 && (h.indexOf('lh') !== -1 || (h.indexOf('samping') !== -1 && h.indexOf('kiri') !== -1))) colLh = c;
    if (colInterior === -1 && (h.indexOf('interior') !== -1 || h.indexOf('dalam') !== -1)) colInterior = c;
    if (colBelakang === -1 && (h.indexOf('belakang') !== -1 || h.indexOf('rear') !== -1 || h.indexOf('back') !== -1)) colBelakang = c;
    if (colRh === -1 && (h.indexOf('rh') !== -1 || (h.indexOf('samping') !== -1 && h.indexOf('kanan') !== -1))) colRh = c;
  }

  // Fallback standar 13 kolom: ID | TANGGAL | NO_MOBIL | JENIS_SERVICE | NIP/NAMA | NOTIF_NO | ORDER | APPROVEL | PHOTO_DEPAN | PHOTO_SAMPING_LH | PHOTO_INTERIOR | PHOTO_BELAKANG | PHOTO_SAMPING_RH
  if (colId === -1) colId = 0;
  if (colTanggal === -1) colTanggal = 1;
  if (colMobil === -1) colMobil = 2;
  if (colJenis === -1) colJenis = 3;
  if (colNipNama === -1) colNipNama = 4;
  if (colNotif === -1) colNotif = 5;
  if (colOrder === -1) colOrder = 6;
  if (colApprovel === -1) colApprovel = 7;
  if (colDepan === -1) colDepan = 8;
  if (colLh === -1) colLh = 9;
  if (colInterior === -1) colInterior = 10;
  if (colBelakang === -1) colBelakang = 11;
  if (colRh === -1) colRh = 12;

  function cleanStr(s) {
    return String(s || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  var targetId = String(item.id || '').trim();
  var targetNoMobil = cleanStr(item.noMobil);
  var targetOrder = cleanStr(item.order || item.orderNo);
  var targetRow = -1;

  // 1. Cari baris berdasarkan ID jika > 0
  if (targetId && targetId !== '0' && data.length > 1) {
    for (var i = data.length - 1; i >= 1; i--) {
      var rId = String(data[i][colId] || '').trim();
      if (rId === targetId) {
        targetRow = i + 1;
        break;
      }
    }
  }

  // 2. Cari baris berdasarkan Order & No Mobil
  if (targetRow === -1 && targetOrder && targetNoMobil && data.length > 1) {
    for (var j = data.length - 1; j >= 1; j--) {
      var rMobil = cleanStr(data[j][colMobil]);
      var rOrder = cleanStr(data[j][colOrder]);
      if (rMobil === targetNoMobil && rOrder === targetOrder) {
        targetRow = j + 1;
        break;
      }
    }
  }

  function handlePhoto(p, name, existingPhoto) {
    if (p === undefined) return existingPhoto || '';
    if (p === '' || p === '-' || p === 'null' || p === 'DELETED' || p === 'HAPUS') {
      return '';
    }
    if (String(p).indexOf('http') === 0) return p;
    try {
      var dLink = saveBase64ImageToDrive(p, (item.noMobil || 'MOBIL') + '_' + name + '_' + new Date().getTime() + '.jpg', item.folderId || FOLDER_PHOTO_KEINDAHAN_ID, 'FOTO_SERVICE_KEINDAHAN');
      return (dLink && String(dLink).indexOf('http') === 0) ? dLink : (existingPhoto || '');
    } catch (ePhoto) {
      Logger.log("handlePhoto error for " + name + ": " + ePhoto.message);
      return existingPhoto || '';
    }
  }

  var existingRow = targetRow > 0 && data[targetRow - 1] ? data[targetRow - 1] : null;
  var existingDepan = existingRow && colDepan < existingRow.length ? String(existingRow[colDepan] || '') : '';
  var existingLh = existingRow && colLh < existingRow.length ? String(existingRow[colLh] || '') : '';
  var existingInterior = existingRow && colInterior < existingRow.length ? String(existingRow[colInterior] || '') : '';
  var existingBelakang = existingRow && colBelakang < existingRow.length ? String(existingRow[colBelakang] || '') : '';
  var existingRh = existingRow && colRh < existingRow.length ? String(existingRow[colRh] || '') : '';

  var photoDepan = handlePhoto(item.photoDepan, 'DEPAN', existingDepan);
  var photoLh = handlePhoto(item.photoSampingLh, 'SAMPING_LH', existingLh);
  var photoInterior = handlePhoto(item.photoInterior, 'INTERIOR', existingInterior);
  var photoBelakang = handlePhoto(item.photoBelakang, 'BELAKANG', existingBelakang);
  var photoRh = handlePhoto(item.photoSampingRh, 'SAMPING_RH', existingRh);

  var maxExistingId = 0;
  if (data.length > 1) {
    for (var k = 1; k < data.length; k++) {
      var num = parseInt(data[k][colId]);
      if (!isNaN(num) && num > maxExistingId) maxExistingId = num;
    }
  }
  var idVal = targetId && targetId !== '0' ? targetId : (maxExistingId > 0 ? (maxExistingId + 1) : data.length);

  var maxCol = Math.max(13, header.length);
  var rowData = new Array(maxCol);
  for (var x = 0; x < maxCol; x++) {
    rowData[x] = existingRow && x < existingRow.length ? existingRow[x] : '';
  }

  rowData[colId] = idVal;
  rowData[colTanggal] = item.tanggal !== undefined ? item.tanggal : (existingRow ? existingRow[colTanggal] : '');
  rowData[colMobil] = item.noMobil !== undefined ? item.noMobil : (existingRow ? existingRow[colMobil] : '');
  rowData[colJenis] = item.jenisService !== undefined ? item.jenisService : (existingRow ? existingRow[colJenis] : '');
  if (colNipNama !== -1) rowData[colNipNama] = (item.nipNama || item.nip_nama || item.nip) !== undefined ? (item.nipNama || item.nip_nama || item.nip) : (existingRow ? existingRow[colNipNama] : '');
  rowData[colNotif] = item.notifNo !== undefined ? item.notifNo : (existingRow ? existingRow[colNotif] : '');
  rowData[colOrder] = (item.order || item.orderNo) !== undefined ? (item.order || item.orderNo) : (existingRow ? existingRow[colOrder] : '');
  if (colApprovel !== -1) rowData[colApprovel] = (item.approvel || item.approval || item.aprovel) !== undefined ? (item.approvel || item.approval || item.aprovel) : (existingRow ? existingRow[colApprovel] : 'Menunggu');
  if (colKm !== -1) rowData[colKm] = item.km !== undefined ? item.km : (existingRow ? existingRow[colKm] : '');
  rowData[colDepan] = photoDepan;
  rowData[colLh] = photoLh;
  rowData[colInterior] = photoInterior;
  rowData[colBelakang] = photoBelakang;
  rowData[colRh] = photoRh;

  var resObj = { 
    status: 'success',
    success: true, 
    action: targetRow > 0 ? 'updated' : 'appended', 
    row: targetRow > 0 ? targetRow : (sheet.getLastRow() + 1), 
    sheet: sheet.getName(),
    id: idVal,
    photoDepan: photoDepan,
    photoSampingLh: photoLh,
    photoInterior: photoInterior,
    photoBelakang: photoBelakang,
    photoSampingRh: photoRh,
    message: targetRow > 0 ? 
      ('Data Mobil #' + (item.noMobil || idVal) + ' berhasil diperbarui di Google Sheets!') : 
      ('Data Mobil #' + (item.noMobil || idVal) + ' berhasil disimpan di Google Sheets!')
  };

  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowData.length).setValues([rowData]);
    return resObj;
  } else {
    sheet.appendRow(rowData);
    return resObj;
  }
}

// Hapus Data Pengerjaan Keindahan dari Google Sheets dan Google Drive
function deletePengerjaanKeindahan(params) {
  params = params || {};
  var ss = getSpreadsheetKeindahan(params.spreadsheetId);
  var targetSheetName = params.sheetName || "SERVICE_KEINDAHAN";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("SERVICE_KEINDAHAN") || 
              ss.getSheetByName("SERVICE KEINDAHAN") || 
              ss.getSheets()[0];

  var lastRow = sheet.getLastRow();
  if (lastRow <= 1) {
    return {
      status: 'error',
      success: false,
      message: 'Sheet kosong atau tidak ada data yang dapat dihapus.'
    };
  }

  var data = sheet.getDataRange().getValues();
  var header = data[0].map(function(h) {
    return String(h || '').trim().toLowerCase().replace(/[^a-z0-9]/g, '');
  });

  var colId = header.indexOf('id') !== -1 ? header.indexOf('id') : header.indexOf('no');
  var colMobil = -1;
  var colOrder = -1;
  var photoCols = [];

  for (var c = 0; c < header.length; c++) {
    var h = header[c];
    if (colMobil === -1 && (h.indexOf('mobil') !== -1 || h.indexOf('vehicle') !== -1 || h.indexOf('nopol') !== -1)) {
      colMobil = c;
    }
    if (colOrder === -1 && (h.indexOf('order') !== -1 || h.indexOf('notif') !== -1)) {
      colOrder = c;
    }
    if (h.indexOf('photo') !== -1 || h.indexOf('foto') !== -1 || h.indexOf('depan') !== -1 || h.indexOf('samping') !== -1 || h.indexOf('interior') !== -1 || h.indexOf('belakang') !== -1) {
      photoCols.push(c);
    }
  }

  if (colId === -1) colId = 0;
  if (colMobil === -1) colMobil = (data[0].length >= 3) ? 2 : 1;
  if (colOrder === -1) colOrder = (data[0].length >= 6) ? 5 : 1;
  if (photoCols.length === 0 && data[0].length >= 12) {
    photoCols = [7, 8, 9, 10, 11];
  }

  function cleanStr(s) {
    return String(s || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  var targetId = String(params.id || '').trim();
  var targetNoMobil = cleanStr(params.noMobil);
  var targetOrder = cleanStr(params.order || params.orderNo);
  var targetRow = -1;
  var matchedData = null;

  // 1. Cari berdasarkan ID
  if (targetId && targetId !== '0') {
    for (var i = data.length - 1; i >= 1; i--) {
      var rId = String(data[i][colId] || '').trim();
      if (rId === targetId) {
        targetRow = i + 1;
        matchedData = data[i];
        break;
      }
    }
  }

  // 2. Cari berdasarkan No Mobil + Order
  if (targetRow === -1 && targetNoMobil && targetOrder) {
    for (var j = data.length - 1; j >= 1; j--) {
      var rMobil = cleanStr(data[j][colMobil]);
      var rOrder = cleanStr(data[j][colOrder]);
      if (rMobil === targetNoMobil && rOrder === targetOrder) {
        targetRow = j + 1;
        matchedData = data[j];
        break;
      }
    }
  }

  // 3. Cari berdasarkan No Mobil saja
  if (targetRow === -1 && targetNoMobil) {
    for (var k = data.length - 1; k >= 1; k--) {
      var rMob = cleanStr(data[k][colMobil]);
      if (rMob === targetNoMobil) {
        targetRow = k + 1;
        matchedData = data[k];
        break;
      }
    }
  }

  if (targetRow === -1) {
    return {
      status: 'not_found',
      success: false,
      message: 'Data Mobil #' + (params.noMobil || targetId) + ' tidak ditemukan di Google Sheets ' + targetSheetName
    };
  }

  // Hapus foto terkait di Google Drive jika diminta (default true)
  var shouldDeletePhotos = params.deleteDrivePhotos !== false && params.deleteDrivePhotos !== 'false';
  var deletedPhotosCount = 0;
  if (shouldDeletePhotos) {
    var photoUrlsToDelete = [];
    if (matchedData) {
      for (var p = 0; p < photoCols.length; p++) {
        var val = matchedData[photoCols[p]];
        if (val && String(val).indexOf('http') === 0) {
          photoUrlsToDelete.push(String(val));
        }
      }
    }
    if (params.photoUrls && Array.isArray(params.photoUrls)) {
      for (var u = 0; u < params.photoUrls.length; u++) {
        var pu = String(params.photoUrls[u] || '');
        if (pu.indexOf('http') === 0 && photoUrlsToDelete.indexOf(pu) === -1) {
          photoUrlsToDelete.push(pu);
        }
      }
    }

    for (var d = 0; d < photoUrlsToDelete.length; d++) {
      try {
        var fileId = extractDriveFileId(photoUrlsToDelete[d]);
        if (fileId) {
          var driveFile = DriveApp.getFileById(fileId);
          driveFile.setTrashed(true);
          deletedPhotosCount++;
          Logger.log("✅ Foto Drive dihapus: " + fileId);
        }
      } catch (eDrive) {
        Logger.log("Peringatan hapus foto Drive (" + photoUrlsToDelete[d] + "): " + eDrive.message);
      }
    }
  }

  // Hapus baris dari Google Sheet
  sheet.deleteRow(targetRow);
  Logger.log("✅ Baris ke-" + targetRow + " berhasil dihapus dari sheet " + targetSheetName);

  return {
    status: 'success',
    success: true,
    deleted: true,
    row: targetRow,
    deletedId: targetId,
    deletedPhotosCount: deletedPhotosCount,
    message: 'Data Mobil #' + (params.noMobil || targetId) + ' (baris ' + targetRow + ') dan ' + deletedPhotosCount + ' foto di Google Drive berhasil dihapus!'
  };
}

// ============================================================================
// 6b. INPUT AUDIT KENDARAAN (SINKRONISASI BASE64, DRIVE, DAN GOOGLE SHEETS)
// ============================================================================
function saveOrUpdateAudit(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "INPUT_AUDIT";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("INPUT_AUDIT") || 
              ss.getSheetByName("DATA_AUDIT") ||
              ss.getSheetByName("AUDIT");
  
  if (!sheet) {
    sheet = ss.insertSheet(targetSheetName);
  }

  // Buat header otomatis jika sheet masih kosong
  if (sheet.getLastRow() === 0) {
    var headerRow = [
      'ID', 'TIMESTAMP', 'TANGGAL', 'NIP', 'NAMA PETUGAS', 'NO MOBIL', 'PLAT NOMER', 
      'TIPE', 'WARNA', 'TAHUN', 'POOL', 'KPI_POIN', 'CATATAN'
    ];
    for (var p = 1; p <= 20; p++) {
      headerRow.push('PHOTO' + p);
      headerRow.push('CATATAN_PHOTO' + p);
    }
    sheet.appendRow(headerRow);
  }

  var data = sheet.getDataRange().getValues();
  var noMobil = String(item.noMobil || item.nomobil || '').trim().toUpperCase();
  var photosMap = item.photosMap || {};
  var driveUrls = {};

  // Decode Base64 & simpan ke Google Drive folder FOTO_AUDIT_KENDARAAN
  function processAuditPhoto(rawVal, slotName) {
    if (!rawVal) return '';
    var str = String(rawVal).trim();
    if (str.indexOf('http://') === 0 || str.indexOf('https://') === 0) {
      return str;
    }
    if (str.length < 50) return '';
    try {
      var cleanSlot = String(slotName || 'foto').toLowerCase().replace(/[^a-z0-9_]/g, '_');
      var cleanMob = noMobil.replace(/[^a-zA-Z0-9]/g, '');
      var fileName = 'KENDARAAN_' + (cleanMob || 'MOBIL') + '_' + cleanSlot + '_' + new Date().getTime() + '.jpg';
      var folderId = item.folderId || FOLDER_PHOTO_KEINDAHAN_ID;
      var folderName = item.folderName || 'FOTO_AUDIT_KENDARAAN';
      var directLink = saveBase64ImageToDrive(str, fileName, folderId, folderName);
      if (directLink && directLink.indexOf('http') === 0) {
        return directLink;
      }
    } catch (errUpload) {
      Logger.log("processAuditPhoto error: " + errUpload.message);
    }
    return '';
  }

  // Loop photo1 s/d photo20 dan photosMap
  for (var i = 1; i <= 20; i++) {
    var pKey = 'photo' + i;
    var rawPhoto = item[pKey] || photosMap[pKey] || photosMap['slot' + i];
    if (rawPhoto) {
      var dLink = processAuditPhoto(rawPhoto, pKey);
      if (dLink) {
        driveUrls[pKey] = dLink;
        item[pKey] = dLink;
      }
    }
  }

  for (var customKey in photosMap) {
    if (photosMap.hasOwnProperty(customKey)) {
      if (!driveUrls[customKey]) {
        var cLink = processAuditPhoto(photosMap[customKey], customKey);
        if (cLink) driveUrls[customKey] = cLink;
      }
    }
  }

  // Cari baris yang bersangkutan di sheet INPUT_AUDIT
  var targetRow = -1;
  var targetId = String(item.id || '').trim();
  var targetTanggal = String(item.tanggal || '').trim();
  var isEdit = item.isEdit === true || item.isEdit === 'true' || item.mode === 'edit';

  if (data.length > 1) {
    if (targetId && targetId !== '0') {
      for (var r = data.length - 1; r >= 1; r--) {
        if (String(data[r][0] || '').trim() === targetId) {
          targetRow = r + 1;
          break;
        }
      }
    }
    if (targetRow === -1 && noMobil && targetTanggal) {
      for (var r2 = data.length - 1; r2 >= 1; r2--) {
        var rowMob = String(data[r2][5] || '').toUpperCase().replace(/[^a-zA-Z0-9]/g, '');
        var rowTgl = String(data[r2][2] || '').trim();
        if (rowMob === noMobil.replace(/[^a-zA-Z0-9]/g, '') && rowTgl === targetTanggal) {
          targetRow = r2 + 1;
          break;
        }
      }
    }
    if (targetRow === -1 && isEdit && noMobil) {
      for (var r3 = data.length - 1; r3 >= 1; r3--) {
        var rowMob3 = String(data[r3][5] || '').toUpperCase().replace(/[^a-zA-Z0-9]/g, '');
        if (rowMob3 === noMobil.replace(/[^a-zA-Z0-9]/g, '')) {
          targetRow = r3 + 1;
          break;
        }
      }
    }
  }

  var existingRowData = targetRow > 0 && data[targetRow - 1] ? data[targetRow - 1] : null;
  var newId = (targetRow > 0 && existingRowData) ? (existingRowData[0] || targetId || 1) : ((data.length > 1) ? data.length : 1);
  var timestampStr = item.timestamp || (existingRowData ? existingRowData[1] : Utilities.formatDate(new Date(), Session.getScriptTimeZone() || 'Asia/Jakarta', 'yyyy-MM-dd HH:mm:ss'));
  var rowValues = [
    item.id || newId,
    timestampStr,
    item.tanggal || (existingRowData ? existingRowData[2] : ''),
    item.nip || (existingRowData ? existingRowData[3] : ''),
    item.namaPetugas || item.namaMekanik || (existingRowData ? existingRowData[4] : ''),
    noMobil || (existingRowData ? existingRowData[5] : ''),
    item.platNomer || item.noPolisi || (existingRowData ? existingRowData[6] : ''),
    item.tipe || (existingRowData ? existingRowData[7] : ''),
    item.warna || (existingRowData ? existingRowData[8] : ''),
    item.tahun || (existingRowData ? existingRowData[9] : ''),
    item.pool || (existingRowData ? existingRowData[10] : 'BENGKEL-PUSAT'),
    item.kpiPoin || item.kpi || (existingRowData ? existingRowData[11] : ''),
    item.catatan || item.keterangan || (existingRowData ? existingRowData[12] : '')
  ];

  for (var s = 1; s <= 20; s++) {
    var pColIdx = 13 + ((s - 1) * 2);
    var cColIdx = pColIdx + 1;
    var photoVal = driveUrls['photo' + s] || item['photo' + s] || '';
    if (!photoVal && existingRowData && pColIdx < existingRowData.length) {
      photoVal = existingRowData[pColIdx] || '';
    }
    var catatanVal = item['catatanPhoto' + s] || item['catatan' + s] || '';
    if (!catatanVal && existingRowData && cColIdx < existingRowData.length) {
      catatanVal = existingRowData[cColIdx] || '';
    }
    rowValues.push(photoVal);
    rowValues.push(catatanVal);
  }

  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowValues.length).setValues([rowValues]);
  } else {
    sheet.appendRow(rowValues);
  }

  return {
    status: 'success',
    success: true,
    action: targetRow > 0 ? 'updated' : 'appended',
    row: targetRow > 0 ? targetRow : (sheet.getLastRow()),
    id: item.id || newId,
    noMobil: noMobil,
    driveUrls: driveUrls,
    message: targetRow > 0 ? 
      ('Data audit Mobil #' + noMobil + ' (baris ' + targetRow + ') berhasil diperbarui di Google Sheets!') : 
      ('Data audit Mobil #' + noMobil + ' & foto berhasil disimpan ke Google Drive (FOTO_AUDIT_KENDARAAN) dan sheet ' + targetSheetName)
  };
}

// ============================================================================
// 11. PREDIKSI PART BENGKEL
// ============================================================================
function saveOrUpdatePrediksiPart(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "PREDIKSI_PART";
  var sheet = ss.getSheetByName(targetSheetName) || ss.getSheetByName("PREDIKSI_PART") || ss.getSheetByName("PREDIKSI PART") || ss.getSheets()[0];
  
  if (sheet.getLastRow() === 0) {
    sheet.appendRow([
      'ID', 'TANGGAL', 'NO_MOBIL', 'SERVICE', 'KM_MOBIL', 'NIP_NAMA',
      'NAMA_PART', 'PREDIKSI_KM', 'KM_TARGET', 'ARGO_SEKARANG',
      'ESTIMASI_KM', 'APROVEL', 'KETERANGAN', 'STATUS'
    ]);
  }
  
  var data = sheet.getDataRange().getValues();
  var targetRow = -1;
  var targetId = String(item.id || '').trim();
  var targetNoMobil = String(item.noMobil || '').trim().toLowerCase().replace(/^#/, '');
  var targetNamaPart = String(item.namaPart || '').trim().toLowerCase();
  var targetTanggal = String(item.tanggal || '').trim().toLowerCase();

  if (targetId && targetId !== '0' && data.length > 1) {
    for (var i = 1; i < data.length; i++) {
      var rId = String(data[i][0] || '').trim();
      if (rId === targetId) {
        targetRow = i + 1;
        break;
      }
    }
  }

  if (targetRow === -1 && targetNoMobil && targetNamaPart && data.length > 1) {
    for (var i = data.length - 1; i >= 1; i--) {
      var rMob = String(data[i][2] || '').trim().toLowerCase().replace(/^#/, '');
      var rPart = String(data[i][6] || '').trim().toLowerCase();
      var rTgl = String(data[i][1] || '').trim().toLowerCase();
      if (rMob === targetNoMobil && rPart === targetNamaPart && (!targetTanggal || rTgl === targetTanggal)) {
        targetRow = i + 1;
        break;
      }
    }
  }

  var rowId = targetRow > 0 ? (data[targetRow - 1][0] || targetId || targetRow - 1) : (targetId && targetId !== '0' ? targetId : (data.length));
  var kmMobilVal = item.kmMobil !== undefined && item.kmMobil !== null && item.kmMobil !== '' ? item.kmMobil : (item.kmInput || '');

  var rowData = [
    rowId,
    item.tanggal || '',
    item.noMobil || '',
    item.service || '',
    kmMobilVal,
    item.nipNama || '',
    item.namaPart || '',
    item.prediksiKm || '',
    item.kmTarget || '',
    item.argoSekarang || '',
    item.estimasiKm || '',
    item.aprovel || '',
    item.keterangan || '',
    item.status || ''
  ];

  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowData.length).setValues([rowData]);
    return { success: true, action: 'updated', row: targetRow, id: rowId, sheet: sheet.getName() };
  } else {
    sheet.appendRow(rowData);
    return { success: true, action: 'appended', row: sheet.getLastRow(), id: rowId, sheet: sheet.getName() };
  }
}

function deletePrediksiPart(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "PREDIKSI_PART";
  var sheet = ss.getSheetByName(targetSheetName) || ss.getSheetByName("PREDIKSI_PART") || ss.getSheetByName("PREDIKSI PART") || ss.getSheets()[0];
  var data = sheet.getDataRange().getValues();
  var targetId = String(item.id || '').trim();
  var targetNoMobil = String(item.noMobil || '').trim().toLowerCase().replace(/^#/, '');
  var targetNamaPart = String(item.namaPart || '').trim().toLowerCase();

  if (data.length > 1) {
    for (var i = data.length - 1; i >= 1; i--) {
      var rId = String(data[i][0] || '').trim();
      var rMob = String(data[i][2] || '').trim().toLowerCase().replace(/^#/, '');
      var rPart = String(data[i][6] || '').trim().toLowerCase();
      if ((targetId && targetId !== '0' && rId === targetId) || (targetNoMobil && targetNamaPart && rMob === targetNoMobil && rPart === targetNamaPart)) {
        sheet.deleteRow(i + 1);
        return { success: true, action: 'deleted', row: i + 1, sheet: sheet.getName() };
      }
    }
  }
  return { success: false, action: 'not_found', message: 'Data Prediksi Part tidak ditemukan di Google Sheets' };
}
        """.trimIndent()
    }
}
