package com.example.haritap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.haritap.data.AppDatabase
import com.example.haritap.ui.ReportAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val openMapBtn = findViewById<Button>(R.id.openMapBtn)
        val openNewReportBtn = findViewById<Button>(R.id.openNewReportBtn)
        val profileButton = findViewById<Button>(R.id.profileButton)

        openMapBtn.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        openNewReportBtn.setOnClickListener {
            val intent = Intent(this, NewReportActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        val rvReports = findViewById<RecyclerView>(R.id.rvReports)

        val adapter = ReportAdapter(emptyList()) { report ->
            Toast.makeText(this, report.title, Toast.LENGTH_SHORT).show()
        }

        rvReports.layoutManager = LinearLayoutManager(this)
        rvReports.adapter = adapter

        lifecycleScope.launch(Dispatchers.IO) {
            val list = AppDatabase.getInstance(this@MainActivity)
                .reportDao()
                .getAll()

            withContext(Dispatchers.Main) {
                adapter.submit(list)
            }
        }
    }
}
