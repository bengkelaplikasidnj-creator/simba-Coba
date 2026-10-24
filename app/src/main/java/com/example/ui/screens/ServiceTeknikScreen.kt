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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ArgoEntity
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.data.local.entity.PrediksiPartEntity
import com.example.data.local.entity.ServiceTeknikEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper untuk memeriksa apakah status atau status mobil adalah "KALONG".
 * Jika status sama dengan "kalong" (case-insensitive), maka kolom No Mobil akan berubah menjadi kuning.
 */
fun isKalongStatus(item: ServiceTeknikEntity): Boolean {
    return item.statusMobil.trim().equals("KALONG", ignoreCase = true) ||
            item.status.trim().equals("KALONG", ignoreCase = true)
}

/**
 * MENU: SERVICE KENDARAAN
 *
 * Kolom Tabel yang Ditampilkan (Lebar Otomatis & Fleksibel):
 * 1. No Mobil | 2. Service | 3. Selisih KM | 4. Notif Desc | 5. Notif_NO | 6. STATUS | 7. AKSI
 */
@Composable
fun ServiceTeknikScreen(
    serviceList: List<ServiceTeknikEntity>,
    kendaraanList: List<MasterKendaraanEntity>,
    argoList: List<ArgoEntity> = emptyList(),
    prediksiPartList: List<PrediksiPartEntity> = emptyList(),
    currentUser: UserEntity? = null,
    onSaveService: (ServiceTeknikEntity) -> Unit,
    onDeleteService: (Long) -> Unit,
    onSavePrediksiPart: (PrediksiPartEntity) -> Unit = {},
    onNavigateToPrediksiPart: (() -> Unit)? = null,
    onVehicleClick: (String) -> Unit,
    onSyncClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ServiceTeknikEntity?>(null) }
    var viewingItem by remember { mutableStateOf<ServiceTeknikEntity?>(null) }
    var prediksiPartTargetItem by remember { mutableStateOf<ServiceTeknikEntity?>(null) }
    var selectedPrediksiDetailItem by remember { mutableStateOf<PrediksiPartEntity?>(null) }

    // Dialog form state untuk 14 data
    var formId by remember { mutableStateOf("") }
    var formStatusMobil by remember { mutableStateOf("Siap Operasi") }
    var formNoMobil by remember { mutableStateOf("") }
    var formService by remember { mutableStateOf("Service Berkala") }
    var formSelisihKm by remember { mutableStateOf("0") }
    var formNotificationDesc by remember { mutableStateOf("") }
    var formNotifNo by remember { mutableStateOf("") }
    var formStatus by remember { mutableStateOf("NOPR") }
    var formKmServisSebelumnya by remember { mutableStateOf("0") }
    var formKmJatuhTempo by remember { mutableStateOf("0") }
    var formEquipmentNo by remember { mutableStateOf("") }
    var formNotifDate by remember { mutableStateOf("") }
    var formKmTotal by remember { mutableStateOf("0") }
    var formStickerBandara by remember { mutableStateOf("Tidak Ada") }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    // Parser KM untuk styling warna badge
    fun parseKm(kmStr: String): Double {
        val clean = kmStr.trim()
            .replace(",", ".")
            .replace("[^0-9.-]".toRegex(), "")
        return clean.toDoubleOrNull() ?: 0.0
    }

    // Urutkan data secara rapi di latar belakang
    val processedList = remember(serviceList, searchQuery) {
        serviceList
            .sortedBy { it.id }
            .filter { item ->
                if (searchQuery.isBlank()) true else {
                    val q = searchQuery.trim().lowercase()
                    item.id.toString().contains(q) ||
                            item.noMobil.lowercase().contains(q) ||
                            item.service.lowercase().contains(q) ||
                            item.selisihKm.lowercase().contains(q) ||
                            item.notificationDesc.lowercase().contains(q) ||
                            item.notifNo.lowercase().contains(q) ||
                            item.status.lowercase().contains(q) ||
                            item.statusMobil.lowercase().contains(q) ||
                            item.equipmentNo.lowercase().contains(q) ||
                            item.kmJatuhTempo.lowercase().contains(q) ||
                            item.kmTotal.lowercase().contains(q) ||
                            item.notifDate.lowercase().contains(q) ||
                            item.stickerBandara.lowercase().contains(q)
                }
            }
    }

    // Proporsi bobot kolom otomatis (responsif & adaptif tanpa kolom Aksi)
    val colNoMobilWeight = 1.85f
    val colServiceWeight = 1.40f
    val colSelisihKmWeight = 1.30f
    val colNotifDescWeight = 2.95f
    val colNotifNoWeight = 1.30f
    val colStatusWeight = 1.20f

    val cellBorderColor = Color(0xFFE2E8F0)
    val headerBorderColor = Color(0x33FFFFFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Card (Judul SERVICE TEKNIK & Form Pencarian di Bawahnya)
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
                // 1. Judul SERVICE TEKNIK & Tombol Sinkron Hanya Logo di Pojok Kanan Atas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = HighDensityBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "JADWAL SERVICE TEKNIK",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Blue900,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    // Tombol sinkron hanya logo di pojok kanan atas
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

                // 2. Form Pencarian di Bawah Judul (Lebih Kecil & Tulisan Tidak Terpotong)
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
                                    tint = Slate400,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CARD TABEL SERVICE KENDARAAN (Lebar Otomatis & Menyesuaikan Layar Secara Dinamis)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                if (processedList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada data service kendaraan yang ditemukan.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
                        )
                    }
                } else {
                    // Lebar Tabel Otomatis: Mengisi penuh lebar layar, dan mengizinkan scroll halus bila layar sangat sempit
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val minTableWidth = 720.dp
                        val isScrollable = maxWidth < minTableWidth
                        val tableWidthModifier = if (isScrollable) {
                            Modifier
                                .width(minTableWidth)
                                .fillMaxHeight()
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (isScrollable) Modifier.horizontalScroll(rememberScrollState()) else Modifier)
                        ) {
                            Column(
                                modifier = tableWidthModifier.border(BorderStroke(1.dp, cellBorderColor))
                            ) {
                                // HEADER TABEL (Lebar Otomatis berdasarkan bobot kolom)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(HighDensityBlue),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Kolom 1: No Mobil
                                    Box(
                                        modifier = Modifier
                                            .weight(colNoMobilWeight)
                                            .padding(vertical = 12.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No Mobil",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            ),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(headerBorderColor))

                                    // Kolom 2: Service (Rata Tengah)
                                    Box(
                                        modifier = Modifier
                                            .weight(colServiceWeight)
                                            .padding(vertical = 12.dp, horizontal = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Service",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(headerBorderColor))

                                    // Kolom 3: Selisih KM
                                    Box(
                                        modifier = Modifier
                                            .weight(colSelisihKmWeight)
                                            .padding(vertical = 12.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Selisih KM",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(headerBorderColor))

                                    // Kolom 4: Notif Desc
                                    Box(
                                        modifier = Modifier
                                            .weight(colNotifDescWeight)
                                            .padding(vertical = 12.dp, horizontal = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = "Notif Desc",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(headerBorderColor))

                                    // Kolom 5: Notif_NO
                                    Box(
                                        modifier = Modifier
                                            .weight(colNotifNoWeight)
                                            .padding(vertical = 12.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Notif_NO",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(headerBorderColor))

                                    // Kolom 6: STATUS
                                    Box(
                                        modifier = Modifier
                                            .weight(colStatusWeight)
                                            .padding(vertical = 12.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "STATUS",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                }

                                HorizontalDivider(color = cellBorderColor, thickness = 1.dp)

                                // BARIS DATA TABEL (Lebar Otomatis mengisi penuh kontainer)
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    itemsIndexed(processedList, key = { _, item -> item.id }) { index, item ->
                                        val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                                        val kmStyle = getSelisihKmStyle(item.selisihKm)

                                        // Kolom 1: No Mobil styling (Jika status == "KALONG", berubah menjadi kuning)
                                        val isKalong = isKalongStatus(item)
                                        val noMobilBg = if (isKalong) Color(0xFFFEF08A) else Blue50
                                        val noMobilBorder = if (isKalong) Color(0xFFEAB308) else Color(0xFF93C5FD)
                                        val noMobilText = if (isKalong) Color(0xFF713F12) else HighDensityBlue
                                        val noMobilIcon = if (isKalong) Color(0xFF854D0E) else HighDensityBlue

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(rowBg)
                                                .clickable { viewingItem = item },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Kolom 1: No Mobil
                                            Box(
                                                modifier = Modifier
                                                    .weight(colNoMobilWeight)
                                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Surface(
                                                    color = noMobilBg,
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, noMobilBorder),
                                                    modifier = Modifier.clickable { onVehicleClick(item.noMobil) }
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isKalong) Icons.Default.NightlightRound else Icons.Default.DirectionsCar,
                                                            contentDescription = if (isKalong) "Shift Kalong" else null,
                                                            tint = noMobilIcon,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "#${item.noMobil} ↗",
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = noMobilText
                                                            ),
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                    }
                                                }
                                            }
                                            Box(modifier = Modifier.width(1.dp).height(44.dp).background(cellBorderColor))

                                            // Kolom 2: Service (Rata Tengah)
                                            Box(
                                                modifier = Modifier
                                                    .weight(colServiceWeight)
                                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = item.service.ifBlank { "-" },
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Blue900,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Box(modifier = Modifier.width(1.dp).height(44.dp).background(cellBorderColor))

                                            // Kolom 3: Selisih KM
                                            // - jika nilai di atas -500 dan di bawah 0 -> kuning angka cetak tebal
                                            // - jika nilai di atas 0 -> merah
                                            Box(
                                                modifier = Modifier
                                                    .weight(colSelisihKmWeight)
                                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Surface(
                                                    color = kmStyle.containerColor,
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, kmStyle.borderColor)
                                                ) {
                                                    Text(
                                                        text = if (item.selisihKm.endsWith("KM", ignoreCase = true)) item.selisihKm else "${item.selisihKm} KM",
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
                                            Box(modifier = Modifier.width(1.dp).height(44.dp).background(cellBorderColor))

                                            // Kolom 4: Notif Desc
                                            Box(
                                                modifier = Modifier
                                                    .weight(colNotifDescWeight)
                                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Text(
                                                    text = item.notificationDesc.ifBlank { "-" },
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate800),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Box(modifier = Modifier.width(1.dp).height(44.dp).background(cellBorderColor))

                                            // Kolom 5: Notif_NO
                                            Box(
                                                modifier = Modifier
                                                    .weight(colNotifNoWeight)
                                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Surface(
                                                    color = Slate100,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = item.notifNo.ifBlank { "-" },
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Slate800
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            Box(modifier = Modifier.width(1.dp).height(44.dp).background(cellBorderColor))

                                            // Kolom 6: STATUS
                                            Box(
                                                modifier = Modifier
                                                    .weight(colStatusWeight)
                                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val isNopr = item.status.equals("NOPR", ignoreCase = true)
                                                Surface(
                                                    color = if (isNopr) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = item.status.ifBlank { "NOPR" },
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isNopr) Color(0xFF15803D) else Color(0xFFB45309)
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = cellBorderColor, thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOG VIEW DETAIL 14 DATA
    if (viewingItem != null) {
        val item = viewingItem!!
        var showAllDetails by remember { mutableStateOf(false) }

        // Lookup data PREDIKSI PART berdasarkan No Mobil
        val matchedPrediksiPartList = remember(prediksiPartList, item.noMobil, argoList) {
            prediksiPartList.filter { isVehicleMatch(it.noMobil, item.noMobil) }
                .map { resolvePrediksiPartWithLiveArgo(it, argoList) }
        }

        // Data prediksi yang masih aktif (status bukan SELESAI / DONE) diurutkan dari ESTIMASI_KM tertinggi
        // Status SELESAI di-hide sesuai permintaan user
        val activeMatchedPrediksiList = remember(matchedPrediksiPartList) {
            matchedPrediksiPartList.filter { p ->
                val isSelesai = p.status.trim().equals("SELESAI", ignoreCase = true) ||
                    p.status.contains("SELESAI", ignoreCase = true) ||
                    p.status.contains("DONE", ignoreCase = true)
                !isSelesai
            }.sortedWith(
                compareByDescending<PrediksiPartEntity> { parseEstimasiKmValue(it.estimasiKm) }
                    .thenByDescending { it.id }
            )
        }

        // Target item prediksi aktif untuk dibuka saat tombol prediksi atas diklik
        val targetActivePrediksiItem = remember(activeMatchedPrediksiList) {
            activeMatchedPrediksiList.firstOrNull()
        }

        AlertDialog(
            onDismissRequest = { viewingItem = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = HighDensityBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Data Lengkap Service Kendaraan #${item.noMobil}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // SEKSI 1: DATA PREDIKSI PART JIKA TERDAPAT LOOKUP DARI NO MOBIL DENGAN STATUS AKTIF (STATUS SELESAI HIDDEN)
                    if (activeMatchedPrediksiList.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp)
                                .testTag("prediksi_part_lookup_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFF15803D),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.PrecisionManufacturing,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "PREDIKSI PART AKTIF (${activeMatchedPrediksiList.size})",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF166534),
                                                    fontSize = 12.5.sp
                                                )
                                            )
                                            Text(
                                                text = if (targetActivePrediksiItem != null) {
                                                    "Aktif: ${targetActivePrediksiItem.namaPart.ifBlank { "Part" }} • No #${item.noMobil}"
                                                } else {
                                                    "Lookup No Mobil #${item.noMobil}"
                                                },
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF15803D),
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    // Tombol PREDIKSI di atas: saat terdapat data dan diklik maka akan ke detail data mengacu dari data prediksi yang masih aktif tersebut (tidak tambah prediksi)
                                    Button(
                                        onClick = {
                                            if (targetActivePrediksiItem != null) {
                                                selectedPrediksiDetailItem = targetActivePrediksiItem
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.testTag("btn_detail_prediksi_part_top")
                                    ) {
                                        Icon(Icons.Default.Visibility, contentDescription = "Detail Data Prediksi Aktif", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "PREDIKSI (Aktif)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFBBF7D0), thickness = 0.8.dp)

                                activeMatchedPrediksiList.forEachIndexed { idx, pPart ->
                                    val estKmVal = parseEstimasiKmValue(pPart.estimasiKm)
                                    val isOverdue = estKmVal >= 0L && estKmVal != Long.MIN_VALUE
                                    val isWarning = estKmVal > -500L && estKmVal < 0L
                                    val estBg = when {
                                        isOverdue -> Color(0xFFFEE2E2)
                                        isWarning -> Color(0xFFFEF9C3)
                                        else -> Color(0xFFEFF6FF)
                                    }
                                    val estText = when {
                                        isOverdue -> Color(0xFFDC2626)
                                        isWarning -> Color(0xFFB45309)
                                        else -> Blue900
                                    }

                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPrediksiDetailItem = pPart }
                                            .testTag("lookup_prediksi_item_$idx")
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    val isItemActive = !pPart.status.trim().equals("SELESAI", ignoreCase = true) &&
                                                        !pPart.status.contains("SELESAI", ignoreCase = true) &&
                                                        !pPart.status.contains("DONE", ignoreCase = true)
                                                    if (isItemActive) {
                                                        Surface(
                                                            color = Color(0xFF15803D),
                                                            shape = RoundedCornerShape(4.dp),
                                                            modifier = Modifier.padding(end = 6.dp)
                                                        ) {
                                                            Text(
                                                                text = "AKTIF",
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    color = Color.White,
                                                                    fontSize = 9.sp
                                                                ),
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = pPart.namaPart.ifBlank { "Part" },
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Slate800,
                                                            fontSize = 12.5.sp
                                                        )
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Surface(
                                                    color = estBg,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "EST: ${pPart.estimasiKm.ifBlank { "-" }} KM",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = estText,
                                                            fontSize = 10.5.sp
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Target: ${if (pPart.kmTarget.isNotBlank()) "${pPart.kmTarget} km" else "-"}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = HighDensityBlue,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.5.sp
                                                    )
                                                )
                                                Text(
                                                    text = "Argo: ${if (pPart.argoSekarang.isNotBlank()) "${pPart.argoSekarang} km" else "-"}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Slate800,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 10.5.sp
                                                    )
                                                )
                                                Text(
                                                    text = "Prediksi: ${if (pPart.prediksiKm.isNotBlank()) "${pPart.prediksiKm} km" else "-"}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Slate600,
                                                        fontSize = 10.5.sp
                                                    )
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    val apVal = pPart.aprovel.ifBlank { "Menunggu" }
                                                    Surface(
                                                        color = when {
                                                            apVal.contains("SETUJU", ignoreCase = true) -> Color(0xFFDCFCE7)
                                                            apVal.contains("TOLAK", ignoreCase = true) -> Color(0xFFFEE2E2)
                                                            else -> Color(0xFFFEF3C7)
                                                        },
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = apVal,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = when {
                                                                    apVal.contains("SETUJU", ignoreCase = true) -> Color(0xFF15803D)
                                                                    apVal.contains("TOLAK", ignoreCase = true) -> Color(0xFFDC2626)
                                                                    else -> Color(0xFFB45309)
                                                                },
                                                                fontSize = 9.5.sp
                                                            ),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }

                                                    if (pPart.status.isNotBlank()) {
                                                        Surface(
                                                            color = Color(0xFFF1F5F9),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                text = pPart.status,
                                                                style = MaterialTheme.typography.labelSmall.copy(
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = Slate800,
                                                                    fontSize = 9.5.sp
                                                                ),
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Lihat Detail →",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = HighDensityBlue,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp
                                                        ),
                                                        modifier = Modifier.clickable {
                                                            selectedPrediksiDetailItem = pPart
                                                        }
                                                    )
                                                    if (onNavigateToPrediksiPart != null) {
                                                        Text(
                                                            text = "• Menu Prediksi",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = Slate400,
                                                                fontSize = 10.sp
                                                            ),
                                                            modifier = Modifier.clickable {
                                                                viewingItem = null
                                                                onNavigateToPrediksiPart()
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Jika belum ada data lookup PREDIKSI PART, tampilkan card hitung prediksi part
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "PREDIKSI PART",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Blue900)
                                    )
                                    Text(
                                        text = "Hitung estimasi KM pergantian part unit #${item.noMobil}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        prediksiPartTargetItem = item
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_prediksi_part_service_detail")
                                ) {
                                    Icon(Icons.Default.AddChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PREDIKSI PART", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }

                    // SEKSI 2: DATA DETAIL SERVICE KENDARAAN (6 data utama, data 7-14 view all)
                    DetailRowItem(label = "1. ID", value = item.id.toString())
                    DetailRowItem(label = "2. STATUS MOBIL", value = item.statusMobil)
                    DetailRowItem(label = "3. No_Mobil", value = item.noMobil, isHighlight = true)
                    DetailRowItem(label = "4. SERVICE", value = item.service)
                    DetailRowItem(label = "5. Selisih_KM", value = item.selisihKm)
                    DetailRowItem(label = "6. Notification_Desc", value = item.notificationDesc)

                    if (showAllDetails) {
                        DetailRowItem(label = "7. Notif_No", value = item.notifNo)
                        DetailRowItem(label = "8. Status", value = item.status)
                        DetailRowItem(label = "9. KM_Servis_Sebelumnya", value = item.kmServisSebelumnya)
                        DetailRowItem(label = "10. KM_Jatuh_Tempo", value = item.kmJatuhTempo)
                        DetailRowItem(label = "11. Equipment_No", value = item.equipmentNo)
                        DetailRowItem(label = "12. Notif_Date", value = item.notifDate)
                        DetailRowItem(label = "13. KM_Total", value = item.kmTotal)
                        DetailRowItem(label = "14. Sticker_Bandara", value = item.stickerBandara)
                    }

                    // Tombol View All / Sembunyikan untuk 8 data lainnya
                    OutlinedButton(
                        onClick = { showAllDetails = !showAllDetails },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .testTag("btn_view_all_service_details"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (showAllDetails) Color(0xFFF1F5F9) else Color(0xFFEFF6FF),
                            contentColor = HighDensityBlue
                        ),
                        border = BorderStroke(1.dp, if (showAllDetails) Color(0xFFCBD5E1) else Color(0xFF93C5FD)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (showAllDetails) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showAllDetails) "Tampilkan 6 Data Saja ▲" else "View All (8 Data Lainnya) ▼",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol bawah: masih berlaku untuk tambah data
                    Button(
                        onClick = {
                            prediksiPartTargetItem = item
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityBlue
                        ),
                        modifier = Modifier.testTag("btn_prediksi_part_bottom")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddChart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TAMBAH PREDIKSI",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick = { viewingItem = null }
                    ) {
                        Text("Tutup")
                    }
                }
            }
        )
    }

    // DIALOG VIEW DETAIL PREDIKSI PART DARI DATA LENGKAP SERVICE
    if (selectedPrediksiDetailItem != null) {
        PrediksiPartDetailDialog(
            item = selectedPrediksiDetailItem!!,
            argoList = argoList,
            currentUser = currentUser,
            onDismiss = { selectedPrediksiDetailItem = null },
            onEditClick = {
                val toEdit = selectedPrediksiDetailItem
                selectedPrediksiDetailItem = null
                if (toEdit != null) {
                    prediksiPartTargetItem = viewingItem ?: serviceList.find { isVehicleMatch(it.noMobil, toEdit.noMobil) }
                }
            },
            onVehicleClick = { vehicleNo ->
                selectedPrediksiDetailItem = null
                onVehicleClick(vehicleNo)
            },
            onUpdateApproval = { newStatus ->
                val current = selectedPrediksiDetailItem ?: return@PrediksiPartDetailDialog
                val updated = resolvePrediksiPartWithLiveArgo(current, argoList).copy(aprovel = newStatus)
                onSavePrediksiPart(updated)
                selectedPrediksiDetailItem = updated
            }
        )
    }

    // DIALOG FORM EDIT 14 DATA
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Edit Service Kendaraan #${formNoMobil} (ID: $formId)",
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
                            value = formId,
                            onValueChange = { formId = it },
                            label = { Text("1. ID") },
                            singleLine = true,
                            modifier = Modifier.weight(0.4f)
                        )
                        OutlinedTextField(
                            value = formStatusMobil,
                            onValueChange = { formStatusMobil = it },
                            label = { Text("2. STATUS MOBIL") },
                            placeholder = { Text("Siap Operasi") },
                            singleLine = true,
                            modifier = Modifier.weight(0.6f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = formNoMobil,
                            onValueChange = { formNoMobil = it },
                            label = { Text("3. No_Mobil") },
                            placeholder = { Text("101") },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f)
                        )
                        OutlinedTextField(
                            value = formService,
                            onValueChange = { formService = it },
                            label = { Text("4. SERVICE") },
                            placeholder = { Text("Service Berkala") },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = formSelisihKm,
                            onValueChange = { formSelisihKm = it },
                            label = { Text("5. Selisih_KM") },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f)
                        )
                        OutlinedTextField(
                            value = formNotifNo,
                            onValueChange = { formNotifNo = it },
                            label = { Text("7. Notif_No") },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f)
                        )
                    }

                    OutlinedTextField(
                        value = formNotificationDesc,
                        onValueChange = { formNotificationDesc = it },
                        label = { Text("6. Notification_Desc") },
                        placeholder = { Text("Deskripsi pekerjaan") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(0.5f)) {
                            OutlinedTextField(
                                value = formStatus,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("8. Status") },
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
                                listOf("NOPR", "OSNO").forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s) },
                                        onClick = {
                                            formStatus = s
                                            statusDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = formEquipmentNo,
                            onValueChange = { formEquipmentNo = it },
                            label = { Text("11. Equipment_No") },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = formKmServisSebelumnya,
                            onValueChange = { formKmServisSebelumnya = it },
                            label = { Text("9. KM_Servis_Sblm") },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f)
                        )
                        OutlinedTextField(
                            value = formKmJatuhTempo,
                            onValueChange = { formKmJatuhTempo = it },
                            label = { Text("10. KM_Jatuh_Tempo") },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = formNotifDate,
                            onValueChange = { formNotifDate = it },
                            label = { Text("12. Notif_Date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f)
                        )
                        OutlinedTextField(
                            value = formKmTotal,
                            onValueChange = { formKmTotal = it },
                            label = { Text("13. KM_Total") },
                            singleLine = true,
                            modifier = Modifier.weight(0.5f)
                        )
                    }

                    OutlinedTextField(
                        value = formStickerBandara,
                        onValueChange = { formStickerBandara = it },
                        label = { Text("14. Sticker_Bandara") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (formNoMobil.isNotBlank()) {
                            val entityId = formId.toLongOrNull() ?: (editingItem?.id ?: 0L)
                            val entity = ServiceTeknikEntity(
                                id = entityId,
                                statusMobil = formStatusMobil.trim(),
                                noMobil = formNoMobil.trim(),
                                service = formService.trim(),
                                selisihKm = formSelisihKm.trim(),
                                notificationDesc = formNotificationDesc.trim(),
                                notifNo = formNotifNo.trim(),
                                status = formStatus.trim(),
                                kmServisSebelumnya = formKmServisSebelumnya.trim(),
                                kmJatuhTempo = formKmJatuhTempo.trim(),
                                equipmentNo = formEquipmentNo.trim(),
                                notifDate = formNotifDate.trim(),
                                kmTotal = formKmTotal.trim(),
                                stickerBandara = formStickerBandara.trim(),
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

    // DIALOG PREDIKSI PART DARI DATA SERVICE KENDARAAN
    if (prediksiPartTargetItem != null) {
        PrediksiPartFromServiceDialog(
            serviceItem = prediksiPartTargetItem!!,
            argoList = argoList,
            currentUser = currentUser,
            onDismiss = { prediksiPartTargetItem = null },
            onSave = { entity ->
                onSavePrediksiPart(entity)
                prediksiPartTargetItem = null
            },
            onViewInPrediksiScreen = onNavigateToPrediksiPart?.let { nav ->
                {
                    prediksiPartTargetItem = null
                    viewingItem = null
                    nav()
                }
            }
        )
    }
}

@Composable
private fun DetailRowItem(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Surface(
        color = if (isHighlight) Blue50 else Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isHighlight) HighDensityBlue else Slate600
                ),
                modifier = Modifier.weight(0.45f)
            )
            Text(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
                    color = if (isHighlight) HighDensityBlue else Slate800,
                    textAlign = TextAlign.End
                ),
                modifier = Modifier.weight(0.55f)
            )
        }
    }
}

/**
 * Dialog Form PREDIKSI PART yang dipanggil langsung dari Data Lengkap Service Kendaraan.
 *
 * Rumus & Aturan:
 * - NO MOBIL = NO MOBIL (Otomatis dari data Service)
 * - SERVICE = SERVICE (Otomatis dari data Service)
 * - KM_MOBIL = ambil data dari ARGO KM (Mencari data argo kendaraan berdasarkan No Mobil)
 * - NAMA PART = input manual huruf kapital (Huruf besar otomatis)
 * - Prediksi km = input number (Hanya digit angka)
 * - KM target = KM_MOBIL + Prediksi km (Kalkulasi otomatis)
 * - argo sekarang = argo yang tertera di Argo (Data argo saat ini)
 * - Estimasi KM = KM TARGET - ARGO SEKARANG (Kalkulasi otomatis)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrediksiPartFromServiceDialog(
    serviceItem: ServiceTeknikEntity,
    argoList: List<ArgoEntity>,
    currentUser: UserEntity? = null,
    onDismiss: () -> Unit,
    onSave: (PrediksiPartEntity) -> Unit,
    onViewInPrediksiScreen: (() -> Unit)? = null
) {
    val autoNipNama = remember(currentUser) {
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
    val matchingArgo = remember(serviceItem.noMobil, argoList) {
        argoList.find { isVehicleMatch(it.noMobil, serviceItem.noMobil) }
    }
    val defaultArgoKm = matchingArgo?.km?.trim().orEmpty().ifBlank { "0" }

    var tanggal by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    val noMobil = serviceItem.noMobil.trim()
    var service by remember { mutableStateOf(serviceItem.service.trim().ifBlank { "Service Berkala" }) }
    var kmInput by remember { mutableStateOf(defaultArgoKm) }
    var nipNama by remember { mutableStateOf(autoNipNama) }
    var namaPart by remember { mutableStateOf("") }
    var prediksiKm by remember { mutableStateOf("") }
    var argoSekarang by remember { mutableStateOf(defaultArgoKm) }
    var aprovel by remember { mutableStateOf("Menunggu") }
    var status by remember { mutableStateOf("PROSES") }
    var keterangan by remember { mutableStateOf("") }

    val isApproved = remember(aprovel) {
        aprovel.trim().equals("Disetujui", ignoreCase = true) ||
            aprovel.contains("SETUJU", ignoreCase = true) ||
            aprovel.contains("APPROV", ignoreCase = true) ||
            aprovel.contains("ACC", ignoreCase = true)
    }

    val isKeteranganRequired = isApproved && status.trim().equals("SELESAI", ignoreCase = true)
    val isKeteranganValid = !isKeteranganRequired || keterangan.trim().isNotBlank()

    val allowedApprovalLevels = remember {
        listOf("SUPER ADMIN", "GH", "SH", "KEPALA BENGKEL", "ADMIN", "ALL", "ADMIN,ALL", "ADMIN, ALL")
    }
    val userLevel = remember(currentUser) {
        currentUser?.level?.trim()?.uppercase() ?: ""
    }
    val canApprove = remember(userLevel) {
        val clean = userLevel.replace(" ", "")
        userLevel in allowedApprovalLevels || clean in listOf("SUPERADMIN", "GH", "SH", "KEPALABENGKEL")
    }

    var aprovelExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    val uniqueId = remember { System.currentTimeMillis() }

    // Perhitungan otomatis real-time: [ARGO_SEKARANG] - [KM_TARGET]
    val kmInputLong = kmInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val prediksiKmLong = prediksiKm.filter { it.isDigit() }.toLongOrNull() ?: 0L
    val kmTargetLong = kmInputLong + prediksiKmLong
    val kmTargetStr = kmTargetLong.toString()

    val argoSekarangLong = argoSekarang.filter { it.isDigit() || it == '-' }.toLongOrNull() ?: 0L
    val estimasiKmLong = argoSekarangLong - kmTargetLong
    val estimasiKmStr = estimasiKmLong.toString()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddChart, contentDescription = null, tint = HighDensityBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PREDIKSI PART",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                    )
                    Text(
                        text = "Unit #${serviceItem.noMobil} • Dari Data Lengkap Service",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

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
                        .testTag("service_teknik_prediksi_part_id_input")
                )

                // 1. TANGGAL (tidak dapat edit)
                OutlinedTextField(
                    value = tanggal,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("TANGGAL") },
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

                // 2. NO MOBIL (tidak dapat edit)
                OutlinedTextField(
                    value = noMobil,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("NO MOBIL") },
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

                // 3. SERVICE (tidak dapat edit)
                OutlinedTextField(
                    value = service,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("SERVICE") },
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

                // 4. KM_MOBIL (tidak dapat edit) | ARGO_SEKARANG (tidak dapat edit)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kmInput,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("KM_MOBIL") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            disabledContainerColor = Color(0xFFF1F5F9),
                            focusedBorderColor = Color(0xFFCBD5E1),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.weight(0.5f)
                    )
                    OutlinedTextField(
                        value = argoSekarang,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("ARGO_SEKARANG") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            disabledContainerColor = Color(0xFFF1F5F9),
                            focusedBorderColor = Color(0xFFCBD5E1),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.weight(0.5f)
                    )
                }

                // 5. NIP_NAMA (tidak dapat edit)
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

                // 6. NAMA_PART
                OutlinedTextField(
                    value = namaPart,
                    onValueChange = { namaPart = it.uppercase() },
                    label = { Text("NAMA PART") },
                    placeholder = { Text("CONTOH: OLI MESIN, KANVAS REM") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 7. PREDIKSI KM
                OutlinedTextField(
                    value = prediksiKm,
                    onValueChange = { prediksiKm = it.filter { c -> c.isDigit() } },
                    label = { Text("PREDIKSI KM") },
                    placeholder = { Text("Contoh: 5000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 8. KALKULASI OTOMATIS
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KALKULASI OTOMATIS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                            )
                            Surface(
                                color = if (estimasiKmLong >= 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (estimasiKmLong >= 0) "Sisa: +$estimasiKmStr KM (Aman)" else "Lewat: $estimasiKmStr KM (Jatuh Tempo)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (estimasiKmLong >= 0) Color(0xFF15803D) else Color(0xFFB91C1C)
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        // KM TARGET = KM_MOBIL + PREDIKSI_KM
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "KM TARGET",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Slate800)
                                )
                                Text(
                                    text = "KM_MOBIL ($kmInputLong) + Prediksi ($prediksiKmLong)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate600, fontSize = 11.sp)
                                )
                            }
                            Text(
                                text = "$kmTargetStr KM",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = HighDensityBlue)
                            )
                        }

                        // ESTIMASI KM = ARGO SEKARANG - KM TARGET
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ESTIMASI KM",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Slate800)
                                )
                                Text(
                                    text = "ARGO ($argoSekarangLong) - KM TARGET ($kmTargetLong)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Slate600, fontSize = 11.sp)
                                )
                            }
                            Text(
                                text = "$estimasiKmStr KM",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (estimasiKmLong >= 0) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            )
                        }
                    }
                }

                // 9. STATUS & 10. KETERANGAN (Hanya tampil jika APROVEL Disetujui, selain itu hiden)
                if (isApproved) {
                    // 9. STATUS (Drop Down PROSES , SELESAI)
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

                    // 10. KETERANGAN (long texs huruf besar)
                    OutlinedTextField(
                        value = keterangan,
                        onValueChange = { keterangan = it.uppercase() },
                        label = { Text(if (isKeteranganRequired) "KETERANGAN *" else "KETERANGAN") },
                        placeholder = { Text("CATATAN TAMBAHAN PREDIKSI PART...") },
                        isError = isKeteranganRequired && keterangan.trim().isBlank(),
                        minLines = 3,
                        maxLines = 6,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color(0xFFFFF1F2),
                            focusedBorderColor = if (isKeteranganRequired) HighDensityBlue else Color(0xFFCBD5E1),
                            errorBorderColor = Color(0xFFDC2626),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // TOMBOL APPROVEL TARUH PALING BAWAH (Hanya muncul jika user Level SUPER ADMIN, GH, SH, KEPALA BENGKEL)
                if (canApprove) {
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
                            Text(
                                text = "APROVEL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Blue900
                                )
                            )
                            Surface(
                                color = when (aprovel) {
                                    "Disetujui" -> Color(0xFFDCFCE7)
                                    "Ditolak" -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFFEF3C7)
                                },
                                shape = RoundedCornerShape(4.dp)
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
                            listOf("Menunggu", "Disetujui", "Ditolak").forEach { option ->
                                val isSelected = aprovel.equals(option, ignoreCase = true)
                                val btnBg = when {
                                    isSelected && option == "Disetujui" -> Color(0xFF15803D)
                                    isSelected && option == "Ditolak" -> Color(0xFFDC2626)
                                    isSelected && option == "Menunggu" -> Color(0xFFB45309)
                                    option == "Disetujui" -> Color(0xFFDCFCE7)
                                    option == "Ditolak" -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFFEF3C7)
                                }
                                val btnText = when {
                                    isSelected -> Color.White
                                    option == "Disetujui" -> Color(0xFF15803D)
                                    option == "Ditolak" -> Color(0xFFDC2626)
                                    else -> Color(0xFFB45309)
                                }

                                Button(
                                    onClick = { aprovel = option },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = btnBg,
                                        contentColor = btnText
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(option, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    if (namaPart.isNotBlank() && prediksiKm.isNotBlank() && isKeteranganValid) {
                        val newEntity = PrediksiPartEntity(
                            id = uniqueId,
                            tanggal = tanggal.trim(),
                            noMobil = noMobil,
                            service = service.trim(),
                            kmInput = kmInput.trim(),
                            nipNama = nipNama.trim(),
                            namaPart = namaPart.trim(),
                            prediksiKm = prediksiKm.trim(),
                            kmTarget = kmTargetStr,
                            argoSekarang = argoSekarang.trim(),
                            estimasiKm = estimasiKmStr,
                            aprovel = aprovel.trim(),
                            keterangan = if (isApproved) keterangan.trim() else "",
                            status = if (isApproved) status.trim() else "PROSES",
                            isSynced = false
                        )
                        onSave(newEntity)
                    }
                },
                enabled = namaPart.isNotBlank() && prediksiKm.isNotBlank() && isKeteranganValid,
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
            ) {
                Text("Simpan Prediksi Part", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
