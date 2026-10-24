package com.example.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.entity.UserEntity

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bengkel_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NIP = "nip"
        private const val KEY_NAMA_MEKANIK = "nama_mekanik"
        private const val KEY_USERNAME = "username"
        private const val KEY_LEVEL = "level"
        private const val KEY_KODE_POOL = "kode_pool"

        // 48 Hours session timeout
        private const val SESSION_DURATION_MS = 48L * 60L * 60L * 1000L

        // Sheet IDs from User
        const val DEFAULT_LAPORAN_HARIAN_SHEET_ID = "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30"
        const val DEFAULT_LAPORAN_HARIAN_SHEET_NAME = "LAPORAN KENDARAAN"
        const val DEFAULT_SERVICE_TEKNIK_SHEET_ID = "1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4"
        const val DEFAULT_SERVICE_TEKNIK_SHEET_NAME = "TEKNIK"
        const val DEFAULT_SERVICE_KEINDAHAN_SHEET_ID = "1KCULJS1G6r9jDdcuFmjGpNLV3-hw_gwWqyn0gEAv-o4"
        const val DEFAULT_SERVICE_KEINDAHAN_SHEET_NAME = "KEINDAHAN"
        const val DEFAULT_CLAIM_SHEET_ID = "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs"
        const val DEFAULT_CLAIM_SHEET_NAME = "MONITORING"
        const val DEFAULT_KENDARAAN_SHEET_ID = "13iuagC5JY1NOkVRMwebz8-XvaEp-2q_qiyaDedGedNQ"
        const val DEFAULT_KENDARAAN_SHEET_NAME = "MASTER"
        const val DEFAULT_KOPLING_SHEET_ID = "1qBtcaJP1adyawgZ2hbAIvlld-usZ3CV5YB6QSRpVxd4"
        const val DEFAULT_KOPLING_SHEET_NAME = "KOPLING"
        const val DEFAULT_BAN_SHEET_ID = "17_3zrlGea5ViRvzFvQhJyTWIreEZARwfcLk8AXwoVJo"
        const val DEFAULT_BAN_SHEET_NAME = "DATA_BAN"
        const val DEFAULT_BATERAY_SHEET_ID = "145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY"
        const val DEFAULT_BATERAY_SHEET_NAME = "ACCU"
        const val DEFAULT_ARGO_SHEET_ID = "13iuagC5JY1NOkVRMwebz8-XvaEp-2q_qiyaDedGedNQ"
        const val DEFAULT_ARGO_SHEET_NAME = "ARGO"
        const val DEFAULT_RESERVASI_SHEET_ID = "145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY"
        const val DEFAULT_RESERVASI_SHEET_NAME = "GOODMOVEN"
        const val DEFAULT_PENGERJAAN_TEKNIK_SHEET_ID = "145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY"
        const val DEFAULT_PENGERJAAN_TEKNIK_SHEET_NAME = "SKD"
        const val DEFAULT_RIWAYAT_SKI_SHEET_ID = "145VZWE2Zr-oiCxSRNLW8DwjtdclvrgsiRo4ZRMy8SgY"
        const val DEFAULT_RIWAYAT_SKI_SHEET_NAME = "SKI"
        const val DEFAULT_RIWAYAT_CLAIM_SHEET_ID = "1hyvU96v-ZQlpHO_B1IWhVAeJI3Txpu_Bwl3yNOMpbsE"
        const val DEFAULT_RIWAYAT_CLAIM_SHEET_NAME = "SECURITY"
        const val DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID = "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30"
        const val DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME = "SERVICE_KEINDAHAN"
        const val DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID = "1Mrvsc2aaC4Juzs6D4gsZnivdN54Ptgi6"
        const val DEFAULT_USER_SHEET_ID = "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30"
        const val DEFAULT_USER_SHEET_NAME = "USER"
        const val DEFAULT_PENGERJAAN_CAT_SHEET_ID = "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs"
        const val DEFAULT_PENGERJAAN_CAT_SHEET_NAME = "PENGERJAAN_CAT"
        const val DEFAULT_PREDIKSI_PART_SHEET_ID = "1C6EwcmIMXU5AajbKqpaZO79gpBwUKHfS-YqKLYrXw30"
        const val DEFAULT_PREDIKSI_PART_SHEET_NAME = "PREDIKSI_PART"

        // Web Apps Script untuk LAPORAN HARIAN & Modul Bengkel
        const val DEFAULT_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbwOAOqvX91d0fasOzjnCkmpQjM61Cw_4RgMyeNc8D-hnFmIoYqgPuuTrsVH5jn2RTvS/exec"
        const val DEFAULT_SCRIPT_URL_LAPORAN_HARIAN = "https://script.google.com/macros/s/AKfycbwOAOqvX91d0fasOzjnCkmpQjM61Cw_4RgMyeNc8D-hnFmIoYqgPuuTrsVH5jn2RTvS/exec"

        private const val KEY_SHEET_LAPORAN = "sheet_laporan"
        private const val KEY_SHEET_LAPORAN_NAME = "sheet_laporan_name"
        private const val KEY_SHEET_TEKNIK = "sheet_teknik"
        private const val KEY_SHEET_TEKNIK_NAME = "sheet_teknik_name"
        private const val KEY_SHEET_KEINDAHAN = "sheet_keindahan"
        private const val KEY_SHEET_KEINDAHAN_NAME = "sheet_keindahan_name"
        private const val KEY_SHEET_CAT = "sheet_cat"
        private const val KEY_SHEET_CAT_NAME = "sheet_cat_name"
        private const val KEY_SHEET_CLAIM = "sheet_claim"
        private const val KEY_SHEET_CLAIM_NAME = "sheet_claim_name"
        private const val KEY_SHEET_BAN = "sheet_ban"
        private const val KEY_SHEET_BAN_NAME = "sheet_ban_name"
        private const val KEY_SHEET_KOPLING = "sheet_kopling"
        private const val KEY_SHEET_KOPLING_NAME = "sheet_kopling_name"
        private const val KEY_SHEET_BATERAY = "sheet_bateray"
        private const val KEY_SHEET_BATERAY_NAME = "sheet_bateray_name"
        private const val KEY_SHEET_ARGO = "sheet_argo"
        private const val KEY_SHEET_ARGO_NAME = "sheet_argo_name"
        private const val KEY_SHEET_RESERVASI = "sheet_reservasi"
        private const val KEY_SHEET_RESERVASI_NAME = "sheet_reservasi_name"
        private const val KEY_SHEET_PENGERJAAN_TEKNIK = "sheet_pengerjaan_teknik"
        private const val KEY_SHEET_PENGERJAAN_TEKNIK_NAME = "sheet_pengerjaan_teknik_name"
        private const val KEY_SHEET_USER = "sheet_user"
        private const val KEY_SHEET_USER_NAME = "sheet_user_name"
        private const val KEY_SHEET_KENDARAAN = "sheet_kendaraan"
        private const val KEY_SHEET_KENDARAAN_NAME = "sheet_kendaraan_name"
        private const val KEY_SCRIPT_URL = "script_url"
        private const val KEY_LOGO_URL = "logo_url"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
    }

    fun saveSession(user: UserEntity) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            putInt(KEY_USER_ID, user.id)
            putString(KEY_NIP, user.nip)
            putString(KEY_NAMA_MEKANIK, user.namaMekanik)
            putString(KEY_USERNAME, user.username)
            putString(KEY_LEVEL, user.level)
            putString(KEY_KODE_POOL, user.kodePool)
            commit()
        }
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_LOGIN_TIMESTAMP)
            .remove(KEY_USER_ID)
            .remove(KEY_NIP)
            .remove(KEY_NAMA_MEKANIK)
            .remove(KEY_USERNAME)
            .remove(KEY_LEVEL)
            .remove(KEY_KODE_POOL)
            .commit()
    }

    /**
     * Sesi login bersifat permanen (tidak akan logout otomatis karena batas waktu).
     * User akan tetap login sampai menekan tombol Logout secara manual.
     */
    fun isSessionValid(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getCurrentUser(): UserEntity? {
        if (!isSessionValid()) return null
        val id = prefs.getInt(KEY_USER_ID, 0)
        val nip = prefs.getString(KEY_NIP, "") ?: ""
        val nama = prefs.getString(KEY_NAMA_MEKANIK, "") ?: ""
        val username = prefs.getString(KEY_USERNAME, "") ?: ""
        val level = prefs.getString(KEY_LEVEL, "SUPER ADMIN") ?: "SUPER ADMIN"
        val pool = prefs.getString(KEY_KODE_POOL, "BENGKEL-PUSAT") ?: "BENGKEL-PUSAT"

        if (nip.isBlank() && username.isBlank() && nama.isBlank()) return null

        return UserEntity(
            id = id,
            nip = nip,
            namaMekanik = nama,
            username = username,
            password = "",
            level = level,
            kodePool = pool
        )
    }

    fun getRemainingSessionHours(): Long {
        return if (isSessionValid()) Long.MAX_VALUE else 0L
    }

    // Google Sheets Configuration
    var sheetLaporanHarian: String
        get() {
            val v = prefs.getString(KEY_SHEET_LAPORAN, DEFAULT_LAPORAN_HARIAN_SHEET_ID) ?: DEFAULT_LAPORAN_HARIAN_SHEET_ID
            return if (v.isBlank() || v == "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs") DEFAULT_LAPORAN_HARIAN_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_LAPORAN, value).apply()

    var sheetLaporanHarianName: String
        get() {
            val v = prefs.getString(KEY_SHEET_LAPORAN_NAME, DEFAULT_LAPORAN_HARIAN_SHEET_NAME) ?: DEFAULT_LAPORAN_HARIAN_SHEET_NAME
            return if (v.isBlank() || v.equals("LAPORAN_HARIAN", ignoreCase = true) || v.equals("Form Responses 1", ignoreCase = true)) {
                DEFAULT_LAPORAN_HARIAN_SHEET_NAME
            } else {
                v
            }
        }
        set(value) = prefs.edit().putString(KEY_SHEET_LAPORAN_NAME, value).apply()

    var sheetServiceTeknik: String
        get() {
            val v = prefs.getString(KEY_SHEET_TEKNIK, DEFAULT_SERVICE_TEKNIK_SHEET_ID) ?: DEFAULT_SERVICE_TEKNIK_SHEET_ID
            return if (v.isBlank() || v == "1rAVsNj0YiNzZMTr9bbgEEwP1o_sOBmcZTDjHPAYIutY") DEFAULT_SERVICE_TEKNIK_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_TEKNIK, value).apply()

    var sheetServiceTeknikName: String
        get() {
            val v = prefs.getString(KEY_SHEET_TEKNIK_NAME, DEFAULT_SERVICE_TEKNIK_SHEET_NAME) ?: DEFAULT_SERVICE_TEKNIK_SHEET_NAME
            return if (v.isBlank() || v.equals("SERVICE_TEKNIK", ignoreCase = true)) DEFAULT_SERVICE_TEKNIK_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_TEKNIK_NAME, value).apply()

    var sheetServiceKeindahan: String
        get() {
            val v = prefs.getString(KEY_SHEET_KEINDAHAN, DEFAULT_SERVICE_KEINDAHAN_SHEET_ID) ?: DEFAULT_SERVICE_KEINDAHAN_SHEET_ID
            return if (v.isBlank() || v == "1rAVsNj0YiNzZMTr9bbgEEwP1o_sOBmcZTDjHPAYIutY") DEFAULT_SERVICE_KEINDAHAN_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_KEINDAHAN, value).apply()

    var sheetServiceKeindahanName: String
        get() {
            val v = prefs.getString(KEY_SHEET_KEINDAHAN_NAME, DEFAULT_SERVICE_KEINDAHAN_SHEET_NAME) ?: DEFAULT_SERVICE_KEINDAHAN_SHEET_NAME
            return if (v.isBlank() || v.equals("SERVICE_KEINDAHAN", ignoreCase = true)) DEFAULT_SERVICE_KEINDAHAN_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_KEINDAHAN_NAME, value).apply()

    var sheetPengerjaanCat: String
        get() {
            val v = prefs.getString(KEY_SHEET_CAT, DEFAULT_PENGERJAAN_CAT_SHEET_ID) ?: DEFAULT_PENGERJAAN_CAT_SHEET_ID
            return if (v.isBlank() || v == "1ECZDMv9OJGGvCedDncXCaVnI11xomu9wykzLXWM9m5U") DEFAULT_PENGERJAAN_CAT_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_CAT, value).apply()

    var sheetPengerjaanCatName: String
        get() {
            val v = prefs.getString(KEY_SHEET_CAT_NAME, DEFAULT_PENGERJAAN_CAT_SHEET_NAME) ?: DEFAULT_PENGERJAAN_CAT_SHEET_NAME
            return if (v.isBlank() || v == "Form Responses 1") DEFAULT_PENGERJAAN_CAT_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_CAT_NAME, value).apply()

    var sheetClaim: String
        get() {
            val v = prefs.getString(KEY_SHEET_CLAIM, DEFAULT_CLAIM_SHEET_ID) ?: DEFAULT_CLAIM_SHEET_ID
            return if (v.isBlank() || v == "1hyvU96v-ZQlpHO_B1IWhVAeJI3Txpu_Bwl3yNOMpbsE") DEFAULT_CLAIM_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_CLAIM, value).apply()

    var sheetClaimName: String
        get() {
            val v = prefs.getString(KEY_SHEET_CLAIM_NAME, DEFAULT_CLAIM_SHEET_NAME) ?: DEFAULT_CLAIM_SHEET_NAME
            return if (v.isBlank() || v.equals("SECURITY", ignoreCase = true) || v.equals("Form Responses 1", ignoreCase = true)) DEFAULT_CLAIM_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_CLAIM_NAME, value).apply()

    var sheetBan: String
        get() {
            val v = prefs.getString(KEY_SHEET_BAN, DEFAULT_BAN_SHEET_ID) ?: DEFAULT_BAN_SHEET_ID
            return if (v.isBlank() || v == "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU") DEFAULT_BAN_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_BAN, value).apply()

    var sheetBanName: String
        get() {
            val v = prefs.getString(KEY_SHEET_BAN_NAME, DEFAULT_BAN_SHEET_NAME) ?: DEFAULT_BAN_SHEET_NAME
            return if (v.isBlank() || v.equals("BAN", ignoreCase = true) || v.equals("DATA BAN", ignoreCase = true)) DEFAULT_BAN_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_BAN_NAME, value).apply()

    var sheetKopling: String
        get() {
            val v = prefs.getString(KEY_SHEET_KOPLING, DEFAULT_KOPLING_SHEET_ID) ?: DEFAULT_KOPLING_SHEET_ID
            return if (v.isBlank() || v == "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU") DEFAULT_KOPLING_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_KOPLING, value).apply()

    var sheetKoplingName: String
        get() {
            val v = prefs.getString(KEY_SHEET_KOPLING_NAME, DEFAULT_KOPLING_SHEET_NAME) ?: DEFAULT_KOPLING_SHEET_NAME
            return if (v.isBlank() || v.equals("Form Responses 1", ignoreCase = true)) DEFAULT_KOPLING_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_KOPLING_NAME, value).apply()

    var sheetBateray: String
        get() {
            val v = prefs.getString(KEY_SHEET_BATERAY, DEFAULT_BATERAY_SHEET_ID) ?: DEFAULT_BATERAY_SHEET_ID
            return if (v.isBlank() || v == "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU") DEFAULT_BATERAY_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_BATERAY, value).apply()

    var sheetBaterayName: String
        get() {
            val v = prefs.getString(KEY_SHEET_BATERAY_NAME, DEFAULT_BATERAY_SHEET_NAME) ?: DEFAULT_BATERAY_SHEET_NAME
            return if (v.isBlank()) DEFAULT_BATERAY_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_BATERAY_NAME, value).apply()

    var isBaterayUpdatedToNewId: Boolean
        get() = prefs.getBoolean("bateray_updated_to_new_id_v2", false)
        set(value) = prefs.edit().putBoolean("bateray_updated_to_new_id_v2", value).apply()

    var sheetArgo: String
        get() {
            val v = prefs.getString(KEY_SHEET_ARGO, DEFAULT_ARGO_SHEET_ID) ?: DEFAULT_ARGO_SHEET_ID
            return if (v.isBlank() || v == "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU") DEFAULT_ARGO_SHEET_ID else v
        }
        set(value) {
            val finalVal = if (value.isBlank() || value == "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU") DEFAULT_ARGO_SHEET_ID else value
            prefs.edit().putString(KEY_SHEET_ARGO, finalVal).apply()
        }

    var sheetArgoName: String
        get() {
            val v = prefs.getString(KEY_SHEET_ARGO_NAME, DEFAULT_ARGO_SHEET_NAME) ?: DEFAULT_ARGO_SHEET_NAME
            return if (v.isBlank()) DEFAULT_ARGO_SHEET_NAME else v
        }
        set(value) {
            val finalVal = if (value.isBlank()) DEFAULT_ARGO_SHEET_NAME else value
            prefs.edit().putString(KEY_SHEET_ARGO_NAME, finalVal).apply()
        }

    var isArgoUpdatedToNewId: Boolean
        get() = prefs.getBoolean("argo_updated_to_new_id_v2", false)
        set(value) = prefs.edit().putBoolean("argo_updated_to_new_id_v2", value).apply()

    var sheetPrediksiPart: String
        get() {
            val v = prefs.getString("sheet_prediksi_part", DEFAULT_PREDIKSI_PART_SHEET_ID) ?: DEFAULT_PREDIKSI_PART_SHEET_ID
            return if (v.isBlank() || v == "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs") DEFAULT_PREDIKSI_PART_SHEET_ID else v
        }
        set(value) = prefs.edit().putString("sheet_prediksi_part", value).apply()

    var sheetPrediksiPartName: String
        get() {
            val v = prefs.getString("sheet_prediksi_part_name", DEFAULT_PREDIKSI_PART_SHEET_NAME) ?: DEFAULT_PREDIKSI_PART_SHEET_NAME
            return if (v.isBlank()) DEFAULT_PREDIKSI_PART_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString("sheet_prediksi_part_name", value).apply()

    var sheetReservasi: String
        get() {
            val v = prefs.getString(KEY_SHEET_RESERVASI, DEFAULT_RESERVASI_SHEET_ID) ?: DEFAULT_RESERVASI_SHEET_ID
            return if (v.isBlank() || v == "1tG5Q_uh6rWyiUWvwJYAxai-whpIyuDosgpWjVpUP3PY") DEFAULT_RESERVASI_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_RESERVASI, value).apply()

    var sheetReservasiName: String
        get() {
            val v = prefs.getString(KEY_SHEET_RESERVASI_NAME, DEFAULT_RESERVASI_SHEET_NAME) ?: DEFAULT_RESERVASI_SHEET_NAME
            return if (v.isBlank() || v.equals("RESERVASI", ignoreCase = true) || v.equals("GM", ignoreCase = true)) DEFAULT_RESERVASI_SHEET_NAME else v
        }
        set(value) {
            val target = if (value.isBlank() || value.equals("RESERVASI", ignoreCase = true) || value.equals("GM", ignoreCase = true)) {
                DEFAULT_RESERVASI_SHEET_NAME
            } else {
                value
            }
            prefs.edit().putString(KEY_SHEET_RESERVASI_NAME, target).apply()
        }

    var isReservasiGoodmovenUpdated: Boolean
        get() = prefs.getBoolean("reservasi_goodmoven_updated_v1", false)
        set(value) = prefs.edit().putBoolean("reservasi_goodmoven_updated_v1", value).apply()

    var isReservasiGmSheetCleared: Boolean
        get() = prefs.getBoolean("reservasi_gm_sheet_cleared_v1", false)
        set(value) = prefs.edit().putBoolean("reservasi_gm_sheet_cleared_v1", value).apply()

    var isReservasiAndKomponenCleared: Boolean
        get() = prefs.getBoolean("reservasi_and_komponen_cleared_v2", false)
        set(value) = prefs.edit().putBoolean("reservasi_and_komponen_cleared_v2", value).apply()

    var sheetPengerjaanTeknik: String
        get() {
            val v = prefs.getString(KEY_SHEET_PENGERJAAN_TEKNIK, DEFAULT_PENGERJAAN_TEKNIK_SHEET_ID) ?: DEFAULT_PENGERJAAN_TEKNIK_SHEET_ID
            return if (v.isBlank() || v == "1tG5Q_uh6rWyiUWvwJYAxai-whpIyuDosgpWjVpUP3PY") DEFAULT_PENGERJAAN_TEKNIK_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_PENGERJAAN_TEKNIK, value).apply()

    var sheetPengerjaanTeknikName: String
        get() {
            val v = prefs.getString(KEY_SHEET_PENGERJAAN_TEKNIK_NAME, DEFAULT_PENGERJAAN_TEKNIK_SHEET_NAME) ?: DEFAULT_PENGERJAAN_TEKNIK_SHEET_NAME
            return if (v.isBlank() || v.equals("SKD", ignoreCase = true)) DEFAULT_PENGERJAAN_TEKNIK_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_PENGERJAAN_TEKNIK_NAME, value).apply()

    var sheetRiwayatSki: String
        get() {
            val v = prefs.getString("sheet_riwayat_ski", DEFAULT_RIWAYAT_SKI_SHEET_ID) ?: DEFAULT_RIWAYAT_SKI_SHEET_ID
            return if (v.isBlank() || v == "1tG5Q_uh6rWyiUWvwJYAxai-whpIyuDosgpWjVpUP3PY") DEFAULT_RIWAYAT_SKI_SHEET_ID else v
        }
        set(value) = prefs.edit().putString("sheet_riwayat_ski", value).apply()

    var sheetRiwayatSkiName: String
        get() {
            val v = prefs.getString("sheet_riwayat_ski_name", DEFAULT_RIWAYAT_SKI_SHEET_NAME) ?: DEFAULT_RIWAYAT_SKI_SHEET_NAME
            return if (v.isBlank()) DEFAULT_RIWAYAT_SKI_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString("sheet_riwayat_ski_name", value).apply()

    var isRiwayatSkiUpdatedToNewId: Boolean
        get() = prefs.getBoolean("riwayat_ski_updated_to_new_id_v2", false)
        set(value) = prefs.edit().putBoolean("riwayat_ski_updated_to_new_id_v2", value).apply()

    var sheetRiwayatClaim: String
        get() {
            val v = prefs.getString("sheet_riwayat_claim", DEFAULT_RIWAYAT_CLAIM_SHEET_ID) ?: DEFAULT_RIWAYAT_CLAIM_SHEET_ID
            return if (v.isBlank()) DEFAULT_RIWAYAT_CLAIM_SHEET_ID else v
        }
        set(value) = prefs.edit().putString("sheet_riwayat_claim", value).apply()

    var sheetRiwayatClaimName: String
        get() {
            val v = prefs.getString("sheet_riwayat_claim_name", DEFAULT_RIWAYAT_CLAIM_SHEET_NAME) ?: DEFAULT_RIWAYAT_CLAIM_SHEET_NAME
            return if (v.isBlank()) DEFAULT_RIWAYAT_CLAIM_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString("sheet_riwayat_claim_name", value).apply()

    var sheetPekerjaanServiceKeindahan: String
        get() {
            val v = prefs.getString("sheet_pekerjaan_service_keindahan", DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID) ?: DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID
            return if (v.isBlank() || v == "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs") DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID else v
        }
        set(value) {
            val finalVal = if (value.isBlank() || value == "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs") {
                DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_ID
            } else {
                value
            }
            prefs.edit().putString("sheet_pekerjaan_service_keindahan", finalVal).apply()
        }

    var sheetPekerjaanServiceKeindahanName: String
        get() {
            val v = prefs.getString("sheet_pekerjaan_service_keindahan_name", DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME) ?: DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME
            return if (v.isBlank()) DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME else v
        }
        set(value) {
            val finalVal = if (value.isBlank()) DEFAULT_PEKERJAAN_SERVICE_KEINDAHAN_SHEET_NAME else value
            prefs.edit().putString("sheet_pekerjaan_service_keindahan_name", finalVal).apply()
        }

    var isPekerjaanKeindahanUpdatedToNewId: Boolean
        get() = prefs.getBoolean("pekerjaan_keindahan_updated_to_new_id_v2", false)
        set(value) = prefs.edit().putBoolean("pekerjaan_keindahan_updated_to_new_id_v2", value).apply()

    var folderPhotoKeindahanId: String
        get() {
            val v = prefs.getString("folder_photo_keindahan_id", DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID) ?: DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID
            return if (v.isBlank() || v == "1Exy7AawdwoV5byqucwVBPIbTxI6anAjk") DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID else v
        }
        set(value) {
            val finalVal = if (value.isBlank() || value == "1Exy7AawdwoV5byqucwVBPIbTxI6anAjk") DEFAULT_FOLDER_PHOTO_KEINDAHAN_ID else value
            prefs.edit().putString("folder_photo_keindahan_id", finalVal).apply()
        }

    var isFolderPhotoUpdatedV2: Boolean
        get() = prefs.getBoolean("folder_photo_updated_v2", false)
        set(value) = prefs.edit().putBoolean("folder_photo_updated_v2", value).apply()

    var sheetUser: String
        get() {
            val v = prefs.getString(KEY_SHEET_USER, DEFAULT_USER_SHEET_ID)?.trim() ?: DEFAULT_USER_SHEET_ID
            return if (v.isBlank() || v.length < 15 || v == "1DDFhwatqnl6lyNOc12GZ-eQnvbuCz89YOtqjMjY8TDs") DEFAULT_USER_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_USER, value.trim()).apply()

    var sheetUserName: String
        get() {
            val v = prefs.getString(KEY_SHEET_USER_NAME, DEFAULT_USER_SHEET_NAME)?.trim() ?: DEFAULT_USER_SHEET_NAME
            return if (v.isBlank()) DEFAULT_USER_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_USER_NAME, value.trim()).apply()

    var sheetKendaraan: String
        get() {
            val v = prefs.getString(KEY_SHEET_KENDARAAN, DEFAULT_KENDARAAN_SHEET_ID) ?: DEFAULT_KENDARAAN_SHEET_ID
            return if (v.isBlank() || v == "1Wvf1I0xSGSUI7DsZesodJqhrn1O8KbG9k0J5fdPV_VU") DEFAULT_KENDARAAN_SHEET_ID else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_KENDARAAN, value).apply()

    var sheetKendaraanName: String
        get() {
            val v = prefs.getString(KEY_SHEET_KENDARAAN_NAME, DEFAULT_KENDARAAN_SHEET_NAME) ?: DEFAULT_KENDARAAN_SHEET_NAME
            return if (v.isBlank() || v == "MASTER_KENDARAAN") DEFAULT_KENDARAAN_SHEET_NAME else v
        }
        set(value) = prefs.edit().putString(KEY_SHEET_KENDARAAN_NAME, value).apply()

    var scriptUrl: String
        get() {
            val v = prefs.getString(KEY_SCRIPT_URL, DEFAULT_SCRIPT_URL) ?: DEFAULT_SCRIPT_URL
            return if (v.isBlank() ||
                v.contains("AKfycbxyIaBKU1W3U4_DWIzbfPYnhc07xR-YBrwuPd4YGV_JCFEioR_iRx8ID9Hr0TOs5heM") ||
                v.contains("AKfycbyS9B9yCUSfx8Cr41NXjRJ1_HClMvt210dQGbCrV3dO0m_8oT4H6bKfpefxXYh9GtS94w") ||
                v.contains("AKfycbwjfZGwauVXpisbx7T8UPioBTJP802LJvKmvcD5e3smdSuuBOL2T3A_cOYtz2JxM4FZfg")
            ) {
                DEFAULT_SCRIPT_URL
            } else {
                v
            }
        }
        set(value) = prefs.edit().putString(KEY_SCRIPT_URL, value).apply()

    var scriptUrlLaporanHarian: String
        get() {
            val v = prefs.getString("script_url_laporan_harian", DEFAULT_SCRIPT_URL_LAPORAN_HARIAN) ?: DEFAULT_SCRIPT_URL_LAPORAN_HARIAN
            return if (v.isBlank() ||
                v.contains("AKfycbxyIaBKU1W3U4_DWIzbfPYnhc07xR-YBrwuPd4YGV_JCFEioR_iRx8ID9Hr0TOs5heM") ||
                v.contains("AKfycbyS9B9yCUSfx8Cr41NXjRJ1_HClMvt210dQGbCrV3dO0m_8oT4H6bKfpefxXYh9GtS94w") ||
                v.contains("AKfycbwjfZGwauVXpisbx7T8UPioBTJP802LJvKmvcD5e3smdSuuBOL2T3A_cOYtz2JxM4FZfg")
            ) {
                DEFAULT_SCRIPT_URL_LAPORAN_HARIAN
            } else {
                v
            }
        }
        set(value) = prefs.edit().putString("script_url_laporan_harian", value).apply()

    var isScriptUrlUpdatedV3: Boolean
        get() = prefs.getBoolean("script_url_updated_v3", false)
        set(value) = prefs.edit().putBoolean("script_url_updated_v3", value).apply()

    var logoUrl: String
        get() = prefs.getString(KEY_LOGO_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LOGO_URL, value).apply()

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC, value).apply()

    // Backward compatibility properties
    val sheetId: String get() = sheetLaporanHarian
    val driveFolderId: String get() = ""
    val bigQueryDataset: String get() = ""
    val isAutoSyncEnabled: Boolean get() = true
}
