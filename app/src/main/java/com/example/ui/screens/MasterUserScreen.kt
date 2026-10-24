package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.Blue50
import com.example.ui.theme.Blue700
import com.example.ui.theme.Blue900
import com.example.ui.theme.HighDensityBlue
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800

/**
 * Menu USER BENGKEL
 * Tabel dari ID: 1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30 dengan nama sheet USER
 * Susunan Tabel: | NIP | NAMA_MEKANIK | USERNAME | PASSWORD | LEVELAKSES |
 * Mendukung koreksi (edit) maupun update data ke database lokal dan Google Sheets.
 */
@Composable
fun MasterUserScreen(
    users: List<UserEntity>,
    currentUser: UserEntity? = null,
    onSaveUser: (user: UserEntity, originalNip: String, originalUsername: String) -> Unit,
    onDeleteUser: (Int) -> Unit,
    onSyncClick: () -> Unit = {},
    onCheckRemoteChanges: suspend () -> Unit = {},
    onInitializeUsers: () -> Unit = {}
) {
    // Data user selalu otomatis mengikuti Google Sheets saat layar USER BENGKEL dibuka
    // dan memantau setiap ada perubahan di Google Sheets secara real-time
    LaunchedEffect(Unit) {
        onSyncClick()
        while (true) {
            kotlinx.coroutines.delay(5000)
            onCheckRemoteChanges()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserEntity?>(null) }

    // Dialog state (NIP | NAMA_MEKANIK | USERNAME | PASSWORD | LEVEL AKSES)
    var nip by remember { mutableStateOf("") }
    var namaMekanik by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf("TEKNIK") }
    var levelExpanded by remember { mutableStateOf(false) }

    // Level Akses Roles:
    val roleList = listOf(
        "SUPER ADMIN",
        "ADMIN",
        "ALL",
        "KEPALA BENGKEL",
        "GH",
        "SH",
        "TEKNIK",
        "KEINDAHAN",
        "CAT"
    )

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users else {
            val q = searchQuery.trim().lowercase()
            users.filter {
                it.nip.lowercase().contains(q) ||
                        it.namaMekanik.lowercase().contains(q) ||
                        it.username.lowercase().contains(q) ||
                        it.level.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ==========================================
        // Top Bar Card: Header & Action Controls
        // ==========================================
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
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MASTER USER",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Blue900,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Blue50,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${filteredUsers.size} User",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Blue700
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Tombol Update / Sinkronisasi Data dari Google Sheets
                        Button(
                            onClick = onSyncClick,
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Update & Sinkron", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("UPDATE DATA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Tambah User Baru
                    Button(
                        onClick = {
                            editingUser = null
                            nip = ""
                            namaMekanik = ""
                            username = ""
                            password = ""
                            passwordVisible = false
                            level = "TEKNIK"
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TAMBAH USER", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari NIP, Nama Mekanik, Username, Level Akses...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = HighDensityBlue)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada user terdaftar.", style = MaterialTheme.typography.bodyMedium.copy(color = Slate600))
            }
        } else {
            // ==========================================
            // TAMPILAN TABEL GOOGLE SHEETS (Sheet: USER):
            // | NIP | NAMA_MEKANIK | USERNAMA | PASSWORD | LEVELAKSES |
            // ==========================================
            Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val horizontalScrollState = rememberScrollState()
                        val minTotalWidth = 760.dp
                        val useFullWidth = maxWidth > minTotalWidth

                        // Definisi Lebar Kolom: NIP | NAMA_MEKANIK | USERNAMA | PASSWORD | LEVELAKSES
                        val colNipWidth = if (useFullWidth) maxWidth * 0.14f else 110.dp
                        val colNamaWidth = if (useFullWidth) maxWidth * 0.30f else 220.dp
                        val colUsernameWidth = if (useFullWidth) maxWidth * 0.18f else 140.dp
                        val colPasswordWidth = if (useFullWidth) maxWidth * 0.18f else 140.dp
                        val colLevelWidth = if (useFullWidth) maxWidth * 0.20f else 160.dp
                        val totalTableWidth = colNipWidth + colNamaWidth + colUsernameWidth + colPasswordWidth + colLevelWidth

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Kontainer Tabel Horizontal Scroll
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .horizontalScroll(horizontalScrollState)
                            ) {
                                Column(modifier = Modifier.width(totalTableWidth)) {
                                    // ------------------------------------
                                    // Header Tabel: NIP | NAMA_MEKANIK | USERNAMA | PASSWORD | LEVELAKSES
                                    // ------------------------------------
                                    Surface(
                                        color = HighDensityBlue,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            UserTableHeadCell(title = "NIP", width = colNipWidth, align = Alignment.Center)
                                            UserTableDivider()
                                            UserTableHeadCell(title = "NAMA_MEKANIK", width = colNamaWidth, align = Alignment.CenterStart)
                                            UserTableDivider()
                                            UserTableHeadCell(title = "USERNAMA", width = colUsernameWidth, align = Alignment.CenterStart)
                                            UserTableDivider()
                                            UserTableHeadCell(title = "PASSWORD", width = colPasswordWidth, align = Alignment.Center)
                                            UserTableDivider()
                                            UserTableHeadCell(title = "LEVEL AKSES", width = colLevelWidth, align = Alignment.Center)
                                        }
                                    }

                                    // ------------------------------------
                                    // Baris Data Tabel: NIP | NAMA_MEKANIK | USERNAMA | PASSWORD | LEVELAKSES
                                    // ------------------------------------
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        itemsIndexed(filteredUsers, key = { _, item -> item.id }) { index, user ->
                                            val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(rowBg)
                                                    .height(50.dp)
                                                    .clickable {
                                                        // Klik baris untuk langsung koreksi / hapus
                                                        editingUser = user
                                                        nip = user.nip
                                                        namaMekanik = user.namaMekanik
                                                        username = user.username
                                                        password = user.password
                                                        passwordVisible = false
                                                        level = user.level.ifBlank { "TEKNIK" }
                                                        showDialog = true
                                                    },
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // 1. NIP
                                                Box(
                                                    modifier = Modifier
                                                        .width(colNipWidth)
                                                        .fillMaxHeight()
                                                        .padding(horizontal = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = user.nip,
                                                        fontWeight = FontWeight.Bold,
                                                        color = HighDensityBlue,
                                                        fontSize = 12.sp,
                                                        maxLines = 1
                                                    )
                                                }
                                                UserTableCellDivider()

                                                // 2. NAMA_MEKANIK
                                                Box(
                                                    modifier = Modifier
                                                        .width(colNamaWidth)
                                                        .fillMaxHeight()
                                                        .padding(horizontal = 10.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Text(
                                                        text = user.namaMekanik,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Blue900,
                                                            fontSize = 12.sp
                                                        ),
                                                        maxLines = 1
                                                    )
                                                }
                                                UserTableCellDivider()

                                                // 3. USERNAMA
                                                Box(
                                                    modifier = Modifier
                                                        .width(colUsernameWidth)
                                                        .fillMaxHeight()
                                                        .padding(horizontal = 10.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    Text(
                                                        text = user.username,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = HighDensityBlue,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        ),
                                                        maxLines = 1
                                                    )
                                                }
                                                UserTableCellDivider()

                                                // 4. PASSWORD
                                                Box(
                                                    modifier = Modifier
                                                        .width(colPasswordWidth)
                                                        .fillMaxHeight()
                                                        .padding(horizontal = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val displayPass = if (user.password.isBlank()) "" else "••••••"
                                                    Text(
                                                        text = displayPass,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Normal,
                                                            color = Slate800,
                                                            fontSize = 12.sp
                                                        ),
                                                        maxLines = 1
                                                    )
                                                }
                                                UserTableCellDivider()

                                                // 5. LEVELAKSES
                                                Box(
                                                    modifier = Modifier
                                                        .width(colLevelWidth)
                                                        .fillMaxHeight()
                                                        .padding(horizontal = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (user.level.isNotBlank()) {
                                                        UserRoleBadge(level = user.level)
                                                    } else {
                                                        Text(
                                                            text = "",
                                                            fontSize = 12.sp,
                                                            color = Slate600
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
    }

    // ==========================================
    // MODAL DIALOG: KOREKSI MAUPUN UPDATE DATA USER
    // Susunan Form: | NIP | NAMA_MEKANIK | USERNAMA | PASSWORD | LEVELAKSES |
    // ==========================================
    if (showDialog) {
        val nowTs = remember { System.currentTimeMillis() }
        val newIdFromTs = remember { (nowTs % 10000000).toInt() }
        val activeId = editingUser?.id ?: newIdFromTs

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (editingUser == null) Blue50 else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (editingUser == null) "INPUT BARU" else "KOREKSI DATA",
                            color = if (editingUser == null) HighDensityBlue else Color(0xFFB45309),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (editingUser == null) "Tambah Master User" else "Koreksi User (${editingUser?.namaMekanik?.ifBlank { editingUser?.nip ?: "" }})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {


                    // 1. NIP
                    OutlinedTextField(
                        value = nip,
                        onValueChange = { nip = it },
                        label = { Text("NIP") },
                        placeholder = { Text("Contoh: 12406") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 2. NAMA_MEKANIK
                    OutlinedTextField(
                        value = namaMekanik,
                        onValueChange = { namaMekanik = it },
                        label = { Text("NAMA_MEKANIK") },
                        placeholder = { Text("Contoh: JOKO RIYANTO") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 3. USERNAMA
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("USERNAMA") },
                        placeholder = { Text("Contoh: 12406") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 4. PASSWORD
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("PASSWORD") },
                        placeholder = { Text("Masukkan kata sandi") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.testTag("toggle_password_visibility")
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Sembunyikan password" else "Lihat password",
                                    tint = if (passwordVisible) HighDensityBlue else Slate400
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_password_input")
                    )

                    // 5. LEVEL AKSES
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = level,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("LEVEL AKSES") },
                            trailingIcon = {
                                IconButton(onClick = { levelExpanded = !levelExpanded }) {
                                    Icon(
                                        imageVector = if (levelExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Pilih Role",
                                        tint = HighDensityBlue
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { levelExpanded = true }
                        )
                        DropdownMenu(
                            expanded = levelExpanded,
                            onDismissRequest = { levelExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.75f)
                        ) {
                            roleList.forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (level == role) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = HighDensityBlue, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = role,
                                                fontWeight = if (level == role) FontWeight.Bold else FontWeight.Normal,
                                                color = if (level == role) HighDensityBlue else Slate800
                                            )
                                        }
                                    },
                                    onClick = {
                                        level = role
                                        levelExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nip.isNotBlank() || namaMekanik.isNotBlank() || username.isNotBlank()) {
                            onSaveUser(
                                UserEntity(
                                    id = editingUser?.id ?: activeId,
                                    nip = nip.trim(),
                                    namaMekanik = namaMekanik.trim(),
                                    username = username.trim(),
                                    password = password.trim(),
                                    level = level.trim().uppercase()
                                ),
                                editingUser?.nip ?: "",
                                editingUser?.username ?: ""
                            )
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue)
                ) {
                    Text(if (editingUser == null) "Simpan User" else "Koreksi & Update")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (editingUser != null) {
                        OutlinedButton(
                            onClick = {
                                editingUser?.let { onDeleteUser(it.id) }
                                showDialog = false
                            },
                            border = BorderStroke(1.dp, Rose600),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose600)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus")
                        }
                    }
                    TextButton(onClick = { showDialog = false }) {
                        Text("Batal")
                    }
                }
            }
        )
    }
}

@Composable
private fun UserTableHeadCell(title: String, width: Dp, align: Alignment) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(horizontal = 8.dp),
        contentAlignment = align
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 11.5.sp,
                letterSpacing = 0.5.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun UserTableDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(Color(0xFF1E3A8A))
    )
}

@Composable
private fun UserTableCellDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(Color(0xFFE2E8F0))
    )
}

@Composable
private fun UserRoleBadge(level: String) {
    val role = level.trim().uppercase()
    val (bgColor, textColor) = when (role) {
        "SUPER ADMIN" -> Pair(Color(0xFFEEF2FF), Color(0xFF4338CA))
        "ADMIN" -> Pair(Color(0xFFE0E7FF), Color(0xFF3730A3))
        "ALL" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        "ADMIN,ALL", "ADMIN, ALL" -> Pair(Color(0xFFEDE9FE), Color(0xFF6D28D9))
        "KEPALA BENGKEL" -> Pair(Blue50, HighDensityBlue)
        "GH" -> Pair(Color(0xFFF0FDF4), Color(0xFF15803D))
        "SH" -> Pair(Color(0xFFECFEFF), Color(0xFF0E7490))
        "TEKNIK" -> Pair(Color(0xFFF0F9FF), Color(0xFF0369A1))
        "KEINDAHAN" -> Pair(Color(0xFFFAF5FF), Color(0xFF7E22CE))
        "CAT" -> Pair(Color(0xFFFFF1F2), Color(0xFFBE123C))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF334155))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, textColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = role,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 10.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
