package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ArgoEntity
import com.example.data.local.entity.BanEntity
import com.example.data.local.entity.BaterayEntity
import com.example.data.local.entity.KoplingEntity
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.data.local.entity.PengerjaanTeknikEntity
import com.example.data.local.entity.ReservasiEntity
import com.example.data.local.entity.RiwayatSkiEntity
import com.example.ui.viewmodel.isOrderMatch
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueBorder
import com.example.ui.theme.HighDensityBlueDark
import com.example.ui.theme.HighDensityBlueLight
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800

data class QuadColor(val bg: Color, val text: Color, val border: Color, val label: String)

/**
 * Helper untuk mencocokkan nomor mobil secara fleksibel
 * (misal: "101", "#101", atau format numerik sama)
 */
fun isVehicleMatch(vehicleNo: String, targetNo: String): Boolean {
    val cleanV = vehicleNo.trim().removePrefix("#").trim()
    val cleanT = targetNo.trim().removePrefix("#").trim()
    if (cleanV.isBlank() || cleanT.isBlank()) return false
    if (cleanV.equals(cleanT, ignoreCase = true)) return true
    val numV = cleanV.toLongOrNull()
    val numT = cleanT.toLongOrNull()
    if (numV != null && numT != null && numV == numT) return true
    val digitsV = cleanV.filter { it.isDigit() }
    val digitsT = cleanT.filter { it.isDigit() }
    if (digitsV.length >= 3 && digitsV == digitsT) return true
    return false
}

/**
 * Fungsi bantu untuk mengecek apakah nilai Usia pada Baterai/Accu lebih dari 12
 */
fun parseUsiaValue(rawUsia: String): Double {
    val clean = rawUsia.trim()
        .replace("bulan", "", ignoreCase = true)
        .replace("bln", "", ignoreCase = true)
        .replace("thn", "", ignoreCase = true)
        .trim()
    if (clean.isBlank()) return 0.0
    // Contoh format di sheet: "3,77", "10,80", "13,57", "12,24" (pemisah desimal koma)
    val normalized = clean.replace(",", ".")
    return normalized.toDoubleOrNull() ?: 0.0
}

/**
 * Fungsi bantu untuk mengecek apakah nilai KM pada Ban atau Kopling lebih dari 90.000
 */
fun parseKmValue(rawKm: String): Double {
    val clean = rawKm.trim().replace("KM", "", ignoreCase = true).trim()
    if (clean.isBlank()) return 0.0
    // Format contoh: "90.323" atau "134.075" atau "95000"
    return if (clean.contains(".") && !clean.contains(",")) {
        val parts = clean.split(".")
        if (parts.size == 2 && parts[1].length == 3) {
            (parts[0] + parts[1]).toDoubleOrNull() ?: (clean.replace(".", "").toDoubleOrNull() ?: 0.0)
        } else {
            clean.replace(".", "").toDoubleOrNull() ?: 0.0
        }
    } else if (clean.contains(",") && !clean.contains(".")) {
        val parts = clean.split(",")
        if (parts.size == 2 && parts[1].length == 3) {
            (parts[0] + parts[1]).toDoubleOrNull() ?: (clean.replace(",", "").toDoubleOrNull() ?: 0.0)
        } else {
            clean.replace(",", "").toDoubleOrNull() ?: 0.0
        }
    } else {
        clean.replace(".", "").replace(",", "").toDoubleOrNull() ?: 0.0
    }
}

/**
 * Komparator pengurutan Master Kendaraan berdasarkan ID dari atas (1, 2, 3...)
 */
fun compareKendaraanById(a: MasterKendaraanEntity, b: MasterKendaraanEntity, ascendingId: Boolean = true): Int {
    return if (ascendingId) a.id.compareTo(b.id) else b.id.compareTo(a.id)
}

/**
 * MENU: MASTER KENDARAAN
 *
 * Kolom Tabel yang Ditampilkan:
 * No Mobil | No Polisi | Tipe Kendaraan | Tahun Pembuatan | Jadwal Mobil
 *
 * Interaksi:
 * - Klik No Mobil: memunculkan rincian unit tersebut
 *   - Informasi utama armada
 *   - Data BATERAY (referensi dari No Mobil)
 *   - Data BAN (di bawah Bateray, referensi dari No Mobil)
 *   - Data KOPLING (di bawah Ban, referensi dari No Mobil)
 *   - Data RESERVASI (di bawah Kopling, hanya tampil 5 item dengan tombol View All untuk melihat keseluruhan)
 */
@Composable
fun MasterKendaraanScreen(
    kendaraanList: List<MasterKendaraanEntity>,
    baterayList: List<BaterayEntity> = emptyList(),
    banList: List<BanEntity> = emptyList(),
    koplingList: List<KoplingEntity> = emptyList(),
    reservasiList: List<ReservasiEntity> = emptyList(),
    pengerjaanTeknikList: List<PengerjaanTeknikEntity> = emptyList(),
    argoList: List<ArgoEntity> = emptyList(),
    onSaveKendaraan: (MasterKendaraanEntity) -> Unit,
    onDeleteKendaraan: (Int) -> Unit,
    onOrderClick: (String) -> Unit = {},
    onSyncClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingKendaraan by remember { mutableStateOf<MasterKendaraanEntity?>(null) }

    // State untuk popup rincian unit saat No Mobil diklik
    var selectedDetailKendaraan by remember { mutableStateOf<MasterKendaraanEntity?>(null) }

    // Dialog form states
    var noMobil by remember { mutableStateOf("") }
    var noPolisi by remember { mutableStateOf("") }
    var tipe by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }
    var noChassis by remember { mutableStateOf("") }
    var tahunPembuatan by remember { mutableStateOf("") }
    var stikerBandara by remember { mutableStateOf("") }
    var engineType by remember { mutableStateOf("") }
    var jadwalMobil by remember { mutableStateOf("") }

    val processedList = remember(kendaraanList, searchQuery) {
        val list = if (searchQuery.isBlank()) kendaraanList else {
            val q = searchQuery.trim().lowercase()
            kendaraanList.filter {
                it.noMobil.lowercase().contains(q) ||
                        it.noPolisi.lowercase().contains(q) ||
                        it.tipe.lowercase().contains(q) ||
                        it.equipment.lowercase().contains(q) ||
                        it.noChassis.lowercase().contains(q) ||
                        it.tahunPembuatan.lowercase().contains(q) ||
                        it.stikerBandara.lowercase().contains(q) ||
                        it.engineType.lowercase().contains(q) ||
                        it.jadwalMobil.lowercase().contains(q) ||
                        it.id.toString().contains(q)
            }
        }
        list.sortedWith { a, b -> compareKendaraanById(a, b, ascendingId = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Top Card: Header & Pencarian
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = HighDensityBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "MASTER KENDARAAN",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Blue900
                            )
                        )
                        Surface(
                            color = Blue50,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Total: ${kendaraanList.size} Unit",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Blue700),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Tombol Sinkronisasi Cepat Google Sheets
                    IconButton(onClick = onSyncClick) {
                        Icon(Icons.Default.Sync, contentDescription = "Sinkronisasi Sheets", tint = Blue700)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari No Mobil, No Polisi, Type, Equipment, Chassis, Tahun, Stiker, Engine...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HighDensityBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TABEL MASTER KENDARAAN
        if (processedList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tidak ada kendaraan yang cocok dengan pencarian."
                            else "Belum ada data Master Kendaraan dari Google Sheets.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
                        )
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                MasterKendaraanTableView(
                    items = processedList,
                    onNoMobilClick = { item ->
                        selectedDetailKendaraan = item
                    },
                    onEditItem = { item ->
                        editingKendaraan = item
                        noMobil = item.noMobil
                        noPolisi = item.noPolisi
                        tipe = item.tipe
                        equipment = item.equipment
                        noChassis = item.noChassis
                        tahunPembuatan = item.tahunPembuatan
                        stikerBandara = item.stikerBandara
                        engineType = item.engineType
                        jadwalMobil = item.jadwalMobil
                        showEditDialog = true
                    },
                    onDeleteItem = { item ->
                        onDeleteKendaraan(item.id)
                    }
                )
            }
        }
    }

    // POPUP RINCIAN UNIT KENDARAAN (SAAT NO MOBIL DIKLIK)
    if (selectedDetailKendaraan != null) {
        val vehicle = selectedDetailKendaraan!!

        RincianKendaraanDialog(
            vehicle = vehicle,
            baterayList = baterayList,
            banList = banList,
            koplingList = koplingList,
            reservasiList = reservasiList,
            pengerjaanTeknikList = pengerjaanTeknikList,
            argoList = argoList,
            onDismiss = { selectedDetailKendaraan = null },
            onEditClick = {
                editingKendaraan = vehicle
                noMobil = vehicle.noMobil
                noPolisi = vehicle.noPolisi
                tipe = vehicle.tipe
                equipment = vehicle.equipment
                noChassis = vehicle.noChassis
                tahunPembuatan = vehicle.tahunPembuatan
                stikerBandara = vehicle.stikerBandara
                engineType = vehicle.engineType
                jadwalMobil = vehicle.jadwalMobil
                selectedDetailKendaraan = null
                showEditDialog = true
            },
            onOrderClick = { orderNo ->
                selectedDetailKendaraan = null
                onOrderClick(orderNo)
            }
        )
    }

    // Dialog Tambah / Edit Master Kendaraan
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (editingKendaraan == null) "Tambah Master Kendaraan" else "Edit Kendaraan #${editingKendaraan?.noMobil}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = noMobil,
                            onValueChange = { noMobil = it },
                            label = { Text("NO_MOBIL") },
                            placeholder = { Text("Contoh: 101") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = noPolisi,
                            onValueChange = { noPolisi = it },
                            label = { Text("NO_POLISI") },
                            placeholder = { Text("Contoh: B 1024 MW") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tipe,
                            onValueChange = { tipe = it },
                            label = { Text("TYPE") },
                            placeholder = { Text("Transmover / Avanza") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = equipment,
                            onValueChange = { equipment = it },
                            label = { Text("Equipment") },
                            placeholder = { Text("Terpasang / GPS") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = noChassis,
                            onValueChange = { noChassis = it },
                            label = { Text("ChassisNo") },
                            placeholder = { Text("Nomor Rangka") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = tahunPembuatan,
                            onValueChange = { tahunPembuatan = it },
                            label = { Text("TAHUN") },
                            placeholder = { Text("2024") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stikerBandara,
                            onValueChange = { stikerBandara = it },
                            label = { Text("Stiker Bandara") },
                            placeholder = { Text("Aktif / Expired / -") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = engineType,
                            onValueChange = { engineType = it },
                            label = { Text("Engine") },
                            placeholder = { Text("GASOLINE / 1NR-VE") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = jadwalMobil,
                        onValueChange = { jadwalMobil = it },
                        label = { Text("Jadwal Mobil (Opsional)") },
                        placeholder = { Text("Jadwal Reguler") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noMobil.isNotBlank()) {
                            onSaveKendaraan(
                                MasterKendaraanEntity(
                                    id = editingKendaraan?.id ?: 0,
                                    noMobil = noMobil.trim(),
                                    noPolisi = noPolisi.trim(),
                                    tipe = tipe.trim().ifBlank { "Armada Bengkel" },
                                    equipment = equipment.trim(),
                                    noChassis = noChassis.trim(),
                                    tahunPembuatan = tahunPembuatan.trim(),
                                    stikerBandara = stikerBandara.trim(),
                                    engineType = engineType.trim(),
                                    jadwalMobil = jadwalMobil.trim().ifBlank { "Jadwal Reguler" }
                                )
                            )
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Komponen Tabel Master Kendaraan Sesuai Permintaan:
 * Susunan Kolom: | NO_MOBIL | NO_POLISI | TYPE | Equipment | ChassisNo | TAHUN | Stiker Bandara | Engine |
 */
@Composable
fun MasterKendaraanTableView(
    items: List<MasterKendaraanEntity>,
    onNoMobilClick: (MasterKendaraanEntity) -> Unit,
    onEditItem: (MasterKendaraanEntity) -> Unit = {},
    onDeleteItem: (MasterKendaraanEntity) -> Unit = {}
) {
    // Lebar kolom tabel proporsional & presisi untuk 8 kolom
    val colNoMobilWidth = 130.dp
    val colNoPolisiWidth = 140.dp
    val colTypeWidth = 170.dp
    val colEquipmentWidth = 150.dp
    val colChassisNoWidth = 180.dp
    val colTahunWidth = 110.dp
    val colStikerBandaraWidth = 150.dp
    val colEngineWidth = 140.dp
    val totalTableWidth = colNoMobilWidth + colNoPolisiWidth + colTypeWidth + colEquipmentWidth +
            colChassisNoWidth + colTahunWidth + colStikerBandaraWidth + colEngineWidth

    val headerBg = HighDensityBlueDark
    val headerBorderColor = Color(0xFF1E3A8A)
    val cellBorderColor = Color(0xFFE2E8F0)

    val horizontalScrollState = rememberScrollState()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
                    .width(totalTableWidth)
            ) {
                // Header Tabel: NO_MOBIL | NO_POLISI | TYPE | Equipment | ChassisNo | TAHUN | Stiker Bandara | Engine
                Surface(
                    color = headerBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. NO_MOBIL
                        TableHeadCell(title = "NO_MOBIL", width = colNoMobilWidth, align = Alignment.Center)
                        TableDivider(headerBorderColor)

                        // 2. NO_POLISI
                        TableHeadCell(title = "NO_POLISI", width = colNoPolisiWidth, align = Alignment.Center)
                        TableDivider(headerBorderColor)

                        // 3. TYPE
                        TableHeadCell(title = "TYPE", width = colTypeWidth, align = Alignment.CenterStart)
                        TableDivider(headerBorderColor)

                        // 4. Equipment
                        TableHeadCell(title = "Equipment", width = colEquipmentWidth, align = Alignment.CenterStart)
                        TableDivider(headerBorderColor)

                        // 5. ChassisNo
                        TableHeadCell(title = "ChassisNo", width = colChassisNoWidth, align = Alignment.CenterStart)
                        TableDivider(headerBorderColor)

                        // 6. TAHUN
                        TableHeadCell(title = "TAHUN", width = colTahunWidth, align = Alignment.Center)
                        TableDivider(headerBorderColor)

                        // 7. Stiker Bandara
                        TableHeadCell(title = "Stiker Bandara", width = colStikerBandaraWidth, align = Alignment.Center)
                        TableDivider(headerBorderColor)

                        // 8. Engine
                        TableHeadCell(title = "Engine", width = colEngineWidth, align = Alignment.Center)
                    }
                }

                // Data Baris Tabel
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                        val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. NO_MOBIL (Clickable pill badge)
                            Box(
                                modifier = Modifier
                                    .width(colNoMobilWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = Blue50,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                                    modifier = Modifier.clickable { onNoMobilClick(item) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = "Rincian Unit",
                                            tint = HighDensityBlue,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "#${item.noMobil}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = HighDensityBlue
                                            ),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                            TableDivider(cellBorderColor)

                            // 2. NO_POLISI
                            Box(
                                modifier = Modifier
                                    .width(colNoPolisiWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Text(
                                        text = item.noPolisi.ifBlank { "-" },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Blue900
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            TableDivider(cellBorderColor)

                            // 3. TYPE
                            Box(
                                modifier = Modifier
                                    .width(colTypeWidth)
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = item.tipe.ifBlank { "-" },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate800
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TableDivider(cellBorderColor)

                            // 4. Equipment
                            Box(
                                modifier = Modifier
                                    .width(colEquipmentWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val equip = item.equipment.trim()
                                if (equip.isNotBlank()) {
                                    val isInstalled = equip.contains("Terpasang", ignoreCase = true) ||
                                            equip.contains("Ya", ignoreCase = true) ||
                                            equip.contains("Ada", ignoreCase = true) ||
                                            equip.contains("GPS", ignoreCase = true)
                                    val equipBg = if (isInstalled) Color(0xFFF0FDF4) else Color(0xFFF1F5F9)
                                    val equipBorder = if (isInstalled) Color(0xFFBBF7D0) else Color(0xFFCBD5E1)
                                    val equipText = if (isInstalled) Color(0xFF166534) else Slate600

                                    Surface(
                                        color = equipBg,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, equipBorder)
                                    ) {
                                        Text(
                                            text = equip,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = equipText
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                            }
                            TableDivider(cellBorderColor)

                            // 5. ChassisNo
                            Box(
                                modifier = Modifier
                                    .width(colChassisNoWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = item.noChassis.ifBlank { "-" },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Slate800,
                                        fontSize = 11.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            TableDivider(cellBorderColor)

                            // 6. TAHUN
                            Box(
                                modifier = Modifier
                                    .width(colTahunWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = Blue50,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, HighDensityBlueBorder)
                                ) {
                                    Text(
                                        text = item.tahunPembuatan.ifBlank { "-" },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Blue700
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            TableDivider(cellBorderColor)

                            // 7. Stiker Bandara
                            Box(
                                modifier = Modifier
                                    .width(colStikerBandaraWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val stiker = item.stikerBandara.trim()
                                if (stiker.isNotBlank()) {
                                    val isAktif = stiker.contains("Aktif", ignoreCase = true) ||
                                            stiker.contains("Ada", ignoreCase = true) ||
                                            stiker.contains("Ya", ignoreCase = true) ||
                                            stiker.contains("Berlaku", ignoreCase = true)
                                    val isExpired = stiker.contains("Expired", ignoreCase = true) ||
                                            stiker.contains("Habis", ignoreCase = true) ||
                                            stiker.contains("Mati", ignoreCase = true)
                                    val stikerBg = when {
                                        isAktif -> Color(0xFFF0FDF4)
                                        isExpired -> Color(0xFFFEF2F2)
                                        else -> Color(0xFFF8FAFC)
                                    }
                                    val stikerBorder = when {
                                        isAktif -> Color(0xFFBBF7D0)
                                        isExpired -> Color(0xFFFECACA)
                                        else -> Color(0xFFCBD5E1)
                                    }
                                    val stikerText = when {
                                        isAktif -> Color(0xFF166534)
                                        isExpired -> Color(0xFFDC2626)
                                        else -> Blue900
                                    }

                                    Surface(
                                        color = stikerBg,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, stikerBorder)
                                    ) {
                                        Text(
                                            text = stiker,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = stikerText
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            TableDivider(cellBorderColor)

                            // 8. Engine
                            Box(
                                modifier = Modifier
                                    .width(colEngineWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val engine = item.engineType.trim()
                                if (engine.isNotBlank()) {
                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Text(
                                            text = engine,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Slate800
                                            ),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400),
                                        textAlign = TextAlign.Center
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

/**
 * MODAL DIALOG RINCIAN DARI UNIT KENDARAAN
 * Menampilkan:
 * 1. Informasi Master Mobil
 * 2. Data BATERAY (referensi dari No Mobil)
 * 3. Data BAN (di bawah Bateray, referensi dari No Mobil)
 * 4. Data KOPLING (di bawah Ban, referensi dari No Mobil)
 * 5. Data RESERVASI (di bawah Kopling, hanya tampil 5 saja dengan tombol View All)
 */
@Composable
fun RincianKendaraanDialog(
    vehicle: MasterKendaraanEntity,
    baterayList: List<BaterayEntity>,
    banList: List<BanEntity>,
    koplingList: List<KoplingEntity>,
    reservasiList: List<ReservasiEntity>,
    pengerjaanTeknikList: List<PengerjaanTeknikEntity> = emptyList(),
    riwayatSkiList: List<RiwayatSkiEntity> = emptyList(),
    argoList: List<ArgoEntity> = emptyList(),
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onOrderClick: (String) -> Unit = {}
) {
    // State toggle untuk Reservasi "View All"
    var showAllReservasi by remember { mutableStateOf(false) }
    // State pencarian part reservasi
    var reservasiSearchQuery by remember { mutableStateOf("") }

    // Filter komponen berdasarkan No Mobil
    val matchingArgo = remember(vehicle.noMobil, argoList) {
        argoList.filter { isVehicleMatch(it.noMobil, vehicle.noMobil) }
    }

    val matchingBateray = remember(vehicle.noMobil, baterayList) {
        baterayList.filter { isVehicleMatch(it.noMobil, vehicle.noMobil) }
    }

    val matchingBan = remember(vehicle.noMobil, banList) {
        banList.filter { isVehicleMatch(it.noMobil, vehicle.noMobil) }
    }

    val matchingKopling = remember(vehicle.noMobil, koplingList) {
        koplingList.filter { isVehicleMatch(it.noMobil, vehicle.noMobil) }
    }

    val matchingReservasi = remember(vehicle.noMobil, reservasiList) {
        reservasiList
            .filter { isVehicleMatch(it.noMobil, vehicle.noMobil) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.materialDescription.trim().ifBlank { it.material.trim() } })
    }

    val searchedReservasi = remember(matchingReservasi, reservasiSearchQuery) {
        val query = reservasiSearchQuery.trim().lowercase()
        if (query.isEmpty()) {
            matchingReservasi
        } else {
            matchingReservasi.filter { res ->
                res.material.lowercase().contains(query) ||
                        res.materialDescription.lowercase().contains(query) ||
                        res.orderNo.lowercase().contains(query) ||
                        res.batch.lowercase().contains(query) ||
                        res.postingDate.lowercase().contains(query) ||
                        res.documentDate.lowercase().contains(query)
            }
        }
    }

    val displayedReservasi = remember(searchedReservasi, showAllReservasi, reservasiSearchQuery) {
        // Jika sedang mencari, tampilkan semua hasil pencarian yang cocok
        if (reservasiSearchQuery.isNotBlank() || showAllReservasi) searchedReservasi else searchedReservasi.take(5)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Blue50,
                        border = BorderStroke(1.dp, HighDensityBlueBorder),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = HighDensityBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Rincian Unit #${vehicle.noMobil}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Blue900,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = "${vehicle.noPolisi} • ${vehicle.tipe}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate600,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Slate400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ==========================================
                // INFORMASI UTAMA UNIT ARMADA
                // ==========================================
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "INFORMASI UTAMA ARMADA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = HighDensityBlue,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        InfoRowDetail(label = "NO_MOBIL", value = "#${vehicle.noMobil}", isBold = true)
                        InfoRowDetail(label = "NO_POLISI", value = vehicle.noPolisi.ifBlank { "-" })
                        InfoRowDetail(label = "TYPE", value = vehicle.tipe.ifBlank { "-" })
                        InfoRowDetail(label = "Equipment", value = vehicle.equipment.ifBlank { "-" })
                        InfoRowDetail(label = "ChassisNo", value = vehicle.noChassis.ifBlank { "-" })
                        InfoRowDetail(label = "TAHUN", value = vehicle.tahunPembuatan.ifBlank { "-" })
                        InfoRowDetail(label = "Stiker Bandara", value = vehicle.stikerBandara.ifBlank { "-" })
                        InfoRowDetail(label = "Engine", value = vehicle.engineType.ifBlank { "-" })
                        if (vehicle.jadwalMobil.isNotBlank()) {
                            InfoRowDetail(label = "Jadwal Mobil", value = vehicle.jadwalMobil)
                        }
                    }
                }

                // ==========================================
                // 0. DATA ARGO (JIKA ADA REFERENSI NO MOBIL)
                // ==========================================
                if (matchingArgo.isNotEmpty()) {
                    SectionContainer(
                        icon = Icons.Default.LocalAtm,
                        title = "DATA ARGO",
                        badgeText = "${matchingArgo.size} Data",
                        headerColor = HighDensityBlue,
                        badgeBg = Blue50
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            matchingArgo.forEachIndexed { idx, argo ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, HighDensityBlueBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${idx + 1}. Golongan: ${argo.golongan.ifBlank { "-" }}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Blue900,
                                                    fontSize = 12.sp
                                                )
                                            )
                                            Surface(
                                                color = Blue50,
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, HighDensityBlueBorder)
                                            ) {
                                                Text(
                                                    text = "KM: ${argo.km.ifBlank { "-" }}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = HighDensityBlue,
                                                        fontSize = 11.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "KM Kosong: ${argo.kmKosong.ifBlank { "-" }} • KM Isi: ${argo.kmIsi.ifBlank { "-" }}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontSize = 11.5.sp)
                                        )
                                        Text(
                                            text = "Rit: ${argo.rit.ifBlank { "-" }} • Drop: ${argo.drop.ifBlank { "-" }}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 1. DATA BATERAY (REFERENSI NO MOBIL)
                // ==========================================
                SectionContainer(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "DATA BATERAY / ACCU",
                    badgeText = "${matchingBateray.size} Data",
                    headerColor = Color(0xFF047857),
                    badgeBg = Color(0xFFECFDF5)
                ) {
                    if (matchingBateray.isEmpty()) {
                        EmptyComponentCard(
                            text = "Tidak ada data Bateray / Accu untuk unit #${vehicle.noMobil}"
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            matchingBateray.forEachIndexed { idx, bat ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${idx + 1}. ${bat.displayTipeAccu.ifBlank { "Accu Unit" }}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF065F46),
                                                    fontSize = 12.sp
                                                )
                                            )
                                            val usiaVal = parseUsiaValue(bat.displayUsia)
                                            val isUsiaOver12 = usiaVal > 12.0
                                            val usiaTextColor = if (isUsiaOver12) Rose600 else Color(0xFF047857)
                                            val usiaBgColor = if (isUsiaOver12) Color(0xFFFEF2F2) else Color(0xFFECFDF5)
                                            val usiaBorderColor = if (isUsiaOver12) Color(0xFFFECACA) else Color(0xFF10B981)

                                            Surface(
                                                color = usiaBgColor,
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(0.5.dp, usiaBorderColor)
                                            ) {
                                                Text(
                                                    text = bat.displayUsia.ifBlank { "Usia -" },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = usiaTextColor,
                                                        fontSize = 11.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tipe: ${bat.displayTipe} • Plat: ${bat.platNomer.ifBlank { vehicle.noPolisi }}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.5.sp)
                                        )
                                        if (bat.tanggal.isNotBlank() || bat.tanggalGanti.isNotBlank()) {
                                            Text(
                                                text = "Tgl Pasang: ${bat.tanggalGanti.ifBlank { bat.tanggal }}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                                            )
                                        }
                                        if (bat.voltaseKondisi.isNotBlank()) {
                                            Text(
                                                text = "Kondisi: ${bat.voltaseKondisi}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF059669), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 2. DATA BAN (DI BAWAH BATERAY, REFERENSI NO MOBIL)
                // ==========================================
                SectionContainer(
                    icon = Icons.Default.TripOrigin,
                    title = "DATA BAN",
                    badgeText = "${matchingBan.size} Data",
                    headerColor = HighDensityBlue,
                    badgeBg = Blue50
                ) {
                    if (matchingBan.isEmpty()) {
                        EmptyComponentCard(
                            text = "Tidak ada data Ban untuk unit #${vehicle.noMobil}"
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            matchingBan.forEachIndexed { idx, ban ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, HighDensityBlueBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${idx + 1}. Posisi: ${ban.posisiBan.ifBlank { "Roda" }}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Blue900,
                                                    fontSize = 12.sp
                                                )
                                            )
                                            if (ban.displayKmTotal.isNotBlank()) {
                                                val kmBanVal = parseKmValue(ban.displayKmTotal)
                                                val isBanOver90k = kmBanVal > 90000.0
                                                val kmBanTextColor = if (isBanOver90k) Rose600 else HighDensityBlue
                                                val kmBanBgColor = if (isBanOver90k) Color(0xFFFEF2F2) else Blue50
                                                val kmBanBorderColor = if (isBanOver90k) Color(0xFFFECACA) else HighDensityBlue.copy(alpha = 0.3f)

                                                Surface(
                                                    color = kmBanBgColor,
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(0.5.dp, kmBanBorderColor)
                                                ) {
                                                    Text(
                                                        text = "${ban.displayKmTotal} KM",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = kmBanTextColor,
                                                            fontSize = 11.sp
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Merk / Embos: ${ban.displayEmbos.ifBlank { "-" }}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                        )
                                        if (ban.tanggalPasang.isNotBlank()) {
                                            Text(
                                                text = "Tgl Pasang: ${ban.tanggalPasang}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 3. DATA KOPLING (DI BAWAH BAN)
                // ==========================================
                SectionContainer(
                    icon = Icons.Default.Tune,
                    title = "DATA KOPLING",
                    badgeText = "${matchingKopling.size} Data",
                    headerColor = Color(0xFFB45309),
                    badgeBg = Color(0xFFFEF3C7)
                ) {
                    if (matchingKopling.isEmpty()) {
                        EmptyComponentCard(
                            text = "Tidak ada data Kopling untuk unit #${vehicle.noMobil}"
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            matchingKopling.forEachIndexed { idx, kop ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${idx + 1}. Tipe: ${kop.jenisTipe.ifBlank { "Kopling Unit" }}",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF78350F),
                                                    fontSize = 12.sp
                                                )
                                            )
                                            if (kop.displayTotalKm.isNotBlank()) {
                                                val kmKopVal = parseKmValue(kop.displayTotalKm)
                                                val isKopOver90k = kmKopVal > 90000.0
                                                val kmKopTextColor = if (isKopOver90k) Rose600 else Color(0xFFB45309)
                                                val kmKopBgColor = if (isKopOver90k) Color(0xFFFEF2F2) else Color(0xFFFEF3C7)
                                                val kmKopBorderColor = if (isKopOver90k) Color(0xFFFECACA) else Color(0xFFF59E0B)

                                                Surface(
                                                    color = kmKopBgColor,
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(0.5.dp, kmKopBorderColor)
                                                ) {
                                                    Text(
                                                        text = "${kop.displayTotalKm} KM",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = kmKopTextColor,
                                                            fontSize = 11.sp
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (kop.tanggalGanti.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Tgl Ganti: ${kop.tanggalGanti}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                                            )
                                        }
                                        if (kop.catatan.isNotBlank()) {
                                            Text(
                                                text = "Catatan: ${kop.catatan}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontSize = 11.sp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. DATA RESERVASI (DI BAWAH KOPLING, DILENGKAPI PENCARIAN PART)
                // ==========================================
                SectionContainer(
                    icon = Icons.Default.ReceiptLong,
                    title = "DATA RESERVASI",
                    badgeText = "${matchingReservasi.size} Item",
                    headerColor = Color(0xFF6B21A8),
                    badgeBg = Color(0xFFF3E8FF)
                ) {
                    if (matchingReservasi.isEmpty()) {
                        EmptyComponentCard(
                            text = "Tidak ada data Reservasi suku cadang untuk unit #${vehicle.noMobil}"
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Kolom Pencarian Part / Order Reservasi
                            OutlinedTextField(
                                value = reservasiSearchQuery,
                                onValueChange = { reservasiSearchQuery = it },
                                placeholder = { Text("Cari part / material, deskripsi, order...", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Cari Part",
                                        tint = Color(0xFF6B21A8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (reservasiSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { reservasiSearchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Hapus",
                                                tint = Slate400,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF9333EA),
                                    unfocusedBorderColor = Color(0xFFE9D5FF),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFFAF5FF)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            if (searchedReservasi.isEmpty()) {
                                Surface(
                                    color = Color(0xFFFAF5FF),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFF3E8FF)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFF9333EA),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Tidak ditemukan part reservasi yang cocok dengan \"$reservasiSearchQuery\"",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF6B21A8),
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = if (reservasiSearchQuery.isNotBlank()) {
                                        "Ditemukan ${searchedReservasi.size} dari ${matchingReservasi.size} item reservasi:"
                                    } else if (matchingReservasi.size > 5 && !showAllReservasi) {
                                        "Menampilkan 5 dari ${matchingReservasi.size} item reservasi:"
                                    } else {
                                        "Menampilkan seluruh ${matchingReservasi.size} item reservasi:"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate600,
                                        fontSize = 11.sp
                                    )
                                )

                                displayedReservasi.forEachIndexed { idx, res ->
                                val cleanOrder = res.orderNo.trim()
                                val hasOrder = cleanOrder.isNotBlank() && cleanOrder != "-"
                                val hasTeknik = hasOrder && pengerjaanTeknikList.any { isOrderMatch(it.orderNo, cleanOrder) }

                                val (badgeBg, badgeText, badgeBorder, badgeLabel) = when {
                                    hasTeknik -> QuadColor(HighDensityBlueLight, HighDensityBlue, HighDensityBlueBorder, "Ke Teknik (SKD)")
                                    else -> QuadColor(Color(0xFFF8FAFC), Color(0xFF581C87), Color(0xFFE9D5FF), "Buka Pengerjaan")
                                }

                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (hasOrder) {
                                                Surface(
                                                    color = badgeBg,
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, badgeBorder),
                                                    modifier = Modifier.clickable { onOrderClick(cleanOrder) }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "${idx + 1}. Order #$cleanOrder",
                                                            style = MaterialTheme.typography.labelMedium.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = badgeText,
                                                                fontSize = 12.sp
                                                            )
                                                        )
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.OpenInNew,
                                                            contentDescription = "Buka $badgeLabel",
                                                            tint = badgeText,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = badgeLabel,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = badgeText,
                                                                fontSize = 10.sp
                                                            )
                                                        )
                                                    }
                                                }
                                            } else {
                                                Text(
                                                    text = "${idx + 1}. Order -",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Slate600,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                            }

                                            if (res.quantity.isNotBlank()) {
                                                Surface(
                                                    color = Color(0xFFF3E8FF),
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = BorderStroke(0.5.dp, Color(0xFFA855F7))
                                                ) {
                                                    Text(
                                                        text = "${res.quantity} ${res.baseUnitOfMeasure}".trim(),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF6B21A8),
                                                            fontSize = 11.sp
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = res.materialDescription.ifBlank { res.material }.ifBlank { "Suku Cadang" },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate800,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            val tgl = res.postingDate.ifBlank { res.documentDate }
                                            if (tgl.isNotBlank()) {
                                                Text(
                                                    text = "Tgl: $tgl",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Slate600,
                                                        fontSize = 10.5.sp
                                                    )
                                                )
                                            }
                                            if (res.amtInLocCur.isNotBlank()) {
                                                Text(
                                                    text = "Rp ${res.amtInLocCur}",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF047857),
                                                        fontSize = 10.5.sp
                                                    )
                                                )
                                            }
                                        }

                                        if (res.batch.isNotBlank()) {
                                            Text(
                                                text = "Batch: ${res.batch}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Slate400,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // TOMBOL "VIEW ALL" JIKA DATA LEBIH DARI 5 ITEM DAN TIDAK SEDANG FILTER SEARCH
                            if (reservasiSearchQuery.isBlank() && matchingReservasi.size > 5) {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = { showAllReservasi = !showAllReservasi },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B21A8)),
                                    border = BorderStroke(1.dp, Color(0xFFA855F7)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (showAllReservasi) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (showAllReservasi) "Tampilkan 5 Saja" else "View All (${matchingReservasi.size} Item Reservasi)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                ) {
                    Text("Tutup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    )
}

@Composable
private fun TableHeadCell(title: String, width: androidx.compose.ui.unit.Dp, align: Alignment) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp),
        contentAlignment = align
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
private fun SectionContainer(
    icon: ImageVector,
    title: String,
    badgeText: String,
    headerColor: Color,
    badgeBg: Color,
    content: @Composable () -> Unit
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = headerColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = headerColor,
                            letterSpacing = 0.5.sp,
                            fontSize = 11.5.sp
                        )
                    )
                }
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, headerColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = headerColor,
                            fontSize = 10.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun EmptyComponentCard(text: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.5.sp)
            )
        }
    }
}

@Composable
private fun InfoRowDetail(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.5.sp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                color = if (isBold) HighDensityBlue else Slate800,
                fontSize = if (isBold) 12.5.sp else 11.5.sp
            )
        )
    }
}
