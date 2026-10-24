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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
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
import com.example.ui.theme.HighDensityBlueBorder
import com.example.ui.theme.HighDensityBlueLight
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MENU RESERVASI
 * Database ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY
 * Nama Sheet: GOODMOVEN
 *
 * Tampilan Kolom Tabel:
 * NO_MOBIL | Order | Material | Material_Description | Doc_Date | Pstng_Date | Quantity | BUn | LC_Amount | Batch
 */
@Composable
fun ReservasiScreen(
    reservasiList: List<ReservasiEntity>,
    kendaraanList: List<MasterKendaraanEntity> = emptyList(),
    pengerjaanTeknikList: List<PengerjaanTeknikEntity> = emptyList(),
    onSaveReservasi: (ReservasiEntity) -> Unit = {},
    onDeleteReservasi: (Long) -> Unit = {},
    onClearOldData: () -> Unit = {},
    onClearDataOnly: () -> Unit = {},
    onVehicleClick: (String) -> Unit,
    onOrderClick: (String) -> Unit = {},
    onSyncClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state for full item detail view
    var detailItem by remember { mutableStateOf<ReservasiEntity?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Diurutkan dari A-Z berdasarkan data Material Description
    val sortedList = remember(reservasiList) {
        reservasiList.sortedWith(
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.materialDescription.trim().ifBlank { it.material.trim() } }
        )
    }

    // Filter search
    val filteredList = remember(sortedList, searchQuery) {
        if (searchQuery.isBlank()) {
            sortedList
        } else {
            val q = searchQuery.trim().lowercase()
            sortedList.filter {
                it.noMobil.lowercase().contains(q) ||
                it.orderNo.lowercase().contains(q) ||
                it.material.lowercase().contains(q) ||
                it.materialDescription.lowercase().contains(q) ||
                it.batch.lowercase().contains(q) ||
                it.documentDate.lowercase().contains(q) ||
                it.postingDate.lowercase().contains(q) ||
                it.amtInLocCur.lowercase().contains(q) ||
                it.id.toString() == q
            }
        }
    }

    val cellBorderColor = Color(0xFFE2E8F0)
    val headerBorderColor = Color(0xFF2563EB)

    // Column widths in dp for optimal readability
    val widthNoMobil = 110.dp
    val widthOrder = 110.dp
    val widthMaterial = 120.dp
    val widthMatDesc = 240.dp
    val widthDocDate = 120.dp
    val widthPostDate = 120.dp
    val widthQty = 90.dp
    val widthUnit = 100.dp
    val widthAmt = 140.dp
    val widthBatch = 110.dp
    val totalTableWidth = widthNoMobil + widthOrder + widthMaterial + widthMatDesc +
            widthDocDate + widthPostDate + widthQty + widthUnit + widthAmt + widthBatch

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header & Action Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Blue50, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = HighDensityBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "RESERVASI",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Blue900,
                                        fontSize = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Blue50,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${reservasiList.size} Data (A-Z)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Blue700
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showClearDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Rose600
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFECDD3))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Rose600, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus Data & Komponen", style = MaterialTheme.typography.labelMedium.copy(color = Rose600, fontWeight = FontWeight.SemiBold))
                        }

                        Button(
                            onClick = onSyncClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sinkron", style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search field
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
                                        text = "Cari No Mobil, Order, Material, Description, Batch...",
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
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Slate400, modifier = Modifier.size(46.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tidak ada data reservasi yang sesuai dengan pencarian." else "Data reservasi belum dimuat.",
                            style = MaterialTheme.typography.titleSmall.copy(color = Slate800, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Blue50,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Konfigurasi Database GOODMOVEN:",
                                    style = MaterialTheme.typography.labelMedium.copy(color = Blue900, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ID: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY | Sheet: GOODMOVEN",
                                    style = MaterialTheme.typography.labelSmall.copy(color = HighDensityBlue, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Susunan Kolom:\nNO_MOBIL | Order | Material | Material_Description | Doc_Date | Pstng_Date | Quantity | BUn | LC_Amount | Batch",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate600, textAlign = TextAlign.Center, lineHeight = 16.sp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onSyncClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sinkron Sekarang")
                        }
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
                        // NO_MOBIL | Order | Material | Material_Description | Doc_Date | Pstng_Date | Quantity | BUn | LC_Amount | Batch
                        // ==========================================
                        Row(
                            modifier = Modifier
                                .width(totalTableWidth)
                                .background(HighDensityBlue),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. NO_MOBIL
                            HeaderCell("NO_MOBIL", widthNoMobil, headerBorderColor)
                            // 2. Order
                            HeaderCell("Order", widthOrder, headerBorderColor)
                            // 3. Material
                            HeaderCell("Material", widthMaterial, headerBorderColor)
                            // 4. Material_Description
                            HeaderCell("Material_Description", widthMatDesc, headerBorderColor)
                            // 5. Doc_Date
                            HeaderCell("Doc_Date", widthDocDate, headerBorderColor)
                            // 6. Pstng_Date
                            HeaderCell("Pstng_Date", widthPostDate, headerBorderColor)
                            // 7. Quantity
                            HeaderCell("Quantity", widthQty, headerBorderColor)
                            // 8. BUn
                            HeaderCell("BUn", widthUnit, headerBorderColor)
                            // 9. LC_Amount
                            HeaderCell("LC_Amount", widthAmt, headerBorderColor)
                            // 10. Batch
                            HeaderCell("Batch", widthBatch, null)
                        }

                        // ROWS
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
                                        .border(BorderStroke(0.5.dp, cellBorderColor))
                                        .clickable { detailItem = item },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. NO MOBIL (Click to view vehicle detail)
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
                                    ColumnDivider(cellBorderColor)

                                    // 2. Order (Click to open Pengerjaan Teknik / Keindahan)
                                    Box(
                                        modifier = Modifier
                                            .width(widthOrder)
                                            .padding(vertical = 4.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val cleanOrder = item.orderNo.trim()
                                        if (cleanOrder.isNotBlank() && cleanOrder != "-") {
                                            val hasTeknik = pengerjaanTeknikList.any { isOrderMatch(it.orderNo, cleanOrder) }

                                            val (badgeBg, badgeText, badgeBorder) = when {
                                                hasTeknik -> Triple(HighDensityBlueLight, HighDensityBlue, HighDensityBlueBorder)
                                                else -> Triple(Color(0xFFF8FAFC), Slate800, Color(0xFFCBD5E1))
                                            }

                                            Surface(
                                                color = badgeBg,
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(1.dp, badgeBorder),
                                                modifier = Modifier.clickable { onOrderClick(cleanOrder) }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = cleanOrder,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = badgeText,
                                                            fontSize = 11.sp
                                                        ),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.OpenInNew,
                                                        contentDescription = "Buka Pengerjaan",
                                                        tint = badgeText,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "-",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Slate400,
                                                    fontSize = 11.5.sp
                                                )
                                            )
                                        }
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 3. Material
                                    Box(
                                        modifier = Modifier
                                            .width(widthMaterial)
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            color = Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Text(
                                                text = item.material.ifBlank { "-" },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF334155),
                                                    fontSize = 11.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 4. Material description (Click to view full detail)
                                    Box(
                                        modifier = Modifier
                                            .width(widthMatDesc)
                                            .clickable { detailItem = item }
                                            .padding(vertical = 8.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = item.materialDescription.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate800,
                                                fontSize = 11.5.sp,
                                                lineHeight = 15.sp
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 5. Document Date
                                    Box(
                                        modifier = Modifier
                                            .width(widthDocDate)
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.documentDate.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate600,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 6. Posting Date
                                    Box(
                                        modifier = Modifier
                                            .width(widthPostDate)
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.postingDate.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate600,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 7. Quantity
                                    Box(
                                        modifier = Modifier
                                            .width(widthQty)
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.quantity.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Blue900,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 8. Base Unit of Measure
                                    Box(
                                        modifier = Modifier
                                            .width(widthUnit)
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Surface(
                                            color = Color(0xFFF0FDF4),
                                            shape = RoundedCornerShape(4.dp),
                                            border = BorderStroke(0.5.dp, Color(0xFFBBF7D0))
                                        ) {
                                            Text(
                                                text = item.baseUnitOfMeasure.ifBlank { "-" },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF166534),
                                                    fontSize = 11.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 9. Amt.in loc.cur.
                                    Box(
                                        modifier = Modifier
                                            .width(widthAmt)
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.amtInLocCur.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate800,
                                                fontSize = 11.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    ColumnDivider(cellBorderColor)

                                    // 10. Batch
                                    Box(
                                        modifier = Modifier
                                            .width(widthBatch)
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.batch.ifBlank { "-" },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate600,
                                                fontSize = 11.sp
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
            onDismissRequest = { detailItem = null },
            icon = {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = HighDensityBlue)
            },
            title = {
                Text(
                    text = "Detail Reservasi Mobil #${item.noMobil}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Blue50,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NO_MOBIL:", style = MaterialTheme.typography.labelMedium.copy(color = Slate600))
                            Text(item.noMobil, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Blue900))
                        }
                    }

                    ReservasiDetailRow("Order", item.orderNo)
                    ReservasiDetailRow("Material", item.material)
                    ReservasiDetailRow("Material_Description", item.materialDescription)
                    ReservasiDetailRow("Doc_Date", item.documentDate)
                    ReservasiDetailRow("Pstng_Date", item.postingDate)
                    ReservasiDetailRow("Quantity", item.quantity)
                    ReservasiDetailRow("BUn", item.baseUnitOfMeasure)
                    ReservasiDetailRow("LC_Amount", item.amtInLocCur)
                    ReservasiDetailRow("Batch", item.batch)
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.orderNo.isNotBlank() && item.orderNo != "-") {
                        Button(
                            onClick = {
                                val oNo = item.orderNo.trim()
                                detailItem = null
                                onOrderClick(oNo)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buka Pengerjaan", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val noMobil = item.noMobil
                            detailItem = null
                            if (noMobil.isNotBlank()) {
                                onVehicleClick(noMobil)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Master Mobil", fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { detailItem = null }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Modal Konfirmasi Hapus Data Reservasi & Komponen Pendukung
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Rose600, modifier = Modifier.size(28.dp))
            },
            title = {
                Text(
                    text = "Hapus Data Reservasi Beserta Komponen Pendukung?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Data lokal reservasi beserta seluruh komponen pendukung armada (Ban, Kopling, Bateray) di memori HP akan dihapus dan dibersihkan.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate800)
                    )
                    Surface(
                        color = Blue50,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Database Reservasi: 145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY", style = MaterialTheme.typography.labelSmall.copy(color = Blue900, fontWeight = FontWeight.Bold))
                            Text("Sheet: GOODMOVEN", style = MaterialTheme.typography.labelSmall.copy(color = HighDensityBlue, fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Komponen Pendukung yang dibersihkan:\n• BAN (Data Ban Armada)\n• KOPLING (Data Kopling Armada)\n• BATERAY (Data Bateray / Aki Armada)\n• RESERVASI (Data Reservasi Bengkel)", style = MaterialTheme.typography.labelSmall.copy(color = Slate600, lineHeight = 16.sp))
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            showClearDialog = false
                            onClearDataOnly()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose600),
                        border = BorderStroke(1.dp, Rose600),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Kosongkan Saja", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            showClearDialog = false
                            onClearOldData()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Rose600),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hapus & Sinkron", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp, dividerColor: Color?) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 11.5.sp
            ),
            maxLines = 1,
            softWrap = false
        )
    }
    if (dividerColor != null) {
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(20.dp)
                .background(dividerColor)
        )
    }
}

@Composable
private fun ColumnDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(color)
    )
}

@Composable
private fun ReservasiDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Slate600),
            modifier = Modifier.width(130.dp)
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Slate800),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
