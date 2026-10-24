package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.data.local.entity.ReservasiEntity
import com.example.data.local.entity.RiwayatSkiEntity
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueBorder
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.isOrderMatch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MENU RIWAYAT SERVICE SKI
 * Database ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY
 * Nama Sheet: SKI
 *
 * Susunan Kolom Tabel:
 * | No. | Tanggal | Order | No Mobil | Description | SERVICE | Km Service | Aksi |
 *
 * Hak Akses: Super Admin, Kepala Bengkel, SH, GH, Keindahan
 */
@Composable
fun RiwayatSkiScreen(
    riwayatSkiList: List<RiwayatSkiEntity>,
    reservasiList: List<ReservasiEntity> = emptyList(),
    kendaraanList: List<MasterKendaraanEntity> = emptyList(),
    initialOrderNo: String? = null,
    initialDetailItem: RiwayatSkiEntity? = null,
    onClearInitialDetail: () -> Unit = {},
    onSaveRiwayatSki: (RiwayatSkiEntity) -> Unit = {},
    onDeleteRiwayatSki: (Long) -> Unit = {},
    onVehicleClick: (String) -> Unit,
    onSyncClick: () -> Unit,
    onBack: () -> Unit = {}
) {
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf(initialOrderNo ?: "") }
    var detailItem by remember { mutableStateOf(initialDetailItem) }
    var editingItem by remember { mutableStateOf<RiwayatSkiEntity?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialDetailItem) {
        if (initialDetailItem != null) {
            detailItem = initialDetailItem
            if (initialDetailItem.orderNo.isNotBlank()) {
                searchQuery = initialDetailItem.orderNo
            }
        }
    }

    LaunchedEffect(initialOrderNo, riwayatSkiList) {
        if (!initialOrderNo.isNullOrBlank()) {
            searchQuery = initialOrderNo
            if (detailItem == null) {
                val found = riwayatSkiList.find { isOrderMatch(it.orderNo, initialOrderNo) }
                if (found != null) {
                    detailItem = found
                }
            }
        }
    }

    // Urutkan data tabel sesuai dengan urutan data tabel yang di Google Sheets
    val sortedList = remember(riwayatSkiList) {
        riwayatSkiList.sortedBy { it.id }
    }

    val filteredList = remember(sortedList, searchQuery) {
        val base = if (searchQuery.isBlank()) {
            sortedList
        } else {
            val q = searchQuery.trim().lowercase()
            sortedList.filter {
                it.noMobil.lowercase().contains(q) ||
                        it.orderNo.lowercase().contains(q) ||
                        it.service.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.tanggal.lowercase().contains(q) ||
                        it.kmService.lowercase().contains(q)
            }
        }
        base.take(500)
    }

    // Lebar kolom tabel
    val widthNo = 52.dp
    val widthTanggal = 120.dp
    val widthOrder = 140.dp
    val widthNoMobil = 120.dp
    val widthDescription = 280.dp
    val widthService = 180.dp
    val widthKmService = 120.dp
    val totalTableWidth = widthNo + widthTanggal + widthOrder + widthNoMobil + widthDescription + widthService + widthKmService

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // TOP CONTROL CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "RIWAYAT SERVICE SKI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Blue900,
                                fontSize = 17.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SEARCH BAR
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Slate800),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari No Mobil, Order, Service, Deskripsi, Tanggal...",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate400, fontSize = 13.sp)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus",
                                    tint = Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // HORIZONTAL SCROLLABLE TABLE
        val horizontalScrollState = rememberScrollState()

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp)
                .padding(bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column(modifier = Modifier.width(totalTableWidth)) {
                    val headerBorderColor = Color.White.copy(alpha = 0.25f)
                    val cellBorderColor = Color(0xFFE2E8F0)

                    // TABLE HEADER: No. | Tanggal | Order | No Mobil | Description | SERVICE | Km Service
                    Row(
                        modifier = Modifier
                            .width(totalTableWidth)
                            .background(Color(0xFF065F46))
                            .height(44.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderCell("No.", widthNo, headerBorderColor)
                        HeaderCell("Tanggal", widthTanggal, headerBorderColor)
                        HeaderCell("Order", widthOrder, headerBorderColor)
                        HeaderCell("No Mobil", widthNoMobil, headerBorderColor)
                        HeaderCell("Description", widthDescription, headerBorderColor)
                        HeaderCell("SERVICE", widthService, headerBorderColor)
                        HeaderCell("Km Service", widthKmService, null)
                    }

                    // ROWS
                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .width(totalTableWidth)
                                .height(260.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "Data tidak ditemukan untuk pencarian \"$searchQuery\""
                                    else "Belum ada data Riwayat Service SKI.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Slate600,
                                        textAlign = TextAlign.Center
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                                ) {
                                    Text(
                                        text = "Database: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY | Sheet: SKI",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF047857),
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onSyncClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sinkron Sekarang", style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .width(totalTableWidth)
                                .fillMaxHeight()
                        ) {
                            itemsIndexed(filteredList) { index, item ->
                                val rowBgColor = if (index % 2 == 0) Color.White else Color(0xFFF9FAFB)

                                Row(
                                    modifier = Modifier
                                        .width(totalTableWidth)
                                        .background(rowBgColor)
                                        .border(BorderStroke(0.5.dp, cellBorderColor))
                                        .clickable { detailItem = item },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. No.
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate600,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier
                                            .width(widthNo)
                                            .padding(vertical = 10.dp, horizontal = 4.dp)
                                    )

                                    // 2. Tanggal
                                    Text(
                                        text = item.tanggal.ifBlank { "-" },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate800,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        modifier = Modifier
                                            .width(widthTanggal)
                                            .padding(vertical = 10.dp, horizontal = 8.dp)
                                    )

                                    // 3. Order (Clickable to view row detail)
                                    Box(
                                        modifier = Modifier
                                            .width(widthOrder)
                                            .clickable { detailItem = item }
                                            .padding(vertical = 8.dp, horizontal = 8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFECFDF5),
                                            border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                            modifier = Modifier.clickable { detailItem = item }
                                        ) {
                                            Text(
                                                text = if (item.orderNo.isNotBlank()) "#${item.orderNo.removePrefix("#")}" else "-",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF047857),
                                                    fontSize = 11.5.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // 4. No Mobil (Clickable -> respon sesuai dengan baris / buka rincian baris Riwayat Service SKI)
                                    Box(
                                        modifier = Modifier
                                            .width(widthNoMobil)
                                            .clickable { detailItem = item }
                                            .padding(vertical = 8.dp, horizontal = 8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Blue50,
                                            border = BorderStroke(1.dp, HighDensityBlueBorder),
                                            modifier = Modifier.clickable { detailItem = item }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsCar,
                                                    contentDescription = "Rincian Baris",
                                                    tint = HighDensityBlue,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = item.noMobil.ifBlank { "-" },
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = HighDensityBlue,
                                                        fontSize = 11.5.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    // 5. Description
                                    Text(
                                        text = item.description.ifBlank { "-" },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate800,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .width(widthDescription)
                                            .padding(vertical = 10.dp, horizontal = 8.dp)
                                    )

                                    // 6. SERVICE
                                    Box(
                                        modifier = Modifier
                                            .width(widthService)
                                            .padding(vertical = 8.dp, horizontal = 8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFEF3C7),
                                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                                        ) {
                                            Text(
                                                text = item.service.ifBlank { "-" },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF92400E),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 11.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // 7. Km Service
                                    Row(
                                        modifier = Modifier
                                            .width(widthKmService)
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = null,
                                            tint = Slate400,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (item.kmService.isNotBlank()) "${item.kmService} KM" else "-",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate800,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.5.sp
                                            ),
                                            maxLines = 1
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

    // DETAIL DIALOG
    detailItem?.let { item ->
        AlertDialog(
            onDismissRequest = {
                detailItem = null
                onClearInitialDetail()
            },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFECFDF5),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Rincian Riwayat Service SKI",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                    )
                    Text(
                        text = "Order #${item.orderNo.ifBlank { "-" }}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRowItem(label = "Tanggal", value = item.tanggal.ifBlank { "-" })
                    DetailRowItem(label = "Nomor Order", value = item.orderNo.ifBlank { "-" })
                    DetailRowItem(label = "No Mobil", value = item.noMobil.ifBlank { "-" }, isCar = true, onCarClick = {
                        detailItem = null
                        onClearInitialDetail()
                        onVehicleClick(item.noMobil)
                    })
                    DetailRowItem(label = "Jenis Service", value = item.service.ifBlank { "-" })
                    DetailRowItem(label = "KM Service", value = if (item.kmService.isNotBlank()) "${item.kmService} KM" else "-")
                    DetailRowItem(label = "Deskripsi / Pekerjaan", value = item.description.ifBlank { "-" })

                    // DATA RESERVASI SESUAI DENGAN NO ORDER PADA BARIS INI
                    val matchingReservasi = remember(item.orderNo, reservasiList) {
                        val cleanOrder = item.orderNo.trim().removePrefix("#")
                        if (cleanOrder.isBlank()) emptyList()
                        else {
                            val numOrder = cleanOrder.toLongOrNull()
                            reservasiList.filter { res ->
                                val resClean = res.orderNo.trim().removePrefix("#")
                                val resNum = resClean.toLongOrNull()
                                resClean.equals(cleanOrder, ignoreCase = true) ||
                                        (numOrder != null && resNum != null && numOrder == resNum) ||
                                        (cleanOrder.length >= 4 && resClean.contains(cleanOrder, ignoreCase = true)) ||
                                        (resClean.length >= 4 && cleanOrder.contains(resClean, ignoreCase = true))
                            }
                        }
                    }

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "DATA RESERVASI (ORDER #${item.orderNo.ifBlank { "-" }})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate600,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (item.orderNo.isBlank()) {
                                Text(
                                    text = "Nomor Order tidak tersedia pada baris ini.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 12.sp)
                                )
                            } else if (matchingReservasi.isEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tidak ada data reservasi suku cadang untuk Order #${item.orderNo}.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.5.sp)
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Ditemukan ${matchingReservasi.size} item reservasi suku cadang:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF047857),
                                            fontSize = 11.sp
                                        )
                                    )
                                    matchingReservasi.forEachIndexed { idx, res ->
                                        Surface(
                                            color = Color.White,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${idx + 1}. ${res.material.ifBlank { "Material" }}",
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Slate800,
                                                            fontSize = 11.5.sp
                                                        )
                                                    )
                                                    if (res.quantity.isNotBlank()) {
                                                        Surface(
                                                            color = Color(0xFFECFDF5),
                                                            shape = RoundedCornerShape(4.dp),
                                                            border = BorderStroke(0.5.dp, Color(0xFFA7F3D0))
                                                        ) {
                                                            Text(
                                                                text = "${res.quantity} ${res.baseUnitOfMeasure}".trim(),
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF047857),
                                                                    fontSize = 10.5.sp
                                                                ),
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                if (res.materialDescription.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = res.materialDescription,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = Slate600,
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
                    }

                    // Sinkronisasi status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status Cloud:",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontWeight = FontWeight.Medium)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (item.isSynced) Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, if (item.isSynced) Color(0xFFA7F3D0) else Color(0xFFFDE68A))
                        ) {
                            Text(
                                text = if (item.isSynced) "Tersinkron (Sheet SKI)" else "Tersimpan Lokal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (item.isSynced) Color(0xFF047857) else Color(0xFFB45309),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        detailItem = null
                        onClearInitialDetail()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
                ) {
                    Text("Tutup")
                }
            },
            dismissButton = {
                if (item.noMobil.isNotBlank() && item.noMobil != "-") {
                    OutlinedButton(
                        onClick = {
                            val car = item.noMobil
                            detailItem = null
                            onClearInitialDetail()
                            onVehicleClick(car)
                        }
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lihat Mobil")
                    }
                }
            }
        )
    }

    // FORM ADD / EDIT DIALOG
    if (showFormDialog) {
        RiwayatSkiFormDialog(
            item = editingItem,
            kendaraanList = kendaraanList,
            onDismiss = {
                showFormDialog = false
                editingItem = null
            },
            onSave = { savedItem ->
                onSaveRiwayatSki(savedItem)
                showFormDialog = false
                editingItem = null
            }
        )
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp, borderColor: Color?) {
    val mod = Modifier
        .width(width)
        .padding(horizontal = 8.dp)
    val borderedMod = if (borderColor != null) {
        mod.border(BorderStroke(0.5.dp, borderColor))
    } else mod

    Text(
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        ),
        modifier = borderedMod,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun DetailRowItem(
    label: String,
    value: String,
    isCar: Boolean = false,
    onCarClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontWeight = FontWeight.Medium),
            modifier = Modifier.width(110.dp)
        )
        if (isCar && onCarClick != null) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Blue50,
                border = BorderStroke(1.dp, HighDensityBlueBorder),
                modifier = Modifier.clickable { onCarClick() }
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = HighDensityBlue,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate800,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun RiwayatSkiFormDialog(
    item: RiwayatSkiEntity?,
    kendaraanList: List<MasterKendaraanEntity>,
    onDismiss: () -> Unit,
    onSave: (RiwayatSkiEntity) -> Unit
) {
    val defaultDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var tanggal by remember { mutableStateOf(item?.tanggal ?: defaultDate) }
    var orderNo by remember { mutableStateOf(item?.orderNo ?: "") }
    var noMobil by remember { mutableStateOf(item?.noMobil ?: "") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    var service by remember { mutableStateOf(item?.service ?: "SERVICE KEINDAHAN") }
    var kmService by remember { mutableStateOf(item?.kmService ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (item == null) "Tambah Riwayat Service SKI" else "Edit Riwayat Service SKI",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = tanggal,
                    onValueChange = { tanggal = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = orderNo,
                    onValueChange = { orderNo = it },
                    label = { Text("Nomor Order") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = noMobil,
                    onValueChange = { noMobil = it.uppercase() },
                    label = { Text("Nomor Mobil") },
                    placeholder = { Text("Contoh: MWD101") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = service,
                    onValueChange = { service = it },
                    label = { Text("Jenis Service") },
                    placeholder = { Text("Contoh: Cuci Mesin, Salon, Body") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = kmService,
                    onValueChange = { kmService = it },
                    label = { Text("KM Service") },
                    placeholder = { Text("Contoh: 45000") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi / Keterangan") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newItem = RiwayatSkiEntity(
                        id = item?.id ?: 0L,
                        tanggal = tanggal.trim(),
                        orderNo = orderNo.trim(),
                        noMobil = noMobil.trim().uppercase(),
                        description = description.trim(),
                        service = service.trim(),
                        kmService = kmService.trim(),
                        isSynced = item?.isSynced ?: false
                    )
                    onSave(newItem)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857))
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

/**
 * Mengubah string tanggal ke milidetik untuk pengurutan tanggal terbaru ke terlama
 */
private fun parseDateToMillis(dateStr: String): Long {
    val clean = dateStr.trim()
    if (clean.isBlank() || clean == "-") return 0L
    val formats = listOf(
        "yyyy-MM-dd",
        "yyyy/MM/dd",
        "dd/MM/yyyy",
        "dd-MM-yyyy",
        "yyyy-MM-dd HH:mm:ss"
    )
    for (fmt in formats) {
        try {
            val sdf = SimpleDateFormat(fmt, Locale.getDefault())
            sdf.isLenient = true
            val parsed = sdf.parse(clean)
            if (parsed != null) return parsed.time
        } catch (_: Exception) {}
    }
    return try {
        clean.replace("-", "").replace("/", "").trim().toLong()
    } catch (_: Exception) {
        0L
    }
}

