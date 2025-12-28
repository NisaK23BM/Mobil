package com.example.haritap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.haritap.data.AppDatabase
import com.example.haritap.ui.ReportAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FollowedReportsActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_followed_reports)

        rv = findViewById(R.id.rvFollowed)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = ReportAdapter(emptyList()) { report ->
            val i = Intent(this, ReportDetailActivity::class.java)
            i.putExtra("reportId", report.id)
            startActivity(i)
        }

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadFollowed()
    }

    private fun loadFollowed() {
        val ids = UserPrefs.getFollowedIds(this).mapNotNull { it.toLongOrNull() }
        if (ids.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            adapter.submit(emptyList())
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getInstance(this@FollowedReportsActivity).reportDao()
            val list = dao.getByIds(ids)

            withContext(Dispatchers.Main) {
                tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                adapter.submit(list)
            }
        }
    }
}
