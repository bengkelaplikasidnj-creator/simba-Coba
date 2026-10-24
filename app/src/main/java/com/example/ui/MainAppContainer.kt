package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.MasterKendaraanEntity
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.AppTopBar
import com.example.ui.screens.ArgoScreen
import com.example.ui.screens.BanScreen
import com.example.ui.screens.BaterayScreen
import com.example.ui.screens.GoogleScriptSetupScreen
import com.example.ui.screens.KomponenArmadaScreen
import com.example.ui.screens.KoplingScreen
import com.example.ui.screens.LaporanHarianScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MasterKendaraanScreen
import com.example.ui.screens.MasterUserScreen
import com.example.ui.screens.MonitoringScreen
import com.example.ui.screens.PekerjaanServiceKeindahanScreen
import com.example.ui.screens.PengerjaanTeknikScreen
import com.example.ui.screens.PrediksiPartScreen
import com.example.ui.screens.ReservasiScreen
import com.example.ui.screens.RiwayatSkiScreen
import com.example.ui.screens.ServiceKeindahanScreen
import com.example.ui.screens.ServiceTeknikScreen
import com.example.ui.theme.Blue200
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBg
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AuditViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun MainAppContainer(
    viewModel: AuditViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val unsyncedKeindahanCount by viewModel.unsyncedKeindahanCount.collectAsState()

    // Data lists from ViewModel
    val laporanList by viewModel.allLaporan.collectAsState()
    val teknikList by viewModel.allServiceTeknik.collectAsState()
    val keindahanList by viewModel.allServiceKeindahan.collectAsState()
    val pekerjaanKeindahanList by viewModel.allPekerjaanServiceKeindahan.collectAsState()
    val banList by viewModel.allBan.collectAsState()
    val koplingList by viewModel.allKopling.collectAsState()
    val baterayList by viewModel.allBateray.collectAsState()
    val reservasiList by viewModel.allReservasi.collectAsState()
    val argoList by viewModel.allArgo.collectAsState()
    val prediksiPartList by viewModel.allPrediksiPart.collectAsState()
    val pengerjaanTeknikList by viewModel.allPengerjaanTeknik.collectAsState()
    val riwayatSkiList by viewModel.allRiwayatSki.collectAsState()
    val kendaraanList by viewModel.allKendaraan.collectAsState()
    val userList by viewModel.allUsers.collectAsState()

    // Target Order & Detail navigation states
    val targetTeknikOrder by viewModel.targetTeknikOrder.collectAsState()
    val targetTeknikItem by viewModel.targetTeknikItem.collectAsState()
    val targetSkiOrder by viewModel.targetSkiOrder.collectAsState()
    val targetSkiItem by viewModel.targetSkiItem.collectAsState()
    val orderChoiceState by viewModel.orderNavigationChoice.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Quick vehicle popup state when NO_MOBIL is clicked in any screen
    var selectedVehicleNumber by remember { mutableStateOf<String?>(null) }
    // Warning confirmation dialog before logging out and clearing session
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (currentScreen == AppScreen.LOGIN || currentUser == null) {
        LoginScreen(
            onDownloadUsers = {
                viewModel.downloadUserToPhone()
            },
            onLogin = { user, pass, callback ->
                viewModel.login(user, pass, callback)
            }
        )
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 720.dp

            val title = when (currentScreen) {
                AppScreen.MONITORING -> "Monitoring Bengkel"
                AppScreen.LAPORAN_HARIAN -> "Laporan Kendaraan"
                AppScreen.PREDIKSI_PART -> "Prediksi Part"
                AppScreen.SERVICE_TEKNIK -> "Jadwal Service Teknik"
                AppScreen.PENGERJAAN_TEKNIK -> "Riwayat Service Teknik"
                AppScreen.SERVICE_KEINDAHAN -> "Jadwal Service Keindahan"
                AppScreen.PEKERJAAN_SERVICE_KEINDAHAN -> "Riwayat Service Keindahan"
                AppScreen.RIWAYAT_SKI -> "Riwayat Service SKI"
                AppScreen.BAN -> "Data Ban Bengkel"
                AppScreen.KOPLING -> "Data Kopling Bengkel"
                AppScreen.BATERAY -> "Data Baterai / Accu Bengkel"
                AppScreen.RESERVASI -> "Data Reservasi Bengkel"
                AppScreen.MASTER_KENDARAAN -> "Master Kendaraan"
                AppScreen.ARGO -> "Data ARGO Bengkel"
                AppScreen.MASTER_USER -> "Master User"
                AppScreen.GOOGLE_SCRIPT_CONFIG -> "Pengaturan Google Sheets"
                else -> "SIMBA BC"
            }

            if (isExpandedScreen) {
                // Expanded screen layout with left drawer
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(HighDensityBg)
                ) {
                    AppDrawerContent(
                        currentScreen = currentScreen,
                        currentUser = currentUser,
                        lastSyncTime = lastSyncTime,
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onLogout = { showLogoutConfirmDialog = true }
                    )

                    Scaffold(
                        topBar = {
                            AppTopBar(
                                title = title,
                                currentUser = currentUser,
                                isSyncing = isSyncing,
                                isOnline = isOnline,
                                lastSyncTime = lastSyncTime,
                                onMenuClick = { /* Sidebar always open on wide screens */ },
                                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) },
                                onProfileClick = { showLogoutConfirmDialog = true }
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        containerColor = HighDensityBg
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            ScreenRouter(
                                currentScreen = currentScreen,
                                viewModel = viewModel,
                                currentUser = currentUser,
                                laporanList = laporanList,
                                teknikList = teknikList,
                                keindahanList = keindahanList,
                                pekerjaanKeindahanList = pekerjaanKeindahanList,
                                banList = banList,
                                koplingList = koplingList,
                                baterayList = baterayList,
                                reservasiList = reservasiList,
                                argoList = argoList,
                                prediksiPartList = prediksiPartList,
                                pengerjaanTeknikList = pengerjaanTeknikList,
                                riwayatSkiList = riwayatSkiList,
                                kendaraanList = kendaraanList,
                                userList = userList,
                                targetTeknikOrder = targetTeknikOrder,
                                targetTeknikItem = targetTeknikItem,
                                targetSkiOrder = targetSkiOrder,
                                targetSkiItem = targetSkiItem,
                                onVehicleClick = { noMobil -> selectedVehicleNumber = noMobil },
                                onOrderClick = { orderNo -> viewModel.openOrderByOrderNo(orderNo) },
                                onBackToHome = { viewModel.navigateTo(AppScreen.MONITORING) }
                            )
                        }
                    }
                }
            } else {
                // Mobile layout with drawer
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        AppDrawerContent(
                            currentScreen = currentScreen,
                            currentUser = currentUser,
                            lastSyncTime = lastSyncTime,
                            onNavigate = { screen ->
                                viewModel.navigateTo(screen)
                                coroutineScope.launch { drawerState.close() }
                            },
                            onLogout = {
                                coroutineScope.launch { drawerState.close() }
                                showLogoutConfirmDialog = true
                            }
                        )
                    }
                ) {
                    Scaffold(
                        topBar = {
                            AppTopBar(
                                title = title,
                                currentUser = currentUser,
                                isSyncing = isSyncing,
                                isOnline = isOnline,
                                lastSyncTime = lastSyncTime,
                                onMenuClick = {
                                    coroutineScope.launch { drawerState.open() }
                                },
                                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) },
                                onProfileClick = { showLogoutConfirmDialog = true }
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        containerColor = HighDensityBg
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            ScreenRouter(
                                currentScreen = currentScreen,
                                viewModel = viewModel,
                                currentUser = currentUser,
                                laporanList = laporanList,
                                teknikList = teknikList,
                                keindahanList = keindahanList,
                                pekerjaanKeindahanList = pekerjaanKeindahanList,
                                banList = banList,
                                koplingList = koplingList,
                                baterayList = baterayList,
                                reservasiList = reservasiList,
                                argoList = argoList,
                                prediksiPartList = prediksiPartList,
                                pengerjaanTeknikList = pengerjaanTeknikList,
                                riwayatSkiList = riwayatSkiList,
                                kendaraanList = kendaraanList,
                                userList = userList,
                                targetTeknikOrder = targetTeknikOrder,
                                targetTeknikItem = targetTeknikItem,
                                targetSkiOrder = targetSkiOrder,
                                targetSkiItem = targetSkiItem,
                                onVehicleClick = { noMobil -> selectedVehicleNumber = noMobil },
                                onOrderClick = { orderNo -> viewModel.openOrderByOrderNo(orderNo) },
                                onBackToHome = { viewModel.navigateTo(AppScreen.MONITORING) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Vehicle Quick View Dialog when clicking "NO_MOBIL"
    if (selectedVehicleNumber != null) {
        val targetNo = selectedVehicleNumber!!
        val vehicle = kendaraanList.find { com.example.ui.screens.isVehicleMatch(it.noMobil, targetNo) }

        if (vehicle != null) {
            com.example.ui.screens.RincianKendaraanDialog(
                vehicle = vehicle,
                baterayList = baterayList,
                banList = banList,
                koplingList = koplingList,
                reservasiList = reservasiList,
                pengerjaanTeknikList = pengerjaanTeknikList,
                riwayatSkiList = riwayatSkiList,
                argoList = argoList,
                onDismiss = { selectedVehicleNumber = null },
                onEditClick = {
                    selectedVehicleNumber = null
                    viewModel.navigateTo(AppScreen.MASTER_KENDARAAN)
                },
                onOrderClick = { orderNo ->
                    selectedVehicleNumber = null
                    viewModel.openOrderByOrderNo(orderNo)
                }
            )
        } else {
            val fallbackVehicle = MasterKendaraanEntity(
                id = 0,
                noMobil = targetNo.trim().removePrefix("#"),
                noPolisi = "-",
                tipe = "Armada Bengkel",
                tahunPembuatan = "-",
                jadwalMobil = "Jadwal Reguler"
            )
            com.example.ui.screens.RincianKendaraanDialog(
                vehicle = fallbackVehicle,
                baterayList = baterayList,
                banList = banList,
                koplingList = koplingList,
                reservasiList = reservasiList,
                pengerjaanTeknikList = pengerjaanTeknikList,
                riwayatSkiList = riwayatSkiList,
                argoList = argoList,
                onDismiss = { selectedVehicleNumber = null },
                onEditClick = {
                    selectedVehicleNumber = null
                    viewModel.navigateTo(AppScreen.MASTER_KENDARAAN)
                },
                onOrderClick = { orderNo ->
                    selectedVehicleNumber = null
                    viewModel.openOrderByOrderNo(orderNo)
                }
            )
        }
    }

    // Modal dialog pemilihan tujuan pengerjaan saat order tercatat di keduanya atau membutuhkan konfirmasi
    if (orderChoiceState != null) {
        val choice = orderChoiceState!!
        AlertDialog(
            onDismissRequest = { viewModel.clearOrderChoice() },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Blue50,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = HighDensityBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Order #${choice.orderNo}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                        )
                        Text(
                            text = "Pilih Menu Pengerjaan",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.5.sp)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (choice.teknikItem != null) {
                            "Nomor Order #${choice.orderNo} tercatat di Riwayat Service Teknik. Silakan pilih rincian yang ingin dibuka:"
                        } else {
                            "Nomor Order #${choice.orderNo} belum ditemukan di data lokal saat ini. Buka dan cari langsung di menu:"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate800, fontSize = 12.5.sp)
                    )

                    // Option 1: Riwayat Service Teknik (SKD)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Blue50),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Blue200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.navigateToTeknikWithOrder(choice.orderNo, choice.teknikItem)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Riwayat Service Teknik (SKD)", fontWeight = FontWeight.Bold, color = HighDensityBlue, fontSize = 13.5.sp)
                                val sub = if (choice.teknikItem != null) {
                                    "${choice.teknikItem.service.ifBlank { "Service Teknik" }} • ${choice.teknikItem.description.ifBlank { "Mobil #${choice.teknikItem.noMobil}" }}"
                                } else "Buka data teknik dengan filter Order #${choice.orderNo}"
                                Text(sub, color = Slate600, fontSize = 11.sp, maxLines = 1)
                            }
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Option 2: Riwayat Service SKI
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.navigateToSkiWithOrder(choice.orderNo, choice.skiItem)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Riwayat Service SKI", fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8), fontSize = 13.5.sp)
                                val sub = if (choice.skiItem != null) {
                                    "${choice.skiItem.service.ifBlank { "Service Keindahan" }} • ${choice.skiItem.description.ifBlank { "Mobil #${choice.skiItem.noMobil}" }}"
                                } else "Buka data riwayat SKI dengan filter Order #${choice.orderNo}"
                                Text(sub, color = Slate600, fontSize = 11.sp, maxLines = 1)
                            }
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.clearOrderChoice() }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Dialog Peringatan Konfirmasi Keluar Akun & Clear Session Login
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE4E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Konfirmasi Keluar Akun",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontSize = 18.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Apakah Anda yakin ingin keluar dari akun ini?",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155),
                            textAlign = TextAlign.Center
                        )
                    )

                    val user = currentUser
                    if (user != null) {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = user.namaMekanik,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    )
                                    Text(
                                        text = "NIP: ${user.nip.ifBlank { "-" }}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF64748B)
                                        )
                                    )
                                }
                                Surface(
                                    color = HighDensityBlue,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = user.level,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 9.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE11D48),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ya, Keluar Akun",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutConfirmDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.testTag("cancel_logout_button")
                ) {
                    Text(
                        text = "Batal",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Slate600))
        Text(value.ifBlank { "-" }, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Slate800))
    }
}

@Composable
private fun ScreenRouter(
    currentScreen: AppScreen,
    viewModel: AuditViewModel,
    currentUser: com.example.data.local.entity.UserEntity?,
    laporanList: List<com.example.data.local.entity.LaporanHarianEntity>,
    teknikList: List<com.example.data.local.entity.ServiceTeknikEntity>,
    keindahanList: List<com.example.data.local.entity.ServiceKeindahanEntity>,
    pekerjaanKeindahanList: List<com.example.data.local.entity.PekerjaanServiceKeindahanEntity> = emptyList(),
    banList: List<com.example.data.local.entity.BanEntity>,
    koplingList: List<com.example.data.local.entity.KoplingEntity>,
    baterayList: List<com.example.data.local.entity.BaterayEntity>,
    reservasiList: List<com.example.data.local.entity.ReservasiEntity>,
    argoList: List<com.example.data.local.entity.ArgoEntity> = emptyList(),
    prediksiPartList: List<com.example.data.local.entity.PrediksiPartEntity> = emptyList(),
    pengerjaanTeknikList: List<com.example.data.local.entity.PengerjaanTeknikEntity>,
    riwayatSkiList: List<com.example.data.local.entity.RiwayatSkiEntity> = emptyList(),
    kendaraanList: List<com.example.data.local.entity.MasterKendaraanEntity>,
    userList: List<com.example.data.local.entity.UserEntity>,
    targetTeknikOrder: String? = null,
    targetTeknikItem: com.example.data.local.entity.PengerjaanTeknikEntity? = null,
    targetSkiOrder: String? = null,
    targetSkiItem: com.example.data.local.entity.RiwayatSkiEntity? = null,
    onVehicleClick: (String) -> Unit,
    onOrderClick: (String) -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    when (currentScreen) {
        AppScreen.MONITORING -> {
            MonitoringScreen(
                teknikList = teknikList,
                laporanList = laporanList,
                keindahanList = keindahanList,
                kendaraanList = kendaraanList,
                riwayatSkiList = riwayatSkiList,
                prediksiPartList = prediksiPartList,
                argoList = argoList,
                currentUser = currentUser,
                onVehicleClick = onVehicleClick,
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) },
                onNavigateToSki = { viewModel.navigateTo(AppScreen.RIWAYAT_SKI) },
                onNavigateToPrediksiPart = { viewModel.navigateTo(AppScreen.PREDIKSI_PART) },
                onNavigateToLaporan = { viewModel.navigateTo(AppScreen.LAPORAN_HARIAN) },
                onNavigateToServiceTeknik = { viewModel.navigateTo(AppScreen.SERVICE_TEKNIK) },
                onNavigateToServiceKeindahan = { viewModel.navigateTo(AppScreen.SERVICE_KEINDAHAN) }
            )
        }

        AppScreen.LAPORAN_HARIAN -> {
            LaporanHarianScreen(
                laporanList = laporanList,
                kendaraanList = kendaraanList,
                userList = userList,
                currentUser = currentUser,
                onSaveLaporan = { viewModel.saveLaporan(it) },
                onDeleteLaporan = { viewModel.deleteLaporan(it) },
                onVehicleClick = { /* Popup pesan laporan mobil dihilangkan */ },
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) }
            )
        }

        AppScreen.PREDIKSI_PART -> {
            PrediksiPartScreen(
                prediksiPartList = prediksiPartList,
                kendaraanList = kendaraanList,
                baterayList = baterayList,
                banList = banList,
                koplingList = koplingList,
                reservasiList = reservasiList,
                pengerjaanTeknikList = pengerjaanTeknikList,
                riwayatSkiList = riwayatSkiList,
                argoList = argoList,
                currentUser = currentUser,
                onSavePrediksiPart = { viewModel.savePrediksiPart(it) },
                onDeletePrediksiPart = { viewModel.deletePrediksiPart(it) },
                onVehicleClick = onVehicleClick,
                onOrderClick = onOrderClick,
                onEditMasterKendaraan = {
                    viewModel.navigateTo(AppScreen.MASTER_KENDARAAN)
                },
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) }
            )
        }

        AppScreen.SERVICE_TEKNIK -> {
            ServiceTeknikScreen(
                serviceList = teknikList,
                kendaraanList = kendaraanList,
                argoList = argoList,
                prediksiPartList = prediksiPartList,
                currentUser = currentUser,
                onSaveService = { viewModel.saveServiceTeknik(it) },
                onDeleteService = { viewModel.deleteServiceTeknik(it) },
                onSavePrediksiPart = { viewModel.savePrediksiPart(it) },
                onNavigateToPrediksiPart = { viewModel.navigateTo(AppScreen.PREDIKSI_PART) },
                onVehicleClick = onVehicleClick,
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) }
            )
        }

        AppScreen.PENGERJAAN_TEKNIK -> {
            PengerjaanTeknikScreen(
                pengerjaanTeknikList = pengerjaanTeknikList,
                reservasiList = reservasiList,
                kendaraanList = kendaraanList,
                initialOrderNo = targetTeknikOrder,
                initialDetailItem = targetTeknikItem,
                onClearInitialDetail = { viewModel.clearTargetTeknik() },
                onVehicleClick = onVehicleClick,
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) },
                onBack = onBackToHome
            )
        }

        AppScreen.SERVICE_KEINDAHAN -> {
            val keindahanUploadStatus by viewModel.keindahanUploadStatus.collectAsState()
            ServiceKeindahanScreen(
                serviceList = keindahanList,
                kendaraanList = kendaraanList,
                uploadStatusMessage = keindahanUploadStatus,
                onSaveService = { viewModel.saveServiceKeindahan(it) },
                onDeleteService = { viewModel.deleteServiceKeindahan(it) },
                onVehicleClick = onVehicleClick,
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) },
                onKerjakan = { noMobil, jenisService, notifNo ->
                    viewModel.navigateToPekerjaanKeindahanWithPrefill(noMobil, jenisService, notifNo)
                }
            )
        }

        AppScreen.PEKERJAAN_SERVICE_KEINDAHAN -> {
            val targetPrefill by viewModel.targetKeindahanPrefill.collectAsState()
            PekerjaanServiceKeindahanScreen(
                items = pekerjaanKeindahanList,
                kendaraanList = kendaraanList,
                currentUser = currentUser,
                isSyncing = viewModel.isSyncing.collectAsState().value,
                isOnline = viewModel.isOnline.collectAsState().value,
                unsyncedCount = viewModel.unsyncedKeindahanCount.collectAsState().value,
                onSyncUnsyncedClick = { viewModel.syncUnsyncedInspections() },
                initialPrefillData = targetPrefill,
                onClearPrefillData = { viewModel.clearTargetKeindahanPrefill() },
                uploadStatusMessage = viewModel.keindahanUploadStatus.collectAsState().value,
                onSavePekerjaan = { item, callback ->
                    viewModel.savePekerjaanServiceKeindahan(item, callback)
                },
                onDeletePekerjaan = { id, callback ->
                    viewModel.deletePekerjaanServiceKeindahan(id, callback)
                },
                onSyncClick = { viewModel.syncPekerjaanServiceKeindahan() },
                onInisialisasiClick = { callback ->
                    viewModel.inisialisasiPengerjaanKeindahan(callback)
                },
                onTestDrivePermission = { callback ->
                    viewModel.testDrivePermission(callback)
                },
                onUpdateScriptUrl = { newUrl ->
                    viewModel.updateScriptUrl(newUrl)
                },
                currentScriptUrl = viewModel.getCurrentScriptUrl(),
                onUploadPhotoToDrive = { path, fileName, callback ->
                    viewModel.uploadPhotoToDrive(context, path, fileName, isHighRes4K = true, onResult = callback)
                },
                onVehicleClick = onVehicleClick
            )
        }

        AppScreen.RIWAYAT_SKI -> {
            RiwayatSkiScreen(
                riwayatSkiList = riwayatSkiList,
                reservasiList = reservasiList,
                kendaraanList = kendaraanList,
                initialOrderNo = targetSkiOrder,
                initialDetailItem = targetSkiItem,
                onClearInitialDetail = { viewModel.clearTargetSki() },
                onSaveRiwayatSki = { viewModel.saveRiwayatSki(it) },
                onDeleteRiwayatSki = { viewModel.deleteRiwayatSki(it) },
                onVehicleClick = onVehicleClick,
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) },
                onBack = onBackToHome
            )
        }

        AppScreen.BAN -> {
            BanScreen(
                banList = banList,
                kendaraanList = kendaraanList,
                onVehicleClick = onVehicleClick,
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) }
            )
        }

        AppScreen.KOPLING -> {
            KoplingScreen(
                koplingList = koplingList,
                kendaraanList = kendaraanList,
                onSaveKopling = { viewModel.saveKopling(it) },
                onDeleteKopling = { viewModel.deleteKopling(it) },
                onVehicleClick = onVehicleClick,
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) }
            )
        }

        AppScreen.BATERAY -> {
            BaterayScreen(
                baterayList = baterayList,
                kendaraanList = kendaraanList,
                onVehicleClick = onVehicleClick,
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) }
            )
        }

        AppScreen.RESERVASI -> {
            val userLevel = currentUser?.level?.trim()?.uppercase().orEmpty()
            val isSuperAdmin = currentUser != null && (
                userLevel == "SUPER ADMIN" ||
                userLevel == "SUPERADMIN" ||
                userLevel.contains("SUPER ADMIN") ||
                currentUser.username.trim().equals("superadmin", ignoreCase = true)
            )
            if (isSuperAdmin) {
                ReservasiScreen(
                    reservasiList = reservasiList,
                    kendaraanList = kendaraanList,
                    pengerjaanTeknikList = pengerjaanTeknikList,
                    onSaveReservasi = { viewModel.saveReservasi(it) },
                    onDeleteReservasi = { viewModel.deleteReservasi(it) },
                    onClearOldData = { viewModel.clearAllReservasiAndSync() },
                    onClearDataOnly = { viewModel.clearAllReservasiAndKomponenPendukung() },
                    onVehicleClick = onVehicleClick,
                    onOrderClick = onOrderClick,
                    onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) }
                )
            } else {
                LaunchedEffect(Unit) {
                    viewModel.navigateTo(AppScreen.MONITORING)
                }
            }
        }

        AppScreen.MASTER_KENDARAAN -> {
            MasterKendaraanScreen(
                kendaraanList = kendaraanList,
                baterayList = baterayList,
                banList = banList,
                koplingList = koplingList,
                reservasiList = reservasiList,
                pengerjaanTeknikList = pengerjaanTeknikList,
                argoList = argoList,
                onSaveKendaraan = { viewModel.saveKendaraan(it) },
                onDeleteKendaraan = { viewModel.deleteKendaraan(it) },
                onOrderClick = onOrderClick,
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) }
            )
        }

        AppScreen.ARGO -> {
            ArgoScreen(
                argoList = argoList,
                kendaraanList = kendaraanList,
                baterayList = baterayList,
                banList = banList,
                koplingList = koplingList,
                reservasiList = reservasiList,
                pengerjaanTeknikList = pengerjaanTeknikList,
                riwayatSkiList = riwayatSkiList,
                onSaveArgo = { viewModel.saveArgo(it) },
                onDeleteArgo = { viewModel.deleteArgo(it) },
                onVehicleClick = onVehicleClick,
                onOrderClick = onOrderClick,
                onEditMasterKendaraan = {
                    viewModel.navigateTo(AppScreen.MASTER_KENDARAAN)
                },
                onSyncClick = { viewModel.syncFromGoogleSheets(isManual = true) }
            )
        }

        AppScreen.MASTER_USER -> {
            MasterUserScreen(
                users = userList,
                currentUser = currentUser,
                onSaveUser = { user, origNip, origUsername ->
                    viewModel.saveUser(user, origNip, origUsername)
                },
                onDeleteUser = { viewModel.deleteUser(it) },
                onSyncClick = { viewModel.syncUserFromGoogleSheets() },
                onCheckRemoteChanges = { viewModel.checkAndAutoSyncUserSheet() },
                onInitializeUsers = { viewModel.initializeDefaultUsers() }
            )
        }

        AppScreen.GOOGLE_SCRIPT_CONFIG -> {
            val sessionManager = viewModel.repository.sessionManager
            GoogleScriptSetupScreen(
                currentLaporanId = sessionManager.sheetLaporanHarian,
                currentLaporanSheetName = sessionManager.sheetLaporanHarianName,
                currentTeknikId = sessionManager.sheetServiceTeknik,
                currentTeknikSheetName = sessionManager.sheetServiceTeknikName,
                currentKeindahanId = sessionManager.sheetServiceKeindahan,
                currentKeindahanSheetName = sessionManager.sheetServiceKeindahanName,
                currentBanId = sessionManager.sheetBan,
                currentBanSheetName = sessionManager.sheetBanName,
                currentKoplingId = sessionManager.sheetKopling,
                currentKoplingSheetName = sessionManager.sheetKoplingName,
                currentBaterayId = sessionManager.sheetBateray,
                currentBateraySheetName = sessionManager.sheetBaterayName,
                currentArgoId = sessionManager.sheetArgo,
                currentArgoSheetName = sessionManager.sheetArgoName,
                currentReservasiId = sessionManager.sheetReservasi,
                currentReservasiSheetName = sessionManager.sheetReservasiName,
                currentPengerjaanTeknikId = sessionManager.sheetPengerjaanTeknik,
                currentPengerjaanTeknikSheetName = sessionManager.sheetPengerjaanTeknikName,
                currentPekerjaanKeindahanId = sessionManager.sheetPekerjaanServiceKeindahan,
                currentPekerjaanKeindahanSheetName = sessionManager.sheetPekerjaanServiceKeindahanName,
                currentRiwayatSkiId = sessionManager.sheetRiwayatSki,
                currentRiwayatSkiSheetName = sessionManager.sheetRiwayatSkiName,
                currentUserId = sessionManager.sheetUser,
                currentKendaraanId = sessionManager.sheetKendaraan,
                currentKendaraanSheetName = sessionManager.sheetKendaraanName,
                currentScriptUrl = sessionManager.scriptUrl,
                currentFolderPhotoKeindahanId = sessionManager.folderPhotoKeindahanId,
                onSaveSettings = { laporanId, laporanSheetName, teknikId, teknikSheetName, keindahanId, keindahanSheetName, banId, banSheetName, koplingId, koplingSheetName, baterayId, bateraySheetName, argoId, argoSheetName, reservasiId, reservasiSheetName, pengerjaanTeknikId, pengerjaanTeknikSheetName, pekerjaanKeindahanId, pekerjaanKeindahanSheetName, riwayatSkiId, riwayatSkiSheetName, userId, kendaraanId, kendaraanSheetName, scriptUrl, folderPhotoKeindahanId ->
                    viewModel.saveGoogleSheetsSettings(
                        laporanId, laporanSheetName, teknikId, teknikSheetName, keindahanId, keindahanSheetName, banId, banSheetName, koplingId, koplingSheetName, baterayId, bateraySheetName, argoId, argoSheetName, reservasiId, reservasiSheetName, pengerjaanTeknikId, pengerjaanTeknikSheetName, pekerjaanKeindahanId, pekerjaanKeindahanSheetName, riwayatSkiId, riwayatSkiSheetName, userId, kendaraanId, kendaraanSheetName, scriptUrl, folderPhotoKeindahanId
                    )
                },
                onSyncNow = {
                    viewModel.syncFromGoogleSheets(isManual = true)
                },
                onTestScriptUrl = { url, callback ->
                    viewModel.testGoogleScriptUrl(url, callback)
                },
                onTestDrivePermission = { url, callback ->
                    viewModel.testGoogleScriptDrivePermission(url, callback)
                },
                onTestUploadSamplePhoto = { callback ->
                    viewModel.testUploadSamplePhotoToDrive(context, callback)
                }
            )
        }

        else -> {}
    }
}
