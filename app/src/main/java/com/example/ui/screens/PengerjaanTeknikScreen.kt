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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
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
import com.example.data.local.entity.PengerjaanTeknikEntity
import com.example.data.local.entity.ReservasiEntity
import com.example.ui.viewmodel.isOrderMatch
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800

/**
 * MENU PENGERJAAN TEKNIK / RIWAYAT SERVICE TEKNIK
 * Spreadsheet ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY
 * Nama Sheet: SKD
 *
 * Susunan Kolom Tabel:
 * | Created On | Order | Object Description | Description | KETERANGAN | COUNTER | TIPE |
 *
 * Susunan Kolom Tampilan:
 * | Created On | Order | Object Description | Description | KETERANGAN | COUNTER |
 *
 * Hak Akses: Super Admin, SH, GH, Teknik
 */
@Composable
fun PengerjaanTeknikScreen(
    pengerjaanTeknikList: List<PengerjaanTeknikEntity>,
    reservasiList: List<ReservasiEntity> = emptyList(),
    kendaraanList: List<MasterKendaraanEntity> = emptyList(),
    initialOrderNo: String? = null,
    initialDetailItem: PengerjaanTeknikEntity? = null,
    onClearInitialDetail: () -> Unit = {},
    onVehicleClick: (String) -> Unit,
    onSyncClick: () -> Unit,
    onBack: () -> Unit = {}
) {
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf(initialOrderNo ?: "") }
    var detailItem by remember { mutableStateOf(initialDetailItem) }

    LaunchedEffect(initialDetailItem) {
        if (initialDetailItem != null) {
            detailItem = initialDetailItem
            if (initialDetailItem.orderNo.isNotBlank()) {
                searchQuery = initialDetailItem.orderNo
            }
        }
    }

    LaunchedEffect(initialOrderNo, pengerjaanTeknikList) {
        if (!initialOrderNo.isNullOrBlank()) {
            searchQuery = initialOrderNo
            if (detailItem == null) {
                val found = pengerjaanTeknikList.find { isOrderMatch(it.orderNo, initialOrderNo) }
                if (found != null) {
                    detailItem = found
                }
            }
        }
    }

    val sortedList = remember(pengerjaanTeknikList) {
        pengerjaanTeknikList.sortedBy { it.id }
    }

    val filteredList = remember(sortedList, searchQuery) {
        val base = if (searchQuery.isBlank()) {
            sortedList
        } else {
            val q = searchQuery.trim().lowercase()
            sortedList.filter {
                it.objectDescription.lowercase().contains(q) ||
                    it.noMobil.lowercase().contains(q) ||
                    it.order.lowercase().contains(q) ||
                    it.orderNo.lowercase().contains(q) ||
                    it.displayKeterangan.lowercase().contains(q) ||
                    it.service.lowercase().contains(q) ||
                    it.description.lowercase().contains(q) ||
                    it.createdOn.lowercase().contains(q) ||
                    it.tanggal.lowercase().contains(q) ||
                    it.displayCounter.lowercase().contains(q) ||
                    it.kmService.lowercase().contains(q) ||
                    it.tipe.lowercase().contains(q)
            }
        }
        base.take(500)
    }

    // Column widths for table layout (Susunan Tampilan: Created On | Order | Object Description | Description | KETERANGAN | COUNTER)
    val widthCreatedOn = 110.dp
    val widthOrder = 120.dp
    val widthObjectDesc = 125.dp
    val widthDescription = 260.dp
    val widthKeterangan = 110.dp
    val widthCounter = 115.dp
    val totalTableWidth = widthCreatedOn + widthOrder + widthObjectDesc + widthDescription + widthKeterangan + widthCounter

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // TOP CONTROL CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header Row: Title & Sync button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Blue50,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = HighDensityBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "RIWAYAT SERVICE TEKNIK",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Blue900,
                                letterSpacing = (-0.2).sp
                            )
                        )
                    }

                    // Tombol sinkron hanya logo
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
                .padding(horizontal = 10.dp)
                .padding(bottom = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column(modifier = Modifier.width(totalTableWidth)) {
                    val headerBorderColor = Color.White.copy(alpha = 0.25f)
                    val cellBorderColor = Color(0xFFE2E8F0)

                    // TABLE HEADER: Created On | Order | Object Description | Description | KETERANGAN | COUNTER
                    Row(
                        modifier = Modifier
                            .width(totalTableWidth)
                            .background(HighDensityBlue)
                            .height(44.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Created On
                        HeaderCell("Created On", widthCreatedOn, headerBorderColor)
                        // 2. Order
                        HeaderCell("Order", widthOrder, headerBorderColor)
                        // 3. Object Description
                        HeaderCell("Object Description", widthObjectDesc, headerBorderColor)
                        // 4. Description
                        HeaderCell("Description", widthDescription, headerBorderColor)
                        // 5. KETERANGAN
                        HeaderCell("KETERANGAN", widthKeterangan, headerBorderColor)
                        // 6. COUNTER
                        HeaderCell("COUNTER", widthCounter, null)
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
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = Slate400,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "Data tidak ditemukan untuk pencarian \"$searchQuery\"" else "Belum ada data Riwayat Service Teknik (SKD).\nTekan Sinkron untuk memuat dari Google Sheets.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Slate600,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .width(totalTableWidth)
                                .fillMaxHeight()
                        ) {
                            itemsIndexed(filteredList) { index, item ->
                                val rowBgColor = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)

                                Row(
                                    modifier = Modifier
                                        .width(totalTableWidth)
                                        .background(rowBgColor)
                                        .border(BorderStroke(0.5.dp, cellBorderColor))
                                        .clickable { detailItem = item },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Created On
                                    Box(
                                        modifier = Modifier
                                            .width(widthCreatedOn)
                                            .padding(horizontal = 8.dp, vertical = 9.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = item.createdOn.ifBlank { item.tanggal.ifBlank { "-" } },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                color = Slate800,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 2. Order (Clickable to view Reservasi)
                                    Box(
                                        modifier = Modifier
                                            .width(widthOrder)
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Surface(
                                            color = Blue50,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Blue700.copy(alpha = 0.25f)),
                                            modifier = Modifier.clickable { detailItem = item }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ReceiptLong,
                                                    contentDescription = "Lihat Reservasi Order",
                                                    tint = HighDensityBlue,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = item.order.ifBlank { item.orderNo.ifBlank { "-" } },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 11.5.sp,
                                                        color = Blue900,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 3. Object Description (No Mobil - Clickable -> buka rincian order)
                                    Box(
                                        modifier = Modifier
                                            .width(widthObjectDesc)
                                            .clickable { detailItem = item }
                                            .padding(horizontal = 8.dp, vertical = 9.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = item.objectDescription.ifBlank { item.noMobil.ifBlank { "-" } },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = HighDensityBlue,
                                                    fontSize = 12.5.sp
                                                ),
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.ReceiptLong,
                                                contentDescription = "Detail Order",
                                                tint = HighDensityBlue.copy(alpha = 0.7f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 4. Description
                                    Box(
                                        modifier = Modifier
                                            .width(widthDescription)
                                            .padding(horizontal = 8.dp, vertical = 9.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = item.description.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.5.sp,
                                                color = Slate800,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 5. KETERANGAN
                                    Box(
                                        modifier = Modifier
                                            .width(widthKeterangan)
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            color = Blue50,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.5.dp, Color(0xFFBFDBFE))
                                        ) {
                                            Text(
                                                text = item.displayKeterangan.ifBlank { "-" },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Blue900,
                                                    fontSize = 11.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 6. COUNTER
                                    Box(
                                        modifier = Modifier
                                            .width(widthCounter)
                                            .padding(horizontal = 8.dp, vertical = 9.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            text = item.displayCounter.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F766E)
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

    // Modal Detail View
    if (detailItem != null) {
        val item = detailItem!!
        AlertDialog(
            onDismissRequest = {
                detailItem = null
                onClearInitialDetail()
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Blue50,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Engineering, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Riwayat Service Teknik",
                                fontWeight = FontWeight.Bold,
                                color = Blue900,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Order #${item.orderNo}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 12.sp)
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailCardSection(title = "INFORMASI UTAMA") {
                        DetailItemRow("Created On", item.createdOn.ifBlank { item.tanggal.ifBlank { "-" } })
                        DetailItemRow("Order", item.order.ifBlank { item.orderNo.ifBlank { "-" } })
                        DetailItemRow("Object Description", item.objectDescription.ifBlank { item.noMobil.ifBlank { "-" } }, isHighlight = true)
                        DetailItemRow("KETERANGAN", item.displayKeterangan.ifBlank { "-" })
                        DetailItemRow("COUNTER", item.displayCounter.ifBlank { "-" })
                        if (item.tipe.isNotBlank()) {
                            DetailItemRow("TIPE", item.tipe)
                        }
                    }

                    DetailCardSection(title = "DESKRIPSI PEKERJAAN") {
                        Text(
                            text = item.description.ifBlank { "Tidak ada deskripsi" },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Slate800,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.padding(4.dp)
                        )
                    }

                    // DATA RESERVASI SESUAI DENGAN NO ORDER
                    val matchingReservasi = remember(item.orderNo, reservasiList) {
                        val cleanOrder = item.orderNo.trim()
                        if (cleanOrder.isBlank()) emptyList()
                        else {
                            val numOrder = cleanOrder.toLongOrNull()
                            reservasiList.filter { res ->
                                val resClean = res.orderNo.trim()
                                val resNum = resClean.toLongOrNull()
                                resClean.equals(cleanOrder, ignoreCase = true) ||
                                        (numOrder != null && resNum != null && numOrder == resNum) ||
                                        (cleanOrder.length >= 4 && resClean.contains(cleanOrder, ignoreCase = true)) ||
                                        (resClean.length >= 4 && cleanOrder.contains(resClean, ignoreCase = true))
                            }
                        }
                    }

                    DetailCardSection(title = "DATA RESERVASI (ORDER #${item.orderNo.ifBlank { "-" }})") {
                        if (item.orderNo.isBlank()) {
                            Text(
                                text = "Nomor Order tidak tersedia pada pengerjaan ini.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 12.sp),
                                modifier = Modifier.padding(4.dp)
                            )
                        } else if (matchingReservasi.isEmpty()) {
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
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tidak ada data reservasi suku cadang untuk Order #${item.orderNo}.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.5.sp)
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Ditemukan ${matchingReservasi.size} item reservasi suku cadang:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = HighDensityBlue,
                                        fontSize = 11.sp
                                    )
                                )
                                matchingReservasi.forEachIndexed { idx, res ->
                                    Surface(
                                        color = Color.White,
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
                                                    text = "${idx + 1}. ${res.material.ifBlank { "Material" }}",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Slate800,
                                                        fontSize = 12.sp
                                                    )
                                                )
                                                if (res.quantity.isNotBlank()) {
                                                    Surface(
                                                        color = Blue50,
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = BorderStroke(0.5.dp, HighDensityBlue.copy(alpha = 0.3f))
                                                    ) {
                                                        Text(
                                                            text = "${res.quantity} ${res.baseUnitOfMeasure}".trim(),
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = HighDensityBlue,
                                                                fontSize = 11.sp
                                                            ),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            if (res.materialDescription.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = res.materialDescription,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Slate600,
                                                        fontSize = 11.5.sp
                                                    )
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                if (res.postingDate.isNotBlank() || res.documentDate.isNotBlank()) {
                                                    Text(
                                                        text = "Tgl: ${res.postingDate.ifBlank { res.documentDate }}",
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
                                                            fontWeight = FontWeight.SemiBold,
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
                            }
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
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                ) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun HeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    borderColor: Color?
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    if (borderColor != null) {
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight(0.6f)
                .background(borderColor)
        )
    }
}

@Composable
private fun ColumnDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(34.dp)
            .background(color)
    )
}

@Composable
private fun DetailCardSection(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate600,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun DetailItemRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 12.sp)
        )
        if (onClick != null) {
            Surface(
                color = Blue50,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.clickable { onClick() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = value.ifBlank { "-" },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = HighDensityBlue,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = HighDensityBlue,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        } else {
            Text(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
                    color = if (isHighlight) HighDensityBlue else Slate800,
                    fontSize = 12.sp
                )
            )
        }
    }
}
