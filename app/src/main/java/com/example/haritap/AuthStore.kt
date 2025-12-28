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
    private const val PREFS = "auth_prefs"
    private const val KEY_USERS = "users_json"
    private const val KEY_CURRENT = "current_email"
    private val gson = Gson()

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isLoggedIn(ctx: Context): Boolean = !prefs(ctx).getString(KEY_CURRENT, null).isNullOrBlank()
    fun currentEmail(ctx: Context): String? = prefs(ctx).getString(KEY_CURRENT, null)

    fun currentUser(ctx: Context): UserAccount? {
        val email = currentEmail(ctx) ?: return null
        return getUsers(ctx).firstOrNull { it.email.equals(email, true) }
    }

    fun logout(ctx: Context) {
        prefs(ctx).edit().remove(KEY_CURRENT).apply()
    }

    fun hasAnyUser(ctx: Context): Boolean = getUsers(ctx).isNotEmpty()

    fun login(ctx: Context, email: String, password: String): Boolean {
        val u = getUsers(ctx).firstOrNull { it.email.equals(email, true) } ?: return false
        if (u.password != password) return false
        prefs(ctx).edit().putString(KEY_CURRENT, u.email).apply()
        // Profil ekranında görünsün diye kaydet:
        UserPrefs.setProfile(ctx, u.name, u.email, u.role, u.department)
        return true
    }

    fun register(ctx: Context, user: UserAccount): Pair<Boolean, String> {
        val list = getUsers(ctx).toMutableList()
        if (list.any { it.email.equals(user.email, true) }) {
            return false to "Bu e-posta zaten kayıtlı"
        }
        list.add(user)
        saveUsers(ctx, list)
        // kayıt sonrası otomatik giriş:
        prefs(ctx).edit().putString(KEY_CURRENT, user.email).apply()
        UserPrefs.setProfile(ctx, user.name, user.email, user.role, user.department)
        return true to "Kayıt başarılı"
    }

    fun resetPassword(ctx: Context, email: String, newPass: String): Pair<Boolean, String> {
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
        return try { gson.fromJson(json, type) } catch (_: Exception) { emptyList() }
    }

    private fun saveUsers(ctx: Context, list: List<UserAccount>) {
        prefs(ctx).edit().putString(KEY_USERS, gson.toJson(list)).apply()
    }
}
