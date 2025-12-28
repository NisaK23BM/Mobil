package com.example.haritap

import android.content.Context

object UserPrefs {
    private const val PREFS = "haritap_prefs"

    private const val KEY_NAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_ROLE = "role"
    private const val KEY_DEPT = "dept"
    private const val KEY_PROFILE_DONE = "profile_done"

    private const val KEY_FOLLOWED_IDS = "followed_ids" // String Set

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isProfileDone(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_PROFILE_DONE, false)

    fun setProfile(ctx: Context, name: String, email: String, role: String, dept: String) {
        prefs(ctx).edit()
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_ROLE, role)
            .putString(KEY_DEPT, dept)
            .putBoolean(KEY_PROFILE_DONE, true)
            .apply()
    }

    fun getName(ctx: Context): String = prefs(ctx).getString(KEY_NAME, "Ad Soyad") ?: "Ad Soyad"
    fun getEmail(ctx: Context): String = prefs(ctx).getString(KEY_EMAIL, "email@edu.tr") ?: "email@edu.tr"
    fun getRole(ctx: Context): String = prefs(ctx).getString(KEY_ROLE, "User") ?: "User"
    fun getDept(ctx: Context): String = prefs(ctx).getString(KEY_DEPT, "Birim") ?: "Birim"

    fun clearProfile(ctx: Context) {
        prefs(ctx).edit()
            .remove(KEY_NAME).remove(KEY_EMAIL).remove(KEY_ROLE).remove(KEY_DEPT)
            .remove(KEY_PROFILE_DONE)
            .remove(KEY_FOLLOWED_IDS)
            .apply()
    }

    // ---- Takip ----
    fun getFollowedIds(ctx: Context): Set<String> =
        prefs(ctx).getStringSet(KEY_FOLLOWED_IDS, emptySet()) ?: emptySet()

    fun isFollowed(ctx: Context, reportId: Long): Boolean =
        getFollowedIds(ctx).contains(reportId.toString())

    fun toggleFollow(ctx: Context, reportId: Long): Boolean {
        val set = getFollowedIds(ctx).toMutableSet()
        val key = reportId.toString()
        val nowFollowed = if (set.contains(key)) { set.remove(key); false } else { set.add(key); true }
        prefs(ctx).edit().putStringSet(KEY_FOLLOWED_IDS, set).apply()
        return nowFollowed
    }

    fun removeFollow(ctx: Context, reportId: Long) {
        val set = getFollowedIds(ctx).toMutableSet()
        set.remove(reportId.toString())
        prefs(ctx).edit().putStringSet(KEY_FOLLOWED_IDS, set).apply()
    }
}
