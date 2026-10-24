package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.HighDensityBlueLight
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.AppScreen

@Composable
fun AppDrawerContent(
    currentScreen: AppScreen,
    currentUser: UserEntity?,
    lastSyncTime: String = "",
    onNavigate: (AppScreen) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ROLES: SUPER ADMIN | ADMIN | ALL | KEPALA BENGKEL | GH | SH | TEKNIK | KEINDAHAN | CAT
    val level = currentUser?.level?.trim()?.uppercase().orEmpty()

    val isSuperAdmin = currentUser != null && (
        level == "SUPER ADMIN" ||
        level == "SUPERADMIN" ||
        level.contains("SUPER ADMIN") ||
        currentUser.username.trim().equals("superadmin", ignoreCase = true)
    )

    // Pembagian Akses Menu Sesuai Aturan Sistem:
    // 1. MONITORING: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , TEKNIK , KEINDAHAN , CAT
    val showMonitoring = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "KEINDAHAN", "CAT", "ADMIN,ALL", "ADMIN, ALL")

    // 2. LAPORAN KENDARAAN: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , TEKNIK , KEINDAHAN , CAT
    val showLaporanKendaraan = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "KEINDAHAN", "CAT", "ADMIN,ALL", "ADMIN, ALL")

    // 3. PREDIKSI PART: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , TEKNIK
    val showPrediksiPart = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "ADMIN,ALL", "ADMIN, ALL")

    // 4. JADWAL SERVICE TEKNIK: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , TEKNIK
    val showServiceTeknik = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "ADMIN,ALL", "ADMIN, ALL")

    // 5. RIWAYAT SERVICE TEKNIK: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , TEKNIK
    val showPengerjaanTeknik = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "ADMIN,ALL", "ADMIN, ALL")

    // 6. JADWAL SERVICE KEINDAHAN: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , KEINDAHAN
    val showServiceKeindahan = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "KEINDAHAN", "ADMIN,ALL", "ADMIN, ALL")

    // 7. RIWAYAT SERVICE KEINDAHAN: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , KEINDAHAN
    val showPekerjaanServiceKeindahan = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "KEINDAHAN", "ADMIN,ALL", "ADMIN, ALL")

    // 8. RIWAYAT SERVICE SKI: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , KEINDAHAN
    val showRiwayatSki = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "KEINDAHAN", "ADMIN,ALL", "ADMIN, ALL")

    // 11. BAN: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , TEKNIK
    val showBan = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "ADMIN,ALL", "ADMIN, ALL")

    // 12. KOPLING: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , TEKNIK
    val showKopling = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "ADMIN,ALL", "ADMIN, ALL")

    // 13. BATERAY: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , TEKNIK
    val showBateray = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "ADMIN,ALL", "ADMIN, ALL")

    // 14. RESERVASI: HANYA SUPER ADMIN
    val showReservasi = isSuperAdmin

    val showKomponenSection = showBan || showKopling || showBateray || showReservasi

    // 15. MASTER KENDARAAN: SUPER ADMIN , ADMIN , ALL , KEPALA BENGKEL , GH , SH , TEKNIK , KEINDAHAN , CAT
    val showMasterKendaraan = isSuperAdmin || level in listOf("ADMIN", "ALL", "KEPALA BENGKEL", "GH", "SH", "TEKNIK", "KEINDAHAN", "CAT", "ADMIN,ALL", "ADMIN, ALL")

    // 16. ARGO: SUPER ADMIN , ADMIN
    val showArgo = isSuperAdmin || level in listOf("ADMIN")

    // 17. MASTER USER: SUPER ADMIN , ADMIN
    val showMasterUser = isSuperAdmin || level in listOf("ADMIN")

    // 18. GOOGLE SHEETS & SYNC: SUPER ADMIN
    val showGoogleSheetsSync = isSuperAdmin

    val showMasterSection = showMasterKendaraan || showArgo || showMasterUser || showGoogleSheetsSync

    Surface(
        color = Color.White,
        modifier = modifier
            .fillMaxHeight()
            .width(290.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Sidebar Header (Blue dominated)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HighDensityBlue)
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.bluebird_taxi_icon_1788513014229),
                                contentDescription = "Logo Bengkel",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SIMBA BC",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.2).sp
                                )
                            )
                            Text(
                                text = "Sistem Informasi & Monitoring Bengkel Armada",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            )
                        }
                    }

                    if (currentUser != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color.White.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = currentUser.namaMekanik,
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Role: ${currentUser.level} • NIP: ${currentUser.nip}",
                                        color = Color.White.copy(alpha = 0.85f),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sync Status Indicator Banner
            Surface(
                color = Blue50,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = Blue700,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sinkron Google Sheets: $lastSyncTime",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Blue700
                        )
                    )
                }
            }

            // MENU SECTION 1: MONITORING
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                MenuSectionHeader(title = "MONITORING UTAMA")

                if (showMonitoring) {
                    DrawerMenuItem(
                        icon = Icons.Default.Speed,
                        label = "MONITORING",
                        isSelected = currentScreen == AppScreen.MONITORING,
                        onClick = { onNavigate(AppScreen.MONITORING) }
                    )
                }

                if (showLaporanKendaraan) {
                    DrawerMenuItem(
                        icon = Icons.Default.Assignment,
                        label = "LAPORAN KENDARAAN",
                        isSelected = currentScreen == AppScreen.LAPORAN_HARIAN,
                        onClick = { onNavigate(AppScreen.LAPORAN_HARIAN) }
                    )
                }

                if (showPrediksiPart) {
                    DrawerMenuItem(
                        icon = Icons.Default.PrecisionManufacturing,
                        label = "PREDIKSI PART",
                        isSelected = currentScreen == AppScreen.PREDIKSI_PART,
                        onClick = { onNavigate(AppScreen.PREDIKSI_PART) }
                    )
                }

                if (showServiceTeknik) {
                    DrawerMenuItem(
                        icon = Icons.Default.Build,
                        label = "JADWAL SERVICE TEKNIK",
                        isSelected = currentScreen == AppScreen.SERVICE_TEKNIK,
                        onClick = { onNavigate(AppScreen.SERVICE_TEKNIK) }
                    )
                }

                if (showPengerjaanTeknik) {
                    DrawerMenuItem(
                        icon = Icons.Default.Engineering,
                        label = "RIWAYAT SERVICE TEKNIK",
                        isSelected = currentScreen == AppScreen.PENGERJAAN_TEKNIK,
                        onClick = { onNavigate(AppScreen.PENGERJAAN_TEKNIK) }
                    )
                }

                if (showServiceKeindahan) {
                    DrawerMenuItem(
                        icon = Icons.Default.AutoAwesome,
                        label = "JADWAL SERVICE KEINDAHAN",
                        isSelected = currentScreen == AppScreen.SERVICE_KEINDAHAN,
                        onClick = { onNavigate(AppScreen.SERVICE_KEINDAHAN) }
                    )
                }

                if (showPekerjaanServiceKeindahan) {
                    DrawerMenuItem(
                        icon = Icons.Default.CleaningServices,
                        label = "RIWAYAT SERVICE KEINDAHAN",
                        isSelected = currentScreen == AppScreen.PEKERJAAN_SERVICE_KEINDAHAN,
                        onClick = { onNavigate(AppScreen.PEKERJAAN_SERVICE_KEINDAHAN) }
                    )
                }

                if (showRiwayatSki) {
                    DrawerMenuItem(
                        icon = Icons.Default.ReceiptLong,
                        label = "RIWAYAT SERVICE SKI",
                        isSelected = currentScreen == AppScreen.RIWAYAT_SKI,
                        onClick = { onNavigate(AppScreen.RIWAYAT_SKI) }
                    )
                }

                // MENU SECTION 2: KOMPONEN ARMADA (BAN, KOPLING, BATERAY, RESERVASI)
                if (showKomponenSection) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp), color = Color(0xFFE2E8F0))
                    MenuSectionHeader(title = "KOMPONEN ARMADA")

                    if (showBan) {
                        DrawerMenuItem(
                            icon = Icons.Default.TripOrigin,
                            label = "BAN",
                            isSelected = currentScreen == AppScreen.BAN,
                            onClick = { onNavigate(AppScreen.BAN) }
                        )
                    }

                    if (showKopling) {
                        DrawerMenuItem(
                            icon = Icons.Default.Tune,
                            label = "KOPLING",
                            isSelected = currentScreen == AppScreen.KOPLING,
                            onClick = { onNavigate(AppScreen.KOPLING) }
                        )
                    }

                    if (showBateray) {
                        DrawerMenuItem(
                            icon = Icons.Default.BatteryChargingFull,
                            label = "BATERAY",
                            isSelected = currentScreen == AppScreen.BATERAY,
                            onClick = { onNavigate(AppScreen.BATERAY) }
                        )
                    }

                    if (showReservasi) {
                        DrawerMenuItem(
                            icon = Icons.Default.Bookmark,
                            label = "RESERVASI",
                            isSelected = currentScreen == AppScreen.RESERVASI,
                            onClick = { onNavigate(AppScreen.RESERVASI) }
                        )
                    }
                }

                // MENU SECTION 3: MASTER & SISTEM
                if (showMasterSection) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp), color = Color(0xFFE2E8F0))
                    MenuSectionHeader(title = "MASTER & SISTEM")

                    if (showMasterKendaraan) {
                        DrawerMenuItem(
                            icon = Icons.Default.DirectionsCar,
                            label = "MASTER KENDARAAN",
                            isSelected = currentScreen == AppScreen.MASTER_KENDARAAN,
                            onClick = { onNavigate(AppScreen.MASTER_KENDARAAN) }
                        )
                    }

                    if (showArgo) {
                        DrawerMenuItem(
                            icon = Icons.Default.LocalAtm,
                            label = "ARGO",
                            isSelected = currentScreen == AppScreen.ARGO,
                            onClick = { onNavigate(AppScreen.ARGO) }
                        )
                    }

                    if (showMasterUser) {
                        DrawerMenuItem(
                            icon = Icons.Default.ManageAccounts,
                            label = "MASTER USER",
                            isSelected = currentScreen == AppScreen.MASTER_USER,
                            onClick = { onNavigate(AppScreen.MASTER_USER) }
                        )
                    }

                    if (showGoogleSheetsSync) {
                        DrawerMenuItem(
                            icon = Icons.Default.CloudSync,
                            label = "GOOGLE SHEETS & SYNC",
                            isSelected = currentScreen == AppScreen.GOOGLE_SCRIPT_CONFIG,
                            onClick = { onNavigate(AppScreen.GOOGLE_SCRIPT_CONFIG) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer with Logout
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Surface(
                    color = Color(0xFFFFF1F2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogout() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Keluar",
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Keluar Akun",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE11D48)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            color = Slate400,
            fontSize = 11.sp
        ),
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp)
    )
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) HighDensityBlueLight else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) HighDensityBlue else Slate600,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) HighDensityBlue else Slate800,
                    fontSize = 13.sp
                )
            )
        }
    }
}
