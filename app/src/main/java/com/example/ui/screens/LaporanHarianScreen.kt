package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LaporanHarianEntity
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LaporanFilterTab {
    BELUM,
    SELESAI
}

private fun parseLaporanTimestampMillis(item: LaporanHarianEntity): Long? {
    val candidates = listOf(item.updateData, item.timestamp)
    val datePatterns = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd",
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy HH:mm",
        "dd/MM/yyyy",
        "MM/dd/yyyy HH:mm:ss",
        "MM/dd/yyyy",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd"
    )

    for (raw in candidates) {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) continue

        trimmed.toLongOrNull()?.let { epoch ->
            if (epoch > 1000000000000L) return epoch
        }

        for (pattern in datePatterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                val parsed = sdf.parse(trimmed)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
    }

    if (item.id > 1000000000000L) {
        return item.id
    }
    if (item.syncTimestamp > 1000000000000L) {
        return item.syncTimestamp
    }

    return null
}

@Composable
fun LaporanHarianScreen(
    laporanList: List<LaporanHarianEntity>,
    kendaraanList: List<MasterKendaraanEntity>,
    userList: List<UserEntity>,
    currentUser: UserEntity? = null,
    onSaveLaporan: (LaporanHarianEntity) -> Unit,
    onDeleteLaporan: (Long) -> Unit = {},
    onVehicleClick: (String) -> Unit,
    onSyncClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<LaporanHarianEntity?>(null) }
    var timestampInput by remember { mutableStateOf("") }

    val level = currentUser?.level?.trim()?.uppercase() ?: "SUPER ADMIN"
    val isSuperAdmin = level in listOf("SUPER ADMIN", "SUPERADMIN", "ADMIN", "ALL", "ADMIN,ALL", "ADMIN, ALL") || level.contains("ADMIN") || currentUser?.username.equals("superadmin", ignoreCase = true)

    // Format otomatis Nama Mekanik dari user login: [NIP]/[NAMA MEKANIK]
    val loggedInMekanik = remember(currentUser) {
        if (currentUser != null) {
            val nip = currentUser.nip.trim()
            val nama = currentUser.namaMekanik.trim()
            when {
                nip.isNotEmpty() && nama.isNotEmpty() -> "$nip/$nama"
                nip.isNotEmpty() -> nip
                nama.isNotEmpty() -> nama
                else -> currentUser.username.trim()
            }
        } else ""
    }

    // Dialog state
    var noMobil by remember { mutableStateOf("") }
    var namaNip by remember { mutableStateOf("") }
    var keluhanKendaraan by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("PERBAIKAN SELESAI") }
    var namaMekanik by remember { mutableStateOf("") }
    var pengerjaan by remember { mutableStateOf("") }
    var pengerjaanError by remember { mutableStateOf(false) }

    var statusExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf(
        "BELUM DI KERJAKAN",
        "PERBAIKAN SELESAI",
        "MOBIL TIDAK KE BENGKEL"
    )

    val belumDikerjakanCount = remember(laporanList) {
        val countBelum = laporanList.count { it.status.contains("belum", ignoreCase = true) || it.status.isBlank() }
        if (countBelum > 0) countBelum else laporanList.count { !it.status.contains("SELESAI", ignoreCase = true) }
    }

    val filteredList = remember(laporanList, searchQuery) {
        val query = searchQuery.trim().lowercase()
        val queryClean = query.replace("[^a-z0-9]".toRegex(), "")

        laporanList.filter { item ->
            val isSelesai = item.status.contains("SELESAI", ignoreCase = true)
            // Hanya tampilkan data yang BELUM selesai
            if (isSelesai) return@filter false

            if (query.isBlank()) {
                true
            } else {
                val mobilClean = item.noMobil.lowercase().replace("[^a-z0-9]".toRegex(), "")
                item.noMobil.lowercase().contains(query) ||
                        (queryClean.isNotEmpty() && mobilClean.contains(queryClean)) ||
                        item.namaNip.lowercase().contains(query) ||
                        item.keluhanKendaraan.lowercase().contains(query) ||
                        item.namaMekanik.lowercase().contains(query) ||
                        item.poolVendor.lowercase().contains(query) ||
                        item.status.lowercase().contains(query) ||
                        item.pengerjaan.lowercase().contains(query) ||
                        item.kodeAkses.lowercase().contains(query) ||
                        item.token.lowercase().contains(query)
            }
        }.sortedWith { a, b ->
            val timeA = parseLaporanTimestampMillis(a) ?: a.id
            val timeB = parseLaporanTimestampMillis(b) ?: b.id
            timeB.compareTo(timeA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Card Top Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LAPORAN KENDARAAN",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Blue900,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "Belum Dikerjakan : $belumDikerjakanCount Kendaraan",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$belumDikerjakanCount Antrean",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(onClick = onSyncClick) {
                            Icon(Icons.Default.Sync, contentDescription = "Sync", tint = Blue700)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari") },
                    placeholder = { Text("Cari no mobil, pengemudi, keluhan, mekanik...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Cari", tint = HighDensityBlue)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian", tint = Slate400)
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // List of Laporan Harian
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Semua laporan kendaraan telah selesai dikerjakan!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Slate600,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    LaporanCardItem(
                        item = item,
                        isSuperAdmin = isSuperAdmin,
                        onEdit = {
                            editingItem = item
                            timestampInput = item.timestamp
                            noMobil = item.noMobil
                            namaNip = item.namaNip
                            keluhanKendaraan = item.keluhanKendaraan
                            // Sarankan status PERBAIKAN SELESAI
                            status = if (item.status.contains("SELESAI", ignoreCase = true)) item.status else "PERBAIKAN SELESAI"
                            namaMekanik = loggedInMekanik.ifBlank { item.namaMekanik }
                            pengerjaan = item.pengerjaan.uppercase()
                            pengerjaanError = false
                            showDialog = true
                        }
                    )
                }
            }
        }
    }

    // Modal Add / Edit Laporan
    if (showDialog) {
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val activeTimestamp = if (editingItem != null) (editingItem?.timestamp ?: timestampInput) else timestampInput.ifBlank { nowStr }
        val whiteFieldColors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = HighDensityBlue,
            unfocusedBorderColor = Color(0xFFCBD5E1),
            focusedTextColor = Slate800,
            unfocusedTextColor = Slate800
        )

        val readOnlyFieldColors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF8FAFC),
            unfocusedContainerColor = Color(0xFFF8FAFC),
            disabledContainerColor = Color(0xFFF8FAFC),
            focusedBorderColor = Color(0xFFCBD5E1),
            unfocusedBorderColor = Color(0xFFCBD5E1),
            disabledBorderColor = Color(0xFFCBD5E1),
            focusedTextColor = Slate800,
            unfocusedTextColor = Slate800,
            disabledTextColor = Slate800,
            disabledLabelColor = Slate600
        )

        AlertDialog(
            onDismissRequest = { 
                showDialog = false 
                editingItem = null
            },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "MODE KERJAKAN",
                            color = Color(0xFFB45309),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kerjakan Laporan • Mobil #${editingItem?.noMobil ?: noMobil}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ID IDENTITAS UNTUK MEMASTIKAN EDIT MENGUBAH BARIS YANG TEPAT
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (editingItem != null) "ID LAPORAN YANG DI-EDIT: #${editingItem?.id}" else "ID IDENTITAS BARU (TIMESTAMP):",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534),
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (editingItem != null) "ID #${editingItem?.id} • $activeTimestamp" else activeTimestamp,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF14532D),
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }

                    // 1. No Mobil (Tidak dapat diedit)
                    OutlinedTextField(
                        value = noMobil,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("No Mobil") },
                        leadingIcon = {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = HighDensityBlue)
                        },
                        singleLine = true,
                        colors = readOnlyFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 2. Pengemudi/Pelapor (Tidak dapat diedit)
                    OutlinedTextField(
                        value = namaNip,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Pengemudi/Pelapor") },
                        singleLine = true,
                        colors = readOnlyFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 3. Keluhan (Tidak dapat diedit)
                    OutlinedTextField(
                        value = keluhanKendaraan,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Keluhan") },
                        maxLines = 4,
                        colors = readOnlyFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 4. Status Terpilih (BELUM DI KERJAKAN | PERBAIKAN SELESAI | MOBIL TIDAK KE BENGKEL)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Status Terpilih:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate600)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            statusOptions.forEach { opt ->
                                FilterChip(
                                    selected = status.equals(opt, ignoreCase = true),
                                    onClick = { status = opt },
                                    label = {
                                        Text(
                                            text = opt,
                                            fontSize = 11.sp,
                                            fontWeight = if (status.equals(opt, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (status.equals(opt, ignoreCase = true)) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color.White,
                                        selectedContainerColor = when (opt) {
                                            "PERBAIKAN SELESAI" -> Color(0xFFDCFCE7)
                                            "MOBIL TIDAK KE BENGKEL" -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFFEF3C7)
                                        },
                                        selectedLabelColor = when (opt) {
                                            "PERBAIKAN SELESAI" -> Color(0xFF15803D)
                                            "MOBIL TIDAK KE BENGKEL" -> Color(0xFFB91C1C)
                                            else -> Color(0xFFB45309)
                                        }
                                    )
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Status Terpilih") },
                                trailingIcon = {
                                    IconButton(onClick = { statusExpanded = !statusExpanded }) {
                                        Icon(
                                            imageVector = if (statusExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = HighDensityBlue
                                        )
                                    }
                                },
                                colors = whiteFieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { statusExpanded = true }
                            )
                            DropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false },
                                containerColor = Color.White
                            ) {
                                statusOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = opt,
                                                fontWeight = if (status.equals(opt, ignoreCase = true)) FontWeight.Bold else FontWeight.Normal,
                                                color = Slate800
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
                    }

                    // 5. Nama Mekanik (Tidak dapat diedit)
                    OutlinedTextField(
                        value = namaMekanik,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Nama Mekanik ([NIP]/[NAMA MEKANIK])") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = HighDensityBlue)
                        },
                        singleLine = true,
                        colors = readOnlyFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 6. Pengerjaan (SELALU HURUF KAPITAL & WAJIB DIISI)
                    OutlinedTextField(
                        value = pengerjaan,
                        onValueChange = {
                            pengerjaan = it.uppercase()
                            if (it.isNotBlank()) pengerjaanError = false
                        },
                        label = { Text("Pengerjaan * (HURUF KAPITAL)") },
                        placeholder = { Text("CONTOH: GANTI BRAKE PAD, KURAS FREON") },
                        isError = pengerjaanError,
                        supportingText = {
                            if (pengerjaanError) {
                                Text(
                                    text = "Form Pengerjaan wajib diisi sebelum disimpan!",
                                    color = Rose600,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            } else {
                                Text(
                                    text = "* Wajib diisi sebelum disimpan",
                                    color = Slate600,
                                    fontSize = 10.5.sp
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        maxLines = 3,
                        colors = whiteFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pengerjaan.trim().isBlank() && status.contains("SELESAI", ignoreCase = true)) {
                            pengerjaanError = true
                            return@Button
                        }
                        val current = editingItem
                        if (current != null) {
                            val finalMekanik = namaMekanik.trim().ifBlank { loggedInMekanik }
                            val entity = current.copy(
                                noMobil = current.noMobil,
                                namaNip = current.namaNip,
                                keluhanKendaraan = current.keluhanKendaraan,
                                status = status,
                                updateData = nowStr,
                                namaMekanik = finalMekanik,
                                pengerjaan = pengerjaan.trim().uppercase(),
                                isSynced = false
                            )
                            onSaveLaporan(entity)
                            showDialog = false
                            editingItem = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SIMPAN PERUBAHAN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDialog = false 
                    editingItem = null
                }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun LaporanCardItem(
    item: LaporanHarianEntity,
    isSuperAdmin: Boolean,
    onEdit: () -> Unit
) {
    val normStatus = item.status.trim().uppercase()
    val isSelesaiPerbaikan = normStatus.contains("SELESAI") || normStatus.contains("PERBAIKAN SELESAI")
    val hideActions = isSelesaiPerbaikan

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!hideActions) {
                    Modifier.clickable { onEdit() }
                } else {
                    Modifier
                }
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // [No Mobil] dan StatusBadge di pojok kanan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Blue50,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Mobil #${item.noMobil}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = HighDensityBlue
                            )
                        )
                    }
                }

                StatusBadge(status = item.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // [ID]
            Surface(
                color = Color(0xFFF1F5F9),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ID #${item.id} • ${item.timestamp}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pengemudi:[namapengemudi]
            Text(
                text = "Pengemudi: ${item.namaNip}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Blue900
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Keluhan:
            // [keluhan]
            Text(
                text = "Keluhan:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate600
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.keluhanKendaraan.ifBlank { "-" },
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate800)
            )

            if (item.pengerjaan.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Pengerjaan Bengkel: ${item.pengerjaan}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Blue900)
                        )
                        if (item.namaMekanik.isNotBlank()) {
                            Text(
                                text = "Mekanik: ${item.namaMekanik}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Blue700, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Baris Tombol: KERJAKAN & Status Sheets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isSynced) {
                    Surface(
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Sheets",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF047857),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (!hideActions) {
                    // Tombol EDIT / KERJAKAN
                    Button(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Kerjakan",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "KERJAKAN",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    onClick: (() -> Unit)? = null
) {
    val norm = status.trim().uppercase()
    val (bgColor, textColor) = when {
        norm.contains("SELESAI") -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        norm.contains("TIDAK KE BENGKEL") -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
        else -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 10.5.sp
                )
            )
            if (onClick != null) {
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Ganti Status",
                    tint = textColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
