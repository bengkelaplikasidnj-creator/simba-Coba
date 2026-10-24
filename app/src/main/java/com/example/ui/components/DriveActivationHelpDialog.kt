package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.remote.GoogleAppsScriptHub

private val Blue900 = Color(0xFF0F172A)
private val HighDensityBlue = Color(0xFF1E40AF)
private val Slate600 = Color(0xFF475569)

@Composable
fun DriveActivationHelpDialog(
    errorMessage: String = "",
    onDismiss: () -> Unit,
    onTestAgain: (() -> Unit)? = null,
    isTesting: Boolean = false
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFFED7AA)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Aktivasi Izin Google Drive", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Blue900))
                            Text("Penyebab & Solusi Gagal Upload (1 Menit)", style = MaterialTheme.typography.bodySmall.copy(color = Slate600))
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Slate600)
                    }
                }

                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Kenapa muncul pesan gagal upload ke Google Drive?\nGoogle Apps Script membutuhkan otorisasi izin DriveApp (akses Google Drive) yang harus disetujui satu kali oleh pemilik akun Google di browser.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF92400E), fontSize = 12.sp)
                        )
                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Detail pesan: $errorMessage",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFB45309), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                Text("4 LANGKAH MUDAH MENGAKTIFKAN IZIN:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Blue900)

                StepItem(
                    num = "1",
                    title = "Buka Google Apps Script Editor",
                    desc = "Buka script.google.com di browser dan buka project skrip Anda."
                )

                StepItem(
                    num = "2",
                    title = "Pilih Fungsi 'aktivasiIzinGoogleDrive'",
                    desc = "Pada dropdown menu fungsi di bagian atas (sebelah tombol Jalankan ▶), pilih: aktivasiIzinGoogleDrive"
                )

                StepItem(
                    num = "3",
                    title = "Klik Run (▶) & Setujui Izin",
                    desc = "Akan muncul 'Authorization Required' > klik 'Review permissions' > pilih Akun Google Anda > klik 'Advanced' (Lanjutan) > klik 'Go to ... (unsafe)' > klik 'Allow' (Izinkan)."
                )

                StepItem(
                    num = "4",
                    title = "Deploy Versi Baru (Wajib)",
                    desc = "Klik tombol biru 'Deploy' (Terapkan) di kanan atas > 'Manage deployments' > klik ikon Pensil (Edit) > Version: 'New version' (Versi Baru) > klik 'Deploy'."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("function_name", "aktivasiIzinGoogleDrive"))
                            Toast.makeText(context, "Nama fungsi 'aktivasiIzinGoogleDrive' disalin!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin Fungsi", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("google_script_code", GoogleAppsScriptHub.getGoogleScriptCode("1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs")))
                            Toast.makeText(context, "Seluruh kode skrip Code.gs berhasil disalin!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin Code.gs", fontSize = 11.sp)
                    }
                }

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://script.google.com"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Tidak dapat membuka browser", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka script.google.com di Browser")
                }

                if (onTestAgain != null) {
                    Button(
                        onClick = onTestAgain,
                        enabled = !isTesting,
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sedang Menguji...")
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tes Ulang Izin Drive Sekarang")
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Tutup", color = Slate600)
                }
            }
        }
    }
}

@Composable
private fun StepItem(num: String, title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = HighDensityBlue,
            shape = CircleShape,
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(num, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Blue900)
            Text(desc, fontSize = 11.sp, color = Slate600)
        }
    }
}
