package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.util.ImageCompressor
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.data.local.entity.PekerjaanServiceKeindahanEntity
import com.example.data.local.entity.UserEntity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBg
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueBorder
import com.example.ui.theme.HighDensityBlueDark
import com.example.ui.theme.HighDensityBlueLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PekerjaanServiceKeindahanScreen(
    items: List<PekerjaanServiceKeindahanEntity>,
    kendaraanList: List<MasterKendaraanEntity> = emptyList(),
    currentUser: UserEntity? = null,
    isSyncing: Boolean = false,
    isOnline: Boolean = true,
    unsyncedCount: Int = 0,
    onSyncUnsyncedClick: () -> Unit = {},
    initialPrefillData: Triple<String, String, String>? = null,
    onClearPrefillData: () -> Unit = {},
    uploadStatusMessage: String? = null,
    onSavePekerjaan: (PekerjaanServiceKeindahanEntity, (Boolean, String) -> Unit) -> Unit,
    onDeletePekerjaan: (Long, (Boolean, String) -> Unit) -> Unit,
    onSyncClick: () -> Unit,
    onInisialisasiClick: ((Boolean, String) -> Unit) -> Unit,
    onTestDrivePermission: (((Boolean, String) -> Unit) -> Unit)? = null,
    onUploadPhotoToDrive: ((String, String, (Boolean, String, String) -> Unit) -> Unit)? = null,
    onUpdateScriptUrl: ((String) -> Unit)? = null,
    currentScriptUrl: String = "",
    onVehicleClick: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    // Dialogs
    var showFormDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<PekerjaanServiceKeindahanEntity?>(null) }
    var viewingItem by remember { mutableStateOf<PekerjaanServiceKeindahanEntity?>(null) }
    var zoomPhoto by remember { mutableStateOf<ZoomPhotoState?>(null) }
    var isInisialisasiRunning by remember { mutableStateOf(false) }

    // Otomatis buka form jika ada prefill data (dari tombol "Kerjakan" di Service Keindahan)
    androidx.compose.runtime.LaunchedEffect(initialPrefillData) {
        if (initialPrefillData != null) {
            val (prefillMob, prefillSrv, prefillNotif) = initialPrefillData
            editingItem = PekerjaanServiceKeindahanEntity(
                id = 0L,
                noMobil = prefillMob,
                jenisService = prefillSrv,
                notifNo = prefillNotif
            )
            showFormDialog = true
            onClearPrefillData()
        }
    }

    var isTestingDrive by remember { mutableStateOf(false) }
    var showDriveHelpDialog by remember { mutableStateOf(false) }
    var driveHelpErrorMsg by remember { mutableStateOf("") }
    var showScriptUrlDialog by remember { mutableStateOf(false) }

    // Data yang ditampilkan maksimal 50 data terbaru, data terlama otomatis disembunyikan (hidden)
    val baseItems = remember(items) {
        items.sortedWith(
            compareByDescending<PekerjaanServiceKeindahanEntity> {
                val time = parseTimestampToMillis(it.tanggal)
                if (time > 0L) time else it.id
            }.thenByDescending { it.id }
        ).take(50)
    }

    // Filter list berdasarkan pencarian
    val filteredList = remember(baseItems, searchQuery) {
        if (searchQuery.isBlank()) baseItems else {
            val q = searchQuery.trim().lowercase()
            baseItems.filter { item ->
                item.noMobil.lowercase().contains(q) ||
                        item.jenisService.lowercase().contains(q) ||
                        item.nipNama.lowercase().contains(q) ||
                        item.approvel.lowercase().contains(q) ||
                        item.notifNo.lowercase().contains(q) ||
                        item.orderNo.lowercase().contains(q) ||
                        item.tanggal.lowercase().contains(q) ||
                        item.km.lowercase().contains(q) ||
                        item.id.toString().contains(q)
            }
        }
    }

    // Stats calculations
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayCount = remember(baseItems) { baseItems.count { it.tanggal.startsWith(todayDateStr) } }
    val syncedCount = remember(baseItems) { baseItems.count { it.isSynced } }
    val totalPhotosUploaded = remember(baseItems) {
        baseItems.sumOf { item ->
            var count = 0
            if (item.photoDepan.startsWith("http")) count++
            if (item.photoSampingLh.startsWith("http")) count++
            if (item.photoInterior.startsWith("http")) count++
            if (item.photoBelakang.startsWith("http")) count++
            if (item.photoSampingRh.startsWith("http")) count++
            count
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HighDensityBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. BANNER UTAMA (DATABASE & GOOGLE DRIVE INTEGRATION)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Blue50,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, HighDensityBlueBorder),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = HighDensityBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "RIWAYAT SERVICE KEINDAHAN",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Blue900,
                                letterSpacing = 0.3.sp
                            )
                        )
                    }

                    // Action button: Hanya sinkronisasi
                    IconButton(
                        onClick = onSyncClick,
                        enabled = !isSyncing,
                        modifier = Modifier.size(36.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = HighDensityBlue)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sinkronkan",
                                tint = HighDensityBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pencarian
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Slate400, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    placeholder = { Text("Cari No Mobil, Order, Jenis Service, NIP/Nama, Approvel...", fontSize = 12.5.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

            }
        }

        // 2. STAT CARD (PENGERJAAN HARI INI)
        PekerjaanStatCard(
            title = "Pengerjaan Hari Ini",
            value = todayCount.toString(),
            icon = Icons.Default.Speed,
            color = Color(0xFF0284C7),
            modifier = Modifier.fillMaxWidth()
        )

        // Banner Status Upload Foto ke Google Drive Otomatis
        if (!uploadStatusMessage.isNullOrBlank()) {
            val isSuccessMsg = uploadStatusMessage.startsWith("✅")
            val isErrorMsg = uploadStatusMessage.startsWith("⚠️")
            val bannerBg = when {
                isSuccessMsg -> Color(0xFFECFDF5)
                isErrorMsg -> Color(0xFFFEF2F2)
                else -> Color(0xFFEFF6FF)
            }
            val bannerBorder = when {
                isSuccessMsg -> Color(0xFFA7F3D0)
                isErrorMsg -> Color(0xFFFECACA)
                else -> Color(0xFFBFDBFE)
            }
            val bannerText = when {
                isSuccessMsg -> Color(0xFF065F46)
                isErrorMsg -> Color(0xFF991B1B)
                else -> Color(0xFF1E40AF)
            }

            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = bannerBg),
                border = BorderStroke(1.dp, bannerBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isSuccessMsg && !isErrorMsg) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = bannerText
                        )
                    } else {
                        Icon(
                            imageVector = if (isSuccessMsg) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                            contentDescription = "Status Upload Drive",
                            tint = bannerText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uploadStatusMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = bannerText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. KONTEN (TABEL RESMI 12 KOLOM ATAU KARTU)
        if (filteredList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            color = Blue50,
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "Tidak ada hasil pencarian untuk '$searchQuery'"
                            } else {
                                "Belum Ada Data Riwayat Service Keindahan"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Blue900),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "Periksa kata kunci pencarian Anda."
                            } else {
                                "Belum ada data riwayat pekerjaan keindahan."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onSyncClick) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sinkronkan Google Sheets")
                            }
                        }
                    }
                }
            }
        } else {
            // TAMPILAN KARTU (Kondisi Kartu)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    PekerjaanKeindahanCardItem(
                        item = item,
                        onEdit = {
                            editingItem = item
                            showFormDialog = true
                        },
                        onDetail = { viewingItem = item },
                        onZoomPhoto = { url, title, vehicle ->
                            zoomPhoto = ZoomPhotoState(url = url, title = title, vehicle = vehicle)
                        },
                        onVehicleClick = onVehicleClick
                    )
                }
            }
        }
    }

        // DIALOG FORM TAMBAH / EDIT
    if (showFormDialog) {
        FormPekerjaanServiceKeindahanDialog(
            item = editingItem,
            kendaraanList = kendaraanList,
            currentUser = currentUser,
            onDismiss = { showFormDialog = false },
            onUploadPhotoToDrive = onUploadPhotoToDrive,
            onSave = { entityToSave, callback ->
                showFormDialog = false
                editingItem = null
                onSavePekerjaan(entityToSave) { success, msg ->
                    callback(success, msg)
                    if (!success) {
                        if (msg.contains("DriveApp") || msg.contains("Izin") || msg.contains("Google Drive")) {
                            driveHelpErrorMsg = msg
                            showDriveHelpDialog = true
                        }
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onShowDriveHelp = { err ->
                driveHelpErrorMsg = err
                showDriveHelpDialog = true
            }
        )
    }

    if (showDriveHelpDialog) {
        DriveActivationHelpDialog(
            errorMessage = driveHelpErrorMsg,
            onDismiss = { showDriveHelpDialog = false },
            onTestAgain = {
                if (onTestDrivePermission != null) {
                    isTestingDrive = true
                    onTestDrivePermission { success, msg ->
                        isTestingDrive = false
                        if (success) {
                            showDriveHelpDialog = false
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        } else {
                            driveHelpErrorMsg = msg
                            Toast.makeText(context, "Izin masih belum aktif: $msg", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Fungsi tes izin tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            },
            isTesting = isTestingDrive
        )
    }

    if (showScriptUrlDialog) {
        ScriptUrlEditDialog(
            currentUrl = currentScriptUrl,
            onDismiss = { showScriptUrlDialog = false },
            onSaveUrl = { newUrl ->
                onUpdateScriptUrl?.invoke(newUrl)
            }
        )
    }

    // DIALOG RINCIAN LENGKAP
    viewingItem?.let { item ->
        DetailPekerjaanDialog(
            item = item,
            currentUser = currentUser,
            onDismiss = { viewingItem = null },
            onEdit = {
                viewingItem = null
                editingItem = item
                showFormDialog = true
            },
            onUpdateApproval = { targetItem, newStatus ->
                val updatedItem = targetItem.copy(approvel = newStatus)
                onSavePekerjaan(updatedItem) { success, msg ->
                    if (success) {
                        viewingItem = updatedItem
                        Toast.makeText(context, "Approvel Mobil #${targetItem.noMobil} berhasil diubah: $newStatus", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Gagal mengubah approvel: $msg", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onZoomPhoto = { url, title, vehicle ->
                zoomPhoto = ZoomPhotoState(url = url, title = title, vehicle = vehicle)
            }
        )
    }

    // DIALOG ZOOM FOTO FULLSCREEN
    zoomPhoto?.let { zp ->
        PhotoZoomDialog(
            state = zp,
            onDismiss = { zoomPhoto = null }
        )
    }
}

data class ZoomPhotoState(
    val url: String,
    val title: String,
    val vehicle: String
)

@Composable
private fun PekerjaanStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text(text = title, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Slate600)
                    Text(text = "$value Pengerjaan", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Blue900)
                }
            }
        }
    }
}

/**
 * TAMPILAN TABEL RESMI:
 * ID | TANGGAL | NO_MOBIL | JENIS_SERVICE | NIP/NAMA | NOTIF_NO | ORDER | APPROVEL | PHOTO_DEPAN | PHOTO_SAMPING_LH | PHOTO_INTERIOR | PHOTO_BELAKANG | PHOTO_SAMPING_RH | AKSI
 */
@Composable
private fun PekerjaanKeindahanTableView(
    items: List<PekerjaanServiceKeindahanEntity>,
    onEdit: (PekerjaanServiceKeindahanEntity) -> Unit,
    onDetail: (PekerjaanServiceKeindahanEntity) -> Unit,
    onZoomPhoto: (String, String, String) -> Unit,
    onVehicleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colIdWidth = 90.dp
    val colTanggalWidth = 140.dp
    val colNoMobilWidth = 110.dp
    val colJenisServiceWidth = 145.dp
    val colNipNamaWidth = 160.dp
    val colNotifNoWidth = 110.dp
    val colOrderWidth = 110.dp
    val colApprovelWidth = 115.dp
    val colPhotoWidth = 105.dp
    val colAksiWidth = 85.dp

    val totalWidth = colIdWidth + colTanggalWidth + colNoMobilWidth + colJenisServiceWidth +
            colNipNamaWidth + colNotifNoWidth + colOrderWidth + colApprovelWidth +
            (colPhotoWidth * 5) + colAksiWidth

    val cellBorder = Color(0xFFCBD5E1)
    val headerBorder = Color(0x33FFFFFF)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, cellBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        val hScroll = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(hScroll)
                .width(totalWidth)
        ) {
            // HEADER TABEL (Deep Blue #003366, Teks Putih Tebal)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF003366))
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("ID", colIdWidth)
                TableDivider(headerBorder)
                TableHeaderCell("TANGGAL", colTanggalWidth)
                TableDivider(headerBorder)
                TableHeaderCell("NO MOBIL", colNoMobilWidth)
                TableDivider(headerBorder)
                TableHeaderCell("JENIS SERVICE", colJenisServiceWidth)
                TableDivider(headerBorder)
                TableHeaderCell("NIP/NAMA", colNipNamaWidth)
                TableDivider(headerBorder)
                TableHeaderCell("NOTIF NO", colNotifNoWidth)
                TableDivider(headerBorder)
                TableHeaderCell("ORDER", colOrderWidth)
                TableDivider(headerBorder)
                TableHeaderCell("APPROVEL", colApprovelWidth)
                TableDivider(headerBorder)
                TableHeaderCell("PHOTO DEPAN", colPhotoWidth)
                TableDivider(headerBorder)
                TableHeaderCell("PHOTO SAMPING LH", colPhotoWidth)
                TableDivider(headerBorder)
                TableHeaderCell("PHOTO INTERIOR", colPhotoWidth)
                TableDivider(headerBorder)
                TableHeaderCell("PHOTO BELAKANG", colPhotoWidth)
                TableDivider(headerBorder)
                TableHeaderCell("PHOTO SAMPING RH", colPhotoWidth)
                TableDivider(headerBorder)
                TableHeaderCell("AKSI", colAksiWidth)
            }

            // BARIS TABEL
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items, key = { it.id }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .border(BorderStroke(0.5.dp, Color(0xFFE2E8F0)))
                            .clickable { onDetail(item) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. ID & SYNC STATUS
                        Box(modifier = Modifier.width(colIdWidth), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (item.id > 0L) item.id.toString() else "-",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = Slate600, fontWeight = FontWeight.SemiBold),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    color = if (item.isSynced) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(3.dp),
                                    border = BorderStroke(0.5.dp, if (item.isSynced) Color(0xFF86EFAC) else Color(0xFFFDE68A))
                                ) {
                                    Text(
                                        text = if (item.isSynced) "Cloud" else "Cache",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isSynced) Color(0xFF15803D) else Color(0xFFB45309)
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        TableDivider(Color(0xFFE2E8F0))

                        // 2. TANGGAL
                        Box(modifier = Modifier.width(colTanggalWidth), contentAlignment = Alignment.Center) {
                            Text(
                                text = item.tanggal.ifBlank { "-" },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, color = Slate800, fontWeight = FontWeight.Medium),
                                textAlign = TextAlign.Center
                            )
                        }
                        TableDivider(Color(0xFFE2E8F0))

                        // 3. NO MOBIL (Pill clickable -> muncul rincian)
                        Box(
                            modifier = Modifier
                                .width(colNoMobilWidth)
                                .clickable { onDetail(item) },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = Blue50,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, HighDensityBlueBorder),
                                modifier = Modifier.clickable { onDetail(item) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "#${item.noMobil}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = HighDensityBlue)
                                    )
                                }
                            }
                        }
                        TableDivider(Color(0xFFE2E8F0))

                        // 3. JENIS SERVICE
                        Box(modifier = Modifier.width(colJenisServiceWidth).padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                            ) {
                                Text(
                                    text = item.jenisService.ifBlank { "-" },
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Blue900, fontSize = 11.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        TableDivider(Color(0xFFE2E8F0))

                        // 4. NIP/NAMA (sesuai user login)
                        Box(modifier = Modifier.width(colNipNamaWidth).padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                            Surface(
                                color = Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = item.nipNama.ifBlank { "-" },
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Slate800, fontSize = 11.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        TableDivider(Color(0xFFE2E8F0))

                        // 5. NOTIF NO
                        Box(modifier = Modifier.width(colNotifNoWidth), contentAlignment = Alignment.Center) {
                            Text(
                                text = item.notifNo.ifBlank { "-" },
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontSize = 11.5.sp),
                                textAlign = TextAlign.Center
                            )
                        }
                        TableDivider(Color(0xFFE2E8F0))

                        // 6. ORDER
                        Box(modifier = Modifier.width(colOrderWidth), contentAlignment = Alignment.Center) {
                            Surface(
                                color = Color(0xFFEFF6FF),
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, Color(0xFFBFDBFE))
                            ) {
                                Text(
                                    text = item.orderNo.ifBlank { "-" },
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), fontSize = 11.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        TableDivider(Color(0xFFE2E8F0))

                        // 7. APPROVEL
                        Box(modifier = Modifier.width(colApprovelWidth), contentAlignment = Alignment.Center) {
                            val app = item.approvel.trim()
                            val isApproved = app.contains("setuju", ignoreCase = true) || app.contains("approv", ignoreCase = true)
                            val isRejected = app.contains("tolak", ignoreCase = true)
                            val badgeBg = if (isApproved) Color(0xFFDCFCE7) else if (isRejected) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
                            val badgeBorder = if (isApproved) Color(0xFF86EFAC) else if (isRejected) Color(0xFFFCA5A5) else Color(0xFFFDE68A)
                            val badgeColor = if (isApproved) Color(0xFF15803D) else if (isRejected) Color(0xFFDC2626) else Color(0xFFB45309)
                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, badgeBorder)
                            ) {
                                Text(
                                    text = if (app.isNotBlank()) app else "Menunggu",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = badgeColor, fontSize = 10.5.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    maxLines = 1
                                )
                            }
                        }
                        TableDivider(Color(0xFFE2E8F0))

                        // 8. PHOTO DEPAN
                        TablePhotoCell(
                            photoUrl = item.photoDepan,
                            label = "Photo Depan",
                            vehicle = item.noMobil,
                            width = colPhotoWidth,
                            onZoom = onZoomPhoto
                        )
                        TableDivider(Color(0xFFE2E8F0))

                        // 9. PHOTO SAMPING LH
                        TablePhotoCell(
                            photoUrl = item.photoSampingLh,
                            label = "Photo Samping LH",
                            vehicle = item.noMobil,
                            width = colPhotoWidth,
                            onZoom = onZoomPhoto
                        )
                        TableDivider(Color(0xFFE2E8F0))

                        // 10. PHOTO INTERIOR
                        TablePhotoCell(
                            photoUrl = item.photoInterior,
                            label = "Photo Interior",
                            vehicle = item.noMobil,
                            width = colPhotoWidth,
                            onZoom = onZoomPhoto
                        )
                        TableDivider(Color(0xFFE2E8F0))

                        // 11. PHOTO BELAKANG
                        TablePhotoCell(
                            photoUrl = item.photoBelakang,
                            label = "Photo Belakang",
                            vehicle = item.noMobil,
                            width = colPhotoWidth,
                            onZoom = onZoomPhoto
                        )
                        TableDivider(Color(0xFFE2E8F0))

                        // 12. PHOTO SAMPING RH
                        TablePhotoCell(
                            photoUrl = item.photoSampingRh,
                            label = "Photo Samping RH",
                            vehicle = item.noMobil,
                            width = colPhotoWidth,
                            onZoom = onZoomPhoto
                        )
                        TableDivider(Color(0xFFE2E8F0))

                        // 13. AKSI
                        val isItemApproved = item.approvel.trim().contains("setuju", ignoreCase = true) || item.approvel.trim().contains("approv", ignoreCase = true)
                        Box(
                            modifier = Modifier.width(colAksiWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isItemApproved) {
                                IconButton(onClick = { onEdit(item) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = HighDensityBlue, modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Text("-", fontSize = 11.sp, color = Slate400)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun TableDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(color)
    )
}

@Composable
private fun TablePhotoCell(
    photoUrl: String,
    label: String,
    vehicle: String,
    width: androidx.compose.ui.unit.Dp,
    onZoom: (String, String, String) -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNotBlank()) {
            val isDriveUrl = photoUrl.startsWith("http")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (isDriveUrl) Color(0xFF16A34A) else Color(0xFFD97706)),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier
                        .size(width = 68.dp, height = 44.dp)
                        .clickable { onZoom(photoUrl, label, vehicle) }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = normalizeDriveThumbnailUrl(photoUrl, 280),
                            contentDescription = label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Badge logo status Google Drive vs Lokal (hanya logo status tanpa teks Drive)
                        Surface(
                            shape = RoundedCornerShape(topStart = 4.dp),
                            color = if (isDriveUrl) Color(0xDD15803D) else Color(0xDDD97706),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDriveUrl) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                    contentDescription = if (isDriveUrl) "Terupload" else "Lokal",
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                // Link teks untuk membuka resolusi lebih besar
                Row(
                    modifier = Modifier
                        .clickable { onZoom(photoUrl, label, vehicle) }
                        .padding(horizontal = 2.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDriveUrl) Icons.Default.CheckCircle else Icons.Default.ZoomIn,
                        contentDescription = "Status",
                        tint = if (isDriveUrl) Color(0xFF15803D) else HighDensityBlue,
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "HD ↗",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDriveUrl) Color(0xFF15803D) else HighDensityBlue
                    )
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.size(width = 68.dp, height = 44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("-", fontSize = 11.sp, color = Slate400)
                }
            }
        }
    }
}

/**
 * TAMPILAN KARTU PENGERJAAN SERVICE KEINDAHAN (CARD VIEW)
 */
@Composable
private fun PekerjaanKeindahanCardItem(
    item: PekerjaanServiceKeindahanEntity,
    onEdit: () -> Unit,
    onDetail: () -> Unit,
    onZoomPhoto: (String, String, String) -> Unit,
    onVehicleClick: (String) -> Unit
) {
    val app = item.approvel.trim()
    val isApproved = app.contains("setuju", ignoreCase = true) || app.contains("approv", ignoreCase = true)
    val isRejected = app.contains("tolak", ignoreCase = true)
    val badgeBg = if (isApproved) Color(0xFFDCFCE7) else if (isRejected) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
    val badgeBorder = if (isApproved) Color(0xFF86EFAC) else if (isRejected) Color(0xFFFCA5A5) else Color(0xFFFDE68A)
    val badgeColor = if (isApproved) Color(0xFF15803D) else if (isRejected) Color(0xFFDC2626) else Color(0xFFB45309)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetail() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: No Mobil, Approvel, & Aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // NO MOBIL
                    Surface(
                        color = Blue50,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, HighDensityBlueBorder),
                        modifier = Modifier.clickable { onDetail() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "#${item.noMobil}",
                                fontWeight = FontWeight.Bold,
                                color = HighDensityBlue,
                                fontSize = 13.5.sp
                            )
                        }
                    }

                    // APPROVEL BADGE
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, badgeBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isApproved) Icons.Default.CheckCircle else if (isRejected) Icons.Default.Cancel else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (app.isNotBlank()) app else "Menunggu",
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // SYNC STATUS BADGE
                    Surface(
                        color = if (item.isSynced) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.5.dp, if (item.isSynced) Color(0xFF86EFAC) else Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (item.isSynced) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = if (item.isSynced) Color(0xFF15803D) else Color(0xFFB45309),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (item.isSynced) "Cloud" else "Cache",
                                fontWeight = FontWeight.Bold,
                                color = if (item.isSynced) Color(0xFF15803D) else Color(0xFFB45309),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // TANGGAL (Tanggal dan Jam saat itu) & ID (Kode Unix)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Slate600, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.tanggal.ifBlank { "-" },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate600
                    )
                }

                // ID (Kode Unix)
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                ) {
                    Text(
                        text = "Unix ID: #${item.id}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate600,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid Metadata: JENIS SERVICE, NIP/NAMA, NOTIF NO, ORDER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                        Text("JENIS SERVICE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text(item.jenisService.ifBlank { "-" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue900, maxLines = 1)
                    }
                }

                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                        Text("NIP / NAMA MEKANIK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(item.nipNama.ifBlank { "-" }, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Slate800, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                        Text("NOTIF NO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate400)
                        Text(item.notifNo.ifBlank { "-" }, fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = Slate800, maxLines = 1)
                    }
                }

                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                        Text("ORDER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                        Text(item.orderNo.ifBlank { "-" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Foto 5 Sisi Preview Row dengan Status Logo Google Drive
            val photoUrlsList = listOf(item.photoDepan, item.photoSampingLh, item.photoInterior, item.photoBelakang, item.photoSampingRh)
            val countValidPhotos = photoUrlsList.count { it.isNotBlank() }
            val countDrivePhotos = photoUrlsList.count { it.startsWith("http", ignoreCase = true) }
            val allPhotosOnDrive = countValidPhotos > 0 && countDrivePhotos == countValidPhotos

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FOTO 5 SISI KENDARAAN:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate600)

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (allPhotosOnDrive) Color(0xFFDCFCE7) else if (countValidPhotos > 0) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                    border = BorderStroke(0.5.dp, if (allPhotosOnDrive) Color(0xFF86EFAC) else if (countValidPhotos > 0) Color(0xFFFCD34D) else Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (allPhotosOnDrive) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                            contentDescription = "Status Upload Google Drive",
                            tint = if (allPhotosOnDrive) Color(0xFF15803D) else if (countValidPhotos > 0) Color(0xFFB45309) else Slate400,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (allPhotosOnDrive) {
                                "Terupload ($countDrivePhotos/$countValidPhotos) ✓"
                            } else if (countValidPhotos > 0) {
                                "$countDrivePhotos/$countValidPhotos Terupload (Otomatis)"
                            } else {
                                "Belum Ada Foto"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allPhotosOnDrive) Color(0xFF15803D) else if (countValidPhotos > 0) Color(0xFFB45309) else Slate600
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PhotoThumbnailCard("Depan", item.photoDepan, item.noMobil, Modifier.weight(1f), onZoomPhoto)
                PhotoThumbnailCard("Samping LH", item.photoSampingLh, item.noMobil, Modifier.weight(1f), onZoomPhoto)
                PhotoThumbnailCard("Interior", item.photoInterior, item.noMobil, Modifier.weight(1f), onZoomPhoto)
                PhotoThumbnailCard("Belakang", item.photoBelakang, item.noMobil, Modifier.weight(1f), onZoomPhoto)
                PhotoThumbnailCard("Samping RH", item.photoSampingRh, item.noMobil, Modifier.weight(1f), onZoomPhoto)
            }
        }
    }
}

@Composable
private fun PhotoThumbnailCard(
    slotName: String,
    urlOrPath: String,
    vehicle: String,
    modifier: Modifier = Modifier,
    onZoom: (String, String, String) -> Unit
) {
    val isDriveUrl = urlOrPath.startsWith("http", ignoreCase = true)
    val hasPhoto = urlOrPath.isNotBlank()

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(
                if (hasPhoto) 1.dp else 0.5.dp,
                if (isDriveUrl) Color(0xFF16A34A) else if (hasPhoto) Color(0xFFD97706) else Color(0xFFCBD5E1)
            ),
            color = Color(0xFFF1F5F9),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable {
                    if (hasPhoto) {
                        onZoom(urlOrPath, "Photo $slotName", vehicle)
                    }
                }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (hasPhoto) {
                    AsyncImage(
                        model = normalizeDriveThumbnailUrl(urlOrPath, 300),
                        contentDescription = slotName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Logo Status di pojok foto (hanya logo status tanpa teks Drive)
                    Surface(
                        color = if (isDriveUrl) Color(0xDD15803D) else Color(0xDDD97706),
                        shape = RoundedCornerShape(bottomStart = 4.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDriveUrl) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                contentDescription = if (isDriveUrl) "Terupload" else "Lokal",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = slotName, fontSize = 9.sp, color = Slate600, textAlign = TextAlign.Center, maxLines = 1)
        if (hasPhoto) {
            Spacer(modifier = Modifier.height(1.dp))
            Surface(
                color = if (isDriveUrl) Color(0xFFDCFCE7) else Blue50,
                shape = RoundedCornerShape(3.dp),
                border = BorderStroke(0.5.dp, if (isDriveUrl) Color(0xFF86EFAC) else HighDensityBlueBorder),
                modifier = Modifier.clickable { onZoom(urlOrPath, "Photo $slotName", vehicle) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDriveUrl) Icons.Default.CheckCircle else Icons.Default.OpenInNew,
                        contentDescription = "Status",
                        tint = if (isDriveUrl) Color(0xFF15803D) else HighDensityBlue,
                        modifier = Modifier.size(8.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "HD ↗",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDriveUrl) Color(0xFF15803D) else HighDensityBlue
                    )
                }
            }
        }
    }
}

/**
 * FORM INPUT & EDIT PEKERJAAN SERVICE KEINDAHAN (DENGAN KAMERA & GALERI UNTUK 5 FOTO)
 */
@Composable
private fun FormPekerjaanServiceKeindahanDialog(
    item: PekerjaanServiceKeindahanEntity?,
    kendaraanList: List<MasterKendaraanEntity>,
    currentUser: UserEntity? = null,
    onDismiss: () -> Unit,
    onUploadPhotoToDrive: ((String, String, (Boolean, String, String) -> Unit) -> Unit)? = null,
    onSave: (PekerjaanServiceKeindahanEntity, (Boolean, String) -> Unit) -> Unit,
    onShowDriveHelp: (String) -> Unit
) {
    val context = LocalContext.current
    val isEdit = item != null && item.id > 0L

    val currentTimestampStr = remember {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    }

    var tanggal by remember {
        mutableStateOf(item?.tanggal?.ifBlank { currentTimestampStr } ?: currentTimestampStr)
    }
    var noMobil by remember { mutableStateOf(item?.noMobil ?: "") }
    var jenisService by remember { mutableStateOf(item?.jenisService ?: "P09") }

    val cleanNoMobil = remember(noMobil) { noMobil.trim() }
    val matchedKendaraan = remember(cleanNoMobil, kendaraanList) {
        if (cleanNoMobil.isBlank()) null
        else kendaraanList.firstOrNull { it.noMobil.trim().equals(cleanNoMobil, ignoreCase = true) }
    }
    val isNoMobilVerified = matchedKendaraan != null

    val cleanJenisService = remember(jenisService) { jenisService.trim() }
    val isJenisServiceValid = cleanJenisService.length >= 3
    
    // NIP/NAMA sesuai dengan user login dan tidak dapat diedit
    val loggedInNipNama = remember(currentUser, item) {
        if (item != null && item.nipNama.isNotBlank()) {
            item.nipNama
        } else if (currentUser != null) {
            val nip = currentUser.nip.trim()
            val nama = currentUser.nama.trim().ifBlank { currentUser.username.trim() }
            if (nip.isNotBlank() && nama.isNotBlank()) "$nip / $nama" else nip.ifBlank { nama }
        } else {
            "STAFF"
        }
    }

    var notifNo by remember { mutableStateOf(item?.notifNo ?: "") }
    var orderNo by remember { mutableStateOf(item?.orderNo ?: "") }
    var approvel by remember { mutableStateOf(item?.approvel?.ifBlank { "Menunggu" } ?: "Menunggu") }
    var km by remember { mutableStateOf(item?.km ?: "") }

    var photoDepan by remember { mutableStateOf(item?.photoDepan ?: "") }
    var photoSampingLh by remember { mutableStateOf(item?.photoSampingLh ?: "") }
    var photoInterior by remember { mutableStateOf(item?.photoInterior ?: "") }
    var photoBelakang by remember { mutableStateOf(item?.photoBelakang ?: "") }
    var photoSampingRh by remember { mutableStateOf(item?.photoSampingRh ?: "") }

    var isSaving by remember { mutableStateOf(false) }
    var uploadingSlot by remember { mutableStateOf<String?>(null) }

    fun uploadSlotToDrive(slotName: String, pathOrUri: String) {
        if (pathOrUri.isBlank() || pathOrUri.startsWith("http")) return
        if (onUploadPhotoToDrive == null) {
            Toast.makeText(context, "Fungsi upload ke Google Drive sedang tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        uploadingSlot = slotName
        val mob = if (noMobil.isNotBlank()) noMobil.trim().uppercase() else "MOBIL"
        val fileName = "KEINDAHAN_${mob}_${slotName}_${System.currentTimeMillis()}.jpg"
        onUploadPhotoToDrive(pathOrUri, fileName) { success, driveUrl, msg ->
            uploadingSlot = null
            if (success && driveUrl.isNotBlank()) {
                when (slotName) {
                    "DEPAN" -> photoDepan = driveUrl
                    "LH" -> photoSampingLh = driveUrl
                    "INTERIOR" -> photoInterior = driveUrl
                    "BELAKANG" -> photoBelakang = driveUrl
                    "RH" -> photoSampingRh = driveUrl
                }
                Toast.makeText(context, "Foto $slotName berhasil diunggah ke Google Drive!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal upload: $msg", Toast.LENGTH_LONG).show()
                if (msg.contains("DriveApp") || msg.contains("Izin") || msg.contains("Google Drive")) {
                    onShowDriveHelp(msg)
                }
            }
        }
    }

    // Activity Result Launchers per photo slot (Resolusi Tinggi / 4K Ultra HD)
    var currentSlotTarget by remember { mutableStateOf<String?>(null) }
    var currentCameraTempFile by remember { mutableStateOf<File?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val takePicture4kLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val file = currentCameraTempFile
        val slot = currentSlotTarget
        if (success && file != null && slot != null) {
            coroutineScope.launch {
                val savedPath = ImageCompressor.save4KImageFile(
                    context = context,
                    sourceFile = file,
                    prefix = "keindahan_4k_${slot.lowercase()}"
                )
                if (savedPath.isNotBlank()) {
                    when (slot) {
                        "DEPAN" -> photoDepan = savedPath
                        "LH" -> photoSampingLh = savedPath
                        "INTERIOR" -> photoInterior = savedPath
                        "BELAKANG" -> photoBelakang = savedPath
                        "RH" -> photoSampingRh = savedPath
                    }
                    Toast.makeText(context, "Foto [$slot] resolusi tinggi/HD berhasil diambil!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        currentCameraTempFile = null
        currentSlotTarget = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val slot = currentSlotTarget
        if (uri != null && slot != null) {
            coroutineScope.launch {
                val savedPath = ImageCompressor.save4KImageFromUri(
                    context = context,
                    sourceUri = uri,
                    prefix = "keindahan_4k_${slot.lowercase()}"
                )
                if (savedPath.isNotBlank()) {
                    when (slot) {
                        "DEPAN" -> photoDepan = savedPath
                        "LH" -> photoSampingLh = savedPath
                        "INTERIOR" -> photoInterior = savedPath
                        "BELAKANG" -> photoBelakang = savedPath
                        "RH" -> photoSampingRh = savedPath
                    }
                    Toast.makeText(context, "Foto [$slot] resolusi tinggi/HD berhasil dipilih dari galeri!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        currentSlotTarget = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val tempPair = ImageCompressor.createTempImageUri(context)
                if (tempPair != null) {
                    currentCameraTempFile = tempPair.second
                    takePicture4kLauncher.launch(tempPair.first)
                } else {
                    Toast.makeText(context, "Gagal membuat file foto kamera", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Tidak dapat membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto inspeksi.", Toast.LENGTH_LONG).show()
        }
    }

    fun openCamera(slot: String) {
        currentSlotTarget = slot
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            val tempPair = ImageCompressor.createTempImageUri(context)
            if (tempPair != null) {
                currentCameraTempFile = tempPair.second
                takePicture4kLauncher.launch(tempPair.first)
            } else {
                Toast.makeText(context, "Gagal membuat file foto kamera", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun openGallery(slot: String) {
        currentSlotTarget = slot
        galleryLauncher.launch("image/*")
    }

    val userLevel = remember(currentUser) {
        currentUser?.level?.trim()?.uppercase() ?: ""
    }
    val canEditApproval = remember(userLevel) {
        userLevel in listOf("SUPER ADMIN", "SH", "GH", "KEPALA BENGKEL", "ADMIN", "ALL", "ADMIN,ALL", "ADMIN, ALL")
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEdit) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = HighDensityBlue,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEdit) "Edit Pekerjaan Keindahan (#${item?.noMobil})" else if (noMobil.isNotBlank()) "Pekerjaan Keindahan (#$noMobil)" else "Input Pekerjaan Service Keindahan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // TANGGAL
                OutlinedTextField(
                    value = tanggal,
                    onValueChange = { /* tidak dapat di edit */ },
                    readOnly = true,
                    label = { Text("TANGGAL") },
                    leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = HighDensityBlue) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color(0xFF94A3B8),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // NO MOBIL
                OutlinedTextField(
                    value = noMobil,
                    onValueChange = { /* tidak dapat di edit */ },
                    readOnly = true,
                    label = { Text("NO MOBIL") },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = HighDensityBlue) },
                    supportingText = if (matchedKendaraan?.tipe?.isNotBlank() == true) {
                        {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(13.dp))
                                Text(
                                    text = "Terverifikasi • ${matchedKendaraan.tipe}",
                                    color = Color(0xFF16A34A),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color(0xFF94A3B8),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // JENIS SERVICE
                OutlinedTextField(
                    value = jenisService,
                    onValueChange = { /* tidak dapat di edit */ },
                    readOnly = true,
                    label = { Text("JENIS SERVICE") },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HighDensityBlue) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color(0xFF94A3B8),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // NIP/NAMA
                OutlinedTextField(
                    value = loggedInNipNama,
                    onValueChange = { /* tidak dapat di edit */ },
                    readOnly = true,
                    label = { Text("NIP/NAMA") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = HighDensityBlue) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color(0xFF94A3B8),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // NOTIF NO & ORDER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = notifNo,
                        onValueChange = { /* tidak dapat di edit */ },
                        readOnly = true,
                        label = { Text("NOTIF NO") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedBorderColor = Color(0xFF94A3B8),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = orderNo,
                        onValueChange = { orderNo = it },
                        label = { Text("ORDER") },
                        placeholder = { Text("50021490") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // APPROVEL (Hanya level SUPER ADMIN | SH | GH | KEPALA BENGKEL yang bisa isi dan edit)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("APPROVEL:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Blue900)
                        if (!canEditApproval) {
                            Text(
                                text = "(Hanya SUPER ADMIN | SH | GH | KEPALA BENGKEL)",
                                fontSize = 10.sp,
                                color = Slate600
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Menunggu", "Disetujui", "Ditolak").forEach { status ->
                            val isSelected = approvel.equals(status, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                enabled = canEditApproval,
                                onClick = {
                                    if (canEditApproval) {
                                        approvel = status
                                    }
                                },
                                label = { Text(status, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (status) {
                                        "Disetujui" -> Color(0xFFDCFCE7)
                                        "Ditolak" -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFFEF3C7)
                                    },
                                    selectedLabelColor = when (status) {
                                        "Disetujui" -> Color(0xFF15803D)
                                        "Ditolak" -> Color(0xFFDC2626)
                                        else -> Color(0xFFB45309)
                                    },
                                    disabledContainerColor = Color(0xFFF1F5F9),
                                    disabledSelectedContainerColor = when (status) {
                                        "Disetujui" -> Color(0xFFDCFCE7)
                                        "Ditolak" -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFFEF3C7)
                                    }
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFCBD5E1))

                // FOTO 5 SISI INSPEKSI KEINDAHAN
                Text(
                    text = "FOTO INSPEKSI 5 SISI KENDARAAN (GOOGLE DRIVE):",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                )

                // 1. PHOTO DEPAN
                PhotoSlotUploader(
                    label = "1. PHOTO DEPAN",
                    photoUrl = photoDepan,
                    isUploading = uploadingSlot == "DEPAN",
                    onUploadToDrive = { uploadSlotToDrive("DEPAN", photoDepan) },
                    onCaptureCamera = { openCamera("DEPAN") },
                    onPickGallery = { openGallery("DEPAN") },
                    onClear = { photoDepan = "" }
                )

                // 2. PHOTO SAMPING LH
                PhotoSlotUploader(
                    label = "2. PHOTO SAMPING LH (Kiri)",
                    photoUrl = photoSampingLh,
                    isUploading = uploadingSlot == "LH",
                    onUploadToDrive = { uploadSlotToDrive("LH", photoSampingLh) },
                    onCaptureCamera = { openCamera("LH") },
                    onPickGallery = { openGallery("LH") },
                    onClear = { photoSampingLh = "" }
                )

                // 3. PHOTO INTERIOR
                PhotoSlotUploader(
                    label = "3. PHOTO INTERIOR (Kabin/Jok)",
                    photoUrl = photoInterior,
                    isUploading = uploadingSlot == "INTERIOR",
                    onUploadToDrive = { uploadSlotToDrive("INTERIOR", photoInterior) },
                    onCaptureCamera = { openCamera("INTERIOR") },
                    onPickGallery = { openGallery("INTERIOR") },
                    onClear = { photoInterior = "" }
                )

                // 4. PHOTO BELAKANG
                PhotoSlotUploader(
                    label = "4. PHOTO BELAKANG",
                    photoUrl = photoBelakang,
                    isUploading = uploadingSlot == "BELAKANG",
                    onUploadToDrive = { uploadSlotToDrive("BELAKANG", photoBelakang) },
                    onCaptureCamera = { openCamera("BELAKANG") },
                    onPickGallery = { openGallery("BELAKANG") },
                    onClear = { photoBelakang = "" }
                )

                // 5. PHOTO SAMPING RH
                PhotoSlotUploader(
                    label = "5. PHOTO SAMPING RH (Kanan)",
                    photoUrl = photoSampingRh,
                    isUploading = uploadingSlot == "RH",
                    onUploadToDrive = { uploadSlotToDrive("RH", photoSampingRh) },
                    onCaptureCamera = { openCamera("RH") },
                    onPickGallery = { openGallery("RH") },
                    onClear = { photoSampingRh = "" }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cleanNoMobil.isBlank()) {
                        Toast.makeText(context, "No Mobil wajib ada!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (cleanJenisService.isBlank()) {
                        Toast.makeText(context, "Jenis service wajib ada!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val unixId = if (item != null && item.id > 0L) item.id else System.currentTimeMillis()
                    val finalApprovel = if (canEditApproval) {
                        approvel.trim().ifBlank { "Menunggu" }
                    } else {
                        item?.approvel?.trim()?.ifBlank { "Menunggu" } ?: "Menunggu"
                    }
                    val toSave = PekerjaanServiceKeindahanEntity(
                        id = unixId,
                        tanggal = tanggal.trim(),
                        noMobil = noMobil.trim(),
                        jenisService = jenisService.trim(),
                        nipNama = loggedInNipNama.trim(),
                        notifNo = notifNo.trim(),
                        orderNo = orderNo.trim(),
                        approvel = finalApprovel,
                        km = km.trim(),
                        photoDepan = photoDepan.trim(),
                        photoSampingLh = photoSampingLh.trim(),
                        photoInterior = photoInterior.trim(),
                        photoBelakang = photoBelakang.trim(),
                        photoSampingRh = photoSampingRh.trim(),
                        isSynced = false
                    )
                    // Langsung tutup form dialog dan jalankan proses simpan & upload di latar belakang
                    onDismiss()
                    onSave(toSave) { _, _ -> }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simpan & Otomatis Upload ke Drive")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun PhotoSlotUploader(
    label: String,
    photoUrl: String,
    isUploading: Boolean = false,
    onUploadToDrive: (() -> Unit)? = null,
    onCaptureCamera: () -> Unit,
    onPickGallery: () -> Unit,
    onClear: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue900
                )
                if (photoUrl.isNotBlank()) {
                    val isDrive = photoUrl.startsWith("http")
                    Surface(
                        color = if (isDrive) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(4.dp),
                        modifier = if (isDrive) Modifier.clickable { uriHandler.openUri(photoUrl) } else Modifier
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDrive) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (isDrive) Color(0xFF15803D) else Color(0xFFB45309),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDrive) "✓ Terupload ↗" else "Lokal (Siap Upload)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDrive) Color(0xFF15803D) else Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (photoUrl.isNotBlank()) {
                val isDrive = photoUrl.startsWith("http")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .size(width = 80.dp, height = 56.dp)
                            .clickable {
                                try {
                                    uriHandler.openUri(if (isDrive) getDriveWebUrl(photoUrl) else photoUrl)
                                } catch (_: Exception) {}
                            }
                    ) {
                        AsyncImage(
                            model = normalizeDriveThumbnailUrl(photoUrl, 320),
                            contentDescription = label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onCaptureCamera,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kamera", fontSize = 10.sp)
                            }
                            OutlinedButton(
                                onClick = onPickGallery,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Galeri", fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isDrive && onUploadToDrive != null) {
                                Button(
                                    onClick = onUploadToDrive,
                                    enabled = !isUploading,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    if (isUploading) {
                                        CircularProgressIndicator(modifier = Modifier.size(10.dp), color = Color.White, strokeWidth = 1.5.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Unggah...", fontSize = 9.5.sp)
                                    } else {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Upload Sekarang", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            TextButton(
                                onClick = {
                                    try {
                                        uriHandler.openUri(if (isDrive) getDriveWebUrl(photoUrl) else photoUrl)
                                    } catch (_: Exception) {}
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(10.dp), tint = HighDensityBlue)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Resolusi Besar ↗", fontSize = 9.5.sp, color = HighDensityBlue, fontWeight = FontWeight.Bold)
                            }

                            TextButton(
                                onClick = onClear,
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Hapus", fontSize = 10.5.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCaptureCamera,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kamera", fontSize = 11.5.sp, color = HighDensityBlue)
                    }
                    OutlinedButton(
                        onClick = onPickGallery,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galeri HP", fontSize = 11.5.sp, color = HighDensityBlue)
                    }
                }
            }
        }
    }
}

/**
 * DIALOG RINCIAN PENUH DENGAN 5 FOTO BESAR
 */
@Composable
private fun DetailPekerjaanDialog(
    item: PekerjaanServiceKeindahanEntity,
    currentUser: UserEntity? = null,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onUpdateApproval: (PekerjaanServiceKeindahanEntity, String) -> Unit = { _, _ -> },
    onZoomPhoto: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val userLevel = remember(currentUser) {
        currentUser?.level?.trim()?.uppercase() ?: ""
    }
    val canEditApproval = remember(userLevel, currentUser) {
        currentUser == null || userLevel in listOf("SUPER ADMIN", "SH", "GH", "KEPALA BENGKEL", "ADMIN", "ALL", "ADMIN,ALL", "ADMIN, ALL")
    }
    val isApproved = item.approvel.trim().contains("setuju", ignoreCase = true) || item.approvel.trim().contains("approv", ignoreCase = true)
    val isRejected = item.approvel.trim().contains("tolak", ignoreCase = true)
    var showApprovalMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Detail Pekerjaan Mobil #${item.noMobil}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRowItem("ID (KODE UNIX)", "#${item.id}")
                DetailRowItem("TANGGAL", item.tanggal)
                DetailRowItem("NO MOBIL", item.noMobil)
                DetailRowItem("JENIS SERVICE", item.jenisService)
                DetailRowItem("NIP/NAMA", item.nipNama.ifBlank { "-" })
                DetailRowItem("NOTIF NO", item.notifNo)
                DetailRowItem("ORDER", item.orderNo)

                // APPROVEL DENGAN BADGE STATUS BERWARNA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "APPROVEL", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate600)
                    Surface(
                        color = if (isApproved) Color(0xFFDCFCE7) else if (isRejected) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, if (isApproved) Color(0xFF86EFAC) else if (isRejected) Color(0xFFFCA5A5) else Color(0xFFFCD34D))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isApproved) Icons.Default.CheckCircle else if (isRejected) Icons.Default.Cancel else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isApproved) Color(0xFF15803D) else if (isRejected) Color(0xFFDC2626) else Color(0xFFB45309),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.approvel.ifBlank { "Menunggu" },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isApproved) Color(0xFF15803D) else if (isRejected) Color(0xFFDC2626) else Color(0xFFB45309)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFCBD5E1))

                Text("FOTO 5 SISI KENDARAAN:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Blue900)

                DetailPhotoSlot("PHOTO DEPAN", item.photoDepan, item.noMobil, onZoomPhoto)
                DetailPhotoSlot("PHOTO SAMPING LH (Kiri)", item.photoSampingLh, item.noMobil, onZoomPhoto)
                DetailPhotoSlot("PHOTO INTERIOR", item.photoInterior, item.noMobil, onZoomPhoto)
                DetailPhotoSlot("PHOTO BELAKANG", item.photoBelakang, item.noMobil, onZoomPhoto)
                DetailPhotoSlot("PHOTO SAMPING RH (Kanan)", item.photoSampingRh, item.noMobil, onZoomPhoto)
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TOMBOL APPROVEL DI SETUJUI (JIKA STATUS SUDAH DISETUJUI MAKA TOMBOL APPROVEL HIDDEN)
                if (!isApproved) {
                    androidx.compose.foundation.layout.Box {
                        Button(
                            onClick = {
                                if (!canEditApproval) {
                                    Toast.makeText(context, "Hanya SUPER ADMIN, SH, GH, atau KEPALA BENGKEL yang dapat menyetujui", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (isApproved) {
                                        showApprovalMenu = true
                                    } else {
                                        onUpdateApproval(item, "Disetujui")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isApproved) Color(0xFF15803D) else Color(0xFF16A34A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isApproved) "Disetujui ✓" else "Setujui",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (canEditApproval) {
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilihan Approvel",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        if (canEditApproval) {
                            DropdownMenu(
                                expanded = showApprovalMenu,
                                onDismissRequest = { showApprovalMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Disetujui", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                        }
                                    },
                                    onClick = {
                                        showApprovalMenu = false
                                        onUpdateApproval(item, "Disetujui")
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Menunggu", fontWeight = FontWeight.Medium, color = Color(0xFFB45309))
                                        }
                                    },
                                    onClick = {
                                        showApprovalMenu = false
                                        onUpdateApproval(item, "Menunggu")
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Ditolak", fontWeight = FontWeight.Medium, color = Color(0xFFDC2626))
                                        }
                                    },
                                    onClick = {
                                        showApprovalMenu = false
                                        onUpdateApproval(item, "Ditolak")
                                    }
                                )
                            }
                        }
                    }
                }

                // TOMBOL EDIT DATA (HANYA MUNCUL JIKA STATUS BELUM DISETUJUI, JIKA DISETUJUI MAKA HIDDEN)
                if (!isApproved) {
                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun DetailRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate600)
        Text(text = value.ifBlank { "-" }, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Slate800)
    }
}

@Composable
private fun DetailPhotoSlot(
    title: String,
    url: String,
    vehicle: String,
    onZoom: (String, String, String) -> Unit
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (url.isNotBlank()) onZoom(url, title, vehicle)
            }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.size(width = 64.dp, height = 48.dp)
            ) {
                if (url.isNotBlank()) {
                    AsyncImage(
                        model = normalizeDriveThumbnailUrl(url, 320),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text("-", color = Slate400, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Blue900)
                Spacer(modifier = Modifier.height(2.dp))
                val isDriveUrl = url.startsWith("http", ignoreCase = true)
                if (url.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isDriveUrl) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        border = BorderStroke(0.5.dp, if (isDriveUrl) Color(0xFF86EFAC) else Color(0xFFFCD34D))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDriveUrl) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                contentDescription = "Status Upload Drive",
                                tint = if (isDriveUrl) Color(0xFF15803D) else Color(0xFFB45309),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isDriveUrl) "✓ Terupload" else "⏳ Belum Terupload (Lokal)",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDriveUrl) Color(0xFF15803D) else Color(0xFFB45309)
                            )
                        }
                    }
                } else {
                    Text(text = "Foto Belum Ada", fontSize = 10.sp, color = Slate400)
                }
                if (url.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.clickable { onZoom(url, title, vehicle) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Buka Resolusi Besar (HD) ↗", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = HighDensityBlue)
                    }
                }
            }
            if (url.isNotBlank()) {
                IconButton(onClick = { onZoom(url, title, vehicle) }) {
                    Icon(Icons.Default.ZoomIn, contentDescription = "Perbesar", tint = HighDensityBlue, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

/**
 * DIALOG ZOOM / LIGHTBOX FOTO RESOLUSI BESAR
 */
@Composable
private fun PhotoZoomDialog(
    state: ZoomPhotoState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 4f)
        scale = newScale
        if (newScale > 1f) {
            val maxOffset = 500f * (newScale - 1f)
            offset = Offset(
                x = (offset.x + offsetChange.x).coerceIn(-maxOffset, maxOffset),
                y = (offset.y + offsetChange.y).coerceIn(-maxOffset, maxOffset)
            )
        } else {
            offset = Offset.Zero
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Mobil #${state.vehicle} • Resolusi Penuh HD/4K (Pinch / Ketuk 2x untuk Perbesar)",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (scale > 1.2f) {
                                            scale = 1f
                                            offset = Offset.Zero
                                        } else {
                                            scale = 2.5f
                                            offset = Offset.Zero
                                        }
                                    }
                                )
                            }
                            .transformable(state = transformableState),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(normalizeDriveHighResUrl(state.url))
                                .size(coil.size.Size.ORIGINAL)
                                .crossfade(true)
                                .build(),
                            contentDescription = state.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = HighDensityBlue)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Memuat foto resolusi tinggi...", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            },
                            error = {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Tidak dapat menampilkan gambar langsung.", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Bar: Link Buka di Google Drive / Browser & Salin Link Resolusi Besar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val highResWebUrl = getDriveWebUrl(state.url)
                    val isWeb = state.url.startsWith("http")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isWeb) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(highResWebUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Tidak dapat membuka browser: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Buka Browser ↗", fontSize = 11.5.sp, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Link Foto", highResWebUrl)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Link foto resolusi besar berhasil disalin!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salin Link 📋", fontSize = 11.5.sp, maxLines = 1)
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Path Foto", state.url)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Path foto berhasil disalin!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salin Lokasi File", fontSize = 11.5.sp, maxLines = 1)
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Tutup", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog Panduan Aktivasi Izin Google Drive (DriveApp) 1-Menit
 */
@Composable
private fun DriveActivationHelpDialog(
    errorMessage: String,
    onDismiss: () -> Unit,
    onTestAgain: () -> Unit,
    isTesting: Boolean
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFFED7AA)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Aktivasi Izin Google Drive", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900))
                            Text("Hanya 1x Langkah (1 Menit)", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate600)
                    }
                }

                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Foto tersimpan di HP, tetapi Google Drive menolak upload karena Web App belum diberi izin akses DriveApp oleh pemilik akun Google.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF92400E), fontSize = 12.sp)
                        )
                    }
                }

                Text("4 LANGKAH CEPAT AKTIVASI IZIN:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Blue900)

                StepItem(
                    num = "1",
                    title = "Buka Google Apps Script Editor",
                    desc = "Buka project script di browser melalui script.google.com"
                )

                StepItem(
                    num = "2",
                    title = "Pilih Fungsi 'aktivasiIzinGoogleDrive'",
                    desc = "Pada dropdown fungsi di toolbar atas sebelah tombol Run ▶, pilih fungsi: aktivasiIzinGoogleDrive"
                )

                StepItem(
                    num = "3",
                    title = "Klik Run (▶) & Setujui Izin",
                    desc = "Klik Review Permissions > Pilih Akun Google > Advanced (Lanjutan) > Go to ... (unsafe) > Allow (Izinkan)."
                )

                StepItem(
                    num = "4",
                    title = "Deploy Versi Baru",
                    desc = "Klik tombol biru 'Deploy' di kanan atas > Manage deployments > ikon Pensil (Edit) > Version: New version > Deploy."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("function_name", "aktivasiIzinGoogleDrive"))
                            Toast.makeText(context, "Nama fungsi 'aktivasiIzinGoogleDrive' disalin!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin Fungsi", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://script.google.com"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Tidak dapat membuka browser", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buka Script", fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = onTestAgain,
                    enabled = !isTesting,
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sedang Menguji...")
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tes Ulang Izin Drive Sekarang")
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(num: String, title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = HighDensityBlue,
            shape = CircleShape,
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(num, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Blue900)
            Text(desc, fontSize = 11.sp, color = Slate600)
        }
    }
}

@Composable
private fun ScriptUrlEditDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSaveUrl: (String) -> Unit
) {
    val context = LocalContext.current
    var urlText by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Pengaturan URL Google Apps Script", fontWeight = FontWeight.Bold, color = Blue900)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Pastikan Web App URL di-deploy sebagai 'Execute as: Me' dan 'Who has access: Anyone'.",
                    fontSize = 12.sp,
                    color = Slate600
                )
                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("Web App URL (/exec)") },
                    placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveUrl(urlText.trim())
                    Toast.makeText(context, "URL Google Apps Script disimpan!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

private fun extractDriveFileId(raw: String): String? {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return null
    val idRegex = Regex("""(?:id=|\/d\/)([a-zA-Z0-9_-]{15,})""")
    return idRegex.find(trimmed)?.groupValues?.get(1)
}

private fun getDriveWebUrl(raw: String): String {
    val fileId = extractDriveFileId(raw)
    return if (fileId != null) {
        "https://drive.google.com/file/d/$fileId/view?usp=sharing"
    } else {
        raw.trim()
    }
}

private fun normalizeDriveThumbnailUrl(raw: String, size: Int = 300): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return ""
    val fileId = extractDriveFileId(trimmed)
    return if (fileId != null) {
        "https://drive.google.com/thumbnail?id=$fileId&sz=w$size"
    } else {
        trimmed
    }
}

private fun normalizeDriveHighResUrl(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return ""
    val fileId = extractDriveFileId(trimmed)
    return if (fileId != null) {
        "https://drive.google.com/thumbnail?id=$fileId&sz=w1200"
    } else {
        trimmed
    }
}

private fun parseTimestampToMillis(rawDate: String): Long {
    val trimmed = rawDate.trim()
    if (trimmed.isBlank()) return 0L
    val formats = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd",
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy HH:mm",
        "dd/MM/yyyy"
    )
    for (pattern in formats) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.isLenient = false
            val parsed = sdf.parse(trimmed)
            if (parsed != null) return parsed.time
        } catch (_: Exception) {
        }
    }
    return 0L
}

