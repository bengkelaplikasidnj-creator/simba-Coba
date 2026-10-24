// ==============================================================
// SIMBA BC - SISTEM INFORMASI & MONITORING BENGKEL ARMADA - GOOGLE APPS SCRIPT (Code.gs)
// Otomatisasi Sinkronisasi 2 Arah (Read & Update/Write) & Upload Foto Google Drive
// Versi: 17.0 (Mendukung 14 Kolom Resmi Pengerjaan Cat + ID Uniq + 6 Slot Foto Drive)
// ==============================================================
//
// PANDUAN PENTING AKTIVASI & DEPLOYMENT WEB APP (HANYA 1 MENIT):
// -------------------------------------------------------------
// 1. Buka Google Apps Script di: https://script.google.com
// 2. Buat Project Baru (atau buka project yang sudah ada).
// 3. Salin/Paste seluruh isi file ini ke file 'Code.gs'.
//
// 4. AKTIVASI IZIN GOOGLE DRIVE (PENTING! AGAR FOTO DAPAT DIUNGGAH):
//    - Pada toolbar atas di samping tombol Jalankan (▶ Run), klik menu dropdown fungsi.
//    - Pilih fungsi: 'aktivasiIzinGoogleDrive' (atau 'testRun').
//    - Klik tombol: 'Jalankan' (▶ Run).
//    - Akan muncul jendela popup: "Authorization Required" (Perlu Otorisasi).
//    - Klik: "Review Permissions" / "Tinjau Izin".
//    - Pilih Akun Google Anda.
//    - Klik tulisan kecil di kiri bawah: "Advanced" / "Lanjutan" -> "Go to ... (unsafe)" / "Buka ... (tidak aman)".
//    - Klik: "Allow" / "Izinkan".
//    - Tunggu hingga muncul log: "✅ Izin DriveApp BERHASIL aktif!".
//
// 5. DEPLOYMENT WEB APP RESMI:
//    - Klik tombol biru 'Deploy' (Terapkan) di pojok kanan atas -> pilih 'Manage deployments' (Kelola Deployment) atau 'New deployment' (Deployment Baru).
//    - Jenis Deployment: 'Web app' (Aplikasi Web).
//    - Description: Bengkel MW v17 (14 Kolom Pengerjaan Cat).
//    - Execute as (Jalankan sebagai): 'Me' / 'Saya' (email Anda).
//    - Who has access (Siapa saja yang memiliki akses): 'Anyone' / 'Siapa saja' (WAJIB DIPILIH).
//    - Klik 'Deploy' / 'Terapkan'.
//    - Salin 'Web App URL' (akhiran /exec).
//
// 6. MASUKKAN URL KE APLIKASI:
//    - Buka aplikasi Bengkel Mobile MW -> menu 'Pengaturan Google Sheets'.
//    - Paste Web App URL pada kolom 'URL Google Apps Script'.
//    - Klik 'Test Koneksi Web' & 'Test Izin Google Drive'.
// ==============================================================

// ID Spreadsheet Default
var SHEET_LAPORAN_ID = "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30";
var SHEET_TEKNIK_ID = "1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4";
var SHEET_CAT_ID = "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs";

// ID Folder Google Drive Resmi untuk Penyimpanan Foto Pengerjaan Keindahan & Cat
var FOLDER_PHOTO_KEINDAHAN_ID = "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6";
var FOLDER_PHOTO_CAT_ID = "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6";

// ============================================================================
// 1. FUNGSI AKTIVASI IZIN GOOGLE DRIVE (JALANKAN INI SEKALI DI EDITOR ▶)
// ============================================================================
function aktivasiIzinGoogleDrive() {
  Logger.log("Menguji dan mengaktifkan izin Google Drive (DriveApp)...");
  try {
    var root = DriveApp.getRootFolder();
    Logger.log("✅ Izin DriveApp BERHASIL aktif! Folder Utama: " + root.getName());
    
    var folderTargetId = FOLDER_PHOTO_KEINDAHAN_ID || "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6";
    try {
      var folder = DriveApp.getFolderById(folderTargetId);
      Logger.log("✅ Folder Foto Keindahan Ditemukan: " + folder.getName());
    } catch (eF) {
      Logger.log("ℹ️ Folder target ID (" + folderTargetId + ") belum terhubung/privat. Script akan otomatis menggunakan folder 'FOTO_SERVICE_KEINDAHAN' saat upload foto.");
    }
    return "✅ Izin Google Drive (DriveApp) Berhasil Diaktifkan!";
  } catch (err) {
    Logger.log("❌ Error saat aktivasi izin Drive: " + err.message);
    throw err;
  }
}

// ============================================================================
// 1b. INISIALISASI PENGERJAAN KEINDAHAN (TINGGAL KLIK!)
// ============================================================================
function inisialisasiPengerjaanKeindahan(params) {
  params = params || {};
  var logs = [];
  var result = {
    status: 'success',
    success: true,
    sheetReady: false,
    folderReady: false,
    driveAccessReady: false,
    sheetName: 'SERVICE_KEINDAHAN',
    spreadsheetId: '',
    folderId: '',
    folderName: 'FOTO_AUDIT_KENDARAAN',
    message: '',
    logs: []
  };

  try {
    // 1. Inisialisasi Spreadsheet & Sheet SERVICE_KEINDAHAN
    var ss = getSpreadsheetKeindahan(params.spreadsheetId);
    result.spreadsheetId = ss.getId();
    var sheetName = params.sheetName || 'SERVICE_KEINDAHAN';
    var sheet = ss.getSheetByName(sheetName);

    if (!sheet) {
      sheet = ss.insertSheet(sheetName);
      logs.push("✅ Sheet '" + sheetName + "' berhasil dibuat baru.");
    } else {
      logs.push("ℹ️ Sheet '" + sheetName + "' sudah tersedia.");
    }

    var expectedHeaders = [
      'ID', 'TANGGAL', 'NO MOBIL', 'JENIS SERVICE', 'NIP/NAMA', 'NOTIF NO', 'ORDER', 'APPROVEL',
      'PHOTO DEPAN', 'PHOTO SAMPING LH', 'PHOTO INTERIOR', 'PHOTO BELAKANG', 'PHOTO SAMPING RH'
    ];

    if (sheet.getLastRow() === 0) {
      sheet.appendRow(expectedHeaders);
      logs.push("✅ Header resmi 13 kolom Pengerjaan Keindahan berhasil ditambahkan.");
    } else {
      var currentHeaders = sheet.getRange(1, 1, 1, Math.max(expectedHeaders.length, sheet.getLastColumn())).getValues()[0];
      var needUpdate = false;
      for (var h = 0; h < expectedHeaders.length; h++) {
        if (!currentHeaders[h] || String(currentHeaders[h]).trim() === '') {
          needUpdate = true;
          break;
        }
      }
      if (needUpdate) {
        sheet.getRange(1, 1, 1, expectedHeaders.length).setValues([expectedHeaders]);
        logs.push("✅ Header sheet disesuaikan dengan format 13 kolom resmi.");
      }
    }

    // Rapikan tampilan Header Sheet: Warna Biru Tua, Teks Putih Tebal, Freeze Baris 1
    try {
      var headerRange = sheet.getRange(1, 1, 1, 13);
      headerRange.setBackground("#003366")
                 .setFontColor("#FFFFFF")
                 .setFontWeight("bold")
                 .setFontSize(10)
                 .setHorizontalAlignment("center")
                 .setVerticalAlignment("middle");
      sheet.setRowHeight(1, 36);
      sheet.setFrozenRows(1);
      
      sheet.setColumnWidth(1, 130);  // ID (kode unix)
      sheet.setColumnWidth(2, 140);  // TANGGAL (tanggal dan jam saat itu)
      sheet.setColumnWidth(3, 110);  // NO MOBIL
      sheet.setColumnWidth(4, 150);  // JENIS SERVICE
      sheet.setColumnWidth(5, 160);  // NIP/NAMA
      sheet.setColumnWidth(6, 120);  // NOTIF NO
      sheet.setColumnWidth(7, 110);  // ORDER
      sheet.setColumnWidth(8, 110);  // APPROVEL
      sheet.setColumnWidth(9, 160);  // PHOTO DEPAN
      sheet.setColumnWidth(10, 160); // PHOTO SAMPING LH
      sheet.setColumnWidth(11, 160); // PHOTO INTERIOR
      sheet.setColumnWidth(12, 160); // PHOTO BELAKANG
      sheet.setColumnWidth(13, 160); // PHOTO SAMPING RH
      logs.push("✅ Format tabel (warna biru, teks putih, freeze baris 1, lebar kolom) berhasil dirapikan.");
    } catch (eFmt) {
      Logger.log("Format header warning: " + eFmt.message);
    }
    result.sheetReady = true;

    // 2. Inisialisasi Google Drive Folder
    var folderTarget = null;
    var targetFolderId = params.folderId || FOLDER_PHOTO_KEINDAHAN_ID || "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6";
    if (targetFolderId) {
      try {
        folderTarget = DriveApp.getFolderById(targetFolderId);
        logs.push("✅ Folder Google Drive ID (" + targetFolderId + ") siap: '" + folderTarget.getName() + "'.");
      } catch (eF1) {
        Logger.log("Folder target ID belum ditemukan: " + eF1.message);
      }
    }

    if (!folderTarget) {
      var targetFolderName = params.folderName || "FOTO_AUDIT_KENDARAAN";
      var fldrs = DriveApp.getFoldersByName(targetFolderName);
      folderTarget = fldrs.hasNext() ? fldrs.next() : DriveApp.createFolder(targetFolderName);
      logs.push("✅ Folder Google Drive siap: '" + folderTarget.getName() + "' (ID: " + folderTarget.getId() + ").");
    }

    if (folderTarget) {
      try {
        folderTarget.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
        logs.push("✅ Izin sharing folder diset publik/view (ANYONE_WITH_LINK).");
      } catch (eShare) {
        try {
          folderTarget.setSharing(DriveApp.Access.DOMAIN_WITH_LINK, DriveApp.Permission.VIEW);
        } catch (eShare2) {}
      }
      result.folderReady = true;
      result.folderId = folderTarget.getId();
      result.folderName = folderTarget.getName();
    }
    result.driveAccessReady = true;

    result.message = "Inisialisasi Pengerjaan Keindahan Berhasil! Sheet SERVICE_KEINDAHAN dan Folder Google Drive telah aktif & siap dipakai.";
    result.logs = logs;
    return result;

  } catch (err) {
    Logger.log("inisialisasiPengerjaanKeindahan error: " + err.message);
    result.status = 'error';
    result.success = false;
    result.message = "Gagal inisialisasi: " + err.message;
    logs.push("❌ Error: " + err.message);
    result.logs = logs;
    return result;
  }
}

// ============================================================================
// 1c. INISIALISASI PENGERJAAN CAT (14 KOLOM RESMI - TINGGAL KLIK!)
// ============================================================================
function inisialisasiPengerjaanCat(params) {
  params = params || {};
  var logs = [];
  var result = {
    status: 'success',
    success: true,
    sheetReady: false,
    folderReady: false,
    sheetName: 'PENGERJAAN_CAT',
    spreadsheetId: '',
    folderName: 'FOTO_AUDIT_KENDARAAN',
    message: '',
    logs: []
  };

  try {
    var ss = getSpreadsheetCat(params.spreadsheetId);
    result.spreadsheetId = ss.getId();
    var sheetName = params.sheetName || 'PENGERJAAN_CAT';
    var sheet = ss.getSheetByName(sheetName) || ss.getSheetByName("PENGERJAAN CAT");

    var officialHeaders = [
      'ID (kode uniq)',
      'Timestamp',
      'TANGGAL',
      'NO MOBIL',
      'KERUSAKAN',
      'PHOTO KERUSAKAN',
      'FOTO NOTIF',
      'FOTO HASIL PENGECATAN',
      'GAMBAR',
      'MEKANIK',
      'NO REGISTRASI CAT',
      'PHOTO LAS KETOK',
      'FOTO HASIL PENGECATAN_2',
      'FOTO HASIL PENGECATAN_3'
    ];

    if (!sheet) {
      sheet = ss.insertSheet(sheetName);
      logs.push("✅ Sheet '" + sheetName + "' berhasil dibuat baru.");
    } else {
      logs.push("ℹ️ Sheet '" + sheet.getName() + "' sudah ada.");
    }

    if (sheet.getLastRow() === 0) {
      sheet.appendRow(officialHeaders);
      logs.push("✅ 14 Kolom Header Pengerjaan Cat berhasil dibuat.");
    } else {
      var currentHeaders = sheet.getRange(1, 1, 1, sheet.getLastColumn()).getValues()[0];
      var firstH = String(currentHeaders[0] || '').trim().toUpperCase();
      if (firstH !== 'ID' && firstH.indexOf('KODE') === -1 && firstH.indexOf('UNIQ') === -1) {
        sheet.insertColumnBefore(1);
        sheet.getRange(1, 1).setValue('ID (kode uniq)');
        logs.push("✅ Menambahkan kolom 'ID (kode uniq)' di awal tabel.");
      }
      if (sheet.getLastColumn() < 14) {
        for (var c = sheet.getLastColumn(); c < officialHeaders.length; c++) {
          sheet.getRange(1, c + 1).setValue(officialHeaders[c]);
        }
        logs.push("✅ Melengkapi sisa kolom header Pengerjaan Cat hingga 14 kolom.");
      }
    }

    try {
      var headerRange = sheet.getRange(1, 1, 1, officialHeaders.length);
      headerRange.setBackground('#1565C0');
      headerRange.setFontColor('#FFFFFF');
      headerRange.setFontWeight('bold');
      headerRange.setHorizontalAlignment('center');
      sheet.setFrozenRows(1);
      logs.push("✅ Format styling header & freeze baris 1 selesai.");
    } catch (eFmt) {}

    result.sheetReady = true;

    try {
      var root = DriveApp.getRootFolder();
      var fIterator = DriveApp.getFoldersByName('FOTO_AUDIT_KENDARAAN');
      if (!fIterator.hasNext()) {
        DriveApp.createFolder('FOTO_AUDIT_KENDARAAN');
        logs.push("✅ Folder 'FOTO_AUDIT_KENDARAAN' berhasil dibuat di Google Drive.");
      } else {
        logs.push("ℹ️ Folder 'FOTO_AUDIT_KENDARAAN' sudah aktif di Google Drive.");
      }
      result.folderReady = true;
    } catch (eDrive) {
      logs.push("⚠️ Catatan Drive: Jalankan fungsi 'aktivasiIzinGoogleDrive' untuk izin upload foto.");
    }

    result.message = "Inisialisasi Pengerjaan Cat Berhasil! Sheet " + sheetName + " (14 Kolom) siap dipakai.";
    result.logs = logs;
    return result;
  } catch (err) {
    result.status = 'error';
    result.success = false;
    result.message = "Gagal inisialisasi: " + err.message;
    logs.push("❌ Error: " + err.message);
    result.logs = logs;
    return result;
  }
}

// Menu otomatis pada Google Sheets saat file dibuka di browser
function onOpen() {
  try {
    var ui = SpreadsheetApp.getUi();
    ui.createMenu('⚡ Bengkel MW')
      .addItem('▶ 1-Klik Inisialisasi Pengerjaan Cat (14 Kolom)', 'inisialisasiPengerjaanCat')
      .addItem('▶ 1-Klik Inisialisasi Pengerjaan Keindahan', 'inisialisasiPengerjaanKeindahan')
      .addItem('▶ Aktivasi Izin Google Drive', 'aktivasiIzinGoogleDrive')
      .addItem('▶ Test Run Sistem', 'testRun')
      .addToUi();
  } catch (e) {}
}

// Fungsi Uji Coba: Klik tombol 'Run' (Jalankan ▶) di atas untuk mengetes
function testRun() {
  Logger.log("Sistem Bengkel Aktif & Siap Digunakan!");
  try {
    aktivasiIzinGoogleDrive();
    inisialisasiPengerjaanCat({});
    inisialisasiPengerjaanKeindahan({});
    Logger.log("✅ Status Sistem: NORMAL & SIAP DIGUNAKAN");
  } catch (e) {
    Logger.log("Peringatan DriveApp: " + e.message);
  }
}

// ============================================================================
// 2. HANDLER HTTP GET
// ============================================================================
function doGet(e) {
  try {
    e = e || {};
    var params = e.parameter || {};
    var action = params.action || '';
    
    // Action 1: Ping / Cek Konektivitas
    if (action === 'ping' || action === '') {
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'OK', 
        message: 'Google Apps Script Bengkel MW Aktif dan Siap Digunakan' 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Inisialisasi Pengerjaan Keindahan via GET (Tinggal Klik!)
    if (action === 'inisialisasiPengerjaanKeindahan' || action === 'initPengerjaanKeindahan' || action === 'setupKeindahan') {
      var resInitGet = inisialisasiPengerjaanKeindahan(params);
      return ContentService.createTextOutput(JSON.stringify(resInitGet)).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 2: Update / Simpan Laporan Harian via GET
    if (action === 'saveLaporanHarian' || action === 'updateLaporanHarian' || action === 'saveOrUpdateLaporanHarian') {
      var result = saveOrUpdateLaporan(params);
      return ContentService.createTextOutput(JSON.stringify(result)).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 3: Baca Data Laporan Harian
    if (action === 'getLaporanHarian') {
      return ContentService.createTextOutput(JSON.stringify(
        fetchSheetData(getSpreadsheetLaporan(), 'LAPORAN_HARIAN')
      )).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 4: Baca Data Service Teknik
    if (action === 'getServiceTeknik') {
      var ssTeknik = getSpreadsheetTeknik(params.spreadsheetId);
      var tabTeknik = params.sheetName || 'TEKNIK';
      if (!ssTeknik.getSheetByName(tabTeknik)) {
        tabTeknik = ssTeknik.getSheetByName('TEKNIK') ? 'TEKNIK' :
                    (ssTeknik.getSheetByName('SERVICE_TEKNIK') ? 'SERVICE_TEKNIK' :
                    (ssTeknik.getSheetByName('SERVICE TEKNIK') ? 'SERVICE TEKNIK' : ''));
      }
      return ContentService.createTextOutput(JSON.stringify(
        fetchSheetData(ssTeknik, tabTeknik)
      )).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 5: Update / Simpan User Bengkel via GET
    if (action === 'saveUser' || action === 'updateUser' || action === 'saveOrUpdateUser') {
      var resultUser = saveOrUpdateUser(params);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultUser 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 5b: Hapus User Bengkel via GET
    if (action === 'deleteUser' || action === 'hapusUser' || action === 'removeUser') {
      var resultDelUserGet = deleteUser(params);
      return ContentService.createTextOutput(JSON.stringify(resultDelUserGet)).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 6: Update / Simpan Pengerjaan Cat via GET
    if (action === 'savePengerjaanCat' || action === 'updatePengerjaanCat' || action === 'saveOrUpdatePengerjaanCat') {
      var resultCat = saveOrUpdatePengerjaanCat(params);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultCat 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 6b: Hapus Pengerjaan Cat via GET
    if (action === 'deletePengerjaanCat' || action === 'deleteCat' || action === 'hapusPengerjaanCat') {
      var resultDelCat = deletePengerjaanCat(params);
      return ContentService.createTextOutput(JSON.stringify(resultDelCat)).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 6c: Inisialisasi Pengerjaan Cat via GET (14 Kolom)
    if (action === 'inisialisasiPengerjaanCat' || action === 'initPengerjaanCat' || action === 'setupPengerjaanCat') {
      var resultInitCat = inisialisasiPengerjaanCat(params);
      return ContentService.createTextOutput(JSON.stringify(resultInitCat)).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Action 7: Update / Simpan Pengerjaan Keindahan via GET
    if (action === 'savePengerjaanKeindahan' || action === 'updatePengerjaanKeindahan' || action === 'saveOrUpdatePengerjaanKeindahan') {
      var resultKeindahan = saveOrUpdatePengerjaanKeindahan(params);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultKeindahan 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 7b: Simpan Data Audit / Input Audit via GET
    if (action === 'saveAudit' || action === 'saveDataAudit' || action === 'syncAudit' || action === 'kirimAudit') {
      var resultAuditGet = saveOrUpdateAudit(params);
      return ContentService.createTextOutput(JSON.stringify(resultAuditGet)).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 7c: Update / Simpan Prediksi Part via GET
    if (action === 'savePrediksiPart' || action === 'updatePrediksiPart' || action === 'saveOrUpdatePrediksiPart') {
      var resultPrediksiGet = saveOrUpdatePrediksiPart(params);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultPrediksiGet 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 7d: Hapus Prediksi Part via GET
    if (action === 'deletePrediksiPart' || action === 'hapusPrediksiPart' || action === 'removePrediksiPart') {
      var resultDelPrediksiGet = deletePrediksiPart(params);
      return ContentService.createTextOutput(JSON.stringify(resultDelPrediksiGet)).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 8: Upload Foto ke Google Drive via GET
    if (action === 'uploadImageToDrive' || action === 'uploadImage' || action === 'uploadPhoto') {
      try {
        var photoBase64 = params.image || params.base64 || params.photo || params.file;
        if (!photoBase64) {
          return ContentService.createTextOutput(JSON.stringify({ 
            status: 'error', 
            success: false, 
            message: 'Data foto (base64) tidak ditemukan dalam request' 
          })).setMimeType(ContentService.MimeType.JSON);
        }
        var fName = params.fileName || ('KEINDAHAN_' + new Date().getTime() + '.jpg');
        var fFolderId = params.folderId || FOLDER_PHOTO_KEINDAHAN_ID;
        var fFolderName = params.folderName || 'FOTO_AUDIT_KENDARAAN';
        var driveLink = saveBase64ImageToDrive(photoBase64, fName, fFolderId, fFolderName);
        if (!driveLink || String(driveLink).indexOf('http') !== 0) {
          return ContentService.createTextOutput(JSON.stringify({ 
            status: 'error', 
            success: false, 
            message: 'Izin DriveApp belum aktif pada Web App Google Apps Script. Harap deploy ulang Web App dan setujui izin Google Drive.' 
          })).setMimeType(ContentService.MimeType.JSON);
        }
        var fileId = extractDriveFileId(driveLink) || '';
        var canonicalUrl = fileId ? ('https://drive.google.com/file/d/' + fileId + '/view?usp=sharing') : driveLink;
        var directCdnUrl = fileId ? ('https://lh3.googleusercontent.com/d/' + fileId) : driveLink;
        return ContentService.createTextOutput(JSON.stringify({ 
          status: 'success', 
          success: true, 
          url: canonicalUrl, 
          link: canonicalUrl,
          fileId: fileId,
          webUrl: canonicalUrl,
          directUrl: directCdnUrl,
          message: 'Foto berhasil diunggah ke Google Drive!'
        })).setMimeType(ContentService.MimeType.JSON);
      } catch (errGetUpload) {
        return ContentService.createTextOutput(JSON.stringify({ 
          status: 'error', 
          success: false, 
          message: 'Error upload Google Drive: ' + (errGetUpload.message || errGetUpload.toString()) 
        })).setMimeType(ContentService.MimeType.JSON);
      }
    }

    // Action 9: Baca Data Service Keindahan
    if (action === 'getServiceKeindahan' || action === 'getPengerjaanKeindahan') {
      var ssKeindahan = getSpreadsheetKeindahan(params.spreadsheetId);
      var tabKeindahan = params.sheetName || 'KEINDAHAN';
      return ContentService.createTextOutput(JSON.stringify(
        fetchSheetData(ssKeindahan, tabKeindahan)
      )).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 10: Baca Data Pengerjaan Cat
    if (action === 'getPengerjaanCat' || action === 'bacaPengerjaanCat' || action === 'fetchPengerjaanCat') {
      var ssCat = getSpreadsheetCat(params.spreadsheetId);
      var tabCat = params.sheetName || 'PENGERJAAN_CAT';
      if (!ssCat.getSheetByName(tabCat)) {
        tabCat = ssCat.getSheetByName('PENGERJAAN CAT') ? 'PENGERJAAN CAT' :
                 (ssCat.getSheetByName('PENGERJAAN_CAT') ? 'PENGERJAAN_CAT' :
                 (ssCat.getSheetByName('Form Responses 1') ? 'Form Responses 1' : ''));
      }
      return ContentService.createTextOutput(JSON.stringify(
        fetchSheetData(ssCat, tabCat)
      )).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 11: Hapus Pengerjaan Keindahan via GET
    if (action === 'deletePengerjaanKeindahan' || action === 'deleteKeindahan') {
      var resultDelKeindahan = deletePengerjaanKeindahan(params);
      return ContentService.createTextOutput(JSON.stringify(resultDelKeindahan)).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 12: Hapus Foto dari Google Drive via GET
    if (action === 'deleteImageFromDrive' || action === 'deletePhoto' || action === 'deleteImage') {
      var resultDelPhoto = deleteImageFromDrive(params);
      return ContentService.createTextOutput(JSON.stringify(resultDelPhoto)).setMimeType(ContentService.MimeType.JSON);
    }

    // Action 13: Baca Sheet CSV (Mendukung RESERVASI sheet GOODMOVEN & tabel lainnya via Google Apps Script)
    if (action === 'getSheetCsv' || action === 'fetchSheetCsv' || action === 'getReservasi') {
      try {
        var targetSsId = params.spreadsheetId || params.sheetId || '145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY';
        var targetTabName = params.sheetName || params.tabName || 'GOODMOVEN';
        var targetSs = SpreadsheetApp.openById(targetSsId);
        var targetSheet = targetSs.getSheetByName(targetTabName) || targetSs.getSheetByName('GOODMOVEN') || targetSs.getSheetByName('GM') || targetSs.getSheets()[0];
        var dataValues = targetSheet.getDataRange().getValues();
        var csvLines = [];
        for (var rowIdx = 0; rowIdx < dataValues.length; rowIdx++) {
          var rowCells = dataValues[rowIdx];
          var escapedCells = [];
          for (var colIdx = 0; colIdx < rowCells.length; colIdx++) {
            var cellVal = rowCells[colIdx];
            if (cellVal instanceof Date) {
              cellVal = Utilities.formatDate(cellVal, targetSs.getSpreadsheetTimeZone() || "GMT+7", "yyyy-MM-dd");
            }
            var cellStr = String(cellVal === null || cellVal === undefined ? '' : cellVal);
            if (cellStr.indexOf(',') !== -1 || cellStr.indexOf('"') !== -1 || cellStr.indexOf('\n') !== -1) {
              cellStr = '"' + cellStr.replace(/"/g, '""') + '"';
            }
            escapedCells.push(cellStr);
          }
          csvLines.push(escapedCells.join(','));
        }
        return ContentService.createTextOutput(csvLines.join('\n')).setMimeType(ContentService.MimeType.TEXT);
      } catch (errGetSheet) {
        return ContentService.createTextOutput(JSON.stringify({
          status: 'error',
          success: false,
          message: 'Gagal membaca sheet ' + (params.sheetName || 'GOODMOVEN') + ': ' + (errGetSheet.message || errGetSheet.toString())
        })).setMimeType(ContentService.MimeType.JSON);
      }
    }
    
    return ContentService.createTextOutput(JSON.stringify({ 
      status: 'OK', 
      action: action 
    })).setMimeType(ContentService.MimeType.JSON);
    
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ 
      status: 'error', 
      message: err.toString() 
    })).setMimeType(ContentService.MimeType.JSON);
  }
}

// ============================================================================
// 3. HANDLER HTTP POST
// ============================================================================
function doPost(e) {
  try {
    e = e || {};
    var postData = {};
    if (e.postData && e.postData.contents) {
      try {
        postData = JSON.parse(e.postData.contents);
      } catch (errParse) {
        postData = e.parameter || {};
      }
    } else {
      postData = e.parameter || {};
    }

    if (typeof postData === 'string') {
      try { postData = JSON.parse(postData); } catch (e) {}
    }

    var action = (postData && postData.action) || (e.parameter ? e.parameter.action : '');
    var data = (postData && postData.data !== undefined) ? postData.data : postData;
    if (typeof data === 'string') {
      try { data = JSON.parse(data); } catch (e) {}
    }

    // Update / Simpan Laporan Harian
    if (action === 'saveLaporanHarian' || action === 'updateLaporanHarian' || action === 'saveOrUpdateLaporanHarian') {
      var result = saveOrUpdateLaporan(data);
      return ContentService.createTextOutput(JSON.stringify(result)).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Simpan Service Teknik
    if (action === 'saveServiceTeknik') {
      var resTeknik = appendServiceTeknik(data);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resTeknik 
      })).setMimeType(ContentService.MimeType.JSON);
    }
    
    // Simpan User Bengkel
    if (action === 'saveUser' || action === 'updateUser' || action === 'saveOrUpdateUser') {
      var resultUser = saveOrUpdateUser(data);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultUser 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Hapus User Bengkel
    if (action === 'deleteUser' || action === 'hapusUser' || action === 'removeUser') {
      var resultDelUserPost = deleteUser(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resultDelUserPost)).setMimeType(ContentService.MimeType.JSON);
    }

    // Simpan Pengerjaan Cat
    if (action === 'savePengerjaanCat' || action === 'updatePengerjaanCat' || action === 'saveOrUpdatePengerjaanCat') {
      var resultCat = saveOrUpdatePengerjaanCat(data);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultCat 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Hapus Pengerjaan Cat
    if (action === 'deletePengerjaanCat' || action === 'deleteCat' || action === 'hapusPengerjaanCat') {
      var resultDelCatPost = deletePengerjaanCat(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resultDelCatPost)).setMimeType(ContentService.MimeType.JSON);
    }

    // Inisialisasi Pengerjaan Cat (14 Kolom)
    if (action === 'inisialisasiPengerjaanCat' || action === 'initPengerjaanCat' || action === 'setupPengerjaanCat') {
      var resInitCatPost = inisialisasiPengerjaanCat(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resInitCatPost)).setMimeType(ContentService.MimeType.JSON);
    }

    // Simpan Pengerjaan Keindahan (dengan Foto 5 Sisi)
    if (action === 'savePengerjaanKeindahan' || action === 'updatePengerjaanKeindahan' || action === 'saveOrUpdatePengerjaanKeindahan') {
      var resultKeindahan = saveOrUpdatePengerjaanKeindahan(data);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultKeindahan 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Inisialisasi Pengerjaan Keindahan via POST (Tinggal Klik!)
    if (action === 'inisialisasiPengerjaanKeindahan' || action === 'initPengerjaanKeindahan' || action === 'setupKeindahan') {
      var resInitPost = inisialisasiPengerjaanKeindahan(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resInitPost)).setMimeType(ContentService.MimeType.JSON);
    }

    // Simpan Audit / Input Audit (Data Audit, KPI, Catatan, dan Base64 photo1 s/d photo20 & photosMap)
    if (action === 'saveAudit' || action === 'saveDataAudit' || action === 'syncAudit' || action === 'kirimAudit') {
      var resultAudit = saveOrUpdateAudit(data || postData);
      return ContentService.createTextOutput(JSON.stringify(resultAudit)).setMimeType(ContentService.MimeType.JSON);
    }

    // Simpan Prediksi Part via POST
    if (action === 'savePrediksiPart' || action === 'updatePrediksiPart' || action === 'saveOrUpdatePrediksiPart') {
      var resultPrediksiPost = saveOrUpdatePrediksiPart(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify({ 
        status: 'success', 
        data: resultPrediksiPost 
      })).setMimeType(ContentService.MimeType.JSON);
    }

    // Hapus Prediksi Part via POST
    if (action === 'deletePrediksiPart' || action === 'hapusPrediksiPart' || action === 'removePrediksiPart') {
      var resultDelPrediksiPost = deletePrediksiPart(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resultDelPrediksiPost)).setMimeType(ContentService.MimeType.JSON);
    }

    // Upload Foto Base64 ke Google Drive
    if (action === 'uploadImageToDrive' || action === 'uploadImage' || action === 'uploadPhoto') {
      try {
        var photoBase64 = (data && (data.image || data.base64 || data.photo || data.file)) || 
                          (postData && (postData.image || postData.base64 || postData.photo || postData.file)) || 
                          (e.parameter && (e.parameter.image || e.parameter.base64 || e.parameter.photo || e.parameter.file));
        if (!photoBase64) {
          return ContentService.createTextOutput(JSON.stringify({ 
            status: 'error', 
            success: false, 
            message: 'Data foto (base64) tidak ditemukan dalam request' 
          })).setMimeType(ContentService.MimeType.JSON);
        }
        var fName = (data && data.fileName) || (postData && postData.fileName) || ('KEINDAHAN_' + new Date().getTime() + '.jpg');
        var fFolderId = (data && data.folderId) || (postData && postData.folderId) || (e.parameter && e.parameter.folderId) || FOLDER_PHOTO_KEINDAHAN_ID;
        var fFolderName = (data && data.folderName) || (postData && postData.folderName) || (e.parameter && e.parameter.folderName) || 'FOTO_AUDIT_KENDARAAN';
        var driveLink = saveBase64ImageToDrive(photoBase64, fName, fFolderId, fFolderName);
        if (!driveLink || String(driveLink).indexOf('http') !== 0) {
          return ContentService.createTextOutput(JSON.stringify({ 
            status: 'error', 
            success: false, 
            message: 'Izin DriveApp belum aktif pada Web App Google Apps Script. Harap deploy ulang Web App dan setujui izin Google Drive.' 
          })).setMimeType(ContentService.MimeType.JSON);
        }
        var fileId = extractDriveFileId(driveLink) || '';
        var canonicalUrl = fileId ? ('https://drive.google.com/file/d/' + fileId + '/view?usp=sharing') : driveLink;
        var directCdnUrl = fileId ? ('https://lh3.googleusercontent.com/d/' + fileId) : driveLink;
        return ContentService.createTextOutput(JSON.stringify({ 
          status: 'success', 
          success: true, 
          url: canonicalUrl, 
          link: canonicalUrl,
          fileId: fileId,
          webUrl: canonicalUrl,
          directUrl: directCdnUrl,
          message: 'Foto berhasil diunggah ke Google Drive!'
        })).setMimeType(ContentService.MimeType.JSON);
      } catch (errPostUpload) {
        return ContentService.createTextOutput(JSON.stringify({ 
          status: 'error', 
          success: false, 
          message: 'Error upload Google Drive: ' + (errPostUpload.message || errPostUpload.toString()) 
        })).setMimeType(ContentService.MimeType.JSON);
      }
    }

    // Hapus Pengerjaan Keindahan via POST
    if (action === 'deletePengerjaanKeindahan' || action === 'deleteKeindahan') {
      var resultDelPost = deletePengerjaanKeindahan(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resultDelPost)).setMimeType(ContentService.MimeType.JSON);
    }

    // Hapus Foto dari Google Drive via POST
    if (action === 'deleteImageFromDrive' || action === 'deletePhoto' || action === 'deleteImage') {
      var resultDelImgPost = deleteImageFromDrive(data || postData || e.parameter);
      return ContentService.createTextOutput(JSON.stringify(resultDelImgPost)).setMimeType(ContentService.MimeType.JSON);
    }
    
    return ContentService.createTextOutput(JSON.stringify({ 
      status: 'unknown_action', 
      action: action 
    })).setMimeType(ContentService.MimeType.JSON);
    
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({ 
      status: 'error', 
      message: err.toString() 
    })).setMimeType(ContentService.MimeType.JSON);
  }
}

// ============================================================================
// 4. HELPER SHEET & SPREADSHEET
// ============================================================================
function getSpreadsheetLaporan(overrideId) {
  var id = String(overrideId || SHEET_LAPORAN_ID || '').trim();
  if (id.indexOf('http') !== -1) {
    var match = id.match(/\/d\/([a-zA-Z0-9-_]+)/);
    if (match) id = match[1];
  }
  try {
    var active = SpreadsheetApp.getActiveSpreadsheet();
    if (active) return active;
  } catch (e) {}

  if (id) {
    try {
      return SpreadsheetApp.openById(id);
    } catch (e) {
      Logger.log("openById error: " + e.message);
    }
  }
  throw new Error("Spreadsheet (" + (id || 'kosong') + ") tidak dapat dibuka. Pastikan ID Spreadsheet benar.");
}

function getSpreadsheetTeknik(overrideId) {
  var id = String(overrideId || SHEET_TEKNIK_ID || '').trim();
  if (id.indexOf('http') !== -1) {
    var match = id.match(/\/d\/([a-zA-Z0-9-_]+)/);
    if (match) id = match[1];
  }
  try {
    var active = SpreadsheetApp.getActiveSpreadsheet();
    if (active) return active;
  } catch (e) {}

  if (id) {
    try {
      return SpreadsheetApp.openById(id);
    } catch (e) {
      Logger.log("openById error: " + e.message);
    }
  }
  throw new Error("Spreadsheet Teknik (" + (id || 'kosong') + ") tidak dapat dibuka.");
}

function getSpreadsheetCat(customId) {
  try {
    var id = String(customId || SHEET_CAT_ID || SHEET_LAPORAN_ID || '').trim();
    if (id.indexOf('http') !== -1) {
      var match = id.match(/\/d\/([a-zA-Z0-9-_]+)/);
      if (match) id = match[1];
    }
    if (id !== '') {
      return SpreadsheetApp.openById(id);
    }
  } catch (e) {}
  return getSpreadsheetLaporan();
}

function getSpreadsheetKeindahan(overrideId) {
  var id = String(overrideId || SHEET_LAPORAN_ID || '').trim();
  if (id.indexOf('http') !== -1) {
    var match = id.match(/\/d\/([a-zA-Z0-9-_]+)/);
    if (match) id = match[1];
  }
  try {
    var active = SpreadsheetApp.getActiveSpreadsheet();
    if (active && (!overrideId || active.getId() === id)) return active;
  } catch (e) {}
  if (id) {
    try {
      return SpreadsheetApp.openById(id);
    } catch (e) {}
  }
  return getSpreadsheetLaporan();
}

function fetchSheetData(ss, tabName) {
  try {
    var sheet = tabName ? (ss.getSheetByName(tabName) || ss.getSheets()[0]) : ss.getSheets()[0];
    var values = sheet.getDataRange().getValues();
    if (values.length <= 1) return [];
    var headers = values[0];
    var results = [];
    for (var r = 1; r < values.length; r++) {
      var row = values[r];
      var obj = {};
      for (var c = 0; c < headers.length; c++) {
        obj[headers[c]] = row[c];
      }
      results.push(obj);
    }
    return results;
  } catch (e) {
    return [];
  }
}

// ============================================================================
// 5. PENYIMPANAN FOTO GOOGLE DRIVE (ANTI-GAGAL)
// ============================================================================
function saveBase64ImageToDrive(base64Data, fileName, folderId, folderName) {
  if (!base64Data) return '';
  var strData = String(base64Data).trim();
  if (strData.indexOf('http://') === 0 || strData.indexOf('https://') === 0) {
    return strData;
  }
  try {
    var rawBase64 = strData;
    var contentType = 'image/jpeg';
    if (rawBase64.indexOf(';base64,') !== -1) {
      var parts = rawBase64.split(';base64,');
      contentType = parts[0].replace('data:', '').trim() || 'image/jpeg';
      rawBase64 = parts[1];
    } else if (rawBase64.indexOf('data:') === 0 && rawBase64.indexOf(',') !== -1) {
      var commaIdx = rawBase64.indexOf(',');
      rawBase64 = rawBase64.substring(commaIdx + 1);
    }
    rawBase64 = rawBase64.replace(/\s+/g, '');
    var decoded = Utilities.base64Decode(rawBase64);
    var fName = fileName || ('KEINDAHAN_' + new Date().getTime() + '.jpg');
    var blob = Utilities.newBlob(decoded, contentType, fName);

    var file = null;
    var targetFolderId = String(folderId || FOLDER_PHOTO_KEINDAHAN_ID || '1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6').trim();
    if (targetFolderId.indexOf('http') !== -1) {
      var match = targetFolderId.match(/folders\/([a-zA-Z0-9-_]+)/);
      if (match) targetFolderId = match[1];
    }

    // 1. Coba simpan ke folder spesifik (Target Folder ID)
    if (targetFolderId && targetFolderId.length > 5) {
      try {
        var folder = DriveApp.getFolderById(targetFolderId);
        if (folder) {
          file = folder.createFile(blob);
        }
      } catch (errF) {
        Logger.log("Folder target (" + targetFolderId + ") tidak dapat diakses/ditulis: " + errF + ". Beralih ke folder cadangan...");
        file = null;
      }
    }

    // 2. Jika gagal atau folder ID tidak valid, simpan ke folder cadangan di Drive pengguna
    if (!file) {
      try {
        var dirName = folderName || "FOTO_AUDIT_KENDARAAN";
        var folders = DriveApp.getFoldersByName(dirName);
        var customFolder = folders.hasNext() ? folders.next() : DriveApp.createFolder(dirName);
        file = customFolder.createFile(blob);
      } catch (errCreate) {
        Logger.log("createFolder error: " + errCreate + ". Menyimpan langsung ke Root Drive...");
        file = DriveApp.createFile(blob);
      }
    }

    // Set permission sharing agar foto diset publik/view (ANYONE_WITH_LINK)
    try {
      file.setSharing(DriveApp.Access.ANYONE_WITH_LINK, DriveApp.Permission.VIEW);
    } catch (e1) {
      try {
        file.setSharing(DriveApp.Access.DOMAIN_WITH_LINK, DriveApp.Permission.VIEW);
      } catch (e2) {}
    }

    // Mengembalikan link URL resmi Google Drive
    var fId = file.getId();
    return "https://drive.google.com/file/d/" + fId + "/view?usp=sharing";
  } catch (e) {
    Logger.log("saveBase64ImageToDrive error: " + e.message);
    var errStr = String(e.message || e);
    if (errStr.indexOf("DriveApp") !== -1 || errStr.indexOf("permission") !== -1 || errStr.indexOf("auth/drive") !== -1) {
      throw new Error("Izin DriveApp belum aktif pada Web App. Buka editor Google Apps Script, jalankan fungsi 'aktivasiIzinGoogleDrive' untuk menyetujui izin Drive, lalu Deploy Versi Baru.");
    }
    throw new Error("Gagal simpan ke Google Drive: " + errStr);
  }
}

// Ekstrak ID file Google Drive dari berbagai format URL
function extractDriveFileId(url) {
  if (!url) return null;
  url = String(url).trim();
  var m1 = url.match(/\/file\/d\/([a-zA-Z0-9_-]{15,})/);
  if (m1) return m1[1];
  var m2 = url.match(/[?&]id=([a-zA-Z0-9_-]{15,})/);
  if (m2) return m2[1];
  var m3 = url.match(/\/d\/([a-zA-Z0-9_-]{15,})/);
  if (m3) return m3[1];
  if (/^[a-zA-Z0-9_-]{20,}$/.test(url)) return url;
  return null;
}

// Hapus Foto Google Drive (dipindahkan ke Sampah)
function deleteImageFromDrive(params) {
  params = params || {};
  var targetUrl = params.fileId || params.url || params.link || params.image || params.photo;
  var fileId = extractDriveFileId(targetUrl);
  if (!fileId) {
    return {
      status: 'error',
      success: false,
      message: 'ID file Google Drive atau URL foto tidak valid (' + String(targetUrl) + ')'
    };
  }
  try {
    var file = DriveApp.getFileById(fileId);
    var fName = file.getName();
    file.setTrashed(true);
    Logger.log("✅ Foto Google Drive dipindahkan ke sampah: " + fName + " (" + fileId + ")");
    return {
      status: 'success',
      success: true,
      message: 'Foto ' + fName + ' berhasil dihapus dari Google Drive (dipindahkan ke Sampah).'
    };
  } catch (err) {
    Logger.log("❌ Gagal hapus foto Drive: " + err.message);
    return {
      status: 'error',
      success: false,
      message: 'Gagal hapus foto Google Drive: ' + err.message
    };
  }
}

// ============================================================================
// 6. PENGERJAAN KEINDAHAN (SERVICE_KEINDAHAN)
// ============================================================================
function saveOrUpdatePengerjaanKeindahan(item) {
  item = item || {};
  var ss = getSpreadsheetKeindahan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "SERVICE_KEINDAHAN";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("SERVICE_KEINDAHAN") || 
              ss.getSheetByName("SERVICE KEINDAHAN") || 
              ss.getSheets()[0];

  // Buat header otomatis jika sheet masih kosong
  if (sheet.getLastRow() === 0) {
    sheet.appendRow([
      'ID', 'TANGGAL', 'NO MOBIL', 'JENIS SERVICE', 'NIP/NAMA', 'NOTIF NO', 'ORDER', 'APPROVEL',
      'PHOTO DEPAN', 'PHOTO SAMPING LH', 'PHOTO INTERIOR', 'PHOTO BELAKANG', 'PHOTO SAMPING RH'
    ]);
  }

  var data = sheet.getDataRange().getValues();
  var header = data[0].map(function(h) {
    return String(h || '').trim().toLowerCase().replace(/[^a-z0-9]/g, '');
  });

  // Deteksi indeks kolom secara dinamis agar tahan terhadap posisi kolom manapun
  var colId = header.indexOf('id') !== -1 ? header.indexOf('id') : header.indexOf('no');
  var colTanggal = -1;
  var colMobil = -1;
  var colJenis = -1;
  var colNipNama = -1;
  var colNotif = -1;
  var colOrder = -1;
  var colApprovel = -1;
  var colKm = -1;
  var colDepan = -1;
  var colLh = -1;
  var colInterior = -1;
  var colBelakang = -1;
  var colRh = -1;

  for (var c = 0; c < header.length; c++) {
    var h = header[c];
    if (colTanggal === -1 && (h.indexOf('tanggal') !== -1 || h.indexOf('tgl') !== -1 || h.indexOf('date') !== -1)) colTanggal = c;
    if (colMobil === -1 && (h.indexOf('mobil') !== -1 || h.indexOf('vehicle') !== -1 || h.indexOf('nopol') !== -1)) colMobil = c;
    if (colJenis === -1 && (h.indexOf('jenis') !== -1 || (h.indexOf('service') !== -1 && h.indexOf('km') === -1))) colJenis = c;
    if (colNipNama === -1 && (h.indexOf('nip') !== -1 || h.indexOf('nama') !== -1 || h.indexOf('mekanik') !== -1 || h.indexOf('petugas') !== -1)) colNipNama = c;
    if (colNotif === -1 && (h.indexOf('notif') !== -1 || h.indexOf('desc') !== -1)) colNotif = c;
    if (colOrder === -1 && h.indexOf('order') !== -1) colOrder = c;
    if (colApprovel === -1 && (h.indexOf('approv') !== -1 || h.indexOf('aprov') !== -1 || h.indexOf('status') !== -1)) colApprovel = c;
    if (colKm === -1 && (h === 'km' || h.indexOf('km') !== -1)) colKm = c;
    if (colDepan === -1 && (h.indexOf('depan') !== -1 || h.indexOf('front') !== -1)) colDepan = c;
    if (colLh === -1 && (h.indexOf('lh') !== -1 || (h.indexOf('samping') !== -1 && h.indexOf('kiri') !== -1))) colLh = c;
    if (colInterior === -1 && (h.indexOf('interior') !== -1 || h.indexOf('dalam') !== -1)) colInterior = c;
    if (colBelakang === -1 && (h.indexOf('belakang') !== -1 || h.indexOf('rear') !== -1 || h.indexOf('back') !== -1)) colBelakang = c;
    if (colRh === -1 && (h.indexOf('rh') !== -1 || (h.indexOf('samping') !== -1 && h.indexOf('kanan') !== -1))) colRh = c;
  }

  // Fallback standar 13 kolom: ID | TANGGAL | NO_MOBIL | JENIS_SERVICE | NIP/NAMA | NOTIF_NO | ORDER | APPROVEL | PHOTO_DEPAN | PHOTO_SAMPING_LH | PHOTO_INTERIOR | PHOTO_BELAKANG | PHOTO_SAMPING_RH
  if (colId === -1) colId = 0;
  if (colTanggal === -1) colTanggal = 1;
  if (colMobil === -1) colMobil = 2;
  if (colJenis === -1) colJenis = 3;
  if (colNipNama === -1) colNipNama = 4;
  if (colNotif === -1) colNotif = 5;
  if (colOrder === -1) colOrder = 6;
  if (colApprovel === -1) colApprovel = 7;
  if (colDepan === -1) colDepan = 8;
  if (colLh === -1) colLh = 9;
  if (colInterior === -1) colInterior = 10;
  if (colBelakang === -1) colBelakang = 11;
  if (colRh === -1) colRh = 12;

  function cleanStr(s) {
    return String(s || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  var isEdit = item.isEdit === true || item.isEdit === 'true' || item.mode === 'edit';
  var targetId = String(item.id || '').trim();
  var targetNoMobil = cleanStr(item.noMobil || item.nomobil || item.no_mobil || item.nopol || item.mobil);
  var targetOrder = cleanStr(item.order || item.orderNo || item.orderno || item.order_no);
  var targetTanggal = cleanStr(item.tanggal || item.tgl || item.date);
  var targetRow = -1;

  // Hanya cari baris eksisting jika mode adalah EDIT
  if (isEdit && data.length > 1) {
    // 1. Cari baris berdasarkan ID jika > 0
    if (targetId && targetId !== '0') {
      for (var i = data.length - 1; i >= 1; i--) {
        var rId = String(data[i][colId] || '').trim();
        if (rId === targetId) {
          targetRow = i + 1;
          break;
        }
      }
    }

    // 2. Cari baris berdasarkan Order & No Mobil
    if (targetRow === -1 && targetOrder && targetNoMobil) {
      for (var j = data.length - 1; j >= 1; j--) {
        var rMobil = cleanStr(data[j][colMobil]);
        var rOrder = cleanStr(data[j][colOrder]);
        if (rMobil === targetNoMobil && rOrder === targetOrder) {
          targetRow = j + 1;
          break;
        }
      }
    }

    // 3. Cari baris berdasarkan Tanggal & No Mobil
    if (targetRow === -1 && targetTanggal && targetNoMobil) {
      for (var m = data.length - 1; m >= 1; m--) {
        var rMobTgl = cleanStr(data[m][colMobil]);
        var rTgl = cleanStr(data[m][colTanggal]);
        if (rMobTgl === targetNoMobil && rTgl === targetTanggal) {
          targetRow = m + 1;
          break;
        }
      }
    }

    // 4. Cari baris berdasarkan No Mobil saja (data terakhir mobil ini)
    if (targetRow === -1 && targetNoMobil) {
      for (var n = data.length - 1; n >= 1; n--) {
        var rMobOnly = cleanStr(data[n][colMobil]);
        if (rMobOnly === targetNoMobil) {
          targetRow = n + 1;
          break;
        }
      }
    }
  }

  var lastPhotoError = null;
  function handlePhoto(p, name, existingPhoto) {
    if (p === undefined) return existingPhoto || '';
    if (p === '' || p === '-' || p === 'null' || p === 'DELETED' || p === 'HAPUS') {
      // Jika foto dihapus dan sebelumnya ada file di Google Drive, pindahkan ke sampah Drive
      if (existingPhoto && String(existingPhoto).indexOf('http') === 0) {
        try {
          var oldFileId = extractDriveFileId(existingPhoto);
          if (oldFileId) {
            DriveApp.getFileById(oldFileId).setTrashed(true);
            Logger.log("✅ Foto lama " + name + " berhasil dihapus dari Google Drive (Sampah): " + oldFileId);
          }
        } catch (eOld) {
          Logger.log("Peringatan hapus foto lama: " + eOld.message);
        }
      }
      return '';
    }
    // Jika sudah berupa link HTTP Drive
    if (String(p).indexOf('http') === 0) {
      // Jika link berbeda dengan sebelumnya, hapus foto lama dari Drive
      if (existingPhoto && String(existingPhoto).indexOf('http') === 0 && existingPhoto !== p) {
        try {
          var repId = extractDriveFileId(existingPhoto);
          if (repId) DriveApp.getFileById(repId).setTrashed(true);
        } catch (eRep) {}
      }
      return p;
    }
    // Upload foto Base64 ke Google Drive
    try {
      var cleanSlot = String(name || 'foto').toLowerCase().replace(/[^a-z0-9_]/g, '_');
      var cleanMob = String(item.noMobil || item.nomobil || item.no_mobil || item.nopol || 'MOBIL').toUpperCase().replace(/[^a-zA-Z0-9]/g, '');
      var fName = 'KENDARAAN_' + (cleanMob || 'MOBIL') + '_' + cleanSlot + '_' + new Date().getTime() + '.jpg';
      var dLink = saveBase64ImageToDrive(p, fName, item.folderId || FOLDER_PHOTO_KEINDAHAN_ID, item.folderName || 'FOTO_AUDIT_KENDARAAN');
      if (dLink && String(dLink).indexOf('http') === 0) {
        // Hapus foto lama yang diganti
        if (existingPhoto && String(existingPhoto).indexOf('http') === 0) {
          try {
            var oldId2 = extractDriveFileId(existingPhoto);
            if (oldId2) DriveApp.getFileById(oldId2).setTrashed(true);
          } catch (eOld2) {}
        }
        return dLink;
      }
      return existingPhoto || '';
    } catch (ePhoto) {
      Logger.log("handlePhoto error for " + name + ": " + ePhoto.message);
      lastPhotoError = ePhoto.message || String(ePhoto);
      return existingPhoto || '';
    }
  }

  var existingRow = targetRow > 0 && data[targetRow - 1] ? data[targetRow - 1] : null;
  var existingDepan = existingRow && colDepan < existingRow.length ? String(existingRow[colDepan] || '') : '';
  var existingLh = existingRow && colLh < existingRow.length ? String(existingRow[colLh] || '') : '';
  var existingInterior = existingRow && colInterior < existingRow.length ? String(existingRow[colInterior] || '') : '';
  var existingBelakang = existingRow && colBelakang < existingRow.length ? String(existingRow[colBelakang] || '') : '';
  var existingRh = existingRow && colRh < existingRow.length ? String(existingRow[colRh] || '') : '';

  var pMap = item.photosMap || {};
  var inDepan = item.photoDepan !== undefined ? item.photoDepan : (item.photo_depan !== undefined ? item.photo_depan : (pMap.photoDepan || pMap.photo1 || pMap.slot1 || pMap.depan));
  var inLh = item.photoSampingLh !== undefined ? item.photoSampingLh : (item.photo_samping_lh !== undefined ? item.photo_samping_lh : (pMap.photoSampingLh || pMap.photo2 || pMap.slot2 || pMap.sampingLh));
  var inInterior = item.photoInterior !== undefined ? item.photoInterior : (item.photo_interior !== undefined ? item.photo_interior : (pMap.photoInterior || pMap.photo3 || pMap.slot3 || pMap.interior));
  var inBelakang = item.photoBelakang !== undefined ? item.photoBelakang : (item.photo_belakang !== undefined ? item.photo_belakang : (pMap.photoBelakang || pMap.photo4 || pMap.slot4 || pMap.belakang));
  var inRh = item.photoSampingRh !== undefined ? item.photoSampingRh : (item.photo_samping_rh !== undefined ? item.photo_samping_rh : (pMap.photoSampingRh || pMap.photo5 || pMap.slot5 || pMap.sampingRh));

  var photoDepan = handlePhoto(inDepan, 'kendaraan_depan', existingDepan);
  var photoLh = handlePhoto(inLh, 'kendaraan_samping_lh', existingLh);
  var photoInterior = handlePhoto(inInterior, 'kendaraan_interior', existingInterior);
  var photoBelakang = handlePhoto(inBelakang, 'kendaraan_belakang', existingBelakang);
  var photoRh = handlePhoto(inRh, 'kendaraan_samping_rh', existingRh);

  var maxExistingId = 0;
  if (data.length > 1) {
    for (var k = 1; k < data.length; k++) {
      var num = parseInt(data[k][colId]);
      if (!isNaN(num) && num > maxExistingId) maxExistingId = num;
    }
  }
  // Jika edit dan ada targetId, gunakan targetId itu. Jika data baru, gunakan ID (kode unix) timestamp
  var idVal = (isEdit && targetId && targetId !== '0') ? targetId : ((item.id && String(item.id).length > 5) ? item.id : new Date().getTime());

  var maxCol = Math.max(13, header.length);
  var rowData = new Array(maxCol);
  for (var x = 0; x < maxCol; x++) {
    rowData[x] = existingRow && x < existingRow.length ? existingRow[x] : '';
  }

  var valMobil = (item.noMobil !== undefined && item.noMobil !== '') ? item.noMobil : 
                 (item.nomobil !== undefined && item.nomobil !== '') ? item.nomobil : 
                 (item.no_mobil !== undefined && item.no_mobil !== '') ? item.no_mobil : 
                 (item.nopol !== undefined && item.nopol !== '') ? item.nopol : 
                 (item.mobil !== undefined && item.mobil !== '') ? item.mobil : '';

  var valTanggal = (item.tanggal !== undefined && item.tanggal !== '') ? item.tanggal : 
                   (item.tgl !== undefined && item.tgl !== '') ? item.tgl : 
                   (item.date !== undefined && item.date !== '') ? item.date : '';

  var valJenis = (item.jenisService !== undefined && item.jenisService !== '') ? item.jenisService : 
                 (item.jenisservice !== undefined && item.jenisservice !== '') ? item.jenisservice : 
                 (item.jenis_service !== undefined && item.jenis_service !== '') ? item.jenis_service : 
                 (item.jenis !== undefined && item.jenis !== '') ? item.jenis : '';

  var valNipNama = (item.nipNama !== undefined && item.nipNama !== '') ? item.nipNama : 
                   (item.nip_nama !== undefined && item.nip_nama !== '') ? item.nip_nama : 
                   (item.nip !== undefined && item.nip !== '') ? item.nip : '';

  var valNotif = (item.notifNo !== undefined && item.notifNo !== '') ? item.notifNo : 
                 (item.notifno !== undefined && item.notifno !== '') ? item.notifno : 
                 (item.notif_no !== undefined && item.notif_no !== '') ? item.notif_no : 
                 (item.notif !== undefined && item.notif !== '') ? item.notif : '';

  var valOrder = (item.order !== undefined && item.order !== '') ? item.order : 
                 (item.orderNo !== undefined && item.orderNo !== '') ? item.orderNo : 
                 (item.orderno !== undefined && item.orderno !== '') ? item.orderno : 
                 (item.order_no !== undefined && item.order_no !== '') ? item.order_no : '';

  var valApprovel = (item.approvel !== undefined && item.approvel !== '') ? item.approvel : 
                    (item.approval !== undefined && item.approval !== '') ? item.approval : 
                    (item.aprovel !== undefined && item.aprovel !== '') ? item.aprovel : 'Menunggu';

  var valKm = (item.km !== undefined && item.km !== '') ? item.km : 
              (item.kilometer !== undefined && item.kilometer !== '') ? item.kilometer : '';

  rowData[colId] = idVal;
  rowData[colTanggal] = valTanggal !== '' ? valTanggal : (existingRow && colTanggal < existingRow.length ? existingRow[colTanggal] : '');
  rowData[colMobil] = valMobil !== '' ? valMobil : (existingRow && colMobil < existingRow.length ? existingRow[colMobil] : '');
  rowData[colJenis] = valJenis !== '' ? valJenis : (existingRow && colJenis < existingRow.length ? existingRow[colJenis] : '');
  if (colNipNama !== -1) rowData[colNipNama] = valNipNama !== '' ? valNipNama : (existingRow && colNipNama < existingRow.length ? existingRow[colNipNama] : '');
  rowData[colNotif] = valNotif !== '' ? valNotif : (existingRow && colNotif < existingRow.length ? existingRow[colNotif] : '');
  rowData[colOrder] = valOrder !== '' ? valOrder : (existingRow && colOrder < existingRow.length ? existingRow[colOrder] : '');
  if (colApprovel !== -1) rowData[colApprovel] = valApprovel !== '' ? valApprovel : (existingRow && colApprovel < existingRow.length ? existingRow[colApprovel] : 'Menunggu');
  if (colKm !== -1) rowData[colKm] = valKm !== '' ? valKm : (existingRow && colKm < existingRow.length ? existingRow[colKm] : '');
  rowData[colDepan] = photoDepan;
  rowData[colLh] = photoLh;
  rowData[colInterior] = photoInterior;
  rowData[colBelakang] = photoBelakang;
  rowData[colRh] = photoRh;

  var resObj = { 
    status: 'success',
    success: true, 
    action: targetRow > 0 ? 'updated' : 'appended', 
    row: targetRow > 0 ? targetRow : (sheet.getLastRow() + 1), 
    sheet: sheet.getName(),
    id: idVal,
    photoDepan: photoDepan,
    photoSampingLh: photoLh,
    photoInterior: photoInterior,
    photoBelakang: photoBelakang,
    photoSampingRh: photoRh,
    photoError: lastPhotoError,
    warning: lastPhotoError,
    driveUrls: {
      photoDepan: photoDepan,
      photoSampingLh: photoLh,
      photoInterior: photoInterior,
      photoBelakang: photoBelakang,
      photoSampingRh: photoRh,
      photo1: photoDepan,
      photo2: photoLh,
      photo3: photoInterior,
      photo4: photoBelakang,
      photo5: photoRh
    },
    message: targetRow > 0 ? 
      ('Data Mobil #' + (valMobil || idVal) + ' (baris ' + targetRow + ') berhasil diperbarui di Google Sheets!') : 
      ('Data Mobil #' + (valMobil || idVal) + ' berhasil disimpan sebagai baris baru di Google Sheets!')
  };

  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowData.length).setValues([rowData]);
    return resObj;
  } else {
    sheet.appendRow(rowData);
    return resObj;
  }
}

// Hapus Data Pengerjaan Keindahan dari Google Sheets dan Google Drive
function deletePengerjaanKeindahan(params) {
  params = params || {};
  var ss = getSpreadsheetKeindahan(params.spreadsheetId);
  var targetSheetName = params.sheetName || "SERVICE_KEINDAHAN";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("SERVICE_KEINDAHAN") || 
              ss.getSheetByName("SERVICE KEINDAHAN") || 
              ss.getSheets()[0];

  var lastRow = sheet.getLastRow();
  if (lastRow <= 1) {
    return {
      status: 'error',
      success: false,
      message: 'Sheet kosong atau tidak ada data yang dapat dihapus.'
    };
  }

  var data = sheet.getDataRange().getValues();
  var header = data[0].map(function(h) {
    return String(h || '').trim().toLowerCase().replace(/[^a-z0-9]/g, '');
  });

  var colId = header.indexOf('id') !== -1 ? header.indexOf('id') : header.indexOf('no');
  var colMobil = -1;
  var colOrder = -1;
  var colTanggal = -1;
  var colNotif = -1;
  var photoCols = [];

  for (var c = 0; c < header.length; c++) {
    var h = header[c];
    if (colMobil === -1 && (h.indexOf('mobil') !== -1 || h.indexOf('vehicle') !== -1 || h.indexOf('nopol') !== -1)) {
      colMobil = c;
    }
    if (colOrder === -1 && h.indexOf('order') !== -1) {
      colOrder = c;
    }
    if (colTanggal === -1 && (h.indexOf('tanggal') !== -1 || h.indexOf('tgl') !== -1 || h.indexOf('date') !== -1)) {
      colTanggal = c;
    }
    if (colNotif === -1 && (h.indexOf('notif') !== -1 || h.indexOf('desc') !== -1)) {
      colNotif = c;
    }
    if (h.indexOf('photo') !== -1 || h.indexOf('foto') !== -1 || h.indexOf('depan') !== -1 || h.indexOf('samping') !== -1 || h.indexOf('interior') !== -1 || h.indexOf('belakang') !== -1) {
      photoCols.push(c);
    }
  }

  if (colId === -1) colId = 0;
  if (colMobil === -1) colMobil = (data[0].length >= 3) ? 2 : 1;
  if (colOrder === -1) colOrder = (data[0].length >= 6) ? 5 : 1;
  if (colTanggal === -1) colTanggal = (data[0].length >= 2) ? 1 : -1;
  if (photoCols.length === 0 && data[0].length >= 12) {
    photoCols = [7, 8, 9, 10, 11];
  }

  function cleanStr(s) {
    return String(s || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  var targetId = String(params.id || '').trim();
  var targetNoMobil = cleanStr(params.noMobil);
  var targetOrder = cleanStr(params.order || params.orderNo);
  var targetTanggal = cleanStr(params.tanggal);
  var targetRow = -1;
  var matchedData = null;

  // 1. Cari berdasarkan ID & No Mobil (Paling Akurat)
  if (targetId && targetId !== '0') {
    for (var i = data.length - 1; i >= 1; i--) {
      var rId = String(data[i][colId] || '').trim();
      var rMobMatch = cleanStr(data[i][colMobil]);
      if (rId === targetId && (!targetNoMobil || rMobMatch === targetNoMobil)) {
        targetRow = i + 1;
        matchedData = data[i];
        break;
      }
    }
  }

  // 2. Cari berdasarkan No Mobil + Order
  if (targetRow === -1 && targetNoMobil && targetOrder) {
    for (var j = data.length - 1; j >= 1; j--) {
      var rMobil = cleanStr(data[j][colMobil]);
      var rOrder = cleanStr(data[j][colOrder]);
      if (rMobil === targetNoMobil && rOrder === targetOrder) {
        targetRow = j + 1;
        matchedData = data[j];
        break;
      }
    }
  }

  // 3. Cari berdasarkan No Mobil + Tanggal
  if (targetRow === -1 && targetNoMobil && targetTanggal && colTanggal !== -1) {
    for (var m = data.length - 1; m >= 1; m--) {
      var rMob2 = cleanStr(data[m][colMobil]);
      var rTgl2 = cleanStr(data[m][colTanggal]);
      if (rMob2 === targetNoMobil && rTgl2 === targetTanggal) {
        targetRow = m + 1;
        matchedData = data[m];
        break;
      }
    }
  }

  // 4. Cari berdasarkan ID saja
  if (targetRow === -1 && targetId && targetId !== '0') {
    for (var n = data.length - 1; n >= 1; n--) {
      var rIdOnly = String(data[n][colId] || '').trim();
      if (rIdOnly === targetId) {
        targetRow = n + 1;
        matchedData = data[n];
        break;
      }
    }
  }

  // 5. Cari berdasarkan No Mobil saja (data terakhir mobil ini)
  if (targetRow === -1 && targetNoMobil) {
    for (var k = data.length - 1; k >= 1; k--) {
      var rMob = cleanStr(data[k][colMobil]);
      if (rMob === targetNoMobil) {
        targetRow = k + 1;
        matchedData = data[k];
        break;
      }
    }
  }

  if (targetRow === -1) {
    return {
      status: 'not_found',
      success: false,
      message: 'Data Mobil #' + (params.noMobil || targetId) + ' tidak ditemukan di Google Sheets ' + targetSheetName
    };
  }

  // Hapus foto terkait di Google Drive jika diminta (default true)
  var shouldDeletePhotos = params.deleteDrivePhotos !== false && params.deleteDrivePhotos !== 'false';
  var deletedPhotosCount = 0;
  if (shouldDeletePhotos) {
    var photoUrlsToDelete = [];
    if (matchedData) {
      for (var p = 0; p < photoCols.length; p++) {
        var val = matchedData[photoCols[p]];
        if (val && String(val).indexOf('http') === 0) {
          photoUrlsToDelete.push(String(val));
        }
      }
    }
    // Masukkan juga photoUrls dari parameter request jika ada
    if (params.photoUrls && Array.isArray(params.photoUrls)) {
      for (var u = 0; u < params.photoUrls.length; u++) {
        var pu = String(params.photoUrls[u] || '');
        if (pu.indexOf('http') === 0 && photoUrlsToDelete.indexOf(pu) === -1) {
          photoUrlsToDelete.push(pu);
        }
      }
    }

    for (var d = 0; d < photoUrlsToDelete.length; d++) {
      try {
        var fileId = extractDriveFileId(photoUrlsToDelete[d]);
        if (fileId) {
          var driveFile = DriveApp.getFileById(fileId);
          driveFile.setTrashed(true);
          deletedPhotosCount++;
          Logger.log("✅ Foto Drive dihapus: " + fileId);
        }
      } catch (eDrive) {
        Logger.log("Peringatan hapus foto Drive (" + photoUrlsToDelete[d] + "): " + eDrive.message);
      }
    }
  }

  // Hapus baris dari Google Sheet
  sheet.deleteRow(targetRow);
  Logger.log("✅ Baris ke-" + targetRow + " berhasil dihapus dari sheet " + targetSheetName);

  return {
    status: 'success',
    success: true,
    deleted: true,
    row: targetRow,
    deletedId: targetId,
    deletedPhotosCount: deletedPhotosCount,
    message: 'Data Mobil #' + (params.noMobil || targetId) + ' (baris ' + targetRow + ') dan ' + deletedPhotosCount + ' foto di Google Drive berhasil dihapus!'
  };
}

// ============================================================================
// 6b. INPUT AUDIT KENDARAAN (SINKRONISASI BASE64, DRIVE, DAN GOOGLE SHEETS)
// ============================================================================
function saveOrUpdateAudit(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "INPUT_AUDIT";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("INPUT_AUDIT") || 
              ss.getSheetByName("DATA_AUDIT") ||
              ss.getSheetByName("AUDIT");
  
  if (!sheet) {
    sheet = ss.insertSheet(targetSheetName);
  }

  // Buat header otomatis jika sheet masih kosong
  if (sheet.getLastRow() === 0) {
    var headerRow = [
      'ID', 'TIMESTAMP', 'TANGGAL', 'NIP', 'NAMA PETUGAS', 'NO MOBIL', 'PLAT NOMER', 
      'TIPE', 'WARNA', 'TAHUN', 'POOL', 'KPI_POIN', 'CATATAN'
    ];
    for (var p = 1; p <= 20; p++) {
      headerRow.push('PHOTO' + p);
      headerRow.push('CATATAN_PHOTO' + p);
    }
    sheet.appendRow(headerRow);
  }

  var data = sheet.getDataRange().getValues();
  var noMobil = String(item.noMobil || item.nomobil || '').trim().toUpperCase();
  var photosMap = item.photosMap || {};
  var driveUrls = {};

  // Decode Base64 & simpan ke Google Drive folder FOTO_AUDIT_KENDARAAN
  function processAuditPhoto(rawVal, slotName) {
    if (!rawVal) return '';
    var str = String(rawVal).trim();
    if (str.indexOf('http://') === 0 || str.indexOf('https://') === 0) {
      return str;
    }
    if (str.length < 50) return '';
    try {
      var cleanSlot = String(slotName || 'foto').toLowerCase().replace(/[^a-z0-9_]/g, '_');
      var cleanMob = noMobil.replace(/[^a-zA-Z0-9]/g, '');
      var fileName = 'KENDARAAN_' + (cleanMob || 'MOBIL') + '_' + cleanSlot + '_' + new Date().getTime() + '.jpg';
      var folderId = item.folderId || FOLDER_PHOTO_KEINDAHAN_ID;
      var folderName = item.folderName || 'FOTO_AUDIT_KENDARAAN';
      var directLink = saveBase64ImageToDrive(str, fileName, folderId, folderName);
      if (directLink && directLink.indexOf('http') === 0) {
        return directLink;
      }
    } catch (errUpload) {
      Logger.log("processAuditPhoto error: " + errUpload.message);
    }
    return '';
  }

  // Loop photo1 s/d photo20 dan photosMap
  for (var i = 1; i <= 20; i++) {
    var pKey = 'photo' + i;
    var rawPhoto = item[pKey] || photosMap[pKey] || photosMap['slot' + i];
    if (rawPhoto) {
      var dLink = processAuditPhoto(rawPhoto, pKey);
      if (dLink) {
        driveUrls[pKey] = dLink;
        item[pKey] = dLink;
      }
    }
  }

  for (var customKey in photosMap) {
    if (photosMap.hasOwnProperty(customKey)) {
      if (!driveUrls[customKey]) {
        var cLink = processAuditPhoto(photosMap[customKey], customKey);
        if (cLink) driveUrls[customKey] = cLink;
      }
    }
  }

  // Cari baris yang bersangkutan di sheet INPUT_AUDIT
  var targetRow = -1;
  var targetId = String(item.id || '').trim();
  var targetTanggal = String(item.tanggal || '').trim();
  var isEdit = item.isEdit === true || item.isEdit === 'true' || item.mode === 'edit';

  if (data.length > 1) {
    // 1. Cari berdasarkan ID jika ada
    if (targetId && targetId !== '0') {
      for (var r = data.length - 1; r >= 1; r--) {
        if (String(data[r][0] || '').trim() === targetId) {
          targetRow = r + 1;
          break;
        }
      }
    }
    // 2. Cari berdasarkan No Mobil + Tanggal
    if (targetRow === -1 && noMobil && targetTanggal) {
      for (var r2 = data.length - 1; r2 >= 1; r2--) {
        var rowMob = String(data[r2][5] || '').toUpperCase().replace(/[^a-zA-Z0-9]/g, '');
        var rowTgl = String(data[r2][2] || '').trim();
        if (rowMob === noMobil.replace(/[^a-zA-Z0-9]/g, '') && rowTgl === targetTanggal) {
          targetRow = r2 + 1;
          break;
        }
      }
    }
    // 3. Cari berdasarkan No Mobil jika mode edit
    if (targetRow === -1 && isEdit && noMobil) {
      for (var r3 = data.length - 1; r3 >= 1; r3--) {
        var rowMob3 = String(data[r3][5] || '').toUpperCase().replace(/[^a-zA-Z0-9]/g, '');
        if (rowMob3 === noMobil.replace(/[^a-zA-Z0-9]/g, '')) {
          targetRow = r3 + 1;
          break;
        }
      }
    }
  }

  var existingRowData = targetRow > 0 && data[targetRow - 1] ? data[targetRow - 1] : null;
  var newId = (targetRow > 0 && existingRowData) ? (existingRowData[0] || targetId || 1) : ((data.length > 1) ? data.length : 1);
  var timestampStr = item.timestamp || (existingRowData ? existingRowData[1] : Utilities.formatDate(new Date(), Session.getScriptTimeZone() || 'Asia/Jakarta', 'yyyy-MM-dd HH:mm:ss'));
  var rowValues = [
    item.id || newId,
    timestampStr,
    item.tanggal || (existingRowData ? existingRowData[2] : ''),
    item.nip || (existingRowData ? existingRowData[3] : ''),
    item.namaPetugas || item.namaMekanik || (existingRowData ? existingRowData[4] : ''),
    noMobil || (existingRowData ? existingRowData[5] : ''),
    item.platNomer || item.noPolisi || (existingRowData ? existingRowData[6] : ''),
    item.tipe || (existingRowData ? existingRowData[7] : ''),
    item.warna || (existingRowData ? existingRowData[8] : ''),
    item.tahun || (existingRowData ? existingRowData[9] : ''),
    item.pool || (existingRowData ? existingRowData[10] : 'BENGKEL-PUSAT'),
    item.kpiPoin || item.kpi || (existingRowData ? existingRowData[11] : ''),
    item.catatan || item.keterangan || (existingRowData ? existingRowData[12] : '')
  ];

  for (var s = 1; s <= 20; s++) {
    var pColIdx = 13 + ((s - 1) * 2);
    var cColIdx = pColIdx + 1;
    var photoVal = driveUrls['photo' + s] || item['photo' + s] || '';
    if (!photoVal && existingRowData && pColIdx < existingRowData.length) {
      photoVal = existingRowData[pColIdx] || '';
    }
    var catatanVal = item['catatanPhoto' + s] || item['catatan' + s] || '';
    if (!catatanVal && existingRowData && cColIdx < existingRowData.length) {
      catatanVal = existingRowData[cColIdx] || '';
    }
    rowValues.push(photoVal);
    rowValues.push(catatanVal);
  }

  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowValues.length).setValues([rowValues]);
  } else {
    sheet.appendRow(rowValues);
  }

  return {
    status: 'success',
    success: true,
    action: targetRow > 0 ? 'updated' : 'appended',
    row: targetRow > 0 ? targetRow : (sheet.getLastRow()),
    id: item.id || newId,
    noMobil: noMobil,
    driveUrls: driveUrls,
    message: targetRow > 0 ? 
      ('Data audit Mobil #' + noMobil + ' (baris ' + targetRow + ') berhasil diperbarui di Google Sheets!') : 
      ('Data audit Mobil #' + noMobil + ' & foto berhasil disimpan ke Google Drive (FOTO_AUDIT_KENDARAAN) dan sheet ' + targetSheetName)
  };
}

// ============================================================================
// 7. LAPORAN KENDARAAN (LAPORAN_HARIAN)
// ============================================================================
function saveOrUpdateLaporan(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "LAPORAN KENDARAAN";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("LAPORAN KENDARAAN") || 
              ss.getSheetByName("LAPORAN_KENDARAAN") || 
              ss.getSheetByName("LAPORAN HARIAN") || 
              ss.getSheetByName("LAPORAN_HARIAN") || 
              ss.getSheets()[0];
  var lastRow = sheet.getLastRow();
  var targetRow = -1;

  function cleanStr(s) {
    return String(s || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  // Header default jika kosong
  if (lastRow === 0) {
    sheet.appendRow([
      'ID', 'Timestamp', 'NO MOBIL', 'NAMA/NIP', 'KELUHAN KENDARAAN',
      'KODE AKSES', 'TOKEN', 'STATUS', 'UPDATE DATA', 'POOL/VENDOR',
      'NAMA MEKANIK', 'PENGERJAAN'
    ]);
    lastRow = 1;
  }

  var firstHeader = lastRow >= 1 ? String(sheet.getRange(1, 1).getDisplayValue() || '').trim().toUpperCase() : 'ID';
  var hasIdCol = !(firstHeader === 'TIMESTAMP' || firstHeader === 'WAKTU');
  var colId = hasIdCol ? 1 : 0;
  var colTs = hasIdCol ? 2 : 1;
  var colMobil = hasIdCol ? 3 : 2;
  var colNamaNip = hasIdCol ? 4 : 3;
  var colKeluhan = hasIdCol ? 5 : 4;
  var colKodeAkses = hasIdCol ? 6 : 5;
  var colToken = hasIdCol ? 7 : 6;
  var colStatus = hasIdCol ? 8 : 7;
  var colUpdateData = hasIdCol ? 9 : 8;
  var colPoolVendor = hasIdCol ? 10 : 9;
  var colNamaMekanik = hasIdCol ? 11 : 10;
  var colPengerjaan = hasIdCol ? 12 : 11;

  var displayData = sheet.getDataRange().getDisplayValues();
  var targetId = String(item.id || '').trim();
  var targetCleanMobil = cleanStr(item.noMobil);
  var targetTimestamp = String(item.timestamp || '').trim();
  var isEdit = item.isEdit === true || item.isEdit === 'true' || item.mode === 'edit';

  if (targetId && targetId !== '0' && hasIdCol && displayData.length > 1) {
    for (var i = displayData.length - 1; i >= 1; i--) {
      var rowId = String(displayData[i][colId - 1] || '').trim();
      if (rowId === targetId) {
        targetRow = i + 1;
        break;
      }
    }
  }

  if (targetRow === -1 && targetCleanMobil && displayData.length > 1) {
    if (targetTimestamp) {
      for (var i = displayData.length - 1; i >= 1; i--) {
        var rTs = String(displayData[i][colTs - 1] || '').trim();
        var rMob = cleanStr(displayData[i][colMobil - 1]);
        if (rMob === targetCleanMobil && (rTs === targetTimestamp || cleanStr(rTs) === cleanStr(targetTimestamp))) {
          targetRow = i + 1;
          break;
        }
      }
    }

    if (targetRow === -1) {
      for (var k = displayData.length - 1; k >= 1; k--) {
        var rMob3 = cleanStr(displayData[k][colMobil - 1]);
        var rStat = String(displayData[k][colStatus - 1] || '').toUpperCase();
        if (rMob3 === targetCleanMobil && rStat.indexOf('SELESAI') === -1) {
          targetRow = k + 1;
          break;
        }
      }
    }
  }

  var nowStr = Utilities.formatDate(new Date(), 'GMT+7', 'yyyy-MM-dd HH:mm:ss');
  var updateDataStr = item.updateData || nowStr;

  if (targetRow !== -1) {
    if (hasIdCol) {
      var existingId = String(sheet.getRange(targetRow, colId).getDisplayValue() || '').trim();
      if (!existingId && item.id) {
        sheet.getRange(targetRow, colId).setValue(item.id);
      }
    }
    if (!isEdit && item.timestamp) sheet.getRange(targetRow, colTs).setValue(item.timestamp);
    if (item.noMobil !== undefined && item.noMobil !== '') sheet.getRange(targetRow, colMobil).setValue(item.noMobil);
    if (item.namaNip !== undefined && item.namaNip !== '') sheet.getRange(targetRow, colNamaNip).setValue(item.namaNip);
    if (item.keluhanKendaraan !== undefined && item.keluhanKendaraan !== '') sheet.getRange(targetRow, colKeluhan).setValue(item.keluhanKendaraan);
    if (item.kodeAkses !== undefined) sheet.getRange(targetRow, colKodeAkses).setValue(item.kodeAkses);
    if (item.token !== undefined) sheet.getRange(targetRow, colToken).setValue(item.token);
    if (item.status !== undefined) sheet.getRange(targetRow, colStatus).setValue(item.status);
    sheet.getRange(targetRow, colUpdateData).setValue(updateDataStr);
    if (item.poolVendor !== undefined) sheet.getRange(targetRow, colPoolVendor).setValue(item.poolVendor);
    if (item.namaMekanik !== undefined) sheet.getRange(targetRow, colNamaMekanik).setValue(item.namaMekanik);
    if (item.pengerjaan !== undefined) sheet.getRange(targetRow, colPengerjaan).setValue(item.pengerjaan);

    return { 
      success: true, 
      status: 'success', 
      action: 'updated', 
      row: targetRow, 
      id: item.id || (targetRow - 1), 
      noMobil: item.noMobil, 
      status: item.status, 
      sheet: sheet.getName() 
    };
  } else {
    var newTimestamp = targetTimestamp || nowStr;
    var newId = (item.id && Number(item.id) > 0) ? item.id : Math.max(1, lastRow);
    var newRowValues = hasIdCol ? [
      newId,
      newTimestamp,
      item.noMobil || '',
      item.namaNip || '',
      item.keluhanKendaraan || '',
      item.kodeAkses || '',
      item.token || '',
      item.status || 'Belum Dikerjakan',
      updateDataStr,
      item.poolVendor || '',
      item.namaMekanik || '',
      item.pengerjaan || ''
    ] : [
      newTimestamp,
      item.noMobil || '',
      item.namaNip || '',
      item.keluhanKendaraan || '',
      item.kodeAkses || '',
      item.token || '',
      item.status || 'Belum Dikerjakan',
      updateDataStr,
      item.poolVendor || '',
      item.namaMekanik || '',
      item.pengerjaan || ''
    ];

    sheet.appendRow(newRowValues);
    var appendedRow = sheet.getLastRow();
    return { 
      success: true, 
      status: 'success', 
      action: 'appended', 
      row: appendedRow, 
      id: newId, 
      noMobil: item.noMobil, 
      status: item.status || 'Belum Dikerjakan', 
      sheet: sheet.getName() 
    };
  }
}

// ============================================================================
// 8. SERVICE TEKNIK
// ============================================================================
function appendServiceTeknik(item) {
  item = item || {};
  var ss = getSpreadsheetTeknik(item.spreadsheetId);
  var targetSheetName = item.sheetName || "SERVICE_TEKNIK";
  var sheet = ss.getSheetByName(targetSheetName) || ss.getSheetByName("SERVICE_TEKNIK") || ss.getSheetByName("SERVICE TEKNIK") || ss.getSheets()[0];
  var nowStr = Utilities.formatDate(new Date(), 'GMT+7', 'yyyy-MM-dd HH:mm:ss');
  
  sheet.appendRow([
    nowStr,
    item.noPolisi || item.noMobil || '',
    item.orderNo || item.order || '',
    item.notifNo || '',
    item.tanggalMasuk || nowStr,
    item.tanggalSelesai || '',
    item.activity || '',
    item.keterangan || '',
    item.mekanik || '',
    item.sa || '',
    item.km || '',
    item.kmJatuhTempo || '',
    item.equipmentNo || '',
    item.notifDate || nowStr,
    item.kmTotal || '',
    item.stickerBandara || ''
  ]);
  return { success: true, action: 'appended', row: sheet.getLastRow() };
}

// ============================================================================
// 9. MASTER USER BENGKEL
// ============================================================================
function saveOrUpdateUser(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "USER";
  var sheet = ss.getSheetByName(targetSheetName) || ss.getSheetByName("USER") || ss.getSheetByName("user") || ss.getSheets()[0];
  if (sheet.getLastRow() === 0) {
    sheet.appendRow(['NIP', 'NAMA_MEKANIK', 'USERNAMA', 'PASSWORD', 'LEVELAKSES']);
  }
  var data = sheet.getDataRange().getValues();
  var targetRow = -1;
  var targetNip = String(item.nip || '').trim().toLowerCase();
  var origNip = String(item.originalNip || item.nip || '').trim().toLowerCase();
  var targetUsername = String(item.username || '').trim().toLowerCase();
  var origUsername = String(item.originalUsername || item.username || '').trim().toLowerCase();

  if (data.length > 1) {
    for (var i = 1; i < data.length; i++) {
      var rNip = String(data[i][0] || '').trim().toLowerCase();
      var rUser = String(data[i][2] || '').trim().toLowerCase();
      if ((origNip && rNip === origNip) || (origUsername && rUser === origUsername) || (targetNip && rNip === targetNip) || (targetUsername && rUser === targetUsername)) {
        targetRow = i + 1;
        break;
      }
    }
  }

  var rowData = [
    item.nip || '',
    item.namaMekanik || '',
    item.username || '',
    item.password || '',
    item.levelAkses || item.level || ''
  ];

  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowData.length).setValues([rowData]);
    return { success: true, action: 'updated', row: targetRow, sheet: sheet.getName() };
  } else {
    sheet.appendRow(rowData);
    return { success: true, action: 'appended', row: sheet.getLastRow(), sheet: sheet.getName() };
  }
}

function deleteUser(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "USER";
  var sheet = ss.getSheetByName(targetSheetName) || ss.getSheetByName("USER") || ss.getSheetByName("user") || ss.getSheets()[0];
  var data = sheet.getDataRange().getValues();
  var targetNip = String(item.nip || '').trim().toLowerCase();
  var targetUsername = String(item.username || '').trim().toLowerCase();

  if (data.length > 1) {
    for (var i = data.length - 1; i >= 1; i--) {
      var rNip = String(data[i][0] || '').trim().toLowerCase();
      var rUser = String(data[i][2] || '').trim().toLowerCase();
      if ((targetNip && rNip === targetNip) || (targetUsername && rUser === targetUsername)) {
        sheet.deleteRow(i + 1);
        return { success: true, action: 'deleted', row: i + 1, sheet: sheet.getName() };
      }
    }
  }
  return { success: false, action: 'not_found', message: 'User tidak ditemukan di Google Sheets' };
}

// ============================================================================
// 10. PENGERJAAN CAT BENGKEL (14 KOLOM RESMI & 6 FOTO DRIVE)
// ============================================================================
function saveOrUpdatePengerjaanCat(item) {
  item = item || {};
  var ss = getSpreadsheetCat(item.spreadsheetId);
  var targetSheetName = item.sheetName || "PENGERJAAN_CAT";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("PENGERJAAN_CAT") || 
              ss.getSheetByName("PENGERJAAN CAT") || 
              ss.getSheetByName("Form Responses 1") || 
              ss.getSheets()[0];
  var data = sheet.getDataRange().getValues();
  var targetRow = -1;

  function cleanStr(s) {
    return String(s || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  var targetId = String(item.id || item.idUniq || '').trim();
  var targetTimestamp = String(item.timestamp || '').trim();
  var targetNoMobil = cleanStr(item.noMobil);
  var targetNoReg = cleanStr(item.noRegistrasi || item.noRegistrasiCat);
  var targetTanggal = String(item.tanggal || '').trim();

  // Deteksi kolom secara dinamis dari Header
  var header = data.length > 0 ? data[0] : [];
  var firstHeader = header.length > 0 ? String(header[0] || '').trim().toUpperCase() : '';
  var is14Col = (firstHeader === 'ID' || firstHeader.indexOf('KODE') !== -1 || firstHeader.indexOf('UNIQ') !== -1) || header.length >= 14;

  var colId = is14Col ? 0 : -1;
  var colTs = is14Col ? 1 : 0;
  var colTgl = is14Col ? 2 : 1;
  var colMobil = is14Col ? 3 : 2;
  var colKerusakan = is14Col ? 4 : 3;
  var colPhotoKerusakan = is14Col ? 5 : 4;
  var colFotoNotif = is14Col ? 6 : 5;
  var colFotoHasil = is14Col ? 7 : 6;
  var colGambar = is14Col ? 8 : 7;
  var colMekanik = is14Col ? 9 : 8;
  var colNoReg = is14Col ? 10 : 9;
  var colPhotoLasKetok = is14Col ? 11 : 10;
  var colFotoHasil2 = is14Col ? 12 : 11;
  var colFotoHasil3 = is14Col ? 13 : 12;

  if (data.length > 0) {
    for (var c = 0; c < header.length; c++) {
      var h = String(header[c] || '').trim().toUpperCase().replace(/[^A-Z0-9]/g, '');
      if (h === 'ID' || h.indexOf('KODEUNIQ') !== -1 || h === 'UNIQ') colId = c;
      else if (h.indexOf('TIMESTAMP') !== -1 || h.indexOf('WAKTU') !== -1) colTs = c;
      else if (h.indexOf('TANGGAL') !== -1 || h === 'TGL') colTgl = c;
      else if (h.indexOf('NOMOBIL') !== -1 || h === 'MOBIL') colMobil = c;
      else if (h.indexOf('KERUSAKAN') !== -1) colKerusakan = c;
      else if (h.indexOf('PHOTOKERUSAKAN') !== -1 || h.indexOf('FOTOKERUSAKAN') !== -1) colPhotoKerusakan = c;
      else if (h.indexOf('FOTONOTIF') !== -1 || h.indexOf('NOTIF') !== -1) colFotoNotif = c;
      else if (h === 'FOTOHASILPENGECATAN' || (h.indexOf('HASIL') !== -1 && h.indexOf('2') === -1 && h.indexOf('3') === -1 && h.indexOf('GAMBAR') === -1)) colFotoHasil = c;
      else if (h === 'GAMBAR' || (h.indexOf('GAMBAR') !== -1 && h.indexOf('2') === -1 && h.indexOf('3') === -1)) colGambar = c;
      else if (h.indexOf('MEKANIK') !== -1 || h.indexOf('PETUGAS') !== -1) colMekanik = c;
      else if (h.indexOf('REGISTRASI') !== -1) colNoReg = c;
      else if (h.indexOf('LAS') !== -1 || h.indexOf('KETOK') !== -1) colPhotoLasKetok = c;
      else if (h.indexOf('HASIL2') !== -1 || h.indexOf('PENGECATAN2') !== -1) colFotoHasil2 = c;
      else if (h.indexOf('HASIL3') !== -1 || h.indexOf('PENGECATAN3') !== -1) colFotoHasil3 = c;
    }
  }

  var isEdit = (item.isEdit === true || String(item.isEdit).toLowerCase() === 'true');

  // Kumpulkan daftar ID yang sudah ada di sheet untuk mencegah tabrakan/penimpaan ID
  var existingIds = {};
  if (colId >= 0 && data.length > 1) {
    for (var r = 1; r < data.length; r++) {
      var valId = String(data[r][colId] || '').trim();
      if (valId) existingIds[valId] = true;
    }
  }

  if (isEdit) {
    // Mode EDIT: cari baris yang akan diperbarui
    // 1. Cari baris berdasarkan ID unik
    if (targetId && colId >= 0 && data.length > 1) {
      for (var i = data.length - 1; i >= 1; i--) {
        var rId = String(data[i][colId] || '').trim();
        if (rId === targetId) {
          targetRow = i + 1;
          break;
        }
      }
    }

    // 2. Cari baris berdasarkan Timestamp & No Mobil lama
    var searchTs = String(item.oldTimestamp || item.timestamp || '').trim();
    var searchMob = cleanStr(item.oldNoMobil || item.noMobil);
    if (targetRow === -1 && searchTs && searchMob && colTs >= 0 && colMobil >= 0 && data.length > 1) {
      for (var i = data.length - 1; i >= 1; i--) {
        var rTimestamp = String(data[i][colTs] || '').trim();
        var rMobil = cleanStr(data[i][colMobil]);
        if (rTimestamp === searchTs && rMobil === searchMob) {
          targetRow = i + 1;
          break;
        }
      }
    }

    // 3. Cari baris berdasarkan No Registrasi Cat lama
    var searchReg = cleanStr(item.oldNoRegistrasi || item.noRegistrasi || item.noRegistrasiCat);
    if (targetRow === -1 && searchReg && colNoReg >= 0 && data.length > 1) {
      for (var j = data.length - 1; j >= 1; j--) {
        var rReg = cleanStr(data[j][colNoReg]);
        if (rReg === searchReg) {
          targetRow = j + 1;
          break;
        }
      }
    }
  } else {
    // Mode PENAMBAHAN DATA BARU:
    // Pastikan TIDAK MENIMPA baris manapun di sheet (targetRow tetap -1)
    targetRow = -1;
  }

  var nowStr = Utilities.formatDate(new Date(), 'GMT+7', 'yyyy-MM-dd HH:mm:ss');
  var ts = item.timestamp || nowStr;
  var mobClean = String(item.noMobil || 'MOBIL').toUpperCase().replace(/[^a-zA-Z0-9]/g, '');

  var photoKerusakanVal = item.photoKerusakan || '';
  if (photoKerusakanVal && photoKerusakanVal.indexOf('http') !== 0 && (photoKerusakanVal.indexOf(';base64,') !== -1 || photoKerusakanVal.indexOf('data:') === 0 || photoKerusakanVal.length > 200)) {
    try {
      photoKerusakanVal = saveBase64ImageToDrive(photoKerusakanVal, 'CAT_' + mobClean + '_KERUSAKAN_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePK) {}
  }

  var photoNotifVal = item.photoNotif || '';
  if (photoNotifVal && photoNotifVal.indexOf('http') !== 0 && (photoNotifVal.indexOf(';base64,') !== -1 || photoNotifVal.indexOf('data:') === 0 || photoNotifVal.length > 200)) {
    try {
      photoNotifVal = saveBase64ImageToDrive(photoNotifVal, 'CAT_' + mobClean + '_NOTIF_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePN) {}
  }

  var gambarVal = item.gambar || item.gambarFotoHasilPengecatan || '';
  if (gambarVal && gambarVal.indexOf('http') !== 0 && (gambarVal.indexOf(';base64,') !== -1 || gambarVal.indexOf('data:') === 0 || gambarVal.length > 200)) {
    try {
      gambarVal = saveBase64ImageToDrive(gambarVal, 'CAT_' + mobClean + '_HASIL_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePG) {}
  }

  var photoLasKetokVal = item.photoLasKetok || '';
  if (photoLasKetokVal && photoLasKetokVal.indexOf('http') !== 0 && (photoLasKetokVal.indexOf(';base64,') !== -1 || photoLasKetokVal.indexOf('data:') === 0 || photoLasKetokVal.length > 200)) {
    try {
      photoLasKetokVal = saveBase64ImageToDrive(photoLasKetokVal, 'CAT_' + mobClean + '_LASKETOK_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePLK) {}
  }

  var fotoHasil2Val = item.fotoHasilPengecatan2 || item.fotoHasil2 || '';
  if (fotoHasil2Val && fotoHasil2Val.indexOf('http') !== 0 && (fotoHasil2Val.indexOf(';base64,') !== -1 || fotoHasil2Val.indexOf('data:') === 0 || fotoHasil2Val.length > 200)) {
    try {
      fotoHasil2Val = saveBase64ImageToDrive(fotoHasil2Val, 'CAT_' + mobClean + '_HASIL2_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePH2) {}
  }

  var fotoHasil3Val = item.fotoHasilPengecatan3 || item.fotoHasil3 || '';
  if (fotoHasil3Val && fotoHasil3Val.indexOf('http') !== 0 && (fotoHasil3Val.indexOf(';base64,') !== -1 || fotoHasil3Val.indexOf('data:') === 0 || fotoHasil3Val.length > 200)) {
    try {
      fotoHasil3Val = saveBase64ImageToDrive(fotoHasil3Val, 'CAT_' + mobClean + '_HASIL3_' + new Date().getTime() + '.jpg', item.folderId, 'FOTO_AUDIT_KENDARAAN');
    } catch (ePH3) {}
  }

  var officialHeaders = [
    'ID (kode uniq)', 'Timestamp', 'TANGGAL', 'NO MOBIL', 'KERUSAKAN',
    'PHOTO KERUSAKAN', 'FOTO NOTIF', 'FOTO HASIL PENGECATAN', 'GAMBAR', 'MEKANIK',
    'NO REGISTRASI CAT', 'PHOTO LAS KETOK', 'FOTO HASIL PENGECATAN_2', 'FOTO HASIL PENGECATAN_3'
  ];

  if (sheet.getLastRow() === 0) {
    sheet.appendRow(officialHeaders);
    try {
      var hr = sheet.getRange(1, 1, 1, officialHeaders.length);
      hr.setBackground('#1565C0');
      hr.setFontColor('#FFFFFF');
      hr.setFontWeight('bold');
      sheet.setFrozenRows(1);
    } catch (eFmt) {}
  }

  // Tentukan idVal secara aman:
  var idVal = String(item.id || item.idUniq || '').trim();
  if (!idVal || idVal === '0') {
    idVal = String(new Date().getTime());
  }

  // Jika penambahan data baru dan ID sudah ada di sheet, generate ID baru yang unik agar data lama tidak tertimpa
  if (!isEdit && existingIds[idVal]) {
    var collisionGuard = 0;
    var freshId = idVal;
    while (existingIds[freshId] && collisionGuard < 1000) {
      collisionGuard++;
      freshId = String(new Date().getTime() + collisionGuard);
    }
    idVal = freshId;
  }

  var rowData = is14Col ? [
    idVal,
    ts,
    item.tanggal || '',
    item.noMobil || '',
    item.kerusakan || '',
    photoKerusakanVal,
    photoNotifVal,
    item.fotoHasilPengecatan || '',
    gambarVal,
    item.mekanik || '',
    item.noRegistrasi || item.noRegistrasiCat || '',
    photoLasKetokVal,
    fotoHasil2Val,
    fotoHasil3Val
  ] : [
    ts,
    item.tanggal || '',
    item.noMobil || '',
    item.kerusakan || '',
    photoKerusakanVal,
    photoNotifVal,
    item.fotoHasilPengecatan || '',
    gambarVal,
    item.mekanik || '',
    item.noRegistrasi || item.noRegistrasiCat || '',
    photoLasKetokVal,
    fotoHasil2Val,
    fotoHasil3Val
  ];

  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowData.length).setValues([rowData]);
    return { success: true, action: 'updated', row: targetRow, id: idVal, sheet: sheet.getName() };
  } else {
    sheet.appendRow(rowData);
    return { success: true, action: 'appended', row: sheet.getLastRow(), id: idVal, sheet: sheet.getName() };
  }
}

// Hapus Data Pengerjaan Cat dari Google Sheets
function deletePengerjaanCat(params) {
  params = params || {};
  var ss = getSpreadsheetCat(params.spreadsheetId);
  var targetSheetName = params.sheetName || "PENGERJAAN_CAT";
  var sheet = ss.getSheetByName(targetSheetName) || 
              ss.getSheetByName("PENGERJAAN_CAT") || 
              ss.getSheetByName("PENGERJAAN CAT") || 
              ss.getSheetByName("Form Responses 1") || 
              ss.getSheets()[0];

  var lastRow = sheet.getLastRow();
  if (lastRow <= 1) {
    return { status: 'error', success: false, message: 'Sheet kosong atau tidak ada data yang dapat dihapus.' };
  }

  function cleanStr(s) {
    return String(s || '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }

  var targetId = String(params.id || params.idUniq || '').trim();
  var targetTimestamp = String(params.timestamp || '').trim();
  var targetNoMobil = cleanStr(params.noMobil);
  var targetNoReg = cleanStr(params.noRegistrasi || params.noRegistrasiCat);

  var data = sheet.getDataRange().getValues();
  var header = data[0];
  var colId = 0, colTs = 1, colMobil = 3, colNoReg = 10;
  for (var c = 0; c < header.length; c++) {
    var h = String(header[c] || '').trim().toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (h === 'ID' || h.indexOf('KODEUNIQ') !== -1 || h === 'UNIQ') colId = c;
    else if (h.indexOf('TIMESTAMP') !== -1 || h.indexOf('WAKTU') !== -1) colTs = c;
    else if (h.indexOf('NOMOBIL') !== -1 || h === 'MOBIL') colMobil = c;
    else if (h.indexOf('REGISTRASI') !== -1) colNoReg = c;
  }

  var targetRow = -1;
  if (targetId) {
    for (var i = data.length - 1; i >= 1; i--) {
      if (String(data[i][colId] || '').trim() === targetId) {
        targetRow = i + 1;
        break;
      }
    }
  }

  if (targetRow === -1 && targetTimestamp && targetNoMobil) {
    for (var i = data.length - 1; i >= 1; i--) {
      var rTs = String(data[i][colTs] || '').trim();
      var rMob = cleanStr(data[i][colMobil]);
      if (rTs === targetTimestamp && rMob === targetNoMobil) {
        targetRow = i + 1;
        break;
      }
    }
  }

  if (targetRow === -1 && targetNoReg) {
    for (var j = data.length - 1; j >= 1; j--) {
      if (cleanStr(data[j][colNoReg]) === targetNoReg) {
        targetRow = j + 1;
        break;
      }
    }
  }

  if (targetRow > 0) {
    sheet.deleteRow(targetRow);
    return { status: 'success', success: true, action: 'deleted', row: targetRow, sheet: sheet.getName() };
  }
  return { status: 'not_found', success: false, message: 'Data Pengerjaan Cat tidak ditemukan di Google Sheets.' };
}

// ============================================================================
// 11. PREDIKSI PART BENGKEL
// ============================================================================
function saveOrUpdatePrediksiPart(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "PREDIKSI_PART";
  var sheet = ss.getSheetByName(targetSheetName) || ss.getSheetByName("PREDIKSI_PART") || ss.getSheetByName("PREDIKSI PART") || ss.getSheets()[0];
  
  if (sheet.getLastRow() === 0) {
    sheet.appendRow([
      'ID', 'TANGGAL', 'NO_MOBIL', 'SERVICE', 'KM_MOBIL', 'NIP_NAMA',
      'NAMA_PART', 'PREDIKSI_KM', 'KM_TARGET', 'ARGO_SEKARANG',
      'ESTIMASI_KM', 'APROVEL', 'KETERANGAN', 'STATUS'
    ]);
  }
  
  var data = sheet.getDataRange().getValues();
  var targetRow = -1;
  var targetId = String(item.id || '').trim();
  var targetNoMobil = String(item.noMobil || '').trim().toLowerCase().replace(/^#/, '');
  var targetNamaPart = String(item.namaPart || '').trim().toLowerCase();
  var targetTanggal = String(item.tanggal || '').trim().toLowerCase();

  if (targetId && targetId !== '0' && data.length > 1) {
    for (var i = 1; i < data.length; i++) {
      var rId = String(data[i][0] || '').trim();
      if (rId === targetId) {
        targetRow = i + 1;
        break;
      }
    }
  }

  if (targetRow === -1 && targetNoMobil && targetNamaPart && data.length > 1) {
    for (var i = data.length - 1; i >= 1; i--) {
      var rMob = String(data[i][2] || '').trim().toLowerCase().replace(/^#/, '');
      var rPart = String(data[i][6] || '').trim().toLowerCase();
      var rTgl = String(data[i][1] || '').trim().toLowerCase();
      if (rMob === targetNoMobil && rPart === targetNamaPart && (!targetTanggal || rTgl === targetTanggal)) {
        targetRow = i + 1;
        break;
      }
    }
  }

  var rowId = targetRow > 0 ? (data[targetRow - 1][0] || targetId || targetRow - 1) : (targetId && targetId !== '0' ? targetId : (data.length));
  var kmMobilVal = item.kmMobil !== undefined && item.kmMobil !== null && item.kmMobil !== '' ? item.kmMobil : (item.kmInput || '');

  var rowData = [
    rowId,
    item.tanggal || '',
    item.noMobil || '',
    item.service || '',
    kmMobilVal,
    item.nipNama || '',
    item.namaPart || '',
    item.prediksiKm || '',
    item.kmTarget || '',
    item.argoSekarang || '',
    item.estimasiKm || '',
    item.aprovel || '',
    item.keterangan || '',
    item.status || ''
  ];

  if (targetRow > 0) {
    sheet.getRange(targetRow, 1, 1, rowData.length).setValues([rowData]);
    return { success: true, action: 'updated', row: targetRow, id: rowId, sheet: sheet.getName() };
  } else {
    sheet.appendRow(rowData);
    return { success: true, action: 'appended', row: sheet.getLastRow(), id: rowId, sheet: sheet.getName() };
  }
}

function deletePrediksiPart(item) {
  item = item || {};
  var ss = getSpreadsheetLaporan(item.spreadsheetId);
  var targetSheetName = item.sheetName || "PREDIKSI_PART";
  var sheet = ss.getSheetByName(targetSheetName) || ss.getSheetByName("PREDIKSI_PART") || ss.getSheetByName("PREDIKSI PART") || ss.getSheets()[0];
  var data = sheet.getDataRange().getValues();
  var targetId = String(item.id || '').trim();
  var targetNoMobil = String(item.noMobil || '').trim().toLowerCase().replace(/^#/, '');
  var targetNamaPart = String(item.namaPart || '').trim().toLowerCase();

  if (data.length > 1) {
    for (var i = data.length - 1; i >= 1; i--) {
      var rId = String(data[i][0] || '').trim();
      var rMob = String(data[i][2] || '').trim().toLowerCase().replace(/^#/, '');
      var rPart = String(data[i][6] || '').trim().toLowerCase();
      if ((targetId && targetId !== '0' && rId === targetId) || (targetNoMobil && targetNamaPart && rMob === targetNoMobil && rPart === targetNamaPart)) {
        sheet.deleteRow(i + 1);
        return { success: true, action: 'deleted', row: i + 1, sheet: sheet.getName() };
      }
    }
  }
  return { success: false, action: 'not_found', message: 'Data Prediksi Part tidak ditemukan di Google Sheets' };
}
