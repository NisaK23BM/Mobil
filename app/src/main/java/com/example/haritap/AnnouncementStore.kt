package com.example.haritap

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Announcement(val id: Long, val title: String, val message: String, val time: Long)

object AnnouncementStore {
    private const val PREFS = "ann_prefs"
    private const val KEY = "ann_list"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun add(ctx: Context, title: String, message: String) {
        val arr = JSONArray(prefs(ctx).getString(KEY, "[]"))
        val id = System.currentTimeMillis()
        val obj = JSONObject().apply {
            put("id", id)
            put("title", title)
            put("message", message)
            put("time", id)
        }
        arr.put(obj)
        prefs(ctx).edit().putString(KEY, arr.toString()).apply()
    }

    fun getAll(ctx: Context): List<Announcement> {
        val arr = JSONArray(prefs(ctx).getString(KEY, "[]"))
        val out = mutableListOf<Announcement>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Announcement(
                    o.getLong("id"),
                    o.getString("title"),
                    o.getString("message"),
                    o.getLong("time")
                )
            )
        }
        return out.sortedByDescending { it.time }
    }

    fun delete(ctx: Context, id: Long) {
        val arr = JSONArray(prefs(ctx).getString(KEY, "[]"))
        val newArr = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getLong("id") != id) newArr.put(o)
        }
        prefs(ctx).edit().putString(KEY, newArr.toString()).apply()
    }

    fun latest(ctx: Context): Announcement? = getAll(ctx).firstOrNull()
}
