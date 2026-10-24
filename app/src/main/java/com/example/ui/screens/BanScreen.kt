package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BanEntity
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800

/**
 * MENU BAN
 * Database ID: 17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo
 * Nama Sheet: DATA_BAN
 *
 * Tampilan Kolom Tabel:
 * | No_Mobil | No_Polisi | EMBOS | KM_BAN |
 * Data diurutkan berdasarkan KM_BAN tertinggi ke terendah (DESC)
 */

enum class BanSortColumn {
    NO_MOBIL, NO_POLISI, EMBOS, KM_BAN
}

@Composable
fun BanScreen(
    banList: List<BanEntity>,
    kendaraanList: List<MasterKendaraanEntity> = emptyList(),
    onVehicleClick: (String) -> Unit,
    onSyncClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var sortColumn by remember { mutableStateOf(BanSortColumn.KM_BAN) }
    var sortAscending by remember { mutableStateOf(false) }

    // Data diurutkan berdasarkan kolom terpilih (default KM_BAN DESC)
    val sortedList = remember(banList, sortColumn, sortAscending, kendaraanList) {
        val comparator = when (sortColumn) {
            BanSortColumn.NO_MOBIL -> compareBy<BanEntity> { it.noMobil.lowercase() }
            BanSortColumn.NO_POLISI -> compareBy<BanEntity> {
                val nopol = it.displayNoPolisi.ifBlank {
                    kendaraanList.find { k -> k.noMobil.equals(it.noMobil, ignoreCase = true) }?.noPolisi ?: ""
                }
                nopol.lowercase()
            }
            BanSortColumn.EMBOS -> compareBy<BanEntity> { it.displayEmbos.lowercase() }
            BanSortColumn.KM_BAN -> compareBy<BanEntity> { kotlin.math.abs(parseKmValue(it.displayKmBan)) }
        }

        val listSorted = if (sortAscending) {
            banList.sortedWith(comparator.thenBy { it.id })
        } else {
            banList.sortedWith(comparator.reversed().thenBy { it.id })
        }
        listSorted
    }

    // Filter pencarian
    val filteredList = remember(sortedList, searchQuery, kendaraanList) {
        if (searchQuery.isBlank()) {
            sortedList
        } else {
            val q = searchQuery.trim().lowercase()
            sortedList.filter {
                val nopol = it.displayNoPolisi.ifBlank {
                    kendaraanList.find { k -> k.noMobil.equals(it.noMobil, ignoreCase = true) }?.noPolisi ?: ""
                }
                it.noMobil.lowercase().contains(q) ||
                nopol.lowercase().contains(q) ||
                it.displayEmbos.lowercase().contains(q) ||
                it.displayKmBan.lowercase().contains(q) ||
                it.id.toString() == q
            }
        }
    }

    val cellBorderColor = Color(0xFFE2E8F0)
    val headerBorderColor = Color(0xFF2563EB)

    // Lebar kolom rapi & proporsional:
    // No_Mobil: 105dp
    // No_Polisi: 110dp
    // EMBOS: 115dp
    // KM_BAN: 115dp
    // Total Lebar: 445dp
    val widthNoMobil = 105.dp
    val widthNoPolisi = 110.dp
    val widthEmbos = 115.dp
    val widthKmBan = 115.dp
    val totalTableWidth = widthNoMobil + widthNoPolisi + widthEmbos + widthKmBan // 445.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Blue50, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TripOrigin,
                                contentDescription = null,
                                tint = HighDensityBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BAN",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Blue900
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Blue50,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${banList.size} Data",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Blue700
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onSyncClick,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, tint = Blue700, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sinkron", style = MaterialTheme.typography.labelMedium.copy(color = Blue700, fontWeight = FontWeight.SemiBold))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Kotak Pencarian Ramping mengikuti ukuran font
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
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = HighDensityBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Cari No_Mobil, No_Polisi, EMBOS, atau KM_BAN...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = Slate400
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Table Container Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TripOrigin, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tidak ada data ban yang cocok dengan pencarian." else "Tidak ada data ban.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate600, textAlign = TextAlign.Center)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tekan tombol 'Sinkron' untuk mengambil data dari Google Sheets (ID: 17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo, Sheet: DATA_BAN)",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate400, textAlign = TextAlign.Center)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(
                        modifier = Modifier
                            .width(totalTableWidth)
                            .fillMaxHeight()
                            .border(BorderStroke(1.dp, cellBorderColor))
                    ) {
                        // ==========================================
                        // HEADER TABEL SESUAI PERMINTAAN:
                        // | No_Mobil | No_Polisi | EMBOS | KM_BAN |
                        // ==========================================
                        Row(
                            modifier = Modifier
                                .width(totalTableWidth)
                                .background(HighDensityBlue),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Kolom: No_Mobil
                            Box(
                                modifier = Modifier
                                    .width(widthNoMobil)
                                    .clickable {
                                        if (sortColumn == BanSortColumn.NO_MOBIL) {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = BanSortColumn.NO_MOBIL
                                            sortAscending = true
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "No_Mobil",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    if (sortColumn == BanSortColumn.NO_MOBIL) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (sortAscending) "▲" else "▼",
                                            color = Color(0xFF93C5FD),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                            // 2. Kolom: No_Polisi
                            Box(
                                modifier = Modifier
                                    .width(widthNoPolisi)
                                    .clickable {
                                        if (sortColumn == BanSortColumn.NO_POLISI) {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = BanSortColumn.NO_POLISI
                                            sortAscending = true
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "No_Polisi",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    if (sortColumn == BanSortColumn.NO_POLISI) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (sortAscending) "▲" else "▼",
                                            color = Color(0xFF93C5FD),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                            // 3. Kolom: EMBOS
                            Box(
                                modifier = Modifier
                                    .width(widthEmbos)
                                    .clickable {
                                        if (sortColumn == BanSortColumn.EMBOS) {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = BanSortColumn.EMBOS
                                            sortAscending = true
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "EMBOS",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    if (sortColumn == BanSortColumn.EMBOS) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (sortAscending) "▲" else "▼",
                                            color = Color(0xFF93C5FD),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                            // 4. Kolom: KM_BAN (Default DESC)
                            Box(
                                modifier = Modifier
                                    .width(widthKmBan)
                                    .clickable {
                                        if (sortColumn == BanSortColumn.KM_BAN) {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = BanSortColumn.KM_BAN
                                            sortAscending = false
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "KM_BAN",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (sortColumn == BanSortColumn.KM_BAN) (if (sortAscending) "▲" else "▼") else "▼",
                                        color = if (sortColumn == BanSortColumn.KM_BAN) Color(0xFF93C5FD) else Color.White.copy(alpha = 0.5f),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // BARIS DATA TABEL (Urut sesuai sort, default KM_BAN tertinggi ke terendah)
                        LazyColumn(
                            modifier = Modifier
                                .width(totalTableWidth)
                                .weight(1f)
                        ) {
                            itemsIndexed(filteredList, key = { _, item -> item.id }) { index, item ->
                                val rowBgColor = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)

                                Row(
                                    modifier = Modifier
                                        .width(totalTableWidth)
                                        .background(rowBgColor)
                                        .border(BorderStroke(0.5.dp, cellBorderColor)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Kolom: No_Mobil (Klik untuk detail kendaraan)
                                    Box(
                                        modifier = Modifier
                                            .width(widthNoMobil)
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            color = Blue50,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.clickable { onVehicleClick(item.noMobil) }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.DirectionsCar,
                                                    contentDescription = null,
                                                    tint = HighDensityBlue,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = item.noMobil.ifBlank { "-" },
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = HighDensityBlue,
                                                        fontSize = 12.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                    // 2. Kolom: No_Polisi
                                    val nopol = item.displayNoPolisi.ifBlank {
                                        kendaraanList.find { k -> k.noMobil.equals(item.noMobil, ignoreCase = true) }?.noPolisi ?: "-"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(widthNoPolisi)
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            color = Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Text(
                                                text = nopol.ifBlank { "-" },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate800,
                                                    fontSize = 11.5.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                    // 3. Kolom: EMBOS
                                    Box(
                                        modifier = Modifier
                                            .width(widthEmbos)
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            color = Color(0xFFF0FDF4),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                                        ) {
                                            Text(
                                                text = item.displayEmbos.ifBlank { "-" },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF166534),
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                    // 4. Kolom: KM_BAN (> 90.000 berwarna merah)
                                    val kmAbs = kotlin.math.abs(parseKmValue(item.displayKmBan))
                                    val isOver90k = kmAbs > 90000.0
                                    val kmTextColor = if (isOver90k) Rose600 else Slate800
                                    val kmIconColor = if (isOver90k) Rose600 else Slate600
                                    val kmDisplayText = if (item.displayKmBan.isBlank()) {
                                        "-"
                                    } else if (item.displayKmBan.endsWith("KM", ignoreCase = true)) {
                                        item.displayKmBan
                                    } else {
                                        "${item.displayKmBan} KM"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(widthKmBan)
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Speed,
                                                contentDescription = null,
                                                tint = kmIconColor,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = kmDisplayText,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = kmTextColor,
                                                    fontSize = 11.5.sp,
                                                    textAlign = TextAlign.Center
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
    }
}
