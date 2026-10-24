package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ArgoEntity
import com.example.data.local.entity.BanEntity
import com.example.data.local.entity.BaterayEntity
import com.example.data.local.entity.KoplingEntity
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.data.local.entity.PengerjaanTeknikEntity
import com.example.data.local.entity.PrediksiPartEntity
import com.example.data.local.entity.ReservasiEntity
import com.example.data.local.entity.RiwayatSkiEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueBorder
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Menyinkronkan dan memperbarui data Prediksi Part dengan data ARGO terkini dari armada
 * agar ARGO_SEKARANG selalu akurat dan ESTIMASI_KM selalu terupdate real-time.
 */
fun resolvePrediksiPartWithLiveArgo(
    item: PrediksiPartEntity,
    argoList: List<ArgoEntity>
): PrediksiPartEntity {
    val isSelesai = item.status.trim().equals("SELESAI", ignoreCase = true) ||
        item.status.contains("SELESAI", ignoreCase = true) ||
        item.status.contains("DONE", ignoreCase = true)

    val cleanTarget = item.noMobil.trim().removePrefix("#").trim()
    val match = if (!isSelesai) argoList.find { isVehicleMatch(it.noMobil, cleanTarget) } else null
    val liveArgoKm = match?.km?.trim().orEmpty()
    val effectiveArgo = if (!isSelesai && liveArgoKm.isNotBlank()) liveArgoKm else item.argoSekarang.trim()

    val kmInputLong = item.kmInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val prediksiKmLong = item.prediksiKm.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val existingTargetLong = item.kmTarget.filter { it.isDigit() }.toLongOrNull()
    val effectiveTargetLong = if (existingTargetLong != null && existingTargetLong > 0L) {
        existingTargetLong
    } else if (kmInputLong > 0L || prediksiKmLong > 0L) {
        kmInputLong + prediksiKmLong
    } else null
    val effectiveTargetStr = effectiveTargetLong?.toString() ?: item.kmTarget.trim()

    val argoLong = effectiveArgo.filter { it.isDigit() || it == '-' }.toLongOrNull()
    val targetLong = effectiveTargetStr.filter { it.isDigit() || it == '-' }.toLongOrNull() ?: effectiveTargetLong
    val effectiveEstimasiStr = if (targetLong != null && argoLong != null) {
        (argoLong - targetLong).toString() // [ARGO_SEKARANG] - [KM_TARGET]
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

    return item.copy(
        kmTarget = effectiveTargetStr,
        argoSekarang = effectiveArgo,
        estimasiKm = effectiveEstimasiStr,
        aprovel = normalizedAprovel
    )
}

/**
 * Mengubah string estimasi KM menjadi Long untuk pengurutan numerik dari nilai tertinggi ke terendah.
 * Contoh: "12500" -> 12500, "-1500" -> -1500, "+300" -> 300, "" -> Long.MIN_VALUE
 */
fun parseEstimasiKmValue(estimasiStr: String): Long {
    val trimmed = estimasiStr.trim().replace(".", "").replace(",", "").replace(" ", "")
    val isNegative = trimmed.startsWith("-")
    val digits = trimmed.filter { it.isDigit() }
    val number = digits.toLongOrNull() ?: return Long.MIN_VALUE
    return if (isNegative) -number else number
}

/**
 * Memeriksa apakah nilai ESTIMASI_KM berada di atas (-500).
 * Contoh:
 * - Nilai -400 > -500 -> true
 * - Nilai 0 > -500 -> true
 * - Nilai 1200 > -500 -> true
 * - Nilai -500 > -500 -> false
 * - Nilai -1500 > -500 -> false
 */
fun isEstimasiKmOverThreshold(estimasiStr: String, threshold: Long = -500L): Boolean {
    val value = parseEstimasiKmValue(estimasiStr)
    return value != Long.MIN_VALUE && value > threshold
}

enum class PrediksiFilterType {
    PROSES,
    APPROVEL
}

/**
 * Layar PREDIKSI PART
 * Database ID: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30
 * Sheet: PREDIKSI_PART
 * Susunan Tabel Tampilan:
 * | TANGGAL | NO_MOBIL | SERVICE | KM_MOBIL | NIP_NAMA | NAMA_PART | PREDIKSI_KM | KM_TARGET | ARGO_SEKARANG | ESTIMASI_KM | APROVEL | KETERANGAN | STATUS | AKSI |
 */
@Composable
fun PrediksiPartScreen(
    prediksiPartList: List<PrediksiPartEntity>,
    kendaraanList: List<MasterKendaraanEntity> = emptyList(),
    baterayList: List<BaterayEntity> = emptyList(),
    banList: List<BanEntity> = emptyList(),
    koplingList: List<KoplingEntity> = emptyList(),
    reservasiList: List<ReservasiEntity> = emptyList(),
    pengerjaanTeknikList: List<PengerjaanTeknikEntity> = emptyList(),
    riwayatSkiList: List<RiwayatSkiEntity> = emptyList(),
    argoList: List<ArgoEntity> = emptyList(),
    currentUser: UserEntity? = null,
    onSavePrediksiPart: (PrediksiPartEntity) -> Unit,
    onDeletePrediksiPart: (Long) -> Unit,
    onVehicleClick: (String) -> Unit,
    onOrderClick: (String) -> Unit = {},
    onEditMasterKendaraan: () -> Unit = {},
    onSyncClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterChip by remember { mutableStateOf<PrediksiFilterType?>(null) }
    var selectedDetailItem by remember { mutableStateOf<PrediksiPartEntity?>(null) }
    var editingItem by remember { mutableStateOf<PrediksiPartEntity?>(null) }
    var showAddForm by remember { mutableStateOf(false) }
    var inspectedVehicleNoMobil by remember { mutableStateOf<String?>(null) }
    var itemToDeleteConfirm by remember { mutableStateOf<PrediksiPartEntity?>(null) }
    var showOverdueAlertPopup by remember { mutableStateOf(false) }
    var hasAutoShownAlert by remember { mutableStateOf(false) }

    // Selalu perbarui ARGO_SEKARANG dan ESTIMASI_KM secara real-time dari argoList armada
    val livePrediksiPartList = remember(prediksiPartList, argoList) {
        prediksiPartList.map { resolvePrediksiPartWithLiveArgo(it, argoList) }
    }

    // Data akan di hide jika status SELESAI, diurutkan dari ESTIMASI_KM tertinggi ke terendah
    val activePrediksiPartList = remember(livePrediksiPartList) {
        livePrediksiPartList.filter { item ->
            val isSelesai = item.status.trim().equals("SELESAI", ignoreCase = true) ||
                item.status.contains("SELESAI", ignoreCase = true)
            !isSelesai
        }.sortedWith(
            compareByDescending<PrediksiPartEntity> { parseEstimasiKmValue(it.estimasiKm) }
                .thenByDescending { it.id }
        )
    }

    // Filter data yang memiliki ESTIMASI_KM di atas (-500)
    val alertPartList = remember(activePrediksiPartList) {
        activePrediksiPartList.filter { isEstimasiKmOverThreshold(it.estimasiKm) }
    }

    // Otomatis munculkan popup jika terdapat data di atas (-500)
    LaunchedEffect(alertPartList) {
        if (alertPartList.isNotEmpty() && !hasAutoShownAlert) {
            showOverdueAlertPopup = true
            hasAutoShownAlert = true
        }
    }

    val prosesCount = remember(activePrediksiPartList) {
        activePrediksiPartList.count {
            it.status.trim().equals("PROSES", ignoreCase = true) ||
                it.status.contains("PROSES", ignoreCase = true)
        }
    }

    val menungguCount = remember(activePrediksiPartList) {
        activePrediksiPartList.count {
            it.aprovel.contains("TUNGGU", ignoreCase = true) ||
                it.aprovel.contains("MENUNGGU", ignoreCase = true) ||
                it.aprovel.contains("PENDING", ignoreCase = true)
        }
    }

    // Urutkan data ESTIMASI_KM dari nilai tertinggi ke terendah
    val filteredList = remember(activePrediksiPartList, searchQuery, selectedFilterChip) {
        activePrediksiPartList.filter { item ->
            val matchFilterChip = when (selectedFilterChip) {
                null -> true
                PrediksiFilterType.PROSES -> {
                    item.status.trim().equals("PROSES", ignoreCase = true) ||
                        item.status.contains("PROSES", ignoreCase = true)
                }
                PrediksiFilterType.APPROVEL -> {
                    item.aprovel.contains("TUNGGU", ignoreCase = true) ||
                        item.aprovel.contains("MENUNGGU", ignoreCase = true) ||
                        item.aprovel.contains("PENDING", ignoreCase = true)
                }
            }

            matchFilterChip && (
                searchQuery.isBlank() ||
                item.noMobil.contains(searchQuery, ignoreCase = true) ||
                item.namaPart.contains(searchQuery, ignoreCase = true) ||
                item.service.contains(searchQuery, ignoreCase = true) ||
                item.nipNama.contains(searchQuery, ignoreCase = true) ||
                item.tanggal.contains(searchQuery, ignoreCase = true) ||
                item.keterangan.contains(searchQuery, ignoreCase = true) ||
                item.kmInput.contains(searchQuery, ignoreCase = true) ||
                item.prediksiKm.contains(searchQuery, ignoreCase = true) ||
                item.kmTarget.contains(searchQuery, ignoreCase = true) ||
                item.argoSekarang.contains(searchQuery, ignoreCase = true) ||
                item.estimasiKm.contains(searchQuery, ignoreCase = true) ||
                item.aprovel.contains(searchQuery, ignoreCase = true) ||
                item.status.contains(searchQuery, ignoreCase = true)
            )
        }.sortedWith(
            compareByDescending<PrediksiPartEntity> { parseEstimasiKmValue(it.estimasiKm) }
                .thenByDescending { it.id }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Banner Info Header
        Card(
            colors = CardDefaults.cardColors(containerColor = HighDensityBlue),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PrecisionManufacturing,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "PREDIKSI PART ARMADA",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        // Tombol Tambah di bawah judul Prediksi Part Armada
                        Button(
                            onClick = { showAddForm = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = HighDensityBlue
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("prediksi_part_add_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Tombol Singkron di pojok kanan atas
                OutlinedButton(
                    onClick = onSyncClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.Top)
                        .testTag("prediksi_part_sync_button")
                ) {
                    Icon(Icons.Default.Sync, contentDescription = "Singkron", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Singkron", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar (Cari)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Cari",
                    fontSize = 13.sp,
                    color = Slate400
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Cari",
                    tint = HighDensityBlue,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = HighDensityBlue,
                unfocusedBorderColor = HighDensityBlueBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("prediksi_part_search_input")
        )

        // Chip Filter: PROSES | APROVEL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedFilterChip == PrediksiFilterType.PROSES,
                onClick = {
                    selectedFilterChip = if (selectedFilterChip == PrediksiFilterType.PROSES) {
                        null
                    } else {
                        PrediksiFilterType.PROSES
                    }
                },
                label = {
                    Text(
                        if (prosesCount > 0) "PROSES ($prosesCount)" else "PROSES",
                        fontSize = 12.sp,
                        fontWeight = if (selectedFilterChip == PrediksiFilterType.PROSES) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = if (selectedFilterChip == PrediksiFilterType.PROSES) {
                    { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFF59E0B),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                modifier = Modifier.testTag("prediksi_part_filter_proses")
            )

            FilterChip(
                selected = selectedFilterChip == PrediksiFilterType.APPROVEL,
                onClick = {
                    selectedFilterChip = if (selectedFilterChip == PrediksiFilterType.APPROVEL) {
                        null
                    } else {
                        PrediksiFilterType.APPROVEL
                    }
                },
                label = {
                    Text(
                        if (menungguCount > 0) "APROVEL ($menungguCount)" else "APROVEL",
                        fontSize = 12.sp,
                        fontWeight = if (selectedFilterChip == PrediksiFilterType.APPROVEL) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = if (selectedFilterChip == PrediksiFilterType.APPROVEL) {
                    { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE11D48),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                modifier = Modifier.testTag("prediksi_part_filter_approvel")
            )
        }

        // Banner Peringatan jika terdapat data dengan ESTIMASI_KM > -500
        if (alertPartList.isNotEmpty()) {
            Surface(
                color = Color(0xFFFEF2F2),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFFECACA)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showOverdueAlertPopup = true }
                    .testTag("prediksi_part_alert_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = Color(0xFFDC2626),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Peringatan",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PERINGATAN: ${alertPartList.size} Data ESTIMASI KM > -500",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B),
                                    fontSize = 12.5.sp
                                )
                            )
                            Text(
                                text = "Armada mendekati atau melewati batas waktu servis",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFB91C1C),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                    Button(
                        onClick = { showOverdueAlertPopup = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("prediksi_part_open_alert_button")
                    ) {
                        Text("Lihat Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tabel Data Prediksi Part
        if (filteredList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, HighDensityBlueBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tidak ada data Prediksi Part yang cocok dengan pencarian."
                            else "Belum ada data PREDIKSI PART aktif. Tekan Singkron untuk memuat dari Google Sheets.",
                            color = Slate600,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                PrediksiPartTableView(
                    items = filteredList,
                    onDetailClick = { selectedDetailItem = it }
                )
            }
        }
    }

    // Popup Peringatan Prediksi Part jika ESTIMASI_KM > -500
    if (showOverdueAlertPopup && alertPartList.isNotEmpty()) {
        PrediksiPartAlertPopupDialog(
            items = alertPartList,
            onDismiss = { showOverdueAlertPopup = false },
            onSelectItem = { item ->
                showOverdueAlertPopup = false
                selectedDetailItem = item
            }
        )
    }

    // Popup Rincian Kendaraan saat No Mobil diklik
    if (inspectedVehicleNoMobil != null) {
        val targetNo = inspectedVehicleNoMobil!!
        val matchedVehicle = kendaraanList.find { isVehicleMatch(it.noMobil, targetNo) }
            ?: MasterKendaraanEntity(
                id = 0,
                noMobil = targetNo.removePrefix("#").trim(),
                noPolisi = "-",
                tipe = "Armada Bengkel",
                tahunPembuatan = "-",
                jadwalMobil = "Jadwal Reguler"
            )

        RincianKendaraanDialog(
            vehicle = matchedVehicle,
            baterayList = baterayList,
            banList = banList,
            koplingList = koplingList,
            reservasiList = reservasiList,
            pengerjaanTeknikList = pengerjaanTeknikList,
            riwayatSkiList = riwayatSkiList,
            argoList = argoList,
            onDismiss = { inspectedVehicleNoMobil = null },
            onEditClick = {
                inspectedVehicleNoMobil = null
                onEditMasterKendaraan()
            },
            onOrderClick = { orderNo ->
                inspectedVehicleNoMobil = null
                onOrderClick(orderNo)
            }
        )
    }

    // Dialog Detail Lengkap (termasuk ID, KM INPUT, ARGO SEKARANG)
    if (selectedDetailItem != null) {
        PrediksiPartDetailDialog(
            item = selectedDetailItem!!,
            argoList = argoList,
            currentUser = currentUser,
            onDismiss = { selectedDetailItem = null },
            onEditClick = {
                val current = selectedDetailItem
                val isSelesai = current?.status?.trim()?.equals("SELESAI", ignoreCase = true) == true ||
                    current?.status?.contains("SELESAI", ignoreCase = true) == true ||
                    current?.status?.contains("DONE", ignoreCase = true) == true
                if (!isSelesai) {
                    editingItem = selectedDetailItem
                    selectedDetailItem = null
                    showAddForm = true
                }
            },
            onDeleteClick = {
                val toDel = selectedDetailItem
                selectedDetailItem = null
                itemToDeleteConfirm = toDel
            },
            onVehicleClick = { noMobil ->
                val cleaned = noMobil.trim()
                selectedDetailItem = null
                if (cleaned.isNotBlank() && cleaned != "-") {
                    inspectedVehicleNoMobil = cleaned
                }
            },
            onUpdateApproval = { newStatus ->
                val current = selectedDetailItem ?: return@PrediksiPartDetailDialog
                val updated = resolvePrediksiPartWithLiveArgo(current, argoList).copy(aprovel = newStatus)
                onSavePrediksiPart(updated)
                selectedDetailItem = updated
            }
        )
    }

    // Dialog Konfirmasi Hapus Data Prediksi Part
    if (itemToDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { itemToDeleteConfirm = null },
            shape = RoundedCornerShape(14.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Hapus Prediksi Part",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus data Prediksi Part mobil #${itemToDeleteConfirm!!.noMobil} (${itemToDeleteConfirm!!.namaPart})? Data akan dihapus dari aplikasi dan Google Sheets.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Slate800)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = itemToDeleteConfirm!!.id
                        itemToDeleteConfirm = null
                        onDeletePrediksiPart(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDeleteConfirm = null }) {
                    Text("Batal", color = Slate600)
                }
            }
        )
    }

    // Dialog Tambah / Edit
    if (showAddForm || editingItem != null) {
        PrediksiPartFormDialog(
            initialData = editingItem,
            kendaraanList = kendaraanList,
            argoList = argoList,
            currentUser = currentUser,
            onDismiss = {
                showAddForm = false
                editingItem = null
            },
            onSave = { entity ->
                onSavePrediksiPart(entity)
                showAddForm = false
                editingItem = null
            }
        )
    }
}

@Composable
private fun PrediksiPartTableView(
    items: List<PrediksiPartEntity>,
    onDetailClick: (PrediksiPartEntity) -> Unit
) {
    val horizontalScrollState = rememberScrollState()

    val wTanggal = 100.dp
    val wNoMobil = 110.dp
    val wNipNama = 135.dp
    val wNamaPart = 155.dp
    val wPrediksiKm = 105.dp
    val wEstimasiKm = 115.dp
    val wAprovel = 115.dp
    val wStatus = 110.dp
    val totalTableWidth = wTanggal + wNoMobil + wNipNama + wNamaPart + wPrediksiKm + wEstimasiKm + wAprovel + wStatus

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, HighDensityBlueBorder),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
        ) {
            Column(modifier = Modifier.width(totalTableWidth)) {
                // Header Tabel Sesuai Permintaan:
                // TANGGAL | No_MOBIL | NIP_NAMA | NAMA_PART | PREDIKSI_KM | ESTIMASI_KM | APROVEL | STATUS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HighDensityBlue)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderCell("TANGGAL", wTanggal)
                    HeaderCell("No_MOBIL", wNoMobil)
                    HeaderCell("NIP_NAMA", wNipNama)
                    HeaderCell("NAMA_PART", wNamaPart)
                    HeaderCell("PREDIKSI_KM", wPrediksiKm)
                    HeaderCell("ESTIMASI_KM ▼", wEstimasiKm)
                    HeaderCell("APROVEL", wAprovel)
                    HeaderCell("STATUS", wStatus)
                }

                // Baris Data Tabel (Klik di baris mana saja / sembarang akan membuka detail)
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(items, key = { idx, item -> "${item.id}_${item.noMobil}_$idx" }) { index, item ->
                        val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .clickable { onDetailClick(item) }
                                .border(width = 0.5.dp, color = Color(0xFFE2E8F0))
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. TANGGAL
                            DataCell(item.tanggal.ifBlank { "-" }, wTanggal, color = Slate600)

                            // 2. No_MOBIL
                            Box(
                                modifier = Modifier
                                    .width(wNoMobil)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Surface(
                                    color = Blue50,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, HighDensityBlueBorder)
                                ) {
                                    Text(
                                        text = if (item.noMobil.startsWith("#")) item.noMobil else "#${item.noMobil.ifBlank { "-" }}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = HighDensityBlue,
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // 3. NIP_NAMA
                            DataCell(item.nipNama.ifBlank { "-" }, wNipNama, fontWeight = FontWeight.Medium, color = Slate800)

                            // 4. NAMA_PART (Warna tabel lebih putih)
                            Box(
                                modifier = Modifier
                                    .width(wNamaPart)
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                                ) {
                                    Text(
                                        text = item.namaPart.ifBlank { "-" },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate800,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // 4b. PREDIKSI_KM (Warna tabel lebih putih)
                            Box(
                                modifier = Modifier
                                    .width(wPrediksiKm)
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                                ) {
                                    Text(
                                        text = if (item.prediksiKm.isNotBlank()) "${item.prediksiKm} km" else "-",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate800,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // 5. ESTIMASI_KM
                            Box(
                                modifier = Modifier
                                    .width(wEstimasiKm)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val estVal = item.estimasiKm.ifBlank { "-" }
                                val isNegative = estVal.trim().startsWith("-") && estVal.trim().length > 1
                                Surface(
                                    color = if (isNegative) Color(0xFFFEF2F2) else Blue50,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = estVal,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNegative) Color(0xFFDC2626) else Blue900,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // 6. APROVEL (Menunggu, Disetujui, Ditolak)
                            Box(
                                modifier = Modifier
                                    .width(wAprovel)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val rawAp = item.aprovel.ifBlank { "Menunggu" }
                                val apVal = when {
                                    rawAp.equals("Disetujui", ignoreCase = true) || rawAp.contains("SETUJU", ignoreCase = true) || rawAp.contains("APPROV", ignoreCase = true) -> "Disetujui"
                                    rawAp.equals("Ditolak", ignoreCase = true) || rawAp.contains("TOLAK", ignoreCase = true) || rawAp.contains("REJECT", ignoreCase = true) -> "Ditolak"
                                    else -> "Menunggu"
                                }
                                val isApproved = apVal == "Disetujui"
                                val isRejected = apVal == "Ditolak"

                                val bgColor = when {
                                    isApproved -> Color(0xFFDCFCE7)
                                    isRejected -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFFEF3C7)
                                }
                                val txtColor = when {
                                    isApproved -> Color(0xFF15803D)
                                    isRejected -> Color(0xFFDC2626)
                                    else -> Color(0xFFB45309)
                                }

                                Surface(
                                    color = bgColor,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = apVal,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = txtColor,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // 7. STATUS (Hiden jika APROVEL Menunggu)
                            Box(
                                modifier = Modifier
                                    .width(wStatus)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val rawAp = item.aprovel.ifBlank { "Menunggu" }
                                val isItemApproved = rawAp.trim().equals("Disetujui", ignoreCase = true) ||
                                    rawAp.contains("SETUJU", ignoreCase = true) ||
                                    rawAp.contains("APPROV", ignoreCase = true) ||
                                    rawAp.contains("ACC", ignoreCase = true)

                                if (!isItemApproved) {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate400,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                } else {
                                    val stVal = item.status.ifBlank { "-" }
                                    val isDone = stVal.contains("SELESAI", ignoreCase = true) ||
                                        stVal.contains("DONE", ignoreCase = true) ||
                                        stVal.contains("OK", ignoreCase = true)
                                    val isUrgent = stVal.contains("OVERDUE", ignoreCase = true) ||
                                        stVal.contains("GANTI", ignoreCase = true) ||
                                        stVal.contains("URGENT", ignoreCase = true)
                                    val isProgress = stVal.contains("PROSES", ignoreCase = true) ||
                                        stVal.contains("PROGRESS", ignoreCase = true) ||
                                        stVal.contains("AKTIF", ignoreCase = true)
                                    val isPending = stVal.contains("PENDING", ignoreCase = true) ||
                                        stVal.contains("TUNGGU", ignoreCase = true)

                                    val bgColor = when {
                                        isDone -> Color(0xFFECFDF5)
                                        isUrgent -> Color(0xFFFEF2F2)
                                        isProgress -> Color(0xFFEFF6FF)
                                        isPending -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFF1F5F9)
                                    }
                                    val txtColor = when {
                                        isDone -> Color(0xFF059669)
                                        isUrgent -> Color(0xFFDC2626)
                                        isProgress -> Blue700
                                        isPending -> Color(0xFF92400E)
                                        else -> Slate800
                                    }

                                    Surface(
                                        color = bgColor,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = stVal,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = txtColor,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Slate800
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = color,
                fontWeight = fontWeight,
                fontSize = 12.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PrediksiPartDetailDialog(
    item: PrediksiPartEntity,
    argoList: List<ArgoEntity> = emptyList(),
    currentUser: UserEntity? = null,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: (() -> Unit)? = null,
    onVehicleClick: ((String) -> Unit)? = null,
    onUpdateApproval: ((String) -> Unit)? = null
) {
    val hiddenApprovalLevels = remember {
        listOf("ALL", "TEKNIK", "CAT", "KEINDAHAN")
    }
    val allowedApprovalLevels = remember {
        listOf("SUPER ADMIN", "ADMIN", "KEPALA BENGKEL", "GH", "SH")
    }
    val userLevel = remember(currentUser) {
        currentUser?.level?.trim()?.uppercase() ?: ""
    }
    val canApprove = remember(userLevel) {
        val clean = userLevel.replace(" ", "")
        val isExplicitlyHidden = hiddenApprovalLevels.any { hidden ->
            userLevel == hidden || clean == hidden || userLevel.split(",", "/", " ", "|").any { it.trim() == hidden }
        }
        if (isExplicitlyHidden) {
            false
        } else {
            userLevel in allowedApprovalLevels || clean in listOf("SUPERADMIN", "ADMIN", "KEPALABENGKEL", "GH", "SH")
        }
    }

    val liveItem = remember(item, argoList) {
        resolvePrediksiPartWithLiveArgo(item, argoList)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(14.dp),
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rincian Prediksi Part #${liveItem.noMobil}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = HighDensityBlue
                            )
                        )
                        if (onVehicleClick != null && liveItem.noMobil.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Blue50,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, HighDensityBlueBorder),
                                modifier = Modifier.clickable { onVehicleClick(liveItem.noMobil) }
                            ) {
                                Text(
                                    text = "Rincian Armada",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = HighDensityBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "ID Database: ${liveItem.id} • Sheet: PREDIKSI_PART",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate600)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate600)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRowItem("ID", liveItem.id.toString())
                DetailRowItem("TANGGAL", liveItem.tanggal)
                DetailRowItem("NO_MOBIL", liveItem.noMobil)
                DetailRowItem("SERVICE", liveItem.service)
                DetailRowItem("KM_MOBIL", liveItem.kmInput)
                DetailRowItem("ARGO_SEKARANG", liveItem.argoSekarang)
                DetailRowItem("NIP_NAMA", liveItem.nipNama)
                DetailRowItem("NAMA_PART", liveItem.namaPart)
                DetailRowItem("PREDIKSI_KM", liveItem.prediksiKm)
                DetailRowItem("KM_TARGET", liveItem.kmTarget)
                DetailRowItem("ESTIMASI_KM", liveItem.estimasiKm)
                val isSelesaiStatus = liveItem.status.trim().equals("SELESAI", ignoreCase = true) ||
                    liveItem.status.contains("SELESAI", ignoreCase = true) ||
                    liveItem.status.contains("DONE", ignoreCase = true)
                val statusDisplay = when {
                    isSelesaiStatus -> "SELESAI"
                    liveItem.status.isNotBlank() -> liveItem.status
                    else -> "AKTIF"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STATUS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate600
                        )
                    )
                    Surface(
                        color = if (isSelesaiStatus) Color(0xFFF1F5F9) else Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, if (isSelesaiStatus) Color(0xFFCBD5E1) else Color(0xFF86EFAC))
                    ) {
                        Text(
                            text = if (isSelesaiStatus) "SELESAI" else (if (statusDisplay.contains("AKTIF", ignoreCase = true)) statusDisplay else "$statusDisplay (AKTIF)"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelesaiStatus) Slate600 else Color(0xFF15803D),
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                val isDetailApproved = liveItem.aprovel.trim().equals("Disetujui", ignoreCase = true) ||
                    liveItem.aprovel.contains("SETUJU", ignoreCase = true) ||
                    liveItem.aprovel.contains("APPROV", ignoreCase = true) ||
                    liveItem.aprovel.contains("ACC", ignoreCase = true)
                if (isDetailApproved && liveItem.keterangan.isNotBlank()) {
                    DetailRowItem("KETERANGAN", liveItem.keterangan)
                }
                DetailRowItem("APROVEL", liveItem.aprovel)

                val isApprovedStatus = liveItem.aprovel.equals("Disetujui", ignoreCase = true) ||
                    liveItem.aprovel.contains("SETUJU", ignoreCase = true) ||
                    liveItem.aprovel.contains("APPROV", ignoreCase = true)

                // TOMBOL APPROVEL TARUH PALING BAWAH (Hanya muncul jika user Level SUPER ADMIN, GH, SH, KEPALA BENGKEL dan BELUM Disetujui)
                if (canApprove && onUpdateApproval != null && !isApprovedStatus) {
                    HorizontalDivider(color = HighDensityBlueBorder)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = HighDensityBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TOMBOL APPROVEL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Blue900
                                    )
                                )
                            }
                            val currentAp = liveItem.aprovel.ifBlank { "Menunggu" }
                            Surface(
                                color = when {
                                    currentAp.equals("Disetujui", ignoreCase = true) -> Color(0xFFDCFCE7)
                                    currentAp.equals("Ditolak", ignoreCase = true) -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFFEF3C7)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = currentAp,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            currentAp.equals("Disetujui", ignoreCase = true) -> Color(0xFF15803D)
                                            currentAp.equals("Ditolak", ignoreCase = true) -> Color(0xFFDC2626)
                                            else -> Color(0xFFB45309)
                                        },
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Menunggu", "Disetujui", "Ditolak").forEach { statusChoice ->
                                val isSelected = liveItem.aprovel.equals(statusChoice, ignoreCase = true)
                                val btnBg = when {
                                    isSelected && statusChoice == "Disetujui" -> Color(0xFF15803D)
                                    isSelected && statusChoice == "Ditolak" -> Color(0xFFDC2626)
                                    isSelected && statusChoice == "Menunggu" -> Color(0xFFB45309)
                                    statusChoice == "Disetujui" -> Color(0xFFDCFCE7)
                                    statusChoice == "Ditolak" -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFFEF3C7)
                                }
                                val btnText = when {
                                    isSelected -> Color.White
                                    statusChoice == "Disetujui" -> Color(0xFF15803D)
                                    statusChoice == "Ditolak" -> Color(0xFFDC2626)
                                    else -> Color(0xFFB45309)
                                }
                                val btnBorder = when (statusChoice) {
                                    "Disetujui" -> Color(0xFF86EFAC)
                                    "Ditolak" -> Color(0xFFFCA5A5)
                                    else -> Color(0xFFFDE68A)
                                }

                                Button(
                                    onClick = { onUpdateApproval.invoke(statusChoice) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = btnBg,
                                        contentColor = btnText
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) btnBg else btnBorder),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("detail_approvel_btn_${statusChoice.lowercase()}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = when (statusChoice) {
                                                    "Disetujui" -> Icons.Default.CheckCircle
                                                    "Ditolak" -> Icons.Default.Cancel
                                                    else -> Icons.Default.Schedule
                                                },
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                        }
                                        Text(
                                            text = statusChoice,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val isApprovedStatus = liveItem.aprovel.equals("Disetujui", ignoreCase = true) ||
                liveItem.aprovel.contains("SETUJU", ignoreCase = true) ||
                liveItem.aprovel.contains("APPROV", ignoreCase = true)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Hapus disembunyikan jika status APROVEL Disetujui
                if (onDeleteClick != null && !isApprovedStatus) {
                    OutlinedButton(
                        onClick = onDeleteClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus")
                    }
                }
                val isSelesaiStatus = liveItem.status.trim().equals("SELESAI", ignoreCase = true) ||
                    liveItem.status.contains("SELESAI", ignoreCase = true) ||
                    liveItem.status.contains("DONE", ignoreCase = true)

                if (!isSelesaiStatus) {
                    Button(
                        onClick = onEditClick,
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Data")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Slate600)
            }
        }
    )
}

@Composable
private fun DetailRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Slate600
            )
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Slate800
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrediksiPartFormDialog(
    initialData: PrediksiPartEntity?,
    kendaraanList: List<MasterKendaraanEntity> = emptyList(),
    argoList: List<ArgoEntity> = emptyList(),
    currentUser: UserEntity? = null,
    onDismiss: () -> Unit,
    onSave: (PrediksiPartEntity) -> Unit
) {
    val hiddenApprovalLevels = remember {
        listOf("ALL", "TEKNIK", "CAT", "KEINDAHAN")
    }
    val allowedApprovalLevels = remember {
        listOf("SUPER ADMIN", "ADMIN", "KEPALA BENGKEL", "GH", "SH")
    }
    val userLevel = remember(currentUser) {
        currentUser?.level?.trim()?.uppercase() ?: ""
    }
    val canApprove = remember(userLevel) {
        val clean = userLevel.replace(" ", "")
        val isExplicitlyHidden = hiddenApprovalLevels.any { hidden ->
            userLevel == hidden || clean == hidden || userLevel.split(",", "/", " ", "|").any { it.trim() == hidden }
        }
        if (isExplicitlyHidden) {
            false
        } else {
            userLevel in allowedApprovalLevels || clean in listOf("SUPERADMIN", "ADMIN", "KEPALABENGKEL", "GH", "SH")
        }
    }

    val defaultNipNama = remember(currentUser) {
        if (currentUser != null) {
            val nip = currentUser.nip.trim()
            val nama = currentUser.namaMekanik.trim()
            when {
                nip.isNotBlank() && nama.isNotBlank() -> "$nip/$nama"
                nama.isNotBlank() -> nama
                nip.isNotBlank() -> nip
                else -> currentUser.username.trim()
            }
        } else ""
    }
    val todayStr = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }
    val uniqueId = remember(initialData) {
        if (initialData != null && initialData.id > 0L) initialData.id else System.currentTimeMillis()
    }
    var tanggal by remember { mutableStateOf(initialData?.tanggal ?: todayStr) }
    var noMobil by remember { mutableStateOf(initialData?.noMobil?.uppercase() ?: "") }
    var service by remember { mutableStateOf(initialData?.service ?: "") }

    val initialCleanNo = remember(initialData) {
        initialData?.noMobil?.trim()?.uppercase()?.removePrefix("#")?.trim().orEmpty()
    }
    val initialArgoMatch = remember(initialCleanNo, argoList) {
        if (initialCleanNo.isNotBlank()) argoList.find { isVehicleMatch(it.noMobil, initialCleanNo) } else null
    }
    val initialArgoKm = initialArgoMatch?.km?.trim().orEmpty().filter { it.isDigit() }

    var kmInput by remember(initialData, initialArgoKm) {
        mutableStateOf(
            if (initialData?.kmInput?.isNotBlank() == true) initialData.kmInput
            else initialArgoKm
        )
    }
    val nipNama = remember(initialData, defaultNipNama) {
        initialData?.nipNama?.ifBlank { defaultNipNama } ?: defaultNipNama
    }
    var namaPart by remember { mutableStateOf(initialData?.namaPart ?: "") }
    var prediksiKm by remember { mutableStateOf(initialData?.prediksiKm ?: "") }

    var argoSekarang by remember(initialData, initialArgoKm) {
        mutableStateOf(
            if (initialData?.argoSekarang?.isNotBlank() == true) initialData.argoSekarang
            else initialArgoKm
        )
    }

    val cleanNoMobil = noMobil.trim().uppercase().removePrefix("#").trim()
    val matchedKendaraan = remember(cleanNoMobil, kendaraanList) {
        if (cleanNoMobil.isNotBlank()) kendaraanList.find { isVehicleMatch(it.noMobil, cleanNoMobil) } else null
    }
    val isNoMobilVerified = matchedKendaraan != null

    val noMobilSuggestions = remember(cleanNoMobil, kendaraanList, isNoMobilVerified) {
        if (cleanNoMobil.isBlank() || isNoMobilVerified) emptyList()
        else kendaraanList.filter {
            it.noMobil.contains(cleanNoMobil, ignoreCase = true) ||
            it.noPolisi.contains(cleanNoMobil, ignoreCase = true)
        }.take(5)
    }

    fun applyVehicleNo(input: String) {
        val upper = input.uppercase()
        noMobil = upper
        val clean = upper.trim().removePrefix("#").trim()
        val matchArgo = argoList.find { a -> isVehicleMatch(a.noMobil, clean) }
        val newArgo = matchArgo?.km?.trim().orEmpty().filter { c -> c.isDigit() }
        if (newArgo.isNotBlank()) {
            kmInput = newArgo
            argoSekarang = newArgo
        }
    }

    LaunchedEffect(cleanNoMobil, argoList) {
        if (cleanNoMobil.isNotBlank()) {
            val matchArgo = argoList.find { isVehicleMatch(it.noMobil, cleanNoMobil) }
            val newArgo = matchArgo?.km?.trim().orEmpty().filter { c -> c.isDigit() }
            if (newArgo.isNotBlank()) {
                if (initialData == null || kmInput.isBlank()) {
                    kmInput = newArgo
                }
                if (initialData == null || argoSekarang.isBlank()) {
                    argoSekarang = newArgo
                }
            }
        }
    }

    val initialAprovel = remember(initialData) {
        val raw = initialData?.aprovel?.trim().orEmpty()
        when {
            raw.contains("SETUJU", ignoreCase = true) || raw.contains("APPROV", ignoreCase = true) || raw.contains("ACC", ignoreCase = true) -> "Disetujui"
            raw.contains("TOLAK", ignoreCase = true) || raw.contains("REJECT", ignoreCase = true) -> "Ditolak"
            raw.contains("TUNGGU", ignoreCase = true) || raw.contains("PENDING", ignoreCase = true) || raw.contains("PROSES", ignoreCase = true) -> "Menunggu"
            raw.isNotBlank() -> raw
            else -> "Menunggu"
        }
    }
    var aprovel by remember { mutableStateOf(initialAprovel) }
    var keterangan by remember { mutableStateOf(if (initialData == null) "" else (initialData.keterangan ?: "")) }
    var status by remember { mutableStateOf(initialData?.status ?: "PROSES") }
    var showKeteranganError by remember { mutableStateOf(false) }

    val isApproved = remember(aprovel) {
        aprovel.trim().equals("Disetujui", ignoreCase = true) ||
            aprovel.contains("SETUJU", ignoreCase = true) ||
            aprovel.contains("APPROV", ignoreCase = true) ||
            aprovel.contains("ACC", ignoreCase = true)
    }

    // Validasi: saat Status SELESAI dan APROVEL Disetujui, Keterangan wajib diisi
    val isKeteranganRequired = isApproved && status.trim().equals("SELESAI", ignoreCase = true)
    val isKeteranganValid = !isKeteranganRequired || keterangan.trim().isNotBlank()
    val isKeteranganError = isKeteranganRequired && (keterangan.trim().isBlank() || showKeteranganError)

    val isApprovedStatus = isApproved

    // Perhitungan otomatis real-time: [ARGO_SEKARANG] - [KM_TARGET]
    val kmInputLong = kmInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val prediksiKmLong = prediksiKm.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val kmTargetLong = kmInputLong + prediksiKmLong
    val kmTargetStr = if (kmTargetLong > 0L) kmTargetLong.toString() else (initialData?.kmTarget ?: "")

    val argoSekarangLong = argoSekarang.filter { it.isDigit() || it == '-' }.toLongOrNull() ?: 0L
    val targetForCalc = kmTargetStr.filter { it.isDigit() || it == '-' }.toLongOrNull() ?: kmTargetLong
    val estimasiKmLong = argoSekarangLong - targetForCalc
    val estimasiKmStr = if (argoSekarang.isNotBlank() && kmTargetStr.isNotBlank()) estimasiKmLong.toString() else (initialData?.estimasiKm ?: "")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (initialData == null) "Tambah Data Prediksi Part" else "Edit Prediksi Part #${initialData.noMobil}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = HighDensityBlue
                    )
                )

                HorizontalDivider(color = HighDensityBlueBorder)

                // ID (Unic Key ID - Otomatis)
                OutlinedTextField(
                    value = uniqueId.toString(),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("ID (Unic Key Otomatis)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        disabledContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color(0xFFCBD5E1),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prediksi_part_id_input")
                )

                // 1. TANGGAL (tidak dapat di edit)
                OutlinedTextField(
                    value = tanggal,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("TANGGAL (Otomatis)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        disabledContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color(0xFFCBD5E1),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. NO_MOBIL (tidak dapat di edit jika status APROVEL Disetujui)
                OutlinedTextField(
                    value = noMobil,
                    onValueChange = { if (!isApprovedStatus) applyVehicleNo(it) },
                    readOnly = isApprovedStatus,
                    label = { Text(if (isApprovedStatus) "NO MOBIL (Disetujui - Terkunci)" else "NO MOBIL *") },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = HighDensityBlue) },
                    trailingIcon = {
                        if (isApprovedStatus) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Terkunci", tint = Color(0xFF15803D), modifier = Modifier.size(20.dp))
                        } else if (isNoMobilVerified) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Terverifikasi", tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                        } else if (cleanNoMobil.isNotBlank()) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = "Tidak Ditemukan", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        }
                    },
                    supportingText = {
                        if (isApprovedStatus) {
                            Text(
                                text = "Sudah disetujui (tidak dapat diedit)",
                                color = Color(0xFF15803D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (isNoMobilVerified) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(13.dp))
                                Text(
                                    text = "Terverifikasi${if (matchedKendaraan?.tipe?.isNotBlank() == true) " • ${matchedKendaraan.tipe}" else ""}",
                                    color = Color(0xFF16A34A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (cleanNoMobil.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(13.dp))
                                Text(
                                    text = "No mobil tidak ditemukan di Master Kendaraan",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    isError = !isApprovedStatus && cleanNoMobil.isNotBlank() && !isNoMobilVerified,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isApprovedStatus) Color(0xFFF1F5F9) else Color.White,
                        unfocusedContainerColor = if (isApprovedStatus) Color(0xFFF1F5F9) else Color.White,
                        disabledContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = if (isApprovedStatus) Color(0xFFCBD5E1) else HighDensityBlue,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isApprovedStatus && noMobilSuggestions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        noMobilSuggestions.forEach { sug ->
                            Surface(
                                color = Blue50,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, HighDensityBlueBorder),
                                modifier = Modifier.clickable { applyVehicleNo(sug.noMobil) }
                            ) {
                                Text(
                                    text = "#${sug.noMobil} (${sug.noPolisi.ifBlank { sug.tipe }})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = HighDensityBlue,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 3. SERVICE (tidak dapat di edit)
                OutlinedTextField(
                    value = service,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("SERVICE (Tidak dapat diedit)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        disabledContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color(0xFFCBD5E1),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. KM_MOBIL (tidak dapat di edit) | ARGO_SEKARANG (tidak dapat di edit)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kmInput,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("KM_MOBIL") },
                        supportingText = { Text("Dari tabel argo", fontSize = 9.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            disabledContainerColor = Color(0xFFF1F5F9),
                            focusedBorderColor = Color(0xFFCBD5E1),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = argoSekarang,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("ARGO_SEKARANG") },
                        supportingText = { Text("Dari tabel argo", fontSize = 9.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            disabledContainerColor = Color(0xFFF1F5F9),
                            focusedBorderColor = Color(0xFFCBD5E1),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // 5. NIP_NAMA (tidak dapat di edit)
                OutlinedTextField(
                    value = nipNama,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("NIP_NAMA") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        disabledContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color(0xFFCBD5E1),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 6. NAMA_PART (tidak dapat di edit jika status APROVEL Disetujui)
                OutlinedTextField(
                    value = namaPart,
                    onValueChange = { if (!isApprovedStatus) namaPart = it.uppercase() },
                    readOnly = isApprovedStatus,
                    label = { Text(if (isApprovedStatus) "NAMA_PART (Disetujui - Terkunci)" else "NAMA_PART") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isApprovedStatus) Color(0xFFF1F5F9) else Color.White,
                        unfocusedContainerColor = if (isApprovedStatus) Color(0xFFF1F5F9) else Color.White,
                        disabledContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = if (isApprovedStatus) Color(0xFFCBD5E1) else HighDensityBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 7. PREDIKSI_KM (tidak dapat di edit jika status APROVEL Disetujui)
                OutlinedTextField(
                    value = prediksiKm,
                    onValueChange = { if (!isApprovedStatus) prediksiKm = it.filter { c -> c.isDigit() } },
                    readOnly = isApprovedStatus,
                    label = { Text(if (isApprovedStatus) "PREDIKSI_KM (Disetujui - Terkunci)" else "PREDIKSI_KM") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isApprovedStatus) Color(0xFFF1F5F9) else Color.White,
                        unfocusedContainerColor = if (isApprovedStatus) Color(0xFFF1F5F9) else Color.White,
                        disabledContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = if (isApprovedStatus) Color(0xFFCBD5E1) else HighDensityBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 8. KM_TARGET (tidak dapat di edit)
                OutlinedTextField(
                    value = kmTargetStr,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("KM_TARGET (Auto)") },
                    supportingText = { Text("KM_MOBIL + PREDIKSI_KM", fontSize = 9.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        disabledContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color(0xFFCBD5E1),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 9. ESTIMASI_KM (tidak dapat di edit)
                OutlinedTextField(
                    value = estimasiKmStr,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("ESTIMASI_KM (Auto)") },
                    supportingText = { Text("[ARGO_SEKARANG] - [KM_TARGET]", fontSize = 9.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9),
                        disabledContainerColor = Color(0xFFF1F5F9),
                        focusedBorderColor = Color(0xFFCBD5E1),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 10. STATUS & 11. KETERANGAN (Hanya tampil jika APROVEL Disetujui, selain itu hiden)
                if (isApproved) {
                    // 10. STATUS (Drop Down PROSES , SELESAI)
                    var statusExpanded by remember { mutableStateOf(false) }
                    val statusOptions = listOf("PROSES", "SELESAI")
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("STATUS") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            statusOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = opt,
                                            fontWeight = if (status == opt) FontWeight.Bold else FontWeight.Normal,
                                            color = if (opt == "SELESAI") Color(0xFF16A34A) else HighDensityBlue
                                        )
                                    },
                                    onClick = {
                                        status = opt
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 11. KETERANGAN (long texs huruf besar)
                    OutlinedTextField(
                        value = keterangan,
                        onValueChange = {
                            keterangan = it.uppercase()
                            if (it.isNotBlank()) {
                                showKeteranganError = false
                            }
                        },
                        label = {
                            Text(
                                if (isKeteranganRequired) "KETERANGAN *"
                                else "KETERANGAN"
                            )
                        },
                        isError = isKeteranganError,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        minLines = 3,
                        maxLines = 6,
                        placeholder = {
                            Text("MASUKKAN KETERANGAN...")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color(0xFFFFF1F2),
                            focusedBorderColor = if (isKeteranganRequired) HighDensityBlue else Color(0xFFCBD5E1),
                            errorBorderColor = Color(0xFFDC2626)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prediksi_part_keterangan_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = Slate600)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!isKeteranganValid) {
                                showKeteranganError = true
                                return@Button
                            }
                            if (cleanNoMobil.isNotBlank()) {
                                if (kendaraanList.isNotEmpty() && !isNoMobilVerified) {
                                    return@Button
                                }
                                onSave(
                                    PrediksiPartEntity(
                                        id = uniqueId,
                                        tanggal = tanggal.trim(),
                                        noMobil = cleanNoMobil,
                                        service = service.trim(),
                                        kmInput = kmInput.trim(),
                                        nipNama = nipNama.trim(),
                                        namaPart = namaPart.trim(),
                                        prediksiKm = prediksiKm.trim(),
                                        kmTarget = kmTargetStr.trim(),
                                        argoSekarang = argoSekarang.trim(),
                                        estimasiKm = estimasiKmStr.trim(),
                                        aprovel = if (canApprove) aprovel.trim() else (initialData?.aprovel?.ifBlank { "Menunggu" } ?: "Menunggu"),
                                        keterangan = if (isApproved) keterangan.trim() else (initialData?.keterangan ?: ""),
                                        status = if (isApproved) status.trim() else (initialData?.status?.ifBlank { "PROSES" } ?: "PROSES")
                                    )
                                )
                            }
                        },
                        enabled = cleanNoMobil.isNotBlank() && (kendaraanList.isEmpty() || isNoMobilVerified) && isKeteranganValid,
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                        modifier = Modifier.testTag("prediksi_part_save_button")
                    ) {
                        Text("Simpan")
                    }
                }

                // TOMBOL APPROVEL TARUH PALING BAWAH (Hanya muncul jika user Level SUPER ADMIN, GH, SH, KEPALA BENGKEL dan BELUM Disetujui)
                if (canApprove && !isApprovedStatus) {
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = HighDensityBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TOMBOL APPROVEL",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Blue900
                                    )
                                )
                            }
                            Surface(
                                color = when (aprovel) {
                                    "Disetujui" -> Color(0xFFDCFCE7)
                                    "Ditolak" -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFFEF3C7)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = aprovel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when (aprovel) {
                                            "Disetujui" -> Color(0xFF15803D)
                                            "Ditolak" -> Color(0xFFDC2626)
                                            else -> Color(0xFFB45309)
                                        },
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Menunggu", "Disetujui", "Ditolak").forEach { option ->
                                val isSelected = aprovel.equals(option, ignoreCase = true)
                                val btnBgColor = when {
                                    isSelected && option == "Disetujui" -> Color(0xFF15803D)
                                    isSelected && option == "Ditolak" -> Color(0xFFDC2626)
                                    isSelected && option == "Menunggu" -> Color(0xFFB45309)
                                    option == "Disetujui" -> Color(0xFFDCFCE7)
                                    option == "Ditolak" -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFFEF3C7)
                                }
                                val btnTextColor = when {
                                    isSelected -> Color.White
                                    option == "Disetujui" -> Color(0xFF15803D)
                                    option == "Ditolak" -> Color(0xFFDC2626)
                                    else -> Color(0xFFB45309)
                                }
                                val btnBorderColor = when (option) {
                                    "Disetujui" -> Color(0xFF86EFAC)
                                    "Ditolak" -> Color(0xFFFCA5A5)
                                    else -> Color(0xFFFDE68A)
                                }

                                Button(
                                    onClick = { aprovel = option },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = btnBgColor,
                                        contentColor = btnTextColor
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) btnBgColor else btnBorderColor),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("form_approvel_${option.lowercase()}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = when (option) {
                                                    "Disetujui" -> Icons.Default.CheckCircle
                                                    "Ditolak" -> Icons.Default.Cancel
                                                    else -> Icons.Default.Schedule
                                                },
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog Popup Peringatan Prediksi Part jika terdapat data dengan ESTIMASI_KM di atas (-500).
 * Menampilkan rincian data armada yang berada di atas ambang batas -500 KM.
 */
@Composable
fun PrediksiPartAlertPopupDialog(
    items: List<PrediksiPartEntity>,
    onDismiss: () -> Unit,
    onSelectItem: (PrediksiPartEntity) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("prediksi_part_alert_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Peringatan Prediksi Part",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF991B1B),
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = "Estimasi KM di Atas -500",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_alert_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Slate600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pesan Info
                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Terdapat ${items.size} data dengan ESTIMASI KM di atas -500 (mendekati atau melewati jadwal penggantian part).",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF92400E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Daftar Data di atas -500
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(items, key = { idx, item -> "${item.id}_${item.noMobil}_$idx" }) { _, item ->
                        val estKmVal = parseEstimasiKmValue(item.estimasiKm)
                        val isOverdue = estKmVal >= 0L
                        val estContainerBg = if (isOverdue) Color(0xFFFEE2E2) else Color(0xFFFEF9C3)
                        val estTextColor = if (isOverdue) Color(0xFFDC2626) else Color(0xFFB45309)
                        val estBorderColor = if (isOverdue) Color(0xFFFCA5A5) else Color(0xFFFDE047)
                        val estStatusLabel = if (isOverdue) "Lewat Target" else "Mendekati Servis"

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectItem(item) }
                                .testTag("alert_item_${item.noMobil}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Baris Atas: No Mobil & Estimasi KM Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Blue50,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, HighDensityBlueBorder)
                                    ) {
                                        Text(
                                            text = if (item.noMobil.startsWith("#")) item.noMobil else "#${item.noMobil.ifBlank { "-" }}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = HighDensityBlue,
                                                fontSize = 12.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Surface(
                                        color = estContainerBg,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, estBorderColor)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ESTIMASI: ${item.estimasiKm.ifBlank { "-" }} KM",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = estTextColor,
                                                    fontSize = 11.5.sp
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "($estStatusLabel)",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    color = estTextColor,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                // Nama Part & Service
                                Column {
                                    Text(
                                        text = item.namaPart.ifBlank { "-" },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate800,
                                            fontSize = 13.sp
                                        )
                                    )
                                    if (item.service.isNotBlank()) {
                                        Text(
                                            text = item.service,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate600,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)

                                // Baris Rincian Kilometer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Argo Sekarang",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 10.sp)
                                        )
                                        Text(
                                            text = if (item.argoSekarang.isNotBlank()) "${item.argoSekarang} km" else "-",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate800,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "KM Target",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 10.sp)
                                        )
                                        Text(
                                            text = if (item.kmTarget.isNotBlank()) "${item.kmTarget} km" else "-",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = HighDensityBlue,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Prediksi KM",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontSize = 10.sp)
                                        )
                                        Text(
                                            text = if (item.prediksiKm.isNotBlank()) "${item.prediksiKm} km" else "-",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate800,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                }

                                // Status & Aksi
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Surface(
                                            color = when {
                                                item.aprovel.contains("SETUJU", ignoreCase = true) -> Color(0xFFDCFCE7)
                                                item.aprovel.contains("TOLAK", ignoreCase = true) -> Color(0xFFFEE2E2)
                                                else -> Color(0xFFFEF3C7)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.aprovel.ifBlank { "Menunggu" },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        item.aprovel.contains("SETUJU", ignoreCase = true) -> Color(0xFF15803D)
                                                        item.aprovel.contains("TOLAK", ignoreCase = true) -> Color(0xFFDC2626)
                                                        else -> Color(0xFFB45309)
                                                    },
                                                    fontSize = 10.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (item.status.isNotBlank()) {
                                            Surface(
                                                color = Color(0xFFF1F5F9),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = item.status,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Medium,
                                                        color = Slate800,
                                                        fontSize = 10.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = { onSelectItem(item) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Lihat Detail →",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = HighDensityBlue,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tombol Tutup
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_alert_dialog_button")
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
