package com.example.haritap

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class UserAccount(
    val name: String,
    val email: String,
    var password: String,
    val department: String,
    val role: String // "User" veya "Admin"
)

object AuthStore {
    private const val ADMIN_EMAIL = "admin"
    private const val ADMIN_PASS = "admin"

    private const val PREFS = "auth_prefs"
    private const val KEY_USERS = "users_json"
    private const val KEY_CURRENT = "current_email"
    private val gson = Gson()

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isLoggedIn(ctx: Context): Boolean =
        !prefs(ctx).getString(KEY_CURRENT, null).isNullOrBlank()

    fun currentEmail(ctx: Context): String? =
        prefs(ctx).getString(KEY_CURRENT, null)

    fun currentUser(ctx: Context): UserAccount? {
        val email = currentEmail(ctx) ?: return null

        // Admin özel kullanıcı (kayıttan bağımsız)
        if (email == ADMIN_EMAIL) {
            return UserAccount(
                name = "Admin",
                email = ADMIN_EMAIL,
                password = ADMIN_PASS,
                department = "Yönetim",
                role = "Admin"
            )
        }

        return getUsers(ctx).firstOrNull { it.email.equals(email, true) }
    }

    fun logout(ctx: Context) {
        prefs(ctx).edit().remove(KEY_CURRENT).apply()
    }

    fun hasAnyUser(ctx: Context): Boolean = getUsers(ctx).isNotEmpty()

    fun login(ctx: Context, email: String, password: String): Boolean {
        // ✅ ADMIN GİRİŞİ (tek admin: admin/admin)
        if (email == ADMIN_EMAIL && password == ADMIN_PASS) {
            prefs(ctx).edit().putString(KEY_CURRENT, ADMIN_EMAIL).apply()
            // Profil ekranında gözüksün
            UserPrefs.setProfile(ctx, "Admin", ADMIN_EMAIL, "Admin", "Yönetim")
            return true
        }

        // Normal kullanıcı girişi
        val u = getUsers(ctx).firstOrNull { it.email.equals(email, true) } ?: return false
        if (u.password != password) return false

        prefs(ctx).edit().putString(KEY_CURRENT, u.email).apply()
        UserPrefs.setProfile(ctx, u.name, u.email, u.role, u.department)
        return true
    }

    fun register(ctx: Context, user: UserAccount): Pair<Boolean, String> {
        // admin email ile kayıt olamasın
        if (user.email.equals(ADMIN_EMAIL, true)) {
            return false to "Bu e-posta kullanılamaz"
        }

        val list = getUsers(ctx).toMutableList()
        if (list.any { it.email.equals(user.email, true) }) {
            return false to "Bu e-posta zaten kayıtlı"
        }

        // Role her zaman User olsun (kayıttan Admin oluşmasın)
        val safeUser = user.copy(role = "User")

        list.add(safeUser)
        saveUsers(ctx, list)

        // kayıt sonrası otomatik giriş
        prefs(ctx).edit().putString(KEY_CURRENT, safeUser.email).apply()
        UserPrefs.setProfile(ctx, safeUser.name, safeUser.email, safeUser.role, safeUser.department)

        return true to "Kayıt başarılı"
    }

    fun resetPassword(ctx: Context, email: String, newPass: String): Pair<Boolean, String> {
        // admin şifresi resetlenmesin (istersen kaldırabilirsin)
        if (email.equals(ADMIN_EMAIL, true)) {
            return false to "Admin şifresi değiştirilemez"
        }

        val list = getUsers(ctx).toMutableList()
        val idx = list.indexOfFirst { it.email.equals(email, true) }
        if (idx == -1) return false to "Bu e-posta bulunamadı"

        list[idx].password = newPass
        saveUsers(ctx, list)
        return true to "Şifre güncellendi"
    }

    private fun getUsers(ctx: Context): List<UserAccount> {
        val json = prefs(ctx).getString(KEY_USERS, null) ?: return emptyList()
        val type = object : TypeToken<List<UserAccount>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveUsers(ctx: Context, list: List<UserAccount>) {
        prefs(ctx).edit().putString(KEY_USERS, gson.toJson(list)).apply()
    }
}
