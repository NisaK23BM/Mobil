package com.example.haritap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.haritap.ui.AnnouncementAdapter

class AnnouncementsActivity : AppCompatActivity() {

    private val isAdmin: Boolean by lazy { UserPrefs.getRole(this) == "Admin" }
    private lateinit var rv: RecyclerView
    private lateinit var adapter: AnnouncementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_announcements)

        rv = findViewById(R.id.rvAnnouncements)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = AnnouncementAdapter(
            items = emptyList(),
            isAdmin = isAdmin,
            onDelete = { a ->
                AnnouncementStore.delete(this, a.id)
                load()
            }
        )
        rv.adapter = adapter

        load()
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        adapter.submit(AnnouncementStore.getAll(this))
    }
}
