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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.KoplingEntity
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
 * Helper untuk menentukan Tipe armada kopling:
 * 1. Mengambil dari kolom jenisTipe milik KoplingEntity
 * 2. Jika kosong, mencari kesesuaian nomor mobil di MasterKendaraanEntity
 * 3. Fallback default ke "NEW TRANSMOVER"
 */
private fun resolveKoplingTipe(item: KoplingEntity, kendaraanList: List<MasterKendaraanEntity>): String {
    val raw = item.jenisTipe.trim()
    if (raw.isNotBlank()) return raw
    val itemNoClean = item.noMobil.trim().removePrefix("#").trim()
    val matched = kendaraanList.firstOrNull { k ->
        val kClean = k.noMobil.trim().removePrefix("#").trim()
        kClean.isNotBlank() && (
            kClean.equals(itemNoClean, ignoreCase = true) ||
            (kClean.filter { it.isDigit() }.isNotEmpty() &&
             kClean.filter { it.isDigit() } == itemNoClean.filter { it.isDigit() })
        )
    }
    val kTipe = matched?.tipe?.trim()
    return if (!kTipe.isNullOrBlank()) kTipe else "NEW TRANSMOVER"
}

/**
 * MENU KOPLING
 * Database ID: 1qBtcaJP1adyawgZ2hbAIvlld-usZ3CV5YB6QSRpVxd4
 * Nama Sheet: KOPLING
 *
 * Susunan Tampilan Tabel:
 * | No Mobil | Tipe | KM_KOPLING |
 *
 * Data diurutkan sesuai dengan ID (ASC) secara default, atau dapat diurutkan via header kolom.
 */

enum class KoplingSortColumn {
    DEFAULT_ID, NO_MOBIL, TIPE, KM_KOPLING
}

@Composable
fun KoplingScreen(
    koplingList: List<KoplingEntity>,
    kendaraanList: List<MasterKendaraanEntity> = emptyList(),
    onSaveKopling: (KoplingEntity) -> Unit,
    onDeleteKopling: (Long) -> Unit,
    onVehicleClick: (String) -> Unit,
    onSyncClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTipeFilter by remember { mutableStateOf<String?>(null) } // null = Semua Tipe
    var showTipeDropdown by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<KoplingEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<KoplingEntity?>(null) }

    var sortColumn by remember { mutableStateOf(KoplingSortColumn.DEFAULT_ID) }
    var sortAscending by remember { mutableStateOf(true) }

    // Dialog state
    var formId by remember { mutableStateOf("") }
    var formNoMobil by remember { mutableStateOf("") }
    var formJenisTipe by remember { mutableStateOf("NEW TRANSMOVER") }
    var formTotalKm by remember { mutableStateOf("") }

    // Urutkan data berdasarkan kolom yang dipilih (default ID ASC)
    val sortedList = remember(koplingList, sortColumn, sortAscending, kendaraanList) {
        when (sortColumn) {
            KoplingSortColumn.DEFAULT_ID -> if (sortAscending) koplingList.sortedBy { it.id } else koplingList.sortedByDescending { it.id }
            KoplingSortColumn.NO_MOBIL -> {
                val comp = compareBy<KoplingEntity> { it.noMobil.lowercase() }.thenBy { it.id }
                if (sortAscending) koplingList.sortedWith(comp) else koplingList.sortedWith(comp.reversed())
            }
            KoplingSortColumn.TIPE -> {
                val comp = compareBy<KoplingEntity> { resolveKoplingTipe(it, kendaraanList).lowercase() }.thenBy { it.id }
                if (sortAscending) koplingList.sortedWith(comp) else koplingList.sortedWith(comp.reversed())
            }
            KoplingSortColumn.KM_KOPLING -> {
                val comp = compareBy<KoplingEntity> { kotlin.math.abs(parseKmValue(it.displayKmKopling)) }.thenBy { it.id }
                if (sortAscending) koplingList.sortedWith(comp) else koplingList.sortedWith(comp.reversed())
            }
        }
    }

    // Hitung jumlah unit per Tipe (dinamis dari seluruh data kopling)
    val typeCounts = remember(koplingList, kendaraanList) {
        val counts = mutableMapOf<String, Int>()
        koplingList.forEach { item ->
            val t = resolveKoplingTipe(item, kendaraanList).uppercase()
            counts[t] = (counts[t] ?: 0) + 1
        }
        counts
    }

    // Daftar semua tipe unik yang tersedia
    val availableTipeList = remember(typeCounts) {
        typeCounts.keys.sorted()
    }

    // Filter gabungan: Tipe & Pencarian
    val filteredList = remember(sortedList, searchQuery, selectedTipeFilter, kendaraanList) {
        val byTipe = if (selectedTipeFilter.isNullOrBlank() || selectedTipeFilter.equals("SEMUA", ignoreCase = true)) {
            sortedList
        } else {
            sortedList.filter { item ->
                resolveKoplingTipe(item, kendaraanList).equals(selectedTipeFilter, ignoreCase = true)
            }
        }

        if (searchQuery.isBlank()) {
            byTipe
        } else {
            val q = searchQuery.trim().lowercase()
            byTipe.filter { item ->
                val tipe = resolveKoplingTipe(item, kendaraanList)
                item.noMobil.lowercase().contains(q) ||
                item.jenisTipe.lowercase().contains(q) ||
                tipe.lowercase().contains(q) ||
                item.displayKmKopling.lowercase().contains(q) ||
                item.displayTotalKm.lowercase().contains(q) ||
                item.id.toString() == q
            }
        }
    }

    val cellBorderColor = Color(0xFFE2E8F0)
    val headerBorderColor = Color(0xFF2563EB)

    // Lebar tetap yang pas dan rapat:
    // No Mobil: 110dp
    // Tipe: 145dp
    // KM_KOPLING: 125dp
    // Total lebar tabel: 380dp
    val widthNoMobil = 110.dp
    val widthTipe = 145.dp
    val widthKmKopling = 125.dp
    val totalTableWidth = widthNoMobil + widthTipe + widthKmKopling // 380.dp

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
                                Icons.Default.Tune,
                                contentDescription = null,
                                tint = HighDensityBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "KOPLING",
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
                                    text = if (selectedTipeFilter != null || searchQuery.isNotBlank()) {
                                        "${filteredList.size} / ${koplingList.size} Unit"
                                    } else {
                                        "${koplingList.size} Unit"
                                    },
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

                // Search Box dibuat lebih kecil mengikuti ukuran huruf
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
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("kopling_search_input"),
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
                                        text = "Cari No Mobil, Tipe, atau KM_KOPLING...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = Slate400
                                        )
                                    )
                                }
                                innerTextField()
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Hapus pencarian",
                                        tint = Slate400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ==========================================
                // FILTER BERDASARKAN TIPE
                // ==========================================
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Blue50, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = HighDensityBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = "Filter Tipe:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800,
                                    fontSize = 12.sp
                                )
                            )

                            // Dropdown Menu Trigger untuk memilih Tipe secara cepat
                            Box {
                                Surface(
                                    color = if (selectedTipeFilter != null) HighDensityBlue else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (selectedTipeFilter != null) HighDensityBlue else Color(0xFFCBD5E1)),
                                    modifier = Modifier
                                        .clickable { showTipeDropdown = true }
                                        .testTag("kopling_tipe_dropdown_trigger")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = selectedTipeFilter ?: "Pilih Tipe",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (selectedTipeFilter != null) Color.White else Slate800,
                                                fontSize = 11.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Pilih Tipe",
                                            tint = if (selectedTipeFilter != null) Color.White else Slate600,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = showTipeDropdown,
                                    onDismissRequest = { showTipeDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Semua Tipe (${sortedList.size})",
                                                fontWeight = if (selectedTipeFilter == null) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selectedTipeFilter == null) HighDensityBlue else Slate800
                                            )
                                        },
                                        onClick = {
                                            selectedTipeFilter = null
                                            showTipeDropdown = false
                                        },
                                        leadingIcon = if (selectedTipeFilter == null) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(16.dp)) }
                                        } else null
                                    )
                                    availableTipeList.forEach { tipe ->
                                        val count = typeCounts[tipe] ?: 0
                                        val isSelected = selectedTipeFilter.equals(tipe, ignoreCase = true)
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "$tipe ($count)",
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) HighDensityBlue else Slate800
                                                )
                                            },
                                            onClick = {
                                                selectedTipeFilter = tipe
                                                showTipeDropdown = false
                                            },
                                            leadingIcon = if (isSelected) {
                                                { Icon(Icons.Default.Check, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(16.dp)) }
                                            } else null
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedTipeFilter != null) {
                            TextButton(
                                onClick = { selectedTipeFilter = null },
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("kopling_tipe_reset_button"),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Rose600,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "Reset",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Rose600,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Deretan FilterChip Scrollable
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Chip SEMUA
                        FilterChip(
                            selected = selectedTipeFilter == null,
                            onClick = { selectedTipeFilter = null },
                            label = {
                                Text(
                                    text = "Semua (${sortedList.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTipeFilter == null) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (selectedTipeFilter == null) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF8FAFC),
                                selectedContainerColor = HighDensityBlue,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("kopling_chip_all")
                        )

                        // Chip Tiap Tipe Armada
                        availableTipeList.forEach { tipe ->
                            val count = typeCounts[tipe] ?: 0
                            val isSelected = selectedTipeFilter.equals(tipe, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTipeFilter = if (isSelected) null else tipe
                                },
                                label = {
                                    Text(
                                        text = "$tipe ($count)",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFFF8FAFC),
                                    selectedContainerColor = HighDensityBlue,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                modifier = Modifier.testTag("kopling_chip_${tipe.lowercase().replace(" ", "_")}")
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Table Container Card (Wrap content width jika memungkinkan, rapat dan fix dengan tulisan)
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
                        Icon(Icons.Default.Tune, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        val message = if (selectedTipeFilter != null && searchQuery.isNotBlank()) {
                            "Tidak ada data kopling untuk tipe \"$selectedTipeFilter\" dengan kata kunci \"$searchQuery\"."
                        } else if (selectedTipeFilter != null) {
                            "Tidak ada data kopling untuk tipe \"$selectedTipeFilter\"."
                        } else if (searchQuery.isNotBlank()) {
                            "Tidak ada data kopling yang cocok dengan pencarian \"$searchQuery\"."
                        } else {
                            "Tidak ada data kopling."
                        }
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate600, textAlign = TextAlign.Center)
                        )
                        if (selectedTipeFilter != null || searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    selectedTipeFilter = null
                                    searchQuery = ""
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reset Semua Filter")
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tekan tombol 'Sinkron' untuk mengambil data dari Google Sheets (ID: 1qBtcaJP1adyawgZ2hbAIvlld-usZ3CV5YB6QSRpVxd4, Sheet: KOPLING)",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate400, textAlign = TextAlign.Center)
                            )
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
                        // HEADER TABEL SESUAI PERMINTAAN:
                        // | No Mobil | Tipe | KM_KOPLING |
                        // ==========================================
                        Row(
                            modifier = Modifier
                                .width(totalTableWidth)
                                .background(HighDensityBlue),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Kolom 1: No Mobil
                            Box(
                                modifier = Modifier
                                    .width(widthNoMobil)
                                    .clickable {
                                        if (sortColumn == KoplingSortColumn.NO_MOBIL) {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = KoplingSortColumn.NO_MOBIL
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
                                        text = "No Mobil",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    if (sortColumn == KoplingSortColumn.NO_MOBIL) {
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

                            // Kolom 2: Tipe
                            Box(
                                modifier = Modifier
                                    .width(widthTipe)
                                    .clickable {
                                        if (sortColumn == KoplingSortColumn.TIPE) {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = KoplingSortColumn.TIPE
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
                                        text = "Tipe",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    if (sortColumn == KoplingSortColumn.TIPE) {
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

                            // Kolom 3: KM_KOPLING
                            Box(
                                modifier = Modifier
                                    .width(widthKmKopling)
                                    .clickable {
                                        if (sortColumn == KoplingSortColumn.KM_KOPLING) {
                                            sortAscending = !sortAscending
                                        } else {
                                            sortColumn = KoplingSortColumn.KM_KOPLING
                                            sortAscending = false // Default ke DESC agar KM tertinggi di atas
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
                                        text = "KM_KOPLING",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        ),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    if (sortColumn == KoplingSortColumn.KM_KOPLING) {
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
                        }

                        // BARIS DATA TABEL (Urut sesuai ID ASC atau kolom terpilih)
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
                                    // 1. Kolom 1: No Mobil (Klik untuk buka detail Master Kendaraan)
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

                                    // 2. Kolom 2: Tipe (e.g. NEW TRANSMOVER)
                                    Box(
                                        modifier = Modifier
                                            .width(widthTipe)
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val tipeResolved = resolveKoplingTipe(item, kendaraanList)
                                        val isTipeSelected = selectedTipeFilter.equals(tipeResolved, ignoreCase = true)
                                        Surface(
                                            color = if (isTipeSelected) Color(0xFFDCFCE7) else Color(0xFFF0FDF4),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, if (isTipeSelected) Color(0xFF16A34A) else Color(0xFFBBF7D0)),
                                            modifier = Modifier.clickable {
                                                selectedTipeFilter = if (isTipeSelected) null else tipeResolved
                                            }
                                        ) {
                                            Text(
                                                text = tipeResolved,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (isTipeSelected) FontWeight.Bold else FontWeight.SemiBold,
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

                                    // 3. Kolom 3: KM_KOPLING (> 90.000 berwarna merah)
                                    val kmAbs = kotlin.math.abs(parseKmValue(item.displayKmKopling))
                                    val isOver90k = kmAbs > 90000.0
                                    val kmTextColor = if (isOver90k) Rose600 else Slate800
                                    val kmIconColor = if (isOver90k) Rose600 else Slate600
                                    val kmDisplayText = if (item.displayKmKopling.isBlank()) {
                                        "-"
                                    } else if (item.displayKmKopling.endsWith("KM", ignoreCase = true)) {
                                        item.displayKmKopling
                                    } else {
                                        "${item.displayKmKopling} KM"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(widthKmKopling)
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

    // Modal Tambah / Edit Data Kopling
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (editingItem == null) "Tambah Data Kopling" else "Edit Kopling #${editingItem?.id}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = formId,
                        onValueChange = { formId = it },
                        label = { Text("ID (Urutan)") },
                        placeholder = { Text("Contoh: 1, 2, 3...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formNoMobil,
                        onValueChange = { formNoMobil = it },
                        label = { Text("Kolom 1: No Mobil") },
                        placeholder = { Text("Contoh: MWD1336") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formJenisTipe,
                        onValueChange = { formJenisTipe = it },
                        label = { Text("Kolom 2: Tipe") },
                        placeholder = { Text("Contoh: NEW TRANSMOVER") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formTotalKm,
                        onValueChange = { formTotalKm = it },
                        label = { Text("Kolom 3: KM_KOPLING") },
                        placeholder = { Text("Contoh: 151.920") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (formNoMobil.isNotBlank()) {
                            val idVal = formId.trim().toLongOrNull() ?: (editingItem?.id ?: 0L)
                            onSaveKopling(
                                KoplingEntity(
                                    id = idVal,
                                    noMobil = formNoMobil.trim().uppercase(),
                                    jenisTipe = formJenisTipe.trim().ifBlank { "NEW TRANSMOVER" },
                                    totalKm = formTotalKm.trim(),
                                    kmKopling = formTotalKm.trim(),
                                    isSynced = false
                                )
                            )
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                ) {
                    Text("Simpan Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal Konfirmasi Hapus
    if (itemToDelete != null) {
        val item = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Hapus Data Kopling", fontWeight = FontWeight.Bold, color = Rose600) },
            text = {
                Text("Apakah Anda yakin ingin menghapus data kopling Mobil #${item.noMobil} (ID: ${item.id})?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteKopling(item.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}
