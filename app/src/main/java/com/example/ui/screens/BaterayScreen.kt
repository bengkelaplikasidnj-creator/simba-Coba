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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
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
import com.example.data.local.entity.BaterayEntity
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.ui.theme.Amber600
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800

/**
 * MENU BATERAY / ACCU
 * Database ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY
 * Nama Sheet: ACCU
 *
 * Tampilan Kolom Tabel:
 * | NO_MOBIL | NO_POLISI | TYPE | RESERVASI | USIA | Tipe Accu |
 * Urutkan berdasarkan ID
 * Jika nilai pada USIA lebih dari 12 Font dibuat Merah
 */
@Composable
fun BaterayScreen(
    baterayList: List<BaterayEntity>,
    kendaraanList: List<MasterKendaraanEntity> = emptyList(),
    onVehicleClick: (String) -> Unit,
    onSyncClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Data diurutkan sesuai dengan ID secara ASC
    val sortedList = remember(baterayList) {
        baterayList.sortedBy { it.id }
    }

    // Filter pencarian
    val filteredList = remember(sortedList, searchQuery) {
        if (searchQuery.isBlank()) {
            sortedList
        } else {
            val q = searchQuery.trim().lowercase()
            sortedList.filter {
                it.noMobil.lowercase().contains(q) ||
                it.displayPlatNomer.lowercase().contains(q) ||
                it.displayTipe.lowercase().contains(q) ||
                it.displayReservasi.lowercase().contains(q) ||
                it.displayUsia.lowercase().contains(q) ||
                it.displayTipeAccu.lowercase().contains(q) ||
                it.id.toString() == q
            }
        }
    }

    val cellBorderColor = Color(0xFFE2E8F0)
    val headerBorderColor = Color(0xFF2563EB)

    // Lebar kolom tabel:
    // 1. NO_MOBIL: 95.dp
    // 2. NO_POLISI: 105.dp
    // 3. TYPE: 125.dp
    // 4. RESERVASI: 105.dp
    // 5. USIA: 85.dp
    // 6. Tipe Accu: 110.dp
    val widthNoMobil = 95.dp
    val widthNoPolisi = 105.dp
    val widthType = 125.dp
    val widthReservasi = 105.dp
    val widthUsia = 85.dp
    val widthTipeAccu = 110.dp
    val totalTableWidth = widthNoMobil + widthNoPolisi + widthType + widthReservasi + widthUsia + widthTipeAccu // 625.dp

    // Fungsi bantu untuk mengecek apakah nilai Usia lebih dari 12
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
                                Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = HighDensityBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BATERAY / ACCU",
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
                                    text = "${baterayList.size} Data",
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

                // Kotak Pencarian Ramping
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
                                        text = "Cari NO_MOBIL, NO_POLISI, TYPE, RESERVASI, USIA, atau Tipe Accu...",
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

        // Table Container Card (Rapat dan Fix)
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
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tidak ada data baterai yang cocok dengan pencarian." else "Tidak ada data baterai.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate600, textAlign = TextAlign.Center)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tekan tombol 'Sinkron' untuk mengambil data dari Google Sheets (ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY, Sheet: ACCU)",
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
                        // HEADER TABEL SESUAI PERMINTAAN USER:
                        // | NO_MOBIL | NO_POLISI | TYPE | RESERVASI | USIA | Tipe Accu |
                        // ==========================================
                        Row(
                            modifier = Modifier
                                .width(totalTableWidth)
                                .background(HighDensityBlue),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Kolom: NO_MOBIL
                            Box(
                                modifier = Modifier
                                    .width(widthNoMobil)
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "NO_MOBIL",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                            // 2. Kolom: NO_POLISI
                            Box(
                                modifier = Modifier
                                    .width(widthNoPolisi)
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "NO_POLISI",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                            // 3. Kolom: TYPE
                            Box(
                                modifier = Modifier
                                    .width(widthType)
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "TYPE",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                            // 4. Kolom: RESERVASI
                            Box(
                                modifier = Modifier
                                    .width(widthReservasi)
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "RESERVASI",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                            // 5. Kolom: USIA
                            Box(
                                modifier = Modifier
                                    .width(widthUsia)
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "USIA",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                            // 6. Kolom: Tipe Accu
                            Box(
                                modifier = Modifier
                                    .width(widthTipeAccu)
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tipe Accu",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // BARIS DATA TABEL (Urut sesuai ID ASC)
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
                                    // 1. NO_MOBIL (Klik untuk detail armada)
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
                                                    text = item.displayNoMobil.ifBlank { "-" },
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

                                    // 2. NO_POLISI
                                    Box(
                                        modifier = Modifier
                                            .width(widthNoPolisi)
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.displayPlatNomer.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Slate800,
                                                fontSize = 11.5.sp,
                                                textAlign = TextAlign.Center
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                    // 3. TYPE (Tipe Kendaraan)
                                    Box(
                                        modifier = Modifier
                                            .width(widthType)
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            color = Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Text(
                                                text = item.displayTipe.ifBlank { "-" },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate800,
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

                                    // 4. RESERVASI (Tanggal Reservasi)
                                    Box(
                                        modifier = Modifier
                                            .width(widthReservasi)
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.displayReservasi.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate800,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                    // 5. USIA (Jika nilai pada USIA lebih dari 12 Font dibuat Merah)
                                    val usiaValue = parseUsiaValue(item.displayUsia)
                                    val isOver12 = usiaValue > 12.0
                                    val usiaColor = if (isOver12) Rose600 else Slate800

                                    Box(
                                        modifier = Modifier
                                            .width(widthUsia)
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.displayUsia.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = usiaColor,
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                    // 6. Tipe Accu
                                    Box(
                                        modifier = Modifier
                                            .width(widthTipeAccu)
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            color = Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                                        ) {
                                            Text(
                                                text = item.displayTipeAccu.ifBlank { "-" },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Amber600,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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
