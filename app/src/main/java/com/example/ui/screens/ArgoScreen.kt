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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
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
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueBorder
import com.example.ui.theme.HighDensityBlueDark
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800

/**
 * MENU: ARGO
 * Spreadsheet ID: 13iuagC5JY1NOkVRMwebz8-XvaEp-2q_qiyaDedGedNQ, Tab: ARGO
 *
 * Kolom Tabel:
 * ID | NO MOBIL | KM KOSONG | KM ISI | RIT | DROP | KM | GOLONGAN | Aksi
 */
@Composable
fun ArgoScreen(
    argoList: List<ArgoEntity>,
    kendaraanList: List<MasterKendaraanEntity> = emptyList(),
    baterayList: List<BaterayEntity> = emptyList(),
    banList: List<BanEntity> = emptyList(),
    koplingList: List<KoplingEntity> = emptyList(),
    reservasiList: List<ReservasiEntity> = emptyList(),
    pengerjaanTeknikList: List<PengerjaanTeknikEntity> = emptyList(),
    riwayatSkiList: List<RiwayatSkiEntity> = emptyList(),
    onSaveArgo: (ArgoEntity) -> Unit = {},
    onDeleteArgo: (Long) -> Unit = {},
    onVehicleClick: (String) -> Unit = {},
    onOrderClick: (String) -> Unit = {},
    onEditMasterKendaraan: () -> Unit = {},
    onSyncClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ArgoEntity?>(null) }
    var selectedNoMobilForDetail by remember { mutableStateOf<String?>(null) }

    // Dialog state
    var noMobilInput by remember { mutableStateOf("") }
    var kmKosongInput by remember { mutableStateOf("") }
    var kmIsiInput by remember { mutableStateOf("") }
    var ritInput by remember { mutableStateOf("") }
    var dropInput by remember { mutableStateOf("") }
    var kmInput by remember { mutableStateOf("") }
    var golonganInput by remember { mutableStateOf("") }

    val filteredList = remember(argoList, searchQuery) {
        val q = searchQuery.trim().lowercase()
        val list = if (q.isBlank()) {
            argoList
        } else {
            argoList.filter {
                it.noMobil.lowercase().contains(q) ||
                        it.kmKosong.lowercase().contains(q) ||
                        it.kmIsi.lowercase().contains(q) ||
                        it.rit.lowercase().contains(q) ||
                        it.drop.lowercase().contains(q) ||
                        it.km.lowercase().contains(q) ||
                        it.golongan.lowercase().contains(q) ||
                        it.id.toString().contains(q)
            }
        }
        list.sortedBy { it.id }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // TOP CARD: Header Info & Pencarian
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
                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalAtm,
                                    contentDescription = null,
                                    tint = HighDensityBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Text(
                            text = "ARGO",
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
                                text = "Total: ${argoList.size} Data",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Blue700
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onSyncClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue700)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sinkronisasi",
                            tint = Blue700,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sinkron",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Blue700
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Kolom Pencarian
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = Slate800
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, Color(0xFFCBD5E1)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = HighDensityBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari No Mobil, KM, Rit, Drop, Golongan...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate400,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus",
                                    tint = Slate400,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TABEL ARGO
        if (filteredList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocalAtm,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tidak ada data ARGO yang cocok dengan pencarian."
                            else "Belum ada data ARGO dari Google Sheets.",
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
                ArgoTableView(
                    items = filteredList,
                    onVehicleClick = { noMobil ->
                        val cleaned = noMobil.trim()
                        if (cleaned.isNotBlank() && cleaned != "-") {
                            selectedNoMobilForDetail = cleaned
                        }
                    },
                    onRowClick = { item ->
                        val cleaned = item.noMobil.trim()
                        if (cleaned.isNotBlank() && cleaned != "-") {
                            selectedNoMobilForDetail = cleaned
                        }
                    },
                    onEditItem = { item ->
                        editingItem = item
                        noMobilInput = item.noMobil
                        kmKosongInput = item.kmKosong
                        kmIsiInput = item.kmIsi
                        ritInput = item.rit
                        dropInput = item.drop
                        kmInput = item.km
                        golonganInput = item.golongan
                        showEditDialog = true
                    },
                    onDeleteItem = { item ->
                        onDeleteArgo(item.id)
                    }
                )
            }
        }
    }

    // POPUP RINCIAN UNIT KENDARAAN (SAAT NO MOBIL DIKLIK DI MENU ARGO)
    if (selectedNoMobilForDetail != null) {
        val targetNo = selectedNoMobilForDetail!!
        val vehicle = kendaraanList.find { isVehicleMatch(it.noMobil, targetNo) }
            ?: MasterKendaraanEntity(
                id = 0,
                noMobil = targetNo.trim().removePrefix("#"),
                noPolisi = "-",
                tipe = "Armada Bengkel",
                tahunPembuatan = "-",
                jadwalMobil = "Jadwal Reguler"
            )

        RincianKendaraanDialog(
            vehicle = vehicle,
            baterayList = baterayList,
            banList = banList,
            koplingList = koplingList,
            reservasiList = reservasiList,
            pengerjaanTeknikList = pengerjaanTeknikList,
            riwayatSkiList = riwayatSkiList,
            argoList = argoList,
            onDismiss = { selectedNoMobilForDetail = null },
            onEditClick = {
                selectedNoMobilForDetail = null
                onEditMasterKendaraan()
            },
            onOrderClick = { orderNo ->
                selectedNoMobilForDetail = null
                onOrderClick(orderNo)
            }
        )
    }

    // Dialog Edit / Tambah ARGO
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (editingItem == null) "Tambah Data ARGO" else "Edit ARGO #${editingItem?.noMobil}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = noMobilInput,
                        onValueChange = { noMobilInput = it },
                        label = { Text("No Mobil") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = kmKosongInput,
                            onValueChange = { kmKosongInput = it },
                            label = { Text("KM Kosong") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = kmIsiInput,
                            onValueChange = { kmIsiInput = it },
                            label = { Text("KM Isi") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ritInput,
                            onValueChange = { ritInput = it },
                            label = { Text("Rit") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dropInput,
                            onValueChange = { dropInput = it },
                            label = { Text("Drop") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = kmInput,
                            onValueChange = { kmInput = it },
                            label = { Text("KM") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = golonganInput,
                            onValueChange = { golonganInput = it },
                            label = { Text("Golongan") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noMobilInput.isNotBlank()) {
                            onSaveArgo(
                                ArgoEntity(
                                    id = editingItem?.id ?: 0,
                                    noMobil = noMobilInput.trim(),
                                    kmKosong = kmKosongInput.trim(),
                                    kmIsi = kmIsiInput.trim(),
                                    rit = ritInput.trim(),
                                    drop = dropInput.trim(),
                                    km = kmInput.trim(),
                                    golongan = golonganInput.trim(),
                                    isSynced = false
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
 * Tabel ARGO dengan susunan kolom:
 * ID | NO MOBIL | KM KOSONG | KM ISI | RIT | DROP | KM | GOLONGAN | Aksi
 */
@Composable
fun ArgoTableView(
    items: List<ArgoEntity>,
    onVehicleClick: (String) -> Unit,
    onRowClick: (ArgoEntity) -> Unit = {},
    onEditItem: (ArgoEntity) -> Unit,
    onDeleteItem: (ArgoEntity) -> Unit
) {
    val colIdWidth = 70.dp
    val colNoMobilWidth = 140.dp
    val colKmKosongWidth = 130.dp
    val colKmIsiWidth = 130.dp
    val colRitWidth = 100.dp
    val colDropWidth = 120.dp
    val colKmWidth = 110.dp
    val colGolonganWidth = 110.dp
    val colAksiWidth = 80.dp

    val totalTableWidth = colIdWidth + colNoMobilWidth + colKmKosongWidth + colKmIsiWidth +
            colRitWidth + colDropWidth + colKmWidth + colGolonganWidth + colAksiWidth

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
                // Header Tabel
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
                        // 1. ID
                        ArgoHeadCell(title = "ID", width = colIdWidth, align = Alignment.Center)
                        ArgoDivider(headerBorderColor)

                        // 2. NO MOBIL
                        ArgoHeadCell(title = "NO MOBIL", width = colNoMobilWidth, align = Alignment.Center)
                        ArgoDivider(headerBorderColor)

                        // 3. KM KOSONG
                        ArgoHeadCell(title = "KM KOSONG", width = colKmKosongWidth, align = Alignment.Center)
                        ArgoDivider(headerBorderColor)

                        // 4. KM ISI
                        ArgoHeadCell(title = "KM ISI", width = colKmIsiWidth, align = Alignment.Center)
                        ArgoDivider(headerBorderColor)

                        // 5. RIT
                        ArgoHeadCell(title = "RIT", width = colRitWidth, align = Alignment.Center)
                        ArgoDivider(headerBorderColor)

                        // 6. DROP
                        ArgoHeadCell(title = "DROP", width = colDropWidth, align = Alignment.Center)
                        ArgoDivider(headerBorderColor)

                        // 7. KM
                        ArgoHeadCell(title = "KM", width = colKmWidth, align = Alignment.Center)
                        ArgoDivider(headerBorderColor)

                        // 8. GOLONGAN
                        ArgoHeadCell(title = "GOLONGAN", width = colGolonganWidth, align = Alignment.Center)
                        ArgoDivider(headerBorderColor)

                        // 9. Aksi
                        ArgoHeadCell(title = "Aksi", width = colAksiWidth, align = Alignment.Center)
                    }
                }

                // Baris Data Tabel
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(items, key = { idx, item -> "${item.id}_${item.noMobil}_$idx" }) { index, item ->
                        val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Kolom 1: ID
                            Box(
                                modifier = Modifier
                                    .width(colIdWidth)
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.id.toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate600
                                    )
                                )
                            }
                            ArgoDivider(cellBorderColor)

                            // Kolom 2: NO MOBIL (Clickable pill badge)
                            Box(
                                modifier = Modifier
                                    .width(colNoMobilWidth)
                                    .clickable { onVehicleClick(item.noMobil) }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = Blue50,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                                    modifier = Modifier.clickable { onVehicleClick(item.noMobil) }
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
                                            text = if (item.noMobil.isNotBlank()) "#${item.noMobil.trim().removePrefix("#")}" else "-",
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
                            ArgoDivider(cellBorderColor)

                            // Kolom 3: KM KOSONG
                            Box(
                                modifier = Modifier
                                    .width(colKmKosongWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.kmKosong.ifBlank { "-" },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Slate800
                                    ),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            ArgoDivider(cellBorderColor)

                            // Kolom 4: KM ISI
                            Box(
                                modifier = Modifier
                                    .width(colKmIsiWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.kmIsi.ifBlank { "-" },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Slate800
                                    ),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            ArgoDivider(cellBorderColor)

                            // Kolom 5: RIT
                            Box(
                                modifier = Modifier
                                    .width(colRitWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Text(
                                        text = item.rit.ifBlank { "-" },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate800
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                            ArgoDivider(cellBorderColor)

                            // Kolom 6: DROP
                            Box(
                                modifier = Modifier
                                    .width(colDropWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.drop.ifBlank { "-" },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = Slate800
                                    ),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            ArgoDivider(cellBorderColor)

                            // Kolom 7: KM
                            Box(
                                modifier = Modifier
                                    .width(colKmWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    color = Blue50,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, HighDensityBlueBorder)
                                ) {
                                    Text(
                                        text = item.km.ifBlank { "-" },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Blue700
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                            ArgoDivider(cellBorderColor)

                            // Kolom 8: GOLONGAN
                            Box(
                                modifier = Modifier
                                    .width(colGolonganWidth)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val isCs = item.golongan.contains("CS", ignoreCase = true)
                                val golBg = if (isCs) Color(0xFFF0FDF4) else Color(0xFFFEF3C7)
                                val golBorder = if (isCs) Color(0xFFBBF7D0) else Color(0xFFFDE68A)
                                val golText = if (isCs) Color(0xFF166534) else Color(0xFF92400E)

                                Surface(
                                    color = golBg,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, golBorder)
                                ) {
                                    Text(
                                        text = item.golongan.ifBlank { "-" },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = golText
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                            ArgoDivider(cellBorderColor)

                            // Kolom 9: Aksi
                            Box(
                                modifier = Modifier
                                    .width(colAksiWidth)
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onEditItem(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = HighDensityBlue,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteItem(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus",
                                            tint = Rose600,
                                            modifier = Modifier.size(17.dp)
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
private fun ArgoHeadCell(title: String, width: androidx.compose.ui.unit.Dp, align: Alignment) {
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
private fun ArgoDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(color)
    )
}

@Composable
private fun ArgoDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Slate600)
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Slate800)
        )
    }
}
