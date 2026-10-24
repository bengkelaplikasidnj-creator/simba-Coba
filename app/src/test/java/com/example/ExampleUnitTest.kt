package com.example

import com.example.data.remote.GoogleAppsScriptHub
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testParsePengerjaanTeknikCsv() {
    val sampleCsv = """
      "Tanggal","Order","No Mobil","Description","SERVICE","Km Service"
      "2024-05-10","1002345","B1234XYZ","Ganti oli mesin dan filter","SERVICE REGULER","45000"
      "2024-05-11","1002346","B5678ABC","Tune up dan perbaikan rem","SERVICE BERKALA","60000"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parsePengerjaanTeknikCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals("2024-05-10", parsed[0].tanggal)
    assertEquals("1002345", parsed[0].orderNo)
    assertEquals("B1234XYZ", parsed[0].noMobil)
    assertEquals("Ganti oli mesin dan filter", parsed[0].description)
    assertEquals("SERVICE REGULER", parsed[0].service)
    assertEquals("45000", parsed[0].kmService)
  }

  @Test
  fun testParseRiwayatSkiCsv() {
    val sampleCsv = """
      "Tanggal","Order","No Mobil","Description","SERVICE","Km Service"
      "2026-09-29","ORD-901","MWD105","Cuci dan poles bodi","CUCI BESAR","38000"
      "2026-09-30","ORD-902","MWD106","Pembersihan jok interior","SALON INTERIOR","42000"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parseRiwayatSkiCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals("2026-09-29", parsed[0].tanggal)
    assertEquals("ORD-901", parsed[0].orderNo)
    assertEquals("MWD105", parsed[0].noMobil)
    assertEquals("Cuci dan poles bodi", parsed[0].description)
    assertEquals("CUCI BESAR", parsed[0].service)
    assertEquals("38000", parsed[0].kmService)
  }

  @Test
  fun testIsVehicleMatch() {
    org.junit.Assert.assertTrue(com.example.ui.screens.isVehicleMatch("101", "101"))
    org.junit.Assert.assertTrue(com.example.ui.screens.isVehicleMatch("#101", "101"))
    org.junit.Assert.assertTrue(com.example.ui.screens.isVehicleMatch("0101", "101"))
    org.junit.Assert.assertTrue(com.example.ui.screens.isVehicleMatch("B 1024 MW", "b 1024 mw"))
    org.junit.Assert.assertFalse(com.example.ui.screens.isVehicleMatch("101", "102"))
  }

  @Test
  fun testBaterayUsiaThreshold() {
    org.junit.Assert.assertTrue(com.example.ui.screens.parseUsiaValue("13") > 12.0)
    org.junit.Assert.assertTrue(com.example.ui.screens.parseUsiaValue("12,5") > 12.0)
    org.junit.Assert.assertTrue(com.example.ui.screens.parseUsiaValue("14 Bulan") > 12.0)
    org.junit.Assert.assertFalse(com.example.ui.screens.parseUsiaValue("12") > 12.0)
    org.junit.Assert.assertFalse(com.example.ui.screens.parseUsiaValue("10 Bulan") > 12.0)
  }

  @Test
  fun testKmThreshold() {
    org.junit.Assert.assertTrue(com.example.ui.screens.parseKmValue("90.323") > 90000.0)
    org.junit.Assert.assertTrue(com.example.ui.screens.parseKmValue("134075") > 90000.0)
    org.junit.Assert.assertTrue(com.example.ui.screens.parseKmValue("95.000 KM") > 90000.0)
    org.junit.Assert.assertFalse(com.example.ui.screens.parseKmValue("90.000") > 90000.0)
    org.junit.Assert.assertFalse(com.example.ui.screens.parseKmValue("45000") > 90000.0)
  }

  @Test
  fun testParseLaporanHarianCsvWithIdColumn() {
    val sampleCsv = """
      "ID","Timestamp","NO MOBIL","NAMA/NIP","KELUHAN KENDARAAN","KODE AKSES","TOKEN","STATUS","UPDATE DATA","POOL/VENDOR","NAMA MEKANIK","PENGERJAAN"
      "10","2026-09-28 08:30:00","MWD101","1001 - Budi","Rem bunyi dan AC kurang dingin","KA-01","TK-99","Belum Dikerjakan","2026-09-28 09:00:00","Pool Pusat MW","1005 - Budi Santoso","Cek kampas rem"
      "11","2026-09-28 09:15:00","MWD102","1002 - Andi","Kopling berat","KA-02","TK-100","Sedang di kerjakan","2026-09-28 09:30:00","Pool Pusat MW","1005 - Budi Santoso","Setel kopling"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parseLaporanHarianCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals(10L, parsed[0].id)
    assertEquals("2026-09-28 08:30:00", parsed[0].timestamp)
    assertEquals("MWD101", parsed[0].noMobil)
    assertEquals("1001 - Budi", parsed[0].namaNip)
    assertEquals("Rem bunyi dan AC kurang dingin", parsed[0].keluhanKendaraan)
    assertEquals("KA-01", parsed[0].kodeAkses)
    assertEquals("TK-99", parsed[0].token)
    assertEquals("Belum Dikerjakan", parsed[0].status)
    assertEquals("2026-09-28 09:00:00", parsed[0].updateData)
    assertEquals("Pool Pusat MW", parsed[0].poolVendor)
    assertEquals("1005 - Budi Santoso", parsed[0].namaMekanik)
    assertEquals("Cek kampas rem", parsed[0].pengerjaan)

    assertEquals(11L, parsed[1].id)
    assertEquals("MWD102", parsed[1].noMobil)
    assertEquals("Sedang di kerjakan", parsed[1].status)
  }

  @Test
  fun testParsePrediksiPartCsvWithNewColumns() {
    val sampleCsv = """
      "ID","TANGGAL","NO_MOBIL","SERVICE","KM_INPUT","NAMA_PART","PREDIKSI_KM","KM_TARGET","ARGO_SEKARANG","ESTIMASI_KM","APROVEL","KETERANGAN","STATUS"
      "1","2026-10-08","MW101","SERVICE BERKALA","50000","KAMPAS REM DEPAN","15000","65000","62000","3000","APPROVED","AMAN","PROSES"
      "2","2026-10-08","MW102","GANTI OLI","80000","OLI MESIN","10000","90000","91500","-1500","PENDING","LEWAT KM","OVERDUE"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parsePrediksiPartCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals(1L, parsed[0].id)
    assertEquals("2026-10-08", parsed[0].tanggal)
    assertEquals("MW101", parsed[0].noMobil)
    assertEquals("SERVICE BERKALA", parsed[0].service)
    assertEquals("50000", parsed[0].kmInput)
    assertEquals("KAMPAS REM DEPAN", parsed[0].namaPart)
    assertEquals("15000", parsed[0].prediksiKm)
    assertEquals("65000", parsed[0].kmTarget)
    assertEquals("62000", parsed[0].argoSekarang)
    assertEquals("3000", parsed[0].estimasiKm)
    assertEquals("APPROVED", parsed[0].aprovel)
    assertEquals("AMAN", parsed[0].keterangan)
    assertEquals("PROSES", parsed[0].status)

    assertEquals(2L, parsed[1].id)
    assertEquals("MW102", parsed[1].noMobil)
    assertEquals("-1500", parsed[1].estimasiKm)
    assertEquals("PENDING", parsed[1].aprovel)
    assertEquals("LEWAT KM", parsed[1].keterangan)
    assertEquals("OVERDUE", parsed[1].status)
  }

  @Test
  fun testPrediksiPartFormulaAndApproval() {
    val entity = com.example.data.local.entity.PrediksiPartEntity(
      id = 1,
      noMobil = "101",
      kmInput = "50000",
      prediksiKm = "15000",
      kmTarget = "65000",
      argoSekarang = "62000",
      aprovel = "APPROVED"
    )
    val argoList = listOf(
      com.example.data.local.entity.ArgoEntity(id = 1, noMobil = "101", km = "62000")
    )

    val resolved = com.example.ui.screens.resolvePrediksiPartWithLiveArgo(entity, argoList)
    // Rumus: [ARGO_SEKARANG] - [KM_TARGET] = 62000 - 65000 = -3000
    assertEquals("-3000", resolved.estimasiKm)
    // Approval mapping: APPROVED -> Disetujui
    assertEquals("Disetujui", resolved.aprovel)

    // Test with argo greater than target: 70000 - 65000 = 5000
    val entityOver = entity.copy(aprovel = "PENDING")
    val argoListOver = listOf(
      com.example.data.local.entity.ArgoEntity(id = 2, noMobil = "101", km = "70000")
    )
    val resolvedOver = com.example.ui.screens.resolvePrediksiPartWithLiveArgo(entityOver, argoListOver)
    assertEquals("5000", resolvedOver.estimasiKm)
    assertEquals("Menunggu", resolvedOver.aprovel)

    // Test rejected mapping: TOLAK -> Ditolak
    val entityTolak = entity.copy(aprovel = "TOLAK")
    val resolvedTolak = com.example.ui.screens.resolvePrediksiPartWithLiveArgo(entityTolak, emptyList())
    assertEquals("Ditolak", resolvedTolak.aprovel)
  }

  @Test
  fun testPrediksiPartStatusSelesaiRequiresKeterangan() {
    // Validasi: Status SELESAI memerlukan keterangan hanya jika APROVEL adalah Disetujui (selain itu hiden)
    fun isKeteranganValid(aprovel: String, status: String, keterangan: String): Boolean {
      val isApproved = aprovel.trim().equals("Disetujui", ignoreCase = true) ||
          aprovel.contains("SETUJU", ignoreCase = true) ||
          aprovel.contains("APPROV", ignoreCase = true) ||
          aprovel.contains("ACC", ignoreCase = true)
      val isRequired = isApproved && status.trim().equals("SELESAI", ignoreCase = true)
      return !isRequired || keterangan.trim().isNotBlank()
    }

    // APROVEL Menunggu -> STATUS dan KETERANGAN tersembunyi (selain Disetujui adalah hiden)
    assertTrue(isKeteranganValid("Menunggu", "SELESAI", ""))

    // APROVEL Ditolak -> STATUS dan KETERANGAN tersembunyi (selain Disetujui adalah hiden)
    assertTrue(isKeteranganValid("Ditolak", "SELESAI", ""))

    // APROVEL Disetujui + Status PROSES dengan keterangan kosong -> Valid
    assertTrue(isKeteranganValid("Disetujui", "PROSES", ""))
    assertTrue(isKeteranganValid("Disetujui", "PROSES", "   "))

    // APROVEL Disetujui + Status SELESAI dengan keterangan kosong -> Tidak Valid (Wajib diisi)
    assertFalse(isKeteranganValid("Disetujui", "SELESAI", ""))
    assertFalse(isKeteranganValid("Disetujui", "SELESAI", "   "))

    // APROVEL Disetujui + Status SELESAI dengan keterangan terisi -> Valid
    assertTrue(isKeteranganValid("Disetujui", "SELESAI", "PART SUDAH DIGANTI OLEH MEKANIK"))
  }

  @Test
  fun testPrediksiPartStatusSelesaiDoesNotUpdateArgo() {
    val entitySelesai = com.example.data.local.entity.PrediksiPartEntity(
      id = 10,
      noMobil = "202",
      kmInput = "40000",
      prediksiKm = "10000",
      kmTarget = "50000",
      argoSekarang = "51000",
      status = "SELESAI"
    )
    val liveArgoList = listOf(
      com.example.data.local.entity.ArgoEntity(id = 1, noMobil = "202", km = "60000")
    )

    // Jika Status SELESAI -> argoSekarang TIDAK update otomatis dari argo armada
    val resolvedSelesai = com.example.ui.screens.resolvePrediksiPartWithLiveArgo(entitySelesai, liveArgoList)
    assertEquals("51000", resolvedSelesai.argoSekarang)

    // Jika Status PROSES -> argoSekarang TETAP update otomatis dari argo armada
    val entityProses = entitySelesai.copy(status = "PROSES")
    val resolvedProses = com.example.ui.screens.resolvePrediksiPartWithLiveArgo(entityProses, liveArgoList)
    assertEquals("60000", resolvedProses.argoSekarang)
  }

  @Test
  fun testPrediksiPartStatusSelesaiIsHiddenAndChipFilters() {
    val itemSelesai = com.example.data.local.entity.PrediksiPartEntity(
      id = 1,
      noMobil = "101",
      status = "SELESAI",
      aprovel = "Disetujui"
    )
    val itemProses = com.example.data.local.entity.PrediksiPartEntity(
      id = 2,
      noMobil = "102",
      status = "PROSES",
      aprovel = "Disetujui"
    )
    val itemMenunggu = com.example.data.local.entity.PrediksiPartEntity(
      id = 3,
      noMobil = "103",
      status = "PROSES",
      aprovel = "Menunggu"
    )

    val allItems = listOf(itemSelesai, itemProses, itemMenunggu)

    // Data akan di-hide jika status SELESAI
    val activeItems = allItems.filter { item ->
      val isSelesai = item.status.trim().equals("SELESAI", ignoreCase = true) ||
          item.status.contains("SELESAI", ignoreCase = true)
      !isSelesai
    }

    assertEquals(2, activeItems.size)
    assertFalse(activeItems.any { it.status == "SELESAI" })
    assertTrue(activeItems.any { it.id == 2L })
    assertTrue(activeItems.any { it.id == 3L })

    // Chip PROSES filter
    val prosesFiltered = activeItems.filter { it.status.equals("PROSES", ignoreCase = true) }
    assertEquals(2, prosesFiltered.size)

    // Chip APROVEL filter (status Menunggu)
    val aprovelFiltered = activeItems.filter {
      it.aprovel.contains("TUNGGU", ignoreCase = true) ||
          it.aprovel.contains("MENUNGGU", ignoreCase = true) ||
          it.aprovel.contains("PENDING", ignoreCase = true)
    }
    assertEquals(1, aprovelFiltered.size)
    assertEquals("103", aprovelFiltered.first().noMobil)
  }

  @Test
  fun testPrediksiPartUniqueKeyIdInput() {
    val beforeTime = System.currentTimeMillis()
    // Simulasi input baru tanpa ID (0L)
    val inputNew = com.example.data.local.entity.PrediksiPartEntity(
      id = 0L,
      noMobil = "205",
      namaPart = "OLI MESIN",
      prediksiKm = "5000",
      status = "PROSES",
      aprovel = "Menunggu"
    )

    // Memastikan logic assignment unique key ID
    val uniqueId = if (inputNew.id <= 0L) System.currentTimeMillis() else inputNew.id
    val afterTime = System.currentTimeMillis()

    assertTrue(uniqueId >= beforeTime)
    assertTrue(uniqueId <= afterTime)
    assertTrue(uniqueId > 1000000000000L) // Unix timestamp format

    val entityWithUniqueKey = inputNew.copy(id = uniqueId)
    assertEquals(uniqueId, entityWithUniqueKey.id)
    assertEquals("205", entityWithUniqueKey.noMobil)
  }

  @Test
  fun testPrediksiPartSortingEstimasiKmDescending() {
    val item1 = com.example.data.local.entity.PrediksiPartEntity(id = 1, noMobil = "101", estimasiKm = "500")
    val item2 = com.example.data.local.entity.PrediksiPartEntity(id = 2, noMobil = "102", estimasiKm = "12500")
    val item3 = com.example.data.local.entity.PrediksiPartEntity(id = 3, noMobil = "103", estimasiKm = "-200")
    val item4 = com.example.data.local.entity.PrediksiPartEntity(id = 4, noMobil = "104", estimasiKm = "-1500")
    val item5 = com.example.data.local.entity.PrediksiPartEntity(id = 5, noMobil = "105", estimasiKm = "0")
    val item6 = com.example.data.local.entity.PrediksiPartEntity(id = 6, noMobil = "106", estimasiKm = "")

    val list = listOf(item1, item3, item2, item6, item4, item5)
    val sorted = list.sortedWith(
      compareByDescending<com.example.data.local.entity.PrediksiPartEntity> {
        com.example.ui.screens.parseEstimasiKmValue(it.estimasiKm)
      }.thenByDescending { it.id }
    )

    assertEquals("102", sorted[0].noMobil) // 12500 (tertinggi)
    assertEquals("101", sorted[1].noMobil) // 500
    assertEquals("105", sorted[2].noMobil) // 0
    assertEquals("103", sorted[3].noMobil) // -200
    assertEquals("104", sorted[4].noMobil) // -1500
    assertEquals("106", sorted[5].noMobil) // "" (terendah)
  }

  @Test
  fun testIsEstimasiKmOverThreshold() {
    // Nilai di atas -500 (> -500)
    assertTrue(com.example.ui.screens.isEstimasiKmOverThreshold("12500"))
    assertTrue(com.example.ui.screens.isEstimasiKmOverThreshold("500"))
    assertTrue(com.example.ui.screens.isEstimasiKmOverThreshold("0"))
    assertTrue(com.example.ui.screens.isEstimasiKmOverThreshold("-200"))
    assertTrue(com.example.ui.screens.isEstimasiKmOverThreshold("-450"))
    assertTrue(com.example.ui.screens.isEstimasiKmOverThreshold("-499"))

    // Nilai sama dengan atau di bawah -500 (<= -500)
    assertFalse(com.example.ui.screens.isEstimasiKmOverThreshold("-500"))
    assertFalse(com.example.ui.screens.isEstimasiKmOverThreshold("-600"))
    assertFalse(com.example.ui.screens.isEstimasiKmOverThreshold("-1500"))
    assertFalse(com.example.ui.screens.isEstimasiKmOverThreshold(""))
    assertFalse(com.example.ui.screens.isEstimasiKmOverThreshold("-"))
  }

  @Test
  fun testPrediksiPartLookupByVehicle() {
    val list = listOf(
      com.example.data.local.entity.PrediksiPartEntity(id = 1, noMobil = "101", namaPart = "KAMPAS REM DEPAN"),
      com.example.data.local.entity.PrediksiPartEntity(id = 2, noMobil = "#101", namaPart = "OLI MESIN"),
      com.example.data.local.entity.PrediksiPartEntity(id = 3, noMobil = "102", namaPart = "FILTER OLI")
    )

    // Lookup unit #101
    val matched101 = list.filter { com.example.ui.screens.isVehicleMatch(it.noMobil, "101") }
    assertEquals(2, matched101.size)
    assertTrue(matched101.any { it.namaPart == "KAMPAS REM DEPAN" })
    assertTrue(matched101.any { it.namaPart == "OLI MESIN" })

    // Lookup unit 103 (tidak ada)
    val matched103 = list.filter { com.example.ui.screens.isVehicleMatch(it.noMobil, "103") }
    assertEquals(0, matched103.size)
  }

  @Test
  fun testActivePrediksiPartSelectionForVehicle() {
    val items = listOf(
      com.example.data.local.entity.PrediksiPartEntity(id = 1, noMobil = "101", namaPart = "BUSI", status = "SELESAI", estimasiKm = "500"),
      com.example.data.local.entity.PrediksiPartEntity(id = 2, noMobil = "101", namaPart = "OLI MESIN", status = "PROSES", estimasiKm = "-100"),
      com.example.data.local.entity.PrediksiPartEntity(id = 3, noMobil = "101", namaPart = "KAMPAS REM", status = "PROSES", estimasiKm = "200")
    )

    // Filter active items (bukan SELESAI / DONE)
    val activeItems = items.filter { p ->
      val isSelesai = p.status.trim().equals("SELESAI", ignoreCase = true) ||
          p.status.contains("SELESAI", ignoreCase = true) ||
          p.status.contains("DONE", ignoreCase = true)
      !isSelesai
    }.sortedWith(
      compareByDescending<com.example.data.local.entity.PrediksiPartEntity> { com.example.ui.screens.parseEstimasiKmValue(it.estimasiKm) }
        .thenByDescending { it.id }
    )

    assertEquals(2, activeItems.size)
    // Status SELESAI di-hide
    assertTrue(activeItems.none { it.status.equals("SELESAI", ignoreCase = true) })
    // Urutan tertinggi estimasi: KAMPAS REM (200) sebelum OLI MESIN (-100)
    assertEquals("KAMPAS REM", activeItems.first().namaPart)
    assertEquals(3L, activeItems.first().id)

    // Kasus jika semua berstatus SELESAI: activeItems kosong (card prediksi part hiden)
    val allSelesai = listOf(
      com.example.data.local.entity.PrediksiPartEntity(id = 1, noMobil = "101", namaPart = "BUSI", status = "SELESAI", estimasiKm = "500"),
      com.example.data.local.entity.PrediksiPartEntity(id = 2, noMobil = "101", namaPart = "OLI", status = "SELESAI", estimasiKm = "100")
    )
    val filteredAllSelesai = allSelesai.filter { p ->
      val isSelesai = p.status.trim().equals("SELESAI", ignoreCase = true) ||
          p.status.contains("SELESAI", ignoreCase = true) ||
          p.status.contains("DONE", ignoreCase = true)
      !isSelesai
    }
    assertTrue(filteredAllSelesai.isEmpty())
  }

  @Test
  fun testParseServiceKeindahanCsvWithNewStructure() {
    val sampleCsv = """
      "No Mobil","SERVICE","Selisih KM","Notification Desc","Notif. No","Status","KM Servis Sebelum nya","KM Jatuh Tempo","Equipment No","Notif Date","KM Total","Sticker Bandara"
      "BD2473","P15","1.692","@(P15) SERVICE KI TRANS","12213393","OSNO","223.173","230.673","1098474","02/08/2026","232.365","Tidak Terpasang"
      "BD2474","P09","-450","@(P09) SERVICE KI","12213394","NOPR","150.000","160.000","1098475","01/08/2026","159.550","Terpasang"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parseServiceKeindahanCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals("BD2473", parsed[0].noMobil)
    assertEquals("P15", parsed[0].jenisService)
    assertEquals("1.692", parsed[0].selisihKm)
    assertEquals("@(P15) SERVICE KI TRANS", parsed[0].notificationDesc)
    assertEquals("12213393", parsed[0].notifNo)
    assertEquals("OSNO", parsed[0].status)
    assertEquals("223.173", parsed[0].kmServisSebelumnya)
    assertEquals("230.673", parsed[0].kmJatuhTempo)
    assertEquals("1098474", parsed[0].equipmentNo)
    assertEquals("02/08/2026", parsed[0].notifDate)
    assertEquals("232.365", parsed[0].kmTotal)
    assertEquals("Tidak Terpasang", parsed[0].stickerBandara)

    assertEquals("BD2474", parsed[1].noMobil)
    assertEquals("P09", parsed[1].jenisService)
    assertEquals("NOPR", parsed[1].status)
  }

  @Test
  fun testBanSheetDefaultConstants() {
    assertEquals("17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo", com.example.data.session.SessionManager.DEFAULT_BAN_SHEET_ID)
    assertEquals("DATA_BAN", com.example.data.session.SessionManager.DEFAULT_BAN_SHEET_NAME)
  }

  @Test
  fun testParseDataBanCsvFormat() {
    val sampleCsv = """
      "No_Mobil","No_Polisi","EMBOS","KM_BAN"
      "BB2285","B2285SUB","26BC1718","-110.989"
      "BB2287","B2287SUB","8064D","0"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parseBanCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals("BB2285", parsed[0].noMobil)
    assertEquals("B2285SUB", parsed[0].platNomer)
    assertEquals("B2285SUB", parsed[0].displayNoPolisi)
    assertEquals("26BC1718", parsed[0].embos)
    assertEquals("-110.989", parsed[0].kmBan)
    assertEquals("-110.989", parsed[0].displayKmBan)

    assertEquals("BB2287", parsed[1].noMobil)
    assertEquals("B2287SUB", parsed[1].platNomer)
    assertEquals("8064D", parsed[1].embos)
    assertEquals("0", parsed[1].kmBan)
  }

  @Test
  fun testParseBanCsv() {
    val sampleCsv = """
      "ID","NO MOBIL","PLAT NOMER","EMBOS","KM TOTAL"
      "1","MWD101","B 1234 ABC","BRIDGESTONE 185/65 R15","45000"
      "2","MWD102","B 5678 DEF","GAJAH TUNGGAL 185/65 R15","52000"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parseBanCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals(1L, parsed[0].id)
    assertEquals("MWD101", parsed[0].noMobil)
    assertEquals("B 1234 ABC", parsed[0].platNomer)
    assertEquals("BRIDGESTONE 185/65 R15", parsed[0].embos)
    assertEquals("45000", parsed[0].kmTotal)

    assertEquals(2L, parsed[1].id)
    assertEquals("MWD102", parsed[1].noMobil)
    assertEquals("52000", parsed[1].kmTotal)
  }

  @Test
  fun testParseBanCsvWithDataBanFlexibleHeaders() {
    val sampleCsv = """
      "NO","NO_MOBIL","PLAT","MERK","TOTAL KM","TANGGAL PASANG","POSISI"
      "10","MWD201","B 9999 XYZ","DUNLOP","65000","2026-05-12","Depan Kanan"
      "11","MWD202","B 8888 XYZ","GOODYEAR","72000","2026-06-15","Belakang Kiri"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parseBanCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals(10L, parsed[0].id)
    assertEquals("MWD201", parsed[0].noMobil)
    assertEquals("B 9999 XYZ", parsed[0].platNomer)
    assertEquals("DUNLOP", parsed[0].embos)
    assertEquals("65000", parsed[0].kmTotal)
    assertEquals("2026-05-12", parsed[0].tanggalPasang)
    assertEquals("Depan Kanan", parsed[0].posisiBan)

    assertEquals(11L, parsed[1].id)
    assertEquals("MWD202", parsed[1].noMobil)
    assertEquals("72000", parsed[1].kmTotal)
  }

  @Test
  fun testKoplingSheetDefaultConstants() {
    assertEquals("1qBtcaJP1adyawgZ2hbAIvlld-usZ3CV5YB6QSRpVxd4", com.example.data.session.SessionManager.DEFAULT_KOPLING_SHEET_ID)
    assertEquals("KOPLING", com.example.data.session.SessionManager.DEFAULT_KOPLING_SHEET_NAME)
  }

  @Test
  fun testParseKoplingCsv() {
    val sampleCsv = """
      "ID","NO MOBIL","JENIS TIPE","TOTAL KM"
      "1","MWD101","NEW TRANSMOVER","85000"
      "2","MWD102","AVANZA 1.3","112000"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parseKoplingCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals(1L, parsed[0].id)
    assertEquals("MWD101", parsed[0].noMobil)
    assertEquals("NEW TRANSMOVER", parsed[0].jenisTipe)
    assertEquals("85000", parsed[0].totalKm)

    assertEquals(2L, parsed[1].id)
    assertEquals("MWD102", parsed[1].noMobil)
    assertEquals("AVANZA 1.3", parsed[1].jenisTipe)
    assertEquals("112000", parsed[1].totalKm)
  }

  @Test
  fun testParseKoplingCsvFlexibleHeaders() {
    val sampleCsv = """
      "NO.","KENDARAAN","TIPE","KM KOPLING"
      "5","MWD301","CALYA","95000"
      "6","MWD302","SIGRA","104000"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parseKoplingCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals(5L, parsed[0].id)
    assertEquals("MWD301", parsed[0].noMobil)
    assertEquals("CALYA", parsed[0].jenisTipe)
    assertEquals("95000", parsed[0].totalKm)

    assertEquals(6L, parsed[1].id)
    assertEquals("MWD302", parsed[1].noMobil)
    assertEquals("SIGRA", parsed[1].jenisTipe)
    assertEquals("104000", parsed[1].totalKm)
  }

  @Test
  fun testParseKoplingCsvExactFormat() {
    val sampleCsv = """
      "No Mobil","Tipe","KM_KOPLING"
      "BB2283","NEW TRANSMOVER","0"
      "BB2285","NEW TRANSMOVER","-126610"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parseKoplingCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals("BB2283", parsed[0].noMobil)
    assertEquals("NEW TRANSMOVER", parsed[0].jenisTipe)
    assertEquals("NEW TRANSMOVER", parsed[0].displayTipe)
    assertEquals("0", parsed[0].kmKopling)
    assertEquals("0", parsed[0].displayKmKopling)

    assertEquals("BB2285", parsed[1].noMobil)
    assertEquals("NEW TRANSMOVER", parsed[1].jenisTipe)
    assertEquals("-126610", parsed[1].kmKopling)
    assertEquals("-126610", parsed[1].displayKmKopling)
  }

  @Test
  fun testPengerjaanTeknikSheetDefaultConstants() {
    assertEquals("145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY", com.example.data.session.SessionManager.DEFAULT_PENGERJAAN_TEKNIK_SHEET_ID)
    assertEquals("SKD", com.example.data.session.SessionManager.DEFAULT_PENGERJAAN_TEKNIK_SHEET_NAME)
  }

  @Test
  fun testParsePengerjaanTeknikCsvExactFormat() {
    val sampleCsv = """
      Created On ,Order ,Object Description ,Description ,KETERANGAN ,COUNTER ,TIPE 
      20.10.2026,11917347,BD5236,(S07) SERVICE TEKNIK NEW TRANS,S07,187461,SKD
      19.10.2026,11917348,BD5237,(S08) REPARASI REM DAN KOPLING,S08,,SKD
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parsePengerjaanTeknikCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals("20.10.2026", parsed[0].tanggal)
    assertEquals("20.10.2026", parsed[0].createdOn)
    assertEquals("11917347", parsed[0].orderNo)
    assertEquals("11917347", parsed[0].order)
    assertEquals("BD5236", parsed[0].noMobil)
    assertEquals("BD5236", parsed[0].objectDescription)
    assertEquals("(S07) SERVICE TEKNIK NEW TRANS", parsed[0].description)
    assertEquals("S07", parsed[0].service)
    assertEquals("S07", parsed[0].keterangan)
    assertEquals("S07", parsed[0].displayKeterangan)
    assertEquals("187461", parsed[0].kmService)
    assertEquals("187461", parsed[0].counter)
    assertEquals("187461", parsed[0].displayCounter)
    assertEquals("SKD", parsed[0].tipe)
    assertEquals("SKD", parsed[0].displayTipe)

    assertEquals("19.10.2026", parsed[1].createdOn)
    assertEquals("11917348", parsed[1].order)
    assertEquals("BD5237", parsed[1].objectDescription)
    assertEquals("S08", parsed[1].displayKeterangan)
    assertEquals("", parsed[1].counter)
    assertEquals("", parsed[1].displayCounter)
  }

  @Test
  fun testParseReservasiCsvWithExactHeaders() {
    val sampleCsv = """
      "NO_MOBIL","Order","Material","Material_Description","Doc_Date","Pstng_Date","Quantity","BUn","LC_Amount","Batch"
      "BD5236","11917347","1000123","FILTER OLI MESIN","20.10.2026","21.10.2026","2","PC","150.000","BATCH-01"
      "BD5237","11917348","1000124","KAMPAS REM DEPAN","19.10.2026","20.10.2026","1","SET","450.000","BATCH-02"
    """.trimIndent()

    val parsed = GoogleAppsScriptHub.parseReservasiCsv(sampleCsv)
    assertEquals(2, parsed.size)
    assertEquals("BD5236", parsed[0].noMobil)
    assertEquals("11917347", parsed[0].orderNo)
    assertEquals("1000123", parsed[0].material)
    assertEquals("FILTER OLI MESIN", parsed[0].materialDescription)
    assertEquals("20.10.2026", parsed[0].documentDate)
    assertEquals("20.10.2026", parsed[0].docDate)
    assertEquals("21.10.2026", parsed[0].postingDate)
    assertEquals("21.10.2026", parsed[0].pstngDate)
    assertEquals("2", parsed[0].quantity)
    assertEquals("PC", parsed[0].baseUnitOfMeasure)
    assertEquals("PC", parsed[0].bun)
    assertEquals("150.000", parsed[0].amtInLocCur)
    assertEquals("150.000", parsed[0].lcAmount)
    assertEquals("BATCH-01", parsed[0].batch)

    assertEquals("BD5237", parsed[1].noMobil)
    assertEquals("11917348", parsed[1].orderNo)
    assertEquals("1000124", parsed[1].material)
    assertEquals("KAMPAS REM DEPAN", parsed[1].materialDescription)
    assertEquals("BATCH-02", parsed[1].batch)
  }
}


