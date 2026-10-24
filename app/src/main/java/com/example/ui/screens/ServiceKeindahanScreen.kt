package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.data.local.entity.ServiceKeindahanEntity
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueBorder
import com.example.ui.theme.HighDensityBlueDark
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800

/**
 * Komparator pengurutan Service Keindahan berdasarkan ID.
 * Secara default: ID pertama di atas (ascendingId = true: 1, 2, 3...)
 */
fun compareKeindahanById(a: ServiceKeindahanEntity, b: ServiceKeindahanEntity, ascendingId: Boolean = true): Int {
    return if (ascendingId) a.id.compareTo(b.id) else b.id.compareTo(a.id)
}

/**
 * Helper untuk memeriksa apakah status atau status mobil adalah "KALONG".
 * Jika status sama dengan "kalong" (case-insensitive), maka kolom No Mobil akan berubah menjadi kuning.
 */
fun isKalongStatus(item: ServiceKeindahanEntity): Boolean {
    return item.statusMobil.trim().equals("KALONG", ignoreCase = true) ||
            item.status.trim().equals("KALONG", ignoreCase = true)
}

/**
 * Helper untuk menentukan style Selisih KM:
 * - Jika nilai > -500 dan < 0 -> warna kuning angka cetak tebal (FontWeight.Bold)
 * - Jika nilai > 0 -> warna merah angka cetak tebal
 * - Nilai <= -500 -> warna netral normal
 */
data class SelisihKmStyle(
    val containerColor: Color,
    val textColor: Color,
    val borderColor: Color,
    val isBold: Boolean
)

fun getSelisihKmStyle(rawKm: String): SelisihKmStyle {
    val num = parseSelisihKmNumber(rawKm)
    return when {
        num != null && num > -500.0 && num < 0.0 -> {
            // Kuning & cetak tebal
            SelisihKmStyle(
                containerColor = Color(0xFFFEF9C3), // Yellow 100
                textColor = Color(0xFFB45309),      // Dark Amber/Yellow
                borderColor = Color(0xFFFDE047),    // Yellow 300
                isBold = true
            )
        }
        num != null && num >= 0.0 -> {
            // Merah (> 0)
            SelisihKmStyle(
                containerColor = Color(0xFFFEE2E2), // Red 100
                textColor = Color(0xFFDC2626),      // Red 600
                borderColor = Color(0xFFFCA5A5),    // Red 300
                isBold = true
            )
        }
        else -> {
            // Netral (<= -500 atau belum terdefinisi)
            SelisihKmStyle(
                containerColor = Color(0xFFF1F5F9), // Slate 100
                textColor = Color(0xFF334155),      // Slate 700
                borderColor = Color(0xFFE2E8F0),    // Slate 200
                isBold = false
            )
        }
    }
}

@Composable
fun ServiceKeindahanScreen(
    serviceList: List<ServiceKeindahanEntity>,
    kendaraanList: List<MasterKendaraanEntity> = emptyList(),
    uploadStatusMessage: String? = null,
    onSaveService: (ServiceKeindahanEntity) -> Unit,
    onDeleteService: (Long) -> Unit,
    onVehicleClick: (String) -> Unit,
    onSyncClick: () -> Unit,
    onKerjakan: (noMobil: String, jenisService: String, notifNo: String) -> Unit = { _, _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortAscendingId by remember { mutableStateOf(true) } // ID pertama di atas secara default
    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ServiceKeindahanEntity?>(null) }
    var viewingItem by remember { mutableStateOf<ServiceKeindahanEntity?>(null) }

    // Dialog state
    var statusMobil by remember { mutableStateOf("PAGI") }
    var noMobil by remember { mutableStateOf("") }
    var jenisService by remember { mutableStateOf("P09") }
    var selisihKm by remember { mutableStateOf("0") }
    var notificationDesc by remember { mutableStateOf("") }
    var notifNo by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("OSNO") }
    var kmServisSebelumnya by remember { mutableStateOf("") }
    var kmJatuhTempo by remember { mutableStateOf("") }
    var equipmentNo by remember { mutableStateOf("") }
    var notifDate by remember { mutableStateOf("") }
    var kmTotal by remember { mutableStateOf("") }
    var stickerBandara by remember { mutableStateOf("Terpasang") }

    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var statusMobilDropdownExpanded by remember { mutableStateOf(false) }

    // Filter & Sort list sesuai ID
    val filteredList = remember(serviceList, searchQuery, sortAscendingId) {
        serviceList
            .filter { item ->
                if (searchQuery.isBlank()) true else {
                    val q = searchQuery.trim().lowercase()
                    item.noMobil.lowercase().contains(q) ||
                            item.jenisService.lowercase().contains(q) ||
                            item.notificationDesc.lowercase().contains(q) ||
                            item.notifNo.lowercase().contains(q) ||
                            item.equipmentNo.lowercase().contains(q) ||
                            item.statusMobil.lowercase().contains(q) ||
                            item.status.lowercase().contains(q)
                }
            }
            .sortedWith { a, b -> compareKeindahanById(a, b, ascendingId = sortAscendingId) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Control Bar Card (SERVICE KEINDAHAN, Sync Logo & Pencarian)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // 1. Judul SERVICE KEINDAHAN & Tombol Sinkron Hanya Logo di Pojok Kanan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = HighDensityBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "JADWAL SERVICE KEINDAHAN",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Blue900,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    // Tombol sinkron hanya logo di pojok kanan
                    IconButton(
                        onClick = onSyncClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sinkronkan Data",
                            tint = HighDensityBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Form Pencarian di Bawah Tulisan SERVICE KEINDAHAN (Lebar Menyesuaikan Tulisan)
                val searchFieldWidth = remember(searchQuery) {
                    if (searchQuery.isBlank()) 140.dp else (130 + searchQuery.length * 11).coerceIn(140, 320).dp
                }

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .width(searchFieldWidth)
                        .height(36.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = HighDensityBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Cari",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                cursorBrush = SolidColor(HighDensityBlue),
                                textStyle = TextStyle(
                                    fontSize = 12.sp,
                                    color = Blue900,
                                    fontWeight = FontWeight.Normal
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Banner Proses Upload Foto Pengerjaan Service Keindahan di latar belakang
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
                isSuccessMsg -> Color(0xFF047857)
                isErrorMsg -> Color(0xFFB91C1C)
                else -> HighDensityBlue
            }

            Surface(
                color = bannerBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, bannerBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
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
                            color = HighDensityBlue
                        )
                    } else {
                        Icon(
                            imageVector = if (isSuccessMsg) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                            contentDescription = null,
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

        // Konten: Tabel atau Kartu
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Tidak ada data Service Keindahan.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onSyncClick,
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sinkronkan dari Google Sheets")
                    }
                }
            }
        } else {
            // TAMPILAN TABEL DENGAN LEBAR KOLOM FIX KE TEKS
            ServiceKeindahanTableView(
                modifier = Modifier.weight(1f),
                items = filteredList,
                onEdit = { item ->
                    editingItem = item
                    statusMobil = item.statusMobil
                    noMobil = item.noMobil
                    jenisService = item.jenisService
                    selisihKm = item.selisihKm
                    notificationDesc = item.notificationDesc
                    notifNo = item.notifNo
                    status = item.status
                    kmServisSebelumnya = item.kmServisSebelumnya
                    kmJatuhTempo = item.kmJatuhTempo
                    equipmentNo = item.equipmentNo
                    notifDate = item.notifDate
                    kmTotal = item.kmTotal
                    stickerBandara = item.stickerBandara
                    showDialog = true
                },
                onDelete = { onDeleteService(it.id) },
                onViewDetail = { viewingItem = it },
                onVehicleClick = onVehicleClick,
                onKerjakan = { item ->
                    onKerjakan(item.noMobil, item.jenisService, item.notifNo)
                }
            )
        }
    }

    // Detail Dialog (Ditambahkan Tombol Kerjakan)
    viewingItem?.let { item ->
        val isKalong = isKalongStatus(item)
        val kmStyle = getSelisihKmStyle(item.selisihKm)

        AlertDialog(
            onDismissRequest = { viewingItem = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Detail Mobil #${item.noMobil}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Blue900
                        )
                    )
                    if (isKalong) {
                        Surface(
                            color = Color(0xFFFEF08A),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFFEAB308))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.NightlightRound, contentDescription = null, tint = Color(0xFF854D0E), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("KALONG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF854D0E))
                            }
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRowItem(label = "ID", value = "#${item.id}")
                    DetailRowItem(label = "Status Mobil", value = item.statusMobil)
                    DetailRowItem(label = "No Mobil", value = item.noMobil)
                    DetailRowItem(label = "Jenis Service", value = item.jenisService)
                    DetailRowItem(label = "Selisih KM", value = item.selisihKm)
                    DetailRowItem(label = "Notification Desc", value = item.notificationDesc)
                    DetailRowItem(label = "Notif. No", value = item.notifNo)
                    DetailRowItem(label = "Status", value = item.status)
                    DetailRowItem(label = "KM Servis Sebelumnya", value = item.kmServisSebelumnya)
                    DetailRowItem(label = "KM Jatuh Tempo", value = item.kmJatuhTempo)
                    DetailRowItem(label = "Equipment No", value = item.equipmentNo)
                    DetailRowItem(label = "Notif Date", value = item.notifDate)
                    DetailRowItem(label = "KM Total", value = item.kmTotal)
                    DetailRowItem(label = "Sticker Bandara", value = item.stickerBandara)
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val mob = item.noMobil
                            val srv = item.jenisService
                            val notif = item.notifNo
                            viewingItem = null
                            onKerjakan(mob, srv, notif)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kerjakan")
                    }
                    TextButton(
                        onClick = { viewingItem = null }
                    ) {
                        Text("Tutup")
                    }
                }
            }
        )
    }

    // Add / Edit Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (editingItem == null) "Tambah Service Keindahan" else "Edit Keindahan Mobil #${editingItem?.noMobil}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = noMobil,
                        onValueChange = { noMobil = it },
                        label = { Text("Kolom 1: No Mobil") },
                        placeholder = { Text("Contoh: MWD6404") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Dropdown Status Mobil (PAGI / KALONG / etc.)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = statusMobil,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status Mobil (PAGI / KALONG)") },
                            trailingIcon = {
                                IconButton(onClick = { statusMobilDropdownExpanded = !statusMobilDropdownExpanded }) {
                                    Icon(
                                        imageVector = if (statusMobilDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = HighDensityBlue
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { statusMobilDropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = statusMobilDropdownExpanded,
                            onDismissRequest = { statusMobilDropdownExpanded = false }
                        ) {
                            listOf("PAGI", "KALONG", "SIANG", "MALAM").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        statusMobil = s
                                        statusMobilDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = jenisService,
                        onValueChange = { jenisService = it },
                        label = { Text("Kolom 2: Service") },
                        placeholder = { Text("Contoh: P09") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = selisihKm,
                        onValueChange = { selisihKm = it },
                        label = { Text("Kolom 3: Selisih KM") },
                        placeholder = { Text("Contoh: -159 atau 278") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notificationDesc,
                        onValueChange = { notificationDesc = it },
                        label = { Text("Kolom 4: Notif Desc") },
                        placeholder = { Text("Contoh: (P09) SERVICE KI NEW TRANS") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notifNo,
                        onValueChange = { notifNo = it },
                        label = { Text("Kolom 5: Notif_NO") },
                        placeholder = { Text("Contoh: 12256771") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Dropdown Status (OSNO / NOPR)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kolom 6: Status (OSNO / NOPR)") },
                            trailingIcon = {
                                IconButton(onClick = { statusDropdownExpanded = !statusDropdownExpanded }) {
                                    Icon(
                                        imageVector = if (statusDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = HighDensityBlue
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { statusDropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            listOf("OSNO", "NOPR", "KALONG").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        status = s
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = kmServisSebelumnya,
                            onValueChange = { kmServisSebelumnya = it },
                            label = { Text("KM Servis Sblm") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = kmJatuhTempo,
                            onValueChange = { kmJatuhTempo = it },
                            label = { Text("KM Jatuh Tempo") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = equipmentNo,
                            onValueChange = { equipmentNo = it },
                            label = { Text("Equipment No") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stickerBandara,
                            onValueChange = { stickerBandara = it },
                            label = { Text("Sticker Bandara") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noMobil.isNotBlank()) {
                            val entity = ServiceKeindahanEntity(
                                id = editingItem?.id ?: 0L,
                                statusMobil = statusMobil.trim(),
                                noMobil = noMobil.trim(),
                                jenisService = jenisService.trim(),
                                selisihKm = selisihKm.trim(),
                                notificationDesc = notificationDesc.trim(),
                                notifNo = notifNo.trim(),
                                status = status.trim(),
                                kmServisSebelumnya = kmServisSebelumnya.trim(),
                                kmJatuhTempo = kmJatuhTempo.trim(),
                                equipmentNo = equipmentNo.trim(),
                                notifDate = notifDate.trim(),
                                kmTotal = kmTotal.trim(),
                                stickerBandara = stickerBandara.trim(),
                                isSynced = false
                            )
                            onSaveService(entity)
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Tabel Service Keindahan dengan lebar kolom Fix ke teks:
 * - Kolom 1: No Mobil (Lebar 120.dp) - Jika status == KALONG, berubah menjadi kuning
 * - Kolom 2: Service (Lebar 110.dp)
 * - Kolom 3: Selisih KM (Lebar 130.dp) - Jika -500 < nilai < 0 -> Kuning Tebal, jika nilai > 0 -> Merah
 * - Kolom 4: Notif Desc (Lebar 250.dp)
 * - Kolom 5: Notif_NO (Lebar 115.dp)
 * - Kolom 6: Status (Lebar 95.dp)
 * - Kolom 7: Aksi (Lebar 75.dp)
 */
@Composable
private fun ServiceKeindahanTableView(
    modifier: Modifier = Modifier,
    items: List<ServiceKeindahanEntity>,
    onEdit: (ServiceKeindahanEntity) -> Unit,
    onDelete: (ServiceKeindahanEntity) -> Unit,
    onViewDetail: (ServiceKeindahanEntity) -> Unit,
    onVehicleClick: (String) -> Unit,
    onKerjakan: (ServiceKeindahanEntity) -> Unit
) {
    val colNoMobilWidth = 120.dp
    val colServiceWidth = 110.dp
    val colSelisihKmWidth = 130.dp
    val colNotifDescWidth = 250.dp
    val colNotifNoWidth = 115.dp
    val colStatusWidth = 95.dp
    val colAksiWidth = 110.dp

    val totalTableWidth = colNoMobilWidth + colServiceWidth + colSelisihKmWidth +
            colNotifDescWidth + colNotifNoWidth + colStatusWidth + colAksiWidth

    val cellBorderColor = Color(0xFFE2E8F0)
    val headerBorderColor = Color(0x33FFFFFF)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val horizontalScrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
                    .width(totalTableWidth)
            ) {
                // HEADER TABEL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Blue900)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kolom 1: No Mobil
                    Box(
                        modifier = Modifier
                            .width(colNoMobilWidth)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Mobil",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                    // Kolom 2: Service (Rata Tengah)
                    Box(
                        modifier = Modifier
                            .width(colServiceWidth)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Service",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                    // Kolom 3: Selisih KM
                    Box(
                        modifier = Modifier
                            .width(colSelisihKmWidth)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Selisih KM",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                    // Kolom 4: Notif Desc
                    Box(
                        modifier = Modifier
                            .width(colNotifDescWidth)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "Notif Desc",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                    // Kolom 5: Notif_NO
                    Box(
                        modifier = Modifier
                            .width(colNotifNoWidth)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Notif_NO",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                    // Kolom 6: Status
                    Box(
                        modifier = Modifier
                            .width(colStatusWidth)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Status",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                    // Kolom 7: Aksi (Kerjakan)
                    Box(
                        modifier = Modifier
                            .width(colAksiWidth)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aksi",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ISI BARIS TABEL
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { item ->
                        val isKalong = isKalongStatus(item)
                        val kmStyle = getSelisihKmStyle(item.selisihKm)

                        // Styling Kolom 1 (No Mobil):
                        // "jika status sama dengan kalong maka 1 pada No Mobil akan berubah menjadi kuning"
                        val noMobilContainerColor = if (isKalong) Color(0xFFFEF08A) else Blue50
                        val noMobilBorderColor = if (isKalong) Color(0xFFEAB308) else HighDensityBlueBorder
                        val noMobilTextColor = if (isKalong) Color(0xFF854D0E) else HighDensityBlue
                        val noMobilIconColor = if (isKalong) Color(0xFF854D0E) else HighDensityBlue

                        val rowBg = if (isKalong) Color(0xFFFFFBEB) else Color.White

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .border(BorderStroke(0.5.dp, cellBorderColor))
                                .clickable { onViewDetail(item) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Kolom 1: No Mobil
                            Box(
                                modifier = Modifier
                                    .width(colNoMobilWidth)
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = noMobilContainerColor,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, noMobilBorderColor),
                                    modifier = Modifier.clickable { onVehicleClick(item.noMobil) }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isKalong) Icons.Default.NightlightRound else Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = noMobilIconColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "#${item.noMobil}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = noMobilTextColor
                                            ),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                            Box(modifier = Modifier.width(1.dp).height(36.dp).background(cellBorderColor))

                            // Kolom 2: Service (Rata Tengah)
                            Box(
                                modifier = Modifier
                                    .width(colServiceWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.jenisService.ifBlank { "-" },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Blue900
                                    ),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(36.dp).background(cellBorderColor))

                            // Kolom 3: Selisih KM
                            // - jika nilai di atas -500 dan di bawah 0 -> kuning angka cetak tebal
                            // - jika nilai di atas 0 -> merah
                            Box(
                                modifier = Modifier
                                    .width(colSelisihKmWidth)
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = kmStyle.containerColor,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, kmStyle.borderColor)
                                ) {
                                    val formattedKm = if (item.selisihKm.endsWith("KM", ignoreCase = true)) {
                                        item.selisihKm
                                    } else {
                                        "${item.selisihKm} KM"
                                    }
                                    Text(
                                        text = formattedKm,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (kmStyle.isBold) FontWeight.Bold else FontWeight.Normal,
                                            color = kmStyle.textColor
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Box(modifier = Modifier.width(1.dp).height(36.dp).background(cellBorderColor))

                            // Kolom 4: Notif Desc
                            Box(
                                modifier = Modifier
                                    .width(colNotifDescWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = item.notificationDesc.ifBlank { "-" },
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate800),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(36.dp).background(cellBorderColor))

                            // Kolom 5: Notif_NO
                            Box(
                                modifier = Modifier
                                    .width(colNotifNoWidth)
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = Slate100,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = item.notifNo.ifBlank { "-" },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = Slate800
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Box(modifier = Modifier.width(1.dp).height(36.dp).background(cellBorderColor))

                            // Kolom 6: Status
                            Box(
                                modifier = Modifier
                                    .width(colStatusWidth)
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val isNopr = item.status.equals("NOPR", ignoreCase = true)
                                val isOsno = item.status.equals("OSNO", ignoreCase = true)
                                val statusBg = when {
                                    isNopr -> Color(0xFFDCFCE7)
                                    isOsno -> Color(0xFFFEF3C7)
                                    isKalong -> Color(0xFFFEF08A)
                                    else -> Color(0xFFE2E8F0)
                                }
                                val statusText = when {
                                    isNopr -> Color(0xFF15803D)
                                    isOsno -> Color(0xFFB45309)
                                    isKalong -> Color(0xFF854D0E)
                                    else -> Color(0xFF334155)
                                }
                                Surface(
                                    color = statusBg,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = item.status.ifBlank { "OSNO" },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = statusText
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Box(modifier = Modifier.width(1.dp).height(36.dp).background(cellBorderColor))

                            // Kolom 7: Aksi (Tombol Kerjakan)
                            Box(
                                modifier = Modifier
                                    .width(colAksiWidth)
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = { onKerjakan(item) },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Kerjakan",
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Kerjakan",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
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

@Composable
private fun ServiceKeindahanCard(
    item: ServiceKeindahanEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onVehicleClick: () -> Unit,
    onKerjakan: () -> Unit = {}
) {
    val isKalong = isKalongStatus(item)
    val kmStyle = getSelisihKmStyle(item.selisihKm)

    val noMobilContainerColor = if (isKalong) Color(0xFFFEF08A) else Blue50
    val noMobilBorderColor = if (isKalong) Color(0xFFEAB308) else HighDensityBlueBorder
    val noMobilTextColor = if (isKalong) Color(0xFF854D0E) else HighDensityBlue

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isKalong) Color(0xFFFFFBEB) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (isKalong) BorderStroke(1.dp, Color(0xFFFDE047)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kolom 1: No Mobil (Kuning jika Kalong)
                Surface(
                    color = noMobilContainerColor,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, noMobilBorderColor),
                    modifier = Modifier.clickable { onVehicleClick() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isKalong) Icons.Default.NightlightRound else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = noMobilTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mobil #${item.noMobil} ↗",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = noMobilTextColor
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isKalong) {
                        Surface(
                            color = Color(0xFFFEF08A),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFFEAB308))
                        ) {
                            Text(
                                text = "KALONG",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF854D0E)
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        color = if (item.status.equals("NOPR", ignoreCase = true)) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.status,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (item.status.equals("NOPR", ignoreCase = true)) Color(0xFF15803D) else Color(0xFFB45309)
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = Blue50,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.statusMobil,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Blue700
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kolom 2: Service
            Text(
                text = item.jenisService,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Blue900)
            )

            // Kolom 4: Notif Desc
            if (item.notificationDesc.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.notificationDesc,
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate800)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kolom 3: Selisih KM
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Selisih KM: ", style = MaterialTheme.typography.labelSmall.copy(color = Slate600))
                            Surface(
                                color = kmStyle.containerColor,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(0.5.dp, kmStyle.borderColor)
                            ) {
                                val formattedKm = if (item.selisihKm.endsWith("KM", ignoreCase = true)) {
                                    item.selisihKm
                                } else {
                                    "${item.selisihKm} KM"
                                }
                                Text(
                                    text = formattedKm,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = kmStyle.textColor,
                                        fontWeight = if (kmStyle.isBold) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (item.kmServisSebelumnya.isNotBlank()) {
                            Text("KM Sblm: ${item.kmServisSebelumnya}", style = MaterialTheme.typography.labelSmall.copy(color = Slate600))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        // Kolom 5: Notif_NO
                        Text("Notif No: ${item.notifNo}", style = MaterialTheme.typography.labelSmall.copy(color = Slate600))
                        if (item.equipmentNo.isNotBlank()) {
                            Text("Eq: ${item.equipmentNo}", style = MaterialTheme.typography.labelSmall.copy(color = Slate600))
                        }
                    }

                    if (item.stickerBandara.isNotBlank()) {
                        Text("Sticker Bandara: ${item.stickerBandara}", style = MaterialTheme.typography.labelSmall.copy(color = Blue700, fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onKerjakan,
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Kerjakan",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = HighDensityBlue, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Rose600, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRowItem(label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontWeight = FontWeight.Medium),
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontWeight = FontWeight.Bold),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
