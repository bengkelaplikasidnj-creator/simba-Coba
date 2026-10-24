package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.GoogleAppsScriptHub
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Teal600

@Composable
fun GoogleScriptSetupScreen(
    currentLaporanId: String,
    currentLaporanSheetName: String = "LAPORAN KENDARAAN",
    currentTeknikId: String = com.example.data.session.SessionManager.DEFAULT_SERVICE_TEKNIK_SHEET_ID,
    currentTeknikSheetName: String = com.example.data.session.SessionManager.DEFAULT_SERVICE_TEKNIK_SHEET_NAME,
    currentKeindahanId: String = com.example.data.session.SessionManager.DEFAULT_SERVICE_KEINDAHAN_SHEET_ID,
    currentKeindahanSheetName: String = com.example.data.session.SessionManager.DEFAULT_SERVICE_KEINDAHAN_SHEET_NAME,
    currentBanId: String = com.example.data.session.SessionManager.DEFAULT_BAN_SHEET_ID,
    currentBanSheetName: String = com.example.data.session.SessionManager.DEFAULT_BAN_SHEET_NAME,
    currentKoplingId: String = com.example.data.session.SessionManager.DEFAULT_KOPLING_SHEET_ID,
    currentKoplingSheetName: String = com.example.data.session.SessionManager.DEFAULT_KOPLING_SHEET_NAME,
    currentBaterayId: String = com.example.data.session.SessionManager.DEFAULT_BATERAY_SHEET_ID,
    currentBateraySheetName: String = com.example.data.session.SessionManager.DEFAULT_BATERAY_SHEET_NAME,
    currentArgoId: String = com.example.data.session.SessionManager.DEFAULT_ARGO_SHEET_ID,
    currentArgoSheetName: String = com.example.data.session.SessionManager.DEFAULT_ARGO_SHEET_NAME,
    currentReservasiId: String = com.example.data.session.SessionManager.DEFAULT_RESERVASI_SHEET_ID,
    currentReservasiSheetName: String = com.example.data.session.SessionManager.DEFAULT_RESERVASI_SHEET_NAME,
    currentPengerjaanTeknikId: String = com.example.data.session.SessionManager.DEFAULT_PENGERJAAN_TEKNIK_SHEET_ID,
    currentPengerjaanTeknikSheetName: String = com.example.data.session.SessionManager.DEFAULT_PENGERJAAN_TEKNIK_SHEET_NAME,
    currentPekerjaanKeindahanId: String = com.example.data.session.SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID,
    currentPekerjaanKeindahanSheetName: String = com.example.data.session.SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME,
    currentRiwayatSkiId: String = com.example.data.session.SessionManager.DEFAULT_RIWAYAT_SKI_SHEET_ID,
    currentRiwayatSkiSheetName: String = com.example.data.session.SessionManager.DEFAULT_RIWAYAT_SKI_SHEET_NAME,
    currentUserId: String,
    currentKendaraanId: String,
    currentKendaraanSheetName: String = "MASTER",
    currentScriptUrl: String,
    currentFolderPhotoKeindahanId: String = com.example.data.session.SessionManager.DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID,
    onSaveSettings: (
        laporanId: String,
        laporanSheetName: String,
        teknikId: String,
        teknikSheetName: String,
        keindahanId: String,
        keindahanSheetName: String,
        banId: String,
        banSheetName: String,
        koplingId: String,
        koplingSheetName: String,
        baterayId: String,
        bateraySheetName: String,
        argoId: String,
        argoSheetName: String,
        reservasiId: String,
        reservasiSheetName: String,
        pengerjaanTeknikId: String,
        pengerjaanTeknikSheetName: String,
        pekerjaanKeindahanId: String,
        pekerjaanKeindahanSheetName: String,
        riwayatSkiId: String,
        riwayatSkiSheetName: String,
        userId: String,
        kendaraanId: String,
        kendaraanSheetName: String,
        scriptUrl: String,
        folderPhotoKeindahanId: String
    ) -> Unit,
    onSyncNow: () -> Unit,
    onTestScriptUrl: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onTestDrivePermission: ((String, (Boolean, String) -> Unit) -> Unit)? = null,
    onTestUploadSamplePhoto: (((Boolean, String, String) -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var laporanId by remember { mutableStateOf(currentLaporanId) }
    var laporanSheetName by remember { mutableStateOf(currentLaporanSheetName) }
    var teknikId by remember { mutableStateOf(currentTeknikId) }
    var teknikSheetName by remember { mutableStateOf(currentTeknikSheetName) }
    var keindahanId by remember { mutableStateOf(currentKeindahanId) }
    var keindahanSheetName by remember { mutableStateOf(currentKeindahanSheetName) }
    var banId by remember { mutableStateOf(currentBanId) }
    var banSheetName by remember { mutableStateOf(currentBanSheetName) }
    var koplingId by remember { mutableStateOf(currentKoplingId) }
    var koplingSheetName by remember { mutableStateOf(currentKoplingSheetName) }
    var baterayId by remember { mutableStateOf(currentBaterayId) }
    var bateraySheetName by remember { mutableStateOf(currentBateraySheetName) }
    var argoId by remember(currentArgoId) {
        mutableStateOf(
            if (currentArgoId.isBlank() || currentArgoId == "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU") {
                com.example.data.session.SessionManager.DEFAULT_ARGO_SHEET_ID
            } else {
                currentArgoId
            }
        )
    }
    var argoSheetName by remember(currentArgoSheetName) {
        mutableStateOf(
            if (currentArgoSheetName.isBlank()) {
                com.example.data.session.SessionManager.DEFAULT_ARGO_SHEET_NAME
            } else {
                currentArgoSheetName
            }
        )
    }
    var reservasiId by remember { mutableStateOf(currentReservasiId) }
    var reservasiSheetName by remember(currentReservasiSheetName) {
        mutableStateOf(
            if (currentReservasiSheetName.trim().equals("GM", ignoreCase = true) || currentReservasiSheetName.trim().isBlank()) {
                com.example.data.session.SessionManager.DEFAULT_RESERVASI_SHEET_NAME
            } else {
                currentReservasiSheetName
            }
        )
    }
    var pengerjaanTeknikId by remember { mutableStateOf(currentPengerjaanTeknikId) }
    var pengerjaanTeknikSheetName by remember { mutableStateOf(currentPengerjaanTeknikSheetName) }
    var pekerjaanKeindahanId by remember(currentPekerjaanKeindahanId) {
        mutableStateOf(
            if (currentPekerjaanKeindahanId.isBlank() || currentPekerjaanKeindahanId == "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs") {
                com.example.data.session.SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID
            } else {
                currentPekerjaanKeindahanId
            }
        )
    }
    var pekerjaanKeindahanSheetName by remember(currentPekerjaanKeindahanSheetName) {
        mutableStateOf(
            if (currentPekerjaanKeindahanSheetName.isBlank()) {
                com.example.data.session.SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME
            } else {
                currentPekerjaanKeindahanSheetName
            }
        )
    }
    var riwayatSkiId by remember { mutableStateOf(currentRiwayatSkiId) }
    var riwayatSkiSheetName by remember { mutableStateOf(currentRiwayatSkiSheetName) }
    var userId by remember { mutableStateOf(currentUserId) }
    var kendaraanId by remember { mutableStateOf(currentKendaraanId) }
    var kendaraanSheetName by remember { mutableStateOf(currentKendaraanSheetName) }
    var scriptUrl by remember(currentScriptUrl) {
        mutableStateOf(
            if (currentScriptUrl.isBlank() ||
                currentScriptUrl.contains("AKfycbxyIaBKU1W3U4_DWIzbfPYnhc07xR-YBrwuPd4YGV_JCFEioR_iRx8ID9Hr0TOs5heM") ||
                currentScriptUrl.contains("AKfycbyS9B9yCUSfx8Cr41NXjRJ1_HClMvt210dQGbCrV3dO0m_8oT4H6bKfpefxXYh9GtS94w") ||
                currentScriptUrl.contains("AKfycbwjfZGwauVXpisbx7T8UPioBTJP802LJvKmvcD5e3smdSuuBOL2T3A_cOYtz2JxM4FZfg")
            ) {
                com.example.data.session.SessionManager.DEFAULT_SCRIPT_URL
            } else {
                currentScriptUrl
            }
        )
    }
    var folderPhotoKeindahanId by remember(currentFolderPhotoKeindahanId) {
        mutableStateOf(
            if (currentFolderPhotoKeindahanId.isBlank() || currentFolderPhotoKeindahanId == "1Exy7AawdwoV5byqucwVBPIbTxI6anAjk") {
                com.example.data.session.SessionManager.DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID
            } else {
                currentFolderPhotoKeindahanId
            }
        )
    }

    var isTestingScript by remember { mutableStateOf(false) }
    var testScriptResult by remember { mutableStateOf<String?>(null) }
    var testScriptSuccess by remember { mutableStateOf(false) }

    var isTestingDrive by remember { mutableStateOf(false) }
    var testDriveResult by remember { mutableStateOf<String?>(null) }
    var testDriveSuccess by remember { mutableStateOf(false) }

    var isTestingUpload by remember { mutableStateOf(false) }
    var testUploadResult by remember { mutableStateOf<String?>(null) }
    var testUploadDriveUrl by remember { mutableStateOf("") }
    var testUploadSuccess by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label disalin ke clipboard!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info card Manual Sync (Hemat Daya & Bebas Panas)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sinkron Otomatis Setiap Ada Perubahan",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                    )
                    Text(
                        text = "Aplikasi otomatis melakukan sinkronisasi dengan Google Sheets setiap kali ada penambahan, perubahan, atau penghapusan data, serta saat awal aplikasi dibuka. Data selalu tersimpan secara real-time dan terbarui.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF14532D))
                    )
                }
            }
        }

        // Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ID DATABASE GOOGLE SHEETS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Blue900
                    )
                )
                Text(
                    text = "Masukkan Spreadsheet ID untuk masing-masing modul bengkel sesuai tabel data.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = laporanId,
                    onValueChange = { laporanId = it },
                    label = { Text("1. ID Google Sheets LAPORAN HARIAN") },
                    placeholder = { Text("1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = laporanSheetName,
                    onValueChange = { laporanSheetName = it },
                    label = { Text("Nama Sheet / Tab LAPORAN HARIAN (Default: LAPORAN KENDARAAN)") },
                    placeholder = { Text("LAPORAN KENDARAAN") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = Teal600) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = teknikId,
                    onValueChange = { teknikId = it },
                    label = { Text("2. ID Google Sheets JADWAL SERVICE TEKNIK") },
                    placeholder = { Text("1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = teknikSheetName,
                    onValueChange = { teknikSheetName = it },
                    label = { Text("Nama Sheet / Tab TEKNIK (Default: TEKNIK)") },
                    placeholder = { Text("Contoh: TEKNIK") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = keindahanId,
                    onValueChange = { keindahanId = it },
                    label = { Text("3. ID Google Sheets JADWAL SERVICE KEINDAHAN") },
                    placeholder = { Text("1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = keindahanSheetName,
                    onValueChange = { keindahanSheetName = it },
                    label = { Text("Nama Sheet / Tab KEINDAHAN (Default: KEINDAHAN)") },
                    placeholder = { Text("Contoh: KEINDAHAN") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = banId,
                    onValueChange = { banId = it },
                    label = { Text("4. ID Google Sheets BAN") },
                    placeholder = { Text("17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = banSheetName,
                    onValueChange = { banSheetName = it },
                    label = { Text("Nama Sheet / Tab BAN (Default: DATA_BAN)") },
                    placeholder = { Text("Contoh: DATA_BAN") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = koplingId,
                    onValueChange = { koplingId = it },
                    label = { Text("7. ID Google Sheets KOPLING") },
                    placeholder = { Text("1qBtcaJP1adyawgZ2hbAIvlld-usZ3CV5YB6QSRpVxd4") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = koplingSheetName,
                    onValueChange = { koplingSheetName = it },
                    label = { Text("Nama Sheet / Tab KOPLING (Default: KOPLING)") },
                    placeholder = { Text("Contoh: KOPLING") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = baterayId,
                    onValueChange = { baterayId = it },
                    label = { Text("8. ID Google Sheets BATERAI / ACCU") },
                    placeholder = { Text("145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bateraySheetName,
                    onValueChange = { bateraySheetName = it },
                    label = { Text("Nama Sheet / Tab BATERAI / ACCU (Default: ACCU)") },
                    placeholder = { Text("Contoh: ACCU") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = argoId,
                    onValueChange = { argoId = it },
                    label = { Text("8b. ID Google Sheets ARGO") },
                    placeholder = { Text(com.example.data.session.SessionManager.DEFAULT_ARGO_SHEET_ID) },
                    supportingText = {
                        Text(
                            "Susunan: | ID | NO MOBIL | KM KOSONG | KM ISI | RIT | DROP | KM | GOLONGAN |",
                            fontSize = 11.sp,
                            color = HighDensityBlue
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = argoSheetName,
                    onValueChange = { argoSheetName = it },
                    label = { Text("Nama Sheet / Tab ARGO (Default: ARGO)") },
                    placeholder = { Text("ARGO") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reservasiId,
                    onValueChange = { reservasiId = it },
                    label = { Text("9. ID Google Sheets RESERVASI") },
                    placeholder = { Text("145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reservasiSheetName,
                    onValueChange = { reservasiSheetName = it },
                    label = { Text("Nama Sheet / Tab RESERVASI (Default: GOODMOVEN)") },
                    placeholder = { Text("GOODMOVEN") },
                    supportingText = { Text("Nama sheet resmi: GOODMOVEN (sebelumnya GM)", fontSize = 11.sp, color = Slate600) },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pengerjaanTeknikId,
                    onValueChange = { pengerjaanTeknikId = it },
                    label = { Text("10. ID Google Sheets RIWAYAT SERVICE TEKNIK") },
                    placeholder = { Text("145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pengerjaanTeknikSheetName,
                    onValueChange = { pengerjaanTeknikSheetName = it },
                    label = { Text("Nama Sheet / Tab RIWAYAT SERVICE TEKNIK (Default: SKD)") },
                    placeholder = { Text("SKD") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pekerjaanKeindahanId,
                    onValueChange = { pekerjaanKeindahanId = it },
                    label = { Text("10b. ID Google Sheets RIWAYAT SERVICE KEINDAHAN") },
                    placeholder = { Text("1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30") },
                    supportingText = { Text("ID Database: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30", fontSize = 11.sp, color = Slate600) },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pekerjaanKeindahanSheetName,
                    onValueChange = { pekerjaanKeindahanSheetName = it },
                    label = { Text("Nama Sheet / Tab RIWAYAT SERVICE KEINDAHAN (Default: SERVICE_KEINDAHAN)") },
                    placeholder = { Text("SERVICE_KEINDAHAN") },
                    supportingText = { Text("Nama sheet resmi: SERVICE_KEINDAHAN", fontSize = 11.sp, color = Slate600) },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Petunjuk Izin Google Drive (Status HTTP: 200)",
                                fontWeight = FontWeight.Bold,
                                color = Blue900,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Jika upload foto menampilkan pesan gagal (HTTP 200), skrip Apps Script Anda perlu di-deploy ulang agar memiliki izin Google Drive:\n1. Buka Google Sheets > Ekstensi > Apps Script.\n2. Tempel kode skrip terbaru dari tab di bawah ini & simpan.\n3. Klik 'Deploy' (Terapkan) > 'New deployment' (Deployment baru).\n4. Pilih 'Web app' > Akses: 'Anyone' (Siapa saja) > Klik 'Deploy'.\n5. Klik 'Authorize access' untuk menyetujui izin Google Drive.",
                            fontSize = 11.sp,
                            color = Slate600,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = folderPhotoKeindahanId,
                    onValueChange = { folderPhotoKeindahanId = it },
                    label = { Text("11. ID Folder Foto Google Drive (Folder ID / Link)") },
                    placeholder = { Text("1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6") },
                    supportingText = { Text("Default: 1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6", fontSize = 11.sp, color = Slate600) },
                    leadingIcon = { Icon(Icons.Default.FolderShared, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = riwayatSkiId,
                    onValueChange = { riwayatSkiId = it },
                    label = { Text("12. ID Google Sheets RIWAYAT SERVICE SKI") },
                    placeholder = { Text("145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = riwayatSkiSheetName,
                    onValueChange = { riwayatSkiSheetName = it },
                    label = { Text("Nama Sheet / Tab RIWAYAT SERVICE SKI (Default: SKI)") },
                    placeholder = { Text("Contoh: SKI") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("13. ID Google Sheets USER BENGKEL (Sheet: USER)") },
                    placeholder = { Text("1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = kendaraanId,
                    onValueChange = { kendaraanId = it },
                    label = { Text("14. ID Google Sheets MASTER KENDARAAN") },
                    placeholder = { Text("13iuagC5JY1NOkVRMwebz8-XvaEp-2q_qiyaDedGedNQ") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = kendaraanSheetName,
                    onValueChange = { kendaraanSheetName = it },
                    label = { Text("Nama Sheet / Tab MASTER KENDARAAN (Default: MASTER)") },
                    placeholder = { Text("MASTER") },
                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Alert Banner Penjelasan Web App
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PENTING: Cara Agar Data di Google Sheets Otomatis Berubah",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Jika di HP muncul popup tersimpan tapi di Google Sheets belum berubah, hal ini karena Web Script belum memiliki izin edit ke Google Sheets Anda.\n\n" +
                                        "Langkah mudah mengatasinya:\n" +
                                        "1. Buka file Google Sheets Anda di browser.\n" +
                                        "2. Klik menu 'Ekstensi' (Extensions) > 'Apps Script'.\n" +
                                        "3. Salin kode dari tombol 'Salin Script' di bawah, lalu paste di Apps Script.\n" +
                                        "4. Klik 'Terapkan' (Deploy) > 'Penerapan Baru' (New Deployment) > pilih 'Aplikasi Web' (Web App).\n" +
                                        "5. Pilih 'Jalankan sebagai: Saya (Me)' dan 'Akses: Siapa saja (Anyone)'.\n" +
                                        "6. Salin URL Web App yang berakhiran /exec dan tempel di kotak nomor 14 di bawah.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F), fontSize = 11.sp, lineHeight = 16.sp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = scriptUrl,
                    onValueChange = { 
                        scriptUrl = it 
                        testScriptResult = null
                    },
                    label = { Text("14. URL Google Apps Script Web App (Wajib untuk Edit/Write)") },
                    placeholder = { Text("https://script.google.com/macros/s/AKfycbwOAOqvX91d0fasOzjnCkmpQjM61Cw_4RgMyeNc8D-hnFmIoYqgPuuTrsVH5jn2RTvS/exec") },
                    supportingText = { Text("Default: https://script.google.com/macros/s/AKfycbwOAOqvX91d0fasOzjnCkmpQjM61Cw_4RgMyeNc8D-hnFmIoYqgPuuTrsVH5jn2RTvS/exec", fontSize = 11.sp, color = Slate600) },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (scriptUrl.isBlank()) {
                                Toast.makeText(context, "Masukkan URL Web App terlebih dahulu", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            isTestingScript = true
                            testScriptResult = null
                            onTestScriptUrl?.invoke(scriptUrl.trim()) { success, msg ->
                                isTestingScript = false
                                testScriptSuccess = success
                                testScriptResult = msg
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isTestingScript && !isTestingDrive,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTestingScript) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Menguji...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Ping Server", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (scriptUrl.isBlank()) {
                                Toast.makeText(context, "Masukkan URL Web App terlebih dahulu", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isTestingDrive = true
                            testDriveResult = null
                            onTestDrivePermission?.invoke(scriptUrl.trim()) { success, msg ->
                                isTestingDrive = false
                                testDriveSuccess = success
                                testDriveResult = msg
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (testDriveSuccess) Color(0xFF16A34A) else Blue900),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isTestingScript && !isTestingDrive,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTestingDrive) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cek Drive...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Izin Drive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (scriptUrl.isBlank()) {
                            Toast.makeText(context, "Masukkan URL Web App terlebih dahulu", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isTestingUpload = true
                        testUploadResult = null
                        testUploadDriveUrl = ""
                        onTestUploadSamplePhoto?.invoke { success, driveUrl, msg ->
                            isTestingUpload = false
                            testUploadSuccess = success
                            testUploadResult = msg
                            testUploadDriveUrl = driveUrl
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (testUploadSuccess) Color(0xFF15803D) else Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isTestingScript && !isTestingDrive && !isTestingUpload,
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    if (isTestingUpload) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mengunggah Foto Uji Coba ke Drive...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Upload Foto Langsung ke Google Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (testUploadResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = if (testUploadSuccess) Color(0xFFDCFCE7) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (testUploadSuccess) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (testUploadSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (testUploadSuccess) Color(0xFF15803D) else Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (testUploadSuccess) "Berhasil Upload Foto ke Google Drive!" else "Gagal Upload Foto ke Google Drive",
                                    fontWeight = FontWeight.Bold,
                                    color = if (testUploadSuccess) Color(0xFF166534) else Color(0xFF7F1D1D),
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = testUploadResult ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (testUploadSuccess) Color(0xFF166534) else Color(0xFF7F1D1D),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            )
                            if (testUploadDriveUrl.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { uriHandler.openUri(testUploadDriveUrl) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Buka Foto di Google Drive ↗", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (testScriptResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = if (testScriptSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (testScriptSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (testScriptSuccess) Color(0xFF15803D) else Color(0xFFB91C1C),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = testScriptResult ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (testScriptSuccess) Color(0xFF166534) else Color(0xFF991B1B),
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }

                if (testDriveResult != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = if (testDriveSuccess) Color(0xFFDCFCE7) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (testDriveSuccess) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (testDriveSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (testDriveSuccess) Color(0xFF15803D) else Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = testDriveResult ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (testDriveSuccess) Color(0xFF166534) else Color(0xFF7F1D1D),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveSettings(
                                laporanId.trim(),
                                laporanSheetName.trim(),
                                teknikId.trim(),
                                teknikSheetName.trim(),
                                keindahanId.trim(),
                                keindahanSheetName.trim(),
                                banId.trim(),
                                banSheetName.trim(),
                                koplingId.trim(),
                                koplingSheetName.trim(),
                                baterayId.trim(),
                                bateraySheetName.trim(),
                                if (argoId.trim().isBlank() || argoId.trim() == "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU") {
                                    com.example.data.session.SessionManager.DEFAULT_ARGO_SHEET_ID
                                } else {
                                    argoId.trim()
                                },
                                if (argoSheetName.trim().isBlank()) {
                                    com.example.data.session.SessionManager.DEFAULT_ARGO_SHEET_NAME
                                } else {
                                    argoSheetName.trim()
                                },
                                reservasiId.trim(),
                                if (reservasiSheetName.trim().isBlank() || reservasiSheetName.trim().equals("GM", ignoreCase = true) || reservasiSheetName.trim().equals("RESERVASI", ignoreCase = true)) {
                                    com.example.data.session.SessionManager.DEFAULT_RESERVASI_SHEET_NAME
                                } else {
                                    reservasiSheetName.trim()
                                },
                                pengerjaanTeknikId.trim(),
                                pengerjaanTeknikSheetName.trim(),
                                if (pekerjaanKeindahanId.trim().isBlank()) {
                                    com.example.data.session.SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID
                                } else {
                                    pekerjaanKeindahanId.trim()
                                },
                                if (pekerjaanKeindahanSheetName.trim().isBlank()) {
                                    com.example.data.session.SessionManager.DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME
                                } else {
                                    pekerjaanKeindahanSheetName.trim()
                                },
                                riwayatSkiId.trim(),
                                riwayatSkiSheetName.trim(),
                                userId.trim(),
                                kendaraanId.trim(),
                                kendaraanSheetName.trim(),
                                if (scriptUrl.trim().isBlank()) {
                                    com.example.data.session.SessionManager.DEFAULT_SCRIPT_URL
                                } else {
                                    scriptUrl.trim()
                                },
                                if (folderPhotoKeindahanId.trim().isBlank() || folderPhotoKeindahanId.trim() == "1Exy7AawdwoV5byqucwVBPIbTxI6anAjk") {
                                    com.example.data.session.SessionManager.DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID
                                } else {
                                    folderPhotoKeindahanId.trim()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SIMPAN PENGATURAN")
                    }

                    OutlinedButton(
                        onClick = onSyncNow,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SINKRONKAN SEKARANG")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card Panduan Langkah Demi Langkah
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PANDUAN MENGAKTIFKAN EDIT & UPDATE GOOGLE SHEETS",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Blue900)
                )
                Spacer(modifier = Modifier.height(8.dp))
                val steps = listOf(
                    "1. Buka Google Sheets Anda (Spreadsheet Laporan Harian) di laptop / browser.",
                    "2. Di menu atas Google Sheets, klik menu Ekstensi (Extensions) > Apps Script.",
                    "3. Klik tombol 'Salin Script' di kotak bawah, lalu tempelkan (paste) menggantikan semua kode lama di editor Apps Script, lalu klik Simpan (ikon disket).",
                    "4. Klik tombol biru 'Deploy' (Terapkan) di kanan atas > 'New deployment' (Penerapan baru).",
                    "5. Pada ikon roda gigi, pilih tipe 'Web app' (Aplikasi web).",
                    "6. Ubah 'Who has access' (Siapa yang memiliki akses) menjadi 'Anyone' (Siapa saja). Hal ini wajib agar aplikasi Android bisa mengirim update tanpa login!",
                    "7. Klik Deploy, selesaikan otorisasi akun Google jika diminta, lalu salin URL Web App yang berakhiran '/exec'.",
                    "8. Tempelkan URL tersebut ke kolom '14. URL Google Apps Script Web App' di atas, lalu klik 'SIMPAN PENGATURAN'."
                )
                steps.forEach { step ->
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontSize = 11.5.sp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card Solusi Masalah Google Drive HTTP 200
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFFECACA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SOLUSI: GAGAL UPLOAD GOOGLE DRIVE (STATUS HTTP: 200)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Penyebab error ini: Google Apps Script Web App Anda belum disetujui (diberi izin) untuk mengakses Google Drive (DriveApp). Karena skrip menangkap error secara internal, status HTTP yang kembali adalah 200.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7F1D1D), fontSize = 11.5.sp, lineHeight = 16.sp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Langkah Perbaikan (Hanya 1 Menit):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                )
                Spacer(modifier = Modifier.height(4.dp))
                val driveSteps = listOf(
                    "1. Buka spreadsheet Google Sheets Anda di browser > Ekstensi > Apps Script.",
                    "2. Salin kode terbaru dari tombol 'Salin Script' di bawah, lalu paste di editor Apps Script dan Simpan (Ctrl+S / ikon Disket).",
                    "3. Di toolbar atas editor Apps Script (sebelah tombol 'Debug' dan 'Jalankan ▶'), klik dropdown daftar fungsi, lalu pilih: 'aktivasiIzinGoogleDrive'.",
                    "4. Klik tombol 'Jalankan' (Run ▶).",
                    "5. Muncul popup 'Diperlukan Otorisasi'. Klik 'Tinjau Izin' (Review Permissions) > Pilih Akun Google Anda.",
                    "6. Klik 'Lanjutan' (Advanced) di kiri bawah popup > Klik 'Buka Proyek (tidak aman)' > Klik 'Izinkan' (Allow).",
                    "7. Setelah sukses berjalan di log, klik menu 'Terapkan' (Deploy) di pojok kanan atas > 'Kelola penerapan' (Manage deployments).",
                    "8. Klik ikon Pensil (Edit) di samping Web App aktif > Pada 'Versi', pilih: 'Versi baru' (New version) > Klik 'Terapkan' (Deploy).",
                    "9. Kembali ke aplikasi ini dan klik tombol 'Test Izin Drive' di atas. Status akan berubah menjadi hijau ✅!"
                )
                driveSteps.forEach { step ->
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF450A0A), fontSize = 11.sp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Code template snippet card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TEMPLATE GOOGLE APPS SCRIPT",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue900
                        )
                    )

                    Button(
                        onClick = {
                            val code = GoogleAppsScriptHub.getBengkelAppsScriptCode(laporanId, teknikId)
                            copyToClipboard(code, "Google Apps Script Bengkel")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Script", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val scriptCode = remember(laporanId, teknikId) {
                    GoogleAppsScriptHub.getBengkelAppsScriptCode(laporanId, teknikId)
                }

                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = scriptCode,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Slate800,
                        modifier = Modifier
                            .padding(12.dp)
                            .height(220.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}
