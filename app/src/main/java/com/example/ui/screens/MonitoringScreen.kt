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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ArgoEntity
import com.example.data.local.entity.LaporanHarianEntity
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.data.local.entity.PrediksiPartEntity
import com.example.data.local.entity.RiwayatSkiEntity
import com.example.data.local.entity.ServiceKeindahanEntity
import com.example.data.local.entity.ServiceTeknikEntity
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBg
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800

/**
 * Helper pembagian hak akses kolom Monitoring sesuai role pengguna:
 * 1. LAPORAN KENDARAAN =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|TEKNIK|KEINDAHAN|CAT
 * 2. MONITORING TEKNIK =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|TEKNIK
 * 3. MONITORING KEINDAHAN =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|KEINDAHAN
 * 4. PREDIKSI PART =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|TEKNIK|KEINDAHAN
 */
fun hasMonitoringRoleAccess(userLevel: String, allowedRoles: List<String>): Boolean {
    val norm = userLevel.trim().uppercase()
    if (norm == "SUPER ADMIN" || norm.contains("SUPER ADMIN")) return true
    val parts = norm.split(",").map { it.trim() }
    val upperAllowed = allowedRoles.map { it.uppercase() }
    return parts.any { part ->
        upperAllowed.any { allowed ->
            part == allowed || part.equals(allowed, ignoreCase = true)
        }
    }
}

/**
 * Helper to parse Selisih KM string into signed numeric value
 * Examples:
 * "-389" -> -389.0
 * "-511" -> -511.0
 * "+240 KM" -> 240.0
 * "-50 KM" -> -50.0
 * "+1.450 KM" -> 1450.0
 */
fun parseSelisihKmNumber(raw: String): Double? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    val isNegative = trimmed.startsWith("-")
    val clean = trimmed
        .replace("+", "")
        .replace("-", "")
        .replace("KM", "", ignoreCase = true)
        .replace("km", "")
        .replace(" ", "")
        .replace(".", "")
        .replace(",", ".")
        .trim()
    val num = clean.toDoubleOrNull() ?: return null
    return if (isNegative) -num else num
}

@Composable
fun MonitoringScreen(
    teknikList: List<ServiceTeknikEntity>,
    laporanList: List<LaporanHarianEntity>,
    keindahanList: List<ServiceKeindahanEntity>,
    kendaraanList: List<MasterKendaraanEntity>,
    riwayatSkiList: List<RiwayatSkiEntity> = emptyList(),
    prediksiPartList: List<PrediksiPartEntity> = emptyList(),
    argoList: List<ArgoEntity> = emptyList(),
    currentUser: UserEntity? = null,
    onVehicleClick: (String) -> Unit,
    onSyncClick: () -> Unit,
    onNavigateToSki: () -> Unit = {},
    onNavigateToPrediksiPart: () -> Unit = {},
    onNavigateToLaporan: () -> Unit = {},
    onNavigateToServiceTeknik: () -> Unit = {},
    onNavigateToServiceKeindahan: () -> Unit = {}
) {
    // 1. LAPORAN HARIAN: data yang dengan status bukan SELESAI PERBAIKAN
    val laporanAktifCount = remember(laporanList) {
        laporanList.count { !it.status.contains("SELESAI", ignoreCase = true) }
    }

    // 2. TEKNIK: jumlah service lebih selisih km lebih -500
    val teknikDiAtas500 = remember(teknikList) {
        teknikList.filter { item ->
            val num = parseSelisihKmNumber(item.selisihKm)
            num != null && num > -500.0
        }
    }

    // CM TK: jumlah service selisih km lebih -500 status KALONG atau jadwal mobil KALONG
    val cmTkList = remember(teknikDiAtas500) {
        teknikDiAtas500.filter { item ->
            item.status.equals("KALONG", ignoreCase = true) ||
            item.statusMobil.contains("KALONG", ignoreCase = true)
        }
    }

    // CS TK: jumlah service selisih km lebih -500 status pagi atau jadwal mobil CS
    val csTkList = remember(teknikDiAtas500) {
        teknikDiAtas500.filter { item ->
            item.status.contains("pagi", ignoreCase = true) ||
            item.statusMobil.contains("PAGI", ignoreCase = true) ||
            item.statusMobil.contains("CS", ignoreCase = true)
        }
    }

    // 3. KEINDAHAN: jumlah service keindahan dengan selisih km lebih dari -500
    val keindahanDiAtas500 = remember(keindahanList) {
        keindahanList.filter { item ->
            val num = parseSelisihKmNumber(item.selisihKm)
            num != null && num > -500.0
        }
    }

    // CM KI: jumlah service keindahan dengan selisih km lebih dari -500 dengan status Kalong
    val cmKiList = remember(keindahanDiAtas500) {
        keindahanDiAtas500.filter { item ->
            item.status.equals("KALONG", ignoreCase = true) ||
            item.statusMobil.contains("KALONG", ignoreCase = true)
        }
    }

    // CS KI: jumlah service keindahan dengan selisih km lebih dari -500 dengan status pagi
    val csKiList = remember(keindahanDiAtas500) {
        keindahanDiAtas500.filter { item ->
            item.status.contains("pagi", ignoreCase = true) ||
            item.statusMobil.contains("PAGI", ignoreCase = true) ||
            item.statusMobil.contains("CS", ignoreCase = true)
        }
    }

    // Hanya tampilkan ringkasan kolom
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HighDensityBg)
    ) {
        MonitoringOverviewTab(
            laporanList = laporanList,
            laporanAktifCount = laporanAktifCount,
            teknikList = teknikList,
            teknikDiAtas500 = teknikDiAtas500,
            cmTkList = cmTkList,
            csTkList = csTkList,
            keindahanList = keindahanList,
            keindahanDiAtas500 = keindahanDiAtas500,
            cmKiList = cmKiList,
            csKiList = csKiList,
            riwayatSkiList = riwayatSkiList,
            prediksiPartList = prediksiPartList,
            argoList = argoList,
            currentUser = currentUser,
            onVehicleClick = onVehicleClick,
            onSyncClick = onSyncClick,
            onNavigateToSki = onNavigateToSki,
            onNavigateToPrediksiPart = onNavigateToPrediksiPart,
            onNavigateToLaporan = onNavigateToLaporan,
            onNavigateToServiceTeknik = onNavigateToServiceTeknik,
            onNavigateToServiceKeindahan = onNavigateToServiceKeindahan
        )
    }
}

/**
 * RINGKASAN KOLOM MONITORING
 * Struktur Kolom & Hak Akses:
 * 1. LAPORAN KENDARAAN =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|TEKNIK|KEINDAHAN|CAT
 * 2. MONITORING TEKNIK =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|TEKNIK
 * 3. MONITORING KEINDAHAN =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|KEINDAHAN
 * 4. PREDIKSI PART =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|TEKNIK|KEINDAHAN
 */
@Composable
private fun MonitoringOverviewTab(
    laporanList: List<LaporanHarianEntity>,
    laporanAktifCount: Int,
    teknikList: List<ServiceTeknikEntity>,
    teknikDiAtas500: List<ServiceTeknikEntity>,
    cmTkList: List<ServiceTeknikEntity>,
    csTkList: List<ServiceTeknikEntity>,
    keindahanList: List<ServiceKeindahanEntity>,
    keindahanDiAtas500: List<ServiceKeindahanEntity>,
    cmKiList: List<ServiceKeindahanEntity>,
    csKiList: List<ServiceKeindahanEntity>,
    riwayatSkiList: List<RiwayatSkiEntity> = emptyList(),
    prediksiPartList: List<PrediksiPartEntity> = emptyList(),
    argoList: List<ArgoEntity> = emptyList(),
    currentUser: UserEntity? = null,
    onVehicleClick: (String) -> Unit,
    onSyncClick: () -> Unit,
    onNavigateToSki: () -> Unit = {},
    onNavigateToPrediksiPart: () -> Unit = {},
    onNavigateToLaporan: () -> Unit = {},
    onNavigateToServiceTeknik: () -> Unit = {},
    onNavigateToServiceKeindahan: () -> Unit = {}
) {
    val userLevel = currentUser?.level?.trim()?.uppercase() ?: "SUPER ADMIN"

    // 1. LAPORAN KENDARAAN =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|TEKNIK|KEINDAHAN|CAT
    val canAccessLaporan = remember(userLevel) {
        hasMonitoringRoleAccess(
            userLevel,
            listOf("SUPER ADMIN", "ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "KEINDAHAN", "CAT")
        )
    }

    // 2. MONITORING TEKNIK =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|TEKNIK
    val canAccessTeknik = remember(userLevel) {
        hasMonitoringRoleAccess(
            userLevel,
            listOf("SUPER ADMIN", "ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK")
        )
    }

    // 3. MONITORING KEINDAHAN =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|KEINDAHAN
    val canAccessKeindahan = remember(userLevel) {
        hasMonitoringRoleAccess(
            userLevel,
            listOf("SUPER ADMIN", "ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "KEINDAHAN")
        )
    }

    // 4. PREDIKSI PART =>> SUPER ADMIN|ADMIN|ALL|KEPALA BENGKEL|GH|SH|TEKNIK|KEINDAHAN
    val canAccessPrediksiPart = remember(userLevel) {
        hasMonitoringRoleAccess(
            userLevel,
            listOf("SUPER ADMIN", "ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "KEINDAHAN")
        )
    }

    // Tentukan kategori awal yang diizinkan untuk dibuka
    val defaultCategory = remember(canAccessTeknik, canAccessLaporan, canAccessKeindahan, canAccessPrediksiPart) {
        when {
            canAccessTeknik -> "TEKNIK"
            canAccessLaporan -> "LAPORAN_HARIAN"
            canAccessKeindahan -> "KEINDAHAN"
            canAccessPrediksiPart -> "PROSES_PART"
            else -> ""
        }
    }
    var selectedCategory by remember(defaultCategory) { mutableStateOf(defaultCategory) }

    // Validasi kategori aktif agar tidak membuka kategori yang dihiden
    val activeCategory = when {
        selectedCategory in listOf("TEKNIK", "CM_TK", "CS_TK") && canAccessTeknik -> selectedCategory
        selectedCategory == "LAPORAN_HARIAN" && canAccessLaporan -> selectedCategory
        selectedCategory in listOf("KEINDAHAN", "CM_KI", "CS_KI") && canAccessKeindahan -> selectedCategory
        selectedCategory in listOf("PROSES_PART", "APROVEL_PART") && canAccessPrediksiPart -> selectedCategory
        else -> defaultCategory
    }

    var searchQuery by remember { mutableStateOf("") }

    // PREDIKSI PART live & active
    val livePrediksiPartList = remember(prediksiPartList, argoList) {
        prediksiPartList.map { resolvePrediksiPartWithLiveArgo(it, argoList) }
    }

    val activePrediksiPartList = remember(livePrediksiPartList) {
        livePrediksiPartList.filter { item ->
            val isSelesai = item.status.trim().equals("SELESAI", ignoreCase = true) ||
                item.status.contains("SELESAI", ignoreCase = true)
            !isSelesai
        }
    }

    val prosesPartList = remember(activePrediksiPartList) {
        activePrediksiPartList.filter { item ->
            item.status.trim().equals("PROSES", ignoreCase = true) ||
                item.status.contains("PROSES", ignoreCase = true)
        }
    }

    val aprovelPartList = remember(activePrediksiPartList) {
        activePrediksiPartList.filter { item ->
            item.aprovel.contains("TUNGGU", ignoreCase = true) ||
                item.aprovel.contains("MENUNGGU", ignoreCase = true) ||
                item.aprovel.contains("PENDING", ignoreCase = true) ||
                item.aprovel.isBlank()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP BANNER
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MONITORING BENGKEL & ARMADA",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Klik salah satu kolom untuk melihat rincian armada secara langsung.",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )
                }

                IconButton(
                    onClick = onSyncClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sinkronkan",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ==========================================
        // 1. LAPORAN KENDARAAN (LAPORAN HARIAN)
        // ==========================================
        if (canAccessLaporan) {
            MonitoringLaporanCard(
                count = laporanAktifCount,
                isSelected = activeCategory == "LAPORAN_HARIAN",
                onClick = { selectedCategory = "LAPORAN_HARIAN" },
                onNavigateToLaporan = onNavigateToLaporan
            )
        }

        // ==========================================
        // 2. TEKNIK | CM TK | CS TK
        // ==========================================
        if (canAccessTeknik) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Blue50, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = HighDensityBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MONITORING TEKNIK",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Blue900,
                                        fontSize = 13.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Surface(
                                    color = Blue50,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Text(
                                        text = "${teknikDiAtas500.size} Data",
                                        color = HighDensityBlue,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onNavigateToServiceTeknik,
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Open",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Baris 3 Kolom: TEKNIK | CM TK | CS TK
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonitoringMetricCard(
                            title = "TEKNIK",
                            badgeText = "KM > -500",
                            count = teknikDiAtas500.size,
                            description = "Seluruh Servis",
                            accentColor = HighDensityBlue,
                            accentLightColor = Blue50,
                            isSelected = activeCategory == "TEKNIK",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategory = "TEKNIK" }
                        )

                        MonitoringMetricCard(
                            title = "CM TK",
                            badgeText = "Kalong",
                            count = cmTkList.size,
                            description = "Shift / Status Kalong",
                            accentColor = Color(0xFFD97706),
                            accentLightColor = Color(0xFFFEF3C7),
                            isSelected = activeCategory == "CM_TK",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategory = "CM_TK" }
                        )

                        MonitoringMetricCard(
                            title = "CS TK",
                            badgeText = "Pagi / CS",
                            count = csTkList.size,
                            description = "Status Pagi / CS",
                            accentColor = Color(0xFF0891B2),
                            accentLightColor = Color(0xFFCFFAFE),
                            isSelected = activeCategory == "CS_TK",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategory = "CS_TK" }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. KEINDAHAN | CM KI | CS KI
        // ==========================================
        if (canAccessKeindahan) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "MONITORING KEINDAHAN",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Blue900,
                                        fontSize = 13.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                                ) {
                                    Text(
                                        text = "${keindahanDiAtas500.size} Data",
                                        color = Color(0xFF059669),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onNavigateToServiceKeindahan,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Open",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Baris 3 Kolom: KEINDAHAN | CM KI | CS KI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonitoringMetricCard(
                            title = "KEINDAHAN",
                            badgeText = "KM > -500",
                            count = keindahanDiAtas500.size,
                            description = "Seluruh Keindahan",
                            accentColor = Color(0xFF059669),
                            accentLightColor = Color(0xFFDCFCE7),
                            isSelected = activeCategory == "KEINDAHAN",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategory = "KEINDAHAN" }
                        )

                        MonitoringMetricCard(
                            title = "CM KI",
                            badgeText = "Kalong",
                            count = cmKiList.size,
                            description = "Status Kalong",
                            accentColor = Color(0xFFCA8A04),
                            accentLightColor = Color(0xFFFEF9C3),
                            isSelected = activeCategory == "CM_KI",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategory = "CM_KI" }
                        )

                        MonitoringMetricCard(
                            title = "CS KI",
                            badgeText = "Pagi",
                            count = csKiList.size,
                            description = "Status Pagi",
                            accentColor = Color(0xFF0D9488),
                            accentLightColor = Color(0xFFCCFBF1),
                            isSelected = activeCategory == "CS_KI",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategory = "CS_KI" }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. PREDIKSI PART: PROSES | APROVEL
        // ==========================================
        if (canAccessPrediksiPart) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PrecisionManufacturing,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PREDIKSI PART",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Blue900,
                                        fontSize = 13.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                                ) {
                                    Text(
                                        text = "${activePrediksiPartList.size} Data",
                                        color = Color(0xFFB45309),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onNavigateToPrediksiPart,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Open",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Baris 2 Kolom: PROSES | APROVEL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonitoringMetricCard(
                            title = "PROSES",
                            badgeText = "Status",
                            count = prosesPartList.size,
                            description = "Dalam Pengerjaan",
                            accentColor = Color(0xFFF59E0B),
                            accentLightColor = Color(0xFFFEF3C7),
                            isSelected = activeCategory == "PROSES_PART",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategory = "PROSES_PART" }
                        )

                        MonitoringMetricCard(
                            title = "APROVEL",
                            badgeText = "Approval",
                            count = aprovelPartList.size,
                            description = "Menunggu Persetujuan",
                            accentColor = Color(0xFFE11D48),
                            accentLightColor = Color(0xFFFFE4E6),
                            isSelected = activeCategory == "APROVEL_PART",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategory = "APROVEL_PART" }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 5. DAFTAR DETAIL REAL-TIME SESUAI KOLOM TERPILIH
        // ==========================================
        val hasAnyAccess = canAccessLaporan || canAccessTeknik || canAccessKeindahan || canAccessPrediksiPart
        if (hasAnyAccess && activeCategory.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header Kategori Terpilih
                    val (categoryTitle, categoryColor, categoryCount) = when (activeCategory) {
                        "TEKNIK" -> Triple("TEKNIK (KM > -500)", HighDensityBlue, teknikDiAtas500.size)
                        "CM_TK" -> Triple("CM TK (Kalong & KM > -500)", Color(0xFFD97706), cmTkList.size)
                        "CS_TK" -> Triple("CS TK (Pagi/CS & KM > -500)", Color(0xFF0891B2), csTkList.size)
                        "KEINDAHAN" -> Triple("KEINDAHAN (KM > -500)", Color(0xFF059669), keindahanDiAtas500.size)
                        "CM_KI" -> Triple("CM KI (Kalong & KM > -500)", Color(0xFFCA8A04), cmKiList.size)
                        "CS_KI" -> Triple("CS KI (Pagi & KM > -500)", Color(0xFF0D9488), csKiList.size)
                        "PROSES_PART" -> Triple("PREDIKSI PART - PROSES", Color(0xFFF59E0B), prosesPartList.size)
                        "APROVEL_PART" -> Triple("PREDIKSI PART - APROVEL", Color(0xFFE11D48), aprovelPartList.size)
                        "LAPORAN_HARIAN" -> Triple("LAPORAN KENDARAAN", Color(0xFF4F46E5), laporanAktifCount)
                        else -> Triple("-", Slate600, 0)
                    }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = categoryColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$categoryCount Data",
                                color = categoryColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = categoryTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Blue900,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nomor mobil, servis, atau keluhan...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = categoryColor,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Konten List Sesuai Pilihan
                when (activeCategory) {
                    "LAPORAN_HARIAN" -> {
                        val filteredLaporan = remember(laporanList, searchQuery) {
                            val activeLaporan = laporanList.filter { !it.status.contains("SELESAI", ignoreCase = true) }
                            if (searchQuery.isBlank()) activeLaporan
                            else {
                                val q = searchQuery.trim().lowercase()
                                activeLaporan.filter {
                                    it.noMobil.lowercase().contains(q) ||
                                            it.keluhanKendaraan.lowercase().contains(q) ||
                                            it.status.lowercase().contains(q)
                                }
                            }
                        }

                        if (filteredLaporan.isEmpty()) {
                            Text(
                                text = "Tidak ada Laporan Harian yang belum selesai perbaikan.",
                                color = Slate600,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                filteredLaporan.take(15).forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { onVehicleClick(item.noMobil) }
                                        ) {
                                            Surface(
                                                color = Color(0xFFEEF2FF),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "#${item.noMobil} ↗",
                                                    color = Color(0xFF4F46E5),
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = item.keluhanKendaraan.ifBlank { "Keluhan Umum" },
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Blue900
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = item.timestamp.ifBlank { "-" },
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                                                )
                                            }
                                        }

                                        val statusBg = when {
                                            item.status.contains("Selesai", ignoreCase = true) -> Color(0xFFDCFCE7)
                                            item.status.contains("Sedang", ignoreCase = true) -> Color(0xFFFEF3C7)
                                            else -> Color(0xFFFEE2E2)
                                        }
                                        val statusText = when {
                                            item.status.contains("Selesai", ignoreCase = true) -> Color(0xFF166534)
                                            item.status.contains("Sedang", ignoreCase = true) -> Color(0xFF92400E)
                                            else -> Color(0xFF991B1B)
                                        }
                                        Surface(
                                            color = statusBg,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.status.ifBlank { "Belum" },
                                                color = statusText,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "TEKNIK", "CM_TK", "CS_TK" -> {
                        val baseTeknik = when (selectedCategory) {
                            "CM_TK" -> cmTkList
                            "CS_TK" -> csTkList
                            else -> teknikDiAtas500
                        }

                        val filteredTeknik = remember(baseTeknik, searchQuery) {
                            if (searchQuery.isBlank()) baseTeknik
                            else {
                                val q = searchQuery.trim().lowercase()
                                baseTeknik.filter {
                                    it.noMobil.lowercase().contains(q) ||
                                            it.service.lowercase().contains(q) ||
                                            it.statusMobil.lowercase().contains(q) ||
                                            it.status.lowercase().contains(q)
                                }
                            }
                        }

                        if (filteredTeknik.isEmpty()) {
                            Text(
                                text = "Tidak ada data servis teknik yang sesuai.",
                                color = Slate600,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                filteredTeknik.take(15).forEach { item ->
                                    val isKalong = item.status.equals("KALONG", ignoreCase = true) ||
                                            item.statusMobil.contains("KALONG", ignoreCase = true)
                                    val kmNum = parseSelisihKmNumber(item.selisihKm) ?: 0.0

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { onVehicleClick(item.noMobil) }
                                        ) {
                                            Surface(
                                                color = if (isKalong) Color(0xFFFEF08A) else Blue50,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isKalong) Icons.Default.NightlightRound else Icons.Default.DirectionsCar,
                                                        contentDescription = null,
                                                        tint = if (isKalong) Color(0xFF854D0E) else HighDensityBlue,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "#${item.noMobil}",
                                                        color = if (isKalong) Color(0xFF854D0E) else HighDensityBlue,
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = item.service.ifBlank { "Service Berkala" },
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Blue900
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Shift: ${item.statusMobil.ifBlank { "-" }}",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Status: ${item.status.ifBlank { "OSNO" }}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = if (item.status.equals("NOPR", ignoreCase = true)) Color(0xFF15803D) else Color(0xFFB45309),
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 11.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            color = if (kmNum >= 0.0) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "${item.selisihKm} KM",
                                                color = if (kmNum >= 0.0) Color(0xFFDC2626) else Color(0xFFB45309),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "KEINDAHAN", "CM_KI", "CS_KI" -> {
                        val baseKi = when (selectedCategory) {
                            "CM_KI" -> cmKiList
                            "CS_KI" -> csKiList
                            else -> keindahanDiAtas500
                        }

                        val filteredKi = remember(baseKi, searchQuery) {
                            if (searchQuery.isBlank()) baseKi
                            else {
                                val q = searchQuery.trim().lowercase()
                                baseKi.filter {
                                    it.noMobil.lowercase().contains(q) ||
                                            it.jenisService.lowercase().contains(q) ||
                                            it.statusMobil.lowercase().contains(q) ||
                                            it.status.lowercase().contains(q)
                                }
                            }
                        }

                        if (filteredKi.isEmpty()) {
                            Text(
                                text = "Tidak ada data servis keindahan yang sesuai.",
                                color = Slate600,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                filteredKi.take(15).forEach { item ->
                                    val isKalong = item.status.equals("KALONG", ignoreCase = true) ||
                                            item.statusMobil.contains("KALONG", ignoreCase = true)
                                    val kmNum = parseSelisihKmNumber(item.selisihKm) ?: 0.0

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { onVehicleClick(item.noMobil) }
                                        ) {
                                            Surface(
                                                color = if (isKalong) Color(0xFFFEF08A) else Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isKalong) Icons.Default.NightlightRound else Icons.Default.DirectionsCar,
                                                        contentDescription = null,
                                                        tint = if (isKalong) Color(0xFF854D0E) else Color(0xFF059669),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "#${item.noMobil}",
                                                        color = if (isKalong) Color(0xFF854D0E) else Color(0xFF059669),
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = item.jenisService.ifBlank { "Service Keindahan" },
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Blue900
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Shift: ${item.statusMobil.ifBlank { "-" }}",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Status: ${item.status.ifBlank { "OSNO" }}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = if (item.status.equals("NOPR", ignoreCase = true)) Color(0xFF15803D) else Color(0xFFB45309),
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 11.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            color = if (kmNum >= 0.0) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "${item.selisihKm} KM",
                                                color = if (kmNum >= 0.0) Color(0xFFDC2626) else Color(0xFFB45309),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "PROSES_PART", "APROVEL_PART" -> {
                        val basePrediksi = if (selectedCategory == "PROSES_PART") prosesPartList else aprovelPartList
                        val filteredPrediksi = remember(basePrediksi, searchQuery) {
                            if (searchQuery.isBlank()) basePrediksi
                            else {
                                val q = searchQuery.trim().lowercase()
                                basePrediksi.filter {
                                    it.noMobil.lowercase().contains(q) ||
                                            it.namaPart.lowercase().contains(q) ||
                                            it.service.lowercase().contains(q) ||
                                            it.nipNama.lowercase().contains(q) ||
                                            it.status.lowercase().contains(q) ||
                                            it.aprovel.lowercase().contains(q) ||
                                            it.keterangan.lowercase().contains(q)
                                }
                            }
                        }

                        if (filteredPrediksi.isEmpty()) {
                            Text(
                                text = "Tidak ada data prediksi part yang sesuai.",
                                color = Slate600,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                filteredPrediksi.take(15).forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onVehicleClick(item.noMobil) }
                                        ) {
                                            Surface(
                                                color = Color(0xFFFEF3C7),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DirectionsCar,
                                                        contentDescription = null,
                                                        tint = Color(0xFFB45309),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "#${item.noMobil}",
                                                        color = Color(0xFFB45309),
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.namaPart.ifBlank { item.service.ifBlank { "Part" } },
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Blue900
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "Service: ${item.service.ifBlank { "-" }}",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (item.estimasiKm.isNotBlank()) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Est: ${item.estimasiKm} KM",
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                color = Color(0xFFDC2626),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 10.5.sp
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        val badgeText = if (selectedCategory == "PROSES_PART") {
                                            item.status.ifBlank { "PROSES" }
                                        } else {
                                            item.aprovel.ifBlank { "Menunggu" }
                                        }
                                        val badgeBg = if (selectedCategory == "PROSES_PART") Color(0xFFFEF3C7) else Color(0xFFFFE4E6)
                                        val badgeColor = if (selectedCategory == "PROSES_PART") Color(0xFFB45309) else Color(0xFFE11D48)

                                        Surface(
                                            color = badgeBg,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = badgeText,
                                                color = badgeColor,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
    } else if (!hasAnyAccess) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tidak ada kolom monitoring yang dapat diakses untuk akun Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
}

/**
 * Komponen Kartu Ringkasan LAPORAN KENDARAAN (LAPORAN HARIAN)
 */
@Composable
private fun MonitoringLaporanCard(
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onNavigateToLaporan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEEF2FF) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 2.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF4F46E5) else Color(0xFFCBD5E1)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "LAPORAN KENDARAAN",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Blue900,
                                fontSize = 13.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Surface(
                            color = Color(0xFFEEF2FF),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                        ) {
                            Text(
                                text = "$count Laporan (Belum Selesai)",
                                color = Color(0xFF4F46E5),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Button(
                    onClick = onNavigateToLaporan,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Open",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Komponen Kolom Kartu Metrik Monitoring Individual
 */
@Composable
private fun MonitoringMetricCard(
    title: String,
    badgeText: String,
    count: Int,
    description: String,
    accentColor: Color,
    accentLightColor: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentLightColor.copy(alpha = 0.5f) else Color(0xFFF8FAFC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 0.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else Color(0xFFE2E8F0)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = accentLightColor,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = badgeText,
                    color = accentColor,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                    ),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Blue900,
                    letterSpacing = 0.2.sp,
                    fontSize = 12.5.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        fontSize = 22.sp
                    )
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "Unit",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate600,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate600,
                    fontSize = 9.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
