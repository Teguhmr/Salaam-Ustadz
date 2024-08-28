package id.uinjkt.salaamustadz.utils


enum class TuntunanIbadahFeatures(val position: Int, val title: String) {
    PRAYER(0, "Shalat"),
    QURAN(1, "Quran"),
    DZIKIR(2, "Dzikir"),
    DOA(3, "Doa"),
    ZAKAT(4, "Zakat");
    companion object {
        fun getPosition(position: Int): TuntunanIbadahFeatures {
            return values().find { it.position.toString().contains(position.toString(), true) }.orDefault(PRAYER)
        }
    }
}

enum class ChatAvailability(val status: Int, val title: String) {
    PENDING(0, "Menunggu"),
    ON_GOING(1, "Berlangsung"),
    COMPLETED(2, "Selesai"),
    ALL(3, "Semua");
    companion object {
        fun getType(type: Int?): ChatAvailability {
            return values().find { it.status.toString().contains(type.toString(), true) }.orDefault(PENDING)
        }
        fun getTitle(type: String?): ChatAvailability {
            return values().find { it.title.contains(type.toString(), true) }.orDefault(PENDING)
        }
    }
}

enum class MyNotification(val position: Int, val title: String) {
    ARTICLE(0, "Artikel"),
    DZIKIR(1, "Dzikir"),
    PRAYER_TIME(2, "Jadwal Shalat");
    companion object {
        fun getType(type: String): MyNotification {
            return values().find { it.title.contains(type, true) }.orDefault(ARTICLE)
        }
    }
}

enum class UstadzType(val role: String, val title: String) {
    USTADZ("ustadz", "Ustadz"),
    USTADZAH("ustadzah", "Ustadzah");
    companion object {
        fun getType(type: String): UstadzType {
            return values().find { it.role.contains(type, true) }.orDefault(USTADZ)
        }
    }
}
enum class ConfirmType(val type: String) {
    CONFIRM_SEND_REVIEW("confirmSendReview"),
    CONFIRM_SUBMIT_GENDER("confirmSubmitGender"),
    CONFIRM_SIGN_OUT("confirmSignOut"),
    CONFIRM_UPDATE_APP("latestVersionCodeSalaamUstadz");
    companion object {
        fun getType(type: String): ConfirmType {
            return values().find { it.type.contains(type, true) }.orDefault(CONFIRM_SEND_REVIEW)
        }
    }
}
enum class AccountSetting(val position: Int, val title: String) {
    PROFILE(0, "Profil Saya"),
    CHANGE_PASSWORD(1, "Ubah Kata Sandi"),
    NOTIFICATION(2, "Pengaturan Notifikasi");
    companion object {
        fun getPosition(position: Int): AccountSetting {
            return values().find { it.position.toString().contains(position.toString(), true) }.orDefault(PROFILE)
        }
    }
}
enum class AccountSettingAdmin(val position: Int, val title: String) {
    PROFILE_ADMIN(0, "Profil Saya"),
    CHANGE_PASSWORD_ADMIN(1, "Ubah Kata Sandi"),
    ADD_USER(2, "Tambah User (Ustadz/Ustadzah)"),
    NOTIFICATION_ADMIN(3, "Pengaturan Notifikasi");
    companion object {
        fun getPosition(position: Int): AccountSettingAdmin {
            return values().find { it.position.toString().contains(position.toString(), true) }.orDefault(PROFILE_ADMIN)
        }
    }
}
enum class AboutApp(val position: Int, val title: String) {
    ABOUT_US(0, "Tentang Kami"),
    HELP(1, "Bantuan"),
    PRIVACY_POLICY(2, "Kebijakan Privasi"),
    LIKE(3, "Nilai Kami"),
    CALL_US(4, "Hubungi Kami");
    companion object {
        fun getPosition(position: Int): AboutApp {
            return values().find { it.position.toString().contains(position.toString(), true) }.orDefault(ABOUT_US)
        }
    }
}
enum class NotificationType(val position: Int, val title: String) {
    NOTIFICATION_VIBRATE(0, "Notifikasi dengan Getar"),
    ONLY_NOTIFICATION(1, "Hanya Notifikasi");
    companion object {
        fun getType(type: String): NotificationType {
            return values().find { it.title.contains(type, true) }.orDefault(NOTIFICATION_VIBRATE)
        }
    }
}

enum class SuccessType(val type: String) {
    SEND_ARTICLE("Send"),
    SEND_CONSULT_REVIEW("ConsultReview"),
    UPDATE_ARTICLE("UpdateArticle"),
    UPDATE_PROFILE("UpdateProfile"),
    CHANGE_PASSWORD("ChangePassword");
    companion object {
        fun getType(type: String): SuccessType {
            return values().find { it.type.contains(type, true) }.orDefault(SEND_ARTICLE)
        }
    }
}
enum class NotificationTimeRemaining(val position: Int, val title: String) {
    MINUTES_30(0, "30"),
    MINUTES_15(1, "15"),
    MINUTES_5(2, "5"),
    MINUTES_1(3, "1");
    companion object {
        fun getPosition(position: Int): NotificationTimeRemaining {
            return values().find { it.position.toString().contains(position.toString(), true) }.orDefault(MINUTES_15)
        }
    }
}
enum class HijriMonth(val position: Int, val title: String) {
    MUHARRAM(1, "Muharram"),
    SHAFAR(2, "Shafar"),
    RABIUL_AWWAL(3, "Rabiul Awwal"),
    RABIUL_AKHIR(4, "Rabiul Akhir"),
    JUMADIL_AWWAL(5, "Jumadil Awwal"),
    JUMADIL_AKHIR(6, "Jumadil Akhir"),
    RAJAB(7, "Rajab"),
    SYABAN(8, "Sya'ban"),
    RAMADHAN(9, "Ramadhan"),
    SYAWWAL(10, "Syawwal"),
    DZULQODAH(11, "Dzulqo'dah"),
    DZULHIJJAH(12, "Dzulhijjah");
    companion object {
        fun getNumber(position: Int): HijriMonth {
            return values().find { it.position.toString().contains(position.toString(), true) }.orDefault(MUHARRAM)
        }
    }
}

enum class DzikirType(val type: String, val title: String) {
    DZIKIR_PAGI("DZIKIR_PAGI", "Dzikir Pagi"),
    DZIKIR_PETANG("DZIKIR_PETANG", "Dzikir Petang");

    companion object {
        fun getType(type: String): DzikirType {
            return values().find { it.type.contains(type, true) }.orDefault(DZIKIR_PAGI)
        }
    }
}