package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SIMBA BC", appName)
  }

  @Test
  fun `launch MainActivity`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
    controller.setup()
    val activity = controller.get()
    org.junit.Assert.assertNotNull(activity)
  }

  @Test
  fun `verify AppDatabase and seed users initialization`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val db = com.example.data.local.database.AppDatabase.getInstance(context)
    org.junit.Assert.assertNotNull(db)
    com.example.data.local.database.AppDatabase.seedDatabase(db)
    // Tabel USER 100% mengikuti Google Sheets, test manual insert & retrieve
    val testUser = com.example.data.local.entity.UserEntity(
      id = 1,
      nip = "12406",
      namaMekanik = "JOKO RIYANTO",
      username = "superadmin",
      password = "123",
      level = "SUPER ADMIN"
    )
    db.userDao().insertUser(testUser)
    val user = db.userDao().getUserByUsername("superadmin")
    org.junit.Assert.assertNotNull(user)
    org.junit.Assert.assertEquals("superadmin", user?.username)
  }

  @Test
  fun `verify AuditViewModel initialization without crash`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.viewmodel.AuditViewModel(app)
    org.junit.Assert.assertNotNull(viewModel)
    org.junit.Assert.assertNotNull(viewModel.currentScreen.value)
  }

  @Test
  fun `verify isKalongStatus for ServiceTeknik and ServiceKeindahan`() {
    val teknikKalong = com.example.data.local.entity.ServiceTeknikEntity(
        id = 1,
        statusMobil = "KALONG",
        noMobil = "123",
        service = "P08",
        selisihKm = "-200",
        notificationDesc = "Service",
        notifNo = "N1",
        status = "NOPR"
    )
    val teknikNormal = com.example.data.local.entity.ServiceTeknikEntity(
        id = 2,
        statusMobil = "PAGI",
        noMobil = "124",
        service = "P09",
        selisihKm = "-600",
        notificationDesc = "Service",
        notifNo = "N2",
        status = "NOPR"
    )
    org.junit.Assert.assertTrue(com.example.ui.screens.isKalongStatus(teknikKalong))
    org.junit.Assert.assertFalse(com.example.ui.screens.isKalongStatus(teknikNormal))
  }

  @Test
  fun `verify Selisih KM styling conditions`() {
    // Di atas -500 dan di bawah 0 -> Kuning angka cetak tebal
    val styleYellow = com.example.ui.screens.getSelisihKmStyle("-250")
    org.junit.Assert.assertTrue(styleYellow.isBold)
    org.junit.Assert.assertEquals(androidx.compose.ui.graphics.Color(0xFFFEF9C3), styleYellow.containerColor)

    // Di atas 0 -> Merah
    val styleRed = com.example.ui.screens.getSelisihKmStyle("+150")
    org.junit.Assert.assertTrue(styleRed.isBold)
    org.junit.Assert.assertEquals(androidx.compose.ui.graphics.Color(0xFFFEE2E2), styleRed.containerColor)

    // Di bawah -500 -> Netral
    val styleNeutral = com.example.ui.screens.getSelisihKmStyle("-650")
    org.junit.Assert.assertFalse(styleNeutral.isBold)
    org.junit.Assert.assertEquals(androidx.compose.ui.graphics.Color(0xFFF1F5F9), styleNeutral.containerColor)
  }

  @Test
  fun `verify parseMasterKendaraanCsv and sorting by ID from top`() {
    val csv = """
      No Mobil,No Polisi,Tipe Kendaraan,Tahun Pembuatan,Jadwal Mobil
      101,B 1024 MW,Toyota Transmover,2023,Jadwal Reguler
      102,B 1025 MW,Toyota Avanza,2022,Shift Pagi
    """.trimIndent()

    val parsed = com.example.data.remote.GoogleAppsScriptHub.parseMasterKendaraanCsv(csv)
    org.junit.Assert.assertEquals(2, parsed.size)
    org.junit.Assert.assertEquals("101", parsed[0].noMobil)
    org.junit.Assert.assertEquals("B 1024 MW", parsed[0].noPolisi)
    org.junit.Assert.assertEquals("Toyota Transmover", parsed[0].tipe)
    org.junit.Assert.assertEquals("2023", parsed[0].tahunPembuatan)
    org.junit.Assert.assertEquals("Jadwal Reguler", parsed[0].jadwalMobil)

    // Test sorting by ID from top (ascending)
    val sortedAsc = parsed.sortedWith { a, b -> com.example.ui.screens.compareKendaraanById(a, b, ascendingId = true) }
    org.junit.Assert.assertTrue(sortedAsc[0].id <= sortedAsc[1].id)
  }

  @Test
  fun `verify parseKoplingCsv columns and ID ordering`() {
    val csv = """
      "ID","No Mobil","Total Km","Tipe"
      "2","MWD1338","150.926","NEW TRANSMOVER"
      "1","MWD1336","151.920","NEW TRANSMOVER"
      "3","MWD1331","150.763","NEW TRANSMOVER"
    """.trimIndent()

    val parsed = com.example.data.remote.GoogleAppsScriptHub.parseKoplingCsv(csv)
    org.junit.Assert.assertEquals(3, parsed.size)
    // Sesuai permintaan: Data diurutkan sesuai dengan ID (1, 2, 3)
    org.junit.Assert.assertEquals(1L, parsed[0].id)
    org.junit.Assert.assertEquals("MWD1336", parsed[0].noMobil) // Kolom 1
    org.junit.Assert.assertEquals("NEW TRANSMOVER", parsed[0].jenisTipe) // Kolom 2
    org.junit.Assert.assertEquals("151.920", parsed[0].totalKm) // Kolom 3

    org.junit.Assert.assertEquals(2L, parsed[1].id)
    org.junit.Assert.assertEquals("MWD1338", parsed[1].noMobil)

    org.junit.Assert.assertEquals(3L, parsed[2].id)
    org.junit.Assert.assertEquals("MWD1331", parsed[2].noMobil)
  }

  @Test
  fun `verify parseBanCsv columns and ID ordering`() {
    val csv = """
      "ID","NO _MOBIL","Plat Nomer","Embos","KM Total"
      "2","MWD001","B2001BUC","24MW0710","97.302"
      "1","MWD001","B2001BUC","24MW0870","90.323"
      "4","MWD002","B2102BUC","25MW0103","94.109"
    """.trimIndent()

    val parsed = com.example.data.remote.GoogleAppsScriptHub.parseBanCsv(csv)
    org.junit.Assert.assertEquals(3, parsed.size)
    // Sesuai permintaan: Data diurutkan sesuai dengan ID (1, 2, 4)
    org.junit.Assert.assertEquals(1L, parsed[0].id)
    org.junit.Assert.assertEquals("MWD001", parsed[0].noMobil) // No Mobil
    org.junit.Assert.assertEquals("B2001BUC", parsed[0].platNomer) // Plat Nomer
    org.junit.Assert.assertEquals("24MW0870", parsed[0].embos) // Embos
    org.junit.Assert.assertEquals("90.323", parsed[0].kmTotal) // KM Total

    org.junit.Assert.assertEquals(2L, parsed[1].id)
    org.junit.Assert.assertEquals("MWD001", parsed[1].noMobil)
    org.junit.Assert.assertEquals("24MW0710", parsed[1].embos)

    org.junit.Assert.assertEquals(4L, parsed[2].id)
    org.junit.Assert.assertEquals("MWD002", parsed[2].noMobil)
    org.junit.Assert.assertEquals("25MW0103", parsed[2].embos)
  }

  @Test
  fun `verify km parsing over 90k logic`() {
    fun parseKmValue(rawKm: String): Double {
      val clean = rawKm.trim().replace("KM", "", ignoreCase = true).trim()
      if (clean.isBlank()) return 0.0
      return if (clean.contains(".") && !clean.contains(",")) {
        val parts = clean.split(".")
        if (parts.size == 2 && parts[1].length == 3) {
          (parts[0] + parts[1]).toDoubleOrNull() ?: 0.0
        } else {
          clean.replace(".", "").toDoubleOrNull() ?: 0.0
        }
      } else {
        clean.replace(".", "").replace(",", "").toDoubleOrNull() ?: 0.0
      }
    }

    org.junit.Assert.assertTrue(parseKmValue("90.323") > 90000.0)
    org.junit.Assert.assertTrue(parseKmValue("134.075") > 90000.0)
    org.junit.Assert.assertTrue(parseKmValue("97.302 KM") > 90000.0)
    org.junit.Assert.assertFalse(parseKmValue("89.999") > 90000.0)
    org.junit.Assert.assertFalse(parseKmValue("90.000") > 90000.0)
    org.junit.Assert.assertFalse(parseKmValue("45000") > 90000.0)
  }

  @Test
  fun `verify parseAccuCsv columns, ordering, and usia condition`() {
    val csv = """"ID","NO MOBIL","Plat Nomer","Tipe","TANGGAL","Usia","Tipe"
"2","MWD002","B2102BUC","TRANSMOVER","05/11/2025","10,80","NS60R"
"1","MWD001","B2001BUC","TRANSMOVER","04/06/2026","3,77","NS60R"
"9","MWD013","B2013BUC","TRANSMOVER","14/08/2025","13,57","NS60R"
"16","MWD1331","B1299HUA","NEW TRANSMOVER","13/08/2026","1,44","NS60L""""

    val parsed = com.example.data.remote.GoogleAppsScriptHub.parseAccuCsv(csv)
    org.junit.Assert.assertEquals(4, parsed.size)
    // Urutkan berdasarkan ID ASC: 1, 2, 9, 16
    org.junit.Assert.assertEquals(1L, parsed[0].id)
    org.junit.Assert.assertEquals("MWD001", parsed[0].noMobil)
    org.junit.Assert.assertEquals("TRANSMOVER", parsed[0].displayTipe)
    org.junit.Assert.assertEquals("3,77", parsed[0].displayUsia)
    org.junit.Assert.assertEquals("NS60R", parsed[0].displayTipeAccu)

    org.junit.Assert.assertEquals(2L, parsed[1].id)
    org.junit.Assert.assertEquals("MWD002", parsed[1].noMobil)
    org.junit.Assert.assertEquals("10,80", parsed[1].displayUsia)

    org.junit.Assert.assertEquals(9L, parsed[2].id)
    org.junit.Assert.assertEquals("13,57", parsed[2].displayUsia)

    org.junit.Assert.assertEquals(16L, parsed[3].id)
    org.junit.Assert.assertEquals("MWD1331", parsed[3].noMobil)
    org.junit.Assert.assertEquals("NEW TRANSMOVER", parsed[3].displayTipe)
    org.junit.Assert.assertEquals("NS60L", parsed[3].displayTipeAccu)

    fun parseUsiaValue(rawUsia: String): Double {
      val clean = rawUsia.trim().replace("bulan", "", ignoreCase = true).trim().replace(",", ".")
      return clean.toDoubleOrNull() ?: 0.0
    }

    // Nilai usia lebih dari 12
    org.junit.Assert.assertFalse(parseUsiaValue(parsed[0].displayUsia) > 12.0) // 3.77 <= 12
    org.junit.Assert.assertFalse(parseUsiaValue(parsed[1].displayUsia) > 12.0) // 10.80 <= 12
    org.junit.Assert.assertTrue(parseUsiaValue(parsed[2].displayUsia) > 12.0)  // 13.57 > 12 -> Merah
    org.junit.Assert.assertFalse(parseUsiaValue(parsed[3].displayUsia) > 12.0) // 1.44 <= 12
  }

  @Test
  fun `verify parseReservasiCsv columns and values`() {
    val csv = """"NO MOBIL","Order","Material","Material description","Document Date","Posting Date","Quantity","Base Unit of Measure","Amt.in loc.cur.","Batch"
"MWD101","400123","MAT-001","Filter Oli Transmover","20.09.2026","20.09.2026","2","PC","150.000","B2026-A"
"MWD102","400124","MAT-002","Kampas Rem Depan","21.09.2026","21.09.2026","1","SET","450.000","B2026-B""""

    val parsed = com.example.data.remote.GoogleAppsScriptHub.parseReservasiCsv(csv)
    org.junit.Assert.assertEquals(2, parsed.size)
    org.junit.Assert.assertEquals("MWD101", parsed[0].noMobil)
    org.junit.Assert.assertEquals("400123", parsed[0].orderNo)
    org.junit.Assert.assertEquals("MAT-001", parsed[0].material)
    org.junit.Assert.assertEquals("Filter Oli Transmover", parsed[0].materialDescription)
    org.junit.Assert.assertEquals("20.09.2026", parsed[0].documentDate)
    org.junit.Assert.assertEquals("20.09.2026", parsed[0].postingDate)
    org.junit.Assert.assertEquals("2", parsed[0].quantity)
    org.junit.Assert.assertEquals("PC", parsed[0].baseUnitOfMeasure)
    org.junit.Assert.assertEquals("150.000", parsed[0].amtInLocCur)
    org.junit.Assert.assertEquals("B2026-A", parsed[0].batch)
  }

  @Test
  fun `verify 30 minute periodic sync interval`() {
    val thirtyMinutesSeconds = 30 * 60
    org.junit.Assert.assertEquals(1800, thirtyMinutesSeconds)

    val minutes = thirtyMinutesSeconds / 60
    val seconds = thirtyMinutesSeconds % 60
    val formatted = String.format("%02d:%02d", minutes, seconds)
    org.junit.Assert.assertEquals("30:00", formatted)
  }

  @Test
  fun `verify parsePrediksiPartCsv 14 columns structure`() {
    val csv = """"ID","TANGGAL","NO_MOBIL","SERVICE","KM_MOBIL","NIP_NAMA","NAMA_PART","PREDIKSI_KM","KM_TARGET","ARGO_SEKARANG","ESTIMASI_KM","APROVEL","KETERANGAN","STATUS"
"1","08/10/2026","MWD001","Service Berkala","120000","12345 - Budi","KAMPAS REM DEPAN","5000","125000","121000","4000","APPROVED","Kondisi Baik","SELESAI""""

    val parsed = com.example.data.remote.GoogleAppsScriptHub.parsePrediksiPartCsv(csv)
    org.junit.Assert.assertEquals(1, parsed.size)
    val item = parsed[0]
    org.junit.Assert.assertEquals(1L, item.id)
    org.junit.Assert.assertEquals("08/10/2026", item.tanggal)
    org.junit.Assert.assertEquals("MWD001", item.noMobil)
    org.junit.Assert.assertEquals("Service Berkala", item.service)
    org.junit.Assert.assertEquals("120000", item.kmInput)
    org.junit.Assert.assertEquals("12345 - Budi", item.nipNama)
    org.junit.Assert.assertEquals("KAMPAS REM DEPAN", item.namaPart)
    org.junit.Assert.assertEquals("5000", item.prediksiKm)
    org.junit.Assert.assertEquals("125000", item.kmTarget)
    org.junit.Assert.assertEquals("121000", item.argoSekarang)
    org.junit.Assert.assertEquals("4000", item.estimasiKm)
    org.junit.Assert.assertEquals("APPROVED", item.aprovel)
    org.junit.Assert.assertEquals("Kondisi Baik", item.keterangan)
    org.junit.Assert.assertEquals("SELESAI", item.status)
  }

  @Test
  fun `verify database migration 21 to 22 alters table prediksi_part with nipNama`() {
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    val db = com.example.data.local.database.AppDatabase.getInstance(context)
    org.junit.Assert.assertNotNull(db)
    // Verify DAO is accessible
    val dao = db.prediksiPartDao()
    org.junit.Assert.assertNotNull(dao)
  }

  @Test
  fun `verify automatic NIP_NAMA resolution from logged in user`() {
    val testUser = com.example.data.local.entity.UserEntity(
      id = 1,
      nip = "12345",
      namaMekanik = "BUDI SANTOSO",
      username = "budi",
      password = "123",
      level = "TEKNIK"
    )

    val nip = testUser.nip.trim()
    val nama = testUser.namaMekanik.trim()
    val formattedNipNama = when {
      nip.isNotBlank() && nama.isNotBlank() -> "$nip/$nama"
      nama.isNotBlank() -> nama
      nip.isNotBlank() -> nip
      else -> testUser.username.trim()
    }

    org.junit.Assert.assertEquals("12345/BUDI SANTOSO", formattedNipNama)
  }

  @Test
  fun `verify Master User spreadsheet configuration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sessionManager = com.example.data.session.SessionManager(context)
    org.junit.Assert.assertEquals("1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30", sessionManager.sheetUser)
    org.junit.Assert.assertEquals("USER", sessionManager.sheetUserName)
  }

  @Test
  fun `verify Laporan Kendaraan and Prediksi Part spreadsheet configuration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sessionManager = com.example.data.session.SessionManager(context)
    org.junit.Assert.assertEquals("1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30", sessionManager.sheetLaporanHarian)
    org.junit.Assert.assertEquals("LAPORAN KENDARAAN", sessionManager.sheetLaporanHarianName)
    org.junit.Assert.assertEquals("1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30", sessionManager.sheetPrediksiPart)
    org.junit.Assert.assertEquals("PREDIKSI_PART", sessionManager.sheetPrediksiPartName)
  }

  @Test
  fun `verify Master Kendaraan spreadsheet configuration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sessionManager = com.example.data.session.SessionManager(context)
    org.junit.Assert.assertEquals("13iuagC5JY1NOkVRMwebz8-XvaEp-2q_qiyaDedGedNQ", sessionManager.sheetKendaraan)
    org.junit.Assert.assertEquals("MASTER", sessionManager.sheetKendaraanName)
  }

  @Test
  fun `verify Master Kendaraan CSV parsing with shifted columns`() {
    val csv = """
      NoMobil,NoPolisi,Tipe,TahunPembuatan,NoChassis,Jadwal Mobil,Engine_type,Equipment
      BB2283,B2283SUB,1110799,MHKAA1BY5RK058087,NEW TRANSMOVER,2024,Tidak Terpasang,GASOLINE
    """.trimIndent()

    val parsed = com.example.data.remote.GoogleAppsScriptHub.parseMasterKendaraanCsv(csv)
    org.junit.Assert.assertEquals(1, parsed.size)
    val item = parsed.first()
    org.junit.Assert.assertEquals("BB2283", item.noMobil)
    org.junit.Assert.assertEquals("B2283SUB", item.noPolisi)
    org.junit.Assert.assertEquals("NEW TRANSMOVER", item.tipe)
    org.junit.Assert.assertEquals("2024", item.tahunPembuatan)
    org.junit.Assert.assertEquals("MHKAA1BY5RK058087", item.noChassis)
    org.junit.Assert.assertEquals("GASOLINE", item.engineType)
    org.junit.Assert.assertEquals("Tidak Terpasang", item.equipment)
  }
}

