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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BanEntity
import com.example.data.local.entity.BaterayEntity
import com.example.data.local.entity.KoplingEntity
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.data.local.entity.ReservasiEntity
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun KomponenArmadaScreen(
    initialTab: Int = 0, // 0 = BAN, 1 = KOPLING, 2 = BATERAY, 3 = RESERVASI
    currentUser: com.example.data.local.entity.UserEntity? = null,
    banList: List<BanEntity>,
    koplingList: List<KoplingEntity>,
    baterayList: List<BaterayEntity>,
    reservasiList: List<ReservasiEntity> = emptyList(),
    kendaraanList: List<MasterKendaraanEntity>,
    onSaveBan: (BanEntity) -> Unit,
    onDeleteBan: (Long) -> Unit,
    onSaveKopling: (KoplingEntity) -> Unit,
    onDeleteKopling: (Long) -> Unit,
    onSaveBateray: (BaterayEntity) -> Unit,
    onDeleteBateray: (Long) -> Unit,
    onSaveReservasi: (ReservasiEntity) -> Unit = {},
    onDeleteReservasi: (Long) -> Unit = {},
    onVehicleClick: (String) -> Unit,
    onSyncClick: () -> Unit
) {
    val userLevel = currentUser?.level?.trim()?.uppercase().orEmpty()
    val isSuperAdmin = currentUser != null && (
        userLevel == "SUPER ADMIN" ||
        userLevel == "SUPERADMIN" ||
        userLevel.contains("SUPER ADMIN") ||
        currentUser.username.trim().equals("superadmin", ignoreCase = true)
    )

    var selectedTab by remember { mutableIntStateOf(if (!isSuperAdmin && initialTab == 3) 0 else initialTab) }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state for BAN
    var showBanDialog by remember { mutableStateOf(false) }
    var editingBan by remember { mutableStateOf<BanEntity?>(null) }
    var banNoMobil by remember { mutableStateOf("") }
    var banKm by remember { mutableStateOf("") }
    var banMerk by remember { mutableStateOf("") }

    // Dialog state for KOPLING
    var showKoplingDialog by remember { mutableStateOf(false) }
    var editingKopling by remember { mutableStateOf<KoplingEntity?>(null) }
    var koplingNoMobil by remember { mutableStateOf("") }
    var koplingJenisTipe by remember { mutableStateOf("NEW TRANSMOVER") }
    var koplingKm by remember { mutableStateOf("") }
    var koplingSelectedTipeFilter by remember { mutableStateOf<String?>(null) }

    // Dialog state for BATERAY
    var showBaterayDialog by remember { mutableStateOf(false) }
    var editingBateray by remember { mutableStateOf<BaterayEntity?>(null) }
    var baterayNoMobil by remember { mutableStateOf("") }
    var baterayTanggal by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Row
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = HighDensityBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = HighDensityBlue
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; searchQuery = "" },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TripOrigin, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BAN (${banList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; searchQuery = "" },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("KOPLING (${koplingList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; searchQuery = "" },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BATERAY (${baterayList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                if (isSuperAdmin) {
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3; searchQuery = "" },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("RESERVASI (${reservasiList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action & Search Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedTab == 0 || selectedTab == 2) {
                        Button(
                            onClick = {
                                when (selectedTab) {
                                    0 -> {
                                        editingBan = null
                                        banNoMobil = ""
                                        banKm = "35000"
                                        banMerk = "Bridgestone Ecopia"
                                        showBanDialog = true
                                    }
                                    2 -> {
                                        editingBateray = null
                                        baterayNoMobil = ""
                                        baterayTanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                        showBaterayDialog = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val btnTitle = if (selectedTab == 0) "TAMBAH BAN" else "TAMBAH BATERAY"
                            Text(btnTitle, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    } else if (selectedTab == 1) {
                        Text(
                            text = "DATA KOPLING (${koplingList.size} Unit)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Blue900
                            )
                        )
                    } else {
                        Text(
                            text = "DATA RESERVASI (${reservasiList.size} Data)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Blue900
                            )
                        )
                    }

                    IconButton(onClick = onSyncClick) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync", tint = Blue700)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedTab == 1) {
                    androidx.compose.foundation.text.BasicTextField(
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
                                            text = "Cari No Mobil atau Tipe...",
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
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari berdasarkan No Mobil atau Keterangan...") },
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Lists
        when (selectedTab) {
            0 -> {
                // BAN LIST (ID | NO_MOBIL | KM_BAN | MERK_BAN)
                val filteredBan = remember(banList, searchQuery) {
                    banList.filter {
                        if (searchQuery.isBlank()) true else {
                            val q = searchQuery.trim().lowercase()
                            it.noMobil.lowercase().contains(q) || it.merkBan.lowercase().contains(q) || it.kmBan.contains(q)
                        }
                    }
                }

                if (filteredBan.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Tidak ada data ban.", color = Slate600)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredBan, key = { it.id }) { item ->
                            KomponenItemCard(
                                title = "Mobil #${item.noMobil}",
                                subtitle = "KM Ban: ${item.kmBan} km",
                                badgeText = item.merkBan,
                                onEdit = {
                                    editingBan = item
                                    banNoMobil = item.noMobil
                                    banKm = item.kmBan
                                    banMerk = item.merkBan
                                    showBanDialog = true
                                },
                                onDelete = { onDeleteBan(item.id) },
                                onVehicleClick = { onVehicleClick(item.noMobil) }
                            )
                        }
                    }
                }
            }
            1 -> {
                // KOPLING TABLE (Sesuai ID | Kolom 1: No Mobil | Kolom 2: Jenis Tipe | Kolom 3: Total KM)
                val sortedKopling = remember(koplingList) { koplingList.sortedBy { it.id } }

                val typeCounts = remember(sortedKopling, kendaraanList) {
                    val map = mutableMapOf<String, Int>()
                    sortedKopling.forEach { item ->
                        val raw = item.jenisTipe.trim()
                        val tipe = if (raw.isNotBlank()) raw else {
                            kendaraanList.find { k ->
                                k.noMobil.trim().equals(item.noMobil.trim(), ignoreCase = true) ||
                                k.noMobil.trim().removePrefix("#").equals(item.noMobil.trim().removePrefix("#"), ignoreCase = true)
                            }?.tipe?.trim()?.ifBlank { "NEW TRANSMOVER" } ?: "NEW TRANSMOVER"
                        }
                        val key = tipe.uppercase()
                        map[key] = (map[key] ?: 0) + 1
                    }
                    map
                }

                val availableTipes = remember(typeCounts) { typeCounts.keys.sorted() }

                val filteredKopling = remember(sortedKopling, searchQuery, koplingSelectedTipeFilter, kendaraanList) {
                    val byTipe = if (koplingSelectedTipeFilter.isNullOrBlank()) {
                        sortedKopling
                    } else {
                        sortedKopling.filter { item ->
                            val raw = item.jenisTipe.trim()
                            val tipe = if (raw.isNotBlank()) raw else {
                                kendaraanList.find { k ->
                                    k.noMobil.trim().equals(item.noMobil.trim(), ignoreCase = true) ||
                                    k.noMobil.trim().removePrefix("#").equals(item.noMobil.trim().removePrefix("#"), ignoreCase = true)
                                }?.tipe?.trim()?.ifBlank { "NEW TRANSMOVER" } ?: "NEW TRANSMOVER"
                            }
                            tipe.equals(koplingSelectedTipeFilter, ignoreCase = true)
                        }
                    }

                    if (searchQuery.isBlank()) byTipe else {
                        val q = searchQuery.trim().lowercase()
                        byTipe.filter {
                            it.noMobil.lowercase().contains(q) ||
                            it.jenisTipe.lowercase().contains(q) ||
                            it.displayTotalKm.contains(q) ||
                            it.id.toString() == q
                        }
                    }
                }

                // Filter Tipe Bar
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = koplingSelectedTipeFilter == null,
                            onClick = { koplingSelectedTipeFilter = null },
                            label = { Text("Semua (${sortedKopling.size})", fontSize = 11.sp, fontWeight = if (koplingSelectedTipeFilter == null) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (koplingSelectedTipeFilter == null) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF8FAFC),
                                selectedContainerColor = HighDensityBlue,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )

                        availableTipes.forEach { tipe ->
                            val count = typeCounts[tipe] ?: 0
                            val isSelected = koplingSelectedTipeFilter.equals(tipe, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    koplingSelectedTipeFilter = if (isSelected) null else tipe
                                },
                                label = { Text("$tipe ($count)", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF8FAFC),
                                    selectedContainerColor = HighDensityBlue,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (filteredKopling.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tidak ada data kopling yang cocok.", color = Slate600)
                            if (koplingSelectedTipeFilter != null || searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(onClick = { koplingSelectedTipeFilter = null; searchQuery = "" }) {
                                    Text("Reset Filter")
                                }
                            }
                        }
                    }
                } else {
                    val cellBorderColor = Color(0xFFE2E8F0)
                    val headerBorderColor = Color(0xFF2563EB)
                    val widthNoMobil = 105.dp
                    val widthTipe = 150.dp
                    val widthTotalKm = 105.dp
                    val totalTableWidth = widthNoMobil + widthTipe + widthTotalKm // 360.dp

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Column(
                            modifier = Modifier
                                .width(totalTableWidth)
                                .fillMaxHeight()
                                .border(BorderStroke(1.dp, cellBorderColor))
                        ) {
                            // HEADER TABEL (No Mobil | Tipe | Total KM)
                            Row(
                                modifier = Modifier
                                    .width(totalTableWidth)
                                    .background(HighDensityBlue),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.width(widthNoMobil).padding(vertical = 10.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                    Text("No Mobil", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                                }
                                Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                                Box(modifier = Modifier.width(widthTipe).padding(vertical = 10.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                    Text("Tipe", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                                }
                                Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))

                                Box(modifier = Modifier.width(widthTotalKm).padding(vertical = 10.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                                    Text("Total KM", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                                }
                            }

                            // ROWS
                            LazyColumn(modifier = Modifier.width(totalTableWidth).weight(1f)) {
                                itemsIndexed(filteredKopling, key = { _, item -> item.id }) { index, item ->
                                    val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                                    Row(
                                        modifier = Modifier
                                            .width(totalTableWidth)
                                            .background(rowBg)
                                            .border(BorderStroke(0.5.dp, cellBorderColor)),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Kolom 1: No Mobil
                                        Box(modifier = Modifier.width(widthNoMobil).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Surface(color = Blue50, shape = RoundedCornerShape(6.dp), modifier = Modifier.clickable { onVehicleClick(item.noMobil) }) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
                                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(item.noMobil, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = HighDensityBlue, fontSize = 12.sp), maxLines = 1)
                                                }
                                            }
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                        // Kolom 2: Tipe
                                        Box(modifier = Modifier.width(widthTipe).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Surface(color = Color(0xFFF0FDF4), shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Color(0xFFBBF7D0))) {
                                                Text(item.jenisTipe.ifBlank { "NEW TRANSMOVER" }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF166534), fontSize = 11.sp), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                        // Kolom 3: Total KM
                                        Box(modifier = Modifier.width(widthTotalKm).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Speed, contentDescription = null, tint = Slate600, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text("${item.displayTotalKm} KM", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Slate800, fontSize = 11.5.sp), maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // BATERAY LIST (ID | NO_MOBIL | TANGGAL_GANTI)
                val filteredBateray = remember(baterayList, searchQuery) {
                    baterayList.filter {
                        if (searchQuery.isBlank()) true else {
                            val q = searchQuery.trim().lowercase()
                            it.noMobil.lowercase().contains(q) || it.tanggalGanti.contains(q)
                        }
                    }
                }

                if (filteredBateray.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Tidak ada data baterai.", color = Slate600)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredBateray, key = { it.id }) { item ->
                            KomponenItemCard(
                                title = "Mobil #${item.noMobil}",
                                subtitle = "Tanggal Pergantian Aki / Baterai",
                                badgeText = item.tanggalGanti,
                                onEdit = {
                                    editingBateray = item
                                    baterayNoMobil = item.noMobil
                                    baterayTanggal = item.tanggalGanti
                                    showBaterayDialog = true
                                },
                                onDelete = { onDeleteBateray(item.id) },
                                onVehicleClick = { onVehicleClick(item.noMobil) }
                            )
                        }
                    }
                }
            }
            3 -> {
                // RESERVASI TABLE
                // NO MOBIL | Order | Material | Material description | Document Date | Posting Date | Quantity | Base Unit of Measure | Amt.in loc.cur. | Batch
                val sortedReservasi = remember(reservasiList) { reservasiList.sortedBy { it.id } }
                val filteredReservasi = remember(sortedReservasi, searchQuery) {
                    if (searchQuery.isBlank()) sortedReservasi else {
                        val q = searchQuery.trim().lowercase()
                        sortedReservasi.filter {
                            it.noMobil.lowercase().contains(q) ||
                            it.orderNo.lowercase().contains(q) ||
                            it.material.lowercase().contains(q) ||
                            it.materialDescription.lowercase().contains(q) ||
                            it.batch.lowercase().contains(q) ||
                            it.id.toString() == q
                        }
                    }
                }

                if (filteredReservasi.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Tidak ada data reservasi.", color = Slate600)
                    }
                } else {
                    val cellBorderColor = Color(0xFFE2E8F0)
                    val headerBorderColor = Color(0xFF2563EB)
                    val widthNoMobil = 110.dp
                    val widthOrder = 100.dp
                    val widthMaterial = 110.dp
                    val widthMatDesc = 220.dp
                    val widthDocDate = 115.dp
                    val widthPostDate = 115.dp
                    val widthQty = 85.dp
                    val widthUnit = 95.dp
                    val widthAmt = 130.dp
                    val widthBatch = 100.dp
                    val totalTableWidth = widthNoMobil + widthOrder + widthMaterial + widthMatDesc +
                            widthDocDate + widthPostDate + widthQty + widthUnit + widthAmt + widthBatch

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Column(
                            modifier = Modifier
                                .width(totalTableWidth)
                                .fillMaxHeight()
                                .border(BorderStroke(1.dp, cellBorderColor))
                        ) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .width(totalTableWidth)
                                    .background(HighDensityBlue),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    "NO MOBIL" to widthNoMobil,
                                    "Order" to widthOrder,
                                    "Material" to widthMaterial,
                                    "Material description" to widthMatDesc,
                                    "Document Date" to widthDocDate,
                                    "Posting Date" to widthPostDate,
                                    "Quantity" to widthQty,
                                    "Base Unit of Measure" to widthUnit,
                                    "Amt.in loc.cur." to widthAmt,
                                    "Batch" to widthBatch
                                ).forEachIndexed { idx, pair ->
                                    Box(
                                        modifier = Modifier.width(pair.second).padding(vertical = 10.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            pair.first,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 11.5.sp
                                            ),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                    if (idx < 9) {
                                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(headerBorderColor))
                                    }
                                }
                            }

                            // Rows
                            LazyColumn(modifier = Modifier.width(totalTableWidth).weight(1f)) {
                                itemsIndexed(filteredReservasi, key = { _, item -> item.id }) { index, item ->
                                    val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                                    Row(
                                        modifier = Modifier
                                            .width(totalTableWidth)
                                            .background(rowBg)
                                            .border(BorderStroke(0.5.dp, cellBorderColor)),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. NO MOBIL
                                        Box(modifier = Modifier.width(widthNoMobil).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Surface(color = Blue50, shape = RoundedCornerShape(6.dp), modifier = Modifier.clickable { onVehicleClick(item.noMobil) }) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
                                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(item.noMobil, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = HighDensityBlue, fontSize = 12.sp), maxLines = 1)
                                                }
                                            }
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                        // 2. Order
                                        Box(modifier = Modifier.width(widthOrder).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Text(item.orderNo.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Slate800, fontSize = 11.sp), maxLines = 1)
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                        // 3. Material
                                        Box(modifier = Modifier.width(widthMaterial).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Text(item.material.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155), fontSize = 11.sp), maxLines = 1)
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                        // 4. Material description
                                        Box(modifier = Modifier.width(widthMatDesc).padding(vertical = 8.dp, horizontal = 6.dp), contentAlignment = Alignment.CenterStart) {
                                            Text(item.materialDescription.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontSize = 11.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                        // 5. Document Date
                                        Box(modifier = Modifier.width(widthDocDate).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Text(item.documentDate.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp), maxLines = 1)
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                        // 6. Posting Date
                                        Box(modifier = Modifier.width(widthPostDate).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Text(item.postingDate.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp), maxLines = 1)
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                        // 7. Quantity
                                        Box(modifier = Modifier.width(widthQty).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Text(item.quantity.ifBlank { "-" }, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Blue900, fontSize = 11.5.sp), maxLines = 1)
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                        // 8. Base Unit of Measure
                                        Box(modifier = Modifier.width(widthUnit).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Text(item.baseUnitOfMeasure.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF166534), fontSize = 11.sp), maxLines = 1)
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                        // 9. Amt.in loc.cur.
                                        Box(modifier = Modifier.width(widthAmt).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Text(item.amtInLocCur.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Slate800, fontSize = 11.sp), maxLines = 1)
                                        }
                                        Box(modifier = Modifier.width(1.dp).height(38.dp).background(cellBorderColor))

                                        // 10. Batch
                                        Box(modifier = Modifier.width(widthBatch).padding(vertical = 8.dp, horizontal = 4.dp), contentAlignment = Alignment.Center) {
                                            Text(item.batch.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp), maxLines = 1)
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

    // Modal BAN
    if (showBanDialog) {
        AlertDialog(
            onDismissRequest = { showBanDialog = false },
            title = { Text(if (editingBan == null) "Tambah Data Ban" else "Edit Ban #${editingBan?.noMobil}", fontWeight = FontWeight.Bold, color = Blue900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = banNoMobil,
                        onValueChange = { banNoMobil = it },
                        label = { Text("No Mobil") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = banKm,
                        onValueChange = { banKm = it },
                        label = { Text("KM Ban") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = banMerk,
                        onValueChange = { banMerk = it },
                        label = { Text("Merk Ban") },
                        placeholder = { Text("Bridgestone / Dunlop / GT Radial") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (banNoMobil.isNotBlank()) {
                            onSaveBan(
                                BanEntity(
                                    id = editingBan?.id ?: 0L,
                                    noMobil = banNoMobil.trim(),
                                    kmBan = banKm.trim(),
                                    merkBan = banMerk.trim(),
                                    isSynced = false
                                )
                            )
                            showBanDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                ) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showBanDialog = false }) { Text("Batal") } }
        )
    }

    // Modal KOPLING
    if (showKoplingDialog) {
        AlertDialog(
            onDismissRequest = { showKoplingDialog = false },
            title = { Text(if (editingKopling == null) "Tambah Data Kopling" else "Edit Kopling #${editingKopling?.noMobil}", fontWeight = FontWeight.Bold, color = Blue900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = koplingNoMobil,
                        onValueChange = { koplingNoMobil = it },
                        label = { Text("Kolom 1: No Mobil") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = koplingJenisTipe,
                        onValueChange = { koplingJenisTipe = it },
                        label = { Text("Kolom 2: Jenis Tipe") },
                        placeholder = { Text("NEW TRANSMOVER") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = koplingKm,
                        onValueChange = { koplingKm = it },
                        label = { Text("Kolom 3: Total KM") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (koplingNoMobil.isNotBlank()) {
                            onSaveKopling(
                                KoplingEntity(
                                    id = editingKopling?.id ?: 0L,
                                    noMobil = koplingNoMobil.trim().uppercase(),
                                    jenisTipe = koplingJenisTipe.trim().ifBlank { "NEW TRANSMOVER" },
                                    totalKm = koplingKm.trim(),
                                    kmKopling = koplingKm.trim(),
                                    isSynced = false
                                )
                            )
                            showKoplingDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                ) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showKoplingDialog = false }) { Text("Batal") } }
        )
    }

    // Modal BATERAY
    if (showBaterayDialog) {
        AlertDialog(
            onDismissRequest = { showBaterayDialog = false },
            title = { Text(if (editingBateray == null) "Tambah Data Baterai" else "Edit Baterai #${editingBateray?.noMobil}", fontWeight = FontWeight.Bold, color = Blue900) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = baterayNoMobil,
                        onValueChange = { baterayNoMobil = it },
                        label = { Text("No Mobil") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = baterayTanggal,
                        onValueChange = { baterayTanggal = it },
                        label = { Text("Tanggal Ganti Baterai") },
                        placeholder = { Text("YYYY-MM-DD") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (baterayNoMobil.isNotBlank()) {
                            onSaveBateray(
                                BaterayEntity(
                                    id = editingBateray?.id ?: 0L,
                                    noMobil = baterayNoMobil.trim(),
                                    tanggalGanti = baterayTanggal.trim(),
                                    isSynced = false
                                )
                            )
                            showBaterayDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                ) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showBaterayDialog = false }) { Text("Batal") } }
        )
    }
}

@Composable
private fun KomponenItemCard(
    title: String,
    subtitle: String,
    badgeText: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onVehicleClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = Blue50,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onVehicleClick() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$title ↗",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = HighDensityBlue)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
            }

            Surface(
                color = Blue50,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Blue700),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            Row(modifier = Modifier.padding(start = 8.dp)) {
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
